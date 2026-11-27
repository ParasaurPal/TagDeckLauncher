package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.db.AppEntity
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.NotificationBadgeStore
import com.example.data.system.LineIconProfile
import com.example.data.system.LineIconProcessor
import java.io.File
import kotlin.math.roundToInt

val LocalIconPackOrder = staticCompositionLocalOf<List<String>> { emptyList() }
val LocalNotificationBadgesEnabled = staticCompositionLocalOf { false }
val LocalLineIconProfiles = staticCompositionLocalOf<Map<String, LineIconProfile>> { emptyMap() }
val LocalDefaultLineIconProfile = staticCompositionLocalOf<LineIconProfile?> { null }

fun resolveIconShape(shapeKey: String, sizeDp: Dp): Shape = when (shapeKey) {
    "NONE" -> androidx.compose.ui.graphics.RectangleShape
    "CIRCLE" -> CircleShape
    "ROUNDED_SQUARE" -> RoundedCornerShape(sizeDp * 0.20f)
    "DIAMOND" -> androidx.compose.foundation.shape.CutCornerShape(50)
    "HEXAGON" -> androidx.compose.foundation.shape.CutCornerShape(
        topStart = sizeDp * 0.25f,
        topEnd = sizeDp * 0.25f,
        bottomStart = sizeDp * 0.25f,
        bottomEnd = sizeDp * 0.25f,
    )
    else -> RoundedCornerShape(sizeDp * 0.30f) // SQUIRCLE
}

