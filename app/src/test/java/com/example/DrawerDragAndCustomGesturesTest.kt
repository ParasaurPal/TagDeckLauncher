package com.example

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppEntity
import com.example.data.db.AppWithTags
import com.example.data.db.CategoryEntity
import com.example.data.db.GestureBindingEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.LauncherDatabase
import com.example.data.db.PlayCategoryCatalog
import com.example.data.db.TagEntity
import com.example.data.db.TagWithApps
import com.example.data.prefs.LauncherPreferencesRepository
import com.example.data.prefs.LauncherSettings
import com.example.data.repository.LauncherRepository
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.ImageStorageManager
import com.example.data.system.LauncherWidgetHost
import com.example.ui.components.ActiveOverlayHost
import com.example.ui.components.AppDragOverlayGhost
import com.example.ui.components.AppDrawerOverlay
import com.example.ui.components.LocalAppDragCoordinator
import com.example.ui.components.rememberAppDragToDesktopController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.state.ActiveOverlay
import com.example.ui.state.LauncherUiState
import com.example.ui.state.LauncherViewModel
import com.example.ui.theme.TagDeckTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DrawerDragAndCustomGesturesTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var db: LauncherDatabase
    private lateinit var appDiscovery: AppDiscoveryManager
    private lateinit var widgetHost: LauncherWidgetHost

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, LauncherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        appDiscovery = AppDiscoveryManager(context)
        widgetHost = LauncherWidgetHost(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `app drawer drag beyond threshold drops app onto desktop while stationary long press edits and tap launches`() {
        val spotify = AppEntity(packageName = "com.spotify.music", appName = "Spotify", primaryCategoryId = 20L)
        var launchedPkg: String? = null
        var longPressedPkg: String? = null
        var droppedPkg: String? = null
        var droppedFx = -1f
        var droppedFy = -1f
        var drawerVisible by mutableStateOf(true)

        composeRule.setContent {
            val state = LauncherUiState(
                isLoading = false,
                appsWithTags = listOf(AppWithTags(spotify, emptyList())),
                categories = listOf(
                    CategoryEntity(
                        categoryId = PlayCategoryCatalog.SYSTEM_ALL_ID,
                        name = "All",
                        categoryType = "SYSTEM_ALL",
                        sortOrder = -10,
                    )
                ),
                isDrawerOpen = drawerVisible,
                settings = LauncherSettings(drawerLayoutMode = "GRID", gridSnapEnabled = false),
            )
            TagDeckTheme {
                val controller = rememberAppDragToDesktopController(
                    gridSnapEnabled = false,
                    gridSnapStepDp = 16,
                    onDropApp = { app, fx, fy ->
                        droppedPkg = app.packageName
                        droppedFx = fx
                        droppedFy = fy
                        drawerVisible = false
                    },
                )
                controller.updateCanvasBounds(Rect(0f, 0f, 1080f, 1920f))

                CompositionLocalProvider(LocalAppDragCoordinator provides controller) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppDrawerOverlay(
                            visible = drawerVisible,
                            state = state,
                            appDiscovery = appDiscovery,
                            onClose = { drawerVisible = false },
                            onLaunchApp = { launchedPkg = it.packageName },
                            onAppLongPress = { longPressedPkg = it.packageName },
                            onOpenTagSidebar = {},
                            onToggleLayout = {},
                            onCycleSort = {},
                        )
                        AppDragOverlayGhost(
                            session = controller.session,
                            targetPageTitle = "Main Deck",
                            appDiscovery = appDiscovery,
                            iconShapeKey = "SQUIRCLE",
                        )
                    }
                }
            }
        }

        composeRule.waitForIdle()
        // 1. Quick tap -> launches app, does not drop or long-press
        composeRule.onNodeWithTag("drawer_app_com.spotify.music").performClick()
        composeRule.waitForIdle()
        assertEquals("com.spotify.music", launchedPkg)
        assertEquals(null, longPressedPkg)
        assertEquals(null, droppedPkg)

        // 2. Stationary long-press (< 18dp movement) -> triggers app options/longPress, does not drop
        drawerVisible = true
        launchedPkg = null
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_app_com.spotify.music").performTouchInput { longClick() }
        composeRule.waitForIdle()
        assertEquals("com.spotify.music", longPressedPkg)
        assertEquals(null, droppedPkg)
        assertEquals(null, launchedPkg)

        // 3. Long-press + drag (> 18dp) -> reveals ghost & drops app shortcut onto desktop
        longPressedPkg = null
        composeRule.onNodeWithTag("drawer_app_com.spotify.music").performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 120L)
            moveBy(Offset(140f, 180f))
            advanceEventTime(60L)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_drag_to_desktop_ghost").assertIsDisplayed()

        composeRule.onNodeWithTag("drawer_app_com.spotify.music").performTouchInput {
            up()
        }
        composeRule.waitForIdle()
        assertEquals("com.spotify.music", droppedPkg)
        assertEquals(null, longPressedPkg)
        assertEquals(null, launchedPkg)
        assertTrue(droppedFx in 0f..0.90f)
        assertTrue(droppedFy in 0f..0.90f)
        assertFalse(drawerVisible)
    }

    @Test
    fun `sidebar drawer drag beyond threshold drops app onto desktop`() {
        val discord = AppEntity(packageName = "com.discord", appName = "Discord")
        val gamingTag = TagEntity(tagId = 1L, name = "GAMING", colorHex = 0xFF34D399L)
        var droppedPkg: String? = null
        var longPressedPkg: String? = null

        composeRule.setContent {
            val state = LauncherUiState(
                isLoading = false,
                allTags = listOf(gamingTag),
                tagsWithApps = listOf(TagWithApps(gamingTag, listOf(discord))),
                activeOverlay = ActiveOverlay.TagSidebar(1L),
            )
            TagDeckTheme {
                val controller = rememberAppDragToDesktopController(
                    gridSnapEnabled = false,
                    gridSnapStepDp = 16,
                    onDropApp = { app, _, _ -> droppedPkg = app.packageName },
                )
                controller.updateCanvasBounds(Rect(0f, 0f, 1080f, 1920f))

                CompositionLocalProvider(LocalAppDragCoordinator provides controller) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ActiveOverlayHost(
                            state = state,
                            appDiscovery = appDiscovery,
                            onDismiss = {},
                            onSwitchToTag = {},
                            onLaunchApp = {},
                            onEditTag = {},
                            onAssignAppsToTag = {},
                            onUpdateTagLayout = {},
                            onAppLongPress = { longPressedPkg = it.packageName },
                            onToggleSidebarPin = { _, _ -> },
                        )
                    }
                }
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("sidebar_app_com.discord").performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 120L)
            moveBy(Offset(150f, 120f))
            advanceEventTime(60L)
            up()
        }
        composeRule.waitForIdle()
        assertEquals("com.discord", droppedPkg)
        assertEquals(null, longPressedPkg)
    }

    @Test
    fun `viewmodel dropAppShortcutOnHome persists app shortcut on current home page and closes overlays`() = runBlocking {
        val dao = db.launcherDao()
        val pageId = dao.upsertHomePage(HomePageEntity(title = "Studio Deck", pageOrder = 0))
        val spotify = AppEntity(packageName = "com.spotify.music", appName = "Spotify")
        dao.upsertApps(listOf(spotify))

        val repo = LauncherRepository(
            dao = dao,
            appDiscovery = appDiscovery,
            prefsRepo = LauncherPreferencesRepository(context),
        )
        val vm = LauncherViewModel(
            repository = repo,
            appDiscovery = appDiscovery,
            imageStorage = ImageStorageManager(context),
            widgetHost = widgetHost,
        )

        val collectorJob = launch { vm.uiState.collect {} }
        repeat(8) {
            ShadowLooper.idleMainLooper()
            delay(30L)
        }

        vm.setDrawerOpen(true)
        vm.dropAppShortcutOnHome(
            app = spotify,
            xFraction = 0.42f,
            yFraction = 0.35f,
            isLandscape = false,
        )
        repeat(15) {
            ShadowLooper.idleMainLooper()
            if (dao.getHomeElementsOnce().any { it.targetPackageName == "com.spotify.music" }) return@repeat
            delay(40L)
        }
        collectorJob.cancel()

        val elements = dao.getHomeElementsOnce()
        val placed = elements.find { it.targetPackageName == "com.spotify.music" && it.type == "APP_SHORTCUT" }
        assertNotNull(placed)
        assertEquals(pageId, placed!!.pageId)
        assertEquals("PAGE", placed.ownerType)
        assertEquals(0.42f, placed.xFraction, 0.01f)
        assertEquals(0.35f, placed.yFraction, 0.01f)
        assertFalse(vm.uiState.value.isDrawerOpen)
    }

    @Test
    fun `settings custom home gestures menu configures swipe up app action and double tap launcher function`() {
        val spotify = AppEntity(packageName = "com.spotify.music", appName = "Spotify")
        val gmail = AppEntity(packageName = "com.google.android.gm", appName = "Gmail")
        var resetCalled = false

        composeRule.setContent {
            var uiState by remember {
                mutableStateOf(
                    LauncherUiState(
                        isLoading = false,
                        appsWithTags = listOf(
                            AppWithTags(spotify, emptyList()),
                            AppWithTags(gmail, emptyList()),
                        ),
                        gestures = mapOf(
                            "SWIPE_UP" to GestureBindingEntity("SWIPE_UP", "OPEN_DRAWER"),
                            "SWIPE_DOWN" to GestureBindingEntity("SWIPE_DOWN", "OPEN_SEARCH"),
                            "DOUBLE_TAP" to GestureBindingEntity("DOUBLE_TAP", "TOGGLE_EDIT_MODE"),
                        ),
                    )
                )
            }
            TagDeckTheme {
                SettingsScreen(
                    state = uiState,
                    appDiscovery = appDiscovery,
                    onBack = {},
                    onNavigate = {},
                    onRequestDefaultLauncher = {},
                    onUpdateSettings = {},
                    onSaveGesture = { saved ->
                        uiState = uiState.copy(gestures = uiState.gestures + (saved.gestureType to saved))
                    },
                    onResetDefaultGestures = { resetCalled = true },
                    onImportImage = { _, _, _ -> null },
                    onRenameImageResource = { _, _, _ -> },
                    onDeleteImageResource = {},
                    onSaveBackgroundPreset = {},
                    onDeleteBackgroundPreset = {},
                    onExportBackupJson = { "{}" },
                    onImportBackupJson = {},
                    onShowMessage = {},
                )
            }
        }

        composeRule.waitForIdle()
        // Open top-level Custom Home Screen Gestures menu item
        composeRule.onNodeWithTag("settings_menu_custom_gestures").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_gestures_config_dialog").assertIsDisplayed()

        // Configure SWIPE_UP -> OPEN_APP -> Spotify
        composeRule.onNodeWithTag("gesture_config_row_SWIPE_UP").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gesture_binding_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("gesture_action_dropdown").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gesture_action_option_OPEN_APP").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gesture_app_search_input").performTextReplacement("spot")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gesture_target_app_com.spotify.music").performClick()
        composeRule.onNodeWithTag("save_gesture_binding_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Launch: Spotify").assertIsDisplayed()

        // Configure DOUBLE_TAP -> OPEN_SYSTEM_SETTING (Wi-Fi Settings)
        composeRule.onNodeWithTag("home_gestures_config_list")
            .performScrollToNode(hasTestTag("gesture_config_row_DOUBLE_TAP"))
        composeRule.onNodeWithTag("gesture_config_row_DOUBLE_TAP").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gesture_action_dropdown").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gesture_action_option_OPEN_SYSTEM_SETTING").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Wi-Fi Settings").performScrollTo().performClick()
        composeRule.onNodeWithTag("save_gesture_binding_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("System: Wi-Fi Settings").assertIsDisplayed()

        composeRule.onNodeWithText("Done").performClick()
        composeRule.waitForIdle()

        // Verify the lower Gestures & Shortcuts card also reflects saved bindings and Reset Defaults works
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("reset_gestures_button"))
        composeRule.onNodeWithTag("reset_gestures_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertTrue(resetCalled)

        // Silence unused constant warning in imports
        assertTrue(Settings.ACTION_WIFI_SETTINGS.isNotBlank())
    }

    @Test
    fun `home screen triggers custom vertical and horizontal swipe gestures on single page`() {
        val triggeredGestures = mutableListOf<String>()
        val singlePage = HomePageEntity(pageId = 1L, title = "Main Deck", pageOrder = 0)

        composeRule.setContent {
            val state = LauncherUiState(
                isLoading = false,
                homePages = listOf(singlePage),
                isDefaultLauncher = true,
                settings = LauncherSettings(widgetFeedEnabled = false),
                gestures = mapOf(
                    "SWIPE_UP" to GestureBindingEntity("SWIPE_UP", "OPEN_DRAWER"),
                    "SWIPE_DOWN" to GestureBindingEntity("SWIPE_DOWN", "OPEN_SEARCH"),
                    "SWIPE_LEFT" to GestureBindingEntity("SWIPE_LEFT", "OPEN_SETTINGS"),
                    "SWIPE_RIGHT" to GestureBindingEntity("SWIPE_RIGHT", "OPEN_TAG_MANAGER"),
                ),
            )
            TagDeckTheme {
                HomeScreen(
                    state = state,
                    appDiscovery = appDiscovery,
                    widgetHost = widgetHost,
                    onImportImage = { _, _, _ -> null },
                    onLaunchApp = {},
                    onOpenTagSidebar = {},
                    onOpenCategorySidebar = {},
                    onOpenConfiguredSidebar = {},
                    onOpenRuleSidebar = {},
                    onOpenWindow = {},
                    onOpenFolder = {},
                    onOpenDrawer = {},
                    onOpenSearch = {},
                    onNavigate = {},
                    onToggleEditMode = {},
                    onSelectElement = {},
                    onToggleMultiSelect = {},
                    onDeleteMultiSelected = {},
                    onLockMultiSelected = {},
                    onUndo = {},
                    onRedo = {},
                    onToggleGridSnap = {},
                    onSaveElement = { _, _ -> },
                    onDuplicateElement = {},
                    onCopyElement = {},
                    onCutElement = {},
                    onPasteElement = {},
                    onBringForward = {},
                    onSendBackward = {},
                    onDeleteElement = {},
                    onExecuteElementAction = { _, _, _ -> false },
                    onAddPage = {},
                    onSavePage = {},
                    onDuplicatePage = {},
                    onReorderPages = {},
                    onDeletePage = {},
                    onSaveSnapshot = {},
                    onRestoreSnapshot = {},
                    onDeleteSnapshot = {},
                    onPageChanged = {},
                    onTriggerGesture = { gesture, _ -> triggeredGestures += gesture },
                    onRequestSetDefaultHome = {},
                    onDismissDefaultBanner = {},
                    onStartSystemWidgetBind = { _, _, _ -> },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_canvas_1").performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_canvas_1").performTouchInput { swipeDown() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_canvas_1").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_canvas_1").performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        assertEquals(listOf("SWIPE_UP", "SWIPE_DOWN", "SWIPE_LEFT", "SWIPE_RIGHT"), triggeredGestures)
    }
}
