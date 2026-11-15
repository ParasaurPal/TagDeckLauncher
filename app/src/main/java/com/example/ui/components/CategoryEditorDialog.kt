package com.example.ui.components

import android.net.Uri
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.unit.dp
import com.example.data.db.CategoryEntity
import com.example.ui.theme.CategoryBackgroundPalette
import com.example.ui.theme.TagPresetColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CategoryEditorDialog(
    initialCategory: CategoryEntity?,
    onImportImage: suspend (Uri) -> String?,
    onSave: (CategoryEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var name by remember(initialCategory?.categoryId) { mutableStateOf(initialCategory?.name ?: "") }
    var iconKey by remember(initialCategory?.categoryId) { mutableStateOf(initialCategory?.iconKey ?: "tag") }
    var customIconPath by remember(initialCategory?.categoryId) { mutableStateOf(initialCategory?.customIconPath) }
    var customEmoji by remember(initialCategory?.categoryId) { mutableStateOf(initialCategory?.customEmoji ?: "") }
    var colorHex by remember(initialCategory?.categoryId) {
        mutableLongStateOf(initialCategory?.colorHex ?: TagPresetColors.first())
    }
    var iconSizeDp by remember(initialCategory?.categoryId) { mutableIntStateOf(initialCategory?.iconSizeDp ?: 24) }
    var openIconStateEnabled by remember(initialCategory?.categoryId) { mutableStateOf(initialCategory?.iconOpenStateEnabled ?: false) }
    var openIconKey by remember(initialCategory?.categoryId) { mutableStateOf(initialCategory?.openIconKey ?: initialCategory?.iconKey ?: "tag") }
    var openCustomIconPath by remember(initialCategory?.categoryId) { mutableStateOf(initialCategory?.openCustomIconPath) }
    var openCustomEmoji by remember(initialCategory?.categoryId) { mutableStateOf(initialCategory?.openCustomEmoji ?: "") }
    var openColorHex by remember(initialCategory?.categoryId) { mutableLongStateOf(initialCategory?.openColorHex ?: initialCategory?.colorHex ?: TagPresetColors.first()) }

    var bgType by remember(initialCategory?.categoryId) {
        mutableStateOf(initialCategory?.backgroundType ?: "INHERIT")
    }
    var bgColorHex by remember(initialCategory?.categoryId) {
        val stored = initialCategory?.backgroundColorHex
        val initialBg = if (initialCategory?.backgroundType != "INHERIT" && stored != null) {
            stored
        } else {
            CategoryBackgroundPalette.first()
        }
        mutableLongStateOf(initialBg)
    }
    var bgGradientEndHex by remember(initialCategory?.categoryId) {
        mutableLongStateOf(
            initialCategory?.backgroundGradientEndHex ?: CategoryBackgroundPalette[2]
        )
    }
    var bgImagePath by remember(initialCategory?.categoryId) {
        mutableStateOf(initialCategory?.backgroundImagePath)
    }
    var bgBlurDp by remember(initialCategory?.categoryId) {
        mutableFloatStateOf(initialCategory?.backgroundBlurDp ?: 0f)
    }
    var bgAlpha by remember(initialCategory?.categoryId) {
        mutableFloatStateOf(initialCategory?.backgroundAlpha ?: 1f)
    }
    var showInMainBar by remember(initialCategory?.categoryId) {
        mutableStateOf(initialCategory?.showInMainBar ?: true)
    }

    val iconPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri)
                if (path != null) {
                    customIconPath = path
                    customEmoji = ""
                }
            }
        }
    }
    val iconFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri)
                if (path != null) {
                    customIconPath = path
                    customEmoji = ""
                }
            }
        }
    }
    val openIconFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val path = onImportImage(uri)
            if (path != null) { openCustomIconPath = path; openCustomEmoji = "" }
        }
    }
    val bgPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri)
                if (path != null) {
                    bgImagePath = path
                    bgType = "IMAGE"
                }
            }
        }
    }
    val bgFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri)
                if (path != null) {
                    bgImagePath = path
                    bgType = "IMAGE"
                }
            }
        }
    }

    val savedBackgroundColorHex = if (bgType == "INHERIT") {
        (0x22000000L or (colorHex and 0x00FFFFFFL))
    } else {
        bgColorHex
    }

    val preview = (initialCategory ?: CategoryEntity(name = name.ifBlank { "Custom" }, categoryType = "CUSTOM", isCustom = true)).copy(
        name = name.ifBlank { "Custom" },
        iconKey = iconKey,
        customIconPath = customIconPath,
        customEmoji = customEmoji.takeIf { it.isNotBlank() },
        colorHex = colorHex,
        backgroundColorHex = savedBackgroundColorHex,
        backgroundType = bgType,
        backgroundGradientEndHex = if (bgType == "GRADIENT") bgGradientEndHex else null,
        backgroundImagePath = if (bgType == "IMAGE") bgImagePath else null,
        backgroundBlurDp = if (bgType == "IMAGE") bgBlurDp else 0f,
        backgroundAlpha = bgAlpha,
        showInMainBar = showInMainBar,
        iconSizeDp = iconSizeDp,
        iconOpenStateEnabled = openIconStateEnabled,
        openIconKey = openIconKey,
        openCustomIconPath = openCustomIconPath,
        openCustomEmoji = openCustomEmoji.takeIf { it.isNotBlank() },
        openColorHex = if (openIconStateEnabled) openColorHex else null,
    )

    val bgTypeOptions = listOf(
        "INHERIT" to "Inherit",
        "SOLID" to "Solid",
        "GRADIENT" to "Gradient",
        "IMAGE" to "Image",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("category_editor_dialog"),
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
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Live preview strip showing dock badge + active drawer/sidebar background
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp),
                ) {
                    Box {
                        SurfaceBackgroundLayer(
                            bgType = if (bgType == "INHERIT") "SOLID" else bgType,
                            colorHex = if (bgType == "INHERIT") CategoryBackgroundPalette.first() else bgColorHex,
                            gradientEndHex = if (bgType == "GRADIENT") bgGradientEndHex else null,
                            imagePath = if (bgType == "IMAGE") bgImagePath else null,
                            blurDp = if (bgType == "IMAGE") bgBlurDp else 0f,
                            alpha = bgAlpha,
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CategoryBadgeIcon(preview, size = 38.dp)
                            Column {
                                Text(
                                    text = preview.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                                Text(
                                    text = if (bgType == "INHERIT") {
                                        "Using global drawer background"
                                    } else {
                                        "Custom $bgType drawer & sidebar background"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(colorHex),
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (initialCategory?.isSystemDockTab == true) "Dock Tab Label" else "Category Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input"),
                )
                OutlinedTextField(
                    value = customEmoji,
                    onValueChange = { customEmoji = it.take(4) },
                    label = { Text("Custom Emoji Badge (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_emoji_input"),
                )

                Text("Accent Color", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TagPresetColors) { c ->
                        val selected = c == colorHex
                        ColorSwatchCircle(
                            colorHex = c,
                            selected = selected,
                            onClick = { colorHex = c },
                        )
                    }
                }

                Text("Icon or Custom PNG/SVG", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            iconPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Gallery")
                    }
                    OutlinedButton(
                        onClick = {
                            iconFilePicker.launch(arrayOf("image/*", "image/svg+xml"))
                        },
                    ) {
                        Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
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
                            modifier = Modifier
                                .testTag("category_icon_${opt.key}")
                                .clickable {
                                    iconKey = opt.key
                                    customIconPath = null
                                    customEmoji = ""
                                },
                        ) {
                            Icon(opt.icon, contentDescription = opt.label, modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                    }
                }
                Text("Icon Size: ${iconSizeDp}dp", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = iconSizeDp.toFloat(),
                    onValueChange = { iconSizeDp = it.roundToInt() },
                    valueRange = 18f..42f,
                )

                if (initialCategory?.isSystemDockTab != true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show in main category bar", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Turn off to hide this category without deleting it.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = showInMainBar,
                            onCheckedChange = { showInMainBar = it },
                        )
                    }
                }

                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Open-State Category Icon", style = MaterialTheme.typography.labelLarge)
                        Text("Use a different badge while this category is open.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = openIconStateEnabled, onCheckedChange = { openIconStateEnabled = it })
                }
                if (openIconStateEnabled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { openIconFilePicker.launch(arrayOf("image/*", "image/svg+xml")) }) { Text("Choose open icon") }
                        if (openCustomIconPath != null) TextButton(onClick = { openCustomIconPath = null }) { Text("Clear image") }
                    }
                    OutlinedTextField(value = openCustomEmoji, onValueChange = { openCustomEmoji = it.take(4); if (it.isNotBlank()) openCustomIconPath = null }, label = { Text("Open-state emoji (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("Open-state icon", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(TagIconCatalog) { opt ->
                            val selected = openCustomIconPath == null && openCustomEmoji.isBlank() && openIconKey == opt.key
                            Surface(shape = RoundedCornerShape(10.dp), color = if (selected) Color(openColorHex).copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant, border = BorderStroke(1.dp, if (selected) Color(openColorHex) else Color.Transparent), modifier = Modifier.clickable { openIconKey = opt.key; openCustomIconPath = null; openCustomEmoji = "" }) {
                                Icon(opt.icon, contentDescription = opt.label, modifier = Modifier.padding(8.dp).size(20.dp))
                            }
                        }
                    }
                    Text("Open-state accent", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(TagPresetColors) { color -> ColorSwatchCircle(colorHex = color, selected = color == openColorHex, onClick = { openColorHex = color }) } }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CategoryBadgeIcon(preview, size = 36.dp, isOpen = true)
                        Text("Preview while category is open", style = MaterialTheme.typography.labelSmall)
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Drawer & Sidebar Background", style = MaterialTheme.typography.labelLarge)
                    if (bgType != "INHERIT") {
                        TextButton(
                            onClick = {
                                bgType = "INHERIT"
                                bgImagePath = null
                                bgBlurDp = 0f
                            },
                            modifier = Modifier.testTag("category_bg_reset_button"),
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Inherit")
                        }
                    }
                }

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    bgTypeOptions.forEachIndexed { idx, (key, label) ->
                        SegmentedButton(
                            selected = bgType == key,
                            onClick = { bgType = key },
                            shape = SegmentedButtonDefaults.itemShape(idx, bgTypeOptions.size),
                            modifier = Modifier.testTag("category_bg_type_$key"),
                        ) {
                            Text(label, maxLines = 1)
                        }
                    }
                }

                if (bgType != "INHERIT") {
                    Text("Background Opacity: ${(bgAlpha * 100).roundToInt()}%", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = bgAlpha,
                        onValueChange = { bgAlpha = it },
                        valueRange = 0.05f..1f,
                        modifier = Modifier.testTag("category_bg_opacity_slider"),
                    )
                    Text(
                        text = when (bgType) {
                            "GRADIENT" -> "Gradient Start Color"
                            "IMAGE" -> "Image Overlay Tint"
                            else -> "Background Color"
                        },
                        style = MaterialTheme.typography.labelMedium,
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CategoryBackgroundPalette + TagPresetColors) { c ->
                            ColorSwatchCircle(
                                colorHex = c,
                                selected = c == bgColorHex,
                                onClick = { bgColorHex = c },
                            )
                        }
                    }
                }

                if (bgType == "GRADIENT") {
                    Text("Gradient End Color", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CategoryBackgroundPalette + TagPresetColors) { c ->
                            ColorSwatchCircle(
                                colorHex = c,
                                selected = c == bgGradientEndHex,
                                onClick = { bgGradientEndHex = c },
                            )
                        }
                    }
                }

                if (bgType == "IMAGE") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                bgPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        ) {
                            Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Pick Image")
                        }
                        OutlinedButton(
                            onClick = {
                                bgFilePicker.launch(arrayOf("image/*", "image/svg+xml"))
                            },
                        ) {
                            Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("File/SVG")
                        }
                        if (bgImagePath != null) {
                            TextButton(onClick = { bgImagePath = null }) { Text("Clear") }
                        }
                    }
                    Text("Background Blur: ${bgBlurDp.roundToInt()}dp", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = bgBlurDp,
                        onValueChange = { bgBlurDp = it },
                        valueRange = 0f..24f,
                    )
                }
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
                        backgroundColorHex = savedBackgroundColorHex,
                        backgroundType = bgType,
                        backgroundGradientEndHex = if (bgType == "GRADIENT") bgGradientEndHex else null,
                        backgroundImagePath = if (bgType == "IMAGE") bgImagePath else null,
                        backgroundBlurDp = if (bgType == "IMAGE") bgBlurDp else 0f,
                        backgroundAlpha = bgAlpha,
                        showInMainBar = showInMainBar,
                        iconSizeDp = iconSizeDp,
                        iconOpenStateEnabled = openIconStateEnabled,
                        openIconKey = openIconKey,
                        openCustomIconPath = openCustomIconPath,
                        openCustomEmoji = openCustomEmoji.takeIf { it.isNotBlank() },
                        openColorHex = if (openIconStateEnabled) openColorHex else null,
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

@Composable
private fun ColorSwatchCircle(
    colorHex: Long,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = Color(colorHex)
    val iconStyle = LocalIconStyleConfig.current
    val wouldBeBlack = (color.red * 0.299f + color.green * 0.587f + color.blue * 0.114f) > 0.55f
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.25f),
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            if (wouldBeBlack && iconStyle.enabled) {
                OutlinedVectorIcon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    fillColor = Color(iconStyle.fillColorHex),
                    outlineColor = Color(iconStyle.outlineColorHex),
                    outlineWidthDp = (iconStyle.outlineWidthDp * 0.55f).dp,
                    modifier = Modifier.size(16.dp),
                )
            } else {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = if (wouldBeBlack) Color.Black else Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
