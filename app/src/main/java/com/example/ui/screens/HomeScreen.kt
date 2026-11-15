package com.example.ui.screens

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.rounded.ViewSidebar
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AppEntity
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.LayoutSnapshotEntity
import com.example.data.prefs.LauncherSettings
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.InstalledWidgetProvider
import com.example.data.system.LauncherWidgetHost
import com.example.ui.components.LocalIconStyleConfig
import com.example.ui.components.OutlinedVectorIcon
import com.example.ui.components.StudioQuickCustomizerDialog
import com.example.ui.components.TagBadgeIcon
import com.example.ui.components.WallpaperBackground
import com.example.ui.components.WidgetPickerDialog
import com.example.ui.state.LauncherRoute
import com.example.ui.state.LauncherUiState
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun HomeScreen(
    state: LauncherUiState,
    appDiscovery: AppDiscoveryManager,
    widgetHost: LauncherWidgetHost,
    onImportImage: suspend (Uri, String, String?) -> String?,
    onLaunchApp: (AppEntity) -> Unit,
    onOpenTagSidebar: (Long) -> Unit,
    onOpenCategorySidebar: (Long) -> Unit,
    onOpenConfiguredSidebar: (Long) -> Unit,
    onOpenRuleSidebar: (Long) -> Unit,
    onOpenWindow: (Long) -> Unit,
    onOpenFolder: (Long) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onNavigate: (LauncherRoute) -> Unit,
    onToggleEditMode: (Boolean) -> Unit,
    onSelectElement: (Long?) -> Unit,
    onToggleMultiSelect: (Long) -> Unit,
    onDeleteMultiSelected: () -> Unit,
    onLockMultiSelected: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleGridSnap: () -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit = {},
    onSaveElement: (HomeElementEntity, Boolean) -> Unit,
    onDuplicateElement: (HomeElementEntity) -> Unit,
    onCopyElement: (HomeElementEntity) -> Unit,
    onCutElement: (HomeElementEntity) -> Unit,
    onPasteElement: (Long) -> Unit,
    onBringForward: (HomeElementEntity) -> Unit,
    onSendBackward: (HomeElementEntity) -> Unit,
    onDeleteElement: (HomeElementEntity) -> Unit,
    onExecuteElementAction: (HomeElementEntity, String, (Int) -> Unit) -> Boolean,
    onAddPage: (String) -> Unit,
    onCreateStarterTemplate: (String) -> Unit,
    onSavePage: (HomePageEntity) -> Unit,
    onDuplicatePage: (HomePageEntity) -> Unit,
    onReorderPages: (List<Long>) -> Unit,
    onDeletePage: (Long) -> Unit,
    onSaveSnapshot: (String) -> Unit,
    onRestoreSnapshot: (LayoutSnapshotEntity) -> Unit,
    onDeleteSnapshot: (Long) -> Unit,
    onPageChanged: (Int) -> Unit,
    onTriggerGesture: (String, (Int) -> Unit) -> Unit,
    onRequestSetDefaultHome: () -> Unit,
    onDismissDefaultBanner: () -> Unit,
    onStartSystemWidgetBind: (InstalledWidgetProvider, Long, String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val visiblePages = remember(state.homePages, state.isEditMode) {
        val list = if (state.isEditMode) state.homePages else state.homePages.filter { !it.isHidden }
        list.ifEmpty { listOf(HomePageEntity(pageId = 1L, title = "Main Deck", pageOrder = 0)) }
    }
    val feedEnabled = state.settings.widgetFeedEnabled
    val loopPagerEnabled = visiblePages.size > 1 && visiblePages.any { it.loopNavigation }
    val pagerPageOffset = if (loopPagerEnabled) 1 else 0
    val feedPagerIndex = visiblePages.size + pagerPageOffset
    val loopNextPagerIndex = visiblePages.size + pagerPageOffset + if (feedEnabled) 1 else 0
    val pagerPageCount = visiblePages.size + (if (loopPagerEnabled) 2 else 0) + (if (feedEnabled) 1 else 0)
    fun logicalPageIndex(pagerIndex: Int): Int = if (loopPagerEnabled) {
        when (pagerIndex) {
            0 -> visiblePages.lastIndex
            loopNextPagerIndex -> 0
            else -> (pagerIndex - 1).coerceIn(0, visiblePages.lastIndex)
        }
    } else pagerIndex.coerceIn(0, visiblePages.lastIndex)
    val pagerState = rememberPagerState(
        initialPage = state.currentPageIndex.coerceIn(0, visiblePages.lastIndex) + pagerPageOffset
    ) { pagerPageCount }

    val activeTransitionDurationMs = visiblePages[logicalPageIndex(pagerState.currentPage)].transitionDurationMs

    LaunchedEffect(pagerState.currentPage, visiblePages.size, loopPagerEnabled, feedEnabled, pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) return@LaunchedEffect
        if (loopPagerEnabled && pagerState.currentPage == 0) {
            pagerState.scrollToPage(if (visiblePages.first().loopNavigation) visiblePages.size else 1)
        } else if (loopPagerEnabled && pagerState.currentPage == loopNextPagerIndex) {
            pagerState.scrollToPage(if (visiblePages.last().loopNavigation) 1 else visiblePages.size)
        } else if (pagerState.currentPage < feedPagerIndex) {
            onPageChanged(logicalPageIndex(pagerState.currentPage))
        }
    }

    LaunchedEffect(state.currentPageIndex, visiblePages.size, feedEnabled, loopPagerEnabled) {
        val target = state.currentPageIndex.coerceIn(0, visiblePages.lastIndex) + pagerPageOffset
        if (!feedEnabled || pagerState.currentPage < feedPagerIndex) {
            if (pagerState.currentPage != target) {
                pagerState.animateScrollToPage(target, animationSpec = tween(durationMillis = activeTransitionDurationMs))
            }
        }
    }

    var showAddElementDialog by remember { mutableStateOf(false) }
    var showPagesDialog by remember { mutableStateOf(false) }
    var showLayerManager by remember { mutableStateOf(false) }
    var showWidgetPicker by remember { mutableStateOf(false) }
    var showFeedWidgetPicker by remember { mutableStateOf(false) }
    var showStyleDialog by remember { mutableStateOf(false) }
    var inspectingElement by remember { mutableStateOf<HomeElementEntity?>(null) }
    var styleClipboard by remember { mutableStateOf<HomeElementEntity?>(null) }
    var multiSelectActive by remember { mutableStateOf(false) }

    BackHandler(enabled = state.isEditMode) {
        multiSelectActive = false
        onToggleEditMode(false)
    }

    val currentPage = visiblePages[logicalPageIndex(pagerState.currentPage)]
    val deviceConfiguration = LocalConfiguration.current
    fun resolvePageTarget(target: Int): Int {
        if (currentPage.loopNavigation && visiblePages.isNotEmpty()) {
            if (target < 0) return visiblePages.lastIndex + pagerPageOffset
            if (target >= visiblePages.size) return pagerPageOffset
        }
        return target.coerceIn(0, visiblePages.lastIndex) + pagerPageOffset
    }
    fun executePageGesture(trigger: String): Boolean {
        val actions = currentPage.pageGesturesJson?.takeIf { it.isNotBlank() } ?: return false
        val pageAction = HomeElementEntity(pageId = currentPage.pageId, type = "PAGE_ACTION", actionsConfigJson = actions)
        return onExecuteElementAction(pageAction, trigger) { target ->
            scope.launch { pagerState.animateScrollToPage(resolvePageTarget(target), animationSpec = tween(durationMillis = activeTransitionDurationMs)) }
        }
    }
    val leftEdgeSidebar = state.sidebars.firstOrNull { it.side == "LEFT" && it.edgeZoneEnabled }
    val rightEdgeSidebar = state.sidebars.firstOrNull { it.side == "RIGHT" && it.edgeZoneEnabled }
    val topEdgeSidebar = state.sidebars.firstOrNull { it.side == "TOP" && it.edgeZoneEnabled }
    val bottomEdgeSidebar = state.sidebars.firstOrNull { it.side == "BOTTOM" && it.edgeZoneEnabled }

    Box(modifier = Modifier.fillMaxSize()) {
        WallpaperBackground(
            settings = state.settings,
            currentPage = currentPage,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.isEditMode, state.gestures) {
                    if (!state.isEditMode) {
                        var twoFingerStartX = 0f
                        var twoFingerStartY = 0f
                        var twoFingerStartDistance = 0f
                        var twoFingerStartAt = 0L
                        var trackingTwoFinger = false
                        var twoFingerMoved = false
                        var twoFingerGestureFired = false
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val pressed = event.changes.filter { it.pressed }
                                if (pressed.size == 2) {
                                    val avgX = (pressed[0].position.x + pressed[1].position.x) / 2f
                                    val avgY = (pressed[0].position.y + pressed[1].position.y) / 2f
                                    val dx = pressed[0].position.x - pressed[1].position.x
                                    val dyBetween = pressed[0].position.y - pressed[1].position.y
                                    val distance = sqrt(dx * dx + dyBetween * dyBetween)
                                    if (!trackingTwoFinger) {
                                        trackingTwoFinger = true
                                        twoFingerStartX = avgX
                                        twoFingerStartY = avgY
                                        twoFingerStartDistance = distance
                                        twoFingerStartAt = System.currentTimeMillis()
                                        twoFingerMoved = false
                                        twoFingerGestureFired = false
                                    } else if (!twoFingerGestureFired) {
                                        val moveX = avgX - twoFingerStartX
                                        val moveY = avgY - twoFingerStartY
                                        val distanceDelta = distance - twoFingerStartDistance
                                        if (kotlin.math.abs(moveX) > 28f || kotlin.math.abs(moveY) > 28f || kotlin.math.abs(distanceDelta) > 24f) twoFingerMoved = true
                                        when {
                                            distanceDelta > 90f -> {
                                                twoFingerGestureFired = true
                                                onTriggerGesture("PINCH_OUT") { target ->
                                                    scope.launch { pagerState.animateScrollToPage(resolvePageTarget(target), animationSpec = tween(durationMillis = activeTransitionDurationMs)) }
                                                }
                                            }
                                            distanceDelta < -90f -> {
                                                twoFingerGestureFired = true
                                                onTriggerGesture("PINCH_IN") { target ->
                                                    scope.launch { pagerState.animateScrollToPage(resolvePageTarget(target), animationSpec = tween(durationMillis = activeTransitionDurationMs)) }
                                                }
                                            }
                                            moveY < -110f -> {
                                                twoFingerGestureFired = true
                                                onTriggerGesture("TWO_FINGER_SWIPE_UP") { target ->
                                                    scope.launch { pagerState.animateScrollToPage(resolvePageTarget(target), animationSpec = tween(durationMillis = activeTransitionDurationMs)) }
                                                }
                                            }
                                            moveY > 110f -> {
                                                twoFingerGestureFired = true
                                                onTriggerGesture("TWO_FINGER_SWIPE_DOWN") { target ->
                                                    scope.launch { pagerState.animateScrollToPage(resolvePageTarget(target), animationSpec = tween(durationMillis = activeTransitionDurationMs)) }
                                                }
                                            }
                                        }
                                    }
                                } else if (pressed.isEmpty()) {
                                    if (trackingTwoFinger && !twoFingerGestureFired && !twoFingerMoved && System.currentTimeMillis() - twoFingerStartAt < 320L) {
                                        onTriggerGesture("TWO_FINGER_TAP") { target ->
                                            scope.launch { pagerState.animateScrollToPage(resolvePageTarget(target), animationSpec = tween(durationMillis = activeTransitionDurationMs)) }
                                        }
                                    }
                                    trackingTwoFinger = false
                                    twoFingerGestureFired = false
                                    twoFingerMoved = false
                                }
                            }
                        }
                    }
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                if (state.isEditMode) {
                    StudioEditToolbar(
                        pageTitle = currentPage.title,
                        pageLocked = currentPage.isLocked,
                        layoutLocked = state.settings.layoutLocked,
                        canUndo = state.canUndo,
                        canRedo = state.canRedo,
                        hasClipboard = state.clipboardElement != null,
                        gridSnap = state.settings.gridSnapEnabled,
                        multiSelectCount = state.multiSelectedIds.size,
                        multiSelectMode = multiSelectActive,
                        surfaceOpacity = state.settings.surfaceOpacity,
                        cornerRadiusDp = state.settings.globalCornerRadiusDp,
                        borderWidthDp = state.settings.globalBorderWidthDp,
                        onUndo = onUndo,
                        onRedo = onRedo,
                        onToggleGridSnap = onToggleGridSnap,
                        onToggleMultiSelectMode = { multiSelectActive = !multiSelectActive },
                        onDeleteMulti = onDeleteMultiSelected,
                        onLockMulti = onLockMultiSelected,
                        onGroupMulti = {
                            val groupId = "group_" + System.currentTimeMillis()
                            state.homeElements.filter { it.elementId in state.multiSelectedIds }.forEach { onSaveElement(it.copy(groupId = groupId), true) }
                        },
                        onUngroupMulti = {
                            val groupIds = state.homeElements.filter { it.elementId in state.multiSelectedIds }.mapNotNull { it.groupId }.toSet()
                            state.homeElements.filter { it.groupId in groupIds }.forEach { onSaveElement(it.copy(groupId = null), true) }
                        },
                        onAlignMulti = { mode ->
                            val selected = state.homeElements.filter { it.elementId in state.multiSelectedIds }
                            if (selected.size > 1) {
                                val screenWidth = deviceConfiguration.screenWidthDp.coerceAtLeast(1).toFloat()
                                val screenHeight = deviceConfiguration.screenHeightDp.coerceAtLeast(1).toFloat()
                                val horizontal = mode == "LEFT" || mode == "RIGHT" || mode == "CENTER_X"
                                val vertical = mode == "TOP" || mode == "BOTTOM" || mode == "CENTER_Y"
                                val targetX = when (mode) {
                                    "LEFT" -> selected.minOf { it.xFraction }
                                    "RIGHT" -> selected.maxOf { it.xFraction + it.widthDp / screenWidth }
                                    else -> selected.map { it.xFraction + it.widthDp / screenWidth / 2f }.average().toFloat()
                                }
                                val targetY = when (mode) {
                                    "TOP" -> selected.minOf { it.yFraction }
                                    "BOTTOM" -> selected.maxOf { it.yFraction + it.heightDp / screenHeight }
                                    else -> selected.map { it.yFraction + it.heightDp / screenHeight / 2f }.average().toFloat()
                                }
                                selected.forEach { element ->
                                    val x = if (!horizontal) element.xFraction else when (mode) {
                                        "LEFT" -> targetX
                                        "RIGHT" -> targetX - element.widthDp / screenWidth
                                        else -> targetX - element.widthDp / screenWidth / 2f
                                    }.coerceIn(0f, 0.92f)
                                    val y = if (!vertical) element.yFraction else when (mode) {
                                        "TOP" -> targetY
                                        "BOTTOM" -> targetY - element.heightDp / screenHeight
                                        else -> targetY - element.heightDp / screenHeight / 2f
                                    }.coerceIn(0f, 0.92f)
                                    onSaveElement(element.copy(xFraction = x, yFraction = y), true)
                                }
                            }
                        },
                        onPaste = { onPasteElement(currentPage.pageId) },
                        onAddElement = { showAddElementDialog = true },
                        onOpenWidgets = { showWidgetPicker = true },
                        onOpenPages = { showPagesDialog = true },
                        onOpenLayers = { showLayerManager = true },
                        onOpenStyleStudio = { showStyleDialog = true },
                        onOpenSettings = { onNavigate(LauncherRoute.Settings) },
                        onUnlockPage = { onSavePage(currentPage.copy(isLocked = false)) },
                        onDone = {
                            multiSelectActive = false
                            onToggleEditMode(false)
                        },
                    )
                } else if (!state.isDefaultLauncher && state.settings.showDefaultHomeBanner) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(state.settings.globalCornerRadiusDp.coerceIn(8f, 28f).dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = state.settings.surfaceOpacity.coerceIn(0.35f, 0.95f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Set TagDeck as your default Home launcher",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            AssistChip(onClick = onRequestSetDefaultHome, label = { Text("Enable") })
                            IconButton(onClick = onDismissDefaultBanner, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    HorizontalPager(
                        state = pagerState,
                        key = { idx ->
                            when {
                                feedEnabled && idx == feedPagerIndex -> "WIDGET_FEED"
                                loopPagerEnabled && idx == 0 -> "LOOP_PREV_${visiblePages.last().pageId}"
                                loopPagerEnabled && idx == loopNextPagerIndex -> "LOOP_NEXT_${visiblePages.first().pageId}"
                                else -> "PAGE_${visiblePages[logicalPageIndex(idx)].pageId}"
                            }
                        },
                        userScrollEnabled = !state.isEditMode,
                        modifier = Modifier.fillMaxSize(),
                    ) { pageIdx ->
                                                    if (feedEnabled && pageIdx == feedPagerIndex) {
                                val feedElements = state.homeElements
                                    .filter { it.ownerType == "FEED" && it.visible }
                                    .sortedBy { it.zIndex }

                                val feedScrollState = rememberScrollState()
            Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(feedScrollState) {
                        awaitEachGesture {
                            val down = awaitFirstDown(
                                requireUnconsumed = false,
                                pass = PointerEventPass.Initial,
                            )
                            var last = down.position
                            var total = Offset.Zero
                            var verticalDrag = false

                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break

                                val delta = change.position - last
                                total += delta

                                if (!verticalDrag && total.getDistance() > viewConfiguration.touchSlop) {
                                    if (kotlin.math.abs(total.y) > kotlin.math.abs(total.x)) {
                                        verticalDrag = true
                                    } else {
                                        break
                                    }
                                }

                                if (verticalDrag) {
                                    change.consume()
                                    feedScrollState.dispatchRawDelta(-delta.y)
                                }
                                last = change.position
                            }
                        }
                    }
                    .verticalScroll(feedScrollState)
                                        .padding(16.dp),
                                ) {
                                    Text(
                                        text = "Widget Feed",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = if (state.settings.widgetFeedTitleColor == "BLACK") Color.Black else Color.White,
                                    )
                                    Button(
                                        onClick = { showFeedWidgetPicker = true },
                                        modifier = Modifier.padding(vertical = 12.dp),
                                    ) {
                                        Text("Add widget")
                                    }

                                    feedElements.forEach { element ->
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(element.widthDp.coerceIn(120f, 600f).dp)
                                                    .height(element.heightDp.coerceIn(80f, 600f).dp),
                                            ) {
                                                HomeElementBody(
                                                    element = element,
                                                    state = state.copy(isEditMode = false),
                                                    appDiscovery = appDiscovery,
                                                    widgetHost = widgetHost,
                                                    onLaunchApp = onLaunchApp,
                                                    onOpenTagSidebar = onOpenTagSidebar,
                                                    onOpenCategorySidebar = onOpenCategorySidebar,
                                                    onOpenRuleSidebar = onOpenRuleSidebar,
                                                    onOpenSidebar = onOpenConfiguredSidebar,
                                                    onOpenWindow = onOpenWindow,
                                                    onOpenFolder = onOpenFolder,
                                                    onElementTrigger = { elem, trigger ->
                                                        onExecuteElementAction(elem, trigger) { target ->
                                                            scope.launch { pagerState.animateScrollToPage(resolvePageTarget(target), animationSpec = tween(durationMillis = activeTransitionDurationMs)) }
                                                        }
                                                    },
                                                    onUpdateElement = { onSaveElement(it, true) },
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
val logicalPageIdx = logicalPageIndex(pageIdx)
                        val page = visiblePages[logicalPageIdx]
                        val pageOffset = ((pagerState.currentPage - pageIdx) + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
                        val transitionStyle = if (page.parallaxEnabled) "PARALLAX" else (page.transitionOverride ?: state.settings.pageTransitionStyle)
                        val pageTransitionMod = when (transitionStyle) {
                            "FADE" -> Modifier.graphicsLayer {
                                alpha = 1f - abs(pageOffset) * 0.72f
                            }
                            "DEPTH" -> Modifier.graphicsLayer {
                                val absOff = abs(pageOffset)
                                alpha = 1f - absOff * 0.45f
                                val sc = 1f - absOff * 0.10f
                                scaleX = sc
                                scaleY = sc
                            }
                            "ZOOM" -> Modifier.graphicsLayer {
                                val absOff = abs(pageOffset)
                                alpha = 1f - absOff * 0.35f
                                scaleX = 1f - absOff * 0.22f
                                scaleY = 1f - absOff * 0.22f
                            }
                            "CUBE" -> Modifier.graphicsLayer {
                                cameraDistance = 12f * density
                                rotationY = -pageOffset * 90f
                                alpha = 1f - abs(pageOffset) * 0.25f
                            }
                            "FLIP" -> Modifier.graphicsLayer {
                                cameraDistance = 12f * density
                                rotationX = pageOffset * 80f
                                alpha = 1f - abs(pageOffset) * 0.35f
                            }
                            "ROTATE" -> Modifier.graphicsLayer {
                                rotationZ = pageOffset * 14f
                                val sc = 1f - abs(pageOffset) * 0.12f
                                scaleX = sc
                                scaleY = sc
                            }
                            "PARALLAX" -> Modifier.graphicsLayer {
                                translationX = -pageOffset * size.width * 0.22f
                                alpha = 1f - abs(pageOffset) * 0.18f
                            }
                            "STACK" -> Modifier.graphicsLayer {
                                val depth = abs(pageOffset)
                                translationX = pageOffset * size.width * 0.16f
                                scaleX = 1f - depth * 0.12f
                                scaleY = 1f - depth * 0.12f
                                alpha = 1f - depth * 0.22f
                            }
                            "ACCORDION" -> Modifier.graphicsLayer {
                                cameraDistance = 10f * density
                                rotationY = -pageOffset * 72f
                                scaleX = 1f - abs(pageOffset) * 0.18f
                                alpha = 1f - abs(pageOffset) * 0.25f
                            }
                            "WINDMILL" -> Modifier.graphicsLayer {
                                rotationZ = pageOffset * 90f
                                val scale = 1f - abs(pageOffset) * 0.28f
                                scaleX = scale
                                scaleY = scale
                                alpha = 1f - abs(pageOffset) * 0.3f
                            }
                            else -> Modifier
                        }
                        val elements = remember(state.homeElements, page.pageId) {
                            state.homeElements
                                .filter { it.ownerType == "PAGE" && it.pageId == page.pageId && it.visible }
                                .filter { element ->
                                    element.type != "TAG_SHORTCUT" ||
                                        state.allTags.find { it.tagId == element.targetTagId }?.showOnHome != false
                                }
                                .sortedBy { it.zIndex }
                        }
                        Box(modifier = Modifier.fillMaxSize().then(pageTransitionMod)) {
                            FreeformPageCanvas(
                            page = page,
                            elements = elements,
                            state = state,
                            multiSelectActive = multiSelectActive,
                            appDiscovery = appDiscovery,
                            widgetHost = widgetHost,
                            onEnterEditMode = { onToggleEditMode(true) },
                            onCanvasTap = {
                                if (state.isEditMode) {
                                    onSelectElement(null)
                                } else {
                                    if (!executePageGesture("TAP")) {
                                        onTriggerGesture("SINGLE_TAP") { target ->
                                            scope.launch { pagerState.animateScrollToPage(resolvePageTarget(target), animationSpec = tween(durationMillis = activeTransitionDurationMs)) }
                                        }
                                    }
                                }
                            },
                            onCanvasDoubleTap = {
                                if (!state.isEditMode) {
                                    if (!executePageGesture("DOUBLE_TAP")) {
                                        onTriggerGesture("DOUBLE_TAP") { target ->
                                            scope.launch { pagerState.animateScrollToPage(resolvePageTarget(target), animationSpec = tween(durationMillis = activeTransitionDurationMs)) }
                                        }
                                    }
                                }
                            },
                            onCanvasLongPress = {
                                if (!state.isEditMode && !executePageGesture("LONG_PRESS")) {
                                    onToggleEditMode(true)
                                }
                            },
                            onVerticalSwipe = { isUp ->
                                if (!state.isEditMode) {
                                    val trigger = if (isUp) "SWIPE_UP" else "SWIPE_DOWN"
                                    if (!executePageGesture(trigger)) {
                                        onTriggerGesture(trigger) { target ->
                                            scope.launch { pagerState.animateScrollToPage(resolvePageTarget(target), animationSpec = tween(durationMillis = activeTransitionDurationMs)) }
                                        }
                                    }
                                }
                            },
                            onLaunchApp = onLaunchApp,
                            onOpenTagSidebar = onOpenTagSidebar,
                            onOpenCategorySidebar = onOpenCategorySidebar,
                            onOpenRuleSidebar = onOpenRuleSidebar,
                            onOpenSidebar = onOpenConfiguredSidebar,
                            onOpenWindow = onOpenWindow,
                            onOpenFolder = onOpenFolder,
                            onElementTrigger = { elem, trigger ->
                                onExecuteElementAction(elem, trigger) { target ->
                                    scope.launch { pagerState.animateScrollToPage(resolvePageTarget(target), animationSpec = tween(durationMillis = activeTransitionDurationMs)) }
                                }
                            },
                            onSelectElement = { id ->
                                if (multiSelectActive && id != null) onToggleMultiSelect(id) else onSelectElement(id)
                            },
                            onInspectElement = { inspectingElement = it },
                            onSaveElement = onSaveElement,
                        )
                        }
                                            }
}

                    // 4-Edge Activation Zones (Left / Right / Top / Bottom)
                    if (!state.isEditMode && !(feedEnabled && pagerState.currentPage == feedPagerIndex) && leftEdgeSidebar != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .width(20.dp)
                                .fillMaxHeight(0.65f)
                                .pointerInput(leftEdgeSidebar.sidebarId) {
                                    detectHorizontalDragGestures { _, dragAmount ->
                                        if (dragAmount > 14f) onOpenConfiguredSidebar(leftEdgeSidebar.sidebarId)
                                    }
                                }
                                .clickable { onOpenConfiguredSidebar(leftEdgeSidebar.sidebarId) },
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (state.settings.edgeHandlesVisible && leftEdgeSidebar.showHandle) {
                                Box(
                                    modifier = Modifier
                                        .width(state.settings.edgeHandleWidthDp.coerceIn(2f, 12f).dp)
                                        .height(state.settings.edgeHandleHeightDp.coerceIn(24f, 140f).dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(
                                                alpha = state.settings.edgeHandleOpacity.coerceIn(0.05f, 1f),
                                            )
                                        )
                                )
                            }
                        }
                    }

                    if (!state.isEditMode && !(feedEnabled && pagerState.currentPage == feedPagerIndex) && rightEdgeSidebar != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(32.dp)
                                .fillMaxHeight(0.82f)
                                .pointerInput(rightEdgeSidebar.sidebarId) {
                                    var totalDrag = 0f
                                    var opened = false
                                    detectHorizontalDragGestures(
                                        onDragStart = { totalDrag = 0f; opened = false },
                                        onHorizontalDrag = { change, dragAmount ->
                                            totalDrag += dragAmount
                                            if (!opened && totalDrag < -56f) {
                                                opened = true
                                                change.consume()
                                                onOpenConfiguredSidebar(rightEdgeSidebar.sidebarId)
                                            }
                                        },
                                    )
                                }
                                .clickable { onOpenConfiguredSidebar(rightEdgeSidebar.sidebarId) },
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            if (state.settings.edgeHandlesVisible && rightEdgeSidebar.showHandle) {
                                Box(
                                    modifier = Modifier
                                        .width(state.settings.edgeHandleWidthDp.coerceIn(2f, 12f).dp)
                                        .height(state.settings.edgeHandleHeightDp.coerceIn(24f, 140f).dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            MaterialTheme.colorScheme.secondary.copy(
                                                alpha = state.settings.edgeHandleOpacity.coerceIn(0.05f, 1f),
                                            )
                                        )
                                )
                            }
                        }
                    }

                    if (!state.isEditMode && topEdgeSidebar != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .height(18.dp)
                                .fillMaxWidth(0.45f)
                                .pointerInput(topEdgeSidebar.sidebarId) {
                                    detectVerticalDragGestures { _, dragAmount ->
                                        if (dragAmount > 14f) onOpenConfiguredSidebar(topEdgeSidebar.sidebarId)
                                    }
                                }
                                .clickable { onOpenConfiguredSidebar(topEdgeSidebar.sidebarId) },
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            if (state.settings.edgeHandlesVisible && topEdgeSidebar.showHandle) {
                                Box(
                                    modifier = Modifier
                                        .height(state.settings.edgeHandleWidthDp.coerceIn(2f, 12f).dp)
                                        .width(state.settings.edgeHandleHeightDp.coerceIn(24f, 140f).dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(
                                                alpha = state.settings.edgeHandleOpacity.coerceIn(0.05f, 1f),
                                            )
                                        )
                                )
                            }
                        }
                    }

                    if (!state.isEditMode && bottomEdgeSidebar != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .height(18.dp)
                                .fillMaxWidth(0.45f)
                                .pointerInput(bottomEdgeSidebar.sidebarId) {
                                    detectVerticalDragGestures { _, dragAmount ->
                                        if (dragAmount < -14f) onOpenConfiguredSidebar(bottomEdgeSidebar.sidebarId)
                                    }
                                }
                                .clickable { onOpenConfiguredSidebar(bottomEdgeSidebar.sidebarId) },
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            if (state.settings.edgeHandlesVisible && bottomEdgeSidebar.showHandle) {
                                Box(
                                    modifier = Modifier
                                        .height(state.settings.edgeHandleWidthDp.coerceIn(2f, 12f).dp)
                                        .width(state.settings.edgeHandleHeightDp.coerceIn(24f, 140f).dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            MaterialTheme.colorScheme.secondary.copy(
                                                alpha = state.settings.edgeHandleOpacity.coerceIn(0.05f, 1f),
                                            )
                                        )
                                )
                            }
                        }
                    }
                }

                if (visiblePages.size > 1 && state.settings.pageIndicatorStyle != "HIDDEN") {
                    val indicatorColor = Color(state.settings.pageIndicatorColorHex)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("home_page_indicator"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        when (state.settings.pageIndicatorStyle) {
                            "PILL" -> {
                                visiblePages.forEachIndexed { idx, _ ->
                                    val active = idx == logicalPageIndex(pagerState.currentPage)
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .width(if (active) 22.dp else 7.dp)
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(if (active) indicatorColor else Color.White.copy(alpha = 0.35f))
                                    )
                                }
                            }
                            "NUMBERS" -> {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.Black.copy(alpha = 0.45f),
                                    border = BorderStroke(1.dp, indicatorColor.copy(alpha = 0.6f)),
                                ) {
                                    Text(
                                        text = "${logicalPageIndex(pagerState.currentPage) + 1} / ${visiblePages.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = indicatorColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            else -> {
                                visiblePages.forEachIndexed { idx, _ ->
                                    val active = idx == logicalPageIndex(pagerState.currentPage)
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(if (active) 10.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(if (active) indicatorColor else Color.White.copy(alpha = 0.4f))
                                    )
                                }
                            }
                        }
                    }
                }

                if (!state.isEditMode && state.settings.showDockBar) {
                    val dockRadius = state.settings.globalCornerRadiusDp.coerceIn(12f, 32f).dp
                    val dockAlpha = state.settings.surfaceOpacity.coerceIn(0.42f, 0.96f)
                    val dockBorderWidth = state.settings.globalBorderWidthDp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("home_dock_bar"),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            onClick = onOpenSearch,
                            shape = RoundedCornerShape(dockRadius),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = dockAlpha),
                            tonalElevation = 6.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("home_global_search_bar"),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    Icons.Rounded.Search,
                                    contentDescription = "Global search",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = "Global search: apps, settings, tags...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        Surface(
                            onClick = onOpenDrawer,
                            shape = RoundedCornerShape(dockRadius),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = dockAlpha),
                            tonalElevation = 6.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("home_open_drawer_button"),
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    Icons.Rounded.Apps,
                                    contentDescription = "All Apps",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddElementDialog) {
        AddHomeElementDialog(
            pageId = currentPage.pageId,
            state = state,
            appDiscovery = appDiscovery,
            onImportImage = { uri -> onImportImage(uri, "Objects", null) },
            onAddElement = { onSaveElement(it, true) },
            onOpenWidgetPicker = { showWidgetPicker = true },
            onDismiss = { showAddElementDialog = false },
        )
    }

    if (showFeedWidgetPicker) {
        val providers = remember { widgetHost.getInstalledProviders() }
        WidgetPickerDialog(
            providers = providers,
            tags = state.allTags,
            onPickSystemWidget = { provider ->
                showFeedWidgetPicker = false
                onStartSystemWidgetBind(provider, 0L, "FEED")
            },
            onPickBuiltinType = { type, label, wDp, hDp ->
                onSaveElement(
                    HomeElementEntity(
                        pageId = 0L,
                        ownerType = "FEED",
                        type = type,
                        xFraction = 0.08f,
                        yFraction = 0.08f,
                        widthDp = wDp,
                        heightDp = hDp,
                        label = label,
                    ),
                    false,
                )
                showFeedWidgetPicker = false
            },
            onPickBuiltinTagWidget = { tag ->
                onSaveElement(
                    HomeElementEntity(
                        pageId = 0L,
                        ownerType = "FEED",
                        type = "TAG_COLLECTION_WIDGET",
                        xFraction = 0.06f,
                        yFraction = 0.08f,
                        widthDp = 310f,
                        heightDp = 240f,
                        label = tag.name,
                        targetTagId = tag.tagId,
                        backgroundColorHex = 0x660F172AL,
                        borderColorHex = (0x88000000L or (tag.colorHex and 0x00FFFFFFL)),
                    ),
                    false,
                )
                showFeedWidgetPicker = false
            },
            onDismiss = { showFeedWidgetPicker = false },
        )
    }

    if (showFeedWidgetPicker) {
        val providers = remember { widgetHost.getInstalledProviders() }
        WidgetPickerDialog(
            providers = providers,
            tags = state.allTags,
            onPickSystemWidget = { provider ->
                showFeedWidgetPicker = false
                onStartSystemWidgetBind(provider, 0L, "FEED")
            },
            onPickBuiltinType = { type, label, wDp, hDp ->
                onSaveElement(
                    HomeElementEntity(
                        pageId = 0L,
                        ownerType = "FEED",
                        type = type,
                        xFraction = 0.08f,
                        yFraction = 0.08f,
                        widthDp = wDp,
                        heightDp = hDp,
                        label = label,
                    ),
                    false,
                )
                showFeedWidgetPicker = false
            },
            onPickBuiltinTagWidget = { tag ->
                onSaveElement(
                    HomeElementEntity(
                        pageId = 0L,
                        ownerType = "FEED",
                        type = "TAG_COLLECTION_WIDGET",
                        xFraction = 0.06f,
                        yFraction = 0.08f,
                        widthDp = 310f,
                        heightDp = 240f,
                        label = tag.name,
                        targetTagId = tag.tagId,
                        backgroundColorHex = 0x660F172AL,
                        borderColorHex = (0x88000000L or (tag.colorHex and 0x00FFFFFFL)),
                    ),
                    false,
                )
                showFeedWidgetPicker = false
            },
            onDismiss = { showFeedWidgetPicker = false },
        )
    }

    if (showWidgetPicker) {
        val providers = remember { widgetHost.getInstalledProviders() }
        WidgetPickerDialog(
            providers = providers,
            tags = state.allTags,
            onPickSystemWidget = { provider ->
                showWidgetPicker = false
                onStartSystemWidgetBind(provider, currentPage.pageId, "PAGE")
            },
            onPickBuiltinType = { type, label, wDp, hDp ->
                onSaveElement(
                    HomeElementEntity(
                        pageId = currentPage.pageId,
                        type = type,
                        xFraction = 0.08f,
                        yFraction = 0.08f,
                        widthDp = wDp,
                        heightDp = hDp,
                        label = label,
                    ),
                    true,
                )
                showWidgetPicker = false
            },
            onPickBuiltinTagWidget = { tag ->
                onSaveElement(
                    HomeElementEntity(
                        pageId = currentPage.pageId,
                        type = "TAG_COLLECTION_WIDGET",
                        xFraction = 0.06f,
                        yFraction = 0.14f,
                        widthDp = 310f,
                        heightDp = 240f,
                        label = tag.name,
                        targetTagId = tag.tagId,
                        backgroundColorHex = 0x660F172AL,
                        borderColorHex = (0x88000000L or (tag.colorHex and 0x00FFFFFFL)),
                    ),
                    true,
                )
                showWidgetPicker = false
            },
            onDismiss = { showWidgetPicker = false },
        )
    }

    if (showLayerManager) {
        LayerManagerDialog(
            elements = state.homeElements.filter { it.ownerType == "PAGE" && it.pageId == currentPage.pageId },
            onSelect = { id ->
                onSelectElement(id)
                inspectingElement = state.homeElements.firstOrNull { it.elementId == id }
                showLayerManager = false
            },
            onUpdate = { onSaveElement(it, true) },
            onDismiss = { showLayerManager = false },
        )
    }

    if (showPagesDialog) {
        PagesAndSnapshotsDialog(
            pages = state.homePages,
            snapshots = state.snapshots,
            currentPageId = currentPage.pageId,
            onJumpToPage = { pageId ->
                val idx = visiblePages.indexOfFirst { it.pageId == pageId }
                if (idx >= 0) {
                    onPageChanged(idx)
                    scope.launch { pagerState.animateScrollToPage(idx + pagerPageOffset, animationSpec = tween(durationMillis = activeTransitionDurationMs)) }
                }
            },
            onImportImage = onImportImage,
            onAddPage = onAddPage,
            onCreateStarterTemplate = onCreateStarterTemplate,
            onSavePage = onSavePage,
            onDuplicatePage = onDuplicatePage,
            onReorderPages = onReorderPages,
            onDeletePage = onDeletePage,
            onSaveSnapshot = onSaveSnapshot,
            onRestoreSnapshot = onRestoreSnapshot,
            onDeleteSnapshot = onDeleteSnapshot,
            onDismiss = { showPagesDialog = false },
        )
    }

    inspectingElement?.let { snap ->
        val elem = state.homeElements.find { it.elementId == snap.elementId } ?: snap
        ElementInspectorDialog(
            element = elem,
            state = state,
            onImportImage = onImportImage,
            onUpdate = { onSaveElement(it, true) },
            onDuplicate = {
                onDuplicateElement(elem)
                inspectingElement = null
            },
            onCopy = {
                onCopyElement(elem)
                inspectingElement = null
            },
            onCut = {
                onCutElement(elem)
                inspectingElement = null
            },
            onBringForward = { onBringForward(elem) },
            onSendBackward = { onSendBackward(elem) },
            onDelete = {
                onDeleteElement(elem)
                inspectingElement = null
            },
            onDismiss = { inspectingElement = null },
            styleToPaste = styleClipboard,
            onCopyStyle = { styleClipboard = it },
        )
    }

    if (showStyleDialog) {
        StudioQuickCustomizerDialog(
            currentPage = currentPage,
            settings = state.settings,
            onSavePage = onSavePage,
            onUpdateSettings = onUpdateSettings,
            onImportImage = onImportImage,
            onDismiss = { showStyleDialog = false },
        )
    }
}

