package com.example.ui.screens

import com.example.ui.components.ImageCropEditor
import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import androidx.core.content.ContextCompat
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewCarousel
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.db.BackgroundPresetEntity
import com.example.data.db.CategoryEntity
import com.example.data.db.CustomFontEntity
import com.example.data.db.GestureBindingEntity
import com.example.data.db.ImageResourceEntity
import com.example.data.db.ThemeProfileEntity
import com.example.data.prefs.LauncherSettings
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.NotificationAccess
import com.example.data.system.CloudBackupScheduler
import com.example.ui.components.AnyconsConfig
import com.example.ui.components.AnyconsRenderer
import com.example.ui.components.BackgroundPresetDialog
import com.example.ui.components.CategoryEditorDialog
import com.example.ui.components.ColorSwatchRow
import com.example.ui.components.IconOutlineProcessor
import com.example.ui.components.IconStyleConfig
import com.example.ui.components.ImageLibraryDialog
import com.example.ui.components.StudioDarkSurfaceSwatches
import com.example.ui.components.StudioNeutralAndAccentSwatches
import com.example.ui.state.LauncherUiState
import com.example.ui.state.LauncherRoute
import com.example.ui.theme.TagPresetColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private val GestureLabels = listOf(
    "SWIPE_UP" to "Swipe Up",
    "SWIPE_DOWN" to "Swipe Down",
    "SWIPE_LEFT" to "Swipe Left",
    "SWIPE_RIGHT" to "Swipe Right",
    "DOUBLE_TAP" to "Double Tap",
    "SINGLE_TAP" to "Single Tap",
    "LONG_PRESS" to "Long Press",
    "TWO_FINGER_SWIPE_UP" to "Two-Finger Swipe Up",
    "TWO_FINGER_SWIPE_DOWN" to "Two-Finger Swipe Down",
    "TWO_FINGER_TAP" to "Two-Finger Tap",
    "PINCH_IN" to "Pinch In",
    "PINCH_OUT" to "Pinch Out",
)

private val ActionOptions = listOf(
    "NONE" to "None (Disabled)",
    "OPEN_DRAWER" to "Launcher: Open App Drawer",
    "OPEN_SEARCH" to "Launcher: Open Global Search",
    "OPEN_TAG_MANAGER" to "Launcher: Open Tag Hub",
    "OPEN_TAG" to "Launcher: Open Specific Tag Sidebar",
    "OPEN_CATEGORY" to "Launcher: Open Play Category Sidebar",
    "OPEN_SIDEBAR" to "Launcher: Open Configured Sidebar",
    "OPEN_WINDOW" to "Launcher: Open Floating Window",
    "OPEN_RULE" to "Launcher: Open Dynamic Rule",
    "NEXT_PAGE" to "Launcher: Next Home Page",
    "PREV_PAGE" to "Launcher: Previous Home Page",
    "JUMP_PAGE" to "Launcher: Jump to Specific Page",
    "SHOW_OBJECT" to "Studio: Show Home Element / Group",
    "HIDE_OBJECT" to "Studio: Hide Home Element / Group",
    "TOGGLE_OBJECT" to "Studio: Toggle Home Element / Group",
    "TOGGLE_EDIT_MODE" to "Launcher: Toggle Home Edit Studio",
    "OPEN_SETTINGS" to "Launcher: Open Settings",
    "OPEN_APP_MANAGER" to "Launcher: Open App Icons Manager",
    "OPEN_SIDEBAR_MANAGER" to "Launcher: Open Sidebars & Windows",
    "OPEN_APP" to "App Action: Launch Specific App",
    "OPEN_APP_SHORTCUT" to "App Action: Trigger App Shortcut",
    "OPEN_APP_INFO" to "App Action: Open App Info & Permissions",
    "TOGGLE_TORCH" to "Device Action: Toggle Flashlight",
    "CYCLE_RINGER" to "Device Action: Cycle Ringer Mode",
    "OPEN_SYSTEM_PANEL" to "Device Action: Quick Panel (Wi-Fi/BT/Volume)",
    "OPEN_SYSTEM_SETTING" to "Device Action: Open System Setting",
    "OPEN_WEBSITE" to "Web Action: Open URL Shortcut",
)

private val SystemSettingOptions = listOf(
    Settings.ACTION_SETTINGS to "Android Settings",
    Settings.ACTION_WIFI_SETTINGS to "Wi-Fi Settings",
    Settings.ACTION_BLUETOOTH_SETTINGS to "Bluetooth Settings",
    Settings.ACTION_DISPLAY_SETTINGS to "Display & Brightness",
    Settings.ACTION_SOUND_SETTINGS to "Sound & Vibration",
    Settings.ACTION_BATTERY_SAVER_SETTINGS to "Battery Saver",
    Settings.ACTION_APPLICATION_SETTINGS to "Apps & Notifications",
    Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS to "Notification Access",
)

