package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.LruCache
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Immutable
data class AnyconsConfig(
    val style: String = "NONE",
    val primaryColor: Long = 0xFFF8FAFC,
    val secondaryColor: Long = 0xFF1E293B,
    val accentColor: Long = 0xFF38BDF8,
    val tertiaryColor: Long = 0xFFFB7185,
    val strokeWidth: Float = 2.0f,
    val filled: Boolean = false,
    val isDarkTheme: Boolean = true,
    val sourceMode: String = SOURCE_GLYPHS,
    val colorMode: String = COLOR_AUTO,
    val strokeScale: Float = 1.0f,
    val cacheToken: Int = 0,
) {
    fun key(): String =
        "$style|$primaryColor|$secondaryColor|$accentColor|$tertiaryColor|$strokeWidth|$filled|$isDarkTheme|$sourceMode|$colorMode|$strokeScale|$cacheToken"

    companion object {
        const val SOURCE_GLYPHS = "GLYPHS"
        const val SOURCE_HYBRID = "HYBRID"
        const val SOURCE_APP_ARTWORK = "APP_ARTWORK"

        const val COLOR_AUTO = "AUTO"
        const val COLOR_WHITE = "WHITE"
        const val COLOR_BLACK = "BLACK"
        const val COLOR_ACCENT = "ACCENT"
        const val COLOR_VIBRANT = "VIBRANT"

        val styles = listOf(
            "NONE",
            "OUTLINE",
            "MATERIAL",
            "STICKER",
            "MARBLE",
            "WATERCOLOR",
            "BRIGHT",
            "STELLAR",
            "OUTLINE_CHROMA",
            "DOTS",
            "DOODLE",
            "CLICKS",
        )
    }
}

val LocalAnyconsConfig = staticCompositionLocalOf { AnyconsConfig() }

/**
 * Niagara Launcher-faithful Anycons renderer supporting all 11 icon collections:
 * Outline, Material, Sticker, Marble, Watercolor, Bright, Stellar, Outline Chroma,
 * Dots, Doodle, and Clicks.
 *
 * Supports both Niagara-style minimalist vector glyphs (matched by app & Play category)
 * and cleanly extracted adaptive foreground artwork.
 */
object AnyconsRenderer {
    private const val CANVAS_SIZE = 192
    private val cache = LruCache<String, Bitmap>(260)

    private data class BrightPair(val start: Int, val end: Int, val detail: Int)

    private val vibrantPalettes = listOf(
        BrightPair(0xFFFF5E7E.toInt(), 0xFFFF8A65.toInt(), 0xFFFFFFFF.toInt()), // Coral -> Peach
        BrightPair(0xFFFFA726.toInt(), 0xFFFFD54F.toInt(), 0xFFFFFFFF.toInt()), // Tangerine -> Sun
        BrightPair(0xFF10B981.toInt(), 0xFF34D399.toInt(), 0xFFFFFFFF.toInt()), // Emerald -> Mint
        BrightPair(0xFF06B6D4.toInt(), 0xFF22D3EE.toInt(), 0xFFFFFFFF.toInt()), // Cyan -> Aqua
        BrightPair(0xFF3B82F6.toInt(), 0xFF60A5FA.toInt(), 0xFFFFFFFF.toInt()), // Cobalt -> Sky
        BrightPair(0xFF6366F1.toInt(), 0xFF818CF8.toInt(), 0xFFFFFFFF.toInt()), // Indigo -> Periwinkle
        BrightPair(0xFF8B5CF6.toInt(), 0xFFC084FC.toInt(), 0xFFFFFFFF.toInt()), // Violet -> Lilac
        BrightPair(0xFFEC4899.toInt(), 0xFFF472B6.toInt(), 0xFFFFFFFF.toInt()), // Berry -> Rose
    )

    private val pastelPalettes = listOf(
        BrightPair(0xFFFDA4AF.toInt(), 0xFFFECDD3.toInt(), 0xFF881337.toInt()), // Rose pastel
        BrightPair(0xFFFCD34D.toInt(), 0xFFFDE68A.toInt(), 0xFF78350F.toInt()), // Butter pastel
        BrightPair(0xFF6EE7B7.toInt(), 0xFFA7F3D0.toInt(), 0xFF064E3B.toInt()), // Mint pastel
        BrightPair(0xFF7DD3FC.toInt(), 0xFFBAE6FD.toInt(), 0xFF0C4A6E.toInt()), // Sky pastel
        BrightPair(0xFFA5B4FC.toInt(), 0xFFC7D2FE.toInt(), 0xFF312E81.toInt()), // Periwinkle pastel
        BrightPair(0xFFD8B4FE.toInt(), 0xFFE9D5FF.toInt(), 0xFF4C1D95.toInt()), // Lavender pastel
        BrightPair(0xFFFDBA74.toInt(), 0xFFFED7AA.toInt(), 0xFF7C2D12.toInt()), // Apricot pastel
        BrightPair(0xFF99F6E4.toInt(), 0xFFCCFBF1.toInt(), 0xFF134E4A.toInt()), // Seafoam pastel
    )

    fun isFreeformStyle(style: String): Boolean = style != "NONE" && style != "MATERIAL" && style != "STELLAR"

    fun clearCache() = synchronized(cache) { cache.evictAll() }

    /**
     * Legacy/direct bitmap entry point. Renders the given bitmap using Anycons styling.
     */
    fun render(source: Bitmap, config: AnyconsConfig): Bitmap {
        if (config.style == "NONE" || config.style !in AnyconsConfig.styles) return source
        val key = "bmp|${System.identityHashCode(source)}|${source.generationId}|${config.key()}"
        synchronized(cache) { cache.get(key) }?.let { return it }
        val result = renderFromBitmap(source, seed = System.identityHashCode(source), config = config)
        synchronized(cache) { cache.put(key, result) }
        return result
    }

