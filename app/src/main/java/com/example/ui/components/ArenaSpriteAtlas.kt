package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.example.data.EnemyArchetype
import com.example.ui.theme.AcidLime
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HotMagenta
import com.example.ui.theme.SolarGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * One-time offscreen Bitmap Sprite & Bloom Atlas baked once when the arena initializes.
 * Delivers Unity URP-style layered neon mech visuals, soft radial bloom halos, metallic
 * cyber-floor tiles, and beveled 3D-feel pillars at near-zero per-frame CPU cost!
 */
class ArenaSpriteAtlas {
    private val cyanArgb = ElectricCyan.toArgb()
    private val magentaArgb = HotMagenta.toArgb()
    private val limeArgb = AcidLime.toArgb()
    private val goldArgb = SolarGold.toArgb()
    private val dangerArgb = DangerRed.toArgb()

    // Tiled Arena Floor Panels (160x160) — 1 unique GPU tile per World Map!
    val floorTile: Bitmap = bakeCyberFloorTile(160)
    val floorTileVolcano: Bitmap = bakeVolcanoFloorTile(160)
    val floorTileCosmic: Bitmap = bakeCosmicFloorTile(160)
    val floorTileFrost: Bitmap = bakeFrostFloorTile(160)
    val floorTileToxic: Bitmap = bakeToxicFloorTile(160)
    val floorTileSolar: Bitmap = bakeSolarFloorTile(160)

    // Pre-baked Soft Radial Neon Bloom Glow Stamps (for instant zero-shader bloom!)
    val glowCyan: Bitmap = bakeRadialGlow(120, cyanArgb, 130)
    val glowMagenta: Bitmap = bakeRadialGlow(120, magentaArgb, 130)
    val glowGold: Bitmap = bakeRadialGlow(132, goldArgb, 145)
    val glowRed: Bitmap = bakeRadialGlow(128, dangerArgb, 145)
    val glowLime: Bitmap = bakeRadialGlow(120, limeArgb, 130)

    // Pre-baked 3D Metallic Pillars — 1 unique architectural pillar per World Map!
    val pillarSprite: Bitmap = bakePillarSprite(96, 33f)
    val pillarVolcanoSprite: Bitmap = bakeVolcanoPillarSprite(104, 36f)
    val pillarCosmicSprite: Bitmap = bakeCosmicPillarSprite(96, 32f)
    val pillarFrostSprite: Bitmap = bakeFrostPillarSprite(96, 32f)
    val pillarToxicSprite: Bitmap = bakeToxicPillarSprite(98, 33f)
    val pillarSolarSprite: Bitmap = bakeSolarPillarSprite(98, 33f)

    // High-Detail Multi-Layered Sci-Fi Enemy Sprites (8 directions for Swarm/Runner!)
    val swarmSprites: Array<Bitmap> = Array(8) { dir ->
        bakeCyberDrone(54, EnemyArchetype.SWARM.radius * 0.92f, EnemyArchetype.SWARM.color.toArgb(), dir * (PI.toFloat() / 4f), false)
    }
    val swarmFlashSprite: Bitmap = bakeCyberDrone(54, EnemyArchetype.SWARM.radius * 0.92f, AndroidColor.WHITE, 0f, true)

    val runnerSprites: Array<Bitmap> = Array(8) { dir ->
        bakeRazorInterceptor(54, EnemyArchetype.RUNNER.radius * 0.96f, EnemyArchetype.RUNNER.color.toArgb(), dir * (PI.toFloat() / 4f), false)
    }
    val runnerFlashSprite: Bitmap = bakeRazorInterceptor(54, EnemyArchetype.RUNNER.radius * 0.96f, AndroidColor.WHITE, 0f, true)

    val tankSprite: Bitmap = bakeHeavyJuggernaut(78, EnemyArchetype.TANK.radius * 0.96f, EnemyArchetype.TANK.color.toArgb(), false)
    val tankFlashSprite: Bitmap = bakeHeavyJuggernaut(78, EnemyArchetype.TANK.radius * 0.96f, AndroidColor.WHITE, true)

