package com.example.ui.components

import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.size.Size
import coil.transform.Transformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Immutable
data class IconStyleConfig(
    val enabled: Boolean = true,
    val mode: String = MODE_BLACK_ICONS,
    val fillColorHex: Long = DEFAULT_FILL_COLOR_HEX,
    val outlineColorHex: Long = DEFAULT_OUTLINE_COLOR_HEX,
    val outlineWidthDp: Float = DEFAULT_OUTLINE_WIDTH_DP,
    val darknessThreshold: Float = DEFAULT_DARKNESS_THRESHOLD,
) {
    fun cacheKey(): String =
        "$enabled|$mode|$fillColorHex|$outlineColorHex|${(outlineWidthDp * 10f).roundToInt()}|${(darknessThreshold * 100f).roundToInt()}"

    companion object {
        const val MODE_BLACK_ICONS = "BLACK_ICONS"
        const val MODE_ALL_MONOCHROME = "ALL_MONOCHROME"
        const val MODE_ALL_ICONS_OUTLINE = "ALL_ICONS_OUTLINE"

        const val DEFAULT_FILL_COLOR_HEX = 0xFFFFFFFFL
        const val DEFAULT_OUTLINE_COLOR_HEX = 0xFF000000L
        const val DEFAULT_OUTLINE_WIDTH_DP = 2.0f
        const val DEFAULT_DARKNESS_THRESHOLD = 0.28f
    }
}

val LocalIconStyleConfig = staticCompositionLocalOf { IconStyleConfig() }

fun colorLuminance(color: Color): Float =
    0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue

fun isColorDark(color: Color, threshold: Float = IconStyleConfig.DEFAULT_DARKNESS_THRESHOLD): Boolean =
    color.alpha > 0.1f && colorLuminance(color) <= threshold

object IconOutlineProcessor {

    private const val MAX_PROCESS_DIM_PX = 192
    private val cacheLock = Any()
    private val processedCache = LruCache<String, ImageBitmap>(256)

    fun getCached(key: String, config: IconStyleConfig): ImageBitmap? {
        if (!config.enabled) return null
        val fullKey = "$key|${config.cacheKey()}"
        return synchronized(cacheLock) { processedCache.get(fullKey) }
    }

    fun clearCache() {
        synchronized(cacheLock) { processedCache.evictAll() }
    }

    suspend fun processImageBitmap(
        key: String,
        source: ImageBitmap,
        config: IconStyleConfig,
    ): ImageBitmap {
        if (!config.enabled) return source
        val fullKey = "$key|${config.cacheKey()}"
        synchronized(cacheLock) { processedCache.get(fullKey) }?.let { return it }

        val processed = withContext(Dispatchers.Default) {
            val androidBmp = source.asAndroidBitmap()
            val outBmp = transformBitmap(androidBmp, config)
            if (outBmp === androidBmp) source else outBmp.asImageBitmap()
        }

        synchronized(cacheLock) {
            processedCache.put(fullKey, processed)
        }
        return processed
    }

