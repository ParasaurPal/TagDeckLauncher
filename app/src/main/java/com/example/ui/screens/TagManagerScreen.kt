package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.CallMerge
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SubdirectoryArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.db.AppEntity
import com.example.data.db.CategoryEntity
import com.example.data.db.PlayCategoryCatalog
import com.example.data.db.SavedTagRuleEntity
import com.example.data.db.TagEntity
import com.example.data.repository.TagFilterEngine
import com.example.data.repository.TagMatchMode
import com.example.data.system.AppDiscoveryManager
import com.example.ui.components.AppIconView
import com.example.ui.components.BulkAssignAppsToTagDialog
import com.example.ui.components.CategoryBadgeIcon
import com.example.ui.components.TagBadgeIcon
import com.example.ui.components.TagEditorDialog
import com.example.ui.components.TagIconCatalog
import com.example.ui.state.LauncherUiState
import com.example.ui.theme.TagPresetColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagManagerScreen(
    state: LauncherUiState,
    appDiscovery: AppDiscoveryManager,
    onBack: () -> Unit,
    onOpenAppsManager: () -> Unit,
    onOpenTagSidebar: (Long) -> Unit,
    onOpenCategorySidebar: (Long) -> Unit,
    onOpenRuleSidebar: (Long) -> Unit,
    onLaunchApp: (AppEntity) -> Unit,
    onSaveTag: (TagEntity) -> Unit,
    onDeleteTag: (Long) -> Unit,
    onDuplicateTag: (TagEntity) -> Unit,
    onMergeTags: (Long, Long) -> Unit,
    onMoveTagOrder: (Long, Int) -> Unit,
    onReplaceAppsForTag: (Long, List<String>) -> Unit,
    onSaveRule: (SavedTagRuleEntity) -> Unit,
    onDeleteRule: (Long) -> Unit,
    onSaveCategory: (CategoryEntity) -> Unit,
    onDeleteCustomCategory: (Long) -> Unit,
    onImportImage: suspend (Uri, String, String?) -> String?,
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var editingTag by remember { mutableStateOf<TagEntity?>(null) }
    var mergingTag by remember { mutableStateOf<TagEntity?>(null) }
    var showCreateTag by remember { mutableStateOf(false) }
    var assigningAppsTag by remember { mutableStateOf<TagEntity?>(null) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showCreateCategory by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tags_back_button")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Tags & Play Categories",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedButton(
                        onClick = onOpenAppsManager,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_apps_manager_button"),
                    ) {
                        Icon(Icons.Rounded.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Apps")
                    }
                }
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Tags (${state.allTags.size})") },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Filter Rules") },
                        modifier = Modifier.testTag("tab_advanced_filter"),
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Play Cats (${state.categories.size})") },
                        modifier = Modifier.testTag("tab_play_categories"),
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> FloatingActionButton(
                    onClick = { showCreateTag = true },
                    modifier = Modifier.testTag("create_tag_fab"),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Create Tag")
                }
                2 -> FloatingActionButton(
                    onClick = { showCreateCategory = true },
                    modifier = Modifier.testTag("create_category_fab"),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Create Custom Category")
                }
            }
        },
    ) { padding ->
        when (selectedTab) {
            0 -> {
                val filteredTags = remember(state.tagsWithApps, searchQuery) {
                    val q = searchQuery.trim().lowercase()
                    if (q.isEmpty()) state.tagsWithApps
                    else state.tagsWithApps.filter {
                        it.tag.name.lowercase().contains(q) || it.tag.description.lowercase().contains(q)
                    }
                }
                val parentLookup = remember(state.allTags) { state.allTags.associateBy { it.tagId } }

                Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search tags...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("tag_search_input"),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Tap any tag to open its sidebar. Duplicate, merge, nest, or reorder tags.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 84.dp),
                    ) {
                        items(filteredTags, key = { it.tag.tagId }) { item ->
                            val tag = item.tag
                            val parent = tag.parentTagId?.let { parentLookup[it] }
                            val childCount = state.allTags.count { it.parentTagId == tag.tagId }
                            val activeAppsCount = item.apps.count { it.isInstalled && !it.hidden }
                            val color = Color(tag.colorHex)

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenTagSidebar(tag.tagId) }
                                    .testTag("tag_card_${tag.tagId}"),
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    TagBadgeIcon(tag.iconKey, tag.customImagePath, tag.colorHex, 46.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        if (parent != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Rounded.SubdirectoryArrowRight,
                                                    contentDescription = null,
                                                    tint = Color(parent.colorHex),
                                                    modifier = Modifier.size(14.dp),
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = parent.name,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(parent.colorHex),
                                                )
                                            }
                                        }
                                        Text(
                                            text = tag.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = buildString {
                                                append("$activeAppsCount apps")
                                                if (childCount > 0) append(" • $childCount nested")
                                                if (tag.description.isNotBlank()) append(" • ${tag.description}")
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    IconButton(
                                        onClick = { assigningAppsTag = tag },
                                        modifier = Modifier.testTag("assign_apps_tag_${tag.tagId}"),
                                    ) {
                                        Icon(Icons.Rounded.GroupAdd, contentDescription = "Assign apps")
                                    }
                                    IconButton(
                                        onClick = { onDuplicateTag(tag) },
                                        modifier = Modifier.testTag("duplicate_tag_${tag.tagId}"),
                                    ) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Duplicate tag")
                                    }
                                    if (state.allTags.size > 1) {
                                        IconButton(
                                            onClick = { mergingTag = tag },
                                            modifier = Modifier.testTag("merge_tag_${tag.tagId}"),
                                        ) {
                                            Icon(Icons.AutoMirrored.Rounded.CallMerge, contentDescription = "Merge tag")
                                        }
                                    }
                                    IconButton(
                                        onClick = { editingTag = tag },
                                        modifier = Modifier.testTag("edit_tag_${tag.tagId}"),
                                    ) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit tag")
                                    }
                                    Column {
                                        IconButton(
                                            onClick = { onMoveTagOrder(tag.tagId, -1) },
                                            modifier = Modifier.size(28.dp),
                                        ) {
                                            Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move up", modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { onMoveTagOrder(tag.tagId, 1) },
                                            modifier = Modifier.size(28.dp),
                                        ) {
                                            Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move down", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                AdvancedTagFilterTab(
                    state = state,
                    appDiscovery = appDiscovery,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onLaunchApp = onLaunchApp,
                    onOpenRuleSidebar = onOpenRuleSidebar,
                    onSaveRule = onSaveRule,
                    onDeleteRule = onDeleteRule,
                )
            }

            else -> {
                CategoriesAndGamesTab(
                    state = state,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onOpenCategorySidebar = onOpenCategorySidebar,
                    onEditCategory = { editingCategory = it },
                    onDeleteCustomCategory = onDeleteCustomCategory,
                )
            }
        }
    }

    if (showCreateTag || editingTag != null) {
        TagEditorDialog(
            initialTag = editingTag,
            allTags = state.allTags,
            onImportImage = { uri -> onImportImage(uri, "Tags", null) },
            onSave = { saved ->
                onSaveTag(saved)
                showCreateTag = false
                editingTag = null
            },
            onDelete = editingTag?.let { t ->
                {
                    onDeleteTag(t.tagId)
                    editingTag = null
                }
            },
            onDismiss = {
                showCreateTag = false
                editingTag = null
            },
        )
    }

    mergingTag?.let { sourceTag ->
        val targets = state.allTags.filter { it.tagId != sourceTag.tagId }
        AlertDialog(
            onDismissRequest = { mergingTag = null },
            title = { Text("Merge \"${sourceTag.name}\" Into...") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(targets, key = { it.tagId }) { target ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMergeTags(sourceTag.tagId, target.tagId)
                                    mergingTag = null
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TagBadgeIcon(target.iconKey, target.customImagePath, target.colorHex, 30.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(target.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mergingTag = null }) { Text("Cancel") }
            },
        )
    }

    assigningAppsTag?.let { tag ->
        BulkAssignAppsToTagDialog(
            tag = tag,
            allApps = state.appsWithTags,
            appDiscovery = appDiscovery,
            onSave = { pkgList ->
                onReplaceAppsForTag(tag.tagId, pkgList)
                assigningAppsTag = null
            },
            onDismiss = { assigningAppsTag = null },
        )
    }

    if (showCreateCategory || editingCategory != null) {
        CategoryEditorDialog(
            initialCategory = editingCategory,
            onImportImage = { uri -> onImportImage(uri, "Categories", null) },
            onSave = { saved ->
                onSaveCategory(saved)
                showCreateCategory = false
                editingCategory = null
            },
            onDismiss = {
                showCreateCategory = false
                editingCategory = null
            },
        )
    }
}

