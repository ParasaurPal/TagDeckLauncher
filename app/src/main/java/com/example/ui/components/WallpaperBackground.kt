package com.example.ui.components

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.db.HomePageEntity
import com.example.data.prefs.LauncherSettings
import java.io.File

@Composable
fun WallpaperBackground(
    settings: LauncherSettings,
    currentPage: HomePageEntity? = null,
    pageOverrideColorHex: Long? = null,
    pageOverrideImagePath: String? = null,
    pageOverrideAlpha: Float = 0.85f,
    modifier: Modifier = Modifier,
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val blurMod = if (settings.wallpaperBlurDp > 0.5f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(settings.wallpaperBlurDp.dp)
    } else {
        Modifier
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Base global wallpaper
        when (settings.wallpaperType) {
            "BUILTIN_TOPO" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_wallpaper_topo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().then(blurMod),
                )
            }
            "CUSTOM_IMAGE" -> {
                val file = settings.wallpaperCustomPath?.let { File(it) }?.takeIf { it.exists() }
                if (file != null) {
                    AsyncImage(
                        model = file,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().then(blurMod),
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_wallpaper_prism),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().then(blurMod),
                    )
                }
            }
            "GRADIENT" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(settings.wallpaperGradientStartHex),
                                    Color(settings.wallpaperGradientEndHex),
                                )
                            )
                        )
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
                    modifier = Modifier.fillMaxSize().then(blurMod),
                )
            }
        }

        // Global wallpaper dim overlay
        if (settings.wallpaperDimAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = settings.wallpaperDimAlpha.coerceIn(0f, 0.85f)))
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
fun SurfaceBackgroundLayer(
    bgType: String,
    colorHex: Long,
    gradientEndHex: Long? = null,
    imagePath: String? = null,
    blurDp: Float = 0f,
    modifier: Modifier = Modifier,
) {
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
                                listOf(Color(colorHex), Color(gradientEndHex ?: colorHex))
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
                        modifier = Modifier.fillMaxSize().then(blurMod),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(colorHex).copy(alpha = 0.45f))
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(colorHex)))
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize().background(Color(colorHex)))
            }
        }
    }
}
