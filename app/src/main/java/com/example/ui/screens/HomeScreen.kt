package com.example.ui.screens

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.AutoAwesome
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
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewCarousel
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AppEntity
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.LayoutSnapshotEntity
import com.example.data.db.ThemeProfileEntity
import com.example.data.prefs.LauncherSettings
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.InstalledWidgetProvider
import com.example.data.system.LauncherWidgetHost
import com.example.ui.components.LayerStackDrawer
import com.example.ui.components.LocalAppDragCoordinator
import com.example.ui.components.LocalIconStyleConfig
import com.example.ui.components.MultiSelectArrangeBar
import com.example.ui.components.OutlinedVectorIcon
import com.example.ui.components.ReadableLauncherText
import com.example.ui.components.StudioQuickCustomizerDialog
import com.example.ui.components.TagBadgeIcon
import com.example.ui.components.TemplateAndThemeGalleryDialog
import com.example.ui.components.WallpaperBackground
import com.example.ui.components.WidgetPickerDialog
import com.example.ui.state.LauncherRoute
import com.example.ui.state.LauncherUiState
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

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
    onCopyStyle: (HomeElementEntity) -> Unit = {},
    onPasteStyle: (Set<Long>) -> Unit = {},
    onGroupElements: (Set<Long>) -> Unit = {},
    onUngroupElements: (Set<Long>) -> Unit = {},
    onAlignElements: (Set<Long>, String) -> Unit = { _, _ -> },
    onDistributeElements: (Set<Long>, String) -> Unit = { _, _ -> },
    onMatchElementSizes: (Set<Long>, String) -> Unit = { _, _ -> },
    onStackElements: (Set<Long>) -> Unit = {},
    onUnstackElement: (Long) -> Unit = {},
    onAddChildToContainer: (Long, HomeElementEntity) -> Unit = { _, _ -> },
    onMoveChildInParent: (Long, Int) -> Unit = { _, _ -> },
    onCycleWidgetStack: (Long, Int) -> Unit = { _, _ -> },
    onApplyStarterTemplate: (String) -> Unit = {},
    onSaveThemeProfile: (String) -> Unit = {},
    onApplyThemeProfile: (ThemeProfileEntity) -> Unit = {},
    onDeleteThemeProfile: (Long) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val density = LocalDensity.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val systemDark = isSystemInDarkTheme()

    val visiblePages = remember(state.homePages, state.isEditMode) {
        val list = if (state.isEditMode) state.homePages else state.homePages.filter { !it.isHidden }
        list.ifEmpty { listOf(HomePageEntity(pageId = 1L, title = "Main Deck", pageOrder = 0)) }
    }
    val feedEnabled = state.settings.widgetFeedEnabled
    val loopActive = !feedEnabled && state.settings.infinitePageLoop && visiblePages.size > 1 && !state.isEditMode
    val virtualPageCount = if (loopActive) visiblePages.size * 200 else (visiblePages.size + if (feedEnabled) 1 else 0)
    val loopBaseOffset = if (loopActive) (virtualPageCount / 2) - ((virtualPageCount / 2) % visiblePages.size) else 0

    fun realPageIndex(virtualIdx: Int): Int {
        if (!loopActive) return virtualIdx
        val n = visiblePages.size
        return ((virtualIdx % n) + n) % n
    }

    val pagerState = rememberPagerState(
        initialPage = if (loopActive) {
            loopBaseOffset + state.currentPageIndex.coerceIn(0, visiblePages.lastIndex)
        } else {
            state.currentPageIndex.coerceIn(0, visiblePages.lastIndex)
        }
    ) {
        virtualPageCount
    }

    fun resolveVirtualTarget(realTarget: Int): Int {
        val clamped = realTarget.coerceIn(0, visiblePages.lastIndex)
        if (!loopActive) return clamped
        val curReal = realPageIndex(pagerState.currentPage)
        var delta = clamped - curReal
        val n = visiblePages.size
        if (delta > n / 2) delta -= n
        if (delta < -n / 2) delta += n
        return (pagerState.currentPage + delta).coerceIn(0, virtualPageCount - 1)
    }

    val scrollToRealPage: (Int) -> Unit = { targetReal ->
        scope.launch {
            pagerState.animateScrollToPage(resolveVirtualTarget(targetReal))
        }
    }

    LaunchedEffect(pagerState.currentPage, visiblePages.size, loopActive) {
        val realIdx = realPageIndex(pagerState.currentPage)
        if (realIdx < visiblePages.size) {
            onPageChanged(realIdx)
        }
    }

    LaunchedEffect(state.currentPageIndex, visiblePages.size, feedEnabled, loopActive) {
        val target = state.currentPageIndex.coerceIn(0, visiblePages.lastIndex)
        val curReal = realPageIndex(pagerState.currentPage)
        if ((!feedEnabled || pagerState.currentPage < visiblePages.size) && curReal != target) {
            pagerState.animateScrollToPage(resolveVirtualTarget(target))
        }
    }

    var showAddElementDialog by remember { mutableStateOf(false) }
    var showPagesDialog by remember { mutableStateOf(false) }
    var showWidgetPicker by remember { mutableStateOf(false) }
    var showFeedWidgetPicker by remember { mutableStateOf(false) }
    var showStyleDialog by remember { mutableStateOf(false) }
    var showLayerDrawer by remember { mutableStateOf(false) }
    var showTemplateGallery by remember { mutableStateOf(false) }
    var inspectingElement by remember { mutableStateOf<HomeElementEntity?>(null) }
    var multiSelectActive by remember { mutableStateOf(false) }

    BackHandler(enabled = state.isEditMode) {
        multiSelectActive = false
        onToggleEditMode(false)
    }

    val currentRealPageIdx = realPageIndex(pagerState.currentPage).coerceAtMost(visiblePages.lastIndex)
    val onFeedPage = feedEnabled && !loopActive && pagerState.currentPage == visiblePages.size
    val currentPage = visiblePages.getOrElse(currentRealPageIdx) { visiblePages.first() }
    val leftEdgeSidebar = state.sidebars.firstOrNull { it.side == "LEFT" && it.edgeZoneEnabled }
    val rightEdgeSidebar = state.sidebars.firstOrNull { it.side == "RIGHT" && it.edgeZoneEnabled }
    val topEdgeSidebar = state.sidebars.firstOrNull { it.side == "TOP" && it.edgeZoneEnabled }
    val bottomEdgeSidebar = state.sidebars.firstOrNull { it.side == "BOTTOM" && it.edgeZoneEnabled }

    Box(modifier = Modifier.fillMaxSize()) {
        WallpaperBackground(
            settings = state.settings,
            currentPage = currentPage,
            pagerOffsetFraction = (currentRealPageIdx - (visiblePages.size - 1) / 2f) + pagerState.currentPageOffsetFraction,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.isEditMode, state.gestures) {
                    if (!state.isEditMode) {
                        var twoFingerStartY = 0f
                        var twoFingerStartDist = 0f
                        var trackingTwoFinger = false
                        var twoFingerMoved = false
                        var twoFingerTriggered = false
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val pressed = event.changes.filter { it.pressed }
                                if (pressed.size == 2) {
                                    val p0 = pressed[0].position
                                    val p1 = pressed[1].position
                                    val avgY = (p0.y + p1.y) / 2f
                                    val dist = hypot((p0.x - p1.x).toDouble(), (p0.y - p1.y).toDouble()).toFloat()
                                    if (!trackingTwoFinger) {
                                        trackingTwoFinger = true
                                        twoFingerMoved = false
                                        twoFingerTriggered = false
                                        twoFingerStartY = avgY
                                        twoFingerStartDist = dist.coerceAtLeast(24f)
                                    } else if (!twoFingerTriggered) {
                                        val dy = avgY - twoFingerStartY
                                        val dDist = dist - twoFingerStartDist
                                        if (abs(dy) > 26f || abs(dDist) > 26f) {
                                            twoFingerMoved = true
                                        }
                                        when {
                                            dy < -110f -> {
                                                twoFingerTriggered = true
                                                onTriggerGesture("TWO_FINGER_SWIPE_UP") { target -> scrollToRealPage(target) }
                                            }
                                            dy > 110f -> {
                                                twoFingerTriggered = true
                                                onTriggerGesture("TWO_FINGER_SWIPE_DOWN") { target -> scrollToRealPage(target) }
                                            }
                                            dist < twoFingerStartDist * 0.72f && twoFingerStartDist - dist > 45f -> {
                                                twoFingerTriggered = true
                                                onTriggerGesture("PINCH_IN") { target -> scrollToRealPage(target) }
                                            }
                                            dist > twoFingerStartDist * 1.35f && dist - twoFingerStartDist > 45f -> {
                                                twoFingerTriggered = true
                                                onTriggerGesture("PINCH_OUT") { target -> scrollToRealPage(target) }
                                            }
                                        }
                                    }
                                } else {
                                    if (pressed.isEmpty() && trackingTwoFinger) {
                                        if (!twoFingerTriggered && !twoFingerMoved) {
                                            onTriggerGesture("TWO_FINGER_TAP") { target -> scrollToRealPage(target) }
                                        }
                                        trackingTwoFinger = false
                                        twoFingerMoved = false
                                        twoFingerTriggered = false
                                    }
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
                        onPaste = { onPasteElement(currentPage.pageId) },
                        onAddElement = { showAddElementDialog = true },
                        onOpenWidgets = { showWidgetPicker = true },
                        onOpenPages = { showPagesDialog = true },
                        onOpenLayers = { showLayerDrawer = true },
                        onOpenTemplates = { showTemplateGallery = true },
                        onOpenStyleStudio = { showStyleDialog = true },
                        onOpenSettings = { onNavigate(LauncherRoute.Settings) },
                        onUnlockPage = { onSavePage(currentPage.copy(isLocked = false)) },
                        onDone = {
                            multiSelectActive = false
                            onToggleEditMode(false)
                        },
                    )
                    if (state.multiSelectedIds.isNotEmpty()) {
                        MultiSelectArrangeBar(
                            selectedCount = state.multiSelectedIds.size,
                            hasCopiedStyle = state.copiedStyle != null,
                            onStackSelected = { onStackElements(state.multiSelectedIds) },
                            onGroupSelected = { onGroupElements(state.multiSelectedIds) },
                            onUngroupSelected = { onUngroupElements(state.multiSelectedIds) },
                            onAlign = { mode -> onAlignElements(state.multiSelectedIds, mode) },
                            onDistribute = { axis -> onDistributeElements(state.multiSelectedIds, axis) },
                            onMatchSize = { dim -> onMatchElementSizes(state.multiSelectedIds, dim) },
                            onPasteStyle = { onPasteStyle(state.multiSelectedIds) },
                            onLockSelected = onLockMultiSelected,
                            onDeleteSelected = onDeleteMultiSelected,
                        )
                    }
                } else {
                    if (state.settings.showCustomStatusStrip) {
                        val hudText = resolveExtendedDynamicTokens(
                            template = "{GREETING}  •  {BATTERY} ({CHARGING})  •  {NETWORK}  •  {TIME}",
                            state = state,
                            context = context,
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .testTag("home_custom_status_strip"),
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = state.settings.surfaceOpacity.coerceIn(0.28f, 0.9f)),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.16f)),
                        ) {
                            ReadableLauncherText(
                                text = hudText,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            )
                        }
                    }
                    if (!state.isDefaultLauncher && state.settings.showDefaultHomeBanner) {
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
                }

                if (state.settings.pageIndicatorPlacement == "TOP" && visiblePages.size > 1 && state.settings.pageIndicatorStyle != "HIDDEN") {
                    HomePageIndicatorBar(
                        visiblePages = visiblePages,
                        currentRealPageIdx = currentRealPageIdx,
                        settings = state.settings,
                        onSelectPage = scrollToRealPage,
                    )
                }

                val totalPagerPages = visiblePages.size + if (feedEnabled) 1 else 0
                val swipeLeftAction = state.gestures["SWIPE_LEFT"]?.actionType ?: "NEXT_PAGE"
                val swipeRightAction = state.gestures["SWIPE_RIGHT"]?.actionType ?: "PREV_PAGE"
                val hasCustomHorizontalSwipeOverride =
                    (swipeLeftAction != "NEXT_PAGE" && swipeLeftAction != "NONE") ||
                        (swipeRightAction != "PREV_PAGE" && swipeRightAction != "NONE")
                val allowCanvasHorizontalSwipe = totalPagerPages <= 1 || hasCustomHorizontalSwipeOverride

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    HorizontalPager(
                        state = pagerState,
                        key = { idx ->
                            when {
                                loopActive -> "loop_$idx"
                                feedEnabled && idx == visiblePages.size -> "WIDGET_FEED"
                                else -> visiblePages[idx].pageId
                            }
                        },
                        userScrollEnabled = !state.isEditMode && totalPagerPages > 1 && !hasCustomHorizontalSwipeOverride,
                        modifier = Modifier.fillMaxSize(),
                    ) { virtualIdx ->
                        if (feedEnabled && !loopActive && virtualIdx == visiblePages.size) {
                            val feedElements = state.homeElements
                                .filter { it.ownerType == "FEED" && it.parentContainerId == null && it.visible }
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
                                                    onExecuteElementAction(elem, trigger) { target -> scrollToRealPage(target) }
                                                },
                                                onOpenSearch = onOpenSearch,
                                                onJumpToPage = scrollToRealPage,
                                                onSaveElement = onSaveElement,
                                                onCycleStack = onCycleWidgetStack,
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            val pageIdx = realPageIndex(virtualIdx)
                            val page = visiblePages[pageIdx]
                            val pageOffset = ((pagerState.currentPage - virtualIdx) + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
                            val effectiveTransition = page.transitionOverride ?: state.settings.pageTransitionStyle
                            val camDist = 16f * density.density
                            val pageTransitionMod = when (effectiveTransition) {
                                "FADE" -> Modifier.graphicsLayer {
                                    alpha = (1f - abs(pageOffset) * 0.85f).coerceIn(0f, 1f)
                                }
                                "DEPTH" -> Modifier.graphicsLayer {
                                    val absOff = abs(pageOffset)
                                    alpha = (1f - absOff * 0.45f).coerceIn(0f, 1f)
                                    val sc = 1f - absOff * 0.14f
                                    scaleX = sc
                                    scaleY = sc
                                }
                                "CUBE" -> Modifier.graphicsLayer {
                                    cameraDistance = camDist
                                    rotationY = pageOffset * 90f
                                    transformOrigin = TransformOrigin(if (pageOffset < 0f) 1f else 0f, 0.5f)
                                    alpha = (1f - abs(pageOffset) * 0.25f).coerceIn(0f, 1f)
                                }
                                "FLIP" -> Modifier.graphicsLayer {
                                    cameraDistance = camDist
                                    rotationY = pageOffset * 180f
                                    alpha = if (abs(pageOffset) > 0.5f) 0f else 1f
                                }
                                "STACK" -> Modifier.graphicsLayer {
                                    if (pageOffset > 0f) {
                                        translationX = size.width * pageOffset * 0.72f
                                        val sc = 1f - pageOffset * 0.12f
                                        scaleX = sc
                                        scaleY = sc
                                        alpha = (1f - pageOffset * 0.4f).coerceIn(0f, 1f)
                                    }
                                }
                                "ZOOM" -> Modifier.graphicsLayer {
                                    val sc = 1f - abs(pageOffset) * 0.35f
                                    scaleX = sc
                                    scaleY = sc
                                    alpha = (1f - abs(pageOffset) * 0.65f).coerceIn(0f, 1f)
                                }
                                "ACCORDION" -> Modifier.graphicsLayer {
                                    scaleX = (1f - abs(pageOffset)).coerceIn(0.05f, 1f)
                                    transformOrigin = TransformOrigin(if (pageOffset < 0f) 0f else 1f, 0.5f)
                                }
                                "ROTATE" -> Modifier.graphicsLayer {
                                    rotationZ = pageOffset * 28f
                                    translationY = abs(pageOffset) * 70f
                                    alpha = (1f - abs(pageOffset) * 0.35f).coerceIn(0f, 1f)
                                }
                                "PARALLAX" -> Modifier.graphicsLayer {
                                    translationX = -pageOffset * size.width * 0.28f
                                }
                                "WINDMILL" -> Modifier.graphicsLayer {
                                    rotationZ = pageOffset * 42f
                                    transformOrigin = TransformOrigin(0.5f, 1.35f)
                                    alpha = (1f - abs(pageOffset) * 0.35f).coerceIn(0f, 1f)
                                }
                                else -> Modifier
                            }
                            val elements = remember(
                                state.homeElements,
                                page.pageId,
                                state.isEditMode,
                                state.runtimeShownElementIds,
                                state.runtimeHiddenElementIds,
                                state.activeVisibilityGroups,
                                state.hiddenVisibilityGroups,
                                state.settings.themeMode,
                                isLandscape,
                                systemDark,
                            ) {
                                state.homeElements
                                    .filter {
                                        it.ownerType == "PAGE" &&
                                            it.pageId == page.pageId &&
                                            it.parentContainerId == null &&
                                            !it.pinnedToAllPages
                                    }
                                    .filter { el ->
                                        if (state.isEditMode) el.visible
                                        else isElementConditionMet(el, state, context, isLandscape, systemDark)
                                    }
                                    .filter { element ->
                                        element.type != "TAG_SHORTCUT" ||
                                            state.allTags.find { it.tagId == element.targetTagId }?.showOnHome != false
                                    }
                                    .sortedBy { it.zIndex }
                            }
                            Box(modifier = Modifier.fillMaxSize().then(pageTransitionMod)) {
                                FreeformPageCanvas(
                                    page = page,
                                    isCurrentPage = page.pageId == currentPage.pageId,
                                    allowHorizontalSwipe = allowCanvasHorizontalSwipe,
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
                                            onTriggerGesture("SINGLE_TAP") { target -> scrollToRealPage(target) }
                                        }
                                    },
                                    onCanvasDoubleTap = {
                                        if (!state.isEditMode) {
                                            when (state.settings.doubleTapPageBehavior) {
                                                "NONE" -> Unit
                                                "EDIT_MODE" -> onToggleEditMode(true)
                                                "OPEN_DRAWER" -> onOpenDrawer()
                                                "OPEN_SEARCH" -> onOpenSearch()
                                                else -> onTriggerGesture("DOUBLE_TAP") { target -> scrollToRealPage(target) }
                                            }
                                        }
                                    },
                                    onCanvasLongPress = {
                                        if (!state.isEditMode) {
                                            val lpBinding = state.gestures["LONG_PRESS"]
                                            if (lpBinding != null && lpBinding.actionType != "NONE" && lpBinding.actionType != "TOGGLE_EDIT_MODE") {
                                                onTriggerGesture("LONG_PRESS") { target -> scrollToRealPage(target) }
                                            } else {
                                                onToggleEditMode(true)
                                            }
                                        }
                                    },
                                    onVerticalSwipe = { isUp ->
                                        if (!state.isEditMode) {
                                            onTriggerGesture(if (isUp) "SWIPE_UP" else "SWIPE_DOWN") { target -> scrollToRealPage(target) }
                                        }
                                    },
                                    onHorizontalSwipe = { isLeft ->
                                        if (!state.isEditMode) {
                                            onTriggerGesture(if (isLeft) "SWIPE_LEFT" else "SWIPE_RIGHT") { target -> scrollToRealPage(target) }
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
                                        onExecuteElementAction(elem, trigger) { target -> scrollToRealPage(target) }
                                    },
                                    onOpenSearch = onOpenSearch,
                                    onJumpToPage = scrollToRealPage,
                                    onCycleStack = onCycleWidgetStack,
                                    onSelectElement = { id ->
                                        if (multiSelectActive && id != null) onToggleMultiSelect(id) else onSelectElement(id)
                                    },
                                    onInspectElement = { inspectingElement = it },
                                    onSaveElement = onSaveElement,
                                )
                            }
                        }
                    }

                    // Sticky Pinned Overlay Layer across all non-feed home pages
                    val pinnedElements = remember(
                        state.homeElements,
                        state.isEditMode,
                        state.runtimeShownElementIds,
                        state.runtimeHiddenElementIds,
                        state.activeVisibilityGroups,
                        state.hiddenVisibilityGroups,
                        state.settings.themeMode,
                        isLandscape,
                        systemDark,
                    ) {
                        state.homeElements
                            .filter { it.ownerType == "PAGE" && it.pinnedToAllPages && it.parentContainerId == null }
                            .filter { el ->
                                if (state.isEditMode) el.visible
                                else isElementConditionMet(el, state, context, isLandscape, systemDark)
                            }
                            .sortedBy { it.zIndex }
                    }
                    if (!onFeedPage && pinnedElements.isNotEmpty()) {
                        PinnedElementsOverlay(
                            page = currentPage,
                            elements = pinnedElements,
                            state = state,
                            multiSelectActive = multiSelectActive,
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
                                onExecuteElementAction(elem, trigger) { target -> scrollToRealPage(target) }
                            },
                            onOpenSearch = onOpenSearch,
                            onJumpToPage = scrollToRealPage,
                            onCycleStack = onCycleWidgetStack,
                            onSelectElement = { id ->
                                if (multiSelectActive && id != null) onToggleMultiSelect(id) else onSelectElement(id)
                            },
                            onInspectElement = { inspectingElement = it },
                            onSaveElement = onSaveElement,
                        )
                    }

                    // 4-Edge Activation Zones (Left / Right / Top / Bottom)
                    if (!state.isEditMode && !(feedEnabled && pagerState.currentPage == visiblePages.size) && leftEdgeSidebar != null) {
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

                    if (!state.isEditMode && !(feedEnabled && pagerState.currentPage == visiblePages.size) && rightEdgeSidebar != null) {
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

                if (state.settings.pageIndicatorPlacement != "TOP" && visiblePages.size > 1 && state.settings.pageIndicatorStyle != "HIDDEN") {
                    HomePageIndicatorBar(
                        visiblePages = visiblePages,
                        currentRealPageIdx = currentRealPageIdx,
                        settings = state.settings,
                        onSelectPage = scrollToRealPage,
                    )
                }

                val effectiveShowDock = currentPage.showDockOverride ?: state.settings.showDockBar
                if (!state.isEditMode && effectiveShowDock) {
                    val dockRadius = state.settings.globalCornerRadiusDp.coerceIn(12f, 32f).dp
                    val dockAlpha = state.settings.dockBackgroundAlpha.coerceIn(0f, 1f)
                    val dockHeight = state.settings.dockHeightDp.coerceIn(40f, 80f).dp
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
                                .height(dockHeight)
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
                                .size(dockHeight)
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

    if (showPagesDialog) {
        PagesAndSnapshotsDialog(
            pages = state.homePages,
            snapshots = state.snapshots,
            currentPageId = currentPage.pageId,
            onJumpToPage = { pageId ->
                val idx = visiblePages.indexOfFirst { it.pageId == pageId }
                if (idx >= 0) {
                    onPageChanged(idx)
                    scrollToRealPage(idx)
                }
            },
            onImportImage = onImportImage,
            onAddPage = onAddPage,
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
            onCopyStyle = { onCopyStyle(elem) },
            onPasteStyle = { onPasteStyle(setOf(elem.elementId)) },
            onUnstack = {
                onUnstackElement(elem.elementId)
                inspectingElement = null
            },
            onAddChildToContainer = onAddChildToContainer,
            onMoveChildInParent = onMoveChildInParent,
            onInspectChild = { child -> inspectingElement = child },
            onDeleteChild = onDeleteElement,
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

    if (showLayerDrawer) {
        LayerStackDrawer(
            currentPage = currentPage,
            allElements = state.homeElements,
            selectedElementId = state.selectedElementId,
            onSelectElement = { el -> onSelectElement(el.elementId) },
            onInspectElement = { el ->
                showLayerDrawer = false
                inspectingElement = el
            },
            onToggleVisibility = { el -> onSaveElement(el.copy(visible = !el.visible), false) },
            onToggleLock = { el -> onSaveElement(el.copy(locked = !el.locked), false) },
            onBringForward = onBringForward,
            onSendBackward = onSendBackward,
            onDismiss = { showLayerDrawer = false },
        )
    }

    if (showTemplateGallery) {
        TemplateAndThemeGalleryDialog(
            themeProfiles = state.themeProfiles,
            onApplyStarterTemplate = onApplyStarterTemplate,
            onSaveThemeProfile = onSaveThemeProfile,
            onApplyThemeProfile = onApplyThemeProfile,
            onDeleteThemeProfile = onDeleteThemeProfile,
            onDismiss = { showTemplateGallery = false },
        )
    }
}

@Composable
private fun HomePageIndicatorBar(
    visiblePages: List<HomePageEntity>,
    currentRealPageIdx: Int,
    settings: LauncherSettings,
    onSelectPage: (Int) -> Unit,
) {
    val indicatorColor = Color(settings.pageIndicatorColorHex)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .testTag("home_page_indicator"),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (settings.pageIndicatorStyle) {
            "PILL" -> {
                visiblePages.forEachIndexed { idx, _ ->
                    val active = idx == currentRealPageIdx
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .width(if (active) 22.dp else 7.dp)
                            .height(7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (active) indicatorColor else Color.White.copy(alpha = 0.35f))
                            .clickable { onSelectPage(idx) },
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
                        text = "${currentRealPageIdx + 1} / ${visiblePages.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = indicatorColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                    )
                }
            }
            "PROGRESS" -> {
                val fraction = (currentRealPageIdx + 1f) / visiblePages.size.coerceAtLeast(1).toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.22f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(indicatorColor),
                    )
                }
            }
            "TABS" -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items(visiblePages.size) { idx ->
                        val pg = visiblePages[idx]
                        val active = idx == currentRealPageIdx
                        Surface(
                            onClick = { onSelectPage(idx) },
                            shape = RoundedCornerShape(50),
                            color = if (active) indicatorColor else Color.Black.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, if (active) indicatorColor else Color.White.copy(alpha = 0.25f)),
                            modifier = Modifier.testTag("home_page_tab_${pg.pageId}"),
                        ) {
                            Text(
                                text = pg.title,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                color = if (active) Color(0xFF06131F) else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
            else -> {
                visiblePages.forEachIndexed { idx, _ ->
                    val active = idx == currentRealPageIdx
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (active) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) indicatorColor else Color.White.copy(alpha = 0.4f))
                            .clickable { onSelectPage(idx) },
                    )
                }
            }
        }
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
    onPaste: () -> Unit,
    onAddElement: () -> Unit,
    onOpenWidgets: () -> Unit,
    onOpenPages: () -> Unit,
    onOpenLayers: () -> Unit = {},
    onOpenTemplates: () -> Unit = {},
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
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
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
                        imageVector = Icons.Rounded.ViewCarousel,
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
                        contentDescription = "Layer Stack",
                        iconSize = 22.dp,
                    )
                }
                IconButton(
                    onClick = onOpenTemplates,
                    modifier = Modifier.size(48.dp).testTag("studio_templates_button"),
                ) {
                    EditToolbarOutlinedIcon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Templates & Themes",
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
    isCurrentPage: Boolean,
    allowHorizontalSwipe: Boolean,
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
    onHorizontalSwipe: (Boolean) -> Unit,
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
    onCycleStack: (Long, Int) -> Unit = { _, _ -> },
    onSelectElement: (Long?) -> Unit,
    onInspectElement: (HomeElementEntity) -> Unit,
    onSaveElement: (HomeElementEntity, Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val dragCoordinator = LocalAppDragCoordinator.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useLandscapeCoords = isLandscape && state.settings.landscapeSeparateLayout
    val latestIsEditMode by rememberUpdatedState(state.isEditMode)
    val latestElements by rememberUpdatedState(elements)
    val latestUseLandscape by rememberUpdatedState(useLandscapeCoords)
    val latestLongPressMs by rememberUpdatedState(state.settings.longPressDelayMs.toLong().coerceIn(250L, 900L))
    val latestSensitivity by rememberUpdatedState(state.settings.swipeGestureSensitivity.coerceIn(0.5f, 1.8f))
    val latestAllowHorizontalSwipe by rememberUpdatedState(allowHorizontalSwipe)
    val latestVertScrollable by rememberUpdatedState(page.verticallyScrollable)

    var activeGuideX by remember { mutableStateOf<Float?>(null) }
    var activeGuideY by remember { mutableStateOf<Float?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = (page.paddingDp + state.settings.homePaddingHorizontalDp).coerceIn(0f, 64f).dp,
                vertical = (page.paddingDp + state.settings.homePaddingVerticalDp).coerceIn(0f, 64f).dp,
            ),
    ) {
        val viewportWidthDp = maxWidth
        val viewportHeightDp = maxHeight
        val canvasWidthPx = with(density) { viewportWidthDp.toPx().coerceAtLeast(1f) }
        val canvasHeightPx = with(density) { viewportHeightDp.toPx().coerceAtLeast(1f) }
        val snapStepPx = with(density) { state.settings.gridSnapStepDp.dp.toPx().coerceAtLeast(8f) }

        val scrollContainerMod = if (page.verticallyScrollable && !state.isEditMode) {
            Modifier.verticalScroll(rememberScrollState())
        } else Modifier
        val contentHeightDp = if (page.verticallyScrollable && !state.isEditMode) {
            val maxElemBottomDp = elements.maxOfOrNull { el ->
                val yf = if (useLandscapeCoords && el.landscapeYFraction != null) el.landscapeYFraction else el.yFraction
                val eh = if (useLandscapeCoords && el.landscapeHeightDp != null) el.landscapeHeightDp else el.heightDp
                (yf * viewportHeightDp.value) + eh + 32f
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
                .onGloballyPositioned { coords ->
                    if (isCurrentPage) {
                        dragCoordinator?.updateCanvasBounds(coords.boundsInRoot())
                    }
                }
                .pointerInput(page.pageId, canvasWidthPx, canvasHeightPx) {
                    val touchSlopPx = viewConfiguration.touchSlop
                    fun hitsAnyElement(offset: Offset): Boolean = latestElements.any { el ->
                        val xf = if (latestUseLandscape && el.landscapeXFraction != null) el.landscapeXFraction else el.xFraction
                        val yf = if (latestUseLandscape && el.landscapeYFraction != null) el.landscapeYFraction else el.yFraction
                        val ew = if (latestUseLandscape && el.landscapeWidthDp != null) el.landscapeWidthDp else el.widthDp
                        val eh = if (latestUseLandscape && el.landscapeHeightDp != null) el.landscapeHeightDp else el.heightDp
                        val left = xf * canvasWidthPx
                        val top = yf * canvasHeightPx
                        val wPx = with(density) { ew.dp.toPx() }
                        val hPx = with(density) { eh.dp.toPx() }
                        offset.x in left..(left + wPx) && offset.y in top..(top + hPx)
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downPos = down.position
                        var currentTimeMs = down.uptimeMillis
                        val hitElem = hitsAnyElement(downPos)
                        val swipeThresholdPx = (48f / latestSensitivity).dp.toPx().coerceAtLeast(24f)
                        var completed = false
                        var isTapUp = false

                        while (true) {
                            val elapsed = (currentTimeMs - down.uptimeMillis).coerceAtLeast(0L)
                            val remainingMs = (latestLongPressMs - elapsed).coerceAtLeast(1L)
                            val event = withTimeoutOrNull(remainingMs) { awaitPointerEvent() }
                            if (event == null) {
                                if (!hitElem) {
                                    if (!latestIsEditMode) {
                                        onCanvasLongPress()
                                    }
                                    completed = true
                                }
                                break
                            }
                            val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                            if (change != null) {
                                currentTimeMs = change.uptimeMillis
                            }
                            if (change == null || !change.pressed) {
                                val upPos = change?.position ?: downPos
                                val dx = upPos.x - downPos.x
                                val dy = upPos.y - downPos.y
                                isTapUp = kotlin.math.hypot(dx, dy) <= touchSlopPx
                                break
                            }
                            if (!latestIsEditMode) {
                                val dx = change.position.x - downPos.x
                                val dy = change.position.y - downPos.y
                                val isVertDominant = abs(dy) >= abs(dx)
                                if (isVertDominant && !latestVertScrollable && abs(dy) >= swipeThresholdPx) {
                                    change.consume()
                                    onVerticalSwipe(dy < 0f)
                                    while (true) {
                                        val next = awaitPointerEvent()
                                        next.changes.forEach { it.consume() }
                                        if (next.changes.none { it.pressed }) break
                                    }
                                    completed = true
                                    break
                                } else if (!isVertDominant && latestAllowHorizontalSwipe && abs(dx) >= swipeThresholdPx) {
                                    change.consume()
                                    onHorizontalSwipe(dx < 0f)
                                    while (true) {
                                        val next = awaitPointerEvent()
                                        next.changes.forEach { it.consume() }
                                        if (next.changes.none { it.pressed }) break
                                    }
                                    completed = true
                                    break
                                }
                            }
                        }

                        if (!completed && isTapUp && !hitElem) {
                            if (latestIsEditMode) {
                                onCanvasTap()
                            } else {
                                val secondDown = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
                                    awaitFirstDown(requireUnconsumed = false)
                                }
                                if (secondDown != null) {
                                    var isSecondTap = false
                                    while (true) {
                                        val ev2 = awaitPointerEvent()
                                        val ch2 = ev2.changes.firstOrNull { it.id == secondDown.id } ?: ev2.changes.firstOrNull()
                                        if (ch2 == null || !ch2.pressed) {
                                            val d2 = ch2?.let {
                                                kotlin.math.hypot(it.position.x - secondDown.position.x, it.position.y - secondDown.position.y)
                                            } ?: 0f
                                            if (d2 <= touchSlopPx) isSecondTap = true
                                            break
                                        }
                                    }
                                    if (isSecondTap) onCanvasDoubleTap() else onCanvasTap()
                                } else {
                                    onCanvasTap()
                                }
                            }
                        } else if (completed) {
                            while (currentEvent.changes.any { it.pressed }) {
                                awaitPointerEvent()
                            }
                        }
                    }
                },
        ) {
            if (isCurrentPage) {
                val dragSession = dragCoordinator?.session
                if (dragSession != null && dragSession.isRevealingDesktop) {
                    val (dropFx, dropFy) = dragCoordinator.computeDropFractions(dragSession.pointerRootPos)
                    val dropXPx = (dropFx * canvasWidthPx).roundToInt()
                    val dropYPx = (dropFy * canvasHeightPx).roundToInt()
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(dropXPx, dropYPx) }
                            .size(width = 84.dp, height = 92.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
                            .testTag("home_drop_preview_slot"),
                    )
                }
            }

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

                    activeGuideX?.let { gx ->
                        drawLine(guideColor, Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 2.5f)
                    }
                    activeGuideY?.let { gy ->
                        drawLine(guideColor, Offset(0f, gy), Offset(size.width, gy), strokeWidth = 2.5f)
                    }
                }
            }

            elements.forEach { elem ->
                EditableCanvasElementBox(
                    elem = elem,
                    page = page,
                    allPageElements = elements,
                    canvasWidthPx = canvasWidthPx,
                    canvasHeightPx = canvasHeightPx,
                    snapStepPx = snapStepPx,
                    isLandscape = isLandscape,
                    state = state,
                    multiSelectActive = multiSelectActive,
                    appDiscovery = appDiscovery,
                    widgetHost = widgetHost,
                    onGuideChanged = { gx, gy ->
                        activeGuideX = gx
                        activeGuideY = gy
                    },
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
                    onCycleStack = onCycleStack,
                    onSelectElement = onSelectElement,
                    onInspectElement = onInspectElement,
                    onSaveElement = onSaveElement,
                )
            }
        }
        }
    }
}

