package com.example.ui.screens

import com.example.ui.components.OutlinedLabelText

import android.net.Uri
import android.os.Build
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ViewSidebar
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FlipToBack
import androidx.compose.material.icons.rounded.FlipToFront
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SmartButton
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.db.AppEntity
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.LayoutSnapshotEntity
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.LauncherWidgetHost
import com.example.ui.components.AppIconView
import com.example.ui.components.CategoryBadgeIcon
import com.example.ui.components.ColorSwatchRow
import com.example.ui.components.ObjectImageAndTransformDialog
import com.example.ui.components.StudioDarkSurfaceSwatches
import com.example.ui.components.StudioNeutralAndAccentSwatches
import com.example.ui.components.TagBadgeIcon
import com.example.ui.components.buildElementColorFilter
import com.example.ui.state.LauncherUiState
import com.example.ui.theme.TagPresetColors
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private fun contrastLabelModifier(textColor: Color): Modifier {
    val luminance = textColor.red * 0.299f + textColor.green * 0.587f + textColor.blue * 0.114f
    val scrim = if (luminance > 0.55f) Color.Black.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.14f)
    return Modifier.drawBehind {
        drawRoundRect(color = scrim, cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))
    }
}

private fun resolveMaskShape(element: HomeElementEntity): Shape {
    val extras = runCatching { JSONObject(element.styleExtrasJson) }.getOrNull()
    val base = element.cornerRadiusDp.coerceIn(0f, 74f).toDouble()
    val customCorners = RoundedCornerShape(
        topStart = extras?.optDouble("topStartDp", base)?.toFloat()?.coerceIn(0f, 120f)?.dp ?: element.cornerRadiusDp.dp,
        topEnd = extras?.optDouble("topEndDp", base)?.toFloat()?.coerceIn(0f, 120f)?.dp ?: element.cornerRadiusDp.dp,
        bottomEnd = extras?.optDouble("bottomEndDp", base)?.toFloat()?.coerceIn(0f, 120f)?.dp ?: element.cornerRadiusDp.dp,
        bottomStart = extras?.optDouble("bottomStartDp", base)?.toFloat()?.coerceIn(0f, 120f)?.dp ?: element.cornerRadiusDp.dp,
    )
    return when (element.imageMaskShape) {
    "CIRCLE" -> CircleShape
    "SQUIRCLE" -> RoundedCornerShape((element.widthDp.coerceAtMost(element.heightDp) * 0.28f).dp)
    "DIAMOND" -> GenericShape { size, _ ->
        moveTo(size.width / 2f, 0f)
        lineTo(size.width, size.height / 2f)
        lineTo(size.width / 2f, size.height)
        lineTo(0f, size.height / 2f)
        close()
    }
    "HEXAGON" -> GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.5f, 0f)
        lineTo(w, h * 0.25f)
        lineTo(w, h * 0.75f)
        lineTo(w * 0.5f, h)
        lineTo(0f, h * 0.75f)
        lineTo(0f, h * 0.25f)
        close()
    }
    else -> customCorners
    }
}

