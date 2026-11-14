package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FormatPaint
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.ThemeProfileEntity
import com.example.data.system.StarterTemplates
import com.example.ui.state.LauncherUiState
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun MultiSelectArrangeBar(
    selectedCount: Int,
    hasCopiedStyle: Boolean,
    onStackSelected: () -> Unit,
    onGroupSelected: () -> Unit,
    onUngroupSelected: () -> Unit,
    onAlign: (String) -> Unit,
    onDistribute: (String) -> Unit,
    onMatchSize: (String) -> Unit,
    onPasteStyle: () -> Unit,
    onLockSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("multi_select_arrange_bar"),
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "$selectedCount selected • Arrange",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(
                        onClick = onLockSelected,
                        leadingIcon = { Icon(Icons.Rounded.Lock, null, Modifier.size(15.dp)) },
                        label = { Text("Lock") },
                        modifier = Modifier.testTag("arrange_lock_btn"),
                    )
                    AssistChip(
                        onClick = onDeleteSelected,
                        leadingIcon = { Icon(Icons.Rounded.Delete, null, Modifier.size(15.dp)) },
                        label = { Text("Delete") },
                        modifier = Modifier.testTag("arrange_delete_btn"),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selectedCount >= 2) {
                    AssistChip(
                        onClick = onStackSelected,
                        leadingIcon = { Icon(Icons.Rounded.ViewAgenda, null, Modifier.size(15.dp)) },
                        label = { Text("Stack") },
                        modifier = Modifier.testTag("arrange_stack_btn"),
                    )
                    AssistChip(onClick = onGroupSelected, label = { Text("Group") }, modifier = Modifier.testTag("arrange_group_btn"))
                }
                AssistChip(onClick = onUngroupSelected, label = { Text("Ungroup") }, modifier = Modifier.testTag("arrange_ungroup_btn"))
                if (hasCopiedStyle) {
                    AssistChip(
                        onClick = onPasteStyle,
                        leadingIcon = { Icon(Icons.Rounded.FormatPaint, null, Modifier.size(15.dp)) },
                        label = { Text("Paste Style") },
                        modifier = Modifier.testTag("arrange_paste_style_btn"),
                    )
                }
                if (selectedCount >= 2) {
                    listOf(
                        "LEFT" to "Align L",
                        "CENTER_H" to "Center H",
                        "RIGHT" to "Align R",
                        "TOP" to "Align T",
                        "CENTER_V" to "Center V",
                        "BOTTOM" to "Align B",
                    ).forEach { (mode, label) ->
                        AssistChip(onClick = { onAlign(mode) }, label = { Text(label) })
                    }
                    AssistChip(onClick = { onMatchSize("WIDTH") }, label = { Text("Match W") })
                    AssistChip(onClick = { onMatchSize("HEIGHT") }, label = { Text("Match H") })
                }
                if (selectedCount >= 3) {
                    AssistChip(onClick = { onDistribute("HORIZONTAL") }, label = { Text("Dist H") })
                    AssistChip(onClick = { onDistribute("VERTICAL") }, label = { Text("Dist V") })
                }
            }
        }
    }
}