    /**
     * Full app-aware Niagara Anycons entry point.
     */
    fun renderForApp(
        packageName: String,
        appName: String,
        categoryId: Long,
        sourceBitmap: Bitmap?,
        foregroundBitmap: Bitmap?,
        config: AnyconsConfig,
    ): Bitmap? {
        if (config.style == "NONE" || config.style !in AnyconsConfig.styles) return sourceBitmap

        val exactGlyphKey = AnyconsGlyphCatalog.resolveExactAppGlyphKey(packageName, appName)
        val useGlyph = when (config.sourceMode) {
            AnyconsConfig.SOURCE_APP_ARTWORK -> false
            AnyconsConfig.SOURCE_HYBRID -> exactGlyphKey != null || (foregroundBitmap == null && sourceBitmap == null)
            else -> true
        }

        if (useGlyph) {
            val glyphKey = exactGlyphKey ?: AnyconsGlyphCatalog.resolveGlyphKey(packageName, appName, categoryId)
            val appSeed = abs((packageName.hashCode() * 31) xor appName.hashCode())
            val key = "glyph|$glyphKey|$appSeed|${config.key()}"
            synchronized(cache) { cache.get(key) }?.let { return it }
            val glyph = AnyconsGlyphCatalog.getGlyph(glyphKey)
            val result = renderVectorGlyph(glyph, appSeed, config)
            synchronized(cache) { cache.put(key, result) }
            return result
        }

        val art = foregroundBitmap ?: sourceBitmap ?: run {
            val glyphKey = AnyconsGlyphCatalog.resolveGlyphKey(packageName, appName, categoryId)
            val appSeed = abs((packageName.hashCode() * 31) xor appName.hashCode())
            return renderVectorGlyph(AnyconsGlyphCatalog.getGlyph(glyphKey), appSeed, config)
        }
        val appSeed = abs((packageName.hashCode() * 31) xor appName.hashCode())
        val key = "art|$packageName|${System.identityHashCode(art)}|${art.generationId}|$appSeed|${config.key()}"
        synchronized(cache) { cache.get(key) }?.let { return it }
        val result = renderFromBitmap(art, seed = appSeed, config = config)
        synchronized(cache) { cache.put(key, result) }
        return result
    }

    // -------------------------------------------------------------------------
    // Vector Glyph Rendering Engine (Authentic Niagara Anycons)
    // -------------------------------------------------------------------------

    private fun renderVectorGlyph(glyph: AnyconsGlyph, appSeed: Int, config: AnyconsConfig): Bitmap {
        val size = CANVAS_SIZE
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val scale = size / 100f
        val strokeScale = config.strokeScale.coerceIn(0.65f, 1.6f)
        val baseStroke = 4.4f * scale * strokeScale

        val palette = resolvePalette(glyph.paletteIndex, appSeed, config)
        val inkColor = resolveMonochromeInk(config, appSeed, glyph.paletteIndex)

        when (config.style) {
            "OUTLINE" -> {
                drawOutlineStyle(canvas, glyph, scale, baseStroke, inkColor, chroma = false)
            }
            "OUTLINE_CHROMA" -> {
                drawOutlineStyle(canvas, glyph, scale, baseStroke, inkColor, chroma = true, seed = appSeed)
            }
            "MATERIAL" -> {
                drawMaterialStyle(canvas, glyph, size, scale, baseStroke, config, palette)
            }
            "STICKER" -> {
                drawStickerStyle(canvas, glyph, size, scale, baseStroke, strokeScale, palette)
            }
            "MARBLE" -> {
                drawMarbleStyle(canvas, glyph, size, scale, baseStroke, appSeed, palette)
            }
            "WATERCOLOR" -> {
                drawWatercolorStyle(canvas, glyph, size, scale, baseStroke, appSeed, palette)
            }
            "BRIGHT" -> {
                drawBrightStyle(canvas, glyph, size, scale, baseStroke, palette)
            }
            "STELLAR" -> {
                drawStellarStyle(canvas, glyph, size, scale, baseStroke, appSeed, config)
            }
            "DOTS" -> {
                drawDotsStyle(canvas, glyph, size, scale, strokeScale, inkColor, config.accentColor.toInt())
            }
            "DOODLE" -> {
                drawDoodleStyle(canvas, glyph, size, scale, baseStroke, appSeed, inkColor, palette)
            }
            "CLICKS" -> {
                drawClicksStyle(canvas, glyph, size, scale, baseStroke, appSeed, config, palette)
            }
        }
        return out
    }

