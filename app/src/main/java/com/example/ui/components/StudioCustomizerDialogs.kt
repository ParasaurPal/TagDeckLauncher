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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import com.example.data.db.HomePageEntity
import com.example.data.db.LauncherWindowEntity
import com.example.data.prefs.LauncherSettings
import com.example.ui.theme.CategoryBackgroundPalette
import com.example.ui.theme.TagPresetColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

val StudioNeutralAndAccentSwatches: List<Long> = listOf(
    0xFFFFFFFFL,
    0xFFF8FAFCL,
    0xFFCBD5E1L,
    0xFF000000L,
    0xFF090D16L,
    0xFF0F172AL,
) + TagPresetColors

val StudioDarkSurfaceSwatches: List<Long> = CategoryBackgroundPalette + listOf(
    0xFF0A1124L,
    0xFF1A1038L,
    0xFF000000L,
    0xFFF4F7FBL,
    0xFFFFFFFFL,
)

@Composable
fun ColorSwatchRow(
    selectedColorHex: Long?,
    colors: List<Long>,
    onSelect: (Long) -> Unit,
    allowClear: Boolean = false,
    clearColorHex: Long = 0x00000000L,
    onClear: (() -> Unit)? = null,
    tagPrefix: String = "swatch",
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (allowClear && onClear != null) {
            item {
                val isCleared = selectedColorHex == null || selectedColorHex == clearColorHex
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(
                        width = if (isCleared) 2.dp else 1.dp,
                        color = if (isCleared) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    ),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onClear)
                        .testTag("${tagPrefix}_clear"),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("0", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        items(colors) { c ->
            val isSelected = selectedColorHex == c
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(c))
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.55f),
                        shape = CircleShape,
                    )
                    .clickable { onSelect(c) }
                    .testTag("${tagPrefix}_$c"),
            )
        }
    }
}