    val eliteSprite: Bitmap = bakeBossSovereign(106, EnemyArchetype.ELITE_BOSS.radius * 0.96f, EnemyArchetype.ELITE_BOSS.color.toArgb(), false)
    val eliteFlashSprite: Bitmap = bakeBossSovereign(106, EnemyArchetype.ELITE_BOSS.radius * 0.96f, AndroidColor.WHITE, true)

    val coreSprite: Bitmap = bakeCorruptedCoreSprite(78, EnemyArchetype.CORRUPTED_CORE.radius * 0.96f, false)
    val coreFlashSprite: Bitmap = bakeCorruptedCoreSprite(78, EnemyArchetype.CORRUPTED_CORE.radius * 0.96f, true)

    // Crisp Glowing Orbs, Coins, and Orbit Blades
    val orbCyanSprite: Bitmap = bakeDiamondSprite(26, 7.2f, cyanArgb)
    val orbLimeSprite: Bitmap = bakeDiamondSprite(30, 8.6f, limeArgb)
    val orbGoldSprite: Bitmap = bakeDiamondSprite(34, 10.4f, goldArgb)
    val coinSprite: Bitmap = bakeCoinSprite(32)
    val bladeMagentaSprite: Bitmap = bakeBladeSprite(36, 12.5f, magentaArgb)
    val bladeGoldSprite: Bitmap = bakeBladeSprite(42, 15.0f, goldArgb)

    private fun bakeCyberFloorTile(size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val s = size.toFloat()

        // Deep brushed slate-obsidian tile base
        c.drawColor(0xFF0A0D1D.toInt())

        // Inner chamfered deck plating
        val platePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFF0E1328.toInt()
        }
        val inset = 7f
        val cut = 14f
        val plate = Path().apply {
            moveTo(inset + cut, inset)
            lineTo(s - inset - cut, inset)
            lineTo(s - inset, inset + cut)
            lineTo(s - inset, s - inset - cut)
            lineTo(s - inset - cut, s - inset)
            lineTo(inset + cut, s - inset)
            lineTo(inset, s - inset - cut)
            lineTo(inset, inset + cut)
            close()
        }
        c.drawPath(plate, platePaint)

