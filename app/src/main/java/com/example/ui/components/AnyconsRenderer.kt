package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.LruCache
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Immutable
data class AnyconsConfig(
    val style: String = "NONE",
    val primaryColor: Long = 0xFFF4F1EA,
    val secondaryColor: Long = 0xFF24242B,
    val accentColor: Long = 0xFF8D75C7,
    val strokeWidth: Float = 2.0f,
    val filled: Boolean = false,
    val cacheToken: Int = 0,
) {
    fun key() = "$style|$primaryColor|$secondaryColor|$accentColor|$strokeWidth|$filled|$cacheToken"
    companion object {
        val styles = listOf("NONE", "OUTLINE", "MATERIAL", "STICKER", "MARBLE", "WATERCOLOR", "BRIGHT", "STELLAR", "OUTLINE_CHROMA", "DOTS", "DOODLE", "CLICKS")
    }
}

val LocalAnyconsConfig = staticCompositionLocalOf { AnyconsConfig() }

/**
 * Independent bitmap renderer for the eleven Anycons-inspired treatments.
 * It intentionally works on the app's resolved icon source, so icon packs and
 * user-selected icons remain usable as source artwork.
 */
object AnyconsRenderer {
    private val cache = LruCache<String, Bitmap>(220)

    fun render(source: Bitmap, config: AnyconsConfig): Bitmap {
        if (config.style == "NONE" || config.style !in AnyconsConfig.styles) return source
        val key = "${System.identityHashCode(source)}|${source.generationId}|${config.key()}"
        synchronized(cache) { cache.get(key) }?.let { return it }
        val w = min(192, source.width).coerceAtLeast(1)
        val h = min(192, source.height).coerceAtLeast(1)
        val input = Bitmap.createScaledBitmap(source, w, h, true)
        val pixels = IntArray(w * h)
        input.getPixels(pixels, 0, w, 0, 0, w, h)
        val output = IntArray(w * h)
        val primary = config.primaryColor.toInt()
        val secondary = config.secondaryColor.toInt()
        val accent = config.accentColor.toInt()
        val stroke = config.strokeWidth.coerceIn(0.7f, 5f)
        val alphaMask = IntArray(w * h) { (pixels[it] ushr 24) and 255 }
        val edge = IntArray(w * h)
        for (y in 0 until h) for (x in 0 until w) {
            val i = y * w + x
            val a = alphaMask[i]
            var neighbor = 0
            for (dy in -1..1) for (dx in -1..1) {
                val nx = x + dx; val ny = y + dy
                if (nx in 0 until w && ny in 0 until h) neighbor = max(neighbor, alphaMask[ny * w + nx])
            }
            edge[i] = (neighbor - a).coerceIn(0, 255)
        }
        fun channel(c: Int, shift: Int) = (c ushr shift) and 255
        fun blend(a: Int, b: Int, t: Float): Int {
            val f = t.coerceIn(0f, 1f)
            val aa = (channel(a,24) + (channel(b,24)-channel(a,24))*f).toInt()
            val r = (channel(a,16) + (channel(b,16)-channel(a,16))*f).toInt()
            val g = (channel(a,8) + (channel(b,8)-channel(a,8))*f).toInt()
            val bl = (channel(a,0) + (channel(b,0)-channel(a,0))*f).toInt()
            return (aa shl 24) or (r shl 16) or (g shl 8) or bl
        }
        val maxDim = max(w,h).toFloat()
        for (y in 0 until h) for (x in 0 until w) {
            val i = y*w+x; val p = pixels[i]; val a = alphaMask[i]
            if (a == 0) { output[i] = 0; continue }
            val nx = x/maxDim; val ny = y/maxDim
            val grain = ((x*17 + y*31 + (x*y)%23)%19)/19f
            val vein = abs(sin((x*0.12 + y*0.08) + sin(y*0.09)*2.2)).toFloat()
            val color = when(config.style) {
                "OUTLINE" -> if (config.filled) blend(primary, secondary, 0.08f) else primary
                "MATERIAL" -> blend(primary, accent, (0.18f + 0.38f*ny).coerceIn(0f,0.7f))
                "STICKER" -> blend(primary, 0xFFFFFFFF.toInt(), 0.18f)
                "MARBLE" -> blend(primary, accent, (vein*0.48f).coerceIn(0f,0.55f))
                "WATERCOLOR" -> blend(primary, accent, (grain*0.62f).coerceIn(0f,0.45f))
                "BRIGHT" -> {
                    val palette = intArrayOf(0xFFFF5B79.toInt(),0xFFFFB52E.toInt(),0xFF36D6C5.toInt(),0xFF5B8CFF.toInt(),0xFFB66CFF.toInt())
                    palette[((x/ max(1,w/5)) + (y/ max(1,h/5)))%palette.size]
                }
                "STELLAR" -> blend(primary, accent, if ((x*13+y*7)%47 < 5) 0.95f else 0.12f)
                "OUTLINE_CHROMA" -> blend(primary, accent, (0.18f+0.72f*nx).coerceIn(0f,1f))
                "DOTS" -> if ((x%5 in 1..3) && (y%5 in 1..3)) primary else blend(primary,secondary,0.42f)
                "DOODLE" -> if ((x*7+y*11)%13 < 3) blend(primary,accent,0.65f) else primary
                "CLICKS" -> if ((x%8 < 2) || (y%8 < 2)) blend(primary,accent,0.6f) else primary
                else -> p
            }
            val insideAlpha = when(config.style) {
                "WATERCOLOR" -> (a * (0.62f + grain*0.34f)).toInt()
                "DOODLE" -> if ((x*5+y*3)%37 < 2) (a*0.45f).toInt() else a
                else -> a
            }
            output[i] = (insideAlpha.coerceIn(0,255) shl 24) or (color and 0x00FFFFFF)
        }
        val out = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        out.setPixels(output,0,w,0,0,w,h)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when(config.style) {
            "OUTLINE", "OUTLINE_CHROMA" -> {
                val radius = (stroke*maxDim/48f).coerceIn(1f,10f)
                val expanded = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
                val ep = IntArray(w*h)
                for(y in 0 until h) for(x in 0 until w) {
                    var m=0
                    for(dy in -radius.toInt()..radius.toInt()) for(dx in -radius.toInt()..radius.toInt()) {
                        val xx=x+dx; val yy=y+dy
                        if(xx in 0 until w && yy in 0 until h && dx*dx+dy*dy<=radius*radius) m=max(m,alphaMask[yy*w+xx])
                    }
                    ep[y*w+x]=((m*(if(config.style=="OUTLINE_CHROMA") 210 else 255)/255) shl 24) or (secondary and 0x00FFFFFF)
                }
                expanded.setPixels(ep,0,w,0,0,w,h)
                canvas.drawBitmap(expanded,0f,0f,paint)
                canvas.drawBitmap(out,0f,0f,paint)
                expanded.recycle()
            }
            "STICKER" -> {
                val pad=(stroke*maxDim/32f).coerceIn(2f,8f)
                val border=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
                val bp=IntArray(w*h)
                for(y in 0 until h) for(x in 0 until w) {
                    var m=0
                    for(dy in -pad.toInt()..pad.toInt()) for(dx in -pad.toInt()..pad.toInt()) {
                        val xx=x+dx; val yy=y+dy
                        if(xx in 0 until w && yy in 0 until h) m=max(m,alphaMask[yy*w+xx])
                    }
                    bp[y*w+x]=((m*255/255) shl 24) or 0x00FFFFFF
                }
                border.setPixels(bp,0,w,0,0,w,h)
                canvas.drawBitmap(border,0f,0f,paint)
                canvas.drawBitmap(out,0f,0f,paint)
                border.recycle()
            }
            "STELLAR" -> {
                paint.color = android.graphics.Color.WHITE
                paint.strokeWidth = max(1f,maxDim/90f)
                for(k in 0..7) {
                    val x=((k*37+11)%w).toFloat(); val y=((k*53+19)%h).toFloat()
                    if(alphaMask[(y.toInt()*w+x.toInt()).coerceIn(0,w*h-1)]>0) {
                        canvas.drawLine(x-3,y,x+3,y,paint); canvas.drawLine(x,y-3,x,y+3,paint)
                    }
                }
            }
        }
        input.recycle()
        synchronized(cache) { cache.put(key,out) }
        return out
    }

    fun clearCache() = synchronized(cache) { cache.evictAll() }
}
