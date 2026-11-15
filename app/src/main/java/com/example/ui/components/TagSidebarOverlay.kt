package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import com.example.data.db.SidebarConfigEntity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import coil.compose.AsyncImage
import com.example.data.db.AppEntity
import com.example.data.db.CategoryEntity
import com.example.data.db.HomeElementEntity
import com.example.data.db.LauncherWindowEntity
import com.example.data.db.PlayCategoryCatalog
import com.example.data.db.TagEntity
import com.example.data.repository.TagFilterEngine
import com.example.data.repository.TagMatchMode
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.InstalledWidgetProvider
import com.example.ui.screens.AddHomeElementDialog
import com.example.ui.screens.ElementInspectorDialog
import com.example.ui.state.ActiveOverlay
import com.example.ui.state.LauncherUiState
import java.io.File
import kotlin.math.roundToInt

@Composable
fun ActiveOverlayHost(
    state: LauncherUiState,
    appDiscovery: AppDiscoveryManager,
    onDismiss: () -> Unit,
    onSwitchToTag: (Long) -> Unit,
    onSwitchToCategory: (Long) -> Unit = {},
    onOpenNestedFolder: (Long) -> Unit = {},
    onLaunchApp: (AppEntity) -> Unit,
    onEditTag: (TagEntity) -> Unit,
    onEditCategory: (CategoryEntity) -> Unit = {},
    onAssignAppsToTag: (TagEntity) -> Unit,
    onUpdateTagLayout: (TagEntity) -> Unit,
    onAppLongPress: (AppEntity) -> Unit,
    onToggleSidebarPin: (Long, Boolean) -> Unit,
    onSaveSidebar: (SidebarConfigEntity) -> Unit = {},
    onSaveWindow: (LauncherWindowEntity) -> Unit = {},
    installedWidgetProviders: List<InstalledWidgetProvider> = emptyList(),
    onImportImage: suspend (Uri, String, String?) -> String? = { _, _, _ -> null },
    onSaveWindowElement: (HomeElementEntity, Boolean) -> Unit = { _, _ -> },
    onDuplicateWindowElement: (HomeElementEntity) -> Unit = {},
    onCopyWindowElement: (HomeElementEntity) -> Unit = {},
    onCutWindowElement: (HomeElementEntity) -> Unit = {},
    onPasteToWindow: (Long, Long) -> Unit = { _, _ -> },
    onBringWindowElementForward: (HomeElementEntity) -> Unit = {},
    onSendWindowElementBackward: (HomeElementEntity) -> Unit = {},
    onDeleteWindowElement: (HomeElementEntity) -> Unit = {},
    onStartSystemWidgetBindForWindow: (InstalledWidgetProvider, Long, Long) -> Unit = { _, _, _ -> },
    windowElementRenderer: @Composable (HomeElementEntity, Boolean) -> Unit = { _, _ -> },
    onAddCategory: (CategoryEntity) -> Unit = {},
    onAddButton: (CategoryEntity) -> Unit = {},
    onClickSidebarButton: (HomeElementEntity) -> Unit = {},
    onEditSidebarButton: (HomeElementEntity) -> Unit = {},
) {
    val visible = state.activeOverlay !is ActiveOverlay.None
    if (visible) {
        BackHandler { onDismiss() }
    }

    val overlay = state.activeOverlay
    val folderPopupBackdropAlpha = (overlay as? ActiveOverlay.FolderOverlay)?.let { active ->
        state.homeElements.firstOrNull { it.elementId == active.elementId }?.let { folder ->
            runCatching { JSONObject(folder.styleExtrasJson).optDouble("folderPopupBackdropAlpha", 0.54).toFloat() }.getOrDefault(0.54f)
        }
    }?.coerceIn(0f, 0.9f) ?: 0.54f
    val animDuration = (240 * state.settings.animationSpeedScale.coerceIn(0.05f, 2f)).toInt()

    val configuredSide = when (overlay) {
        is ActiveOverlay.ConfiguredSidebar -> state.sidebars.find { it.sidebarId == overlay.sidebarId }?.side ?: "LEFT"
        is ActiveOverlay.TagSidebar -> overlay.side
        is ActiveOverlay.CategorySidebar -> overlay.side
        is ActiveOverlay.RuleSidebar -> overlay.side
        else -> "LEFT"
    }
    val isRightSide = configuredSide == "RIGHT"

    val animStyle = when (overlay) {
        is ActiveOverlay.ConfiguredSidebar -> state.sidebars.find { it.sidebarId == overlay.sidebarId }?.animationStyle ?: "SLIDE"
        is ActiveOverlay.WindowOverlay -> state.windows.find { it.windowId == overlay.windowId }?.animationStyle ?: "SCALE"
        else -> "SLIDE"
    }

    val isPinned = when (overlay) {
        is ActiveOverlay.ConfiguredSidebar -> state.sidebars.find { it.sidebarId == overlay.sidebarId }?.pinned == true
        else -> false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(animDuration)),
            exit = fadeOut(tween(animDuration)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (isPinned) 0.18f else folderPopupBackdropAlpha))
                    .clickable(enabled = !isPinned) { onDismiss() }
            )
        }

        when (overlay) {
            is ActiveOverlay.FolderOverlay -> {
                val folderElem = state.homeElements.find { it.elementId == overlay.elementId }
                if (folderElem != null) {
                    val pkgSet = folderElem.folderPackages().toSet()
                    val folderApps = state.appsWithTags
                        .map { it.app }
                        .filter { it.packageName in pkgSet }
                    val nestedIds = folderElem.nestedFolderIds().toSet()
                    val nestedFolders = state.homeElements.filter { it.elementId in nestedIds && it.type == "FOLDER" }

                    FolderPopupModal(
                        folder = folderElem,
                        apps = folderApps,
                        nestedFolders = nestedFolders,
                        appDiscovery = appDiscovery,
                        iconShapeKey = state.settings.globalIconShape,
                        onOpenNestedFolder = onOpenNestedFolder,
                        onLaunchApp = {
                            onLaunchApp(it)
                            onDismiss()
                        },
                        onDismiss = onDismiss,
                    )
                }
            }

            is ActiveOverlay.WindowOverlay -> {
                val win = state.windows.find { it.windowId == overlay.windowId }
                if (win != null) {
                    val innerElements = state.homeElements
                        .filter { it.ownerType == "WINDOW" && it.ownerWindowId == win.windowId && it.visible }
                        .sortedBy { it.zIndex }
                    val defaultPageId = state.homePages.firstOrNull()?.pageId ?: 1L
                    WindowPopupHost(
                        window = win,
                        innerElements = innerElements,
                        state = state,
                        appDiscovery = appDiscovery,
                        defaultPageId = defaultPageId,
                        installedWidgetProviders = installedWidgetProviders,
                        onDismiss = onDismiss,
                        onSaveWindow = onSaveWindow,
                        onImportImage = onImportImage,
                        onSaveElement = onSaveWindowElement,
                        onDuplicateElement = onDuplicateWindowElement,
                        onCopyElement = onCopyWindowElement,
                        onCutElement = onCutWindowElement,
                        onPasteToWindow = onPasteToWindow,
                        onBringForward = onBringWindowElementForward,
                        onSendBackward = onSendWindowElementBackward,
                        onDeleteElement = onDeleteWindowElement,
                        onStartSystemWidgetBind = onStartSystemWidgetBindForWindow,
                        elementRenderer = windowElementRenderer,
                    )
                }
            }

            else -> {
                val enterTrans = when {
                    animStyle == "SCALE" -> scaleIn(tween(animDuration), initialScale = 0.88f) + fadeIn(tween(animDuration))
                    animStyle == "FADE" -> fadeIn(tween(animDuration))
                    configuredSide == "TOP" -> slideInVertically(tween(animDuration)) { -it } + fadeIn(tween(animDuration / 2))
                    configuredSide == "BOTTOM" -> slideInVertically(tween(animDuration)) { it } + fadeIn(tween(animDuration / 2))
                    configuredSide == "RIGHT" -> slideInHorizontally(tween(animDuration)) { it } + fadeIn(tween(animDuration / 2))
                    else -> slideInHorizontally(tween(animDuration)) { -it } + fadeIn(tween(animDuration / 2))
                }
                val exitTrans = when {
                    animStyle == "SCALE" -> scaleOut(tween(animDuration), targetScale = 0.88f) + fadeOut(tween(animDuration))
                    animStyle == "FADE" -> fadeOut(tween(animDuration))
                    configuredSide == "TOP" -> slideOutVertically(tween(animDuration)) { -it } + fadeOut(tween(animDuration / 2))
                    configuredSide == "BOTTOM" -> slideOutVertically(tween(animDuration)) { it } + fadeOut(tween(animDuration / 2))
                    configuredSide == "RIGHT" -> slideOutHorizontally(tween(animDuration)) { it } + fadeOut(tween(animDuration / 2))
                    else -> slideOutHorizontally(tween(animDuration)) { -it } + fadeOut(tween(animDuration / 2))
                }
                val alignBox = when (configuredSide) {
                    "TOP" -> Alignment.TopCenter
                    "BOTTOM" -> Alignment.BottomCenter
                    "RIGHT" -> Alignment.CenterEnd
                    else -> Alignment.CenterStart
                }

                AnimatedVisibility(
                    visible = visible && overlay !is ActiveOverlay.FolderOverlay && overlay !is ActiveOverlay.WindowOverlay,
                    enter = enterTrans,
                    exit = exitTrans,
                    modifier = Modifier.align(alignBox),
                ) {
                    when (overlay) {
                        is ActiveOverlay.TagSidebar -> {
                            val tag = state.allTags.find { it.tagId == overlay.tagId }
                            if (tag != null) {
                                val childTagIds = state.allTags.filter { it.parentTagId == tag.tagId }
                                val tagWithApps = state.tagsWithApps.find { it.tag.tagId == tag.tagId }
                                val matchingApps = tagWithApps?.apps
                                    ?.filter { it.isInstalled && !it.hidden }
                                    ?.sortedBy { it.displayName.lowercase() }
                                    .orEmpty()

                                TagTriggeredSidebarContent(
                                    tag = tag,
                                    allTags = state.allTags,
                                    childTags = childTagIds,
                                    apps = matchingApps,
                                    isRightSide = isRightSide,
                                    appDiscovery = appDiscovery,
                                    iconShapeKey = state.settings.globalIconShape,
                                    sidebarTextColorMode = state.settings.sidebarTextColorMode,
                                    sidebarCustomTextColorHex = state.settings.globalTextColorHex,
                                    sidebarCategoryIconStyle = state.settings.sidebarCategoryIconStyle,
                                    onSwitchToTag = onSwitchToTag,
                                    onLaunchApp = { app ->
                                        onLaunchApp(app)
                                        onDismiss()
                                    },
                                    onAppLongPress = onAppLongPress,
                                    onEditTag = { onEditTag(tag) },
                                    onAssignApps = { onAssignAppsToTag(tag) },
                                    onUpdateTagLayout = onUpdateTagLayout,
                                    onClose = onDismiss,
                                )
                            }
                        }

                        is ActiveOverlay.CategorySidebar -> {
                            val cat = state.categories.find { it.categoryId == overlay.categoryId }
                            if (cat != null) {
                                val matchingApps = state.appsWithTags
                                    .filter { item ->
                                        !item.app.hidden && item.app.isInstalled && (
                                            when (cat.categoryId) {
                                                PlayCategoryCatalog.SYSTEM_ALL_ID -> true
                                                PlayCategoryCatalog.SYSTEM_FAVORITES_ID -> item.app.favorite
                                                PlayCategoryCatalog.GAMES_PARENT_ID -> item.app.isGame || item.app.primaryCategoryId in 100L..199L
                                                else -> item.app.primaryCategoryId == cat.categoryId ||
                                                    item.categories.any { it.categoryId == cat.categoryId }
                                            }
                                            )
                                    }
                                    .map { it.app }
                                    .sortedBy { it.displayName.lowercase() }

                                val subCategories = if (cat.categoryId == PlayCategoryCatalog.GAMES_PARENT_ID || cat.categoryType == "GAME_SUB") {
                                    state.categories.filter { it.categoryType == "GAME_SUB" }
                                } else emptyList()

                                val useCustomBg = cat.backgroundType != "INHERIT"
                                GenericSidebarContent(
                                    title = cat.name,
                                    subtitle = when {
                                        cat.isSystemDockTab -> "Drawer Dock Tab"
                                        cat.categoryType == "GAME_SUB" -> "Games Subcategory"
                                        cat.isCustom -> "Custom Category"
                                        else -> "Google Play Category"
                                    },
                                    accentColor = Color(cat.colorHex),
                                    iconKey = cat.iconKey,
                                    customImagePath = cat.customIconPath,
                                    customEmoji = cat.customEmoji,
                                    backgroundType = if (useCustomBg) cat.backgroundType else "SOLID",
                                    backgroundColorHex = if (useCustomBg) cat.backgroundColorHex else 0xEE0B1120L,
                                    backgroundGradientEndHex = if (useCustomBg) cat.backgroundGradientEndHex else null,
                                    backgroundImagePath = if (useCustomBg) cat.backgroundImagePath else null,
                                    backgroundBlurDp = if (useCustomBg) cat.backgroundBlurDp else 0f,
                                    backgroundAlpha = if (useCustomBg) cat.backgroundAlpha else 1f,
                                    side = configuredSide,
                                    widthFraction = 0.84f,
                                    heightFraction = 0.72f,
                                    cornerRadiusDp = 28f,
                                    initialLayoutMode = "GRID",
                                    initialIconSizeDp = 52,
                                    columns = 3,
                                    showCount = true,
                                    isPinned = false,
                                    canPin = false,
                                    allTags = state.allTags,
                                    quickCategories = subCategories,
                                    onClickSubChip = onSwitchToCategory,
                                    sidebarButtons = state.homeElements.filter { it.type == "CUSTOM_BUTTON" && it.targetCategoryId == cat.categoryId },
                                    onClickSidebarButton = onClickSidebarButton,
                                    onEditSidebarButton = onEditSidebarButton,
                                    onAddCategory = { onAddCategory(cat) },
                                    onAddButton = { onAddButton(cat) },
                                    apps = matchingApps,
                                    appDiscovery = appDiscovery,
                                    iconShapeKey = state.settings.globalIconShape,
                                    onSwitchToTag = onSwitchToTag,
                                    onLaunchApp = {
                                        onLaunchApp(it)
                                        onDismiss()
                                    },
                                    onAppLongPress = onAppLongPress,
                                    onEditAction = { onEditCategory(cat) },
                                    onTogglePin = {},
                                    onClose = onDismiss,
                                    sidebarTextColorMode = state.settings.sidebarTextColorMode,
                                    sidebarCustomTextColorHex = state.settings.globalTextColorHex,

                                )
                            }
                        }

                        is ActiveOverlay.RuleSidebar -> {
                            val rule = state.savedRules.find { it.ruleId == overlay.ruleId }
                            if (rule != null) {
                                val mode = if (rule.matchMode == "AND") TagMatchMode.AND else TagMatchMode.OR
                                val matchingApps = TagFilterEngine.filterApps(
                                    appsWithTags = state.appsWithTags,
                                    includeTagIds = rule.includeIds().toSet(),
                                    excludeTagIds = rule.excludeIds().toSet(),
                                    matchMode = mode,
                                    allTags = state.allTags,
                                    includeNestedTags = rule.includeNestedTags,
                                ).map { it.app }

                                GenericSidebarContent(
                                    title = rule.name,
                                    subtitle = "${rule.matchMode} dynamic collection",
                                    accentColor = Color(rule.colorHex),
                                    iconKey = rule.iconKey,
                                    customImagePath = null,
                                    backgroundColorHex = 0xEE0B1120L,
                                    backgroundImagePath = null,
                                    side = configuredSide,
                                    widthFraction = 0.80f,
                                    heightFraction = 0.72f,
                                    cornerRadiusDp = 28f,
                                    initialLayoutMode = "GRID",
                                    initialIconSizeDp = 52,
                                    columns = 3,
                                    showCount = true,
                                    isPinned = false,
                                    canPin = false,
                                    allTags = state.allTags,
                                    apps = matchingApps,
                                    appDiscovery = appDiscovery,
                                    iconShapeKey = state.settings.globalIconShape,
                                    onSwitchToTag = onSwitchToTag,
                                    onLaunchApp = {
                                        onLaunchApp(it)
                                        onDismiss()
                                    },
                                    onAppLongPress = onAppLongPress,
                                    onTogglePin = {},
                                    onClose = onDismiss,
                                    sidebarTextColorMode = state.settings.sidebarTextColorMode,
                                    sidebarCustomTextColorHex = state.settings.globalTextColorHex,

                                )
                            }
                        }

                        is ActiveOverlay.ConfiguredSidebar -> {
                            val cfg = state.sidebars.find { it.sidebarId == overlay.sidebarId }
                            if (cfg != null) {
                                val matchingApps = when (cfg.sourceType) {
                                    "FAVORITES" -> state.appsWithTags.map { it.app }
                                        .filter { it.isInstalled && !it.hidden && it.favorite }
                                        .sortedBy { it.displayName.lowercase() }
                                    "TAG" -> {
                                        val tId = cfg.targetTagId
                                        state.tagsWithApps.find { it.tag.tagId == tId }?.apps
                                            ?.filter { it.isInstalled && !it.hidden }
                                            ?.sortedBy { it.displayName.lowercase() }
                                            .orEmpty()
                                    }
                                    "CATEGORY" -> {
                                        val cId = cfg.targetCategoryId ?: PlayCategoryCatalog.GAMES_PARENT_ID
                                        state.appsWithTags.filter { item ->
                                            !item.app.hidden && item.app.isInstalled && (
                                                when (cId) {
                                                    PlayCategoryCatalog.SYSTEM_ALL_ID -> true
                                                    PlayCategoryCatalog.SYSTEM_FAVORITES_ID -> item.app.favorite
                                                    PlayCategoryCatalog.GAMES_PARENT_ID -> item.app.isGame || item.app.primaryCategoryId in 100L..199L
                                                    else -> item.app.primaryCategoryId == cId || item.categories.any { it.categoryId == cId }
                                                }
                                                )
                                        }.map { it.app }.sortedBy { it.displayName.lowercase() }
                                    }
                                    "RULE" -> {
                                        val rule = state.savedRules.find { it.ruleId == cfg.targetRuleId }
                                        if (rule != null) {
                                            val mode = if (rule.matchMode == "AND") TagMatchMode.AND else TagMatchMode.OR
                                            TagFilterEngine.filterApps(
                                                state.appsWithTags,
                                                rule.includeIds().toSet(),
                                                rule.excludeIds().toSet(),
                                                mode,
                                                allTags = state.allTags,
                                                includeNestedTags = rule.includeNestedTags,
                                            ).map { it.app }
                                        } else emptyList()
                                    }
                                    "CUSTOM_COLLECTION" -> {
                                        val pkgSet = cfg.customPackages().toSet()
                                        state.appsWithTags.map { it.app }
                                            .filter { it.packageName in pkgSet && it.isInstalled }
                                            .sortedBy { it.displayName.lowercase() }
                                    }
                                    else -> state.appsWithTags.map { it.app }
                                        .filter { it.isInstalled && !it.hidden }
                                        .sortedBy { it.displayName.lowercase() }
                                }
                                val linkedTag = if (cfg.sourceType == "TAG") {
                                    state.allTags.find { it.tagId == cfg.targetTagId }
                                } else null
                                val linkedCat = if (cfg.sourceType == "CATEGORY") {
                                    state.categories.find { it.categoryId == cfg.targetCategoryId }
                                } else null

                                val resolvedBgType = when {
                                    !cfg.backgroundImagePath.isNullOrBlank() -> "IMAGE"
                                    cfg.backgroundGradientEndHex != null -> "GRADIENT"
                                    else -> "SOLID"
                                }

                                GenericSidebarContent(
                                    title = linkedTag?.name ?: cfg.title,
                                    subtitle = when (cfg.sourceType) {
                                        "FAVORITES" -> "Pinned favorites"
                                        "ALL_APPS" -> "All installed apps"
                                        "TAG" -> linkedTag?.description?.ifBlank { "Tagged collection" } ?: "Tag sidebar"
                                        "CATEGORY" -> linkedCat?.name ?: "Category drawer"
                                        "RULE" -> "Dynamic rule collection"
                                        else -> "Custom sidebar"
                                    },
                                    accentColor = Color(linkedTag?.colorHex ?: linkedCat?.colorHex ?: 0xFF38BDF8L),
                                    iconKey = linkedTag?.iconKey ?: linkedCat?.iconKey ?: if (cfg.sourceType == "FAVORITES") "star" else "folder",
                                    customImagePath = linkedTag?.customImagePath ?: linkedCat?.customIconPath,
                                    customEmoji = linkedCat?.customEmoji,
                                    backgroundType = resolvedBgType,
                                    backgroundColorHex = cfg.backgroundColorHex,
                                    backgroundGradientEndHex = cfg.backgroundGradientEndHex,
                                    backgroundImagePath = cfg.backgroundImagePath,
                                    backgroundBlurDp = cfg.backgroundBlurDp,
                                    side = cfg.side,
                                    widthFraction = cfg.widthFraction,
                                    heightFraction = cfg.heightFraction,
                                    cornerRadiusDp = cfg.cornerRadiusDp,
                                    initialLayoutMode = cfg.layoutMode,
                                    initialIconSizeDp = cfg.iconSizeDp,
                                    columns = cfg.columns,
                                    showCount = true,
                                    isPinned = cfg.pinned,
                                    canPin = true,
                                    allTags = state.allTags,
                                    apps = matchingApps,
                                    appDiscovery = appDiscovery,
                                    iconShapeKey = state.settings.globalIconShape,
                                    onSwitchToTag = onSwitchToTag,
                                    onLaunchApp = {
                                        onLaunchApp(it)
                                        if (!cfg.pinned) onDismiss()
                                    },
                                    onAppLongPress = onAppLongPress,
                                    onTogglePin = { onToggleSidebarPin(cfg.sidebarId, !cfg.pinned) },
                                    onUpdateLayout = { mode, size -> onSaveSidebar(cfg.copy(layoutMode = mode, iconSizeDp = size)) },
                                    onClose = onDismiss,
                                    sidebarTextColorMode = "LIGHT",
                                    sidebarCustomTextColorHex = state.settings.globalTextColorHex,

                                )
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun WindowPopupHost(
    window: LauncherWindowEntity,
    innerElements: List<HomeElementEntity>,
    state: LauncherUiState,
    appDiscovery: AppDiscoveryManager,
    defaultPageId: Long,
    installedWidgetProviders: List<InstalledWidgetProvider>,
    onDismiss: () -> Unit,
    onSaveWindow: (LauncherWindowEntity) -> Unit,
    onImportImage: suspend (Uri, String, String?) -> String?,
    onSaveElement: (HomeElementEntity, Boolean) -> Unit,
    onDuplicateElement: (HomeElementEntity) -> Unit,
    onCopyElement: (HomeElementEntity) -> Unit,
    onCutElement: (HomeElementEntity) -> Unit,
    onPasteToWindow: (Long, Long) -> Unit,
    onBringForward: (HomeElementEntity) -> Unit,
    onSendBackward: (HomeElementEntity) -> Unit,
    onDeleteElement: (HomeElementEntity) -> Unit,
    onStartSystemWidgetBind: (InstalledWidgetProvider, Long, Long) -> Unit,
    elementRenderer: @Composable (HomeElementEntity, Boolean) -> Unit,
) {
    val density = LocalDensity.current
    var isWindowEditMode by remember(window.windowId) { mutableStateOf(false) }
    var selectedElementId by remember(window.windowId) { mutableStateOf<Long?>(null) }
    var inspectingElement by remember(window.windowId) { mutableStateOf<HomeElementEntity?>(null) }
    var showAddDialog by remember(window.windowId) { mutableStateOf(false) }
    var showWidgetPicker by remember(window.windowId) { mutableStateOf(false) }
    var showWindowStyleDialog by remember(window.windowId) { mutableStateOf(false) }

    BackHandler(enabled = isWindowEditMode) {
        isWindowEditMode = false
        selectedElementId = null
    }

    val latestEditMode by rememberUpdatedState(isWindowEditMode)
    val latestElements by rememberUpdatedState(innerElements)

    val align = when (window.alignment) {
        "TOP" -> Alignment.TopCenter
        "BOTTOM" -> Alignment.BottomCenter
        "LEFT" -> Alignment.CenterStart
        "RIGHT" -> Alignment.CenterEnd
        else -> Alignment.Center
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        contentAlignment = align,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(if (window.fullScreen) 1f else window.widthFraction.coerceIn(0.45f, 1f))
                .fillMaxHeight(if (window.fullScreen) 1f else window.heightFraction.coerceIn(0.30f, 1f))
                .pointerInput(Unit) { detectTapGestures {} }
                .testTag("window_overlay_${window.windowId}"),
            shape = RoundedCornerShape(if (window.fullScreen) 0.dp else window.cornerRadiusDp.dp),
            color = Color(window.backgroundColorHex),
            border = BorderStroke(
                width = if (isWindowEditMode) 2.dp else 1.dp,
                color = if (isWindowEditMode) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.22f),
            ),
            shadowElevation = 24.dp,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!window.backgroundImagePath.isNullOrBlank() && File(window.backgroundImagePath).exists()) {
                    AsyncImage(
                        model = File(window.backgroundImagePath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alpha = 0.35f,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isWindowEditMode) {
                            OutlinedVectorIcon(
                                imageVector = Icons.Rounded.Layers,
                                contentDescription = null,
                                fillColor = Color.White,
                                outlineColor = Color.Black,
                                outlineWidthDp = 1.3.dp,
                                modifier = Modifier.size(22.dp),
                            )
                        } else {
                            Icon(Icons.Rounded.Layers, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isWindowEditMode) "Edit • ${window.title}" else window.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color.Black.copy(alpha = 0.72f))
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                        )
                        if (isWindowEditMode) {
                            IconButton(
                                onClick = {
                                    isWindowEditMode = false
                                    selectedElementId = null
                                },
                                modifier = Modifier.size(44.dp).testTag("window_done_button"),
                            ) {
                                OutlinedVectorIcon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Done editing",
                                    fillColor = Color.White,
                                    outlineColor = Color.Black,
                                    outlineWidthDp = 1.3.dp,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { isWindowEditMode = true },
                                modifier = Modifier.size(44.dp).testTag("window_edit_button"),
                            ) {
                                Icon(Icons.Rounded.Edit, "Edit window layout", tint = Color.White)
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(44.dp).testTag("window_overlay_close"),
                        ) {
                            if (isWindowEditMode) {
                                OutlinedVectorIcon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close window",
                                    fillColor = Color.White,
                                    outlineColor = Color.Black,
                                    outlineWidthDp = 1.3.dp,
                                    modifier = Modifier.size(22.dp),
                                )
                            } else {
                                Icon(Icons.Rounded.Close, "Close window", tint = Color.White)
                            }
                        }
                    }
                    if (isWindowEditMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            IconButton(
                                onClick = { showWindowStyleDialog = true },
                                modifier = Modifier.size(48.dp).testTag("window_style_button"),
                            ) {
                                OutlinedVectorIcon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = "Customize window style",
                                    fillColor = Color.White,
                                    outlineColor = Color.Black,
                                    outlineWidthDp = 1.3.dp,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                            IconButton(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.size(48.dp).testTag("window_add_element"),
                            ) {
                                OutlinedVectorIcon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Add element",
                                    fillColor = Color.White,
                                    outlineColor = Color.Black,
                                    outlineWidthDp = 1.3.dp,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                            IconButton(
                                onClick = { showWidgetPicker = true },
                                modifier = Modifier.size(48.dp).testTag("window_widgets_button"),
                            ) {
                                OutlinedVectorIcon(
                                    imageVector = Icons.Rounded.Widgets,
                                    contentDescription = "Add widget",
                                    fillColor = Color.White,
                                    outlineColor = Color.Black,
                                    outlineWidthDp = 1.3.dp,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                            if (state.clipboardElement != null) {
                                IconButton(
                                    onClick = { onPasteToWindow(defaultPageId, window.windowId) },
                                    modifier = Modifier.size(48.dp).testTag("window_paste_button"),
                                ) {
                                    OutlinedVectorIcon(
                                        imageVector = Icons.Rounded.ContentPaste,
                                        contentDescription = "Paste",
                                        fillColor = Color.White,
                                        outlineColor = Color.Black,
                                        outlineWidthDp = 1.3.dp,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.14f))
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        val canvasWidthPx = with(density) { maxWidth.toPx().coerceAtLeast(1f) }
                        val canvasHeightPx = with(density) { maxHeight.toPx().coerceAtLeast(1f) }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(window.windowId, canvasWidthPx, canvasHeightPx) {
                                    detectTapGestures(
                                        onTap = {
                                            if (latestEditMode) selectedElementId = null
                                        },
                                        onLongPress = { pressOffset ->
                                            val hit = latestElements.any { el ->
                                                val left = el.xFraction * canvasWidthPx
                                                val top = el.yFraction * canvasHeightPx
                                                val wPx = with(density) { el.widthDp.dp.toPx() }
                                                val hPx = with(density) { el.heightDp.dp.toPx() }
                                                pressOffset.x in left..(left + wPx) && pressOffset.y in top..(top + hPx)
                                            }
                                            if (!hit && !latestEditMode) {
                                                isWindowEditMode = true
                                            }
                                        },
                                    )
                                },
                        ) {
                            if (innerElements.isEmpty()) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = if (isWindowEditMode) "Tap + or Widgets above to place items" else "Empty window — tap Edit or long-press canvas",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center,
                                    )
                                    if (!isWindowEditMode) {
                                        OutlinedButton(onClick = { isWindowEditMode = true }) {
                                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Edit Window")
                                        }
                                    }
                                }
                            }

                            innerElements.forEach { el ->
                                val latestEl by rememberUpdatedState(el)
                                val selected = isWindowEditMode && selectedElementId == el.elementId
                                var dragX by remember(el.elementId, el.xFraction, canvasWidthPx) {
                                    mutableFloatStateOf(el.xFraction * canvasWidthPx)
                                }
                                var dragY by remember(el.elementId, el.yFraction, canvasHeightPx) {
                                    mutableFloatStateOf(el.yFraction * canvasHeightPx)
                                }
                                var widthDp by remember(el.elementId, el.widthDp) {
                                    mutableFloatStateOf(el.widthDp)
                                }
                                var heightDp by remember(el.elementId, el.heightDp) {
                                    mutableFloatStateOf(el.heightDp)
                                }

                                val canMove = isWindowEditMode && !el.locked
                                val dragModifier = if (canMove) {
                                    Modifier.pointerInput(el.elementId, canvasWidthPx, canvasHeightPx) {
                                        detectDragGestures(
                                            onDragStart = { selectedElementId = latestEl.elementId },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragX = (dragX + dragAmount.x).coerceIn(0f, (canvasWidthPx - 40f).coerceAtLeast(0f))
                                                dragY = (dragY + dragAmount.y).coerceIn(0f, (canvasHeightPx - 40f).coerceAtLeast(0f))
                                            },
                                            onDragEnd = {
                                                val fx = (dragX / canvasWidthPx).coerceIn(0f, 0.92f)
                                                val fy = (dragY / canvasHeightPx).coerceIn(0f, 0.92f)
                                                onSaveElement(
                                                    latestEl.copy(
                                                        xFraction = fx,
                                                        yFraction = fy,
                                                        widthDp = widthDp,
                                                        heightDp = heightDp,
                                                    ),
                                                    true,
                                                )
                                            },
                                        )
                                    }
                                } else Modifier

                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset(dragX.roundToInt(), dragY.roundToInt()) }
                                        .size(width = widthDp.dp, height = heightDp.dp)
                                        .graphicsLayer {
                                            rotationZ = el.rotationDeg
                                            scaleX = el.scaleX * (if (el.flipHorizontal) -1f else 1f)
                                            scaleY = el.scaleY * (if (el.flipVertical) -1f else 1f)
                                        }
                                        .alpha(el.alpha.coerceIn(0.1f, 1f))
                                        .then(
                                            if (isWindowEditMode) {
                                                Modifier.border(
                                                    width = if (selected) 2.dp else 1.dp,
                                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.28f),
                                                    shape = RoundedCornerShape(el.cornerRadiusDp.dp),
                                                )
                                            } else Modifier
                                        )
                                        .testTag("window_element_${el.elementId}"),
                                ) {
                                    elementRenderer(el, isWindowEditMode)

                                    if (isWindowEditMode) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .then(dragModifier)
                                                .clickable {
                                                    val cur = latestEl
                                                    if (selected) inspectingElement = cur else selectedElementId = cur.elementId
                                                }
                                                .testTag("window_shield_${el.elementId}"),
                                        )

                                        if (el.locked) {
                                            Icon(
                                                Icons.Rounded.Lock,
                                                contentDescription = "Locked",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.align(Alignment.TopStart).padding(4.dp).size(16.dp),
                                            )
                                        }

                                        if (selected) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = 6.dp, y = (-6).dp)
                                                    .size(28.dp)
                                                    .clickable { inspectingElement = latestEl }
                                                    .testTag("window_inspect_element_${el.elementId}"),
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    val iconStyle = LocalIconStyleConfig.current
                                                    if (iconStyle.enabled) {
                                                        OutlinedVectorIcon(
                                                            imageVector = Icons.Rounded.Tune,
                                                            contentDescription = "Inspect",
                                                            fillColor = Color(iconStyle.fillColorHex),
                                                            outlineColor = Color(iconStyle.outlineColorHex),
                                                            outlineWidthDp = (iconStyle.outlineWidthDp * 0.55f).dp,
                                                            modifier = Modifier.size(15.dp),
                                                        )
                                                    } else {
                                                        Icon(Icons.Rounded.Tune, contentDescription = "Inspect", tint = Color.Black, modifier = Modifier.size(15.dp))
                                                    }
                                                }
                                            }

                                            if (canMove) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .offset(x = 6.dp, y = 6.dp)
                                                        .size(28.dp)
                                                        .pointerInput(el.elementId, canvasWidthPx, canvasHeightPx) {
                                                            detectDragGestures(
                                                                onDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    val dxDp = with(density) { dragAmount.x.toDp().value }
                                                                    val dyDp = with(density) { dragAmount.y.toDp().value }
                                                                    widthDp = (widthDp + dxDp).coerceIn(48f, 380f)
                                                                    heightDp = (heightDp + dyDp).coerceIn(48f, 440f)
                                                                },
                                                                onDragEnd = {
                                                                    val fx = (dragX / canvasWidthPx).coerceIn(0f, 0.92f)
                                                                    val fy = (dragY / canvasHeightPx).coerceIn(0f, 0.92f)
                                                                    onSaveElement(
                                                                        latestEl.copy(
                                                                            xFraction = fx,
                                                                            yFraction = fy,
                                                                            widthDp = widthDp,
                                                                            heightDp = heightDp,
                                                                        ),
                                                                        true,
                                                                    )
                                                                },
                                                            )
                                                        }
                                                        .testTag("window_resize_element_${el.elementId}"),
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Rounded.OpenInFull, contentDescription = "Resize", tint = Color.White, modifier = Modifier.size(15.dp))
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
            }
        }
    }

    if (showAddDialog) {
        AddHomeElementDialog(
            pageId = defaultPageId,
            ownerWindowId = window.windowId,
            state = state,
            appDiscovery = appDiscovery,
            onImportImage = { uri -> onImportImage(uri, "Windows", null) },
            onAddElement = {
                onSaveElement(it, true)
            },
            onOpenWidgetPicker = { showWidgetPicker = true },
            onDismiss = { showAddDialog = false },
        )
    }

    if (showWidgetPicker) {
        WidgetPickerDialog(
            providers = installedWidgetProviders,
            tags = state.allTags,
            onPickSystemWidget = { provider ->
                showWidgetPicker = false
                onStartSystemWidgetBind(provider, defaultPageId, window.windowId)
            },
            onPickBuiltinType = { type, label, wDp, hDp ->
                onSaveElement(
                    HomeElementEntity(
                        pageId = defaultPageId,
                        ownerType = "WINDOW",
                        ownerWindowId = window.windowId,
                        type = type,
                        xFraction = 0.08f,
                        yFraction = 0.08f,
                        widthDp = wDp,
                        heightDp = hDp,
                        label = label,
                    ),
                    true,
                )
                showWidgetPicker = false
            },
            onPickBuiltinTagWidget = { tag ->
                onSaveElement(
                    HomeElementEntity(
                        pageId = defaultPageId,
                        ownerType = "WINDOW",
                        ownerWindowId = window.windowId,
                        type = "TAG_COLLECTION_WIDGET",
                        xFraction = 0.06f,
                        yFraction = 0.14f,
                        widthDp = 280f,
                        heightDp = 220f,
                        label = tag.name,
                        targetTagId = tag.tagId,
                        backgroundColorHex = 0x660F172AL,
                        borderColorHex = (0x88000000L or (tag.colorHex and 0x00FFFFFFL)),
                    ),
                    true,
                )
                showWidgetPicker = false
            },
            onDismiss = { showWidgetPicker = false },
        )
    }

    inspectingElement?.let { snap ->
        val elem = innerElements.find { it.elementId == snap.elementId } ?: snap
        ElementInspectorDialog(
            element = elem,
            state = state,
            onImportImage = onImportImage,
            onUpdate = { onSaveElement(it, true) },
            onDuplicate = {
                onDuplicateElement(elem)
                inspectingElement = null
            },
            onCopy = {
                onCopyElement(elem)
                inspectingElement = null
            },
            onCut = {
                onCutElement(elem)
                inspectingElement = null
            },
            onBringForward = { onBringForward(elem) },
            onSendBackward = { onSendBackward(elem) },
            onDelete = {
                onDeleteElement(elem)
                inspectingElement = null
            },
            onDismiss = { inspectingElement = null },
        )
    }

    if (showWindowStyleDialog) {
        WindowStyleQuickDialog(
            window = window,
            onSaveWindow = onSaveWindow,
            onImportImage = onImportImage,
            onDismiss = { showWindowStyleDialog = false },
        )
    }
}

