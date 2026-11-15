package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.LauncherDatabase
import com.example.data.db.LauncherWindowEntity
import com.example.data.prefs.LauncherSettings
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.LauncherWidgetHost
import com.example.ui.components.ActiveOverlayHost
import com.example.ui.screens.HomeScreen
import com.example.ui.state.ActiveOverlay
import com.example.ui.state.LauncherUiState
import com.example.ui.theme.TagDeckTheme
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeEditingAndIconHierarchyTest {

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
    fun `home screen hides add widgets settings outside edit mode and enters edit mode only on empty space long press`() {
        val page1 = HomePageEntity(pageId = 1L, title = "Main Deck", pageOrder = 0)
        val page2 = HomePageEntity(pageId = 2L, title = "Studio Deck", pageOrder = 1)
        val elementOnTopLeft = HomeElementEntity(
            elementId = 10L,
            pageId = 1L,
            type = "DIGITAL_CLOCK",
            xFraction = 0.0f,
            yFraction = 0.0f,
            widthDp = 160f,
            heightDp = 100f,
            label = "Clock",
        )

        composeRule.setContent {
            var uiState by remember {
                mutableStateOf(
                    LauncherUiState(
                        isLoading = false,
                        homePages = listOf(page1, page2),
                        homeElements = listOf(elementOnTopLeft),
                        isEditMode = false,
                        isDefaultLauncher = true,
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
                    onToggleEditMode = { enabled -> uiState = uiState.copy(isEditMode = enabled) },
                    onSelectElement = { id -> uiState = uiState.copy(selectedElementId = id) },
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
                    onCreateStarterTemplate = {},
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
        // Add, Widgets, Settings and Edit toolbar must NOT exist in normal mode
        composeRule.onNodeWithTag("studio_edit_toolbar").assertDoesNotExist()
        composeRule.onNodeWithTag("studio_add_element").assertDoesNotExist()
        composeRule.onNodeWithTag("studio_widgets_button").assertDoesNotExist()
        composeRule.onNodeWithTag("studio_settings_button").assertDoesNotExist()

        // Long-pressing inside the occupied element bounds must NOT enter edit mode
        composeRule.onNodeWithTag("home_canvas_1").performTouchInput {
            longClick(Offset(20f, 20f))
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("studio_edit_toolbar").assertDoesNotExist()

        // Long-pressing empty home space enters edit mode and shows Add, Widgets, Settings, Pages, Done
        composeRule.onNodeWithTag("home_canvas_1").performTouchInput {
            longClick(center)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("studio_edit_toolbar").assertIsDisplayed()
        composeRule.onNodeWithTag("studio_undo_button").assertIsDisplayed()
        composeRule.onNodeWithTag("studio_redo_button").assertIsDisplayed()
        composeRule.onNodeWithTag("studio_add_element").assertIsDisplayed()
        composeRule.onNodeWithTag("studio_widgets_button").assertIsDisplayed()
        composeRule.onNodeWithTag("studio_pages_button").assertIsDisplayed()
        composeRule.onNodeWithTag("studio_style_button").assertIsDisplayed()
        composeRule.onNodeWithTag("studio_grid_snap_button").assertIsDisplayed()
        composeRule.onNodeWithTag("studio_multi_select_button").assertIsDisplayed()
        composeRule.onNodeWithTag("studio_settings_button").assertIsDisplayed()
        composeRule.onNodeWithTag("studio_done_button").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_shield_10").assertIsDisplayed()

        // Tapping Done exits edit mode and hides Add / Widgets / Settings again
        composeRule.onNodeWithTag("studio_done_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("studio_edit_toolbar").assertDoesNotExist()
        composeRule.onNodeWithTag("studio_add_element").assertDoesNotExist()
        composeRule.onNodeWithTag("studio_widgets_button").assertDoesNotExist()
        composeRule.onNodeWithTag("studio_settings_button").assertDoesNotExist()
    }

    @Test
    fun `atomic page reorder and page deletion remove orphans cleanly`() = runBlocking {
        val dao = db.launcherDao()
        val p1 = dao.upsertHomePage(HomePageEntity(title = "Main Deck", pageOrder = 0))
        val p2 = dao.upsertHomePage(HomePageEntity(title = "Studio Deck", pageOrder = 1))
        val p3 = dao.upsertHomePage(HomePageEntity(title = "Widget Deck", pageOrder = 2))

        dao.upsertHomeElement(
            HomeElementEntity(pageId = p2, ownerType = "PAGE", type = "ANALOG_CLOCK", label = "Studio Clock")
        )
        dao.upsertHomeElement(
            HomeElementEntity(pageId = p1, ownerType = "PAGE", type = "DIGITAL_CLOCK", label = "Main Clock")
        )

        // Atomic reorder to [p3, p1, p2]
        dao.reorderHomePages(listOf(p3, p1, p2))
        val reordered = dao.getHomePagesOnce()
        assertEquals(listOf(p3, p1, p2), reordered.map { it.pageId })
        assertEquals(listOf(0, 1, 2), reordered.map { it.pageOrder })

        // Deleting p2 deletes both page row and its owned page elements
        dao.deleteHomePageAndElements(p2)
        val remainingPages = dao.getHomePagesOnce()
        val remainingElements = dao.getHomeElementsOnce()
        assertEquals(listOf(p3, p1), remainingPages.map { it.pageId })
        assertEquals(1, remainingElements.size)
        assertEquals(p1, remainingElements.first().pageId)
    }

    @Test
    fun `floating window overlay supports interactive edit mode widgets and element selection`() {
        val win = LauncherWindowEntity(windowId = 5L, title = "Studio Panel", widthFraction = 0.9f, heightFraction = 0.7f)
        val el = HomeElementEntity(
            elementId = 44L,
            pageId = 1L,
            ownerType = "WINDOW",
            ownerWindowId = 5L,
            type = "DIGITAL_CLOCK",
            xFraction = 0.1f,
            yFraction = 0.1f,
            widthDp = 180f,
            heightDp = 96f,
            label = "Clock",
        )
        val uiState = LauncherUiState(
            isLoading = false,
            windows = listOf(win),
            homeElements = listOf(el),
            activeOverlay = ActiveOverlay.WindowOverlay(5L),
        )

        composeRule.setContent {
            TagDeckTheme {
                ActiveOverlayHost(
                    state = uiState,
                    appDiscovery = appDiscovery,
                    onDismiss = {},
                    onSwitchToTag = {},
                    onLaunchApp = {},
                    onEditTag = {},
                    onAssignAppsToTag = {},
                    onUpdateTagLayout = {},
                    onAppLongPress = {},
                    onToggleSidebarPin = { _, _ -> },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("window_overlay_5").assertIsDisplayed()
        composeRule.onNodeWithTag("window_element_44").assertIsDisplayed()
        composeRule.onNodeWithTag("window_edit_button").assertIsDisplayed()
        composeRule.onNodeWithTag("window_add_element").assertDoesNotExist()

        composeRule.onNodeWithTag("window_edit_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("window_style_button").assertIsDisplayed()
        composeRule.onNodeWithTag("window_add_element").assertIsDisplayed()
        composeRule.onNodeWithTag("window_widgets_button").assertIsDisplayed()
        composeRule.onNodeWithTag("window_done_button").assertIsDisplayed()
        composeRule.onNodeWithTag("window_overlay_close").assertIsDisplayed()
        composeRule.onNodeWithTag("window_shield_44").assertIsDisplayed()

        // Selecting placed window element reveals inspect and resize handles
        composeRule.onNodeWithTag("window_shield_44").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("window_inspect_element_44").assertIsDisplayed()
        composeRule.onNodeWithTag("window_resize_element_44").assertIsDisplayed()
    }

    @Test
    fun `icon pack order parsing handles empty, trimmed, and deduplicated package lists`() {
        assertEquals(emptyList<String>(), LauncherSettings(iconPackOrderCsv = "").iconPackOrder())
        assertEquals(
            listOf("com.pack.alpha", "com.pack.beta"),
            LauncherSettings(iconPackOrderCsv = " com.pack.alpha , , com.pack.beta , com.pack.alpha ").iconPackOrder(),
        )
    }
}
