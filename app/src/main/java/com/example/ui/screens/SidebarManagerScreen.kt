package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ViewSidebar
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.HomeElementEntity
import com.example.data.db.LauncherWindowEntity
import com.example.data.db.SidebarConfigEntity
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.InstalledWidgetProvider
import com.example.ui.components.WidgetPickerDialog
import com.example.ui.state.LauncherUiState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SidebarManagerScreen(
    state: LauncherUiState,
    appDiscovery: AppDiscoveryManager,
    onBack: () -> Unit,
    onOpenSidebar: (Long) -> Unit,
    onSaveSidebar: (SidebarConfigEntity) -> Unit,
    onDeleteSidebar: (Long) -> Unit,
    onOpenWindow: (Long) -> Unit,
    onSaveWindow: (LauncherWindowEntity) -> Unit,
    onDeleteWindow: (Long) -> Unit,
    onAddWindowElement: (HomeElementEntity) -> Unit,
    onDeleteWindowElement: (HomeElementEntity) -> Unit,
    onImportImage: suspend (Uri, String, String?) -> String?,
    installedWidgetProviders: List<InstalledWidgetProvider> = emptyList(),
    onDuplicateWindowElement: (HomeElementEntity) -> Unit = {},
    onCopyWindowElement: (HomeElementEntity) -> Unit = {},
    onCutWindowElement: (HomeElementEntity) -> Unit = {},
    onBringWindowElementForward: (HomeElementEntity) -> Unit = {},
    onSendWindowElementBackward: (HomeElementEntity) -> Unit = {},
    onStartSystemWidgetBindForWindow: (InstalledWidgetProvider, Long, Long) -> Unit = { _, _, _ -> },
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Edge Drawers, 1 = Windows Builder
    var editingSidebar by remember { mutableStateOf<SidebarConfigEntity?>(null) }
    var showCreateSidebar by remember { mutableStateOf(false) }

    var editingWindow by remember { mutableStateOf<LauncherWindowEntity?>(null) }
    var showCreateWindow by remember { mutableStateOf(false) }
    var addingElementToWindow by remember { mutableStateOf<LauncherWindowEntity?>(null) }
    var widgetPickerForWindow by remember { mutableStateOf<LauncherWindowEntity?>(null) }
    var inspectingWindowElement by remember { mutableStateOf<HomeElementEntity?>(null) }

    val defaultPageId = state.homePages.firstOrNull()?.pageId ?: 1L

    Scaffold(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        topBar = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("sidebar_studio_back")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Text("Sidebars & Windows", style = MaterialTheme.typography.headlineMedium)
                }
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("4-Edge Sidebars (${state.sidebars.size})") },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Windows (${state.windows.size})") },
                        modifier = Modifier.testTag("tab_windows_builder"),
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showCreateSidebar = true else showCreateWindow = true
                },
                modifier = Modifier.testTag("sidebar_or_window_add_fab"),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = if (selectedTab == 0) "Create Sidebar" else "Create Window")
            }
        },
    ) { padding ->
        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 84.dp),
            ) {
                item {
                    Text(
                        text = "Configure Left, Right, Top, or Bottom sliding drawers backed by Favorites, All Apps, Tags, Play Categories, Rules, or Custom Collections.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                items(state.sidebars, key = { it.sidebarId }) { sb ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth().clickable { onOpenSidebar(sb.sidebarId) },
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ViewSidebar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sb.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${sb.side} • ${sb.sourceType} • ${(sb.widthFraction * 100).roundToInt()}%W • ${sb.animationStyle}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { onOpenSidebar(sb.sidebarId) }) {
                                Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = "Open sidebar")
                            }
                            IconButton(onClick = { editingSidebar = sb }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit sidebar")
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 84.dp),
            ) {
                item {
                    Text(
                        text = "Windows are independent Total Launcher overlay panels containing their own freely placed widgets, shortcuts, tags, and controls.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                items(state.windows, key = { it.windowId }) { win ->
                    val innerElements = state.homeElements.filter {
                        it.ownerType == "WINDOW" && it.ownerWindowId == win.windowId
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth().clickable { onOpenWindow(win.windowId) },
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Layers, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(win.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${if (win.fullScreen) "Full Screen" else "${(win.widthFraction * 100).roundToInt()}%×${(win.heightFraction * 100).roundToInt()}%"} • ${win.alignment} • ${innerElements.size} elements",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { onOpenWindow(win.windowId) }) {
                                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = "Open window")
                                }
                                IconButton(onClick = { editingWindow = win }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit window")
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { addingElementToWindow = win }) {
                                    Icon(Icons.Rounded.Add, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add Object")
                                }
                                OutlinedButton(onClick = { widgetPickerForWindow = win }) {
                                    Icon(Icons.Rounded.Widgets, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Widget")
                                }
                                Button(onClick = { onOpenWindow(win.windowId) }) {
                                    Text("Open")
                                }
                            }

                            if (innerElements.isNotEmpty()) {
                                innerElements.forEach { el ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { inspectingWindowElement = el }
                                            .padding(start = 6.dp, top = 2.dp, bottom = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "• ${el.label.ifBlank { el.type }} (${el.type})",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f),
                                        )
                                        IconButton(onClick = { inspectingWindowElement = el }, modifier = Modifier.size(30.dp)) {
                                            Icon(Icons.Rounded.Tune, contentDescription = "Customize element", modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = { onDeleteWindowElement(el) }, modifier = Modifier.size(30.dp)) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Remove element", modifier = Modifier.size(16.dp))
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

    if (showCreateSidebar || editingSidebar != null) {
        SidebarEditorDialog(
            initial = editingSidebar,
            state = state,
            onImportImage = { uri -> onImportImage(uri, "Sidebars", null) },
            onSave = {
                onSaveSidebar(it)
                showCreateSidebar = false
                editingSidebar = null
            },
            onDelete = editingSidebar?.let { sb ->
                {
                    onDeleteSidebar(sb.sidebarId)
                    editingSidebar = null
                }
            },
            onDismiss = {
                showCreateSidebar = false
                editingSidebar = null
            },
        )
    }

    if (showCreateWindow || editingWindow != null) {
        WindowEditorDialog(
            initial = editingWindow,
            onImportImage = { uri -> onImportImage(uri, "Windows", null) },
            onSave = {
                onSaveWindow(it)
                showCreateWindow = false
                editingWindow = null
            },
            onDelete = editingWindow?.let { w ->
                {
                    onDeleteWindow(w.windowId)
                    editingWindow = null
                }
            },
            onDismiss = {
                showCreateWindow = false
                editingWindow = null
            },
        )
    }

    addingElementToWindow?.let { targetWin ->
        AddHomeElementDialog(
            pageId = defaultPageId,
            ownerWindowId = targetWin.windowId,
            state = state,
            appDiscovery = appDiscovery,
            onImportImage = { uri -> onImportImage(uri, "Windows", null) },
            onAddElement = { element ->
                onAddWindowElement(element)
            },
            onOpenWidgetPicker = {
                widgetPickerForWindow = targetWin
            },
            onDismiss = { addingElementToWindow = null },
        )
    }

    widgetPickerForWindow?.let { targetWin ->
        WidgetPickerDialog(
            providers = installedWidgetProviders,
            tags = state.allTags,
            onPickSystemWidget = { provider ->
                widgetPickerForWindow = null
                onStartSystemWidgetBindForWindow(provider, defaultPageId, targetWin.windowId)
            },
            onPickBuiltinType = { type, label, wDp, hDp ->
                onAddWindowElement(
                    HomeElementEntity(
                        pageId = defaultPageId,
                        ownerType = "WINDOW",
                        ownerWindowId = targetWin.windowId,
                        type = type,
                        xFraction = 0.08f,
                        yFraction = 0.08f,
                        widthDp = wDp,
                        heightDp = hDp,
                        label = label,
                    )
                )
                widgetPickerForWindow = null
            },
            onPickBuiltinTagWidget = { tag ->
                onAddWindowElement(
                    HomeElementEntity(
                        pageId = defaultPageId,
                        ownerType = "WINDOW",
                        ownerWindowId = targetWin.windowId,
                        type = "TAG_COLLECTION_WIDGET",
                        xFraction = 0.06f,
                        yFraction = 0.14f,
                        widthDp = 280f,
                        heightDp = 220f,
                        label = tag.name,
                        targetTagId = tag.tagId,
                        backgroundColorHex = 0x660F172AL,
                        borderColorHex = (0x88000000L or (tag.colorHex and 0x00FFFFFFL)),
                    )
                )
                widgetPickerForWindow = null
            },
            onDismiss = { widgetPickerForWindow = null },
        )
    }

    inspectingWindowElement?.let { snap ->
        val elem = state.homeElements.find { it.elementId == snap.elementId } ?: snap
        ElementInspectorDialog(
            element = elem,
            state = state,
            onImportImage = onImportImage,
            onUpdate = { onAddWindowElement(it) },
            onDuplicate = {
                onDuplicateWindowElement(elem)
                inspectingWindowElement = null
            },
            onCopy = {
                onCopyWindowElement(elem)
                inspectingWindowElement = null
            },
            onCut = {
                onCutWindowElement(elem)
                inspectingWindowElement = null
            },
            onBringForward = { onBringWindowElementForward(elem) },
            onSendBackward = { onSendWindowElementBackward(elem) },
            onDelete = {
                onDeleteWindowElement(elem)
                inspectingWindowElement = null
            },
            onDismiss = { inspectingWindowElement = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SidebarEditorDialog(
    initial: SidebarConfigEntity?,
    state: LauncherUiState,
    onImportImage: suspend (Uri) -> String?,
    onSave: (SidebarConfigEntity) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var side by remember { mutableStateOf(initial?.side ?: "LEFT") }
    var sourceType by remember { mutableStateOf(initial?.sourceType ?: "TAG") }
    var targetTagId by remember { mutableStateOf(initial?.targetTagId ?: state.allTags.firstOrNull()?.tagId) }
    var targetCategoryId by remember { mutableStateOf(initial?.targetCategoryId ?: state.categories.firstOrNull()?.categoryId) }
    var targetRuleId by remember { mutableStateOf(initial?.targetRuleId ?: state.savedRules.firstOrNull()?.ruleId) }
    var customPackages by remember { mutableStateOf(initial?.customPackages()?.toSet() ?: emptySet()) }
    var widthFraction by remember { mutableFloatStateOf(initial?.widthFraction ?: 0.78f) }
    var heightFraction by remember { mutableFloatStateOf(initial?.heightFraction ?: 0.72f) }
    var cornerRadiusDp by remember { mutableFloatStateOf(initial?.cornerRadiusDp ?: 28f) }
    var blurDp by remember { mutableFloatStateOf(initial?.backgroundBlurDp ?: 0f) }
    var layoutMode by remember { mutableStateOf(initial?.layoutMode ?: "GRID") }
    var iconSizeDp by remember { mutableIntStateOf(initial?.iconSizeDp ?: 52) }
    var animationStyle by remember { mutableStateOf(initial?.animationStyle ?: "SLIDE") }
    var edgeEnabled by remember { mutableStateOf(initial?.edgeZoneEnabled ?: true) }
    var showHandle by remember { mutableStateOf(initial?.showHandle ?: true) }
    var pinned by remember { mutableStateOf(initial?.pinned ?: false) }
    var bgImagePath by remember { mutableStateOf(initial?.backgroundImagePath) }

    var sourceMenuExpanded by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri)
                if (path != null) bgImagePath = path
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Sidebar" else "Edit Sidebar") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Sidebar Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Opening Edge (4-Side Drawer)", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf("LEFT", "RIGHT", "TOP", "BOTTOM").forEachIndexed { idx, edge ->
                        SegmentedButton(
                            selected = side == edge,
                            onClick = { side = edge },
                            shape = SegmentedButtonDefaults.itemShape(idx, 4),
                        ) { Text(edge.take(1) + edge.drop(1).lowercase()) }
                    }
                }

                ExposedDropdownMenuBox(expanded = sourceMenuExpanded, onExpandedChange = { sourceMenuExpanded = it }) {
                    OutlinedTextField(
                        value = sourceType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Content Source") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sourceMenuExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = sourceMenuExpanded, onDismissRequest = { sourceMenuExpanded = false }) {
                        listOf("FAVORITES", "ALL_APPS", "TAG", "CATEGORY", "RULE", "CUSTOM_COLLECTION").forEach { src ->
                            DropdownMenuItem(
                                text = { Text(src) },
                                onClick = {
                                    sourceType = src
                                    sourceMenuExpanded = false
                                },
                            )
                        }
                    }
                }

                if (sourceType == "TAG" && state.allTags.isNotEmpty()) {
                    Text("Selected Tag", style = MaterialTheme.typography.labelLarge)
                    state.allTags.forEach { tag ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { targetTagId = tag.tagId }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = (if (targetTagId == tag.tagId) "● " else "○ ") + tag.name,
                                fontWeight = if (targetTagId == tag.tagId) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }

                if (sourceType == "CATEGORY" && state.categories.isNotEmpty()) {
                    Text("Selected Play Category", style = MaterialTheme.typography.labelLarge)
                    Column(modifier = Modifier.heightIn(max = 160.dp).verticalScroll(rememberScrollState())) {
                        state.categories.forEach { cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { targetCategoryId = cat.categoryId }.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = (if (targetCategoryId == cat.categoryId) "● " else "○ ") + cat.name,
                                    fontWeight = if (targetCategoryId == cat.categoryId) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }

                if (sourceType == "RULE" && state.savedRules.isNotEmpty()) {
                    Text("Selected Rule", style = MaterialTheme.typography.labelLarge)
                    state.savedRules.forEach { rule ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { targetRuleId = rule.ruleId }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = (if (targetRuleId == rule.ruleId) "● " else "○ ") + rule.name,
                                fontWeight = if (targetRuleId == rule.ruleId) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }

                if (sourceType == "CUSTOM_COLLECTION") {
                    Text("Custom Apps (${customPackages.size})", style = MaterialTheme.typography.labelLarge)
                    Column(modifier = Modifier.heightIn(max = 160.dp).verticalScroll(rememberScrollState())) {
                        state.appsWithTags.take(25).forEach { item ->
                            val checked = item.app.packageName in customPackages
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    customPackages = if (checked) customPackages - item.app.packageName else customPackages + item.app.packageName
                                },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(checked = checked, onCheckedChange = {
                                    customPackages = if (it) customPackages + item.app.packageName else customPackages - item.app.packageName
                                })
                                Text(item.app.displayName)
                            }
                        }
                    }
                }

                Text("Width: ${(widthFraction * 100).roundToInt()}%")
                Slider(value = widthFraction, onValueChange = { widthFraction = it }, valueRange = 0.45f..0.95f)

                if (side == "TOP" || side == "BOTTOM") {
                    Text("Height: ${(heightFraction * 100).roundToInt()}%")
                    Slider(value = heightFraction, onValueChange = { heightFraction = it }, valueRange = 0.35f..0.92f)
                }

                Text("Corner Radius: ${cornerRadiusDp.roundToInt()}dp")
                Slider(value = cornerRadiusDp, onValueChange = { cornerRadiusDp = it }, valueRange = 0f..36f)

                Text("Background Blur: ${blurDp.roundToInt()}dp")
                Slider(value = blurDp, onValueChange = { blurDp = it }, valueRange = 0f..24f)

                Text("Animation Style", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SLIDE", "SCALE", "FADE").forEach { anim ->
                        OutlinedButton(
                            onClick = { animationStyle = anim },
                            border = BorderStroke(1.dp, if (animationStyle == anim) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        ) { Text(anim) }
                    }
                }

                Text("Layout Mode", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("GRID", "LIST").forEach { lm ->
                        OutlinedButton(
                            onClick = { layoutMode = lm },
                            border = BorderStroke(1.dp, if (layoutMode == lm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        ) { Text(lm) }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) { Text("Background Image") }
                    if (bgImagePath != null) {
                        TextButton(onClick = { bgImagePath = null }) { Text("Clear") }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Edge swipe activation zone")
                    Switch(checked = edgeEnabled, onCheckedChange = { edgeEnabled = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Show visible handle bar")
                    Switch(checked = showHandle, onCheckedChange = { showHandle = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Keep pinned on app launch")
                    Switch(checked = pinned, onCheckedChange = { pinned = it })
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    val saved = (initial ?: SidebarConfigEntity(title = title.trim())).copy(
                        title = title.trim(),
                        side = side,
                        sourceType = sourceType,
                        targetTagId = if (sourceType == "TAG") targetTagId else null,
                        targetCategoryId = if (sourceType == "CATEGORY") targetCategoryId else null,
                        targetRuleId = if (sourceType == "RULE") targetRuleId else null,
                        customPackagesCsv = if (sourceType == "CUSTOM_COLLECTION") customPackages.joinToString(",") else "",
                        widthFraction = widthFraction,
                        heightFraction = heightFraction,
                        cornerRadiusDp = cornerRadiusDp,
                        backgroundBlurDp = blurDp,
                        layoutMode = layoutMode,
                        iconSizeDp = iconSizeDp,
                        animationStyle = animationStyle,
                        edgeZoneEnabled = edgeEnabled,
                        showHandle = showHandle,
                        pinned = pinned,
                        backgroundImagePath = bgImagePath,
                    )
                    onSave(saved)
                },
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

@Composable
private fun WindowEditorDialog(
    initial: LauncherWindowEntity?,
    onImportImage: suspend (Uri) -> String?,
    onSave: (LauncherWindowEntity) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var fullScreen by remember { mutableStateOf(initial?.fullScreen ?: false) }
    var widthFraction by remember { mutableFloatStateOf(initial?.widthFraction ?: 0.88f) }
    var heightFraction by remember { mutableFloatStateOf(initial?.heightFraction ?: 0.68f) }
    var alignment by remember { mutableStateOf(initial?.alignment ?: "CENTER") }
    var cornerRadiusDp by remember { mutableFloatStateOf(initial?.cornerRadiusDp ?: 28f) }
    var animStyle by remember { mutableStateOf(initial?.animationStyle ?: "SCALE") }
    var bgImagePath by remember { mutableStateOf(initial?.backgroundImagePath) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri)
                if (path != null) bgImagePath = path
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New Window" else "Edit Window") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Window Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Full Screen Window")
                    Switch(checked = fullScreen, onCheckedChange = { fullScreen = it })
                }
                if (!fullScreen) {
                    Text("Width: ${(widthFraction * 100).roundToInt()}%")
                    Slider(value = widthFraction, onValueChange = { widthFraction = it }, valueRange = 0.5f..0.98f)
                    Text("Height: ${(heightFraction * 100).roundToInt()}%")
                    Slider(value = heightFraction, onValueChange = { heightFraction = it }, valueRange = 0.35f..0.95f)
                }
                Text("Alignment", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("CENTER", "TOP", "BOTTOM").forEach { al ->
                        FilterChip(selected = alignment == al, onClick = { alignment = al }, label = { Text(al) })
                    }
                }
                Text("Corner Radius: ${cornerRadiusDp.roundToInt()}dp")
                Slider(value = cornerRadiusDp, onValueChange = { cornerRadiusDp = it }, valueRange = 0f..36f)
                Text("Animation", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("SCALE", "SLIDE", "FADE").forEach { a ->
                        FilterChip(selected = animStyle == a, onClick = { animStyle = a }, label = { Text(a) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) { Text("Window Background Image") }
                    if (bgImagePath != null) {
                        TextButton(onClick = { bgImagePath = null }) { Text("Clear") }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    val saved = (initial ?: LauncherWindowEntity(title = title.trim())).copy(
                        title = title.trim(),
                        fullScreen = fullScreen,
                        widthFraction = widthFraction,
                        heightFraction = heightFraction,
                        alignment = alignment,
                        cornerRadiusDp = cornerRadiusDp,
                        animationStyle = animStyle,
                        backgroundImagePath = bgImagePath,
                    )
                    onSave(saved)
                },
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, "Delete window", tint = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}
