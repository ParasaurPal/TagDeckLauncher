package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AppEntity
import com.example.data.db.CategoryEntity
import com.example.data.db.PlayCategoryCatalog
import com.example.data.prefs.LauncherSettings
import com.example.data.system.AppDiscoveryManager
import com.example.ui.state.LauncherUiState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val FILTER_ALL = PlayCategoryCatalog.SYSTEM_ALL_ID
private const val FILTER_FAVORITES = PlayCategoryCatalog.SYSTEM_FAVORITES_ID
private const val FILTER_GAMES = PlayCategoryCatalog.GAMES_PARENT_ID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerOverlay(
    visible: Boolean,
    state: LauncherUiState,
    appDiscovery: AppDiscoveryManager,
    onClose: () -> Unit,
    onLaunchApp: (AppEntity) -> Unit,
    onAppLongPress: (AppEntity) -> Unit,
    onOpenTagSidebar: (Long) -> Unit,
    onEditCategory: (CategoryEntity) -> Unit = {},
    onToggleLayout: () -> Unit,
    onCycleSort: () -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit = {},
    onOpenGlobalSearch: (String) -> Unit = {},
) {
    if (visible) {
        BackHandler { onClose() }
    }

    val edge = state.settings.allAppsSidebarEdge
    val animDuration = (240 * state.settings.animationSpeedScale.coerceIn(0.05f, 2f)).toInt()
    val enterAnim = when (edge) {
        "LEFT" -> slideInHorizontally(tween(animDuration)) { -it } + fadeIn(tween(animDuration))
        "RIGHT" -> slideInHorizontally(tween(animDuration)) { it } + fadeIn(tween(animDuration))
        else -> slideInVertically(tween(animDuration)) { it / 2 } + fadeIn(tween(animDuration))
    }
    val exitAnim = when (edge) {
        "LEFT" -> slideOutHorizontally(tween(animDuration)) { -it } + fadeOut(tween(animDuration))
        "RIGHT" -> slideOutHorizontally(tween(animDuration)) { it } + fadeOut(tween(animDuration))
        else -> slideOutVertically(tween(animDuration)) { it / 2 } + fadeOut(tween(animDuration))
    }

    AnimatedVisibility(
        visible = visible,
        enter = enterAnim,
        exit = exitAnim,
    ) {
        val scope = rememberCoroutineScope()
        val gridState = rememberLazyGridState()
        val listState = rememberLazyListState()

        var query by remember { mutableStateOf("") }
        var selectedFilterId by remember { mutableLongStateOf(FILTER_ALL) }
        var selectedTagFilterId by remember { mutableStateOf<Long?>(null) }
        var showCustomizeDialog by remember { mutableStateOf(false) }

        val allCategory = remember(state.categories) {
            state.categories.find { it.categoryId == FILTER_ALL }
                ?: CategoryEntity(
                    categoryId = FILTER_ALL,
                    name = "All",
                    iconKey = "apps",
                    colorHex = 0xFF38BDF8L,
                    categoryType = "SYSTEM_ALL",
                    sortOrder = -10,
                )
        }
        val favoritesCategory = remember(state.categories) {
            state.categories.find { it.categoryId == FILTER_FAVORITES }
                ?: CategoryEntity(
                    categoryId = FILTER_FAVORITES,
                    name = "Favorites",
                    iconKey = "star",
                    colorHex = 0xFFFBBF24L,
                    categoryType = "SYSTEM_FAVORITES",
                    sortOrder = -9,
                )
        }
        val gamesCategory = remember(state.categories) {
            state.categories.find { it.categoryId == FILTER_GAMES }
                ?: CategoryEntity(
                    categoryId = FILTER_GAMES,
                    name = "Games",
                    iconKey = "gamepad",
                    colorHex = 0xFF34D399L,
                    categoryType = "GAMES_PARENT",
                )
        }
        val gameSubcategories = remember(state.categories) {
            state.categories.filter { it.categoryType == "GAME_SUB" && it.showInMainBar }.sortedBy { it.sortOrder }
        }
        val playAndCustomCategories = remember(state.categories, state.appsWithTags) {
            val nonGame = state.categories.filter {
                !it.isSystemDockTab && it.showInMainBar && it.categoryType != "GAMES_PARENT" && it.categoryType != "GAME_SUB"
            }
            // Order: categories with apps first (preserving sortOrder), then remaining categories
            nonGame.sortedWith(
                compareByDescending<CategoryEntity> { cat ->
                    state.appsWithTags.count { item ->
                        !item.app.hidden && (item.app.primaryCategoryId == cat.categoryId ||
                            item.categories.any { it.categoryId == cat.categoryId })
                    } > 0
                }.thenBy { it.sortOrder }
            )
        }

        val isGamesContext = selectedTagFilterId == null &&
            (selectedFilterId == FILTER_GAMES || gameSubcategories.any { it.categoryId == selectedFilterId })

        val excludedMap = remember(state.appCategories) {
            state.appCategories.filter { it.isExcluded }.groupBy { it.packageName }.mapValues { e ->
                e.value.map { it.categoryId }.toSet()
            }
        }

        val visibleApps = remember(
            state.appsWithTags,
            query,
            selectedFilterId,
            selectedTagFilterId,
            state.settings.drawerSortOrder,
            excludedMap,
        ) {
            val base = state.appsWithTags.filter { !it.app.hidden && it.app.isInstalled }
            val byCategoryOrTag = when {
                selectedTagFilterId != null -> base.filter { item -> item.tags.any { it.tagId == selectedTagFilterId } }
                selectedFilterId == FILTER_ALL -> base
                selectedFilterId == FILTER_FAVORITES -> base.filter { it.app.favorite }
                selectedFilterId == FILTER_GAMES -> base.filter { it.app.isGame || it.app.primaryCategoryId in 100L..199L }
                else -> base.filter { item ->
                    val pkgExcluded = excludedMap[item.app.packageName].orEmpty()
                    selectedFilterId !in pkgExcluded &&
                        (item.app.primaryCategoryId == selectedFilterId || item.categories.any { it.categoryId == selectedFilterId })
                }
            }
            val byQuery = if (query.isBlank()) {
                byCategoryOrTag
            } else {
                val q = query.trim().lowercase()
                byCategoryOrTag.filter {
                    it.app.displayName.lowercase().contains(q) ||
                        it.app.packageName.lowercase().contains(q) ||
                        it.tags.any { t -> t.name.lowercase().contains(q) }
                }
            }
            when (state.settings.drawerSortOrder) {
                "MANUAL" -> byQuery.sortedBy { it.app.manualOrder }
                "MOST_USED" -> byQuery.sortedByDescending { it.app.launchCount }
                "RECENTLY_INSTALLED" -> byQuery.sortedByDescending { it.app.installTimeMillis }
                else -> byQuery.sortedBy { it.app.displayName.lowercase() }
            }
        }

        val activeLetters = remember(visibleApps) {
            visibleApps.map { appIndexLetter(it.app.displayName) }.toSet()
        }

        val activeTag = remember(selectedTagFilterId, state.allTags) {
            selectedTagFilterId?.let { tid -> state.allTags.find { it.tagId == tid } }
        }
        val activeCategory = when {
            selectedTagFilterId != null -> null
            selectedFilterId == FILTER_ALL -> allCategory
            selectedFilterId == FILTER_FAVORITES -> favoritesCategory
            selectedFilterId == FILTER_GAMES -> gamesCategory
            else -> state.categories.find { it.categoryId == selectedFilterId }
        }
        val headerTitle = when {
            activeTag != null -> activeTag.name
            selectedFilterId == FILTER_ALL && allCategory.name == "All" -> "All Apps"
            activeCategory != null -> activeCategory.name
            else -> "Category"
        }

        val useCategoryBg = activeCategory != null && activeCategory.backgroundType != "INHERIT"
        val resolvedBgType = if (useCategoryBg) activeCategory!!.backgroundType else state.settings.allAppsBgType
        val resolvedBgColor = if (useCategoryBg) activeCategory!!.backgroundColorHex else state.settings.allAppsBgColorHex
        val resolvedBgGradEnd = if (useCategoryBg) activeCategory!!.backgroundGradientEndHex else state.settings.allAppsBgGradientEndHex
        val resolvedBgImage = if (useCategoryBg) activeCategory!!.backgroundImagePath else state.settings.allAppsBgImagePath
        val resolvedBgBlur = if (useCategoryBg) activeCategory!!.backgroundBlurDp else state.settings.allAppsBgBlurDp

        val drawerFraction = if (edge == "BOTTOM") 1f else state.settings.allAppsSidebarWidthFraction.coerceIn(0.60f, 1f)
        val boxAlignment = when (edge) {
            "LEFT" -> Alignment.CenterStart
            "RIGHT" -> Alignment.CenterEnd
            else -> Alignment.Center
        }

        val isRevealingDesktop = LocalAppDragCoordinator.current?.session?.isRevealingDesktop == true

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (isRevealingDesktop) 0.05f else 1f },
        ) {
            if (drawerFraction < 0.99f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable { onClose() },
                )
            }
            Surface(
                modifier = Modifier
                    .align(boxAlignment)
                    .fillMaxWidth(drawerFraction)
                    .fillMaxHeight()
                    .pointerInput(Unit) { detectTapGestures {} } // prevent scrim pass-through without merging child semantics
                    .testTag("app_drawer_container"),
                shape = if (drawerFraction < 0.99f) RoundedCornerShape(state.settings.drawerCornerRadiusDp.coerceIn(0f, 40f).dp) else RoundedCornerShape(0.dp),
                color = Color.Transparent,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    SurfaceBackgroundLayer(
                        bgType = resolvedBgType,
                        colorHex = resolvedBgColor,
                        gradientEndHex = resolvedBgGradEnd,
                        imagePath = resolvedBgImage,
                        blurDp = resolvedBgBlur,
                        modifier = Modifier.testTag(
                            if (useCategoryBg) "drawer_bg_custom_${activeCategory!!.backgroundType}" else "drawer_bg_global"
                        ),
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding(),
                    ) {
                        // TOP SECTION: Title, Count, Search & Actions
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (activeTag != null) {
                                        TagBadgeIcon(
                                            iconKey = activeTag.iconKey,
                                            customImagePath = activeTag.customImagePath,
                                            colorHex = activeTag.colorHex,
                                            size = 28.dp,
                                        )
                                    } else if (activeCategory != null) {
                                        CategoryBadgeIcon(activeCategory, size = 28.dp, isOpen = true)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = headerTitle,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Clip,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.62f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp),
                                        )
                                        Text(
                                            text = "${visibleApps.size} apps • ${state.settings.drawerSortOrder.lowercase().replace('_', ' ')}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Clip,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(Color.Black.copy(alpha = 0.68f))
                                                .padding(horizontal = 6.dp, vertical = 3.dp),
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val blackFill = state.settings.sidebarCategoryIconStyle == "BLACK_WHITE"
    val toolbarFill = if (blackFill) Color.Black else Color.White
    val toolbarOutline = if (blackFill) Color.White else Color.Black

    IconButton(onClick = { onOpenGlobalSearch(query) }, modifier = Modifier.testTag("drawer_global_search_button")) {
                                        OutlinedVectorIcon(Icons.Rounded.Search, "Global Search", toolbarFill, toolbarOutline)
                                    }
                                    IconButton(onClick = { showCustomizeDialog = true }) {
                                        OutlinedVectorIcon(Icons.Rounded.Tune, "Customize drawer", toolbarFill, toolbarOutline)
                                    }
                                    IconButton(onClick = onCycleSort) {
                                        OutlinedVectorIcon(Icons.Rounded.SortByAlpha, "Cycle sort order", toolbarFill, toolbarOutline)
                                    }
                                    IconButton(onClick = onToggleLayout) {
                                        OutlinedVectorIcon(
                imageVector = if (state.settings.drawerLayoutMode == "GRID") {
                    Icons.AutoMirrored.Rounded.ViewList
                } else {
                    Icons.Rounded.GridView
                },
                contentDescription = "Toggle grid or list",
                fillColor = toolbarFill,
                outlineColor = toolbarOutline,
            )
                                    }
                                    IconButton(onClick = onClose, modifier = Modifier.testTag("drawer_close_button")) {
                                        OutlinedVectorIcon(Icons.Rounded.Close, "Close drawer", toolbarFill, toolbarOutline)
                                    }
                                }
                            }

                            if (state.settings.drawerSearchBarVisible && state.settings.drawerSearchPosition != "BOTTOM") {
                                DrawerSearchBarBox(
                                    query = query,
                                    onQueryChange = { query = it },
                                    cornerRadiusDp = state.settings.drawerCornerRadiusDp,
                                    onOpenGlobalSearch = onOpenGlobalSearch,
                                )
                            }
                        }

                        // CENTER SECTION: Scrollable App Grid/List + Niagara Wave Index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                        ) {
                            val niagaraLeft = state.settings.niagaraIndexVisible && state.settings.niagaraEdge == "LEFT"
                            val niagaraRight = state.settings.niagaraIndexVisible && state.settings.niagaraEdge != "LEFT"
                            val contentStartPad = if (niagaraLeft) 34.dp else 6.dp
                            val contentEndPad = if (niagaraRight) 34.dp else 6.dp

                            if (visibleApps.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Text(
                                            text = if (query.isNotBlank()) "No apps match \"$query\"" else "No apps match \"$headerTitle\"",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        if (query.isNotBlank()) {
                                            OutlinedButton(
                                                onClick = { onOpenGlobalSearch(query) },
                                                modifier = Modifier.testTag("drawer_search_global_fallback"),
                                            ) {
                                                Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Search Settings & Launcher")
                                            }
                                        }
                                    }
                                }
                            } else if (state.settings.drawerLayoutMode == "LIST") {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(start = contentStartPad, end = contentEndPad),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(bottom = 16.dp),
                                ) {
                                    items(visibleApps, key = { it.app.packageName }) { item ->
                                        val catName = state.categories.find { it.categoryId == item.app.primaryCategoryId }?.name
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .drawerAppItemInteraction(
                                                    app = item.app,
                                                    onClick = {
                                                        onLaunchApp(item.app)
                                                        onClose()
                                                    },
                                                    onLongPress = { onAppLongPress(item.app) },
                                                )
                                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                                .testTag("drawer_app_${item.app.packageName}"),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            AppIconView(
                                                app = item.app,
                                                appDiscovery = appDiscovery,
                                                size = state.settings.drawerIconSizeDp.dp,
                                                iconShapeKey = state.settings.globalIconShape,
                                            )
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    ReadableLauncherText(
                                                        text = item.app.displayName,
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                    if (item.app.favorite) {
                                                        Spacer(Modifier.width(6.dp))
                                                        Icon(
                                                            Icons.Rounded.Star,
                                                            contentDescription = "Favorite",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(16.dp),
                                                        )
                                                    }
                                                }
                                                val subtitle = buildList {
                                                    if (catName != null) add(catName)
                                                    if (item.tags.isNotEmpty()) add(item.tags.joinToString(", ") { it.name })
                                                }.joinToString(" • ")
                                                if (subtitle.isNotEmpty()) {
                                                    Text(
                                                        text = subtitle,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                LazyVerticalGrid(
                                    state = gridState,
                                    columns = GridCells.Fixed(state.settings.drawerColumns.coerceIn(3, 6)),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(start = contentStartPad, end = contentEndPad),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = 16.dp),
                                ) {
                                    items(visibleApps, key = { it.app.packageName }) { item ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .drawerAppItemInteraction(
                                                    app = item.app,
                                                    onClick = {
                                                        onLaunchApp(item.app)
                                                        onClose()
                                                    },
                                                    onLongPress = { onAppLongPress(item.app) },
                                                )
                                                .padding(vertical = 6.dp, horizontal = 2.dp)
                                                .testTag("drawer_app_${item.app.packageName}"),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            AppIconView(
                                                app = item.app,
                                                appDiscovery = appDiscovery,
                                                size = state.settings.drawerIconSizeDp.dp,
                                                iconShapeKey = state.settings.globalIconShape,
                                            )
                                            Spacer(Modifier.height(5.dp))
                                            ReadableLauncherText(
                                                text = item.app.displayName,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontSize = state.settings.drawerLabelSizeSp.sp,
                                                    lineHeight = (state.settings.drawerLabelSizeSp + 2).sp,
                                                ),
                                                maxLines = state.settings.labelMaxLines,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            )
                                        }
                                    }
                                }
                            }

                            if (state.settings.niagaraIndexVisible && visibleApps.isNotEmpty()) {
                                NiagaraWaveAlphabetBar(
                                    activeLetters = activeLetters,
                                    waveEnabled = state.settings.niagaraWaveEnabled,
                                    waveIntensity = state.settings.niagaraWaveIntensity,
                                    baseFontSp = state.settings.niagaraBaseFontSp,
                                    selectedFontSp = state.settings.niagaraSelectedFontSp,
                                    accentColorHex = state.settings.niagaraAccentColorHex,
                                    isLeftEdge = niagaraLeft,
                                    hapticsEnabled = state.settings.niagaraHapticFeedback,
                                    onSelectLetter = { targetLetter ->
                                        val idx = visibleApps.indexOfFirst {
                                            appIndexLetter(it.app.displayName) == targetLetter
                                        }
                                        if (idx >= 0) {
                                            scope.launch {
                                                if (state.settings.drawerLayoutMode == "LIST") {
                                                    listState.scrollToItem(idx)
                                                } else {
                                                    gridState.scrollToItem(idx)
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.align(if (niagaraLeft) Alignment.CenterStart else Alignment.CenterEnd),
                                )
                            }
                        }

                        if (state.settings.drawerSearchBarVisible && state.settings.drawerSearchPosition == "BOTTOM") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                            ) {
                                DrawerSearchBarBox(
                                    query = query,
                                    onQueryChange = { query = it },
                                    cornerRadiusDp = state.settings.drawerCornerRadiusDp,
                                    onOpenGlobalSearch = onOpenGlobalSearch,
                                )
                            }
                        }

                        // MANDATORY BOTTOM SECTION: Persistent Horizontal Category & Tag Bar
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = state.settings.drawerCardOpacity.coerceIn(0.2f, 1f)),
                            tonalElevation = 6.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("drawer_bottom_category_bar"),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                // Expandable Game Subcategories Bar when Games is selected
                                AnimatedVisibility(visible = isGamesContext && gameSubcategories.isNotEmpty()) {
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        item {
                                            val allGamesOpen = selectedTagFilterId == null && selectedFilterId == FILTER_GAMES
                                            CategoryDockChip(
                                                label = "All Games",
                                                selected = allGamesOpen,
                                                accent = Color(gamesCategory.colorHex),
                                                leading = { CategoryBadgeIcon(gamesCategory, size = (gamesCategory.iconSizeDp * 0.75f).coerceIn(12f, 32f).dp, isOpen = allGamesOpen) },
                                                onClick = {
                                                    selectedTagFilterId = null
                                                    selectedFilterId = FILTER_GAMES
                                                },
                                                onLongClick = { onEditCategory(gamesCategory) },
                                                modifier = Modifier.testTag("dock_chip_all_games"),
                                            )
                                        }
                                        items(gameSubcategories, key = { it.categoryId }) { sub ->
                                            val sel = selectedTagFilterId == null && selectedFilterId == sub.categoryId
                                            CategoryDockChip(
                                                label = sub.name,
                                                selected = sel,
                                                accent = Color(sub.colorHex),
                                                leading = { CategoryBadgeIcon(sub, size = (sub.iconSizeDp * 0.75f).coerceIn(12f, 32f).dp, isOpen = sel) },
                                                onClick = {
                                                    selectedTagFilterId = null
                                                    selectedFilterId = sub.categoryId
                                                },
                                                onLongClick = { onEditCategory(sub) },
                                                modifier = Modifier.testTag("dock_chip_${sub.categoryId}"),
                                            )
                                        }
                                    }
                                }

                                // Primary Bottom Category Navigation Row (All + Favorites + Games + Play + Custom + Tags)
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    item {
                                        val allSelected = selectedTagFilterId == null && selectedFilterId == FILTER_ALL
                                        CategoryDockChip(
                                            label = allCategory.name,
                                            selected = allSelected,
                                            accent = Color(allCategory.colorHex),
                                            leading = { CategoryBadgeIcon(allCategory, size = (allCategory.iconSizeDp * 0.75f).coerceIn(12f, 32f).dp, isOpen = allSelected, vectorIconScale = 0.58f) },
                                            onClick = {
                                                selectedTagFilterId = null
                                                selectedFilterId = FILTER_ALL
                                            },
                                            onLongClick = { onEditCategory(allCategory) },
                                            modifier = Modifier.testTag("dock_chip_all"),
                                        )
                                    }
                                    item {
                                        val favSelected = selectedTagFilterId == null && selectedFilterId == FILTER_FAVORITES
                                        CategoryDockChip(
                                            label = favoritesCategory.name,
                                            selected = favSelected,
                                            accent = Color(favoritesCategory.colorHex),
                                            leading = { CategoryBadgeIcon(favoritesCategory, size = (favoritesCategory.iconSizeDp * 0.75f).coerceIn(12f, 32f).dp, isOpen = favSelected) },
                                            onClick = {
                                                selectedTagFilterId = null
                                                selectedFilterId = FILTER_FAVORITES
                                            },
                                            onLongClick = { onEditCategory(favoritesCategory) },
                                            modifier = Modifier.testTag("dock_chip_favorites"),
                                        )
                                    }
                                    item {
                                        CategoryDockChip(
                                            label = gamesCategory.name,
                                            selected = isGamesContext,
                                            accent = Color(gamesCategory.colorHex),
                                            leading = { CategoryBadgeIcon(gamesCategory, size = (gamesCategory.iconSizeDp * 0.75f).coerceIn(12f, 32f).dp, isOpen = isGamesContext) },
                                            onClick = {
                                                selectedTagFilterId = null
                                                selectedFilterId = FILTER_GAMES
                                            },
                                            onLongClick = { onEditCategory(gamesCategory) },
                                            modifier = Modifier.testTag("dock_chip_games"),
                                        )
                                    }
                                    items(playAndCustomCategories, key = { it.categoryId }) { cat ->
                                        val selected = selectedTagFilterId == null && selectedFilterId == cat.categoryId
                                        CategoryDockChip(
                                            label = cat.name,
                                            selected = selected,
                                            accent = Color(cat.colorHex),
                                            leading = { CategoryBadgeIcon(cat, size = (cat.iconSizeDp * 0.75f).coerceIn(12f, 32f).dp, isOpen = selected) },
                                            onClick = {
                                                selectedTagFilterId = null
                                                selectedFilterId = cat.categoryId
                                            },
                                            onLongClick = { onEditCategory(cat) },
                                            modifier = Modifier.testTag("dock_chip_${cat.categoryId}"),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCustomizeDialog) {
            DrawerCustomizeQuickDialog(
                settings = state.settings,
                onUpdateSettings = onUpdateSettings,
                onDismiss = { showCustomizeDialog = false },
            )
        }
    }
}

