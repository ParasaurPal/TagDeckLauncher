package com.example.ui.screens

import com.example.ui.components.ImageCropEditor
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.foundation.shape.CutCornerShape
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
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FlipToBack
import androidx.compose.material.icons.rounded.FlipToFront
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FormatPaint
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SmartButton
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.ToggleOn
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewAgenda
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.db.AppEntity
import com.example.data.db.ElementStyleExtras
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.LayoutSnapshotEntity
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.LauncherWidgetHost
import com.example.ui.components.AppIconView
import com.example.ui.components.CategoryBadgeIcon
import com.example.ui.components.ColorSwatchRow
import com.example.ui.components.MacroStepBuilderDialog
import com.example.ui.components.ObjectImageAndTransformDialog
import com.example.ui.components.ReadableLauncherText
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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

fun isElementConditionMet(
    element: HomeElementEntity,
    state: LauncherUiState,
    context: Context,
    isLandscape: Boolean,
    systemDark: Boolean,
): Boolean {
    if (element.elementId in state.runtimeHiddenElementIds) return false
    if (element.elementId in state.runtimeShownElementIds) return true

    val group = element.visibilityGroupId?.takeIf { it.isNotBlank() }
    if (group != null && group in state.hiddenVisibilityGroups) return false

    if (!element.visible) {
        return group != null && group in state.activeVisibilityGroups
    }

    val extras = element.parsedExtras()
    val isDark = when (state.settings.themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> systemDark
    }
    return when (extras.visibilityCondition) {
        "PORTRAIT_ONLY" -> !isLandscape
        "LANDSCAPE_ONLY" -> isLandscape
        "DARK_THEME_ONLY" -> isDark
        "LIGHT_THEME_ONLY" -> !isDark
        "MORNING_ONLY" -> Calendar.getInstance().get(Calendar.HOUR_OF_DAY) in 5..11
        "EVENING_ONLY" -> {
            val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            h >= 17 || h < 5
        }
        "CHARGING_ONLY" -> {
            val intent = runCatching {
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            }.getOrNull()
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        }
        else -> true
    }
}