        // Subtle beveled plate edge
        val seamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
            color = 0xFF1A2548.toInt()
        }
        c.drawPath(plate, seamPaint)

        // Outer subtle circuit border
        seamPaint.color = 0xFF141D3A.toInt()
        seamPaint.strokeWidth = 1.2f
        c.drawRect(0.5f, 0.5f, s - 0.5f, s - 0.5f, seamPaint)

        // Cross circuit trace inside tile
        seamPaint.color = 0xFF152042.toInt()
        seamPaint.strokeWidth = 1.2f
        c.drawLine(s * 0.5f, inset + 12f, s * 0.5f, s - inset - 12f, seamPaint)
        c.drawLine(inset + 12f, s * 0.5f, s - inset - 12f, s * 0.5f, seamPaint)

        // Glowing subtle cyber node at center & corners
        val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x4400F0FF
        }
        c.drawCircle(s * 0.5f, s * 0.5f, 3.2f, nodePaint)
        nodePaint.color = 0x336D5DF6
        c.drawCircle(2f, 2f, 3f, nodePaint)
        c.drawCircle(s - 2f, 2f, 3f, nodePaint)
        c.drawCircle(2f, s - 2f, 3f, nodePaint)
        c.drawCircle(s - 2f, s - 2f, 3f, nodePaint)

        return bmp
    }

    private fun bakeRadialGlow(size: Int, colorArgb: Int, maxAlpha: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx,
                cy,
                cx,
                intArrayOf(
                    PlayerAvatarRenderer.withAlpha(colorArgb, maxAlpha),
                    PlayerAvatarRenderer.withAlpha(colorArgb, maxAlpha / 3),
                    0x00000000
                ),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        c.drawCircle(cx, cy, cx, p)
        return bmp
    }

    private fun bakePillarSprite(size: Int, r: Float): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f

        // Soft outer cyan halo
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, r * 1.45f,
                intArrayOf(0x6600F0FF, 0x00000000),
                null,
                Shader.TileMode.CLAMP
            )
        }
        c.drawCircle(cx, cy, r * 1.45f, glow)

        // Octagonal armored titanium body
        val oct = Path()
        for (i in 0 until 8) {
            val a = i * (PI / 4.0) + PI / 8.0
            val px = cx + (cos(a) * r).toFloat()
            val py = cy + (sin(a) * r).toFloat()
            if (i == 0) oct.moveTo(px, py) else oct.lineTo(px, py)
        }
        oct.close()

        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = RadialGradient(
                cx - r * 0.2f, cy - r * 0.25f, r * 1.15f,
                intArrayOf(0xFF2B3560.toInt(), 0xFF141A36.toInt(), 0xFF0A0D1E.toInt()),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        c.drawPath(oct, fp)

        // Neon rim bevel
        val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = cyanArgb
        }
        c.drawPath(oct, sp)

        // Inner energy core ring
        sp.strokeWidth = 2.2f
        sp.color = magentaArgb
        c.drawCircle(cx, cy, r * 0.52f, sp)

        val core = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = cyanArgb
        }
        c.drawCircle(cx, cy, r * 0.25f, core)
        core.color = AndroidColor.WHITE
        c.drawCircle(cx, cy, r * 0.12f, core)

        return bmp
    }

    private fun bakeVolcanoFloorTile(size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val s = size.toFloat()

        // Scorched volcanic obsidian-basalt base
        c.drawColor(0xFF160908.toInt())

        // Layered dark basalt slabs
        val slabPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFF200E0C.toInt()
        }
        c.drawRect(6f, 6f, s * 0.48f, s * 0.48f, slabPaint)
        c.drawRect(s * 0.52f, 6f, s - 6f, s * 0.48f, slabPaint)
        c.drawRect(6f, s * 0.52f, s * 0.48f, s - 6f, slabPaint)
        c.drawRect(s * 0.52f, s * 0.52f, s - 6f, s - 6f, slabPaint)

        // Glowing molten magma fissure seams
        val magmaGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4.5f
            color = 0x55FF4500
        }
        val magmaCore = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
            color = 0xAAFF7A18.toInt()
        }
        val cracks = Path().apply {
            moveTo(0f, s * 0.5f)
            lineTo(s * 0.34f, s * 0.47f)
            lineTo(s * 0.5f, s * 0.5f)
            lineTo(s * 0.72f, s * 0.53f)
            lineTo(s, s * 0.5f)
            moveTo(s * 0.5f, 0f)
            lineTo(s * 0.48f, s * 0.33f)
            lineTo(s * 0.5f, s * 0.5f)
            lineTo(s * 0.53f, s * 0.74f)
            lineTo(s * 0.5f, s)
        }
        c.drawPath(cracks, magmaGlow)
        c.drawPath(cracks, magmaCore)

        // Glowing ember vents at junctions
        val emberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x77FFAA22
        }
        c.drawCircle(s * 0.5f, s * 0.5f, 4.2f, emberPaint)
        emberPaint.color = 0x44FF3D00
        c.drawCircle(2f, 2f, 3.5f, emberPaint)
        c.drawCircle(s - 2f, 2f, 3.5f, emberPaint)
        c.drawCircle(2f, s - 2f, 3.5f, emberPaint)
        c.drawCircle(s - 2f, s - 2f, 3.5f, emberPaint)

        return bmp
    }

    private fun bakeCosmicFloorTile(size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val s = size.toFloat()

        // Deep astral nebula void base
        c.drawColor(0xFF0A0619.toInt())

        // Diamond quantum lattice
        val lattice = Path().apply {
            moveTo(s * 0.5f, 6f)
            lineTo(s - 6f, s * 0.5f)
            lineTo(s * 0.5f, s - 6f)
            lineTo(6f, s * 0.5f)
            close()
        }
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFF110B28.toInt()
        }
        c.drawPath(lattice, fill)

        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
            color = 0x558B5CF6
        }
        c.drawPath(lattice, stroke)
        stroke.color = 0x33FFD166
        stroke.strokeWidth = 1.1f
        c.drawRect(0.5f, 0.5f, s - 0.5f, s - 0.5f, stroke)

        // Subtle starfield dust + quantum center node
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        starPaint.color = 0x66FFFFFF
        c.drawCircle(s * 0.22f, s * 0.24f, 1.5f, starPaint)
        c.drawCircle(s * 0.78f, s * 0.28f, 1.3f, starPaint)
        c.drawCircle(s * 0.26f, s * 0.78f, 1.4f, starPaint)
        c.drawCircle(s * 0.76f, s * 0.74f, 1.8f, starPaint)
        starPaint.color = 0x55FFD166
        c.drawCircle(s * 0.5f, s * 0.5f, 3.4f, starPaint)
        starPaint.color = 0x44A855F7
        c.drawCircle(s * 0.5f, 6f, 2.5f, starPaint)
        c.drawCircle(s * 0.5f, s - 6f, 2.5f, starPaint)
        c.drawCircle(6f, s * 0.5f, 2.5f, starPaint)
        c.drawCircle(s - 6f, s * 0.5f, 2.5f, starPaint)

        return bmp
    }

    private fun bakeVolcanoPillarSprite(size: Int, r: Float): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f

        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, r * 1.42f,
                intArrayOf(0x77FF5722, 0x00000000),
                null,
                Shader.TileMode.CLAMP
            )
        }
        c.drawCircle(cx, cy, r * 1.42f, glow)

        val oct = Path()
        for (i in 0 until 8) {
            val a = i * (PI / 4.0)
            val px = cx + (cos(a) * r).toFloat()
            val py = cy + (sin(a) * r).toFloat()
            if (i == 0) oct.moveTo(px, py) else oct.lineTo(px, py)
        }
        oct.close()

        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = RadialGradient(
                cx - r * 0.2f, cy - r * 0.2f, r * 1.15f,
                intArrayOf(0xFF4A1812.toInt(), 0xFF280C0A.toInt(), 0xFF140505.toInt()),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        c.drawPath(oct, fp)

        val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.2f
            color = 0xFFFF5722.toInt()
        }
        c.drawPath(oct, sp)

        sp.strokeWidth = 2.4f
        sp.color = goldArgb
        c.drawCircle(cx, cy, r * 0.54f, sp)

        val core = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFFFF6D00.toInt()
        }
        c.drawCircle(cx, cy, r * 0.30f, core)
        core.color = goldArgb
        c.drawCircle(cx, cy, r * 0.17f, core)
        core.color = AndroidColor.WHITE
        c.drawCircle(cx, cy, r * 0.08f, core)

        return bmp
    }

    private fun bakeCosmicPillarSprite(size: Int, r: Float): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f

        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, r * 1.45f,
                intArrayOf(0x779D4EDD, 0x00000000),
                null,
                Shader.TileMode.CLAMP
            )
        }
        c.drawCircle(cx, cy, r * 1.45f, glow)

        val hex = Path()
        for (i in 0 until 6) {
            val a = i * (PI / 3.0) - PI / 6.0
            val px = cx + (cos(a) * r).toFloat()
            val py = cy + (sin(a) * r).toFloat()
            if (i == 0) hex.moveTo(px, py) else hex.lineTo(px, py)
        }
        hex.close()

        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = RadialGradient(
                cx - r * 0.18f, cy - r * 0.22f, r * 1.15f,
                intArrayOf(0xFF331B5C.toInt(), 0xFF1A0D36.toInt(), 0xFF0A0518.toInt()),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        c.drawPath(hex, fp)

        val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = 0xFFA855F7.toInt()
        }
        c.drawPath(hex, sp)

        sp.strokeWidth = 2.2f
        sp.color = goldArgb
        c.drawCircle(cx, cy, r * 0.50f, sp)

        val core = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = goldArgb
        }
        c.drawCircle(cx, cy, r * 0.24f, core)
        core.color = AndroidColor.WHITE
        c.drawCircle(cx, cy, r * 0.12f, core)

        return bmp
    }

    private fun bakeFrostFloorTile(size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val s = size.toFloat()

        // Deep glacial ice slate
        c.drawColor(0xFF071624.toInt())

        val slab = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFF0C2236.toInt()
        }
        val hex = Path().apply {
            moveTo(s * 0.5f, 8f)
            lineTo(s - 10f, s * 0.28f)
            lineTo(s - 10f, s * 0.72f)
            lineTo(s * 0.5f, s - 8f)
            lineTo(10f, s * 0.72f)
            lineTo(10f, s * 0.28f)
            close()
        }
        c.drawPath(hex, slab)

        val frostLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
            color = 0x6667E8F9
        }
        c.drawPath(hex, frostLine)
        frostLine.color = 0x3338BDF8
        frostLine.strokeWidth = 1.2f
        c.drawLine(0f, 0f, s, s, frostLine)
        c.drawLine(s, 0f, 0f, s, frostLine)
        c.drawRect(0.5f, 0.5f, s - 0.5f, s - 0.5f, frostLine)

        val glint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x77E0F2FE
        }
        c.drawCircle(s * 0.5f, s * 0.5f, 3.6f, glint)
        return bmp
    }

    private fun bakeToxicFloorTile(size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val s = size.toFloat()

        // Dark bio-containment steel base
        c.drawColor(0xFF0A140D.toInt())

        val panel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFF102016.toInt()
        }
        c.drawRect(8f, 8f, s - 8f, s - 8f, panel)

        // Acid-lime bio-conduit channels
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = 0x3339FF14
        }
        val core = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.4f
            color = 0x887CFF3A.toInt()
        }
        c.drawRect(8f, 8f, s - 8f, s - 8f, glow)
        c.drawRect(8f, 8f, s - 8f, s - 8f, core)
        c.drawLine(s * 0.5f, 0f, s * 0.5f, s, core)
        c.drawLine(0f, s * 0.5f, s, s * 0.5f, core)

        // Hazard vent dots
        val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x66A3E635
        }
        c.drawCircle(s * 0.25f, s * 0.25f, 3f, dot)
        c.drawCircle(s * 0.75f, s * 0.25f, 3f, dot)
        c.drawCircle(s * 0.25f, s * 0.75f, 3f, dot)
        c.drawCircle(s * 0.75f, s * 0.75f, 3f, dot)
        return bmp
    }

    private fun bakeSolarFloorTile(size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val s = size.toFloat()

        // Royal obsidian-bronze marble base
        c.drawColor(0xFF151008.toInt())

        val tile = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFF1E170B.toInt()
        }
        c.drawRect(6f, 6f, s - 6f, s - 6f, tile)

        // Golden imperial circuit inlay
        val goldLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
            color = 0x66FFD166
        }
        c.drawRect(6f, 6f, s - 6f, s - 6f, goldLine)
        c.drawCircle(s * 0.5f, s * 0.5f, s * 0.26f, goldLine)

        goldLine.color = 0x33F59E0B
        goldLine.strokeWidth = 1.2f
        c.drawLine(s * 0.5f, 6f, s * 0.5f, s - 6f, goldLine)
        c.drawLine(6f, s * 0.5f, s - 6f, s * 0.5f, goldLine)

        val sunDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x88FFD166.toInt()
        }
        c.drawCircle(s * 0.5f, s * 0.5f, 4f, sunDot)
        return bmp
    }

    private fun bakeFrostPillarSprite(size: Int, r: Float): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f

        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx, cy, r * 1.45f, intArrayOf(0x7767E8F9, 0x00000000), null, Shader.TileMode.CLAMP)
        }
        c.drawCircle(cx, cy, r * 1.45f, glow)

        val crystal = Path()
        for (i in 0 until 6) {
            val a = i * (PI / 3.0)
            val px = cx + (cos(a) * r).toFloat()
            val py = cy + (sin(a) * r).toFloat()
            if (i == 0) crystal.moveTo(px, py) else crystal.lineTo(px, py)
        }
        crystal.close()

        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = RadialGradient(
                cx - r * 0.2f, cy - r * 0.2f, r * 1.15f,
                intArrayOf(0xFF38BDF8.toInt(), 0xFF0C4A6E.toInt(), 0xFF082F49.toInt()),
                floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP
            )
        }
        c.drawPath(crystal, fp)

        val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = 0xFFE0F2FE.toInt()
        }
        c.drawPath(crystal, sp)
        sp.strokeWidth = 2f
        sp.color = 0xFF67E8F9.toInt()
        c.drawCircle(cx, cy, r * 0.5f, sp)

        val core = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = AndroidColor.WHITE }
        c.drawCircle(cx, cy, r * 0.22f, core)
        return bmp
    }

    private fun bakeToxicPillarSprite(size: Int, r: Float): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f

        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx, cy, r * 1.45f, intArrayOf(0x7739FF14, 0x00000000), null, Shader.TileMode.CLAMP)
        }
        c.drawCircle(cx, cy, r * 1.45f, glow)

        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = RadialGradient(
                cx - r * 0.2f, cy - r * 0.2f, r * 1.15f,
                intArrayOf(0xFF214A1F.toInt(), 0xFF102814.toInt(), 0xFF07130A.toInt()),
                floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP
            )
        }
        c.drawCircle(cx, cy, r, fp)

        val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.2f
            color = limeArgb
        }
        c.drawCircle(cx, cy, r, sp)
        sp.strokeWidth = 2.2f
        sp.color = goldArgb
        c.drawCircle(cx, cy, r * 0.56f, sp)

        val core = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = limeArgb }
        c.drawCircle(cx, cy, r * 0.30f, core)
        core.color = AndroidColor.WHITE
        c.drawCircle(cx, cy, r * 0.13f, core)
        return bmp
    }

    private fun bakeSolarPillarSprite(size: Int, r: Float): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f

        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx, cy, r * 1.45f, intArrayOf(0x88FFD166.toInt(), 0x00000000), null, Shader.TileMode.CLAMP)
        }
        c.drawCircle(cx, cy, r * 1.45f, glow)

        val oct = Path()
        for (i in 0 until 8) {
            val a = i * (PI / 4.0) + PI / 8.0
            val px = cx + (cos(a) * r).toFloat()
            val py = cy + (sin(a) * r).toFloat()
            if (i == 0) oct.moveTo(px, py) else oct.lineTo(px, py)
        }
        oct.close()

        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = RadialGradient(
                cx - r * 0.2f, cy - r * 0.2f, r * 1.15f,
                intArrayOf(0xFF5C4312.toInt(), 0xFF2B1E08.toInt(), 0xFF140E04.toInt()),
                floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP
            )
        }
        c.drawPath(oct, fp)

        val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.2f
            color = goldArgb
        }
        c.drawPath(oct, sp)
        sp.strokeWidth = 2.2f
        sp.color = AndroidColor.WHITE
        c.drawCircle(cx, cy, r * 0.52f, sp)

        val core = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = goldArgb }
        c.drawCircle(cx, cy, r * 0.28f, core)
        core.color = AndroidColor.WHITE
        c.drawCircle(cx, cy, r * 0.13f, core)
        return bmp
    }

    private fun bakeCyberDrone(size: Int, r: Float, colorArgb: Int, angleRad: Float, isFlash: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f
        val cosR = cos(angleRad)
        val sinR = sin(angleRad)
        fun rx(x: Float, y: Float) = cx + x * cosR - y * sinR
        fun ry(x: Float, y: Float) = cy + x * sinR + y * cosR

        if (!isFlash) {
            val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    cx, cy, r * 1.65f,
                    intArrayOf(PlayerAvatarRenderer.withAlpha(colorArgb, 105), 0x00000000),
                    null, Shader.TileMode.CLAMP
                )
            }
            c.drawCircle(cx, cy, r * 1.65f, glow)
        }

        val hull = Path().apply {
            moveTo(rx(r * 1.25f, 0f), ry(r * 1.25f, 0f))
            lineTo(rx(-r * 0.45f, -r * 0.98f), ry(-r * 0.45f, -r * 0.98f))
            lineTo(rx(-r * 0.95f, -r * 0.45f), ry(-r * 0.95f, -r * 0.45f))
            lineTo(rx(-r * 0.55f, 0f), ry(-r * 0.55f, 0f))
            lineTo(rx(-r * 0.95f, r * 0.45f), ry(-r * 0.95f, r * 0.45f))
            lineTo(rx(-r * 0.45f, r * 0.98f), ry(-r * 0.45f, r * 0.98f))
            close()
        }

        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isFlash) AndroidColor.WHITE else 0xFF1D1538.toInt()
        }
        c.drawPath(hull, fp)

        if (!isFlash) {
            val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.6f
                color = colorArgb
            }
            c.drawPath(hull, sp)

            // Glowing cockpit eye
            fp.color = colorArgb
            c.drawCircle(rx(r * 0.35f, 0f), ry(r * 0.35f, 0f), r * 0.32f, fp)
            fp.color = AndroidColor.WHITE
            c.drawCircle(rx(r * 0.42f, 0f), ry(r * 0.42f, 0f), r * 0.14f, fp)
        }
        return bmp
    }

    private fun bakeRazorInterceptor(size: Int, r: Float, colorArgb: Int, angleRad: Float, isFlash: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f
        val cosR = cos(angleRad)
        val sinR = sin(angleRad)
        fun rx(x: Float, y: Float) = cx + x * cosR - y * sinR
        fun ry(x: Float, y: Float) = cy + x * sinR + y * cosR

        if (!isFlash) {
            val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    cx, cy, r * 1.7f,
                    intArrayOf(PlayerAvatarRenderer.withAlpha(colorArgb, 120), 0x00000000),
                    null, Shader.TileMode.CLAMP
                )
            }
            c.drawCircle(cx, cy, r * 1.7f, glow)
        }

        val jet = Path().apply {
            moveTo(rx(r * 1.45f, 0f), ry(r * 1.45f, 0f))
            lineTo(rx(-r * 0.95f, -r * 1.05f), ry(-r * 0.95f, -r * 1.05f))
            lineTo(rx(-r * 0.32f, 0f), ry(-r * 0.32f, 0f))
            lineTo(rx(-r * 0.95f, r * 1.05f), ry(-r * 0.95f, r * 1.05f))
            close()
        }

        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isFlash) AndroidColor.WHITE else colorArgb
        }
        c.drawPath(jet, fp)

        if (!isFlash) {
            // Dark armor spine
            val inner = Path().apply {
                moveTo(rx(r * 0.85f, 0f), ry(r * 0.85f, 0f))
                lineTo(rx(-r * 0.45f, -r * 0.42f), ry(-r * 0.45f, -r * 0.42f))
                lineTo(rx(-r * 0.25f, 0f), ry(-r * 0.25f, 0f))
                lineTo(rx(-r * 0.45f, r * 0.42f), ry(-r * 0.45f, r * 0.42f))
                close()
            }
            fp.color = 0xFF280A26.toInt()
            c.drawPath(inner, fp)

            val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = 0xEEFFFFFF.toInt()
            }
            c.drawPath(jet, sp)

            // Thruster ember
            fp.color = goldArgb
            c.drawCircle(rx(-r * 0.42f, 0f), ry(-r * 0.42f, 0f), r * 0.24f, fp)
        }
        return bmp
    }

    private fun bakeHeavyJuggernaut(size: Int, r: Float, colorArgb: Int, isFlash: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f

        if (!isFlash) {
            val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    cx, cy, r * 1.48f,
                    intArrayOf(PlayerAvatarRenderer.withAlpha(colorArgb, 110), 0x00000000),
                    null, Shader.TileMode.CLAMP
                )
            }
            c.drawCircle(cx, cy, r * 1.48f, glow)
        }

        val hex = Path()
        for (i in 0 until 6) {
            val a = i * (PI / 3.0)
            val px = cx + (cos(a) * r).toFloat()
            val py = cy + (sin(a) * r).toFloat()
            if (i == 0) hex.moveTo(px, py) else hex.lineTo(px, py)
        }
        hex.close()

        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isFlash) AndroidColor.WHITE else 0xFF2C0E1B.toInt()
        }
        c.drawPath(hex, fp)

        if (!isFlash) {
            val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3.6f
                color = colorArgb
            }
            c.drawPath(hex, sp)

            // Inner armor plates
            sp.strokeWidth = 2f
            sp.color = 0xAAFFFFFF.toInt()
            c.drawCircle(cx, cy, r * 0.62f, sp)

            fp.color = colorArgb
            c.drawCircle(cx, cy, r * 0.42f, fp)
            fp.color = goldArgb
            c.drawCircle(cx, cy, r * 0.22f, fp)
            fp.color = AndroidColor.WHITE
            c.drawCircle(cx, cy, r * 0.11f, fp)
        }
        return bmp
    }

    private fun bakeBossSovereign(size: Int, r: Float, colorArgb: Int, isFlash: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f

        if (!isFlash) {
            val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    cx, cy, r * 1.45f,
                    intArrayOf(PlayerAvatarRenderer.withAlpha(goldArgb, 130), PlayerAvatarRenderer.withAlpha(magentaArgb, 55), 0x00000000),
                    floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP
                )
            }
            c.drawCircle(cx, cy, r * 1.45f, glow)
        }

        // 8-point Dreadnought Star Hull
        val star = Path()
        for (i in 0 until 16) {
            val a = i * (PI / 8.0)
            val rad = if (i % 2 == 0) r * 1.06f else r * 0.78f
            val px = cx + (cos(a) * rad).toFloat()
            val py = cy + (sin(a) * rad).toFloat()
            if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
        }
        star.close()

        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isFlash) AndroidColor.WHITE else 0xFF28190A.toInt()
        }
        c.drawPath(star, fp)

        if (!isFlash) {
            val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3.6f
                color = colorArgb
            }
            c.drawPath(star, sp)

            sp.color = magentaArgb
            sp.strokeWidth = 3f
            c.drawCircle(cx, cy, r * 0.58f, sp)

            fp.color = goldArgb
            c.drawCircle(cx, cy, r * 0.38f, fp)
            fp.color = AndroidColor.WHITE
            c.drawCircle(cx, cy, r * 0.19f, fp)
        }
        return bmp
    }

    private fun bakeCorruptedCoreSprite(size: Int, r: Float, isFlash: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f

        if (!isFlash) {
            val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    cx, cy, r * 1.55f,
                    intArrayOf(PlayerAvatarRenderer.withAlpha(dangerArgb, 130), 0x00000000),
                    null, Shader.TileMode.CLAMP
                )
            }
            c.drawCircle(cx, cy, r * 1.55f, glow)
        }

        val p = Path()
        for (i in 0 until 6) {
            val a = i * (PI / 3.0)
            val px = cx + (cos(a) * r).toFloat()
            val py = cy + (sin(a) * r).toFloat()
            if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
        }
        p.close()

        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isFlash) AndroidColor.WHITE else 0xFF3A0816.toInt()
        }
        c.drawPath(p, fp)
        if (!isFlash) {
            val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3.4f
                color = dangerArgb
            }
            c.drawPath(p, sp)
            fp.color = dangerArgb
            c.drawCircle(cx, cy, r * 0.5f, fp)
            fp.color = goldArgb
            c.drawCircle(cx, cy, r * 0.28f, fp)
            fp.color = AndroidColor.WHITE
            c.drawCircle(cx, cy, r * 0.13f, fp)
        }
        return bmp
    }

    private fun bakeDiamondSprite(size: Int, r: Float, colorArgb: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, r * 1.75f,
                intArrayOf(PlayerAvatarRenderer.withAlpha(colorArgb, 135), 0x00000000),
                null, Shader.TileMode.CLAMP
            )
        }
        c.drawCircle(cx, cy, r * 1.75f, glow)
        val p = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r, cy)
            lineTo(cx, cy + r)
            lineTo(cx - r, cy)
            close()
        }
        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = colorArgb }
        c.drawPath(p, fp)
        fp.color = AndroidColor.WHITE
        c.drawCircle(cx, cy, r * 0.42f, fp)
        return bmp
    }

    private fun bakeBladeSprite(size: Int, r: Float, colorArgb: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, r * 1.4f,
                intArrayOf(PlayerAvatarRenderer.withAlpha(colorArgb, 150), 0x00000000),
                null, Shader.TileMode.CLAMP
            )
        }
        c.drawCircle(cx, cy, r * 1.4f, glow)
        val p = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r * 0.58f, cy)
            lineTo(cx, cy + r)
            lineTo(cx - r * 0.58f, cy)
            close()
        }
        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = colorArgb }
        c.drawPath(p, fp)
        val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = AndroidColor.WHITE
        }
        c.drawPath(p, sp)
        return bmp
    }

    private fun bakeCoinSprite(size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = size * 0.5f
        val cy = size * 0.5f
        val r = size * 0.34f
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, r * 1.75f,
                intArrayOf(PlayerAvatarRenderer.withAlpha(goldArgb, 130), 0x00000000),
                null, Shader.TileMode.CLAMP
            )
        }
        c.drawCircle(cx, cy, r * 1.75f, glow)
        val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = goldArgb }
        c.drawCircle(cx, cy, r, fp)
        val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = AndroidColor.WHITE
        }
        c.drawCircle(cx, cy, r * 0.72f, sp)
        return bmp
    }
}
