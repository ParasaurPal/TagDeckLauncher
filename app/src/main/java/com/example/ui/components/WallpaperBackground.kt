package com.example.ui.components

import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.db.HomePageEntity
import com.example.data.prefs.LauncherSettings
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WallpaperBackground(
    settings: LauncherSettings,
    currentPage: HomePageEntity? = null,
    pageOverrideColorHex: Long? = null,
    pageOverrideImagePath: String? = null,
    pageOverrideAlpha: Float = 0.85f,
    pagerOffsetFraction: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val blurMod = if (settings.wallpaperBlurDp > 0.5f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(settings.wallpaperBlurDp.dp)
    } else {
        Modifier
    }

    val colorFilter = remember(
        settings.wallpaperSaturation,
        settings.wallpaperContrast,
        settings.wallpaperBrightness,
    ) {
        if (settings.wallpaperSaturation == 1f && settings.wallpaperContrast == 1f && settings.wallpaperBrightness == 0f) {
            null
        } else {
            val satMatrix = ColorMatrix().apply { setToSaturation(settings.wallpaperSaturation.coerceIn(0f, 2f)) }
            val c = settings.wallpaperContrast.coerceIn(0.4f, 1.8f)
            val b = settings.wallpaperBrightness.coerceIn(-0.4f, 0.4f) * 255f
            val translate = (1f - c) * 127.5f + b
            val contrastMatrix = ColorMatrix(
                floatArrayOf(
                    c, 0f, 0f, 0f, translate,
                    0f, c, 0f, 0f, translate,
                    0f, 0f, c, 0f, translate,
                    0f, 0f, 0f, 1f, 0f,
                )
            )
            satMatrix.timesAssign(contrastMatrix)
            ColorFilter.colorMatrix(satMatrix)
        }
    }

    val parallaxMod = if (settings.wallpaperParallax > 0.01f && settings.wallpaperType != "SYSTEM_LIVE") {
        Modifier.graphicsLayer {
            val strength = settings.wallpaperParallax.coerceIn(0f, 1f)
            scaleX = 1f + strength * 0.12f
            scaleY = 1f + strength * 0.12f
            translationX = (-pagerOffsetFraction * 28f * strength).coerceIn(-60f, 60f)
        }
    } else Modifier

    Box(modifier = modifier.fillMaxSize()) {
        // Base global wallpaper
        when (settings.wallpaperType) {
            "SYSTEM_LIVE" -> Unit // Transparent home canvas reveals Android's own static or live wallpaper.
            "BUILTIN_TOPO" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_wallpaper_topo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = colorFilter,
                    modifier = Modifier.fillMaxSize().then(parallaxMod).then(blurMod),
                )
            }
            "CUSTOM_IMAGE" -> {
                val file = settings.wallpaperCustomPath?.let { File(it) }?.takeIf { it.exists() }
                if (file != null) {
                    AsyncImage(
                        model = file,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        colorFilter = colorFilter,
                        modifier = Modifier.fillMaxSize().then(parallaxMod).then(blurMod),
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_wallpaper_prism),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        colorFilter = colorFilter,
                        modifier = Modifier.fillMaxSize().then(parallaxMod).then(blurMod),
                    )
                }
            }
            "GRADIENT" -> {
                val gradColors = listOf(
                    Color(settings.wallpaperGradientStartHex),
                    Color(settings.wallpaperGradientEndHex),
                )
                val brush = when (settings.wallpaperGradientDirection) {
                    "HORIZONTAL" -> Brush.horizontalGradient(gradColors)
                    "DIAGONAL" -> Brush.linearGradient(gradColors)
                    else -> Brush.verticalGradient(gradColors)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush)
                        .then(blurMod)
                )
            }
            "SOLID" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(settings.wallpaperSolidHex))
                )
            }
            else -> {
                Image(
                    painter = painterResource(id = R.drawable.img_wallpaper_prism),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = colorFilter,
                    modifier = Modifier.fillMaxSize().then(parallaxMod).then(blurMod),
                )
            }
        }

        // Global wallpaper dim overlay
        if (settings.wallpaperDimAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(settings.wallpaperDimColorHex).copy(alpha = settings.wallpaperDimAlpha.coerceIn(0f, 0.85f)))
            )
        }

        // Radial vignette overlay
        if (settings.wallpaperVignette > 0.01f) {
            val vAlpha = settings.wallpaperVignette.coerceIn(0f, 0.85f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = vAlpha)),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.maxDimension * 0.72f,
                    )
                )
            }
        }

        // Ambient animated background FX
        if (settings.ambientEffectType != "NONE") {
            AmbientWallpaperFxOverlay(
                effectType = settings.ambientEffectType,
                accentColor = Color(settings.accentColorHex),
                secondaryColor = Color(settings.secondaryColorHex),
            )
        }

        // Per-page orientation-aware background override
        val resolvedPageImage = if (currentPage != null) {
            if (isLandscape && !currentPage.landscapeBackgroundImagePath.isNullOrBlank()) {
                currentPage.landscapeBackgroundImagePath
            } else {
                currentPage.backgroundImagePath
            }
        } else {
            pageOverrideImagePath
        }
        val bgType = currentPage?.backgroundType ?: if (pageOverrideImagePath != null) "IMAGE" else if (pageOverrideColorHex != null) "SOLID" else "INHERIT"
        val resolvedColor = currentPage?.backgroundColorHex ?: pageOverrideColorHex
        val resolvedGradEnd = currentPage?.backgroundGradientEndHex
        val resolvedAlpha = (currentPage?.backgroundAlpha ?: pageOverrideAlpha).coerceIn(0f, 1f)
        val pageBlur = if ((currentPage?.backgroundBlurDp ?: 0f) > 0.5f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.blur((currentPage?.backgroundBlurDp ?: 0f).dp)
        } else Modifier

        when (bgType) {
            "TRANSPARENT" -> Unit
            "IMAGE" -> {
                if (!resolvedPageImage.isNullOrBlank() && File(resolvedPageImage).exists()) {
                    AsyncImage(
                        model = File(resolvedPageImage),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alpha = resolvedAlpha,
                        modifier = Modifier.fillMaxSize().then(pageBlur),
                    )
                }
            }
            "GRADIENT" -> {
                val c1 = Color(resolvedColor ?: 0xFF0A1124L).copy(alpha = resolvedAlpha)
                val c2 = Color(resolvedGradEnd ?: 0xFF1A1038L).copy(alpha = resolvedAlpha)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(c1, c2)))
                )
            }
            "SOLID" -> {
                if (resolvedColor != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(resolvedColor).copy(alpha = resolvedAlpha))
                    )
                }
            }
            else -> {
                // INHERIT or legacy fallback
                if (!resolvedPageImage.isNullOrBlank() && File(resolvedPageImage).exists()) {
                    AsyncImage(
                        model = File(resolvedPageImage),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alpha = resolvedAlpha,
                        modifier = Modifier.fillMaxSize().then(pageBlur),
                    )
                } else if (resolvedColor != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(resolvedColor).copy(alpha = resolvedAlpha))
                    )
                }
            }
        }
    }
}

