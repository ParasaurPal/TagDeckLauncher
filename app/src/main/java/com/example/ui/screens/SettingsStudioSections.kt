package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.CategoryEntity
import com.example.data.db.CustomFontEntity
import com.example.data.db.ThemeProfileEntity
import com.example.data.prefs.LauncherSettings
import com.example.ui.components.CategoryBadgeIcon
import com.example.ui.components.ColorSwatchRow
import com.example.ui.components.LocalCustomFonts
import com.example.ui.components.LocalReadabilityConfig
import com.example.ui.components.ReadableLauncherText
import com.example.ui.components.ReadabilityConfig
import com.example.ui.components.StudioDarkSurfaceSwatches
import com.example.ui.components.StudioNeutralAndAccentSwatches
import com.example.ui.state.LauncherUiState
import kotlin.math.roundToInt

@Composable
fun CategoryIconsSettingsSection(
    categories: List<CategoryEntity>,
    matchedEntries: List<SettingEntrySpec>,
    onEditCategory: (CategoryEntity) -> Unit,
    onOpenFullTagManager: () -> Unit,
) {
    var filterQuery by remember { mutableStateOf("") }
    val ordered = remember(categories, filterQuery) {
        val q = filterQuery.trim().lowercase()
        categories
            .sortedWith(compareBy<CategoryEntity> { it.sortOrder }.thenBy { it.name })
            .filter { q.isEmpty() || it.name.lowercase().contains(q) || it.categoryType.lowercase().contains(q) }
    }

    SettingsCard(
        title = "Category Icons & Open-State Badges",
        icon = Icons.Rounded.Category,
        matchedEntries = matchedEntries,
    ) {
        Text(
            text = "Pick a closed icon and an alternate open icon (built-in icon, emoji, or custom SVG/PNG) for every App Drawer category. Shows in both the drawer category bar and header.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = filterQuery,
            onValueChange = { filterQuery = it },
            placeholder = { Text("Filter categories...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_category_icons_filter"),
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ordered.forEach { cat ->
                val hasOpenOverride = !cat.openIconKey.isNullOrBlank() ||
                    !cat.openCustomIconPath.isNullOrBlank() ||
                    !cat.openCustomEmoji.isNullOrBlank()
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                    border = BorderStroke(
                        1.dp,
                        if (hasOpenOverride) Color(cat.colorHex).copy(alpha = 0.55f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditCategory(cat) }
                        .testTag("settings_category_icon_row_${cat.categoryId}"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CategoryBadgeIcon(cat, size = 32.dp, isOpen = false)
                        Text(
                            text = "↔",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        CategoryBadgeIcon(cat, size = 32.dp, isOpen = true)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = if (hasOpenOverride) "Custom open icon active" else "Same icon when open • Tap to customize",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasOpenOverride) Color(cat.colorHex) else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Edit ${cat.name} icon",
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onOpenFullTagManager,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("settings_category_icons_open_manager"),
        ) {
            Text("Open Full Tags & Categories Manager")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomFontsReadabilitySection(
    state: LauncherUiState,
    matchedEntries: List<SettingEntrySpec>,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onImportCustomFont: (Uri) -> Unit,
    onApplyCustomFont: (CustomFontEntity?) -> Unit,
    onDeleteCustomFont: (CustomFontEntity) -> Unit,
) {
    val s = state.settings
    val fontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) onImportCustomFont(uri)
    }

    SettingsCard(
        title = "Custom Fonts & Readability Studio",
        icon = Icons.Rounded.FontDownload,
        matchedEntries = matchedEntries,
    ) {
        Text(
            text = "Import your own .ttf or .otf fonts right from device storage and tune launcher text contrast, pill backdrops, outlines, spacing, and shadows.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    fontPicker.launch(
                        arrayOf(
                            "font/ttf",
                            "font/otf",
                            "application/x-font-ttf",
                            "application/x-font-otf",
                            "application/octet-stream",
                            "*/*",
                        )
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("upload_custom_font_button"),
            ) {
                Icon(Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Upload .TTF / .OTF")
            }
            if (s.fontStyle == "CUSTOM") {
                OutlinedButton(
                    onClick = { onApplyCustomFont(null) },
                    modifier = Modifier.testTag("revert_builtin_font_button"),
                ) {
                    Text("Use Built-in")
                }
            }
        }

        if (state.customFonts.isNotEmpty()) {
            Text("Uploaded Fonts (${state.customFonts.size})", style = MaterialTheme.typography.labelLarge)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                state.customFonts.forEach { font ->
                    val selected = s.fontStyle == "CUSTOM" && s.customFontId == font.fontId
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        border = BorderStroke(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onApplyCustomFont(font) }
                            .testTag("custom_font_chip_${font.fontId}"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(font.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = if (selected) "Active globally" else "Tap to apply globally (or pick per-widget)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(
                                onClick = { onDeleteCustomFont(font) },
                                modifier = Modifier.testTag("delete_custom_font_${font.fontId}"),
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete ${font.name}", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Live wallpaper-backed readability preview
        CompositionLocalProvider(
            LocalReadabilityConfig provides ReadabilityConfig.fromSettings(s),
            LocalCustomFonts provides state.customFonts,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0EA5E9), Color(0xFFF43F5E), Color(0xFFF8FAFC))
                        )
                    )
                    .padding(14.dp)
                    .testTag("readability_live_preview"),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ReadableLauncherText(
                        text = "TagDeck • Readable Label Preview",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                    ReadableLauncherText(
                        text = "09:42 • Calendar, Notes & Battery 84%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                    )
                }
            }
        }

        ToggleRow("High-Readability Boost Mode", s.highReadabilityMode) { v ->
            onUpdateSettings { it.copy(highReadabilityMode = v) }
        }
        ToggleRow("Bold App & Widget Labels", s.boldLabelsEnabled) { v ->
            onUpdateSettings { it.copy(boldLabelsEnabled = v) }
        }
        ToggleRow("Auto-Contrast Label Colors", s.autoContrastText) { v ->
            onUpdateSettings { it.copy(autoContrastText = v) }
        }
        ToggleRow("Label Pill Backdrop", s.labelBackdropEnabled) { v ->
            onUpdateSettings { it.copy(labelBackdropEnabled = v) }
        }
        if (s.labelBackdropEnabled) {
            Text("Backdrop Opacity: ${(s.labelBackdropAlpha * 100).roundToInt()}%")
            Slider(
                value = s.labelBackdropAlpha,
                onValueChange = { v -> onUpdateSettings { it.copy(labelBackdropAlpha = v) } },
                valueRange = 0.2f..1f,
            )
            Text("Backdrop Color", style = MaterialTheme.typography.labelLarge)
            ColorSwatchRow(
                selectedColorHex = s.labelBackdropColorHex,
                colors = StudioDarkSurfaceSwatches + StudioNeutralAndAccentSwatches,
                onSelect = { c -> onUpdateSettings { it.copy(labelBackdropColorHex = c) } },
                tagPrefix = "readability_backdrop_color",
            )
        }

        ToggleRow("Crisp Text Stroke Outline", s.textOutlineEnabled) { v ->
            onUpdateSettings { it.copy(textOutlineEnabled = v) }
        }
        if (s.textOutlineEnabled) {
            Text("Outline Width: ${"%.1f".format(s.textOutlineWidthDp)}dp")
            Slider(
                value = s.textOutlineWidthDp,
                onValueChange = { v -> onUpdateSettings { it.copy(textOutlineWidthDp = v) } },
                valueRange = 0.5f..4.5f,
            )
            Text("Outline Color", style = MaterialTheme.typography.labelLarge)
            ColorSwatchRow(
                selectedColorHex = s.textOutlineColorHex,
                colors = StudioNeutralAndAccentSwatches,
                onSelect = { c -> onUpdateSettings { it.copy(textOutlineColorHex = c) } },
                tagPrefix = "readability_outline_color",
            )
        }

        ToggleRow("Drop Shadow Under Text", s.textShadowEnabled) { v ->
            onUpdateSettings { it.copy(textShadowEnabled = v) }
        }
        if (s.textShadowEnabled) {
            Text("Shadow Softness: ${s.textShadowRadiusDp.roundToInt()}dp")
            Slider(
                value = s.textShadowRadiusDp,
                onValueChange = { v -> onUpdateSettings { it.copy(textShadowRadiusDp = v) } },
                valueRange = 1f..14f,
            )
        }

        Text("Letter Spacing: ${"%.2f".format(s.letterSpacingEm)}em")
        Slider(
            value = s.letterSpacingEm,
            onValueChange = { v -> onUpdateSettings { it.copy(letterSpacingEm = v) } },
            valueRange = -0.02f..0.18f,
        )

        Text("Line Height Multiplier: ${"%.2f".format(s.lineHeightMultiplier)}x")
        Slider(
            value = s.lineHeightMultiplier,
            onValueChange = { v -> onUpdateSettings { it.copy(lineHeightMultiplier = v) } },
            valueRange = 1.0f..1.65f,
        )

        Text("Minimum Label Size Floor: ${s.minLabelFontSp.roundToInt()}sp")
        Slider(
            value = s.minLabelFontSp,
            onValueChange = { v -> onUpdateSettings { it.copy(minLabelFontSp = v) } },
            valueRange = 9f..18f,
        )

        Text("Max Label Lines: ${s.labelMaxLines}", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 2, 3).forEach { lines ->
                FilterChip(
                    selected = s.labelMaxLines == lines,
                    onClick = { onUpdateSettings { it.copy(labelMaxLines = lines) } },
                    label = { Text("$lines line${if (lines == 1) "" else "s"}") },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeProfilesAndSurfacePaletteControls(
    state: LauncherUiState,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onSaveThemeProfile: (String) -> Unit,
    onApplyThemeProfile: (ThemeProfileEntity) -> Unit,
    onDeleteThemeProfile: (Long) -> Unit,
) {
    val s = state.settings
    var showSaveDialog by remember { mutableStateOf(false) }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Custom Surface, Card & Outline Palette", style = MaterialTheme.typography.labelLarge)
        if (s.customSurfaceColorHex != null || s.customCardColorHex != null || s.customOutlineColorHex != null) {
            TextButton(onClick = {
                onUpdateSettings {
                    it.copy(customSurfaceColorHex = null, customCardColorHex = null, customOutlineColorHex = null)
                }
            }) { Text("Auto") }
        }
    }
    Text("Custom Surface Color", style = MaterialTheme.typography.bodySmall)
    ColorSwatchRow(
        selectedColorHex = s.customSurfaceColorHex ?: 0xFF0D1424L,
        colors = StudioDarkSurfaceSwatches,
        onSelect = { c -> onUpdateSettings { it.copy(customSurfaceColorHex = c) } },
        tagPrefix = "settings_custom_surface",
    )
    Text("Custom Card Color", style = MaterialTheme.typography.bodySmall)
    ColorSwatchRow(
        selectedColorHex = s.customCardColorHex ?: 0xFF151E34L,
        colors = StudioDarkSurfaceSwatches,
        onSelect = { c -> onUpdateSettings { it.copy(customCardColorHex = c) } },
        tagPrefix = "settings_custom_card",
    )
    Text("Custom Outline Color", style = MaterialTheme.typography.bodySmall)
    ColorSwatchRow(
        selectedColorHex = s.customOutlineColorHex ?: 0xFF334155L,
        colors = StudioNeutralAndAccentSwatches,
        onSelect = { c -> onUpdateSettings { it.copy(customOutlineColorHex = c) } },
        tagPrefix = "settings_custom_outline",
    )

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Saved Theme Profiles (${state.themeProfiles.size})", style = MaterialTheme.typography.labelLarge)
        OutlinedButton(
            onClick = { showSaveDialog = true },
            modifier = Modifier.testTag("save_theme_profile_button"),
        ) {
            Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Save Current")
        }
    }
    if (state.themeProfiles.isNotEmpty()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            state.themeProfiles.forEach { profile ->
                FilterChip(
                    selected = false,
                    onClick = { onApplyThemeProfile(profile) },
                    label = { Text(profile.name) },
                    trailingIcon = {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Delete ${profile.name}",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onDeleteThemeProfile(profile.profileId) },
                        )
                    },
                    modifier = Modifier.testTag("theme_profile_${profile.profileId}"),
                )
            }
        }
    }

    if (showSaveDialog) {
        var profileName by remember { mutableStateOf("My Studio Theme") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Theme Profile") },
            text = {
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Profile Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(onClick = {
                    onSaveThemeProfile(profileName)
                    showSaveDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WallpaperFxStudioControls(
    settings: LauncherSettings,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    Text("Atmosphere, Parallax & Color Grading", style = MaterialTheme.typography.labelLarge)

    Text("Ambient Canvas Effect", style = MaterialTheme.typography.bodySmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("NONE", "AURORA", "GRID", "DOTS", "BOKEH").forEach { fx ->
            FilterChip(
                selected = settings.ambientEffectType == fx,
                onClick = { onUpdateSettings { it.copy(ambientEffectType = fx) } },
                label = { Text(fx) },
                modifier = Modifier.testTag("settings_ambient_$fx"),
            )
        }
    }

    if (settings.wallpaperType == "CUSTOM_IMAGE" && !settings.wallpaperCustomPath.isNullOrBlank()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        Text("Custom Wallpaper Crop & Position", style = MaterialTheme.typography.labelLarge)
        Text("Drag to reposition • pinch to zoom", style = MaterialTheme.typography.bodySmall)
        ImageCropEditor(
            imagePath = settings.wallpaperCustomPath,
            cropScale = settings.wallpaperCropScale,
            cropX = settings.wallpaperCropX,
            cropY = settings.wallpaperCropY,
            onCropChange = { scale, x, y -> onUpdateSettings { it.copy(wallpaperCropScale = scale, wallpaperCropX = x, wallpaperCropY = y) } },
        )
        Text("Zoom: ${String.format("%.1f", settings.wallpaperCropScale)}x")
        Slider(
            value = settings.wallpaperCropScale,
            onValueChange = { v -> onUpdateSettings { it.copy(wallpaperCropScale = v) } },
            valueRange = 1f..4f,
        )
        Text("Horizontal: ${(settings.wallpaperCropX * 100).roundToInt()}%")
        Slider(
            value = settings.wallpaperCropX,
            onValueChange = { v -> onUpdateSettings { it.copy(wallpaperCropX = v) } },
            valueRange = -1f..1f,
        )
        Text("Vertical: ${(settings.wallpaperCropY * 100).roundToInt()}%")
        Slider(
            value = settings.wallpaperCropY,
            onValueChange = { v -> onUpdateSettings { it.copy(wallpaperCropY = v) } },
            valueRange = -1f..1f,
        )
        TextButton(onClick = { onUpdateSettings { it.copy(wallpaperCropScale = 1f, wallpaperCropX = 0f, wallpaperCropY = 0f) } }) {
            Text("Reset Wallpaper Crop")
        }
    }

    Text("Page Parallax Depth: ${(settings.wallpaperParallax * 100).roundToInt()}%")
    Slider(
        value = settings.wallpaperParallax,
        onValueChange = { v -> onUpdateSettings { it.copy(wallpaperParallax = v) } },
        valueRange = 0f..1f,
    )

    Text("Vignette Framing: ${(settings.wallpaperVignette * 100).roundToInt()}%")
    Slider(
        value = settings.wallpaperVignette,
        onValueChange = { v -> onUpdateSettings { it.copy(wallpaperVignette = v) } },
        valueRange = 0f..0.85f,
    )

    Text("Saturation: ${"%.2f".format(settings.wallpaperSaturation)}x")
    Slider(
        value = settings.wallpaperSaturation,
        onValueChange = { v -> onUpdateSettings { it.copy(wallpaperSaturation = v) } },
        valueRange = 0f..1.8f,
    )

    Text("Contrast: ${"%.2f".format(settings.wallpaperContrast)}x")
    Slider(
        value = settings.wallpaperContrast,
        onValueChange = { v -> onUpdateSettings { it.copy(wallpaperContrast = v) } },
        valueRange = 0.5f..1.8f,
    )

    Text("Brightness Offset: ${"%.2f".format(settings.wallpaperBrightness)}")
    Slider(
        value = settings.wallpaperBrightness,
        onValueChange = { v -> onUpdateSettings { it.copy(wallpaperBrightness = v) } },
        valueRange = -0.35f..0.35f,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderAndChromeStudioControls(
    settings: LauncherSettings,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
) {
    ToggleRow("Custom HUD Status Strip (Time/Battery/Net)", settings.showCustomStatusStrip) { v ->
        onUpdateSettings { it.copy(showCustomStatusStrip = v) }
    }
    Text("Home Folder Cover Style", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(
            "GRID_2X2" to "2×2 Grid",
            "GRID_3X3" to "3×3 Grid",
            "STACK" to "Card Stack",
            "FAN" to "Fanned Arc",
        ).forEach { (key, label) ->
            FilterChip(
                selected = settings.folderCoverStyle == key,
                onClick = { onUpdateSettings { it.copy(folderCoverStyle = key) } },
                label = { Text(label) },
                modifier = Modifier.testTag("settings_folder_cover_$key"),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExtendedPagingAndIndicatorControls(
    settings: LauncherSettings,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
) {
    Text("Page Transition Style (11 Modes)", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(
            "SLIDE", "DEPTH", "FADE", "CUBE", "FLIP",
            "STACK", "ZOOM", "ACCORDION", "ROTATE", "PARALLAX", "WINDMILL",
        ).forEach { tr ->
            FilterChip(
                selected = settings.pageTransitionStyle == tr,
                onClick = { onUpdateSettings { it.copy(pageTransitionStyle = tr) } },
                label = { Text(tr) },
                modifier = Modifier.testTag("settings_transition_$tr"),
            )
        }
    }
    ToggleRow("Infinite Page Loop (when Widget Feed off)", settings.infinitePageLoop) { v ->
        onUpdateSettings { it.copy(infinitePageLoop = v) }
    }
    Text("Page Indicator Placement", style = MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("BOTTOM", "TOP").forEach { pos ->
            FilterChip(
                selected = settings.pageIndicatorPlacement == pos,
                onClick = { onUpdateSettings { it.copy(pageIndicatorPlacement = pos) } },
                label = { Text(pos) },
                modifier = Modifier.testTag("settings_indicator_pos_$pos"),
            )
        }
    }
}
