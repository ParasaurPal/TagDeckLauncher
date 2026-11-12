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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.db.AppEntity
import com.example.data.system.AppDiscoveryManager
import com.example.ui.state.LauncherUiState

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
) {
    if (visible) {
        BackHandler { onClose() }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { -it / 4 } + fadeIn(),
        exit = slideOutVertically { -it / 4 } + fadeOut(),
    ) {
        var query by remember { mutableStateOf("") }
        val q = query.trim().lowercase()

        val recentSearches = remember(state.settings.recentSearchesCsv) {
            state.settings.recentSearchesCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        }

        val matchingApps = remember(state.appsWithTags, q) {
            val base = state.appsWithTags.filter { it.app.isInstalled && !it.app.hidden }
            if (q.isEmpty()) base.sortedByDescending { it.app.launchCount }.take(8)
            else base.filter {
                it.app.displayName.lowercase().contains(q) ||
                    it.app.appName.lowercase().contains(q) ||
                    it.app.packageName.lowercase().contains(q) ||
                    it.tags.any { t -> t.name.lowercase().contains(q) }
            }.sortedBy { !it.app.displayName.lowercase().startsWith(q) }
                .take(18)
        }

        val matchingCategories = remember(state.categories, q) {
            if (q.isEmpty()) state.categories.filter { it.categoryType == "GAMES_PARENT" || it.categoryId in listOf(7L, 20L, 24L, 25L, 27L, 29L) }
            else state.categories.filter { it.name.lowercase().contains(q) }.take(8)
        }

        val matchingTags = remember(state.allTags, q) {
            if (q.isEmpty()) state.allTags.take(8)
            else state.allTags.filter { it.name.lowercase().contains(q) || it.description.lowercase().contains(q) }
        }

        val matchingRules = remember(state.savedRules, q) {
            if (q.isEmpty()) emptyList()
            else state.savedRules.filter { it.name.lowercase().contains(q) }
        }

        val matchingFolders = remember(state.homeElements, q) {
            val folders = state.homeElements.filter { it.type == "FOLDER" }
            if (q.isEmpty()) emptyList()
            else folders.filter { it.label.lowercase().contains(q) }
        }

        val matchingPages = remember(state.homePages, q) {
            if (q.isEmpty()) emptyList()
            else state.homePages.mapIndexedNotNull { idx, p ->
                if (p.title.lowercase().contains(q)) idx to p else null
            }
        }

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
                        placeholder = { Text("Universal Search: apps, tags, categories, pages...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Rounded.Clear, "Clear query")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f).testTag("universal_search_input"),
                    )
                    IconButton(onClick = onClose, modifier = Modifier.testTag("universal_search_close")) {
                        Icon(Icons.Rounded.Close, "Close search")
                    }
                }

                Spacer(Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    if (q.isEmpty() && recentSearches.isNotEmpty()) {
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
                                        leadingIcon = { Icon(Icons.Rounded.History, null, Modifier.size(16.dp)) },
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
                        items(matchingApps, key = { it.app.packageName }) { item ->
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
                                modifier = Modifier.fillMaxWidth().clickable { onOpenRuleSidebar(rule.ruleId) },
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Rounded.AutoAwesome, null, tint = Color(rule.colorHex))
                                    Text("${rule.name} (${rule.matchMode})", fontWeight = FontWeight.SemiBold)
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
                                    onClose()
                                    onOpenFolder(fld.elementId)
                                },
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Rounded.FolderOpen, null, tint = MaterialTheme.colorScheme.primary)
                                    Text(fld.label.ifBlank { "Folder" }, fontWeight = FontWeight.SemiBold)
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
                                    onJumpToPage(idx)
                                    onClose()
                                },
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Icon(Icons.Rounded.Layers, null, tint = MaterialTheme.colorScheme.primary)
                                    Text(page.title, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
