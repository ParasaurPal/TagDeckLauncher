package com.example.ui.screens

import android.Manifest
import android.graphics.Typeface
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import androidx.core.content.ContextCompat
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.content.Intent
import android.net.Uri
import java.io.File
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
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Notifications
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
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.db.BackgroundPresetEntity
import com.example.data.db.CustomFontEntity
import com.example.data.db.CategoryEntity
import com.example.data.db.ThemeProfileEntity
import org.json.JSONObject
import org.json.JSONArray
import com.example.data.db.GestureBindingEntity
import com.example.data.db.ImageResourceEntity
import com.example.data.prefs.LauncherSettings
import com.example.data.system.NotificationAccess
import com.example.data.system.CloudBackupScheduler
import com.example.ui.components.BackgroundPresetDialog
import com.example.ui.components.ColorSwatchRow
import com.example.ui.components.IconOutlineProcessor
import com.example.ui.components.IconStyleConfig
import com.example.ui.components.CategoryBadgeIcon
import com.example.ui.components.CategoryEditorDialog
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
    "SINGLE_TAP" to "Single Tap",
    "DOUBLE_TAP" to "Double Tap",
    "LONG_PRESS" to "Long Press",
    "SWIPE_UP" to "Swipe Up",
    "SWIPE_DOWN" to "Swipe Down",
    "SWIPE_LEFT" to "Swipe Left",
    "SWIPE_RIGHT" to "Swipe Right",
    "TWO_FINGER_SWIPE_UP" to "Two-Finger Swipe Up",
    "TWO_FINGER_SWIPE_DOWN" to "Two-Finger Swipe Down",
    "TWO_FINGER_TAP" to "Two-Finger Tap",
    "PINCH_IN" to "Pinch In",
    "PINCH_OUT" to "Pinch Out",
)

private data class MacroStepDraft(
    val actionType: String = "OPEN_DRAWER",
    val delayMs: Long = 0L,
    val targetElementId: Long? = null,
    val targetPackageName: String? = null,
    val targetTagId: Long? = null,
    val targetCategoryId: Long? = null,
    val targetSidebarId: Long? = null,
    val targetWindowId: Long? = null,
    val targetRuleId: Long? = null,
    val targetUrl: String? = null,
)