@Composable
fun StudioQuickCustomizerDialog(
    currentPage: HomePageEntity,
    settings: LauncherSettings,
    onSavePage: (HomePageEntity) -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onImportImage: suspend (Uri, String, String?) -> String?,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var activeTab by remember { mutableStateOf("PAGE") }
    var workingPage by remember(currentPage.pageId, currentPage) { mutableStateOf(currentPage) }

    val portraitPagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri, "Wallpapers", null)
                if (path != null) {
                    val updated = workingPage.copy(backgroundType = "IMAGE", backgroundImagePath = path)
                    workingPage = updated
                    onSavePage(updated)
                }
            }
        }
    }
    val landscapePagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri, "Wallpapers", null)
                if (path != null) {
                    val updated = workingPage.copy(backgroundType = "IMAGE", landscapeBackgroundImagePath = path)
                    workingPage = updated
                    onSavePage(updated)
                }
            }
        }
    }
    val globalWallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri, "Wallpapers", null)
                if (path != null) {
                    onUpdateSettings { it.copy(wallpaperType = "CUSTOM_IMAGE", wallpaperCustomPath = path) }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Edit Mode Style Studio")
            }
        },
        text = {
            @OptIn(ExperimentalLayoutApi::class)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf(
                        "PAGE" to "Page Look",
                        "CANVAS" to "Grid & Canvas",
                        "THEME" to "Theme & Fonts",
                        "WALLPAPER" to "Wallpaper",
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = activeTab == key,
                            onClick = { activeTab = key },
                            label = { Text(label) },
                            modifier = Modifier.testTag("studio_style_tab_$key"),
                        )
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    when (activeTab) {
                        "PAGE" -> {
                            OutlinedTextField(
                                value = workingPage.title,
                                onValueChange = { workingPage = workingPage.copy(title = it) },
                                label = { Text("Page Title") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("studio_page_title_input"),
                            )
                            StudioToggle("Lock Page Objects", workingPage.isLocked) {
                                val next = workingPage.copy(isLocked = it)
                                workingPage = next
                                onSavePage(next)
                            }
                            StudioToggle("Hide Page from Swipe", workingPage.isHidden) {
                                val next = workingPage.copy(isHidden = it)
                                workingPage = next
                                onSavePage(next)
                            }
                            StudioToggle("Vertically Scrollable Canvas", workingPage.verticallyScrollable) {
                                val next = workingPage.copy(verticallyScrollable = it)
                                workingPage = next
                                onSavePage(next)
                            }

                            Text("Page Edge Padding: ${workingPage.paddingDp.roundToInt()}dp")
                            Slider(
                                value = workingPage.paddingDp,
                                onValueChange = { v ->
                                    val next = workingPage.copy(paddingDp = v)
                                    workingPage = next
                                    onSavePage(next)
                                },
                                valueRange = 0f..40f,
                                modifier = Modifier.testTag("studio_page_padding_slider"),
                            )

                            Text("Page Background Mode", style = MaterialTheme.typography.labelLarge)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                listOf("INHERIT", "TRANSPARENT", "SOLID", "GRADIENT", "IMAGE").forEach { mode ->
                                    FilterChip(
                                        selected = workingPage.backgroundType == mode,
                                        onClick = {
                                            val next = when (mode) {
                                                "SOLID" -> workingPage.copy(
                                                    backgroundType = mode,
                                                    backgroundColorHex = workingPage.backgroundColorHex ?: 0xFF0F172AL,
                                                )
                                                "GRADIENT" -> workingPage.copy(
                                                    backgroundType = mode,
                                                    backgroundColorHex = workingPage.backgroundColorHex ?: 0xFF0A1424L,
                                                    backgroundGradientEndHex = workingPage.backgroundGradientEndHex ?: 0xFF1E1032L,
                                                )
                                                else -> workingPage.copy(backgroundType = mode)
                                            }
                                            workingPage = next
                                            onSavePage(next)
                                        },
                                        label = { Text(mode) },
                                        modifier = Modifier.testTag("studio_page_bg_$mode"),
                                    )
                                }
                            }

                            if (workingPage.backgroundType == "SOLID" || workingPage.backgroundType == "GRADIENT") {
                                Text(
                                    if (workingPage.backgroundType == "GRADIENT") "Gradient Start Color" else "Solid Color",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                ColorSwatchRow(
                                    selectedColorHex = workingPage.backgroundColorHex,
                                    colors = StudioDarkSurfaceSwatches,
                                    onSelect = { c ->
                                        val next = workingPage.copy(backgroundColorHex = c)
                                        workingPage = next
                                        onSavePage(next)
                                    },
                                    tagPrefix = "studio_page_color1",
                                )
                            }
                            if (workingPage.backgroundType == "GRADIENT") {
                                Text("Gradient End Color", style = MaterialTheme.typography.labelLarge)
                                ColorSwatchRow(
                                    selectedColorHex = workingPage.backgroundGradientEndHex,
                                    colors = StudioDarkSurfaceSwatches,
                                    onSelect = { c ->
                                        val next = workingPage.copy(backgroundGradientEndHex = c)
                                        workingPage = next
                                        onSavePage(next)
                                    },
                                    tagPrefix = "studio_page_color2",
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    portraitPagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }) { Text("Portrait Img") }
                                OutlinedButton(onClick = {
                                    landscapePagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }) { Text("Landscape Img") }
                            }
                            if (workingPage.backgroundImagePath != null || workingPage.landscapeBackgroundImagePath != null) {
                                TextButton(onClick = {
                                    val next = workingPage.copy(backgroundImagePath = null, landscapeBackgroundImagePath = null)
                                    workingPage = next
                                    onSavePage(next)
                                }) { Text("Clear Page Images") }
                            }

                            Text("Page Blur: ${workingPage.backgroundBlurDp.roundToInt()}dp")
                            Slider(
                                value = workingPage.backgroundBlurDp,
                                onValueChange = { v ->
                                    val next = workingPage.copy(backgroundBlurDp = v)
                                    workingPage = next
                                    onSavePage(next)
                                },
                                valueRange = 0f..24f,
                            )

                            Text("Page Overlay Opacity: ${(workingPage.backgroundAlpha * 100).roundToInt()}%")
                            Slider(
                                value = workingPage.backgroundAlpha,
                                onValueChange = { v ->
                                    val next = workingPage.copy(backgroundAlpha = v)
                                    workingPage = next
                                    onSavePage(next)
                                },
                                valueRange = 0.05f..1f,
                            )
                        }

                        "CANVAS" -> {
                            StudioToggle("Grid Snap", settings.gridSnapEnabled) { v ->
                                onUpdateSettings { it.copy(gridSnapEnabled = v) }
                            }
                            Text("Grid Snap Step: ${settings.gridSnapStepDp}dp")
                            Slider(
                                value = settings.gridSnapStepDp.toFloat(),
                                onValueChange = { v -> onUpdateSettings { it.copy(gridSnapStepDp = v.roundToInt().coerceIn(8, 48)) } },
                                valueRange = 8f..48f,
                                modifier = Modifier.testTag("studio_grid_step_slider"),
                            )
                            StudioToggle("Show Edit Mode Grid Lines", settings.editGridVisible) { v ->
                                onUpdateSettings { it.copy(editGridVisible = v) }
                            }
                            if (settings.editGridVisible) {
                                Text("Edit Grid Opacity: ${(settings.editGridOpacity * 100).roundToInt()}%")
                                Slider(
                                    value = settings.editGridOpacity,
                                    onValueChange = { v -> onUpdateSettings { it.copy(editGridOpacity = v) } },
                                    valueRange = 0.02f..0.40f,
                                )
                                Text("Edit Grid Line Color", style = MaterialTheme.typography.labelLarge)
                                ColorSwatchRow(
                                    selectedColorHex = settings.editGridColorHex,
                                    colors = StudioNeutralAndAccentSwatches,
                                    onSelect = { c -> onUpdateSettings { it.copy(editGridColorHex = c) } },
                                    tagPrefix = "studio_grid_color",
                                )
                            }
                            StudioToggle("Magnetic Alignment Guides", settings.alignmentGuidesEnabled) { v ->
                                onUpdateSettings { it.copy(alignmentGuidesEnabled = v) }
                            }
                            if (settings.alignmentGuidesEnabled) {
                                Text("Alignment Guide Color", style = MaterialTheme.typography.labelLarge)
                                ColorSwatchRow(
                                    selectedColorHex = settings.alignmentGuideColorHex,
                                    colors = TagPresetColors,
                                    onSelect = { c -> onUpdateSettings { it.copy(alignmentGuideColorHex = c) } },
                                    tagPrefix = "studio_guide_color",
                                )
                            }

                            HorizontalDivider()
                            Text("Page Indicator Style", style = MaterialTheme.typography.labelLarge)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                listOf("DOTS", "PILL", "NUMBERS", "HIDDEN").forEach { st ->
                                    FilterChip(
                                        selected = settings.pageIndicatorStyle == st,
                                        onClick = { onUpdateSettings { it.copy(pageIndicatorStyle = st) } },
                                        label = { Text(st) },
                                        modifier = Modifier.testTag("studio_indicator_style_$st"),
                                    )
                                }
                            }
                            if (settings.pageIndicatorStyle != "HIDDEN") {
                                Text("Page Indicator Color", style = MaterialTheme.typography.labelLarge)
                                ColorSwatchRow(
                                    selectedColorHex = settings.pageIndicatorColorHex,
                                    colors = TagPresetColors,
                                    onSelect = { c -> onUpdateSettings { it.copy(pageIndicatorColorHex = c) } },
                                    tagPrefix = "studio_indicator_color",
                                )
                            }

                            Text("Page Swipe Transition", style = MaterialTheme.typography.labelLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("SLIDE", "DEPTH", "FADE").forEach { tr ->
                                    FilterChip(
                                        selected = settings.pageTransitionStyle == tr,
                                        onClick = { onUpdateSettings { it.copy(pageTransitionStyle = tr) } },
                                        label = { Text(tr) },
                                    )
                                }
                            }

                            HorizontalDivider()
                            StudioToggle("Show Edge Sidebar Handles", settings.edgeHandlesVisible) { v ->
                                onUpdateSettings { it.copy(edgeHandlesVisible = v) }
                            }
                            if (settings.edgeHandlesVisible) {
                                Text("Handle Opacity: ${(settings.edgeHandleOpacity * 100).roundToInt()}%")
                                Slider(
                                    value = settings.edgeHandleOpacity,
                                    onValueChange = { v -> onUpdateSettings { it.copy(edgeHandleOpacity = v) } },
                                    valueRange = 0.10f..1.0f,
                                )
                                Text("Handle Thickness: ${settings.edgeHandleWidthDp.roundToInt()}dp")
                                Slider(
                                    value = settings.edgeHandleWidthDp,
                                    onValueChange = { v -> onUpdateSettings { it.copy(edgeHandleWidthDp = v) } },
                                    valueRange = 2f..10f,
                                )
                                Text("Handle Length: ${settings.edgeHandleHeightDp.roundToInt()}dp")
                                Slider(
                                    value = settings.edgeHandleHeightDp,
                                    onValueChange = { v -> onUpdateSettings { it.copy(edgeHandleHeightDp = v) } },
                                    valueRange = 24f..140f,
                                )
                            }
                            StudioToggle("Show Labels on Home Shortcuts", settings.showHomeAppLabels) { v ->
                                onUpdateSettings { it.copy(showHomeAppLabels = v) }
                            }
                            StudioToggle("Lock Entire Home Layout", settings.layoutLocked) { v ->
                                onUpdateSettings { it.copy(layoutLocked = v) }
                            }
                        }

                        "THEME" -> {
                            Text("Theme Mode", style = MaterialTheme.typography.labelLarge)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                listOf("DARK", "LIGHT", "SYSTEM", "DYNAMIC").forEach { mode ->
                                    FilterChip(
                                        selected = settings.themeMode == mode,
                                        onClick = { onUpdateSettings { it.copy(themeMode = mode) } },
                                        label = { Text(mode) },
                                    )
                                }
                            }

                            Text("Font Style", style = MaterialTheme.typography.labelLarge)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                listOf(
                                    "DEFAULT" to "Default Mix",
                                    "SPACE_GROTESK" to "Space Grotesk",
                                    "PLUS_JAKARTA" to "Plus Jakarta",
                                    "JETBRAINS_MONO" to "JetBrains Mono",
                                    "SYSTEM" to "System",
                                ).forEach { (key, label) ->
                                    FilterChip(
                                        selected = settings.fontStyle == key,
                                        onClick = { onUpdateSettings { it.copy(fontStyle = key) } },
                                        label = { Text(label) },
                                        modifier = Modifier.testTag("studio_font_$key"),
                                    )
                                }
                            }

                            Text("Global Text Scale: ${"%.2f".format(settings.fontScale)}x")
                            Slider(
                                value = settings.fontScale,
                                onValueChange = { v -> onUpdateSettings { it.copy(fontScale = v) } },
                                valueRange = 0.80f..1.40f,
                            )

                            Text("Primary Accent Color", style = MaterialTheme.typography.labelLarge)
                            ColorSwatchRow(
                                selectedColorHex = settings.accentColorHex,
                                colors = TagPresetColors,
                                onSelect = { c -> onUpdateSettings { it.copy(accentColorHex = c) } },
                                tagPrefix = "studio_accent",
                            )
                            Text("Secondary Color", style = MaterialTheme.typography.labelLarge)
                            ColorSwatchRow(
                                selectedColorHex = settings.secondaryColorHex,
                                colors = TagPresetColors,
                                onSelect = { c -> onUpdateSettings { it.copy(secondaryColorHex = c) } },
                                tagPrefix = "studio_secondary",
                            )
                            Text("Tertiary Color", style = MaterialTheme.typography.labelLarge)
                            ColorSwatchRow(
                                selectedColorHex = settings.tertiaryColorHex,
                                colors = TagPresetColors,
                                onSelect = { c -> onUpdateSettings { it.copy(tertiaryColorHex = c) } },
                                tagPrefix = "studio_tertiary",
                            )

                            Text("Icon Shape", style = MaterialTheme.typography.labelLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("SQUIRCLE", "CIRCLE", "ROUNDED_SQUARE").forEach { shp ->
                                    FilterChip(
                                        selected = settings.globalIconShape == shp,
                                        onClick = { onUpdateSettings { it.copy(globalIconShape = shp) } },
                                        label = { Text(shp.replace('_', ' ')) },
                                    )
                                }
                            }

                            Text("Global Corner Radius: ${settings.globalCornerRadiusDp.roundToInt()}dp")
                            Slider(
                                value = settings.globalCornerRadiusDp,
                                onValueChange = { v -> onUpdateSettings { it.copy(globalCornerRadiusDp = v) } },
                                valueRange = 4f..32f,
                            )

                            Text("UI Surface Opacity: ${(settings.surfaceOpacity * 100).roundToInt()}%")
                            Slider(
                                value = settings.surfaceOpacity,
                                onValueChange = { v -> onUpdateSettings { it.copy(surfaceOpacity = v) } },
                                valueRange = 0.25f..1.0f,
                            )

                            Text("Global Border Width: ${"%.1f".format(settings.globalBorderWidthDp)}dp")
                            Slider(
                                value = settings.globalBorderWidthDp,
                                onValueChange = { v -> onUpdateSettings { it.copy(globalBorderWidthDp = v) } },
                                valueRange = 0f..4.0f,
                            )

                            Text("Animation Speed: ${"%.1f".format(settings.animationSpeedScale)}x")
                            Slider(
                                value = settings.animationSpeedScale,
                                onValueChange = { v -> onUpdateSettings { it.copy(animationSpeedScale = v) } },
                                valueRange = 0.2f..1.8f,
                            )

                            HorizontalDivider()
                            StudioToggle("White + Black Outline on Black Icons", settings.blackIconStyleEnabled) { v ->
                                onUpdateSettings { it.copy(blackIconStyleEnabled = v) }
                            }
                            if (settings.blackIconStyleEnabled) {
                                Text("Icon Outline Width: ${"%.1f".format(settings.blackIconOutlineWidthDp)}dp")
                                Slider(
                                    value = settings.blackIconOutlineWidthDp,
                                    onValueChange = { v -> onUpdateSettings { it.copy(blackIconOutlineWidthDp = v) } },
                                    valueRange = 0.5f..5.0f,
                                )
                            }
                        }

                        "WALLPAPER" -> {
                            Text("Wallpaper Mode", style = MaterialTheme.typography.labelLarge)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                listOf(
                                    "BUILTIN_PRISM" to "Prism",
                                    "BUILTIN_TOPO" to "Topo",
                                    "GRADIENT" to "Gradient",
                                    "SOLID" to "Solid",
                                    "CUSTOM_IMAGE" to "Custom Image",
                                ).forEach { (key, label) ->
                                    FilterChip(
                                        selected = settings.wallpaperType == key,
                                        onClick = { onUpdateSettings { it.copy(wallpaperType = key) } },
                                        label = { Text(label) },
                                        modifier = Modifier.testTag("studio_wallpaper_$key"),
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    globalWallpaperPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }) { Text("Import Wallpaper") }
                                if (settings.wallpaperCustomPath != null) {
                                    TextButton(onClick = {
                                        onUpdateSettings { it.copy(wallpaperCustomPath = null, wallpaperType = "BUILTIN_PRISM") }
                                    }) { Text("Clear") }
                                }
                            }

                            if (settings.wallpaperType == "GRADIENT") {
                                Text("Gradient Direction", style = MaterialTheme.typography.labelLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("VERTICAL", "HORIZONTAL", "DIAGONAL").forEach { dir ->
                                        FilterChip(
                                            selected = settings.wallpaperGradientDirection == dir,
                                            onClick = { onUpdateSettings { it.copy(wallpaperGradientDirection = dir) } },
                                            label = { Text(dir) },
                                        )
                                    }
                                }
                                Text("Gradient Start Color", style = MaterialTheme.typography.labelLarge)
                                ColorSwatchRow(
                                    selectedColorHex = settings.wallpaperGradientStartHex,
                                    colors = StudioDarkSurfaceSwatches,
                                    onSelect = { c -> onUpdateSettings { it.copy(wallpaperGradientStartHex = c) } },
                                    tagPrefix = "studio_wp_grad_start",
                                )
                                Text("Gradient End Color", style = MaterialTheme.typography.labelLarge)
                                ColorSwatchRow(
                                    selectedColorHex = settings.wallpaperGradientEndHex,
                                    colors = StudioDarkSurfaceSwatches,
                                    onSelect = { c -> onUpdateSettings { it.copy(wallpaperGradientEndHex = c) } },
                                    tagPrefix = "studio_wp_grad_end",
                                )
                            } else if (settings.wallpaperType == "SOLID") {
                                Text("Solid Wallpaper Color", style = MaterialTheme.typography.labelLarge)
                                ColorSwatchRow(
                                    selectedColorHex = settings.wallpaperSolidHex,
                                    colors = StudioDarkSurfaceSwatches,
                                    onSelect = { c -> onUpdateSettings { it.copy(wallpaperSolidHex = c) } },
                                    tagPrefix = "studio_wp_solid",
                                )
                            }

                            Text("Wallpaper Blur: ${settings.wallpaperBlurDp.roundToInt()}dp")
                            Slider(
                                value = settings.wallpaperBlurDp,
                                onValueChange = { v -> onUpdateSettings { it.copy(wallpaperBlurDp = v) } },
                                valueRange = 0f..24f,
                            )

                            Text("Wallpaper Dimming: ${(settings.wallpaperDimAlpha * 100).roundToInt()}%")
                            Slider(
                                value = settings.wallpaperDimAlpha,
                                onValueChange = { v -> onUpdateSettings { it.copy(wallpaperDimAlpha = v) } },
                                valueRange = 0f..0.85f,
                            )

                            Text("Dim Overlay Tint Color", style = MaterialTheme.typography.labelLarge)
                            ColorSwatchRow(
                                selectedColorHex = settings.wallpaperDimColorHex,
                                colors = StudioNeutralAndAccentSwatches,
                                onSelect = { c -> onUpdateSettings { it.copy(wallpaperDimColorHex = c) } },
                                tagPrefix = "studio_wp_dim_tint",
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanTitle = workingPage.title.trim().ifBlank { currentPage.title }
                    if (cleanTitle != currentPage.title) {
                        onSavePage(workingPage.copy(title = cleanTitle))
                    }
                    onDismiss()
                },
                modifier = Modifier.testTag("studio_style_done"),
            ) { Text("Done") }
        },
    )
}

