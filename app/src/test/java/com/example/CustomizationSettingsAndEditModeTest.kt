package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.LauncherWindowEntity
import com.example.data.prefs.LauncherSettings
import com.example.data.system.BackupManager
import com.example.data.system.FullBackupPayload
import com.example.ui.components.StudioQuickCustomizerDialog
import com.example.ui.components.WindowStyleQuickDialog
import com.example.ui.screens.ElementInspectorDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.state.LauncherUiState
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.TagDeckTheme
import com.example.ui.theme.buildTagDeckTypography
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CustomizationSettingsAndEditModeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `backup manager round-trips all expanded launcher visual settings`() {
        val customSettings = LauncherSettings(
            themeMode = "LIGHT",
            accentColorHex = 0xFFFB7185L,
            secondaryColorHex = 0xFF34D399L,
            tertiaryColorHex = 0xFFFBBF24L,
            fontStyle = "JETBRAINS_MONO",
            fontScale = 1.25f,
            surfaceOpacity = 0.62f,
            globalBorderWidthDp = 2.5f,
            globalTextColorHex = 0xFF000000L,
            showHomeAppLabels = false,
            wallpaperType = "GRADIENT",
            wallpaperGradientDirection = "DIAGONAL",
            wallpaperDimColorHex = 0xFF1E1B4BL,
            drawerLayoutMode = "LIST",
            drawerSortOrder = "MOST_USED",
            drawerCardOpacity = 0.70f,
            drawerCornerRadiusDp = 14f,
            drawerSearchBarVisible = false,
            alignmentGuideColorHex = 0xFFFBBF24L,
            editGridVisible = false,
            editGridOpacity = 0.22f,
            editGridColorHex = 0xFF38BDF8L,
            pageTransitionStyle = "DEPTH",
            pageIndicatorStyle = "NUMBERS",
            pageIndicatorColorHex = 0xFF34D399L,
            edgeHandlesVisible = false,
            edgeHandleOpacity = 0.8f,
            edgeHandleWidthDp = 8f,
            edgeHandleHeightDp = 96f,
            showDefaultHomeBanner = false,
        )
        val payload = FullBackupPayload(
            apps = emptyList(),
            tags = emptyList(),
            crossRefs = emptyList(),
            rules = emptyList(),
            pages = listOf(HomePageEntity(pageId = 1L, title = "Home", paddingDp = 12f)),
            elements = emptyList(),
            sidebars = emptyList(),
            gestures = emptyList(),
            settings = customSettings,
        )

        val encoded = BackupManager.encodeFullBackup(payload)
        val decoded = BackupManager.decodeFullBackup(encoded)

        val s = decoded.settings
        assertEquals("LIGHT", s.themeMode)
        assertEquals(0xFFFB7185L, s.accentColorHex)
        assertEquals(0xFF34D399L, s.secondaryColorHex)
        assertEquals(0xFFFBBF24L, s.tertiaryColorHex)
        assertEquals("JETBRAINS_MONO", s.fontStyle)
        assertEquals(1.25f, s.fontScale, 0.01f)
        assertEquals(0.62f, s.surfaceOpacity, 0.01f)
        assertEquals(2.5f, s.globalBorderWidthDp, 0.01f)
        assertEquals(0xFF000000L, s.globalTextColorHex)
        assertFalse(s.showHomeAppLabels)
        assertEquals("DIAGONAL", s.wallpaperGradientDirection)
        assertEquals(0xFF1E1B4BL, s.wallpaperDimColorHex)
        assertEquals("LIST", s.drawerLayoutMode)
        assertEquals("MOST_USED", s.drawerSortOrder)
        assertEquals(0.70f, s.drawerCardOpacity, 0.01f)
        assertEquals(14f, s.drawerCornerRadiusDp, 0.01f)
        assertFalse(s.drawerSearchBarVisible)
        assertEquals(0xFFFBBF24L, s.alignmentGuideColorHex)
        assertFalse(s.editGridVisible)
        assertEquals(0.22f, s.editGridOpacity, 0.01f)
        assertEquals(0xFF38BDF8L, s.editGridColorHex)
        assertEquals("DEPTH", s.pageTransitionStyle)
        assertEquals("NUMBERS", s.pageIndicatorStyle)
        assertEquals(0xFF34D399L, s.pageIndicatorColorHex)
        assertFalse(s.edgeHandlesVisible)
        assertEquals(0.8f, s.edgeHandleOpacity, 0.01f)
        assertEquals(8f, s.edgeHandleWidthDp, 0.01f)
        assertEquals(96f, s.edgeHandleHeightDp, 0.01f)
        assertFalse(s.showDefaultHomeBanner)
    }

    @Test
    fun `buildTagDeckTypography applies font family presets and scale`() {
        val defaultTypo = buildTagDeckTypography("DEFAULT", 1.0f)
        assertEquals(SpaceGroteskFamily, defaultTypo.headlineLarge.fontFamily)
        assertEquals(PlusJakartaSansFamily, defaultTypo.bodyLarge.fontFamily)
        assertEquals(15f, defaultTypo.bodyLarge.fontSize.value, 0.01f)

        val monoScaled = buildTagDeckTypography("JETBRAINS_MONO", 1.2f)
        assertEquals(JetBrainsMonoFamily, monoScaled.headlineLarge.fontFamily)
        assertEquals(JetBrainsMonoFamily, monoScaled.bodyLarge.fontFamily)
        assertEquals(18f, monoScaled.bodyLarge.fontSize.value, 0.01f)
    }

    @Test
    fun `settings screen edits typography wallpaper drawer indicator and resets all defaults`() {
        var latestSettings = LauncherSettings(seededDefaults = true, recentSearchesCsv = "calc")
        var toastMessage = ""

        composeRule.setContent {
            var uiState by remember {
                mutableStateOf(
                    LauncherUiState(
                        isLoading = false,
                        settings = latestSettings,
                    )
                )
            }
            TagDeckTheme(
                themeMode = uiState.settings.themeMode,
                accentColorHex = uiState.settings.accentColorHex,
                secondaryColorHex = uiState.settings.secondaryColorHex,
                tertiaryColorHex = uiState.settings.tertiaryColorHex,
                fontStyle = uiState.settings.fontStyle,
                fontScale = uiState.settings.fontScale,
            ) {
                SettingsScreen(
                    state = uiState,
                    onBack = {},
                    onNavigate = {},
                    onRequestDefaultLauncher = {},
                    onUpdateSettings = { transform ->
                        val next = transform(uiState.settings)
                        latestSettings = next
                        uiState = uiState.copy(settings = next)
                    },
                    onSaveGesture = {},
                    onImportImage = { _, _, _ -> null },
                    onRenameImageResource = { _, _, _ -> },
                    onDeleteImageResource = {},
                    onSaveBackgroundPreset = {},
                    onDeleteBackgroundPreset = {},
                    onExportBackupJson = { "{}" },
                    onImportBackupJson = {},
                    onShowMessage = { toastMessage = it },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_font_JETBRAINS_MONO"))
        composeRule.onNodeWithTag("settings_font_JETBRAINS_MONO").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("JETBRAINS_MONO", latestSettings.fontStyle)

        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_wallpaper_GRADIENT"))
        composeRule.onNodeWithTag("settings_wallpaper_GRADIENT").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("GRADIENT", latestSettings.wallpaperType)

        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_wp_dir_DIAGONAL"))
        composeRule.onNodeWithTag("settings_wp_dir_DIAGONAL").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("DIAGONAL", latestSettings.wallpaperGradientDirection)

        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_drawer_layout_LIST"))
        composeRule.onNodeWithTag("settings_drawer_layout_LIST").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("LIST", latestSettings.drawerLayoutMode)

        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("settings_indicator_style_NUMBERS"))
        composeRule.onNodeWithTag("settings_indicator_style_NUMBERS").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("NUMBERS", latestSettings.pageIndicatorStyle)

        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("reset_all_settings_button"))
        composeRule.onNodeWithTag("reset_all_settings_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("confirm_reset_all_settings_button").performClick()
        composeRule.waitForIdle()

        assertEquals("DEFAULT", latestSettings.fontStyle)
        assertEquals("BUILTIN_PRISM", latestSettings.wallpaperType)
        assertEquals("GRID", latestSettings.drawerLayoutMode)
        assertEquals("DOTS", latestSettings.pageIndicatorStyle)
        assertTrue(latestSettings.seededDefaults)
        assertEquals("calc", latestSettings.recentSearchesCsv)
        assertEquals("All launcher preferences restored to defaults", toastMessage)
    }

    @Test
    fun `edit mode style studio customizes page canvas theme and wallpaper`() {
        var savedPage = HomePageEntity(pageId = 2L, title = "Studio Deck")
        var latestSettings = LauncherSettings()
        var dismissed = false

        composeRule.setContent {
            var uiPage by remember { mutableStateOf(savedPage) }
            var uiSettings by remember { mutableStateOf(latestSettings) }
            TagDeckTheme {
                StudioQuickCustomizerDialog(
                    currentPage = uiPage,
                    settings = uiSettings,
                    onSavePage = {
                        savedPage = it
                        uiPage = it
                    },
                    onUpdateSettings = { transform ->
                        val next = transform(uiSettings)
                        latestSettings = next
                        uiSettings = next
                    },
                    onImportImage = { _, _, _ -> null },
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("studio_page_title_input").performTextReplacement("Creative Deck")
        composeRule.onNodeWithTag("studio_page_bg_TRANSPARENT").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("TRANSPARENT", savedPage.backgroundType)

        composeRule.onNodeWithTag("studio_style_tab_CANVAS").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("studio_indicator_style_PILL").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("PILL", latestSettings.pageIndicatorStyle)

        composeRule.onNodeWithTag("studio_style_tab_THEME").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("studio_font_SPACE_GROTESK").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("SPACE_GROTESK", latestSettings.fontStyle)

        composeRule.onNodeWithTag("studio_style_tab_WALLPAPER").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("studio_wallpaper_SOLID").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("SOLID", latestSettings.wallpaperType)

        composeRule.onNodeWithTag("studio_style_done").performClick()
        composeRule.waitForIdle()
        assertEquals("Creative Deck", savedPage.title)
        assertTrue(dismissed)
    }

    @Test
    fun `element inspector tabs customize layout style mask and label`() {
        var updatedElement = HomeElementEntity(
            elementId = 7L,
            pageId = 1L,
            type = "CUSTOM_BUTTON",
            label = "Launch",
            zIndex = 2,
        )
        var dismissed = false

        composeRule.setContent {
            TagDeckTheme {
                ElementInspectorDialog(
                    element = updatedElement,
                    state = LauncherUiState(isLoading = false),
                    onImportImage = { _, _, _ -> null },
                    onUpdate = { updatedElement = it },
                    onDuplicate = {},
                    onCopy = {},
                    onCut = {},
                    onBringForward = {},
                    onSendBackward = {},
                    onDelete = {},
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("inspector_z_plus").performScrollTo().performClick()

        composeRule.onNodeWithTag("inspector_tab_STYLE").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("inspector_mask_HEXAGON").performScrollTo().performClick()

        composeRule.onNodeWithTag("inspector_tab_TEXT").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("inspector_label_input").performTextReplacement("Run Macro")

        composeRule.onNodeWithTag("inspector_apply_button").performClick()
        composeRule.waitForIdle()

        assertEquals(3, updatedElement.zIndex)
        assertEquals("HEXAGON", updatedElement.imageMaskShape)
        assertEquals("Run Macro", updatedElement.label)
        assertTrue(dismissed)
    }

    @Test
    fun `floating window style dialog updates title fullscreen and animation`() {
        var savedWindow = LauncherWindowEntity(windowId = 5L, title = "Mini Dock")
        var dismissed = false

        composeRule.setContent {
            TagDeckTheme {
                WindowStyleQuickDialog(
                    window = savedWindow,
                    onSaveWindow = { savedWindow = it },
                    onImportImage = { _, _, _ -> null },
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("window_style_title_input").performTextReplacement("Focus Panel")
        composeRule.onNodeWithText("FADE").performScrollTo().performClick()
        composeRule.onNodeWithTag("window_style_save").performClick()
        composeRule.waitForIdle()

        assertEquals("Focus Panel", savedWindow.title)
        assertEquals("FADE", savedWindow.animationStyle)
        assertTrue(dismissed)
    }
}