private fun describeGestureBinding(binding: GestureBindingEntity, state: LauncherUiState): String {
    return when (binding.actionType) {
        "NONE" -> "None"
        "OPEN_APP" -> {
            val name = state.appsWithTags.find { it.app.packageName == binding.targetPackageName }?.app?.displayName
                ?: binding.targetPackageName.orEmpty()
            if (name.isNotBlank()) "Launch: $name" else "Launch App"
        }
        "OPEN_APP_SHORTCUT" -> {
            val appName = state.appsWithTags.find { it.app.packageName == binding.targetPackageName }?.app?.displayName
                ?: binding.targetPackageName.orEmpty()
            val sc = binding.targetUrl.orEmpty()
            if (appName.isNotBlank() && sc.isNotBlank()) "Shortcut: $appName ($sc)" else "App Shortcut"
        }
        "OPEN_APP_INFO" -> {
            val name = state.appsWithTags.find { it.app.packageName == binding.targetPackageName }?.app?.displayName
                ?: binding.targetPackageName.orEmpty()
            if (name.isNotBlank()) "App Info: $name" else "Open App Info"
        }
        "OPEN_TAG" -> {
            val tagName = state.allTags.find { it.tagId == binding.targetTagId }?.name
            if (!tagName.isNullOrBlank()) "Tag: $tagName" else "Open Tag Sidebar"
        }
        "OPEN_CATEGORY" -> {
            val catName = state.categories.find { it.categoryId == binding.targetCategoryId }?.name
            if (!catName.isNullOrBlank()) "Category: $catName" else "Open Category Sidebar"
        }
        "OPEN_SIDEBAR" -> {
            val sbName = state.sidebars.find { it.sidebarId == binding.targetSidebarId }?.title
            if (!sbName.isNullOrBlank()) "Sidebar: $sbName" else "Open Configured Sidebar"
        }
        "OPEN_WINDOW" -> {
            val winName = state.windows.find { it.windowId == binding.targetWindowId }?.title
            if (!winName.isNullOrBlank()) "Window: $winName" else "Open Floating Window"
        }
        "OPEN_RULE" -> {
            val rName = state.savedRules.find { it.ruleId == binding.targetRuleId }?.name
            if (!rName.isNullOrBlank()) "Rule: $rName" else "Open Dynamic Rule"
        }
        "OPEN_SYSTEM_SETTING" -> {
            val settingLabel = SystemSettingOptions.find { it.first == binding.targetUrl }?.second ?: "Android Settings"
            "System: $settingLabel"
        }
        "OPEN_WEBSITE" -> {
            val url = binding.targetUrl?.trim().orEmpty()
            if (url.isNotBlank()) "URL: $url" else "Open URL"
        }
        else -> ActionOptions.find { it.first == binding.actionType }?.second?.substringAfter(": ")
            ?: binding.actionType
    }
}

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
    onRefreshIconPacks: () -> Unit = {},
    onAddIconPack: (String) -> Unit = {},
    onRemoveIconPack: (String) -> Unit = {},
    onMoveIconPack: (String, Int) -> Unit = { _, _ -> },
    onResetDefaultGestures: () -> Unit = {},
    onSaveCategory: (CategoryEntity) -> Unit = {},
    onImportCustomFont: (Uri) -> Unit = {},
    onApplyCustomFont: (CustomFontEntity?) -> Unit = {},
    onDeleteCustomFont: (CustomFontEntity) -> Unit = {},
    onSaveThemeProfile: (String) -> Unit = {},
    onApplyThemeProfile: (ThemeProfileEntity) -> Unit = {},
    onDeleteThemeProfile: (Long) -> Unit = {},
    appDiscovery: AppDiscoveryManager? = null,
    initialSearchQuery: String = "",
    onConsumeInitialSearchQuery: () -> Unit = {},
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val resolvedDiscovery = remember(appDiscovery, context) { appDiscovery ?: AppDiscoveryManager(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionRefresh by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permissionRefresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val hasCalendarAccess = remember(permissionRefresh) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
    }
    val hasUsageAccess = remember(permissionRefresh) {
        runCatching {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName) == AppOpsManager.MODE_ALLOWED
        }.getOrDefault(false)
    }
    val hasNotificationAccess = remember(permissionRefresh) { NotificationAccess.isEnabled(context) }
    val scope = rememberCoroutineScope()
    val settingsDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val s = state.settings
    var searchQuery by remember { mutableStateOf(initialSearchQuery) }
    LaunchedEffect(initialSearchQuery) {
        if (initialSearchQuery.isNotBlank()) {
            searchQuery = initialSearchQuery
            onConsumeInitialSearchQuery()
        }
    }
    val matchingSections = remember(searchQuery) {
        LauncherSettingsCatalog.filterSections(searchQuery)
    }
    var showHomeGesturesDialog by remember { mutableStateOf(false) }
    var editingGesture by remember { mutableStateOf<GestureBindingEntity?>(null) }
    var editingCategoryForIcon by remember { mutableStateOf<CategoryEntity?>(null) }
    var showImageLibrary by remember { mutableStateOf(false) }
    var showBackgroundPresets by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

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

    val calendarAccessLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        onShowMessage(if (granted) "Calendar access enabled for upcoming events" else "Calendar access was not granted")
    }

    val cloudBackupFolderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
                onUpdateSettings { it.copy(cloudBackupTreeUri = uri.toString(), cloudBackupEnabled = true) }
                CloudBackupScheduler.schedule(context)
                onShowMessage("Automatic cloud backup enabled")
            }.onFailure {
                onShowMessage("Could not access that cloud folder")
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

    ModalNavigationDrawer(
        drawerState = settingsDrawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                val drawerClosed = settingsDrawerState.isClosed && !settingsDrawerState.isAnimationRunning
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 18.dp)
                        .then(
                            if (drawerClosed) {
                                Modifier.clearAndSetSemantics {}
                            } else {
                                Modifier
                            }
                        ),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("SETTINGS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    NavigationDrawerItem(
                        label = { Text("All settings") },
                        selected = searchQuery.isBlank(),
                        onClick = { scope.launch { searchQuery = ""; settingsDrawerState.close() } },
                        modifier = Modifier.testTag("settings_nav_all"),
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    LauncherSettingsCatalog.sections.forEach { section ->
                        NavigationDrawerItem(
                            label = { Text(section.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                            selected = searchQuery == section.title,
                            onClick = { scope.launch { searchQuery = section.title; settingsDrawerState.close() } },
                            modifier = Modifier.testTag("settings_nav_${section.id}"),
                        )
                    }
                }
            }
        },
    ) {
    Scaffold(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { scope.launch { settingsDrawerState.open() } },
                        modifier = Modifier.testTag("settings_sidebar_toggle"),
                    ) {
                        Icon(Icons.Rounded.Menu, contentDescription = "Open settings sections")
                    }
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Text("Launcher Settings", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search settings (theme, grid, icon outline, drawer...)") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.testTag("settings_search_clear"),
                            ) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear settings search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .testTag("settings_search_input"),
                )
                if (searchQuery.isNotBlank()) {
                    val visibleCount = matchingSections.keys.count { id ->
                        id != LauncherSettingsCatalog.SECTION_BLACK_ICON_COLORS || s.blackIconStyleEnabled
                    }
                    Text(
                        text = "$visibleCount matching section${if (visibleCount == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).testTag("settings_list"),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Direct access to every editable launcher manager.
            if (LauncherSettingsCatalog.SECTION_MANAGE in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_MANAGE) {
                    SettingsCard(
                        title = "Manage Your Launcher",
                        icon = Icons.Rounded.Tune,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_MANAGE].orEmpty() else emptyList(),
                    ) {
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
                    Surface(
                        onClick = { showHomeGesturesDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_menu_custom_gestures"),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Gesture,
                                contentDescription = "Custom Home Gestures",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Custom Home Screen Gestures",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "Configure swipe up/down/left/right & double tap to trigger app actions or launcher functions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    }
                }
            }

            // Dedicated Category Icons & Open-State Badges section
            if (LauncherSettingsCatalog.SECTION_CATEGORY_ICONS in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_CATEGORY_ICONS) {
                    CategoryIconsSettingsSection(
                        categories = state.categories,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_CATEGORY_ICONS].orEmpty() else emptyList(),
                        onEditCategory = { cat -> editingCategoryForIcon = cat },
                        onOpenFullTagManager = { onNavigate(LauncherRoute.Tags) },
                    )
                }
            }

            // Default Home Launcher Card
            if (LauncherSettingsCatalog.SECTION_DEFAULT_HOME in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_DEFAULT_HOME) {
                    SettingsCard(
                        title = "Default Home Application",
                        icon = Icons.Rounded.Home,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_DEFAULT_HOME].orEmpty() else emptyList(),
                    ) {
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
            }

            // Image Library & Background Presets
            if (LauncherSettingsCatalog.SECTION_MEDIA in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_MEDIA) {
                    SettingsCard(
                        title = "Media Library & Background Presets",
                        icon = Icons.Rounded.Image,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_MEDIA].orEmpty() else emptyList(),
                    ) {
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
            }

            // Theme & Typography
            if (LauncherSettingsCatalog.SECTION_THEME_TYPOGRAPHY in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_THEME_TYPOGRAPHY) {
                    SettingsCard(
                        title = "Theme & Typography",
                        icon = Icons.Rounded.TextFields,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_THEME_TYPOGRAPHY].orEmpty() else emptyList(),
                    ) {
                    Text("Theme Mode", style = MaterialTheme.typography.labelLarge)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        listOf("DARK", "LIGHT", "SYSTEM", "DYNAMIC").forEach { mode ->
                            FilterChip(
                                selected = s.themeMode == mode,
                                onClick = { onUpdateSettings { it.copy(themeMode = mode) } },
                                label = { Text(mode) },
                            )
                        }
                    }

                    Text("Font Family", style = MaterialTheme.typography.labelLarge)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                selected = s.fontStyle == key,
                                onClick = { onUpdateSettings { it.copy(fontStyle = key) } },
                                label = { Text(label) },
                                modifier = Modifier.testTag("settings_font_$key"),
                            )
                        }
                    }

                    Text("Global Font Scale: ${"%.2f".format(s.fontScale)}x")
                    Slider(
                        value = s.fontScale,
                        onValueChange = { v -> onUpdateSettings { it.copy(fontScale = v) } },
                        valueRange = 0.80f..1.80f,
                        modifier = Modifier.testTag("settings_font_scale_slider"),
                    )

                    Text("Home Element Default Text Color", style = MaterialTheme.typography.labelLarge)
                    ColorSwatchRow(
                        selectedColorHex = s.globalTextColorHex,
                        colors = StudioNeutralAndAccentSwatches,
                        onSelect = { c -> onUpdateSettings { it.copy(globalTextColorHex = c) } },
                        tagPrefix = "settings_text_color",
                    )
                    }
                }
            }

            // Custom Fonts (.TTF / .OTF) & Readability Studio
            if (LauncherSettingsCatalog.SECTION_CUSTOM_FONTS_READABILITY in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_CUSTOM_FONTS_READABILITY) {
                    CustomFontsReadabilitySection(
                        state = state,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_CUSTOM_FONTS_READABILITY].orEmpty() else emptyList(),
                        onUpdateSettings = onUpdateSettings,
                        onImportCustomFont = onImportCustomFont,
                        onApplyCustomFont = onApplyCustomFont,
                        onDeleteCustomFont = onDeleteCustomFont,
                    )
                }
            }

            // Palette, Surfaces & Motion
            if (LauncherSettingsCatalog.SECTION_PALETTE_SURFACES in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_PALETTE_SURFACES) {
                    SettingsCard(
                        title = "Palette, Surfaces & Motion",
                        icon = Icons.Rounded.Palette,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_PALETTE_SURFACES].orEmpty() else emptyList(),
                    ) {
                    Text("Primary Accent Color", style = MaterialTheme.typography.labelLarge)
                    ColorSwatchRow(
                        selectedColorHex = s.accentColorHex,
                        colors = TagPresetColors,
                        onSelect = { c -> onUpdateSettings { it.copy(accentColorHex = c) } },
                        tagPrefix = "settings_accent",
                    )

                    Text("Secondary Color", style = MaterialTheme.typography.labelLarge)
                    ColorSwatchRow(
                        selectedColorHex = s.secondaryColorHex,
                        colors = TagPresetColors,
                        onSelect = { c -> onUpdateSettings { it.copy(secondaryColorHex = c) } },
                        tagPrefix = "settings_secondary",
                    )

                    Text("Tertiary Color", style = MaterialTheme.typography.labelLarge)
                    ColorSwatchRow(
                        selectedColorHex = s.tertiaryColorHex,
                        colors = TagPresetColors,
                        onSelect = { c -> onUpdateSettings { it.copy(tertiaryColorHex = c) } },
                        tagPrefix = "settings_tertiary",
                    )

                    Text("UI Surface Opacity: ${(s.surfaceOpacity * 100).roundToInt()}%")
                    Slider(
                        value = s.surfaceOpacity,
                        onValueChange = { v -> onUpdateSettings { it.copy(surfaceOpacity = v) } },
                        valueRange = 0.25f..1.0f,
                    )

                    Text("Global Border Width: ${"%.1f".format(s.globalBorderWidthDp)}dp")
                    Slider(
                        value = s.globalBorderWidthDp,
                        onValueChange = { v -> onUpdateSettings { it.copy(globalBorderWidthDp = v) } },
                        valueRange = 0f..4.0f,
                    )

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

                    ThemeProfilesAndSurfacePaletteControls(
                        state = state,
                        onUpdateSettings = onUpdateSettings,
                        onSaveThemeProfile = onSaveThemeProfile,
                        onApplyThemeProfile = onApplyThemeProfile,
                        onDeleteThemeProfile = onDeleteThemeProfile,
                    )
                    }
                }
            }

            // Icon Packs & Loading Hierarchy
            if (LauncherSettingsCatalog.SECTION_ICON_PACKS in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_ICON_PACKS) {
                    val activePackOrder = remember(s.iconPackOrderCsv) { s.iconPackOrder() }
                    val packLookup = remember(state.installedIconPacks) {
                        state.installedIconPacks.associateBy { it.packageName }
                    }
                    val availableToAdd = remember(state.installedIconPacks, activePackOrder) {
                        state.installedIconPacks.filter { it.packageName !in activePackOrder }
                    }
                    SettingsCard(
                        title = "Icon Packs & Loading Hierarchy",
                        icon = Icons.Rounded.Apps,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_ICON_PACKS].orEmpty() else emptyList(),
                    ) {
                    Text(
                        text = "Icons resolve in priority order:\n1. Per-App Custom Icon  ->  2. Active Icon Packs (top to bottom)  ->  3. System Icon",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Active Pack Order (${activePackOrder.size})",
                            style = MaterialTheme.typography.labelLarge,
                        )

                        OutlinedButton(
                            onClick = onRefreshIconPacks,
                            modifier = Modifier.testTag("refresh_icon_packs_button"),
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Rescan")
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Sidebar Text Color", style = MaterialTheme.typography.labelLarge)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            listOf("AUTO" to "Auto", "LIGHT" to "Light", "DARK" to "Dark", "CUSTOM" to "Custom").forEach { (key, label) ->
                                FilterChip(
                                    selected = s.sidebarTextColorMode == key,
                                    onClick = { onUpdateSettings { it.copy(sidebarTextColorMode = key) } },
                                    label = { Text(label) },
                                )
                            }
                        }
                        Text(
                            "Auto follows the app theme. Light and Dark force a text color. Custom uses Home Element Default Text Color above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text("Right Sidebar Category Icon Style", style = MaterialTheme.typography.labelLarge)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            listOf("BLACK_WHITE" to "Black + white outline", "WHITE_BLACK" to "White + black outline").forEach { (key, label) ->
                                FilterChip(
                                    selected = s.sidebarCategoryIconStyle == key,
                                    onClick = { onUpdateSettings { it.copy(sidebarCategoryIconStyle = key) } },
                                    label = { Text(label) },
                                )
                            }
                        }
                    }

                    if (activePackOrder.isEmpty()) {
                        Text(
                            text = "No icon packs active. Per-app custom icons fall back directly to system icons.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            activePackOrder.forEachIndexed { idx, pkg ->
                                val info = packLookup[pkg]
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("active_icon_pack_$pkg"),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "#${idx + 1}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.width(28.dp),
                                        )
                                        if (info?.icon != null) {
                                            Image(
                                                bitmap = info.icon,
                                                contentDescription = info.label,
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                            )
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = info?.label ?: pkg,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            Text(
                                                text = if (info != null) "${info.mappedEntryCount} mapped icons" else "Pack not installed",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        IconButton(
                                            onClick = { onMoveIconPack(pkg, -1) },
                                            enabled = idx > 0,
                                            modifier = Modifier.size(36.dp).testTag("move_pack_up_$pkg"),
                                        ) {
                                            Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move pack higher", modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = { onMoveIconPack(pkg, 1) },
                                            enabled = idx < activePackOrder.lastIndex,
                                            modifier = Modifier.size(36.dp).testTag("move_pack_down_$pkg"),
                                        ) {
                                            Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move pack lower", modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = { onRemoveIconPack(pkg) },
                                            modifier = Modifier.size(36.dp).testTag("remove_pack_$pkg"),
                                        ) {
                                            Icon(Icons.Rounded.Close, contentDescription = "Remove pack", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Text(
                        text = "Installed Icon Packs (${availableToAdd.size} available)",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    if (state.installedIconPacks.isEmpty()) {
                        Text(
                            text = "No compatible icon pack APKs found on this device. Install any ADW/Nova/Smart Launcher icon pack APK and tap Rescan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else if (availableToAdd.isEmpty()) {
                        Text(
                            text = "All discovered icon packs are already in your hierarchy above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            availableToAdd.forEach { pack ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("available_icon_pack_${pack.packageName}"),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (pack.icon != null) {
                                            Image(
                                                bitmap = pack.icon,
                                                contentDescription = pack.label,
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                            )
                                            Spacer(Modifier.width(10.dp))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(pack.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = "${pack.mappedEntryCount} icons • ${pack.packageName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Button(
                                            onClick = { onAddIconPack(pack.packageName) },
                                            modifier = Modifier.testTag("add_pack_${pack.packageName}"),
                                        ) {
                                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Add")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }

            // Global Arcticons-style Line Icon Generation
            if (LauncherSettingsCatalog.SECTION_LINE_ICONS in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_LINE_ICONS) {
                    SettingsCard(
                        title = "Arcticons-Style Line Icons",
                        icon = Icons.Rounded.Contrast,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_LINE_ICONS].orEmpty() else emptyList(),
                    ) {
                        Text(
                            "Automatically turn every installed app icon into transparent black or white line art. Individual icons can be refined by drawing over detected edges in App Icons.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        ToggleRow("Generate line icons for all apps", s.lineIconsEnabled) { enabled ->
                            onUpdateSettings { it.copy(lineIconsEnabled = enabled) }
                        }
                        Text("Line color", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = s.lineIconDefaultColorHex == 0xFFFFFFFFL,
                                onClick = { onUpdateSettings { it.copy(lineIconDefaultColorHex = 0xFFFFFFFFL, lineIconDefaultOutlineColorHex = 0xFF000000L) } },
                                label = { Text("White") },
                            )
                            FilterChip(
                                selected = s.lineIconDefaultColorHex == 0xFF000000L,
                                onClick = { onUpdateSettings { it.copy(lineIconDefaultColorHex = 0xFF000000L, lineIconDefaultOutlineColorHex = 0xFFFFFFFFL) } },
                                label = { Text("Black") },
                            )
                        }
                        ToggleRow("Add a thin contrast outline", s.lineIconDefaultOutlineEnabled) { enabled ->
                            onUpdateSettings { it.copy(lineIconDefaultOutlineEnabled = enabled) }
                        }
                        if (s.lineIconDefaultOutlineEnabled) {
                            Text("Outline color", style = MaterialTheme.typography.labelLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = s.lineIconDefaultOutlineColorHex == 0xFF000000L,
                                    onClick = { onUpdateSettings { it.copy(lineIconDefaultOutlineColorHex = 0xFF000000L) } },
                                    label = { Text("Black") },
                                )
                                FilterChip(
                                    selected = s.lineIconDefaultOutlineColorHex == 0xFFFFFFFFL,
                                    onClick = { onUpdateSettings { it.copy(lineIconDefaultOutlineColorHex = 0xFFFFFFFFL) } },
                                    label = { Text("White") },
                                )
                            }
                            Text("Outline thickness: ${"%.1f".format(s.lineIconDefaultOutlineWidthPx)} px")
                            Slider(
                                value = s.lineIconDefaultOutlineWidthPx,
                                onValueChange = { value -> onUpdateSettings { it.copy(lineIconDefaultOutlineWidthPx = value) } },
                                valueRange = 0.5f..3f,
                            )
                        }
                        Text("Line thickness: ${"%.1f".format(s.lineIconDefaultLineWidthPx)} px")
                        Slider(
                            value = s.lineIconDefaultLineWidthPx,
                            onValueChange = { value -> onUpdateSettings { it.copy(lineIconDefaultLineWidthPx = value) } },
                            valueRange = 0.7f..4f,
                        )
                        Text(
                            "Per-app drawings and settings override these global defaults. Turn this off to return uncustomized apps to their normal icon sources.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Niagara Anycons icon collection
            if (LauncherSettingsCatalog.SECTION_ANYCONS in matchingSections) {
                item(key = "anycons_icon_styles") {
                    val sampleApps = remember {
                        listOf(
                            Triple("com.google.android.dialer", "Phone", 7L),
                            Triple("com.google.android.apps.messaging", "Messages", 7L),
                            Triple("com.android.camera", "Camera", 24L),
                            Triple("com.spotify.music", "Music", 20L),
                            Triple("com.android.chrome", "Browser", 29L),
                        )
                    }
                    val currentAnyconsCfg = remember(
                        s.anyconsStyle,
                        s.anyconsSourceMode,
                        s.anyconsColorMode,
                        s.anyconsStrokeScale,
                        s.accentColorHex,
                        s.secondaryColorHex,
                        s.tertiaryColorHex,
                        s.globalTextColorHex,
                        s.themeMode,
                    ) {
                        s.anyconsConfig(isDarkTheme = s.themeMode != "LIGHT")
                    }
                    val anyconsStyleSpecs = remember {
                        listOf(
                            "NONE" to "Original",
                            "OUTLINE" to "Outline",
                            "MATERIAL" to "Material",
                            "STICKER" to "Sticker",
                            "MARBLE" to "Marble",
                            "WATERCOLOR" to "Watercolor",
                            "BRIGHT" to "Bright",
                            "STELLAR" to "Stellar",
                            "OUTLINE_CHROMA" to "Outline Chroma",
                            "DOTS" to "Dots",
                            "DOODLE" to "Doodle",
                            "CLICKS" to "Clicks",
                        )
                    }
                    val liveStripBitmaps by produceState<List<androidx.compose.ui.graphics.ImageBitmap?>>(
                        initialValue = List(sampleApps.size) { null },
                        currentAnyconsCfg,
                    ) {
                        value = if (currentAnyconsCfg.style == "NONE") {
                            List(sampleApps.size) { null }
                        } else {
                            withContext(Dispatchers.Default) {
                                sampleApps.map { (pkg, label, catId) ->
                                    AnyconsRenderer.renderForApp(
                                        packageName = pkg,
                                        appName = label,
                                        categoryId = catId,
                                        sourceBitmap = null,
                                        foregroundBitmap = null,
                                        config = currentAnyconsCfg.copy(sourceMode = AnyconsConfig.SOURCE_GLYPHS),
                                    )?.asImageBitmap()
                                }
                            }
                        }
                    }

                    SettingsCard(
                        title = "Anycons Icon Styles",
                        icon = Icons.Rounded.Contrast,
                        matchedEntries = if (searchQuery.isNotBlank()) {
                            matchingSections[LauncherSettingsCatalog.SECTION_ANYCONS].orEmpty()
                        } else emptyList(),
                    ) {
                        Text(
                            "Niagara-inspired minimalist icon collections with unified glyphs, borders, textures, and color treatments.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        if (s.anyconsStyle != "NONE") {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (s.themeMode == "LIGHT") Color(0xFFE2E8F0) else Color(0xFF0F172A),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    sampleApps.forEachIndexed { idx, (_, label, _) ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            val bmp = liveStripBitmaps.getOrNull(idx)
                                            if (bmp != null) {
                                                Image(
                                                    bitmap = bmp,
                                                    contentDescription = "$label preview",
                                                    modifier = Modifier.size(40.dp),
                                                )
                                            } else {
                                                Box(modifier = Modifier.size(40.dp))
                                            }
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (s.themeMode == "LIGHT") Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                                maxLines = 1,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            anyconsStyleSpecs.forEach { (key, label) ->
                                val chipPreview by produceState<androidx.compose.ui.graphics.ImageBitmap?>(
                                    initialValue = null,
                                    key,
                                    s.themeMode,
                                    s.accentColorHex,
                                ) {
                                    value = if (key == "NONE") {
                                        null
                                    } else {
                                        withContext(Dispatchers.Default) {
                                            AnyconsRenderer.renderForApp(
                                                packageName = "com.android.camera",
                                                appName = "Camera",
                                                categoryId = 24L,
                                                sourceBitmap = null,
                                                foregroundBitmap = null,
                                                config = currentAnyconsCfg.copy(
                                                    style = key,
                                                    sourceMode = AnyconsConfig.SOURCE_GLYPHS,
                                                ),
                                            )?.asImageBitmap()
                                        }
                                    }
                                }
                                FilterChip(
                                    selected = s.anyconsStyle == key,
                                    onClick = { onUpdateSettings { it.copy(anyconsStyle = key) } },
                                    leadingIcon = chipPreview?.let { bmp ->
                                        {
                                            Image(
                                                bitmap = bmp,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("anycons_style_$key"),
                                )
                            }
                        }

                        if (s.anyconsStyle != "NONE") {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Text("Icon Source", style = MaterialTheme.typography.labelLarge)
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                listOf(
                                    AnyconsConfig.SOURCE_GLYPHS to "Minimalist Glyphs",
                                    AnyconsConfig.SOURCE_HYBRID to "Hybrid",
                                    AnyconsConfig.SOURCE_APP_ARTWORK to "App Artwork",
                                ).forEach { (srcKey, srcLabel) ->
                                    FilterChip(
                                        selected = s.anyconsSourceMode == srcKey,
                                        onClick = { onUpdateSettings { it.copy(anyconsSourceMode = srcKey) } },
                                        label = { Text(srcLabel) },
                                        modifier = Modifier.testTag("anycons_source_$srcKey"),
                                    )
                                }
                            }

                            Text("Color Treatment", style = MaterialTheme.typography.labelLarge)
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                listOf(
                                    AnyconsConfig.COLOR_AUTO to "Auto",
                                    AnyconsConfig.COLOR_WHITE to "White",
                                    AnyconsConfig.COLOR_BLACK to "Black",
                                    AnyconsConfig.COLOR_ACCENT to "Accent",
                                    AnyconsConfig.COLOR_VIBRANT to "Vibrant",
                                ).forEach { (colKey, colLabel) ->
                                    FilterChip(
                                        selected = s.anyconsColorMode == colKey,
                                        onClick = { onUpdateSettings { it.copy(anyconsColorMode = colKey) } },
                                        label = { Text(colLabel) },
                                        modifier = Modifier.testTag("anycons_color_$colKey"),
                                    )
                                }
                            }

                            Text(
                                "Stroke & Border Weight: ${"%.2f".format(s.anyconsStrokeScale)}x",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Slider(
                                value = s.anyconsStrokeScale,
                                onValueChange = { value ->
                                    onUpdateSettings { it.copy(anyconsStrokeScale = value.coerceIn(0.65f, 1.6f)) }
                                },
                                valueRange = 0.65f..1.6f,
                            )
                        }

                        Text(
                            "Minimalist Glyphs match Niagara's uniform vector style across apps and Play categories; switch to Hybrid or App Artwork to style adaptive foregrounds directly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Black Launcher Icon Contrast & Outline Styling
            if (LauncherSettingsCatalog.SECTION_BLACK_ICONS in matchingSections ||
                (!s.blackIconStyleEnabled && LauncherSettingsCatalog.SECTION_BLACK_ICON_COLORS in matchingSections)
            ) {
                item(key = LauncherSettingsCatalog.SECTION_BLACK_ICONS) {
                    val iconStyle = remember(
                        s.blackIconStyleEnabled,
                        s.blackIconMode,
                        s.blackIconFillColorHex,
                        s.blackIconOutlineColorHex,
                        s.blackIconOutlineWidthDp,
                        s.blackIconDarknessThreshold,
                    ) { s.iconStyleConfig() }
                    val sampleRawBitmap = remember { createSampleBlackLauncherIconBitmap() }
                    val previewBitmap by produceState(
                        initialValue = sampleRawBitmap.asImageBitmap(),
                        iconStyle,
                    ) {
                        value = withContext(Dispatchers.Default) {
                            IconOutlineProcessor.transformBitmap(sampleRawBitmap, iconStyle).asImageBitmap()
                        }
                    }

                    SettingsCard(
                        title = "Black Icon Contrast & Outline",
                        icon = Icons.Rounded.Contrast,
                        matchedEntries = if (searchQuery.isNotBlank()) {
                            matchingSections[LauncherSettingsCatalog.SECTION_BLACK_ICONS].orEmpty()
                        } else emptyList(),
                    ) {
                    Text(
                        text = "Automatically converts black or dark launcher icons into white glyphs with a crisp black outline for legibility on any wallpaper.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "White + Black Outline for Black Icons",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = s.blackIconStyleEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(blackIconStyleEnabled = enabled) }
                            },
                            modifier = Modifier.testTag("toggle_black_icon_style"),
                        )
                    }

                    // Live dark/light surface preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Image(
                                    bitmap = previewBitmap,
                                    contentDescription = "Dark wallpaper preview",
                                    modifier = Modifier.size(40.dp),
                                )
                                Column {
                                    Text("Dark BG", style = MaterialTheme.typography.labelMedium, color = Color.White)
                                    Text(
                                        if (s.blackIconStyleEnabled) "Outlined" else "Original",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Image(
                                    bitmap = previewBitmap,
                                    contentDescription = "Light wallpaper preview",
                                    modifier = Modifier.size(40.dp),
                                )
                                Column {
                                    Text("Light BG", style = MaterialTheme.typography.labelMedium, color = Color(0xFF0F172A))
                                    Text(
                                        if (s.blackIconStyleEnabled) "Outlined" else "Original",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF475569),
                                    )
                                }
                            }
                        }
                    }

                    if (s.blackIconStyleEnabled) {
                        Text("Detection Mode", style = MaterialTheme.typography.labelLarge)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            listOf(
                                IconStyleConfig.MODE_BLACK_ICONS to "Black Icons",
                                IconStyleConfig.MODE_ALL_MONOCHROME to "All Monochrome",
                                IconStyleConfig.MODE_ALL_ICONS_OUTLINE to "Outline All",
                            ).forEach { (modeKey, modeLabel) ->
                                FilterChip(
                                    selected = s.blackIconMode == modeKey,
                                    onClick = { onUpdateSettings { it.copy(blackIconMode = modeKey) } },
                                    label = { Text(modeLabel) },
                                    modifier = Modifier.testTag("black_icon_mode_$modeKey"),
                                )
                            }
                        }
                    }
                    }
                }
            }

            if (s.blackIconStyleEnabled && LauncherSettingsCatalog.SECTION_BLACK_ICON_COLORS in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_BLACK_ICON_COLORS) {
                    SettingsCard(
                        title = "Black Icon Outline & Colors",
                        icon = Icons.Rounded.Contrast,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_BLACK_ICON_COLORS].orEmpty() else emptyList(),
                    ) {
                        Text("Outline Thickness: ${"%.1f".format(s.blackIconOutlineWidthDp)}dp")
                        Slider(
                            value = s.blackIconOutlineWidthDp,
                            onValueChange = { v -> onUpdateSettings { it.copy(blackIconOutlineWidthDp = v) } },
                            valueRange = 0.5f..5.0f,
                            modifier = Modifier.testTag("slider_black_icon_outline_width"),
                        )

                        Text("Blackness Threshold: ${(s.blackIconDarknessThreshold * 100).roundToInt()}%")
                        Slider(
                            value = s.blackIconDarknessThreshold,
                            onValueChange = { v -> onUpdateSettings { it.copy(blackIconDarknessThreshold = v) } },
                            valueRange = 0.10f..0.60f,
                            modifier = Modifier.testTag("slider_black_icon_threshold"),
                        )

                        Text("Icon Fill Color", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(
                                listOf(
                                    0xFFFFFFFFL, // Pure White (default)
                                    0xFFE2E8F0L, // Soft Slate White
                                    0xFF38BDF8L, // Electric Cyan
                                    0xFF34D399L, // Mint Emerald
                                    0xFFFBBF24L, // Solar Amber
                                    0xFFFB7185L, // Neon Coral
                                )
                            ) { fillHex ->
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(fillHex))
                                        .border(
                                            width = if (s.blackIconFillColorHex == fillHex) 2.5.dp else 1.dp,
                                            color = if (s.blackIconFillColorHex == fillHex) MaterialTheme.colorScheme.primary else Color.Gray,
                                            shape = CircleShape,
                                        )
                                        .clickable { onUpdateSettings { it.copy(blackIconFillColorHex = fillHex) } }
                                        .testTag("black_icon_fill_$fillHex"),
                                )
                            }
                        }

                        Text("Outline Color", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(
                                listOf(
                                    0xFF000000L, // Pure Black (default)
                                    0xFF090D16L, // Deep Obsidian
                                    0xFF1E293BL, // Slate Charcoal
                                    0xFF1E1B4BL, // Midnight Indigo
                                    0xFFFFFFFFL, // White
                                )
                            ) { outlineHex ->
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color(outlineHex))
                                        .border(
                                            width = if (s.blackIconOutlineColorHex == outlineHex) 2.5.dp else 1.dp,
                                            color = if (s.blackIconOutlineColorHex == outlineHex) MaterialTheme.colorScheme.primary else Color.Gray,
                                            shape = CircleShape,
                                        )
                                        .clickable { onUpdateSettings { it.copy(blackIconOutlineColorHex = outlineHex) } }
                                        .testTag("black_icon_outline_$outlineHex"),
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                onUpdateSettings {
                                    it.copy(
                                        blackIconStyleEnabled = true,
                                        blackIconMode = IconStyleConfig.MODE_BLACK_ICONS,
                                        blackIconFillColorHex = IconStyleConfig.DEFAULT_FILL_COLOR_HEX,
                                        blackIconOutlineColorHex = IconStyleConfig.DEFAULT_OUTLINE_COLOR_HEX,
                                        blackIconOutlineWidthDp = IconStyleConfig.DEFAULT_OUTLINE_WIDTH_DP,
                                        blackIconDarknessThreshold = IconStyleConfig.DEFAULT_DARKNESS_THRESHOLD,
                                    )
                                }
                            },
                            modifier = Modifier.testTag("reset_black_icon_style_button"),
                        ) {
                            Text("Reset to Default (White + Black Outline)")
                        }
                    }
                }
            }

            // Wallpaper & Atmosphere
            if (LauncherSettingsCatalog.SECTION_WALLPAPER in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_WALLPAPER) {
                    SettingsCard(
                        title = "Wallpaper & Atmosphere",
                        icon = Icons.Rounded.Wallpaper,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_WALLPAPER].orEmpty() else emptyList(),
                    ) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        listOf(
                            "SYSTEM_LIVE" to "System / Live",
                            "BUILTIN_PRISM" to "Prism",
                            "BUILTIN_TOPO" to "Topo",
                            "GRADIENT" to "Gradient",
                            "SOLID" to "Solid",
                            "CUSTOM_IMAGE" to "Custom Image",
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = s.wallpaperType == key,
                                onClick = { onUpdateSettings { it.copy(wallpaperType = key) } },
                                label = { Text(label) },
                                modifier = Modifier.testTag("settings_wallpaper_$key"),
                            )
                        }
                    }

                    if (s.wallpaperType == "GRADIENT") {
                        Text("Gradient Direction", style = MaterialTheme.typography.labelLarge)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            listOf("VERTICAL", "HORIZONTAL", "DIAGONAL").forEach { dir ->
                                FilterChip(
                                    selected = s.wallpaperGradientDirection == dir,
                                    onClick = { onUpdateSettings { it.copy(wallpaperGradientDirection = dir) } },
                                    label = { Text(dir) },
                                    modifier = Modifier.testTag("settings_wp_dir_$dir"),
                                )
                            }
                        }
                        Text("Gradient Start Color", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = s.wallpaperGradientStartHex,
                            colors = StudioDarkSurfaceSwatches,
                            onSelect = { c -> onUpdateSettings { it.copy(wallpaperGradientStartHex = c) } },
                            tagPrefix = "settings_wp_grad_start",
                        )
                        Text("Gradient End Color", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = s.wallpaperGradientEndHex,
                            colors = StudioDarkSurfaceSwatches,
                            onSelect = { c -> onUpdateSettings { it.copy(wallpaperGradientEndHex = c) } },
                            tagPrefix = "settings_wp_grad_end",
                        )
                    } else if (s.wallpaperType == "SOLID") {
                        Text("Solid Wallpaper Color", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = s.wallpaperSolidHex,
                            colors = StudioDarkSurfaceSwatches,
                            onSelect = { c -> onUpdateSettings { it.copy(wallpaperSolidHex = c) } },
                            tagPrefix = "settings_wp_solid",
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            wallpaperPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Text("Import Custom Wallpaper")
                        }
                        if (s.wallpaperCustomPath != null) {
                            TextButton(onClick = {
                                onUpdateSettings { it.copy(wallpaperCustomPath = null, wallpaperType = "BUILTIN_PRISM") }
                            }) { Text("Clear Image") }
                        }
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
                        valueRange = 0f..0.85f,
                    )
                    Text("Dim Overlay Tint", style = MaterialTheme.typography.labelLarge)
                    ColorSwatchRow(
                        selectedColorHex = s.wallpaperDimColorHex,
                        colors = StudioNeutralAndAccentSwatches,
                        onSelect = { c -> onUpdateSettings { it.copy(wallpaperDimColorHex = c) } },
                        tagPrefix = "settings_wp_dim",
                    )

                    ToggleRow(
                        label = "Scroll Wallpaper Across Pages",
                        checked = s.wallpaperScrollingEnabled,
                        switchModifier = Modifier.testTag("settings_wallpaper_scrolling_switch"),
                    ) { v ->
                        onUpdateSettings { it.copy(wallpaperScrollingEnabled = v) }
                    }

                    WallpaperFxStudioControls(
                        settings = s,
                        onUpdateSettings = onUpdateSettings,
                    )
                    }
                }
            }

            // Universal All-Apps Sidebar
            if (LauncherSettingsCatalog.SECTION_DRAWER in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_DRAWER) {
                    SettingsCard(
                        title = "Universal All-Apps Drawer",
                        icon = Icons.Rounded.Apps,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_DRAWER].orEmpty() else emptyList(),
                    ) {
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

                    Text("Drawer Width: ${(s.allAppsSidebarWidthFraction * 100).roundToInt()}%")
                    Slider(
                        value = s.allAppsSidebarWidthFraction,
                        onValueChange = { v -> onUpdateSettings { it.copy(allAppsSidebarWidthFraction = v) } },
                        valueRange = 0.65f..1.0f,
                    )

                    Text("Layout Mode", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("GRID", "LIST").forEach { mode ->
                            FilterChip(
                                selected = s.drawerLayoutMode == mode,
                                onClick = { onUpdateSettings { it.copy(drawerLayoutMode = mode) } },
                                label = { Text(mode) },
                                modifier = Modifier.testTag("settings_drawer_layout_$mode"),
                            )
                        }
                    }

                    Text("App Sort Order", style = MaterialTheme.typography.labelLarge)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        listOf(
                            "ALPHABETICAL" to "A–Z",
                            "MOST_USED" to "Most Used",
                            "RECENTLY_INSTALLED" to "Recent",
                            "MANUAL" to "Manual",
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = s.drawerSortOrder == key,
                                onClick = { onUpdateSettings { it.copy(drawerSortOrder = key) } },
                                label = { Text(label) },
                                modifier = Modifier.testTag("settings_drawer_sort_$key"),
                            )
                        }
                    }

                    ToggleRow("Show Search Bar in Drawer", s.drawerSearchBarVisible) { v ->
                        onUpdateSettings { it.copy(drawerSearchBarVisible = v) }
                    }
                    ToggleRow(
                        label = "Close Drawer on App Launch",
                        checked = s.closeDrawerOnLaunch,
                        switchModifier = Modifier.testTag("settings_drawer_close_on_launch_switch"),
                    ) { v ->
                        onUpdateSettings { it.copy(closeDrawerOnLaunch = v) }
                    }
                    if (s.drawerSearchBarVisible) {
                        ToggleRow(
                            label = "Auto-Focus Drawer Search",
                            checked = s.autoFocusDrawerSearch,
                            switchModifier = Modifier.testTag("settings_drawer_autofocus_switch"),
                        ) { v ->
                            onUpdateSettings { it.copy(autoFocusDrawerSearch = v) }
                        }
                        Text("Search Bar Position", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("TOP", "BOTTOM").forEach { pos ->
                                FilterChip(
                                    selected = s.drawerSearchPosition == pos,
                                    onClick = { onUpdateSettings { it.copy(drawerSearchPosition = pos) } },
                                    label = { Text(pos) },
                                    modifier = Modifier.testTag("settings_drawer_search_pos_$pos"),
                                )
                            }
                        }
                    }

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

                    Text("Drawer Background Color", style = MaterialTheme.typography.labelLarge)
                    ColorSwatchRow(
                        selectedColorHex = s.allAppsBgColorHex,
                        colors = StudioDarkSurfaceSwatches,
                        onSelect = { c -> onUpdateSettings { it.copy(allAppsBgColorHex = c) } },
                        tagPrefix = "settings_drawer_bg1",
                    )

                    if (s.allAppsBgType == "GRADIENT") {
                        Text("Drawer Gradient End", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = s.allAppsBgGradientEndHex,
                            colors = StudioDarkSurfaceSwatches,
                            onSelect = { c -> onUpdateSettings { it.copy(allAppsBgGradientEndHex = c) } },
                            tagPrefix = "settings_drawer_bg2",
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            drawerBgPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) { Text("Set Drawer Image") }
                        if (s.allAppsBgImagePath != null) {
                            TextButton(onClick = {
                                onUpdateSettings { it.copy(allAppsBgImagePath = null, allAppsBgType = "SOLID") }
                            }) { Text("Clear") }
                        }
                    }

                    Text("Drawer Blur: ${s.allAppsBgBlurDp.roundToInt()}dp")
                    Slider(
                        value = s.allAppsBgBlurDp,
                        onValueChange = { v -> onUpdateSettings { it.copy(allAppsBgBlurDp = v) } },
                        valueRange = 0f..24f,
                    )
                    if (s.allAppsBgImagePath != null) {
                        Text("Drag to reposition • pinch to zoom", style = MaterialTheme.typography.bodySmall)
                        ImageCropEditor(
                            imagePath = s.allAppsBgImagePath,
                            cropScale = s.allAppsBgCropScale,
                            cropX = s.allAppsBgCropX,
                            cropY = s.allAppsBgCropY,
                            onCropChange = { scale, x, y -> onUpdateSettings { it.copy(allAppsBgCropScale = scale, allAppsBgCropX = x, allAppsBgCropY = y) } },
                            cropAspectRatio = (
                                LocalConfiguration.current.screenWidthDp.toFloat() *
                                    (if (s.allAppsSidebarEdge == "BOTTOM") 1f else s.allAppsSidebarWidthFraction)
                                    / LocalConfiguration.current.screenHeightDp.toFloat()
                                ).coerceIn(0.25f, 1f),
                        )
                        Text("Drawer Image Zoom: ${String.format("%.1f", s.allAppsBgCropScale)}x")
                        Slider(
                            value = s.allAppsBgCropScale,
                            onValueChange = { v -> onUpdateSettings { it.copy(allAppsBgCropScale = v) } },
                            valueRange = 1f..4f,
                        )
                        Text("Horizontal Crop: ${(s.allAppsBgCropX * 100).roundToInt()}%")
                        Slider(
                            value = s.allAppsBgCropX,
                            onValueChange = { v -> onUpdateSettings { it.copy(allAppsBgCropX = v) } },
                            valueRange = -1f..1f,
                        )
                        Text("Vertical Crop: ${(s.allAppsBgCropY * 100).roundToInt()}%")
                        Slider(
                            value = s.allAppsBgCropY,
                            onValueChange = { v -> onUpdateSettings { it.copy(allAppsBgCropY = v) } },
                            valueRange = -1f..1f,
                        )
                        TextButton(onClick = { onUpdateSettings { it.copy(allAppsBgCropScale = 1f, allAppsBgCropX = 0f, allAppsBgCropY = 0f) } }) {
                            Text("Reset Drawer Image Crop")
                        }
                    }
                    Text("Card Opacity: ${(s.drawerCardOpacity * 100).roundToInt()}%")
                    Slider(
                        value = s.drawerCardOpacity,
                        onValueChange = { v -> onUpdateSettings { it.copy(drawerCardOpacity = v) } },
                        valueRange = 0.25f..1.0f,
                    )
                    Text("Drawer Corner Radius: ${s.drawerCornerRadiusDp.roundToInt()}dp")
                    Slider(
                        value = s.drawerCornerRadiusDp,
                        onValueChange = { v -> onUpdateSettings { it.copy(drawerCornerRadiusDp = v) } },
                        valueRange = 8f..36f,
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
            }

            // Niagara Wave Alphabet Navigation
            if (LauncherSettingsCatalog.SECTION_NIAGARA in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_NIAGARA) {
                    SettingsCard(
                        title = "Niagara Wave Alphabet Index",
                        icon = Icons.Rounded.SortByAlpha,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_NIAGARA].orEmpty() else emptyList(),
                    ) {
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
                    Text("Base Letter Size: ${s.niagaraBaseFontSp.roundToInt()}sp")
                    Slider(
                        value = s.niagaraBaseFontSp,
                        onValueChange = { v -> onUpdateSettings { it.copy(niagaraBaseFontSp = v) } },
                        valueRange = 8f..18f,
                    )
                    Text("Selected Letter Size: ${s.niagaraSelectedFontSp.roundToInt()}sp")
                    Slider(
                        value = s.niagaraSelectedFontSp,
                        onValueChange = { v -> onUpdateSettings { it.copy(niagaraSelectedFontSp = v) } },
                        valueRange = 16f..34f,
                    )
                    Text("Wave Accent Color", style = MaterialTheme.typography.labelLarge)
                    ColorSwatchRow(
                        selectedColorHex = s.niagaraAccentColorHex,
                        colors = TagPresetColors,
                        onSelect = { c -> onUpdateSettings { it.copy(niagaraAccentColorHex = c) } },
                        tagPrefix = "settings_niagara_accent",
                    )
                    }
                }
            }

            // Home Canvas, Grid, Page Indicators & Edge Handles
            if (LauncherSettingsCatalog.SECTION_HOME_CANVAS in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_HOME_CANVAS) {
                    SettingsCard(
                        title = "Home Canvas, Grid & Indicators",
                        icon = Icons.Rounded.ViewCarousel,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_HOME_CANVAS].orEmpty() else emptyList(),
                    ) {
                    ToggleRow("Show Bottom Dock Bar", s.showDockBar) { v ->
                        onUpdateSettings { it.copy(showDockBar = v) }
                    }
                    ToggleRow("Show Quick Tag Strip", s.showTagStripOnHome) { v ->
                        onUpdateSettings { it.copy(showTagStripOnHome = v) }
                    }
                    ToggleRow("Show Labels on Home Shortcuts", s.showHomeAppLabels) { v ->
                        onUpdateSettings { it.copy(showHomeAppLabels = v) }
                    }
                    ToggleRow("Outline App Labels (0.5 px)", s.appLabelOutlineEnabled) { v ->
                        onUpdateSettings { it.copy(appLabelOutlineEnabled = v) }
                    }
                    ToggleRow("Show Default Home Prompt Banner", s.showDefaultHomeBanner) { v ->
                        onUpdateSettings { it.copy(showDefaultHomeBanner = v) }
                    }
                    ToggleRow("Lock Entire Home Layout", s.layoutLocked) { v ->
                        onUpdateSettings { it.copy(layoutLocked = v) }
                    }
                    ToggleRow(
                        label = "Close Folder When Launching App",
                        checked = s.closeFolderOnLaunch,
                        switchModifier = Modifier.testTag("settings_close_folder_on_launch_switch"),
                    ) { v ->
                        onUpdateSettings { it.copy(closeFolderOnLaunch = v) }
                    }
                    ToggleRow(
                        label = "Return to Default Page on Home Press",
                        checked = s.returnToDefaultPageOnHome,
                        switchModifier = Modifier.testTag("settings_return_default_home_switch"),
                    ) { v ->
                        onUpdateSettings { it.copy(returnToDefaultPageOnHome = v) }
                    }
                    ToggleRow(
                        label = "Separate Landscape Layout",
                        checked = s.landscapeSeparateLayout,
                        switchModifier = Modifier.testTag("settings_landscape_separate_switch"),
                    ) { v ->
                        onUpdateSettings { it.copy(landscapeSeparateLayout = v) }
                    }
                    ToggleRow(
                        label = "Allow Overlapping Elements",
                        checked = s.allowOverlappingElements,
                        switchModifier = Modifier.testTag("settings_allow_overlap_switch"),
                    ) { v ->
                        onUpdateSettings { it.copy(allowOverlappingElements = v) }
                    }

                    Text("Double-Tap Empty Page Behavior", style = MaterialTheme.typography.labelLarge)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        listOf(
                            "GESTURE" to "Gesture Binding",
                            "EDIT_MODE" to "Toggle Edit",
                            "OPEN_DRAWER" to "Drawer",
                            "OPEN_SEARCH" to "Search",
                            "NONE" to "None",
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = s.doubleTapPageBehavior == key,
                                onClick = { onUpdateSettings { it.copy(doubleTapPageBehavior = key) } },
                                label = { Text(label) },
                                modifier = Modifier.testTag("settings_double_tap_behavior_$key"),
                            )
                        }
                    }

                    Text("Dock Bar Height: ${s.dockHeightDp.roundToInt()}dp")
                    Slider(
                        value = s.dockHeightDp,
                        onValueChange = { v -> onUpdateSettings { it.copy(dockHeightDp = v) } },
                        valueRange = 40f..80f,
                    )
                    Text("Dock Bar Opacity: ${(s.dockBackgroundAlpha * 100).roundToInt()}%")
                    Slider(
                        value = s.dockBackgroundAlpha,
                        onValueChange = { v -> onUpdateSettings { it.copy(dockBackgroundAlpha = v) } },
                        valueRange = 0f..1f,
                    )
                    Text("Canvas Horizontal Margin: ${s.homePaddingHorizontalDp.roundToInt()}dp")
                    Slider(
                        value = s.homePaddingHorizontalDp,
                        onValueChange = { v -> onUpdateSettings { it.copy(homePaddingHorizontalDp = v) } },
                        valueRange = 0f..32f,
                    )
                    Text("Canvas Vertical Margin: ${s.homePaddingVerticalDp.roundToInt()}dp")
                    Slider(
                        value = s.homePaddingVerticalDp,
                        onValueChange = { v -> onUpdateSettings { it.copy(homePaddingVerticalDp = v) } },
                        valueRange = 0f..32f,
                    )
                    Text("Default Shortcut Width: ${s.defaultShortcutWidthDp.roundToInt()}dp")
                    Slider(
                        value = s.defaultShortcutWidthDp,
                        onValueChange = { v -> onUpdateSettings { it.copy(defaultShortcutWidthDp = v) } },
                        valueRange = 56f..140f,
                    )
                    Text("Default Shortcut Height: ${s.defaultShortcutHeightDp.roundToInt()}dp")
                    Slider(
                        value = s.defaultShortcutHeightDp,
                        onValueChange = { v -> onUpdateSettings { it.copy(defaultShortcutHeightDp = v) } },
                        valueRange = 56f..160f,
                    )

                    FolderAndChromeStudioControls(
                        settings = s,
                        onUpdateSettings = onUpdateSettings,
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    ToggleRow("Grid Snap", s.gridSnapEnabled) { v ->
                        onUpdateSettings { it.copy(gridSnapEnabled = v) }
                    }
                    Text("Grid Snap Step: ${s.gridSnapStepDp}dp")
                    Slider(
                        value = s.gridSnapStepDp.toFloat(),
                        onValueChange = { v -> onUpdateSettings { it.copy(gridSnapStepDp = v.roundToInt().coerceIn(8, 48)) } },
                        valueRange = 8f..48f,
                    )
                    ToggleRow("Show Edit Mode Grid Lines", s.editGridVisible) { v ->
                        onUpdateSettings { it.copy(editGridVisible = v) }
                    }
                    if (s.editGridVisible) {
                        Text("Grid Line Opacity: ${(s.editGridOpacity * 100).roundToInt()}%")
                        Slider(
                            value = s.editGridOpacity,
                            onValueChange = { v -> onUpdateSettings { it.copy(editGridOpacity = v) } },
                            valueRange = 0.02f..0.40f,
                        )
                        Text("Grid Line Color", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = s.editGridColorHex,
                            colors = StudioNeutralAndAccentSwatches,
                            onSelect = { c -> onUpdateSettings { it.copy(editGridColorHex = c) } },
                            tagPrefix = "settings_grid_color",
                        )
                    }
                    ToggleRow("Magnetic Alignment Guides", s.alignmentGuidesEnabled) { v ->
                        onUpdateSettings { it.copy(alignmentGuidesEnabled = v) }
                    }
                    if (s.alignmentGuidesEnabled) {
                        Text("Alignment Guide Color", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = s.alignmentGuideColorHex,
                            colors = TagPresetColors,
                            onSelect = { c -> onUpdateSettings { it.copy(alignmentGuideColorHex = c) } },
                            tagPrefix = "settings_guide_color",
                        )
                    }
                    }
                }
            }

            // Page Transitions, Indicators & Edge Handles
            if (LauncherSettingsCatalog.SECTION_INDICATORS_HANDLES in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_INDICATORS_HANDLES) {
                    SettingsCard(
                        title = "Page Indicators & Edge Handles",
                        icon = Icons.Rounded.ViewCarousel,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_INDICATORS_HANDLES].orEmpty() else emptyList(),
                    ) {
                    ExtendedPagingAndIndicatorControls(
                        settings = s,
                        onUpdateSettings = onUpdateSettings,
                    )

                    ToggleRow("Enable Widget Feed", s.widgetFeedEnabled) { v ->
                        onUpdateSettings { it.copy(widgetFeedEnabled = v) }
                    }
                    Text("Widget Feed Title Color", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("WHITE", "BLACK").forEach { color ->
                            FilterChip(
                                selected = s.widgetFeedTitleColor == color,
                                onClick = { onUpdateSettings { it.copy(widgetFeedTitleColor = color) } },
                                label = { Text(color.lowercase().replaceFirstChar { it.uppercase() }) },
                            )
                        }
                    }
                    Text(
                        "Swipe left past your last home page to open the separate widget feed. Disabling it keeps your feed widgets saved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text("Page Indicator Style", style = MaterialTheme.typography.labelLarge)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        listOf("DOTS", "PILL", "NUMBERS", "PROGRESS", "TABS", "HIDDEN").forEach { st ->
                            FilterChip(
                                selected = s.pageIndicatorStyle == st,
                                onClick = { onUpdateSettings { it.copy(pageIndicatorStyle = st) } },
                                label = { Text(st) },
                                modifier = Modifier.testTag("settings_indicator_style_$st"),
                            )
                        }
                    }
                    if (s.pageIndicatorStyle != "HIDDEN") {
                        Text("Page Indicator Color", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(
                            selectedColorHex = s.pageIndicatorColorHex,
                            colors = TagPresetColors,
                            onSelect = { c -> onUpdateSettings { it.copy(pageIndicatorColorHex = c) } },
                            tagPrefix = "settings_indicator_color",
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    ToggleRow("Show Edge Sidebar Handles", s.edgeHandlesVisible) { v ->
                        onUpdateSettings { it.copy(edgeHandlesVisible = v) }
                    }
                    if (s.edgeHandlesVisible) {
                        Text("Handle Opacity: ${(s.edgeHandleOpacity * 100).roundToInt()}%")
                        Slider(
                            value = s.edgeHandleOpacity,
                            onValueChange = { v -> onUpdateSettings { it.copy(edgeHandleOpacity = v) } },
                            valueRange = 0.10f..1.0f,
                        )
                        Text("Handle Thickness: ${s.edgeHandleWidthDp.roundToInt()}dp")
                        Slider(
                            value = s.edgeHandleWidthDp,
                            onValueChange = { v -> onUpdateSettings { it.copy(edgeHandleWidthDp = v) } },
                            valueRange = 2f..10f,
                        )
                        Text("Handle Length: ${s.edgeHandleHeightDp.roundToInt()}dp")
                        Slider(
                            value = s.edgeHandleHeightDp,
                            onValueChange = { v -> onUpdateSettings { it.copy(edgeHandleHeightDp = v) } },
                            valueRange = 24f..140f,
                        )
                    }
                    }
                }
            }

            // System Bars & Notifications
            if (LauncherSettingsCatalog.SECTION_SYSTEM_UI in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_SYSTEM_UI) {
                    SettingsCard(
                        title = "System UI & Feedback",
                        icon = Icons.Rounded.GridOn,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_SYSTEM_UI].orEmpty() else emptyList(),
                    ) {
                    ToggleRow("Show Android Status Bar", s.showStatusBar) { v ->
                        onUpdateSettings { it.copy(showStatusBar = v) }
                    }
                    ToggleRow("Show System Navigation Bar", s.showNavBar) { v ->
                        onUpdateSettings { it.copy(showNavBar = v) }
                    }
                    ToggleRow(
                        label = "Keep Screen On",
                        checked = s.keepScreenOn,
                        switchModifier = Modifier.testTag("settings_keep_screen_on_switch"),
                    ) { v ->
                        onUpdateSettings { it.copy(keepScreenOn = v) }
                    }
                    Text("Screen Orientation", style = MaterialTheme.typography.labelLarge)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        listOf("SYSTEM", "PORTRAIT", "LANDSCAPE", "SENSOR").forEach { mode ->
                            FilterChip(
                                selected = s.screenOrientationMode == mode,
                                onClick = { onUpdateSettings { it.copy(screenOrientationMode = mode) } },
                                label = { Text(mode) },
                                modifier = Modifier.testTag("settings_orientation_$mode"),
                            )
                        }
                    }
                    Text("Status Bar Icon Contrast", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("AUTO", "LIGHT", "DARK").forEach { st ->
                            FilterChip(
                                selected = s.statusBarIconStyle == st,
                                onClick = { onUpdateSettings { it.copy(statusBarIconStyle = st) } },
                                label = { Text(st) },
                                modifier = Modifier.testTag("settings_statusbar_style_$st"),
                            )
                        }
                    }
                    Text("Navigation Bar Icon Contrast", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("AUTO", "LIGHT", "DARK").forEach { st ->
                            FilterChip(
                                selected = s.navBarIconStyle == st,
                                onClick = { onUpdateSettings { it.copy(navBarIconStyle = st) } },
                                label = { Text(st) },
                                modifier = Modifier.testTag("settings_navbar_style_$st"),
                            )
                        }
                    }
                    ToggleRow("Haptic Feedback", s.hapticFeedbackEnabled) { v ->
                        onUpdateSettings { it.copy(hapticFeedbackEnabled = v) }
                    }

                    Text(
                        "Optional access is requested only for the feature that uses it. You can change these choices later in Android Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(
                        onClick = {
                            if (!hasCalendarAccess) {
                                calendarAccessLauncher.launch(Manifest.permission.READ_CALENDAR)
                            } else {
                                runCatching {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                    )
                                }.onFailure { onShowMessage("Could not open TagDeck app settings") }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (hasCalendarAccess) "Calendar Access: Granted · Manage" else "Allow Calendar Events in At-a-Glance")
                    }
                    Text(
                        if (hasCalendarAccess) "At-a-Glance can show upcoming calendar events."
                        else "Calendar access is optional and is used only to display upcoming events in At-a-Glance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!hasCalendarAccess) {
                        OutlinedButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                    )
                                }.onFailure { onShowMessage("Could not open TagDeck app settings") }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Open TagDeck App Permissions")
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                )
                            }.onFailure {
                                runCatching {
                                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                                }.onFailure { onShowMessage("Could not open Usage Access settings") }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (hasUsageAccess) "Usage Access: Granted · Manage" else "Grant App Usage Access")
                    }
                    Text(
                        if (hasUsageAccess) "The App Usage widget can display the most-used apps from the last 24 hours."
                        else "Usage Access is optional and is used only by the App Usage widget. Android requires you to enable it in Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Notifications, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Notification Badges", style = MaterialTheme.typography.bodyLarge)
                            }
                            Text(
                                "Shows active counts only; TagDeck does not read or store notification text. Android requires separate Notification Access approval. Turning this off hides badges; revoke access in Android Settings to remove permission.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                if (hasNotificationAccess) "Notification Access: Granted"
                                else "Notification Access: Not granted",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasNotificationAccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = s.notificationsBadgeHint,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(notificationsBadgeHint = enabled) }
                                if (enabled && !NotificationAccess.isEnabled(context)) {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                    } catch (_: Exception) {
                                        onShowMessage("Could not open Notification Access settings. Open Android Settings and search for Notification access.")
                                    }
                                } else if (enabled) {
                                    onShowMessage("Notification badges enabled")
                                }
                            },
                        )
                    }
                    }
                }
            }

            // Gestures
            if (LauncherSettingsCatalog.SECTION_GESTURES in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_GESTURES) {
                    SettingsCard(
                        title = "Gestures & Shortcuts",
                        icon = Icons.Rounded.Gesture,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_GESTURES].orEmpty() else emptyList(),
                    ) {
                    Text(
                        text = "Customize swipe and tap gestures on the Home canvas to launch app actions, system tools, or launcher panels.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GestureLabels.forEach { (key, label) ->
                        val binding = state.gestures[key] ?: GestureBindingEntity(key, "NONE")
                        val actionSummary = describeGestureBinding(binding, state)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editingGesture = binding }
                                .padding(vertical = 8.dp)
                                .testTag("gesture_row_$key"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Text(
                                text = actionSummary,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showHomeGesturesDialog = true },
                            modifier = Modifier.weight(1f).testTag("open_gesture_configurator_button"),
                        ) {
                            Text("Configure All")
                        }
                        OutlinedButton(
                            onClick = onResetDefaultGestures,
                            modifier = Modifier.testTag("reset_gestures_button"),
                        ) {
                            Text("Reset Defaults")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Text("Long-Press Hold Delay: ${s.longPressDelayMs}ms")
                    Slider(
                        value = s.longPressDelayMs.toFloat(),
                        onValueChange = { v -> onUpdateSettings { it.copy(longPressDelayMs = v.roundToInt().coerceIn(250, 900)) } },
                        valueRange = 250f..900f,
                        modifier = Modifier.testTag("settings_long_press_delay_slider"),
                    )
                    Text("Swipe Gesture Sensitivity: ${"%.2f".format(s.swipeGestureSensitivity)}x")
                    Slider(
                        value = s.swipeGestureSensitivity,
                        onValueChange = { v -> onUpdateSettings { it.copy(swipeGestureSensitivity = v.coerceIn(0.5f, 1.8f)) } },
                        valueRange = 0.5f..1.8f,
                        modifier = Modifier.testTag("settings_swipe_sensitivity_slider"),
                    )
                    Text("Press Feedback Scale: ${"%.2f".format(s.pressFeedbackScale)}x")
                    Slider(
                        value = s.pressFeedbackScale,
                        onValueChange = { v -> onUpdateSettings { it.copy(pressFeedbackScale = v.coerceIn(0.86f, 1.0f)) } },
                        valueRange = 0.86f..1.0f,
                        modifier = Modifier.testTag("settings_press_scale_slider"),
                    )
                    }
                }
            }

            // Backup & Restore
            if (LauncherSettingsCatalog.SECTION_BACKUP in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_BACKUP) {
                    SettingsCard(
                        title = "Portable Local Backup & Restore",
                        icon = Icons.Rounded.Backup,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_BACKUP].orEmpty() else emptyList(),
                    ) {
                    Text(
                        "Export or restore all layouts, windows, tags, categories, manual overrides, images, sidebars, widgets, and preferences.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider()
                    Text("Automatic Cloud Backup", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Choose a folder from any Android document provider (such as Drive, Dropbox, OneDrive, or local storage). TagDeck keeps its local data as the primary copy and writes a full JSON backup every 12 hours while the device is online.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ToggleRow("Enable automatic cloud backup", s.cloudBackupEnabled) { enabled ->
                        if (!enabled) {
                            onUpdateSettings { it.copy(cloudBackupEnabled = false) }
                            CloudBackupScheduler.cancel(context)
                        } else if (s.cloudBackupTreeUri.isNullOrBlank()) {
                            cloudBackupFolderPicker.launch(null)
                        } else {
                            onUpdateSettings { it.copy(cloudBackupEnabled = true) }
                            CloudBackupScheduler.schedule(context)
                        }
                    }
                    OutlinedButton(
                        onClick = { cloudBackupFolderPicker.launch(null) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (s.cloudBackupTreeUri.isNullOrBlank()) "Choose Cloud Backup Folder" else "Change Cloud Backup Folder")
                    }
                    if (!s.cloudBackupTreeUri.isNullOrBlank()) {
                        Text("Backup destination selected. The latest file is TagDeck-AutoBackup.json.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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

            // Reset All Preferences
            if (LauncherSettingsCatalog.SECTION_RESET in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_RESET) {
                    SettingsCard(
                        title = "Reset Preferences",
                        icon = Icons.Rounded.RestartAlt,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_RESET].orEmpty() else emptyList(),
                    ) {
                        Text(
                            "Restore theme, colors, fonts, wallpaper, grid, indicators, drawer, and icon outline preferences to defaults without removing your pages or apps.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.fillMaxWidth().testTag("reset_all_settings_button"),
                        ) {
                            Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Reset All Launcher Preferences")
                        }
                    }
                }
            }

            if (searchQuery.isNotBlank() && matchingSections.isEmpty()) {
                item(key = "settings_search_empty") {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth().testTag("settings_search_empty"),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp),
                            )
                            Text(
                                text = "No settings match \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Try searching for theme, wallpaper, black outline, grid snap, drawer, gestures, or backup.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            OutlinedButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.testTag("settings_search_reset_query"),
                            ) {
                                Text("Clear Search")
                            }
                        }
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

    if (showHomeGesturesDialog) {
        HomeGesturesConfigDialog(
            state = state,
            onEditGesture = { editingGesture = it },
            onResetDefaults = onResetDefaultGestures,
            onDismiss = { showHomeGesturesDialog = false },
        )
    }

    editingGesture?.let { binding ->
        GestureBindingDialog(
            binding = binding,
            state = state,
            appDiscovery = resolvedDiscovery,
            onSave = {
                onSaveGesture(it)
                editingGesture = null
            },
            onDismiss = { editingGesture = null },
        )
    }

    editingCategoryForIcon?.let { cat ->
        CategoryEditorDialog(
            initialCategory = cat,
            onImportImage = { uri -> onImportImage(uri, "CategoryIcons", null) },
            onSave = { updated ->
                onSaveCategory(updated)
                editingCategoryForIcon = null
            },
            onDismiss = { editingCategoryForIcon = null },
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Launcher Preferences?") },
            text = {
                Text("This resets theme, fonts, palette, wallpaper, grid, page indicators, drawer, and icon outline settings to factory defaults. Your home pages, widgets, apps, and tags remain untouched.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSettings {
                            LauncherSettings(
                                seededDefaults = it.seededDefaults,
                                recentSearchesCsv = it.recentSearchesCsv,
                            )
                        }
                        showResetConfirm = false
                        onShowMessage("All launcher preferences restored to defaults")
                    },
                    modifier = Modifier.testTag("confirm_reset_all_settings_button"),
                ) {
                    Text("Reset Defaults")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
internal fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    matchedEntries: List<SettingEntrySpec> = emptyList(),
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
            if (matchedEntries.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                ) {
                    Text(
                        text = "Matched: ${matchedEntries.joinToString(" • ") { it.title }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            content()
        }
    }
}

@Composable
internal fun ToggleRow(
    label: String,
    checked: Boolean,
    switchModifier: Modifier = Modifier,
    onChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChanged, modifier = switchModifier)
    }
}

@Composable
private fun HomeGesturesConfigDialog(
    state: LauncherUiState,
    onEditGesture: (GestureBindingEntity) -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Gesture, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Custom Home Screen Gestures")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .testTag("home_gestures_config_dialog"),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Tap any swipe or tap gesture below to assign a specific app, app shortcut, system action, or launcher function.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .testTag("home_gestures_config_list"),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(GestureLabels, key = { it.first }) { (key, label) ->
                        val binding = state.gestures[key] ?: GestureBindingEntity(key, "NONE")
                        val desc = describeGestureBinding(binding, state)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditGesture(binding) }
                                .testTag("gesture_config_row_$key"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Configure $label")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onResetDefaults) { Text("Reset Defaults") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GestureBindingDialog(
    binding: GestureBindingEntity,
    state: LauncherUiState,
    appDiscovery: AppDiscoveryManager,
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
    var targetUrl by remember { mutableStateOf(binding.targetUrl ?: "") }
    var appFilter by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val filteredApps = remember(state.appsWithTags, appFilter) {
        val q = appFilter.trim().lowercase()
        val installed = state.appsWithTags.filter { it.app.isInstalled }
        if (q.isEmpty()) installed
        else installed.filter {
            it.app.displayName.lowercase().contains(q) || it.app.packageName.lowercase().contains(q)
        }
    }

    val appShortcuts = remember(actionType, targetPkg, state.appsWithTags) {
        if (actionType == "OPEN_APP_SHORTCUT" && !targetPkg.isNullOrBlank()) {
            val comp = state.appsWithTags.find { it.app.packageName == targetPkg }?.app?.componentName
            appDiscovery.getAppShortcuts(targetPkg!!, comp)
        } else {
            emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure ${GestureLabels.find { it.first == binding.gestureType }?.second ?: binding.gestureType}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("gesture_binding_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = ActionOptions.find { it.first == actionType }?.second ?: actionType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Action / Function") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("gesture_action_dropdown"),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        ActionOptions.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    actionType = key
                                    expanded = false
                                    if (key == "OPEN_SYSTEM_SETTING" && targetUrl.isBlank()) {
                                        targetUrl = Settings.ACTION_SETTINGS
                                    }
                                },
                                modifier = Modifier.testTag("gesture_action_option_$key"),
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
                    "OPEN_APP", "OPEN_APP_INFO", "OPEN_APP_SHORTCUT" -> {
                        OutlinedTextField(
                            value = appFilter,
                            onValueChange = { appFilter = it },
                            placeholder = { Text("Filter apps...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("gesture_app_search_input"),
                        )
                        LazyColumn(modifier = Modifier.height(if (actionType == "OPEN_APP_SHORTCUT") 130.dp else 180.dp)) {
                            items(filteredApps, key = { it.app.packageName }) { item ->
                                Text(
                                    text = (if (targetPkg == item.app.packageName) "● " else "○ ") + item.app.displayName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            targetPkg = item.app.packageName
                                            if (actionType == "OPEN_APP_SHORTCUT") {
                                                targetUrl = ""
                                            }
                                        }
                                        .padding(8.dp)
                                        .testTag("gesture_target_app_${item.app.packageName}"),
                                )
                            }
                        }
                        if (actionType == "OPEN_APP_SHORTCUT" && !targetPkg.isNullOrBlank()) {
                            Text("Select App Shortcut", style = MaterialTheme.typography.labelLarge)
                            if (appShortcuts.isEmpty()) {
                                Text(
                                    text = "This app exposes no dynamic/manifest shortcuts. Triggering it will launch the app directly.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                LazyColumn(modifier = Modifier.height(110.dp)) {
                                    items(appShortcuts, key = { it.id }) { sc ->
                                        val scLabel = sc.shortLabel?.toString().orEmpty().ifBlank { sc.id }
                                        Text(
                                            text = (if (targetUrl == sc.id) "● " else "○ ") + scLabel,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { targetUrl = sc.id }
                                                .padding(8.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    "OPEN_SYSTEM_SETTING" -> {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            SystemSettingOptions.forEach { (actionConst, label) ->
                                FilterChip(
                                    selected = targetUrl == actionConst,
                                    onClick = { targetUrl = actionConst },
                                    label = { Text(label) },
                                )
                            }
                        }
                    }
                    "OPEN_WEBSITE" -> {
                        OutlinedTextField(
                            value = targetUrl,
                            onValueChange = { targetUrl = it },
                            label = { Text("Website URL") },
                            placeholder = { Text("https://example.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("gesture_url_input"),
                        )
                    }
                    "JUMP_PAGE" -> {
                        Text("Select Page", style = MaterialTheme.typography.labelLarge)
                        state.homePages.forEachIndexed { idx, p ->
                            Text(
                                text = (if (targetUrl == idx.toString()) "● " else "○ ") + "${idx + 1}. ${p.title}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { targetUrl = idx.toString() }
                                    .padding(8.dp),
                            )
                        }
                    }
                    "SHOW_OBJECT", "HIDE_OBJECT", "TOGGLE_OBJECT" -> {
                        OutlinedTextField(
                            value = targetUrl,
                            onValueChange = { targetUrl = it },
                            label = { Text("Target Element ID or Visibility Group Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    "OPEN_SYSTEM_PANEL" -> {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            listOf("WIFI_PANEL" to "Wi-Fi", "BLUETOOTH_PANEL" to "Bluetooth", "VOLUME_PANEL" to "Volume", "DISPLAY_SETTINGS" to "Display").forEach { (k, lbl) ->
                                FilterChip(
                                    selected = targetUrl == k,
                                    onClick = { targetUrl = k },
                                    label = { Text(lbl) },
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
                    onSave(
                        binding.copy(
                            actionType = actionType,
                            targetPackageName = targetPkg,
                            targetTagId = targetTagId,
                            targetCategoryId = targetCatId,
                            targetSidebarId = targetSidebarId,
                            targetWindowId = targetWindowId,
                            targetRuleId = targetRuleId,
                            targetUrl = targetUrl.trim().takeIf { it.isNotEmpty() },
                        )
                    )
                },
                modifier = Modifier.testTag("save_gesture_binding_button"),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun createSampleBlackLauncherIconBitmap(): android.graphics.Bitmap {
    val size = 96
    val bmp = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        style = android.graphics.Paint.Style.FILL
    }
    val rect = android.graphics.RectF(20f, 20f, 76f, 76f)
    canvas.drawRoundRect(rect, 18f, 18f, paint)
    val cutout = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
    }
    canvas.drawCircle(48f, 48f, 12f, cutout)
    return bmp
}