@Composable
fun WindowStyleQuickDialog(
    window: LauncherWindowEntity,
    onSaveWindow: (LauncherWindowEntity) -> Unit,
    onImportImage: suspend (Uri, String, String?) -> String?,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var working by remember(window.windowId) { mutableStateOf(window) }

    val bgPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri, "Windows", null)
                if (path != null) {
                    working = working.copy(backgroundImagePath = path)
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Customize Window Style")
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
                OutlinedTextField(
                    value = working.title,
                    onValueChange = { working = working.copy(title = it) },
                    label = { Text("Window Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("window_style_title_input"),
                )
                StudioToggle("Full Screen Window", working.fullScreen) {
                    working = working.copy(fullScreen = it)
                }
                if (!working.fullScreen) {
                    Text("Width: ${(working.widthFraction * 100).roundToInt()}%")
                    Slider(
                        value = working.widthFraction,
                        onValueChange = { working = working.copy(widthFraction = it) },
                        valueRange = 0.45f..1.0f,
                    )
                    Text("Height: ${(working.heightFraction * 100).roundToInt()}%")
                    Slider(
                        value = working.heightFraction,
                        onValueChange = { working = working.copy(heightFraction = it) },
                        valueRange = 0.30f..1.0f,
                    )
                    Text("Corner Radius: ${working.cornerRadiusDp.roundToInt()}dp")
                    Slider(
                        value = working.cornerRadiusDp,
                        onValueChange = { working = working.copy(cornerRadiusDp = it) },
                        valueRange = 0f..44f,
                    )
                    Text("Alignment", style = MaterialTheme.typography.labelLarge)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        listOf("CENTER", "TOP", "BOTTOM", "LEFT", "RIGHT").forEach { align ->
                            FilterChip(
                                selected = working.alignment == align,
                                onClick = { working = working.copy(alignment = align) },
                                label = { Text(align) },
                            )
                        }
                    }
                }

                Text("Animation Style", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("SCALE", "SLIDE", "FADE").forEach { anim ->
                        FilterChip(
                            selected = working.animationStyle == anim,
                            onClick = { working = working.copy(animationStyle = anim) },
                            label = { Text(anim) },
                        )
                    }
                }

                Text("Window Blur: ${working.blurBehindDp.roundToInt()}dp")
                Slider(
                    value = working.blurBehindDp,
                    onValueChange = { working = working.copy(blurBehindDp = it) },
                    valueRange = 0f..24f,
                )

                Text("Background Color", style = MaterialTheme.typography.labelLarge)
                ColorSwatchRow(
                    selectedColorHex = working.backgroundColorHex,
                    colors = StudioDarkSurfaceSwatches,
                    onSelect = { c -> working = working.copy(backgroundColorHex = c) },
                    tagPrefix = "window_bg_color",
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        bgPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) { Text("Pick BG Image") }
                    if (working.backgroundImagePath != null) {
                        TextButton(onClick = { working = working.copy(backgroundImagePath = null) }) {
                            Text("Clear Image")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveWindow(working.copy(title = working.title.trim().ifBlank { window.title }))
                    onDismiss()
                },
                modifier = Modifier.testTag("window_style_save"),
            ) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun StudioToggle(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}
