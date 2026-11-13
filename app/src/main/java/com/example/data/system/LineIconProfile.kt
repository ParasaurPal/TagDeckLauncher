package com.example.data.system

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.hypot
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
    private data class EdgeMap(val width: Int, val height: Int, val pixels: IntArray, val edges: BooleanArray)

    fun render(source: ImageBitmap, profile: LineIconProfile): Bitmap =
        render(source.asAndroidBitmap(), profile)

    fun render(source: Bitmap, profile: LineIconProfile): Bitmap {
        val edgeMap = buildEdgeMap(source)
        val out = Bitmap.createBitmap(edgeMap.width, edgeMap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val pixels = IntArray(edgeMap.width * edgeMap.height)
        if (profile.autoEdgesEnabled) {
            val lineColor = profile.lineColorHex.toInt()
            if (profile.outlineEnabled) {
                val outlineColor = profile.outlineColorHex.toInt()
                val radius = profile.outlineWidthPx.toInt().coerceIn(1, 4)
                for (i in edgeMap.edges.indices) {
                    if (!edgeMap.edges[i]) continue
                    val x = i % edgeMap.width
                    val y = i / edgeMap.width
                    for (dy in -radius..radius) for (dx in -radius..radius) {
                        if (dx * dx + dy * dy > radius * radius + 1) continue
                        val nx = x + dx
                        val ny = y + dy
                        if (nx in 0 until edgeMap.width && ny in 0 until edgeMap.height) {
                            pixels[ny * edgeMap.width + nx] = outlineColor
                        }
                    }
                }
            }
            for (i in edgeMap.edges.indices) if (edgeMap.edges[i]) pixels[i] = lineColor
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

    fun snapToEdge(source: ImageBitmap, point: LineIconPoint, radiusFraction: Float = 0.09f): LineIconPoint {
        val map = buildEdgeMap(source.asAndroidBitmap())
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
        var maxMagnitude = 0f
        for (y in 1 until height - 1) for (x in 1 until width - 1) {
            val i = y * width + x
            val gx = -luminance[i - width - 1] - 2f * luminance[i - 1] - luminance[i + width - 1] +
                luminance[i - width + 1] + 2f * luminance[i + 1] + luminance[i + width + 1]
            val gy = -luminance[i - width - 1] - 2f * luminance[i - width] - luminance[i - width + 1] +
                luminance[i + width - 1] + 2f * luminance[i + width] + luminance[i + width + 1]
            val alphaEdge = max(
                kotlin.math.abs(alpha[i] - alpha[i - 1]),
                kotlin.math.abs(alpha[i] - alpha[i - width]),
            ) * 1.6f
            magnitudes[i] = max(hypot(gx, gy), alphaEdge)
            if (magnitudes[i] > maxMagnitude) maxMagnitude = magnitudes[i]
        }
        val threshold = (maxMagnitude * 0.18f).coerceIn(0.12f, 0.42f)
        val edges = BooleanArray(width * height)
        for (i in magnitudes.indices) edges[i] = magnitudes[i] >= threshold
        return EdgeMap(width, height, pixels, edges)
    }
}