@Composable
fun LayerStackDrawer(
    currentPage: HomePageEntity,
    allElements: List<HomeElementEntity>,
    selectedElementId: Long?,
    onSelectElement: (HomeElementEntity) -> Unit,
    onInspectElement: (HomeElementEntity) -> Unit,
    onToggleVisibility: (HomeElementEntity) -> Unit,
    onToggleLock: (HomeElementEntity) -> Unit,
    onBringForward: (HomeElementEntity) -> Unit,
    onSendBackward: (HomeElementEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    val pinned = remember(allElements) {
        allElements.filter { it.ownerType == "PAGE" && it.pinnedToAllPages && it.parentContainerId == null }
            .sortedByDescending { it.zIndex }
    }
    val pageTop = remember(allElements, currentPage.pageId) {
        allElements.filter {
            it.ownerType == "PAGE" && it.pageId == currentPage.pageId && !it.pinnedToAllPages && it.parentContainerId == null
        }.sortedByDescending { it.zIndex }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Layer Stack (${currentPage.title})")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("layer_stack_drawer"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (pinned.isNotEmpty()) {
                    Text("Pinned Across All Pages (${pinned.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    pinned.forEach { el ->
                        LayerRowItem(
                            element = el,
                            isSelected = el.elementId == selectedElementId,
                            indented = false,
                            onSelect = { onSelectElement(el) },
                            onInspect = { onInspectElement(el) },
                            onToggleVisibility = { onToggleVisibility(el) },
                            onToggleLock = { onToggleLock(el) },
                            onBringForward = { onBringForward(el) },
                            onSendBackward = { onSendBackward(el) },
                        )
                    }
                    HorizontalDivider()
                }
                Text("Page Layers (${pageTop.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                if (pageTop.isEmpty()) {
                    Text("No objects on this page yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    pageTop.forEach { el ->
                        LayerRowItem(
                            element = el,
                            isSelected = el.elementId == selectedElementId,
                            indented = false,
                            onSelect = { onSelectElement(el) },
                            onInspect = { onInspectElement(el) },
                            onToggleVisibility = { onToggleVisibility(el) },
                            onToggleLock = { onToggleLock(el) },
                            onBringForward = { onBringForward(el) },
                            onSendBackward = { onSendBackward(el) },
                        )
                        val children = allElements.filter { it.parentContainerId == el.elementId }.sortedBy { it.zIndex }
                        children.forEach { child ->
                            LayerRowItem(
                                element = child,
                                isSelected = child.elementId == selectedElementId,
                                indented = true,
                                onSelect = { onSelectElement(child) },
                                onInspect = { onInspectElement(child) },
                                onToggleVisibility = { onToggleVisibility(child) },
                                onToggleLock = { onToggleLock(child) },
                                onBringForward = { onBringForward(child) },
                                onSendBackward = { onSendBackward(child) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
    )
}

@Composable
private fun LayerRowItem(
    element: HomeElementEntity,
    isSelected: Boolean,
    indented: Boolean,
    onSelect: () -> Unit,
    onInspect: () -> Unit,
    onToggleVisibility: () -> Unit,
    onToggleLock: () -> Unit,
    onBringForward: () -> Unit,
    onSendBackward: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (indented) 18.dp else 0.dp)
            .clickable { onSelect() }
            .testTag("layer_row_${element.elementId}"),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).clickable { onInspect() }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (element.pinnedToAllPages) {
                        Icon(Icons.Rounded.PushPin, contentDescription = "Pinned", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                    }
                    Text(
                        text = element.label.ifBlank { element.type },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "${element.type} • Z:${element.zIndex}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onToggleVisibility, modifier = Modifier.size(34.dp)) {
                Icon(
                    if (element.visible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    contentDescription = "Toggle visibility",
                    modifier = Modifier.size(17.dp),
                )
            }
            IconButton(onClick = onToggleLock, modifier = Modifier.size(34.dp)) {
                Icon(
                    if (element.locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                    contentDescription = "Toggle lock",
                    modifier = Modifier.size(17.dp),
                )
            }
            IconButton(onClick = onBringForward, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move up", modifier = Modifier.size(17.dp))
            }
            IconButton(onClick = onSendBackward, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move down", modifier = Modifier.size(17.dp))
            }
        }
    }
}

data class MacroStepDraft(
    val actionType: String = "OPEN_DRAWER",
    val delayMs: Long = 0L,
    val targetUrl: String = "",
    val targetPackage: String = "",
    val targetTagId: Long? = null,
    val targetCategoryId: Long? = null,
    val targetWindowId: Long? = null,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MacroStepBuilderDialog(
    initialMacroJson: String?,
    state: LauncherUiState,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var steps by remember(initialMacroJson) {
        val list = mutableListOf<MacroStepDraft>()
        if (!initialMacroJson.isNullOrBlank()) {
            try {
                val arr = JSONArray(initialMacroJson)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list += MacroStepDraft(
                        actionType = o.optString("actionType", "OPEN_DRAWER"),
                        delayMs = o.optLong("delayMs", 0L),
                        targetUrl = o.optString("targetUrl", ""),
                        targetPackage = o.optString("targetPackage", ""),
                        targetTagId = if (o.has("targetTagId") && !o.isNull("targetTagId")) o.optLong("targetTagId") else null,
                        targetCategoryId = if (o.has("targetCategoryId") && !o.isNull("targetCategoryId")) o.optLong("targetCategoryId") else null,
                        targetWindowId = if (o.has("targetWindowId") && !o.isNull("targetWindowId")) o.optLong("targetWindowId") else null,
                    )
                }
            } catch (_: Exception) {}
        }
        mutableStateOf(list.toList())
    }

    var newAction by remember { mutableStateOf("TOGGLE_OBJECT") }
    var newTarget by remember { mutableStateOf("") }
    var newDelayMs by remember { mutableLongStateOf(0L) }

    val macroActions = listOf(
        "SHOW_OBJECT" to "Show Object/Group",
        "HIDE_OBJECT" to "Hide Object/Group",
        "TOGGLE_OBJECT" to "Toggle Object/Group",
        "JUMP_PAGE" to "Jump Page Index",
        "STACK_NEXT" to "Next Stack Widget",
        "STACK_PREV" to "Prev Stack Widget",
        "OPEN_DRAWER" to "Open Drawer",
        "OPEN_SEARCH" to "Open Search",
        "OPEN_TAG" to "Open Tag",
        "OPEN_WINDOW" to "Open Window",
        "TOGGLE_TORCH" to "Toggle Torch",
        "CYCLE_RINGER" to "Cycle Ringer",
        "OPEN_SYSTEM_PANEL" to "System Panel",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Multi-Step Action Macro Builder") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Steps (${steps.size})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                if (steps.isEmpty()) {
                    Text("No steps yet. Add actions below to chain them together.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    steps.forEachIndexed { idx, step ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${idx + 1}. ${step.actionType}", fontWeight = FontWeight.SemiBold)
                                    val detail = buildString {
                                        if (step.delayMs > 0) append("Wait ${step.delayMs}ms ")
                                        if (step.targetUrl.isNotBlank()) append("Target: ${step.targetUrl}")
                                        step.targetTagId?.let { append("Tag #$it") }
                                        step.targetWindowId?.let { append("Win #$it") }
                                    }
                                    if (detail.isNotBlank()) {
                                        Text(detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (idx > 0) {
                                    IconButton(onClick = {
                                        val m = steps.toMutableList()
                                        val item = m.removeAt(idx)
                                        m.add(idx - 1, item)
                                        steps = m
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Rounded.ArrowUpward, "Move up", modifier = Modifier.size(16.dp))
                                    }
                                }
                                IconButton(onClick = {
                                    steps = steps.filterIndexed { i, _ -> i != idx }
                                }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Rounded.Close, "Remove", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()
                Text("Add Step", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    macroActions.forEach { (k, lbl) ->
                        FilterChip(selected = newAction == k, onClick = { newAction = k }, label = { Text(lbl) })
                    }
                }
                if (newAction in listOf("SHOW_OBJECT", "HIDE_OBJECT", "TOGGLE_OBJECT", "JUMP_PAGE", "STACK_NEXT", "STACK_PREV", "OPEN_SYSTEM_PANEL")) {
                    OutlinedTextField(
                        value = newTarget,
                        onValueChange = { newTarget = it },
                        label = {
                            Text(
                                when (newAction) {
                                    "JUMP_PAGE" -> "Page index (0, 1, 2...)"
                                    "STACK_NEXT", "STACK_PREV" -> "Widget Stack ID"
                                    "OPEN_SYSTEM_PANEL" -> "WIFI_PANEL / BLUETOOTH_PANEL / VOLUME_PANEL"
                                    else -> "Element ID or Visibility Group name"
                                }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text("Delay Before Step: ${newDelayMs}ms", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0L, 150L, 300L, 600L, 1000L).forEach { ms ->
                        FilterChip(selected = newDelayMs == ms, onClick = { newDelayMs = ms }, label = { Text("${ms}ms") })
                    }
                }
                OutlinedButton(
                    onClick = {
                        steps = steps + MacroStepDraft(
                            actionType = newAction,
                            delayMs = newDelayMs,
                            targetUrl = newTarget.trim(),
                            targetTagId = if (newAction == "OPEN_TAG") state.allTags.firstOrNull()?.tagId else null,
                            targetWindowId = if (newAction == "OPEN_WINDOW") state.windows.firstOrNull()?.windowId else null,
                        )
                        newTarget = ""
                    },
                    modifier = Modifier.fillMaxWidth().testTag("macro_add_step_btn"),
                ) {
                    Icon(Icons.Rounded.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Append Step")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val arr = JSONArray()
                    steps.forEach { s ->
                        arr.put(JSONObject().apply {
                            put("actionType", s.actionType)
                            put("delayMs", s.delayMs)
                            if (s.targetUrl.isNotBlank()) put("targetUrl", s.targetUrl)
                            if (s.targetPackage.isNotBlank()) put("targetPackage", s.targetPackage)
                            s.targetTagId?.let { put("targetTagId", it) }
                            s.targetCategoryId?.let { put("targetCategoryId", it) }
                            s.targetWindowId?.let { put("targetWindowId", it) }
                        })
                    }
                    onSave(arr.toString())
                },
                modifier = Modifier.testTag("macro_save_btn"),
            ) { Text("Save Macro") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun TemplateAndThemeGalleryDialog(
    themeProfiles: List<ThemeProfileEntity>,
    onApplyStarterTemplate: (String) -> Unit,
    onSaveThemeProfile: (String) -> Unit,
    onApplyThemeProfile: (ThemeProfileEntity) -> Unit,
    onDeleteThemeProfile: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var tab by remember { mutableStateOf("TEMPLATES") }
    var profileName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Templates & Theme Profiles")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = tab == "TEMPLATES",
                        onClick = { tab = "TEMPLATES" },
                        label = { Text("Starter Templates") },
                        modifier = Modifier.testTag("gallery_tab_templates"),
                    )
                    FilterChip(
                        selected = tab == "PROFILES",
                        onClick = { tab = "PROFILES" },
                        label = { Text("Theme Profiles (${themeProfiles.size})") },
                        modifier = Modifier.testTag("gallery_tab_profiles"),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (tab == "TEMPLATES") {
                        Text(
                            "Applying a starter template automatically saves your current layout as a snapshot so you can restore anytime.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        StarterTemplates.catalog.forEach { tpl ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, Color(tpl.accentHex).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().testTag("template_card_${tpl.id}"),
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(tpl.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Surface(shape = RoundedCornerShape(8.dp), color = Color(tpl.accentHex).copy(alpha = 0.2f)) {
                                            Text(tpl.badge, style = MaterialTheme.typography.labelSmall, color = Color(tpl.accentHex), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                        }
                                    }
                                    Text(tpl.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            tpl.previewColors.forEach { hex ->
                                                Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(Color(hex)))
                                            }
                                        }
                                        Button(
                                            onClick = {
                                                onApplyStarterTemplate(tpl.id)
                                                onDismiss()
                                            },
                                            modifier = Modifier.testTag("apply_template_${tpl.id}"),
                                        ) { Text("Apply") }
                                    }
                                }
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = profileName,
                                onValueChange = { profileName = it },
                                label = { Text("Save Current Theme As") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            Button(
                                enabled = profileName.isNotBlank(),
                                onClick = {
                                    onSaveThemeProfile(profileName.trim())
                                    profileName = ""
                                },
                            ) { Text("Save") }
                        }
                        if (themeProfiles.isEmpty()) {
                            Text("No saved theme profiles yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            themeProfiles.forEach { profile ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(profile.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                        TextButton(onClick = { onApplyThemeProfile(profile) }) { Text("Apply") }
                                        IconButton(onClick = { onDeleteThemeProfile(profile.profileId) }) {
                                            Icon(Icons.Rounded.Delete, "Delete profile", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
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