private val MacroStepActionOptions = listOf("OPEN_DRAWER", "OPEN_SEARCH", "OPEN_TAG_MANAGER", "OPEN_APP_MANAGER", "OPEN_SETTINGS", "OPEN_TAG", "OPEN_CATEGORY", "OPEN_SIDEBAR", "OPEN_WINDOW", "OPEN_RULE", "OPEN_APP", "OPEN_WEBSITE", "OPEN_SYSTEM_SETTING", "NEXT_PAGE", "PREV_PAGE", "TOGGLE_EDIT_MODE", "SHOW_OBJECT", "HIDE_OBJECT", "TOGGLE_OBJECT", "SHOW_GROUP", "HIDE_GROUP", "TOGGLE_GROUP", "NEXT_STACK_ITEM", "PREVIOUS_STACK_ITEM", "OPEN_WIFI_PANEL", "OPEN_INTERNET_PANEL", "OPEN_VOLUME_PANEL", "OPEN_BLUETOOTH_SETTINGS", "TOGGLE_FLASHLIGHT", "TOGGLE_RINGER")

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
    "SHOW_OBJECT" to "Show Home Object",
    "HIDE_OBJECT" to "Hide Home Object",
    "TOGGLE_OBJECT" to "Toggle Home Object Visibility",
    "NEXT_STACK_ITEM" to "Next Widget Stack Item",
    "PREVIOUS_STACK_ITEM" to "Previous Widget Stack Item",
    "OPEN_WIFI_PANEL" to "Open Wi-Fi Controls",
    "OPEN_INTERNET_PANEL" to "Open Internet Controls",
    "OPEN_VOLUME_PANEL" to "Open Volume Controls",
    "OPEN_BLUETOOTH_SETTINGS" to "Open Bluetooth Settings",
    "TOGGLE_FLASHLIGHT" to "Toggle Flashlight",
    "TOGGLE_RINGER" to "Toggle Ring / Vibrate",
    "OPEN_WEBSITE" to "Open Website",
    "OPEN_SYSTEM_SETTING" to "Open System Setting",
    "OPEN_APP_MANAGER" to "Open App Manager",
    "MACRO" to "Run Action Macro",
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
    onSaveCustomFont: (CustomFontEntity) -> Unit = {},
    onDeleteCustomFont: (Long) -> Unit = {},
    onSaveThemeProfile: (ThemeProfileEntity) -> Unit = {},
    onDeleteThemeProfile: (Long) -> Unit = {},
    onSaveCategory: (CategoryEntity) -> Unit = {},
    onCreateSafetySnapshot: (String) -> Unit = {},
    onExportBackupJson: suspend () -> String,
    onImportBackupJson: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    onRefreshIconPacks: () -> Unit = {},
    onAddIconPack: (String) -> Unit = {},
    onRemoveIconPack: (String) -> Unit = {},
    onMoveIconPack: (String, Int) -> Unit = { _, _ -> },
    initialSearchQuery: String = "",
    onConsumeInitialSearchQuery: () -> Unit = {},
) {
    BackHandler { onBack() }

    val context = LocalContext.current
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
    var editingGesture by remember { mutableStateOf<GestureBindingEntity?>(null) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showImageLibrary by remember { mutableStateOf(false) }
    var showBackgroundPresets by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var newThemeProfileName by remember { mutableStateOf("") }

    val customFontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    val rawName = uri.lastPathSegment?.substringAfterLast(':')?.substringAfterLast('/') ?: "CustomFont.ttf"
                    val safeName = rawName.replace(Regex("[^A-Za-z0-9._-]"), "_")
                    val extension = safeName.substringAfterLast('.', "").lowercase()
                    require(extension == "ttf" || extension == "otf") { "Choose a TTF or OTF font file." }
                    val folder = File(context.filesDir, "custom_fonts").apply { mkdirs() }
                    val destination = File(folder, System.currentTimeMillis().toString() + "_" + safeName)
                    try {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            destination.outputStream().use { output -> input.copyTo(output) }
                        } ?: error("Unable to read selected font.")
                        require(destination.length() in 1L..25L * 1024L * 1024L) { "Font files must be smaller than 25 MB." }
                        val validatedTypeface = Typeface.createFromFile(destination)
                        CustomFontEntity(
                            displayName = safeName.substringBeforeLast('.'),
                            localPath = destination.absolutePath,
                            familyName = safeName.substringBeforeLast('.'),
                        )
                    } catch (error: Throwable) {
                        destination.delete()
                        throw error
                    }
                }.onSuccess { font ->
                    withContext(Dispatchers.Main) { onSaveCustomFont(font); onShowMessage("Font imported: " + font.displayName) }
                }.onFailure { error ->
                    withContext(Dispatchers.Main) { onShowMessage(error.message ?: "Font import failed") }
                }
            }
        }
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
                    }
                }
            }

            if (LauncherSettingsCatalog.SECTION_CATEGORY_ICONS in matchingSections) {
                item(key = LauncherSettingsCatalog.SECTION_CATEGORY_ICONS) {
                    SettingsCard(
                        title = "Category Icons & Open States",
                        icon = Icons.Rounded.Category,
                        matchedEntries = if (searchQuery.isNotBlank()) matchingSections[LauncherSettingsCatalog.SECTION_CATEGORY_ICONS].orEmpty() else emptyList(),
                    ) {
                        Text("Customize each category's closed and open-state badge, including custom images, emoji, and accent colors.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Drawer / sidebar icon fill contrast", style = MaterialTheme.typography.labelMedium)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("BLACK_WHITE" to "Dark fill / light outline", "WHITE_BLACK" to "Light fill / dark outline").forEach { (key, label) ->
                                FilterChip(
                                    selected = s.sidebarCategoryIconStyle == key,
                                    onClick = { onUpdateSettings { it.copy(sidebarCategoryIconStyle = key) } },
                                    label = { Text(label) },
                                )
                            }
                        }
                        LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                            items(state.categories, key = { it.categoryId }) { category ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CategoryBadgeIcon(category, size = 28.dp)
                                    CategoryBadgeIcon(category, size = 28.dp, isOpen = true)
                                    Text(category.name, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    TextButton(onClick = { editingCategory = category }) { Text("Edit") }
                                }
                            }
                        }
                    }
                }
            }

            // Default Home Launcher Card            // Default Home Launcher Card
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

                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            ToggleRow("High-Readability Mode (Global)", s.highReadabilityMode) { enabled ->
                                onUpdateSettings { it.copy(highReadabilityMode = enabled) }
                            }
                            Text("Applies stronger text weight, minimum label sizing, outline, contrast, shadow, and backdrop defaults unless an object overrides them.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Aa  Readable text preview • Clear labels over busy wallpapers", modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.2.sp)
                            }
                            HorizontalDivider()
                            Text("Global Readability Defaults", style = MaterialTheme.typography.titleSmall)
                            ToggleRow("Bold label boost", s.boldLabelsEnabled) { value -> onUpdateSettings { it.copy(boldLabelsEnabled = value) } }
                            ToggleRow("Text outline / halo", s.textOutlineEnabled) { value -> onUpdateSettings { it.copy(textOutlineEnabled = value) } }
                            if (s.textOutlineEnabled) {
                                Text("Outline width: ${"%.1f".format(s.textOutlineWidthDp)}dp")
                                Slider(value = s.textOutlineWidthDp, onValueChange = { value -> onUpdateSettings { it.copy(textOutlineWidthDp = value) } }, valueRange = 0.1f..3f)
                                ColorSwatchRow(selectedColorHex = s.textOutlineColorHex, colors = StudioNeutralAndAccentSwatches, onSelect = { color -> onUpdateSettings { it.copy(textOutlineColorHex = color) } }, tagPrefix = "readability_outline")
                            }
                            ToggleRow("Text drop shadow", s.textShadowEnabled) { value -> onUpdateSettings { it.copy(textShadowEnabled = value) } }
                            if (s.textShadowEnabled) {
                                Text("Shadow radius: ${"%.1f".format(s.textShadowRadiusDp)}dp")
                                Slider(value = s.textShadowRadiusDp, onValueChange = { value -> onUpdateSettings { it.copy(textShadowRadiusDp = value) } }, valueRange = 0f..18f)
                            }
                            ToggleRow("Label backdrop pill", s.labelBackdropEnabled) { value -> onUpdateSettings { it.copy(labelBackdropEnabled = value) } }
                            if (s.labelBackdropEnabled) {
                                ColorSwatchRow(selectedColorHex = s.labelBackdropColorHex, colors = StudioNeutralAndAccentSwatches, onSelect = { color -> onUpdateSettings { it.copy(labelBackdropColorHex = color) } }, tagPrefix = "readability_backdrop")
                                Text("Backdrop opacity: ${(s.labelBackdropAlpha * 100).roundToInt()}%")
                                Slider(value = s.labelBackdropAlpha, onValueChange = { value -> onUpdateSettings { it.copy(labelBackdropAlpha = value) } }, valueRange = 0.05f..1f)
                            }
                            ToggleRow("Automatic text contrast", s.autoContrastText) { value -> onUpdateSettings { it.copy(autoContrastText = value) } }
                            Text("Minimum label size: ${s.minLabelFontSp.roundToInt()}sp")
                            Slider(value = s.minLabelFontSp, onValueChange = { value -> onUpdateSettings { it.copy(minLabelFontSp = value) } }, valueRange = 11f..20f)
                            Text("Letter spacing: ${"%.2f".format(s.letterSpacingEm)}em")
                            Slider(value = s.letterSpacingEm, onValueChange = { value -> onUpdateSettings { it.copy(letterSpacingEm = value) } }, valueRange = -0.05f..0.25f)
                            Text("Line height: ${"%.2f".format(s.lineHeightMultiplier)}x")
                            Slider(value = s.lineHeightMultiplier, onValueChange = { value -> onUpdateSettings { it.copy(lineHeightMultiplier = value) } }, valueRange = 0.8f..2f)
                            Text("Maximum label lines")
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(1, 2, 3).forEach { count ->
                                    FilterChip(selected = s.labelMaxLines == count, onClick = { onUpdateSettings { it.copy(labelMaxLines = count) } }, label = { Text(count.toString()) })
                                }
                            }
                            Text("Custom Font Library", style = MaterialTheme.typography.titleSmall)
                            Text("Import TTF or OTF files. Fonts stay in app storage.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = { customFontPicker.launch(arrayOf("font/ttf", "font/otf", "application/octet-stream", "*/*")) }) { Text("Import") }
                    }
                    if (state.customFonts.isEmpty()) {
                        Text("No custom fonts imported yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        state.customFonts.forEach { font ->
                            val previewFamily = remember(font.fontId, font.localPath) {
                                runCatching { FontFamily(Font(file = File(font.localPath))) }.getOrNull()
                            }
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    FilterChip(selected = s.fontStyle == "CUSTOM_" + font.fontId, onClick = { onUpdateSettings { it.copy(fontStyle = "CUSTOM_" + font.fontId) } }, label = { Text(font.displayName, maxLines = 1) })
                                    Text("Aa Bb Cc 0123", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = previewFamily ?: MaterialTheme.typography.bodyMedium.fontFamily), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { onDeleteCustomFont(font.fontId); if (s.fontStyle == "CUSTOM_" + font.fontId) onUpdateSettings { it.copy(fontStyle = "DEFAULT") } }) { Text("Remove") }
                            }
                        }
                    }

                                        HorizontalDivider()
                    Text("Theme Profiles", style = MaterialTheme.typography.titleSmall)
                    Text("Save and reapply your visual setup.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(value = newThemeProfileName, onValueChange = { newThemeProfileName = it.take(40) }, label = { Text("New theme profile name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(enabled = newThemeProfileName.isNotBlank(), onClick = {
                        val json = JSONObject()
                            .put("themeMode", s.themeMode)
                            .put("accentColorHex", s.accentColorHex)
                            .put("secondaryColorHex", s.secondaryColorHex)
                            .put("tertiaryColorHex", s.tertiaryColorHex)
                            .put("fontStyle", s.fontStyle)
                            .put("fontScale", s.fontScale.toDouble())
                            .put("highReadabilityMode", s.highReadabilityMode)
                            .put("boldLabelsEnabled", s.boldLabelsEnabled)
                            .put("labelBackdropEnabled", s.labelBackdropEnabled)
                            .put("labelBackdropColorHex", s.labelBackdropColorHex)
                            .put("labelBackdropAlpha", s.labelBackdropAlpha.toDouble())
                            .put("textOutlineEnabled", s.textOutlineEnabled)
                            .put("textOutlineColorHex", s.textOutlineColorHex)
                            .put("textOutlineWidthDp", s.textOutlineWidthDp.toDouble())
                            .put("textShadowEnabled", s.textShadowEnabled)
                            .put("textShadowRadiusDp", s.textShadowRadiusDp.toDouble())
                            .put("letterSpacingEm", s.letterSpacingEm.toDouble())
                            .put("lineHeightMultiplier", s.lineHeightMultiplier.toDouble())
                            .put("minLabelFontSp", s.minLabelFontSp.toDouble())
                            .put("labelMaxLines", s.labelMaxLines)
                            .put("autoContrastText", s.autoContrastText)
                            .put("surfaceOpacity", s.surfaceOpacity.toDouble())
                            .put("globalBorderWidthDp", s.globalBorderWidthDp.toDouble())
                            .put("globalTextColorHex", s.globalTextColorHex)
                            .put("globalCornerRadiusDp", s.globalCornerRadiusDp.toDouble())
                            .put("globalIconShape", s.globalIconShape)
                            .put("wallpaperType", s.wallpaperType)
                            .put("wallpaperCustomPath", s.wallpaperCustomPath ?: "")
                            .put("wallpaperGradientStartHex", s.wallpaperGradientStartHex)
                            .put("wallpaperGradientEndHex", s.wallpaperGradientEndHex)
                            .put("wallpaperGradientDirection", s.wallpaperGradientDirection)
                            .put("wallpaperSolidHex", s.wallpaperSolidHex)
                            .put("wallpaperBlurDp", s.wallpaperBlurDp.toDouble())
                            .put("wallpaperDimAlpha", s.wallpaperDimAlpha.toDouble())
                            .put("wallpaperDimColorHex", s.wallpaperDimColorHex)
                            .put("wallpaperVignette", s.wallpaperVignette.toDouble())
                            .put("wallpaperSaturation", s.wallpaperSaturation.toDouble())
                            .put("wallpaperContrast", s.wallpaperContrast.toDouble())
                            .put("wallpaperBrightness", s.wallpaperBrightness.toDouble())
                            .put("wallpaperParallaxIntensity", s.wallpaperParallaxIntensity.toDouble())
                            .put("wallpaperTintHex", s.wallpaperTintHex ?: JSONObject.NULL)
                            .put("wallpaperAmbientMotion", s.wallpaperAmbientMotion)
                            .put("wallpaperAmbientEffectType", s.wallpaperAmbientEffectType)
                            .put("sidebarTextColorMode", s.sidebarTextColorMode)
                            .put("sidebarCategoryIconStyle", s.sidebarCategoryIconStyle)
                            .put("showHomeAppLabels", s.showHomeAppLabels)
                            .put("appLabelOutlineEnabled", s.appLabelOutlineEnabled)
                            .put("allAppsSidebarEdge", s.allAppsSidebarEdge)
                            .put("allAppsSidebarWidthFraction", s.allAppsSidebarWidthFraction.toDouble())
                            .put("allAppsBgType", s.allAppsBgType)
                            .put("allAppsBgColorHex", s.allAppsBgColorHex)
                            .put("allAppsBgGradientEndHex", s.allAppsBgGradientEndHex)
                            .put("drawerLayoutMode", s.drawerLayoutMode)
                            .put("drawerColumns", s.drawerColumns)
                            .put("drawerIconSizeDp", s.drawerIconSizeDp)
                            .put("drawerLabelSizeSp", s.drawerLabelSizeSp)
                            .put("drawerSortOrder", s.drawerSortOrder)
                            .put("drawerCardOpacity", s.drawerCardOpacity.toDouble())
                            .put("drawerCornerRadiusDp", s.drawerCornerRadiusDp.toDouble())
                            .put("drawerSearchBarVisible", s.drawerSearchBarVisible)
                            .put("drawerSearchBarPosition", s.drawerSearchBarPosition)
                            .put("drawerCardShadowDp", s.drawerCardShadowDp.toDouble())
                            .put("drawerCardBorderWidthDp", s.drawerCardBorderWidthDp.toDouble())
                            .put("drawerCardBorderColorHex", s.drawerCardBorderColorHex)
                            .put("pageTransitionStyle", s.pageTransitionStyle)
                            .put("pageIndicatorStyle", s.pageIndicatorStyle)
                            .put("pageIndicatorPlacement", s.pageIndicatorPlacement)
                            .put("pageIndicatorColorHex", s.pageIndicatorColorHex)
                            .put("pageIndicatorSizeDp", s.pageIndicatorSizeDp.toDouble())
                            .put("pageIndicatorActiveWidthDp", s.pageIndicatorActiveWidthDp.toDouble())
                            .put("statusStripEnabled", s.statusStripEnabled)
                            .put("statusStripShowDate", s.statusStripShowDate)
                            .put("statusStripShowBattery", s.statusStripShowBattery)
                            .put("statusStripAccentColorHex", s.statusStripAccentColorHex)
                            .put("widgetFeedEnabled", s.widgetFeedEnabled)
                            .put("widgetFeedTitleColor", s.widgetFeedTitleColor)
                            .put("allAppsBgBlurDp", s.allAppsBgBlurDp.toDouble())
                            .put("allAppsBgImagePath", s.allAppsBgImagePath ?: "")
                            .put("niagaraWaveEnabled", s.niagaraWaveEnabled)
                            .put("niagaraIndexVisible", s.niagaraIndexVisible)
                            .put("niagaraEdge", s.niagaraEdge)
                            .put("niagaraWaveIntensity", s.niagaraWaveIntensity.toDouble())
                            .put("niagaraBaseFontSp", s.niagaraBaseFontSp.toDouble())
                            .put("niagaraSelectedFontSp", s.niagaraSelectedFontSp.toDouble())
                            .put("niagaraAccentColorHex", s.niagaraAccentColorHex)
                            .put("niagaraIndexWidthDp", s.niagaraIndexWidthDp.toDouble())
                            .put("niagaraIndexColorHex", s.niagaraIndexColorHex)
                            .put("niagaraHapticFeedback", s.niagaraHapticFeedback)
                        onSaveThemeProfile(ThemeProfileEntity(name = newThemeProfileName.trim(), settingsJson = json.toString()))
                        newThemeProfileName = ""
                    }) { Text("Save Current Theme") }
                    if (state.themeProfiles.isEmpty()) Text("No saved theme profiles.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    state.themeProfiles.forEach { profile ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(profile.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = {
                                onCreateSafetySnapshot("Before theme ${profile.name}")
                                val json = JSONObject(profile.settingsJson)
                                onUpdateSettings { current -> current.copy(
                                    themeMode = json.optString("themeMode", current.themeMode),
                                    accentColorHex = json.optLong("accentColorHex", current.accentColorHex),
                                    secondaryColorHex = json.optLong("secondaryColorHex", current.secondaryColorHex),
                                    tertiaryColorHex = json.optLong("tertiaryColorHex", current.tertiaryColorHex),
                                    fontStyle = json.optString("fontStyle", current.fontStyle),
                                    fontScale = json.optDouble("fontScale", current.fontScale.toDouble()).toFloat(),
                                    highReadabilityMode = json.optBoolean("highReadabilityMode", current.highReadabilityMode),
                                    boldLabelsEnabled = json.optBoolean("boldLabelsEnabled", current.boldLabelsEnabled),
                                    labelBackdropEnabled = json.optBoolean("labelBackdropEnabled", current.labelBackdropEnabled),
                                    labelBackdropColorHex = json.optLong("labelBackdropColorHex", current.labelBackdropColorHex),
                                    labelBackdropAlpha = json.optDouble("labelBackdropAlpha", current.labelBackdropAlpha.toDouble()).toFloat(),
                                    textOutlineEnabled = json.optBoolean("textOutlineEnabled", current.textOutlineEnabled),
                                    textOutlineColorHex = json.optLong("textOutlineColorHex", current.textOutlineColorHex),
                                    textOutlineWidthDp = json.optDouble("textOutlineWidthDp", current.textOutlineWidthDp.toDouble()).toFloat(),
                                    textShadowEnabled = json.optBoolean("textShadowEnabled", current.textShadowEnabled),
                                    textShadowRadiusDp = json.optDouble("textShadowRadiusDp", current.textShadowRadiusDp.toDouble()).toFloat(),
                                    letterSpacingEm = json.optDouble("letterSpacingEm", current.letterSpacingEm.toDouble()).toFloat(),
                                    lineHeightMultiplier = json.optDouble("lineHeightMultiplier", current.lineHeightMultiplier.toDouble()).toFloat(),
                                    minLabelFontSp = json.optDouble("minLabelFontSp", current.minLabelFontSp.toDouble()).toFloat(),
                                    labelMaxLines = json.optInt("labelMaxLines", current.labelMaxLines),
                                    autoContrastText = json.optBoolean("autoContrastText", current.autoContrastText),
                                    surfaceOpacity = json.optDouble("surfaceOpacity", current.surfaceOpacity.toDouble()).toFloat(),
                                    globalBorderWidthDp = json.optDouble("globalBorderWidthDp", current.globalBorderWidthDp.toDouble()).toFloat(),
                                    globalTextColorHex = json.optLong("globalTextColorHex", current.globalTextColorHex),
                                    globalCornerRadiusDp = json.optDouble("globalCornerRadiusDp", current.globalCornerRadiusDp.toDouble()).toFloat(),
                                    globalIconShape = json.optString("globalIconShape", current.globalIconShape),
                                    wallpaperType = json.optString("wallpaperType", current.wallpaperType),
                                    wallpaperCustomPath = if (json.has("wallpaperCustomPath")) json.optString("wallpaperCustomPath", "").takeIf { it.isNotBlank() } else current.wallpaperCustomPath,
                                    wallpaperGradientStartHex = json.optLong("wallpaperGradientStartHex", current.wallpaperGradientStartHex),
                                    wallpaperGradientEndHex = json.optLong("wallpaperGradientEndHex", current.wallpaperGradientEndHex),
                                    wallpaperGradientDirection = json.optString("wallpaperGradientDirection", current.wallpaperGradientDirection),
                                    wallpaperSolidHex = json.optLong("wallpaperSolidHex", current.wallpaperSolidHex),
                                    wallpaperBlurDp = json.optDouble("wallpaperBlurDp", current.wallpaperBlurDp.toDouble()).toFloat(),
                                    wallpaperDimAlpha = json.optDouble("wallpaperDimAlpha", current.wallpaperDimAlpha.toDouble()).toFloat(),
                                    wallpaperDimColorHex = json.optLong("wallpaperDimColorHex", current.wallpaperDimColorHex),
                                    wallpaperVignette = json.optDouble("wallpaperVignette", current.wallpaperVignette.toDouble()).toFloat(),
                                    wallpaperSaturation = json.optDouble("wallpaperSaturation", current.wallpaperSaturation.toDouble()).toFloat(),
                                    wallpaperContrast = json.optDouble("wallpaperContrast", current.wallpaperContrast.toDouble()).toFloat(),
                                    wallpaperBrightness = json.optDouble("wallpaperBrightness", current.wallpaperBrightness.toDouble()).toFloat(),
                                    wallpaperParallaxIntensity = json.optDouble("wallpaperParallaxIntensity", current.wallpaperParallaxIntensity.toDouble()).toFloat(),
                                    wallpaperTintHex = if (!json.has("wallpaperTintHex") || json.isNull("wallpaperTintHex")) null else json.optLong("wallpaperTintHex", 0L),
                                    wallpaperAmbientMotion = json.optBoolean("wallpaperAmbientMotion", current.wallpaperAmbientMotion),
                                    wallpaperAmbientEffectType = json.optString("wallpaperAmbientEffectType", current.wallpaperAmbientEffectType),
                                    sidebarTextColorMode = json.optString("sidebarTextColorMode", current.sidebarTextColorMode),
                                    sidebarCategoryIconStyle = json.optString("sidebarCategoryIconStyle", current.sidebarCategoryIconStyle),
                                    showHomeAppLabels = json.optBoolean("showHomeAppLabels", current.showHomeAppLabels),
                                    appLabelOutlineEnabled = json.optBoolean("appLabelOutlineEnabled", current.appLabelOutlineEnabled),
                                    allAppsSidebarEdge = json.optString("allAppsSidebarEdge", current.allAppsSidebarEdge),
                                    allAppsSidebarWidthFraction = json.optDouble("allAppsSidebarWidthFraction", current.allAppsSidebarWidthFraction.toDouble()).toFloat(),
                                    allAppsBgType = json.optString("allAppsBgType", current.allAppsBgType),
                                    allAppsBgColorHex = json.optLong("allAppsBgColorHex", current.allAppsBgColorHex),
                                    allAppsBgGradientEndHex = json.optLong("allAppsBgGradientEndHex", current.allAppsBgGradientEndHex),
                                    drawerLayoutMode = json.optString("drawerLayoutMode", current.drawerLayoutMode),
                                    drawerColumns = json.optInt("drawerColumns", current.drawerColumns),
                                    drawerIconSizeDp = json.optInt("drawerIconSizeDp", current.drawerIconSizeDp),
                                    drawerLabelSizeSp = json.optInt("drawerLabelSizeSp", current.drawerLabelSizeSp),
                                    drawerSortOrder = json.optString("drawerSortOrder", current.drawerSortOrder),
                                    drawerCardOpacity = json.optDouble("drawerCardOpacity", current.drawerCardOpacity.toDouble()).toFloat(),
                                    drawerCornerRadiusDp = json.optDouble("drawerCornerRadiusDp", current.drawerCornerRadiusDp.toDouble()).toFloat(),
                                    drawerSearchBarVisible = json.optBoolean("drawerSearchBarVisible", current.drawerSearchBarVisible),
                                    drawerSearchBarPosition = json.optString("drawerSearchBarPosition", current.drawerSearchBarPosition),
                                    drawerCardShadowDp = json.optDouble("drawerCardShadowDp", current.drawerCardShadowDp.toDouble()).toFloat(),
                                    drawerCardBorderWidthDp = json.optDouble("drawerCardBorderWidthDp", current.drawerCardBorderWidthDp.toDouble()).toFloat(),
                                    drawerCardBorderColorHex = json.optLong("drawerCardBorderColorHex", current.drawerCardBorderColorHex),
                                    pageTransitionStyle = json.optString("pageTransitionStyle", current.pageTransitionStyle),
                                    pageIndicatorStyle = json.optString("pageIndicatorStyle", current.pageIndicatorStyle),
                                    pageIndicatorPlacement = json.optString("pageIndicatorPlacement", current.pageIndicatorPlacement),
                                    pageIndicatorColorHex = json.optLong("pageIndicatorColorHex", current.pageIndicatorColorHex),
                                    pageIndicatorSizeDp = json.optDouble("pageIndicatorSizeDp", current.pageIndicatorSizeDp.toDouble()).toFloat(),
                                    pageIndicatorActiveWidthDp = json.optDouble("pageIndicatorActiveWidthDp", current.pageIndicatorActiveWidthDp.toDouble()).toFloat(),
                                    statusStripEnabled = json.optBoolean("statusStripEnabled", current.statusStripEnabled),
                                    statusStripShowDate = json.optBoolean("statusStripShowDate", current.statusStripShowDate),
                                    statusStripShowBattery = json.optBoolean("statusStripShowBattery", current.statusStripShowBattery),
                                    statusStripAccentColorHex = json.optLong("statusStripAccentColorHex", current.statusStripAccentColorHex),
                                    widgetFeedEnabled = json.optBoolean("widgetFeedEnabled", current.widgetFeedEnabled),
                                    widgetFeedTitleColor = json.optString("widgetFeedTitleColor", current.widgetFeedTitleColor),
                                    globalCornerRadiusDp = json.optDouble("globalCornerRadiusDp", current.globalCornerRadiusDp.toDouble()).toFloat(),
                                    globalIconShape = json.optString("globalIconShape", current.globalIconShape),
                                    globalBorderWidthDp = json.optDouble("globalBorderWidthDp", current.globalBorderWidthDp.toDouble()).toFloat(),
                                    surfaceOpacity = json.optDouble("surfaceOpacity", current.surfaceOpacity.toDouble()).toFloat(),
                                    globalTextColorHex = json.optLong("globalTextColorHex", current.globalTextColorHex),
                                    allAppsBgBlurDp = json.optDouble("allAppsBgBlurDp", current.allAppsBgBlurDp.toDouble()).toFloat(),
                                    allAppsBgImagePath = if (json.has("allAppsBgImagePath")) json.optString("allAppsBgImagePath", "").takeIf { it.isNotBlank() } else current.allAppsBgImagePath,
                                    niagaraWaveEnabled = json.optBoolean("niagaraWaveEnabled", current.niagaraWaveEnabled),
                                    niagaraIndexVisible = json.optBoolean("niagaraIndexVisible", current.niagaraIndexVisible),
                                    niagaraEdge = json.optString("niagaraEdge", current.niagaraEdge),
                                    niagaraWaveIntensity = json.optDouble("niagaraWaveIntensity", current.niagaraWaveIntensity.toDouble()).toFloat(),
                                    niagaraBaseFontSp = json.optDouble("niagaraBaseFontSp", current.niagaraBaseFontSp.toDouble()).toFloat(),
                                    niagaraSelectedFontSp = json.optDouble("niagaraSelectedFontSp", current.niagaraSelectedFontSp.toDouble()).toFloat(),
                                    niagaraAccentColorHex = json.optLong("niagaraAccentColorHex", current.niagaraAccentColorHex),
                                    niagaraIndexWidthDp = json.optDouble("niagaraIndexWidthDp", current.niagaraIndexWidthDp.toDouble()).toFloat(),
                                    niagaraIndexColorHex = json.optLong("niagaraIndexColorHex", current.niagaraIndexColorHex),
                                    niagaraHapticFeedback = json.optBoolean("niagaraHapticFeedback", current.niagaraHapticFeedback),
                                ) }
                                onShowMessage("Applied theme " + profile.name)
                            }) { Text("Apply") }
                            TextButton(onClick = { onDeleteThemeProfile(profile.profileId) }) { Text("Delete") }
                        }
                    }

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
                        listOf("SQUIRCLE", "CIRCLE", "ROUNDED_SQUARE", "PILL", "DIAMOND", "TRIANGLE", "PENTAGON", "HEXAGON", "OCTAGON", "STAR", "ARCH", "CUT_CORNER").forEach { shp ->
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
                    HorizontalDivider()
                    Text("Vignette: ${(s.wallpaperVignette * 100).roundToInt()}%")
                    Slider(value = s.wallpaperVignette, onValueChange = { v -> onUpdateSettings { it.copy(wallpaperVignette = v) } }, valueRange = 0f..0.9f)
                    Text("Color Saturation: ${(s.wallpaperSaturation * 100).roundToInt()}%")
                    Slider(value = s.wallpaperSaturation, onValueChange = { v -> onUpdateSettings { it.copy(wallpaperSaturation = v) } }, valueRange = 0f..2f)
                    Text("Color Contrast: ${(s.wallpaperContrast * 100).roundToInt()}%")
                    Slider(value = s.wallpaperContrast, onValueChange = { v -> onUpdateSettings { it.copy(wallpaperContrast = v) } }, valueRange = 0f..2f)
                    Text("Brightness: ${(s.wallpaperBrightness * 100).roundToInt()}%")
                    Slider(value = s.wallpaperBrightness, onValueChange = { v -> onUpdateSettings { it.copy(wallpaperBrightness = v) } }, valueRange = -1f..1f)
                    Text("Wallpaper Parallax: ${(s.wallpaperParallaxIntensity * 100).roundToInt()}%")
                    Slider(value = s.wallpaperParallaxIntensity, onValueChange = { v -> onUpdateSettings { it.copy(wallpaperParallaxIntensity = v) } }, valueRange = 0f..1f)
                    ToggleRow("Ambient Wallpaper Motion", s.wallpaperAmbientMotion) { v -> onUpdateSettings { it.copy(wallpaperAmbientMotion = v) } }
                    Text("Ambient Effect Style", style = MaterialTheme.typography.labelLarge)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("RADIAL", "AURORA", "GLOW", "PULSE").forEach { effect ->
                            FilterChip(selected = s.wallpaperAmbientEffectType == effect, onClick = { onUpdateSettings { it.copy(wallpaperAmbientEffectType = effect) } }, label = { Text(effect) })
                        }
                    }
                    Text("Color Grading Tint", style = MaterialTheme.typography.labelLarge)
                    ColorSwatchRow(
                        selectedColorHex = s.wallpaperTintHex ?: 0x00000000L,
                        colors = StudioNeutralAndAccentSwatches,
                        allowClear = true,
                        onClear = { onUpdateSettings { it.copy(wallpaperTintHex = null) } },
                        onSelect = { color -> onUpdateSettings { it.copy(wallpaperTintHex = color) } },
                        tagPrefix = "settings_wp_grade",
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
                    Text("Search Bar Position", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("TOP", "BOTTOM").forEach { position ->
                            FilterChip(selected = s.drawerSearchBarPosition == position, onClick = { onUpdateSettings { it.copy(drawerSearchBarPosition = position) } }, label = { Text(position) })
                        }
                    }
                    Text("App Card Shadow: ${s.drawerCardShadowDp.roundToInt()}dp")
                    Slider(value = s.drawerCardShadowDp, onValueChange = { v -> onUpdateSettings { it.copy(drawerCardShadowDp = v) } }, valueRange = 0f..20f)
                    Text("App Card Border: ${"%.1f".format(s.drawerCardBorderWidthDp)}dp")
                    Slider(value = s.drawerCardBorderWidthDp, onValueChange = { v -> onUpdateSettings { it.copy(drawerCardBorderWidthDp = v) } }, valueRange = 0f..4f)
                    Text("App Card Border Color", style = MaterialTheme.typography.labelLarge)
                    ColorSwatchRow(selectedColorHex = s.drawerCardBorderColorHex, colors = TagPresetColors, onSelect = { c -> onUpdateSettings { it.copy(drawerCardBorderColorHex = c) } }, tagPrefix = "settings_drawer_card_border")

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
                    Text("Alphabet Rail Width: ${s.niagaraIndexWidthDp.roundToInt()}dp")
                    Slider(value = s.niagaraIndexWidthDp, onValueChange = { v -> onUpdateSettings { it.copy(niagaraIndexWidthDp = v) } }, valueRange = 18f..48f)
                    Text("Alphabet Letter Color", style = MaterialTheme.typography.labelLarge)
                    ColorSwatchRow(selectedColorHex = s.niagaraIndexColorHex, colors = TagPresetColors, onSelect = { c -> onUpdateSettings { it.copy(niagaraIndexColorHex = c) } }, tagPrefix = "settings_niagara_index_color")
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
                    Text("Page Transition Style", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SLIDE", "DEPTH", "FADE", "ZOOM", "CUBE", "FLIP", "ROTATE", "PARALLAX", "STACK", "ACCORDION", "WINDMILL").forEach { tr ->
                            FilterChip(
                                selected = s.pageTransitionStyle == tr,
                                onClick = { onUpdateSettings { it.copy(pageTransitionStyle = tr) } },
                                label = { Text(tr) },
                            )
                        }
                    }

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
                    Text("Page Indicator Placement", style = MaterialTheme.typography.labelLarge)
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("TOP", "BOTTOM").forEach { placement ->
                            FilterChip(
                                selected = s.pageIndicatorPlacement == placement,
                                onClick = { onUpdateSettings { it.copy(pageIndicatorPlacement = placement) } },
                                label = { Text(placement) },
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

                    if (s.pageIndicatorStyle != "HIDDEN") {
                        Text("Inactive Dot Size: ${s.pageIndicatorSizeDp.roundToInt()}dp")
                        Slider(value = s.pageIndicatorSizeDp, onValueChange = { v -> onUpdateSettings { it.copy(pageIndicatorSizeDp = v) } }, valueRange = 3f..16f)
                        Text("Active Pill Width: ${s.pageIndicatorActiveWidthDp.roundToInt()}dp")
                        Slider(value = s.pageIndicatorActiveWidthDp, onValueChange = { v -> onUpdateSettings { it.copy(pageIndicatorActiveWidthDp = v) } }, valueRange = 10f..48f)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    ToggleRow("Show Custom Status Strip", s.statusStripEnabled) { v ->
                        onUpdateSettings { it.copy(statusStripEnabled = v) }
                    }
                    if (s.statusStripEnabled) {
                        ToggleRow("Show Date in Status Strip", s.statusStripShowDate) { v -> onUpdateSettings { it.copy(statusStripShowDate = v) } }
                        ToggleRow("Show Battery in Status Strip", s.statusStripShowBattery) { v -> onUpdateSettings { it.copy(statusStripShowBattery = v) } }
                        Text("Status Strip Accent", style = MaterialTheme.typography.labelLarge)
                        ColorSwatchRow(selectedColorHex = s.statusStripAccentColorHex, colors = TagPresetColors, onSelect = { c -> onUpdateSettings { it.copy(statusStripAccentColorHex = c) } }, tagPrefix = "settings_status_strip")
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

    editingCategory?.let { category ->
        CategoryEditorDialog(
            initialCategory = category,
            onImportImage = { uri -> onImportImage(uri, "Category Icons", null) },
            onSave = { updated -> onSaveCategory(updated); editingCategory = null },
            onDismiss = { editingCategory = null },
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
private fun SettingsCard(
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
    val macroSteps = remember(binding.gestureType) {
        mutableStateListOf<MacroStepDraft>().apply {
            runCatching {
                val arr = JSONArray(binding.macroStepsJson ?: "[]")
                for (i in 0 until arr.length()) {
                    val step = arr.getJSONObject(i)
                    add(MacroStepDraft(
                        actionType = step.optString("actionType", "OPEN_DRAWER"),
                        delayMs = step.optLong("delayMs", 0L),
                        targetElementId = if (step.has("targetElementId") && !step.isNull("targetElementId")) step.optLong("targetElementId") else null,
                        targetPackageName = step.optString("targetPackage").takeIf { it.isNotBlank() },
                        targetTagId = if (step.has("targetTagId") && !step.isNull("targetTagId")) step.optLong("targetTagId") else null,
                        targetCategoryId = if (step.has("targetCategoryId") && !step.isNull("targetCategoryId")) step.optLong("targetCategoryId") else null,
                        targetSidebarId = if (step.has("targetSidebarId") && !step.isNull("targetSidebarId")) step.optLong("targetSidebarId") else null,
                        targetWindowId = if (step.has("targetWindowId") && !step.isNull("targetWindowId")) step.optLong("targetWindowId") else null,
                        targetRuleId = if (step.has("targetRuleId") && !step.isNull("targetRuleId")) step.optLong("targetRuleId") else null,
                        targetUrl = step.optString("targetUrl").takeIf { it.isNotBlank() },
                    ))
                }
            }
        }
    }
    var expandedMacroStep by remember { mutableIntStateOf(-1) }

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
                if (actionType == "MACRO") {
                    HorizontalDivider()
                    Text("Macro Steps", style = MaterialTheme.typography.labelLarge)
                    Text("Actions run in order. Each step can wait up to five seconds before the next action.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(modifier = Modifier.heightIn(max = 260.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        macroSteps.forEachIndexed { index, step ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box {
                                    TextButton(onClick = { expandedMacroStep = index }) { Text(step.actionType.replace('_', ' '), maxLines = 1) }
                                    DropdownMenu(expanded = expandedMacroStep == index, onDismissRequest = { expandedMacroStep = -1 }) {
                                        MacroStepActionOptions.forEach { action ->
                                            DropdownMenuItem(
                                                text = { Text(action.replace('_', ' ')) },
                                                onClick = {
                                                    macroSteps[index] = step.copy(
                                                        actionType = action,
                                                        targetElementId = if (action in setOf("SHOW_OBJECT", "HIDE_OBJECT", "TOGGLE_OBJECT", "NEXT_STACK_ITEM", "PREVIOUS_STACK_ITEM")) step.targetElementId else null,
                                                    )
                                                    expandedMacroStep = -1
                                                },
                                            )
                                        }
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Wait: ${step.delayMs}ms", style = MaterialTheme.typography.labelSmall)
                                    Slider(value = step.delayMs.toFloat(), onValueChange = { macroSteps[index] = step.copy(delayMs = it.toLong()) }, valueRange = 0f..5000f, steps = 9)
                                }
                                TextButton(onClick = { macroSteps.removeAt(index) }) { Text("Remove") }
                            }
                            if (step.actionType in setOf("SHOW_OBJECT", "HIDE_OBJECT", "TOGGLE_OBJECT", "SHOW_GROUP", "HIDE_GROUP", "TOGGLE_GROUP", "NEXT_STACK_ITEM", "PREVIOUS_STACK_ITEM")) {
                                val candidates = state.homeElements.filter { element ->
                                    when (step.actionType) {
                                        "NEXT_STACK_ITEM", "PREVIOUS_STACK_ITEM" -> element.type == "WIDGET_STACK"
                                        "SHOW_GROUP", "HIDE_GROUP", "TOGGLE_GROUP" -> !element.visibilityGroupId.isNullOrBlank()
                                        else -> element.type != "WIDGET_STACK"
                                    }
                                }
                                LazyColumn(modifier = Modifier.fillMaxWidth().height(112.dp)) {
                                    items(candidates, key = { it.elementId }) { element ->
                                        Text(
                                            text = (if (step.targetElementId == element.elementId) "● " else "○ ") + element.label.ifBlank { element.type.replace('_', ' ') },
                                            modifier = Modifier.fillMaxWidth().clickable { macroSteps[index] = step.copy(targetElementId = element.elementId) }.padding(8.dp),
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                            when (step.actionType) {
                                "OPEN_TAG" -> LazyColumn(modifier = Modifier.fillMaxWidth().height(112.dp)) {
                                    items(state.allTags, key = { it.tagId }) { tag ->
                                        Text((if (step.targetTagId == tag.tagId) "● " else "○ ") + tag.name, modifier = Modifier.fillMaxWidth().clickable { macroSteps[index] = step.copy(targetTagId = tag.tagId) }.padding(8.dp))
                                    }
                                }
                                "OPEN_CATEGORY" -> LazyColumn(modifier = Modifier.fillMaxWidth().height(112.dp)) {
                                    items(state.categories, key = { it.categoryId }) { category ->
                                        Text((if (step.targetCategoryId == category.categoryId) "● " else "○ ") + category.name, modifier = Modifier.fillMaxWidth().clickable { macroSteps[index] = step.copy(targetCategoryId = category.categoryId) }.padding(8.dp))
                                    }
                                }
                                "OPEN_SIDEBAR" -> LazyColumn(modifier = Modifier.fillMaxWidth().height(112.dp)) {
                                    items(state.sidebars, key = { it.sidebarId }) { sidebar ->
                                        Text((if (step.targetSidebarId == sidebar.sidebarId) "● " else "○ ") + sidebar.title, modifier = Modifier.fillMaxWidth().clickable { macroSteps[index] = step.copy(targetSidebarId = sidebar.sidebarId) }.padding(8.dp))
                                    }
                                }
                                "OPEN_WINDOW" -> LazyColumn(modifier = Modifier.fillMaxWidth().height(112.dp)) {
                                    items(state.windows, key = { it.windowId }) { window ->
                                        Text((if (step.targetWindowId == window.windowId) "● " else "○ ") + window.title, modifier = Modifier.fillMaxWidth().clickable { macroSteps[index] = step.copy(targetWindowId = window.windowId) }.padding(8.dp))
                                    }
                                }
                                "OPEN_RULE" -> LazyColumn(modifier = Modifier.fillMaxWidth().height(112.dp)) {
                                    items(state.savedRules, key = { it.ruleId }) { rule ->
                                        Text((if (step.targetRuleId == rule.ruleId) "● " else "○ ") + rule.name, modifier = Modifier.fillMaxWidth().clickable { macroSteps[index] = step.copy(targetRuleId = rule.ruleId) }.padding(8.dp))
                                    }
                                }
                                "OPEN_APP" -> LazyColumn(modifier = Modifier.fillMaxWidth().height(112.dp)) {
                                    items(state.appsWithTags, key = { it.app.packageName }) { item ->
                                        Text((if (step.targetPackageName == item.app.packageName) "● " else "○ ") + item.app.displayName, modifier = Modifier.fillMaxWidth().clickable { macroSteps[index] = step.copy(targetPackageName = item.app.packageName) }.padding(8.dp))
                                    }
                                }
                                "OPEN_WEBSITE", "OPEN_SYSTEM_SETTING" -> OutlinedTextField(
                                    value = step.targetUrl.orEmpty(),
                                    onValueChange = { macroSteps[index] = step.copy(targetUrl = it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(if (step.actionType == "OPEN_WEBSITE") "Website URL" else "Android setting action") },
                                    singleLine = true,
                                )
                            }
                        }
                    }
                    OutlinedButton(onClick = { macroSteps.add(MacroStepDraft()) }, modifier = Modifier.fillMaxWidth()) { Text("Add Macro Step") }
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
                        macroStepsJson = if (actionType == "MACRO") JSONArray().apply {
                            macroSteps.forEach { step ->
                                put(JSONObject()
                                    .put("actionType", step.actionType)
                                    .put("delayMs", step.delayMs)
                                    .put("targetElementId", step.targetElementId ?: JSONObject.NULL)
                                    .put("targetPackage", step.targetPackageName ?: JSONObject.NULL)
                                    .put("targetTagId", step.targetTagId ?: JSONObject.NULL)
                                    .put("targetCategoryId", step.targetCategoryId ?: JSONObject.NULL)
                                    .put("targetSidebarId", step.targetSidebarId ?: JSONObject.NULL)
                                    .put("targetWindowId", step.targetWindowId ?: JSONObject.NULL)
                                    .put("targetRuleId", step.targetRuleId ?: JSONObject.NULL)
                                    .put("targetUrl", step.targetUrl ?: JSONObject.NULL))
                            }
                        }.toString() else null,
                    )
                )
            }) { Text("Save") }
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
