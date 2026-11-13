package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.db.BackgroundPresetEntity
import com.example.data.db.GestureBindingEntity
import com.example.data.db.ImageResourceEntity
import com.example.data.prefs.LauncherSettings
import com.example.ui.components.BackgroundPresetDialog
import com.example.ui.components.ImageLibraryDialog
import com.example.ui.state.LauncherUiState
import com.example.ui.state.LauncherRoute
import com.example.ui.theme.TagPresetColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val GestureLabels = listOf(
    "SINGLE_TAP" to "Single Tap",
    "DOUBLE_TAP" to "Double Tap",
    "LONG_PRESS" to "Long Press",
    "SWIPE_UP" to "Swipe Up",
    "SWIPE_DOWN" to "Swipe Down",
    "SWIPE_LEFT" to "Swipe Left",
    "SWIPE_RIGHT" to "Swipe Right",
    "TWO_FINGER_SWIPE_UP" to "Two-Finger Swipe Up",
    "TWO_FINGER_SWIPE_DOWN" to "Two-Finger Swipe Down",
)

private val ActionOptions = listOf(
    "NONE" to "None",
    "OPEN_DRAWER" to "Open App Drawer",
    "OPEN_SEARCH" to "Open Universal Search",
    "OPEN_TAG_MANAGER" to "Open Tag Hub",
    "OPEN_TAG" to "Open Specific Tag Sidebar",
    "OPEN_CATEGORY" to "Open Play Category Sidebar",
    "OPEN_SIDEBAR" to "Open Configured Sidebar",
    "OPEN_WINDOW" to "Open Floating Window",
    "OPEN_RULE" to "Open Dynamic Rule",
    "OPEN_APP" to "Launch Specific App",
    "NEXT_PAGE" to "Next Home Page",
    "PREV_PAGE" to "Previous Home Page",
    "TOGGLE_EDIT_MODE" to "Toggle Layout Studio",
    "OPEN_SETTINGS" to "Open Settings",
)