    fun transformBitmap(source: Bitmap, config: IconStyleConfig): Bitmap {
        if (!config.enabled || source.width <= 0 || source.height <= 0) return source

        val working = prepareSoftwareScaledBitmap(source)
        val w = working.width
        val h = working.height
        val pixels = IntArray(w * h)
        working.getPixels(pixels, 0, w, 0, 0, w, h)

        val analysis = analyzePixels(pixels, w, h, config.darknessThreshold.coerceIn(0.05f, 0.75f))
        if (analysis.opaqueCount == 0) return source

        val isBlackIcon = when {
            analysis.transparentFraction >= 0.08f -> {
                analysis.darkMonoFraction >= 0.65f ||
                    (analysis.darkAnyFraction >= 0.78f && analysis.brightFraction < 0.08f)
            }
            else -> {
                analysis.darkAnyFraction >= 0.90f && analysis.brightFraction < 0.03f
            }
        }
        val isMonochromeIcon = analysis.transparentFraction >= 0.08f && analysis.monoFraction >= 0.80f

        val shouldRecolorToFill = isBlackIcon ||
            (config.mode == IconStyleConfig.MODE_ALL_MONOCHROME && isMonochromeIcon)
        val shouldAddOutline = shouldRecolorToFill ||
            (config.mode == IconStyleConfig.MODE_ALL_ICONS_OUTLINE &&
                (analysis.transparentFraction >= 0.06f || analysis.darkAnyFraction >= 0.90f))

        if (!shouldRecolorToFill && !shouldAddOutline) {
            if (working !== source) working.recycle()
            return source
        }

        val scaleFactor = (max(w, h) / 48f).coerceIn(1f, 4f)
        val radiusPx = (config.outlineWidthDp.coerceIn(0.5f, 6.0f) * scaleFactor).coerceIn(1f, 16f)
        val padNeeded = ceil(radiusPx).toInt().coerceAtLeast(1)

        // Ensure edge-touching artwork gets breathing room so the outline isn't clipped.
        val insetSource = if (hasOpaqueBorder(pixels, w, h, padNeeded)) {
            createInsetBitmap(working, w, h, padNeeded)
        } else {
            working
        }
        val insetPixels = if (insetSource === working) {
            pixels
        } else {
            IntArray(w * h).also { insetSource.getPixels(it, 0, w, 0, 0, w, h) }
        }

        val fillArgb = config.fillColorHex.toInt()
        val fillR = (fillArgb ushr 16) and 0xFF
        val fillG = (fillArgb ushr 8) and 0xFF
        val fillB = fillArgb and 0xFF

        val outlineArgb = config.outlineColorHex.toInt()
        val outR = (outlineArgb ushr 16) and 0xFF
        val outG = (outlineArgb ushr 8) and 0xFF
        val outB = outlineArgb and 0xFF
        val outlineGlobalAlpha = ((outlineArgb ushr 24) and 0xFF).takeIf { it > 0 } ?: 255

        val kernel = buildCircularKernel(radiusPx)
        val outPixels = IntArray(w * h)

        for (y in 0 until h) {
            val rowOffset = y * w
            for (x in 0 until w) {
                val idx = rowOffset + x
                val px = insetPixels[idx]
                val fgA = (px ushr 24) and 0xFF
                val fgR = if (shouldRecolorToFill) fillR else (px ushr 16) and 0xFF
                val fgG = if (shouldRecolorToFill) fillG else (px ushr 8) and 0xFF
                val fgB = if (shouldRecolorToFill) fillB else px and 0xFF

                if (fgA == 255) {
                    outPixels[idx] = (255 shl 24) or (fgR shl 16) or (fgG shl 8) or fgB
                    continue
                }

                var maxOutlineAlpha = 0
                for (k in kernel) {
                    val nx = x + k.dx
                    val ny = y + k.dy
                    if (nx in 0 until w && ny in 0 until h) {
                        val na = (insetPixels[ny * w + nx] ushr 24) and 0xFF
                        if (na > 0) {
                            val candidate = (na * k.weight).roundToInt()
                            if (candidate > maxOutlineAlpha) {
                                maxOutlineAlpha = candidate
                                if (maxOutlineAlpha >= 255) break
                            }
                        }
                    }
                }
                val bgA = (maxOutlineAlpha * outlineGlobalAlpha / 255).coerceIn(0, 255)

                outPixels[idx] = when {
                    fgA == 0 && bgA == 0 -> 0
                    fgA == 0 -> (bgA shl 24) or (outR shl 16) or (outG shl 8) or outB
                    bgA == 0 -> (fgA shl 24) or (fgR shl 16) or (fgG shl 8) or fgB
                    else -> {
                        val sa = fgA / 255f
                        val da = bgA / 255f
                        val oneMinusSa = 1f - sa
                        val compA = (sa + da * oneMinusSa).coerceIn(0.0001f, 1f)
                        val compR = ((fgR * sa + outR * da * oneMinusSa) / compA).roundToInt().coerceIn(0, 255)
                        val compG = ((fgG * sa + outG * da * oneMinusSa) / compA).roundToInt().coerceIn(0, 255)
                        val compB = ((fgB * sa + outB * da * oneMinusSa) / compA).roundToInt().coerceIn(0, 255)
                        val finalA = (compA * 255f).roundToInt().coerceIn(0, 255)
                        (finalA shl 24) or (compR shl 16) or (compG shl 8) or compB
                    }
                }
            }
        }

        if (insetSource !== working && insetSource !== source) insetSource.recycle()
        if (working !== source) working.recycle()

        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(outPixels, 0, w, 0, 0, w, h)
        }
    }

    private fun prepareSoftwareScaledBitmap(source: Bitmap): Bitmap {
        val soft = if (source.config != Bitmap.Config.ARGB_8888) {
            source.copy(Bitmap.Config.ARGB_8888, false) ?: source
        } else {
            source
        }
        val maxDim = max(soft.width, soft.height)
        if (maxDim <= MAX_PROCESS_DIM_PX) return soft
        val ratio = MAX_PROCESS_DIM_PX.toFloat() / maxDim.toFloat()
        val newW = (soft.width * ratio).roundToInt().coerceAtLeast(1)
        val newH = (soft.height * ratio).roundToInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(soft, newW, newH, true)
        if (soft !== source && soft !== scaled) soft.recycle()
        return scaled
    }

    private data class PixelAnalysis(
        val opaqueCount: Int,
        val transparentFraction: Float,
        val darkMonoFraction: Float,
        val darkAnyFraction: Float,
        val monoFraction: Float,
        val brightFraction: Float,
    )

    private fun analyzePixels(pixels: IntArray, w: Int, h: Int, darknessThreshold: Float): PixelAnalysis {
        var opaqueCount = 0
        var transparentCount = 0
        var darkMonoCount = 0
        var darkAnyCount = 0
        var monoCount = 0
        var brightCount = 0

        for (px in pixels) {
            val a = (px ushr 24) and 0xFF
            if (a < 24) {
                transparentCount++
                continue
            }
            opaqueCount++
            val r = (px ushr 16) and 0xFF
            val g = (px ushr 8) and 0xFF
            val b = px and 0xFF

            val lum = (0.2126f * r + 0.7152f * g + 0.0722f * b) / 255f
            val maxC = max(r, max(g, b))
            val minC = min(r, min(g, b))
            val chroma = maxC - minC

            val isMono = chroma <= 48
            val isDark = lum <= darknessThreshold
            if (isMono) monoCount++
            if (isDark) {
                darkAnyCount++
                if (chroma <= 56) darkMonoCount++
            }
            if (lum >= 0.72f) brightCount++
        }

        val total = (w * h).coerceAtLeast(1).toFloat()
        val opq = opaqueCount.coerceAtLeast(1).toFloat()
        return PixelAnalysis(
            opaqueCount = opaqueCount,
            transparentFraction = transparentCount / total,
            darkMonoFraction = darkMonoCount / opq,
            darkAnyFraction = darkAnyCount / opq,
            monoFraction = monoCount / opq,
            brightFraction = brightCount / opq,
        )
    }

    private fun hasOpaqueBorder(pixels: IntArray, w: Int, h: Int, pad: Int): Boolean {
        val margin = pad.coerceAtMost(min(w, h) / 4).coerceAtLeast(1)
        for (y in 0 until h) {
            val row = y * w
            val yOnBorder = y < margin || y >= h - margin
            for (x in 0 until w) {
                if (yOnBorder || x < margin || x >= w - margin) {
                    if (((pixels[row + x] ushr 24) and 0xFF) >= 32) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun createInsetBitmap(src: Bitmap, w: Int, h: Int, pad: Int): Bitmap {
        val safePad = pad.coerceAtMost((min(w, h) / 5).coerceAtLeast(1))
        val targetW = (w - 2 * safePad).coerceAtLeast(1)
        val targetH = (h - 2 * safePad).coerceAtLeast(1)
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(out)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
        val srcRect = android.graphics.Rect(0, 0, src.width, src.height)
        val dstRect = android.graphics.Rect(safePad, safePad, safePad + targetW, safePad + targetH)
        canvas.drawBitmap(src, srcRect, dstRect, paint)
        return out
    }

    private data class KernelOffset(val dx: Int, val dy: Int, val weight: Float)

    private fun buildCircularKernel(radiusPx: Float): Array<KernelOffset> {
        val bound = ceil(radiusPx + 0.85f).toInt()
        val list = ArrayList<KernelOffset>((bound * 2 + 1) * (bound * 2 + 1))
        for (dy in -bound..bound) {
            for (dx in -bound..bound) {
                if (dx == 0 && dy == 0) continue
                val dist = hypot(dx.toFloat(), dy.toFloat())
                val weight = (radiusPx + 0.75f - dist).coerceIn(0f, 1f)
                if (weight > 0.02f) {
                    list.add(KernelOffset(dx, dy, weight))
                }
            }
        }
        return list.toTypedArray()
    }
}

class BlackIconCoilTransformation(
    private val config: IconStyleConfig,
) : Transformation {
    override val cacheKey: String = "tagdeck_black_icon_${config.cacheKey()}"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap = withContext(Dispatchers.Default) {
        IconOutlineProcessor.transformBitmap(input, config)
    }
}

@Composable
fun OutlinedVectorIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    fillColor: Color,
    outlineColor: Color,
    outlineWidthDp: Dp = 1.4.dp,
    modifier: Modifier = Modifier,
) {
    val d = outlineWidthDp.coerceIn(0.6.dp, 3.dp)
    val diag = d * 0.72f
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val offsets = listOf(
            -d to 0.dp,
            d to 0.dp,
            0.dp to -d,
            0.dp to d,
            -diag to -diag,
            diag to -diag,
            -diag to diag,
            diag to diag,
        )
        offsets.forEach { (ox, oy) ->
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = outlineColor,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = ox, y = oy),
            )
        }
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = fillColor,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
