package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppEntity
import com.example.data.db.AppWithTags
import com.example.data.db.CategoryEntity
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.LauncherWindowEntity
import com.example.data.db.SidebarConfigEntity
import com.example.data.db.TagEntity
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.LauncherWidgetHost
import com.example.ui.components.UniversalSearchOverlay
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LauncherSettingsCatalog
import com.example.ui.screens.SettingsScreen
import com.example.ui.state.LauncherUiState
import com.example.ui.theme.TagDeckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GlobalAndSettingsSearchTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var appDiscovery: AppDiscoveryManager
    private lateinit var widgetHost: LauncherWidgetHost

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        appDiscovery = AppDiscoveryManager(context)
        widgetHost = LauncherWidgetHost(context)
    }

    @Test
    fun `settings catalog matches multi-word queries across sections and entries`() {
        val blackOutlineMatches = LauncherSettingsCatalog.searchSettingsEntries("black outline")
        assertTrue(blackOutlineMatches.any { it.sectionId == LauncherSettingsCatalog.SECTION_BLACK_ICONS })

        val gridSnapMatches = LauncherSettingsCatalog.searchSettingsEntries("grid snap")
        assertTrue(gridSnapMatches.any { it.sectionId == LauncherSettingsCatalog.SECTION_HOME_CANVAS })

        val filteredSections = LauncherSettingsCatalog.filterSections("backup json")
        assertEquals(setOf(LauncherSettingsCatalog.SECTION_BACKUP), filteredSections.keys)
    }

    @Test
    fun `settings screen filters sections dynamically and supports initial deep-link query and empty state`() {
        var consumedInitial = false

        composeRule.setContent {
            val state = LauncherUiState(isLoading = false)
            TagDeckTheme {
                SettingsScreen(
                    state = state,
                    onBack = {},
                    onNavigate = {},
                    onRequestDefaultLauncher = {},
                    onUpdateSettings = {},
                    onSaveGesture = {},
                    onImportImage = { _, _, _ -> null },
                    onRenameImageResource = { _, _, _ -> },
                    onDeleteImageResource = {},
                    onSaveBackgroundPreset = {},
                    onDeleteBackgroundPreset = {},
                    onExportBackupJson = { "{}" },
                    onImportBackupJson = {},
                    onShowMessage = {},
                    initialSearchQuery = "Wallpaper",
                    onConsumeInitialSearchQuery = { consumedInitial = true },
                )
            }
        }

        composeRule.waitForIdle()
        assertTrue(consumedInitial)
        composeRule.onNodeWithText("Wallpaper & Atmosphere").assertIsDisplayed()
        composeRule.onNodeWithText("Portable Local Backup & Restore").assertDoesNotExist()

        // Search for unmatched query -> empty state + clear button restores full list
        composeRule.onNodeWithTag("settings_search_input").performTextReplacement("zzznomatchsetting")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_search_empty").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_search_reset_query").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Manage Your Launcher").assertIsDisplayed()
    }

    @Test
    fun `global search finds apps settings windows sidebars widgets and deep-links into settings search`() {
        var settingsSearchOpenedWith = ""
        var openedWindowId: Long? = null
        var openedSidebarId: Long? = null
        var jumpedPageIdx: Int? = null

        val state = LauncherUiState(
            isLoading = false,
            appsWithTags = listOf(
                AppWithTags(
                    app = AppEntity("com.example.notes", appName = "Notes Pro"),
                    tags = listOf(TagEntity(1L, "WORK")),
                )
            ),
            allTags = listOf(TagEntity(1L, "WORK")),
            categories = listOf(CategoryEntity(20L, "Productivity")),
            windows = listOf(LauncherWindowEntity(windowId = 7L, title = "Studio Floating Dock")),
            sidebars = listOf(SidebarConfigEntity(sidebarId = 3L, title = "Studio Edge Dock", side = "LEFT")),
            homePages = listOf(
                HomePageEntity(pageId = 1L, title = "Main Deck", pageOrder = 0),
                HomePageEntity(pageId = 2L, title = "Studio Deck", pageOrder = 1),
            ),
            homeElements = listOf(
                HomeElementEntity(
                    elementId = 11L,
                    pageId = 2L,
                    type = "BATTERY_WIDGET",
                    label = "Studio Battery",
                )
            ),
            isUniversalSearchOpen = true,
        )

        composeRule.setContent {
            TagDeckTheme {
                UniversalSearchOverlay(
                    visible = true,
                    state = state,
                    appDiscovery = appDiscovery,
                    onClose = {},
                    onLaunchApp = {},
                    onOpenTagSidebar = {},
                    onOpenCategorySidebar = {},
                    onOpenRuleSidebar = {},
                    onOpenFolder = {},
                    onJumpToPage = { jumpedPageIdx = it },
                    onAddHistory = {},
                    onClearHistory = {},
                    onOpenConfiguredSidebar = { openedSidebarId = it },
                    onOpenWindow = { openedWindowId = it },
                    onOpenSettingsSearch = { settingsSearchOpenedWith = it },
                )
            }
        }

        composeRule.waitForIdle()

        // Search for a setting
        composeRule.onNodeWithTag("universal_search_input").performTextReplacement("black outline")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("global_search_setting_black_icon_toggle").assertIsDisplayed()
        composeRule.onNodeWithTag("global_search_setting_black_icon_toggle").performClick()
        assertEquals("White + Black Outline for Black Icons", settingsSearchOpenedWith)

        // Search across pages, windows, sidebars, widgets
        composeRule.onNodeWithTag("universal_search_input").performTextReplacement("studio")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("global_search_window_7").assertIsDisplayed()
        composeRule.onNodeWithTag("global_search_sidebar_3").assertIsDisplayed()
        composeRule.onNodeWithTag("global_search_element_11").assertIsDisplayed()
        composeRule.onNodeWithText("Studio Deck").assertIsDisplayed()

        composeRule.onNodeWithTag("global_search_window_7").performClick()
        assertEquals(7L, openedWindowId)

        composeRule.onNodeWithTag("global_search_sidebar_3").performClick()
        assertEquals(3L, openedSidebarId)

        composeRule.onNodeWithText("Studio Deck").performClick()
        assertEquals(1, jumpedPageIdx)

        // Empty query result shows clean empty state
        composeRule.onNodeWithTag("universal_search_input").performTextReplacement("xyznonexistent999")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("universal_search_empty").assertIsDisplayed()
    }

    @Test
    fun `home screen shows global search bar outside edit mode and opens search on tap`() {
        var searchOpened = false
        var drawerOpened = false

        composeRule.setContent {
            var uiState by remember {
                mutableStateOf(
                    LauncherUiState(
                        isLoading = false,
                        homePages = listOf(HomePageEntity(pageId = 1L, title = "Main", pageOrder = 0)),
                        isDefaultLauncher = true,
                        isEditMode = false,
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
                    onOpenDrawer = { drawerOpened = true },
                    onOpenSearch = { searchOpened = true },
                    onNavigate = {},
                    onToggleEditMode = { uiState = uiState.copy(isEditMode = it) },
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
        composeRule.onNodeWithTag("home_global_search_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("home_open_drawer_button").assertIsDisplayed()
        composeRule.onNodeWithTag("studio_add_element").assertDoesNotExist()
        composeRule.onNodeWithTag("studio_widgets_button").assertDoesNotExist()
        composeRule.onNodeWithTag("studio_settings_button").assertDoesNotExist()

        composeRule.onNodeWithTag("home_global_search_bar").performClick()
        assertTrue(searchOpened)

        composeRule.onNodeWithTag("home_open_drawer_button").performClick()
        assertTrue(drawerOpened)
    }
}
