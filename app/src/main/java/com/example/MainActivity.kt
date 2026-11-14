package com.example

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.db.AppEntity
import com.example.data.db.CategoryEntity
import com.example.data.db.HomeElementEntity
import com.example.data.db.TagEntity
import com.example.data.db.SidebarConfigEntity
import com.example.data.system.InstalledWidgetProvider
import com.example.data.system.LineIconProfile
import com.example.ui.components.ActiveOverlayHost
import com.example.ui.components.AppActionAndCustomizeDialog
import com.example.ui.components.AppDrawerOverlay
import com.example.ui.components.BulkAssignAppsToTagDialog
import com.example.ui.components.CategoryEditorDialog
import com.example.ui.components.LocalIconPackOrder
import com.example.ui.components.LocalNotificationBadgesEnabled
import com.example.ui.components.LocalLineIconProfiles
import com.example.ui.components.LocalDefaultLineIconProfile
import com.example.ui.components.LineIconStudioDialog
import com.example.ui.components.LocalIconStyleConfig
import com.example.ui.components.TagEditorDialog
import com.example.ui.components.UniversalSearchOverlay
import com.example.ui.screens.AppManagerScreen
import com.example.ui.screens.HomeElementBody
import com.example.ui.screens.ElementInspectorDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SidebarManagerScreen
import com.example.ui.screens.TagManagerScreen
import com.example.ui.state.LauncherRoute
import com.example.ui.state.LauncherViewModel
import com.example.ui.theme.TagDeckTheme

class MainActivity : ComponentActivity() {

    private val appContainer: TagDeckApplication
        get() = application as TagDeckApplication

    private val viewModel: LauncherViewModel by viewModels {
        LauncherViewModel.Factory(
            repository = appContainer.launcherRepository,
            appDiscovery = appContainer.appDiscoveryManager,
            imageStorage = appContainer.imageStorageManager,
            widgetHost = appContainer.widgetHost,
        )
    }

    private var navControllerRef: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val navController = rememberNavController()
            navControllerRef = navController

            val view = LocalView.current
            LaunchedEffect(state.settings.wallpaperType) {
                val activityWindow = (view.context as? Activity)?.window ?: return@LaunchedEffect
                if (state.settings.wallpaperType == "SYSTEM_LIVE") {
                    activityWindow.setFormat(PixelFormat.TRANSLUCENT)
                    activityWindow.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
                    activityWindow.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                    activityWindow.decorView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                } else {
                    activityWindow.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
                    activityWindow.setFormat(PixelFormat.OPAQUE)
                    activityWindow.setBackgroundDrawable(null)
                }
            }
            LaunchedEffect(state.settings.showStatusBar, state.settings.showNavBar) {
                val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
                val controller = WindowCompat.getInsetsController(window, view)
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                if (state.settings.showStatusBar) {
                    controller.show(WindowInsetsCompat.Type.statusBars())
                } else {
                    controller.hide(WindowInsetsCompat.Type.statusBars())
                }
                if (state.settings.showNavBar) {
                    controller.show(WindowInsetsCompat.Type.navigationBars())
                } else {
                    controller.hide(WindowInsetsCompat.Type.navigationBars())
                }
            }

            TagDeckTheme(
                themeMode = state.settings.themeMode,
                accentColorHex = state.settings.accentColorHex,
                secondaryColorHex = state.settings.secondaryColorHex,
                tertiaryColorHex = state.settings.tertiaryColorHex,
                fontStyle = state.settings.fontStyle,
                fontScale = state.settings.fontScale,
                cornerRadiusDp = state.settings.globalCornerRadiusDp,
            ) {
                TagDeckRoot(
                    viewModel = viewModel,
                    navController = navController,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appContainer.widgetHost.startListening()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDefaultLauncherStatus()
        viewModel.refreshInstalledApps()
        viewModel.refreshNotificationAccessStatus()
    }

    override fun onStop() {
        super.onStop()
        appContainer.widgetHost.stopListening()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) {
            viewModel.closeOverlay()
            viewModel.setDrawerOpen(false)
            viewModel.setUniversalSearchOpen(false)
            viewModel.setEditMode(false)
            navControllerRef?.popBackStack(LauncherRoute.Home.route, inclusive = false)
        }
    }
}

