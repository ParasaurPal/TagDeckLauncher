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
import com.example.data.repository.LauncherRepository
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.ImageStorageManager
import com.example.data.system.LauncherWidgetHost
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

private data class TransientLauncherState(
    val isLoading: Boolean = true,
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

class LauncherViewModel(
    private val repository: LauncherRepository,
    val appDiscovery: AppDiscoveryManager,
    val imageStorage: ImageStorageManager,
    val widgetHost: LauncherWidgetHost,
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

    val uiState: StateFlow<LauncherUiState> = combine(
        dataFlow1,
        dataFlow2,
        dataFlow3,
        transientState,
    ) { d1, d2, d3, t ->
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
            settings = d3.settings,
            isDefaultLauncher = t.isDefaultLauncher,
            currentPageIndex = t.currentPageIndex.coerceIn(0, (d2.pages.size - 1).coerceAtLeast(0)),
            isEditMode = t.isEditMode,
            selectedElementId = t.selectedElementId,
            multiSelectedIds = t.multiSelectedIds,
            clipboardElement = t.clipboardElement,
            canUndo = t.canUndo,
            canRedo = t.canRedo,
            activeOverlay = t.activeOverlay,
            isDrawerOpen = t.isDrawerOpen,
            isUniversalSearchOpen = t.isUniversalSearchOpen,
            snackbarMessage = t.snackbarMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherUiState(),
    )

    init {
        refreshInstalledApps()
        refreshDefaultLauncherStatus()
        viewModelScope.launch {
            appDiscovery.observePackageChanges().collect {
                repository.syncInstalledAppsAndSeedDefaults()
            }
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            repository.syncInstalledAppsAndSeedDefaults()
            transientState.update { it.copy(isLoading = false) }
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

    fun setUniversalSearchOpen(open: Boolean) {
        transientState.update {
            it.copy(
                isUniversalSearchOpen = open,
                isDrawerOpen = if (open) false else it.isDrawerOpen,
                activeOverlay = if (open) ActiveOverlay.None else it.activeOverlay,
            )
        }
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
            val nextOrder = (uiState.value.homePages.maxOfOrNull { it.pageOrder } ?: -1) + 1
            repository.saveHomePage(HomePageEntity(title = title, pageOrder = nextOrder))
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
            repository.duplicateHomePage(page)
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
            transientState.update { it.copy(currentPageIndex = 0) }
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
            repository.deleteHomeElement(element.elementId)
            if (transientState.value.selectedElementId == element.elementId) {
                transientState.update { it.copy(selectedElementId = null) }
            }
        }
    }

    fun bringElementForward(element: HomeElementEntity) {
        val maxZ = uiState.value.homeElements.filter { it.pageId == element.pageId }.maxOfOrNull { it.zIndex } ?: 0
        saveHomeElement(element.copy(zIndex = maxZ + 1))
    }

    fun sendElementBackward(element: HomeElementEntity) {
        val minZ = uiState.value.homeElements.filter { it.pageId == element.pageId }.minOfOrNull { it.zIndex } ?: 0
        saveHomeElement(element.copy(zIndex = (minZ - 1).coerceAtLeast(0)))
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

    // --- GESTURES & ACTION MACROS ---
    fun saveGestureBinding(binding: GestureBindingEntity) {
        viewModelScope.launch { repository.saveGestureBinding(binding) }
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
            "OPEN_WEBSITE" -> targetUrl?.let { appDiscovery.openUrl(it) }
            "OPEN_SYSTEM_SETTING" -> appDiscovery.openSystemSettings(targetUrl ?: Settings.ACTION_SETTINGS)
            "OPEN_SETTINGS" -> onNavigate(LauncherRoute.Settings)
            "OPEN_TAG_MANAGER" -> onNavigate(LauncherRoute.Tags)
            "OPEN_APP_MANAGER" -> onNavigate(LauncherRoute.Apps)
            "NEXT_PAGE" -> {
                val pages = uiState.value.homePages.size
                if (pages > 1) {
                    val next = (uiState.value.currentPageIndex + 1).coerceAtMost(pages - 1)
                    onScrollPage(next)
                }
            }
            "PREV_PAGE" -> {
                val prev = (uiState.value.currentPageIndex - 1).coerceAtLeast(0)
                onScrollPage(prev)
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
