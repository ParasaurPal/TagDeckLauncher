package com.example.ui.state

import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppCategoryCrossRef
import com.example.data.db.AppEntity
import com.example.data.db.AppWithTags
import com.example.data.db.BackgroundPresetEntity
import com.example.data.db.CategoryEntity
import com.example.data.db.CustomFontEntity
import com.example.data.db.ElementStyleExtras
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
import com.example.data.db.ThemeProfileEntity
import com.example.data.prefs.LauncherSettings
import com.example.data.repository.LauncherRepository
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.BackupManager
import com.example.data.system.DeviceControlsManager
import com.example.data.system.FontStorageManager
import com.example.data.system.ImageStorageManager
import com.example.data.system.InstalledIconPack
import com.example.data.system.LauncherWidgetHost
import com.example.data.system.LineIconProfile
import com.example.data.system.NotificationAccess
import com.example.data.system.StarterTemplates
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private data class TransientLauncherState(
    val isLoading: Boolean = true,
    val installedIconPacks: List<InstalledIconPack> = emptyList(),
    val isDefaultLauncher: Boolean = false,
    val currentPageIndex: Int = 0,
    val isEditMode: Boolean = false,
    val selectedElementId: Long? = null,
    val multiSelectedIds: Set<Long> = emptySet(),
    val clipboardElement: HomeElementEntity? = null,
    val copiedStyle: CopiedElementStyle? = null,
    val activeVisibilityGroups: Set<String> = emptySet(),
    val hiddenVisibilityGroups: Set<String> = emptySet(),
    val runtimeHiddenElementIds: Set<Long> = emptySet(),
    val runtimeShownElementIds: Set<Long> = emptySet(),
    val activeContainerFocusId: Long? = null,
    val stackIndexOverrides: Map<Long, Int> = emptyMap(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val activeOverlay: ActiveOverlay = ActiveOverlay.None,
    val isDrawerOpen: Boolean = false,
    val isUniversalSearchOpen: Boolean = false,
    val pendingGlobalSearchQuery: String = "",
    val pendingSettingsSearchQuery: String = "",
    val snackbarMessage: String? = null,
)

class LauncherViewModel(
    private val repository: LauncherRepository,
    val appDiscovery: AppDiscoveryManager,
    val imageStorage: ImageStorageManager,
    val widgetHost: LauncherWidgetHost,
    val fontStorage: FontStorageManager = (appDiscovery.context.applicationContext as? com.example.TagDeckApplication)?.fontStorageManager
        ?: FontStorageManager(appDiscovery.context),
) : ViewModel() {

    private val transientState = MutableStateFlow(TransientLauncherState())

    private val undoStack = ArrayDeque<Pair<List<HomePageEntity>, List<HomeElementEntity>>>()
    private val redoStack = ArrayDeque<Pair<List<HomePageEntity>, List<HomeElementEntity>>>()
    private val maxHistory = 25

    private val dataFlow1 = combine(
        repository.appsWithTagsFlow,
        repository.categoriesFlow,
        repository.appCategoriesFlow,
        repository.tagsWithAppsFlow,
        repository.tagsFlow,
    ) { apps, cats, appCats, tagsWithApps, allTags ->
        DataSlice1(apps, cats, appCats, tagsWithApps, allTags)
    }

    private val dataFlow2 = combine(
        repository.savedRulesFlow,
        repository.homePagesFlow,
        repository.windowsFlow,
        repository.homeElementsFlow,
        repository.snapshotsFlow,
    ) { rules, pages, windows, elements, snapshots ->
        DataSlice2(rules, pages, windows, elements, snapshots)
    }

    private val dataFlow3 = combine(
        repository.sidebarsFlow,
        repository.gesturesFlow,
        repository.imageResourcesFlow,
        repository.backgroundPresetsFlow,
        repository.settingsFlow,
    ) { sidebars, gestures, imgs, presets, settings ->
        DataSlice3(sidebars, gestures, imgs, presets, settings)
    }

    private val dataFlow4 = combine(
        repository.customFontsFlow,
        repository.themeProfilesFlow,
    ) { customFonts, themeProfiles ->
        customFonts to themeProfiles
    }

    val uiState: StateFlow<LauncherUiState> = combine(
        dataFlow1,
        dataFlow2,
        dataFlow3,
        dataFlow4,
        transientState,
    ) { d1, d2, d3, d4, t ->
        LauncherUiState(
            isLoading = t.isLoading,
            appsWithTags = d1.apps,
            categories = d1.categories,
            appCategories = d1.appCategories,
            tagsWithApps = d1.tagsWithApps,
            allTags = d1.allTags,
            savedRules = d2.rules,
            homePages = d2.pages,
            windows = d2.windows,
            homeElements = d2.elements,
            snapshots = d2.snapshots,
            sidebars = d3.sidebars,
            gestures = d3.gestures.associateBy { it.gestureType },
            imageResources = d3.imageResources,
            backgroundPresets = d3.backgroundPresets,
            customFonts = d4.first,
            themeProfiles = d4.second,
            settings = d3.settings,
            installedIconPacks = t.installedIconPacks,
            isDefaultLauncher = t.isDefaultLauncher,
            currentPageIndex = t.currentPageIndex.coerceIn(0, (d2.pages.size - 1).coerceAtLeast(0)),
            isEditMode = t.isEditMode,
            selectedElementId = t.selectedElementId,
            multiSelectedIds = t.multiSelectedIds,
            clipboardElement = t.clipboardElement,
            copiedStyle = t.copiedStyle,
            activeVisibilityGroups = t.activeVisibilityGroups,
            hiddenVisibilityGroups = t.hiddenVisibilityGroups,
            runtimeHiddenElementIds = t.runtimeHiddenElementIds,
            runtimeShownElementIds = t.runtimeShownElementIds,
            activeContainerFocusId = t.activeContainerFocusId,
            stackIndexOverrides = t.stackIndexOverrides,
            canUndo = t.canUndo,
            canRedo = t.canRedo,
            activeOverlay = t.activeOverlay,
            isDrawerOpen = t.isDrawerOpen,
            isUniversalSearchOpen = t.isUniversalSearchOpen,
            pendingGlobalSearchQuery = t.pendingGlobalSearchQuery,
            pendingSettingsSearchQuery = t.pendingSettingsSearchQuery,
            snackbarMessage = t.snackbarMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherUiState(),
    )

    init {
        refreshInstalledApps()
        refreshInstalledIconPacks()
        refreshDefaultLauncherStatus()
        viewModelScope.launch {
            appDiscovery.observePackageChanges().collect {
                repository.syncInstalledAppsAndSeedDefaults()
                refreshInstalledIconPacks()
            }
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            repository.syncInstalledAppsAndSeedDefaults()
            transientState.update { it.copy(isLoading = false) }
        }
    }

    fun refreshInstalledIconPacks() {
        viewModelScope.launch {
            val packs = appDiscovery.iconPackManager.discoverInstalledIconPacks()
            transientState.update { it.copy(installedIconPacks = packs) }
        }
    }

    fun addIconPackToHierarchy(packageName: String) {
        updateSettings { s ->
            val list = s.iconPackOrder().toMutableList()
            if (packageName !in list) list.add(packageName)
            s.copy(iconPackOrderCsv = list.joinToString(","))
        }
    }

    fun removeIconPackFromHierarchy(packageName: String) {
        updateSettings { s ->
            val list = s.iconPackOrder().filterNot { it == packageName }
            s.copy(iconPackOrderCsv = list.joinToString(","))
        }
    }

    fun moveIconPackInHierarchy(packageName: String, delta: Int) {
        updateSettings { s ->
            val list = s.iconPackOrder().toMutableList()
            val idx = list.indexOf(packageName)
            if (idx == -1) return@updateSettings s
            val target = (idx + delta).coerceIn(0, list.lastIndex)
            if (target == idx) return@updateSettings s
            val moved = list.removeAt(idx)
            list.add(target, moved)
            s.copy(iconPackOrderCsv = list.joinToString(","))
        }
    }

    fun refreshNotificationAccessStatus() {
        val accessEnabled = NotificationAccess.isEnabled(appDiscovery.context)
        viewModelScope.launch {
            repository.updateSettings { settings ->
                if (!accessEnabled && settings.notificationsBadgeHint) {
                    settings.copy(notificationsBadgeHint = false)
                } else {
                    settings
                }
            }
        }
    }

    fun refreshDefaultLauncherStatus() {
        val isDef = appDiscovery.isDefaultLauncher()
        transientState.update { it.copy(isDefaultLauncher = isDef) }
    }

    fun launchApp(app: AppEntity) {
        viewModelScope.launch {
            val launched = repository.launchApp(app)
            if (!launched) {
                showMessage("Could not launch ${app.displayName}")
            }
        }
    }

    fun openAppInfo(packageName: String) = appDiscovery.openAppInfo(packageName)
    fun requestUninstall(packageName: String) = appDiscovery.requestUninstallApp(packageName)

    // --- OVERLAYS, DRAWER, SEARCH, EDIT MODE ---
    fun openTagSidebar(tagId: Long, side: String = "LEFT") {
        transientState.update {
            it.copy(
                activeOverlay = ActiveOverlay.TagSidebar(tagId, side),
                isDrawerOpen = false,
                isUniversalSearchOpen = false,
            )
        }
    }

    fun openCategorySidebar(categoryId: Long, side: String = "LEFT") {
        transientState.update {
            it.copy(
                activeOverlay = ActiveOverlay.CategorySidebar(categoryId, side),
                isDrawerOpen = false,
                isUniversalSearchOpen = false,
            )
        }
    }

    fun openConfiguredSidebar(sidebarId: Long) {
        transientState.update {
            it.copy(
                activeOverlay = ActiveOverlay.ConfiguredSidebar(sidebarId),
                isDrawerOpen = false,
                isUniversalSearchOpen = false,
            )
        }
    }

    fun openRuleSidebar(ruleId: Long, side: String = "LEFT") {
        transientState.update {
            it.copy(
                activeOverlay = ActiveOverlay.RuleSidebar(ruleId, side),
                isDrawerOpen = false,
                isUniversalSearchOpen = false,
            )
        }
    }

    fun openFolder(elementId: Long) {
        transientState.update {
            it.copy(activeOverlay = ActiveOverlay.FolderOverlay(elementId))
        }
    }

    fun openWindow(windowId: Long) {
        transientState.update {
            it.copy(
                activeOverlay = ActiveOverlay.WindowOverlay(windowId),
                isDrawerOpen = false,
                isUniversalSearchOpen = false,
            )
        }
    }

    fun closeOverlay() {
        transientState.update { it.copy(activeOverlay = ActiveOverlay.None) }
    }

    fun setDrawerOpen(open: Boolean) {
        transientState.update {
            it.copy(
                isDrawerOpen = open,
                isUniversalSearchOpen = if (open) false else it.isUniversalSearchOpen,
                activeOverlay = if (open) ActiveOverlay.None else it.activeOverlay,
            )
        }
    }

    fun setUniversalSearchOpen(open: Boolean, initialQuery: String = "") {
        transientState.update {
            it.copy(
                isUniversalSearchOpen = open,
                pendingGlobalSearchQuery = if (open && initialQuery.isNotBlank()) initialQuery else if (!open) "" else it.pendingGlobalSearchQuery,
                isDrawerOpen = if (open) false else it.isDrawerOpen,
                activeOverlay = if (open) ActiveOverlay.None else it.activeOverlay,
            )
        }
    }

    fun consumePendingGlobalSearchQuery() {
        transientState.update { it.copy(pendingGlobalSearchQuery = "") }
    }

    fun openSettingsWithSearch(query: String, onNavigate: (LauncherRoute) -> Unit) {
        transientState.update {
            it.copy(
                isUniversalSearchOpen = false,
                isDrawerOpen = false,
                activeOverlay = ActiveOverlay.None,
                pendingSettingsSearchQuery = query,
            )
        }
        onNavigate(LauncherRoute.Settings)
    }

    fun consumePendingSettingsSearchQuery() {
        transientState.update { it.copy(pendingSettingsSearchQuery = "") }
    }

    fun addSearchHistory(query: String) {
        val clean = query.trim()
        if (clean.length < 2) return
        updateSettings { settings ->
            val current = settings.recentSearchesCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }
            val updated = (listOf(clean) + current.filterNot { it.equals(clean, ignoreCase = true) }).take(8)
            settings.copy(recentSearchesCsv = updated.joinToString("|"))
        }
    }

    fun clearSearchHistory() {
        updateSettings { it.copy(recentSearchesCsv = "") }
    }

    fun setEditMode(enabled: Boolean) {
        transientState.update {
            it.copy(
                isEditMode = enabled,
                selectedElementId = if (!enabled) null else it.selectedElementId,
                multiSelectedIds = if (!enabled) emptySet() else it.multiSelectedIds,
                activeOverlay = if (enabled) ActiveOverlay.None else it.activeOverlay,
                isDrawerOpen = if (enabled) false else it.isDrawerOpen,
                isUniversalSearchOpen = if (enabled) false else it.isUniversalSearchOpen,
            )
        }
    }

    fun selectElement(elementId: Long?) {
        transientState.update {
            it.copy(
                selectedElementId = elementId,
                multiSelectedIds = if (elementId != null && it.multiSelectedIds.isNotEmpty() && elementId !in it.multiSelectedIds) {
                    emptySet()
                } else it.multiSelectedIds,
            )
        }
    }

    fun toggleMultiSelectElement(elementId: Long) {
        transientState.update { t ->
            val next = t.multiSelectedIds.toMutableSet()
            if (elementId in next) next.remove(elementId) else next.add(elementId)
            t.copy(
                multiSelectedIds = next,
                selectedElementId = elementId,
            )
        }
    }

    fun clearMultiSelect() {
        transientState.update { it.copy(multiSelectedIds = emptySet()) }
    }

    fun setCurrentPageIndex(index: Int) {
        transientState.update { it.copy(currentPageIndex = index.coerceAtLeast(0)) }
    }

    fun clearMessage() {
        transientState.update { it.copy(snackbarMessage = null) }
    }

    fun showMessage(message: String) {
        transientState.update { it.copy(snackbarMessage = message) }
    }

    // --- UNDO / REDO ---
    private suspend fun recordUndoCheckpoint() {
        val current = repository.getHomeLayoutOnce()
        if (undoStack.size >= maxHistory) undoStack.removeFirst()
        undoStack.addLast(current)
        redoStack.clear()
        syncUndoRedoFlags()
    }

    private fun syncUndoRedoFlags() {
        transientState.update {
            it.copy(canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty())
        }
    }

    fun undoLayout() {
        viewModelScope.launch {
            val prev = undoStack.removeLastOrNull() ?: return@launch
            val current = repository.getHomeLayoutOnce()
            redoStack.addLast(current)
            repository.replaceHomeLayout(prev.first, prev.second)
            syncUndoRedoFlags()
            transientState.update { it.copy(selectedElementId = null, multiSelectedIds = emptySet()) }
            showMessage("Undid change")
        }
    }

    fun redoLayout() {
        viewModelScope.launch {
            val next = redoStack.removeLastOrNull() ?: return@launch
            val current = repository.getHomeLayoutOnce()
            undoStack.addLast(current)
            repository.replaceHomeLayout(next.first, next.second)
            syncUndoRedoFlags()
            transientState.update { it.copy(selectedElementId = null, multiSelectedIds = emptySet()) }
            showMessage("Redid change")
        }
    }

    // --- TAGS ---
    fun saveTag(tag: TagEntity, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveTag(tag)
            onSaved(id)
        }
    }

    fun deleteTag(tagId: Long) {
        viewModelScope.launch {
            val currentOverlay = transientState.value.activeOverlay
            if (currentOverlay is ActiveOverlay.TagSidebar && currentOverlay.tagId == tagId) {
                closeOverlay()
            }
            repository.deleteTag(tagId)
            showMessage("Tag deleted")
        }
    }

    fun duplicateTag(tag: TagEntity) {
        viewModelScope.launch {
            repository.duplicateTag(tag)
            showMessage("Duplicated tag \"${tag.name}\"")
        }
    }

    fun mergeTags(sourceTagId: Long, targetTagId: Long) {
        if (sourceTagId == targetTagId) return
        viewModelScope.launch {
            repository.mergeTags(sourceTagId, targetTagId)
            showMessage("Merged tags")
        }
    }

    fun moveTagOrder(tagId: Long, delta: Int) {
        viewModelScope.launch {
            val tags = uiState.value.allTags.toMutableList()
            val index = tags.indexOfFirst { it.tagId == tagId }
            if (index == -1) return@launch
            val targetIndex = (index + delta).coerceIn(0, tags.lastIndex)
            if (targetIndex == index) return@launch
            val item = tags.removeAt(index)
            tags.add(targetIndex, item)
            repository.reorderTags(tags.map { it.tagId })
        }
    }

    fun assignAppToTag(packageName: String, tagId: Long) {
        viewModelScope.launch { repository.assignAppToTag(packageName, tagId) }
    }

    fun removeAppFromTag(packageName: String, tagId: Long) {
        viewModelScope.launch { repository.removeAppFromTag(packageName, tagId) }
    }

    fun replaceAppsForTag(tagId: Long, packageNames: List<String>) {
        viewModelScope.launch { repository.replaceAppsForTag(tagId, packageNames) }
    }

    fun replaceTagsForApp(packageName: String, tagIds: List<Long>) {
        viewModelScope.launch { repository.replaceTagsForApp(packageName, tagIds) }
    }

    fun bulkAssignTagsToApps(packageNames: List<String>, tagIds: List<Long>) {
        viewModelScope.launch {
            repository.bulkAssignTagsToApps(packageNames, tagIds)
            showMessage("Tagged ${packageNames.size} apps")
        }
    }

    // --- CATEGORIES & OVERRIDES ---
    fun saveCategory(category: CategoryEntity, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveCategory(category)
            onSaved(id)
            showMessage("Saved category \"${category.name}\"")
        }
    }

    fun deleteCustomCategory(categoryId: Long) {
        viewModelScope.launch {
            repository.deleteCustomCategory(categoryId)
            showMessage("Custom category deleted")
        }
    }

    fun setManualCategoriesForApp(
        packageName: String,
        primaryCategoryId: Long,
        allCategoryIds: List<Long>,
        excludedCategoryIds: List<Long> = emptyList(),
    ) {
        viewModelScope.launch {
            repository.setManualCategoriesForApp(packageName, primaryCategoryId, allCategoryIds, excludedCategoryIds)
            showMessage("Saved category override")
        }
    }

    fun bulkSetCategoryForApps(packageNames: List<String>, categoryId: Long) {
        viewModelScope.launch {
            repository.bulkSetCategoryForApps(packageNames, categoryId)
            showMessage("Updated category for ${packageNames.size} apps")
        }
    }

    fun restoreAutomaticCategoryForApp(app: AppEntity) {
        viewModelScope.launch {
            repository.restoreAutomaticCategoryForApp(app)
            showMessage("Restored automatic classification for ${app.displayName}")
        }
    }

    // --- SAVED RULES ---
    fun saveRule(rule: SavedTagRuleEntity) {
        viewModelScope.launch {
            repository.saveRule(rule)
            showMessage("Saved collection \"${rule.name}\"")
        }
    }

    fun deleteRule(ruleId: Long) {
        viewModelScope.launch { repository.deleteRule(ruleId) }
    }

    // --- APPS ---
    fun setAppFavorite(packageName: String, favorite: Boolean) {
        viewModelScope.launch { repository.setAppFavorite(packageName, favorite) }
    }

    fun setAppHidden(packageName: String, hidden: Boolean) {
        viewModelScope.launch { repository.setAppHidden(packageName, hidden) }
    }

    fun setAppCustomName(packageName: String, customName: String?) {
        viewModelScope.launch { repository.setAppCustomName(packageName, customName) }
    }

    fun setLineIconProfile(packageName: String, profile: LineIconProfile?) {
        viewModelScope.launch {
            repository.updateSettings { settings ->
                val profiles = LineIconProfile.decodeMap(settings.lineIconProfilesJson).toMutableMap()
                if (profile == null) profiles.remove(packageName) else profiles[packageName] = profile
                settings.copy(lineIconProfilesJson = LineIconProfile.encodeMap(profiles))
            }
            showMessage(if (profile == null) "Line icon removed" else "Line icon saved")
        }
    }

    fun setAppCustomIcon(
        packageName: String,
        path: String?,
        scale: Float = 1f,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        alpha: Float = 1f,
    ) {
        viewModelScope.launch {
            repository.setAppCustomIcon(packageName, path, scale, offsetX, offsetY, alpha)
        }
    }

    fun moveAppManualOrder(packageName: String, delta: Int) {
        viewModelScope.launch {
            val ordered = uiState.value.appsWithTags
                .sortedBy { it.app.manualOrder }
                .map { it.app.packageName }
                .toMutableList()
            val idx = ordered.indexOf(packageName)
            if (idx == -1) return@launch
            val next = (idx + delta).coerceIn(0, ordered.lastIndex)
            if (next == idx) return@launch
            val moved = ordered.removeAt(idx)
            ordered.add(next, moved)
            repository.updateAppManualOrders(ordered)
        }
    }

    // --- HOME PAGES, WINDOWS & ELEMENTS ---
    fun addHomePage(title: String) {
        viewModelScope.launch {
            recordUndoCheckpoint()
            val existingPages = uiState.value.homePages
            val nextOrder = (existingPages.maxOfOrNull { it.pageOrder } ?: -1) + 1
            repository.saveHomePage(HomePageEntity(title = title, pageOrder = nextOrder))
            transientState.update { it.copy(currentPageIndex = existingPages.size) }
            showMessage("Added page \"$title\"")
        }
    }

    fun saveHomePage(page: HomePageEntity) {
        viewModelScope.launch {
            recordUndoCheckpoint()
            repository.saveHomePage(page)
        }
    }

    fun duplicateHomePage(page: HomePageEntity) {
        viewModelScope.launch {
            recordUndoCheckpoint()
            val existingCount = uiState.value.homePages.size
            repository.duplicateHomePage(page)
            transientState.update { it.copy(currentPageIndex = existingCount) }
            showMessage("Duplicated \"${page.title}\"")
        }
    }

    fun reorderHomePages(orderedPageIds: List<Long>) {
        viewModelScope.launch {
            repository.reorderPages(orderedPageIds)
        }
    }

    fun deleteHomePage(pageId: Long) {
        viewModelScope.launch {
            if (uiState.value.homePages.size <= 1) {
                showMessage("At least one home page is required")
                return@launch
            }
            recordUndoCheckpoint()
            repository.deleteHomePage(pageId)
            transientState.update {
                it.copy(currentPageIndex = (it.currentPageIndex).coerceAtMost((uiState.value.homePages.size - 2).coerceAtLeast(0)))
            }
        }
    }

    fun saveWindow(window: LauncherWindowEntity, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveWindow(window)
            onSaved(id)
        }
    }

    fun deleteWindow(windowId: Long) {
        viewModelScope.launch {
            val currentOverlay = transientState.value.activeOverlay
            if (currentOverlay is ActiveOverlay.WindowOverlay && currentOverlay.windowId == windowId) {
                closeOverlay()
            }
            repository.deleteWindow(windowId)
            showMessage("Window deleted")
        }
    }

    fun saveHomeElement(element: HomeElementEntity, selectAfter: Boolean = false, recordHistory: Boolean = true) {
        viewModelScope.launch {
            if (recordHistory) recordUndoCheckpoint()
            val id = repository.saveHomeElement(element)
            if (selectAfter) {
                transientState.update { it.copy(selectedElementId = id) }
            }
        }
    }

    fun dropAppShortcutOnHome(
        app: AppEntity,
        xFraction: Float,
        yFraction: Float,
        isLandscape: Boolean = false,
    ) {
        viewModelScope.launch {
            val state = uiState.value
            val visiblePages = if (state.isEditMode) {
                state.homePages
            } else {
                state.homePages.filter { !it.isHidden }
            }.ifEmpty { listOf(HomePageEntity(pageId = 1L, title = "Main Deck", pageOrder = 0)) }
            val page = visiblePages.getOrNull(state.currentPageIndex.coerceIn(0, visiblePages.lastIndex))
                ?: visiblePages.firstOrNull()
                ?: return@launch

            if (state.settings.layoutLocked || page.isLocked) {
                transientState.update {
                    it.copy(
                        isDrawerOpen = false,
                        activeOverlay = ActiveOverlay.None,
                    )
                }
                showMessage("Unlock \"${page.title}\" or global layout lock in Settings to place icons")
                return@launch
            }

            recordUndoCheckpoint()
            val fx = xFraction.coerceIn(0f, 0.90f)
            val fy = yFraction.coerceIn(0f, 0.90f)
            val useLandscape = isLandscape && state.settings.landscapeSeparateLayout
            val defaultW = state.settings.defaultShortcutWidthDp.coerceIn(56f, 140f)
            val defaultH = state.settings.defaultShortcutHeightDp.coerceIn(56f, 160f)
            val nextZ = (state.homeElements.maxOfOrNull { it.zIndex } ?: 0) + 1
            repository.saveHomeElement(
                HomeElementEntity(
                    pageId = page.pageId,
                    ownerType = "PAGE",
                    type = "APP_SHORTCUT",
                    targetPackageName = app.packageName,
                    label = app.displayName,
                    xFraction = fx,
                    yFraction = fy,
                    landscapeXFraction = if (useLandscape) fx else null,
                    landscapeYFraction = if (useLandscape) fy else null,
                    widthDp = defaultW,
                    heightDp = defaultH,
                    landscapeWidthDp = if (useLandscape) defaultW else null,
                    landscapeHeightDp = if (useLandscape) defaultH else null,
                    zIndex = nextZ,
                    backgroundColorHex = 0x00000000L,
                    borderWidthDp = 0f,
                    showLabel = state.settings.showHomeAppLabels,
                )
            )
            transientState.update {
                it.copy(
                    isDrawerOpen = false,
                    activeOverlay = ActiveOverlay.None,
                )
            }
            showMessage("Added ${app.displayName} to ${page.title}")
        }
    }

    fun copyElementToClipboard(element: HomeElementEntity) {
        transientState.update { it.copy(clipboardElement = element) }
        showMessage("Copied \"${element.label.ifBlank { element.type }}\"")
    }

    fun cutElementToClipboard(element: HomeElementEntity) {
        viewModelScope.launch {
            recordUndoCheckpoint()
            transientState.update { it.copy(clipboardElement = element, selectedElementId = null) }
            repository.deleteHomeElement(element.elementId)
            showMessage("Cut to clipboard")
        }
    }

    fun pasteElementFromClipboard(targetPageId: Long, targetWindowId: Long? = null) {
        val clip = transientState.value.clipboardElement ?: return
        viewModelScope.launch {
            recordUndoCheckpoint()
            val pasted = clip.copy(
                elementId = 0L,
                pageId = targetPageId,
                ownerType = if (targetWindowId != null) "WINDOW" else "PAGE",
                ownerWindowId = targetWindowId,
                xFraction = (clip.xFraction + 0.04f).coerceIn(0f, 0.85f),
                yFraction = (clip.yFraction + 0.04f).coerceIn(0f, 0.85f),
                zIndex = (uiState.value.homeElements.maxOfOrNull { it.zIndex } ?: 0) + 1,
                appWidgetId = null,
            )
            val newId = repository.saveHomeElement(pasted)
            transientState.update { it.copy(selectedElementId = newId) }
            showMessage("Pasted element")
        }
    }

    fun moveMultiSelected(dxFraction: Float, dyFraction: Float) {
        val ids = transientState.value.multiSelectedIds
        if (ids.isEmpty()) return
        viewModelScope.launch {
            recordUndoCheckpoint()
            uiState.value.homeElements.filter { it.elementId in ids && !it.locked }.forEach { el ->
                repository.saveHomeElement(
                    el.copy(
                        xFraction = (el.xFraction + dxFraction).coerceIn(0f, 0.90f),
                        yFraction = (el.yFraction + dyFraction).coerceIn(0f, 0.90f),
                    )
                )
            }
        }
    }

    fun setMultiSelectedLocked(locked: Boolean) {
        val ids = transientState.value.multiSelectedIds
        if (ids.isEmpty()) return
        viewModelScope.launch {
            recordUndoCheckpoint()
            uiState.value.homeElements.filter { it.elementId in ids }.forEach { el ->
                repository.saveHomeElement(el.copy(locked = locked))
            }
            showMessage(if (locked) "Locked ${ids.size} items" else "Unlocked ${ids.size} items")
        }
    }

    fun deleteMultiSelected() {
        val ids = transientState.value.multiSelectedIds
        if (ids.isEmpty()) return
        viewModelScope.launch {
            recordUndoCheckpoint()
            uiState.value.homeElements.filter { it.elementId in ids }.forEach { el ->
                el.appWidgetId?.let { widgetHost.deleteAppWidgetId(it) }
                repository.deleteHomeElement(el.elementId)
            }
            transientState.update { it.copy(multiSelectedIds = emptySet(), selectedElementId = null) }
            showMessage("Deleted ${ids.size} items")
        }
    }

    fun duplicateElement(element: HomeElementEntity) {
        viewModelScope.launch {
            recordUndoCheckpoint()
            val copy = element.copy(
                elementId = 0L,
                xFraction = (element.xFraction + 0.05f).coerceAtMost(0.85f),
                yFraction = (element.yFraction + 0.05f).coerceAtMost(0.85f),
                zIndex = (uiState.value.homeElements.maxOfOrNull { it.zIndex } ?: 0) + 1,
                appWidgetId = null,
            )
            val newId = repository.saveHomeElement(copy)
            transientState.update { it.copy(selectedElementId = newId) }
        }
    }

    fun deleteHomeElement(element: HomeElementEntity) {
        viewModelScope.launch {
            recordUndoCheckpoint()
            element.appWidgetId?.let { widgetHost.deleteAppWidgetId(it) }
            // Clean up any stacked/contained child items referencing this element
            uiState.value.homeElements.filter { it.parentContainerId == element.elementId }.forEach { child ->
                child.appWidgetId?.let { widgetHost.deleteAppWidgetId(it) }
                repository.deleteHomeElement(child.elementId)
            }
            repository.deleteHomeElement(element.elementId)
            if (transientState.value.selectedElementId == element.elementId) {
                transientState.update { it.copy(selectedElementId = null) }
            }
        }
    }

    fun bringElementForward(element: HomeElementEntity) {
        val siblings = uiState.value.homeElements.filter {
            it.ownerType == element.ownerType &&
                if (element.ownerType == "WINDOW") it.ownerWindowId == element.ownerWindowId
                else it.pageId == element.pageId
        }
        val maxZ = siblings.maxOfOrNull { it.zIndex } ?: 0
        saveHomeElement(element.copy(zIndex = maxZ + 1))
    }

    fun sendElementBackward(element: HomeElementEntity) {
        val siblings = uiState.value.homeElements.filter {
            it.ownerType == element.ownerType &&
                if (element.ownerType == "WINDOW") it.ownerWindowId == element.ownerWindowId
                else it.pageId == element.pageId
        }
        val minZ = siblings.minOfOrNull { it.zIndex } ?: 0
        saveHomeElement(element.copy(zIndex = (minZ - 1).coerceAtLeast(0)))
    }

    // --- PRECISION ARRANGE, FORMAT PAINTER & WIDGET STACKS ---
    fun copyElementStyle(element: HomeElementEntity) {
        val style = CopiedElementStyle(
            backgroundColorHex = element.backgroundColorHex,
            backgroundGradientEndHex = element.backgroundGradientEndHex,
            textColorHex = element.textColorHex,
            borderColorHex = element.borderColorHex,
            borderWidthDp = element.borderWidthDp,
            cornerRadiusDp = element.cornerRadiusDp,
            paddingDp = element.paddingDp,
            shadowElevationDp = element.shadowElevationDp,
            alpha = element.alpha,
            extras = element.parsedExtras(),
        )
        transientState.update { it.copy(copiedStyle = style) }
        showMessage("Copied style from \"${element.label.ifBlank { element.type }}\"")
    }

    fun pasteStyleToElements(ids: Set<Long>) {
        val style = transientState.value.copiedStyle ?: return
        if (ids.isEmpty()) return
        viewModelScope.launch {
            recordUndoCheckpoint()
            val extrasJson = style.extras.toJson()
            uiState.value.homeElements.filter { it.elementId in ids }.forEach { el ->
                repository.saveHomeElement(
                    el.copy(
                        backgroundColorHex = style.backgroundColorHex,
                        backgroundGradientEndHex = style.backgroundGradientEndHex,
                        textColorHex = style.textColorHex,
                        borderColorHex = style.borderColorHex,
                        borderWidthDp = style.borderWidthDp,
                        cornerRadiusDp = style.cornerRadiusDp,
                        paddingDp = style.paddingDp,
                        shadowElevationDp = style.shadowElevationDp,
                        alpha = style.alpha,
                        styleExtrasJson = extrasJson,
                    )
                )
            }
            showMessage("Pasted style to ${ids.size} item(s)")
        }
    }

    fun groupElements(ids: Set<Long>) {
        if (ids.size < 2) return
        viewModelScope.launch {
            recordUndoCheckpoint()
            val gid = "grp_${UUID.randomUUID().toString().take(8)}"
            uiState.value.homeElements.filter { it.elementId in ids }.forEach { el ->
                repository.saveHomeElement(el.copy(groupId = gid))
            }
            showMessage("Grouped ${ids.size} items")
        }
    }

    fun ungroupElements(ids: Set<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            recordUndoCheckpoint()
            uiState.value.homeElements.filter { it.elementId in ids }.forEach { el ->
                repository.saveHomeElement(el.copy(groupId = null))
            }
            showMessage("Ungrouped items")
        }
    }

    fun alignElements(ids: Set<Long>, mode: String) {
        val targets = uiState.value.homeElements.filter { it.elementId in ids && !it.locked }
        if (targets.size < 2) return
        viewModelScope.launch {
            recordUndoCheckpoint()
            when (mode) {
                "LEFT" -> {
                    val minX = targets.minOf { it.xFraction }
                    targets.forEach { repository.saveHomeElement(it.copy(xFraction = minX)) }
                }
                "RIGHT" -> {
                    val maxX = targets.maxOf { it.xFraction }
                    targets.forEach { repository.saveHomeElement(it.copy(xFraction = maxX)) }
                }
                "CENTER_H" -> {
                    val avgX = targets.map { it.xFraction }.average().toFloat().coerceIn(0f, 0.90f)
                    targets.forEach { repository.saveHomeElement(it.copy(xFraction = avgX)) }
                }
                "TOP" -> {
                    val minY = targets.minOf { it.yFraction }
                    targets.forEach { repository.saveHomeElement(it.copy(yFraction = minY)) }
                }
                "BOTTOM" -> {
                    val maxY = targets.maxOf { it.yFraction }
                    targets.forEach { repository.saveHomeElement(it.copy(yFraction = maxY)) }
                }
                "CENTER_V" -> {
                    val avgY = targets.map { it.yFraction }.average().toFloat().coerceIn(0f, 0.90f)
                    targets.forEach { repository.saveHomeElement(it.copy(yFraction = avgY)) }
                }
            }
            showMessage("Aligned ${targets.size} items")
        }
    }

    fun distributeElements(ids: Set<Long>, axis: String) {
        val targets = uiState.value.homeElements.filter { it.elementId in ids && !it.locked }
        if (targets.size < 3) {
            showMessage("Select at least 3 items to distribute")
            return
        }
        viewModelScope.launch {
            recordUndoCheckpoint()
            if (axis == "HORIZONTAL") {
                val sorted = targets.sortedBy { it.xFraction }
                val start = sorted.first().xFraction
                val end = sorted.last().xFraction
                val step = (end - start) / (sorted.size - 1)
                sorted.forEachIndexed { idx, el ->
                    repository.saveHomeElement(el.copy(xFraction = (start + step * idx).coerceIn(0f, 0.90f)))
                }
            } else {
                val sorted = targets.sortedBy { it.yFraction }
                val start = sorted.first().yFraction
                val end = sorted.last().yFraction
                val step = (end - start) / (sorted.size - 1)
                sorted.forEachIndexed { idx, el ->
                    repository.saveHomeElement(el.copy(yFraction = (start + step * idx).coerceIn(0f, 0.90f)))
                }
            }
            showMessage("Distributed ${targets.size} items")
        }
    }

    fun matchElementSizes(ids: Set<Long>, dimension: String) {
        val targets = uiState.value.homeElements.filter { it.elementId in ids && !it.locked }
        if (targets.size < 2) return
        val ref = uiState.value.homeElements.find { it.elementId == transientState.value.selectedElementId }
            ?: targets.first()
        viewModelScope.launch {
            recordUndoCheckpoint()
            targets.forEach { el ->
                repository.saveHomeElement(
                    el.copy(
                        widthDp = if (dimension == "WIDTH" || dimension == "BOTH") ref.widthDp else el.widthDp,
                        heightDp = if (dimension == "HEIGHT" || dimension == "BOTH") ref.heightDp else el.heightDp,
                    )
                )
            }
            showMessage("Matched size across ${targets.size} items")
        }
    }

    fun stackElements(ids: Set<Long>) {
        val candidates = uiState.value.homeElements.filter {
            it.elementId in ids && it.parentContainerId == null && it.type != "WIDGET_STACK"
        }
        if (candidates.size < 2) {
            showMessage("Select 2 or more items on the page to create a Widget Stack")
            return
        }
        viewModelScope.launch {
            recordUndoCheckpoint()
            val first = candidates.minByOrNull { it.yFraction } ?: candidates.first()
            val maxW = candidates.maxOf { it.widthDp }.coerceAtLeast(150f)
            val maxH = candidates.maxOf { it.heightDp }.coerceAtLeast(120f)
            val maxZ = uiState.value.homeElements.maxOfOrNull { it.zIndex } ?: 0
            val stackId = repository.saveHomeElement(
                HomeElementEntity(
                    pageId = first.pageId,
                    ownerType = first.ownerType,
                    ownerWindowId = first.ownerWindowId,
                    type = "WIDGET_STACK",
                    xFraction = first.xFraction,
                    yFraction = first.yFraction,
                    widthDp = maxW,
                    heightDp = maxH,
                    zIndex = maxZ + 1,
                    label = "Widget Stack",
                    backgroundColorHex = 0xAA0E1629L,
                    borderColorHex = 0x6638BDF8L,
                    borderWidthDp = 1.2f,
                    cornerRadiusDp = 24f,
                    styleExtrasJson = ElementStyleExtras(
                        stackSwipeAxis = "VERTICAL",
                        stackIndicatorStyle = "DOTS",
                    ).toJson(),
                )
            )
            candidates.forEachIndexed { idx, child ->
                repository.saveHomeElement(
                    child.copy(
                        parentContainerId = stackId,
                        pinnedToAllPages = false,
                        xFraction = 0f,
                        yFraction = 0f,
                        zIndex = idx,
                    )
                )
            }
            transientState.update {
                it.copy(multiSelectedIds = emptySet(), selectedElementId = stackId)
            }
            showMessage("Stacked ${candidates.size} items")
        }
    }

    fun unstackElement(stackId: Long) {
        val stack = uiState.value.homeElements.find { it.elementId == stackId } ?: return
        val children = uiState.value.homeElements.filter { it.parentContainerId == stackId }.sortedBy { it.zIndex }
        viewModelScope.launch {
            recordUndoCheckpoint()
            val baseZ = (uiState.value.homeElements.maxOfOrNull { it.zIndex } ?: 0) + 1
            children.forEachIndexed { idx, child ->
                repository.saveHomeElement(
                    child.copy(
                        pageId = stack.pageId,
                        ownerType = stack.ownerType,
                        ownerWindowId = stack.ownerWindowId,
                        parentContainerId = null,
                        xFraction = (stack.xFraction + (idx * 0.04f)).coerceIn(0f, 0.85f),
                        yFraction = (stack.yFraction + (idx * 0.04f)).coerceIn(0f, 0.85f),
                        zIndex = baseZ + idx,
                    )
                )
            }
            repository.deleteHomeElement(stackId)
            transientState.update { it.copy(selectedElementId = children.firstOrNull()?.elementId) }
            showMessage("Unstacked ${children.size} items")
        }
    }

    fun addChildToContainer(parentId: Long, child: HomeElementEntity) {
        val siblings = uiState.value.homeElements.filter { it.parentContainerId == parentId }
        val nextZ = (siblings.maxOfOrNull { it.zIndex } ?: -1) + 1
        saveHomeElement(
            child.copy(
                parentContainerId = parentId,
                pinnedToAllPages = false,
                xFraction = 0f,
                yFraction = 0f,
                zIndex = nextZ,
            ),
            selectAfter = false,
        )
    }

    fun moveChildInParent(childId: Long, delta: Int) {
        val child = uiState.value.homeElements.find { it.elementId == childId } ?: return
        val parentId = child.parentContainerId ?: return
        val siblings = uiState.value.homeElements
            .filter { it.parentContainerId == parentId }
            .sortedWith(compareBy<HomeElementEntity> { it.zIndex }.thenBy { it.elementId })
            .toMutableList()
        val idx = siblings.indexOfFirst { it.elementId == childId }
        if (idx == -1) return
        val target = (idx + delta).coerceIn(0, siblings.lastIndex)
        if (target == idx) return
        val moved = siblings.removeAt(idx)
        siblings.add(target, moved)
        viewModelScope.launch {
            recordUndoCheckpoint()
            siblings.forEachIndexed { i, item ->
                repository.saveHomeElement(item.copy(zIndex = i))
            }
        }
    }

    fun cycleWidgetStack(stackId: Long, delta: Int) {
        val children = uiState.value.homeElements.filter { it.parentContainerId == stackId }
        if (children.isEmpty()) return
        transientState.update { t ->
            val curr = t.stackIndexOverrides[stackId] ?: 0
            val next = ((curr + delta) % children.size + children.size) % children.size
            t.copy(stackIndexOverrides = t.stackIndexOverrides + (stackId to next))
        }
    }

    fun setActiveContainerFocus(containerId: Long?) {
        transientState.update { it.copy(activeContainerFocusId = containerId) }
    }

    // --- SNAPSHOTS ---
    fun saveLayoutSnapshot(name: String) {
        viewModelScope.launch {
            repository.saveCurrentLayoutSnapshot(name)
            showMessage("Saved theme \"$name\"")
        }
    }

    fun restoreLayoutSnapshot(snapshot: LayoutSnapshotEntity) {
        viewModelScope.launch {
            recordUndoCheckpoint()
            repository.restoreLayoutSnapshot(snapshot)
            transientState.update { it.copy(selectedElementId = null, currentPageIndex = 0) }
            showMessage("Restored \"${snapshot.name}\"")
        }
    }

    fun deleteSnapshot(snapshotId: Long) {
        viewModelScope.launch { repository.deleteSnapshot(snapshotId) }
    }

    // --- SIDEBARS ---
    fun saveSidebar(sidebar: SidebarConfigEntity) {
        viewModelScope.launch { repository.saveSidebar(sidebar) }
    }

    fun deleteSidebar(sidebarId: Long) {
        viewModelScope.launch { repository.deleteSidebar(sidebarId) }
    }

    fun observeTagWithApps(tagId: Long) = repository.observeTagWithApps(tagId)

    fun observeAppsForTag(tagId: Long) = repository.observeAppsForTag(tagId)

    fun observeTagsForApp(packageName: String) = repository.observeTagsForApp(packageName)

    fun createOrUpdateTagSidebar(tagId: Long, side: String = "RIGHT", openAfter: Boolean = true) {
        viewModelScope.launch {
            val sidebarId = repository.createOrUpdateTagTriggeredSidebar(tagId, side)
            if (openAfter) {
                openConfiguredSidebar(sidebarId)
            }
            val tagName = uiState.value.allTags.find { it.tagId == tagId }?.name ?: "Tag"
            showMessage("$tagName pinned to $side edge sidebar")
        }
    }

    fun returnToDefaultHomePage() {
        val visiblePages = uiState.value.homePages.filter { !it.isHidden }
        val defIdx = visiblePages.indexOfFirst { it.isDefaultHome }.takeIf { it >= 0 } ?: 0
        setCurrentPageIndex(defIdx)
    }

    // --- GESTURES & ACTION MACROS ---
    fun saveGestureBinding(binding: GestureBindingEntity) {
        viewModelScope.launch { repository.saveGestureBinding(binding) }
    }

    fun resetDefaultGestures() {
        viewModelScope.launch {
            repository.resetDefaultGestures()
            showMessage("Restored default home screen gestures")
        }
    }

    fun triggerGesture(
        gestureType: String,
        onNavigate: (LauncherRoute) -> Unit,
        onScrollPage: (Int) -> Unit,
    ) {
        val binding = uiState.value.gestures[gestureType] ?: return
        executeAction(
            actionType = binding.actionType,
            targetPackageName = binding.targetPackageName,
            targetTagId = binding.targetTagId,
            targetCategoryId = binding.targetCategoryId,
            targetSidebarId = binding.targetSidebarId,
            targetWindowId = binding.targetWindowId,
            targetRuleId = binding.targetRuleId,
            targetUrl = binding.targetUrl,
            macroStepsJson = binding.macroStepsJson,
            onNavigate = onNavigate,
            onScrollPage = onScrollPage,
        )
    }

    fun executeElementActions(
        actionsConfigJson: String,
        trigger: String, // "TAP", "DOUBLE_TAP", "LONG_PRESS", "SWIPE_UP", "SWIPE_DOWN", "SWIPE_LEFT", "SWIPE_RIGHT"
        onNavigate: (LauncherRoute) -> Unit,
        onScrollPage: (Int) -> Unit,
    ): Boolean {
        if (actionsConfigJson.isBlank()) return false
        return try {
            val obj = JSONObject(actionsConfigJson)
            val actionObj = obj.optJSONObject(trigger) ?: return false
            val type = actionObj.optString("actionType", "NONE")
            if (type == "NONE") return false
            executeAction(
                actionType = type,
                targetPackageName = actionObj.optString("targetPackage").takeIf { it.isNotBlank() },
                targetTagId = if (actionObj.has("targetTagId") && !actionObj.isNull("targetTagId")) actionObj.optLong("targetTagId") else null,
                targetCategoryId = if (actionObj.has("targetCategoryId") && !actionObj.isNull("targetCategoryId")) actionObj.optLong("targetCategoryId") else null,
                targetSidebarId = if (actionObj.has("targetSidebarId") && !actionObj.isNull("targetSidebarId")) actionObj.optLong("targetSidebarId") else null,
                targetWindowId = if (actionObj.has("targetWindowId") && !actionObj.isNull("targetWindowId")) actionObj.optLong("targetWindowId") else null,
                targetRuleId = if (actionObj.has("targetRuleId") && !actionObj.isNull("targetRuleId")) actionObj.optLong("targetRuleId") else null,
                targetUrl = actionObj.optString("targetUrl").takeIf { it.isNotBlank() },
                macroStepsJson = actionObj.optJSONArray("macro")?.toString(),
                onNavigate = onNavigate,
                onScrollPage = onScrollPage,
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    fun executeAction(
        actionType: String,
        targetPackageName: String? = null,
        targetTagId: Long? = null,
        targetCategoryId: Long? = null,
        targetSidebarId: Long? = null,
        targetWindowId: Long? = null,
        targetRuleId: Long? = null,
        targetUrl: String? = null,
        macroStepsJson: String? = null,
        onNavigate: (LauncherRoute) -> Unit,
        onScrollPage: (Int) -> Unit,
    ) {
        when (actionType) {
            "OPEN_DRAWER" -> setDrawerOpen(true)
            "OPEN_SEARCH" -> setUniversalSearchOpen(true)
            "OPEN_TAG" -> targetTagId?.let { openTagSidebar(it) }
            "OPEN_CATEGORY" -> targetCategoryId?.let { openCategorySidebar(it) }
            "OPEN_SIDEBAR" -> targetSidebarId?.let { openConfiguredSidebar(it) }
            "OPEN_WINDOW" -> targetWindowId?.let { openWindow(it) }
            "OPEN_RULE" -> targetRuleId?.let { openRuleSidebar(it) }
            "OPEN_APP" -> targetPackageName?.let { pkg ->
                uiState.value.appsWithTags.find { it.app.packageName == pkg }?.app?.let { launchApp(it) }
            }
            "OPEN_APP_SHORTCUT" -> if (!targetPackageName.isNullOrBlank()) {
                val app = uiState.value.appsWithTags.find { it.app.packageName == targetPackageName }?.app
                val launchedShortcut = !targetUrl.isNullOrBlank() &&
                    appDiscovery.launchAppShortcut(targetPackageName, targetUrl, app?.componentName)
                if (!launchedShortcut) {
                    if (app != null) launchApp(app) else showMessage("Shortcut unavailable on this device")
                }
            }
            "OPEN_APP_INFO" -> targetPackageName?.let { pkg ->
                appDiscovery.openAppInfo(pkg)
            }
            "OPEN_WEBSITE" -> targetUrl?.let { appDiscovery.openUrl(it) }
            "OPEN_SYSTEM_SETTING" -> appDiscovery.openSystemSettings(targetUrl ?: Settings.ACTION_SETTINGS)
            "OPEN_SETTINGS" -> onNavigate(LauncherRoute.Settings)
            "OPEN_TAG_MANAGER" -> onNavigate(LauncherRoute.Tags)
            "OPEN_APP_MANAGER" -> onNavigate(LauncherRoute.Apps)
            "OPEN_SIDEBAR_MANAGER" -> onNavigate(LauncherRoute.Sidebars)
            "NEXT_PAGE" -> {
                val pages = uiState.value.homePages.size
                if (pages > 1) {
                    val next = if (uiState.value.settings.infinitePageLoop) {
                        (uiState.value.currentPageIndex + 1) % pages
                    } else {
                        (uiState.value.currentPageIndex + 1).coerceAtMost(pages - 1)
                    }
                    onScrollPage(next)
                }
            }
            "PREV_PAGE" -> {
                val pages = uiState.value.homePages.size
                if (pages > 1) {
                    val prev = if (uiState.value.settings.infinitePageLoop) {
                        (uiState.value.currentPageIndex - 1 + pages) % pages
                    } else {
                        (uiState.value.currentPageIndex - 1).coerceAtLeast(0)
                    }
                    onScrollPage(prev)
                }
            }
            "JUMP_PAGE" -> {
                val idx = targetUrl?.trim()?.toIntOrNull()
                if (idx != null && idx in uiState.value.homePages.indices) {
                    onScrollPage(idx)
                }
            }
            "SHOW_OBJECT" -> {
                val elemId = targetUrl?.trim()?.toLongOrNull()
                if (elemId != null) {
                    transientState.update {
                        it.copy(
                            runtimeShownElementIds = it.runtimeShownElementIds + elemId,
                            runtimeHiddenElementIds = it.runtimeHiddenElementIds - elemId,
                        )
                    }
                } else if (!targetUrl.isNullOrBlank()) {
                    val group = targetUrl.trim()
                    transientState.update {
                        it.copy(
                            activeVisibilityGroups = it.activeVisibilityGroups + group,
                            hiddenVisibilityGroups = it.hiddenVisibilityGroups - group,
                        )
                    }
                }
            }
            "HIDE_OBJECT" -> {
                val elemId = targetUrl?.trim()?.toLongOrNull()
                if (elemId != null) {
                    transientState.update {
                        it.copy(
                            runtimeHiddenElementIds = it.runtimeHiddenElementIds + elemId,
                            runtimeShownElementIds = it.runtimeShownElementIds - elemId,
                        )
                    }
                } else if (!targetUrl.isNullOrBlank()) {
                    val group = targetUrl.trim()
                    transientState.update {
                        it.copy(
                            hiddenVisibilityGroups = it.hiddenVisibilityGroups + group,
                            activeVisibilityGroups = it.activeVisibilityGroups - group,
                        )
                    }
                }
            }
            "TOGGLE_OBJECT" -> {
                val elemId = targetUrl?.trim()?.toLongOrNull()
                if (elemId != null) {
                    transientState.update { t ->
                        val elem = uiState.value.homeElements.find { it.elementId == elemId }
                        val currentlyVisible = when {
                            elemId in t.runtimeHiddenElementIds -> false
                            elemId in t.runtimeShownElementIds -> true
                            else -> elem?.visible ?: true
                        }
                        if (currentlyVisible) {
                            t.copy(
                                runtimeHiddenElementIds = t.runtimeHiddenElementIds + elemId,
                                runtimeShownElementIds = t.runtimeShownElementIds - elemId,
                            )
                        } else {
                            t.copy(
                                runtimeShownElementIds = t.runtimeShownElementIds + elemId,
                                runtimeHiddenElementIds = t.runtimeHiddenElementIds - elemId,
                            )
                        }
                    }
                } else if (!targetUrl.isNullOrBlank()) {
                    val group = targetUrl.trim()
                    transientState.update { t ->
                        if (group in t.hiddenVisibilityGroups) {
                            t.copy(
                                hiddenVisibilityGroups = t.hiddenVisibilityGroups - group,
                                activeVisibilityGroups = t.activeVisibilityGroups + group,
                            )
                        } else {
                            t.copy(
                                hiddenVisibilityGroups = t.hiddenVisibilityGroups + group,
                                activeVisibilityGroups = t.activeVisibilityGroups - group,
                            )
                        }
                    }
                }
            }
            "STACK_NEXT" -> targetUrl?.trim()?.toLongOrNull()?.let { cycleWidgetStack(it, +1) }
            "STACK_PREV" -> targetUrl?.trim()?.toLongOrNull()?.let { cycleWidgetStack(it, -1) }
            "TOGGLE_TORCH" -> {
                val (_, msg) = DeviceControlsManager.toggleTorch(appDiscovery.context)
                showMessage(msg)
            }
            "CYCLE_RINGER" -> {
                val msg = DeviceControlsManager.cycleRingerMode(appDiscovery.context)
                showMessage(msg)
            }
            "OPEN_SYSTEM_PANEL" -> {
                DeviceControlsManager.openSystemPanel(appDiscovery.context, targetUrl ?: "WIFI_PANEL")
            }
            "TOGGLE_EDIT_MODE" -> setEditMode(!uiState.value.isEditMode)
            "MACRO" -> {
                if (!macroStepsJson.isNullOrBlank()) {
                    viewModelScope.launch {
                        try {
                            val arr = JSONArray(macroStepsJson)
                            for (i in 0 until arr.length()) {
                                val step = arr.getJSONObject(i)
                                val delayMs = step.optLong("delayMs", 0L)
                                if (delayMs > 0) delay(delayMs.coerceAtMost(5000L))
                                executeAction(
                                    actionType = step.optString("actionType", "NONE"),
                                    targetPackageName = step.optString("targetPackage").takeIf { it.isNotBlank() },
                                    targetTagId = if (step.has("targetTagId") && !step.isNull("targetTagId")) step.optLong("targetTagId") else null,
                                    targetCategoryId = if (step.has("targetCategoryId") && !step.isNull("targetCategoryId")) step.optLong("targetCategoryId") else null,
                                    targetSidebarId = if (step.has("targetSidebarId") && !step.isNull("targetSidebarId")) step.optLong("targetSidebarId") else null,
                                    targetWindowId = if (step.has("targetWindowId") && !step.isNull("targetWindowId")) step.optLong("targetWindowId") else null,
                                    targetRuleId = if (step.has("targetRuleId") && !step.isNull("targetRuleId")) step.optLong("targetRuleId") else null,
                                    targetUrl = step.optString("targetUrl").takeIf { it.isNotBlank() },
                                    onNavigate = onNavigate,
                                    onScrollPage = onScrollPage,
                                )
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }
    }

    // --- CUSTOM FONTS (.TTF / .OTF) ---
    fun importCustomFont(uri: Uri, preferredName: String? = null, applyImmediately: Boolean = true) {
        viewModelScope.launch {
            val imported = fontStorage.importFontFromUri(uri, preferredName)
            if (imported == null) {
                showMessage("Could not import font. Select a valid .ttf or .otf file.")
                return@launch
            }
            val id = repository.saveCustomFont(imported)
            val saved = imported.copy(fontId = id)
            if (applyImmediately) {
                applyCustomFontGlobally(saved)
            } else {
                showMessage("Imported font \"${saved.name}\"")
            }
        }
    }

    fun applyCustomFontGlobally(font: CustomFontEntity?) {
        viewModelScope.launch {
            if (font == null) {
                repository.updateSettings {
                    it.copy(
                        fontStyle = if (it.fontStyle == "CUSTOM") "DEFAULT" else it.fontStyle,
                        customFontId = null,
                        customFontPath = null,
                    )
                }
                showMessage("Switched back to built-in font")
            } else {
                repository.updateSettings {
                    it.copy(
                        fontStyle = "CUSTOM",
                        customFontId = font.fontId,
                        customFontPath = font.localPath,
                    )
                }
                showMessage("Applied font \"${font.name}\"")
            }
        }
    }

    fun deleteCustomFont(font: CustomFontEntity) {
        viewModelScope.launch {
            if (uiState.value.settings.customFontId == font.fontId) {
                repository.updateSettings {
                    it.copy(fontStyle = "DEFAULT", customFontId = null, customFontPath = null)
                }
            }
            repository.deleteCustomFont(font)
            showMessage("Deleted font \"${font.name}\"")
        }
    }

    // --- THEME PROFILES & STARTER TEMPLATES ---
    fun saveThemeProfile(name: String) {
        val clean = name.trim().ifBlank { "Custom Theme" }
        viewModelScope.launch {
            val json = BackupManager.encodeSettingsObject(uiState.value.settings).toString()
            repository.saveThemeProfile(ThemeProfileEntity(name = clean, settingsJson = json))
            showMessage("Saved theme profile \"$clean\"")
        }
    }

    fun applyThemeProfile(profile: ThemeProfileEntity) {
        viewModelScope.launch {
            try {
                val decoded = BackupManager.decodeSettingsObject(JSONObject(profile.settingsJson))
                repository.updateSettings { current ->
                    decoded.copy(
                        lockedAppsCsv = current.lockedAppsCsv,
                        cloudBackupEnabled = current.cloudBackupEnabled,
                        cloudBackupTreeUri = current.cloudBackupTreeUri,
                        seededDefaults = true,
                    )
                }
                showMessage("Applied theme \"${profile.name}\"")
            } catch (_: Exception) {
                showMessage("Failed to apply theme profile")
            }
        }
    }

    fun deleteThemeProfile(profileId: Long) {
        viewModelScope.launch {
            repository.deleteThemeProfile(profileId)
            showMessage("Deleted theme profile")
        }
    }

    fun applyStarterTemplate(templateId: String) {
        viewModelScope.launch {
            val descriptor = StarterTemplates.catalog.find { it.id == templateId }
            val title = descriptor?.title ?: templateId
            // Automatic safety backup snapshot before replacing workspace
            repository.saveCurrentLayoutSnapshot("Auto-Backup before $title")
            recordUndoCheckpoint()
            val generated = StarterTemplates.generate(
                templateId = templateId,
                apps = uiState.value.appsWithTags.map { it.app },
                tags = uiState.value.allTags,
            )
            repository.replaceHomeLayout(generated.pages, generated.elements)
            repository.updateSettings(generated.settingsTransform)
            transientState.update {
                it.copy(
                    currentPageIndex = 0,
                    selectedElementId = null,
                    multiSelectedIds = emptySet(),
                    activeContainerFocusId = null,
                    stackIndexOverrides = emptyMap(),
                )
            }
            showMessage("Applied \"$title\" (previous layout saved to Snapshots)")
        }
    }

    // --- SETTINGS, IMAGE LIBRARY & BACKGROUND PRESETS ---
    fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        viewModelScope.launch { repository.updateSettings(transform) }
    }

    suspend fun importImagePreservingOriginal(uri: Uri, folderName: String = "Imported", customName: String? = null): String? {
        val resource = imageStorage.importImageResource(uri, name = customName, folderName = folderName) ?: return null
        repository.saveImageResource(resource)
        return resource.localPath
    }

    fun renameImageResource(resource: ImageResourceEntity, newName: String, folderName: String) {
        viewModelScope.launch {
            repository.saveImageResource(resource.copy(name = newName.trim().ifBlank { resource.name }, folderName = folderName.trim().ifBlank { "Imported" }))
            showMessage("Updated image resource")
        }
    }

    fun deleteImageResource(resource: ImageResourceEntity) {
        viewModelScope.launch {
            repository.deleteImageResource(resource)
            showMessage("Deleted image resource")
        }
    }

    fun saveBackgroundPreset(preset: BackgroundPresetEntity) {
        viewModelScope.launch {
            repository.saveBackgroundPreset(preset)
            showMessage("Saved background preset \"${preset.name}\"")
        }
    }

    fun deleteBackgroundPreset(presetId: Long) {
        viewModelScope.launch { repository.deleteBackgroundPreset(presetId) }
    }

    // --- BACKUP & RESTORE ---
    suspend fun exportFullBackupJson(): String = repository.exportFullBackupJson()

    fun importFullBackupJson(json: String) {
        viewModelScope.launch {
            try {
                repository.importFullBackupJson(json)
                showMessage("Backup restored successfully")
            } catch (_: Exception) {
                showMessage("Invalid backup file")
            }
        }
    }

    private data class DataSlice1(
        val apps: List<AppWithTags>,
        val categories: List<CategoryEntity>,
        val appCategories: List<AppCategoryCrossRef>,
        val tagsWithApps: List<TagWithApps>,
        val allTags: List<TagEntity>,
    )

    private data class DataSlice2(
        val rules: List<SavedTagRuleEntity>,
        val pages: List<HomePageEntity>,
        val windows: List<LauncherWindowEntity>,
        val elements: List<HomeElementEntity>,
        val snapshots: List<LayoutSnapshotEntity>,
    )

    private data class DataSlice3(
        val sidebars: List<SidebarConfigEntity>,
        val gestures: List<GestureBindingEntity>,
        val imageResources: List<ImageResourceEntity>,
        val backgroundPresets: List<BackgroundPresetEntity>,
        val settings: LauncherSettings,
    )

    class Factory(
        private val repository: LauncherRepository,
        private val appDiscovery: AppDiscoveryManager,
        private val imageStorage: ImageStorageManager,
        private val widgetHost: LauncherWidgetHost,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LauncherViewModel(repository, appDiscovery, imageStorage, widgetHost) as T
        }
    }
}