@Composable
private fun TagIconDock(
    tags: List<TagEntity>,
    selectedTagId: Long?,
    onSelect: (Long) -> Unit,
    darkSurface: Boolean = false,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
    ) {
        items(tags, key = { "tag_dock_${it.tagId}" }) { item ->
            val tint = Color(item.colorHex)
            val selected = item.tagId == selectedTagId
            Column(
                modifier = Modifier
                    .widthIn(min = 48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(item.tagId) }
                    .background(if (selected) tint.copy(alpha = 0.22f) else Color.Transparent)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("sidebar_tag_icon_${item.tagId}"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(tint.copy(alpha = if (darkSurface) 0.28f else 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    TagBadgeIcon(
                        iconKey = item.iconKey,
                        customImagePath = item.customImagePath,
                        colorHex = item.colorHex,
                        size = 30.dp,
                        circular = false,
                    )
                }
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}


@Composable
private fun CategoryIconDock(
    categories: List<CategoryEntity>,
    contrastStyle: String,
    onSelect: (Long) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
    ) {
        items(categories, key = { "category_dock_${it.categoryId}" }) { category ->
            Column(
                modifier = Modifier
                    .widthIn(min = 48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(category.categoryId) }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("sidebar_category_icon_${category.categoryId}"),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CategoryBadgeIcon(category = category, size = 40.dp, contrastStyle = contrastStyle)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagTriggeredSidebarContent(
    tag: TagEntity,
    allTags: List<TagEntity>,
    childTags: List<TagEntity> = emptyList(),
    apps: List<AppEntity>,
    isRightSide: Boolean,
    appDiscovery: AppDiscoveryManager,
    iconShapeKey: String,
    sidebarTextColorMode: String = "AUTO",
    sidebarCustomTextColorHex: Long = 0xFFFFFFFFL,
    sidebarCategoryIconStyle: String = "BLACK_WHITE",
    onSwitchToTag: (Long) -> Unit,
    onLaunchApp: (AppEntity) -> Unit,
    onAppLongPress: (AppEntity) -> Unit,
    onEditTag: () -> Unit,
    onAssignApps: () -> Unit,
    onUpdateTagLayout: (TagEntity) -> Unit,
    onClose: () -> Unit,
) {
    val sidebarTextColor = when (sidebarTextColorMode) {
        "LIGHT" -> Color.White
        "DARK" -> Color.Black
        "CUSTOM" -> Color(sidebarCustomTextColorHex)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val accent = Color(tag.colorHex)
    val shape = if (isRightSide) {
        RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
    } else {
        RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    }

    var dragAccum by remember { mutableStateOf(0f) }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.82f)
            .widthIn(max = 420.dp)
            .testTag("tag_sidebar_container")
            .pointerInput(isRightSide) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAccum = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        dragAccum += dragAmount
                        val shouldClose = if (isRightSide) dragAccum > 64f else dragAccum < -64f
                        if (shouldClose) {
                            change.consume()
                            onClose()
                            dragAccum = 0f
                        }
                    },
                )
            },
        shape = shape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        tonalElevation = 12.dp,
        shadowElevation = 20.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TagBadgeIcon(
                    iconKey = tag.iconKey,
                    customImagePath = tag.customImagePath,
                    colorHex = tag.colorHex,
                    size = 46.dp,
                    contentDescription = tag.name,
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tag.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = sidebarTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("tag_sidebar_title"),
                    )
                    if (tag.showCountInSidebar) {
                        Text(
                            text = "${apps.size} ${if (apps.size == 1) "app" else "apps"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = accent,
                            modifier = Modifier.testTag("tag_sidebar_count"),
                        )
                    }
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("tag_sidebar_close_top"),
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close sidebar")
                }
            }

            if (tag.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = tag.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = sidebarTextColor.copy(alpha = 0.78f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(10.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (apps.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        TagBadgeIcon(tag.iconKey, tag.customImagePath, tag.colorHex, 64.dp)
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "No apps in ${tag.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Assign installed apps to this tag so they appear here instantly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onAssignApps,
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            modifier = Modifier.testTag("empty_tag_assign_apps_button"),
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Assign Apps")
                        }
                    }
                } else if (true) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp),
                    ) {
                        items(apps, key = { it.packageName }) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                    .combinedClickable(
                                        onClick = { onLaunchApp(app) },
                                        onLongClick = { onAppLongPress(app) },
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("sidebar_app_${app.packageName}"),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AppIconView(app, appDiscovery, tag.sidebarIconSizeDp.dp, iconShapeKey)
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(tag.sidebarColumns.coerceIn(2, 5)),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 12.dp),
                    ) {
                        items(apps, key = { it.packageName }) { app ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .combinedClickable(
                                        onClick = { onLaunchApp(app) },
                                        onLongClick = { onAppLongPress(app) },
                                    )
                                    .padding(vertical = 6.dp, horizontal = 4.dp)
                                    .testTag("sidebar_app_${app.packageName}"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                AppIconView(app, appDiscovery, tag.sidebarIconSizeDp.dp, iconShapeKey)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = app.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 15.sp,
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val nextMode = if (tag.sidebarLayoutMode == "GRID") "LIST" else "GRID"
                            onUpdateTagLayout(tag.copy(sidebarLayoutMode = nextMode))
                        },
                        modifier = Modifier.testTag("sidebar_toggle_layout").clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.68f)),
                    ) {
                        Icon(
                            imageVector = if (tag.sidebarLayoutMode == "GRID") Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                            contentDescription = "Toggle grid or list",
                            tint = Color.White,
                        )
                    }
                    IconButton(
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.68f)),
                        onClick = {
                            val smaller = (tag.sidebarIconSizeDp - 6).coerceAtLeast(36)
                            onUpdateTagLayout(tag.copy(sidebarIconSizeDp = smaller))
                        },
                    ) {
                        Icon(Icons.Rounded.ZoomOut, contentDescription = "Smaller icons", tint = Color.White)
                    }
                    IconButton(
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.68f)),
                        onClick = {
                            val larger = (tag.sidebarIconSizeDp + 6).coerceAtMost(76)
                            onUpdateTagLayout(tag.copy(sidebarIconSizeDp = larger))
                        },
                    ) {
                        Icon(Icons.Rounded.ZoomIn, contentDescription = "Larger icons", tint = Color.White)
                    }
                    IconButton(
                        onClick = onAssignApps,
                        modifier = Modifier.testTag("sidebar_assign_apps_button").clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.68f)),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Assign apps to tag", tint = Color.White)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onEditTag,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("sidebar_edit_tag_button"),
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit Tag", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.width(6.dp))
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("sidebar_back_button"),
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back / Close")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GenericSidebarContent(
    title: String,
    subtitle: String,
    accentColor: Color,
    iconKey: String,
    customImagePath: String?,
    customEmoji: String? = null,
    backgroundType: String = "SOLID",
    backgroundColorHex: Long,
    backgroundGradientEndHex: Long? = null,
    backgroundImagePath: String?,
    backgroundBlurDp: Float = 0f,
    backgroundAlpha: Float = 1f,
    side: String = "LEFT",
    widthFraction: Float,
    heightFraction: Float = 0.72f,
    cornerRadiusDp: Float,
    initialLayoutMode: String,
    initialIconSizeDp: Int,
    columns: Int,
    showCount: Boolean,
    isPinned: Boolean,
    canPin: Boolean,
    allTags: List<TagEntity>,
    quickCategories: List<CategoryEntity> = emptyList(),
    onClickSubChip: (Long) -> Unit = {},
    sidebarButtons: List<HomeElementEntity> = emptyList(),
    onClickSidebarButton: (HomeElementEntity) -> Unit = {},
    onEditSidebarButton: (HomeElementEntity) -> Unit = {},
    onAddCategory: (() -> Unit)? = null,
    onAddButton: (() -> Unit)? = null,
    apps: List<AppEntity>,
    appDiscovery: AppDiscoveryManager,
    iconShapeKey: String,
    onSwitchToTag: (Long) -> Unit,
    onLaunchApp: (AppEntity) -> Unit,
    onAppLongPress: (AppEntity) -> Unit,
    onEditAction: (() -> Unit)? = null,
    onUpdateLayout: ((String, Int) -> Unit)? = null,
    onTogglePin: () -> Unit,
    onClose: () -> Unit,
    sidebarTextColorMode: String = "AUTO",
    sidebarCustomTextColorHex: Long = 0xFFFFFFFFL,
    sidebarCategoryIconStyle: String = "BLACK_WHITE",
) {
    val genericSidebarTextColor = when (sidebarTextColorMode) {
        "LIGHT" -> Color.White
        "DARK" -> Color.Black
        "CUSTOM" -> Color(sidebarCustomTextColorHex)
        else -> MaterialTheme.colorScheme.onSurface
    }

    var layoutMode by remember(title) { mutableStateOf("LIST") }
    var iconSize by remember(title, initialIconSizeDp) { mutableStateOf(initialIconSizeDp) }
    val radius = cornerRadiusDp.coerceIn(0f, 36f).dp
    val shape = when (side) {
        "RIGHT" -> RoundedCornerShape(topStart = radius, bottomStart = radius)
        "TOP" -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
        "BOTTOM" -> RoundedCornerShape(topStart = radius, topEnd = radius)
        else -> RoundedCornerShape(topEnd = radius, bottomEnd = radius)
    }
    val sizeModifier = if (side == "TOP" || side == "BOTTOM") {
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(heightFraction.coerceIn(0.40f, 0.92f))
    } else {
        Modifier
            .fillMaxHeight()
            .fillMaxWidth(widthFraction.coerceIn(0.45f, 0.95f))
            .widthIn(max = 440.dp)
    }

    Surface(
        modifier = sizeModifier.testTag("generic_sidebar_container"),
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        shadowElevation = 18.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SurfaceBackgroundLayer(
                bgType = backgroundType,
                colorHex = backgroundColorHex,
                gradientEndHex = backgroundGradientEndHex,
                imagePath = backgroundImagePath,
                blurDp = backgroundBlurDp,
                alpha = backgroundAlpha,
                modifier = Modifier.testTag("sidebar_bg_$backgroundType"),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!customEmoji.isNullOrBlank() && customImagePath.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(accentColor.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = customEmoji, fontSize = 24.sp)
                        }
                    } else {
                        TagBadgeIcon(
                            iconKey = iconKey,
                            customImagePath = customImagePath,
                            colorHex = (accentColor.toArgb().toLong() and 0xFFFFFFFFL),
                            size = 44.dp,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = genericSidebarTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp).testTag("generic_sidebar_title"),
                        )
                        if (showCount) {
                            Text(
                                text = "$subtitle • ${apps.size} apps",
                                style = MaterialTheme.typography.labelMedium,
                                color = genericSidebarTextColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            )
                        }
                    }
                    if (canPin) {
                        IconButton(onClick = onTogglePin) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = if (isPinned) "Unpin sidebar" else "Pin sidebar",
                                tint = if (isPinned) accentColor else Color.White.copy(alpha = 0.7f),
                            )
                        }
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.14f))
                Spacer(Modifier.height(10.dp))

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (apps.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No apps match this sidebar yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = genericSidebarTextColor.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else if (true) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 12.dp),
                        ) {
                            items(apps, key = { it.packageName }) { app ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .combinedClickable(
                                            onClick = { onLaunchApp(app) },
                                            onLongClick = { onAppLongPress(app) },
                                        )
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AppIconView(app, appDiscovery, iconSize.dp, iconShapeKey)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = app.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns.coerceIn(2, 5)),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 12.dp),
                        ) {
                            items(apps, key = { it.packageName }) { app ->
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .combinedClickable(
                                            onClick = { onLaunchApp(app) },
                                            onLongClick = { onAppLongPress(app) },
                                        )
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    AppIconView(app, appDiscovery, iconSize.dp, iconShapeKey)
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = app.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 15.sp,
                                    )
                                }
                            }
                        }
                    }
                }
                if (sidebarButtons.isNotEmpty()) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.14f))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        sidebarButtons.forEach { element ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .combinedClickable(
                                        onClick = { onClickSidebarButton(element) },
                                        onLongClick = { onEditSidebarButton(element) },
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = accentColor)
                                Spacer(Modifier.width(8.dp))
                                Text(element.label.ifBlank { "Button" }, color = Color.White,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }


                if (quickCategories.isNotEmpty()) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.14f))
                    CategoryIconDock(
                        categories = quickCategories,
                        contrastStyle = sidebarCategoryIconStyle,
                        onSelect = onClickSubChip,
                    )
                }

                if (onAddCategory != null || onAddButton != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (onAddCategory != null) {
                            OutlinedButton(onClick = onAddCategory, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = accentColor)
                                Spacer(Modifier.width(4.dp))
                                Text("Add Category", color = accentColor)
                            }
                        }
                        if (onAddButton != null) {
                            OutlinedButton(onClick = onAddButton, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = accentColor)
                                Spacer(Modifier.width(4.dp))
                                Text("Add Button", color = accentColor)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }


                HorizontalDivider(color = Color.White.copy(alpha = 0.14f))
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row {
                        IconButton(modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.68f)), onClick = {
                            layoutMode = if (layoutMode == "GRID") "LIST" else "GRID"
                            onUpdateLayout?.invoke(layoutMode, iconSize)
                        }) {
                            Icon(
                                imageVector = if (layoutMode == "GRID") Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                                contentDescription = "Toggle layout",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        IconButton(modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.68f)), onClick = {
                            iconSize = (iconSize - 6).coerceAtLeast(36)
                            onUpdateLayout?.invoke(layoutMode, iconSize)
                        }) {
                            Icon(Icons.Rounded.ZoomOut, contentDescription = "Smaller icons", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.68f)), onClick = {
                            iconSize = (iconSize + 6).coerceAtMost(72)
                            onUpdateLayout?.invoke(layoutMode, iconSize)
                        }) {
                            Icon(Icons.Rounded.ZoomIn, contentDescription = "Larger icons", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (onEditAction != null) {
                            OutlinedButton(
                                onClick = onEditAction,
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
                                modifier = Modifier.testTag("sidebar_edit_category_button"),
                            ) {
                                Icon(Icons.Rounded.Edit, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Edit", color = accentColor)
                            }
                        }
                        OutlinedButton(
                            onClick = onClose,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        ) {
                            Text("Close", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderPopupModal(
    folder: HomeElementEntity,
    apps: List<AppEntity>,
    nestedFolders: List<HomeElementEntity>,
    appDiscovery: AppDiscoveryManager,
    iconShapeKey: String,
    onOpenNestedFolder: (Long) -> Unit,
    onLaunchApp: (AppEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    val popupExtras = remember(folder.styleExtrasJson) { runCatching { JSONObject(folder.styleExtrasJson) }.getOrElse { JSONObject() } }
    val popupMaxHeightDp = popupExtras.optDouble("folderPopupMaxHeightDp", 320.0).toFloat().coerceIn(180f, 620f)
    val popupColumns = popupExtras.optInt("folderPopupColumns", if (folder.folderLayoutMode == "GRID_3X3") 3 else 2).coerceIn(2, 4)
    val popupIconSizeDp = popupExtras.optDouble("folderPopupIconSizeDp", folder.iconSizeDp.toDouble()).toFloat().coerceIn(24f, 88f)
    val popupLabelMaxLines = popupExtras.optInt("folderPopupLabelMaxLines", 2).coerceIn(1, 3)
    val popupGapDp = popupExtras.optDouble("folderPopupGapDp", 10.0).toFloat().coerceIn(2f, 24f)
    val popupHeaderVisible = popupExtras.optBoolean("folderPopupHeaderVisible", true)
    val shape = RoundedCornerShape(folder.cornerRadiusDp.coerceIn(8f, 48f).dp)
    val startColor = Color(folder.backgroundColorHex).takeIf { it.alpha > 0.02f } ?: MaterialTheme.colorScheme.surface
    val endColor = folder.backgroundGradientEndHex?.let { Color(it) }
    val popupBrush = if (endColor != null) Brush.linearGradient(listOf(startColor, endColor)) else Brush.linearGradient(listOf(startColor, startColor))
    val appTextColor = Color(folder.textColorHex)
    Box(
        modifier = Modifier.fillMaxSize().clickable { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .widthIn(max = folder.widthDp.coerceIn(280f, 420f).dp)
                .clickable(enabled = false) {},
            shape = shape,
            color = Color.Transparent,
            border = BorderStroke(folder.borderWidthDp.coerceIn(0f, 6f).dp, Color(folder.borderColorHex)),
            shadowElevation = folder.shadowElevationDp.coerceIn(0f, 24f).dp,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .background(popupBrush, shape)
                    .padding(folder.paddingDp.coerceIn(8f, 28f).dp + 12.dp),
            ) {
                if (popupHeaderVisible) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = appTextColor)
                        Spacer(Modifier.width(10.dp))
                        Text(text = folder.label.ifBlank { "Folder" }, style = MaterialTheme.typography.titleLarge, fontSize = folder.fontSizeSp.coerceIn(14f, 30f).sp, color = appTextColor, modifier = Modifier.weight(1f))
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close folder", tint = appTextColor)
                        }
                    }
                }
                if (nestedFolders.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(nestedFolders, key = { it.elementId }) { sub ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(folder.borderColorHex).copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color(folder.borderColorHex).copy(alpha = 0.55f)),
                                modifier = Modifier.clickable { onOpenNestedFolder(sub.elementId) },
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(Icons.Rounded.FolderOpen, null, modifier = Modifier.size(16.dp), tint = appTextColor)
                                    Text(sub.label.ifBlank { "Folder" }, style = MaterialTheme.typography.labelLarge, color = appTextColor)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (apps.isEmpty() && nestedFolders.isEmpty()) {
                    Text(
                        text = "Folder is empty. Enter Edit Mode to add apps or nested folders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = appTextColor.copy(alpha = 0.75f),
                    )
                } else if (folder.folderLayoutMode == "LIST") {
                    LazyColumn(modifier = Modifier.heightIn(max = popupMaxHeightDp.dp), verticalArrangement = Arrangement.spacedBy(popupGapDp.dp)) {
                        items(apps, key = { it.packageName }) { app ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onLaunchApp(app) }.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AppIconView(app, appDiscovery, popupIconSizeDp, iconShapeKey)
                                Spacer(Modifier.width(12.dp))
                                Text(app.displayName, color = appTextColor, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(popupColumns),
                        modifier = Modifier.heightIn(max = popupMaxHeightDp.dp),
                        verticalArrangement = Arrangement.spacedBy(popupGapDp.dp),
                        horizontalArrangement = Arrangement.spacedBy(popupGapDp.dp),
                    ) {
                        items(apps, key = { it.packageName }) { app ->
                            Column(
                                modifier = Modifier.clip(RoundedCornerShape(14.dp)).clickable { onLaunchApp(app) }.padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                AppIconView(app, appDiscovery, folder.iconSizeDp.coerceIn(28f, 64f).dp, iconShapeKey)
                                Spacer(Modifier.height(6.dp))
                                Text(app.displayName, color = appTextColor, style = MaterialTheme.typography.bodyMedium, maxLines = popupLabelMaxLines, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}
