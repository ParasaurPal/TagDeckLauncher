package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppCategorizer
import com.example.data.db.AppCategoryCrossRef
import com.example.data.db.AppEntity
import com.example.data.db.AppTagCrossRef
import com.example.data.db.AppWithTags
import com.example.data.db.BackgroundPresetEntity
import com.example.data.db.CategoryEntity
import com.example.data.db.GestureBindingEntity
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.ImageResourceEntity
import com.example.data.db.LauncherWindowEntity
import com.example.data.db.PlayCategoryCatalog
import com.example.data.db.SavedTagRuleEntity
import com.example.data.db.SidebarConfigEntity
import com.example.data.db.TagEntity
import com.example.data.prefs.LauncherSettings
import com.example.data.repository.TagFilterEngine
import com.example.data.repository.TagMatchMode
import com.example.data.system.BackupManager
import com.example.data.system.FullBackupPayload
import com.example.ui.components.appIndexLetter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagFilterAndBackupUnitTest {

    private val productivity = TagEntity(tagId = 1L, name = "PRODUCTIVITY")
    private val creative = TagEntity(tagId = 2L, name = "CREATIVE")
    private val games = TagEntity(tagId = 3L, name = "GAMES")
    private val subDesign = TagEntity(tagId = 4L, name = "UI DESIGN", parentTagId = 2L)

    private val sampleApps = listOf(
        AppWithTags(
            app = AppEntity(packageName = "com.figma", appName = "Figma"),
            tags = listOf(productivity, creative, subDesign),
        ),
        AppWithTags(
            app = AppEntity(packageName = "com.unity.game", appName = "Game Studio"),
            tags = listOf(productivity, creative, games),
        ),
        AppWithTags(
            app = AppEntity(packageName = "com.notes", appName = "Notes"),
            tags = listOf(productivity),
        ),
        AppWithTags(
            app = AppEntity(packageName = "com.hidden", appName = "Hidden App", hidden = true),
            tags = listOf(productivity, creative),
        ),
    )

    @Test
    fun `AND filter with EXCLUDE matches only Productivity and Creative excluding Games`() {
        val result = TagFilterEngine.filterApps(
            appsWithTags = sampleApps,
            includeTagIds = setOf(1L, 2L),
            excludeTagIds = setOf(3L),
            matchMode = TagMatchMode.AND,
        )
        assertEquals(listOf("com.figma"), result.map { it.app.packageName })
    }

    @Test
    fun `OR filter with EXCLUDE matches Productivity or Creative excluding Games`() {
        val result = TagFilterEngine.filterApps(
            appsWithTags = sampleApps,
            includeTagIds = setOf(1L, 2L),
            excludeTagIds = setOf(3L),
            matchMode = TagMatchMode.OR,
        )
        assertEquals(listOf("com.figma", "com.notes"), result.map { it.app.packageName })
    }

    @Test
    fun `collectDescendantTagIds resolves nested child tags`() {
        val descendants = TagFilterEngine.collectDescendantTagIds(
            rootTagId = 2L,
            allTags = listOf(productivity, creative, games, subDesign),
        )
        assertEquals(setOf(2L, 4L), descendants)
    }

    @Test
    fun `play store catalog defines 32 app categories plus games parent and 17 game subcategories`() {
        assertEquals(32, PlayCategoryCatalog.appCategories.size)
        assertEquals(17, PlayCategoryCatalog.gameSubcategories.size)
        assertEquals(50, PlayCategoryCatalog.allSeedCategories.size)
        assertTrue(PlayCategoryCatalog.gameSubcategories.all { it.parentCategoryId == PlayCategoryCatalog.GAMES_PARENT_ID })
    }

    @Test
    fun `app categorizer distinguishes games, communication, music, and uncertain apps`() {
        val context: Context = ApplicationProvider.getApplicationContext()

        val minecraft = AppCategorizer.classify(context, "com.mojang.minecraftpe", "Minecraft")
        assertTrue(minecraft.isGame)
        assertEquals(PlayCategoryCatalog.GAMES_PARENT_ID, minecraft.primaryCategoryId)
        assertTrue(minecraft.additionalCategoryIds.contains(113L)) // Simulation

        val spotify = AppCategorizer.classify(context, "com.spotify.music", "Spotify")
        assertFalse(spotify.isGame)
        assertEquals(20L, spotify.primaryCategoryId) // Music & Audio

        val unknown = AppCategorizer.classify(context, "org.example.customutil99", "ZzzMystery")
        assertEquals("UNCERTAIN", unknown.confidence)
        assertEquals(29L, unknown.primaryCategoryId) // Tools fallback
    }

    @Test
    fun `niagara index letter normalizes digits and symbols to hash`() {
        assertEquals("M", appIndexLetter("Minecraft"))
        assertEquals("A", appIndexLetter("  apple music"))
        assertEquals("#", appIndexLetter("1Password"))
        assertEquals("#", appIndexLetter(""))
    }

    @Test
    fun `full backup JSON roundtrip preserves v2 entities, categories, windows, images, and settings`() {
        val original = FullBackupPayload(
            apps = listOf(
                AppEntity(
                    packageName = "com.figma",
                    componentName = "com.figma/.Main",
                    appName = "Figma",
                    customName = "Design Hub",
                    primaryCategoryId = 1L,
                    automaticCategoryId = 25L,
                    manualCategoryOverride = true,
                    favorite = true,
                )
            ),
            categories = listOf(
                CategoryEntity(categoryId = 200L, name = "Dev Tools", categoryType = "CUSTOM", isCustom = true)
            ),
            appCategories = listOf(
                AppCategoryCrossRef("com.figma", 1L, isManualAssignment = true, isExcluded = false)
            ),
            tags = listOf(productivity, creative, subDesign),
            crossRefs = listOf(AppTagCrossRef("com.figma", 1L), AppTagCrossRef("com.figma", 2L)),
            rules = listOf(SavedTagRuleEntity(1L, "Work Focus", matchMode = "AND", includeTagIdsCsv = "1,2", excludeTagIdsCsv = "3", includeNestedTags = true)),
            pages = listOf(HomePageEntity(1L, "Main Deck", 0, isLocked = true, verticallyScrollable = true)),
            windows = listOf(LauncherWindowEntity(1L, "Quick Hub", fullScreen = false, widthFraction = 0.85f, heightFraction = 0.65f)),
            elements = listOf(
                HomeElementEntity(
                    elementId = 1L,
                    pageId = 1L,
                    type = "TAG_SHORTCUT",
                    xFraction = 0.25f,
                    yFraction = 0.35f,
                    targetTagId = 1L,
                    label = "Productivity",
                    imageMaskShape = "HEXAGON",
                    skewX = 0.2f,
                )
            ),
            sidebars = listOf(SidebarConfigEntity(1L, "Favorites", side = "TOP", sourceType = "FAVORITES", widthFraction = 0.75f, heightFraction = 0.60f)),
            gestures = listOf(GestureBindingEntity("SWIPE_UP", "OPEN_DRAWER")),
            imageResources = listOf(ImageResourceEntity(1L, "Logo", "Icons", "/tmp/logo.png", fileFormat = "png")),
            backgroundPresets = listOf(BackgroundPresetEntity(1L, "Midnight", "GLOBAL", "GRADIENT")),
            settings = LauncherSettings(
                themeMode = "DARK",
                drawerColumns = 5,
                globalIconShape = "CIRCLE",
                niagaraWaveIntensity = 1.4f,
                gridSnapEnabled = true,
                seededDefaults = true,
            ),
        )

        val json = BackupManager.encodeFullBackup(original)
        val decoded = BackupManager.decodeFullBackup(json)

        assertEquals(original.apps.first().customName, decoded.apps.first().customName)
        assertTrue(decoded.apps.first().manualCategoryOverride)
        assertEquals(1L, decoded.apps.first().primaryCategoryId)
        assertEquals(original.categories.first().name, decoded.categories.first().name)
        assertEquals(original.appCategories.first().isManualAssignment, decoded.appCategories.first().isManualAssignment)
        assertEquals(original.tags.size, decoded.tags.size)
        assertEquals(original.crossRefs.size, decoded.crossRefs.size)
        assertEquals(original.rules.first().includeTagIdsCsv, decoded.rules.first().includeTagIdsCsv)
        assertTrue(decoded.rules.first().includeNestedTags)
        assertEquals(original.windows.first().title, decoded.windows.first().title)
        assertEquals(original.elements.first().xFraction, decoded.elements.first().xFraction, 0.001f)
        assertEquals("HEXAGON", decoded.elements.first().imageMaskShape)
        assertEquals(original.sidebars.first().side, decoded.sidebars.first().side)
        assertEquals(original.imageResources.first().name, decoded.imageResources.first().name)
        assertEquals(original.backgroundPresets.first().name, decoded.backgroundPresets.first().name)
        assertEquals(original.settings.drawerColumns, decoded.settings.drawerColumns)
        assertEquals(original.settings.globalIconShape, decoded.settings.globalIconShape)
        assertEquals(1.4f, decoded.settings.niagaraWaveIntensity, 0.001f)
        assertTrue(decoded.settings.gridSnapEnabled)
    }
}