    private fun drawOutlineStyle(
        canvas: Canvas,
        glyph: AnyconsGlyph,
        scale: Float,
        baseStroke: Float,
        inkColor: Int,
        chroma: Boolean,
        seed: Int = 0,
    ) {
        val m = Matrix().apply { setScale(scale, scale) }
        val primary = glyph.primaryStrokes.map { Path(it).apply { transform(m) } }
        val detail = glyph.detailStrokes.map { Path(it).apply { transform(m) } }

        val chromaColors = intArrayOf(
            0xFF22D3EE.toInt(),
            0xFF818CF8.toInt(),
            0xFFF472B6.toInt(),
            0xFFFBBF24.toInt(),
        )
        val angleOffset = (seed % 4) * 18f
        val shader = if (chroma) {
            LinearGradient(
                18f * scale + angleOffset,
                18f * scale,
                82f * scale,
                82f * scale - angleOffset,
                chromaColors,
                floatArrayOf(0f, 0.34f, 0.68f, 1f),
                Shader.TileMode.CLAMP,
            )
        } else null

        if (chroma) {
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                strokeWidth = baseStroke * 1.45f
                this.shader = shader
                alpha = 95
                maskFilter = BlurMaskFilter(4.5f, BlurMaskFilter.Blur.NORMAL)
            }
            primary.forEach { canvas.drawPath(it, glowPaint) }
            detail.forEach { canvas.drawPath(it, glowPaint) }
        } else {
            // Subtle wallpaper legibility halo without muddying the crisp stroke
            val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                strokeWidth = baseStroke + 2.2f
                color = if (luminanceOf(inkColor) > 0.5f) 0x44000000 else 0x44FFFFFF
                maskFilter = BlurMaskFilter(2.0f, BlurMaskFilter.Blur.NORMAL)
            }
            primary.forEach { canvas.drawPath(it, haloPaint) }
            detail.forEach { canvas.drawPath(it, haloPaint) }
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke
            if (shader != null) this.shader = shader else color = inkColor
        }
        primary.forEach { canvas.drawPath(it, strokePaint) }

        val detailPaint = Paint(strokePaint).apply {
            strokeWidth = baseStroke * 0.88f
        }
        detail.forEach { canvas.drawPath(it, detailPaint) }
    }

    private fun drawMaterialStyle(
        canvas: Canvas,
        glyph: AnyconsGlyph,
        size: Int,
        scale: Float,
        baseStroke: Float,
        config: AnyconsConfig,
        palette: BrightPair,
    ) {
        val accent = config.accentColor.toInt()
        val containerColor = when (config.colorMode) {
            AnyconsConfig.COLOR_VIBRANT -> blendColors(palette.start, 0xFFFFFFFF.toInt(), 0.72f)
            AnyconsConfig.COLOR_WHITE -> 0xFFF1F5F9.toInt()
            AnyconsConfig.COLOR_BLACK -> 0xFF1E293B.toInt()
            else -> if (config.isDarkTheme) {
                blendColors(accent, 0xFF1A2236.toInt(), 0.74f)
            } else {
                blendColors(accent, 0xFFFFFFFF.toInt(), 0.78f)
            }
        }
        val glyphColor = when (config.colorMode) {
            AnyconsConfig.COLOR_VIBRANT -> blendColors(palette.start, 0xFF0F172A.toInt(), 0.48f)
            AnyconsConfig.COLOR_WHITE -> 0xFF0F172A.toInt()
            AnyconsConfig.COLOR_BLACK -> 0xFFF8FAFC.toInt()
            else -> if (config.isDarkTheme) {
                blendColors(accent, 0xFFFFFFFF.toInt(), 0.52f)
            } else {
                blendColors(accent, 0xFF0F172A.toInt(), 0.45f)
            }
        }
        val detailTint = blendColors(glyphColor, containerColor, 0.45f)

        // Material You soft circle/squircle container
        val pad = size * 0.06f
        val bgRect = RectF(pad, pad, size - pad, size - pad)
        val radius = size * 0.34f
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, pad, 0f, size - pad,
                blendColors(containerColor, 0xFFFFFFFF.toInt(), 0.06f),
                blendColors(containerColor, 0xFF000000.toInt(), 0.08f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(bgRect, radius, radius, bgPaint)

        val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
            color = blendColors(glyphColor, containerColor, 0.72f)
        }
        canvas.drawRoundRect(bgRect, radius, radius, rimPaint)

        // Centered 56% glyph inside container
        val innerScale = scale * 0.62f
        val offset = (size - 100f * innerScale) / 2f
        val m = Matrix().apply {
            setScale(innerScale, innerScale)
            postTranslate(offset, offset)
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = glyphColor
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 0.78f
            color = glyphColor
        }
        glyph.primaryStrokes.forEach { raw ->
            val p = Path(raw).apply { transform(m) }
            canvas.drawPath(p, fillPaint)
            canvas.drawPath(p, strokePaint)
        }

        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 0.62f
            color = containerColor
        }
        val detailFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = detailTint
        }
        glyph.detailStrokes.forEach { raw ->
            val p = Path(raw).apply { transform(m) }
            canvas.drawPath(p, detailFill)
            canvas.drawPath(p, detailPaint)
        }
    }

    private fun drawStickerStyle(
        canvas: Canvas,
        glyph: AnyconsGlyph,
        size: Int,
        scale: Float,
        baseStroke: Float,
        strokeScale: Float,
        palette: BrightPair,
    ) {
        val innerScale = scale * 0.80f
        val offset = (size - 100f * innerScale) / 2f
        val m = Matrix().apply {
            setScale(innerScale, innerScale)
            postTranslate(offset, offset)
        }
        val primary = glyph.primaryStrokes.map { Path(it).apply { transform(m) } }
        val detail = glyph.detailStrokes.map { Path(it).apply { transform(m) } }

        val borderWidth = 11.5f * scale * strokeScale

        // 1. Soft die-cut drop shadow
        val shadowLayer = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val shadowCanvas = Canvas(shadowLayer)
        val shadowStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = borderWidth * 2f
            color = 0x55000000
            maskFilter = BlurMaskFilter(5.5f, BlurMaskFilter.Blur.NORMAL)
        }
        val shadowFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x55000000
            maskFilter = BlurMaskFilter(5.5f, BlurMaskFilter.Blur.NORMAL)
        }
        primary.forEach {
            shadowCanvas.drawPath(it, shadowFill)
            shadowCanvas.drawPath(it, shadowStroke)
        }
        detail.forEach {
            shadowCanvas.drawPath(it, shadowFill)
            shadowCanvas.drawPath(it, shadowStroke)
        }
        canvas.drawBitmap(shadowLayer, 0f, 3.2f, null)
        shadowLayer.recycle()

        // 2. Thick pure-white vinyl sticker border
        val whiteStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = borderWidth * 1.95f
            color = 0xFFFFFFFF.toInt()
        }
        val whiteFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFFFFFFFF.toInt()
        }
        primary.forEach {
            canvas.drawPath(it, whiteFill)
            canvas.drawPath(it, whiteStroke)
        }
        detail.forEach {
            canvas.drawPath(it, whiteFill)
            canvas.drawPath(it, whiteStroke)
        }

        // 3. Vibrant inner sticker artwork
        val artFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = LinearGradient(
                0f, size * 0.15f, size.toFloat(), size * 0.85f,
                palette.start, palette.end, Shader.TileMode.CLAMP,
            )
        }
        val artStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 0.9f
            shader = artFill.shader
        }
        primary.forEach {
            canvas.drawPath(it, artFill)
            canvas.drawPath(it, artStroke)
        }

        val detailStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 0.68f
            color = 0xFFFFFFFF.toInt()
        }
        val detailFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = blendColors(palette.start, 0xFF0F172A.toInt(), 0.42f)
        }
        detail.forEach {
            canvas.drawPath(it, detailFill)
            canvas.drawPath(it, detailStroke)
        }
    }

    private fun drawMarbleStyle(
        canvas: Canvas,
        glyph: AnyconsGlyph,
        size: Int,
        scale: Float,
        baseStroke: Float,
        seed: Int,
        palette: BrightPair,
    ) {
        val m = Matrix().apply { setScale(scale, scale) }
        val primary = glyph.primaryStrokes.map { Path(it).apply { transform(m) } }
        val detail = glyph.detailStrokes.map { Path(it).apply { transform(m) } }

        // Render stone mask first
        val maskBmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(maskBmp)
        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFFFFFFFF.toInt()
        }
        val maskStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 2.1f
            color = 0xFFFFFFFF.toInt()
        }
        primary.forEach {
            maskCanvas.drawPath(it, maskPaint)
            maskCanvas.drawPath(it, maskStroke)
        }

        // Paint marble texture with mineral veins
        val texBmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val texCanvas = Canvas(texBmp)
        val baseCream = blendColors(0xFFF6F3EE.toInt(), palette.end, 0.16f)
        val coolStone = blendColors(0xFFE6E8EF.toInt(), palette.start, 0.14f)
        texCanvas.drawRect(
            0f, 0f, size.toFloat(), size.toFloat(),
            Paint().apply {
                shader = LinearGradient(0f, 0f, size.toFloat(), size.toFloat(), baseCream, coolStone, Shader.TileMode.CLAMP)
            }
        )

        val veinColor1 = blendColors(0xFF64748B.toInt(), palette.start, 0.25f)
        val veinColor2 = 0xFFC8A96A.toInt() // subtle champagne gold mineral vein
        val phase = (seed % 100) * 0.09f
        for (v in 0..4) {
            val veinPath = Path()
            val yBase = size * (0.14f + v * 0.18f)
            veinPath.moveTo(-10f, yBase)
            for (x in 0..size step 12) {
                val nx = x / size.toFloat()
                val dy = sin(nx * 6.5f + phase + v * 1.4f) * 14f + sin(nx * 13f - phase) * 5f
                veinPath.lineTo(x.toFloat(), yBase + dy)
            }
            val veinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeWidth = if (v % 2 == 0) 2.6f else 1.2f
                color = if (v == 2) veinColor2 else veinColor1
                alpha = if (v == 2) 72 else 48
            }
            texCanvas.drawPath(veinPath, veinPaint)
        }

        // Composite texture inside stone mask
        val masked = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val compCanvas = Canvas(masked)
        compCanvas.drawBitmap(maskBmp, 0f, 0f, null)
        compCanvas.drawBitmap(texBmp, 0f, 0f, Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) })

        // Subtle sculpted drop shadow + draw stone
        canvas.drawBitmap(
            maskBmp,
            1.5f,
            2.5f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = android.graphics.PorterDuffColorFilter(0x38000000, PorterDuff.Mode.SRC_IN)
                maskFilter = BlurMaskFilter(3.5f, BlurMaskFilter.Blur.NORMAL)
            }
        )
        canvas.drawBitmap(masked, 0f, 0f, null)

        // Engraved stone inlay details
        val inlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 0.72f
            color = blendColors(0xFF334155.toInt(), palette.start, 0.35f)
            alpha = 195
        }
        detail.forEach { canvas.drawPath(it, inlayPaint) }

        maskBmp.recycle()
        texBmp.recycle()
        masked.recycle()
    }

    private fun drawWatercolorStyle(
        canvas: Canvas,
        glyph: AnyconsGlyph,
        size: Int,
        scale: Float,
        baseStroke: Float,
        seed: Int,
        palette: BrightPair,
    ) {
        val m = Matrix().apply { setScale(scale, scale) }
        val primary = glyph.primaryStrokes.map { Path(it).apply { transform(m) } }
        val detail = glyph.detailStrokes.map { Path(it).apply { transform(m) } }

        // Soft overlapping watercolor pigment washes with slightly offset wet blooms
        val offsets = listOf(-2.4f to -1.8f, 2.2f to 1.6f, 0f to 0f)
        val washColors = listOf(
            palette.start,
            palette.end,
            blendColors(palette.start, palette.end, 0.5f),
        )
        offsets.forEachIndexed { idx, (ox, oy) ->
            val washPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = washColors[idx]
                alpha = if (idx == 2) 155 else 110
                maskFilter = BlurMaskFilter(if (idx == 2) 2.5f else 5.0f, BlurMaskFilter.Blur.NORMAL)
            }
            val washStroke = Paint(washPaint).apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                strokeWidth = baseStroke * 1.65f
            }
            canvas.save()
            val jx = ox + ((seed % 5) - 2) * 0.4f
            val jy = oy + (((seed / 7) % 5) - 2) * 0.4f
            canvas.translate(jx, jy)
            primary.forEach {
                canvas.drawPath(it, washPaint)
                canvas.drawPath(it, washStroke)
            }
            canvas.restore()
        }

        // Wet-edge pigment pooling rim
        val wetEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 0.52f
            color = blendColors(palette.start, 0xFF0F172A.toInt(), 0.35f)
            alpha = 110
        }
        primary.forEach { canvas.drawPath(it, wetEdgePaint) }

        // Soft ink-wash interior details
        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 0.72f
            color = 0xFFFFFFFF.toInt()
            alpha = 215
        }
        detail.forEach { canvas.drawPath(it, detailPaint) }
    }

    private fun drawBrightStyle(
        canvas: Canvas,
        glyph: AnyconsGlyph,
        size: Int,
        scale: Float,
        baseStroke: Float,
        palette: BrightPair,
    ) {
        val m = Matrix().apply { setScale(scale, scale) }
        val primary = glyph.primaryStrokes.map { Path(it).apply { transform(m) } }
        val detail = glyph.detailStrokes.map { Path(it).apply { transform(m) } }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = LinearGradient(
                0f, size * 0.15f, size.toFloat(), size * 0.85f,
                palette.start, palette.end, Shader.TileMode.CLAMP,
            )
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 1.45f
            shader = fillPaint.shader
        }
        primary.forEach {
            canvas.drawPath(it, fillPaint)
            canvas.drawPath(it, strokePaint)
        }

        // Glossy top-left sheen
        val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 0.38f
            color = 0x55FFFFFF
        }
        canvas.save()
        canvas.translate(-1.2f, -1.5f)
        primary.forEach { canvas.drawPath(it, sheenPaint) }
        canvas.restore()

        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 0.78f
            color = palette.detail
        }
        val detailFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = palette.detail
        }
        detail.forEach {
            canvas.drawPath(it, detailFill)
            canvas.drawPath(it, detailPaint)
        }
    }

    private fun drawStellarStyle(
        canvas: Canvas,
        glyph: AnyconsGlyph,
        size: Int,
        scale: Float,
        baseStroke: Float,
        seed: Int,
        config: AnyconsConfig,
    ) {
        val pad = size * 0.06f
        val rect = RectF(pad, pad, size - pad, size - pad)
        val radius = size * 0.34f

        // Cosmic twilight gradient tile
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, pad, size.toFloat(), size - pad,
                intArrayOf(0xFF0B1026.toInt(), 0xFF1E1B4B.toInt(), 0xFF4C1D95.toInt()),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(rect, radius, radius, bgPaint)

        // Subtle nebula glow
        val nebulaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                size * 0.32f, size * 0.28f, size * 0.55f,
                intArrayOf(blendColors(config.accentColor.toInt(), 0xFF818CF8.toInt(), 0.5f) and 0x55FFFFFF, 0x00000000),
                null,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(rect, radius, radius, nebulaPaint)

        // Deterministic twinkling starfield + crisp 4-point diamond sparkles
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        for (i in 0..11) {
            val sx = size * (0.16f + ((seed * 17 + i * 37) % 68) / 100f)
            val sy = size * (0.16f + ((seed * 29 + i * 53) % 68) / 100f)
            starPaint.color = if (i % 3 == 0) 0xFFFDE68A.toInt() else 0xFFE0F2FE.toInt()
            starPaint.alpha = 110 + (i % 4) * 35
            canvas.drawCircle(sx, sy, if (i % 4 == 0) 1.8f else 1.1f, starPaint)
        }

        fun drawFourPointStar(cx: Float, cy: Float, r: Float, color: Int) {
            val p = Path().apply {
                moveTo(cx, cy - r)
                quadTo(cx + r * 0.18f, cy - r * 0.18f, cx + r, cy)
                quadTo(cx + r * 0.18f, cy + r * 0.18f, cx, cy + r)
                quadTo(cx - r * 0.18f, cy + r * 0.18f, cx - r, cy)
                quadTo(cx - r * 0.18f, cy - r * 0.18f, cx, cy - r)
                close()
            }
            canvas.drawPath(p, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
        }
        drawFourPointStar(size * 0.26f, size * 0.26f, 6.5f, 0xFFFDE68A.toInt())
        drawFourPointStar(size * 0.76f, size * 0.30f, 4.8f, 0xFFBAE6FD.toInt())

        // Luminous stardust glyph centered inside
        val innerScale = scale * 0.58f
        val offset = (size - 100f * innerScale) / 2f
        val m = Matrix().apply {
            setScale(innerScale, innerScale)
            postTranslate(offset, offset)
        }
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 1.1f
            color = 0x88FDE68A.toInt()
            maskFilter = BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL)
        }
        val glyphStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 0.8f
            color = 0xFFFEF9C3.toInt()
        }
        val glyphFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFFF8FAFC.toInt()
        }
        glyph.primaryStrokes.forEach { raw ->
            val p = Path(raw).apply { transform(m) }
            canvas.drawPath(p, glowPaint)
            canvas.drawPath(p, glyphFill)
            canvas.drawPath(p, glyphStroke)
        }
        val detailStroke = Paint(glyphStroke).apply {
            color = 0xFF312E81.toInt()
            strokeWidth = baseStroke * 0.55f
        }
        glyph.detailStrokes.forEach { raw ->
            val p = Path(raw).apply { transform(m) }
            canvas.drawPath(p, detailStroke)
        }
    }

    private fun drawDotsStyle(
        canvas: Canvas,
        glyph: AnyconsGlyph,
        size: Int,
        scale: Float,
        strokeScale: Float,
        inkColor: Int,
        accentColor: Int,
    ) {
        val grid = 15
        val sampleSize = grid * 4
        val sScale = sampleSize / 100f
        val m = Matrix().apply { setScale(sScale, sScale) }

        val primaryMask = Bitmap.createBitmap(sampleSize, sampleSize, Bitmap.Config.ARGB_8888)
        val pCanvas = Canvas(primaryMask)
        val pPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = 6.2f * sScale * strokeScale
            color = 0xFFFFFFFF.toInt()
        }
        val pFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFFFFFFFF.toInt()
        }
        glyph.primaryStrokes.forEach { raw ->
            val p = Path(raw).apply { transform(m) }
            pCanvas.drawPath(p, pPaint)
            if (glyph.detailStrokes.isEmpty()) pCanvas.drawPath(p, pFill)
        }

        val detailMask = Bitmap.createBitmap(sampleSize, sampleSize, Bitmap.Config.ARGB_8888)
        val dCanvas = Canvas(detailMask)
        val dPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = 5.6f * sScale * strokeScale
            color = 0xFFFFFFFF.toInt()
        }
        glyph.detailStrokes.forEach { raw ->
            val p = Path(raw).apply { transform(m) }
            dCanvas.drawPath(p, dPaint)
            dCanvas.drawPath(p, pFill)
        }

        val pPixels = IntArray(sampleSize * sampleSize)
        val dPixels = IntArray(sampleSize * sampleSize)
        primaryMask.getPixels(pPixels, 0, sampleSize, 0, 0, sampleSize, sampleSize)
        detailMask.getPixels(dPixels, 0, sampleSize, 0, 0, sampleSize, sampleSize)
        primaryMask.recycle()
        detailMask.recycle()

        val margin = size * 0.08f
        val cellPitch = (size - margin * 2f) / grid
        val maxRadius = cellPitch * 0.37f
        val primaryDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = inkColor
        }
        val accentDotColor = if (accentColor == inkColor) 0xFFEF4444.toInt() else 0xFFEF4444.toInt()
        val detailDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = accentDotColor
        }

        for (gy in 0 until grid) {
            for (gx in 0 until grid) {
                var pCov = 0f
                var dCov = 0f
                for (sy in 0 until 4) {
                    for (sx in 0 until 4) {
                        val idx = (gy * 4 + sy) * sampleSize + (gx * 4 + sx)
                        pCov += ((pPixels[idx] ushr 24) and 255) / 255f
                        dCov += ((dPixels[idx] ushr 24) and 255) / 255f
                    }
                }
                pCov /= 16f
                dCov /= 16f
                val cx = margin + (gx + 0.5f) * cellPitch
                val cy = margin + (gy + 0.5f) * cellPitch
                if (dCov > 0.22f) {
                    canvas.drawCircle(cx, cy, maxRadius * (0.65f + 0.35f * dCov.coerceIn(0f, 1f)), detailDotPaint)
                } else if (pCov > 0.22f) {
                    canvas.drawCircle(cx, cy, maxRadius * (0.65f + 0.35f * pCov.coerceIn(0f, 1f)), primaryDotPaint)
                }
            }
        }
    }

    private fun drawDoodleStyle(
        canvas: Canvas,
        glyph: AnyconsGlyph,
        size: Int,
        scale: Float,
        baseStroke: Float,
        seed: Int,
        inkColor: Int,
        palette: BrightPair,
    ) {
        val m = Matrix().apply { setScale(scale, scale) }
        val primary = glyph.primaryStrokes.map { Path(it).apply { transform(m) } }
        val detail = glyph.detailStrokes.map { Path(it).apply { transform(m) } }

        // 1. Offset hand-colored pastel marker wash
        val markerColor = blendColors(palette.start, 0xFFFFFFFF.toInt(), 0.30f)
        val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = markerColor
            alpha = 180
        }
        val markerStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 1.4f
            color = markerColor
            alpha = 165
        }
        canvas.save()
        canvas.translate(3.5f, 3.8f)
        primary.forEach {
            canvas.drawPath(it, markerPaint)
            canvas.drawPath(it, markerStroke)
        }
        canvas.restore()

        // 2. Hand-drawn wobbly fine-liner ink strokes
        val inkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 0.92f
            color = inkColor
        }
        primary.forEachIndexed { idx, path ->
            canvas.drawPath(wobblePath(path, seed + idx * 17, amplitude = 2.0f), inkPaint)
        }
        val detailInk = Paint(inkPaint).apply { strokeWidth = baseStroke * 0.78f }
        detail.forEachIndexed { idx, path ->
            canvas.drawPath(wobblePath(path, seed + 101 + idx * 23, amplitude = 1.6f), detailInk)
        }

        // 3. Playful sketch tick accents outside the corner
        val tickPaint = Paint(inkPaint).apply {
            strokeWidth = baseStroke * 0.55f
            alpha = 175
        }
        canvas.drawLine(size * 0.16f, size * 0.16f, size * 0.21f, size * 0.20f, tickPaint)
        canvas.drawLine(size * 0.84f, size * 0.16f, size * 0.79f, size * 0.21f, tickPaint)
    }

    private fun drawClicksStyle(
        canvas: Canvas,
        glyph: AnyconsGlyph,
        size: Int,
        scale: Float,
        baseStroke: Float,
        seed: Int,
        config: AnyconsConfig,
        palette: BrightPair,
    ) {
        val isDarkKey = config.isDarkTheme && config.colorMode != AnyconsConfig.COLOR_WHITE
        val keyFaceTop = when (config.colorMode) {
            AnyconsConfig.COLOR_VIBRANT -> blendColors(palette.start, 0xFFFFFFFF.toInt(), 0.76f)
            AnyconsConfig.COLOR_ACCENT -> blendColors(config.accentColor.toInt(), 0xFF1E293B.toInt(), 0.68f)
            else -> if (isDarkKey) 0xFF2B3242.toInt() else 0xFFF4F1EA.toInt()
        }
        val keyFaceBottom = blendColors(keyFaceTop, 0xFF000000.toInt(), 0.10f)
        val keySkirtColor = blendColors(keyFaceTop, 0xFF000000.toInt(), 0.42f)
        val legendColor = if (luminanceOf(keyFaceTop) > 0.55f) 0xFF1E293B.toInt() else 0xFFF8FAFC.toInt()

        // 1. Keycap drop shadow
        val outerRect = RectF(size * 0.08f, size * 0.10f, size * 0.92f, size * 0.93f)
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x55000000
            maskFilter = BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(
            RectF(outerRect.left, outerRect.top + 3f, outerRect.right, outerRect.bottom + 4f),
            size * 0.20f,
            size * 0.20f,
            shadowPaint,
        )

        // 2. Sculpted 3D keycap base/skirt
        val skirtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, outerRect.top, 0f, outerRect.bottom,
                blendColors(keySkirtColor, 0xFFFFFFFF.toInt(), 0.10f),
                keySkirtColor,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(outerRect, size * 0.20f, size * 0.20f, skirtPaint)

        // 3. Raised sculpted keycap face (shifted upward for 3D bottom lip)
        val faceRect = RectF(size * 0.13f, size * 0.11f, size * 0.87f, size * 0.81f)
        val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, faceRect.top, 0f, faceRect.bottom,
                keyFaceTop,
                keyFaceBottom,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(faceRect, size * 0.16f, size * 0.16f, facePaint)

        // Crisp top highlight rim
        val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
            color = if (isDarkKey) 0x44FFFFFF else 0xAAFFFFFF.toInt()
        }
        canvas.drawRoundRect(faceRect, size * 0.16f, size * 0.16f, rimPaint)

        // 4. Printed legend glyph centered on the keycap face
        val innerScale = scale * 0.52f
        val offsetX = (size - 100f * innerScale) / 2f
        val offsetY = faceRect.centerY() - 50f * innerScale
        val m = Matrix().apply {
            setScale(innerScale, innerScale)
            postTranslate(offsetX, offsetY)
        }
        val legendPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = baseStroke * 0.78f
            color = legendColor
        }
        glyph.primaryStrokes.forEach { raw ->
            canvas.drawPath(Path(raw).apply { transform(m) }, legendPaint)
        }
        val detailLegend = Paint(legendPaint).apply { strokeWidth = baseStroke * 0.64f }
        glyph.detailStrokes.forEach { raw ->
            canvas.drawPath(Path(raw).apply { transform(m) }, detailLegend)
        }

        // 5. Tiny tactile LED accent pip in top-right of keycap
        val ledColor = if (seed % 2 == 0) palette.start else config.accentColor.toInt()
        canvas.drawCircle(
            faceRect.right - size * 0.085f,
            faceRect.top + size * 0.085f,
            size * 0.022f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ledColor },
        )
    }

    // -------------------------------------------------------------------------
    // Adaptive Foreground / Legacy Bitmap Fallback Engine
    // -------------------------------------------------------------------------

    private fun renderFromBitmap(source: Bitmap, seed: Int, config: AnyconsConfig): Bitmap {
        val size = CANVAS_SIZE
        val scaled = scaleToSquare(source, size)
        val fgBitmap = stripUniformPlateIfNeeded(scaled)
        if (fgBitmap !== scaled && scaled !== source) scaled.recycle()

        val w = fgBitmap.width
        val h = fgBitmap.height
        val pixels = IntArray(w * h)
        fgBitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val palette = resolvePalette(abs(seed) % vibrantPalettes.size, seed, config)
        val inkColor = resolveMonochromeInk(config, seed, abs(seed) % vibrantPalettes.size)
        val strokeScale = config.strokeScale.coerceIn(0.65f, 1.6f)

        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)

        when (config.style) {
            "OUTLINE", "OUTLINE_CHROMA" -> {
                val contour = buildThinContourBitmap(pixels, w, h, inkColor, chroma = config.style == "OUTLINE_CHROMA", strokeScale = strokeScale)
                canvas.drawBitmap(contour, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG))
                contour.recycle()
            }
            "MATERIAL" -> {
                val accent = config.accentColor.toInt()
                val containerColor = if (config.isDarkTheme) blendColors(accent, 0xFF1A2236.toInt(), 0.74f) else blendColors(accent, 0xFFFFFFFF.toInt(), 0.78f)
                val fgTint = if (config.isDarkTheme) blendColors(accent, 0xFFFFFFFF.toInt(), 0.52f) else blendColors(accent, 0xFF0F172A.toInt(), 0.45f)
                val pad = size * 0.06f
                canvas.drawRoundRect(RectF(pad, pad, size - pad, size - pad), size * 0.34f, size * 0.34f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = containerColor })
                val mono = tintAlphaSilhouette(pixels, w, h, fgTint)
                val inset = size * 0.20f
                canvas.drawBitmap(mono, null, RectF(inset, inset, size - inset, size - inset), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                mono.recycle()
            }
            "STICKER" -> {
                val pad = size * 0.10f
                val dst = RectF(pad, pad, size - pad, size - pad)
                val whiteBorder = expandAlphaSilhouette(pixels, w, h, radiusPx = (10f * strokeScale).toInt().coerceIn(4, 16), color = 0xFFFFFFFF.toInt())
                val shadow = expandAlphaSilhouette(pixels, w, h, radiusPx = (11f * strokeScale).toInt().coerceIn(4, 16), color = 0x44000000)
                canvas.drawBitmap(shadow, null, RectF(pad, pad + 3f, size - pad, size - pad + 3f), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                canvas.drawBitmap(whiteBorder, null, dst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                canvas.drawBitmap(fgBitmap, null, dst, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                whiteBorder.recycle()
                shadow.recycle()
            }
            "DOTS" -> {
                val grid = 15
                val margin = size * 0.08f
                val pitch = (size - margin * 2f) / grid
                val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = inkColor }
                val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFEF4444.toInt() }
                for (gy in 0 until grid) {
                    for (gx in 0 until grid) {
                        val sx = ((gx + 0.5f) / grid * w).toInt().coerceIn(0, w - 1)
                        val sy = ((gy + 0.5f) / grid * h).toInt().coerceIn(0, h - 1)
                        val px = pixels[sy * w + sx]
                        val a = (px ushr 24) and 255
                        if (a > 40) {
                            val lum = luminanceOf(px)
                            val cx = margin + (gx + 0.5f) * pitch
                            val cy = margin + (gy + 0.5f) * pitch
                            val r = pitch * 0.36f * (0.55f + 0.45f * (a / 255f))
                            canvas.drawCircle(cx, cy, r, if (lum > 0.78f) accentPaint else dotPaint)
                        }
                    }
                }
            }
            "STELLAR" -> {
                val pad = size * 0.06f
                val rect = RectF(pad, pad, size - pad, size - pad)
                canvas.drawRoundRect(
                    rect,
                    size * 0.34f,
                    size * 0.34f,
                    Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        shader = LinearGradient(0f, pad, size.toFloat(), size - pad, 0xFF0B1026.toInt(), 0xFF4C1D95.toInt(), Shader.TileMode.CLAMP)
                    }
                )
                val starTint = tintAlphaSilhouette(pixels, w, h, 0xFFFEF9C3.toInt())
                val inset = size * 0.21f
                canvas.drawBitmap(starTint, null, RectF(inset, inset, size - inset, size - inset), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                starTint.recycle()
            }
            "CLICKS" -> {
                val isDarkKey = config.isDarkTheme && config.colorMode != AnyconsConfig.COLOR_WHITE
                val faceTop = if (isDarkKey) 0xFF2B3242.toInt() else 0xFFF4F1EA.toInt()
                val skirt = blendColors(faceTop, 0xFF000000.toInt(), 0.42f)
                val outer = RectF(size * 0.08f, size * 0.10f, size * 0.92f, size * 0.93f)
                val face = RectF(size * 0.13f, size * 0.11f, size * 0.87f, size * 0.81f)
                canvas.drawRoundRect(outer, size * 0.20f, size * 0.20f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = skirt })
                canvas.drawRoundRect(face, size * 0.16f, size * 0.16f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = faceTop })
                val legendColor = if (luminanceOf(faceTop) > 0.55f) 0xFF1E293B.toInt() else 0xFFF8FAFC.toInt()
                val tinted = tintAlphaSilhouette(pixels, w, h, legendColor)
                val inset = size * 0.24f
                canvas.drawBitmap(tinted, null, RectF(inset, face.centerY() - (size * 0.26f), size - inset, face.centerY() + (size * 0.26f)), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                tinted.recycle()
            }
            else -> {
                // MARBLE, WATERCOLOR, BRIGHT, DOODLE bitmap treatment
                val baseColor = when (config.style) {
                    "MARBLE" -> 0xFFF4F1EA.toInt()
                    "WATERCOLOR" -> blendColors(palette.start, palette.end, 0.45f)
                    "DOODLE" -> blendColors(palette.start, 0xFFFFFFFF.toInt(), 0.28f)
                    else -> palette.start
                }
                val tinted = tintAlphaSilhouette(pixels, w, h, baseColor)
                canvas.drawBitmap(tinted, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG))
                tinted.recycle()
                if (config.style == "DOODLE" || config.style == "MARBLE") {
                    val contour = buildThinContourBitmap(pixels, w, h, if (config.style == "MARBLE") 0xFF334155.toInt() else inkColor, chroma = false, strokeScale = strokeScale * 0.85f)
                    canvas.drawBitmap(contour, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG))
                    contour.recycle()
                }
            }
        }
        if (fgBitmap !== source) fgBitmap.recycle()
        return out
    }

    // -------------------------------------------------------------------------
    // Geometry & Color Helpers
    // -------------------------------------------------------------------------

    private fun resolvePalette(paletteIdx: Int, seed: Int, config: AnyconsConfig): BrightPair {
        val list = if (config.style == "WATERCOLOR" || config.style == "DOODLE") pastelPalettes else vibrantPalettes
        val idx = (abs(paletteIdx) + (abs(seed) % 2)) % list.size
        return list[idx]
    }

    private fun resolveMonochromeInk(config: AnyconsConfig, seed: Int, paletteIdx: Int): Int = when (config.colorMode) {
        AnyconsConfig.COLOR_WHITE -> 0xFFF8FAFC.toInt()
        AnyconsConfig.COLOR_BLACK -> 0xFF1E293B.toInt()
        AnyconsConfig.COLOR_ACCENT -> config.accentColor.toInt()
        AnyconsConfig.COLOR_VIBRANT -> vibrantPalettes[(abs(paletteIdx) + abs(seed)) % vibrantPalettes.size].start
        else -> if (config.isDarkTheme) 0xFFF8FAFC.toInt() else 0xFF1E293B.toInt()
    }

    private fun wobblePath(source: Path, seed: Int, amplitude: Float): Path {
        val out = Path()
        val measure = PathMeasure(source, false)
        val pos = FloatArray(2)
        val tan = FloatArray(2)
        do {
            val len = measure.length
            if (len <= 0f) continue
            val steps = (len / 5.5f).roundToInt().coerceIn(4, 60)
            for (i in 0..steps) {
                val dist = (i.toFloat() / steps) * len
                measure.getPosTan(dist, pos, tan)
                val nx = -tan[1]
                val ny = tan[0]
                val wave = sin(i * 1.9f + seed * 0.37f) * amplitude * 0.65f +
                    cos(i * 3.1f - seed * 0.21f) * amplitude * 0.35f
                val wx = pos[0] + nx * wave
                val wy = pos[1] + ny * wave
                if (i == 0) out.moveTo(wx, wy) else out.lineTo(wx, wy)
            }
        } while (measure.nextContour())
        return out
    }

    private fun scaleToSquare(src: Bitmap, size: Int): Bitmap {
        val soft = if (src.config != Bitmap.Config.ARGB_8888) {
            src.copy(Bitmap.Config.ARGB_8888, false) ?: src
        } else src
        if (soft.width == size && soft.height == size) return soft
        return Bitmap.createScaledBitmap(soft, size, size, true).also {
            if (soft !== src && soft !== it) soft.recycle()
        }
    }

    /**
     * If a legacy app icon has an opaque solid-colored rounded/square background plate,
     * flood-fills the outer border color away so Anycons renders the symbol cleanly.
     */
    private fun stripUniformPlateIfNeeded(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val px = IntArray(w * h)
        src.getPixels(px, 0, w, 0, 0, w, h)
        var transparentCount = 0
        for (c in px) if (((c ushr 24) and 255) < 24) transparentCount++
        if (transparentCount > (w * h) * 0.28f) return src

        // Check mid-edge border pixels to see if there's an opaque uniform plate
        val sampleCoords = listOf(w / 2 to 2, w / 2 to h - 3, 2 to h / 2, w - 3 to h / 2)
        val opaqueSamples = sampleCoords.map { (x, y) -> px[y * w + x] }.filter { ((it ushr 24) and 255) > 200 }
        if (opaqueSamples.size < 3) return src
        val bg = opaqueSamples.first()
        val allSimilar = opaqueSamples.all { colorDist(it, bg) < 34 }
        if (!allSimilar) return src

        val out = px.copyOf()
        val visited = BooleanArray(w * h)
        val queue = IntArray(w * h)
        var head = 0
        var tail = 0
        fun enqueue(idx: Int) {
            if (!visited[idx]) {
                visited[idx] = true
                queue[tail++] = idx
            }
        }
        for (x in 0 until w) {
            enqueue(x)
            enqueue((h - 1) * w + x)
        }
        for (y in 0 until h) {
            enqueue(y * w)
            enqueue(y * w + w - 1)
        }
        while (head < tail) {
            val idx = queue[head++]
            val c = out[idx]
            val a = (c ushr 24) and 255
            if (a < 24 || colorDist(c, bg) < 36) {
                out[idx] = 0
                val x = idx % w
                val y = idx / w
                if (x > 0) enqueue(idx - 1)
                if (x < w - 1) enqueue(idx + 1)
                if (y > 0) enqueue(idx - w)
                if (y < h - 1) enqueue(idx + w)
            }
        }
        // Only apply if enough foreground content remained
        var remaining = 0
        for (c in out) if (((c ushr 24) and 255) >= 32) remaining++
        if (remaining < (w * h) * 0.04f || remaining > (w * h) * 0.85f) return src
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply { setPixels(out, 0, w, 0, 0, w, h) }
    }

    private fun buildThinContourBitmap(
        pixels: IntArray,
        w: Int,
        h: Int,
        inkColor: Int,
        chroma: Boolean,
        strokeScale: Float,
    ): Bitmap {
        val lum = FloatArray(w * h)
        val alpha = FloatArray(w * h)
        for (i in pixels.indices) {
            val p = pixels[i]
            alpha[i] = ((p ushr 24) and 255) / 255f
            lum[i] = luminanceOf(p)
        }
        val mag = FloatArray(w * h)
        for (y in 1 until h - 1) for (x in 1 until w - 1) {
            val i = y * w + x
            val gx = -lum[i - w - 1] - 2f * lum[i - 1] - lum[i + w - 1] + lum[i - w + 1] + 2f * lum[i + 1] + lum[i + w + 1]
            val gy = -lum[i - w - 1] - 2f * lum[i - w] - lum[i - w + 1] + lum[i + w - 1] + 2f * lum[i + w] + lum[i + w + 1]
            val agx = -alpha[i - w - 1] - 2f * alpha[i - 1] - alpha[i + w - 1] + alpha[i - w + 1] + 2f * alpha[i + 1] + alpha[i + w + 1]
            val agy = -alpha[i - w - 1] - 2f * alpha[i - w] - alpha[i - w + 1] + alpha[i + w - 1] + 2f * alpha[i + w] + alpha[i + w + 1]
            mag[i] = max(hypot(gx, gy), hypot(agx, agy) * 1.5f)
        }
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val outPx = IntArray(w * h)
        val radius = (1.6f * strokeScale).roundToInt().coerceIn(1, 4)
        for (y in 1 until h - 1) for (x in 1 until w - 1) {
            val i = y * w + x
            if (mag[i] > 0.35f) {
                for (dy in -radius..radius) for (dx in -radius..radius) {
                    if (dx * dx + dy * dy <= radius * radius) {
                        val nx = x + dx; val ny = y + dy
                        if (nx in 0 until w && ny in 0 until h) {
                            val color = if (chroma) {
                                val t = (nx + ny) / (w + h).toFloat()
                                when {
                                    t < 0.33f -> blendColors(0xFF22D3EE.toInt(), 0xFF818CF8.toInt(), t / 0.33f)
                                    t < 0.66f -> blendColors(0xFF818CF8.toInt(), 0xFFF472B6.toInt(), (t - 0.33f) / 0.33f)
                                    else -> blendColors(0xFFF472B6.toInt(), 0xFFFBBF24.toInt(), (t - 0.66f) / 0.34f)
                                }
                            } else inkColor
                            outPx[ny * w + nx] = (255 shl 24) or (color and 0x00FFFFFF)
                        }
                    }
                }
            }
        }
        out.setPixels(outPx, 0, w, 0, 0, w, h)
        return out
    }

    private fun tintAlphaSilhouette(pixels: IntArray, w: Int, h: Int, tint: Int): Bitmap {
        val outPx = IntArray(w * h)
        val rgb = tint and 0x00FFFFFF
        for (i in pixels.indices) {
            val a = (pixels[i] ushr 24) and 255
            if (a > 0) outPx[i] = (a shl 24) or rgb
        }
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply { setPixels(outPx, 0, w, 0, 0, w, h) }
    }

    private fun expandAlphaSilhouette(pixels: IntArray, w: Int, h: Int, radiusPx: Int, color: Int): Bitmap {
        val outPx = IntArray(w * h)
        val rgb = color and 0x00FFFFFF
        val globalA = (color ushr 24) and 255
        for (y in 0 until h) for (x in 0 until w) {
            var maxA = 0
            for (dy in -radiusPx..radiusPx) for (dx in -radiusPx..radiusPx) {
                if (dx * dx + dy * dy <= radiusPx * radiusPx) {
                    val nx = x + dx; val ny = y + dy
                    if (nx in 0 until w && ny in 0 until h) {
                        val a = (pixels[ny * w + nx] ushr 24) and 255
                        if (a > maxA) {
                            maxA = a
                            if (maxA >= 255) break
                        }
                    }
                }
            }
            if (maxA > 0) {
                val fa = (maxA * globalA) / 255
                outPx[y * w + x] = (fa shl 24) or rgb
            }
        }
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply { setPixels(outPx, 0, w, 0, 0, w, h) }
    }

    private fun colorDist(a: Int, b: Int): Int {
        val dr = abs(((a ushr 16) and 255) - ((b ushr 16) and 255))
        val dg = abs(((a ushr 8) and 255) - ((b ushr 8) and 255))
        val db = abs((a and 255) - (b and 255))
        return max(dr, max(dg, db))
    }

    private fun luminanceOf(c: Int): Float =
        (0.2126f * Color.red(c) + 0.7152f * Color.green(c) + 0.0722f * Color.blue(c)) / 255f

    private fun blendColors(a: Int, b: Int, t: Float): Int {
        val f = t.coerceIn(0f, 1f)
        val aa = (Color.alpha(a) + (Color.alpha(b) - Color.alpha(a)) * f).toInt()
        val rr = (Color.red(a) + (Color.red(b) - Color.red(a)) * f).toInt()
        val gg = (Color.green(a) + (Color.green(b) - Color.green(a)) * f).toInt()
        val bb = (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * f).toInt()
        return Color.argb(aa.coerceIn(0, 255), rr.coerceIn(0, 255), gg.coerceIn(0, 255), bb.coerceIn(0, 255))
    }
}
