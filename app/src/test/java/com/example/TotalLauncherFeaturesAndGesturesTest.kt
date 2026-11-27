package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppEntity
import com.example.data.db.AppTagCrossRef
import com.example.data.db.AppWithTags
import com.example.data.db.GestureBindingEntity
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.LauncherDatabase
import com.example.data.db.TagEntity
import com.example.data.prefs.LauncherPreferencesRepository
import com.example.data.prefs.LauncherSettings
import com.example.data.repository.LauncherRepository
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.BackupManager
import com.example.data.system.FullBackupPayload
import com.example.data.system.LauncherWidgetHost
import com.example.ui.screens.ElementInspectorDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LauncherSettingsCatalog
import com.example.ui.screens.SettingsScreen
import com.example.ui.state.LauncherUiState
import com.example.ui.theme.TagDeckTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TotalLauncherFeaturesAndGesturesTest {

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
    fun `room many-to-many app tag queries and tag-triggered sidebar persistence work end-to-end`() = runBlocking {
        val dao = db.launcherDao()
        val repo = LauncherRepository(
            dao = dao,
            appDiscovery = appDiscovery,
            prefsRepo = LauncherPreferencesRepository(context),
        )

        val focusTagId = dao.insertTag(TagEntity(name = "Focus", colorHex = 0xFF38BDF8L))
        val socialTagId = dao.insertTag(TagEntity(name = "Social", colorHex = 0xFFFB7185L))
        dao.upsertApps(
            listOf(
                AppEntity(packageName = "com.notes.app", appName = "Notes"),
                AppEntity(packageName = "com.chat.app", appName = "Chat"),
                AppEntity(packageName = "com.hidden.app", appName = "Hidden", hidden = true),
            )
        )
        dao.insertAppTagCrossRefs(
            listOf(
                AppTagCrossRef("com.notes.app", focusTagId),
                AppTagCrossRef("com.chat.app", focusTagId),
                AppTagCrossRef("com.chat.app", socialTagId),
                AppTagCrossRef("com.hidden.app", focusTagId),
            )
        )

        val focusApps = repo.observeAppsForTag(focusTagId).first()
        assertEquals(listOf("com.chat.app", "com.notes.app"), focusApps.map { it.packageName })

        val chatTags = repo.observeTagsForApp("com.chat.app").first()
        assertEquals(setOf(focusTagId, socialTagId), chatTags.map { it.tagId }.toSet())

        val tagWithApps = repo.observeTagWithApps(focusTagId).first()
        assertNotNull(tagWithApps)
        assertEquals("Focus", tagWithApps!!.tag.name)
        assertEquals(3, tagWithApps.apps.size)

        val sbId = repo.createOrUpdateTagTriggeredSidebar(focusTagId, side = "LEFT")
        val updatedSbId = repo.createOrUpdateTagTriggeredSidebar(focusTagId, side = "RIGHT")
        assertEquals(sbId, updatedSbId)

        val tagSidebars = repo.tagTriggeredSidebarsFlow.first()
        assertEquals(1, tagSidebars.size)
        assertEquals("TAG", tagSidebars.first().sourceType)
        assertEquals(focusTagId, tagSidebars.first().targetTagId)
        assertEquals("RIGHT", tagSidebars.first().side)
    }

    @Test
    fun `total launcher settings roundtrip backup and update in SettingsScreen`() {
        val custom = LauncherSettings(
            screenOrientationMode = "LANDSCAPE",
            keepScreenOn = true,
            statusBarIconStyle = "DARK",
            navBarIconStyle = "LIGHT",
            closeFolderOnLaunch = false,
            closeDrawerOnLaunch = false,
            autoFocusDrawerSearch = true,
            returnToDefaultPageOnHome = true,
            longPressDelayMs = 620,
            swipeGestureSensitivity = 1.45f,
            pressFeedbackScale = 0.89f,
            wallpaperScrollingEnabled = false,
            landscapeSeparateLayout = false,
            allowOverlappingElements = false,
            homePaddingHorizontalDp = 16f,
            homePaddingVerticalDp = 12f,
            defaultShortcutWidthDp = 104f,
            defaultShortcutHeightDp = 112f,
            dockHeightDp = 64f,
            dockBackgroundAlpha = 0.55f,
            doubleTapPageBehavior = "OPEN_DRAWER",
        )
        val encoded = BackupManager.encodeFullBackup(
            FullBackupPayload(
                apps = emptyList(),
                tags = emptyList(),
                crossRefs = emptyList(),
                rules = emptyList(),
                pages = emptyList(),
                elements = emptyList(),
                sidebars = emptyList(),
                gestures = emptyList(),
                settings = custom,
            )
        )
        val decoded = BackupManager.decodeFullBackup(encoded).settings
        assertEquals("LANDSCAPE", decoded.screenOrientationMode)
        assertTrue(decoded.keepScreenOn)
        assertEquals("DARK", decoded.statusBarIconStyle)
        assertEquals("LIGHT", decoded.navBarIconStyle)
        assertFalse(decoded.closeFolderOnLaunch)
        assertFalse(decoded.closeDrawerOnLaunch)
        assertTrue(decoded.autoFocusDrawerSearch)
        assertTrue(decoded.returnToDefaultPageOnHome)
        assertEquals(620, decoded.longPressDelayMs)
        assertEquals(1.45f, decoded.swipeGestureSensitivity, 0.01f)
        assertEquals(0.89f, decoded.pressFeedbackScale, 0.01f)
        assertFalse(decoded.wallpaperScrollingEnabled)
        assertFalse(decoded.landscapeSeparateLayout)
        assertFalse(decoded.allowOverlappingElements)
        assertEquals(16f, decoded.homePaddingHorizontalDp, 0.01f)
        assertEquals(12f, decoded.homePaddingVerticalDp, 0.01f)
        assertEquals(104f, decoded.defaultShortcutWidthDp, 0.01f)
        assertEquals(112f, decoded.defaultShortcutHeightDp, 0.01f)
        assertEquals(64f, decoded.dockHeightDp, 0.01f)
        assertEquals(0.55f, decoded.dockBackgroundAlpha, 0.01f)
        assertEquals("OPEN_DRAWER", decoded.doubleTapPageBehavior)

        assertTrue(LauncherSettingsCatalog.searchSettingsEntries("orientation").isNotEmpty())
        assertTrue(LauncherSettingsCatalog.searchSettingsEntries("overlap").isNotEmpty())
        assertTrue(LauncherSettingsCatalog.searchSettingsEntries("long press delay").isNotEmpty())

        var latest = LauncherSettings()
        composeRule.setContent {
            var uiState by remember { mutableStateOf(LauncherUiState(isLoading = false, settings = latest)) }
            TagDeckTheme {
                SettingsScreen(
                    state = uiState,
                    onBack = {},
                    onNavigate = {},
                    onRequestDefaultLauncher = {},
                    onUpdateSettings = { fn ->
                        latest = fn(uiState.settings)
                        uiState = uiState.copy(settings = latest)
                    },
                    onSaveGesture = {},
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
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_orientation_PORTRAIT"))
        composeRule.onNodeWithTag("settings_orientation_PORTRAIT").performScrollTo().performClick()
        assertEquals("PORTRAIT", latest.screenOrientationMode)

        composeRule.onNodeWithTag("settings_keep_screen_on_switch").performScrollTo().performClick()
        assertTrue(latest.keepScreenOn)

        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_double_tap_behavior_OPEN_SEARCH"))
        composeRule.onNodeWithTag("settings_double_tap_behavior_OPEN_SEARCH").performScrollTo().performClick()
        assertEquals("OPEN_SEARCH", latest.doubleTapPageBehavior)
    }

    @Test
    fun `pointerInputScope handles swipes double-taps long-presses and taps on home shortcut and canvas`() {
        val spotify = AppEntity(packageName = "com.spotify.music", appName = "Spotify")
        val actionsJson = JSONObject().apply {
            put("SWIPE_UP", JSONObject().apply {
                put("actionType", "OPEN_APP_SHORTCUT")
                put("targetPackage", "com.spotify.music")
                put("targetUrl", "sc_play")
            })
            put("SWIPE_LEFT", JSONObject().apply {
                put("actionType", "OPEN_DRAWER")
            })
            put("DOUBLE_TAP", JSONObject().apply {
                put("actionType", "OPEN_APP")
                put("targetPackage", "com.spotify.music")
            })
            put("LONG_PRESS", JSONObject().apply {
                put("actionType", "OPEN_APP_INFO")
                put("targetPackage", "com.spotify.music")
            })
        }.toString()

        val shortcutElement = HomeElementEntity(
            elementId = 77L,
            pageId = 1L,
            type = "APP_SHORTCUT",
            targetPackageName = "com.spotify.music",
            label = "Spotify",
            xFraction = 0.05f,
            yFraction = 0.05f,
            widthDp = 120f,
            heightDp = 120f,
            actionsConfigJson = actionsJson,
        )

        val triggeredElementEvents = mutableListOf<String>()
        val triggeredCanvasGestures = mutableListOf<String>()
        val launchedPkgs = mutableListOf<String>()

        composeRule.setContent {
            val state = LauncherUiState(
                isLoading = false,
                homePages = listOf(HomePageEntity(pageId = 1L, title = "Home", pageOrder = 0)),
                homeElements = listOf(shortcutElement),
                appsWithTags = listOf(AppWithTags(spotify, emptyList())),
                isDefaultLauncher = true,
                settings = LauncherSettings(widgetFeedEnabled = false, longPressDelayMs = 350),
                gestures = mapOf(
                    "LONG_PRESS" to GestureBindingEntity("LONG_PRESS", "OPEN_DRAWER"),
                    "DOUBLE_TAP" to GestureBindingEntity("DOUBLE_TAP", "OPEN_SEARCH"),
                ),
            )
            TagDeckTheme {
                HomeScreen(
                    state = state,
                    appDiscovery = appDiscovery,
                    widgetHost = widgetHost,
                    onImportImage = { _, _, _ -> null },
                    onLaunchApp = {
                        println("SHORTCUT_LAUNCH_CALLBACK=${it.packageName}")
                        launchedPkgs += it.packageName
                    },
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
                    onExecuteElementAction = { el, trig, _ ->
                        println("ELEMENT_TRIGGER=${el.elementId}:$trig")
                        triggeredElementEvents += "${el.elementId}:$trig"
                        true
                    },
                    onAddPage = {},
                    onSavePage = {},
                    onDuplicatePage = {},
                    onReorderPages = {},
                    onDeletePage = {},
                    onSaveSnapshot = {},
                    onRestoreSnapshot = {},
                    onDeleteSnapshot = {},
                    onPageChanged = {},
                    onTriggerGesture = { gesture, _ -> triggeredCanvasGestures += gesture },
                    onRequestSetDefaultHome = {},
                    onDismissDefaultBanner = {},
                    onStartSystemWidgetBind = { _, _, _ -> },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_element_77").performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        assertTrue("77:SWIPE_UP" in triggeredElementEvents)

        composeRule.onNodeWithTag("home_element_77").performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        assertTrue("77:SWIPE_LEFT" in triggeredElementEvents)

        composeRule.onNodeWithTag("home_element_77").performTouchInput { doubleClick() }
        composeRule.waitForIdle()
        assertTrue("77:DOUBLE_TAP" in triggeredElementEvents)

        composeRule.onNodeWithTag("home_element_77").performTouchInput { longClick() }
        composeRule.waitForIdle()
        assertTrue("77:LONG_PRESS" in triggeredElementEvents)

        // Single tap falls back to primary shortcut launch
        val shortcutNode = composeRule.onNodeWithTag("home_element_content_77")
        shortcutNode.assertHasClickAction()
        shortcutNode.performClick()
        composeRule.waitForIdle()
        assertTrue("Expected Spotify launch; launched=$launchedPkgs, events=$triggeredElementEvents",
            "com.spotify.music" in launchedPkgs)

        // Empty-canvas long-press dispatches configured LONG_PRESS gesture
        composeRule.onNodeWithTag("home_canvas_1").performTouchInput { longClick(Offset(centerX, centerY)) }
        composeRule.waitForIdle()
        assertTrue("LONG_PRESS" in triggeredCanvasGestures)
    }

    @Test
    fun `freeform builder displays live coordinate HUD, snaps resize, and inspector aligns coordinates`() {
        var element = HomeElementEntity(
            elementId = 31L,
            pageId = 1L,
            type = "SHAPE",
            label = "Card",
            xFraction = 0.25f,
            yFraction = 0.30f,
            widthDp = 120f,
            heightDp = 96f,
        )
        var latestSaved = element

        composeRule.setContent {
            var uiState by remember {
                mutableStateOf(
                    LauncherUiState(
                        isLoading = false,
                        homePages = listOf(HomePageEntity(pageId = 1L, title = "Studio", pageOrder = 0)),
                        homeElements = listOf(element),
                        isEditMode = true,
                        selectedElementId = 31L,
                        isDefaultLauncher = true,
                        settings = LauncherSettings(gridSnapEnabled = true, gridSnapStepDp = 16),
                    )
                )
            }
            TagDeckTheme {
                HomeScreen(
                    state = uiState,
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
                    onSelectElement = { id -> uiState = uiState.copy(selectedElementId = id) },
                    onToggleMultiSelect = {},
                    onDeleteMultiSelected = {},
                    onLockMultiSelected = {},
                    onUndo = {},
                    onRedo = {},
                    onToggleGridSnap = {},
                    onSaveElement = { saved, _ ->
                        latestSaved = saved
                        element = saved
                        uiState = uiState.copy(homeElements = listOf(saved))
                    },
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
                    onTriggerGesture = { _, _ -> },
                    onRequestSetDefaultHome = {},
                    onDismissDefaultBanner = {},
                    onStartSystemWidgetBind = { _, _, _ -> },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("element_coordinate_hud_31").assertIsDisplayed()
        composeRule.onNodeWithTag("element_coordinate_hud_31").assertTextContains("25%,30%", substring = true)

        // Drag resize handle and verify grid-snapped size persists
        composeRule.onNodeWithTag("resize_element_31").performTouchInput {
            down(center)
            moveBy(Offset(64f, 48f))
            up()
        }
        composeRule.waitForIdle()
        assertEquals(0, latestSaved.widthDp.toInt() % 16)
        assertEquals(0, latestSaved.heightDp.toInt() % 16)
        assertTrue(latestSaved.widthDp > 120f)
    }

    @Test
    fun `element inspector layout tab shows coordinates summary and aligns element`() {
        var updated = HomeElementEntity(
            elementId = 8L,
            pageId = 1L,
            type = "CUSTOM_BUTTON",
            label = "Action",
            xFraction = 0.25f,
            yFraction = 0.25f,
            widthDp = 140f,
            heightDp = 64f,
        )

        composeRule.setContent {
            TagDeckTheme {
                ElementInspectorDialog(
                    element = updated,
                    state = LauncherUiState(isLoading = false),
                    onImportImage = { _, _, _ -> null },
                    onUpdate = { updated = it },
                    onDuplicate = {},
                    onCopy = {},
                    onCut = {},
                    onBringForward = {},
                    onSendBackward = {},
                    onDelete = {},
                    onDismiss = {},
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("inspector_coordinates_summary").assertIsDisplayed()
        composeRule.onNodeWithTag("inspector_align_right").performScrollTo().performClick()
        composeRule.onNodeWithTag("inspector_align_bottom").performScrollTo().performClick()
        composeRule.onNodeWithTag("inspector_apply_button").performClick()
        composeRule.waitForIdle()

        assertEquals(0.80f, updated.xFraction, 0.01f)
        assertEquals(0.80f, updated.yFraction, 0.01f)
    }
}