@Composable
private fun CategoriesAndGamesTab(
    state: LauncherUiState,
    modifier: Modifier = Modifier,
    onOpenCategorySidebar: (Long) -> Unit,
    onEditCategory: (CategoryEntity) -> Unit,
    onDeleteCustomCategory: (Long) -> Unit,
) {
    var filterQuery by remember { mutableStateOf("") }
    var showType by remember { mutableStateOf("ALL") } // ALL, APP, GAMES, CUSTOM

    val appCountsByCategory = remember(state.appsWithTags, state.appCategories) {
        val map = mutableMapOf<Long, MutableSet<String>>()
        state.appsWithTags.filter { it.app.isInstalled && !it.app.hidden }.forEach { item ->
            map.getOrPut(item.app.primaryCategoryId) { mutableSetOf() }.add(item.app.packageName)
            if (item.app.isGame) {
                map.getOrPut(PlayCategoryCatalog.GAMES_PARENT_ID) { mutableSetOf() }.add(item.app.packageName)
            }
        }
        state.appCategories.filter { !it.isExcluded }.forEach { ref ->
            map.getOrPut(ref.categoryId) { mutableSetOf() }.add(ref.packageName)
        }
        map.mapValues { it.value.size }
    }

    val filteredCategories = remember(state.categories, filterQuery, showType) {
        val q = filterQuery.trim().lowercase()
        state.categories.filter { cat ->
            val typeOk = when (showType) {
                "APP" -> cat.categoryType == "APP"
                "GAMES" -> cat.categoryType == "GAMES_PARENT" || cat.categoryType == "GAME_SUB"
                "CUSTOM" -> cat.isCustom || cat.categoryType == "CUSTOM"
                else -> true
            }
            val queryOk = q.isEmpty() || cat.name.lowercase().contains(q)
            typeOk && queryOk
        }
    }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = filterQuery,
            onValueChange = { filterQuery = it },
            placeholder = { Text("Search 32 Play & 17 Game categories...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().testTag("category_search_input"),
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val chips = listOf(
                "ALL" to "All (${state.categories.size})",
                "APP" to "32 Play Apps",
                "GAMES" to "Games + 17 Sub",
                "CUSTOM" to "Custom",
            )
            items(chips) { (key, label) ->
                FilterChip(
                    selected = showType == key,
                    onClick = { showType = key },
                    label = { Text(label) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 84.dp),
        ) {
            items(filteredCategories, key = { it.categoryId }) { cat ->
                val count = appCountsByCategory[cat.categoryId] ?: 0
                val accent = Color(cat.colorHex)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCategorySidebar(cat.categoryId) }
                        .testTag("category_card_${cat.categoryId}"),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CategoryBadgeIcon(cat, size = 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (cat.categoryType == "GAME_SUB" || cat.categoryType == "GAMES_PARENT") {
                                    Icon(
                                        Icons.Rounded.SportsEsports,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(
                                    text = if (cat.categoryType == "GAME_SUB") "Games • ${cat.name}" else cat.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Text(
                                text = "$count apps • ${if (cat.isCustom) "Custom Category" else cat.categoryType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { onEditCategory(cat) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Customize category")
                        }
                        if (cat.isCustom) {
                            IconButton(onClick = { onDeleteCustomCategory(cat.categoryId) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete custom category", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryEditorDialog(
    initialCategory: CategoryEntity?,
    onImportImage: suspend (Uri) -> String?,
    onSave: (CategoryEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var iconKey by remember { mutableStateOf(initialCategory?.iconKey ?: "tag") }
    var customIconPath by remember { mutableStateOf(initialCategory?.customIconPath) }
    var customEmoji by remember { mutableStateOf(initialCategory?.customEmoji ?: "") }
    var colorHex by remember { mutableLongStateOf(initialCategory?.colorHex ?: TagPresetColors.first()) }
    var iconSizeDp by remember { mutableIntStateOf(initialCategory?.iconSizeDp ?: 24) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri)
                if (path != null) customIconPath = path
            }
        }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri)
                if (path != null) customIconPath = path
            }
        }
    }

    val preview = (initialCategory ?: CategoryEntity(name = name.ifBlank { "Custom" }, categoryType = "CUSTOM", isCustom = true)).copy(
        name = name.ifBlank { "Custom" },
        iconKey = iconKey,
        customIconPath = customIconPath,
        customEmoji = customEmoji.takeIf { it.isNotBlank() },
        colorHex = colorHex,
        backgroundColorHex = (0x22000000L or (colorHex and 0x00FFFFFFL)),
        iconSizeDp = iconSizeDp,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CategoryBadgeIcon(preview, size = 36.dp)
                Text(if (initialCategory == null) "New Custom Category" else "Customize Category")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("category_name_input"),
                )
                OutlinedTextField(
                    value = customEmoji,
                    onValueChange = { customEmoji = it.take(4) },
                    label = { Text("Custom Emoji Badge (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Accent Color", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TagPresetColors) { c ->
                        val selected = c == colorHex
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .border(if (selected) 2.5.dp else 0.dp, Color.White, CircleShape)
                                .clickable { colorHex = c },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) Icon(Icons.Rounded.Check, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Text("Icon or Custom PNG/SVG", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Rounded.PhotoLibrary, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Gallery")
                    }
                    OutlinedButton(onClick = {
                        filePicker.launch(arrayOf("image/*", "image/svg+xml"))
                    }) {
                        Icon(Icons.Rounded.FolderOpen, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("SVG/PNG")
                    }
                    if (customIconPath != null) {
                        TextButton(onClick = { customIconPath = null }) { Text("Clear") }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TagIconCatalog) { opt ->
                        val selected = customIconPath == null && customEmoji.isBlank() && iconKey == opt.key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) Color(colorHex).copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (selected) Color(colorHex) else Color.Transparent),
                            modifier = Modifier.clickable {
                                iconKey = opt.key
                                customIconPath = null
                                customEmoji = ""
                            },
                        ) {
                            Icon(opt.icon, opt.label, modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                    }
                }
                Text("Icon Size: ${iconSizeDp}dp")
                Slider(
                    value = iconSizeDp.toFloat(),
                    onValueChange = { iconSizeDp = it.roundToInt() },
                    valueRange = 18f..42f,
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = {
                    val saved = (initialCategory ?: CategoryEntity(
                        name = name.trim(),
                        categoryType = "CUSTOM",
                        isCustom = true,
                        sortOrder = 900,
                    )).copy(
                        name = name.trim(),
                        iconKey = iconKey,
                        customIconPath = customIconPath,
                        customEmoji = customEmoji.takeIf { it.isNotBlank() },
                        colorHex = colorHex,
                        backgroundColorHex = (0x22000000L or (colorHex and 0x00FFFFFFL)),
                        iconSizeDp = iconSizeDp,
                    )
                    onSave(saved)
                },
                modifier = Modifier.testTag("save_category_button"),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdvancedTagFilterTab(
    state: LauncherUiState,
    appDiscovery: AppDiscoveryManager,
    modifier: Modifier = Modifier,
    onLaunchApp: (AppEntity) -> Unit,
    onOpenRuleSidebar: (Long) -> Unit,
    onSaveRule: (SavedTagRuleEntity) -> Unit,
    onDeleteRule: (Long) -> Unit,
) {
    var matchMode by remember { mutableStateOf(TagMatchMode.AND) }
    var includeIds by remember { mutableStateOf(emptySet<Long>()) }
    var excludeIds by remember { mutableStateOf(emptySet<Long>()) }
    var includeNested by remember { mutableStateOf(true) }
    var showSaveRuleDialog by remember { mutableStateOf(false) }

    val matchingApps = remember(state.appsWithTags, state.allTags, includeIds, excludeIds, matchMode, includeNested) {
        val expandedIncludes = if (includeNested) {
            includeIds.flatMap { TagFilterEngine.collectDescendantTagIds(it, state.allTags) }.toSet()
        } else includeIds
        TagFilterEngine.filterApps(
            appsWithTags = state.appsWithTags,
            includeTagIds = expandedIncludes,
            excludeTagIds = excludeIds,
            matchMode = matchMode,
        )
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Dynamic Tag Rule Builder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = matchMode == TagMatchMode.AND,
                            onClick = { matchMode = TagMatchMode.AND },
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                        ) { Text("Match ALL (AND)") }
                        SegmentedButton(
                            selected = matchMode == TagMatchMode.OR,
                            onClick = { matchMode = TagMatchMode.OR },
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                        ) { Text("Match ANY (OR)") }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Include nested child tags", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = includeNested, onCheckedChange = { includeNested = it })
                    }

                    Text("Include Tags", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.allTags.forEach { tag ->
                            val selected = tag.tagId in includeIds
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    includeIds = if (selected) includeIds - tag.tagId else includeIds + tag.tagId
                                    excludeIds = excludeIds - tag.tagId
                                },
                                label = { Text(tag.name) },
                            )
                        }
                    }

                    Text("Exclude Tags", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.allTags.forEach { tag ->
                            val excluded = tag.tagId in excludeIds
                            FilterChip(
                                selected = excluded,
                                onClick = {
                                    excludeIds = if (excluded) excludeIds - tag.tagId else excludeIds + tag.tagId
                                    includeIds = includeIds - tag.tagId
                                },
                                label = { Text("NOT ${tag.name}") },
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${matchingApps.size} matching apps",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Button(
                            enabled = includeIds.isNotEmpty() || excludeIds.isNotEmpty(),
                            onClick = { showSaveRuleDialog = true },
                        ) {
                            Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save Rule")
                        }
                    }
                }
            }
        }

        if (state.savedRules.isNotEmpty()) {
            item {
                Text("Saved Tag Combinations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(state.savedRules, key = { it.ruleId }) { rule ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(rule.colorHex).copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color(rule.colorHex).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenRuleSidebar(rule.ruleId) },
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color(rule.colorHex))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rule.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Mode: ${rule.matchMode} • ${rule.includeIds().size} included • ${rule.excludeIds().size} excluded",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { onDeleteRule(rule.ruleId) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete rule")
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider()
            Spacer(Modifier.height(4.dp))
            Text("Live Matching Apps (${matchingApps.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (matchingApps.isEmpty()) {
            item {
                Text(
                    text = "No apps match the current include/exclude combination.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(matchingApps, key = { it.app.packageName }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLaunchApp(item.app) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppIconView(item.app, appDiscovery, 42.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.app.displayName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = item.tags.joinToString(" • ") { it.name }.ifBlank { "Untagged" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }

    if (showSaveRuleDialog) {
        var ruleName by remember { mutableStateOf("") }
        var colorHex by remember { mutableLongStateOf(TagPresetColors[1]) }
        AlertDialog(
            onDismissRequest = { showSaveRuleDialog = false },
            title = { Text("Save Tag Combination") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ruleName,
                        onValueChange = { ruleName = it },
                        label = { Text("Collection Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TagPresetColors.take(6).forEach { c ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(c),
                                border = if (c == colorHex) BorderStroke(2.dp, Color.White) else null,
                                modifier = Modifier.size(28.dp).clickable { colorHex = c },
                            ) {}
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = ruleName.isNotBlank(),
                    onClick = {
                        onSaveRule(
                            SavedTagRuleEntity(
                                name = ruleName.trim(),
                                colorHex = colorHex,
                                matchMode = matchMode.name,
                                includeTagIdsCsv = includeIds.joinToString(","),
                                excludeTagIdsCsv = excludeIds.joinToString(","),
                                includeNestedTags = includeNested,
                            )
                        )
                        showSaveRuleDialog = false
                    },
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveRuleDialog = false }) { Text("Cancel") }
            },
        )
    }
}
