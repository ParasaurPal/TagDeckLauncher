package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.data.db.HomePageEntity
import com.example.data.prefs.LauncherSettings
import com.example.data.system.BackupManager
import com.example.data.system.FullBackupPayload
import com.example.ui.components.AnyconsConfig
import com.example.ui.components.AnyconsGlyphCatalog
import com.example.ui.components.AnyconsRenderer
import com.example.ui.screens.SettingsScreen
import com.example.ui.state.LauncherUiState
import com.example.ui.theme.TagDeckTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AnyconsRendererTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `glyph catalog resolves common apps and category fallbacks`() {
        assertEquals("phone", AnyconsGlyphCatalog.resolveGlyphKey("com.google.android.dialer", "Phone", 7L))
        assertEquals("messages", AnyconsGlyphCatalog.resolveGlyphKey("com.whatsapp", "WhatsApp", 7L))
        assertEquals("mail", AnyconsGlyphCatalog.resolveGlyphKey("com.google.android.gm", "Gmail", 7L))
        assertEquals("browser", AnyconsGlyphCatalog.resolveGlyphKey("com.android.chrome", "Chrome", 29L))
        assertEquals("music", AnyconsGlyphCatalog.resolveGlyphKey("com.spotify.music", "Spotify", 20L))
        assertEquals("camera", AnyconsGlyphCatalog.resolveGlyphKey("com.android.camera", "Camera", 24L))
        assertEquals("maps", AnyconsGlyphCatalog.resolveGlyphKey("com.google.android.apps.maps", "Maps", 18L))
        assertEquals("games", AnyconsGlyphCatalog.resolveGlyphKey("com.unknown.arcade", "Unknown", 100L))
        assertEquals("weather", AnyconsGlyphCatalog.resolveGlyphKey("com.unknown.sky", "Unknown", 32L))
    }

    @Test
    fun `all eleven anycons vector styles render distinct non-empty bitmaps`() {
        val signatures = mutableSetOf<Int>()
        val styles = AnyconsConfig.styles.filter { it != "NONE" }
        assertEquals(11, styles.size)

        styles.forEach { style ->
            val bmp = AnyconsRenderer.renderForApp(
                packageName = "com.android.camera",
                appName = "Camera",
                categoryId = 24L,
                sourceBitmap = null,
                foregroundBitmap = null,
                config = AnyconsConfig(
                    style = style,
                    sourceMode = AnyconsConfig.SOURCE_GLYPHS,
                    isDarkTheme = true,
                ),
            )
            assertNotNull("Expected non-null bitmap for $style", bmp)
            val nonTransparent = countOpaquePixels(bmp!!)
            assertTrue("Expected visible artwork for $style (found $nonTransparent)", nonTransparent > 100)
            signatures.add(samplePixelHash(bmp))
        }
        assertTrue("Expected styles to produce distinct visual outputs", signatures.size >= 9)
    }

    @Test
    fun `bitmap fallback renders all styles cleanly even on solid plate icons`() {
        val sample = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888).apply {
            val c = Canvas(this)
            c.drawColor(0xFF1E88E5.toInt())
            c.drawCircle(64f, 64f, 34f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() })
        }

        AnyconsConfig.styles.filter { it != "NONE" }.forEach { style ->
            val rendered = AnyconsRenderer.render(
                sample,
                AnyconsConfig(style = style, sourceMode = AnyconsConfig.SOURCE_APP_ARTWORK),
            )
            assertTrue("Expected visible pixels for bitmap fallback $style", countOpaquePixels(rendered) > 60)
        }
    }

    @Test
    fun `backup manager round-trips anycons settings`() {
        val custom = LauncherSettings(
            anyconsStyle = "STICKER",
            anyconsSourceMode = AnyconsConfig.SOURCE_HYBRID,
            anyconsColorMode = AnyconsConfig.COLOR_VIBRANT,
            anyconsStrokeScale = 1.35f,
        )
        val payload = FullBackupPayload(
            apps = emptyList(),
            tags = emptyList(),
            crossRefs = emptyList(),
            rules = emptyList(),
            pages = listOf(HomePageEntity(pageId = 1L, title = "Home")),
            elements = emptyList(),
            sidebars = emptyList(),
            gestures = emptyList(),
            settings = custom,
        )
        val decoded = BackupManager.decodeFullBackup(BackupManager.encodeFullBackup(payload))
        assertEquals("STICKER", decoded.settings.anyconsStyle)
        assertEquals(AnyconsConfig.SOURCE_HYBRID, decoded.settings.anyconsSourceMode)
        assertEquals(AnyconsConfig.COLOR_VIBRANT, decoded.settings.anyconsColorMode)
        assertEquals(1.35f, decoded.settings.anyconsStrokeScale, 0.01f)
    }

    @Test
    fun `settings screen allows selecting anycons style source and color mode`() {
        var latestSettings = LauncherSettings(seededDefaults = true)
        composeRule.setContent {
            var uiState by remember {
                mutableStateOf(LauncherUiState(isLoading = false, settings = latestSettings))
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
        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("anycons_style_STICKER"))
        composeRule.onNodeWithTag("anycons_style_STICKER").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("STICKER", latestSettings.anyconsStyle)

        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("anycons_source_HYBRID"))
        composeRule.onNodeWithTag("anycons_source_HYBRID").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(AnyconsConfig.SOURCE_HYBRID, latestSettings.anyconsSourceMode)

        composeRule.onNodeWithTag("settings_list").performScrollToNode(hasTestTag("anycons_color_VIBRANT"))
        composeRule.onNodeWithTag("anycons_color_VIBRANT").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(AnyconsConfig.COLOR_VIBRANT, latestSettings.anyconsColorMode)
    }

    private fun countOpaquePixels(bmp: Bitmap): Int {
        val px = IntArray(bmp.width * bmp.height)
        bmp.getPixels(px, 0, bmp.width, 0, 0, bmp.width, bmp.height)
        return px.count { ((it ushr 24) and 0xFF) > 20 }
    }

    private fun samplePixelHash(bmp: Bitmap): Int {
        val px = IntArray(bmp.width * bmp.height)
        bmp.getPixels(px, 0, bmp.width, 0, 0, bmp.width, bmp.height)
        var h = 17
        for (i in px.indices step 23) {
            h = h * 31 + px[i]
        }
        return h
    }
}
