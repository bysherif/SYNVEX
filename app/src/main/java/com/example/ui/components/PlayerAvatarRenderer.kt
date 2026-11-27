package com.example.ui.components

import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.toArgb
import com.example.data.PlayerAuraId
import com.example.data.PlayerColorId
import com.example.data.PlayerShapeId
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared high-speed native Android Canvas renderer for customized Player Shapes,
 * Neon Colors (including animated Prism Chroma), and Auras.
 * Used in both the 60+ FPS ArenaCanvasView and the live Shop/Title preview stage.
 */
object PlayerAvatarRenderer {

    fun resolvePrimaryArgb(colorId: PlayerColorId, timeSeconds: Float): Int {
        if (!colorId.isAnimatedChroma) return colorId.primaryColor.toArgb()
        val hue = (timeSeconds * 110f) % 360f
        return AndroidColor.HSVToColor(floatArrayOf(hue, 0.92f, 1.0f))
    }

    fun resolveSecondaryArgb(colorId: PlayerColorId, timeSeconds: Float): Int {
        if (!colorId.isAnimatedChroma) return colorId.secondaryColor.toArgb()
        val hue = ((timeSeconds * 110f) + 120f) % 360f
        return AndroidColor.HSVToColor(floatArrayOf(hue, 0.92f, 1.0f))
    }

    fun drawAura(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        auraId: PlayerAuraId,
        primaryArgb: Int,
        secondaryArgb: Int,
        beatFlash: Float,
        timeSeconds: Float,
        fillPaint: Paint,
        strokePaint: Paint
    ) {
        when (auraId) {
            PlayerAuraId.PULSE_RING -> {
                fillPaint.color = withAlpha(primaryArgb, (45 + (beatFlash * 55).toInt()).coerceIn(0, 255))
                canvas.drawCircle(cx, cy, radius * (1.65f + beatFlash * 0.28f), fillPaint)
            }
            PlayerAuraId.DOUBLE_ORBIT -> {
                strokePaint.color = withAlpha(primaryArgb, 130)
                strokePaint.strokeWidth = 3f
                canvas.drawCircle(cx, cy, radius * (1.55f + beatFlash * 0.18f), strokePaint)
                strokePaint.color = withAlpha(secondaryArgb, 110)
                strokePaint.strokeWidth = 2.5f
                canvas.drawCircle(cx, cy, radius * (2.0f + beatFlash * 0.25f), strokePaint)
            }
            PlayerAuraId.SONIC_SPIKES -> {
                fillPaint.color = withAlpha(primaryArgb, 42)
                canvas.drawCircle(cx, cy, radius * 1.55f, fillPaint)
                strokePaint.color = withAlpha(secondaryArgb, 170)
                strokePaint.strokeWidth = 3.2f
                val inner = radius * 1.32f
                val outer = radius * (1.88f + beatFlash * 0.42f)
                val baseAngle = timeSeconds * 1.8f
                for (i in 0 until 6) {
                    val a = baseAngle + i * (PI.toFloat() / 3f)
                    val c = cos(a)
                    val s = sin(a)
                    canvas.drawLine(cx + c * inner, cy + s * inner, cx + c * outer, cy + s * outer, strokePaint)
                }
            }
            PlayerAuraId.ROYAL_HALO -> {
                fillPaint.color = withAlpha(primaryArgb, 48)
                canvas.drawCircle(cx, cy, radius * (1.7f + beatFlash * 0.25f), fillPaint)
                strokePaint.color = withAlpha(0xFFFFD600.toInt(), 180)
                strokePaint.strokeWidth = 3f
                val haloR = radius * 1.85f
                canvas.drawCircle(cx, cy, haloR, strokePaint)
                fillPaint.color = 0xFFFFD600.toInt()
                val baseAngle = timeSeconds * 2.4f
                for (i in 0 until 4) {
                    val a = baseAngle + i * (PI.toFloat() * 0.5f)
                    canvas.drawCircle(cx + cos(a) * haloR, cy + sin(a) * haloR, 4.5f, fillPaint)
                }
            }
        }
    }

