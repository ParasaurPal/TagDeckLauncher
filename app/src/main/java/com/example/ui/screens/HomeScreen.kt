package com.example.ui.screens

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.db.AppEntity
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.LayoutSnapshotEntity
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.InstalledWidgetProvider
import com.example.data.system.LauncherWidgetHost
import com.example.ui.components.TagBadgeIcon
import com.example.ui.components.WallpaperBackground
import com.example.ui.components.WidgetPickerDialog
import com.example.ui.state.LauncherRoute
import com.example.ui.state.LauncherUiState
import kotlinx.coroutines.launch
import kotlin.math.abs
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
    onStartSystemWidgetBind: (InstalledWidgetProvider, Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val visiblePages = remember(state.homePages, state.isEditMode) {
        val list = if (state.isEditMode) state.homePages else state.homePages.filter { !it.isHidden }
        list.ifEmpty { listOf(HomePageEntity(pageId = 1L, title = "Main Deck", pageOrder = 0)) }
    }
    val pagerState = rememberPagerState(initialPage = state.currentPageIndex.coerceIn(0, visiblePages.lastIndex)) {
        visiblePages.size
    }

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    LaunchedEffect(state.currentPageIndex, visiblePages.size) {
        val target = state.currentPageIndex.coerceIn(0, visiblePages.lastIndex)
        if (pagerState.currentPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }

    var showAddElementDialog by remember { mutableStateOf(false) }
    var showPagesDialog by remember { mutableStateOf(false) }
    var showWidgetPicker by remember { mutableStateOf(false) }
    var inspectingElement by remember { mutableStateOf<HomeElementEntity?>(null) }
    var multiSelectActive by remember { mutableStateOf(false) }

    val currentPage = visiblePages.getOrElse(pagerState.currentPage) { visiblePages.first() }
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
                        var twoFingerStartY = 0f
                        var trackingTwoFinger = false
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val pressed = event.changes.filter { it.pressed }
                                if (pressed.size == 2) {
                                    val avgY = (pressed[0].position.y + pressed[1].position.y) / 2f
                                    if (!trackingTwoFinger) {
                                        trackingTwoFinger = true
                                        twoFingerStartY = avgY
                                    } else {
                                        val dy = avgY - twoFingerStartY
                                        if (dy < -110f) {
                                            trackingTwoFinger = false
                                            onTriggerGesture("TWO_FINGER_SWIPE_UP") { target ->
                                                scope.launch { pagerState.animateScrollToPage(target) }
                                            }
                                        } else if (dy > 110f) {
                                            trackingTwoFinger = false
                                            onTriggerGesture("TWO_FINGER_SWIPE_DOWN") { target ->
                                                scope.launch { pagerState.animateScrollToPage(target) }
                                            }
                                        }
                                    }
                                } else {
                                    trackingTwoFinger = false
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
                        canUndo = state.canUndo,
                        canRedo = state.canRedo,
                        hasClipboard = state.clipboardElement != null,
                        gridSnap = state.settings.gridSnapEnabled,
                        multiSelectCount = state.multiSelectedIds.size,
                        multiSelectMode = multiSelectActive,
                        onUndo = onUndo,
                        onRedo = onRedo,
                        onToggleGridSnap = onToggleGridSnap,
                        onToggleMultiSelectMode = { multiSelectActive = !multiSelectActive },
                        onDeleteMulti = onDeleteMultiSelected,
                        onLockMulti = onLockMultiSelected,
                        onPaste = { onPasteElement(currentPage.pageId) },
                        onAddElement = { showAddElementDialog = true },
                        onOpenPages = { showPagesDialog = true },
                        onOpenAppearance = { onNavigate(LauncherRoute.Settings) },
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
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
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
                        userScrollEnabled = !state.isEditMode,
                        modifier = Modifier.fillMaxSize(),
                    ) { pageIdx ->
                        val page = visiblePages[pageIdx]
                        val elements = remember(state.homeElements, page.pageId) {
                            state.homeElements
                                .filter { it.ownerType == "PAGE" && it.pageId == page.pageId && it.visible }
                                .sortedBy { it.zIndex }
                        }
                        FreeformPageCanvas(
                            page = page,
                            elements = elements,
                            state = state,
                            multiSelectActive = multiSelectActive,
                            appDiscovery = appDiscovery,
                            widgetHost = widgetHost,
                            onCanvasTap = {
                                if (state.isEditMode) {
                                    onSelectElement(null)
                                } else {
                                    onTriggerGesture("SINGLE_TAP") { target ->
                                        scope.launch { pagerState.animateScrollToPage(target) }
                                    }
                                }
                            },
                            onCanvasDoubleTap = {
                                if (!state.isEditMode) {
                                    onTriggerGesture("DOUBLE_TAP") { target ->
                                        scope.launch { pagerState.animateScrollToPage(target) }
                                    }
                                }
                            },
                            onCanvasLongPress = {
                                if (!state.isEditMode) {
                                    onTriggerGesture("LONG_PRESS") { target ->
                                        scope.launch { pagerState.animateScrollToPage(target) }
                                    }
                                }
                            },
                            onVerticalSwipe = { isUp ->
                                if (!state.isEditMode) {
                                    onTriggerGesture(if (isUp) "SWIPE_UP" else "SWIPE_DOWN") { target ->
                                        scope.launch { pagerState.animateScrollToPage(target) }
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
                                    scope.launch { pagerState.animateScrollToPage(target) }
                                }
                            },
                            onSelectElement = { id ->
                                if (multiSelectActive && id != null) onToggleMultiSelect(id) else onSelectElement(id)
                            },
                            onInspectElement = { inspectingElement = it },
                            onSaveElement = onSaveElement,
                        )
                    }

                    // 4-Edge Activation Zones (Left / Right / Top / Bottom)
                    if (!state.isEditMode && leftEdgeSidebar != null) {
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
                            if (leftEdgeSidebar.showHandle) {
                                Box(
                                    modifier = Modifier.width(4.dp).height(64.dp).clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                                )
                            }
                        }
                    }

                    if (!state.isEditMode && rightEdgeSidebar != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(20.dp)
                                .fillMaxHeight(0.65f)
                                .pointerInput(rightEdgeSidebar.sidebarId) {
                                    detectHorizontalDragGestures { _, dragAmount ->
                                        if (dragAmount < -14f) onOpenConfiguredSidebar(rightEdgeSidebar.sidebarId)
                                    }
                                }
                                .clickable { onOpenConfiguredSidebar(rightEdgeSidebar.sidebarId) },
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            if (rightEdgeSidebar.showHandle) {
                                Box(
                                    modifier = Modifier.width(4.dp).height(64.dp).clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f))
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
                            if (topEdgeSidebar.showHandle) {
                                Box(
                                    modifier = Modifier.height(4.dp).width(64.dp).clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
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
                            if (bottomEdgeSidebar.showHandle) {
                                Box(
                                    modifier = Modifier.height(4.dp).width(64.dp).clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f))
                                )
                            }
                        }
                    }
                }

                if (visiblePages.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        visiblePages.forEachIndexed { idx, _ ->
                            val active = idx == pagerState.currentPage
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (active) 10.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(if (active) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f))
                            )
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

    if (showWidgetPicker) {
        val providers = remember { widgetHost.getInstalledProviders() }
        WidgetPickerDialog(
            providers = providers,
            tags = state.allTags,
            onPickSystemWidget = { provider ->
                showWidgetPicker = false
                onStartSystemWidgetBind(provider, currentPage.pageId)
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

    inspectingElement?.let { elem ->
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
        )
    }
}

@Composable
private fun DockActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tag: String,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(21.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StudioEditToolbar(
    pageTitle: String,
    canUndo: Boolean,
    canRedo: Boolean,
    hasClipboard: Boolean,
    gridSnap: Boolean,
    multiSelectCount: Int,
    multiSelectMode: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleGridSnap: () -> Unit,
    onToggleMultiSelectMode: () -> Unit,
    onDeleteMulti: () -> Unit,
    onLockMulti: () -> Unit,
    onPaste: () -> Unit,
    onAddElement: () -> Unit,
    onOpenPages: () -> Unit,
    onOpenAppearance: () -> Unit,
    onDone: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("studio_edit_toolbar"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
        tonalElevation = 10.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Studio • $pageTitle",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                IconButton(onClick = onUndo, enabled = canUndo) {
                    Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = "Undo")
                }
                IconButton(onClick = onRedo, enabled = canRedo) {
                    Icon(Icons.AutoMirrored.Rounded.Redo, contentDescription = "Redo")
                }
                IconButton(onClick = onAddElement, modifier = Modifier.testTag("studio_add_element")) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add element")
                }
                IconButton(onClick = onToggleGridSnap) {
                    Icon(
                        Icons.Rounded.GridOn,
                        contentDescription = "Toggle grid snap",
                        tint = if (gridSnap) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onToggleMultiSelectMode) {
                    Icon(
                        Icons.Rounded.SelectAll,
                        contentDescription = "Multi-select mode",
                        tint = if (multiSelectMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (hasClipboard) {
                    IconButton(onClick = onPaste) {
                        Icon(Icons.Rounded.ContentPaste, contentDescription = "Paste")
                    }
                }
                IconButton(onClick = onOpenPages, modifier = Modifier.testTag("studio_pages_button")) {
                    Icon(Icons.Rounded.Layers, contentDescription = "Pages and snapshots")
                }
                IconButton(onClick = onOpenAppearance) {
                    Icon(Icons.Rounded.Tune, contentDescription = "Settings")
                }
                IconButton(onClick = onDone, modifier = Modifier.testTag("studio_done_button")) {
                    Icon(Icons.Rounded.Check, contentDescription = "Done editing", tint = MaterialTheme.colorScheme.primary)
                }
            }
            if (multiSelectCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("$multiSelectCount selected", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    AssistChip(onClick = onLockMulti, leadingIcon = { Icon(Icons.Rounded.Lock, null, Modifier.size(16.dp)) }, label = { Text("Lock") })
                    AssistChip(onClick = onDeleteMulti, leadingIcon = { Icon(Icons.Rounded.Delete, null, Modifier.size(16.dp)) }, label = { Text("Delete") })
                }
            }
        }
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

    var activeGuideX by remember { mutableStateOf<Float?>(null) }
    var activeGuideY by remember { mutableStateOf<Float?>(null) }

    val scrollMod = if (page.verticallyScrollable && !state.isEditMode) {
        Modifier.verticalScroll(rememberScrollState())
    } else Modifier

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .then(scrollMod)
            .pointerInput(state.isEditMode) {
                detectTapGestures(
                    onTap = { onCanvasTap() },
                    onDoubleTap = { onCanvasDoubleTap() },
                    onLongPress = { onCanvasLongPress() },
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
        val canvasWidthPx = with(density) { maxWidth.toPx().coerceAtLeast(1f) }
        val canvasHeightPx = with(density) { maxHeight.toPx().coerceAtLeast(1f) }
        val snapStepPx = with(density) { state.settings.gridSnapStepDp.dp.toPx().coerceAtLeast(8f) }

        if (state.isEditMode) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = (state.settings.gridSnapStepDp.coerceAtLeast(12) * 2).dp.toPx()
                var x = step
                while (x < size.width) {
                    drawLine(Color.White.copy(alpha = 0.08f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    x += step
                }
                var y = step
                while (y < size.height) {
                    drawLine(Color.White.copy(alpha = 0.08f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += step
                }

                // Magnetic alignment guides during drag
                activeGuideX?.let { gx ->
                    drawLine(Color(0xFF38BDF8L), Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 2.5f)
                }
                activeGuideY?.let { gy ->
                    drawLine(Color(0xFF38BDF8L), Offset(0f, gy), Offset(size.width, gy), strokeWidth = 2.5f)
                }
            }
        }

        elements.forEach { elem ->
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
                        onDragStart = { onSelectElement(elem.elementId) },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            var nextX = (dragX + dragAmount.x).coerceIn(0f, canvasWidthPx - 40f)
                            var nextY = (dragY + dragAmount.y).coerceIn(0f, canvasHeightPx - 40f)

                            if (state.settings.gridSnapEnabled) {
                                nextX = (nextX / snapStepPx).roundToInt() * snapStepPx
                                nextY = (nextY / snapStepPx).roundToInt() * snapStepPx
                            }

                            if (state.settings.alignmentGuidesEnabled) {
                                val centerCanvasX = canvasWidthPx / 2f
                                val elemW = widthDp.dp.toPx()
                                val matchX = elements.filter { it.elementId != elem.elementId }
                                    .map { (if (isLandscape && it.landscapeXFraction != null) it.landscapeXFraction else it.xFraction) * canvasWidthPx }
                                    .plus(centerCanvasX - elemW / 2f)
                                    .firstOrNull { abs(it - nextX) < 12f }
                                if (matchX != null) {
                                    nextX = matchX
                                    activeGuideX = matchX
                                } else activeGuideX = null

                                val matchY = elements.filter { it.elementId != elem.elementId }
                                    .map { (if (isLandscape && it.landscapeYFraction != null) it.landscapeYFraction else it.yFraction) * canvasHeightPx }
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
                            val updated = if (isLandscape) {
                                elem.copy(landscapeXFraction = fx, landscapeYFraction = fy, widthDp = widthDp, heightDp = heightDp)
                            } else {
                                elem.copy(xFraction = fx, yFraction = fy, widthDp = widthDp, heightDp = heightDp)
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
                    .then(dragModifier)
                    .then(
                        if (state.isEditMode) {
                            Modifier
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(elem.cornerRadiusDp.dp),
                                )
                                .clickable {
                                    if (selected && !multiSelectActive) onInspectElement(elem) else onSelectElement(elem.elementId)
                                }
                        } else Modifier
                    ),
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
                )

                if (state.isEditMode) {
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
                                .size(24.dp)
                                .clickable { onInspectElement(elem) },
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Tune, contentDescription = "Inspect", tint = Color.Black, modifier = Modifier.size(14.dp))
                            }
                        }

                        if (canMove) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 6.dp, y = 6.dp)
                                    .size(26.dp)
                                    .pointerInput(elem.elementId) {
                                        detectDragGestures(
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val dxDp = with(density) { dragAmount.x.toDp().value }
                                                val dyDp = with(density) { dragAmount.y.toDp().value }
                                                widthDp = (widthDp + dxDp).coerceIn(48f, 380f)
                                                heightDp = (heightDp + dyDp).coerceIn(48f, 440f)
                                            },
                                            onDragEnd = {
                                                onSaveElement(elem.copy(widthDp = widthDp, heightDp = heightDp), true)
                                            },
                                        )
                                    },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.OpenInFull, contentDescription = "Resize", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
