package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {

    // --- APPS ---
    @Query("SELECT * FROM apps WHERE isInstalled = 1 ORDER BY appName COLLATE NOCASE ASC")
    fun observeInstalledApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps ORDER BY appName COLLATE NOCASE ASC")
    suspend fun getAllAppsOnce(): List<AppEntity>

    @Transaction
    @Query("SELECT * FROM apps WHERE isInstalled = 1 ORDER BY appName COLLATE NOCASE ASC")
    fun observeAppsWithTags(): Flow<List<AppWithTags>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApps(apps: List<AppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApp(app: AppEntity)

    @Query("UPDATE apps SET isInstalled = 0 WHERE packageName NOT IN (:installedPackages)")
    suspend fun markUninstalledExcept(installedPackages: List<String>)

    @Query("UPDATE apps SET hidden = :hidden WHERE packageName = :packageName")
    suspend fun setAppHidden(packageName: String, hidden: Boolean)

    @Query("UPDATE apps SET favorite = :favorite WHERE packageName = :packageName")
    suspend fun setAppFavorite(packageName: String, favorite: Boolean)

    @Query("UPDATE apps SET customName = :customName WHERE packageName = :packageName")
    suspend fun setAppCustomName(packageName: String, customName: String?)

    @Query(
        "UPDATE apps SET customIconPath = :path, iconScale = :scale, iconOffsetX = :offsetX, iconOffsetY = :offsetY, iconAlpha = :alpha WHERE packageName = :packageName"
    )
    suspend fun setAppCustomIcon(
        packageName: String,
        path: String?,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        alpha: Float,
    )

    @Query("UPDATE apps SET primaryCategoryId = :categoryId, manualCategoryOverride = :isManual, isGame = :isGame WHERE packageName = :packageName")
    suspend fun updateAppPrimaryCategory(
        packageName: String,
        categoryId: Long,
        isManual: Boolean,
        isGame: Boolean,
    )

    @Query("UPDATE apps SET launchCount = launchCount + 1, lastLaunchedAt = :timestamp WHERE packageName = :packageName")
    suspend fun recordAppLaunch(packageName: String, timestamp: Long)

    @Query("UPDATE apps SET manualOrder = :order WHERE packageName = :packageName")
    suspend fun updateAppManualOrder(packageName: String, order: Int)

    // --- CATEGORIES ---
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    suspend fun getAllCategoriesOnce(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(category: CategoryEntity): Long

    @Query("DELETE FROM categories WHERE categoryId = :categoryId AND isCustom = 1")
    suspend fun deleteCustomCategory(categoryId: Long)

    @Query("SELECT * FROM app_categories")
    fun observeAllAppCategories(): Flow<List<AppCategoryCrossRef>>

    @Query("SELECT * FROM app_categories")
    suspend fun getAllAppCategoriesOnce(): List<AppCategoryCrossRef>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppCategoryCrossRefs(refs: List<AppCategoryCrossRef>)

    @Query("DELETE FROM app_categories WHERE packageName = :packageName AND isManualAssignment = 0")
    suspend fun clearAutomaticCategoriesForApp(packageName: String)

    @Query("DELETE FROM app_categories WHERE packageName = :packageName")
    suspend fun clearAllCategoriesForApp(packageName: String)

    @Transaction
    suspend fun replaceManualCategoriesForApp(
        packageName: String,
        categoryIds: List<Long>,
        excludedCategoryIds: List<Long> = emptyList(),
    ) {
        clearAllCategoriesForApp(packageName)
        val active = categoryIds.distinct().map {
            AppCategoryCrossRef(packageName, it, isManualAssignment = true, isExcluded = false)
        }
        val excluded = excludedCategoryIds.distinct().filter { it !in categoryIds }.map {
            AppCategoryCrossRef(packageName, it, isManualAssignment = true, isExcluded = true)
        }
        insertAppCategoryCrossRefs(active + excluded)
    }

    // --- TAGS ---
    @Query("SELECT * FROM tags ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    fun observeAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    suspend fun getAllTagsOnce(): List<TagEntity>

    @Transaction
    @Query("SELECT * FROM tags ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    fun observeTagsWithApps(): Flow<List<TagWithApps>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE tagId = :tagId")
    suspend fun deleteTagById(tagId: Long)

    @Query("UPDATE tags SET parentTagId = NULL WHERE parentTagId = :deletedParentId")
    suspend fun clearParentTagReference(deletedParentId: Long)

    @Query("UPDATE tags SET sortOrder = :sortOrder WHERE tagId = :tagId")
    suspend fun updateTagSortOrder(tagId: Long, sortOrder: Int)

    // --- APP <-> TAG MANY-TO-MANY ---
    @Query("SELECT * FROM app_tags")
    fun observeAllCrossRefs(): Flow<List<AppTagCrossRef>>

    @Query("SELECT * FROM app_tags")
    suspend fun getAllCrossRefsOnce(): List<AppTagCrossRef>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAppTagCrossRefs(refs: List<AppTagCrossRef>)

    @Query("DELETE FROM app_tags WHERE packageName = :packageName AND tagId = :tagId")
    suspend fun removeAppTagCrossRef(packageName: String, tagId: Long)

    @Query("DELETE FROM app_tags WHERE tagId = :tagId")
    suspend fun clearTagAssignments(tagId: Long)

    @Query("DELETE FROM app_tags WHERE packageName = :packageName")
    suspend fun clearAppAssignments(packageName: String)

    @Transaction
    suspend fun replaceAppsForTag(tagId: Long, packageNames: List<String>) {
        clearTagAssignments(tagId)
        insertAppTagCrossRefs(packageNames.distinct().map { AppTagCrossRef(it, tagId) })
    }

    @Transaction
    suspend fun replaceTagsForApp(packageName: String, tagIds: List<Long>) {
        clearAppAssignments(packageName)
        insertAppTagCrossRefs(tagIds.distinct().map { AppTagCrossRef(packageName, it) })
    }

    @Transaction
    suspend fun mergeTags(sourceTagId: Long, targetTagId: Long) {
        val refs = getAllCrossRefsOnce().filter { it.tagId == sourceTagId }
        insertAppTagCrossRefs(refs.map { AppTagCrossRef(it.packageName, targetTagId) })
        clearParentTagReference(sourceTagId)
        deleteTagById(sourceTagId)
    }

    // --- SAVED TAG RULES ---
    @Query("SELECT * FROM saved_tag_rules ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    fun observeSavedRules(): Flow<List<SavedTagRuleEntity>>

    @Query("SELECT * FROM saved_tag_rules")
    suspend fun getAllRulesOnce(): List<SavedTagRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavedRule(rule: SavedTagRuleEntity): Long

    @Query("DELETE FROM saved_tag_rules WHERE ruleId = :ruleId")
    suspend fun deleteSavedRule(ruleId: Long)

    // --- HOME PAGES & ELEMENTS & WINDOWS ---
    @Query("SELECT * FROM home_pages ORDER BY pageOrder ASC")
    fun observeHomePages(): Flow<List<HomePageEntity>>

    @Query("SELECT * FROM home_pages ORDER BY pageOrder ASC")
    suspend fun getHomePagesOnce(): List<HomePageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHomePage(page: HomePageEntity): Long

    @Update
    suspend fun updateHomePages(pages: List<HomePageEntity>)

    @Transaction
    suspend fun reorderHomePages(orderedPageIds: List<Long>) {
        val current = getHomePagesOnce()
        val byId = current.associateBy { it.pageId }
        val reordered = mutableListOf<HomePageEntity>()
        orderedPageIds.distinct().forEach { id ->
            byId[id]?.let { page ->
                reordered.add(page.copy(pageOrder = reordered.size))
            }
        }
        current.filter { it.pageId !in orderedPageIds }.forEach { remaining ->
            reordered.add(remaining.copy(pageOrder = reordered.size))
        }
        if (reordered.isNotEmpty()) {
            updateHomePages(reordered)
        }
    }

    @Query("DELETE FROM home_elements WHERE ownerType = 'PAGE' AND pageId = :pageId")
    suspend fun deleteElementsForPage(pageId: Long)

    @Query("DELETE FROM home_pages WHERE pageId = :pageId")
    suspend fun deleteHomePage(pageId: Long)

    @Transaction
    suspend fun deleteHomePageAndElements(pageId: Long) {
        deleteElementsForPage(pageId)
        deleteHomePage(pageId)
    }

    @Query("SELECT * FROM launcher_windows ORDER BY windowId ASC")
    fun observeWindows(): Flow<List<LauncherWindowEntity>>

    @Query("SELECT * FROM launcher_windows")
    suspend fun getWindowsOnce(): List<LauncherWindowEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWindow(window: LauncherWindowEntity): Long

    @Query("DELETE FROM launcher_windows WHERE windowId = :windowId")
    suspend fun deleteWindow(windowId: Long)

    @Query("SELECT * FROM home_elements ORDER BY zIndex ASC, elementId ASC")
    fun observeHomeElements(): Flow<List<HomeElementEntity>>

    @Query("SELECT * FROM home_elements ORDER BY zIndex ASC, elementId ASC")
    suspend fun getHomeElementsOnce(): List<HomeElementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHomeElement(element: HomeElementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeElements(elements: List<HomeElementEntity>)

    @Query("DELETE FROM home_elements WHERE elementId = :elementId")
    suspend fun deleteHomeElementById(elementId: Long)

    @Query("DELETE FROM home_elements WHERE ownerType = 'PAGE'")
    suspend fun deleteAllPageElements()

    @Query("DELETE FROM home_elements")
    suspend fun deleteAllHomeElements()

    @Query("DELETE FROM home_pages")
    suspend fun deleteAllHomePages()

    @Query("DELETE FROM launcher_windows")
    suspend fun deleteAllWindows()

    // --- LAYOUT SNAPSHOTS / THEMES ---
    @Query("SELECT * FROM layout_snapshots ORDER BY createdAt DESC")
    fun observeSnapshots(): Flow<List<LayoutSnapshotEntity>>

    @Query("SELECT * FROM layout_snapshots")
    suspend fun getSnapshotsOnce(): List<LayoutSnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: LayoutSnapshotEntity): Long

    @Query("DELETE FROM layout_snapshots WHERE snapshotId = :snapshotId")
    suspend fun deleteSnapshot(snapshotId: Long)

    // --- SIDEBARS / DRAWERS ---
    @Query("SELECT * FROM sidebar_configs ORDER BY sortOrder ASC, sidebarId ASC")
    fun observeSidebars(): Flow<List<SidebarConfigEntity>>

    @Query("SELECT * FROM sidebar_configs ORDER BY sortOrder ASC")
    suspend fun getSidebarsOnce(): List<SidebarConfigEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSidebar(sidebar: SidebarConfigEntity): Long

    @Query("DELETE FROM sidebar_configs WHERE sidebarId = :sidebarId")
    suspend fun deleteSidebar(sidebarId: Long)

    @Query("DELETE FROM sidebar_configs")
    suspend fun deleteAllSidebars()

    // --- GESTURES ---
    @Query("SELECT * FROM gesture_bindings")
    fun observeGestures(): Flow<List<GestureBindingEntity>>

    @Query("SELECT * FROM gesture_bindings")
    suspend fun getGesturesOnce(): List<GestureBindingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGesture(binding: GestureBindingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGestures(bindings: List<GestureBindingEntity>)

    // --- IMAGE RESOURCE LIBRARY ---
    @Query("SELECT * FROM image_resources ORDER BY createdAt DESC")
    fun observeImageResources(): Flow<List<ImageResourceEntity>>

    @Query("SELECT * FROM image_resources")
    suspend fun getImageResourcesOnce(): List<ImageResourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImageResource(resource: ImageResourceEntity): Long

    @Query("DELETE FROM image_resources WHERE imageId = :imageId")
    suspend fun deleteImageResource(imageId: Long)

    // --- BACKGROUND PRESETS ---
    @Query("SELECT * FROM background_presets ORDER BY presetId DESC")
    fun observeBackgroundPresets(): Flow<List<BackgroundPresetEntity>>

    @Query("SELECT * FROM background_presets")
    suspend fun getBackgroundPresetsOnce(): List<BackgroundPresetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBackgroundPreset(preset: BackgroundPresetEntity): Long

    @Query("DELETE FROM background_presets WHERE presetId = :presetId")
    suspend fun deleteBackgroundPreset(presetId: Long)

    // --- CUSTOM FONTS ---
    @Query("SELECT * FROM custom_fonts ORDER BY importedAt DESC")
    fun observeCustomFonts(): Flow<List<CustomFontEntity>>

    @Query("SELECT * FROM custom_fonts ORDER BY importedAt DESC")
    suspend fun getCustomFontsOnce(): List<CustomFontEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomFont(font: CustomFontEntity): Long

    @Query("DELETE FROM custom_fonts WHERE fontId = :fontId")
    suspend fun deleteCustomFont(fontId: Long)

    // --- THEME PROFILES ---
    @Query("SELECT * FROM theme_profiles ORDER BY createdAt DESC")
    fun observeThemeProfiles(): Flow<List<ThemeProfileEntity>>

    @Query("SELECT * FROM theme_profiles ORDER BY createdAt DESC")
    suspend fun getThemeProfilesOnce(): List<ThemeProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertThemeProfile(profile: ThemeProfileEntity): Long

    @Query("DELETE FROM theme_profiles WHERE profileId = :profileId")
    suspend fun deleteThemeProfile(profileId: Long)
}