@Composable
fun AppIconView(
    app: AppEntity,
    appDiscovery: AppDiscoveryManager,
    size: Dp = 52.dp,
    iconShapeKey: String = "SQUIRCLE",
    modifier: Modifier = Modifier,
    iconPackOrder: List<String> = LocalIconPackOrder.current,
    iconStyle: IconStyleConfig = LocalIconStyleConfig.current,
) {
    val context = LocalContext.current
    val shape = resolveIconShape(iconShapeKey, size)
    val lineProfile = LocalLineIconProfiles.current[app.packageName] ?: LocalDefaultLineIconProfile.current
    val anyconsConfig = LocalAnyconsConfig.current
    val showNotificationBadges = LocalNotificationBadgesEnabled.current
    val notificationCounts by NotificationBadgeStore.counts.collectAsState()
    val notificationCount = if (showNotificationBadges) notificationCounts[app.packageName] ?: 0 else 0
    val customFile = remember(app.customIconPath) {
        app.customIconPath?.let { File(it) }?.takeIf { it.exists() }
    }
    val initialPackIcon = remember(app.packageName, app.componentName, iconPackOrder, customFile) {
        if (customFile == null && iconPackOrder.isNotEmpty()) {
            appDiscovery.iconPackManager.getCachedIconFromHierarchy(
                app.packageName,
                app.componentName,
                iconPackOrder,
            )
        } else {
            null
        }
    }
    val packIcon: ImageBitmap? by produceState(
        initialValue = initialPackIcon,
        app.packageName,
        app.componentName,
        iconPackOrder,
        customFile,
    ) {
        value = if (customFile != null || iconPackOrder.isEmpty()) {
            null
        } else {
            appDiscovery.iconPackManager.getCachedIconFromHierarchy(
                app.packageName,
                app.componentName,
                iconPackOrder,
            ) ?: appDiscovery.iconPackManager.resolveIconFromHierarchy(
                app.packageName,
                app.componentName,
                iconPackOrder,
            )
        }
    }
    val systemIcon = remember(app.packageName, app.isInstalled) {
        appDiscovery.getCachedIcon(app.packageName)
    }

    val rawBitmap: ImageBitmap? = packIcon ?: systemIcon
    val customLineSource: ImageBitmap? by produceState(
        initialValue = null,
        customFile,
    ) {
        value = if (customFile == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching { BitmapFactory.decodeFile(customFile.absolutePath)?.asImageBitmap() }.getOrNull()
            }
        }
    }
    val lineBaseBitmap = customLineSource ?: rawBitmap
    val lineIconCacheKey = remember(app.packageName, app.componentName, app.customIconPath, iconPackOrder, lineProfile, lineBaseBitmap) {
        "${app.packageName}|${app.componentName}|${app.customIconPath.orEmpty()}|${iconPackOrder.joinToString(",")}|${System.identityHashCode(lineBaseBitmap)}"
    }
    val lineDisplayedBitmap: ImageBitmap? by produceState(
        initialValue = null,
        lineBaseBitmap,
        lineProfile,
        lineIconCacheKey,
    ) {
        value = if (lineBaseBitmap == null || lineProfile == null) {
            null
        } else {
            withContext(Dispatchers.Default) {
                runCatching { LineIconProcessor.render(lineIconCacheKey, lineBaseBitmap, lineProfile).asImageBitmap() }.getOrNull()
            }
        }
    }

    val bitmapKey = remember(app.packageName, app.componentName, iconPackOrder, packIcon != null, app.isInstalled) {
        if (packIcon != null) {
            "pack:${app.packageName}:${app.componentName}:${iconPackOrder.joinToString(",")}"
        } else {
            "sys:${app.packageName}:${app.isInstalled}"
        }
    }
    val cachedStyled = remember(bitmapKey, rawBitmap, iconStyle) {
        if (rawBitmap != null && iconStyle.enabled) {
            IconOutlineProcessor.getCached(bitmapKey, iconStyle)
        } else {
            null
        }
    }
    val displayedBitmap: ImageBitmap? by produceState(
        initialValue = cachedStyled ?: rawBitmap,
        bitmapKey,
        rawBitmap,
        iconStyle,
    ) {
        value = when {
            rawBitmap == null -> null
            !iconStyle.enabled -> rawBitmap
            else -> IconOutlineProcessor.getCached(bitmapKey, iconStyle)
                ?: IconOutlineProcessor.processImageBitmap(bitmapKey, rawBitmap, iconStyle)
        }
    }
    val adaptiveForeground: ImageBitmap? by produceState(
        initialValue = null,
        app.packageName,
        anyconsConfig.style,
        anyconsConfig.sourceMode,
    ) {
        value = if (anyconsConfig.style == "NONE" || anyconsConfig.sourceMode == AnyconsConfig.SOURCE_GLYPHS) {
            null
        } else {
            withContext(Dispatchers.IO) {
                appDiscovery.getAdaptiveForegroundBitmap(app.packageName)
            }
        }
    }
    val rawAnyconsSource = lineDisplayedBitmap ?: customLineSource ?: rawBitmap
    val anyconsBitmap: ImageBitmap? by produceState(
        initialValue = null,
        app.packageName,
        app.displayName,
        app.primaryCategoryId,
        rawAnyconsSource,
        adaptiveForeground,
        anyconsConfig,
    ) {
        value = if (anyconsConfig.style == "NONE") {
            null
        } else {
            withContext(Dispatchers.Default) {
                runCatching {
                    AnyconsRenderer.renderForApp(
                        packageName = app.packageName,
                        appName = app.displayName,
                        categoryId = app.primaryCategoryId,
                        sourceBitmap = rawAnyconsSource?.asAndroidBitmap(),
                        foregroundBitmap = adaptiveForeground?.asAndroidBitmap(),
                        config = anyconsConfig,
                    )?.asImageBitmap()
                }.getOrNull()
            }
        }
    }

    val appliedShape = if (anyconsBitmap != null && AnyconsRenderer.isFreeformStyle(anyconsConfig.style)) {
        androidx.compose.ui.graphics.RectangleShape
    } else {
        shape
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(appliedShape)
            .alpha(app.iconAlpha.coerceIn(0.1f, 1f)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            anyconsBitmap != null -> {
                Image(
                    bitmap = anyconsBitmap!!,
                    contentDescription = app.displayName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = app.iconScale
                            scaleY = app.iconScale
                        },
                )
            }
            lineProfile != null && lineDisplayedBitmap != null -> {
                Image(
                    bitmap = lineDisplayedBitmap!!,
                    contentDescription = app.displayName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = app.iconScale
                            scaleY = app.iconScale
                        },
                )
            }
            customFile != null -> {
                val request = remember(customFile, iconStyle) {
                    ImageRequest.Builder(context)
                        .data(customFile)
                        .allowHardware(false)
                        .apply {
                            if (iconStyle.enabled) {
                                transformations(BlackIconCoilTransformation(iconStyle))
                            }
                        }
                        .build()
                }
                AsyncImage(
                    model = request,
                    contentDescription = app.displayName,
                    contentScale = if (iconStyle.enabled) ContentScale.Fit else ContentScale.Crop,
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
            displayedBitmap != null -> {
                Image(
                    bitmap = displayedBitmap!!,
                    contentDescription = app.displayName,
                    contentScale = if (iconStyle.enabled) ContentScale.Fit else ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = app.iconScale
                            scaleY = app.iconScale
                        },
                )
            }
            else -> {
                val primaryTint = MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    if (iconStyle.enabled && isColorDark(primaryTint, iconStyle.darknessThreshold)) {
                        OutlinedVectorIcon(
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = app.displayName,
                            fillColor = Color(iconStyle.fillColorHex),
                            outlineColor = Color(iconStyle.outlineColorHex),
                            outlineWidthDp = (iconStyle.outlineWidthDp * 0.6f).dp,
                            modifier = Modifier.size(size * 0.56f),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Apps,
                            contentDescription = app.displayName,
                            tint = primaryTint,
                            modifier = Modifier.size(size * 0.56f),
                        )
                    }
                }
            }
        }

        if (notificationCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(1.dp)
                    .size(if (notificationCount > 9) 19.dp else 15.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (notificationCount > 99) "99+" else notificationCount.toString(),
                    color = Color.White,
                    fontSize = if (notificationCount > 9) 8.sp else 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}
