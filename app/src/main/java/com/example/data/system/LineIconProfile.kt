package com.example.data.system

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.LruCache
import java.io.File
import java.security.MessageDigest
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.hypot
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

data class LineIconPoint(val x: Float, val y: Float)

data class LineIconProfile(
    val lineColorHex: Long = 0xFFFFFFFFL,
    val autoEdgesEnabled: Boolean = true,
    val lineWidthPx: Float = 1.8f,
    val outlineEnabled: Boolean = false,
    val outlineColorHex: Long = 0xFF000000L,
    val outlineWidthPx: Float = 1.2f,
    val snapToEdges: Boolean = true,
    val strokes: List<List<LineIconPoint>> = emptyList(),
) {
    companion object {
        fun decodeMap(raw: String): Map<String, LineIconProfile> {
            if (raw.isBlank()) return emptyMap()
            return try {
                val root = JSONObject(raw)
                buildMap {
                    val keys = root.keys()
                    while (keys.hasNext()) {
                        val packageName = keys.next()
                        root.optJSONObject(packageName)?.let { put(packageName, decode(it)) }
                    }
                }
            } catch (_: Exception) {
                emptyMap()
            }
        }

        fun decodeForPackage(raw: String, packageName: String): LineIconProfile? =
            decodeMap(raw)[packageName]

        fun encodeMap(profiles: Map<String, LineIconProfile>): String {
            val root = JSONObject()
            profiles.forEach { (pkg, profile) -> root.put(pkg, encode(profile)) }
            return root.toString()
        }

        private fun decode(value: JSONObject): LineIconProfile {
            val strokesJson = value.optJSONArray("strokes") ?: JSONArray()
            val strokes = buildList {
                for (i in 0 until strokesJson.length()) {
                    val pointsJson = strokesJson.optJSONArray(i) ?: continue
                    val points = buildList {
                        for (j in 0 until pointsJson.length()) {
                            val point = pointsJson.optJSONObject(j) ?: continue
                            add(LineIconPoint(point.optDouble("x").toFloat(), point.optDouble("y").toFloat()))
                        }
                    }
                    if (points.isNotEmpty()) add(points)
                }
            }
            return LineIconProfile(
                lineColorHex = value.optLong("lineColor", 0xFFFFFFFFL),
                autoEdgesEnabled = value.optBoolean("autoEdges", true),
                lineWidthPx = value.optDouble("lineWidth", 1.8).toFloat().coerceIn(0.5f, 6f),
                outlineEnabled = value.optBoolean("outlineEnabled", false),
                outlineColorHex = value.optLong("outlineColor", 0xFF000000L),
                outlineWidthPx = value.optDouble("outlineWidth", 1.2).toFloat().coerceIn(0.5f, 4f),
                snapToEdges = value.optBoolean("snapToEdges", true),
                strokes = strokes,
            )
        }

        private fun encode(profile: LineIconProfile): JSONObject {
            val strokes = JSONArray()
            profile.strokes.forEach { stroke ->
                val points = JSONArray()
                stroke.forEach { point ->
                    points.put(JSONObject().put("x", point.x.coerceIn(0f, 1f)).put("y", point.y.coerceIn(0f, 1f)))
                }
                strokes.put(points)
            }
            return JSONObject()
                .put("lineColor", profile.lineColorHex)
                .put("autoEdges", profile.autoEdgesEnabled)
                .put("lineWidth", profile.lineWidthPx.toDouble())
                .put("outlineEnabled", profile.outlineEnabled)
                .put("outlineColor", profile.outlineColorHex)
                .put("outlineWidth", profile.outlineWidthPx.toDouble())
                .put("snapToEdges", profile.snapToEdges)
                .put("strokes", strokes)
        }
    }
}

object LineIconProcessor {
    const val PREVIEW_SIZE = 192
    private val renderedCache = LruCache<String, Bitmap>(512)
    private data class EdgeMap(val width: Int, val height: Int, val pixels: IntArray, val edges: BooleanArray)

    fun render(source: ImageBitmap, profile: LineIconProfile): Bitmap =
        render(source.asAndroidBitmap(), profile)

    fun getCached(cacheKey: String, profile: LineIconProfile): Bitmap? {
        val key = "$cacheKey|${profile.hashCode()}"
        return synchronized(renderedCache) { renderedCache.get(key) }
    }

    /** Persist generated line art so lazy-list scrolling never has to regenerate it. */
    fun renderCached(context: Context, cacheKey: String, source: ImageBitmap, profile: LineIconProfile): Bitmap {
        getCached(cacheKey, profile)?.let { return it }
        val key = "$cacheKey|${profile.hashCode()}"
        val safeName = MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val cacheDir = File(context.cacheDir, "line-icons").apply { mkdirs() }
        val file = File(cacheDir, "$safeName.png")
        if (file.isFile) {
            BitmapFactory.decodeFile(file.absolutePath)?.let { cached ->
                synchronized(renderedCache) { renderedCache.put(key, cached) }
                return cached
            }
        }
        val rendered = render(source, profile)
        runCatching {
            val temp = File(cacheDir, "$safeName.tmp")
            temp.outputStream().use { output -> rendered.compress(Bitmap.CompressFormat.PNG, 100, output) }
            if (!temp.renameTo(file)) { temp.copyTo(file, overwrite = true); temp.delete() }
        }
        return rendered
    }
    fun render(cacheKey: String, source: ImageBitmap, profile: LineIconProfile): Bitmap {
        val key = "$cacheKey|${profile.hashCode()}"
        synchronized(renderedCache) { renderedCache.get(key) }?.let { return it }
        val rendered = render(source, profile)
        synchronized(renderedCache) { renderedCache.put(key, rendered) }
        return rendered
    }

