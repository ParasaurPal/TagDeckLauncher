package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.BatteryManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FlashlightOff
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AppEntity
import com.example.data.db.ElementStyleExtras
import com.example.data.db.HomeElementEntity
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.BatteryTelemetry
import com.example.data.system.DeviceControlsManager
import com.example.data.system.DeviceTelemetryProvider
import com.example.data.system.LauncherWidgetHost
import com.example.ui.components.AppIconView
import com.example.ui.components.ReadableLauncherText
import com.example.ui.state.LauncherUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt

fun resolveExtendedDynamicTokens(
    template: String,
    state: LauncherUiState,
    context: Context,
): String {
    if (!template.contains("{")) return template
    var result = resolveDynamicTokens(template, context)
    if (result.contains("{BATTERY}") || result.contains("{CHARGING}")) {
        val intent = runCatching {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }.getOrNull()
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100)?.coerceAtLeast(1) ?: 100
        val pct = if (level >= 0) ((level * 100f) / scale).roundToInt() else 100
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        result = result
            .replace("{BATTERY}", "$pct%")
            .replace("{CHARGING}", if (charging) "Charging" else "Battery")
    }
    if (result.contains("{WIFI}") || result.contains("{NETWORK}")) {
        val (netState, netKind) = DeviceControlsManager.readNetworkSummary(context)
        result = result
            .replace("{WIFI}", netKind)
            .replace("{NETWORK}", if (netKind == "None") netState else "$netKind $netState")
    }
    if (result.contains("{NEXT_ALARM}")) {
        result = result.replace("{NEXT_ALARM}", DeviceControlsManager.readNextAlarmText(context))
    }
    if (result.contains("{WEATHER_TEMP}")) {
        val weatherCity = state.homeElements
            .firstOrNull { it.type == "WEATHER_WIDGET" || it.type == "AT_A_GLANCE_WIDGET" }
            ?.label?.takeIf { it.isNotBlank() } ?: "Local"
        result = result.replace("{WEATHER_TEMP}", weatherCity)
    }
    if (result.contains("{PAGE_NAME}") || result.contains("{PAGE_INDEX}")) {
        val visiblePages = if (state.isEditMode) state.homePages else state.homePages.filter { !it.isHidden }
        val idx = state.currentPageIndex.coerceIn(0, (visiblePages.size - 1).coerceAtLeast(0))
        val pageName = visiblePages.getOrNull(idx)?.title ?: "Main Deck"
        result = result
            .replace("{PAGE_NAME}", pageName)
            .replace("{PAGE_INDEX}", "${idx + 1}/${visiblePages.size.coerceAtLeast(1)}")
    }
    if (result.contains("{GREETING}")) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
        result = result.replace("{GREETING}", greeting)
    }
    if (result.contains("{APP_COUNT}")) {
        val count = state.appsWithTags.count { it.app.isInstalled && !it.app.hidden }
        result = result.replace("{APP_COUNT}", count.toString())
    }
    return result
}