@Composable
private fun PinnedElementsOverlay(
    page: HomePageEntity,
    elements: List<HomeElementEntity>,
    state: LauncherUiState,
    multiSelectActive: Boolean,
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
    onOpenSearch: () -> Unit,
    onJumpToPage: (Int) -> Unit,
    onCycleStack: (Long, Int) -> Unit,
    onSelectElement: (Long?) -> Unit,
    onInspectElement: (HomeElementEntity) -> Unit,
    onSaveElement: (HomeElementEntity, Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(page.paddingDp.coerceIn(0f, 40f).dp)
            .testTag("home_pinned_overlay"),
    ) {
        val canvasWidthPx = with(density) { maxWidth.toPx().coerceAtLeast(1f) }
        val canvasHeightPx = with(density) { maxHeight.toPx().coerceAtLeast(1f) }
        val snapStepPx = with(density) { state.settings.gridSnapStepDp.dp.toPx().coerceAtLeast(8f) }

        elements.forEach { elem ->
            EditableCanvasElementBox(
                elem = elem,
                page = page,
                allPageElements = elements,
                canvasWidthPx = canvasWidthPx,
                canvasHeightPx = canvasHeightPx,
                snapStepPx = snapStepPx,
                isLandscape = isLandscape,
                state = state,
                multiSelectActive = multiSelectActive,
                appDiscovery = appDiscovery,
                widgetHost = widgetHost,
                onGuideChanged = { _, _ -> },
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
                onCycleStack = onCycleStack,
                onSelectElement = onSelectElement,
                onInspectElement = onInspectElement,
                onSaveElement = onSaveElement,
            )
        }
    }
}