@Composable
private fun TagDeckRoot(
    viewModel: LauncherViewModel,
    navController: NavHostController,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    var pendingWidgetInfo by remember { mutableStateOf<Triple<InstalledWidgetProvider, Long, Pair<Long?, String>>?>(null) }
    var pendingWidgetId by remember { mutableStateOf<Int?>(null) }

    val saveBoundSystemWidget: (InstalledWidgetProvider, Long, Long?, String, Int) -> Unit = { provider, pageId, windowId, ownerType, widgetId ->
        viewModel.saveHomeElement(
            HomeElementEntity(
                pageId = pageId,
                ownerType = ownerType,
                ownerWindowId = windowId,
                type = "APP_WIDGET",
                xFraction = 0.06f,
                yFraction = 0.12f,
                widthDp = provider.minWidthDp.toFloat().coerceIn(140f, 340f),
                heightDp = provider.minHeightDp.toFloat().coerceIn(110f, 320f),
                label = provider.label,
                appWidgetId = widgetId,
                backgroundColorHex = 0x220F172AL,
                cornerRadiusDp = 22f,
            ),
            selectAfter = true,
        )
    }

    val defaultHomeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshDefaultLauncherStatus()
    }

    val configureWidgetLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val widgetId = pendingWidgetId
        val infoTriple = pendingWidgetInfo
        if (result.resultCode == Activity.RESULT_OK && widgetId != null && infoTriple != null) {
            val (provider, pageId, ownerInfo) = infoTriple
            saveBoundSystemWidget(provider, pageId, ownerInfo.first, ownerInfo.second, widgetId)
        } else if (widgetId != null) {
            viewModel.widgetHost.deleteAppWidgetId(widgetId)
        }
        pendingWidgetId = null
        pendingWidgetInfo = null
    }

    val bindWidgetLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val widgetId = pendingWidgetId
        val infoTriple = pendingWidgetInfo
        if (result.resultCode == Activity.RESULT_OK && widgetId != null && infoTriple != null) {
            val (provider, pageId, ownerInfo) = infoTriple
            val configComp = provider.info.configure
            if (configComp != null) {
                val cfgIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                    component = configComp
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                }
                try {
                    configureWidgetLauncher.launch(cfgIntent)
                } catch (_: Exception) {
                    saveBoundSystemWidget(provider, pageId, ownerInfo.first, ownerInfo.second, widgetId)
                    pendingWidgetId = null
                    pendingWidgetInfo = null
                }
            } else {
                saveBoundSystemWidget(provider, pageId, ownerInfo.first, ownerInfo.second, widgetId)
                pendingWidgetId = null
                pendingWidgetInfo = null
            }
        } else if (widgetId != null) {
            viewModel.widgetHost.deleteAppWidgetId(widgetId)
            pendingWidgetId = null
            pendingWidgetInfo = null
        }
    }

    val startSystemWidgetBind: (InstalledWidgetProvider, Long, Long?, String) -> Unit = { provider, pageId, windowId, ownerType ->
        val allocatedId = viewModel.widgetHost.allocateAppWidgetId()
        if (allocatedId == null) {
            viewModel.showMessage("Widget host unavailable")
        } else {
            pendingWidgetId = allocatedId
            pendingWidgetInfo = Triple(provider, pageId, windowId to ownerType)
            if (viewModel.widgetHost.bindIfAllowed(allocatedId, provider.info.provider)) {
                saveBoundSystemWidget(provider, pageId, windowId, ownerType, allocatedId)
                pendingWidgetId = null
                pendingWidgetInfo = null
            } else {
                bindWidgetLauncher.launch(
                    viewModel.widgetHost.createBindIntent(allocatedId, provider.info.provider)
                )
            }
        }
    }

    var inlineEditTag by remember { mutableStateOf<TagEntity?>(null) }
    var inlineEditCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var inlineAssignAppsTag by remember { mutableStateOf<TagEntity?>(null) }
    var inlineCustomizeApp by remember { mutableStateOf<AppEntity?>(null) }
    var inlineLineIconApp by remember { mutableStateOf<AppEntity?>(null) }

    var inlineAddSidebarButtonCategory by remember { mutableStateOf<CategoryEntity?>(null) }
        var inlineAddSidebarButtonLabel by remember { mutableStateOf("") }
        var inlineInspectSidebarButton by remember { mutableStateOf<HomeElementEntity?>(null) }

        val navigateTo: (LauncherRoute) -> Unit = { route ->
        if (navController.currentDestination?.route != route.route) {
            navController.navigate(route.route) {
                launchSingleTop = true
            }
        }
    }

    val installedWidgetProviders = remember { viewModel.widgetHost.getInstalledProviders() }

    CompositionLocalProvider(
        LocalIconPackOrder provides state.settings.iconPackOrder(),
        LocalIconStyleConfig provides state.settings.iconStyleConfig(),
        LocalNotificationBadgesEnabled provides state.settings.notificationsBadgeHint,
        LocalLineIconProfiles provides remember(state.settings.lineIconProfilesJson) { LineIconProfile.decodeMap(state.settings.lineIconProfilesJson) },
        LocalDefaultLineIconProfile provides if (state.settings.lineIconsEnabled) state.settings.defaultLineIconProfile() else null,
    ) {
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = LauncherRoute.Home.route,
        ) {
            composable(LauncherRoute.Home.route) {
                HomeScreen(
                    state = state,
                    appDiscovery = viewModel.appDiscovery,
                    widgetHost = viewModel.widgetHost,
                    onImportImage = { uri, folder, name -> viewModel.importImagePreservingOriginal(uri, folder, name) },
                    onLaunchApp = { viewModel.launchApp(it) },
                    onOpenTagSidebar = { viewModel.openTagSidebar(it) },
                    onOpenCategorySidebar = { viewModel.openCategorySidebar(it) },
                    onOpenConfiguredSidebar = { viewModel.openConfiguredSidebar(it) },
                    onOpenRuleSidebar = { viewModel.openRuleSidebar(it) },
                    onOpenWindow = { viewModel.openWindow(it) },
                    onOpenFolder = { viewModel.openFolder(it) },
                    onOpenDrawer = { viewModel.setDrawerOpen(true) },
                    onOpenSearch = { viewModel.setUniversalSearchOpen(true) },
                    onNavigate = navigateTo,
                    onToggleEditMode = { viewModel.setEditMode(it) },
                    onSelectElement = { viewModel.selectElement(it) },
                    onToggleMultiSelect = { viewModel.toggleMultiSelectElement(it) },
                    onDeleteMultiSelected = { viewModel.deleteMultiSelected() },
                    onLockMultiSelected = { viewModel.setMultiSelectedLocked(true) },
                    onUndo = { viewModel.undoLayout() },
                    onRedo = { viewModel.redoLayout() },
                    onToggleGridSnap = { viewModel.updateSettings { it.copy(gridSnapEnabled = !it.gridSnapEnabled) } },
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onSaveElement = { elem, sel -> viewModel.saveHomeElement(elem, sel) },
                    onDuplicateElement = { viewModel.duplicateElement(it) },
                    onCopyElement = { viewModel.copyElementToClipboard(it) },
                    onCutElement = { viewModel.cutElementToClipboard(it) },
                    onPasteElement = { pageId -> viewModel.pasteElementFromClipboard(pageId) },
                    onBringForward = { viewModel.bringElementForward(it) },
                    onSendBackward = { viewModel.sendElementBackward(it) },
                    onDeleteElement = { viewModel.deleteHomeElement(it) },
                    onExecuteElementAction = { el, trig, onScrollPage ->
                        viewModel.executeElementActions(el.actionsConfigJson, trig, navigateTo, onScrollPage)
                    },
                    onAddPage = { viewModel.addHomePage(it) },
                    onSavePage = { viewModel.saveHomePage(it) },
                    onDuplicatePage = { viewModel.duplicateHomePage(it) },
                    onReorderPages = { viewModel.reorderHomePages(it) },
                    onDeletePage = { viewModel.deleteHomePage(it) },
                    onSaveSnapshot = { viewModel.saveLayoutSnapshot(it) },
                    onRestoreSnapshot = { viewModel.restoreLayoutSnapshot(it) },
                    onDeleteSnapshot = { viewModel.deleteSnapshot(it) },
                    onPageChanged = { viewModel.setCurrentPageIndex(it) },
                    onTriggerGesture = { gestureType, onScrollPage ->
                        viewModel.triggerGesture(gestureType, navigateTo, onScrollPage)
                    },
                    onRequestSetDefaultHome = {
                        try {
                            defaultHomeLauncher.launch(viewModel.appDiscovery.buildRequestDefaultLauncherIntent())
                        } catch (_: Exception) {
                            viewModel.showMessage("Open device Settings -> Default Apps to select TagDeck")
                        }
                    },
                    onDismissDefaultBanner = {
                        viewModel.updateSettings { it.copy(showDefaultHomeBanner = false) }
                    },
                    onStartSystemWidgetBind = { provider, pageId, ownerType ->
                        startSystemWidgetBind(provider, pageId, null, ownerType)
                    },
                )
            }

            composable(LauncherRoute.Tags.route) {
                TagManagerScreen(
                    state = state,
                    appDiscovery = viewModel.appDiscovery,
                    onBack = { navController.popBackStack() },
                    onOpenAppsManager = { navigateTo(LauncherRoute.Apps) },
                    onOpenTagSidebar = { viewModel.openTagSidebar(it) },
                    onOpenCategorySidebar = { viewModel.openCategorySidebar(it) },
                    onOpenRuleSidebar = { viewModel.openRuleSidebar(it) },
                    onLaunchApp = { viewModel.launchApp(it) },
                    onSaveTag = { viewModel.saveTag(it) },
                    onDeleteTag = { viewModel.deleteTag(it) },
                    onDuplicateTag = { viewModel.duplicateTag(it) },
                    onMergeTags = { src, dst -> viewModel.mergeTags(src, dst) },
                    onMoveTagOrder = { id, delta -> viewModel.moveTagOrder(id, delta) },
                    onReplaceAppsForTag = { id, pkgs -> viewModel.replaceAppsForTag(id, pkgs) },
                    onSaveRule = { viewModel.saveRule(it) },
                    onDeleteRule = { viewModel.deleteRule(it) },
                    onSaveCategory = { viewModel.saveCategory(it) },
                    onDeleteCustomCategory = { viewModel.deleteCustomCategory(it) },
                    onImportImage = { uri, folder, name -> viewModel.importImagePreservingOriginal(uri, folder, name) },
                )
            }

            composable(LauncherRoute.Apps.route) {
                AppManagerScreen(
                    state = state,
                    appDiscovery = viewModel.appDiscovery,
                    onBack = { navController.popBackStack() },
                    onLaunchApp = { viewModel.launchApp(it) },
                    onToggleFavorite = { pkg, fav -> viewModel.setAppFavorite(pkg, fav) },
                    onToggleHidden = { pkg, hid -> viewModel.setAppHidden(pkg, hid) },
                    onSaveCustomName = { pkg, name -> viewModel.setAppCustomName(pkg, name) },
                    onSaveCustomIcon = { pkg, path, s, ox, oy, a -> viewModel.setAppCustomIcon(pkg, path, s, ox, oy, a) },
                    onSaveTagsForApp = { pkg, tagIds -> viewModel.replaceTagsForApp(pkg, tagIds) },
                    onBulkAssignTags = { pkgs, tagIds -> viewModel.bulkAssignTagsToApps(pkgs, tagIds) },
                    onSaveCategoriesForApp = { pkg, prim, all, excl ->
                        viewModel.setManualCategoriesForApp(pkg, prim, all, excl)
                    },
                    onRestoreAutomaticCategory = { viewModel.restoreAutomaticCategoryForApp(it) },
                    onBulkSetCategory = { pkgs, cid -> viewModel.bulkSetCategoryForApps(pkgs, cid) },
                    onMoveAppOrder = { pkg, delta -> viewModel.moveAppManualOrder(pkg, delta) },
                    onChangeSortOrder = { sort -> viewModel.updateSettings { it.copy(drawerSortOrder = sort) } },
                    onOpenAppInfo = { viewModel.openAppInfo(it) },
                    onUninstallApp = { viewModel.requestUninstall(it) },
                    onEditLineIcon = { inlineLineIconApp = it },
                    onToggleAppLock = { packageName, locked ->
                        viewModel.updateSettings { settings ->
                            val packages = settings.lockedAppsCsv.split(",").map(String::trim).filter(String::isNotBlank).toMutableSet()
                            if (locked) packages.add(packageName) else packages.remove(packageName)
                            settings.copy(lockedAppsCsv = packages.sorted().joinToString(","))
                        }
                    },
                    onAddShortcutToHome = { app, shortcutId, shortcutLabel ->
                        val pageId = state.homePages.getOrNull(state.currentPageIndex)?.pageId
                            ?: state.homePages.firstOrNull()?.pageId
                        if (pageId != null) {
                            viewModel.saveHomeElement(
                                HomeElementEntity(
                                    pageId = pageId,
                                    type = "APP_SHORTCUT",
                                    targetPackageName = app.packageName,
                                    label = shortcutLabel,
                                    folderAppPackagesCsv = shortcutId,
                                    xFraction = 0.12f,
                                    yFraction = 0.12f,
                                    widthDp = 88f,
                                    heightDp = 96f,
                                    backgroundColorHex = 0x00000000L,
                                ),
                                selectAfter = false,
                            )
                            viewModel.showMessage("Added $shortcutLabel to the home screen")
                        } else {
                            viewModel.showMessage("Create a home page before adding shortcuts")
                        }
                    },
                    onImportImage = { uri -> viewModel.importImagePreservingOriginal(uri, "Icons") },
                )
            }

            composable(LauncherRoute.Sidebars.route) {
                SidebarManagerScreen(
                    state = state,
                    appDiscovery = viewModel.appDiscovery,
                    onBack = { navController.popBackStack() },
                    onOpenSidebar = { viewModel.openConfiguredSidebar(it) },
                    onSaveSidebar = { viewModel.saveSidebar(it) },
                    onDeleteSidebar = { viewModel.deleteSidebar(it) },
                    onOpenWindow = { viewModel.openWindow(it) },
                    onSaveWindow = { viewModel.saveWindow(it) },
                    onDeleteWindow = { viewModel.deleteWindow(it) },
                    onAddWindowElement = { viewModel.saveHomeElement(it, selectAfter = false) },
                    onDeleteWindowElement = { viewModel.deleteHomeElement(it) },
                    onImportImage = { uri, folder, name -> viewModel.importImagePreservingOriginal(uri, folder, name) },
                    installedWidgetProviders = installedWidgetProviders,
                    onDuplicateWindowElement = { viewModel.duplicateElement(it) },
                    onCopyWindowElement = { viewModel.copyElementToClipboard(it) },
                    onCutWindowElement = { viewModel.cutElementToClipboard(it) },
                    onBringWindowElementForward = { viewModel.bringElementForward(it) },
                    onSendWindowElementBackward = { viewModel.sendElementBackward(it) },
                    onStartSystemWidgetBindForWindow = { provider, pageId, winId ->
                        startSystemWidgetBind(provider, pageId, winId, "WINDOW")
                    },
                )
            }

            composable(LauncherRoute.Settings.route) {
                SettingsScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onNavigate = navigateTo,
                    onRequestDefaultLauncher = {
                        try {
                            defaultHomeLauncher.launch(viewModel.appDiscovery.buildRequestDefaultLauncherIntent())
                        } catch (_: Exception) {
                            viewModel.showMessage("Open device Settings -> Default Apps to select TagDeck")
                        }
                    },
                    onUpdateSettings = { viewModel.updateSettings(it) },
                    onSaveGesture = { viewModel.saveGestureBinding(it) },
                    onImportImage = { uri, folder, name -> viewModel.importImagePreservingOriginal(uri, folder, name) },
                    onRenameImageResource = { res, name, folder -> viewModel.renameImageResource(res, name, folder) },
                    onDeleteImageResource = { res -> viewModel.deleteImageResource(res) },
                    onSaveBackgroundPreset = { preset -> viewModel.saveBackgroundPreset(preset) },
                    onDeleteBackgroundPreset = { id -> viewModel.deleteBackgroundPreset(id) },
                    onExportBackupJson = { viewModel.exportFullBackupJson() },
                    onImportBackupJson = { viewModel.importFullBackupJson(it) },
                    onShowMessage = { viewModel.showMessage(it) },
                    onRefreshIconPacks = { viewModel.refreshInstalledIconPacks() },
                    onAddIconPack = { viewModel.addIconPackToHierarchy(it) },
                    onRemoveIconPack = { viewModel.removeIconPackFromHierarchy(it) },
                    onMoveIconPack = { pkg, delta -> viewModel.moveIconPackInHierarchy(pkg, delta) },
                    initialSearchQuery = state.pendingSettingsSearchQuery,
                    onConsumeInitialSearchQuery = { viewModel.consumePendingSettingsSearchQuery() },
                )
            }
        }

        // Universal All-Apps Sidebar / Drawer with Niagara Wave Index & Bottom Category Dock
        AppDrawerOverlay(
            visible = state.isDrawerOpen,
            state = state,
            appDiscovery = viewModel.appDiscovery,
            onClose = { viewModel.setDrawerOpen(false) },
            onLaunchApp = { viewModel.launchApp(it) },
            onAppLongPress = { inlineCustomizeApp = it },
            onOpenTagSidebar = { viewModel.openTagSidebar(it) },
            onEditCategory = { inlineEditCategory = it },
            onToggleLayout = {
                val next = if (state.settings.drawerLayoutMode == "GRID") "LIST" else "GRID"
                viewModel.updateSettings { it.copy(drawerLayoutMode = next) }
            },
            onCycleSort = {
                val next = when (state.settings.drawerSortOrder) {
                    "ALPHABETICAL" -> "MOST_USED"
                    "MOST_USED" -> "RECENTLY_INSTALLED"
                    "RECENTLY_INSTALLED" -> "MANUAL"
                    else -> "ALPHABETICAL"
                }
                viewModel.updateSettings { it.copy(drawerSortOrder = next) }
                viewModel.showMessage("Sort: $next")
            },
            onUpdateSettings = { viewModel.updateSettings(it) },
            onOpenGlobalSearch = { query -> viewModel.setUniversalSearchOpen(true, initialQuery = query) },
        )

        // Universal Search Overlay
        UniversalSearchOverlay(
            visible = state.isUniversalSearchOpen,
            state = state,
            appDiscovery = viewModel.appDiscovery,
            onClose = { viewModel.setUniversalSearchOpen(false) },
            onLaunchApp = { viewModel.launchApp(it) },
            onOpenTagSidebar = { viewModel.openTagSidebar(it) },
            onOpenCategorySidebar = { viewModel.openCategorySidebar(it) },
            onOpenRuleSidebar = { viewModel.openRuleSidebar(it) },
            onOpenFolder = { viewModel.openFolder(it) },
            onJumpToPage = { idx ->
                viewModel.setCurrentPageIndex(idx)
                viewModel.setUniversalSearchOpen(false)
            },
            onAddHistory = { viewModel.addSearchHistory(it) },
            onClearHistory = { viewModel.clearSearchHistory() },
            onOpenConfiguredSidebar = { viewModel.openConfiguredSidebar(it) },
            onOpenWindow = { viewModel.openWindow(it) },
            onOpenSettingsSearch = { settingsQuery ->
                viewModel.openSettingsWithSearch(settingsQuery, navigateTo)
            },
            onNavigate = navigateTo,
            onOpenDrawer = { viewModel.setDrawerOpen(true) },
            onToggleEditMode = { viewModel.setEditMode(true) },
            onConsumePendingQuery = { viewModel.consumePendingGlobalSearchQuery() },
        )

        // Tag, Play Category, 4-Edge Configured Sidebars, Nested Folders & Independent Windows
        ActiveOverlayHost(
            state = state,
            appDiscovery = viewModel.appDiscovery,
            onDismiss = { viewModel.closeOverlay() },
            onSwitchToTag = { tagId -> viewModel.openTagSidebar(tagId) },
            onSwitchToCategory = { catId -> viewModel.openCategorySidebar(catId) },
            onOpenNestedFolder = { folderId -> viewModel.openFolder(folderId) },
            onLaunchApp = { viewModel.launchApp(it) },
            onEditTag = { inlineEditTag = it },
            onEditCategory = { inlineEditCategory = it },
            onAddCategory = { category ->
                inlineEditCategory = CategoryEntity(
                    name = "",
                    parentCategoryId = category.categoryId,
                    isCustom = true,
                    categoryType = "CUSTOM",
                )
            },
            onAddButton = { category ->
                inlineAddSidebarButtonCategory = category
                inlineAddSidebarButtonLabel = ""
            },
            onClickSidebarButton = { element ->
                viewModel.executeElementActions(element.actionsConfigJson, "TAP", navigateTo) { index ->
                    viewModel.setCurrentPageIndex(index)
                }
            },
            onEditSidebarButton = { inlineInspectSidebarButton = it },
            onAssignAppsToTag = { inlineAssignAppsTag = it },
            onUpdateTagLayout = { viewModel.saveTag(it) },
            onAppLongPress = { inlineCustomizeApp = it },
            onToggleSidebarPin = { sbId, pinned ->
                state.sidebars.find { it.sidebarId == sbId }?.let { sb ->
                    viewModel.saveSidebar(sb.copy(pinned = pinned))
                }
            },
            onSaveSidebar = { viewModel.saveSidebar(it) },
            onSaveWindow = { viewModel.saveWindow(it) },
            installedWidgetProviders = installedWidgetProviders,
            onImportImage = { uri, folder, name -> viewModel.importImagePreservingOriginal(uri, folder, name) },
            onSaveWindowElement = { el, sel -> viewModel.saveHomeElement(el, sel) },
            onDuplicateWindowElement = { viewModel.duplicateElement(it) },
            onCopyWindowElement = { viewModel.copyElementToClipboard(it) },
            onCutWindowElement = { viewModel.cutElementToClipboard(it) },
            onPasteToWindow = { pageId, winId -> viewModel.pasteElementFromClipboard(pageId, winId) },
            onBringWindowElementForward = { viewModel.bringElementForward(it) },
            onSendWindowElementBackward = { viewModel.sendElementBackward(it) },
            onDeleteWindowElement = { viewModel.deleteHomeElement(it) },
            onStartSystemWidgetBindForWindow = { provider, pageId, winId ->
                startSystemWidgetBind(provider, pageId, winId, "WINDOW")
            },
            windowElementRenderer = { el, windowEditMode ->
                HomeElementBody(
                    element = el,
                    state = state.copy(isEditMode = windowEditMode),
                    appDiscovery = viewModel.appDiscovery,
                    widgetHost = viewModel.widgetHost,
                    onLaunchApp = { viewModel.launchApp(it) },
                    onOpenTagSidebar = { viewModel.openTagSidebar(it) },
                    onOpenCategorySidebar = { viewModel.openCategorySidebar(it) },
                    onOpenRuleSidebar = { viewModel.openRuleSidebar(it) },
                    onOpenSidebar = { viewModel.openConfiguredSidebar(it) },
                    onOpenWindow = { viewModel.openWindow(it) },
                    onOpenFolder = { viewModel.openFolder(it) },
                    onElementTrigger = { targetEl, trig ->
                        viewModel.executeElementActions(targetEl.actionsConfigJson, trig, navigateTo) { idx ->
                            viewModel.setCurrentPageIndex(idx)
                        }
                    },
                )
            },
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        )
    }

    // Inline dialogs triggered directly from Tag Sidebars or Drawer
    inlineEditTag?.let { t ->
        val freshTag = state.allTags.find { it.tagId == t.tagId } ?: t
        TagEditorDialog(
            initialTag = freshTag,
            allTags = state.allTags,
            onImportImage = { viewModel.importImagePreservingOriginal(it, "Tags") },
            onSave = {
                viewModel.saveTag(it)
                inlineEditTag = null
            },
            onDelete = {
                viewModel.deleteTag(it)
                inlineEditTag = null
            },
            onDismiss = { inlineEditTag = null },
        )
    }

    inlineEditCategory?.let { c ->
        val freshCat = state.categories.find { it.categoryId == c.categoryId } ?: c
        CategoryEditorDialog(
            initialCategory = freshCat,
            onImportImage = { viewModel.importImagePreservingOriginal(it, "Categories") },
            onSave = { saved ->
                val categoryToSave = if (c.categoryId == 0L && c.parentCategoryId != null) {
                    saved.copy(
                        parentCategoryId = c.parentCategoryId,
                        isCustom = true,
                        categoryType = "CUSTOM",
                    )
                } else saved
                viewModel.saveCategory(categoryToSave)
                inlineEditCategory = null
            },
            onDismiss = { inlineEditCategory = null },
        )
    }

    inlineAddSidebarButtonCategory?.let { category ->
            AlertDialog(
                onDismissRequest = { inlineAddSidebarButtonCategory = null },
                title = { Text("Add Button") },
                text = {
                    OutlinedTextField(
                        value = inlineAddSidebarButtonLabel,
                        onValueChange = { inlineAddSidebarButtonLabel = it },
                        label = { Text("Button label") },
                        singleLine = true,
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val label = inlineAddSidebarButtonLabel.trim()
                        val pageId = state.homePages.getOrNull(state.currentPageIndex)?.pageId
                            ?: state.homePages.firstOrNull()?.pageId
                        if (label.isNotEmpty() && pageId != null) {
                            viewModel.saveHomeElement(
                                HomeElementEntity(
                                    pageId = pageId,
                                    type = "CUSTOM_BUTTON",
                                    label = label,
                                    targetCategoryId = category.categoryId,
                                    visible = false,
                                    widthDp = 160f,
                                    heightDp = 56f,
                                    backgroundColorHex = 0xCC38BDF8L,
                                    textColorHex = 0xFF000000L,
                                    cornerRadiusDp = 18f,
                                )
                            )
                        }
                        inlineAddSidebarButtonCategory = null
                    }) { Text("Add") }
                },
                dismissButton = {
                    TextButton(onClick = { inlineAddSidebarButtonCategory = null }) {
                        Text("Cancel")
                    }
                },
            )
        }

        inlineInspectSidebarButton?.let { snapshot ->
            val element = state.homeElements.find { it.elementId == snapshot.elementId } ?: snapshot
            ElementInspectorDialog(
                element = element,
                state = state,
                onImportImage = { uri, folder, name ->
                    viewModel.importImagePreservingOriginal(uri, folder, name)
                },
                onUpdate = { edited ->
                    viewModel.saveHomeElement(
                        edited.copy(visible = false, targetCategoryId = element.targetCategoryId),
                        true,
                    )
                },
                onDuplicate = {
                    viewModel.duplicateElement(element)
                    inlineInspectSidebarButton = null
                },
                onCopy = {
                    viewModel.copyElementToClipboard(element)
                    inlineInspectSidebarButton = null
                },
                onCut = {
                    viewModel.cutElementToClipboard(element)
                    inlineInspectSidebarButton = null
                },
                onBringForward = { viewModel.bringElementForward(element) },
                onSendBackward = { viewModel.sendElementBackward(element) },
                onDelete = {
                    viewModel.deleteHomeElement(element)
                    inlineInspectSidebarButton = null
                },
                onDismiss = { inlineInspectSidebarButton = null },
            )
        }

        inlineAssignAppsTag?.let { t ->
        BulkAssignAppsToTagDialog(
            tag = t,
            allApps = state.appsWithTags,
            appDiscovery = viewModel.appDiscovery,
            onSave = { pkgList ->
                viewModel.replaceAppsForTag(t.tagId, pkgList)
                inlineAssignAppsTag = null
            },
            onDismiss = { inlineAssignAppsTag = null },
        )
    }

    inlineCustomizeApp?.let { app ->
        val fresh = state.appsWithTags.find { it.app.packageName == app.packageName }
        if (fresh != null) {
            AppActionAndCustomizeDialog(
                appWithTags = fresh,
                allTags = state.allTags,
                allCategories = state.categories,
                appCategoryRefs = state.appCategories,
                appDiscovery = viewModel.appDiscovery,
                onImportImage = { viewModel.importImagePreservingOriginal(it, "Icons") },
                onSaveCustomizations = { name, path, scale, ox, oy, alpha ->
                    viewModel.setAppCustomName(fresh.app.packageName, name)
                    viewModel.setAppCustomIcon(fresh.app.packageName, path, scale, ox, oy, alpha)
                },
                onSaveTags = { tagIds -> viewModel.replaceTagsForApp(fresh.app.packageName, tagIds) },
                onSaveCategories = { prim, all, excl ->
                    viewModel.setManualCategoriesForApp(fresh.app.packageName, prim, all, excl)
                },
                onRestoreAutoCategory = { viewModel.restoreAutomaticCategoryForApp(fresh.app) },
                onToggleFavorite = { viewModel.setAppFavorite(fresh.app.packageName, !fresh.app.favorite) },
                onToggleHidden = { viewModel.setAppHidden(fresh.app.packageName, !fresh.app.hidden) },
                onOpenAppInfo = { viewModel.openAppInfo(fresh.app.packageName) },
                onUninstall = { viewModel.requestUninstall(fresh.app.packageName) },
                onEditLineIcon = { inlineLineIconApp = fresh.app; inlineCustomizeApp = null },
                onDismiss = { inlineCustomizeApp = null },
            )
        }
    }

    inlineLineIconApp?.let { app ->
        LineIconStudioDialog(
            app = app,
            appDiscovery = viewModel.appDiscovery,
            iconPackOrder = state.settings.iconPackOrder(),
            initialProfile = state.settings.lineIconProfile(app.packageName),
            onSave = { profile ->
                viewModel.setLineIconProfile(app.packageName, profile)
                inlineLineIconApp = null
            },
            onDismiss = { inlineLineIconApp = null },
        )
    }
    }
}