@Composable
fun WidgetStackCard(
    element: HomeElementEntity,
    extras: ElementStyleExtras,
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
    val rawChildren = remember(state.homeElements, element.elementId) {
        state.homeElements
            .filter { it.parentContainerId == element.elementId && it.visible }
            .sortedWith(compareBy<HomeElementEntity> { it.zIndex }.thenBy { it.elementId })
    }
    val displayChildren = remember(rawChildren, extras.stackSmartPriority, state.isEditMode) {
        if (!extras.stackSmartPriority || state.isEditMode || rawChildren.size <= 1) {
            rawChildren
        } else {
            val audioActive = runCatching {
                (context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)?.isMusicActive == true
            }.getOrDefault(false)
            val batteryIntent = runCatching {
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            }.getOrNull()
            val batPct = batteryIntent?.let {
                val lvl = it.getIntExtra(BatteryManager.EXTRA_LEVEL, 100)
                val scl = it.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
                (lvl * 100) / scl
            } ?: 100
            val priorityType = when {
                audioActive && rawChildren.any { it.type == "MEDIA_WIDGET" } -> "MEDIA_WIDGET"
                batPct <= 25 && rawChildren.any { it.type == "BATTERY_WIDGET" || it.type == "GAUGE_WIDGET" } -> "BATTERY_WIDGET"
                else -> null
            }
            if (priorityType != null) {
                rawChildren.sortedByDescending { it.type == priorityType }
            } else rawChildren
        }
    }

    if (displayChildren.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("widget_stack_empty_${element.elementId}"),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Layers, contentDescription = null, tint = Color(element.textColorHex).copy(alpha = 0.7f))
                Spacer(Modifier.height(4.dp))
                ReadableLauncherText(
                    text = element.label.ifBlank { "Widget Stack (Empty)" },
                    color = Color(element.textColorHex),
                    style = MaterialTheme.typography.labelLarge,
                    extras = extras,
                )
                ReadableLauncherText(
                    text = "Add widgets via Edit -> Inspector",
                    color = Color(element.textColorHex).copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = 0) { displayChildren.size }
    val overrideIdx = state.stackIndexOverrides[element.elementId]
    LaunchedEffect(overrideIdx, displayChildren.size) {
        if (overrideIdx != null && displayChildren.isNotEmpty()) {
            val safe = overrideIdx.coerceIn(0, displayChildren.lastIndex)
            if (pagerState.currentPage != safe) {
                pagerState.animateScrollToPage(safe)
            }
        }
    }

    LaunchedEffect(extras.stackAutoRotateSec, displayChildren.size, state.isEditMode) {
        if (!state.isEditMode && extras.stackAutoRotateSec > 0 && displayChildren.size > 1) {
            while (true) {
                delay(extras.stackAutoRotateSec.coerceIn(3, 120) * 1000L)
                val next = (pagerState.currentPage + 1) % displayChildren.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().testTag("widget_stack_${element.elementId}")) {
        val pageContent: @Composable (Int) -> Unit = { idx ->
            val child = displayChildren[idx]
            Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
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
                    onOpenSearch = onOpenSearch,
                    onJumpToPage = onJumpToPage,
                    onSaveElement = onSaveElement,
                    onCycleStack = onCycleStack,
                )
            }
        }

        if (extras.stackSwipeAxis == "HORIZONTAL") {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !state.isEditMode && displayChildren.size > 1,
                modifier = Modifier.fillMaxSize(),
            ) { idx -> pageContent(idx) }
        } else {
            VerticalPager(
                state = pagerState,
                userScrollEnabled = !state.isEditMode && displayChildren.size > 1,
                modifier = Modifier.fillMaxSize(),
            ) { idx -> pageContent(idx) }
        }

        if (displayChildren.size > 1 && extras.stackIndicatorStyle != "HIDDEN") {
            val indicatorMod = if (extras.stackSwipeAxis == "HORIZONTAL") {
                Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
            } else {
                Modifier.align(Alignment.CenterEnd).padding(end = 6.dp)
            }
            when (extras.stackIndicatorStyle) {
                "NUMBERS" -> {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp),
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${displayChildren.size}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                }
                else -> {
                    val isVert = extras.stackSwipeAxis != "HORIZONTAL"
                    val isPill = extras.stackIndicatorStyle == "PILL"
                    if (isVert) {
                        Column(modifier = indicatorMod, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            displayChildren.indices.forEach { i ->
                                val active = i == pagerState.currentPage
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .height(if (active && isPill) 14.dp else if (active) 7.dp else 5.dp)
                                        .clip(CircleShape)
                                        .background(if (active) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f))
                                )
                            }
                        }
                    } else {
                        Row(modifier = indicatorMod, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            displayChildren.indices.forEach { i ->
                                val active = i == pagerState.currentPage
                                Box(
                                    modifier = Modifier
                                        .height(5.dp)
                                        .width(if (active && isPill) 14.dp else if (active) 7.dp else 5.dp)
                                        .clip(CircleShape)
                                        .background(if (active) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f))
                                )
                            }
                        }
                    }
                }
            }
        }

        if (displayChildren.size > 1 && !state.isEditMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.32f),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            scope.launch {
                                val next = (pagerState.currentPage + 1) % displayChildren.size
                                pagerState.animateScrollToPage(next)
                            }
                        }
                        .testTag("stack_next_btn_${element.elementId}"),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Next widget", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GraphicGaugeWidget(
    element: HomeElementEntity,
    extras: ElementStyleExtras,
) {
    val context = LocalContext.current
    val battery by remember(context) { DeviceTelemetryProvider.observeBattery(context) }
        .collectAsState(initial = BatteryTelemetry(100, false, 28f))
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            tick++
        }
    }
    val mem = remember(tick) { DeviceTelemetryProvider.readStorageAndRam(context) }

    val (fraction, metricLabel, metricValue) = remember(extras.gaugeMetric, battery, mem, tick) {
        when (extras.gaugeMetric) {
            "RAM" -> {
                val used = (mem.totalRamGb - mem.availableRamGb).coerceAtLeast(0f)
                val f = (used / mem.totalRamGb.coerceAtLeast(0.5f)).coerceIn(0f, 1f)
                Triple(f, "RAM", "${(f * 100).roundToInt()}%")
            }
            "STORAGE" -> {
                val f = (mem.usedStorageGb / mem.totalStorageGb.coerceAtLeast(1f)).coerceIn(0f, 1f)
                Triple(f, "STORAGE", "${(f * 100).roundToInt()}%")
            }
            "DAY_PROGRESS" -> {
                val cal = Calendar.getInstance()
                val mins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                val f = (mins / 1440f).coerceIn(0f, 1f)
                Triple(f, "DAY", "${(f * 100).roundToInt()}%")
            }
            else -> {
                val f = (battery.levelPercent / 100f).coerceIn(0f, 1f)
                Triple(f, if (battery.isCharging) "CHARGING" else "BATTERY", "${battery.levelPercent}%")
            }
        }
    }

    val gaugeColor = Color(element.textColorHex)
    val trackColor = gaugeColor.copy(alpha = 0.20f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .testTag("gauge_widget_${element.elementId}"),
        contentAlignment = Alignment.Center,
    ) {
        when (extras.gaugeStyle) {
            "LINEAR" -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ReadableLauncherText(element.label.ifBlank { metricLabel }, color = gaugeColor, style = MaterialTheme.typography.labelMedium, extras = extras)
                        ReadableLauncherText(metricValue, color = gaugeColor, style = MaterialTheme.typography.titleMedium, extras = extras)
                    }
                    LinearProgressIndicator(
                        progress = { fraction },
                        color = gaugeColor,
                        trackColor = trackColor,
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                    )
                }
            }
            "SEGMENTED" -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ReadableLauncherText(metricValue, color = gaugeColor, style = MaterialTheme.typography.headlineSmall, extras = extras)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val segments = 10
                        val activeSegs = (fraction * segments).roundToInt()
                        for (i in 0 until segments) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (i < activeSegs) gaugeColor else trackColor),
                            )
                        }
                    }
                    ReadableLauncherText(element.label.ifBlank { metricLabel }, color = gaugeColor.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall, extras = extras)
                }
            }
            "SPARKLINE" -> {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ReadableLauncherText(element.label.ifBlank { metricLabel }, color = gaugeColor.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall, extras = extras)
                        ReadableLauncherText(metricValue, color = gaugeColor, style = MaterialTheme.typography.titleSmall, extras = extras)
                    }
                    Canvas(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 6.dp)) {
                        val pts = listOf(0.32f, 0.48f, 0.40f, 0.68f, 0.58f, 0.78f, fraction.coerceAtLeast(0.08f))
                        val path = Path()
                        pts.forEachIndexed { idx, v ->
                            val px = size.width * (idx / (pts.size - 1).toFloat())
                            val py = size.height * (1f - v.coerceIn(0.05f, 0.95f))
                            if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        drawPath(path, color = gaugeColor, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                    }
                }
            }
            "RING" -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                        val stroke = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                        val d = minOf(size.width, size.height)
                        val tl = Offset((size.width - d) / 2f, (size.height - d) / 2f)
                        drawArc(trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = tl, size = Size(d, d), style = stroke)
                        drawArc(gaugeColor, startAngle = -90f, sweepAngle = 360f * fraction, useCenter = false, topLeft = tl, size = Size(d, d), style = stroke)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ReadableLauncherText(metricValue, color = gaugeColor, style = MaterialTheme.typography.titleLarge, extras = extras)
                        ReadableLauncherText(element.label.ifBlank { metricLabel }, color = gaugeColor.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall, extras = extras)
                    }
                }
            }
            else -> { // ARC
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        val d = minOf(size.width, size.height)
                        val tl = Offset((size.width - d) / 2f, (size.height - d) / 2f)
                        drawArc(trackColor, startAngle = 140f, sweepAngle = 260f, useCenter = false, topLeft = tl, size = Size(d, d), style = stroke)
                        drawArc(gaugeColor, startAngle = 140f, sweepAngle = 260f * fraction, useCenter = false, topLeft = tl, size = Size(d, d), style = stroke)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ReadableLauncherText(metricValue, color = gaugeColor, style = MaterialTheme.typography.titleLarge, extras = extras)
                        ReadableLauncherText(element.label.ifBlank { metricLabel }, color = gaugeColor.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall, extras = extras)
                    }
                }
            }
        }
    }
}