@Composable
private fun EditableCanvasElementBox(
    elem: HomeElementEntity,
    page: HomePageEntity,
    allPageElements: List<HomeElementEntity>,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    snapStepPx: Float,
    isLandscape: Boolean,
    state: LauncherUiState,
    multiSelectActive: Boolean,
    appDiscovery: AppDiscoveryManager,
    widgetHost: LauncherWidgetHost,
    onGuideChanged: (Float?, Float?) -> Unit,
    onLaunchApp: (AppEntity) -> Unit,
    onOpenTagSidebar: (Long) -> Unit,
    onOpenCategorySidebar: (Long) -> Unit,
    onOpenRuleSidebar: (Long) -> Unit,
    onOpenSidebar: (Long) -> Unit,
    onOpenWindow: (Long) -> Unit,
    onOpenFolder: (Long) -> Unit,
    onElementTrigger: (HomeElementEntity, String) -> Boolean,
    onOpenSearch: () -> Unit,
    onJumpToPage: (Int) -> Unit,
    onCycleStack: (Long, Int) -> Unit,
    onSelectElement: (Long?) -> Unit,
    onInspectElement: (HomeElementEntity) -> Unit,
    onSaveElement: (HomeElementEntity, Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val latestElem by rememberUpdatedState(elem)
    val latestElements by rememberUpdatedState(allPageElements)
    val useLandscapeCoords = isLandscape && state.settings.landscapeSeparateLayout
    val latestUseLandscape by rememberUpdatedState(useLandscapeCoords)

    val selected = state.isEditMode &&
        (state.selectedElementId == elem.elementId || elem.elementId in state.multiSelectedIds)
    val baseXFrac = if (useLandscapeCoords && elem.landscapeXFraction != null) elem.landscapeXFraction else elem.xFraction
    val baseYFrac = if (useLandscapeCoords && elem.landscapeYFraction != null) elem.landscapeYFraction else elem.yFraction
    val baseWidthDp = if (useLandscapeCoords && elem.landscapeWidthDp != null) elem.landscapeWidthDp else elem.widthDp
    val baseHeightDp = if (useLandscapeCoords && elem.landscapeHeightDp != null) elem.landscapeHeightDp else elem.heightDp

    var dragX by remember(elem.elementId, baseXFrac, canvasWidthPx) { mutableFloatStateOf(baseXFrac * canvasWidthPx) }
    var dragY by remember(elem.elementId, baseYFrac, canvasHeightPx) { mutableFloatStateOf(baseYFrac * canvasHeightPx) }
    var widthDp by remember(elem.elementId, baseWidthDp) { mutableFloatStateOf(baseWidthDp) }
    var heightDp by remember(elem.elementId, baseHeightDp) { mutableFloatStateOf(baseHeightDp) }
    var isDraggingOrResizing by remember { mutableStateOf(false) }

    val canMove = state.isEditMode && !page.isLocked && !elem.locked && !state.settings.layoutLocked
    val dragModifier = if (canMove) {
        Modifier.pointerInput(elem.elementId, canvasWidthPx, canvasHeightPx, state.settings.gridSnapEnabled, state.settings.alignmentGuidesEnabled, state.settings.allowOverlappingElements) {
            detectDragGestures(
                onDragStart = {
                    isDraggingOrResizing = true
                    onSelectElement(latestElem.elementId)
                },
                onDragCancel = {
                    isDraggingOrResizing = false
                    onGuideChanged(null, null)
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    var nextX = (dragX + dragAmount.x).coerceIn(0f, (canvasWidthPx - 40f).coerceAtLeast(0f))
                    var nextY = (dragY + dragAmount.y).coerceIn(0f, (canvasHeightPx - 40f).coerceAtLeast(0f))

                    if (state.settings.gridSnapEnabled) {
                        nextX = (nextX / snapStepPx).roundToInt() * snapStepPx
                        nextY = (nextY / snapStepPx).roundToInt() * snapStepPx
                    }

                    var gx: Float? = null
                    var gy: Float? = null
                    if (state.settings.alignmentGuidesEnabled) {
                        val centerCanvasX = canvasWidthPx / 2f
                        val elemW = widthDp.dp.toPx()
                        val matchX = latestElements.filter { it.elementId != latestElem.elementId }
                            .map { (if (latestUseLandscape && it.landscapeXFraction != null) it.landscapeXFraction else it.xFraction) * canvasWidthPx }
                            .plus(centerCanvasX - elemW / 2f)
                            .firstOrNull { abs(it - nextX) < 12f }
                        if (matchX != null) {
                            nextX = matchX
                            gx = matchX
                        }

                        val matchY = latestElements.filter { it.elementId != latestElem.elementId }
                            .map { (if (latestUseLandscape && it.landscapeYFraction != null) it.landscapeYFraction else it.yFraction) * canvasHeightPx }
                            .firstOrNull { abs(it - nextY) < 12f }
                        if (matchY != null) {
                            nextY = matchY
                            gy = matchY
                        }
                    }
                    onGuideChanged(gx, gy)
                    dragX = nextX
                    dragY = nextY
                },
                onDragEnd = {
                    isDraggingOrResizing = false
                    onGuideChanged(null, null)
                    if (!state.settings.allowOverlappingElements) {
                        val myW = with(density) { widthDp.dp.toPx() }
                        val myH = with(density) { heightDp.dp.toPx() }
                        val maxX = (canvasWidthPx - myW).coerceAtLeast(0f)
                        val maxY = (canvasHeightPx - myH).coerceAtLeast(0f)
                        latestElements.filter { it.elementId != latestElem.elementId }.forEach { other ->
                            val ox = (if (latestUseLandscape && other.landscapeXFraction != null) other.landscapeXFraction else other.xFraction) * canvasWidthPx
                            val oy = (if (latestUseLandscape && other.landscapeYFraction != null) other.landscapeYFraction else other.yFraction) * canvasHeightPx
                            val ow = with(density) { (if (latestUseLandscape && other.landscapeWidthDp != null) other.landscapeWidthDp else other.widthDp).dp.toPx() }
                            val oh = with(density) { (if (latestUseLandscape && other.landscapeHeightDp != null) other.landscapeHeightDp else other.heightDp).dp.toPx() }
                            val overlapX = dragX < ox + ow && dragX + myW > ox
                            val overlapY = dragY < oy + oh && dragY + myH > oy
                            if (overlapX && overlapY) {
                                val shiftedRight = (ox + ow + 8f)
                                val shiftedDown = (oy + oh + 8f)
                                if (shiftedRight <= maxX) {
                                    dragX = shiftedRight
                                } else if (shiftedDown <= maxY) {
                                    dragY = shiftedDown
                                } else {
                                    dragY = (oy - myH - 8f).coerceIn(0f, maxY)
                                }
                            }
                        }
                    }
                    val fx = (dragX / canvasWidthPx).coerceIn(0f, 0.92f)
                    val fy = (dragY / canvasHeightPx).coerceIn(0f, 0.92f)
                    val cur = latestElem
                    val updated = if (latestUseLandscape) {
                        cur.copy(landscapeXFraction = fx, landscapeYFraction = fy, landscapeWidthDp = widthDp, landscapeHeightDp = heightDp)
                    } else {
                        cur.copy(xFraction = fx, yFraction = fy, widthDp = widthDp, heightDp = heightDp)
                    }
                    onSaveElement(updated, true)
                },
            )
        }
    } else Modifier

    Box(
        modifier = Modifier
            .offset { IntOffset(dragX.roundToInt(), dragY.roundToInt()) }
            .size(width = widthDp.dp, height = heightDp.dp)
            .graphicsLayer {
                rotationZ = elem.rotationDeg
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
            .testTag("home_element_${elem.elementId}")
            .then(if (!state.isEditMode) Modifier.semantics(mergeDescendants = true) {} else Modifier),
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
            onOpenSearch = onOpenSearch,
            onJumpToPage = onJumpToPage,
            onSaveElement = onSaveElement,
            onCycleStack = onCycleStack,
        )

        if (state.isEditMode) {
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
            if (selected || isDraggingOrResizing) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.82f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(3.dp)
                        .testTag("element_coordinate_hud_${elem.elementId}")
                        .semantics(mergeDescendants = true) {},
                ) {
                    val xPct = ((dragX / canvasWidthPx) * 100f).roundToInt().coerceIn(0, 100)
                    val yPct = ((dragY / canvasHeightPx) * 100f).roundToInt().coerceIn(0, 100)
                    Text(
                        text = "${xPct}%,${yPct}% · ${widthDp.roundToInt()}×${heightDp.roundToInt()}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    )
                }
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
                            .pointerInput(elem.elementId, canvasWidthPx, canvasHeightPx, state.settings.gridSnapEnabled, state.settings.gridSnapStepDp) {
                                detectDragGestures(
                                    onDragStart = { isDraggingOrResizing = true },
                                    onDragCancel = { isDraggingOrResizing = false },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        val dxDp = with(density) { dragAmount.x.toDp().value }
                                        val dyDp = with(density) { dragAmount.y.toDp().value }
                                        var nextW = (widthDp + dxDp).coerceIn(48f, 380f)
                                        var nextH = (heightDp + dyDp).coerceIn(48f, 440f)
                                        if (state.settings.gridSnapEnabled) {
                                            val stepDp = state.settings.gridSnapStepDp.toFloat().coerceAtLeast(8f)
                                            nextW = ((nextW / stepDp).roundToInt() * stepDp).coerceIn(48f, 380f)
                                            nextH = ((nextH / stepDp).roundToInt() * stepDp).coerceIn(48f, 440f)
                                        }
                                        widthDp = nextW
                                        heightDp = nextH
                                    },
                                    onDragEnd = {
                                        isDraggingOrResizing = false
                                        val fx = (dragX / canvasWidthPx).coerceIn(0f, 0.92f)
                                        val fy = (dragY / canvasHeightPx).coerceIn(0f, 0.92f)
                                        val cur = latestElem
                                        val updated = if (latestUseLandscape) {
                                            cur.copy(landscapeXFraction = fx, landscapeYFraction = fy, landscapeWidthDp = widthDp, landscapeHeightDp = heightDp)
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
