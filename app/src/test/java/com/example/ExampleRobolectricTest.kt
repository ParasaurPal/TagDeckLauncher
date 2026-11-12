package com.example

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppEntity
import com.example.data.db.AppTagCrossRef
import com.example.data.db.AppWithTags
import com.example.data.db.CategoryEntity
import com.example.data.db.LauncherDatabase
import com.example.data.db.PlayCategoryCatalog
import com.example.data.db.TagEntity
import com.example.data.prefs.LauncherPreferencesRepository
import com.example.data.repository.LauncherRepository
import com.example.data.system.AppDiscoveryManager
import com.example.ui.components.ActiveOverlayHost
import com.example.ui.components.AppDrawerOverlay
import com.example.ui.components.TagTriggeredSidebarContent
import com.example.ui.components.UniversalSearchOverlay
import com.example.ui.state.ActiveOverlay
import com.example.ui.state.LauncherUiState
import com.example.ui.theme.TagDeckTheme
import kotlinx.coroutines.flow.first
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
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var db: LauncherDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, LauncherDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `app name and separated launcher and home intent filters are configured`() {
        assertEquals("TagDeck", context.getString(R.string.app_name))

        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val launcherActivities = context.packageManager.queryIntentActivities(
            launcherIntent,
            PackageManager.GET_RESOLVED_FILTER,
        ).filter { it.activityInfo.packageName == context.packageName }
        assertTrue(launcherActivities.any { it.activityInfo.name == MainActivity::class.java.name })
        assertFalse(
            launcherActivities.any {
                it.activityInfo.name == MainActivity::class.java.name &&
                    it.filter?.hasCategory(Intent.CATEGORY_HOME) == true
            }
        )

        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val homeActivities = context.packageManager.queryIntentActivities(homeIntent, 0)
        assertTrue(homeActivities.any { it.activityInfo.packageName == context.packageName })
    }

    @Test
    fun `many to many app tag assignments work without duplicating apps`() = runBlocking {
        val dao = db.launcherDao()
        val discord = AppEntity(packageName = "com.discord", appName = "Discord")
        val minecraft = AppEntity(packageName = "com.mojang.minecraftpe", appName = "Minecraft")
        dao.upsertApps(listOf(discord, minecraft))

        val gamingId = dao.insertTag(TagEntity(name = "GAMING"))
        val socialId = dao.insertTag(TagEntity(name = "SOCIAL"))
        val emptyId = dao.insertTag(TagEntity(name = "EMPTY_TAG"))

        dao.insertAppTagCrossRefs(
            listOf(
                AppTagCrossRef(discord.packageName, gamingId),
                AppTagCrossRef(discord.packageName, socialId),
                AppTagCrossRef(minecraft.packageName, gamingId),
            )
        )

        val tagsWithApps = dao.observeTagsWithApps().first()
        val gamingApps = tagsWithApps.first { it.tag.tagId == gamingId }.apps
        val socialApps = tagsWithApps.first { it.tag.tagId == socialId }.apps
        val emptyApps = tagsWithApps.first { it.tag.tagId == emptyId }.apps

        assertEquals(2, gamingApps.size)
        assertEquals(1, socialApps.size)
        assertEquals("Discord", socialApps.first().appName)
        assertEquals(0, emptyApps.size)

        dao.removeAppTagCrossRef(discord.packageName, socialId)
        val allApps = dao.getAllAppsOnce()
        assertEquals(2, allApps.size)
        assertEquals(2, dao.observeTagsWithApps().first().first { it.tag.tagId == gamingId }.apps.size)
        assertEquals(0, dao.observeTagsWithApps().first().first { it.tag.tagId == socialId }.apps.size)
    }

    @Test
    fun `manual category override persists across subsequent automatic app sync`() = runBlocking {
        val dao = db.launcherDao()
        val repo = LauncherRepository(
            dao = dao,
            appDiscovery = AppDiscoveryManager(context),
            prefsRepo = LauncherPreferencesRepository(context),
        )
        val categories = PlayCategoryCatalog.allSeedCategories.map {
            CategoryEntity(
                categoryId = it.id,
                name = it.name,
                iconKey = it.iconKey,
                colorHex = it.colorHex,
                categoryType = it.categoryType,
                parentCategoryId = it.parentCategoryId,
            )
        }
        dao.insertCategories(categories)

        // Seed app with automatic category = Tools (29)
        dao.upsertApps(
            listOf(
                AppEntity(
                    packageName = context.packageName,
                    appName = "TagDeck",
                    primaryCategoryId = 29L,
                    automaticCategoryId = 29L,
                    manualCategoryOverride = false,
                )
            )
        )

        // User manually overrides it to Productivity (25)
        repo.setManualCategoriesForApp(
            packageName = context.packageName,
            primaryCategoryId = 25L,
            allCategoryIds = listOf(25L, 23L),
        )

        // Re-run automatic sync
        repo.syncInstalledAppsAndSeedDefaults()

        val updated = dao.getAllAppsOnce().first { it.packageName == context.packageName }
        assertTrue(updated.manualCategoryOverride)
        assertEquals(25L, updated.primaryCategoryId)
    }

    @Test
    fun `tag triggered sidebar displays selected tag title and switches dynamically`() {
        val gamingTag = TagEntity(tagId = 1L, name = "GAMING", colorHex = 0xFF34D399L)
        val toolsTag = TagEntity(tagId = 2L, name = "TOOLS", colorHex = 0xFFFBBF24L)
        val gamingApps = listOf(
            AppEntity(packageName = "com.mojang.minecraftpe", appName = "Minecraft"),
            AppEntity(packageName = "com.discord", appName = "Discord"),
        )
        val toolsApps = listOf(
            AppEntity(packageName = "com.android.settings", appName = "Settings"),
        )
        val discovery = AppDiscoveryManager(context)

        composeRule.setContent {
            val selectedTagIdState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableLongStateOf(1L) }
            val selectedTagId = selectedTagIdState.longValue
            TagDeckTheme {
                val activeTag = if (selectedTagId == 1L) gamingTag else toolsTag
                val activeApps = if (selectedTagId == 1L) gamingApps else toolsApps
                TagTriggeredSidebarContent(
                    tag = activeTag,
                    allTags = listOf(gamingTag, toolsTag),
                    apps = activeApps,
                    isRightSide = false,
                    appDiscovery = discovery,
                    iconShapeKey = "SQUIRCLE",
                    onSwitchToTag = { selectedTagIdState.longValue = it },
                    onLaunchApp = {},
                    onAppLongPress = {},
                    onEditTag = {},
                    onAssignApps = {},
                    onUpdateTagLayout = {},
                    onClose = {},
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tag_sidebar_title").assertTextEquals("GAMING")
        composeRule.onNodeWithText("Minecraft").assertIsDisplayed()
        composeRule.onNodeWithText("Discord").assertIsDisplayed()

        composeRule.onNodeWithTag("sidebar_switch_tag_2").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tag_sidebar_title").assertTextEquals("TOOLS")
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun `universal all apps sidebar shows niagara wave bar and bottom category dock with games subcategories`() {
        val discovery = AppDiscoveryManager(context)
        val categories = PlayCategoryCatalog.allSeedCategories.map {
            CategoryEntity(
                categoryId = it.id,
                name = it.name,
                iconKey = it.iconKey,
                colorHex = it.colorHex,
                categoryType = it.categoryType,
                parentCategoryId = it.parentCategoryId,
            )
        }
        val apps = listOf(
            AppWithTags(
                app = AppEntity(
                    packageName = "com.mojang.minecraftpe",
                    appName = "Minecraft",
                    primaryCategoryId = PlayCategoryCatalog.GAMES_PARENT_ID,
                    isGame = true,
                ),
                tags = emptyList(),
            ),
            AppWithTags(
                app = AppEntity(
                    packageName = "com.spotify.music",
                    appName = "Spotify",
                    primaryCategoryId = 20L,
                    isGame = false,
                ),
                tags = emptyList(),
            ),
        )
        val state = LauncherUiState(
            isLoading = false,
            appsWithTags = apps,
            categories = categories,
            isDrawerOpen = true,
        )

        composeRule.setContent {
            TagDeckTheme {
                AppDrawerOverlay(
                    visible = true,
                    state = state,
                    appDiscovery = discovery,
                    onClose = {},
                    onLaunchApp = {},
                    onAppLongPress = {},
                    onOpenTagSidebar = {},
                    onToggleLayout = {},
                    onCycleSort = {},
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("niagara_alphabet_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_bottom_category_bar").assertIsDisplayed()
        composeRule.onNodeWithText("Minecraft").assertIsDisplayed()
        composeRule.onNodeWithText("Spotify").assertIsDisplayed()

        // Tap Games in bottom category dock -> expands Game subcategories row (All Games, Action, Puzzle...)
        composeRule.onNodeWithText("Games").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("All Games").assertIsDisplayed()
        composeRule.onNodeWithText("Minecraft").assertIsDisplayed()
    }

    @Test
    fun `universal search overlay renders apps tags and categories`() {
        val discovery = AppDiscoveryManager(context)
        val state = LauncherUiState(
            isLoading = false,
            appsWithTags = listOf(
                AppWithTags(AppEntity("com.discord", appName = "Discord"), tags = listOf(TagEntity(1L, "GAMING")))
            ),
            allTags = listOf(TagEntity(1L, "GAMING")),
            categories = listOf(CategoryEntity(7L, "Communication")),
            isUniversalSearchOpen = true,
        )

        composeRule.setContent {
            TagDeckTheme {
                UniversalSearchOverlay(
                    visible = true,
                    state = state,
                    appDiscovery = discovery,
                    onClose = {},
                    onLaunchApp = {},
                    onOpenTagSidebar = {},
                    onOpenCategorySidebar = {},
                    onOpenRuleSidebar = {},
                    onOpenFolder = {},
                    onJumpToPage = {},
                    onAddHistory = {},
                    onClearHistory = {},
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Discord").assertIsDisplayed()
        composeRule.onNodeWithText("#GAMING").assertIsDisplayed()
        composeRule.onNodeWithText("Communication").assertIsDisplayed()
    }

    @Test
    fun `drawer switches active custom category backgrounds and long press triggers category editor`() {
        val discovery = AppDiscoveryManager(context)
        val categories = listOf(
            CategoryEntity(
                categoryId = PlayCategoryCatalog.SYSTEM_ALL_ID,
                name = "All",
                customEmoji = "✨",
                backgroundType = "SOLID",
                backgroundColorHex = 0xFF0D1B2AL,
                categoryType = "SYSTEM_ALL",
                sortOrder = -10,
            ),
            CategoryEntity(
                categoryId = PlayCategoryCatalog.SYSTEM_FAVORITES_ID,
                name = "Favorites",
                customEmoji = "⭐",
                backgroundType = "GRADIENT",
                backgroundColorHex = 0xFF1A1038L,
                backgroundGradientEndHex = 0xFF064E3BL,
                categoryType = "SYSTEM_FAVORITES",
                sortOrder = -9,
            ),
            CategoryEntity(
                categoryId = PlayCategoryCatalog.GAMES_PARENT_ID,
                name = "Games",
                backgroundType = "INHERIT",
                categoryType = "GAMES_PARENT",
            ),
        )
        val state = LauncherUiState(
            isLoading = false,
            categories = categories,
            appsWithTags = listOf(
                AppWithTags(
                    app = AppEntity("com.spotify.music", appName = "Spotify", favorite = true, primaryCategoryId = 20L),
                    tags = emptyList(),
                ),
            ),
            isDrawerOpen = true,
        )
        var editedCategory: CategoryEntity? = null

        composeRule.setContent {
            TagDeckTheme {
                AppDrawerOverlay(
                    visible = true,
                    state = state,
                    appDiscovery = discovery,
                    onClose = {},
                    onLaunchApp = {},
                    onAppLongPress = {},
                    onOpenTagSidebar = {},
                    onEditCategory = { editedCategory = it },
                    onToggleLayout = {},
                    onCycleSort = {},
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_bg_custom_SOLID").assertIsDisplayed()

        // Switching to Favorites applies its custom gradient background
        composeRule.onNodeWithTag("dock_chip_favorites").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_bg_custom_GRADIENT").assertIsDisplayed()

        // Switching to Games falls back to global drawer background because Games is INHERIT
        composeRule.onNodeWithTag("dock_chip_games").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("drawer_bg_global").assertIsDisplayed()

        // Long-pressing All dock chip invokes category editor
        composeRule.onNodeWithTag("dock_chip_all").performTouchInput { longClick() }
        composeRule.waitForIdle()
        assertEquals(PlayCategoryCatalog.SYSTEM_ALL_ID, editedCategory?.categoryId)

        // Long-pressing Games dock chip invokes category editor
        composeRule.onNodeWithTag("dock_chip_games").performTouchInput { longClick() }
        composeRule.waitForIdle()
        assertEquals(PlayCategoryCatalog.GAMES_PARENT_ID, editedCategory?.categoryId)
    }

    @Test
    fun `category sidebar renders custom category background, emoji badge, and edit action`() {
        val discovery = AppDiscoveryManager(context)
        val favCat = CategoryEntity(
            categoryId = PlayCategoryCatalog.SYSTEM_FAVORITES_ID,
            name = "Favorites",
            customEmoji = "⭐",
            colorHex = 0xFFFBBF24L,
            backgroundType = "GRADIENT",
            backgroundColorHex = 0xFF1A1038L,
            backgroundGradientEndHex = 0xFF064E3BL,
            categoryType = "SYSTEM_FAVORITES",
        )
        val state = LauncherUiState(
            isLoading = false,
            categories = listOf(favCat),
            appsWithTags = listOf(
                AppWithTags(AppEntity("com.spotify.music", appName = "Spotify", favorite = true), tags = emptyList()),
            ),
            activeOverlay = ActiveOverlay.CategorySidebar(PlayCategoryCatalog.SYSTEM_FAVORITES_ID),
        )
        var editedCategory: CategoryEntity? = null

        composeRule.setContent {
            TagDeckTheme {
                ActiveOverlayHost(
                    state = state,
                    appDiscovery = discovery,
                    onDismiss = {},
                    onSwitchToTag = {},
                    onLaunchApp = {},
                    onEditTag = {},
                    onEditCategory = { editedCategory = it },
                    onAssignAppsToTag = {},
                    onUpdateTagLayout = {},
                    onAppLongPress = {},
                    onToggleSidebarPin = { _, _ -> },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("generic_sidebar_title").assertTextEquals("Favorites")
        composeRule.onNodeWithText("⭐").assertIsDisplayed()
        composeRule.onNodeWithTag("sidebar_bg_GRADIENT").assertIsDisplayed()
        composeRule.onNodeWithText("Spotify").assertIsDisplayed()

        composeRule.onNodeWithTag("sidebar_edit_category_button").performClick()
        composeRule.waitForIdle()
        assertEquals(PlayCategoryCatalog.SYSTEM_FAVORITES_ID, editedCategory?.categoryId)
    }
}
