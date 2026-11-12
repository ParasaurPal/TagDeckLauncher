package com.example.data.repository

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
import com.example.data.db.LauncherDao
import com.example.data.db.LauncherWindowEntity
import com.example.data.db.LayoutSnapshotEntity
import com.example.data.db.PlayCategoryCatalog
import com.example.data.db.SavedTagRuleEntity
import com.example.data.db.SidebarConfigEntity
import com.example.data.db.TagEntity
import com.example.data.db.TagWithApps
import com.example.data.prefs.LauncherPreferencesRepository
import com.example.data.prefs.LauncherSettings
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.BackupManager
import com.example.data.system.FullBackupPayload
import com.example.data.system.ImageStorageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class TagMatchMode { AND, OR }

object TagFilterEngine {
    fun filterApps(
        appsWithTags: List<AppWithTags>,
        includeTagIds: Set<Long>,
        excludeTagIds: Set<Long>,
        matchMode: TagMatchMode,
        includeHidden: Boolean = false,
        allTags: List<TagEntity> = emptyList(),
        includeNestedTags: Boolean = false,
    ): List<AppWithTags> {
        val expandedIncludes = if (includeNestedTags && allTags.isNotEmpty()) {
            includeTagIds.flatMap { collectDescendantTagIds(it, allTags) }.toSet()
        } else {
            includeTagIds
        }
        return appsWithTags.filter { item ->
            if (!includeHidden && item.app.hidden) return@filter false
            val appTagIds = item.tags.map { it.tagId }.toSet()
            val matchesInclude = when {
                includeTagIds.isEmpty() -> true
                matchMode == TagMatchMode.AND -> includeTagIds.all { req ->
                    if (includeNestedTags && allTags.isNotEmpty()) {
                        collectDescendantTagIds(req, allTags).any { it in appTagIds }
                    } else req in appTagIds
                }
                else -> expandedIncludes.any { it in appTagIds }
            }
            val matchesExclude = excludeTagIds.any { it in appTagIds }
            matchesInclude && !matchesExclude
        }
    }

    fun collectDescendantTagIds(rootTagId: Long, allTags: List<TagEntity>): Set<Long> {
        val result = linkedSetOf(rootTagId)
        val queue = ArrayDeque<Long>()
        queue.add(rootTagId)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            allTags.filter { it.parentTagId == current }.forEach { child ->
                if (result.add(child.tagId)) {
                    queue.add(child.tagId)
                }
            }
        }
        return result
    }
}

