package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewSidebar
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.db.AppEntity
import com.example.data.system.AppDiscoveryManager
import com.example.ui.screens.LauncherSettingsCatalog
import com.example.ui.state.LauncherRoute
import com.example.ui.state.LauncherUiState

private data class GlobalQuickAction(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val keywords: List<String>,
    val onTrigger: () -> Unit,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UniversalSearchOverlay(
    visible: Boolean,
    state: LauncherUiState,
    appDiscovery: AppDiscoveryManager,
    onClose: () -> Unit,
    onLaunchApp: (AppEntity) -> Unit,
    onOpenTagSidebar: (Long) -> Unit,
    onOpenCategorySidebar: (Long) -> Unit,
    onOpenRuleSidebar: (Long) -> Unit,
    onOpenFolder: (Long) -> Unit,
    onJumpToPage: (Int) -> Unit,
    onAddHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
    onOpenConfiguredSidebar: (Long) -> Unit = {},
    onOpenWindow: (Long) -> Unit = {},
    onOpenSettingsSearch: (String) -> Unit = {},
    onNavigate: (LauncherRoute) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onToggleEditMode: () -> Unit = {},
    onConsumePendingQuery: () -> Unit = {},
) {
    if (visible) {
        BackHandler { onClose() }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { -it / 4 } + fadeIn(),
        exit = slideOutVertically { -it / 4 } + fadeOut(),
    ) {
        var query by remember { mutableStateOf(state.pendingGlobalSearchQuery) }
        var activeScope by remember { mutableStateOf("ALL") }

        LaunchedEffect(visible, state.pendingGlobalSearchQuery) {
            if (visible && state.pendingGlobalSearchQuery.isNotBlank()) {
                query = state.pendingGlobalSearchQuery
                onConsumePendingQuery()
            } else if (!visible) {
                query = ""
                activeScope = "ALL"
            }
        }

        val q = query.trim().lowercase()
        val tokens = remember(q) { LauncherSettingsCatalog.tokenize(q) }

        val recentSearches = remember(state.settings.recentSearchesCsv) {
            state.settings.recentSearchesCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        }

        val quickActions = remember(onClose, onNavigate, onOpenDrawer, onToggleEditMode) {
            listOf(
                GlobalQuickAction(
                    id = "action_settings",
                    title = "Launcher Settings",
                    subtitle = "Open full launcher settings & search preferences",
                    icon = Icons.Rounded.Settings,
                    keywords = listOf("settings", "preferences", "config", "options", "customize"),
                    onTrigger = {
                        onClose()
                        onNavigate(LauncherRoute.Settings)
                    },
                ),
                GlobalQuickAction(
                    id = "action_drawer",
                    title = "All-Apps Drawer",
                    subtitle = "Browse installed apps with Niagara wave A-Z index",
                    icon = Icons.Rounded.Apps,
                    keywords = listOf("apps", "drawer", "all apps", "alphabet"),
                    onTrigger = {
                        onClose()
                        onOpenDrawer()
                    },
                ),
                GlobalQuickAction(
                    id = "action_tags",
                    title = "Tags & Categories Hub",
                    subtitle = "Organize tags, Play categories, and smart collections",
                    icon = Icons.Rounded.LocalOffer,
                    keywords = listOf("tags", "categories", "rules", "organize"),
                    onTrigger = {
                        onClose()
                        onNavigate(LauncherRoute.Tags)
                    },
                ),
                GlobalQuickAction(
                    id = "action_app_manager",
                    title = "App Icons & Visibility Manager",
                    subtitle = "Rename apps, hide apps, or assign custom icons",
                    icon = Icons.Rounded.Tune,
                    keywords = listOf("app manager", "rename", "hide", "custom icon", "icons"),
                    onTrigger = {
                        onClose()
                        onNavigate(LauncherRoute.Apps)
                    },
                ),
                GlobalQuickAction(
                    id = "action_sidebars",
                    title = "Sidebars & Floating Windows",
                    subtitle = "Manage edge panels and resizable widget windows",
                    icon = Icons.AutoMirrored.Rounded.ViewSidebar,
                    keywords = listOf("sidebars", "floating windows", "panels", "edge"),
                    onTrigger = {
                        onClose()
                        onNavigate(LauncherRoute.Sidebars)
                    },
                ),
                GlobalQuickAction(
                    id = "action_edit_mode",
                    title = "Toggle Home Edit Studio",
                    subtitle = "Add elements, widgets, pages, and customize layout",
                    icon = Icons.Rounded.DashboardCustomize,
                    keywords = listOf("edit", "studio", "widgets", "layout", "customize home"),
                    onTrigger = {
                        onClose()
                        onToggleEditMode()
                    },
                ),
            )
        }

        val matchingApps = remember(state.appsWithTags, q, tokens, activeScope) {
            if (activeScope !in listOf("ALL", "APPS")) return@remember emptyList()
            val base = state.appsWithTags.filter { it.app.isInstalled && !it.app.hidden }
            if (q.isEmpty()) base.sortedByDescending { it.app.launchCount }.take(8)
            else base.filter { item ->
                val hay = buildString {
                    append(item.app.displayName.lowercase()).append(' ')
                    append(item.app.appName.lowercase()).append(' ')
                    append(item.app.packageName.lowercase()).append(' ')
                    item.tags.forEach { append(it.name.lowercase()).append(' ') }
                }
                tokens.all { hay.contains(it) }
            }.sortedBy { !it.app.displayName.lowercase().startsWith(q) }
                .take(18)
        }

        val matchingSettings = remember(q, activeScope) {
            if (activeScope !in listOf("ALL", "SETTINGS")) return@remember emptyList()
            if (q.isEmpty()) {
                if (activeScope == "SETTINGS") LauncherSettingsCatalog.allEntries.take(14) else emptyList()
            } else {
                LauncherSettingsCatalog.searchSettingsEntries(q).take(12)
            }
        }

        val matchingQuickActions = remember(quickActions, tokens, q, activeScope) {
            if (activeScope !in listOf("ALL", "SETTINGS") || q.isEmpty()) return@remember emptyList()
            quickActions.filter { act ->
                val hay = (act.title + " " + act.subtitle + " " + act.keywords.joinToString(" ")).lowercase()
                tokens.all { hay.contains(it) }
            }
        }

        val matchingCategories = remember(state.categories, q, tokens, activeScope) {
            if (activeScope !in listOf("ALL", "TAGS")) return@remember emptyList()
            if (q.isEmpty()) state.categories.filter { it.categoryType == "GAMES_PARENT" || it.categoryId in listOf(7L, 20L, 24L, 25L, 27L, 29L) }
            else state.categories.filter { cat ->
                val name = cat.name.lowercase()
                tokens.all { name.contains(it) }
            }.take(8)
        }

        val matchingTags = remember(state.allTags, q, tokens, activeScope) {
            if (activeScope !in listOf("ALL", "TAGS")) return@remember emptyList()
            if (q.isEmpty()) state.allTags.take(8)
            else state.allTags.filter { tag ->
                val hay = "${tag.name} ${tag.description}".lowercase()
                tokens.all { hay.contains(it) }
            }
        }

        val matchingRules = remember(state.savedRules, q, tokens, activeScope) {
            if (activeScope !in listOf("ALL", "TAGS")) return@remember emptyList()
            if (q.isEmpty()) if (activeScope == "TAGS") state.savedRules.take(8) else emptyList()
            else state.savedRules.filter { rule ->
                val hay = "${rule.name} ${rule.matchMode}".lowercase()
                tokens.all { hay.contains(it) }
            }
        }

        val matchingWindows = remember(state.windows, q, tokens, activeScope) {
            if (activeScope !in listOf("ALL", "LAYOUT")) return@remember emptyList()
            if (q.isEmpty()) if (activeScope == "LAYOUT") state.windows else emptyList()
            else state.windows.filter { win ->
                val hay = "${win.title} window floating".lowercase()
                tokens.all { hay.contains(it) }
            }
        }

        val matchingSidebars = remember(state.sidebars, q, tokens, activeScope) {
            if (activeScope !in listOf("ALL", "LAYOUT")) return@remember emptyList()
            if (q.isEmpty()) if (activeScope == "LAYOUT") state.sidebars else emptyList()
            else state.sidebars.filter { sb ->
                val hay = "${sb.title} ${sb.side} sidebar".lowercase()
                tokens.all { hay.contains(it) }
            }
        }

        val matchingFolders = remember(state.homeElements, q, tokens, activeScope) {
            if (activeScope !in listOf("ALL", "LAYOUT")) return@remember emptyList()
            val folders = state.homeElements.filter { it.type == "FOLDER" }
            if (q.isEmpty()) if (activeScope == "LAYOUT") folders.take(8) else emptyList()
            else folders.filter { fld ->
                val hay = "${fld.label} folder".lowercase()
                tokens.all { hay.contains(it) }
            }
        }

        val matchingWidgetsAndElements = remember(state.homeElements, q, tokens, activeScope) {
            if (activeScope !in listOf("ALL", "LAYOUT") || q.isEmpty()) return@remember emptyList()
            state.homeElements.filter { el ->
                if (el.type == "FOLDER") return@filter false
                val readableType = el.type.lowercase().replace('_', ' ')
                val hay = "${el.label.lowercase()} $readableType ${el.targetPackageName.orEmpty().lowercase()}"
                tokens.all { hay.contains(it) }
            }.take(10)
        }

        val matchingPages = remember(state.homePages, q, tokens, activeScope) {
            if (activeScope !in listOf("ALL", "LAYOUT")) return@remember emptyList()
            if (q.isEmpty()) {
                if (activeScope == "LAYOUT") state.homePages.mapIndexed { idx, p -> idx to p } else emptyList()
            } else {
                state.homePages.mapIndexedNotNull { idx, p ->
                    val hay = "${p.title} page".lowercase()
                    if (tokens.all { hay.contains(it) }) idx to p else null
                }
            }
        }

        val hasAnyResults = matchingApps.isNotEmpty() ||
            matchingSettings.isNotEmpty() ||
            matchingQuickActions.isNotEmpty() ||
            matchingCategories.isNotEmpty() ||
            matchingTags.isNotEmpty() ||
            matchingRules.isNotEmpty() ||
            matchingWindows.isNotEmpty() ||
            matchingSidebars.isNotEmpty() ||
            matchingFolders.isNotEmpty() ||
            matchingWidgetsAndElements.isNotEmpty() ||
            matchingPages.isNotEmpty()

        Surface(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
            modifier = Modifier
                .fillMaxSize()
                .testTag("universal_search_overlay"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Global Search: apps, settings, tags, pages...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(
                                    onClick = { query = "" },
                                    modifier = Modifier.testTag("universal_search_clear"),
                                ) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear query")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (q.isNotEmpty()) {
                                    onAddHistory(query)
                                    when {
                                        matchingApps.isNotEmpty() -> {
                                            onLaunchApp(matchingApps.first().app)
                                            onClose()
                                        }
                                        matchingSettings.isNotEmpty() -> {
                                            onOpenSettingsSearch(matchingSettings.first().title)
                                        }
                                        matchingQuickActions.isNotEmpty() -> {
                                            matchingQuickActions.first().onTrigger()
                                        }
                                        matchingPages.isNotEmpty() -> {
                                            onJumpToPage(matchingPages.first().first)
                                            onClose()
                                        }
                                    }
                                }
                            },
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f).testTag("universal_search_input"),
                    )
                    IconButton(onClick = onClose, modifier = Modifier.testTag("universal_search_close")) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close search")
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val scopes = listOf(
                        "ALL" to "All",
                        "APPS" to "Apps",
                        "SETTINGS" to "Settings",
                        "TAGS" to "Tags",
                        "LAYOUT" to "Pages & Panels",
                    )
                    items(scopes, key = { it.first }) { (scopeKey, label) ->
                        FilterChip(
                            selected = activeScope == scopeKey,
                            onClick = { activeScope = scopeKey },
                            label = { Text(label) },
                            modifier = Modifier.testTag("global_search_filter_$scopeKey"),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).testTag("universal_search_results"),
                ) {
                    if (q.isEmpty() && activeScope == "ALL" && recentSearches.isNotEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Recent Searches", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                TextButton(onClick = onClearHistory) { Text("Clear") }
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                recentSearches.forEach { prev ->
                                    AssistChip(
                                        onClick = { query = prev },
                                        leadingIcon = { Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                        label = { Text(prev) },
                                    )
                                }
                            }
                        }
                    }

                    if (matchingApps.isNotEmpty()) {
                        item {
                            Text(
                                text = if (q.isEmpty()) "Frequent Apps" else "Applications (${matchingApps.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        items(matchingApps, key = { "app_${it.app.packageName}" }) { item ->
                            val catName = state.categories.find { it.categoryId == item.app.primaryCategoryId }?.name ?: ""
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (q.isNotEmpty()) onAddHistory(query)
                                    onLaunchApp(item.app)
                                    onClose()
                                },
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    AppIconView(item.app, appDiscovery, 42.dp, state.settings.globalIconShape)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.app.displayName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        val sub = buildList {
                                            if (catName.isNotBlank()) add(catName)
                                            if (item.tags.isNotEmpty()) add(item.tags.joinToString(", ") { "#${it.name}" })
                                        }.joinToString(" • ")
                                        if (sub.isNotBlank()) {
                                            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (matchingSettings.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (q.isEmpty()) "Launcher Settings" else "Settings (${matchingSettings.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        items(matchingSettings, key = { "setting_${it.id}" }) { entry ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (q.isNotEmpty()) onAddHistory(query)
                                        onOpenSettingsSearch(entry.title)
                                    }
                                    .testTag("global_search_setting_${entry.id}"),
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(Icons.Rounded.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(entry.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            "${entry.sectionTitle} • ${entry.subtitle}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (matchingQuickActions.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text("Quick Actions", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        items(matchingQuickActions, key = { it.id }) { action ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (q.isNotEmpty()) onAddHistory(query)
                                        action.onTrigger()
                                    }
                                    .testTag("global_search_${action.id}"),
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(action.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(action.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(action.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }

                    if (matchingCategories.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text("Google Play Categories", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        items(matchingCategories, key = { "cat_${it.categoryId}" }) { cat ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (q.isNotEmpty()) onAddHistory(query)
                                    onOpenCategorySidebar(cat.categoryId)
                                },
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    CategoryBadgeIcon(cat, size = 34.dp)
                                    Text(
                                        text = if (cat.categoryType == "GAME_SUB") "Games • ${cat.name}" else cat.name,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(cat.colorHex),
                                    )
                                }
                            }
                        }
                    }

                    if (matchingTags.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            Text("Tags", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        items(matchingTags, key = { "tag_${it.tagId}" }) { tag ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (q.isNotEmpty()) onAddHistory(query)
                                    onOpenTagSidebar(tag.tagId)
                                },
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    TagBadgeIcon(tag.iconKey, tag.customImagePath, tag.colorHex, 32.dp)
                                    Text(tag.name, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (matchingRules.isNotEmpty()) {
                        item {
                            Text("Dynamic Tag Collections", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        items(matchingRules, key = { "rule_${it.ruleId}" }) { rule ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (q.isNotEmpty()) onAddHistory(query)
                                    onOpenRuleSidebar(rule.ruleId)
                                },
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color(rule.colorHex))
                                    Text("${rule.name} (${rule.matchMode})", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (matchingWindows.isNotEmpty()) {
                        item {
                            Text("Floating Windows", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        items(matchingWindows, key = { "win_${it.windowId}" }) { win ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (q.isNotEmpty()) onAddHistory(query)
                                        onOpenWindow(win.windowId)
                                    }
                                    .testTag("global_search_window_${win.windowId}"),
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Rounded.OpenInNew, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(win.title, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (matchingSidebars.isNotEmpty()) {
                        item {
                            Text("Sidebars", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        items(matchingSidebars, key = { "sb_${it.sidebarId}" }) { sb ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (q.isNotEmpty()) onAddHistory(query)
                                        onOpenConfiguredSidebar(sb.sidebarId)
                                    }
                                    .testTag("global_search_sidebar_${sb.sidebarId}"),
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.AutoMirrored.Rounded.ViewSidebar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Column {
                                        Text(sb.title, fontWeight = FontWeight.SemiBold)
                                        Text("${sb.side} edge sidebar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    if (matchingFolders.isNotEmpty()) {
                        item {
                            Text("Folders", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        items(matchingFolders, key = { "fld_${it.elementId}" }) { fld ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (q.isNotEmpty()) onAddHistory(query)
                                    onClose()
                                    onOpenFolder(fld.elementId)
                                },
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(fld.label.ifBlank { "Folder" }, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (matchingWidgetsAndElements.isNotEmpty()) {
                        item {
                            Text("Widgets & Home Elements", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        items(matchingWidgetsAndElements, key = { "el_${it.elementId}" }) { el ->
                            val ownerLabel = if (el.ownerType == "WINDOW") {
                                state.windows.find { it.windowId == el.ownerWindowId }?.title ?: "Floating Window"
                            } else {
                                val pIdx = state.homePages.indexOfFirst { it.pageId == el.pageId }
                                if (pIdx >= 0) state.homePages[pIdx].title else "Home Page"
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (q.isNotEmpty()) onAddHistory(query)
                                        if (el.ownerType == "WINDOW" && el.ownerWindowId != null) {
                                            onOpenWindow(el.ownerWindowId)
                                        } else {
                                            val pIdx = state.homePages.indexOfFirst { it.pageId == el.pageId }
                                            if (pIdx >= 0) onJumpToPage(pIdx)
                                            onClose()
                                        }
                                    }
                                    .testTag("global_search_element_${el.elementId}"),
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Rounded.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            el.label.ifBlank { el.type.replace('_', ' ') },
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            "$ownerLabel • ${el.type.lowercase().replace('_', ' ')}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (matchingPages.isNotEmpty()) {
                        item {
                            Text("Home Pages", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        items(matchingPages, key = { "pg_${it.second.pageId}" }) { (idx, page) ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (q.isNotEmpty()) onAddHistory(query)
                                    onJumpToPage(idx)
                                    onClose()
                                },
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Rounded.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(page.title, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (!hasAnyResults && q.isNotEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .testTag("universal_search_empty"),
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                    Text("No results for \"$query\"", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Try searching an app name, setting, tag, category, sidebar, window, or page title.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    OutlinedButton(onClick = {
                                        query = ""
                                        activeScope = "ALL"
                                    }) {
                                        Text("Reset Search")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
