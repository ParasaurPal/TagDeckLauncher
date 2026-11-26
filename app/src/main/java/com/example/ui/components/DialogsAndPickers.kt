package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.db.AppCategoryCrossRef
import com.example.data.db.AppEntity
import com.example.data.db.AppWithTags
import com.example.data.db.CategoryEntity
import com.example.data.db.TagEntity
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.InstalledWidgetProvider
import com.example.ui.theme.TagPresetColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagEditorDialog(
    initialTag: TagEntity?,
    allTags: List<TagEntity>,
    onImportImage: suspend (Uri) -> String?,
    onSave: (TagEntity) -> Unit,
    onDelete: ((Long) -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(initialTag?.name ?: "") }
    var description by remember { mutableStateOf(initialTag?.description ?: "") }
    var colorHex by remember { mutableLongStateOf(initialTag?.colorHex ?: TagPresetColors.first()) }
    var iconKey by remember { mutableStateOf(initialTag?.iconKey ?: "tag") }
    var customImagePath by remember { mutableStateOf(initialTag?.customImagePath) }
    var parentTagId by remember { mutableStateOf(initialTag?.parentTagId) }
    var layoutMode by remember { mutableStateOf(initialTag?.sidebarLayoutMode ?: "GRID") }
    var iconSizeDp by remember { mutableIntStateOf(initialTag?.sidebarIconSizeDp ?: 52) }
    var columns by remember { mutableIntStateOf(initialTag?.sidebarColumns ?: 3) }
    var showCount by remember { mutableStateOf(initialTag?.showCountInSidebar ?: true) }
    var showOnHome by remember { mutableStateOf(initialTag?.showOnHome ?: true) }
    var parentExpanded by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri)
                if (path != null) customImagePath = path
            }
        }
    }
    val docPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri)
                if (path != null) customImagePath = path
            }
        }
    }

    val eligibleParents = allTags.filter { it.tagId != initialTag?.tagId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TagBadgeIcon(iconKey, customImagePath, colorHex, 36.dp)
                Spacer(Modifier.width(10.dp))
                Text(if (initialTag == null) "Create Tag" else "Edit Tag")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tag Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("tag_name_input"),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Tag Color", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TagPresetColors) { c ->
                        val selected = c == colorHex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) Color.White else Color.Transparent,
                                    shape = CircleShape,
                                )
                                .clickable { colorHex = c },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                val iconStyle = LocalIconStyleConfig.current
                                if (iconStyle.enabled) {
                                    OutlinedVectorIcon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        fillColor = Color(iconStyle.fillColorHex),
                                        outlineColor = Color(iconStyle.outlineColorHex),
                                        outlineWidthDp = (iconStyle.outlineWidthDp * 0.55f).dp,
                                        modifier = Modifier.size(18.dp),
                                    )
                                } else {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Text("Tag Icon or Custom Image", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Photo")
                    }
                    OutlinedButton(
                        onClick = {
                            docPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/svg+xml"))
                        },
                    ) {
                        Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("File/SVG")
                    }
                    if (customImagePath != null) {
                        TextButton(onClick = { customImagePath = null }) {
                            Text("Clear")
                        }
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TagIconCatalog) { opt ->
                        val selected = customImagePath == null && iconKey == opt.key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) Color(colorHex).copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (selected) Color(colorHex) else Color.Transparent),
                            modifier = Modifier.clickable {
                                iconKey = opt.key
                                customImagePath = null
                            },
                        ) {
                            Icon(
                                imageVector = opt.icon,
                                contentDescription = opt.label,
                                tint = if (selected) Color(colorHex) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp).size(22.dp),
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = parentExpanded,
                    onExpandedChange = { parentExpanded = it },
                ) {
                    val parentName = eligibleParents.find { it.tagId == parentTagId }?.name ?: "None (Top-Level Tag)"
                    OutlinedTextField(
                        value = parentName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Parent Tag (Nesting)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = parentExpanded,
                        onDismissRequest = { parentExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (Top-Level Tag)") },
                            onClick = {
                                parentTagId = null
                                parentExpanded = false
                            },
                        )
                        eligibleParents.forEach { candidate ->
                            DropdownMenuItem(
                                text = { Text(candidate.name) },
                                onClick = {
                                    parentTagId = candidate.tagId
                                    parentExpanded = false
                                },
                            )
                        }
                    }
                }

                HorizontalDivider()
                Text("Sidebar Presentation", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("GRID", "LIST").forEach { mode ->
                        val active = layoutMode == mode
                        OutlinedButton(
                            onClick = { layoutMode = mode },
                            border = BorderStroke(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        ) {
                            Text(mode)
                        }
                    }
                }
                Text("Icon Size: ${iconSizeDp}dp", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = iconSizeDp.toFloat(),
                    onValueChange = { iconSizeDp = it.roundToInt() },
                    valueRange = 36f..76f,
                )
                if (layoutMode == "GRID") {
                    Text("Grid Columns: $columns", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = columns.toFloat(),
                        onValueChange = { columns = it.roundToInt() },
                        valueRange = 2f..5f,
                        steps = 2,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Show app count in sidebar title", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = showCount, onCheckedChange = { showCount = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show on home screen", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Hide this category shortcut without deleting the tag.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = showOnHome, onCheckedChange = { showOnHome = it })
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = {
                    val saved = (initialTag ?: TagEntity(name = name.trim())).copy(
                        name = name.trim(),
                        description = description.trim(),
                        colorHex = colorHex,
                        iconKey = iconKey,
                        customImagePath = customImagePath,
                        parentTagId = parentTagId,
                        sidebarLayoutMode = layoutMode,
                        sidebarIconSizeDp = iconSizeDp,
                        sidebarColumns = columns,
                        showCountInSidebar = showCount,
                        showOnHome = showOnHome,
                    )
                    onSave(saved)
                },
                modifier = Modifier.testTag("save_tag_button"),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (initialTag != null && onDelete != null) {
                    TextButton(onClick = { onDelete(initialTag.tagId) }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppCategoryOverrideDialog(
    appWithTags: AppWithTags,
    allCategories: List<CategoryEntity>,
    appCategoryRefs: List<AppCategoryCrossRef>,
    onSaveCategories: (primaryId: Long, allIds: List<Long>, excludedIds: List<Long>) -> Unit,
    onRestoreAutomatic: () -> Unit,
    onDismiss: () -> Unit,
) {
    val app = appWithTags.app
    val existingRefs = remember(app.packageName, appCategoryRefs) {
        appCategoryRefs.filter { it.packageName == app.packageName }
    }
    var primaryId by remember { mutableLongStateOf(app.primaryCategoryId) }
    var assignedIds by remember {
        mutableStateOf(
            existingRefs.filter { !it.isExcluded }.map { it.categoryId }.toSet().ifEmpty { setOf(app.primaryCategoryId) }
        )
    }
    var excludedIds by remember {
        mutableStateOf(existingRefs.filter { it.isExcluded }.map { it.categoryId }.toSet())
    }
    var filterQuery by remember { mutableStateOf("") }

    val autoCatName = allCategories.find { it.categoryId == app.automaticCategoryId }?.name ?: "Tools"
    val shownCategories = remember(allCategories, filterQuery) {
        val q = filterQuery.trim().lowercase()
        allCategories.filter { !it.isSystemDockTab && (q.isEmpty() || it.name.lowercase().contains(q)) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Category & Play Classification") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(app.displayName, fontWeight = FontWeight.Bold)
                        Text(
                            "Auto-detected: $autoCatName (${app.categoryConfidence})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (app.manualCategoryOverride) {
                            Text(
                                "Manual Override Active (preserved during app sync)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = { Text("Search 32 Play & 17 Game categories...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(shownCategories, key = { it.categoryId }) { cat ->
                        val isPrimary = cat.categoryId == primaryId
                        val isAssigned = cat.categoryId in assignedIds
                        val isExcluded = cat.categoryId in excludedIds
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPrimary) Color(cat.colorHex).copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CategoryBadgeIcon(cat, size = 28.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (cat.categoryType == "GAME_SUB") "Games • ${cat.name}" else cat.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal,
                                    )
                                }
                                FilterChip(
                                    selected = isPrimary,
                                    onClick = {
                                        primaryId = cat.categoryId
                                        assignedIds = assignedIds + cat.categoryId
                                        excludedIds = excludedIds - cat.categoryId
                                    },
                                    label = { Text(if (isPrimary) "Primary" else "Set") },
                                )
                                Checkbox(
                                    checked = isAssigned && !isExcluded,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            assignedIds = assignedIds + cat.categoryId
                                            excludedIds = excludedIds - cat.categoryId
                                        } else {
                                            assignedIds = assignedIds - cat.categoryId
                                            excludedIds = excludedIds + cat.categoryId
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveCategories(primaryId, assignedIds.toList(), excludedIds.toList())
                    onDismiss()
                },
                modifier = Modifier.testTag("save_category_override"),
            ) {
                Text("Save Override")
            }
        },
        dismissButton = {
            Row {
                if (app.manualCategoryOverride) {
                    TextButton(onClick = {
                        onRestoreAutomatic()
                        onDismiss()
                    }) {
                        Text("Restore Auto")
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

@Composable
fun BulkAssignAppsToTagDialog(
    tag: TagEntity,
    allApps: List<AppWithTags>,
    appDiscovery: AppDiscoveryManager,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var selectedPackages by remember(tag.tagId) {
        mutableStateOf(
            allApps.filter { item -> item.tags.any { it.tagId == tag.tagId } }
                .map { it.app.packageName }
                .toSet()
        )
    }

    val filtered = remember(allApps, query) {
        val q = query.trim().lowercase()
        allApps.filter {
            q.isEmpty() || it.app.displayName.lowercase().contains(q)
        }.sortedBy { it.app.displayName.lowercase() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TagBadgeIcon(tag.iconKey, tag.customImagePath, tag.colorHex, 32.dp)
                Spacer(Modifier.width(10.dp))
                Text("Assign Apps to ${tag.name}", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 440.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Filter apps...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered, key = { it.app.packageName }) { item ->
                        val checked = item.app.packageName in selectedPackages
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPackages = if (checked) {
                                        selectedPackages - item.app.packageName
                                    } else {
                                        selectedPackages + item.app.packageName
                                    }
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    selectedPackages = if (it) {
                                        selectedPackages + item.app.packageName
                                    } else {
                                        selectedPackages - item.app.packageName
                                    }
                                },
                            )
                            AppIconView(item.app, appDiscovery, 36.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = item.app.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedPackages.toList()) },
                modifier = Modifier.testTag("confirm_bulk_assign_apps"),
            ) {
                Text("Apply (${selectedPackages.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun AssignTagsToAppDialog(
    appWithTags: AppWithTags,
    allTags: List<TagEntity>,
    onSave: (List<Long>) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedTagIds by remember(appWithTags.app.packageName) {
        mutableStateOf(appWithTags.tags.map { it.tagId }.toSet())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tags for ${appWithTags.app.displayName}") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                items(allTags, key = { it.tagId }) { tag ->
                    val checked = tag.tagId in selectedTagIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTagIds = if (checked) selectedTagIds - tag.tagId else selectedTagIds + tag.tagId
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                selectedTagIds = if (it) selectedTagIds + tag.tagId else selectedTagIds - tag.tagId
                            },
                        )
                        TagBadgeIcon(tag.iconKey, tag.customImagePath, tag.colorHex, 28.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(tag.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedTagIds.toList()) }) {
                Text("Save Tags")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun AppActionAndCustomizeDialog(
    appWithTags: AppWithTags,
    allTags: List<TagEntity>,
    allCategories: List<CategoryEntity> = emptyList(),
    appCategoryRefs: List<AppCategoryCrossRef> = emptyList(),
    appDiscovery: AppDiscoveryManager,
    onImportImage: suspend (Uri) -> String?,
    onSaveCustomizations: (customName: String?, path: String?, scale: Float, offsetX: Float, offsetY: Float, alpha: Float) -> Unit,
    onSaveTags: (List<Long>) -> Unit,
    onSaveCategories: ((Long, List<Long>, List<Long>) -> Unit)? = null,
    onRestoreAutoCategory: (() -> Unit)? = null,
    onToggleFavorite: () -> Unit,
    onToggleHidden: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    onEditLineIcon: () -> Unit,
    onLaunchApp: () -> Unit = {},
    onAddShortcutToHome: (String, String) -> Unit = { _, _ -> },
    appLocked: Boolean = false,
    onToggleAppLock: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val app = appWithTags.app
    var customName by remember { mutableStateOf(app.customName ?: "") }
    var customIconPath by remember { mutableStateOf(app.customIconPath) }
    var scale by remember { mutableFloatStateOf(app.iconScale) }
    var offsetX by remember { mutableFloatStateOf(app.iconOffsetX) }
    var offsetY by remember { mutableFloatStateOf(app.iconOffsetY) }
    var alpha by remember { mutableFloatStateOf(app.iconAlpha) }
    var showTagChecklist by remember { mutableStateOf(false) }
    var showCategoryOverride by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri)
                if (path != null) customIconPath = path
            }
        }
    }
    val docPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri)
                if (path != null) customIconPath = path
            }
        }
    }

    if (showTagChecklist) {
        AssignTagsToAppDialog(
            appWithTags = appWithTags,
            allTags = allTags,
            onSave = {
                onSaveTags(it)
                showTagChecklist = false
            },
            onDismiss = { showTagChecklist = false },
        )
        return
    }

    if (showCategoryOverride && onSaveCategories != null) {
        AppCategoryOverrideDialog(
            appWithTags = appWithTags,
            allCategories = allCategories,
            appCategoryRefs = appCategoryRefs,
            onSaveCategories = onSaveCategories,
            onRestoreAutomatic = { onRestoreAutoCategory?.invoke() },
            onDismiss = { showCategoryOverride = false },
        )
        return
    }

    val previewApp = app.copy(
        customName = customName.takeIf { it.isNotBlank() },
        customIconPath = customIconPath,
        iconScale = scale,
        iconOffsetX = offsetX,
        iconOffsetY = offsetY,
        iconAlpha = alpha,
    )
    val primaryCategoryName = allCategories.find { it.categoryId == app.primaryCategoryId }?.name ?: "Tools"
    val appShortcuts = remember(app.packageName) { appDiscovery.getAppShortcuts(app.packageName, app.componentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIconView(previewApp, appDiscovery, 52.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(previewApp.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        text = "$primaryCategoryName • ${app.packageName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (app.favorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (app.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = onToggleHidden) {
                        Icon(
                            if (app.hidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = "Hide/Show",
                        )
                    }
                    IconButton(onClick = { showTagChecklist = true }) {
                        Icon(Icons.Rounded.LocalOffer, contentDescription = "Assign tags")
                    }
                    if (onSaveCategories != null && allCategories.isNotEmpty()) {
                        IconButton(onClick = { showCategoryOverride = true }) {
                            Icon(Icons.Rounded.Category, contentDescription = "Edit category")
                        }
                    }
                    IconButton(onClick = onOpenAppInfo) {
                        Icon(Icons.Rounded.Info, contentDescription = "App info")
                    }
                    IconButton(onClick = onUninstall) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Uninstall", tint = MaterialTheme.colorScheme.error)
                    }
                }

                if (onSaveCategories != null && allCategories.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showCategoryOverride = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Rounded.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Category: $primaryCategoryName ${if (app.manualCategoryOverride) "(Manual)" else "(Auto)"}")
                    }
                }

                OutlinedButton(
                    onClick = { showTagChecklist = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Rounded.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tags (${appWithTags.tags.size}): ${appWithTags.tags.joinToString { it.name }.ifBlank { "None" }}")
                }

                OutlinedButton(
                    onClick = onToggleAppLock,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (appLocked) "Disable App Lock" else "Require Device Authentication to Open")
                }

                if (appShortcuts.isNotEmpty()) {
                    Text("App Shortcuts", style = MaterialTheme.typography.labelLarge)
                    appShortcuts.forEach { shortcut ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (appLocked) {
                                        onLaunchApp()
                                    } else if (!appDiscovery.launchAppShortcut(app.packageName, shortcut.id, app.componentName)) {
                                        onLaunchApp()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(shortcut.shortLabel?.toString() ?: shortcut.id, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            TextButton(onClick = {
                                onAddShortcutToHome(shortcut.id, shortcut.shortLabel?.toString() ?: shortcut.id)
                            }) {
                                Text("Add to Home")
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onEditLineIcon,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Edit Line Icon")
                }

                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Custom App Name (blank = default)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Custom Icon (Original Image Preserved)", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    ) {
                        Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Photo")
                    }
                    OutlinedButton(
                        onClick = {
                            docPicker.launch(arrayOf("image/png", "image/jpeg", "image/webp", "image/svg+xml"))
                        },
                    ) {
                        Text("PNG/SVG")
                    }
                    IconButton(onClick = {
                        customIconPath = null
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                        alpha = 1f
                    }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Reset icon")
                    }
                }

                Text("Zoom / Crop Scale: ${"%.2f".format(scale)}x", style = MaterialTheme.typography.bodyMedium)
                Slider(value = scale, onValueChange = { scale = it }, valueRange = 0.6f..2.2f)

                Text("Transparency: ${(alpha * 100).roundToInt()}%", style = MaterialTheme.typography.bodyMedium)
                Slider(value = alpha, onValueChange = { alpha = it }, valueRange = 0.25f..1f)

                if (customIconPath != null) {
                    Text("Horizontal / Vertical Crop Offset", style = MaterialTheme.typography.bodyMedium)
                    Slider(value = offsetX, onValueChange = { offsetX = it }, valueRange = -1f..1f)
                    Slider(value = offsetY, onValueChange = { offsetY = it }, valueRange = -1f..1f)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveCustomizations(
                        customName.takeIf { it.isNotBlank() },
                        customIconPath,
                        scale,
                        offsetX,
                        offsetY,
                        alpha,
                    )
                    onDismiss()
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
fun WidgetPickerDialog(
    providers: List<InstalledWidgetProvider>,
    tags: List<TagEntity>,
    onPickSystemWidget: (InstalledWidgetProvider) -> Unit,
    onPickBuiltinType: (type: String, label: String, wDp: Float, hDp: Float) -> Unit,
    onPickBuiltinTagWidget: (TagEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var weatherCity by remember { mutableStateOf("") }
    val filtered = remember(providers, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) providers else providers.filter {
            it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
        }
    }

    val builtinWidgets = listOf(
        Triple("WIDGET_STACK", "Widget Stack (Swipeable)", 280f to 160f),
        Triple("GAUGE_WIDGET", "Graphic Gauge (Battery/RAM/Day)", 150f to 150f),
        Triple("CONTAINER_BOX", "Scrollable Container Box", 300f to 160f),
        Triple("APP_GROUP_WIDGET", "App Group Grid", 300f to 180f),
        Triple("PAGE_SWITCHER_WIDGET", "Page Switcher Strip", 300f to 56f),
        Triple("SEARCH_BAR_WIDGET", "Embedded Search Bar", 300f to 52f),
        Triple("QUICK_NOTES_WIDGET", "Quick Notes Card", 260f to 150f),
        Triple("FOCUS_TIMER_WIDGET", "Focus Timer", 156f to 156f),
        Triple("MINI_CALCULATOR_WIDGET", "Mini Calculator", 260f to 220f),
        Triple("DEVICE_TOGGLE_WIDGET", "Quick Toggle (Torch/Ringer/Panel)", 110f to 84f),
        Triple("DIGITAL_CLOCK", "Live Digital Clock", 220f to 100f),
        Triple("ANALOG_CLOCK", "Analog Clock", 148f to 148f),
        Triple("CALENDAR_WIDGET", "Live Calendar Date", 160f to 120f),
        Triple("BATTERY_WIDGET", "Live Battery Status", 110f to 108f),
        Triple("STORAGE_RAM_WIDGET", "Storage & RAM Monitor", 180f to 132f),
        Triple("COMPASS_WIDGET", "Hardware Compass", 136f to 136f),
        Triple("MEDIA_WIDGET", "Media Playback Controls", 260f to 96f),
        Triple("APP_USAGE_WIDGET", "Top App Usage (24 Hours)", 230f to 150f),
        Triple("RECENT_APPS_WIDGET", "Recently Used Apps", 200f to 190f),
        Triple("PHOTO_SLIDESHOW", "Photo Slideshow Frame", 220f to 148f),
        Triple("NOTIFICATION_FEED", "Glass Notification Feed", 320f to 420f),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Add Widget")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                Text("TagDeck Built-in Live Widgets", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = weatherCity,
                    onValueChange = { weatherCity = it },
                    label = { Text("Weather widget city") },
                    placeholder = { Text("e.g. Euless, Texas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedButton(
                    onClick = { onPickBuiltinType("WEATHER_WIDGET", weatherCity.trim(), 240f, 160f) },
                    enabled = weatherCity.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Add Live Weather Widget")
                }
                OutlinedButton(
                    onClick = { onPickBuiltinType("AT_A_GLANCE_WIDGET", weatherCity.trim(), 250f, 180f) },
                    enabled = weatherCity.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Add At-a-Glance (Clock + Weather)")
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    builtinWidgets.forEach { (type, lbl, size) ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                onPickBuiltinType(type, lbl, size.first, size.second)
                            },
                        ) {
                            Text(
                                text = lbl,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            )
                        }
                    }
                }
                if (tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Live Tag Decks", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tags, key = { it.tagId }) { tag ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(tag.colorHex).copy(alpha = 0.2f),
                                modifier = Modifier.clickable { onPickBuiltinTagWidget(tag) },
                            ) {
                                Text(
                                    text = "${tag.name} Deck",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(tag.colorHex),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Android System & Third-Party Widgets", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search widgets...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (filtered.isEmpty()) {
                        Text(
                            text = "No matching Android widgets found on device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp),
                        )
                    } else {
                        filtered.forEach { p ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPickSystemWidget(p) },
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(p.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${p.packageName} • ${p.minWidthDp}x${p.minHeightDp}dp",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}