    fun drawShape(
        canvas: Canvas,
        path: Path,
        cx: Float,
        cy: Float,
        radius: Float,
        facingAngle: Float,
        shapeId: PlayerShapeId,
        fillArgb: Int,
        accentArgb: Int,
        fillPaint: Paint,
        strokePaint: Paint
    ) {
        val c = cos(facingAngle)
        val s = sin(facingAngle)

        path.reset()
        when (shapeId) {
            PlayerShapeId.APEX_ARROW -> {
                addRotatedVertex(path, cx, cy, radius * 1.22f, 0f, c, s, true)
                addRotatedVertex(path, cx, cy, -radius * 0.85f, -radius * 0.85f, c, s, false)
                addRotatedVertex(path, cx, cy, -radius * 0.42f, 0f, c, s, false)
                addRotatedVertex(path, cx, cy, -radius * 0.85f, radius * 0.85f, c, s, false)
                path.close()
            }
            PlayerShapeId.PRISM_DIAMOND -> {
                addRotatedVertex(path, cx, cy, radius * 1.25f, 0f, c, s, true)
                addRotatedVertex(path, cx, cy, 0f, -radius * 0.92f, c, s, false)
                addRotatedVertex(path, cx, cy, -radius * 1.1f, 0f, c, s, false)
                addRotatedVertex(path, cx, cy, 0f, radius * 0.92f, c, s, false)
                path.close()
            }
            PlayerShapeId.NOVA_STAR -> {
                val points = 8
                for (i in 0 until points) {
                    val ang = facingAngle + i * (PI.toFloat() / 4f)
                    val r = if (i % 2 == 0) radius * 1.25f else radius * 0.52f
                    val px = cx + cos(ang) * r
                    val py = cy + sin(ang) * r
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
            }
            PlayerShapeId.VALKYRIE_WING -> {
                addRotatedVertex(path, cx, cy, radius * 1.25f, 0f, c, s, true)
                addRotatedVertex(path, cx, cy, radius * 0.15f, -radius * 0.55f, c, s, false)
                addRotatedVertex(path, cx, cy, -radius * 0.55f, -radius * 1.25f, c, s, false)
                addRotatedVertex(path, cx, cy, -radius * 0.95f, -radius * 0.45f, c, s, false)
                addRotatedVertex(path, cx, cy, -radius * 0.50f, 0f, c, s, false)
                addRotatedVertex(path, cx, cy, -radius * 0.95f, radius * 0.45f, c, s, false)
                addRotatedVertex(path, cx, cy, -radius * 0.55f, radius * 1.25f, c, s, false)
                addRotatedVertex(path, cx, cy, radius * 0.15f, radius * 0.55f, c, s, false)
                path.close()
            }
            PlayerShapeId.AEGIS_HEX -> {
                for (i in 0 until 6) {
                    val ang = facingAngle + i * (PI.toFloat() / 3f)
                    val px = cx + cos(ang) * radius * 1.05f
                    val py = cy + sin(ang) * radius * 1.05f
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
            }
            PlayerShapeId.CROWN_VORTEX -> {
                val points = 12
                for (i in 0 until points) {
                    val ang = facingAngle + i * (PI.toFloat() / 6f)
                    val r = if (i % 2 == 0) radius * 1.26f else radius * 0.68f
                    val px = cx + cos(ang) * r
                    val py = cy + sin(ang) * r
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
            }
        }

        fillPaint.color = fillArgb
        canvas.drawPath(path, fillPaint)

        strokePaint.color = AndroidColor.WHITE
        strokePaint.strokeWidth = 2.6f
        canvas.drawPath(path, strokePaint)

        // Inner contrasting glowing core
        fillPaint.color = accentArgb
        canvas.drawCircle(cx, cy, radius * 0.42f, fillPaint)
        fillPaint.color = AndroidColor.WHITE
        canvas.drawCircle(cx, cy, radius * 0.22f, fillPaint)
    }

    private fun addRotatedVertex(
        path: Path,
        cx: Float,
        cy: Float,
        lx: Float,
        ly: Float,
        cosA: Float,
        sinA: Float,
        move: Boolean
    ) {
        val wx = cx + lx * cosA - ly * sinA
        val wy = cy + lx * sinA + ly * cosA
        if (move) path.moveTo(wx, wy) else path.lineTo(wx, wy)
    }

    fun withAlpha(argb: Int, alpha: Int): Int {
        return (alpha.coerceIn(0, 255) shl 24) or (argb and 0x00FFFFFF)
    }
}