@Composable
private fun StudioEditToolbar(
    pageTitle: String,
    pageLocked: Boolean,
    layoutLocked: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    hasClipboard: Boolean,
    gridSnap: Boolean,
    multiSelectCount: Int,
    multiSelectMode: Boolean,
    surfaceOpacity: Float = 0.94f,
    cornerRadiusDp: Float = 20f,
    borderWidthDp: Float = 1f,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleGridSnap: () -> Unit,
    onToggleMultiSelectMode: () -> Unit,
    onDeleteMulti: () -> Unit,
    onLockMulti: () -> Unit,
    onGroupMulti: () -> Unit = {},
    onUngroupMulti: () -> Unit = {},
    onAlignMulti: (String) -> Unit = {},
    onPaste: () -> Unit,
    onAddElement: () -> Unit,
    onOpenWidgets: () -> Unit,
    onOpenPages: () -> Unit,
    onOpenLayers: () -> Unit = {},
    onOpenStyleStudio: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onUnlockPage: () -> Unit,
    onDone: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("studio_edit_toolbar"),
        shape = RoundedCornerShape(cornerRadiusDp.coerceIn(4f, 32f).dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = surfaceOpacity.coerceIn(0.35f, 0.98f)),
        border = if (borderWidthDp > 0.05f) {
            BorderStroke(borderWidthDp.coerceIn(0.5f, 4f).dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
        } else null,
        tonalElevation = 10.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Edit • $pageTitle",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenPages)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                )
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.size(44.dp).testTag("studio_undo_button"),
                ) {
                    EditToolbarOutlinedIcon(
                        imageVector = Icons.AutoMirrored.Rounded.Undo,
                        contentDescription = "Undo",
                        iconSize = 20.dp,
                        enabled = canUndo,
                    )
                }
                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier.size(44.dp).testTag("studio_redo_button"),
                ) {
                    EditToolbarOutlinedIcon(
                        imageVector = Icons.AutoMirrored.Rounded.Redo,
                        contentDescription = "Redo",
                        iconSize = 20.dp,
                        enabled = canRedo,
                    )
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(44.dp).testTag("studio_settings_button"),
                ) {
                    EditToolbarOutlinedIcon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        iconSize = 20.dp,
                    )
                }
                IconButton(
                    onClick = onDone,
                    modifier = Modifier.size(44.dp).testTag("studio_done_button"),
                ) {
                    EditToolbarOutlinedIcon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Done editing",
                        iconSize = 22.dp,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                IconButton(
                    onClick = onAddElement,
                    modifier = Modifier.size(48.dp).testTag("studio_add_element"),
                ) {
                    EditToolbarOutlinedIcon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add",
                        iconSize = 22.dp,
                    )
                }
                IconButton(
                    onClick = onOpenWidgets,
                    modifier = Modifier.size(48.dp).testTag("studio_widgets_button"),
                ) {
                    EditToolbarOutlinedIcon(
                        imageVector = Icons.Rounded.Widgets,
                        contentDescription = "Widgets",
                        iconSize = 22.dp,
                    )
                }
                IconButton(
                    onClick = onOpenPages,
                    modifier = Modifier.size(48.dp).testTag("studio_pages_button"),
                ) {
                    EditToolbarOutlinedIcon(
                        imageVector = Icons.Rounded.Layers,
                        contentDescription = "Pages",
                        iconSize = 22.dp,
                    )
                }
                IconButton(
                    onClick = onOpenLayers,
                    modifier = Modifier.size(48.dp).testTag("studio_layers_button"),
                ) {
                    EditToolbarOutlinedIcon(
                        imageVector = Icons.Rounded.Layers,
                        contentDescription = "Layer Manager",
                        iconSize = 22.dp,
                    )
                }
                IconButton(
                    onClick = onOpenStyleStudio,
                    modifier = Modifier.size(48.dp).testTag("studio_style_button"),
                ) {
                    EditToolbarOutlinedIcon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = "Style Studio",
                        iconSize = 22.dp,
                    )
                }
                IconButton(
                    onClick = onToggleGridSnap,
                    modifier = Modifier.size(48.dp).testTag("studio_grid_snap_button"),
                ) {
                    EditToolbarOutlinedIcon(
                        imageVector = Icons.Rounded.GridOn,
                        contentDescription = "Toggle grid snap",
                        iconSize = 22.dp,
                        isActive = gridSnap,
                    )
                }
                IconButton(
                    onClick = onToggleMultiSelectMode,
                    modifier = Modifier.size(48.dp).testTag("studio_multi_select_button"),
                ) {
                    EditToolbarOutlinedIcon(
                        imageVector = Icons.Rounded.SelectAll,
                        contentDescription = "Multi-select mode",
                        iconSize = 22.dp,
                        isActive = multiSelectMode,
                    )
                }
                if (hasClipboard) {
                    IconButton(
                        onClick = onPaste,
                        modifier = Modifier.size(48.dp).testTag("studio_paste_button"),
                    ) {
                        EditToolbarOutlinedIcon(
                            imageVector = Icons.Rounded.ContentPaste,
                            contentDescription = "Paste",
                            iconSize = 22.dp,
                        )
                    }
                }
            }
            if (pageLocked || layoutLocked) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = if (layoutLocked) "Global layout is locked in Settings" else "This page is locked",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f),
                    )
                    if (pageLocked && !layoutLocked) {
                        AssistChip(
                            onClick = onUnlockPage,
                            leadingIcon = { Icon(Icons.Rounded.LockOpen, null, Modifier.size(14.dp)) },
                            label = { Text("Unlock Page") },
                        )
                    } else if (layoutLocked) {
                        AssistChip(
                            onClick = onOpenSettings,
                            label = { Text("Settings") },
                        )
                    }
                }
            }
            if (multiSelectCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("$multiSelectCount selected", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    if (multiSelectCount > 1) {
                        AssistChip(onClick = onGroupMulti, label = { Text("Group") })
                        AssistChip(onClick = onUngroupMulti, label = { Text("Ungroup") })
                        AssistChip(onClick = { onAlignMulti("LEFT") }, label = { Text("Align Left") })
                        AssistChip(onClick = { onAlignMulti("CENTER_X") }, label = { Text("Center X") })
                        AssistChip(onClick = { onAlignMulti("RIGHT") }, label = { Text("Align Right") })
                        AssistChip(onClick = { onAlignMulti("TOP") }, label = { Text("Align Top") })
                        AssistChip(onClick = { onAlignMulti("CENTER_Y") }, label = { Text("Center Y") })
                        AssistChip(onClick = { onAlignMulti("BOTTOM") }, label = { Text("Align Bottom") })
                    }
                    AssistChip(onClick = onLockMulti, leadingIcon = { Icon(Icons.Rounded.Lock, null, Modifier.size(16.dp)) }, label = { Text("Lock") })
                    AssistChip(onClick = onDeleteMulti, leadingIcon = { Icon(Icons.Rounded.Delete, null, Modifier.size(16.dp)) }, label = { Text("Delete") })
                }
            }
        }
    }
}

