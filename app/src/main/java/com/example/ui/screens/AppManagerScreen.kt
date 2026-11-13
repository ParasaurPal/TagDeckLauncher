package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.data.db.AppWithTags
import com.example.data.system.AppDiscoveryManager
import com.example.ui.components.AppActionAndCustomizeDialog
import com.example.ui.components.AppCategoryOverrideDialog
import com.example.ui.components.AppIconView
import com.example.ui.components.CategoryBadgeIcon
import com.example.ui.components.TagBadgeIcon
import com.example.ui.state.LauncherUiState

@Composable
fun AppManagerScreen(
    state: LauncherUiState,
    appDiscovery: AppDiscoveryManager,
    onBack: () -> Unit,
    onLaunchApp: (AppEntity) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onToggleHidden: (String, Boolean) -> Unit,
    onSaveCustomName: (String, String?) -> Unit,
    onSaveCustomIcon: (String, String?, Float, Float, Float, Float) -> Unit,
    onSaveTagsForApp: (String, List<Long>) -> Unit,
    onBulkAssignTags: (List<String>, List<Long>) -> Unit,
    onSaveCategoriesForApp: (String, Long, List<Long>, List<Long>) -> Unit,
    onRestoreAutomaticCategory: (AppEntity) -> Unit,
    onBulkSetCategory: (List<String>, Long) -> Unit,
    onMoveAppOrder: (String, Int) -> Unit,
    onChangeSortOrder: (String) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    onEditLineIcon: (AppEntity) -> Unit,
    onToggleAppLock: (String, Boolean) -> Unit = { _, _ -> },
    onImportImage: suspend (Uri) -> String?,
) {
    BackHandler { onBack() }

    var query by remember { mutableStateOf("") }
    var showHidden by remember { mutableStateOf(true) }
    var categoryFilterMode by remember { mutableStateOf("ALL") } // ALL, MANUAL, UNCERTAIN, GAMES
    var bulkMode by remember { mutableStateOf(false) }
    var selectedPackages by remember { mutableStateOf(emptySet<String>()) }
    var showBulkTagPicker by remember { mutableStateOf(false) }
    var showBulkCategoryPicker by remember { mutableStateOf(false) }
    var customizingApp by remember { mutableStateOf<AppWithTags?>(null) }
    var overrideCategoryApp by remember { mutableStateOf<AppWithTags?>(null) }

    val categoryLookup = remember(state.categories) { state.categories.associateBy { it.categoryId } }

    val apps = remember(state.appsWithTags, query, showHidden, categoryFilterMode, state.settings.drawerSortOrder) {
        val q = query.trim().lowercase()
        val filtered = state.appsWithTags.filter { item ->
            val hiddenOk = showHidden || !item.app.hidden
            val catOk = when (categoryFilterMode) {
                "MANUAL" -> item.app.manualCategoryOverride
                "UNCERTAIN" -> item.app.categoryConfidence == "UNCERTAIN"
                "GAMES" -> item.app.isGame
                else -> true
            }
            val queryOk = q.isEmpty() ||
                item.app.displayName.lowercase().contains(q) ||
                item.app.packageName.lowercase().contains(q) ||
                (categoryLookup[item.app.primaryCategoryId]?.name?.lowercase()?.contains(q) == true)
            hiddenOk && catOk && queryOk
        }
        when (state.settings.drawerSortOrder) {
            "MANUAL" -> filtered.sortedBy { it.app.manualOrder }
            "MOST_USED" -> filtered.sortedByDescending { it.app.launchCount }
            "RECENTLY_INSTALLED" -> filtered.sortedByDescending { it.app.installTimeMillis }
            else -> filtered.sortedBy { it.app.displayName.lowercase() }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = if (bulkMode) "${selectedPackages.size} Selected" else "Applications (${apps.size})",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            bulkMode = !bulkMode
                            if (!bulkMode) selectedPackages = emptySet()
                        },
                        modifier = Modifier.testTag("toggle_bulk_select_button"),
                    ) {
                        Icon(
                            if (bulkMode) Icons.Rounded.Close else Icons.Rounded.SelectAll,
                            contentDescription = "Bulk selection",
                        )
                    }
                }

                if (bulkMode && selectedPackages.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Button(
                            onClick = { showBulkTagPicker = true },
                            modifier = Modifier.weight(1f).testTag("bulk_tag_selected_button"),
                        ) {
                            Icon(Icons.Rounded.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Tag (${selectedPackages.size})")
                        }
                        OutlinedButton(
                            onClick = { showBulkCategoryPicker = true },
                            modifier = Modifier.weight(1f).testTag("bulk_category_selected_button"),
                        ) {
                            Icon(Icons.Rounded.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Category")
                        }
                        OutlinedButton(onClick = { selectedPackages = apps.map { it.app.packageName }.toSet() }) {
                            Text("All")
                        }
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search apps or Play categories...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("ALPHABETICAL" to "A-Z", "MANUAL" to "Manual", "MOST_USED" to "Most Used").forEach { (key, label) ->
                        FilterChip(
                            selected = state.settings.drawerSortOrder == key,
                            onClick = { onChangeSortOrder(key) },
                            label = { Text(label) },
                        )
                    }
                    FilterChip(
                        selected = showHidden,
                        onClick = { showHidden = !showHidden },
                        label = { Text("Hidden") },
                    )
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        "ALL" to "All Categories",
                        "GAMES" to "Games",
                        "MANUAL" to "Manual Overrides",
                        "UNCERTAIN" to "Uncertain Auto",
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = categoryFilterMode == key,
                            onClick = { categoryFilterMode = key },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(apps, key = { it.app.packageName }) { item ->
                val app = item.app
                val selected = app.packageName in selectedPackages
                val primaryCategory = categoryLookup[app.primaryCategoryId]
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (bulkMode) {
                                selectedPackages = if (selected) selectedPackages - app.packageName else selectedPackages + app.packageName
                            } else {
                                onLaunchApp(app)
                            }
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (bulkMode) {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = {
                                    selectedPackages = if (it) selectedPackages + app.packageName else selectedPackages - app.packageName
                                },
                            )
                        }
                        AppIconView(app, appDiscovery, 46.dp, state.settings.globalIconShape)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                if (primaryCategory != null) {
                                    CategoryBadgeIcon(primaryCategory, size = 16.dp)
                                    Text(
                                        text = buildString {
                                            append(primaryCategory.name)
                                            append(if (app.manualCategoryOverride) " • Manual" else " • ${app.categoryConfidence.lowercase()}")
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (app.manualCategoryOverride) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            if (item.tags.isNotEmpty()) {
                                Text(
                                    text = item.tags.joinToString(" • ") { "#${it.name}" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        if (!bulkMode) {
                            if (state.settings.drawerSortOrder == "MANUAL") {
                                IconButton(onClick = { onMoveAppOrder(app.packageName, -1) }, modifier = Modifier.size(30.dp)) {
                                    Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move up", modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { onMoveAppOrder(app.packageName, 1) }, modifier = Modifier.size(30.dp)) {
                                    Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move down", modifier = Modifier.size(16.dp))
                                }
                            }
                            IconButton(onClick = { overrideCategoryApp = item }) {
                                Icon(Icons.Rounded.Category, contentDescription = "Category override")
                            }
                            IconButton(onClick = { onToggleFavorite(app.packageName, !app.favorite) }) {
                                Icon(
                                    if (app.favorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (app.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { onToggleHidden(app.packageName, !app.hidden) }) {
                                Icon(
                                    if (app.hidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Hide/Show",
                                )
                            }
                            IconButton(onClick = { customizingApp = item }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Customize app")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBulkTagPicker) {
        var chosenTagIds by remember { mutableStateOf(emptySet<Long>()) }
        AlertDialog(
            onDismissRequest = { showBulkTagPicker = false },
            title = { Text("Assign Tags to ${selectedPackages.size} Apps") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(state.allTags, key = { it.tagId }) { tag ->
                        val checked = tag.tagId in chosenTagIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    chosenTagIds = if (checked) chosenTagIds - tag.tagId else chosenTagIds + tag.tagId
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    chosenTagIds = if (it) chosenTagIds + tag.tagId else chosenTagIds - tag.tagId
                                },
                            )
                            TagBadgeIcon(tag.iconKey, tag.customImagePath, tag.colorHex, 28.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(tag.name)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = chosenTagIds.isNotEmpty(),
                    onClick = {
                        onBulkAssignTags(selectedPackages.toList(), chosenTagIds.toList())
                        showBulkTagPicker = false
                        bulkMode = false
                        selectedPackages = emptySet()
                    },
                ) {
                    Icon(Icons.Rounded.CheckBox, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkTagPicker = false }) { Text("Cancel") }
            },
        )
    }

    if (showBulkCategoryPicker) {
        var chosenCategoryId by remember { mutableStateOf<Long?>(null) }
        AlertDialog(
            onDismissRequest = { showBulkCategoryPicker = false },
            title = { Text("Override Category for ${selectedPackages.size} Apps") },
            text = {
                val assignableCategories = remember(state.categories) {
                    state.categories.filter { !it.isSystemDockTab }
                }
                LazyColumn(modifier = Modifier.height(320.dp)) {
                    items(assignableCategories, key = { it.categoryId }) { cat ->
                        val selected = cat.categoryId == chosenCategoryId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { chosenCategoryId = cat.categoryId }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CategoryBadgeIcon(cat, size = 28.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = if (cat.categoryType == "GAME_SUB") "Games • ${cat.name}" else cat.name,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = chosenCategoryId != null,
                    onClick = {
                        chosenCategoryId?.let { cid ->
                            onBulkSetCategory(selectedPackages.toList(), cid)
                        }
                        showBulkCategoryPicker = false
                        bulkMode = false
                        selectedPackages = emptySet()
                    },
                ) { Text("Apply Override") }
            },
            dismissButton = {
                TextButton(onClick = { showBulkCategoryPicker = false }) { Text("Cancel") }
            },
        )
    }

    overrideCategoryApp?.let { liveItem ->
        val fresh = state.appsWithTags.find { it.app.packageName == liveItem.app.packageName } ?: liveItem
        AppCategoryOverrideDialog(
            appWithTags = fresh,
            allCategories = state.categories,
            appCategoryRefs = state.appCategories,
            onSaveCategories = { primaryId, allIds, excludedIds ->
                onSaveCategoriesForApp(fresh.app.packageName, primaryId, allIds, excludedIds)
            },
            onRestoreAutomatic = {
                onRestoreAutomaticCategory(fresh.app)
            },
            onDismiss = { overrideCategoryApp = null },
        )
    }

    customizingApp?.let { liveItem ->
        val fresh = state.appsWithTags.find { it.app.packageName == liveItem.app.packageName } ?: liveItem
        AppActionAndCustomizeDialog(
            appWithTags = fresh,
            allTags = state.allTags,
            allCategories = state.categories,
            appCategoryRefs = state.appCategories,
            appDiscovery = appDiscovery,
            onImportImage = onImportImage,
            onSaveCustomizations = { customName, path, scale, ox, oy, alpha ->
                onSaveCustomName(fresh.app.packageName, customName)
                onSaveCustomIcon(fresh.app.packageName, path, scale, ox, oy, alpha)
            },
            onSaveTags = { tagIds -> onSaveTagsForApp(fresh.app.packageName, tagIds) },
            onSaveCategories = { primaryId, allIds, excludedIds ->
                onSaveCategoriesForApp(fresh.app.packageName, primaryId, allIds, excludedIds)
            },
            onRestoreAutoCategory = { onRestoreAutomaticCategory(fresh.app) },
            onToggleFavorite = { onToggleFavorite(fresh.app.packageName, !fresh.app.favorite) },
            onToggleHidden = { onToggleHidden(fresh.app.packageName, !fresh.app.hidden) },
            onOpenAppInfo = { onOpenAppInfo(fresh.app.packageName) },
            onUninstall = { onUninstallApp(fresh.app.packageName) },
            onEditLineIcon = { customizingApp = null; onEditLineIcon(fresh.app) },
            onLaunchApp = { onLaunchApp(fresh.app) },
            appLocked = fresh.app.packageName in state.settings.lockedAppsCsv.split(",").map(String::trim).filter(String::isNotBlank),
            onToggleAppLock = {
                val current = state.settings.lockedAppsCsv.split(",").map(String::trim).filter(String::isNotBlank).toMutableSet()
                val nowLocked = if (fresh.app.packageName in current) {
                    current.remove(fresh.app.packageName)
                    false
                } else {
                    current.add(fresh.app.packageName)
                    true
                }
                onToggleAppLock(fresh.app.packageName, nowLocked)
            },
            onDismiss = { customizingApp = null },
        )
    }
}
