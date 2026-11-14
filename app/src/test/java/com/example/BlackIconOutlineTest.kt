package com.example

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.data.prefs.LauncherSettings
import com.example.ui.components.IconOutlineProcessor
import com.example.ui.components.IconStyleConfig
import com.example.ui.screens.SettingsScreen
import com.example.ui.state.LauncherUiState
import com.example.ui.theme.TagDeckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BlackIconOutlineTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun createGlyphBitmap(colorInt: Int): Bitmap {
        val w = 64
        val h = 64
        val pixels = IntArray(w * h)
        for (y in 16 until 48) {
            for (x in 16 until 48) {
                pixels[y * w + x] = colorInt
            }
        }
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, w, 0, 0, w, h)
        }
    }

    @Test
    fun `default launcher settings enable white fill and black outline for black icons`() {
        val defaults = LauncherSettings()
        assertTrue(defaults.blackIconStyleEnabled)
        assertEquals(IconStyleConfig.MODE_BLACK_ICONS, defaults.blackIconMode)
        assertEquals(0xFFFFFFFFL, defaults.blackIconFillColorHex)
        assertEquals(0xFF000000L, defaults.blackIconOutlineColorHex)
        assertEquals(2.0f, defaults.blackIconOutlineWidthDp, 0.001f)
    }

    @Test
    fun `black glyph icon is transformed to pure white with crisp black outline and transparent corners`() {
        val src = createGlyphBitmap(Color.BLACK)
        val out = IconOutlineProcessor.transformBitmap(src, IconStyleConfig())

        assertNotSame(src, out)
        // Center glyph pixel is white
        val center = out.getPixel(32, 32)
        assertEquals(Color.WHITE, center)

        // Neighboring pixel just outside the glyph [14, 32] is black outline
        val outline = out.getPixel(14, 32)
        assertEquals(255, Color.alpha(outline))
        assertEquals(0, Color.red(outline))
        assertEquals(0, Color.green(outline))
        assertEquals(0, Color.blue(outline))

        // Far corner [0, 0] stays transparent
        assertEquals(0, Color.alpha(out.getPixel(0, 0)))
    }

    @Test
    fun `colorful icon is preserved unchanged in default BLACK_ICONS mode`() {
        val w = 64
        val h = 64
        val pixels = IntArray(w * h)
        for (y in 12 until 52) {
            for (x in 12 until 32) pixels[y * w + x] = Color.rgb(30, 144, 255)
            for (x in 32 until 52) pixels[y * w + x] = Color.rgb(255, 80, 90)
        }
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, w, 0, 0, w, h)
        }

        val out = IconOutlineProcessor.transformBitmap(bmp, IconStyleConfig())
        assertSame(bmp, out)
    }

    @Test
    fun `dark multicolored transparent icon is preserved unchanged`() {
        val w = 64
        val h = 64
        val pixels = IntArray(w * h)
        for (y in 16 until 48) {
            for (x in 16 until 48) {
                pixels[y * w + x] = if (x < 42) {
                    Color.rgb(35, 35, 35)
                } else {
                    Color.rgb(10, 40, 90)
                }
            }
        }
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, w, 0, 0, w, h)
        }

        val out = IconOutlineProcessor.transformBitmap(bmp, IconStyleConfig())
        assertSame(bmp, out)
    }

    @Test
    fun `disabling black icon styling returns original black icon unchanged`() {
        val src = createGlyphBitmap(Color.BLACK)
        val out = IconOutlineProcessor.transformBitmap(
            src,
            IconStyleConfig(enabled = false),
        )
        assertSame(src, out)
    }

    @Test
    fun `ALL_MONOCHROME mode converts gray glyph to custom fill and outline colors`() {
        val graySrc = createGlyphBitmap(Color.rgb(190, 190, 190))
        // Default BLACK_ICONS ignores bright gray
        val untouched = IconOutlineProcessor.transformBitmap(
            graySrc,
            IconStyleConfig(mode = IconStyleConfig.MODE_BLACK_ICONS),
        )
        assertSame(graySrc, untouched)

        // ALL_MONOCHROME converts gray glyph to custom cyan fill + dark navy outline
        val converted = IconOutlineProcessor.transformBitmap(
            graySrc,
            IconStyleConfig(
                enabled = true,
                mode = IconStyleConfig.MODE_ALL_MONOCHROME,
                fillColorHex = 0xFF38BDF8L,
                outlineColorHex = 0xFF1E1B4BL,
                outlineWidthDp = 2.0f,
            ),
        )
        assertNotSame(graySrc, converted)
        assertEquals(Color.rgb(0x38, 0xBD, 0xF8), converted.getPixel(32, 32))
        val borderPx = converted.getPixel(14, 32)
        assertEquals(255, Color.alpha(borderPx))
        assertEquals(Color.rgb(0x1E, 0x1B, 0x4B), borderPx)
    }

    @Test
    fun `settings screen shows black icon contrast controls enabled by default and allows adjustment`() {
        var latestSettings = LauncherSettings()
        composeRule.setContent {
            var uiState by remember {
                mutableStateOf(
                    LauncherUiState(
                        isLoading = false,
                        settings = latestSettings,
                    )
                )
            }
            TagDeckTheme {
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
                    onShowMessage = {},
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("toggle_black_icon_style"))
        composeRule.onNodeWithTag("toggle_black_icon_style").assertIsDisplayed().assertIsOn()

        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("black_icon_mode_${IconStyleConfig.MODE_ALL_MONOCHROME}"))
        composeRule.onNodeWithTag("black_icon_mode_${IconStyleConfig.MODE_ALL_MONOCHROME}").performClick()
        composeRule.waitForIdle()
        assertEquals(IconStyleConfig.MODE_ALL_MONOCHROME, latestSettings.blackIconMode)

        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("reset_black_icon_style_button"))
        composeRule.onNodeWithTag("reset_black_icon_style_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(IconStyleConfig.MODE_BLACK_ICONS, latestSettings.blackIconMode)
        assertTrue(latestSettings.blackIconStyleEnabled)

        composeRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("toggle_black_icon_style"))
        composeRule.onNodeWithTag("toggle_black_icon_style").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("toggle_black_icon_style").assertIsOff()
        composeRule.onNodeWithText("Reset to Default (White + Black Outline)").assertDoesNotExist()
    }
}
