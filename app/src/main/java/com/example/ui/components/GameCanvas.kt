package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.core.GamePhase
import com.example.core.MissionType
import com.example.data.ArenaMapId
import com.example.data.EnemyArchetype
import com.example.data.WeaponId
import com.example.game.ArenaBounds
import com.example.game.GameEngine
import com.example.ui.theme.AcidLime
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HotMagenta
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarGold
import com.example.ui.theme.TankCrimson
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * GPU Hardware-Accelerated SurfaceView game renderer hosted in Compose via AndroidView.
 * Uses [SurfaceHolder.lockHardwareCanvas] (API 26+) + single-pass GPU BitmapShader floor +
 * pre-baked sprite/bloom atlas for locked 60 FPS silky gameplay.
 */
@Composable
fun GameCanvas(
    engine: GameEngine,
    isPlaying: Boolean,
    onFrameLocked: (dtSeconds: Float, nowNanos: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            ArenaSurfaceView(context, engine, onFrameLocked)
        },
        update = { view ->
            view.setPlaying(isPlaying)
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("game_arena_canvas")
    )
}

@SuppressLint("ViewConstructor")
private class ArenaSurfaceView(
    context: Context,
    private val engine: GameEngine,
    private val onFrameLocked: (dtSeconds: Float, nowNanos: Long) -> Unit
) : SurfaceView(context), SurfaceHolder.Callback {

    @Volatile
    private var surfaceReady = false

    @Volatile
    private var playingFlag = false

    @Volatile
    private var threadRunning = false

    @Volatile
    private var needsPausedRedraw = true

    @Volatile
    private var useHardwareCanvas = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    private var renderThread: Thread? = null
    private var activePointerId = MotionEvent.INVALID_POINTER_ID

    // Crisp 720px short-side GPU buffer: consistent arena framing across all phones & fast fill-rate!
    @Volatile
    private var bufferW: Int = 720

    @Volatile
    private var bufferH: Int = 1520

    private val atlas = ArenaSpriteAtlas()

    // Single-pass GPU tiled floor shaders — 1 distinct world texture per Arena Map!
    private val floorShaderCyber = BitmapShader(atlas.floorTile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    private val floorShaderVolcano = BitmapShader(atlas.floorTileVolcano, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    private val floorShaderCosmic = BitmapShader(atlas.floorTileCosmic, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    private val floorShaderFrost = BitmapShader(atlas.floorTileFrost, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    private val floorShaderToxic = BitmapShader(atlas.floorTileToxic, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    private val floorShaderSolar = BitmapShader(atlas.floorTileSolar, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    private var activeFloorMapId: ArenaMapId = ArenaMapId.CYBER_CITY
    private val floorMatrix = Matrix()
    private val floorShaderPaint = Paint().apply {
        shader = floorShaderCyber
        isFilterBitmap = false
        isDither = false
    }

    // Pre-allocated Paints (zero per-frame allocations)
    private val bgArgb = ObsidianVoid.toArgb()
    private val cyanArgb = ElectricCyan.toArgb()
    private val magentaArgb = HotMagenta.toArgb()
    private val limeArgb = AcidLime.toArgb()
    private val goldArgb = SolarGold.toArgb()
    private val dangerArgb = DangerRed.toArgb()
    private val tankArgb = TankCrimson.toArgb()

    private val bitmapPaint = Paint().apply {
        isFilterBitmap = true
        isDither = false
    }
    private val glowPaint = Paint().apply {
        isFilterBitmap = true
        alpha = 185
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val fastFillPaint = Paint().apply { style = Paint.Style.FILL; isAntiAlias = false }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val dmgFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize = 23f
    }
    private val dmgStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4.2f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        color = 0xDD050510.toInt()
        textSize = 23f
    }

    // Reusable zero-GC drawing buffers
    private val playerPath = Path()
    private val arrowPath = Path()
    private val arcRect = RectF()
    private val orbitPos = FloatArray(2)

    // Silky 10-point zero-GC player thruster trail ring buffer
    private val trailSize = 10
    private val trailX = FloatArray(trailSize)
    private val trailY = FloatArray(trailSize)
    private var trailHead = 0
    private var trailCount = 0
    private var trailSampleCounter = 0

    // Zero-GC parallax drifting neon atmosphere motes (18 pre-allocated primitives)
    private val ambientCount = 18
    private val ambientBaseX = FloatArray(ambientCount) { i -> ((i * 193 + 57) % 1000) / 1000f }
    private val ambientBaseY = FloatArray(ambientCount) { i -> ((i * 311 + 89) % 1000) / 1000f }
    private val ambientSpeed = FloatArray(ambientCount) { i -> 18f + (i % 5) * 9f }
    private val ambientRadius = FloatArray(ambientCount) { i -> 2.4f + (i % 4) * 1.15f }
    private val ambientKind = IntArray(ambientCount) { i -> i % 4 }

    init {
        isFocusable = false
        holder.setFormat(PixelFormat.RGBX_8888)
        holder.addCallback(this)
    }

    fun setPlaying(playing: Boolean) {
        if (playingFlag != playing) {
            playingFlag = playing
            needsPausedRedraw = true
            if (playing && engine.elapsedSeconds < 0.2f) {
                trailCount = 0
                trailHead = 0
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceReady = true
        configureScaledSurface(width, height)
        startRenderThread()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (width > 0 && height > 0) {
            val shortSide = minOf(width, height).toFloat()
            if (shortSide > 760f) {
                val scale = 720f / shortSide
                val targetW = ((width * scale).toInt() and 0x7FFFFFFE).coerceAtLeast(480)
                val targetH = ((height * scale).toInt() and 0x7FFFFFFE).coerceAtLeast(480)
                if (targetW != width || targetH != height) {
                    bufferW = targetW
                    bufferH = targetH
                    engine.viewportHalfWidth = targetW * 0.5f
                    engine.viewportHalfHeight = targetH * 0.5f
                    holder.setFixedSize(targetW, targetH)
                    return
                }
            }
            bufferW = width
            bufferH = height
            engine.viewportHalfWidth = width * 0.5f
            engine.viewportHalfHeight = height * 0.5f
            needsPausedRedraw = true
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surfaceReady = false
        stopRenderThread()
    }

    private fun configureScaledSurface(viewW: Int, viewH: Int) {
        if (viewW <= 0 || viewH <= 0) return
        val shortSide = minOf(viewW, viewH).toFloat()
        val scale = if (shortSide > 760f) 720f / shortSide else 1f
        val targetW = ((viewW * scale).toInt() and 0x7FFFFFFE).coerceAtLeast(480)
        val targetH = ((viewH * scale).toInt() and 0x7FFFFFFE).coerceAtLeast(480)
        bufferW = targetW
        bufferH = targetH
        engine.viewportHalfWidth = targetW * 0.5f
        engine.viewportHalfHeight = targetH * 0.5f
        holder.setFixedSize(targetW, targetH)
    }

    private fun startRenderThread() {
        if (threadRunning) return
        threadRunning = true
        needsPausedRedraw = true
        renderThread = Thread({
            runGameLoop()
        }, "SynvexGpuRenderThread").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    private fun stopRenderThread() {
        threadRunning = false
        val t = renderThread
        renderThread = null
        if (t != null) {
            t.interrupt()
            try {
                t.join(250)
            } catch (_: InterruptedException) {
            }
        }
    }

    private fun runGameLoop() {
        val targetFrameNanos = 16_200_000L // Slightly tighter than 16.67ms so VSync stays locked at 60 FPS!
        var lastTickNanos = System.nanoTime()

        while (threadRunning && surfaceReady) {
            val frameStartNanos = System.nanoTime()

            if (!playingFlag) {
                if (needsPausedRedraw) {
                    needsPausedRedraw = false
                    renderSingleFrame(stepSim = false, dt = 0f, nowNanos = frameStartNanos)
                }
                lastTickNanos = System.nanoTime()
                try {
                    Thread.sleep(32)
                } catch (_: InterruptedException) {
                    break
                }
                continue
            }

            val dt = ((frameStartNanos - lastTickNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
            lastTickNanos = frameStartNanos

            renderSingleFrame(stepSim = true, dt = dt, nowNanos = frameStartNanos)

            val elapsedNanos = System.nanoTime() - frameStartNanos
            val remainingNanos = targetFrameNanos - elapsedNanos
            if (remainingNanos > 2_500_000L) {
                val sleepNanos = remainingNanos - 1_200_000L
                try {
                    Thread.sleep(sleepNanos / 1_000_000L, (sleepNanos % 1_000_000L).toInt())
                } catch (_: InterruptedException) {
                    break
                }
            }
        }
    }

    private fun lockBestCanvas(): Canvas? {
        if (useHardwareCanvas && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return try {
                holder.lockHardwareCanvas()
            } catch (_: Throwable) {
                useHardwareCanvas = false
                try {
                    holder.lockCanvas()
                } catch (_: Throwable) {
                    null
                }
            }
        }
        return try {
            holder.lockCanvas()
        } catch (_: Throwable) {
            null
        }
    }

    private fun renderSingleFrame(stepSim: Boolean, dt: Float, nowNanos: Long) {
        if (!surfaceReady) return
        var canvas: Canvas? = null
        try {
            canvas = lockBestCanvas()
            if (canvas != null) {
                synchronized(engine.simLock) {
                    if (stepSim) {
                        onFrameLocked(dt, nowNanos)
                        recordPlayerTrail()
                    }
                    drawArenaLocked(canvas)
                }
            }
        } catch (_: Throwable) {
        } finally {
            if (canvas != null) {
                try {
                    holder.unlockCanvasAndPost(canvas)
                } catch (_: Throwable) {
                }
            }
        }
    }

    private fun recordPlayerTrail() {
        trailSampleCounter++
        if (engine.player.isDashing || trailSampleCounter % 2 == 0) {
            trailX[trailHead] = engine.player.x
            trailY[trailHead] = engine.player.y
            trailHead = (trailHead + 1) % trailSize
            if (trailCount < trailSize) trailCount++
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (engine.phase != GamePhase.PLAYING) {
            synchronized(engine.simLock) {
                if (engine.player.joystick.isActive) engine.player.joystick.onUp()
            }
            activePointerId = MotionEvent.INVALID_POINTER_ID
            return false
        }

        val viewW = width.coerceAtLeast(1).toFloat()
        val viewH = height.coerceAtLeast(1).toFloat()
        val scaleX = bufferW.toFloat() / viewW
        val scaleY = bufferH.toFloat() / viewH

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val idx = event.actionIndex
                activePointerId = event.getPointerId(idx)
                val sx = event.getX(idx) * scaleX
                val sy = event.getY(idx) * scaleY
                val maxJoyRad = (minOf(bufferW, bufferH) * 0.16f).coerceIn(82f, 136f)
                synchronized(engine.simLock) {
                    engine.player.joystick.onDown(sx, sy, maxJoyRad)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId != MotionEvent.INVALID_POINTER_ID) {
                    val idx = event.findPointerIndex(activePointerId)
                    if (idx >= 0) {
                        val sx = event.getX(idx) * scaleX
                        val sy = event.getY(idx) * scaleY
                        synchronized(engine.simLock) {
                            engine.player.joystick.onDrag(sx, sy)
                        }
                    }
                }
                return true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val idx = event.actionIndex
                if (event.getPointerId(idx) == activePointerId) {
                    synchronized(engine.simLock) {
                        engine.player.joystick.onUp()
                    }
                    activePointerId = MotionEvent.INVALID_POINTER_ID
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                synchronized(engine.simLock) {
                    engine.player.joystick.onUp()
                }
                activePointerId = MotionEvent.INVALID_POINTER_ID
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun drawArenaLocked(canvas: Canvas) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        if (w <= 0f || h <= 0f) return

        val centerX = w * 0.5f
        val centerY = h * 0.5f

        val shake = engine.screenShakeMagnitude
        val shakeX = if (shake > 0.2f) (Random.nextFloat() * 2f - 1f) * shake else 0f
        val shakeY = if (shake > 0.2f) (Random.nextFloat() * 2f - 1f) * shake else 0f

        val camX = engine.cameraX + shakeX
        val camY = engine.cameraY + shakeY
        val worldToScreenX = centerX - camX
        val worldToScreenY = centerY - camY

        val beatProgress = engine.beatManager.currentBeatProgress
        val beatFlash = (1f - beatProgress).let { it * it }
        val elapsedSec = engine.elapsedSeconds
        val stageAccentArgb = engine.currentStage.accentColor.toArgb()

        // 1. Single-Pass GPU BitmapShader Map Floor + Stage Atmosphere Wash
        val currentMapId = engine.currentStage.mapId
        val activeShader = when (currentMapId) {
            ArenaMapId.CYBER_CITY -> floorShaderCyber
            ArenaMapId.PLASMA_VOLCANO -> floorShaderVolcano
            ArenaMapId.COSMIC_VOID -> floorShaderCosmic
            ArenaMapId.FROST_CORE -> floorShaderFrost
            ArenaMapId.TOXIC_LAB -> floorShaderToxic
            ArenaMapId.SOLAR_EMPIRE -> floorShaderSolar
        }
        if (activeFloorMapId != currentMapId) {
            activeFloorMapId = currentMapId
            floorShaderPaint.shader = activeShader
        }
        floorMatrix.setTranslate(worldToScreenX, worldToScreenY)
        activeShader.setLocalMatrix(floorMatrix)
        canvas.drawRect(0f, 0f, w, h, floorShaderPaint)

        val pulseAlpha = 10 + (beatFlash * 16).toInt()
        fastFillPaint.color = PlayerAvatarRenderer.withAlpha(stageAccentArgb, pulseAlpha)
        canvas.drawRect(0f, 0f, w, h, fastFillPaint)

        // 1b. Parallax Drifting Neon Cyber Motes (zero-GC, makes the arena feel alive!)
        drawAmbientArenaMotes(canvas, w, h, camX, camY, elapsedSec, beatFlash, stageAccentArgb)

        // 2. Enclosed Electric Neon Arena Walls + 4 Pre-baked 3D Metallic Cyber Pillars
        drawArenaWallsAndPillars(canvas, w, h, worldToScreenX, worldToScreenY, stageAccentArgb, beatFlash)

        // 3. Active Mission Capture Beacon Zone
        val ms = engine.missionSystem
        if (ms.hasActiveMission && ms.activeType == MissionType.CAPTURE_BEACON) {
            drawCaptureBeacon(
                canvas,
                ms.captureX + worldToScreenX,
                ms.captureY + worldToScreenY,
                ms.captureRadius,
                ms.captureHoldSeconds / ms.captureTargetSeconds,
                ms.isPlayerInBeacon,
                beatFlash
            )
        }

        // 4. Telegraphed Red Orbital Hazard Strike Zones & Sweeping Death Laser Beams
        drawHazardZones(canvas, worldToScreenX, worldToScreenY)
        drawLaserBeams(canvas, worldToScreenX, worldToScreenY, beatFlash)

        val playerScreenX = engine.player.x + worldToScreenX
        val playerScreenY = engine.player.y + worldToScreenY

        // 5. Soft Bloom Ambient Glow under Player + Magnet Field Ring
        glowPaint.alpha = if (engine.player.isDashing) 235 else (115 + (beatFlash * 55).toInt())
        val pGlow = if (engine.player.isNovaReady) atlas.glowGold else atlas.glowCyan
        canvas.drawBitmap(pGlow, playerScreenX - pGlow.width * 0.5f, playerScreenY - pGlow.height * 0.5f, glowPaint)

        strokePaint.color = PlayerAvatarRenderer.withAlpha(limeArgb, 28 + (beatFlash * 20).toInt())
        strokePaint.strokeWidth = 1.8f
        canvas.drawCircle(playerScreenX, playerScreenY, engine.player.magnetRadius, strokePaint)

        // 6. Expanding Neon Shockwave Rings
        engine.pool.shockwaves.forEachActive { sw ->
            val t = sw.progress
            val inv = 1f - t
            val eased = 1f - inv * inv * inv
            val r = sw.startRadius + (sw.maxRadius - sw.startRadius) * eased
            val baseColor = sw.color.toArgb()
            val sx = sw.x + worldToScreenX
            val sy = sw.y + worldToScreenY

            strokePaint.color = PlayerAvatarRenderer.withAlpha(baseColor, (inv * 82f).toInt())
            strokePaint.strokeWidth = if (sw.isSuper) 15f else 9.5f
            canvas.drawCircle(sx, sy, r, strokePaint)

            strokePaint.color = PlayerAvatarRenderer.withAlpha(baseColor, (inv * 245f).toInt())
            strokePaint.strokeWidth = if (sw.isSuper) 4.8f else 3.2f
            canvas.drawCircle(sx, sy, r, strokePaint)
        }

        // 7. Bass Mines & Black-Hole Vortices
        engine.pool.bassMines.forEachActive { mine ->
            val mx = mine.x + worldToScreenX
            val my = mine.y + worldToScreenY
            val col = if (mine.isBlackHole) goldArgb else tankArgb
            val pulse = 0.85f + beatFlash * 0.25f

            val mg = if (mine.isBlackHole) atlas.glowGold else atlas.glowRed
            glowPaint.alpha = 135
            canvas.drawBitmap(mg, mx - mg.width * 0.5f, my - mg.height * 0.5f, glowPaint)

            strokePaint.color = PlayerAvatarRenderer.withAlpha(col, 165)
            strokePaint.strokeWidth = 2.4f
            canvas.drawCircle(mx, my, mine.radius * 0.36f * pulse, strokePaint)
            fillPaint.color = if (mine.isBlackHole) AndroidColor.BLACK else col
            canvas.drawCircle(mx, my, 10f * pulse, fillPaint)
            fillPaint.color = AndroidColor.WHITE
            canvas.drawCircle(mx, my, 4.2f, fillPaint)
        }

        // 8. Treble Slash Arcs
        engine.pool.slashArcs.forEachActive { sl ->
            val t = (sl.elapsed / sl.duration).coerceIn(0f, 1f)
            val inv = 1f - t
            val sx = sl.x + worldToScreenX
            val sy = sl.y + worldToScreenY
            val r = sl.radius * (0.85f + t * 0.2f)
            val col = sl.color.toArgb()

            if (sl.isFullCircle) {
                strokePaint.color = PlayerAvatarRenderer.withAlpha(col, (inv * 125f).toInt())
                strokePaint.strokeWidth = 13f
                canvas.drawCircle(sx, sy, r, strokePaint)
                strokePaint.color = PlayerAvatarRenderer.withAlpha(AndroidColor.WHITE, (inv * 235f).toInt())
                strokePaint.strokeWidth = 3.5f
                canvas.drawCircle(sx, sy, r, strokePaint)
            } else {
                val startDeg = Math.toDegrees(sl.angleRad.toDouble()).toFloat() - sl.sweepDeg * 0.5f
                arcRect.set(sx - r, sy - r, sx + r, sy + r)
                strokePaint.color = PlayerAvatarRenderer.withAlpha(col, (inv * 130f).toInt())
                strokePaint.strokeWidth = 14f
                canvas.drawArc(arcRect, startDeg, sl.sweepDeg, false, strokePaint)
                strokePaint.color = PlayerAvatarRenderer.withAlpha(AndroidColor.WHITE, (inv * 240f).toInt())
                strokePaint.strokeWidth = 3.8f
                canvas.drawArc(arcRect, startDeg, sl.sweepDeg, false, strokePaint)
            }
        }

        // 9. XP Orbs & Golden Coins
        engine.pool.rhythmOrbs.forEachActive { orb ->
            val ox = orb.x + worldToScreenX
            val oy = orb.y + worldToScreenY
            if (ox < -24f || ox > w + 24f || oy < -24f || oy > h + 24f) return@forEachActive
            val bmp = when {
                orb.isCoinDrop -> atlas.coinSprite
                orb.tier == 2 -> atlas.orbGoldSprite
                orb.tier == 1 -> atlas.orbLimeSprite
                else -> atlas.orbCyanSprite
            }
            canvas.drawBitmap(bmp, ox - bmp.width * 0.5f, oy - bmp.height * 0.5f, bitmapPaint)
        }

        // 10. High-Detail Multi-Layer Sci-Fi Enemies
        val hpBgColor = PlayerAvatarRenderer.withAlpha(bgArgb, 225)
        engine.pool.enemies.forEachActive { enemy ->
            val ex = enemy.x + worldToScreenX
            val ey = enemy.y + worldToScreenY
            if (ex < -60f || ex > w + 60f || ey < -60f || ey > h + 60f) return@forEachActive

            val flashing = enemy.hitFlashTimer > 0f
            val bmp = when (enemy.archetype) {
                EnemyArchetype.SWARM -> if (flashing) atlas.swarmFlashSprite else {
                    val dir = (((enemy.rotation * 1.2732395f) + 8.5f).toInt() and 7)
                    atlas.swarmSprites[dir]
                }
                EnemyArchetype.RUNNER -> if (flashing) atlas.runnerFlashSprite else {
                    val dir = (((enemy.rotation * 1.2732395f) + 8.5f).toInt() and 7)
                    atlas.runnerSprites[dir]
                }
                EnemyArchetype.TANK -> if (flashing) atlas.tankFlashSprite else atlas.tankSprite
                EnemyArchetype.ELITE_BOSS -> if (flashing) atlas.eliteFlashSprite else atlas.eliteSprite
                EnemyArchetype.CORRUPTED_CORE -> if (flashing) atlas.coreFlashSprite else atlas.coreSprite
            }
            canvas.drawBitmap(bmp, ex - bmp.width * 0.5f, ey - bmp.height * 0.5f, bitmapPaint)

            if (enemy.archetype == EnemyArchetype.CORRUPTED_CORE) {
                strokePaint.color = PlayerAvatarRenderer.withAlpha(dangerArgb, 145 + (beatFlash * 100).toInt())
                strokePaint.strokeWidth = 2.6f
                canvas.drawCircle(ex, ey, enemy.radius + 8f + beatFlash * 6f, strokePaint)
            }

            if (enemy.hp < enemy.maxHp || enemy.archetype == EnemyArchetype.ELITE_BOSS || enemy.archetype == EnemyArchetype.CORRUPTED_CORE) {
                val r = enemy.radius * 0.96f
                val barW = r * 2.0f
                val barH = if (enemy.archetype == EnemyArchetype.ELITE_BOSS || enemy.archetype == EnemyArchetype.CORRUPTED_CORE) 6f else 3.8f
                val left = ex - barW * 0.5f
                val top = ey - r - 11f
                val ratio = (enemy.hp / enemy.maxHp).coerceIn(0f, 1f)
                fastFillPaint.color = hpBgColor
                canvas.drawRect(left - 1f, top - 1f, left + barW + 1f, top + barH + 1f, fastFillPaint)
                fastFillPaint.color = when (enemy.archetype) {
                    EnemyArchetype.ELITE_BOSS -> goldArgb
                    EnemyArchetype.CORRUPTED_CORE -> dangerArgb
                    else -> magentaArgb
                }
                canvas.drawRect(left, top, left + barW * ratio, top + barH, fastFillPaint)
            }
        }

        // 11. Synth Orbit Blades
        val orbitLv = engine.player.weaponLevels[WeaponId.SYNTH_ORBIT] ?: 0
        if (orbitLv > 0) {
            val evolved = engine.player.evolvedWeapons.contains(WeaponId.SYNTH_ORBIT)
            val count = if (evolved) 6 else (1 + orbitLv)
            val orbitRadius = (92f + if (evolved) 26f else 0f) * engine.player.areaMultiplier
            val bladeBmp = if (evolved) atlas.bladeGoldSprite else atlas.bladeMagentaSprite
            val halfW = bladeBmp.width * 0.5f
            val halfH = bladeBmp.height * 0.5f

            strokePaint.color = PlayerAvatarRenderer.withAlpha(if (evolved) goldArgb else magentaArgb, 48)
            strokePaint.strokeWidth = 1.8f
            canvas.drawCircle(playerScreenX, playerScreenY, orbitRadius, strokePaint)

            for (b in 0 until count) {
                engine.player.orbitBladePosition(b, count, orbitRadius, orbitPos)
                canvas.drawBitmap(
                    bladeBmp,
                    orbitPos[0] + worldToScreenX - halfW,
                    orbitPos[1] + worldToScreenY - halfH,
                    bitmapPaint
                )
            }
        }

        // 12. Laser Arp Projectiles
        engine.pool.projectiles.forEachActive { proj ->
            val px = proj.x + worldToScreenX
            val py = proj.y + worldToScreenY
            if (px < -28f || px > w + 28f || py < -28f || py > h + 28f) return@forEachActive
            val spd = hypot(proj.vx, proj.vy).coerceAtLeast(1f)
            val tailLen = if (proj.isSuper) 26f else 19f
            val tx = px - (proj.vx / spd) * tailLen
            val ty = py - (proj.vy / spd) * tailLen

            linePaint.color = PlayerAvatarRenderer.withAlpha(proj.color.toArgb(), 170)
            linePaint.strokeWidth = proj.radius * 1.55f
            canvas.drawLine(tx, ty, px, py, linePaint)

            linePaint.color = AndroidColor.WHITE
            linePaint.strokeWidth = proj.radius * 0.58f
            canvas.drawLine(tx * 0.35f + px * 0.65f, ty * 0.35f + py * 0.65f, px, py, linePaint)
        }

        // 13. Chain Lightning Arcs
        engine.pool.lightningArcs.forEachActive { arc ->
            val inv = (1f - (arc.elapsed / arc.duration)).coerceIn(0f, 1f)
            val x1 = arc.x1 + worldToScreenX
            val y1 = arc.y1 + worldToScreenY
            val x2 = arc.x2 + worldToScreenX
            val y2 = arc.y2 + worldToScreenY
            val midX = (x1 + x2) * 0.5f + (y2 - y1) * 0.14f
            val midY = (y1 + y2) * 0.5f - (x2 - x1) * 0.14f

            linePaint.color = PlayerAvatarRenderer.withAlpha(arc.color.toArgb(), (inv * 165f).toInt())
            linePaint.strokeWidth = if (arc.isSuper) 8.5f else 6f
            canvas.drawLine(x1, y1, midX, midY, linePaint)
            canvas.drawLine(midX, midY, x2, y2, linePaint)

            linePaint.color = PlayerAvatarRenderer.withAlpha(AndroidColor.WHITE, (inv * 250f).toInt())
            linePaint.strokeWidth = 2.6f
            canvas.drawLine(x1, y1, midX, midY, linePaint)
            canvas.drawLine(midX, midY, x2, y2, linePaint)
        }

        // 14. Silky Player Neon Thruster Trail + Dash Shield + Customized Ship Avatar
        val profile = engine.profile
        val primaryArgb = if (engine.player.hitFlashTimer > 0f || engine.player.isDashing) {
            AndroidColor.WHITE
        } else {
            PlayerAvatarRenderer.resolvePrimaryArgb(profile.equippedColor, elapsedSec)
        }
        val secondaryArgb = PlayerAvatarRenderer.resolveSecondaryArgb(profile.equippedColor, elapsedSec)
        val pRadius = (engine.player.radius * 0.96f) * engine.player.pulseScale

        drawPlayerTrail(canvas, worldToScreenX, worldToScreenY, if (engine.player.isDashing) cyanArgb else primaryArgb, engine.player.isDashing)

        PlayerAvatarRenderer.drawAura(
            canvas = canvas,
            cx = playerScreenX,
            cy = playerScreenY,
            radius = pRadius,
            auraId = profile.equippedAura,
            primaryArgb = primaryArgb,
            secondaryArgb = secondaryArgb,
            beatFlash = beatFlash,
            timeSeconds = elapsedSec,
            fillPaint = fillPaint,
            strokePaint = strokePaint
        )

        if (engine.player.isDashing) {
            strokePaint.color = PlayerAvatarRenderer.withAlpha(cyanArgb, 230)
            strokePaint.strokeWidth = 5f
            canvas.drawCircle(playerScreenX, playerScreenY, pRadius * 1.75f, strokePaint)
        }

        // Electric Combo Hype Aura: orbiting golden/magenta sparks when combo >= 15!
        val activeCombo = engine.player.combo
        if (activeCombo >= 15) {
            drawComboHypeSparks(canvas, playerScreenX, playerScreenY, pRadius, activeCombo, elapsedSec, beatFlash)
        }

        val hpRatio = (engine.player.hp / engine.player.maxHp).coerceIn(0f, 1f)
        val hpRad = pRadius * 1.46f
        arcRect.set(playerScreenX - hpRad, playerScreenY - hpRad, playerScreenX + hpRad, playerScreenY + hpRad)
        strokePaint.color = 0x33FFFFFF
        strokePaint.strokeWidth = 3.2f
        canvas.drawCircle(playerScreenX, playerScreenY, hpRad, strokePaint)
        strokePaint.color = if (hpRatio > 0.35f) limeArgb else dangerArgb
        strokePaint.strokeWidth = 4f
        canvas.drawArc(arcRect, -90f, 360f * hpRatio, false, strokePaint)

        PlayerAvatarRenderer.drawShape(
            canvas = canvas,
            path = playerPath,
            cx = playerScreenX,
            cy = playerScreenY,
            radius = pRadius,
            facingAngle = engine.player.facingAngle,
            shapeId = profile.equippedShape,
            fillArgb = primaryArgb,
            accentArgb = secondaryArgb,
            fillPaint = fillPaint,
            strokePaint = strokePaint
        )

        // 15. Enemy Crimson Plasma Bullets
        engine.pool.enemyBullets.forEachActive { b ->
            val bx = b.x + worldToScreenX
            val by = b.y + worldToScreenY
            if (bx < -24f || bx > w + 24f || by < -24f || by > h + 24f) return@forEachActive
            val outerCol = if (b.fromBoss) goldArgb else dangerArgb
            fillPaint.color = PlayerAvatarRenderer.withAlpha(outerCol, 105)
            canvas.drawCircle(bx, by, b.radius * 1.75f, fillPaint)
            fillPaint.color = dangerArgb
            canvas.drawCircle(bx, by, b.radius, fillPaint)
            fillPaint.color = AndroidColor.WHITE
            canvas.drawCircle(bx, by, b.radius * 0.48f, fillPaint)
        }

        // 16. Directional Mission Compass Arrow around Player
        if (ms.hasTargetMarker && engine.phase == GamePhase.PLAYING) {
            drawMissionTargetArrow(
                canvas = canvas,
                playerScreenX = playerScreenX,
                playerScreenY = playerScreenY,
                targetScreenX = ms.targetMarkerX + worldToScreenX,
                targetScreenY = ms.targetMarkerY + worldToScreenY,
                colorArgb = when (ms.activeType) {
                    MissionType.CAPTURE_BEACON -> limeArgb
                    MissionType.DESTROY_CORES -> dangerArgb
                    MissionType.HUNT_BOSS -> goldArgb
                    else -> cyanArgb
                },
                beatFlash = beatFlash
            )
        }

        // 17. Glowing Hit Spark Particles
        engine.pool.particles.forEachActive { pt ->
            val px = pt.x + worldToScreenX
            val py = pt.y + worldToScreenY
            if (px < -12f || px > w + 12f || py < -12f || py > h + 12f) return@forEachActive
            val inv = (1f - (pt.elapsed / pt.duration)).coerceIn(0f, 1f)
            val s = pt.size * 0.85f * (0.55f + inv * 0.45f)
            fillPaint.color = PlayerAvatarRenderer.withAlpha(pt.color.toArgb(), (inv * 245f).toInt())
            canvas.drawCircle(px, py, s, fillPaint)
        }

        // 18. Crisp Dark-Outlined Damage Popups (capped at 14)
        var drawnPopups = 0
        engine.pool.damagePopups.forEachActive { pop ->
            if (drawnPopups >= 14) return@forEachActive
            val px = pop.x + worldToScreenX
            val py = pop.y + worldToScreenY
            if (px < -24f || px > w + 24f || py < -24f || py > h + 24f) return@forEachActive
            val inv = (1f - (pop.elapsed / pop.duration)).coerceIn(0f, 1f)
            val alpha = (inv * 255f).toInt()
            val sz = if (pop.isCrit) 26f else 20f
            dmgStrokePaint.textSize = sz
            dmgStrokePaint.alpha = alpha
            dmgFillPaint.textSize = sz
            dmgFillPaint.color = PlayerAvatarRenderer.withAlpha(pop.color.toArgb(), alpha)
            canvas.drawText(pop.cachedText, px, py, dmgStrokePaint)
            canvas.drawText(pop.cachedText, px, py, dmgFillPaint)
            drawnPopups++
        }

        // 19. Dynamic Floating Virtual Joystick
        val joy = engine.player.joystick
        if (joy.isActive && engine.phase == GamePhase.PLAYING) {
            val ox = joy.originX
            val oy = joy.originY
            val kx = joy.currentX
            val ky = joy.currentY
            val r = joy.maxRadiusPx

            fillPaint.color = PlayerAvatarRenderer.withAlpha(primaryArgb, 28)
            canvas.drawCircle(ox, oy, r, fillPaint)
            strokePaint.color = PlayerAvatarRenderer.withAlpha(primaryArgb, 145)
            strokePaint.strokeWidth = 2.8f
            canvas.drawCircle(ox, oy, r, strokePaint)

            linePaint.color = PlayerAvatarRenderer.withAlpha(primaryArgb, 165)
            linePaint.strokeWidth = 3.6f
            canvas.drawLine(ox, oy, kx, ky, linePaint)

            fillPaint.color = PlayerAvatarRenderer.withAlpha(primaryArgb, 88)
            canvas.drawCircle(kx, ky, 30f, fillPaint)
            fillPaint.color = primaryArgb
            canvas.drawCircle(kx, ky, 18f, fillPaint)
            fillPaint.color = AndroidColor.WHITE
            canvas.drawCircle(kx, ky, 7.5f, fillPaint)
        }
    }

    private fun drawPlayerTrail(
        canvas: Canvas,
        worldToScreenX: Float,
        worldToScreenY: Float,
        primaryArgb: Int,
        isDashing: Boolean
    ) {
        if (trailCount < 2) return
        val maxAlpha = if (isDashing) 175f else 95f
        val extraRad = if (isDashing) 4.5f else 0f
        for (i in 0 until trailCount) {
            val idx = (trailHead - trailCount + i + trailSize) % trailSize
            val frac = (i + 1f) / trailCount
            val sx = trailX[idx] + worldToScreenX
            val sy = trailY[idx] + worldToScreenY
            fillPaint.color = PlayerAvatarRenderer.withAlpha(primaryArgb, (frac * maxAlpha).toInt())
            canvas.drawCircle(sx, sy, 4f + frac * (9f + extraRad), fillPaint)
        }
    }

    private fun drawAmbientArenaMotes(
        canvas: Canvas,
        w: Float,
        h: Float,
        camX: Float,
        camY: Float,
        elapsedSec: Float,
        beatFlash: Float,
        stageAccentArgb: Int
    ) {
        val parallaxX = -camX * 0.32f
        val parallaxY = -camY * 0.32f
        val glowAlpha = 36 + (beatFlash * 32).toInt()
        val coreAlpha = 115 + (beatFlash * 65).toInt()

        for (i in 0 until ambientCount) {
            val rawX = ambientBaseX[i] * w + parallaxX + sin(elapsedSec * 0.9f + i) * 14f
            val rawY = ambientBaseY[i] * h + parallaxY - elapsedSec * ambientSpeed[i]
            val sx = ((rawX % w) + w) % w
            val sy = ((rawY % h) + h) % h
            val col = when (ambientKind[i]) {
                0 -> stageAccentArgb
                1 -> cyanArgb
                2 -> magentaArgb
                else -> goldArgb
            }
            val r = ambientRadius[i]
            fillPaint.color = PlayerAvatarRenderer.withAlpha(col, glowAlpha)
            canvas.drawCircle(sx, sy, r * 2.8f, fillPaint)
            fillPaint.color = PlayerAvatarRenderer.withAlpha(col, coreAlpha)
            canvas.drawCircle(sx, sy, r, fillPaint)
        }
    }

    private fun drawComboHypeSparks(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        pRadius: Float,
        combo: Int,
        elapsedSec: Float,
        beatFlash: Float
    ) {
        val sparkCount = if (combo >= 30) 6 else 4
        val hypeColor = if (combo >= 30) goldArgb else magentaArgb
        val orbitR = pRadius * (1.88f + beatFlash * 0.16f)
        val baseAngle = elapsedSec * (if (combo >= 30) 4.4f else 3.2f)

        strokePaint.color = PlayerAvatarRenderer.withAlpha(hypeColor, 55 + (beatFlash * 65).toInt())
        strokePaint.strokeWidth = 2.2f
        canvas.drawCircle(cx, cy, orbitR, strokePaint)

        val step = 6.2831855f / sparkCount
        for (i in 0 until sparkCount) {
            val a = baseAngle + i * step
            val sx = cx + cos(a) * orbitR
            val sy = cy + sin(a) * orbitR
            fillPaint.color = PlayerAvatarRenderer.withAlpha(hypeColor, 150)
            canvas.drawCircle(sx, sy, 6.2f, fillPaint)
            fillPaint.color = AndroidColor.WHITE
            canvas.drawCircle(sx, sy, 2.7f, fillPaint)
        }
    }

    private fun drawArenaWallsAndPillars(
        canvas: Canvas,
        w: Float,
        h: Float,
        worldToScreenX: Float,
        worldToScreenY: Float,
        stageAccentArgb: Int,
        beatFlash: Float
    ) {
        val left = -ArenaBounds.HALF_WIDTH + worldToScreenX
        val right = ArenaBounds.HALF_WIDTH + worldToScreenX
        val top = -ArenaBounds.HALF_HEIGHT + worldToScreenY
        val bottom = ArenaBounds.HALF_HEIGHT + worldToScreenY

        fastFillPaint.color = 0xD4140514.toInt()
        if (top > 0f) canvas.drawRect(0f, 0f, w, top.coerceAtMost(h), fastFillPaint)
        if (bottom < h) canvas.drawRect(0f, bottom.coerceAtLeast(0f), w, h, fastFillPaint)
        if (left > 0f) canvas.drawRect(0f, top.coerceAtLeast(0f), left.coerceAtMost(w), bottom.coerceAtMost(h), fastFillPaint)
        if (right < w) canvas.drawRect(right.coerceAtLeast(0f), top.coerceAtLeast(0f), w, bottom.coerceAtMost(h), fastFillPaint)

        val shockBoost = engine.missionSystem.wallShockFlash > 0f
        val wallColor = if (shockBoost) AndroidColor.WHITE else dangerArgb

        strokePaint.color = PlayerAvatarRenderer.withAlpha(dangerArgb, if (shockBoost) 210 else (85 + (beatFlash * 65).toInt()))
        strokePaint.strokeWidth = if (shockBoost) 18f else 12f
        canvas.drawRect(left, top, right, bottom, strokePaint)

        strokePaint.color = PlayerAvatarRenderer.withAlpha(wallColor, 245)
        strokePaint.strokeWidth = 3.6f
        canvas.drawRect(left, top, right, bottom, strokePaint)

        strokePaint.color = PlayerAvatarRenderer.withAlpha(stageAccentArgb, 75)
        strokePaint.strokeWidth = 1.6f
        canvas.drawRect(left + 10f, top + 10f, right - 10f, bottom - 10f, strokePaint)

        val cornersX = floatArrayOf(left, right, left, right)
        val cornersY = floatArrayOf(top, top, bottom, bottom)
        for (i in 0 until 4) {
            fillPaint.color = dangerArgb
            canvas.drawCircle(cornersX[i], cornersY[i], 12f, fillPaint)
            fillPaint.color = AndroidColor.WHITE
            canvas.drawCircle(cornersX[i], cornersY[i], 5.5f, fillPaint)
        }

        val pBmp = when (engine.currentStage.mapId) {
            ArenaMapId.CYBER_CITY -> atlas.pillarSprite
            ArenaMapId.PLASMA_VOLCANO -> atlas.pillarVolcanoSprite
            ArenaMapId.COSMIC_VOID -> atlas.pillarCosmicSprite
            ArenaMapId.FROST_CORE -> atlas.pillarFrostSprite
            ArenaMapId.TOXIC_LAB -> atlas.pillarToxicSprite
            ArenaMapId.SOLAR_EMPIRE -> atlas.pillarSolarSprite
        }
        val halfW = pBmp.width * 0.5f
        val halfH = pBmp.height * 0.5f
        val pillars = ArenaBounds.activePillars
        for (i in pillars.indices) {
            val p = pillars[i]
            val px = p.x + worldToScreenX
            val py = p.y + worldToScreenY
            if (px < -75f || px > w + 75f || py < -75f || py > h + 75f) continue
            canvas.drawBitmap(pBmp, px - halfW, py - halfH, bitmapPaint)
        }
    }

    private fun drawCaptureBeacon(
        canvas: Canvas,
        sx: Float,
        sy: Float,
        radius: Float,
        progress: Float,
        playerInside: Boolean,
        beatFlash: Float
    ) {
        val baseCol = if (playerInside) limeArgb else cyanArgb
        val gBmp = if (playerInside) atlas.glowLime else atlas.glowCyan
        glowPaint.alpha = 155 + (beatFlash * 70).toInt()
        canvas.drawBitmap(gBmp, sx - gBmp.width * 0.5f, sy - gBmp.height * 0.5f, glowPaint)

        fillPaint.color = PlayerAvatarRenderer.withAlpha(baseCol, if (playerInside) 52 else 28)
        canvas.drawCircle(sx, sy, radius, fillPaint)

        fillPaint.color = PlayerAvatarRenderer.withAlpha(baseCol, 110 + (beatFlash * 90).toInt())
        canvas.drawCircle(sx, sy, 15f + beatFlash * 6f, fillPaint)
        fillPaint.color = AndroidColor.WHITE
        canvas.drawCircle(sx, sy, 6.5f, fillPaint)

        strokePaint.color = PlayerAvatarRenderer.withAlpha(baseCol, 175)
        strokePaint.strokeWidth = 3f
        canvas.drawCircle(sx, sy, radius, strokePaint)

        if (progress > 0.01f) {
            arcRect.set(sx - radius, sy - radius, sx + radius, sy + radius)
            strokePaint.color = limeArgb
            strokePaint.strokeWidth = 7f
            canvas.drawArc(arcRect, -90f, 360f * progress.coerceIn(0f, 1f), false, strokePaint)
        }
    }

    private fun drawHazardZones(canvas: Canvas, worldToScreenX: Float, worldToScreenY: Float) {
        engine.pool.hazardZones.forEachActive { hz ->
            val sx = hz.x + worldToScreenX
            val sy = hz.y + worldToScreenY
            if (!hz.detonated) {
                val p = hz.chargeProgress
                fillPaint.color = PlayerAvatarRenderer.withAlpha(dangerArgb, 30 + (p * 58).toInt())
                canvas.drawCircle(sx, sy, hz.radius, fillPaint)
                fillPaint.color = PlayerAvatarRenderer.withAlpha(dangerArgb, 95 + (p * 100).toInt())
                canvas.drawCircle(sx, sy, hz.radius * p, fillPaint)
                strokePaint.color = dangerArgb
                strokePaint.strokeWidth = 3f
                canvas.drawCircle(sx, sy, hz.radius, strokePaint)
            } else {
                val flashInv = (1f - ((hz.elapsed - hz.telegraphDuration) / hz.detonationFlashDuration)).coerceIn(0f, 1f)
                fillPaint.color = PlayerAvatarRenderer.withAlpha(dangerArgb, (flashInv * 205f).toInt())
                canvas.drawCircle(sx, sy, hz.radius, fillPaint)
                fillPaint.color = PlayerAvatarRenderer.withAlpha(AndroidColor.WHITE, (flashInv * 230f).toInt())
                canvas.drawCircle(sx, sy, hz.radius * 0.62f, fillPaint)
            }
        }
    }

    private fun drawLaserBeams(canvas: Canvas, worldToScreenX: Float, worldToScreenY: Float, beatFlash: Float) {
        val left = -ArenaBounds.HALF_WIDTH + worldToScreenX
        val right = ArenaBounds.HALF_WIDTH + worldToScreenX
        val top = -ArenaBounds.HALF_HEIGHT + worldToScreenY
        val bottom = ArenaBounds.HALF_HEIGHT + worldToScreenY

        engine.pool.laserBeams.forEachActive { beam ->
            if (!beam.isFiring) {
                val p = beam.telegraphProgress
                val corridorAlpha = 32 + (p * 65).toInt()
                if (beam.isHorizontal) {
                    val sy = beam.position + worldToScreenY
                    fastFillPaint.color = PlayerAvatarRenderer.withAlpha(dangerArgb, corridorAlpha)
                    canvas.drawRect(left, sy - beam.halfWidth, right, sy + beam.halfWidth, fastFillPaint)
                    linePaint.color = PlayerAvatarRenderer.withAlpha(dangerArgb, 175 + (p * 80).toInt())
                    linePaint.strokeWidth = 2.4f + p * 3.2f
                    canvas.drawLine(left, sy, right, sy, linePaint)
                } else {
                    val sx = beam.position + worldToScreenX
                    fastFillPaint.color = PlayerAvatarRenderer.withAlpha(dangerArgb, corridorAlpha)
                    canvas.drawRect(sx - beam.halfWidth, top, sx + beam.halfWidth, bottom, fastFillPaint)
                    linePaint.color = PlayerAvatarRenderer.withAlpha(dangerArgb, 175 + (p * 80).toInt())
                    linePaint.strokeWidth = 2.4f + p * 3.2f
                    canvas.drawLine(sx, top, sx, bottom, linePaint)
                }
            } else {
                val pulseW = beam.halfWidth * (0.96f + beatFlash * 0.24f)
                if (beam.isHorizontal) {
                    val sy = beam.position + worldToScreenY
                    fastFillPaint.color = PlayerAvatarRenderer.withAlpha(dangerArgb, 220)
                    canvas.drawRect(left, sy - pulseW, right, sy + pulseW, fastFillPaint)
                    fastFillPaint.color = AndroidColor.WHITE
                    canvas.drawRect(left, sy - pulseW * 0.38f, right, sy + pulseW * 0.38f, fastFillPaint)
                } else {
                    val sx = beam.position + worldToScreenX
                    fastFillPaint.color = PlayerAvatarRenderer.withAlpha(dangerArgb, 220)
                    canvas.drawRect(sx - pulseW, top, sx + pulseW, bottom, fastFillPaint)
                    fastFillPaint.color = AndroidColor.WHITE
                    canvas.drawRect(sx - pulseW * 0.38f, top, sx + pulseW * 0.38f, bottom, fastFillPaint)
                }
            }
        }
    }

    private fun drawMissionTargetArrow(
        canvas: Canvas,
        playerScreenX: Float,
        playerScreenY: Float,
        targetScreenX: Float,
        targetScreenY: Float,
        colorArgb: Int,
        beatFlash: Float
    ) {
        val dx = targetScreenX - playerScreenX
        val dy = targetScreenY - playerScreenY
        val dist = hypot(dx, dy)
        if (dist < 78f) return

        val angle = atan2(dy, dx)
        val orbitDist = 62f + beatFlash * 6f
        val ax = playerScreenX + cos(angle) * orbitDist
        val ay = playerScreenY + sin(angle) * orbitDist
        val cosA = cos(angle)
        val sinA = sin(angle)

        arrowPath.reset()
        arrowPath.moveTo(ax + cosA * 15f, ay + sinA * 15f)
        arrowPath.lineTo(ax - cosA * 9f - sinA * 9f, ay - sinA * 9f + cosA * 9f)
        arrowPath.lineTo(ax - cosA * 4.5f, ay - sinA * 4.5f)
        arrowPath.lineTo(ax - cosA * 9f + sinA * 9f, ay - sinA * 9f - cosA * 9f)
        arrowPath.close()

        fillPaint.color = colorArgb
        canvas.drawPath(arrowPath, fillPaint)
        strokePaint.color = AndroidColor.WHITE
        strokePaint.strokeWidth = 2f
        canvas.drawPath(arrowPath, strokePaint)
    }
}