@Composable
private fun AmbientWallpaperFxOverlay(
    effectType: String,
    accentColor: Color,
    secondaryColor: Color,
) {
    val phase by rememberInfiniteTransition(label = "ambient_fx").animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart),
        label = "ambient_phase",
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        when (effectType) {
            "AURORA" -> {
                val c1 = Offset(size.width * (0.3f + 0.15f * cos(phase)), size.height * (0.25f + 0.08f * sin(phase)))
                val c2 = Offset(size.width * (0.72f + 0.14f * sin(phase)), size.height * (0.65f + 0.10f * cos(phase)))
                drawCircle(
                    brush = Brush.radialGradient(listOf(accentColor.copy(alpha = 0.22f), Color.Transparent), center = c1, radius = size.minDimension * 0.65f),
                    radius = size.minDimension * 0.65f,
                    center = c1,
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(secondaryColor.copy(alpha = 0.20f), Color.Transparent), center = c2, radius = size.minDimension * 0.7f),
                    radius = size.minDimension * 0.7f,
                    center = c2,
                )
            }
            "GRID" -> {
                val step = 36.dp.toPx()
                val lineColor = accentColor.copy(alpha = 0.11f)
                var x = 0f
                while (x <= size.width) {
                    drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    x += step
                }
                var y = 0f
                while (y <= size.height) {
                    drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += step
                }
            }
            "DOTS" -> {
                val step = 28.dp.toPx()
                val dotColor = Color.White.copy(alpha = 0.14f)
                var x = step * 0.5f
                while (x < size.width) {
                    var y = step * 0.5f
                    while (y < size.height) {
                        drawCircle(dotColor, radius = 1.6.dp.toPx(), center = Offset(x, y))
                        y += step
                    }
                    x += step
                }
            }
            "BOKEH" -> {
                val circles = listOf(
                    Triple(0.18f, 0.22f, 0.18f),
                    Triple(0.78f, 0.28f, 0.22f),
                    Triple(0.32f, 0.68f, 0.24f),
                    Triple(0.82f, 0.76f, 0.16f),
                )
                circles.forEachIndexed { idx, (fx, fy, fr) ->
                    val driftX = size.width * fx + 18f * cos(phase + idx)
                    val driftY = size.height * fy + 18f * sin(phase + idx)
                    val col = if (idx % 2 == 0) accentColor else secondaryColor
                    drawCircle(
                        color = col.copy(alpha = 0.10f),
                        radius = size.minDimension * fr,
                        center = Offset(driftX, driftY),
                    )
                }
            }
        }
    }
}

@Composable
fun SurfaceBackgroundLayer(
    bgType: String,
    colorHex: Long,
    gradientEndHex: Long? = null,
    imagePath: String? = null,
    blurDp: Float = 0f,
    alpha: Float = 1f,
    modifier: Modifier = Modifier,
) {
    val opacity = alpha.coerceIn(0f, 1f)
    val blurMod = if (blurDp > 0.5f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(blurDp.dp)
    } else Modifier

    Box(modifier = modifier.fillMaxSize()) {
        when (bgType) {
            "GRADIENT" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                Color(colorHex).copy(alpha = opacity),
                                Color(gradientEndHex ?: colorHex).copy(alpha = opacity),
                            )
                            )
                        )
                )
            }
            "IMAGE" -> {
                if (!imagePath.isNullOrBlank() && File(imagePath).exists()) {
                    AsyncImage(
                        model = File(imagePath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alpha = opacity,
                        modifier = Modifier.fillMaxSize().then(blurMod),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(colorHex).copy(alpha = 0.45f * opacity))
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(colorHex).copy(alpha = opacity)))
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize().background(Color(colorHex).copy(alpha = opacity)))
            }
        }
    }
}