@Composable
fun HomeElementBody(
    element: HomeElementEntity,
    state: LauncherUiState,
    appDiscovery: AppDiscoveryManager,
    widgetHost: LauncherWidgetHost,
    onLaunchApp: (AppEntity) -> Unit,
    onOpenTagSidebar: (Long) -> Unit,
    onOpenCategorySidebar: (Long) -> Unit,
    onOpenRuleSidebar: (Long) -> Unit,
    onOpenSidebar: (Long) -> Unit,
    onOpenWindow: (Long) -> Unit,
    onOpenFolder: (Long) -> Unit,
    onElementTrigger: (HomeElementEntity, String) -> Boolean,
    onUpdateElement: (HomeElementEntity) -> Unit = {},
) {
    val context = LocalContext.current
    val shape = resolveMaskShape(element)
    val styleExtras = remember(element.styleExtrasJson) { runCatching { JSONObject(element.styleExtrasJson) }.getOrElse { JSONObject() } }
    val glowRadiusDp = styleExtras.optDouble("glowRadiusDp", 0.0).toFloat().coerceIn(0f, 40f)
    val glowColor = Color(styleExtras.optLong("glowColorHex", element.borderColorHex))
    val isPlainAppShortcut = element.type == "APP_SHORTCUT"
    val bgGradEnd = if (!isPlainAppShortcut) element.backgroundGradientEndHex else null
    val bg = if (element.type == "APP_SHORTCUT" || element.type == "APP_WIDGET" || bgGradEnd != null) Color.Transparent else Color(element.backgroundColorHex)
    val gradBrush = bgGradEnd?.let { endHex ->
        Brush.verticalGradient(listOf(Color(element.backgroundColorHex), Color(endHex)))
    }
    val border: BorderStroke? = if (element.borderWidthDp > 0.05f) BorderStroke(element.borderWidthDp.coerceIn(0f, 8f).dp, Color(element.borderColorHex)) else null
    val colorFilter = remember(
        element.imageBrightness, element.imageContrast, element.imageSaturation,
        element.imageHueDeg, element.imageGrayscale, element.imageTintHex,
    ) { buildElementColorFilter(element) }

    val blurMod = if (element.imageBlurDp > 0.5f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(element.imageBlurDp.dp)
    } else Modifier

    val extraLayers = remember(element.extraImageLayersJson) {
        if (element.extraImageLayersJson.isBlank()) emptyList()
        else try {
            val arr = JSONArray(element.extraImageLayersJson)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    val gestureMod = if (!state.isEditMode && element.actionsConfigJson.isNotBlank()) {
        Modifier.pointerInput(element.elementId, element.actionsConfigJson) {
            detectTapGestures(
                onDoubleTap = { onElementTrigger(element, "DOUBLE_TAP") },
                onLongPress = { onElementTrigger(element, "LONG_PRESS") },
                onTap = {
                    if (!onElementTrigger(element, "TAP")) {
                        // Fall through handled below via default action
                    }
                },
            )
        }
    } else Modifier

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .then(if (glowRadiusDp > 0.1f) Modifier.shadow(glowRadiusDp.dp, shape, ambientColor = glowColor, spotColor = glowColor) else Modifier)
            .then(gestureMod),
        shape = shape,
        color = bg,
        border = border,
        shadowElevation = element.shadowElevationDp.coerceIn(0f, 24f).dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (gradBrush != null) Modifier.background(gradBrush) else Modifier),
        ) {
            // Optional element custom background image
            if (element.type != "IMAGE" && !element.imagePath.isNullOrBlank() && File(element.imagePath).exists()) {
                AsyncImage(
                    model = File(element.imagePath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = colorFilter,
                    alpha = 0.45f,
                    modifier = Modifier.fillMaxSize().then(blurMod),
                )
            }

            when (element.type) {
                "CLOCK_WIDGET", "DIGITAL_CLOCK" -> LiveClockWidgetContent(element)
                "ANALOG_CLOCK" -> AnalogClockWidgetContent(element)
                "CALENDAR_WIDGET" -> CalendarWidgetContent(element)
                "BATTERY_WIDGET" -> BatteryWidgetContent(element)
                "STORAGE_RAM_WIDGET" -> StorageRamWidgetContent(element)
                "COMPASS_WIDGET" -> CompassWidgetContent(element)
                "MEDIA_WIDGET" -> MediaWidgetContent(element)
                "WEATHER_WIDGET" -> WeatherWidgetContent(element)
                "AT_A_GLANCE_WIDGET" -> AtAGlanceWidgetContent(element)
                "APP_USAGE_WIDGET" -> AppUsageWidgetContent(element)
                "RECENT_APPS_WIDGET" -> RecentAppsWidgetContent(state.appsWithTags, onLaunchApp)
                "PHOTO_SLIDESHOW" -> PhotoSlideshowWidgetContent(element, state.imageResources)
                "CONTAINER" -> {
                    val children = state.homeElements.filter { it.parentContainerId == element.elementId && it.visible }
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        if (children.isEmpty()) {
                            Text("Assign objects to this container in Layout settings", color = Color(element.textColorHex), modifier = Modifier.align(Alignment.Center))
                        }
                        children.forEach { child ->
                            Box(
                                modifier = Modifier
                                    .offset(x = maxWidth * child.xFraction.coerceIn(0f, 1f), y = maxHeight * child.yFraction.coerceIn(0f, 1f))
                                    .size(child.widthDp.dp, child.heightDp.dp),
                            ) {
                                HomeElementBody(
                                    element = child,
                                    state = state,
                                    appDiscovery = appDiscovery,
                                    widgetHost = widgetHost,
                                    onLaunchApp = onLaunchApp,
                                    onOpenTagSidebar = onOpenTagSidebar,
                                    onOpenCategorySidebar = onOpenCategorySidebar,
                                    onOpenRuleSidebar = onOpenRuleSidebar,
                                    onOpenSidebar = onOpenSidebar,
                                    onOpenWindow = onOpenWindow,
                                    onOpenFolder = onOpenFolder,
                                    onElementTrigger = onElementTrigger,
                                    onUpdateElement = onUpdateElement,
                                )
                            }
                        }
                    }
                }
                "WIDGET_STACK" -> {
                    val stackIds = element.widgetStackItemsJson.split(",").mapNotNull { it.trim().toLongOrNull() }
                    val stackItems = stackIds.mapNotNull { id -> state.homeElements.firstOrNull { it.elementId == id && it.type != "WIDGET_STACK" } }
                    val activeIndex = element.widgetStackIndex.coerceIn(0, (stackItems.size - 1).coerceAtLeast(0))
                    val activeItem = stackItems.getOrNull(activeIndex)
                    Box(
                        modifier = Modifier.fillMaxSize().then(
                            if (stackItems.size > 1 && element.widgetStackSwipeEnabled) Modifier.pointerInput(element.elementId, activeIndex, stackItems.size) {
                                var dragTotal = 0f
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { change, amount -> dragTotal += amount; change.consume() },
                                    onDragEnd = {
                                        if (dragTotal < -32f) onUpdateElement(element.copy(widgetStackIndex = (activeIndex + 1) % stackItems.size))
                                        else if (dragTotal > 32f) onUpdateElement(element.copy(widgetStackIndex = (activeIndex - 1 + stackItems.size) % stackItems.size))
                                    },
                                )
                            } else Modifier
                        ),
                    ) {
                        if (activeItem != null) {
                            HomeElementBody(
                                element = activeItem.copy(widthDp = element.widthDp, heightDp = element.heightDp),
                                state = state,
                                appDiscovery = appDiscovery,
                                widgetHost = widgetHost,
                                onLaunchApp = onLaunchApp,
                                onOpenTagSidebar = onOpenTagSidebar,
                                onOpenCategorySidebar = onOpenCategorySidebar,
                                onOpenRuleSidebar = onOpenRuleSidebar,
                                onOpenSidebar = onOpenSidebar,
                                onOpenWindow = onOpenWindow,
                                onOpenFolder = onOpenFolder,
                                onElementTrigger = onElementTrigger,
                                onUpdateElement = onUpdateElement,
                            )
                        } else {
                            Text("Add items to this widget stack", color = Color(element.textColorHex), modifier = Modifier.align(Alignment.Center))
                        }
                        if (stackItems.size > 1 && element.widgetStackSwipeEnabled) {
                            Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = 0.58f)), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { onUpdateElement(element.copy(widgetStackIndex = (activeIndex - 1 + stackItems.size) % stackItems.size)) }) { Text("‹") }
                                Text("${activeIndex + 1} / ${stackItems.size}", color = Color.White)
                                TextButton(onClick = { onUpdateElement(element.copy(widgetStackIndex = (activeIndex + 1) % stackItems.size)) }) { Text("›") }
                            }
                        }
                    }
                }

                "TEXT_LABEL" -> {
                    val resolvedText = resolveDynamicTokens(element.label.ifBlank { "Label" }, context)
                    Box(
                        modifier = Modifier.fillMaxSize().padding(element.paddingDp.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = resolvedText,
                            color = Color(element.textColorHex),
                            fontSize = element.fontSizeSp.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = contrastLabelModifier(Color(element.textColorHex)),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                "SHAPE" -> {
                    if (element.showLabel && element.label.isNotBlank()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = resolveDynamicTokens(element.label, context),
                                color = Color(element.textColorHex),
                                fontSize = element.fontSizeSp.sp,
                                modifier = contrastLabelModifier(Color(element.textColorHex)),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                "CUSTOM_BUTTON" -> {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = !state.isEditMode) {
                                onElementTrigger(element, "TAP")
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(Icons.Rounded.TouchApp, null, tint = Color(element.textColorHex), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = resolveDynamicTokens(element.label.ifBlank { "Action" }, context),
                            color = Color(element.textColorHex),
                            fontSize = element.fontSizeSp.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = contrastLabelModifier(Color(element.textColorHex)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                "IMAGE" -> {
                    val file = element.imagePath?.let { File(it) }?.takeIf { it.exists() }
                    val scaleMode = when (element.imageScaleType) {
                        "FIT" -> ContentScale.Fit
                        "FILL_XY" -> ContentScale.FillBounds
                        "ORIGINAL" -> ContentScale.None
                        else -> ContentScale.Crop
                    }
                    if (file != null) {
                        AsyncImage(
                            model = file,
                            contentDescription = element.label.ifBlank { "Custom image" },
                            contentScale = scaleMode,
                            colorFilter = colorFilter,
                            modifier = Modifier.fillMaxSize().then(blurMod),
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Image, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }

                "APP_SHORTCUT" -> {
                    val app = state.appsWithTags.find { it.app.packageName == element.targetPackageName }?.app
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = !state.isEditMode && app != null) {
                                if (!onElementTrigger(element, "TAP")) {
                                    app?.let { targetApp ->
                                        val shortcutId = element.folderAppPackagesCsv.takeIf { it.isNotBlank() }
                                        val isLocked = targetApp.packageName in state.settings.lockedAppsCsv.split(",").map(String::trim)
                                        if (shortcutId != null && !isLocked) {
                                            if (!appDiscovery.launchAppShortcut(targetApp.packageName, shortcutId, targetApp.componentName)) {
                                                onLaunchApp(targetApp)
                                            }
                                        } else {
                                            onLaunchApp(targetApp)
                                        }
                                    }
                                }
                            }
                            .padding(element.paddingDp.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (app != null) {
                            AppIconView(
                                app = app,
                                appDiscovery = appDiscovery,
                                size = element.iconSizeDp.coerceIn(28f, 96f).dp,
                                iconShapeKey = state.settings.globalIconShape,
                            )
                            if (element.showLabel && state.settings.showHomeAppLabels) {
                                Spacer(Modifier.height(4.dp))
                                OutlinedLabelText(
                                    text = element.label.ifBlank { app.displayName },
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = element.fontSizeSp.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = contrastLabelModifier(Color(element.textColorHex)),
                                    outlineEnabled = state.settings.appLabelOutlineEnabled,
                                )
                            }
                        } else {
                            Icon(Icons.Rounded.Apps, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                            Text("Missing app", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                }

                "TAG_SHORTCUT" -> {
                    val tag = state.allTags.find { it.tagId == element.targetTagId }
                    val count = state.tagsWithApps.find { it.tag.tagId == element.targetTagId }
                        ?.apps?.count { it.isInstalled && !it.hidden } ?: 0
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = !state.isEditMode && tag != null) {
                                tag?.let { onOpenTagSidebar(it.tagId) }
                            }
                            .padding(element.paddingDp.coerceIn(0f, 32f).dp)
                            .testTag("home_tag_shortcut_${element.targetTagId}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (tag != null) {
                            TagBadgeIcon(tag.iconKey, tag.customImagePath, tag.colorHex, 38.dp)
                            if (element.showLabel && state.settings.showHomeAppLabels) {
                                Spacer(Modifier.height(6.dp))
                                Surface(
                                    color = Color.Black.copy(alpha = 0.64f),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(
                                        text = tag.name,
                                        color = Color.White,
                                        fontSize = element.fontSizeSp.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    )
                                }
                                Text(
                                    text = "$count",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(Color.Black.copy(alpha = 0.68f)).padding(horizontal = 5.dp, vertical = 2.dp),
                                )
                            }
                        } else {
                            Text("Tag removed", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                    }
                }

                "CATEGORY_SHORTCUT" -> {
                    val cat = state.categories.find { it.categoryId == element.targetCategoryId }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = !state.isEditMode && cat != null) {
                                cat?.let { onOpenCategorySidebar(it.categoryId) }
                            }
                            .padding(element.paddingDp.coerceIn(0f, 32f).dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (cat != null) {
                            CategoryBadgeIcon(cat, size = 36.dp)

                        } else {
                            Icon(Icons.Rounded.Category, null, tint = Color.White)
                        }
                    }
                }

                "WINDOW_SHORTCUT" -> {
                    val win = state.windows.find { it.windowId == element.targetWindowId }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = !state.isEditMode && win != null) {
                                win?.let { onOpenWindow(it.windowId) }
                            }
                            .padding(element.paddingDp.coerceIn(0f, 32f).dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(Icons.Rounded.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = win?.title ?: element.label.ifBlank { "Window" },
                            color = Color(element.textColorHex),
                            fontSize = element.fontSizeSp.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = contrastLabelModifier(Color(element.textColorHex)),
                        )
                    }
                }

                "SAVED_RULE_SHORTCUT" -> {
                    val rule = state.savedRules.find { it.ruleId == element.targetRuleId }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = !state.isEditMode && rule != null) {
                                rule?.let { onOpenRuleSidebar(it.ruleId) }
                            }
                            .padding(element.paddingDp.coerceIn(0f, 32f).dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        TagBadgeIcon(rule?.iconKey ?: "filter", null, rule?.colorHex ?: 0xFF6366F1L, 36.dp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = rule?.name ?: element.label.ifBlank { "Rule" },
                            color = Color(element.textColorHex),
                            fontSize = element.fontSizeSp.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = contrastLabelModifier(Color(element.textColorHex)),
                        )
                    }
                }

                "SIDEBAR_SHORTCUT" -> {
                    val sb = state.sidebars.find { it.sidebarId == element.targetSidebarId }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = !state.isEditMode && sb != null) {
                                sb?.let { onOpenSidebar(it.sidebarId) }
                            }
                            .padding(element.paddingDp.coerceIn(0f, 32f).dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ViewSidebar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = sb?.title ?: element.label.ifBlank { "Sidebar" },
                            color = Color(element.textColorHex),
                            fontSize = element.fontSizeSp.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = contrastLabelModifier(Color(element.textColorHex)),
                        )
                    }
                }

                "FOLDER" -> {
                    val pkgSet = element.folderPackages().toSet()
                    val previewApps = state.appsWithTags.map { it.app }.filter { it.packageName in pkgSet }.take(4)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = !state.isEditMode) { onOpenFolder(element.elementId) }
                            .padding(element.paddingDp.coerceIn(0f, 32f).dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        when (element.folderLayoutMode) {
                            "LIST" -> Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                previewApps.take(3).forEach { app ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        AppIconView(app, appDiscovery, 18.dp, state.settings.globalIconShape)
                                        Text(app.displayName, color = Color(element.textColorHex), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                            "STACK" -> Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                previewApps.take(4).forEach { app -> AppIconView(app, appDiscovery, 28.dp, state.settings.globalIconShape) }
                            }
                            else -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    previewApps.take(2).forEach { app -> AppIconView(app, appDiscovery, 22.dp, state.settings.globalIconShape) }
                                }
                                if (previewApps.size > 2) {
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        previewApps.drop(2).forEach { app -> AppIconView(app, appDiscovery, 22.dp, state.settings.globalIconShape) }
                                    }
                                }
                            }
                        }
                        val nestedFolders = state.homeElements.filter { it.elementId in element.nestedFolderIds() }
                        if (nestedFolders.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                nestedFolders.take(3).forEach { Icon(Icons.Rounded.Folder, contentDescription = null, tint = Color(element.textColorHex), modifier = Modifier.size(16.dp)) }
                            }
                        }
                        if (previewApps.isEmpty() && nestedFolders.isEmpty()) {
                            Icon(Icons.Rounded.Folder, contentDescription = null, tint = Color.White)
                        }
                        if (element.showLabel && state.settings.showHomeAppLabels) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = element.label.ifBlank { "Folder" },
                                color = Color(element.textColorHex),
                                fontSize = element.fontSizeSp.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = contrastLabelModifier(Color(element.textColorHex)),
                            )
                        }
                    }
                }

                "TAG_COLLECTION_WIDGET" -> {
                    val tag = state.allTags.find { it.tagId == element.targetTagId }
                    val apps = state.tagsWithApps.find { it.tag.tagId == element.targetTagId }
                        ?.apps?.filter { it.isInstalled && !it.hidden }
                        ?.sortedBy { it.displayName.lowercase() }
                        .orEmpty()
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !state.isEditMode && tag != null) {
                                    tag?.let { onOpenTagSidebar(it.tagId) }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (tag != null) {
                                TagBadgeIcon(tag.iconKey, tag.customImagePath, tag.colorHex, 28.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = tag.name,
                                    color = Color(element.textColorHex),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f).then(contrastLabelModifier(Color(element.textColorHex))),
                                )
                                Text(
                                    text = "${apps.size} apps",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(Color.Black.copy(alpha = 0.68f)).padding(horizontal = 5.dp, vertical = 2.dp),
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(apps, key = { it.packageName }) { app ->
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable(enabled = !state.isEditMode) { onLaunchApp(app) }
                                        .padding(2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    AppIconView(app, appDiscovery, 38.dp, state.settings.globalIconShape)
                                    Text(
                                        text = app.displayName,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        modifier = contrastLabelModifier(Color(element.textColorHex)),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                "APP_WIDGET" -> {
                    val widgetId = element.appWidgetId
                    if (widgetId != null) {
                        AndroidView(
                            factory = { ctx ->
                                widgetHost.createHostView(ctx, widgetId) ?: FrameLayout(ctx)
                            },
                            update = {
                                widgetHost.updateWidgetSize(widgetId, element.widthDp.roundToInt(), element.heightDp.roundToInt())
                            },
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Widgets, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            // Extra overlaid image layers
            extraLayers.forEach { layerPath ->
                if (File(layerPath).exists()) {
                    AsyncImage(
                        model = File(layerPath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
fun AddHomeElementDialog(
    pageId: Long,
    ownerWindowId: Long? = null,
    state: LauncherUiState,
    appDiscovery: AppDiscoveryManager,
    onImportImage: suspend (Uri) -> String?,
    onAddElement: (HomeElementEntity) -> Unit,
    onOpenWidgetPicker: () -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf("TYPE") }
    var textLabelInput by remember { mutableStateOf("{TIME} • {DAY}") }
    var buttonLabelInput by remember { mutableStateOf("Quick Action") }
    var folderNameInput by remember { mutableStateOf("Quick Folder") }
    var selectedFolderPackages by remember { mutableStateOf(emptySet<String>()) }

    val ownerType = if (ownerWindowId != null) "WINDOW" else "PAGE"

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri)
                if (path != null) {
                    onAddElement(
                        HomeElementEntity(
                            pageId = pageId,
                            ownerType = ownerType,
                            ownerWindowId = ownerWindowId,
                            type = "IMAGE",
                            xFraction = 0.15f,
                            yFraction = 0.2f,
                            widthDp = 160f,
                            heightDp = 160f,
                            imagePath = path,
                            backgroundColorHex = 0x00000000L,
                            borderWidthDp = 0f,
                            cornerRadiusDp = 22f,
                        )
                    )
                    onDismiss()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (step) {
                    "APP" -> "Pick App Shortcut"
                    "TAG" -> "Pick Tag Shortcut"
                    "CATEGORY" -> "Pick Category Shortcut"
                    "WINDOW" -> "Pick Window Trigger"
                    "RULE" -> "Pick Smart Collection"
                    "SIDEBAR" -> "Pick Sidebar Trigger"
                    "TEXT" -> "New Dynamic Text Label"
                    "BUTTON" -> "New Custom Button"
                    "FOLDER" -> "Create Folder"
                    else -> "Add Object to Canvas"
                }
            )
        },
        text = {
            when (step) {
                "TYPE" -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                    ) {
                        AddTypeRow(Icons.Rounded.LocalOffer, "Tag Shortcut", "Tapping opens this tag's sidebar") { step = "TAG" }
                        AddTypeRow(Icons.Rounded.Category, "Category / Game Shortcut", "Tapping opens Play category sidebar") { step = "CATEGORY" }
                        AddTypeRow(Icons.Rounded.Apps, "App Shortcut", "Freely positioned app launcher") { step = "APP" }
                        AddTypeRow(Icons.Rounded.Folder, "Custom Folder", "Group apps & nested folders") { step = "FOLDER" }
                        AddTypeRow(Icons.Rounded.Widgets, "Widgets (System & Built-in)", "Clocks, Battery, RAM, Compass, Android Widgets") {
                            onDismiss()
                            onOpenWidgetPicker()
                        }
                        AddTypeRow(Icons.Rounded.Layers, "Window Trigger", "Open a Total Launcher overlay window") { step = "WINDOW" }
                        AddTypeRow(Icons.Rounded.Bolt, "Dynamic Tag Rule", "Shortcut to AND/OR/EXCLUDE collection") { step = "RULE" }
                        AddTypeRow(Icons.AutoMirrored.Rounded.ViewSidebar, "Sidebar Trigger", "Button that opens a configured drawer") { step = "SIDEBAR" }
                        AddTypeRow(Icons.Rounded.SmartButton, "Custom Interactive Button", "Multi-gesture / macro button") { step = "BUTTON" }
                        AddTypeRow(Icons.Rounded.TextFields, "Dynamic Text Label", "Supports {TIME}, {DATE}, {RAM}, {STORAGE}") { step = "TEXT" }
                        AddTypeRow(Icons.Rounded.Layers, "Nested Container", "Create a parent panel that can hold positioned objects") {
                            onAddElement(
                                HomeElementEntity(
                                    pageId = pageId,
                                    ownerType = ownerType,
                                    ownerWindowId = ownerWindowId,
                                    type = "CONTAINER",
                                    xFraction = 0.12f,
                                    yFraction = 0.18f,
                                    widthDp = 240f,
                                    heightDp = 220f,
                                    backgroundColorHex = 0x33202A3AL,
                                    borderColorHex = 0x667C9BC0L,
                                    cornerRadiusDp = 20f,
                                )
                            )
                            onDismiss()
                        }
                        AddTypeRow(Icons.Rounded.Widgets, "Widget Stack", "Combine existing objects into a swipeable stack") {
                            onAddElement(
                                HomeElementEntity(
                                    pageId = pageId,
                                    ownerType = ownerType,
                                    ownerWindowId = ownerWindowId,
                                    type = "WIDGET_STACK",
                                    xFraction = 0.16f,
                                    yFraction = 0.2f,
                                    widthDp = 180f,
                                    heightDp = 150f,
                                    backgroundColorHex = 0x33202A3AL,
                                )
                            )
                            onDismiss()
                        }
                        AddTypeRow(Icons.Rounded.Category, "Decorative Shape / Panel", "Resizable glass/colored geometric layer") {
                            onAddElement(
                                HomeElementEntity(
                                    pageId = pageId,
                                    ownerType = ownerType,
                                    ownerWindowId = ownerWindowId,
                                    type = "SHAPE",
                                    xFraction = 0.15f,
                                    yFraction = 0.25f,
                                    widthDp = 180f,
                                    heightDp = 120f,
                                    label = "",
                                    showLabel = false,
                                    backgroundColorHex = 0x5538BDF8L,
                                    borderColorHex = 0x9938BDF8L,
                                    cornerRadiusDp = 28f,
                                )
                            )
                            onDismiss()
                        }
                        AddTypeRow(Icons.Rounded.Image, "Image Object", "Import PNG/JPG/WEBP/SVG (transforms available)") {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    }
                }
                "TAG" -> {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(state.allTags, key = { it.tagId }) { tag ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    onAddElement(
                                        HomeElementEntity(
                                            pageId = pageId,
                                            ownerType = ownerType,
                                            ownerWindowId = ownerWindowId,
                                            type = "TAG_SHORTCUT",
                                            xFraction = 0.2f,
                                            yFraction = 0.25f,
                                            widthDp = 110f,
                                            heightDp = 92f,
                                            label = tag.name,
                                            targetTagId = tag.tagId,
                                            backgroundColorHex = (0x38000000L or (tag.colorHex and 0x00FFFFFFL)),
                                            borderColorHex = (0x99000000L or (tag.colorHex and 0x00FFFFFFL)),
                                        )
                                    )
                                    onDismiss()
                                }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TagBadgeIcon(tag.iconKey, tag.customImagePath, tag.colorHex, 32.dp)
                                Spacer(Modifier.width(10.dp))
                                Text(tag.name, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                "CATEGORY" -> {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(state.categories, key = { it.categoryId }) { cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    onAddElement(
                                        HomeElementEntity(
                                            pageId = pageId,
                                            ownerType = ownerType,
                                            ownerWindowId = ownerWindowId,
                                            type = "CATEGORY_SHORTCUT",
                                            xFraction = 0.2f,
                                            yFraction = 0.25f,
                                            widthDp = 114f,
                                            heightDp = 92f,
                                            label = cat.name,
                                            targetCategoryId = cat.categoryId,
                                            backgroundColorHex = (0x38000000L or (cat.colorHex and 0x00FFFFFFL)),
                                            borderColorHex = (0x99000000L or (cat.colorHex and 0x00FFFFFFL)),
                                        )
                                    )
                                    onDismiss()
                                }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CategoryBadgeIcon(cat, size = 30.dp)
                                Spacer(Modifier.width(10.dp))
                                Text(if (cat.categoryType == "GAME_SUB") "Games • ${cat.name}" else cat.name)
                            }
                        }
                    }
                }
                "WINDOW" -> {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(state.windows, key = { it.windowId }) { w ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    onAddElement(
                                        HomeElementEntity(
                                            pageId = pageId,
                                            ownerType = ownerType,
                                            ownerWindowId = ownerWindowId,
                                            type = "WINDOW_SHORTCUT",
                                            xFraction = 0.2f,
                                            yFraction = 0.25f,
                                            widthDp = 120f,
                                            heightDp = 88f,
                                            label = w.title,
                                            targetWindowId = w.windowId,
                                        )
                                    )
                                    onDismiss()
                                }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.Layers, null)
                                Spacer(Modifier.width(10.dp))
                                Text(w.title, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                "APP" -> {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(state.appsWithTags.filter { it.app.isInstalled }, key = { it.app.packageName }) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    onAddElement(
                                        HomeElementEntity(
                                            pageId = pageId,
                                            ownerType = ownerType,
                                            ownerWindowId = ownerWindowId,
                                            type = "APP_SHORTCUT",
                                            xFraction = 0.25f,
                                            yFraction = 0.3f,
                                            widthDp = 84f,
                                            heightDp = 96f,
                                            label = item.app.displayName,
                                            targetPackageName = item.app.packageName,
                                        )
                                    )
                                    onDismiss()
                                }.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AppIconView(item.app, appDiscovery, 36.dp)
                                Spacer(Modifier.width(10.dp))
                                Text(item.app.displayName, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                "RULE" -> {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(state.savedRules, key = { it.ruleId }) { rule ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    onAddElement(
                                        HomeElementEntity(
                                            pageId = pageId,
                                            ownerType = ownerType,
                                            ownerWindowId = ownerWindowId,
                                            type = "SAVED_RULE_SHORTCUT",
                                            xFraction = 0.2f,
                                            yFraction = 0.25f,
                                            widthDp = 128f,
                                            heightDp = 88f,
                                            label = rule.name,
                                            targetRuleId = rule.ruleId,
                                            backgroundColorHex = (0x38000000L or (rule.colorHex and 0x00FFFFFFL)),
                                            borderColorHex = (0x99000000L or (rule.colorHex and 0x00FFFFFFL)),
                                        )
                                    )
                                    onDismiss()
                                }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TagBadgeIcon(rule.iconKey, null, rule.colorHex, 30.dp)
                                Spacer(Modifier.width(10.dp))
                                Text(rule.name, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                "SIDEBAR" -> {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(state.sidebars, key = { it.sidebarId }) { sb ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    onAddElement(
                                        HomeElementEntity(
                                            pageId = pageId,
                                            ownerType = ownerType,
                                            ownerWindowId = ownerWindowId,
                                            type = "SIDEBAR_SHORTCUT",
                                            xFraction = 0.2f,
                                            yFraction = 0.25f,
                                            widthDp = 120f,
                                            heightDp = 84f,
                                            label = sb.title,
                                            targetSidebarId = sb.sidebarId,
                                        )
                                    )
                                    onDismiss()
                                }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.ViewSidebar, contentDescription = null)
                                Spacer(Modifier.width(10.dp))
                                Text("${sb.title} (${sb.side})", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                "TEXT" -> {
                    OutlinedTextField(
                        value = textLabelInput,
                        onValueChange = { textLabelInput = it },
                        label = { Text("Text ({TIME}, {DATE}, {RAM}, {STORAGE})") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                "BUTTON" -> {
                    OutlinedTextField(
                        value = buttonLabelInput,
                        onValueChange = { buttonLabelInput = it },
                        label = { Text("Button Label (configure action in Inspector)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                "FOLDER" -> {
                    Column(modifier = Modifier.heightIn(max = 360.dp)) {
                        OutlinedTextField(
                            value = folderNameInput,
                            onValueChange = { folderNameInput = it },
                            label = { Text("Folder Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(state.appsWithTags.filter { it.app.isInstalled }, key = { it.app.packageName }) { item ->
                                val checked = item.app.packageName in selectedFolderPackages
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        selectedFolderPackages = if (checked) selectedFolderPackages - item.app.packageName
                                        else selectedFolderPackages + item.app.packageName
                                    }.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = {
                                            selectedFolderPackages = if (it) selectedFolderPackages + item.app.packageName
                                            else selectedFolderPackages - item.app.packageName
                                        },
                                    )
                                    AppIconView(item.app, appDiscovery, 30.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(item.app.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (step == "TEXT") {
                Button(onClick = {
                    onAddElement(
                        HomeElementEntity(
                            pageId = pageId,
                            ownerType = ownerType,
                            ownerWindowId = ownerWindowId,
                            type = "TEXT_LABEL",
                            xFraction = 0.1f,
                            yFraction = 0.18f,
                            widthDp = 240f,
                            heightDp = 72f,
                            label = textLabelInput,
                            fontSizeSp = 18f,
                        )
                    )
                    onDismiss()
                }) { Text("Add Label") }
            } else if (step == "BUTTON") {
                Button(onClick = {
                    onAddElement(
                        HomeElementEntity(
                            pageId = pageId,
                            ownerType = ownerType,
                            ownerWindowId = ownerWindowId,
                            type = "CUSTOM_BUTTON",
                            xFraction = 0.2f,
                            yFraction = 0.35f,
                            widthDp = 160f,
                            heightDp = 56f,
                            label = buttonLabelInput,
                            backgroundColorHex = 0xCC38BDF8L,
                            textColorHex = 0xFF000000L,
                            cornerRadiusDp = 18f,
                        )
                    )
                    onDismiss()
                }) { Text("Add Button") }
            } else if (step == "FOLDER") {
                Button(onClick = {
                    onAddElement(
                        HomeElementEntity(
                            pageId = pageId,
                            ownerType = ownerType,
                            ownerWindowId = ownerWindowId,
                            type = "FOLDER",
                            xFraction = 0.2f,
                            yFraction = 0.3f,
                            widthDp = 96f,
                            heightDp = 104f,
                            label = folderNameInput.ifBlank { "Folder" },
                            folderAppPackagesCsv = selectedFolderPackages.joinToString(","),
                        )
                    )
                    onDismiss()
                }) { Text("Create Folder") }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (step == "TYPE") onDismiss() else step = "TYPE" }) {
                Text(if (step == "TYPE") "Close" else "Back")
            }
        },
    )
}

@Composable
private fun AddTypeRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ElementInspectorDialog(
    element: HomeElementEntity,
    state: LauncherUiState,
    onImportImage: suspend (Uri, String, String?) -> String?,
    onUpdate: (HomeElementEntity) -> Unit,
    onDuplicate: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onBringForward: () -> Unit,
    onSendBackward: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    styleToPaste: HomeElementEntity? = null,
    onCopyStyle: (HomeElementEntity) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var edited by remember(element.elementId) { mutableStateOf(element) }
    var showFullTransform by remember { mutableStateOf(false) }
    var inspectorTab by remember { mutableStateOf("LAYOUT") }

    LaunchedEffect(element.locked, element.zIndex) {
        edited = edited.copy(locked = element.locked, zIndex = element.zIndex)
    }
    val styleExtras = remember(edited.styleExtrasJson) { runCatching { JSONObject(edited.styleExtrasJson) }.getOrElse { JSONObject() } }
    fun updateStyleExtra(key: String, value: Double) {
        val updated = runCatching { JSONObject(edited.styleExtrasJson) }.getOrElse { JSONObject() }
        updated.put(key, value)
        edited = edited.copy(styleExtrasJson = updated.toString())
    }
    fun updateStyleExtraLong(key: String, value: Long) {
        val updated = runCatching { JSONObject(edited.styleExtrasJson) }.getOrElse { JSONObject() }
        updated.put(key, value)
        edited = edited.copy(styleExtrasJson = updated.toString())
    }
    fun isDescendantOf(candidateId: Long, ancestorId: Long): Boolean {
        var parentId = state.homeElements.firstOrNull { it.elementId == candidateId }?.parentContainerId
        val visited = mutableSetOf<Long>()
        while (parentId != null && visited.add(parentId)) {
            if (parentId == ancestorId) return true
            parentId = state.homeElements.firstOrNull { it.elementId == parentId }?.parentContainerId
        }
        return false
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri, "Objects", null)
                if (path != null) {
                    edited = edited.copy(imagePath = path)
                    onUpdate(edited)
                }
            }
        }
    }

    if (showFullTransform) {
        ObjectImageAndTransformDialog(
            element = edited,
            imageResources = state.imageResources,
            allApps = state.appsWithTags,
            allTags = state.allTags,
            categories = state.categories,
            sidebars = state.sidebars,
            windows = state.windows,
            onImportUri = onImportImage,
            onSave = {
                edited = it
                onUpdate(it)
                showFullTransform = false
            },
            onDismiss = { showFullTransform = false },
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customize Object") },
        text = {
            @OptIn(ExperimentalLayoutApi::class)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    IconButton(onClick = {
                        edited = edited.copy(locked = !edited.locked)
                        onUpdate(edited)
                    }) {
                        Icon(
                            if (edited.locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            contentDescription = "Lock/Unlock",
                            tint = if (edited.locked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = onDuplicate) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = "Duplicate")
                    }
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy")
                    }
                    IconButton(onClick = onCut) {
                        Icon(Icons.Rounded.ContentCut, contentDescription = "Cut")
                    }
                    IconButton(onClick = onBringForward) {
                        Icon(Icons.Rounded.FlipToFront, contentDescription = "Bring forward")
                    }
                    IconButton(onClick = onSendBackward) {
                        Icon(Icons.Rounded.FlipToBack, contentDescription = "Send backward")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }

                Button(
                    onClick = { showFullTransform = true },
                    modifier = Modifier.fillMaxWidth().testTag("inspector_open_transform"),
                ) {
                    Icon(Icons.Rounded.Tune, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Image FX, Skew/Flip/Crop & Gesture Actions")
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf(
                        "LAYOUT" to "Layout",
                        "STYLE" to "Style",
                        "TEXT" to "Text & Icon",
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = inspectorTab == key,
                            onClick = { inspectorTab = key },
                            label = { Text(label) },
                            modifier = Modifier.testTag("inspector_tab_$key"),
                        )
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {

                when (inspectorTab) {
                    "LAYOUT" -> {
                        Text("Width: ${edited.widthDp.roundToInt()}dp")
                        Slider(
                            value = edited.widthDp,
                            onValueChange = { edited = edited.copy(widthDp = it) },
                            valueRange = 48f..380f,
                            modifier = Modifier.testTag("inspector_width_slider"),
                        )
                        Text("Height: ${edited.heightDp.roundToInt()}dp")
                        Slider(
                            value = edited.heightDp,
                            onValueChange = { edited = edited.copy(heightDp = it) },
                            valueRange = 48f..440f,
                            modifier = Modifier.testTag("inspector_height_slider"),
                        )
                        Text((if (edited.parentContainerId != null) "Container X Position: " else "X Position: ") + "${(edited.xFraction * 100).roundToInt()}%")
                        Slider(
                            value = edited.xFraction,
                            onValueChange = { edited = edited.copy(xFraction = it) },
                            valueRange = 0f..0.95f,
                        )
                        Text((if (edited.parentContainerId != null) "Container Y Position: " else "Y Position: ") + "${(edited.yFraction * 100).roundToInt()}%")
                        Slider(
                            value = edited.yFraction,
                            onValueChange = { edited = edited.copy(yFraction = it) },
                            valueRange = 0f..0.95f,
                        )
                        Text("Rotation: ${edited.rotationDeg.roundToInt()}°")
                        Slider(
                            value = edited.rotationDeg,
                            onValueChange = { edited = edited.copy(rotationDeg = it) },
                            valueRange = -180f..180f,
                        )
                        Text("3D Rotation X: ${edited.rotationXDeg.roundToInt()}°")
                        Slider(value = edited.rotationXDeg, onValueChange = { edited = edited.copy(rotationXDeg = it) }, valueRange = -180f..180f)
                        Text("3D Rotation Y: ${edited.rotationYDeg.roundToInt()}°")
                        Slider(value = edited.rotationYDeg, onValueChange = { edited = edited.copy(rotationYDeg = it) }, valueRange = -180f..180f)
                        Text("Perspective Depth: ${(edited.perspective * 100).roundToInt()}%")
                        Slider(value = edited.perspective, onValueChange = { edited = edited.copy(perspective = it) }, valueRange = 0f..1f)
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pin to Viewport")
                            Switch(checked = edited.pinnedToViewport, onCheckedChange = { edited = edited.copy(pinnedToViewport = it) })
                        }
                        if (edited.pinnedToViewport) {
                            Text("Pinned Edge")
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("NONE", "TOP", "BOTTOM", "LEFT", "RIGHT").forEach { edge ->
                                    FilterChip(selected = edited.pinEdge == edge, onClick = { edited = edited.copy(pinEdge = edge) }, label = { Text(edge) })
                                }
                            }
                            Text("Pin Offset: ${edited.pinOffsetDp.roundToInt()}dp")
                            Slider(value = edited.pinOffsetDp, onValueChange = { edited = edited.copy(pinOffsetDp = it) }, valueRange = 0f..80f)
                        }
                        Text("Scale X: ${"%.2f".format(edited.scaleX)}x")
                        Slider(
                            value = edited.scaleX,
                            onValueChange = { edited = edited.copy(scaleX = it) },
                            valueRange = 0.4f..2.0f,
                        )
                        Text("Scale Y: ${"%.2f".format(edited.scaleY)}x")
                        Slider(
                            value = edited.scaleY,
                            onValueChange = { edited = edited.copy(scaleY = it) },
                            valueRange = 0.4f..2.0f,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Flip Horizontal")
                            Switch(checked = edited.flipHorizontal, onCheckedChange = { edited = edited.copy(flipHorizontal = it) })
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Flip Vertical")
                            Switch(checked = edited.flipVertical, onCheckedChange = { edited = edited.copy(flipVertical = it) })
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Z-Index: ${edited.zIndex}", style = MaterialTheme.typography.labelLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { edited = edited.copy(zIndex = (edited.zIndex - 1).coerceAtLeast(0)) },
                                    modifier = Modifier.testTag("inspector_z_minus"),
                                ) { Text("-") }
                                OutlinedButton(
                                    onClick = { edited = edited.copy(zIndex = edited.zIndex + 1) },
                                    modifier = Modifier.testTag("inspector_z_plus"),
                                ) { Text("+") }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Visible on Canvas")
                            Switch(checked = edited.visible, onCheckedChange = { edited = edited.copy(visible = it) })
                        }
                        HorizontalDivider()
                        Text("Widget Stack", style = MaterialTheme.typography.labelLarge)
                        Text("Choose existing objects to place inside this stack. Selected objects are hidden from the page outside the stack.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val currentStackIds = edited.widgetStackItemsJson.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
                        state.homeElements.filter { it.pageId == edited.pageId && it.elementId != edited.elementId && it.type != "WIDGET_STACK" && it.parentContainerId == null }.forEach { child ->
                            FilterChip(
                                selected = child.elementId in currentStackIds,
                                onClick = {
                                    val nextIds = if (child.elementId in currentStackIds) currentStackIds - child.elementId else currentStackIds + child.elementId
                                    edited = edited.copy(type = "WIDGET_STACK", widgetStackItemsJson = nextIds.joinToString(","))
                                },
                                label = { Text(child.label.ifBlank { child.type }, maxLines = 1) },
                            )
                        }
                        HorizontalDivider()
                        Text("Parent Container", style = MaterialTheme.typography.labelLarge)
                        Text("Canvas means this object sits directly on the page.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FilterChip(selected = edited.parentContainerId == null, onClick = { edited = edited.copy(parentContainerId = null) }, label = { Text("Canvas") })
                        state.homeElements.filter { it.type == "CONTAINER" && it.elementId != edited.elementId && !isDescendantOf(it.elementId, edited.elementId) }.forEach { container ->
                            FilterChip(
                                selected = edited.parentContainerId == container.elementId,
                                onClick = { edited = edited.copy(parentContainerId = container.elementId) },
                                label = { Text(container.label.ifBlank { "Container " + container.elementId }) },
                            )
                        }
                        if (edited.type == "FOLDER") {
                            HorizontalDivider()
                            Text("Folder Studio", style = MaterialTheme.typography.titleSmall)
                            Text("Folder Layout", style = MaterialTheme.typography.labelMedium)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("GRID", "LIST", "STACK").forEach { mode ->
                                    FilterChip(selected = edited.folderLayoutMode == mode, onClick = { edited = edited.copy(folderLayoutMode = mode) }, label = { Text(mode) })
                                }
                            }
                            Text("Apps in Folder", style = MaterialTheme.typography.labelMedium)
                            val folderPackages = edited.folderPackages().toSet()
                            LazyColumn(modifier = Modifier.height(160.dp)) {
                                items(state.appsWithTags, key = { it.app.packageName }) { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            val next = if (item.app.packageName in folderPackages) folderPackages - item.app.packageName else folderPackages + item.app.packageName
                                            edited = edited.copy(folderAppPackagesCsv = next.joinToString(","))
                                        }.padding(vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Checkbox(checked = item.app.packageName in folderPackages, onCheckedChange = {
                                            val next = if (it) folderPackages + item.app.packageName else folderPackages - item.app.packageName
                                            edited = edited.copy(folderAppPackagesCsv = next.joinToString(","))
                                        })
                                        Text(item.app.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                            Text("Nested Folders", style = MaterialTheme.typography.labelMedium)
                            val nestedIds = edited.nestedFolderIds().toSet()
                            state.homeElements.filter { it.type == "FOLDER" && it.elementId != edited.elementId }.forEach { folder ->
                                FilterChip(
                                    selected = folder.elementId in nestedIds,
                                    onClick = {
                                        val next = if (folder.elementId in nestedIds) nestedIds - folder.elementId else nestedIds + folder.elementId
                                        edited = edited.copy(nestedFolderIdsCsv = next.joinToString(","))
                                    },
                                    label = { Text(folder.label.ifBlank { "Folder " + folder.elementId }) },
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Show stack navigation")
                            Switch(checked = edited.widgetStackSwipeEnabled, onCheckedChange = { edited = edited.copy(widgetStackSwipeEnabled = it) })
                        }
                    }

                    "STYLE" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onCopyStyle(edited) }) { Text("Copy Style") }
                            OutlinedButton(
                                enabled = styleToPaste != null,
                                onClick = {
                                    styleToPaste?.let { source ->
                                        edited = edited.copy(
                                            backgroundColorHex = source.backgroundColorHex,
                                            backgroundGradientEndHex = source.backgroundGradientEndHex,
                                            textColorHex = source.textColorHex,
                                            borderColorHex = source.borderColorHex,
                                            borderWidthDp = source.borderWidthDp,
                                            cornerRadiusDp = source.cornerRadiusDp,
                                            paddingDp = source.paddingDp,
                                            shadowElevationDp = source.shadowElevationDp,
                                            fontSizeSp = source.fontSizeSp,
                                            showLabel = source.showLabel,
                                            iconSizeDp = source.iconSizeDp,
                                            imageMaskShape = source.imageMaskShape,
                                            styleExtrasJson = source.styleExtrasJson,
                                        )
                                        onUpdate(edited)
                                    }
                                },
                            ) { Text("Paste Style") }
                        }
                        Text("Independent Corner Radii", style = MaterialTheme.typography.labelLarge)
                        listOf(
                            "topStartDp" to "Top Start",
                            "topEndDp" to "Top End",
                            "bottomEndDp" to "Bottom End",
                            "bottomStartDp" to "Bottom Start",
                        ).forEach { (key, label) ->
                            val value = styleExtras.optDouble(key, edited.cornerRadiusDp.toDouble()).toFloat().coerceIn(0f, 120f)
                            Text("$label: ${value.roundToInt()}dp", style = MaterialTheme.typography.labelSmall)
                            Slider(value = value, onValueChange = { updateStyleExtra(key, it.toDouble()) }, valueRange = 0f..120f)
                        }
                        Text("Glow Radius: ${styleExtras.optDouble("glowRadiusDp", 0.0).roundToInt()}dp")
                        Slider(value = styleExtras.optDouble("glowRadiusDp", 0.0).toFloat().coerceIn(0f, 40f), onValueChange = { updateStyleExtra("glowRadiusDp", it.toDouble()) }, valueRange = 0f..40f)
                        Text("Glow Color", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = styleExtras.optLong("glowColorHex", edited.borderColorHex),
                            colors = StudioNeutralAndAccentSwatches,
                            onSelect = { color -> updateStyleExtraLong("glowColorHex", color) },
                            tagPrefix = "inspector_glow_color",
                        )
                        Text("Shape Mask", style = MaterialTheme.typography.labelLarge)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            listOf("NONE", "SQUIRCLE", "CIRCLE", "DIAMOND", "HEXAGON").forEach { m ->
                                FilterChip(
                                    selected = edited.imageMaskShape == m,
                                    onClick = { edited = edited.copy(imageMaskShape = m) },
                                    label = { Text(m) },
                                    modifier = Modifier.testTag("inspector_mask_$m"),
                                )
                            }
                        }

                        Text("Corner Radius: ${edited.cornerRadiusDp.roundToInt()}dp")
                        Slider(value = edited.cornerRadiusDp, onValueChange = { edited = edited.copy(cornerRadiusDp = it) }, valueRange = 0f..74f)
                        Text("Transparency: ${(edited.alpha * 100).roundToInt()}%")
                        Slider(value = edited.alpha, onValueChange = { edited = edited.copy(alpha = it) }, valueRange = 0.1f..1f)
                        Text("Inner Padding: ${edited.paddingDp.roundToInt()}dp")
                        Slider(value = edited.paddingDp, onValueChange = { edited = edited.copy(paddingDp = it) }, valueRange = 0f..32f)
                        Text("Shadow Elevation: ${edited.shadowElevationDp.roundToInt()}dp")
                        Slider(value = edited.shadowElevationDp, onValueChange = { edited = edited.copy(shadowElevationDp = it) }, valueRange = 0f..24f)

                        Text("Border Width: ${"%.1f".format(edited.borderWidthDp)}dp")
                        Slider(
                            value = edited.borderWidthDp,
                            onValueChange = { edited = edited.copy(borderWidthDp = it) },
                            valueRange = 0f..8f,
                            modifier = Modifier.testTag("inspector_border_width_slider"),
                        )

                        Text("Border Color", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = edited.borderColorHex,
                            colors = StudioNeutralAndAccentSwatches,
                            onSelect = { c -> edited = edited.copy(borderColorHex = c) },
                            tagPrefix = "inspector_border_color",
                        )

                        Text("Background Color / Tint", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = edited.backgroundColorHex,
                            colors = StudioDarkSurfaceSwatches,
                            allowClear = true,
                            onClear = { edited = edited.copy(backgroundColorHex = 0x00000000L) },
                            onSelect = { c -> edited = edited.copy(backgroundColorHex = c) },
                            tagPrefix = "inspector_bg_color",
                        )

                        Text("Gradient End Color (Optional)", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = edited.backgroundGradientEndHex,
                            colors = StudioDarkSurfaceSwatches,
                            allowClear = true,
                            onClear = { edited = edited.copy(backgroundGradientEndHex = null) },
                            onSelect = { c -> edited = edited.copy(backgroundGradientEndHex = c) },
                            tagPrefix = "inspector_grad_end",
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }) {
                                Text("Pick Background Image")
                            }
                            if (edited.imagePath != null) {
                                TextButton(onClick = { edited = edited.copy(imagePath = null) }) {
                                    Text("Clear Image")
                                }
                            }
                        }
                    }

                    "TEXT" -> {
                        if (edited.type != "APP_WIDGET") {
                            OutlinedTextField(
                                value = edited.label,
                                onValueChange = { edited = edited.copy(label = it) },
                                label = { Text("Label (supports {TIME}, {DATE}, {RAM})") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("inspector_label_input"),
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Show Label")
                            Switch(checked = edited.showLabel, onCheckedChange = { edited = edited.copy(showLabel = it) })
                        }

                        Text("Font Size: ${edited.fontSizeSp.roundToInt()}sp")
                        Slider(
                            value = edited.fontSizeSp,
                            onValueChange = { edited = edited.copy(fontSizeSp = it) },
                            valueRange = 8f..48f,
                            modifier = Modifier.testTag("inspector_font_size_slider"),
                        )

                        Text("Text Color", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = edited.textColorHex,
                            colors = StudioNeutralAndAccentSwatches,
                            onSelect = { c -> edited = edited.copy(textColorHex = c) },
                            tagPrefix = "inspector_text_color",
                        )

                        Text("Icon Size: ${edited.iconSizeDp.roundToInt()}dp")
                        Slider(
                            value = edited.iconSizeDp,
                            onValueChange = { edited = edited.copy(iconSizeDp = it) },
                            valueRange = 20f..120f,
                        )
                    }
                }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(edited)
                    onDismiss()
                },
                modifier = Modifier.testTag("inspector_apply_button"),
            ) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun PagesAndSnapshotsDialog(
    pages: List<HomePageEntity>,
    snapshots: List<LayoutSnapshotEntity>,
    currentPageId: Long? = null,
    onJumpToPage: (Long) -> Unit = {},
    onImportImage: suspend (Uri, String, String?) -> String?,
    onAddPage: (String) -> Unit,
    onCreateStarterTemplate: (String) -> Unit,
    onSavePage: (HomePageEntity) -> Unit,
    onDuplicatePage: (HomePageEntity) -> Unit,
    onReorderPages: (List<Long>) -> Unit,
    onDeletePage: (Long) -> Unit,
    onSaveSnapshot: (String) -> Unit,
    onRestoreSnapshot: (LayoutSnapshotEntity) -> Unit,
    onDeleteSnapshot: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var newPageName by remember { mutableStateOf("") }
    var snapshotName by remember { mutableStateOf("") }
    var editingPage by remember { mutableStateOf<HomePageEntity?>(null) }

    val pageColorSwatches = remember {
        listOf(
            0xFF090D16L,
            0xFF0F172AL,
            0xFF0A1424L,
            0xFF1E1032L,
            0xFF102A24L,
            0xFF2B1320L,
        ) + TagPresetColors
    }

    editingPage?.let { page ->
        var working by remember(page.pageId) { mutableStateOf(page) }
        fun setPageGesture(trigger: String, action: String) {
            val json = runCatching { JSONObject(working.pageGesturesJson ?: "{}") }.getOrElse { JSONObject() }
            if (action == "NONE") json.remove(trigger) else json.put(trigger, JSONObject().put("actionType", action))
            working = working.copy(pageGesturesJson = json.toString().takeIf { json.length() > 0 })
        }
        fun getPageGesture(trigger: String): String =
            runCatching { JSONObject(working.pageGesturesJson ?: "{}").optJSONObject(trigger)?.optString("actionType", "NONE") ?: "NONE" }.getOrDefault("NONE")
        val portraitPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                scope.launch {
                    val path = onImportImage(uri, "Wallpapers", null)
                    if (path != null) working = working.copy(backgroundType = "IMAGE", backgroundImagePath = path)
                }
            }
        }
        val landscapePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                scope.launch {
                    val path = onImportImage(uri, "Wallpapers", null)
                    if (path != null) working = working.copy(backgroundType = "IMAGE", landscapeBackgroundImagePath = path)
                }
            }
        }
        AlertDialog(
            onDismissRequest = { editingPage = null },
            title = { Text("Edit Page: ${page.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = working.title,
                        onValueChange = { working = working.copy(title = it) },
                        label = { Text("Page Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_page_title_input"),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Lock Page Objects")
                        Switch(checked = working.isLocked, onCheckedChange = { working = working.copy(isLocked = it) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Hide Page from Swipe")
                        Switch(checked = working.isHidden, onCheckedChange = { working = working.copy(isHidden = it) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Vertically Scrollable Canvas")
                        Switch(checked = working.verticallyScrollable, onCheckedChange = { working = working.copy(verticallyScrollable = it) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Loop Home Page Navigation")
                        Switch(checked = working.loopNavigation, onCheckedChange = { working = working.copy(loopNavigation = it) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Parallax Page Motion")
                        Switch(checked = working.parallaxEnabled, onCheckedChange = { working = working.copy(parallaxEnabled = it) })
                    }
                    Text("Page Transition Override", style = MaterialTheme.typography.labelLarge)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("DEFAULT", "SLIDE", "DEPTH", "FADE", "ZOOM", "CUBE", "FLIP", "ROTATE", "PARALLAX").forEach { mode ->
                            FilterChip(
                                selected = (working.transitionOverride ?: "DEFAULT") == mode,
                                onClick = { working = working.copy(transitionOverride = mode.takeUnless { it == "DEFAULT" }) },
                                label = { Text(mode) },
                            )
                        }
                    }
                    Text("Transition Duration: ${working.transitionDurationMs}ms")
                    Slider(value = working.transitionDurationMs.toFloat(), onValueChange = { working = working.copy(transitionDurationMs = it.roundToInt().coerceIn(150, 1200)) }, valueRange = 150f..1200f, steps = 20)
                    HorizontalDivider()
                    Text("Per-Page Gesture Overrides", style = MaterialTheme.typography.labelLarge)
                    Text("These actions run before your global gesture bindings on this page.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf("TAP", "DOUBLE_TAP", "LONG_PRESS", "SWIPE_UP", "SWIPE_DOWN").forEach { trigger ->
                        Text(trigger.replace('_', ' '), style = MaterialTheme.typography.labelMedium)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("NONE", "NEXT_PAGE", "PREV_PAGE", "OPEN_DRAWER", "OPEN_SEARCH", "TOGGLE_EDIT_MODE", "OPEN_SETTINGS").forEach { action ->
                                FilterChip(
                                    selected = getPageGesture(trigger) == action,
                                    onClick = { setPageGesture(trigger, action) },
                                    label = { Text(action.replace('_', ' ')) },
                                )
                            }
                        }
                    }
                    Text("Page Edge Padding: ${working.paddingDp.roundToInt()}dp")
                    Slider(value = working.paddingDp, onValueChange = { working = working.copy(paddingDp = it) }, valueRange = 0f..40f)
                    Text("Page Background Mode", style = MaterialTheme.typography.labelLarge)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        listOf("INHERIT", "TRANSPARENT", "SOLID", "GRADIENT", "IMAGE").forEach { mode ->
                            FilterChip(
                                selected = working.backgroundType == mode,
                                onClick = {
                                    working = when (mode) {
                                        "SOLID" -> working.copy(
                                            backgroundType = mode,
                                            backgroundColorHex = working.backgroundColorHex ?: 0xFF0F172AL,
                                        )
                                        "GRADIENT" -> working.copy(
                                            backgroundType = mode,
                                            backgroundColorHex = working.backgroundColorHex ?: 0xFF0A1424L,
                                            backgroundGradientEndHex = working.backgroundGradientEndHex ?: 0xFF1E1032L,
                                        )
                                        else -> working.copy(backgroundType = mode)
                                    }
                                },
                                label = { Text(mode) },
                            )
                        }
                    }
                    if (working.backgroundType == "SOLID" || working.backgroundType == "GRADIENT") {
                        Text(
                            if (working.backgroundType == "GRADIENT") "Gradient Start Color" else "Solid Background Color",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(pageColorSwatches) { c ->
                                val selected = working.backgroundColorHex == c
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(c))
                                        .border(if (selected) 2.5.dp else 1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.25f), CircleShape)
                                        .clickable { working = working.copy(backgroundColorHex = c) },
                                )
                            }
                        }
                    }
                    if (working.backgroundType == "GRADIENT") {
                        Text("Gradient End Color", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(pageColorSwatches) { c ->
                                val selected = working.backgroundGradientEndHex == c
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(c))
                                        .border(if (selected) 2.5.dp else 1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.25f), CircleShape)
                                        .clickable { working = working.copy(backgroundGradientEndHex = c) },
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { portraitPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                            Text("Portrait Img")
                        }
                        OutlinedButton(onClick = { landscapePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                            Text("Landscape Img")
                        }
                    }
                    if (working.backgroundImagePath != null || working.landscapeBackgroundImagePath != null) {
                        TextButton(onClick = {
                            working = working.copy(backgroundImagePath = null, landscapeBackgroundImagePath = null)
                        }) {
                            Text("Clear Page Background Images")
                        }
                    }
                    Text("Page Blur: ${working.backgroundBlurDp.roundToInt()}dp")
                    Slider(value = working.backgroundBlurDp, onValueChange = { working = working.copy(backgroundBlurDp = it) }, valueRange = 0f..24f)
                    Text("Background Opacity: ${(working.backgroundAlpha * 100).roundToInt()}%")
                    Slider(value = working.backgroundAlpha, onValueChange = { working = working.copy(backgroundAlpha = it) }, valueRange = 0.05f..1f)
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSavePage(working.copy(title = working.title.trim().ifBlank { page.title }))
                    editingPage = null
                }) { Text("Save Page") }
            },
            dismissButton = { TextButton(onClick = { editingPage = null }) { Text("Cancel") } },
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Pages & Themes / Snapshots")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Starter Templates", style = MaterialTheme.typography.titleMedium)
                Text("Create a page with a ready-made layout. You can move and restyle every object afterward.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Minimal", "Productivity", "Widget Board").forEach { template ->
                        OutlinedButton(onClick = { onCreateStarterTemplate(template); onDismiss() }) { Text(template) }
                    }
                }
                HorizontalDivider()
                Text("Home Pages (${pages.size}) • Tap a page to switch & edit", style = MaterialTheme.typography.titleMedium)
                pages.forEachIndexed { idx, p ->
                    val isCurrent = p.pageId == currentPageId
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = if (isCurrent) 6.dp else 2.dp,
                        border = if (isCurrent) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onJumpToPage(p.pageId)
                                onDismiss()
                            }
                            .testTag("page_row_${p.pageId}"),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${idx + 1}. ${p.title}" + if (isCurrent) " (Active)" else "",
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        listOfNotNull(
                                            p.backgroundType,
                                            if (p.isHidden) "Hidden" else null,
                                            if (p.isLocked) "Locked" else null,
                                            if (p.verticallyScrollable) "Scrollable" else null,
                                        ).joinToString(" • "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        onJumpToPage(p.pageId)
                                        onDismiss()
                                    },
                                    modifier = Modifier.size(40.dp).testTag("jump_page_btn_${p.pageId}"),
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, "Go to page", modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { editingPage = p },
                                    modifier = Modifier.size(40.dp).testTag("edit_page_btn_${p.pageId}"),
                                ) {
                                    Icon(Icons.Rounded.Tune, "Edit page", modifier = Modifier.size(18.dp))
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (idx > 0) {
                                    IconButton(
                                        onClick = {
                                            val ids = pages.map { it.pageId }.toMutableList()
                                            val moved = ids.removeAt(idx)
                                            ids.add(idx - 1, moved)
                                            onReorderPages(ids)
                                        },
                                        modifier = Modifier.size(40.dp).testTag("move_page_up_${p.pageId}"),
                                    ) {
                                        Icon(Icons.Rounded.ArrowUpward, "Move up", modifier = Modifier.size(18.dp))
                                    }
                                }
                                if (idx < pages.lastIndex) {
                                    IconButton(
                                        onClick = {
                                            val ids = pages.map { it.pageId }.toMutableList()
                                            val moved = ids.removeAt(idx)
                                            ids.add(idx + 1, moved)
                                            onReorderPages(ids)
                                        },
                                        modifier = Modifier.size(40.dp).testTag("move_page_down_${p.pageId}"),
                                    ) {
                                        Icon(Icons.Rounded.ArrowDownward, "Move down", modifier = Modifier.size(18.dp))
                                    }
                                }
                                IconButton(
                                    onClick = { onDuplicatePage(p) },
                                    modifier = Modifier.size(40.dp).testTag("duplicate_page_${p.pageId}"),
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, "Duplicate page", modifier = Modifier.size(18.dp))
                                }
                                if (pages.size > 1) {
                                    IconButton(
                                        onClick = { onDeletePage(p.pageId) },
                                        modifier = Modifier.size(40.dp).testTag("delete_page_${p.pageId}"),
                                    ) {
                                        Icon(Icons.Rounded.Delete, "Delete page", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newPageName,
                        onValueChange = { newPageName = it },
                        label = { Text("New page title") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        enabled = newPageName.isNotBlank(),
                        onClick = {
                            onAddPage(newPageName.trim())
                            newPageName = ""
                        },
                    ) { Icon(Icons.Rounded.Add, contentDescription = "Add page") }
                }

                HorizontalDivider()
                Text("Layout Snapshots (Save & Restore)", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = snapshotName,
                        onValueChange = { snapshotName = it },
                        label = { Text("Snapshot name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        enabled = snapshotName.isNotBlank(),
                        onClick = {
                            onSaveSnapshot(snapshotName.trim())
                            snapshotName = ""
                        },
                    ) { Icon(Icons.Rounded.Save, contentDescription = "Save snapshot") }
                }

                snapshots.forEach { snap ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(snap.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(snap.createdAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            IconButton(onClick = { onRestoreSnapshot(snap) }) {
                                Icon(Icons.Rounded.Restore, contentDescription = "Restore layout")
                            }
                            IconButton(onClick = { onDeleteSnapshot(snap.snapshotId) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete snapshot")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
    )
}


@Composable
fun LayerManagerDialog(
    elements: List<HomeElementEntity>,
    onSelect: (Long) -> Unit,
    onUpdate: (HomeElementEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Layer Manager") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Select a layer to inspect it. Adjust stacking order, visibility, and locks without hunting for objects on the canvas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                elements.sortedByDescending { it.zIndex }.forEach { element ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(element.elementId) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(element.label.ifBlank { element.type.replace('_', ' ') }, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                            Text("Layer ${element.zIndex}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onUpdate(element.copy(zIndex = element.zIndex + 1)) }) { Text("↑") }
                        TextButton(onClick = { onUpdate(element.copy(zIndex = (element.zIndex - 1).coerceAtLeast(0))) }) { Text("↓") }
                        Switch(checked = element.visible, onCheckedChange = { onUpdate(element.copy(visible = it)) })
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}