@Composable
fun SettingsScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onNavigate: (LauncherRoute) -> Unit,
    onRequestDefaultLauncher: () -> Unit,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onSaveGesture: (GestureBindingEntity) -> Unit,
    onImportImage: suspend (Uri, String, String?) -> String?,
    onRenameImageResource: (ImageResourceEntity, String, String) -> Unit,
    onDeleteImageResource: (ImageResourceEntity) -> Unit,
    onSaveBackgroundPreset: (BackgroundPresetEntity) -> Unit,
    onDeleteBackgroundPreset: (Long) -> Unit,
    onExportBackupJson: suspend () -> String,
    onImportBackupJson: (String) -> Unit,
    onShowMessage: (String) -> Unit,
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val s = state.settings
    var editingGesture by remember { mutableStateOf<GestureBindingEntity?>(null) }
    var showImageLibrary by remember { mutableStateOf(false) }
    var showBackgroundPresets by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        onUpdateSettings { it.copy(notificationsBadgeHint = granted) }
        onShowMessage(if (granted) "Notification badges enabled" else "Notification permission denied")
    }

    val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri, "Wallpapers", null)
                if (path != null) {
                    onUpdateSettings { it.copy(wallpaperType = "CUSTOM_IMAGE", wallpaperCustomPath = path) }
                    onShowMessage("Wallpaper updated")
                }
            }
        }
    }

    val drawerBgPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportImage(uri, "Drawers", null)
                if (path != null) {
                    onUpdateSettings { it.copy(allAppsBgType = "IMAGE", allAppsBgImagePath = path) }
                    onShowMessage("App drawer background updated")
                }
            }
        }
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = onExportBackupJson()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                    onShowMessage("Backup exported successfully")
                } catch (_: Exception) {
                    onShowMessage("Failed to write backup file")
                }
            }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    if (!text.isNullOrBlank()) {
                        onImportBackupJson(text)
                    }
                } catch (_: Exception) {
                    onShowMessage("Could not read backup file")
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Text("Launcher Settings", style = MaterialTheme.typography.headlineMedium)
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Direct access to every editable launcher manager.
            item {
                SettingsCard(title = "Manage Your Launcher", icon = Icons.Rounded.Tune) {
                    Text(
                        "Open the editors for your tags, app icons and visibility, and independent sidebars.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onNavigate(LauncherRoute.Tags) }, modifier = Modifier.weight(1f)) {
                            Text("Tags & Categories")
                        }
                        OutlinedButton(onClick = { onNavigate(LauncherRoute.Apps) }, modifier = Modifier.weight(1f)) {
                            Text("App Icons")
                        }
                    }
                    OutlinedButton(
                        onClick = { onNavigate(LauncherRoute.Sidebars) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Sidebars & Floating Windows")
                    }
                }
            }

            // Default Home Launcher Card
            item {
                SettingsCard(title = "Default Home Application", icon = Icons.Rounded.Home) {
                    if (state.isDefaultLauncher) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("TagDeck is active as your default Home launcher.")
                        }
                    } else {
                        Text(
                            "Select TagDeck as your device's Home app so pressing Home always returns here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onRequestDefaultLauncher, modifier = Modifier.testTag("set_default_home_button")) {
                            Text("Set as Default Launcher")
                        }
                    }
                }
            }

            // Image Library & Background Presets
            item {
                SettingsCard(title = "Media Library & Background Presets", icon = Icons.Rounded.Image) {
                    Text(
                        "Centralized non-destructive library (${state.imageResources.size} images, ${state.backgroundPresets.size} presets). Originals stay intact.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showImageLibrary = true },
                            modifier = Modifier.weight(1f).testTag("open_image_library_button"),
                        ) {
                            Text("Image Library")
                        }
                        OutlinedButton(
                            onClick = { showBackgroundPresets = true },
                            modifier = Modifier.weight(1f).testTag("open_bg_presets_button"),
                        ) {
                            Text("BG Presets")
                        }
                    }
                }
            }

            // Visual Theme & Appearance
            item {
                SettingsCard(title = "Theme & Visual Style", icon = Icons.Rounded.Palette) {
                    Text("Theme Mode", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("DARK", "LIGHT", "SYSTEM", "DYNAMIC").forEach { mode ->
                            FilterChip(
                                selected = s.themeMode == mode,
                                onClick = { onUpdateSettings { it.copy(themeMode = mode) } },
                                label = { Text(mode) },
                            )
                        }
                    }

                    Text("Accent Color", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(TagPresetColors) { c ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .border(if (c == s.accentColorHex) 2.dp else 0.dp, Color.White, CircleShape)
                                    .clickable { onUpdateSettings { it.copy(accentColorHex = c) } },
                            )
                        }
                    }

                    Text("Icon Shape", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SQUIRCLE", "CIRCLE", "ROUNDED_SQUARE").forEach { shp ->
                            FilterChip(
                                selected = s.globalIconShape == shp,
                                onClick = { onUpdateSettings { it.copy(globalIconShape = shp) } },
                                label = { Text(shp.replace('_', ' ')) },
                            )
                        }
                    }

                    Text("Global Corner Radius: ${s.globalCornerRadiusDp.roundToInt()}dp")
                    Slider(
                        value = s.globalCornerRadiusDp,
                        onValueChange = { v -> onUpdateSettings { it.copy(globalCornerRadiusDp = v) } },
                        valueRange = 4f..32f,
                    )

                    Text("Animation Speed: ${"%.1f".format(s.animationSpeedScale)}x")
                    Slider(
                        value = s.animationSpeedScale,
                        onValueChange = { v -> onUpdateSettings { it.copy(animationSpeedScale = v) } },
                        valueRange = 0.2f..1.8f,
                    )
                }
            }

            // Wallpaper & Atmosphere
            item {
                SettingsCard(title = "Wallpaper & Atmosphere", icon = Icons.Rounded.Wallpaper) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            "BUILTIN_PRISM" to "Prism",
                            "BUILTIN_TOPO" to "Topo",
                            "GRADIENT" to "Gradient",
                            "SOLID" to "Solid",
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = s.wallpaperType == key,
                                onClick = { onUpdateSettings { it.copy(wallpaperType = key) } },
                                label = { Text(label) },
                            )
                        }
                    }
                    OutlinedButton(onClick = {
                        wallpaperPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Text("Import Custom Wallpaper Image")
                    }
                    Text("Wallpaper Blur: ${s.wallpaperBlurDp.roundToInt()}dp")
                    Slider(
                        value = s.wallpaperBlurDp,
                        onValueChange = { v -> onUpdateSettings { it.copy(wallpaperBlurDp = v) } },
                        valueRange = 0f..24f,
                    )
                    Text("Wallpaper Dim: ${(s.wallpaperDimAlpha * 100).roundToInt()}%")
                    Slider(
                        value = s.wallpaperDimAlpha,
                        onValueChange = { v -> onUpdateSettings { it.copy(wallpaperDimAlpha = v) } },
                        valueRange = 0f..0.8f,
                    )
                }
            }

            // Universal All-Apps Sidebar
            item {
                SettingsCard(title = "Universal All-Apps Sidebar", icon = Icons.Rounded.Apps) {
                    Text("Opening Side", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("RIGHT", "LEFT", "BOTTOM").forEach { edge ->
                            FilterChip(
                                selected = s.allAppsSidebarEdge == edge,
                                onClick = { onUpdateSettings { it.copy(allAppsSidebarEdge = edge) } },
                                label = { Text(edge) },
                            )
                        }
                    }

                    Text("Sidebar Width: ${(s.allAppsSidebarWidthFraction * 100).roundToInt()}%")
                    Slider(
                        value = s.allAppsSidebarWidthFraction,
                        onValueChange = { v -> onUpdateSettings { it.copy(allAppsSidebarWidthFraction = v) } },
                        valueRange = 0.65f..1.0f,
                    )

                    Text("Background Style", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SOLID", "GRADIENT", "IMAGE").forEach { bg ->
                            FilterChip(
                                selected = s.allAppsBgType == bg,
                                onClick = { onUpdateSettings { it.copy(allAppsBgType = bg) } },
                                label = { Text(bg) },
                            )
                        }
                    }
                    OutlinedButton(onClick = {
                        drawerBgPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) { Text("Set Drawer Wallpaper Image") }

                    Text("Drawer Blur: ${s.allAppsBgBlurDp.roundToInt()}dp")
                    Slider(
                        value = s.allAppsBgBlurDp,
                        onValueChange = { v -> onUpdateSettings { it.copy(allAppsBgBlurDp = v) } },
                        valueRange = 0f..24f,
                    )

                    Text("Grid Columns: ${s.drawerColumns}")
                    Slider(
                        value = s.drawerColumns.toFloat(),
                        onValueChange = { v -> onUpdateSettings { it.copy(drawerColumns = v.roundToInt()) } },
                        valueRange = 3f..6f,
                        steps = 2,
                    )
                    Text("Icon Size: ${s.drawerIconSizeDp}dp")
                    Slider(
                        value = s.drawerIconSizeDp.toFloat(),
                        onValueChange = { v -> onUpdateSettings { it.copy(drawerIconSizeDp = v.roundToInt()) } },
                        valueRange = 40f..72f,
                    )
                    Text("Label Size: ${s.drawerLabelSizeSp}sp")
                    Slider(
                        value = s.drawerLabelSizeSp.toFloat(),
                        onValueChange = { v -> onUpdateSettings { it.copy(drawerLabelSizeSp = v.roundToInt()) } },
                        valueRange = 9f..16f,
                    )
                }
            }

            // Niagara Wave Alphabet Navigation
            item {
                SettingsCard(title = "Niagara Wave Alphabet Index", icon = Icons.Rounded.SortByAlpha) {
                    ToggleRow("Show Alphabet Index Rail", s.niagaraIndexVisible) { v ->
                        onUpdateSettings { it.copy(niagaraIndexVisible = v) }
                    }
                    ToggleRow("Niagara Wave Magnification", s.niagaraWaveEnabled) { v ->
                        onUpdateSettings { it.copy(niagaraWaveEnabled = v) }
                    }
                    ToggleRow("Haptic Ticks on Scrub", s.niagaraHapticFeedback) { v ->
                        onUpdateSettings { it.copy(niagaraHapticFeedback = v) }
                    }

                    Text("Rail Edge", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("RIGHT", "LEFT").forEach { edge ->
                            FilterChip(
                                selected = s.niagaraEdge == edge,
                                onClick = { onUpdateSettings { it.copy(niagaraEdge = edge) } },
                                label = { Text(edge) },
                            )
                        }
                    }

                    Text("Wave Intensity: ${"%.1f".format(s.niagaraWaveIntensity)}x")
                    Slider(
                        value = s.niagaraWaveIntensity,
                        onValueChange = { v -> onUpdateSettings { it.copy(niagaraWaveIntensity = v) } },
                        valueRange = 0.2f..1.8f,
                    )
                    Text("Selected Letter Size: ${s.niagaraSelectedFontSp.roundToInt()}sp")
                    Slider(
                        value = s.niagaraSelectedFontSp,
                        onValueChange = { v -> onUpdateSettings { it.copy(niagaraSelectedFontSp = v) } },
                        valueRange = 16f..34f,
                    )
                }
            }

            // Home Builder, System Bars & Notifications
            item {
                SettingsCard(title = "Home Studio & System UI", icon = Icons.Rounded.GridOn) {
                    ToggleRow("Show Bottom Dock Bar", s.showDockBar) { v ->
                        onUpdateSettings { it.copy(showDockBar = v) }
                    }
                    ToggleRow("Show Quick Tag Strip", s.showTagStripOnHome) { v ->
                        onUpdateSettings { it.copy(showTagStripOnHome = v) }
                    }
                    ToggleRow("Grid Snap", s.gridSnapEnabled) { v ->
                        onUpdateSettings { it.copy(gridSnapEnabled = v) }
                    }
                    ToggleRow("Magnetic Alignment Guides", s.alignmentGuidesEnabled) { v ->
                        onUpdateSettings { it.copy(alignmentGuidesEnabled = v) }
                    }
                    ToggleRow("Lock Entire Home Layout", s.layoutLocked) { v ->
                        onUpdateSettings { it.copy(layoutLocked = v) }
                    }
                    ToggleRow("Show Android Status Bar", s.showStatusBar) { v ->
                        onUpdateSettings { it.copy(showStatusBar = v) }
                    }
                    ToggleRow("Show System Navigation Bar", s.showNavBar) { v ->
                        onUpdateSettings { it.copy(showNavBar = v) }
                    }
                    ToggleRow("Haptic Feedback", s.hapticFeedbackEnabled) { v ->
                        onUpdateSettings { it.copy(hapticFeedbackEnabled = v) }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Notifications, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Notification Hints", style = MaterialTheme.typography.bodyLarge)
                        }
                        Switch(
                            checked = s.notificationsBadgeHint,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        onUpdateSettings { it.copy(notificationsBadgeHint = true) }
                                    } else {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    onUpdateSettings { it.copy(notificationsBadgeHint = enabled) }
                                }
                            },
                        )
                    }

                    Text("Page Transition Style", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SLIDE", "DEPTH", "FADE").forEach { tr ->
                            FilterChip(
                                selected = s.pageTransitionStyle == tr,
                                onClick = { onUpdateSettings { it.copy(pageTransitionStyle = tr) } },
                                label = { Text(tr) },
                            )
                        }
                    }
                }
            }

            // Gestures
            item {
                SettingsCard(title = "Gestures & Shortcuts", icon = Icons.Rounded.Gesture) {
                    GestureLabels.forEach { (key, label) ->
                        val binding = state.gestures[key] ?: GestureBindingEntity(key, "NONE")
                        val actionLabel = ActionOptions.find { it.first == binding.actionType }?.second ?: binding.actionType
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editingGesture = binding }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            }

            // Backup & Restore
            item {
                SettingsCard(title = "Portable Local Backup & Restore", icon = Icons.Rounded.Backup) {
                    Text(
                        "Export or restore all layouts, windows, tags, categories, manual overrides, images, sidebars, widgets, and preferences.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { exportBackupLauncher.launch("tagdeck_backup.json") },
                            modifier = Modifier.weight(1f).testTag("export_backup_button"),
                        ) {
                            Icon(Icons.Rounded.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export JSON")
                        }
                        OutlinedButton(
                            onClick = { importBackupLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier.weight(1f).testTag("restore_backup_button"),
                        ) {
                            Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Restore JSON")
                        }
                    }
                }
            }
        }
    }

    if (showImageLibrary) {
        ImageLibraryDialog(
            resources = state.imageResources,
            onImportUri = onImportImage,
            onSelectPath = null,
            onRenameResource = onRenameImageResource,
            onDeleteResource = onDeleteImageResource,
            onDismiss = { showImageLibrary = false },
        )
    }

    if (showBackgroundPresets) {
        BackgroundPresetDialog(
            presets = state.backgroundPresets,
            onApplyPreset = { preset ->
                onUpdateSettings {
                    it.copy(
                        wallpaperType = preset.backgroundType,
                        wallpaperSolidHex = preset.colorHex,
                        wallpaperGradientStartHex = preset.colorHex,
                        wallpaperGradientEndHex = preset.gradientEndHex,
                        wallpaperCustomPath = preset.imagePath,
                        wallpaperBlurDp = preset.blurDp,
                    )
                }
                showBackgroundPresets = false
                onShowMessage("Applied preset \"${preset.name}\"")
            },
            onSaveCurrentAsPreset = { presetName ->
                onSaveBackgroundPreset(
                    BackgroundPresetEntity(
                        name = presetName,
                        ownerScope = "GLOBAL",
                        backgroundType = s.wallpaperType,
                        colorHex = s.wallpaperGradientStartHex,
                        gradientEndHex = s.wallpaperGradientEndHex,
                        imagePath = s.wallpaperCustomPath,
                        blurDp = s.wallpaperBlurDp,
                    )
                )
            },
            onDeletePreset = onDeleteBackgroundPreset,
            onDismiss = { showBackgroundPresets = false },
        )
    }

    editingGesture?.let { binding ->
        GestureBindingDialog(
            binding = binding,
            state = state,
            onSave = {
                onSaveGesture(it)
                editingGesture = null
            },
            onDismiss = { editingGesture = null },
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GestureBindingDialog(
    binding: GestureBindingEntity,
    state: LauncherUiState,
    onSave: (GestureBindingEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    var actionType by remember { mutableStateOf(binding.actionType) }
    var targetPkg by remember { mutableStateOf(binding.targetPackageName) }
    var targetTagId by remember { mutableStateOf(binding.targetTagId) }
    var targetCatId by remember { mutableStateOf(binding.targetCategoryId) }
    var targetSidebarId by remember { mutableStateOf(binding.targetSidebarId) }
    var targetWindowId by remember { mutableStateOf(binding.targetWindowId) }
    var targetRuleId by remember { mutableStateOf(binding.targetRuleId) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure ${GestureLabels.find { it.first == binding.gestureType }?.second ?: binding.gestureType}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = ActionOptions.find { it.first == actionType }?.second ?: actionType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Action") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        ActionOptions.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    actionType = key
                                    expanded = false
                                },
                            )
                        }
                    }
                }

                when (actionType) {
                    "OPEN_TAG" -> {
                        LazyColumn(modifier = Modifier.height(180.dp)) {
                            items(state.allTags, key = { it.tagId }) { tag ->
                                Text(
                                    text = (if (targetTagId == tag.tagId) "● " else "○ ") + tag.name,
                                    modifier = Modifier.fillMaxWidth().clickable { targetTagId = tag.tagId }.padding(8.dp),
                                )
                            }
                        }
                    }
                    "OPEN_CATEGORY" -> {
                        LazyColumn(modifier = Modifier.height(180.dp)) {
                            items(state.categories, key = { it.categoryId }) { cat ->
                                Text(
                                    text = (if (targetCatId == cat.categoryId) "● " else "○ ") + cat.name,
                                    modifier = Modifier.fillMaxWidth().clickable { targetCatId = cat.categoryId }.padding(8.dp),
                                )
                            }
                        }
                    }
                    "OPEN_SIDEBAR" -> {
                        LazyColumn(modifier = Modifier.height(180.dp)) {
                            items(state.sidebars, key = { it.sidebarId }) { sb ->
                                Text(
                                    text = (if (targetSidebarId == sb.sidebarId) "● " else "○ ") + sb.title,
                                    modifier = Modifier.fillMaxWidth().clickable { targetSidebarId = sb.sidebarId }.padding(8.dp),
                                )
                            }
                        }
                    }
                    "OPEN_WINDOW" -> {
                        LazyColumn(modifier = Modifier.height(180.dp)) {
                            items(state.windows, key = { it.windowId }) { w ->
                                Text(
                                    text = (if (targetWindowId == w.windowId) "● " else "○ ") + w.title,
                                    modifier = Modifier.fillMaxWidth().clickable { targetWindowId = w.windowId }.padding(8.dp),
                                )
                            }
                        }
                    }
                    "OPEN_RULE" -> {
                        LazyColumn(modifier = Modifier.height(180.dp)) {
                            items(state.savedRules, key = { it.ruleId }) { r ->
                                Text(
                                    text = (if (targetRuleId == r.ruleId) "● " else "○ ") + r.name,
                                    modifier = Modifier.fillMaxWidth().clickable { targetRuleId = r.ruleId }.padding(8.dp),
                                )
                            }
                        }
                    }
                    "OPEN_APP" -> {
                        LazyColumn(modifier = Modifier.height(180.dp)) {
                            items(state.appsWithTags, key = { it.app.packageName }) { item ->
                                Text(
                                    text = (if (targetPkg == item.app.packageName) "● " else "○ ") + item.app.displayName,
                                    modifier = Modifier.fillMaxWidth().clickable { targetPkg = item.app.packageName }.padding(8.dp),
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    binding.copy(
                        actionType = actionType,
                        targetPackageName = targetPkg,
                        targetTagId = targetTagId,
                        targetCategoryId = targetCatId,
                        targetSidebarId = targetSidebarId,
                        targetWindowId = targetWindowId,
                        targetRuleId = targetRuleId,
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
