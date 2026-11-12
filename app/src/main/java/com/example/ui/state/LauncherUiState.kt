package com.example.ui.state

import com.example.data.db.AppCategoryCrossRef
import com.example.data.db.AppWithTags
import com.example.data.db.BackgroundPresetEntity
import com.example.data.db.CategoryEntity
import com.example.data.db.GestureBindingEntity
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.ImageResourceEntity
import com.example.data.db.LauncherWindowEntity
import com.example.data.db.LayoutSnapshotEntity
import com.example.data.db.SavedTagRuleEntity
import com.example.data.db.SidebarConfigEntity
import com.example.data.db.TagEntity
import com.example.data.db.TagWithApps
import com.example.data.prefs.LauncherSettings

sealed interface ActiveOverlay {
    data object None : ActiveOverlay
    data class TagSidebar(val tagId: Long, val side: String = "LEFT") : ActiveOverlay
    data class CategorySidebar(val categoryId: Long, val side: String = "LEFT") : ActiveOverlay
    data class ConfiguredSidebar(val sidebarId: Long) : ActiveOverlay
    data class RuleSidebar(val ruleId: Long, val side: String = "LEFT") : ActiveOverlay
    data class FolderOverlay(val elementId: Long) : ActiveOverlay
    data class WindowOverlay(val windowId: Long) : ActiveOverlay
}

sealed class LauncherRoute(val route: String) {
    data object Home : LauncherRoute("home")
    data object Tags : LauncherRoute("tags")
    data object Apps : LauncherRoute("apps")
    data object Sidebars : LauncherRoute("sidebars")
    data object Settings : LauncherRoute("settings")
}

data class LauncherUiState(
    val isLoading: Boolean = true,
    val appsWithTags: List<AppWithTags> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val appCategories: List<AppCategoryCrossRef> = emptyList(),
    val tagsWithApps: List<TagWithApps> = emptyList(),
    val allTags: List<TagEntity> = emptyList(),
    val savedRules: List<SavedTagRuleEntity> = emptyList(),
    val homePages: List<HomePageEntity> = emptyList(),
    val windows: List<LauncherWindowEntity> = emptyList(),
    val homeElements: List<HomeElementEntity> = emptyList(),
    val snapshots: List<LayoutSnapshotEntity> = emptyList(),
    val sidebars: List<SidebarConfigEntity> = emptyList(),
    val gestures: Map<String, GestureBindingEntity> = emptyMap(),
    val imageResources: List<ImageResourceEntity> = emptyList(),
    val backgroundPresets: List<BackgroundPresetEntity> = emptyList(),
    val settings: LauncherSettings = LauncherSettings(),
    val isDefaultLauncher: Boolean = false,
    val currentPageIndex: Int = 0,
    val isEditMode: Boolean = false,
    val selectedElementId: Long? = null,
    val multiSelectedIds: Set<Long> = emptySet(),
    val clipboardElement: HomeElementEntity? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val activeOverlay: ActiveOverlay = ActiveOverlay.None,
    val isDrawerOpen: Boolean = false,
    val isUniversalSearchOpen: Boolean = false,
    val snackbarMessage: String? = null,
)