@Composable
private fun DrawerSearchBarBox(
    query: String,
    onQueryChange: (String) -> Unit,
    cornerRadiusDp: Float,
    onOpenGlobalSearch: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("drawer_search_input"),
            placeholder = { Text("Search apps, categories, tags...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(cornerRadiusDp.coerceIn(8f, 28f).dp),
        )
        if (query.isNotBlank()) {
            TextButton(
                onClick = { onOpenGlobalSearch(query) },
                modifier = Modifier.testTag("drawer_search_global_chip"),
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Search everywhere (Settings, Pages, Tags) for \"$query\"")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryDockChip(
    label: String,
    selected: Boolean,
    accent: Color,
    leading: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) accent.copy(alpha = 0.24f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, if (selected) accent else Color.Transparent),
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            leading?.invoke()
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.58f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun DrawerCustomizeQuickDialog(
    settings: LauncherSettings,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("All-Apps Sidebar Settings") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .background(Color.Transparent),
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Text("Opening Side", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("LEFT", "RIGHT", "BOTTOM").forEach { edge ->
                                FilterChip(
                                    selected = settings.allAppsSidebarEdge == edge,
                                    onClick = { onUpdateSettings { it.copy(allAppsSidebarEdge = edge) } },
                                    label = { Text(edge) },
                                )
                            }
                        }
                    }
                    if (settings.allAppsSidebarEdge != "BOTTOM") {
                        item {
                            Text("Sidebar Width: ${(settings.allAppsSidebarWidthFraction * 100).roundToInt()}%")
                            Slider(
                                value = settings.allAppsSidebarWidthFraction,
                                onValueChange = { v -> onUpdateSettings { it.copy(allAppsSidebarWidthFraction = v) } },
                                valueRange = 0.65f..1f,
                            )
                        }
                    }
                    item {
                        Text("Layout & Sort", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("GRID", "LIST").forEach { mode ->
                                FilterChip(
                                    selected = settings.drawerLayoutMode == mode,
                                    onClick = { onUpdateSettings { it.copy(drawerLayoutMode = mode) } },
                                    label = { Text(mode) },
                                )
                            }
                        }
                        if (settings.drawerLayoutMode == "GRID") {
                            Text("Grid Columns: ${settings.drawerColumns}")
                            Slider(
                                value = settings.drawerColumns.toFloat(),
                                onValueChange = { v -> onUpdateSettings { it.copy(drawerColumns = v.roundToInt().coerceIn(3, 6)) } },
                                valueRange = 3f..6f,
                            )
                        }
                        Text("App Icon Size: ${settings.drawerIconSizeDp}dp")
                        Slider(
                            value = settings.drawerIconSizeDp.toFloat(),
                            onValueChange = { v -> onUpdateSettings { it.copy(drawerIconSizeDp = v.roundToInt().coerceIn(36, 76)) } },
                            valueRange = 36f..76f,
                        )
                        Text("App Label Size: ${settings.drawerLabelSizeSp}sp")
                        Slider(
                            value = settings.drawerLabelSizeSp.toFloat(),
                            onValueChange = { v -> onUpdateSettings { it.copy(drawerLabelSizeSp = v.roundToInt().coerceIn(9, 16)) } },
                            valueRange = 9f..16f,
                        )
                    }
                    item {
                        Text("Background Style", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("SOLID", "GRADIENT", "IMAGE").forEach { bg ->
                                FilterChip(
                                    selected = settings.allAppsBgType == bg,
                                    onClick = { onUpdateSettings { it.copy(allAppsBgType = bg) } },
                                    label = { Text(bg) },
                                )
                            }
                        }
                        Text("Background Start Color", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = settings.allAppsBgColorHex,
                            colors = StudioDarkSurfaceSwatches,
                            onSelect = { c -> onUpdateSettings { it.copy(allAppsBgColorHex = c) } },
                            tagPrefix = "drawer_quick_bg1",
                        )
                        if (settings.allAppsBgType == "GRADIENT") {
                            Text("Background End Color", style = MaterialTheme.typography.labelLarge)
                            ColorSwatchRow(
                                selectedColorHex = settings.allAppsBgGradientEndHex,
                                colors = StudioDarkSurfaceSwatches,
                                onSelect = { c -> onUpdateSettings { it.copy(allAppsBgGradientEndHex = c) } },
                                tagPrefix = "drawer_quick_bg2",
                            )
                        }
                        Text("Card & Dock Opacity: ${(settings.drawerCardOpacity * 100).roundToInt()}%")
                        Slider(
                            value = settings.drawerCardOpacity,
                            onValueChange = { v -> onUpdateSettings { it.copy(drawerCardOpacity = v) } },
                            valueRange = 0.25f..1.0f,
                        )
                        Text("Drawer Corner Radius: ${settings.drawerCornerRadiusDp.roundToInt()}dp")
                        Slider(
                            value = settings.drawerCornerRadiusDp,
                            onValueChange = { v -> onUpdateSettings { it.copy(drawerCornerRadiusDp = v) } },
                            valueRange = 0f..40f,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Show Search Bar")
                            Switch(
                                checked = settings.drawerSearchBarVisible,
                                onCheckedChange = { v -> onUpdateSettings { it.copy(drawerSearchBarVisible = v) } },
                            )
                        }
                        if (settings.drawerSearchBarVisible) {
                            Text("Search Bar Position", style = MaterialTheme.typography.labelLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("TOP", "BOTTOM").forEach { pos ->
                                    FilterChip(
                                        selected = settings.drawerSearchPosition == pos,
                                        onClick = { onUpdateSettings { it.copy(drawerSearchPosition = pos) } },
                                        label = { Text(pos) },
                                        modifier = Modifier.testTag("drawer_quick_search_pos_$pos"),
                                    )
                                }
                            }
                        }
                    }
                    item {
                        HorizontalDivider()
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Niagara A-Z Index Bar")
                            Switch(
                                checked = settings.niagaraIndexVisible,
                                onCheckedChange = { v -> onUpdateSettings { it.copy(niagaraIndexVisible = v) } },
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Niagara Wave Magnification")
                            Switch(
                                checked = settings.niagaraWaveEnabled,
                                onCheckedChange = { v -> onUpdateSettings { it.copy(niagaraWaveEnabled = v) } },
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onUpdateSettings { it.copy(niagaraEdge = "LEFT") } }) {
                                Text("Index Left")
                            }
                            OutlinedButton(onClick = { onUpdateSettings { it.copy(niagaraEdge = "RIGHT") } }) {
                                Text("Index Right")
                            }
                        }
                        Text("Niagara Wave Intensity: ${"%.1f".format(settings.niagaraWaveIntensity)}x")
                        Slider(
                            value = settings.niagaraWaveIntensity,
                            onValueChange = { v -> onUpdateSettings { it.copy(niagaraWaveIntensity = v) } },
                            valueRange = 0.2f..1.8f,
                        )
                        Text("Niagara Accent Color", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = settings.niagaraAccentColorHex,
                            colors = com.example.ui.theme.TagPresetColors,
                            onSelect = { c -> onUpdateSettings { it.copy(niagaraAccentColorHex = c) } },
                            tagPrefix = "drawer_quick_niagara_color",
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
    )
}