@Composable
fun ContainerBoxWidget(
    element: HomeElementEntity,
    extras: ElementStyleExtras,
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
    val children = remember(state.homeElements, element.elementId) {
        state.homeElements
            .filter { it.parentContainerId == element.elementId && it.visible }
            .sortedWith(compareBy<HomeElementEntity> { it.zIndex }.thenBy { it.elementId })
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .testTag("container_box_${element.elementId}"),
    ) {
        if (element.showLabel && element.label.isNotBlank()) {
            ReadableLauncherText(
                text = element.label,
                color = Color(element.textColorHex),
                style = MaterialTheme.typography.labelLarge,
                extras = extras,
            )
            Spacer(Modifier.height(6.dp))
        }
        if (children.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ReadableLauncherText(
                    text = "Scroll Container (add children in Inspector)",
                    color = Color(element.textColorHex).copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
            }
        } else if (extras.containerScrollAxis == "HORIZONTAL") {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                children.forEach { child ->
                    Box(modifier = Modifier.width(child.widthDp.coerceIn(56f, 260f).dp).fillMaxHeight()) {
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
                            onOpenSearch = onOpenSearch,
                            onJumpToPage = onJumpToPage,
                            onSaveElement = onSaveElement,
                            onCycleStack = onCycleStack,
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                children.forEach { child ->
                    Box(modifier = Modifier.fillMaxWidth().height(child.heightDp.coerceIn(48f, 240f).dp)) {
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
                            onOpenSearch = onOpenSearch,
                            onJumpToPage = onJumpToPage,
                            onSaveElement = onSaveElement,
                            onCycleStack = onCycleStack,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppGroupWidget(
    element: HomeElementEntity,
    extras: ElementStyleExtras,
    state: LauncherUiState,
    appDiscovery: AppDiscoveryManager,
    onLaunchApp: (AppEntity) -> Unit,
) {
    val apps = remember(state.appsWithTags, state.tagsWithApps, extras.appGroupSource, element.targetTagId, element.targetCategoryId) {
        val visible = state.appsWithTags.filter { it.app.isInstalled && !it.app.hidden }
        when (extras.appGroupSource) {
            "RECENT" -> visible.map { it.app }.sortedByDescending { it.lastLaunchedAt }.take(12)
            "MOST_USED" -> visible.map { it.app }.sortedByDescending { it.launchCount }.take(12)
            "TAG" -> state.tagsWithApps.find { it.tag.tagId == element.targetTagId }
                ?.apps?.filter { it.isInstalled && !it.hidden }.orEmpty().take(16)
            "CATEGORY" -> visible.filter { awt ->
                awt.app.primaryCategoryId == element.targetCategoryId ||
                    awt.categories.any { it.categoryId == element.targetCategoryId }
            }.map { it.app }.take(16)
            else -> visible.filter { it.app.favorite }.map { it.app }.ifEmpty {
                visible.map { it.app }.take(8)
            }.take(16)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .testTag("app_group_widget_${element.elementId}"),
    ) {
        if (element.showLabel) {
            ReadableLauncherText(
                text = element.label.ifBlank { extras.appGroupSource },
                color = Color(element.textColorHex),
                style = MaterialTheme.typography.labelLarge,
                extras = extras,
            )
            Spacer(Modifier.height(6.dp))
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(extras.appGroupColumns.coerceIn(2, 6)),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(2.dp),
        ) {
            items(apps, key = { it.packageName }) { app ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !state.isEditMode) { onLaunchApp(app) }
                        .padding(3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AppIconView(
                        app = app,
                        appDiscovery = appDiscovery,
                        size = extras.appGroupIconSizeDp.coerceIn(28, 64).dp,
                        iconShapeKey = state.settings.globalIconShape,
                    )
                    if (state.settings.showHomeAppLabels) {
                        Spacer(Modifier.height(3.dp))
                        ReadableLauncherText(
                            text = app.displayName,
                            color = Color(element.textColorHex),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PageSwitcherWidget(
    element: HomeElementEntity,
    extras: ElementStyleExtras,
    state: LauncherUiState,
    onJumpToPage: (Int) -> Unit,
) {
    val visiblePages = remember(state.homePages, state.isEditMode) {
        if (state.isEditMode) state.homePages else state.homePages.filter { !it.isHidden }
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("page_switcher_widget_${element.elementId}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        visiblePages.forEachIndexed { idx, page ->
            val active = idx == state.currentPageIndex
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.16f)),
                modifier = Modifier
                    .clickable(enabled = !state.isEditMode) { onJumpToPage(idx) }
                    .testTag("page_switcher_pill_${page.pageId}"),
            ) {
                ReadableLauncherText(
                    text = page.title,
                    color = if (active) MaterialTheme.colorScheme.primary else Color(element.textColorHex),
                    style = MaterialTheme.typography.labelMedium,
                    extras = extras,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
fun EmbeddedSearchBarWidget(
    element: HomeElementEntity,
    extras: ElementStyleExtras,
    isEditMode: Boolean,
    onOpenSearch: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = !isEditMode) { onOpenSearch() }
            .padding(horizontal = 14.dp)
            .testTag("search_bar_widget_${element.elementId}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Rounded.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
        ReadableLauncherText(
            text = element.label.ifBlank { "Search apps, tags & settings..." },
            color = Color(element.textColorHex).copy(alpha = 0.88f),
            style = MaterialTheme.typography.bodyMedium,
            extras = extras,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun QuickNotesWidget(
    element: HomeElementEntity,
    extras: ElementStyleExtras,
    isEditMode: Boolean,
    onSaveElement: (HomeElementEntity, Boolean) -> Unit,
) {
    var editing by remember { mutableStateOf(false) }
    var draft by remember(extras.quickNotesText) { mutableStateOf(extras.quickNotesText) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = !isEditMode) {
                draft = extras.quickNotesText
                editing = true
            }
            .padding(12.dp)
            .testTag("quick_notes_widget_${element.elementId}"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            ReadableLauncherText(
                text = element.label.ifBlank { "Quick Notes" },
                color = Color(element.textColorHex),
                style = MaterialTheme.typography.labelLarge,
                extras = extras,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(6.dp))
        ReadableLauncherText(
            text = extras.quickNotesText,
            color = Color(element.textColorHex).copy(alpha = 0.88f),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 6,
            overflow = TextOverflow.Ellipsis,
        )
    }

    if (editing) {
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text(element.label.ifBlank { "Quick Notes" }) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    minLines = 4,
                    maxLines = 8,
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth().testTag("quick_notes_input"),
                )
            },
            confirmButton = {
                Button(onClick = {
                    val nextExtras = extras.copy(quickNotesText = draft)
                    onSaveElement(element.copy(styleExtrasJson = nextExtras.toJson()), false)
                    editing = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editing = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
fun FocusTimerWidget(
    element: HomeElementEntity,
    extras: ElementStyleExtras,
    isEditMode: Boolean,
) {
    val totalSec = extras.focusMinutes.coerceIn(1, 120) * 60
    var remainingSec by remember(element.elementId, totalSec) { mutableIntStateOf(totalSec) }
    var running by remember(element.elementId) { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running && remainingSec > 0) {
            delay(1000L)
            remainingSec--
            if (remainingSec == 0) running = false
        }
    }

    val mm = remainingSec / 60
    val ss = remainingSec % 60
    val timeText = "%02d:%02d".format(mm, ss)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .testTag("focus_timer_widget_${element.elementId}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            ReadableLauncherText(element.label.ifBlank { "Focus" }, color = Color(element.textColorHex), style = MaterialTheme.typography.labelMedium, extras = extras)
        }
        ReadableLauncherText(
            text = timeText,
            color = Color(element.textColorHex),
            style = MaterialTheme.typography.headlineMedium,
            extras = extras,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = { if (!isEditMode) running = !running },
                modifier = Modifier.size(40.dp).testTag("focus_timer_toggle_${element.elementId}"),
            ) {
                Icon(
                    if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (running) "Pause timer" else "Start timer",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(
                onClick = {
                    if (!isEditMode) {
                        running = false
                        remainingSec = totalSec
                    }
                },
                modifier = Modifier.size(40.dp).testTag("focus_timer_reset_${element.elementId}"),
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Reset timer", tint = Color(element.textColorHex))
            }
        }
    }
}

@Composable
fun MiniCalculatorWidget(
    element: HomeElementEntity,
    isEditMode: Boolean,
) {
    var display by remember(element.elementId) { mutableStateOf("0") }
    var pendingOp by remember(element.elementId) { mutableStateOf<Char?>(null) }
    var accumulator by remember(element.elementId) { mutableStateOf<Double?>(null) }
    var resetOnNextDigit by remember(element.elementId) { mutableStateOf(false) }

    fun formatNum(v: Double): String {
        val asLong = v.toLong()
        return if (v == asLong.toDouble()) asLong.toString() else "%.4f".format(v).trimEnd('0').trimEnd('.')
    }

    fun applyOp(op: Char, a: Double, b: Double): Double = when (op) {
        '+' -> a + b
        '-' -> a - b
        '×' -> a * b
        '÷' -> if (b == 0.0) Double.NaN else a / b
        else -> b
    }

    fun pressKey(key: String) {
        if (isEditMode) return
        when (key) {
            "C" -> {
                display = "0"
                pendingOp = null
                accumulator = null
                resetOnNextDigit = false
            }
            "⌫" -> {
                display = if (display.length <= 1 || display == "Error") "0" else display.dropLast(1)
            }
            "+", "-", "×", "÷" -> {
                val cur = display.toDoubleOrNull() ?: 0.0
                val acc = accumulator
                val res = if (acc != null && pendingOp != null && !resetOnNextDigit) applyOp(pendingOp!!, acc, cur) else cur
                accumulator = res
                display = if (res.isNaN()) "Error" else formatNum(res)
                pendingOp = key.first()
                resetOnNextDigit = true
            }
            "=" -> {
                val cur = display.toDoubleOrNull() ?: 0.0
                val acc = accumulator
                if (acc != null && pendingOp != null) {
                    val res = applyOp(pendingOp!!, acc, cur)
                    display = if (res.isNaN()) "Error" else formatNum(res)
                    accumulator = null
                    pendingOp = null
                    resetOnNextDigit = true
                }
            }
            "." -> {
                if (resetOnNextDigit) {
                    display = "0."
                    resetOnNextDigit = false
                } else if (!display.contains(".")) {
                    display += "."
                }
            }
            else -> {
                display = if (resetOnNextDigit || display == "0" || display == "Error") {
                    resetOnNextDigit = false
                    key
                } else (display + key).take(12)
            }
        }
    }

    val rows = listOf(
        listOf("C", "⌫", "÷", "×"),
        listOf("7", "8", "9", "-"),
        listOf("4", "5", "6", "+"),
        listOf("1", "2", "3", "="),
        listOf("0", "."),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .testTag("mini_calculator_widget_${element.elementId}"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = display,
                color = Color(element.textColorHex),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f).testTag("calc_display_${element.elementId}"),
            )
        }
        rows.forEach { row ->
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.forEach { key ->
                    val isAccent = key in listOf("+", "-", "×", "÷", "=")
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAccent) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .weight(if (key == "0") 2f else 1f)
                            .fillMaxHeight()
                            .clickable(enabled = !isEditMode) { pressKey(key) }
                            .testTag("calc_key_${key}"),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = key,
                                color = if (isAccent) MaterialTheme.colorScheme.primary else Color(element.textColorHex),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceToggleWidget(
    element: HomeElementEntity,
    extras: ElementStyleExtras,
    isEditMode: Boolean,
) {
    val context = LocalContext.current
    var torchOn by remember { mutableStateOf(DeviceControlsManager.isTorchEnabled()) }
    var statusText by remember(extras.deviceToggleAction) {
        mutableStateOf(
            when (extras.deviceToggleAction) {
                "TORCH" -> if (DeviceControlsManager.isTorchEnabled()) "On" else "Off"
                "RINGER" -> DeviceControlsManager.getRingerLabel(context)
                "WIFI_PANEL" -> DeviceControlsManager.readNetworkSummary(context).second
                "BLUETOOTH_PANEL" -> "Bluetooth"
                "VOLUME_PANEL" -> "Volume"
                else -> "Display"
            }
        )
    }

    val icon = when (extras.deviceToggleAction) {
        "TORCH" -> if (torchOn) Icons.Rounded.FlashlightOn else Icons.Rounded.FlashlightOff
        "RINGER" -> Icons.Rounded.NotificationsActive
        "WIFI_PANEL" -> Icons.Rounded.Wifi
        "BLUETOOTH_PANEL" -> Icons.Rounded.Bluetooth
        "VOLUME_PANEL" -> Icons.Rounded.VolumeUp
        else -> Icons.Rounded.BrightnessMedium
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = !isEditMode) {
                when (extras.deviceToggleAction) {
                    "TORCH" -> {
                        val (_, msg) = DeviceControlsManager.toggleTorch(context)
                        torchOn = DeviceControlsManager.isTorchEnabled()
                        statusText = if (torchOn) "On" else msg.substringAfter(" ").take(8)
                    }
                    "RINGER" -> {
                        val msg = DeviceControlsManager.cycleRingerMode(context)
                        statusText = msg.removePrefix("Ringer: ").take(10)
                    }
                    else -> {
                        DeviceControlsManager.openSystemPanel(context, extras.deviceToggleAction)
                    }
                }
            }
            .padding(8.dp)
            .testTag("device_toggle_${element.elementId}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = element.label.ifBlank { extras.deviceToggleAction },
            tint = if (extras.deviceToggleAction == "TORCH" && torchOn) MaterialTheme.colorScheme.primary else Color(element.textColorHex),
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(4.dp))
        ReadableLauncherText(
            text = element.label.ifBlank { extras.deviceToggleAction },
            color = Color(element.textColorHex),
            style = MaterialTheme.typography.labelMedium,
            extras = extras,
            maxLines = 1,
        )
        ReadableLauncherText(
            text = statusText,
            color = Color(element.textColorHex).copy(alpha = 0.72f),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}