@Composable
private fun EditToolbarOutlinedIcon(
    imageVector: ImageVector,
    contentDescription: String,
    iconSize: Dp = 22.dp,
    enabled: Boolean = true,
    isActive: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .then(
                if (isActive) {
                    Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
                        
                } else {
                    Modifier
                }
            )
            .alpha(if (enabled) 1f else 0.38f),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedVectorIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            fillColor = Color.White,
            outlineColor = Color.Black,
            outlineWidthDp = 1.3.dp,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun FreeformPageCanvas(
    page: HomePageEntity,
    elements: List<HomeElementEntity>,
    state: LauncherUiState,
    multiSelectActive: Boolean,
    appDiscovery: AppDiscoveryManager,
    widgetHost: LauncherWidgetHost,
    onEnterEditMode: () -> Unit,
    onCanvasTap: () -> Unit,
    onCanvasDoubleTap: () -> Unit,
    onCanvasLongPress: () -> Unit,
    onVerticalSwipe: (Boolean) -> Unit,
    onLaunchApp: (AppEntity) -> Unit,
    onOpenTagSidebar: (Long) -> Unit,
    onOpenCategorySidebar: (Long) -> Unit,
    onOpenRuleSidebar: (Long) -> Unit,
    onOpenSidebar: (Long) -> Unit,
    onOpenWindow: (Long) -> Unit,
    onOpenFolder: (Long) -> Unit,
    onElementTrigger: (HomeElementEntity, String) -> Boolean,
    onSelectElement: (Long?) -> Unit,
    onInspectElement: (HomeElementEntity) -> Unit,
    onSaveElement: (HomeElementEntity, Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val latestIsEditMode by rememberUpdatedState(state.isEditMode)
    val latestElements by rememberUpdatedState(elements)
    val latestIsLandscape by rememberUpdatedState(isLandscape)

    var activeGuideX by remember { mutableStateOf<Float?>(null) }
    var activeGuideY by remember { mutableStateOf<Float?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(page.paddingDp.coerceIn(0f, 40f).dp),
    ) {
        val viewportWidthDp = maxWidth
        val viewportHeightDp = maxHeight
        val canvasWidthPx = with(density) { viewportWidthDp.toPx().coerceAtLeast(1f) }
        val canvasHeightPx = with(density) { viewportHeightDp.toPx().coerceAtLeast(1f) }
        val snapStepPx = with(density) { state.settings.gridSnapStepDp.dp.toPx().coerceAtLeast(8f) }

        val pageScrollState = rememberScrollState()
        val scrollContainerMod = if (page.verticallyScrollable && !state.isEditMode) {
            Modifier.verticalScroll(pageScrollState)
        } else Modifier
        val contentHeightDp = if (page.verticallyScrollable && !state.isEditMode) {
            val maxElemBottomDp = elements.maxOfOrNull { el ->
                val yf = if (isLandscape && el.landscapeYFraction != null) el.landscapeYFraction else el.yFraction
                (yf * viewportHeightDp.value) + el.heightDp + 32f
            }?.dp ?: viewportHeightDp
            maxOf(viewportHeightDp, maxElemBottomDp)
        } else {
            viewportHeightDp
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(scrollContainerMod),
        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentHeightDp)
                .testTag("home_canvas_${page.pageId}")
                .pointerInput(page.pageId, canvasWidthPx, canvasHeightPx) {
                    detectTapGestures(
                        onTap = { onCanvasTap() },
                        onDoubleTap = { onCanvasDoubleTap() },
                        onLongPress = { pressOffset ->
                            val hitElement = latestElements.any { el ->
                                val xf = if (latestIsLandscape && el.landscapeXFraction != null) el.landscapeXFraction else el.xFraction
                                val yf = if (latestIsLandscape && el.landscapeYFraction != null) el.landscapeYFraction else el.yFraction
                                val left = xf * canvasWidthPx
                                val top = yf * canvasHeightPx
                                val wPx = with(density) { el.widthDp.dp.toPx() }
                                val hPx = with(density) { el.heightDp.dp.toPx() }
                                pressOffset.x in left..(left + wPx) && pressOffset.y in top..(top + hPx)
                            }
                            if (!hitElement) {
                                if (!latestIsEditMode) {
                                    onEnterEditMode()
                                } else {
                                    onCanvasLongPress()
                                }
                            }
                        },
                    )
                }
                .pointerInput(state.isEditMode, page.verticallyScrollable) {
                    if (!state.isEditMode && !page.verticallyScrollable) {
                        var totalDy = 0f
                        var triggered = false
                        detectVerticalDragGestures(
                            onDragStart = { totalDy = 0f; triggered = false },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                totalDy += dragAmount
                                if (!triggered && abs(totalDy) > 95f) {
                                    triggered = true
                                    onVerticalSwipe(totalDy < 0f)
                                }
                            },
                        )
                    }
                },
        ) {
            if (state.isEditMode && (state.settings.editGridVisible || activeGuideX != null || activeGuideY != null)) {
                val gridColor = Color(state.settings.editGridColorHex).copy(alpha = state.settings.editGridOpacity.coerceIn(0.01f, 0.5f))
                val guideColor = Color(state.settings.alignmentGuideColorHex)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (state.settings.editGridVisible) {
                        val step = (state.settings.gridSnapStepDp.coerceAtLeast(8) * 2).dp.toPx()
                        var x = step
                        while (x < size.width) {
                            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                            x += step
                        }
                        var y = step
                        while (y < size.height) {
                            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                            y += step
                        }
                    }

                    // Magnetic alignment guides during drag
                    activeGuideX?.let { gx ->
                        drawLine(guideColor, Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 2.5f)
                    }
                    activeGuideY?.let { gy ->
                        drawLine(guideColor, Offset(0f, gy), Offset(size.width, gy), strokeWidth = 2.5f)
                    }
                }
            }

            val stackedChildIds = elements.filter { it.type == "WIDGET_STACK" }
                .flatMap { it.widgetStackItemsJson.split(",").mapNotNull { raw -> raw.trim().toLongOrNull() } }
                .toSet()
            elements.filter { it.elementId !in stackedChildIds && it.parentContainerId == null }.forEach { elem ->
                val latestElem by rememberUpdatedState(elem)
                val selected = state.isEditMode &&
                    (state.selectedElementId == elem.elementId || elem.elementId in state.multiSelectedIds)
                val baseXFrac = if (isLandscape && elem.landscapeXFraction != null) elem.landscapeXFraction else elem.xFraction
                val baseYFrac = if (isLandscape && elem.landscapeYFraction != null) elem.landscapeYFraction else elem.yFraction
                var dragX by remember(elem.elementId, baseXFrac, canvasWidthPx) { mutableFloatStateOf(baseXFrac * canvasWidthPx) }
                var dragY by remember(elem.elementId, baseYFrac, canvasHeightPx) { mutableFloatStateOf(baseYFrac * canvasHeightPx) }
                var widthDp by remember(elem.elementId, elem.widthDp) { mutableFloatStateOf(elem.widthDp) }
                var heightDp by remember(elem.elementId, elem.heightDp) { mutableFloatStateOf(elem.heightDp) }

                val canMove = state.isEditMode && !page.isLocked && !elem.locked && !state.settings.layoutLocked
                val dragModifier = if (canMove) {
                    Modifier.pointerInput(elem.elementId, canvasWidthPx, canvasHeightPx, state.settings.gridSnapEnabled, state.settings.alignmentGuidesEnabled) {
                        detectDragGestures(
                            onDragStart = { onSelectElement(latestElem.elementId) },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                var nextX = (dragX + dragAmount.x).coerceIn(0f, (canvasWidthPx - 40f).coerceAtLeast(0f))
                                var nextY = (dragY + dragAmount.y).coerceIn(0f, (canvasHeightPx - 40f).coerceAtLeast(0f))

                                if (state.settings.gridSnapEnabled) {
                                    nextX = (nextX / snapStepPx).roundToInt() * snapStepPx
                                    nextY = (nextY / snapStepPx).roundToInt() * snapStepPx
                                }

                                if (state.settings.alignmentGuidesEnabled) {
                                    val centerCanvasX = canvasWidthPx / 2f
                                    val elemW = widthDp.dp.toPx()
                                    val matchX = latestElements.filter { it.elementId != latestElem.elementId }
                                        .map { (if (latestIsLandscape && it.landscapeXFraction != null) it.landscapeXFraction else it.xFraction) * canvasWidthPx }
                                        .plus(centerCanvasX - elemW / 2f)
                                        .firstOrNull { abs(it - nextX) < 12f }
                                    if (matchX != null) {
                                        nextX = matchX
                                        activeGuideX = matchX
                                    } else activeGuideX = null

                                    val matchY = latestElements.filter { it.elementId != latestElem.elementId }
                                        .map { (if (latestIsLandscape && it.landscapeYFraction != null) it.landscapeYFraction else it.yFraction) * canvasHeightPx }
                                        .firstOrNull { abs(it - nextY) < 12f }
                                    if (matchY != null) {
                                        nextY = matchY
                                        activeGuideY = matchY
                                    } else activeGuideY = null
                                }

                                dragX = nextX
                                dragY = nextY
                            },
                            onDragEnd = {
                                activeGuideX = null
                                activeGuideY = null
                                val fx = (dragX / canvasWidthPx).coerceIn(0f, 0.92f)
                                val fy = (dragY / canvasHeightPx).coerceIn(0f, 0.92f)
                                val cur = latestElem
                                val updated = if (latestIsLandscape) {
                                    cur.copy(landscapeXFraction = fx, landscapeYFraction = fy, widthDp = widthDp, heightDp = heightDp)
                                } else {
                                    cur.copy(xFraction = fx, yFraction = fy, widthDp = widthDp, heightDp = heightDp)
                                }
                                onSaveElement(updated, true)
                                if (!cur.groupId.isNullOrBlank()) {
                                    val oldX = if (latestIsLandscape) cur.landscapeXFraction ?: cur.xFraction else cur.xFraction
                                    val oldY = if (latestIsLandscape) cur.landscapeYFraction ?: cur.yFraction else cur.yFraction
                                    val deltaX = fx - oldX
                                    val deltaY = fy - oldY
                                    latestElements.filter { it.groupId == cur.groupId && it.elementId != cur.elementId }.forEach { sibling ->
                                        val siblingX = if (latestIsLandscape) sibling.landscapeXFraction ?: sibling.xFraction else sibling.xFraction
                                        val siblingY = if (latestIsLandscape) sibling.landscapeYFraction ?: sibling.yFraction else sibling.yFraction
                                        val moved = if (latestIsLandscape) {
                                            sibling.copy(landscapeXFraction = (siblingX + deltaX).coerceIn(0f, 0.92f), landscapeYFraction = (siblingY + deltaY).coerceIn(0f, 0.92f))
                                        } else {
                                            sibling.copy(xFraction = (siblingX + deltaX).coerceIn(0f, 0.92f), yFraction = (siblingY + deltaY).coerceIn(0f, 0.92f))
                                        }
                                        onSaveElement(moved, true)
                                    }
                                }
                            },
                        )
                    }
                } else Modifier

                val elementWidthPx = with(density) { widthDp.dp.toPx() }
                val elementHeightPx = with(density) { heightDp.dp.toPx() }
                val pinnedX = if (!elem.pinnedToViewport) dragX else when (elem.pinEdge) {
                    "LEFT" -> with(density) { elem.pinOffsetDp.dp.toPx() }
                    "RIGHT" -> canvasWidthPx - elementWidthPx - with(density) { elem.pinOffsetDp.dp.toPx() }
                    else -> dragX
                }
                val pinnedY = if (!elem.pinnedToViewport) dragY else when (elem.pinEdge) {
                    "TOP" -> with(density) { elem.pinOffsetDp.dp.toPx() }
                    "BOTTOM" -> canvasHeightPx - elementHeightPx - with(density) { elem.pinOffsetDp.dp.toPx() }
                    else -> dragY
                }
                val scrollCompensation = if (elem.pinnedToViewport && page.verticallyScrollable && !state.isEditMode) pageScrollState.value else 0
                Box(
                    modifier = Modifier
                        .offset { IntOffset(pinnedX.roundToInt(), pinnedY.roundToInt() + scrollCompensation) }
                        .size(width = widthDp.dp, height = heightDp.dp)
                        .graphicsLayer {
                            rotationX = elem.rotationXDeg
                            rotationY = elem.rotationYDeg
                            rotationZ = elem.rotationDeg
                            cameraDistance = (8f + (1f - elem.perspective.coerceIn(0f, 1f)) * 24f) * density
                            scaleX = elem.scaleX * (if (elem.flipHorizontal) -1f else 1f)
                            scaleY = elem.scaleY * (if (elem.flipVertical) -1f else 1f)
                        }
                        .alpha(elem.alpha.coerceIn(0.1f, 1f))
                        .then(
                            if (state.isEditMode) {
                                Modifier.border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(elem.cornerRadiusDp.dp),
                                )
                            } else Modifier
                        )
                        .testTag("home_element_${elem.elementId}"),
                ) {
                    HomeElementBody(
                        element = elem,
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
                        onUpdateElement = { onSaveElement(it, true) },
                    )

                    if (state.isEditMode) {
                        // Touch shield sits above AndroidView / LazyVerticalGrid / widget buttons
                        // so any placed element can be reliably tapped, dragged, or inspected in edit mode.
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(dragModifier)
                                .clickable {
                                    val cur = latestElem
                                    if (selected && !multiSelectActive) onInspectElement(cur) else onSelectElement(cur.elementId)
                                }
                                .testTag("edit_shield_${elem.elementId}"),
                        )

                        if (elem.locked || page.isLocked) {
                            Icon(
                                Icons.Rounded.Lock,
                                contentDescription = "Locked",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.TopStart).padding(4.dp).size(16.dp),
                            )
                        }
                        if (selected) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 6.dp, y = (-6).dp)
                                    .size(28.dp)
                                    .clickable { onInspectElement(latestElem) }
                                    .testTag("inspect_element_${elem.elementId}"),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val iconStyle = LocalIconStyleConfig.current
                                    if (iconStyle.enabled) {
                                        OutlinedVectorIcon(
                                            imageVector = Icons.Rounded.Tune,
                                            contentDescription = "Inspect",
                                            fillColor = Color(iconStyle.fillColorHex),
                                            outlineColor = Color(iconStyle.outlineColorHex),
                                            outlineWidthDp = (iconStyle.outlineWidthDp * 0.55f).dp,
                                            modifier = Modifier.size(15.dp),
                                        )
                                    } else {
                                        Icon(Icons.Rounded.Tune, contentDescription = "Inspect", tint = Color.Black, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }

                            if (canMove) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 6.dp, y = 6.dp)
                                        .size(28.dp)
                                        .pointerInput(elem.elementId, canvasWidthPx, canvasHeightPx) {
                                            detectDragGestures(
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    val dxDp = with(density) { dragAmount.x.toDp().value }
                                                    val dyDp = with(density) { dragAmount.y.toDp().value }
                                                    widthDp = (widthDp + dxDp).coerceIn(48f, 380f)
                                                    heightDp = (heightDp + dyDp).coerceIn(48f, 440f)
                                                },
                                                onDragEnd = {
                                                    val fx = (dragX / canvasWidthPx).coerceIn(0f, 0.92f)
                                                    val fy = (dragY / canvasHeightPx).coerceIn(0f, 0.92f)
                                                    val cur = latestElem
                                                    val updated = if (latestIsLandscape) {
                                                        cur.copy(landscapeXFraction = fx, landscapeYFraction = fy, widthDp = widthDp, heightDp = heightDp)
                                                    } else {
                                                        cur.copy(xFraction = fx, yFraction = fy, widthDp = widthDp, heightDp = heightDp)
                                                    }
                                                    onSaveElement(updated, true)
                                                },
                                            )
                                        }
                                        .testTag("resize_element_${elem.elementId}"),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.OpenInFull, contentDescription = "Resize", tint = Color.White, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}