    fun render(source: Bitmap, profile: LineIconProfile): Bitmap {
        val edgeMap = buildEdgeMap(source)
        val out = Bitmap.createBitmap(edgeMap.width, edgeMap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val pixels = IntArray(edgeMap.width * edgeMap.height)
        if (profile.autoEdgesEnabled) {
            val lineColor = profile.lineColorHex.toInt()
            val lineRadius = ((profile.lineWidthPx - 1f) / 2f).coerceAtLeast(0f)
            val lineRadiusPx = ceil(lineRadius).toInt().coerceAtMost(3)
            val outlineRadiusPx = if (profile.outlineEnabled) {
                (lineRadiusPx + ceil(profile.outlineWidthPx).toInt()).coerceAtMost(6)
            } else 0
            if (profile.outlineEnabled) {
                val outlineColor = profile.outlineColorHex.toInt()
                for (i in edgeMap.edges.indices) {
                    if (!edgeMap.edges[i]) continue
                    val x = i % edgeMap.width
                    val y = i / edgeMap.width
                    for (dy in -outlineRadiusPx..outlineRadiusPx) for (dx in -outlineRadiusPx..outlineRadiusPx) {
                        if (dx * dx + dy * dy > outlineRadiusPx * outlineRadiusPx + 1) continue
                        val nx = x + dx
                        val ny = y + dy
                        if (nx in 0 until edgeMap.width && ny in 0 until edgeMap.height) {
                            pixels[ny * edgeMap.width + nx] = outlineColor
                        }
                    }
                }
            }
            for (i in edgeMap.edges.indices) {
                if (!edgeMap.edges[i]) continue
                val x = i % edgeMap.width
                val y = i / edgeMap.width
                for (dy in -lineRadiusPx..lineRadiusPx) for (dx in -lineRadiusPx..lineRadiusPx) {
                    if (dx * dx + dy * dy > lineRadiusPx * lineRadiusPx + 1) continue
                    val nx = x + dx
                    val ny = y + dy
                    if (nx in 0 until edgeMap.width && ny in 0 until edgeMap.height) {
                        pixels[ny * edgeMap.width + nx] = lineColor
                    }
                }
            }
            out.setPixels(pixels, 0, edgeMap.width, 0, 0, edgeMap.width, edgeMap.height)
        }
        val scale = edgeMap.width / 192f
        profile.strokes.forEach { points ->
            if (points.size < 2) return@forEach
            val path = Path().apply {
                moveTo(points.first().x * edgeMap.width, points.first().y * edgeMap.height)
                points.drop(1).forEach { lineTo(it.x * edgeMap.width, it.y * edgeMap.height) }
            }
            if (profile.outlineEnabled) {
                val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = profile.outlineColorHex.toInt()
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    strokeWidth = (profile.lineWidthPx + profile.outlineWidthPx * 2f) * scale
                }
                canvas.drawPath(path, outline)
            }
            val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = profile.lineColorHex.toInt()
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                strokeWidth = profile.lineWidthPx * scale
            }
            canvas.drawPath(path, line)
        }
        return out
    }

    fun edgePreview(source: ImageBitmap): ImageBitmap =
        edgePreview(source.asAndroidBitmap()).asImageBitmap()

    fun edgePreview(source: Bitmap): Bitmap {
        val map = buildEdgeMap(source)
        return Bitmap.createBitmap(map.width, map.height, Bitmap.Config.ARGB_8888).apply {
            val pixels = IntArray(map.width * map.height)
            for (i in map.edges.indices) if (map.edges[i]) pixels[i] = 0xFFFFFFFF.toInt()
            setPixels(pixels, 0, map.width, 0, 0, map.width, map.height)
        }
    }

    fun createSnapper(source: ImageBitmap, radiusFraction: Float = 0.09f): (LineIconPoint) -> LineIconPoint {
        val map = buildEdgeMap(source.asAndroidBitmap())
        return { point -> snapToEdge(map, point, radiusFraction) }
    }

    fun snapToEdge(source: ImageBitmap, point: LineIconPoint, radiusFraction: Float = 0.09f): LineIconPoint =
        snapToEdge(buildEdgeMap(source.asAndroidBitmap()), point, radiusFraction)

    private fun snapToEdge(map: EdgeMap, point: LineIconPoint, radiusFraction: Float): LineIconPoint {
        val x = (point.x.coerceIn(0f, 1f) * (map.width - 1)).toInt()
        val y = (point.y.coerceIn(0f, 1f) * (map.height - 1)).toInt()
        val radius = (min(map.width, map.height) * radiusFraction).toInt().coerceAtLeast(2)
        var bestX = x
        var bestY = y
        var bestDistance = Float.MAX_VALUE
        for (ny in (y - radius).coerceAtLeast(0)..(y + radius).coerceAtMost(map.height - 1)) {
            for (nx in (x - radius).coerceAtLeast(0)..(x + radius).coerceAtMost(map.width - 1)) {
                if (!map.edges[ny * map.width + nx]) continue
                val distance = hypot((nx - x).toFloat(), (ny - y).toFloat())
                if (distance < bestDistance) {
                    bestDistance = distance
                    bestX = nx
                    bestY = ny
                }
            }
        }
        return if (bestDistance == Float.MAX_VALUE) point else LineIconPoint(
            bestX.toFloat() / (map.width - 1).coerceAtLeast(1),
            bestY.toFloat() / (map.height - 1).coerceAtLeast(1),
        )
    }

    private fun buildEdgeMap(source: Bitmap): EdgeMap {
        val maxDim = max(source.width, source.height).coerceAtLeast(1)
        val ratio = PREVIEW_SIZE.toFloat() / maxDim
        val width = (source.width * ratio).toInt().coerceAtLeast(1)
        val height = (source.height * ratio).toInt().coerceAtLeast(1)
        val working = Bitmap.createScaledBitmap(source, width, height, true)
        val pixels = IntArray(width * height)
        working.getPixels(pixels, 0, width, 0, 0, width, height)
        if (working !== source) working.recycle()
        val luminance = FloatArray(width * height)
        val alpha = FloatArray(width * height)
        for (i in pixels.indices) {
            val p = pixels[i]
            alpha[i] = ((p ushr 24) and 255) / 255f
            val r = (p ushr 16) and 255
            val g = (p ushr 8) and 255
            val b = p and 255
            luminance[i] = (0.2126f * r + 0.7152f * g + 0.0722f * b) / 255f
        }
        val magnitudes = FloatArray(width * height)
        val gradientX = FloatArray(width * height)
        val gradientY = FloatArray(width * height)
        var maxMagnitude = 0f
        for (y in 1 until height - 1) for (x in 1 until width - 1) {
            val i = y * width + x
            val gx = -luminance[i - width - 1] - 2f * luminance[i - 1] - luminance[i + width - 1] +
                luminance[i - width + 1] + 2f * luminance[i + 1] + luminance[i + width + 1]
            val gy = -luminance[i - width - 1] - 2f * luminance[i - width] - luminance[i - width + 1] +
                luminance[i + width - 1] + 2f * luminance[i + width] + luminance[i + width + 1]
            val agx = -alpha[i - width - 1] - 2f * alpha[i - 1] - alpha[i + width - 1] +
                alpha[i - width + 1] + 2f * alpha[i + 1] + alpha[i + width + 1]
            val agy = -alpha[i - width - 1] - 2f * alpha[i - width] - alpha[i - width + 1] +
                alpha[i + width - 1] + 2f * alpha[i + width] + alpha[i + width + 1]
            val colorMagnitude = hypot(gx, gy)
            val alphaMagnitude = hypot(agx, agy) * 1.6f
            val useAlphaGradient = alphaMagnitude > colorMagnitude
            gradientX[i] = if (useAlphaGradient) agx else gx
            gradientY[i] = if (useAlphaGradient) agy else gy
            magnitudes[i] = max(colorMagnitude, alphaMagnitude)
            if (magnitudes[i] > maxMagnitude) maxMagnitude = magnitudes[i]
        }

        // Non-maximum suppression keeps contours thin instead of turning every gradient
        // into a fuzzy band of pixels.
        val suppressed = FloatArray(width * height)
        for (y in 1 until height - 1) for (x in 1 until width - 1) {
            val i = y * width + x
            val gx = gradientX[i]
            val gy = gradientY[i]
            val ax = kotlin.math.abs(gx)
            val ay = kotlin.math.abs(gy)
            val (first, second) = when {
                ax >= ay * 2f -> magnitudes[i - 1] to magnitudes[i + 1]
                ay >= ax * 2f -> magnitudes[i - width] to magnitudes[i + width]
                gx * gy >= 0f -> magnitudes[i - width - 1] to magnitudes[i + width + 1]
                else -> magnitudes[i - width + 1] to magnitudes[i + width - 1]
            }
            if (magnitudes[i] >= first && magnitudes[i] >= second) {
                suppressed[i] = magnitudes[i]
            }
        }

        val threshold = (maxMagnitude * 0.16f).coerceIn(0.10f, 0.38f)
        val edges = BooleanArray(width * height)
        for (y in 1 until height - 1) for (x in 1 until width - 1) {
            val i = y * width + x
            if (suppressed[i] < threshold) continue
            var neighbors = 0
            for (dy in -1..1) for (dx in -1..1) {
                if (dx == 0 && dy == 0) continue
                if (suppressed[(y + dy) * width + x + dx] >= threshold) neighbors++
            }
            edges[i] = neighbors > 0
        }
        return EdgeMap(width, height, pixels, edges)
    }
}
