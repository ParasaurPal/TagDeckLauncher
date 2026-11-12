package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.db.AppEntity
import com.example.data.system.AppDiscoveryManager
import java.io.File
import kotlin.math.roundToInt

fun resolveIconShape(shapeKey: String, sizeDp: Dp): Shape = when (shapeKey) {
    "CIRCLE" -> CircleShape
    "ROUNDED_SQUARE" -> RoundedCornerShape(sizeDp * 0.20f)
    else -> RoundedCornerShape(sizeDp * 0.30f) // SQUIRCLE-like smooth curve
}

@Composable
fun AppIconView(
    app: AppEntity,
    appDiscovery: AppDiscoveryManager,
    size: Dp = 52.dp,
    iconShapeKey: String = "SQUIRCLE",
    modifier: Modifier = Modifier,
) {
    val shape = resolveIconShape(iconShapeKey, size)
    val customFile = remember(app.customIconPath) {
        app.customIconPath?.let { File(it) }?.takeIf { it.exists() }
    }
    val systemIcon = remember(app.packageName, app.isInstalled) {
        appDiscovery.getCachedIcon(app.packageName)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .alpha(app.iconAlpha.coerceIn(0.1f, 1f)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            customFile != null -> {
                AsyncImage(
                    model = customFile,
                    contentDescription = app.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset {
                            IntOffset(
                                (app.iconOffsetX * 40f).roundToInt(),
                                (app.iconOffsetY * 40f).roundToInt()
                            )
                        }
                        .graphicsLayer {
                            scaleX = app.iconScale
                            scaleY = app.iconScale
                        },
                )
            }
            systemIcon != null -> {
                Image(
                    bitmap = systemIcon,
                    contentDescription = app.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = app.iconScale
                            scaleY = app.iconScale
                        },
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Apps,
                        contentDescription = app.displayName,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(size * 0.56f),
                    )
                }
            }
        }
    }
}