private fun resolveMaskShape(element: HomeElementEntity, extras: ElementStyleExtras): Shape = when (element.imageMaskShape) {
    "CIRCLE" -> CircleShape
    "SQUIRCLE" -> RoundedCornerShape((element.widthDp.coerceAtMost(element.heightDp) * 0.28f).dp)
    "PILL" -> RoundedCornerShape(50)
    "CUT_CORNER" -> CutCornerShape(element.cornerRadiusDp.coerceIn(4f, 36f).dp)
    "ARCH" -> RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomStartPercent = 8, bottomEndPercent = 8)
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
    "OCTAGON" -> GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        val c = 0.29f
        moveTo(w * c, 0f)
        lineTo(w * (1f - c), 0f)
        lineTo(w, h * c)
        lineTo(w, h * (1f - c))
        lineTo(w * (1f - c), h)
        lineTo(w * c, h)
        lineTo(0f, h * (1f - c))
        lineTo(0f, h * c)
        close()
    }
    "TRIANGLE" -> GenericShape { size, _ ->
        moveTo(size.width / 2f, 0f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    "STAR" -> GenericShape { size, _ ->
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = minOf(cx, cy)
        val innerR = outerR * 0.45f
        for (i in 0 until 10) {
            val angle = -PI / 2.0 + i * (PI / 5.0)
            val r = if (i % 2 == 0) outerR else innerR
            val px = cx + (r * cos(angle)).toFloat()
            val py = cy + (r * sin(angle)).toFloat()
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    else -> if (extras.useCustomCorners) {
        RoundedCornerShape(
            topStart = extras.cornerTopStartDp.coerceIn(0f, 74f).dp,
            topEnd = extras.cornerTopEndDp.coerceIn(0f, 74f).dp,
            bottomEnd = extras.cornerBottomEndDp.coerceIn(0f, 74f).dp,
            bottomStart = extras.cornerBottomStartDp.coerceIn(0f, 74f).dp,
        )
    } else {
        RoundedCornerShape(element.cornerRadiusDp.coerceIn(0f, 74f).dp)
    }
}

@Composable
private fun ShortcutLayoutSlot(
    placement: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    showLabel: Boolean,
) {
    when (placement) {
        "ICON_ONLY" -> Box(modifier = modifier, contentAlignment = Alignment.Center) { icon() }
        "LABEL_ONLY" -> Box(modifier = modifier, contentAlignment = Alignment.Center) { if (showLabel) label() }
        "OVERLAY" -> Box(modifier = modifier, contentAlignment = Alignment.Center) {
            icon()
            if (showLabel) {
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp)) { label() }
            }
        }
        "LEFT" -> Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            icon()
            if (showLabel) {
                Spacer(Modifier.width(6.dp))
                label()
            }
        }
        "RIGHT" -> Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (showLabel) {
                label()
                Spacer(Modifier.width(6.dp))
            }
            icon()
        }
        "BOTTOM" -> Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (showLabel) {
                label()
                Spacer(Modifier.height(4.dp))
            }
            icon()
        }
        else -> Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            icon()
            if (showLabel) {
                Spacer(Modifier.height(4.dp))
                label()
            }
        }
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
    onOpenSearch: () -> Unit = {},
    onJumpToPage: (Int) -> Unit = {},
    onSaveElement: (HomeElementEntity, Boolean) -> Unit = { _, _ -> },
    onCycleStack: (Long, Int) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val extras = remember(element.styleExtrasJson) { element.parsedExtras() }
    val shape = resolveMaskShape(element, extras)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var pointerPressed by remember { mutableStateOf(false) }

    // Entrance animation
    val entranceProgress = remember(element.elementId, extras.entranceAnim) {
        Animatable(if (extras.entranceAnim == "NONE") 1f else 0f)
    }
    LaunchedEffect(element.elementId, extras.entranceAnim) {
        if (extras.entranceAnim != "NONE") {
            entranceProgress.snapTo(0f)
            if (extras.entranceAnim == "BOUNCE") {
                entranceProgress.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 320f))
            } else {
                entranceProgress.animateTo(1f, tween(340, easing = FastOutSlowInEasing))
            }
        }
    }

    // Idle animation
    val idleTransition = rememberInfiniteTransition(label = "elem_idle_${element.elementId}")
    val idleFloat by if (extras.idleAnim == "FLOAT") {
        idleTransition.animateFloat(-4f, 4f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "float")
    } else remember { mutableStateOf(0f) }
    val idleScale by if (extras.idleAnim == "PULSE" || extras.idleAnim == "BREATHE") {
        idleTransition.animateFloat(0.97f, 1.03f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    } else remember { mutableStateOf(1f) }
    val idleSpin by if (extras.idleAnim == "SPIN_SLOW") {
        idleTransition.animateFloat(0f, 360f, infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart), label = "spin")
    } else remember { mutableStateOf(0f) }

    var tapSpinTarget by remember { mutableStateOf(0f) }
    val tapSpinDeg by animateFloatAsState(tapSpinTarget, tween(360), label = "tap_spin")
    val effectivePressed = isPressed || pointerPressed
    val basePressedScale = if (extras.pressedScale != 0.94f) extras.pressedScale else state.settings.pressFeedbackScale
    val pressScaleTarget = if (effectivePressed && !state.isEditMode && extras.tapAnim != "NONE") basePressedScale.coerceIn(0.75f, 1.15f) else 1f
    val animPressScale by animateFloatAsState(pressScaleTarget, tween(110), label = "press_scale")

    val isPlainAppShortcut = element.type == "APP_SHORTCUT"
    val effectiveBgHex = if (effectivePressed && !state.isEditMode && extras.pressedBgColorHex != null) extras.pressedBgColorHex else element.backgroundColorHex
    val effectiveTextHex = if (effectivePressed && !state.isEditMode && extras.pressedTextColorHex != null) extras.pressedTextColorHex else element.textColorHex

    val bgGradEnd = if (!isPlainAppShortcut) element.backgroundGradientEndHex else null
    val bg = if (element.type == "APP_SHORTCUT" || element.type == "APP_WIDGET" || bgGradEnd != null) Color.Transparent else Color(effectiveBgHex)
    val gradBrush = bgGradEnd?.let { endHex ->
        val colors = listOf(Color(effectiveBgHex), Color(endHex))
        when (extras.gradientDirection) {
            "HORIZONTAL" -> Brush.horizontalGradient(colors)
            "DIAGONAL" -> Brush.linearGradient(colors)
            "RADIAL" -> Brush.radialGradient(colors)
            else -> Brush.verticalGradient(colors)
        }
    }

    val glowModifier = if (extras.shadowColorHex != null && extras.shadowBlurDp > 0.5f) {
        val glowArgb = Color(extras.shadowColorHex).toArgb()
        Modifier.drawBehind {
            drawIntoCanvas { canvas ->
                val frameworkPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        extras.shadowBlurDp.dp.toPx(),
                        extras.shadowOffsetXDp.dp.toPx(),
                        extras.shadowOffsetYDp.dp.toPx(),
                        glowArgb,
                    )
                }
                val paint = Paint().apply { asFrameworkPaint().set(frameworkPaint) }
                val r = element.cornerRadiusDp.dp.toPx()
                canvas.drawRoundRect(0f, 0f, size.width, size.height, r, r, paint)
            }
        }
    } else Modifier

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

    val configuredTriggers = remember(element.actionsConfigJson) {
        if (element.actionsConfigJson.isBlank()) emptySet()
        else try {
            val obj = JSONObject(element.actionsConfigJson)
            obj.keys().asSequence().filter { key ->
                obj.optJSONObject(key)?.optString("actionType", "NONE") != "NONE"
            }.toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    val firePrimaryTap: () -> Unit = {
        if (extras.tapAnim == "SPIN") tapSpinTarget += 360f
        when (element.type) {
            "APP_SHORTCUT" -> {
                val tapActionHandled = "TAP" in configuredTriggers && onElementTrigger(element, "TAP")
                if (!tapActionHandled) {
                    val targetApp = state.appsWithTags.find { it.app.packageName == element.targetPackageName }?.app
                    targetApp?.let { app ->
                        val shortcutId = element.folderAppPackagesCsv.takeIf { it.isNotBlank() }
                        val isLocked = app.packageName in state.settings.lockedAppsCsv.split(",").map(String::trim)
                        if (shortcutId != null && !isLocked) {
                            if (!appDiscovery.launchAppShortcut(app.packageName, shortcutId, app.componentName)) {
                                onLaunchApp(app)
                            }
                        } else {
                            onLaunchApp(app)
                        }
                    }
                }
            }
            "CUSTOM_BUTTON" -> {
                onElementTrigger(element, "TAP")
            }
            "TAG_SHORTCUT" -> {
                val tapActionHandled = "TAP" in configuredTriggers && onElementTrigger(element, "TAP")
                if (!tapActionHandled) {
                    element.targetTagId?.let { onOpenTagSidebar(it) }
                }
            }
            else -> {
                onElementTrigger(element, "TAP")
            }
        }
    }

    val latestTriggers by rememberUpdatedState(configuredTriggers)
    val latestPrimaryTap by rememberUpdatedState(firePrimaryTap)
    val latestOnTrigger by rememberUpdatedState(onElementTrigger)
    val latestElement by rememberUpdatedState(element)
    val latestLongPressMs by rememberUpdatedState(state.settings.longPressDelayMs.toLong().coerceIn(250L, 900L))
    val latestSensitivity by rememberUpdatedState(state.settings.swipeGestureSensitivity.coerceIn(0.5f, 1.8f))

    val handlesPointerGestures = !state.isEditMode &&
        (element.type == "APP_SHORTCUT" || element.type == "CUSTOM_BUTTON" || configuredTriggers.isNotEmpty())

    val gestureMod = if (handlesPointerGestures) {
        Modifier.pointerInput(element.elementId, element.type, element.actionsConfigJson) {
            val swipeThresholdPx = (36f / latestSensitivity).dp.toPx().coerceAtLeast(16f)
            val touchSlopPx = viewConfiguration.touchSlop
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val downPos = down.position
                var currentTimeMs = down.uptimeMillis
                pointerPressed = true
                var finishedBySwipeOrLongPress = false
                var isTapUp = false

                try {
                    while (true) {
                        val elapsed = (currentTimeMs - down.uptimeMillis).coerceAtLeast(0L)
                        val remainingMs = (latestLongPressMs - elapsed).coerceAtLeast(1L)
                        val event = withTimeoutOrNull(remainingMs) { awaitPointerEvent() }
                        if (event == null) {
                            pointerPressed = false
                            if ("LONG_PRESS" in latestTriggers) {
                                latestOnTrigger(latestElement, "LONG_PRESS")
                                finishedBySwipeOrLongPress = true
                            }
                            break
                        }
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change != null) {
                            currentTimeMs = change.uptimeMillis
                        }
                        if (change == null || !change.pressed) {
                            val upPos = change?.position ?: downPos
                            val moveDist = hypot(upPos.x - downPos.x, upPos.y - downPos.y)
                            isTapUp = moveDist <= maxOf(touchSlopPx, swipeThresholdPx * 0.5f)
                            if (isTapUp && (latestElement.type == "APP_SHORTCUT" || latestElement.type == "CUSTOM_BUTTON" || "TAP" in latestTriggers || "DOUBLE_TAP" in latestTriggers)) {
                                change?.consume()
                            }
                            break
                        }
                        val dx = change.position.x - downPos.x
                        val dy = change.position.y - downPos.y
                        val dist = hypot(dx, dy)
                        if (dist >= swipeThresholdPx) {
                            val swipeTrigger = if (abs(dx) > abs(dy)) {
                                if (dx < 0f) "SWIPE_LEFT" else "SWIPE_RIGHT"
                            } else {
                                if (dy < 0f) "SWIPE_UP" else "SWIPE_DOWN"
                            }
                            pointerPressed = false
                            if (swipeTrigger in latestTriggers) {
                                change.consume()
                                latestOnTrigger(latestElement, swipeTrigger)
                                while (true) {
                                    val next = awaitPointerEvent()
                                    next.changes.forEach { it.consume() }
                                    if (next.changes.none { it.pressed }) break
                                }
                            }
                            finishedBySwipeOrLongPress = true
                            break
                        }
                    }
                } finally {
                    pointerPressed = false
                }

                if (!finishedBySwipeOrLongPress && isTapUp) {
                    if ("DOUBLE_TAP" in latestTriggers) {
                        val secondDown = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
                            awaitFirstDown(requireUnconsumed = false)
                        }
                        if (secondDown != null) {
                            var secondTapped = false
                            val secondDownPos = secondDown.position
                            while (true) {
                                val ev2 = awaitPointerEvent()
                                val ch2 = ev2.changes.firstOrNull { it.id == secondDown.id } ?: ev2.changes.firstOrNull()
                                if (ch2 == null || !ch2.pressed) {
                                    val d2 = ch2?.let { hypot(it.position.x - secondDownPos.x, it.position.y - secondDownPos.y) } ?: 0f
                                    if (d2 <= maxOf(touchSlopPx, swipeThresholdPx * 0.5f)) {
                                        ch2?.consume()
                                        secondTapped = true
                                    }
                                    break
                                }
                            }
                            if (secondTapped) {
                                latestOnTrigger(latestElement, "DOUBLE_TAP")
                            } else {
                                latestPrimaryTap()
                            }
                        } else {
                            latestPrimaryTap()
                        }
                    } else {
                        latestPrimaryTap()
                    }
                } else if (finishedBySwipeOrLongPress) {
                    while (currentEvent.changes.any { it.pressed }) {
                        val next = awaitPointerEvent()
                        if ("LONG_PRESS" in latestTriggers) next.changes.forEach { it.consume() }
                    }
                }
            }
        }
    } else Modifier

    val transform3DAndMotion = Modifier.graphicsLayer {
        val ent = entranceProgress.value
        val entScale = when (extras.entranceAnim) {
            "SCALE", "BOUNCE" -> 0.65f + 0.35f * ent
            else -> 1f
        }
        val entAlpha = if (extras.entranceAnim == "FADE" || extras.entranceAnim == "SLIDE_UP") ent else 1f
        val entSlideY = if (extras.entranceAnim == "SLIDE_UP") (1f - ent) * 48f else 0f

        rotationX = extras.rotationXDeg
        rotationY = extras.rotationYDeg
        rotationZ = idleSpin + tapSpinDeg
        cameraDistance = extras.cameraDistance.coerceIn(4f, 40f) * density
        scaleX = entScale * animPressScale * idleScale
        scaleY = entScale * animPressScale * idleScale
        translationY = idleFloat + entSlideY
        alpha = entAlpha
    }

    Box(modifier = Modifier.fillMaxSize().then(transform3DAndMotion).then(glowModifier)) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .then(gestureMod)
                .testTag("home_element_content_${element.elementId}")
                .semantics {
                    if (handlesPointerGestures) {
                        onClick {
                            latestPrimaryTap()
                            true
                        }
                    }
                },
            shape = shape,
            color = bg,
            shadowElevation = element.shadowElevationDp.coerceIn(0f, 24f).dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (gradBrush != null) Modifier.background(gradBrush) else Modifier),
            ) {
                val activeImagePath = if (isPressed && !state.isEditMode && !extras.pressedImagePath.isNullOrBlank()) {
                    extras.pressedImagePath
                } else {
                    element.imagePath
                }
                if (element.type != "IMAGE" && !activeImagePath.isNullOrBlank() && File(activeImagePath).exists()) {
                    AsyncImage(
                        model = File(activeImagePath),
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

                    "WIDGET_STACK" -> WidgetStackCard(
                        element = element,
                        extras = extras,
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
                        onOpenSearch = onOpenSearch,
                        onJumpToPage = onJumpToPage,
                        onSaveElement = onSaveElement,
                        onCycleStack = onCycleStack,
                    )
                    "GAUGE_WIDGET" -> GraphicGaugeWidget(element = element, extras = extras)
                    "CONTAINER_BOX" -> ContainerBoxWidget(
                        element = element,
                        extras = extras,
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
                        onOpenSearch = onOpenSearch,
                        onJumpToPage = onJumpToPage,
                        onSaveElement = onSaveElement,
                        onCycleStack = onCycleStack,
                    )
                    "APP_GROUP_WIDGET" -> AppGroupWidget(
                        element = element,
                        extras = extras,
                        state = state,
                        appDiscovery = appDiscovery,
                        onLaunchApp = onLaunchApp,
                    )
                    "PAGE_SWITCHER_WIDGET" -> PageSwitcherWidget(
                        element = element,
                        extras = extras,
                        state = state,
                        onJumpToPage = onJumpToPage,
                    )
                    "SEARCH_BAR_WIDGET" -> EmbeddedSearchBarWidget(
                        element = element,
                        extras = extras,
                        isEditMode = state.isEditMode,
                        onOpenSearch = onOpenSearch,
                    )
                    "QUICK_NOTES_WIDGET" -> QuickNotesWidget(
                        element = element,
                        extras = extras,
                        isEditMode = state.isEditMode,
                        onSaveElement = onSaveElement,
                    )
                    "FOCUS_TIMER_WIDGET" -> FocusTimerWidget(
                        element = element,
                        extras = extras,
                        isEditMode = state.isEditMode,
                    )
                    "MINI_CALCULATOR_WIDGET" -> MiniCalculatorWidget(
                        element = element,
                        isEditMode = state.isEditMode,
                    )
                    "DEVICE_TOGGLE_WIDGET" -> DeviceToggleWidget(
                        element = element,
                        extras = extras,
                        isEditMode = state.isEditMode,
                    )

                    "TEXT_LABEL" -> {
                        val resolvedText = resolveExtendedDynamicTokens(element.label.ifBlank { "Label" }, state, context)
                        Box(
                            modifier = Modifier.fillMaxSize().padding(element.paddingDp.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            ReadableLauncherText(
                                text = resolvedText,
                                color = Color(effectiveTextHex),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = element.fontSizeSp.sp, fontWeight = FontWeight.SemiBold),
                                textAlign = TextAlign.Center,
                                maxLines = extras.maxLines.coerceAtLeast(1),
                                extras = extras,
                                surfaceHintColor = bg,
                            )
                        }
                    }

                    "SHAPE" -> {
                        if (element.showLabel && element.label.isNotBlank()) {
                            Box(modifier = Modifier.fillMaxSize().padding(element.paddingDp.dp), contentAlignment = Alignment.Center) {
                                ReadableLauncherText(
                                    text = resolveExtendedDynamicTokens(element.label, state, context),
                                    color = Color(effectiveTextHex),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = element.fontSizeSp.sp),
                                    textAlign = TextAlign.Center,
                                    extras = extras,
                                    surfaceHintColor = bg,
                                )
                            }
                        }
                    }

                    "CUSTOM_BUTTON" -> {
                        ShortcutLayoutSlot(
                            placement = extras.iconPlacement.takeIf { it != "TOP" } ?: "LEFT",
                            modifier = Modifier
                                .fillMaxSize()
                                .semantics {
                                    role = Role.Button
                                    onClick {
                                        if (!state.isEditMode) firePrimaryTap()
                                        true
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            showLabel = element.showLabel,
                            icon = {
                                Icon(Icons.Rounded.TouchApp, null, tint = Color(effectiveTextHex), modifier = Modifier.size(18.dp))
                            },
                            label = {
                                ReadableLauncherText(
                                    text = resolveExtendedDynamicTokens(element.label.ifBlank { "Action" }, state, context),
                                    color = Color(effectiveTextHex),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = element.fontSizeSp.sp, fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    extras = extras,
                                    surfaceHintColor = bg,
                                )
                            },
                        )
                    }

                    "IMAGE" -> {
                        val file = activeImagePath?.let { File(it) }?.takeIf { it.exists() }
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
                        val shortcutSemanticsMod = if (!state.isEditMode && app != null) {
                            Modifier.semantics {
                                role = Role.Button
                                onClick {
                                    firePrimaryTap()
                                    true
                                }
                            }
                        } else Modifier
                        if (app != null) {
                            ShortcutLayoutSlot(
                                placement = extras.iconPlacement,
                                modifier = Modifier.fillMaxSize().then(shortcutSemanticsMod).padding(element.paddingDp.dp),
                                showLabel = element.showLabel && state.settings.showHomeAppLabels,
                                icon = {
                                    AppIconView(
                                        app = app,
                                        appDiscovery = appDiscovery,
                                        size = element.iconSizeDp.coerceIn(24f, 96f).dp,
                                        iconShapeKey = state.settings.globalIconShape,
                                    )
                                },
                                label = {
                                    ReadableLauncherText(
                                        text = element.label.ifBlank { app.displayName },
                                        color = Color(effectiveTextHex),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = element.fontSizeSp.sp),
                                        maxLines = extras.maxLines,
                                        textAlign = TextAlign.Center,
                                        extras = extras,
                                        forceOutline = if (state.settings.appLabelOutlineEnabled) true else null,
                                    )
                                },
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(element.paddingDp.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(Icons.Rounded.Apps, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                                Text("Missing app", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }

                    "TAG_SHORTCUT" -> {
                        val tag = state.allTags.find { it.tagId == element.targetTagId }
                        val count = state.tagsWithApps.find { it.tag.tagId == element.targetTagId }
                            ?.apps?.count { it.isInstalled && !it.hidden } ?: 0
                        if (tag != null) {
                            val tagInteractMod = if (configuredTriggers.isEmpty()) {
                                Modifier.clickable(enabled = !state.isEditMode) { onOpenTagSidebar(tag.tagId) }
                            } else {
                                Modifier.semantics {
                                    role = Role.Button
                                    onClick {
                                        if (!state.isEditMode) firePrimaryTap()
                                        true
                                    }
                                }
                            }
                            ShortcutLayoutSlot(
                                placement = extras.iconPlacement,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(tagInteractMod)
                                    .padding(element.paddingDp.coerceIn(0f, 32f).dp)
                                    .testTag("home_tag_shortcut_${element.targetTagId}"),
                                showLabel = element.showLabel && state.settings.showHomeAppLabels,
                                icon = { TagBadgeIcon(tag.iconKey, tag.customImagePath, tag.colorHex, element.iconSizeDp.coerceIn(24f, 64f).dp) },
                                label = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        ReadableLauncherText(
                                            text = tag.name,
                                            color = Color(effectiveTextHex),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = element.fontSizeSp.sp, fontWeight = FontWeight.SemiBold),
                                            maxLines = 1,
                                            extras = extras,
                                            surfaceHintColor = bg,
                                        )
                                        Text(
                                            text = "$count",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(Color.Black.copy(alpha = 0.62f)).padding(horizontal = 5.dp, vertical = 1.dp),
                                        )
                                    }
                                },
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Tag removed", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            }
                        }
                    }

                    "CATEGORY_SHORTCUT" -> {
                        val cat = state.categories.find { it.categoryId == element.targetCategoryId }
                        if (cat != null) {
                            ShortcutLayoutSlot(
                                placement = extras.iconPlacement,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(enabled = !state.isEditMode) { onOpenCategorySidebar(cat.categoryId) }
                                    .padding(element.paddingDp.coerceIn(0f, 32f).dp),
                                showLabel = element.showLabel && state.settings.showHomeAppLabels,
                                icon = { CategoryBadgeIcon(cat, size = element.iconSizeDp.coerceIn(24f, 64f).dp) },
                                label = {
                                    ReadableLauncherText(
                                        text = cat.name,
                                        color = Color(effectiveTextHex),
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = element.fontSizeSp.sp),
                                        maxLines = 1,
                                        extras = extras,
                                    )
                                },
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Category, null, tint = Color.White)
                            }
                        }
                    }

                    "WINDOW_SHORTCUT" -> {
                        val win = state.windows.find { it.windowId == element.targetWindowId }
                        ShortcutLayoutSlot(
                            placement = extras.iconPlacement,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(enabled = !state.isEditMode && win != null) { win?.let { onOpenWindow(it.windowId) } }
                                .padding(element.paddingDp.coerceIn(0f, 32f).dp),
                            showLabel = element.showLabel,
                            icon = { Icon(Icons.Rounded.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp)) },
                            label = {
                                ReadableLauncherText(
                                    text = win?.title ?: element.label.ifBlank { "Window" },
                                    color = Color(effectiveTextHex),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = element.fontSizeSp.sp, fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    extras = extras,
                                )
                            },
                        )
                    }

                    "SAVED_RULE_SHORTCUT" -> {
                        val rule = state.savedRules.find { it.ruleId == element.targetRuleId }
                        ShortcutLayoutSlot(
                            placement = extras.iconPlacement,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(enabled = !state.isEditMode && rule != null) { rule?.let { onOpenRuleSidebar(it.ruleId) } }
                                .padding(element.paddingDp.coerceIn(0f, 32f).dp),
                            showLabel = element.showLabel,
                            icon = { TagBadgeIcon(rule?.iconKey ?: "filter", null, rule?.colorHex ?: 0xFF6366F1L, 36.dp) },
                            label = {
                                ReadableLauncherText(
                                    text = rule?.name ?: element.label.ifBlank { "Rule" },
                                    color = Color(effectiveTextHex),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = element.fontSizeSp.sp, fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    extras = extras,
                                )
                            },
                        )
                    }

                    "SIDEBAR_SHORTCUT" -> {
                        val sb = state.sidebars.find { it.sidebarId == element.targetSidebarId }
                        ShortcutLayoutSlot(
                            placement = extras.iconPlacement,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(enabled = !state.isEditMode && sb != null) { sb?.let { onOpenSidebar(it.sidebarId) } }
                                .padding(element.paddingDp.coerceIn(0f, 32f).dp),
                            showLabel = element.showLabel,
                            icon = { Icon(Icons.AutoMirrored.Rounded.ViewSidebar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp)) },
                            label = {
                                ReadableLauncherText(
                                    text = sb?.title ?: element.label.ifBlank { "Sidebar" },
                                    color = Color(effectiveTextHex),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = element.fontSizeSp.sp),
                                    maxLines = 1,
                                    extras = extras,
                                )
                            },
                        )
                    }

                    "FOLDER" -> {
                        val pkgSet = element.folderPackages().toSet()
                        val previewApps = state.appsWithTags.map { it.app }.filter { it.packageName in pkgSet }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(enabled = !state.isEditMode) { onOpenFolder(element.elementId) }
                                .padding(element.paddingDp.coerceIn(0f, 32f).dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            when {
                                previewApps.isEmpty() -> Icon(Icons.Rounded.Folder, contentDescription = null, tint = Color.White)
                                state.settings.folderCoverStyle == "GRID_3X3" -> {
                                    val chunked = previewApps.take(9).chunked(3)
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        chunked.forEach { r ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                r.forEach { app -> AppIconView(app, appDiscovery, 15.dp, state.settings.globalIconShape) }
                                            }
                                        }
                                    }
                                }
                                state.settings.folderCoverStyle == "STACK" -> {
                                    Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                                        previewApps.take(3).reversed().forEachIndexed { idx, app ->
                                            val off = (idx * 5 - 5).dp
                                            Box(modifier = Modifier.offset(x = off, y = (-idx * 2).dp)) {
                                                AppIconView(app, appDiscovery, 32.dp, state.settings.globalIconShape)
                                            }
                                        }
                                    }
                                }
                                state.settings.folderCoverStyle == "FAN" -> {
                                    Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                                        previewApps.take(3).forEachIndexed { idx, app ->
                                            val rot = (idx - 1) * 14f
                                            Box(modifier = Modifier.offset(x = ((idx - 1) * 7).dp).graphicsLayer { rotationZ = rot }) {
                                                AppIconView(app, appDiscovery, 28.dp, state.settings.globalIconShape)
                                            }
                                        }
                                    }
                                }
                                else -> { // GRID_2X2
                                    val gridApps = previewApps.take(4)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        gridApps.take(2).forEach { app -> AppIconView(app, appDiscovery, 22.dp, state.settings.globalIconShape) }
                                    }
                                    if (gridApps.size > 2) {
                                        Spacer(Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            gridApps.drop(2).forEach { app -> AppIconView(app, appDiscovery, 22.dp, state.settings.globalIconShape) }
                                        }
                                    }
                                }
                            }
                            if (element.showLabel && state.settings.showHomeAppLabels) {
                                Spacer(Modifier.height(6.dp))
                                ReadableLauncherText(
                                    text = element.label.ifBlank { "Folder" },
                                    color = Color(effectiveTextHex),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = element.fontSizeSp.sp),
                                    maxLines = 1,
                                    extras = extras,
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
                                    .clickable(enabled = !state.isEditMode && tag != null) { tag?.let { onOpenTagSidebar(it.tagId) } },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (tag != null) {
                                    TagBadgeIcon(tag.iconKey, tag.customImagePath, tag.colorHex, 28.dp)
                                    Spacer(Modifier.width(8.dp))
                                    ReadableLauncherText(
                                        text = tag.name,
                                        color = Color(effectiveTextHex),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.weight(1f),
                                        extras = extras,
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
                                        ReadableLauncherText(
                                            text = app.displayName,
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
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
                                factory = { ctx -> widgetHost.createHostView(ctx, widgetId) ?: FrameLayout(ctx) },
                                update = { widgetHost.updateWidgetSize(widgetId, element.widthDp.roundToInt(), element.heightDp.roundToInt()) },
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

                // Optional custom badge pill
                if (extras.badgeText.isNotBlank()) {
                    val badgeAlign = when (extras.badgePosition) {
                        "TOP_START" -> Alignment.TopStart
                        "BOTTOM_END" -> Alignment.BottomEnd
                        else -> Alignment.TopEnd
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color(extras.badgeColorHex),
                        modifier = Modifier.align(badgeAlign).padding(4.dp),
                    ) {
                        Text(
                            text = extras.badgeText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
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
                        AddTypeRow(Icons.Rounded.ViewAgenda, "Widget Stack", "Swipeable stack of multiple widgets in one slot") {
                            onAddElement(
                                HomeElementEntity(
                                    pageId = pageId, ownerType = ownerType, ownerWindowId = ownerWindowId,
                                    type = "WIDGET_STACK", xFraction = 0.08f, yFraction = 0.14f,
                                    widthDp = 280f, heightDp = 160f, label = "Widget Stack",
                                    backgroundColorHex = 0xAA0E1629L, borderColorHex = 0x6638BDF8L, cornerRadiusDp = 24f,
                                )
                            )
                            onDismiss()
                        }
                        AddTypeRow(Icons.Rounded.Speed, "Graphic Gauge", "Arc, Ring, Segmented, Linear or Sparkline meter") {
                            onAddElement(
                                HomeElementEntity(
                                    pageId = pageId, ownerType = ownerType, ownerWindowId = ownerWindowId,
                                    type = "GAUGE_WIDGET", xFraction = 0.14f, yFraction = 0.22f,
                                    widthDp = 152f, heightDp = 152f, label = "Battery",
                                    backgroundColorHex = 0x990F172AL, textColorHex = 0xFF34D399L, cornerRadiusDp = 24f,
                                )
                            )
                            onDismiss()
                        }
                        AddTypeRow(Icons.Rounded.Widgets, "Widgets (System & Built-in)", "Clocks, Notes, Focus Timer, Calc, Toggles & Android Widgets") {
                            onDismiss()
                            onOpenWidgetPicker()
                        }
                        AddTypeRow(Icons.Rounded.LocalOffer, "Tag Shortcut", "Tapping opens this tag's sidebar") { step = "TAG" }
                        AddTypeRow(Icons.Rounded.Category, "Category / Game Shortcut", "Tapping opens Play category sidebar") { step = "CATEGORY" }
                        AddTypeRow(Icons.Rounded.Apps, "App Shortcut", "Freely positioned app launcher") { step = "APP" }
                        AddTypeRow(Icons.Rounded.Folder, "Custom Folder", "Group apps & nested folders") { step = "FOLDER" }
                        AddTypeRow(Icons.Rounded.Layers, "Window Trigger", "Open a Total Launcher overlay window") { step = "WINDOW" }
                        AddTypeRow(Icons.Rounded.Bolt, "Dynamic Tag Rule", "Shortcut to AND/OR/EXCLUDE collection") { step = "RULE" }
                        AddTypeRow(Icons.AutoMirrored.Rounded.ViewSidebar, "Sidebar Trigger", "Button that opens a configured drawer") { step = "SIDEBAR" }
                        AddTypeRow(Icons.Rounded.SmartButton, "Custom Interactive Button", "Multi-gesture / macro button") { step = "BUTTON" }
                        AddTypeRow(Icons.Rounded.TextFields, "Dynamic Text Label", "{TIME}, {BATTERY}, {NETWORK}, {GREETING}, {PAGE_NAME}") { step = "TEXT" }
                        AddTypeRow(Icons.Rounded.EditNote, "Quick Notes / Timer / Calc / Search", "Pick interactive utility widgets") {
                            onDismiss()
                            onOpenWidgetPicker()
                        }
                        AddTypeRow(Icons.Rounded.Category, "Decorative Shape / Panel", "Resizable glass/colored geometric layer") {
                            onAddElement(
                                HomeElementEntity(
                                    pageId = pageId, ownerType = ownerType, ownerWindowId = ownerWindowId,
                                    type = "SHAPE", xFraction = 0.15f, yFraction = 0.25f,
                                    widthDp = 180f, heightDp = 120f, label = "", showLabel = false,
                                    backgroundColorHex = 0x5538BDF8L, borderColorHex = 0x9938BDF8L, cornerRadiusDp = 28f,
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
    onCopyStyle: () -> Unit = {},
    onPasteStyle: () -> Unit = {},
    onUnstack: () -> Unit = {},
    onAddChildToContainer: (Long, HomeElementEntity) -> Unit = { _, _ -> },
    onMoveChildInParent: (Long, Int) -> Unit = { _, _ -> },
    onInspectChild: (HomeElementEntity) -> Unit = {},
    onDeleteChild: (HomeElementEntity) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var edited by remember(element.elementId) { mutableStateOf(element) }
    var extras by remember(element.elementId) { mutableStateOf(element.parsedExtras()) }
    var showFullTransform by remember { mutableStateOf(false) }
    var showMacroBuilder by remember { mutableStateOf(false) }
    var inspectorTab by remember { mutableStateOf("LAYOUT") }

    fun updateExtras(next: ElementStyleExtras) {
        extras = next
        edited = edited.copy(styleExtrasJson = next.toJson())
    }

    LaunchedEffect(element.locked, element.zIndex) {
        edited = edited.copy(locked = element.locked, zIndex = element.zIndex)
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
                extras = it.parsedExtras()
                onUpdate(it)
                showFullTransform = false
            },
            onDismiss = { showFullTransform = false },
        )
        return
    }

    if (showMacroBuilder) {
        val currentTapMacro = remember(edited.actionsConfigJson) {
            runCatching {
                if (edited.actionsConfigJson.isBlank()) null
                else JSONObject(edited.actionsConfigJson).optJSONObject("TAP")?.optJSONArray("macro")?.toString()
            }.getOrNull()
        }
        MacroStepBuilderDialog(
            initialMacroJson = currentTapMacro,
            state = state,
            onSave = { macroArrayJson ->
                val root = runCatching {
                    if (edited.actionsConfigJson.isBlank()) JSONObject() else JSONObject(edited.actionsConfigJson)
                }.getOrDefault(JSONObject())
                root.put("TAP", JSONObject().apply {
                    put("actionType", "MACRO")
                    put("macro", JSONArray(macroArrayJson))
                })
                edited = edited.copy(actionsConfigJson = root.toString())
                showMacroBuilder = false
            },
            onDismiss = { showMacroBuilder = false },
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
                    .heightIn(max = 500.dp),
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
                    IconButton(onClick = onDuplicate) { Icon(Icons.Rounded.AutoAwesome, contentDescription = "Duplicate") }
                    IconButton(onClick = onCopy) { Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy") }
                    IconButton(onClick = onCopyStyle, modifier = Modifier.testTag("inspector_copy_style")) {
                        Icon(Icons.Rounded.FormatPaint, contentDescription = "Copy style", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onCut) { Icon(Icons.Rounded.ContentCut, contentDescription = "Cut") }
                    IconButton(onClick = onBringForward) { Icon(Icons.Rounded.FlipToFront, contentDescription = "Bring forward") }
                    IconButton(onClick = onSendBackward) { Icon(Icons.Rounded.FlipToBack, contentDescription = "Send backward") }
                    IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
                }

                if (state.copiedStyle != null || edited.type == "WIDGET_STACK") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        if (state.copiedStyle != null) {
                            OutlinedButton(
                                onClick = {
                                    onPasteStyle()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f).testTag("inspector_paste_style"),
                            ) { Text("Paste Copied Style") }
                        }
                        if (edited.type == "WIDGET_STACK") {
                            OutlinedButton(
                                onClick = {
                                    onUnstack()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f).testTag("inspector_unstack_btn"),
                            ) { Text("Unstack Items") }
                        }
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
                        "STYLE" to "Style FX",
                        "ANIM3D" to "3D & Anim",
                        "TEXT" to "Text & Place",
                        "RULES" to "Widgets & Rules",
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
                        "LAYOUT" -> InspectorLayoutTab(edited = edited, onUpdateEdited = { edited = it })
                        "STYLE" -> InspectorStyleFxTab(
                            edited = edited,
                            extras = extras,
                            onUpdateEdited = { edited = it },
                            onUpdateExtras = ::updateExtras,
                            onPickBackgroundImage = {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        )
                        "ANIM3D" -> InspectorAnim3DTab(extras = extras, onUpdateExtras = ::updateExtras)
                        "TEXT" -> InspectorTextPlaceTab(
                            edited = edited,
                            extras = extras,
                            state = state,
                            onUpdateEdited = { edited = it },
                            onUpdateExtras = ::updateExtras,
                        )
                        "RULES" -> InspectorWidgetsAndRulesTab(
                            edited = edited,
                            extras = extras,
                            state = state,
                            onUpdateEdited = { edited = it },
                            onUpdateExtras = ::updateExtras,
                            onAddChildToContainer = onAddChildToContainer,
                            onMoveChildInParent = onMoveChildInParent,
                            onInspectChild = onInspectChild,
                            onDeleteChild = onDeleteChild,
                            onOpenMacroBuilder = { showMacroBuilder = true },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(edited.copy(styleExtrasJson = extras.toJson()))
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
                    if (working.backgroundImagePath != null || working.landscapeBackgroundImagePath != null) {
                        HorizontalDivider()
                        Text("Drag to reposition • pinch to zoom", style = MaterialTheme.typography.bodySmall)
                        ImageCropEditor(
                            imagePath = working.backgroundImagePath ?: working.landscapeBackgroundImagePath,
                            cropScale = working.backgroundCropScale,
                            cropX = working.backgroundCropX,
                            cropY = working.backgroundCropY,
                            onCropChange = { scale, x, y -> working = working.copy(backgroundCropScale = scale, backgroundCropX = x, backgroundCropY = y) },
                        )
                        Text("Background Image Zoom: ${String.format("%.1f", working.backgroundCropScale)}x")
                        Slider(
                            value = working.backgroundCropScale,
                            onValueChange = { working = working.copy(backgroundCropScale = it) },
                            valueRange = 1f..4f,
                        )
                        Text("Horizontal Crop: ${(working.backgroundCropX * 100).roundToInt()}%")
                        Slider(
                            value = working.backgroundCropX,
                            onValueChange = { working = working.copy(backgroundCropX = it) },
                            valueRange = -1f..1f,
                        )
                        Text("Vertical Crop: ${(working.backgroundCropY * 100).roundToInt()}%")
                        Slider(
                            value = working.backgroundCropY,
                            onValueChange = { working = working.copy(backgroundCropY = it) },
                            valueRange = -1f..1f,
                        )
                        TextButton(onClick = {
                            working = working.copy(backgroundCropScale = 1f, backgroundCropX = 0f, backgroundCropY = 0f)
                        }) { Text("Reset Page Image Crop") }
                    }
                    Text("Page Blur: ${working.backgroundBlurDp.roundToInt()}dp")
                    Slider(value = working.backgroundBlurDp, onValueChange = { working = working.copy(backgroundBlurDp = it) }, valueRange = 0f..24f)
                    Text("Background Opacity: ${(working.backgroundAlpha * 100).roundToInt()}%")
                    Slider(value = working.backgroundAlpha, onValueChange = { working = working.copy(backgroundAlpha = it) }, valueRange = 0.05f..1f)

                    HorizontalDivider()
                    Text("Per-Page Transition Override", style = MaterialTheme.typography.labelLarge)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(null to "Inherit", "SLIDE" to "Slide", "DEPTH" to "Depth", "CUBE" to "Cube", "FLIP" to "Flip", "STACK" to "Stack", "ZOOM" to "Zoom", "WINDMILL" to "Windmill").forEach { (tr, lbl) ->
                            FilterChip(
                                selected = working.transitionOverride == tr,
                                onClick = { working = working.copy(transitionOverride = tr) },
                                label = { Text(lbl) },
                            )
                        }
                    }
                    Text("Dock Bar Override on This Page", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(null to "Inherit", true to "Show", false to "Hide").forEach { (v, lbl) ->
                            FilterChip(
                                selected = working.showDockOverride == v,
                                onClick = { working = working.copy(showDockOverride = v) },
                                label = { Text(lbl) },
                            )
                        }
                    }
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