class LauncherRepository(
    private val dao: LauncherDao,
    private val appDiscovery: AppDiscoveryManager,
    private val prefsRepo: LauncherPreferencesRepository,
    private val imageStorage: ImageStorageManager? = null,
) {
    private val syncMutex = Mutex()

    val appsFlow: Flow<List<AppEntity>> = dao.observeInstalledApps()
    val appsWithTagsFlow: Flow<List<AppWithTags>> = dao.observeAppsWithTags()
    val categoriesFlow: Flow<List<CategoryEntity>> = dao.observeAllCategories()
    val appCategoriesFlow: Flow<List<AppCategoryCrossRef>> = dao.observeAllAppCategories()
    val tagsFlow: Flow<List<TagEntity>> = dao.observeAllTags()
    val tagsWithAppsFlow: Flow<List<TagWithApps>> = dao.observeTagsWithApps()
    val crossRefsFlow: Flow<List<AppTagCrossRef>> = dao.observeAllCrossRefs()
    val savedRulesFlow: Flow<List<SavedTagRuleEntity>> = dao.observeSavedRules()
    val homePagesFlow: Flow<List<HomePageEntity>> = dao.observeHomePages()
    val windowsFlow: Flow<List<LauncherWindowEntity>> = dao.observeWindows()
    val homeElementsFlow: Flow<List<HomeElementEntity>> = dao.observeHomeElements()
    val snapshotsFlow: Flow<List<LayoutSnapshotEntity>> = dao.observeSnapshots()
    val sidebarsFlow: Flow<List<SidebarConfigEntity>> = dao.observeSidebars()
    val gesturesFlow: Flow<List<GestureBindingEntity>> = dao.observeGestures()
    val imageResourcesFlow: Flow<List<ImageResourceEntity>> = dao.observeImageResources()
    val backgroundPresetsFlow: Flow<List<BackgroundPresetEntity>> = dao.observeBackgroundPresets()
    val settingsFlow: Flow<LauncherSettings> = prefsRepo.settingsFlow

    suspend fun syncInstalledAppsAndSeedDefaults() = syncMutex.withLock {
        ensureSeedPlayCategories()

        val discovered = appDiscovery.discoverLaunchableApps()
        val existingByPkg = dao.getAllAppsOnce().associateBy { it.packageName }
        val existingCatRefs = dao.getAllAppCategoriesOnce().groupBy { it.packageName }
        val newAutoCatRefs = mutableListOf<AppCategoryCrossRef>()

        val merged = discovered.mapIndexed { idx, d ->
            val old = existingByPkg[d.packageName]
            val classified = AppCategorizer.classify(appDiscovery.context, d.packageName, d.label)

            if (old != null) {
                // Preserve manual overrides! Only update automatic fields when not overridden
                val primaryCat = if (old.manualCategoryOverride) old.primaryCategoryId else classified.primaryCategoryId
                val isGame = if (old.manualCategoryOverride) old.isGame else classified.isGame
                if (!old.manualCategoryOverride && existingCatRefs[d.packageName].isNullOrEmpty()) {
                    newAutoCatRefs.add(AppCategoryCrossRef(d.packageName, classified.primaryCategoryId, isManualAssignment = false))
                    classified.additionalCategoryIds.forEach { subId ->
                        newAutoCatRefs.add(AppCategoryCrossRef(d.packageName, subId, isManualAssignment = false))
                    }
                }
                old.copy(
                    componentName = d.componentName,
                    appName = d.label,
                    automaticCategoryId = classified.primaryCategoryId,
                    primaryCategoryId = primaryCat,
                    categoryConfidence = classified.confidence,
                    isGame = isGame,
                    installTimeMillis = d.installTimeMillis,
                    isInstalled = true,
                )
            } else {
                newAutoCatRefs.add(AppCategoryCrossRef(d.packageName, classified.primaryCategoryId, isManualAssignment = false))
                classified.additionalCategoryIds.forEach { subId ->
                    newAutoCatRefs.add(AppCategoryCrossRef(d.packageName, subId, isManualAssignment = false))
                }
                AppEntity(
                    packageName = d.packageName,
                    componentName = d.componentName,
                    appName = d.label,
                    primaryCategoryId = classified.primaryCategoryId,
                    automaticCategoryId = classified.primaryCategoryId,
                    manualCategoryOverride = false,
                    categoryConfidence = classified.confidence,
                    isGame = classified.isGame,
                    manualOrder = idx,
                    installTimeMillis = d.installTimeMillis,
                    isInstalled = true,
                )
            }
        }

        if (merged.isNotEmpty()) {
            dao.upsertApps(merged)
            dao.markUninstalledExcept(merged.map { it.packageName })
        }
        if (newAutoCatRefs.isNotEmpty()) {
            dao.insertAppCategoryCrossRefs(newAutoCatRefs)
        }

        val settings = prefsRepo.getSettingsOnce()
        if (!settings.seededDefaults) {
            seedInitialWorkspace(merged)
            prefsRepo.updateSettings { it.copy(seededDefaults = true) }
        }
    }

    private suspend fun ensureSeedPlayCategories() {
        val existing = dao.getAllCategoriesOnce().associateBy { it.categoryId }
        if (existing.size < PlayCategoryCatalog.allSeedCategories.size) {
            val missing = PlayCategoryCatalog.allSeedCategories
                .filter { it.id !in existing }
                .mapIndexed { idx, seed ->
                    CategoryEntity(
                        categoryId = seed.id,
                        name = seed.name,
                        iconKey = seed.iconKey,
                        colorHex = seed.colorHex,
                        backgroundColorHex = (0x24000000L or (seed.colorHex and 0x00FFFFFFL)),
                        parentCategoryId = seed.parentCategoryId,
                        sortOrder = idx,
                        categoryType = seed.categoryType,
                        isCustom = false,
                    )
                }
            if (missing.isNotEmpty()) dao.insertCategories(missing)
        }
    }

    private suspend fun seedInitialWorkspace(installedApps: List<AppEntity>) {
        if (dao.getAllTagsOnce().isEmpty()) {
            val productivityId = dao.insertTag(
                TagEntity(
                    name = "Productivity",
                    description = "Work, notes, mail & daily flow",
                    colorHex = 0xFF38BDF8L,
                    iconKey = "work",
                    sortOrder = 0
                )
            )
            val creativeId = dao.insertTag(
                TagEntity(
                    name = "Creative",
                    description = "Camera, photos, editing & design",
                    colorHex = 0xFFA78BFAL,
                    iconKey = "palette",
                    sortOrder = 1
                )
            )
            val mediaId = dao.insertTag(
                TagEntity(
                    name = "Media",
                    description = "Video, music & podcasts",
                    colorHex = 0xFFFB7185L,
                    iconKey = "play",
                    sortOrder = 2
                )
            )
            val gamingId = dao.insertTag(
                TagEntity(
                    name = "Gaming",
                    description = "Games & interactive entertainment",
                    colorHex = 0xFF34D399L,
                    iconKey = "gamepad",
                    sortOrder = 3
                )
            )
            val toolsId = dao.insertTag(
                TagEntity(
                    name = "Tools",
                    description = "System utilities, settings & browser",
                    colorHex = 0xFFFBBF24L,
                    iconKey = "build",
                    sortOrder = 4
                )
            )

            val refs = mutableListOf<AppTagCrossRef>()
            installedApps.forEach { app ->
                when {
                    app.isGame -> refs.add(AppTagCrossRef(app.packageName, gamingId))
                    app.primaryCategoryId in listOf(1L, 24L) -> refs.add(AppTagCrossRef(app.packageName, creativeId))
                    app.primaryCategoryId in listOf(20L, 31L, 10L) -> refs.add(AppTagCrossRef(app.packageName, mediaId))
                    app.primaryCategoryId in listOf(25L, 5L, 7L) -> refs.add(AppTagCrossRef(app.packageName, productivityId))
                    app.primaryCategoryId == 29L -> refs.add(AppTagCrossRef(app.packageName, toolsId))
                }
            }
            if (refs.isNotEmpty()) dao.insertAppTagCrossRefs(refs)

            dao.upsertSavedRule(
                SavedTagRuleEntity(
                    name = "Focus Studio",
                    colorHex = 0xFF6366F1L,
                    iconKey = "bolt",
                    matchMode = "OR",
                    includeTagIdsCsv = "$productivityId,$creativeId",
                    excludeTagIdsCsv = "$gamingId",
                    sortOrder = 0
                )
            )

            val page1Id = dao.upsertHomePage(HomePageEntity(title = "Main Deck", pageOrder = 0, isDefaultHome = true))
            val page2Id = dao.upsertHomePage(HomePageEntity(title = "Studio Deck", pageOrder = 1, backgroundType = "GRADIENT", backgroundColorHex = 0xFF0A1424L, backgroundGradientEndHex = 0xFF1E1032L))

            val quickWindowId = dao.upsertWindow(
                LauncherWindowEntity(
                    title = "System Telemetry Panel",
                    widthFraction = 0.88f,
                    heightFraction = 0.56f,
                    backgroundColorHex = 0xEE0D1526L,
                )
            )

            val initialElements = mutableListOf<HomeElementEntity>()
            initialElements.add(
                HomeElementEntity(
                    pageId = page1Id,
                    type = "DIGITAL_CLOCK",
                    xFraction = 0.06f,
                    yFraction = 0.03f,
                    widthDp = 240f,
                    heightDp = 104f,
                    zIndex = 0,
                    label = "Live Clock",
                    backgroundColorHex = 0x440D1526L,
                    borderColorHex = 0x4438BDF8L,
                    cornerRadiusDp = 24f,
                )
            )
            initialElements.add(
                HomeElementEntity(
                    pageId = page1Id,
                    type = "BATTERY_WIDGET",
                    xFraction = 0.72f,
                    yFraction = 0.03f,
                    widthDp = 96f,
                    heightDp = 104f,
                    zIndex = 1,
                    label = "Battery",
                    backgroundColorHex = 0x440D1526L,
                    borderColorHex = 0x4434D399L,
                    cornerRadiusDp = 24f,
                )
            )

            listOf(
                Triple(productivityId, "Productivity", 0.06f to 0.24f),
                Triple(creativeId, "Creative", 0.37f to 0.24f),
                Triple(mediaId, "Media", 0.68f to 0.24f),
                Triple(gamingId, "Gaming", 0.06f to 0.41f),
                Triple(toolsId, "Tools", 0.37f to 0.41f),
            ).forEachIndexed { i, (tId, title, pos) ->
                val tagColor = listOf(0xFF38BDF8L, 0xFFA78BFAL, 0xFFFB7185L, 0xFF34D399L, 0xFFFBBF24L)[i]
                initialElements.add(
                    HomeElementEntity(
                        pageId = page1Id,
                        type = "TAG_SHORTCUT",
                        xFraction = pos.first,
                        yFraction = pos.second,
                        widthDp = 104f,
                        heightDp = 88f,
                        zIndex = i + 2,
                        label = title,
                        targetTagId = tId,
                        backgroundColorHex = (0x38000000L or (tagColor and 0x00FFFFFFL)),
                        borderColorHex = (0x99000000L or (tagColor and 0x00FFFFFFL)),
                        cornerRadiusDp = 22f,
                    )
                )
            }

            initialElements.add(
                HomeElementEntity(
                    pageId = page1Id,
                    type = "WINDOW_SHORTCUT",
                    xFraction = 0.68f,
                    yFraction = 0.41f,
                    widthDp = 104f,
                    heightDp = 88f,
                    zIndex = 7,
                    label = "Sys Panel",
                    targetWindowId = quickWindowId,
                    backgroundColorHex = 0x386366F1L,
                    borderColorHex = 0x996366F1L,
                    cornerRadiusDp = 22f,
                )
            )

            installedApps.take(4).forEachIndexed { idx, app ->
                initialElements.add(
                    HomeElementEntity(
                        pageId = page1Id,
                        type = "APP_SHORTCUT",
                        xFraction = 0.06f + (idx * 0.23f),
                        yFraction = 0.62f,
                        widthDp = 82f,
                        heightDp = 94f,
                        zIndex = 10 + idx,
                        label = app.displayName,
                        targetPackageName = app.packageName,
                        backgroundColorHex = 0x22161F33L,
                        borderColorHex = 0x26FFFFFFL,
                        cornerRadiusDp = 20f,
                    )
                )
            }

            // Page 2 widgets
            initialElements.add(
                HomeElementEntity(
                    pageId = page2Id,
                    type = "ANALOG_CLOCK",
                    xFraction = 0.06f,
                    yFraction = 0.04f,
                    widthDp = 148f,
                    heightDp = 148f,
                    zIndex = 0,
                    label = "Analog",
                    backgroundColorHex = 0x550F172AL,
                    borderColorHex = 0x6638BDF8L,
                    cornerRadiusDp = 74f,
                )
            )
            initialElements.add(
                HomeElementEntity(
                    pageId = page2Id,
                    type = "STORAGE_RAM_WIDGET",
                    xFraction = 0.50f,
                    yFraction = 0.04f,
                    widthDp = 172f,
                    heightDp = 148f,
                    zIndex = 1,
                    label = "System",
                    backgroundColorHex = 0x550F172AL,
                    borderColorHex = 0x66A78BFAL,
                    cornerRadiusDp = 24f,
                )
            )
            initialElements.add(
                HomeElementEntity(
                    pageId = page2Id,
                    type = "TAG_COLLECTION_WIDGET",
                    xFraction = 0.06f,
                    yFraction = 0.32f,
                    widthDp = 310f,
                    heightDp = 230f,
                    zIndex = 2,
                    label = "Tools",
                    targetTagId = toolsId,
                    backgroundColorHex = 0x660F172AL,
                    borderColorHex = 0x66FBBF24L,
                    cornerRadiusDp = 26f,
                )
            )

            // Window inner widgets
            initialElements.add(
                HomeElementEntity(
                    pageId = page1Id,
                    ownerType = "WINDOW",
                    ownerWindowId = quickWindowId,
                    type = "STORAGE_RAM_WIDGET",
                    xFraction = 0.06f,
                    yFraction = 0.08f,
                    widthDp = 150f,
                    heightDp = 140f,
                    label = "Storage & RAM",
                )
            )
            initialElements.add(
                HomeElementEntity(
                    pageId = page1Id,
                    ownerType = "WINDOW",
                    ownerWindowId = quickWindowId,
                    type = "COMPASS_WIDGET",
                    xFraction = 0.52f,
                    yFraction = 0.08f,
                    widthDp = 140f,
                    heightDp = 140f,
                    label = "Compass",
                )
            )
            initialElements.add(
                HomeElementEntity(
                    pageId = page1Id,
                    ownerType = "WINDOW",
                    ownerWindowId = quickWindowId,
                    type = "MEDIA_WIDGET",
                    xFraction = 0.06f,
                    yFraction = 0.50f,
                    widthDp = 290f,
                    heightDp = 96f,
                    label = "Media Controls",
                )
            )

            dao.insertHomeElements(initialElements)

            if (dao.getSidebarsOnce().isEmpty()) {
                dao.upsertSidebar(
                    SidebarConfigEntity(
                        title = "Favorites",
                        side = "LEFT",
                        sourceType = "FAVORITES",
                        widthFraction = 0.80f,
                        backgroundColorHex = 0xEE0B1120L,
                        layoutMode = "GRID",
                        columns = 3,
                        sortOrder = 0,
                    )
                )
                dao.upsertSidebar(
                    SidebarConfigEntity(
                        title = "Games Arcade",
                        side = "BOTTOM",
                        sourceType = "CATEGORY",
                        targetCategoryId = PlayCategoryCatalog.GAMES_PARENT_ID,
                        widthFraction = 0.94f,
                        heightFraction = 0.65f,
                        backgroundColorHex = 0xEE0C1626L,
                        layoutMode = "GRID",
                        columns = 4,
                        edgeZoneEnabled = false,
                        sortOrder = 1,
                    )
                )
            }

            if (dao.getGesturesOnce().isEmpty()) {
                dao.upsertGestures(
                    listOf(
                        GestureBindingEntity("SINGLE_TAP", "NONE"),
                        GestureBindingEntity("DOUBLE_TAP", "OPEN_TAG_MANAGER"),
                        GestureBindingEntity("LONG_PRESS", "TOGGLE_EDIT_MODE"),
                        GestureBindingEntity("SWIPE_UP", "OPEN_DRAWER"),
                        GestureBindingEntity("SWIPE_DOWN", "OPEN_SEARCH"),
                        GestureBindingEntity("SWIPE_LEFT", "NEXT_PAGE"),
                        GestureBindingEntity("SWIPE_RIGHT", "PREV_PAGE"),
                        GestureBindingEntity("TWO_FINGER_SWIPE_UP", "OPEN_SETTINGS"),
                        GestureBindingEntity("TWO_FINGER_SWIPE_DOWN", "TOGGLE_EDIT_MODE"),
                    )
                )
            }
        }
    }

    // --- APP OPERATIONS ---
    suspend fun launchApp(app: AppEntity): Boolean {
        val ok = appDiscovery.launchApp(app.packageName, app.componentName)
        if (ok) {
            dao.recordAppLaunch(app.packageName, System.currentTimeMillis())
        }
        return ok
    }

    suspend fun setAppFavorite(packageName: String, favorite: Boolean) = dao.setAppFavorite(packageName, favorite)
    suspend fun setAppHidden(packageName: String, hidden: Boolean) = dao.setAppHidden(packageName, hidden)
    suspend fun setAppCustomName(packageName: String, customName: String?) = dao.setAppCustomName(packageName, customName?.takeIf { it.isNotBlank() })
    suspend fun setAppCustomIcon(
        packageName: String,
        path: String?,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        alpha: Float,
    ) = dao.setAppCustomIcon(packageName, path, scale, offsetX, offsetY, alpha)

    suspend fun updateAppManualOrders(orderedPackages: List<String>) {
        orderedPackages.forEachIndexed { idx, pkg -> dao.updateAppManualOrder(pkg, idx) }
    }

    // --- CATEGORY OPERATIONS & OVERRIDES ---
    suspend fun saveCategory(category: CategoryEntity): Long = dao.upsertCategory(category)
    suspend fun deleteCustomCategory(categoryId: Long) = dao.deleteCustomCategory(categoryId)

    suspend fun setManualCategoriesForApp(
        packageName: String,
        primaryCategoryId: Long,
        allCategoryIds: List<Long>,
        excludedCategoryIds: List<Long> = emptyList(),
    ) {
        val combined = (listOf(primaryCategoryId) + allCategoryIds).distinct()
        val isGame = primaryCategoryId == PlayCategoryCatalog.GAMES_PARENT_ID ||
            combined.any { it in 100L..199L }
        dao.updateAppPrimaryCategory(packageName, primaryCategoryId, isManual = true, isGame = isGame)
        dao.replaceManualCategoriesForApp(packageName, combined, excludedCategoryIds)
    }

    suspend fun bulkSetCategoryForApps(packageNames: List<String>, categoryId: Long) {
        val isGame = categoryId == PlayCategoryCatalog.GAMES_PARENT_ID || categoryId in 100L..199L
        packageNames.forEach { pkg ->
            dao.updateAppPrimaryCategory(pkg, categoryId, isManual = true, isGame = isGame)
            dao.insertAppCategoryCrossRefs(
                listOf(AppCategoryCrossRef(pkg, categoryId, isManualAssignment = true, isExcluded = false))
            )
        }
    }

    suspend fun restoreAutomaticCategoryForApp(app: AppEntity) {
        val classified = AppCategorizer.classify(appDiscovery.context, app.packageName, app.appName)
        dao.updateAppPrimaryCategory(
            app.packageName,
            classified.primaryCategoryId,
            isManual = false,
            isGame = classified.isGame,
        )
        dao.clearAllCategoriesForApp(app.packageName)
        val refs = mutableListOf(AppCategoryCrossRef(app.packageName, classified.primaryCategoryId, isManualAssignment = false))
        classified.additionalCategoryIds.forEach { subId ->
            refs.add(AppCategoryCrossRef(app.packageName, subId, isManualAssignment = false))
        }
        dao.insertAppCategoryCrossRefs(refs)
    }

    // --- TAG OPERATIONS ---
    suspend fun saveTag(tag: TagEntity): Long {
        return if (tag.tagId == 0L) {
            val existing = dao.getAllTagsOnce()
            dao.insertTag(tag.copy(sortOrder = existing.size))
        } else {
            dao.updateTag(tag)
            tag.tagId
        }
    }

    suspend fun deleteTag(tagId: Long) {
        dao.clearParentTagReference(tagId)
        dao.deleteTagById(tagId)
    }

    suspend fun duplicateTag(tag: TagEntity) {
        val newId = dao.insertTag(tag.copy(tagId = 0L, name = "${tag.name} Copy"))
        val apps = dao.getAllCrossRefsOnce().filter { it.tagId == tag.tagId }
        dao.insertAppTagCrossRefs(apps.map { AppTagCrossRef(it.packageName, newId) })
    }

    suspend fun mergeTags(sourceTagId: Long, targetTagId: Long) = dao.mergeTags(sourceTagId, targetTagId)

    suspend fun reorderTags(orderedTagIds: List<Long>) {
        orderedTagIds.forEachIndexed { idx, id -> dao.updateTagSortOrder(id, idx) }
    }

    suspend fun assignAppToTag(packageName: String, tagId: Long) =
        dao.insertAppTagCrossRefs(listOf(AppTagCrossRef(packageName, tagId)))

    suspend fun removeAppFromTag(packageName: String, tagId: Long) =
        dao.removeAppTagCrossRef(packageName, tagId)

    suspend fun replaceAppsForTag(tagId: Long, packageNames: List<String>) =
        dao.replaceAppsForTag(tagId, packageNames)

    suspend fun replaceTagsForApp(packageName: String, tagIds: List<Long>) =
        dao.replaceTagsForApp(packageName, tagIds)

    suspend fun bulkAssignTagsToApps(packageNames: List<String>, tagIds: List<Long>) {
        val refs = packageNames.flatMap { pkg ->
            tagIds.map { tagId -> AppTagCrossRef(pkg, tagId) }
        }
        dao.insertAppTagCrossRefs(refs)
    }

    // --- SAVED RULES ---
    suspend fun saveRule(rule: SavedTagRuleEntity): Long = dao.upsertSavedRule(rule)
    suspend fun deleteRule(ruleId: Long) = dao.deleteSavedRule(ruleId)

    // --- HOME PAGES, WINDOWS & ELEMENTS ---
    suspend fun saveHomePage(page: HomePageEntity): Long = dao.upsertHomePage(page)
    suspend fun deleteHomePage(pageId: Long) = dao.deleteHomePage(pageId)
    suspend fun duplicateHomePage(page: HomePageEntity) {
        val pages = dao.getHomePagesOnce()
        val newPageId = dao.upsertHomePage(
            page.copy(pageId = 0L, title = "${page.title} Copy", pageOrder = pages.size, isDefaultHome = false)
        )
        val elems = dao.getHomeElementsOnce().filter { it.ownerType == "PAGE" && it.pageId == page.pageId }
        dao.insertHomeElements(elems.map { it.copy(elementId = 0L, pageId = newPageId, appWidgetId = null) })
    }

    suspend fun reorderPages(orderedPageIds: List<Long>) {
        val all = dao.getHomePagesOnce().associateBy { it.pageId }
        orderedPageIds.forEachIndexed { idx, id ->
            all[id]?.let { dao.upsertHomePage(it.copy(pageOrder = idx)) }
        }
    }

    suspend fun saveWindow(window: LauncherWindowEntity): Long = dao.upsertWindow(window)
    suspend fun deleteWindow(windowId: Long) = dao.deleteWindow(windowId)

    suspend fun saveHomeElement(element: HomeElementEntity): Long = dao.upsertHomeElement(element)
    suspend fun deleteHomeElement(elementId: Long) = dao.deleteHomeElementById(elementId)

    suspend fun getHomeLayoutOnce(): Pair<List<HomePageEntity>, List<HomeElementEntity>> =
        dao.getHomePagesOnce() to dao.getHomeElementsOnce()

    suspend fun replaceHomeLayout(pages: List<HomePageEntity>, elements: List<HomeElementEntity>) {
        if (pages.isNotEmpty()) {
            dao.deleteAllHomeElements()
            dao.deleteAllHomePages()
            pages.forEach { dao.upsertHomePage(it) }
            dao.insertHomeElements(elements)
        }
    }

    // --- SNAPSHOTS / THEMES ---
    suspend fun saveCurrentLayoutSnapshot(name: String) {
        val pages = dao.getHomePagesOnce()
        val elements = dao.getHomeElementsOnce()
        val json = BackupManager.encodeLayoutSnapshot(pages, elements)
        dao.insertSnapshot(LayoutSnapshotEntity(name = name, payloadJson = json))
    }

    suspend fun restoreLayoutSnapshot(snapshot: LayoutSnapshotEntity) {
        val (pages, elements) = BackupManager.decodeLayoutSnapshot(snapshot.payloadJson)
        replaceHomeLayout(pages, elements)
    }

    suspend fun deleteSnapshot(snapshotId: Long) = dao.deleteSnapshot(snapshotId)

    // --- SIDEBARS ---
    suspend fun saveSidebar(sidebar: SidebarConfigEntity): Long = dao.upsertSidebar(sidebar)
    suspend fun deleteSidebar(sidebarId: Long) = dao.deleteSidebar(sidebarId)

    // --- GESTURES ---
    suspend fun saveGestureBinding(binding: GestureBindingEntity) = dao.upsertGesture(binding)

    // --- IMAGE LIBRARY & BACKGROUND PRESETS ---
    suspend fun saveImageResource(resource: ImageResourceEntity): Long = dao.upsertImageResource(resource)
    suspend fun deleteImageResource(resource: ImageResourceEntity) {
        imageStorage?.deleteImageFile(resource.localPath)
        dao.deleteImageResource(resource.imageId)
    }

    suspend fun saveBackgroundPreset(preset: BackgroundPresetEntity): Long = dao.upsertBackgroundPreset(preset)
    suspend fun deleteBackgroundPreset(presetId: Long) = dao.deleteBackgroundPreset(presetId)

    // --- PREFERENCES ---
    suspend fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) =
        prefsRepo.updateSettings(transform)

    // --- FULL BACKUP & THEME EXPORT/IMPORT ---
    suspend fun exportFullBackupJson(includeEmbeddedImages: Boolean = true): String {
        val imgs = dao.getImageResourcesOnce()
        val embedded = if (includeEmbeddedImages && imageStorage != null) {
            imgs.mapNotNull { img ->
                imageStorage.encodeImageBase64IfSmall(img.localPath)?.let { img.localPath to it }
            }.toMap()
        } else emptyMap()

        val payload = FullBackupPayload(
            apps = dao.getAllAppsOnce(),
            categories = dao.getAllCategoriesOnce(),
            appCategories = dao.getAllAppCategoriesOnce(),
            tags = dao.getAllTagsOnce(),
            crossRefs = dao.getAllCrossRefsOnce(),
            rules = dao.getAllRulesOnce(),
            pages = dao.getHomePagesOnce(),
            windows = dao.getWindowsOnce(),
            elements = dao.getHomeElementsOnce(),
            sidebars = dao.getSidebarsOnce(),
            gestures = dao.getGesturesOnce(),
            imageResources = imgs,
            embeddedImagesBase64 = embedded,
            backgroundPresets = dao.getBackgroundPresetsOnce(),
            settings = prefsRepo.getSettingsOnce(),
        )
        return BackupManager.encodeFullBackup(payload)
    }

    suspend fun importFullBackupJson(json: String) {
        val payload = BackupManager.decodeFullBackup(json)
        val installed = appDiscovery.discoverLaunchableApps().associateBy { it.packageName }

        // Restore embedded image resources if missing locally
        val remappedPaths = mutableMapOf<String, String>()
        payload.imageResources.forEach { img ->
            val existsLocally = java.io.File(img.localPath).exists()
            val finalPath = if (existsLocally) {
                img.localPath
            } else {
                val b64 = payload.embeddedImagesBase64[img.localPath]
                if (b64 != null && imageStorage != null) {
                    imageStorage.restoreImageFromBase64(img.name.replace(" ", "_"), img.fileFormat, b64) ?: img.localPath
                } else img.localPath
            }
            remappedPaths[img.localPath] = finalPath
            dao.upsertImageResource(img.copy(localPath = finalPath))
        }

        if (payload.categories.isNotEmpty()) {
            dao.insertCategories(payload.categories)
        } else {
            ensureSeedPlayCategories()
        }

        val existingApps = dao.getAllAppsOnce().associateBy { it.packageName }
        val restoredApps = payload.apps.map { backedUp ->
            val live = installed[backedUp.packageName]
            val existing = existingApps[backedUp.packageName]
            val mappedIcon = backedUp.customIconPath?.let { remappedPaths[it] ?: it }
            backedUp.copy(
                componentName = live?.componentName ?: existing?.componentName ?: backedUp.componentName,
                appName = live?.label ?: existing?.appName ?: backedUp.appName,
                customIconPath = mappedIcon,
                isInstalled = live != null,
            )
        }
        if (restoredApps.isNotEmpty()) dao.upsertApps(restoredApps)
        if (payload.appCategories.isNotEmpty()) dao.insertAppCategoryCrossRefs(payload.appCategories)

        payload.tags.forEach { tag ->
            dao.insertTag(
                tag.copy(
                    customImagePath = tag.customImagePath?.let { remappedPaths[it] ?: it },
                    backgroundImagePath = tag.backgroundImagePath?.let { remappedPaths[it] ?: it },
                )
            )
        }
        val validPackages = (restoredApps.map { it.packageName } + installed.keys).toSet()
        val validTags = payload.tags.map { it.tagId }.toSet()
        dao.insertAppTagCrossRefs(payload.crossRefs.filter { it.packageName in validPackages && it.tagId in validTags })
        payload.rules.forEach { dao.upsertSavedRule(it) }

        if (payload.windows.isNotEmpty()) {
            dao.deleteAllWindows()
            payload.windows.forEach { dao.upsertWindow(it) }
        }
        if (payload.pages.isNotEmpty()) {
            dao.deleteAllHomeElements()
            dao.deleteAllHomePages()
            payload.pages.forEach { p ->
                dao.upsertHomePage(
                    p.copy(
                        backgroundImagePath = p.backgroundImagePath?.let { remappedPaths[it] ?: it },
                        landscapeBackgroundImagePath = p.landscapeBackgroundImagePath?.let { remappedPaths[it] ?: it },
                    )
                )
            }
            dao.insertHomeElements(
                payload.elements.map { e -> e.copy(imagePath = e.imagePath?.let { remappedPaths[it] ?: it }) }
            )
        }
        if (payload.sidebars.isNotEmpty()) {
            dao.deleteAllSidebars()
            payload.sidebars.forEach { s ->
                dao.upsertSidebar(s.copy(backgroundImagePath = s.backgroundImagePath?.let { remappedPaths[it] ?: it }))
            }
        }
        if (payload.gestures.isNotEmpty()) dao.upsertGestures(payload.gestures)
        payload.backgroundPresets.forEach { dao.upsertBackgroundPreset(it) }
        prefsRepo.updateSettings {
            payload.settings.copy(
                wallpaperCustomPath = payload.settings.wallpaperCustomPath?.let { remappedPaths[it] ?: it },
                allAppsBgImagePath = payload.settings.allAppsBgImagePath?.let { remappedPaths[it] ?: it },
                seededDefaults = true,
            )
        }
    }
}
