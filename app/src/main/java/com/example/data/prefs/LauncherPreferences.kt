package com.example.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.components.IconStyleConfig
import com.example.data.system.LineIconProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(name = "tagdeck_prefs")

data class LauncherSettings(
    val themeMode: String = "DARK", // DARK, LIGHT, SYSTEM, DYNAMIC
    val accentColorHex: Long = 0xFF38BDF8L,
    val secondaryColorHex: Long = 0xFF6366F1L,
    val tertiaryColorHex: Long = 0xFFFB7185L,
    val fontStyle: String = "DEFAULT", // DEFAULT, SPACE_GROTESK, PLUS_JAKARTA, JETBRAINS_MONO, SYSTEM
    val fontScale: Float = 1.0f, // 0.8f..1.8f
    val highReadabilityMode: Boolean = false,
    val boldLabelsEnabled: Boolean = false,
    val labelBackdropEnabled: Boolean = false,
    val labelBackdropColorHex: Long = 0x99000000L,
    val labelBackdropAlpha: Float = 0.6f,
    val textOutlineEnabled: Boolean = false,
    val textOutlineColorHex: Long = 0xFF000000L,
    val textOutlineWidthDp: Float = 0.8f,
    val textShadowEnabled: Boolean = false,
    val textShadowRadiusDp: Float = 4f,
    val letterSpacingEm: Float = 0f,
    val lineHeightMultiplier: Float = 1.25f,
    val minLabelFontSp: Float = 11f,
    val labelMaxLines: Int = 1,
    val autoContrastText: Boolean = false,
    val surfaceOpacity: Float = 0.86f, // 0.2f..1.0f
    val globalBorderWidthDp: Float = 1.0f, // 0f..4.0f
    val globalTextColorHex: Long = 0xFFF8FAFC,
    val sidebarTextColorMode: String = "AUTO", // AUTO, LIGHT, DARK, CUSTOM
    val sidebarCategoryIconStyle: String = "BLACK_WHITE", // BLACK_WHITE, WHITE_BLACK
    val showHomeAppLabels: Boolean = true,
    val appLabelOutlineEnabled: Boolean = false,
    val wallpaperType: String = "BUILTIN_PRISM", // BUILTIN_PRISM, BUILTIN_TOPO, CUSTOM_IMAGE, GRADIENT, SOLID
    val wallpaperCustomPath: String? = null,
    val wallpaperGradientStartHex: Long = 0xFF0A1124L,
    val wallpaperGradientEndHex: Long = 0xFF1A1038L,
    val wallpaperGradientDirection: String = "VERTICAL", // VERTICAL, HORIZONTAL, DIAGONAL
    val wallpaperSolidHex: Long = 0xFF090D16L,
    val wallpaperBlurDp: Float = 0f,
    val wallpaperDimAlpha: Float = 0.25f,
    val wallpaperDimColorHex: Long = 0xFF000000L,
    val wallpaperVignette: Float = 0f,
    val wallpaperSaturation: Float = 1f,
    val wallpaperContrast: Float = 1f,
    val wallpaperBrightness: Float = 0f,
    val wallpaperParallaxIntensity: Float = 0f,
    val wallpaperTintHex: Long? = null,
    val wallpaperAmbientMotion: Boolean = false,
    // Universal All-Apps Sidebar & Drawer
    val allAppsSidebarEdge: String = "RIGHT", // LEFT, RIGHT, BOTTOM
    val allAppsSidebarWidthFraction: Float = 0.90f,
    val allAppsBgType: String = "SOLID", // SOLID, GRADIENT, IMAGE
    val allAppsBgColorHex: Long = 0xF2090D16L,
    val allAppsBgGradientEndHex: Long = 0xF2151E34L,
    val allAppsBgImagePath: String? = null,
    val allAppsBgBlurDp: Float = 0f,
    val drawerLayoutMode: String = "GRID", // GRID, LIST
    val drawerColumns: Int = 4,
    val drawerIconSizeDp: Int = 50,
    val drawerLabelSizeSp: Int = 12,
    val drawerSortOrder: String = "ALPHABETICAL", // ALPHABETICAL, MANUAL, MOST_USED, RECENTLY_INSTALLED
    val drawerCardOpacity: Float = 0.88f,
    val drawerCornerRadiusDp: Float = 26f,
    val drawerSearchBarVisible: Boolean = true,
    val drawerSearchBarPosition: String = "TOP", // TOP, BOTTOM
    val drawerCardShadowDp: Float = 4f,
    val drawerCardBorderWidthDp: Float = 0f,
    val drawerCardBorderColorHex: Long = 0x55FFFFFFL,
    // Niagara Wave Alphabet Navigation
    val niagaraWaveEnabled: Boolean = true,
    val niagaraIndexVisible: Boolean = true,
    val niagaraEdge: String = "RIGHT", // RIGHT or LEFT
    val niagaraWaveIntensity: Float = 1.0f, // 0f..1.8f
    val niagaraBaseFontSp: Float = 11f,
    val niagaraSelectedFontSp: Float = 24f,
    val niagaraAccentColorHex: Long = 0xFF38BDF8L,
    val niagaraIndexWidthDp: Float = 28f,
    val niagaraIndexColorHex: Long = 0xFF38BDF8L,
    val niagaraHapticFeedback: Boolean = true,
    // Edit Mode & Canvas
    val gridSnapEnabled: Boolean = false,
    val alignmentGuidesEnabled: Boolean = true,
    val alignmentGuideColorHex: Long = 0xFF38BDF8L,
    val gridSnapStepDp: Int = 16,
    val editGridVisible: Boolean = true,
    val editGridOpacity: Float = 0.08f,
    val editGridColorHex: Long = 0xFFFFFFFFL,
    val layoutLocked: Boolean = false,
    val pageTransitionStyle: String = "SLIDE", // SLIDE, DEPTH, FADE, ZOOM, CUBE, FLIP, ROTATE, PARALLAX, STACK, ACCORDION, WINDMILL
    val widgetFeedEnabled: Boolean = true,
    val widgetFeedTitleColor: String = "WHITE", // WHITE, BLACK
    val pageIndicatorStyle: String = "DOTS", // DOTS, PILL, NUMBERS, HIDDEN
    val pageIndicatorColorHex: Long = 0xFF38BDF8L,
    val pageIndicatorSizeDp: Float = 7f,
    val pageIndicatorActiveWidthDp: Float = 22f,
    val statusStripEnabled: Boolean = false,
    val statusStripShowDate: Boolean = true,
    val statusStripShowBattery: Boolean = true,
    val statusStripAccentColorHex: Long = 0xFF38BDF8L,
    val edgeHandlesVisible: Boolean = true,
    val edgeHandleOpacity: Float = 0.45f,
    val edgeHandleWidthDp: Float = 4f,
    val edgeHandleHeightDp: Float = 64f,
    // Appearance & System Integration
    val animationSpeedScale: Float = 1f,
    val globalCornerRadiusDp: Float = 20f,
    val globalIconShape: String = "SQUIRCLE", // SQUIRCLE, CIRCLE, ROUNDED_SQUARE
    val showDockBar: Boolean = true,
    val showTagStripOnHome: Boolean = true,
    val showDefaultHomeBanner: Boolean = true,
    val showStatusBar: Boolean = true,
    val showNavBar: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val notificationsBadgeHint: Boolean = false,
    val recentSearchesCsv: String = "",
    val iconPackOrderCsv: String = "",
    // Black Icon Styling (Default: white fill with crisp black outline)
    val blackIconStyleEnabled: Boolean = true,
    val blackIconMode: String = IconStyleConfig.MODE_BLACK_ICONS, // BLACK_ICONS, ALL_MONOCHROME, ALL_ICONS_OUTLINE
    val blackIconFillColorHex: Long = IconStyleConfig.DEFAULT_FILL_COLOR_HEX,
    val blackIconOutlineColorHex: Long = IconStyleConfig.DEFAULT_OUTLINE_COLOR_HEX,
    val blackIconOutlineWidthDp: Float = IconStyleConfig.DEFAULT_OUTLINE_WIDTH_DP,
    val blackIconDarknessThreshold: Float = IconStyleConfig.DEFAULT_DARKNESS_THRESHOLD,
    val lineIconProfilesJson: String = "",
    val lineIconsEnabled: Boolean = false,
    val lineIconDefaultColorHex: Long = 0xFFFFFFFFL,
    val lineIconDefaultLineWidthPx: Float = 1.8f,
    val lineIconDefaultOutlineEnabled: Boolean = false,
    val lineIconDefaultOutlineColorHex: Long = 0xFF000000L,
    val lineIconDefaultOutlineWidthPx: Float = 1.2f,
    val lockedAppsCsv: String = "",
    val cloudBackupEnabled: Boolean = false,
    val cloudBackupTreeUri: String? = null,
    val seededDefaults: Boolean = false,
) {
    fun defaultLineIconProfile(): LineIconProfile = LineIconProfile(
        lineColorHex = lineIconDefaultColorHex,
        lineWidthPx = lineIconDefaultLineWidthPx,
        outlineEnabled = lineIconDefaultOutlineEnabled,
        outlineColorHex = lineIconDefaultOutlineColorHex,
        outlineWidthPx = lineIconDefaultOutlineWidthPx,
    )

    fun lineIconProfile(packageName: String): LineIconProfile? =
        LineIconProfile.decodeForPackage(lineIconProfilesJson, packageName) ?: defaultLineIconProfile().takeIf { lineIconsEnabled }

    fun iconPackOrder(): List<String> =
        iconPackOrderCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()

    fun iconStyleConfig(): IconStyleConfig = IconStyleConfig(
        enabled = blackIconStyleEnabled,
        mode = blackIconMode,
        fillColorHex = blackIconFillColorHex,
        outlineColorHex = blackIconOutlineColorHex,
        outlineWidthDp = blackIconOutlineWidthDp,
        darknessThreshold = blackIconDarknessThreshold,
    )
}

class LauncherPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = longPreferencesKey("accent_color")
        val SECONDARY_COLOR = longPreferencesKey("secondary_color")
        val TERTIARY_COLOR = longPreferencesKey("tertiary_color")
        val FONT_STYLE = stringPreferencesKey("font_style")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val HIGH_READABILITY = booleanPreferencesKey("high_readability")
        val BOLD_LABELS = booleanPreferencesKey("bold_labels")
        val LABEL_BACKDROP = booleanPreferencesKey("label_backdrop")
        val LABEL_BACKDROP_COLOR = longPreferencesKey("label_backdrop_color")
        val LABEL_BACKDROP_ALPHA = floatPreferencesKey("label_backdrop_alpha")
        val TEXT_OUTLINE = booleanPreferencesKey("text_outline")
        val TEXT_OUTLINE_COLOR = longPreferencesKey("text_outline_color")
        val TEXT_OUTLINE_WIDTH = floatPreferencesKey("text_outline_width")
        val TEXT_SHADOW = booleanPreferencesKey("text_shadow")
        val TEXT_SHADOW_RADIUS = floatPreferencesKey("text_shadow_radius")
        val LETTER_SPACING_EM = floatPreferencesKey("letter_spacing_em")
        val LINE_HEIGHT_MULTIPLIER = floatPreferencesKey("line_height_multiplier")
        val MIN_LABEL_FONT_SP = floatPreferencesKey("min_label_font_sp")
        val LABEL_MAX_LINES = intPreferencesKey("label_max_lines")
        val AUTO_CONTRAST_TEXT = booleanPreferencesKey("auto_contrast_text")
        val SURFACE_OPACITY = floatPreferencesKey("surface_opacity")
        val GLOBAL_BORDER_WIDTH = floatPreferencesKey("global_border_width")
        val GLOBAL_TEXT_COLOR = longPreferencesKey("global_text_color")
        val SIDEBAR_TEXT_COLOR_MODE = stringPreferencesKey("sidebar_text_color_mode")
        val SIDEBAR_CATEGORY_ICON_STYLE = stringPreferencesKey("sidebar_category_icon_style")
        val SHOW_HOME_APP_LABELS = booleanPreferencesKey("show_home_app_labels")
        val APP_LABEL_OUTLINE_ENABLED = booleanPreferencesKey("app_label_outline_enabled")
        val WALLPAPER_TYPE = stringPreferencesKey("wallpaper_type")
        val WALLPAPER_CUSTOM_PATH = stringPreferencesKey("wallpaper_custom_path")
        val WALLPAPER_GRADIENT_START = longPreferencesKey("wallpaper_gradient_start")
        val WALLPAPER_GRADIENT_END = longPreferencesKey("wallpaper_gradient_end")
        val WALLPAPER_GRADIENT_DIR = stringPreferencesKey("wallpaper_gradient_dir")
        val WALLPAPER_SOLID = longPreferencesKey("wallpaper_solid")
        val WALLPAPER_BLUR_DP = floatPreferencesKey("wallpaper_blur_dp")
        val WALLPAPER_DIM_ALPHA = floatPreferencesKey("wallpaper_dim_alpha")
        val WALLPAPER_DIM_COLOR = longPreferencesKey("wallpaper_dim_color")
        val WALLPAPER_VIGNETTE = floatPreferencesKey("wallpaper_vignette")
        val WALLPAPER_SATURATION = floatPreferencesKey("wallpaper_saturation")
        val WALLPAPER_CONTRAST = floatPreferencesKey("wallpaper_contrast")
        val WALLPAPER_BRIGHTNESS = floatPreferencesKey("wallpaper_brightness")
        val WALLPAPER_PARALLAX = floatPreferencesKey("wallpaper_parallax")
        val WALLPAPER_TINT = longPreferencesKey("wallpaper_tint")
        val WALLPAPER_AMBIENT = booleanPreferencesKey("wallpaper_ambient")

        val ALL_APPS_SIDEBAR_EDGE = stringPreferencesKey("all_apps_sidebar_edge")
        val ALL_APPS_SIDEBAR_WIDTH = floatPreferencesKey("all_apps_sidebar_width")
        val ALL_APPS_BG_TYPE = stringPreferencesKey("all_apps_bg_type")
        val ALL_APPS_BG_COLOR = longPreferencesKey("all_apps_bg_color")
        val ALL_APPS_BG_GRAD_END = longPreferencesKey("all_apps_bg_grad_end")
        val ALL_APPS_BG_IMAGE = stringPreferencesKey("all_apps_bg_image")
        val ALL_APPS_BG_BLUR = floatPreferencesKey("all_apps_bg_blur")

        val DRAWER_LAYOUT_MODE = stringPreferencesKey("drawer_layout_mode")
        val DRAWER_COLUMNS = intPreferencesKey("drawer_columns")
        val DRAWER_ICON_SIZE_DP = intPreferencesKey("drawer_icon_size_dp")
        val DRAWER_LABEL_SIZE_SP = intPreferencesKey("drawer_label_size_sp")
        val DRAWER_SORT_ORDER = stringPreferencesKey("drawer_sort_order")
        val DRAWER_CARD_OPACITY = floatPreferencesKey("drawer_card_opacity")
        val DRAWER_CORNER_RADIUS = floatPreferencesKey("drawer_corner_radius")
        val DRAWER_SEARCH_BAR_VISIBLE = booleanPreferencesKey("drawer_search_bar_visible")
        val DRAWER_SEARCH_BAR_POSITION = stringPreferencesKey("drawer_search_bar_position")
        val DRAWER_CARD_SHADOW = floatPreferencesKey("drawer_card_shadow")
        val DRAWER_CARD_BORDER_WIDTH = floatPreferencesKey("drawer_card_border_width")
        val DRAWER_CARD_BORDER_COLOR = longPreferencesKey("drawer_card_border_color")

        val NIAGARA_WAVE_ENABLED = booleanPreferencesKey("niagara_wave_enabled")
        val NIAGARA_INDEX_VISIBLE = booleanPreferencesKey("niagara_index_visible")
        val NIAGARA_EDGE = stringPreferencesKey("niagara_edge")
        val NIAGARA_WAVE_INTENSITY = floatPreferencesKey("niagara_wave_intensity")
        val NIAGARA_BASE_FONT = floatPreferencesKey("niagara_base_font")
        val NIAGARA_SELECTED_FONT = floatPreferencesKey("niagara_selected_font")
        val NIAGARA_ACCENT = longPreferencesKey("niagara_accent")
        val NIAGARA_INDEX_WIDTH = floatPreferencesKey("niagara_index_width")
        val NIAGARA_INDEX_COLOR = longPreferencesKey("niagara_index_color")
        val NIAGARA_HAPTIC = booleanPreferencesKey("niagara_haptic")

        val GRID_SNAP = booleanPreferencesKey("grid_snap")
        val ALIGNMENT_GUIDES = booleanPreferencesKey("alignment_guides")
        val ALIGNMENT_GUIDE_COLOR = longPreferencesKey("alignment_guide_color")
        val GRID_SNAP_STEP = intPreferencesKey("grid_snap_step")
        val EDIT_GRID_VISIBLE = booleanPreferencesKey("edit_grid_visible")
        val EDIT_GRID_OPACITY = floatPreferencesKey("edit_grid_opacity")
        val EDIT_GRID_COLOR = longPreferencesKey("edit_grid_color")
        val LAYOUT_LOCKED = booleanPreferencesKey("layout_locked")
        val PAGE_TRANSITION = stringPreferencesKey("page_transition")
    val WIDGET_FEED_ENABLED = booleanPreferencesKey("widget_feed_enabled")
        val WIDGET_FEED_TITLE_COLOR = stringPreferencesKey("widget_feed_title_color")
        val PAGE_INDICATOR_STYLE = stringPreferencesKey("page_indicator_style")
        val PAGE_INDICATOR_COLOR = longPreferencesKey("page_indicator_color")
        val PAGE_INDICATOR_SIZE = floatPreferencesKey("page_indicator_size")
        val PAGE_INDICATOR_ACTIVE_WIDTH = floatPreferencesKey("page_indicator_active_width")
        val STATUS_STRIP_ENABLED = booleanPreferencesKey("status_strip_enabled")
        val STATUS_STRIP_SHOW_DATE = booleanPreferencesKey("status_strip_show_date")
        val STATUS_STRIP_SHOW_BATTERY = booleanPreferencesKey("status_strip_show_battery")
        val STATUS_STRIP_ACCENT = longPreferencesKey("status_strip_accent")
        val EDGE_HANDLES_VISIBLE = booleanPreferencesKey("edge_handles_visible")
        val EDGE_HANDLE_OPACITY = floatPreferencesKey("edge_handle_opacity")
        val EDGE_HANDLE_WIDTH = floatPreferencesKey("edge_handle_width")
        val EDGE_HANDLE_HEIGHT = floatPreferencesKey("edge_handle_height")

        val ANIMATION_SPEED_SCALE = floatPreferencesKey("animation_speed_scale")
        val GLOBAL_CORNER_RADIUS_DP = floatPreferencesKey("global_corner_radius_dp")
        val GLOBAL_ICON_SHAPE = stringPreferencesKey("global_icon_shape")
        val SHOW_DOCK_BAR = booleanPreferencesKey("show_dock_bar")
        val SHOW_TAG_STRIP = booleanPreferencesKey("show_tag_strip")
        val SHOW_DEFAULT_HOME_BANNER = booleanPreferencesKey("show_default_home_banner")
        val SHOW_STATUS_BAR = booleanPreferencesKey("show_status_bar")
        val SHOW_NAV_BAR = booleanPreferencesKey("show_nav_bar")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val NOTIFICATIONS_BADGE = booleanPreferencesKey("notifications_badge")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
        val ICON_PACK_ORDER_CSV = stringPreferencesKey("icon_pack_order_csv")
        val BLACK_ICON_STYLE_ENABLED = booleanPreferencesKey("black_icon_style_enabled")
        val BLACK_ICON_MODE = stringPreferencesKey("black_icon_mode")
        val BLACK_ICON_FILL_COLOR = longPreferencesKey("black_icon_fill_color")
        val BLACK_ICON_OUTLINE_COLOR = longPreferencesKey("black_icon_outline_color")
        val BLACK_ICON_OUTLINE_WIDTH = floatPreferencesKey("black_icon_outline_width")
        val BLACK_ICON_DARKNESS_THRESHOLD = floatPreferencesKey("black_icon_darkness_threshold")
        val LINE_ICON_PROFILES = stringPreferencesKey("line_icon_profiles_json")
        val LINE_ICONS_ENABLED = booleanPreferencesKey("line_icons_enabled")
        val LINE_ICON_DEFAULT_COLOR = longPreferencesKey("line_icon_default_color")
        val LINE_ICON_DEFAULT_WIDTH = floatPreferencesKey("line_icon_default_width")
        val LINE_ICON_DEFAULT_OUTLINE_ENABLED = booleanPreferencesKey("line_icon_default_outline_enabled")
        val LINE_ICON_DEFAULT_OUTLINE_COLOR = longPreferencesKey("line_icon_default_outline_color")
        val LINE_ICON_DEFAULT_OUTLINE_WIDTH = floatPreferencesKey("line_icon_default_outline_width")
        val LOCKED_APPS = stringPreferencesKey("locked_apps_csv")
        val CLOUD_BACKUP_ENABLED = booleanPreferencesKey("cloud_backup_enabled")
        val CLOUD_BACKUP_TREE_URI = stringPreferencesKey("cloud_backup_tree_uri")
        val SEEDED_DEFAULTS = booleanPreferencesKey("seeded_defaults")
    }

    private fun fromPrefs(prefs: Preferences): LauncherSettings = LauncherSettings(
        themeMode = prefs[Keys.THEME_MODE] ?: "DARK",
        accentColorHex = prefs[Keys.ACCENT_COLOR] ?: 0xFF38BDF8L,
        secondaryColorHex = prefs[Keys.SECONDARY_COLOR] ?: 0xFF6366F1L,
        tertiaryColorHex = prefs[Keys.TERTIARY_COLOR] ?: 0xFFFB7185L,
        fontStyle = prefs[Keys.FONT_STYLE] ?: "DEFAULT",
        fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
        highReadabilityMode = prefs[Keys.HIGH_READABILITY] ?: false,
        boldLabelsEnabled = prefs[Keys.BOLD_LABELS] ?: false,
        labelBackdropEnabled = prefs[Keys.LABEL_BACKDROP] ?: false,
        labelBackdropColorHex = prefs[Keys.LABEL_BACKDROP_COLOR] ?: 0x99000000L,
        labelBackdropAlpha = prefs[Keys.LABEL_BACKDROP_ALPHA] ?: 0.6f,
        textOutlineEnabled = prefs[Keys.TEXT_OUTLINE] ?: false,
        textOutlineColorHex = prefs[Keys.TEXT_OUTLINE_COLOR] ?: 0xFF000000L,
        textOutlineWidthDp = prefs[Keys.TEXT_OUTLINE_WIDTH] ?: 0.8f,
        textShadowEnabled = prefs[Keys.TEXT_SHADOW] ?: false,
        textShadowRadiusDp = prefs[Keys.TEXT_SHADOW_RADIUS] ?: 4f,
        letterSpacingEm = prefs[Keys.LETTER_SPACING_EM] ?: 0f,
        lineHeightMultiplier = prefs[Keys.LINE_HEIGHT_MULTIPLIER] ?: 1.25f,
        minLabelFontSp = prefs[Keys.MIN_LABEL_FONT_SP] ?: 11f,
        labelMaxLines = prefs[Keys.LABEL_MAX_LINES] ?: 1,
        autoContrastText = prefs[Keys.AUTO_CONTRAST_TEXT] ?: false,
        surfaceOpacity = prefs[Keys.SURFACE_OPACITY] ?: 0.86f,
        globalBorderWidthDp = prefs[Keys.GLOBAL_BORDER_WIDTH] ?: 1.0f,
        globalTextColorHex = prefs[Keys.GLOBAL_TEXT_COLOR] ?: 0xFFF8FAFC,
            sidebarTextColorMode = prefs[Keys.SIDEBAR_TEXT_COLOR_MODE] ?: "AUTO",
        sidebarCategoryIconStyle = prefs[Keys.SIDEBAR_CATEGORY_ICON_STYLE] ?: "BLACK_WHITE",
        showHomeAppLabels = prefs[Keys.SHOW_HOME_APP_LABELS] ?: true,
        appLabelOutlineEnabled = prefs[Keys.APP_LABEL_OUTLINE_ENABLED] ?: false,
        wallpaperType = prefs[Keys.WALLPAPER_TYPE] ?: "BUILTIN_PRISM",
        wallpaperCustomPath = prefs[Keys.WALLPAPER_CUSTOM_PATH],
        wallpaperGradientStartHex = prefs[Keys.WALLPAPER_GRADIENT_START] ?: 0xFF0A1124L,
        wallpaperGradientEndHex = prefs[Keys.WALLPAPER_GRADIENT_END] ?: 0xFF1A1038L,
        wallpaperGradientDirection = prefs[Keys.WALLPAPER_GRADIENT_DIR] ?: "VERTICAL",
        wallpaperSolidHex = prefs[Keys.WALLPAPER_SOLID] ?: 0xFF090D16L,
        wallpaperBlurDp = prefs[Keys.WALLPAPER_BLUR_DP] ?: 0f,
        wallpaperDimAlpha = prefs[Keys.WALLPAPER_DIM_ALPHA] ?: 0.25f,
        wallpaperDimColorHex = prefs[Keys.WALLPAPER_DIM_COLOR] ?: 0xFF000000L,
        wallpaperVignette = prefs[Keys.WALLPAPER_VIGNETTE] ?: 0f,
        wallpaperSaturation = prefs[Keys.WALLPAPER_SATURATION] ?: 1f,
        wallpaperContrast = prefs[Keys.WALLPAPER_CONTRAST] ?: 1f,
        wallpaperBrightness = prefs[Keys.WALLPAPER_BRIGHTNESS] ?: 0f,
        wallpaperParallaxIntensity = prefs[Keys.WALLPAPER_PARALLAX] ?: 0f,
        wallpaperTintHex = prefs[Keys.WALLPAPER_TINT],
        wallpaperAmbientMotion = prefs[Keys.WALLPAPER_AMBIENT] ?: false,
        allAppsSidebarEdge = prefs[Keys.ALL_APPS_SIDEBAR_EDGE] ?: "RIGHT",
        allAppsSidebarWidthFraction = prefs[Keys.ALL_APPS_SIDEBAR_WIDTH] ?: 0.90f,
        allAppsBgType = prefs[Keys.ALL_APPS_BG_TYPE] ?: "SOLID",
        allAppsBgColorHex = prefs[Keys.ALL_APPS_BG_COLOR] ?: 0xF2090D16L,
        allAppsBgGradientEndHex = prefs[Keys.ALL_APPS_BG_GRAD_END] ?: 0xF2151E34L,
        allAppsBgImagePath = prefs[Keys.ALL_APPS_BG_IMAGE],
        allAppsBgBlurDp = prefs[Keys.ALL_APPS_BG_BLUR] ?: 0f,
        drawerLayoutMode = prefs[Keys.DRAWER_LAYOUT_MODE] ?: "GRID",
        drawerColumns = prefs[Keys.DRAWER_COLUMNS] ?: 4,
        drawerIconSizeDp = prefs[Keys.DRAWER_ICON_SIZE_DP] ?: 50,
        drawerLabelSizeSp = prefs[Keys.DRAWER_LABEL_SIZE_SP] ?: 12,
        drawerSortOrder = prefs[Keys.DRAWER_SORT_ORDER] ?: "ALPHABETICAL",
        drawerCardOpacity = prefs[Keys.DRAWER_CARD_OPACITY] ?: 0.88f,
        drawerCornerRadiusDp = prefs[Keys.DRAWER_CORNER_RADIUS] ?: 26f,
        drawerSearchBarVisible = prefs[Keys.DRAWER_SEARCH_BAR_VISIBLE] ?: true,
        drawerSearchBarPosition = prefs[Keys.DRAWER_SEARCH_BAR_POSITION] ?: "TOP",
        drawerCardShadowDp = prefs[Keys.DRAWER_CARD_SHADOW] ?: 4f,
        drawerCardBorderWidthDp = prefs[Keys.DRAWER_CARD_BORDER_WIDTH] ?: 0f,
        drawerCardBorderColorHex = prefs[Keys.DRAWER_CARD_BORDER_COLOR] ?: 0x55FFFFFFL,
        niagaraWaveEnabled = prefs[Keys.NIAGARA_WAVE_ENABLED] ?: true,
        niagaraIndexVisible = prefs[Keys.NIAGARA_INDEX_VISIBLE] ?: true,
        niagaraEdge = prefs[Keys.NIAGARA_EDGE] ?: "RIGHT",
        niagaraWaveIntensity = prefs[Keys.NIAGARA_WAVE_INTENSITY] ?: 1.0f,
        niagaraBaseFontSp = prefs[Keys.NIAGARA_BASE_FONT] ?: 11f,
        niagaraSelectedFontSp = prefs[Keys.NIAGARA_SELECTED_FONT] ?: 24f,
        niagaraAccentColorHex = prefs[Keys.NIAGARA_ACCENT] ?: 0xFF38BDF8L,
        niagaraIndexWidthDp = prefs[Keys.NIAGARA_INDEX_WIDTH] ?: 28f,
        niagaraIndexColorHex = prefs[Keys.NIAGARA_INDEX_COLOR] ?: 0xFF38BDF8L,
        niagaraHapticFeedback = prefs[Keys.NIAGARA_HAPTIC] ?: true,
        gridSnapEnabled = prefs[Keys.GRID_SNAP] ?: false,
        alignmentGuidesEnabled = prefs[Keys.ALIGNMENT_GUIDES] ?: true,
        alignmentGuideColorHex = prefs[Keys.ALIGNMENT_GUIDE_COLOR] ?: 0xFF38BDF8L,
        gridSnapStepDp = prefs[Keys.GRID_SNAP_STEP] ?: 16,
        editGridVisible = prefs[Keys.EDIT_GRID_VISIBLE] ?: true,
        editGridOpacity = prefs[Keys.EDIT_GRID_OPACITY] ?: 0.08f,
        editGridColorHex = prefs[Keys.EDIT_GRID_COLOR] ?: 0xFFFFFFFFL,
        layoutLocked = prefs[Keys.LAYOUT_LOCKED] ?: false,
        pageTransitionStyle = prefs[Keys.PAGE_TRANSITION] ?: "SLIDE",
        widgetFeedEnabled = prefs[Keys.WIDGET_FEED_ENABLED] ?: true,
        widgetFeedTitleColor = prefs[Keys.WIDGET_FEED_TITLE_COLOR] ?: "WHITE",
        pageIndicatorStyle = prefs[Keys.PAGE_INDICATOR_STYLE] ?: "DOTS",
        pageIndicatorColorHex = prefs[Keys.PAGE_INDICATOR_COLOR] ?: 0xFF38BDF8L,
        pageIndicatorSizeDp = prefs[Keys.PAGE_INDICATOR_SIZE] ?: 7f,
        pageIndicatorActiveWidthDp = prefs[Keys.PAGE_INDICATOR_ACTIVE_WIDTH] ?: 22f,
        statusStripEnabled = prefs[Keys.STATUS_STRIP_ENABLED] ?: false,
        statusStripShowDate = prefs[Keys.STATUS_STRIP_SHOW_DATE] ?: true,
        statusStripShowBattery = prefs[Keys.STATUS_STRIP_SHOW_BATTERY] ?: true,
        statusStripAccentColorHex = prefs[Keys.STATUS_STRIP_ACCENT] ?: 0xFF38BDF8L,
        edgeHandlesVisible = prefs[Keys.EDGE_HANDLES_VISIBLE] ?: true,
        edgeHandleOpacity = prefs[Keys.EDGE_HANDLE_OPACITY] ?: 0.45f,
        edgeHandleWidthDp = prefs[Keys.EDGE_HANDLE_WIDTH] ?: 4f,
        edgeHandleHeightDp = prefs[Keys.EDGE_HANDLE_HEIGHT] ?: 64f,
        animationSpeedScale = prefs[Keys.ANIMATION_SPEED_SCALE] ?: 1f,
        globalCornerRadiusDp = prefs[Keys.GLOBAL_CORNER_RADIUS_DP] ?: 20f,
        globalIconShape = prefs[Keys.GLOBAL_ICON_SHAPE] ?: "SQUIRCLE",
        showDockBar = prefs[Keys.SHOW_DOCK_BAR] ?: true,
        showTagStripOnHome = prefs[Keys.SHOW_TAG_STRIP] ?: true,
        showDefaultHomeBanner = prefs[Keys.SHOW_DEFAULT_HOME_BANNER] ?: true,
        showStatusBar = prefs[Keys.SHOW_STATUS_BAR] ?: true,
        showNavBar = prefs[Keys.SHOW_NAV_BAR] ?: true,
        hapticFeedbackEnabled = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
        notificationsBadgeHint = prefs[Keys.NOTIFICATIONS_BADGE] ?: false,
        recentSearchesCsv = prefs[Keys.RECENT_SEARCHES] ?: "",
        iconPackOrderCsv = prefs[Keys.ICON_PACK_ORDER_CSV] ?: "",
        blackIconStyleEnabled = prefs[Keys.BLACK_ICON_STYLE_ENABLED] ?: true,
        blackIconMode = prefs[Keys.BLACK_ICON_MODE] ?: IconStyleConfig.MODE_BLACK_ICONS,
        blackIconFillColorHex = prefs[Keys.BLACK_ICON_FILL_COLOR] ?: IconStyleConfig.DEFAULT_FILL_COLOR_HEX,
        blackIconOutlineColorHex = prefs[Keys.BLACK_ICON_OUTLINE_COLOR] ?: IconStyleConfig.DEFAULT_OUTLINE_COLOR_HEX,
        blackIconOutlineWidthDp = prefs[Keys.BLACK_ICON_OUTLINE_WIDTH] ?: IconStyleConfig.DEFAULT_OUTLINE_WIDTH_DP,
        blackIconDarknessThreshold = prefs[Keys.BLACK_ICON_DARKNESS_THRESHOLD] ?: IconStyleConfig.DEFAULT_DARKNESS_THRESHOLD,
        lineIconProfilesJson = prefs[Keys.LINE_ICON_PROFILES] ?: "",
        lineIconsEnabled = prefs[Keys.LINE_ICONS_ENABLED] ?: false,
        lineIconDefaultColorHex = prefs[Keys.LINE_ICON_DEFAULT_COLOR] ?: 0xFFFFFFFFL,
        lineIconDefaultLineWidthPx = prefs[Keys.LINE_ICON_DEFAULT_WIDTH] ?: 1.8f,
        lineIconDefaultOutlineEnabled = prefs[Keys.LINE_ICON_DEFAULT_OUTLINE_ENABLED] ?: false,
        lineIconDefaultOutlineColorHex = prefs[Keys.LINE_ICON_DEFAULT_OUTLINE_COLOR] ?: 0xFF000000L,
        lineIconDefaultOutlineWidthPx = prefs[Keys.LINE_ICON_DEFAULT_OUTLINE_WIDTH] ?: 1.2f,
        lockedAppsCsv = prefs[Keys.LOCKED_APPS] ?: "",
        cloudBackupEnabled = prefs[Keys.CLOUD_BACKUP_ENABLED] ?: false,
        cloudBackupTreeUri = prefs[Keys.CLOUD_BACKUP_TREE_URI],
        seededDefaults = prefs[Keys.SEEDED_DEFAULTS] ?: false,
    )

    val settingsFlow: Flow<LauncherSettings> = context.launcherDataStore.data.map(::fromPrefs)

    suspend fun getSettingsOnce(): LauncherSettings = settingsFlow.first()

    suspend fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        context.launcherDataStore.edit { prefs ->
            val updated = transform(fromPrefs(prefs))
            prefs[Keys.THEME_MODE] = updated.themeMode
            prefs[Keys.ACCENT_COLOR] = updated.accentColorHex
            prefs[Keys.SECONDARY_COLOR] = updated.secondaryColorHex
            prefs[Keys.TERTIARY_COLOR] = updated.tertiaryColorHex
            prefs[Keys.FONT_STYLE] = updated.fontStyle
            prefs[Keys.FONT_SCALE] = updated.fontScale
            prefs[Keys.HIGH_READABILITY] = updated.highReadabilityMode
            prefs[Keys.BOLD_LABELS] = updated.boldLabelsEnabled
            prefs[Keys.LABEL_BACKDROP] = updated.labelBackdropEnabled
            prefs[Keys.LABEL_BACKDROP_COLOR] = updated.labelBackdropColorHex
            prefs[Keys.LABEL_BACKDROP_ALPHA] = updated.labelBackdropAlpha
            prefs[Keys.TEXT_OUTLINE] = updated.textOutlineEnabled
            prefs[Keys.TEXT_OUTLINE_COLOR] = updated.textOutlineColorHex
            prefs[Keys.TEXT_OUTLINE_WIDTH] = updated.textOutlineWidthDp
            prefs[Keys.TEXT_SHADOW] = updated.textShadowEnabled
            prefs[Keys.TEXT_SHADOW_RADIUS] = updated.textShadowRadiusDp
            prefs[Keys.LETTER_SPACING_EM] = updated.letterSpacingEm
            prefs[Keys.LINE_HEIGHT_MULTIPLIER] = updated.lineHeightMultiplier
            prefs[Keys.MIN_LABEL_FONT_SP] = updated.minLabelFontSp
            prefs[Keys.LABEL_MAX_LINES] = updated.labelMaxLines
            prefs[Keys.AUTO_CONTRAST_TEXT] = updated.autoContrastText
            prefs[Keys.SURFACE_OPACITY] = updated.surfaceOpacity
            prefs[Keys.GLOBAL_BORDER_WIDTH] = updated.globalBorderWidthDp
            prefs[Keys.GLOBAL_TEXT_COLOR] = updated.globalTextColorHex
            prefs[Keys.SIDEBAR_TEXT_COLOR_MODE] = updated.sidebarTextColorMode
            prefs[Keys.SIDEBAR_CATEGORY_ICON_STYLE] = updated.sidebarCategoryIconStyle
            prefs[Keys.SHOW_HOME_APP_LABELS] = updated.showHomeAppLabels
            prefs[Keys.APP_LABEL_OUTLINE_ENABLED] = updated.appLabelOutlineEnabled
            prefs[Keys.WALLPAPER_TYPE] = updated.wallpaperType
            if (updated.wallpaperCustomPath != null) prefs[Keys.WALLPAPER_CUSTOM_PATH] = updated.wallpaperCustomPath
            else prefs.remove(Keys.WALLPAPER_CUSTOM_PATH)
            prefs[Keys.WALLPAPER_GRADIENT_START] = updated.wallpaperGradientStartHex
            prefs[Keys.WALLPAPER_GRADIENT_END] = updated.wallpaperGradientEndHex
            prefs[Keys.WALLPAPER_GRADIENT_DIR] = updated.wallpaperGradientDirection
            prefs[Keys.WALLPAPER_SOLID] = updated.wallpaperSolidHex
            prefs[Keys.WALLPAPER_BLUR_DP] = updated.wallpaperBlurDp
            prefs[Keys.WALLPAPER_DIM_ALPHA] = updated.wallpaperDimAlpha
            prefs[Keys.WALLPAPER_DIM_COLOR] = updated.wallpaperDimColorHex
            prefs[Keys.WALLPAPER_VIGNETTE] = updated.wallpaperVignette
            prefs[Keys.WALLPAPER_SATURATION] = updated.wallpaperSaturation
            prefs[Keys.WALLPAPER_CONTRAST] = updated.wallpaperContrast
            prefs[Keys.WALLPAPER_BRIGHTNESS] = updated.wallpaperBrightness
            prefs[Keys.WALLPAPER_PARALLAX] = updated.wallpaperParallaxIntensity
            if (updated.wallpaperTintHex != null) prefs[Keys.WALLPAPER_TINT] = updated.wallpaperTintHex else prefs.remove(Keys.WALLPAPER_TINT)
            prefs[Keys.WALLPAPER_AMBIENT] = updated.wallpaperAmbientMotion

            prefs[Keys.ALL_APPS_SIDEBAR_EDGE] = updated.allAppsSidebarEdge
            prefs[Keys.ALL_APPS_SIDEBAR_WIDTH] = updated.allAppsSidebarWidthFraction
            prefs[Keys.ALL_APPS_BG_TYPE] = updated.allAppsBgType
            prefs[Keys.ALL_APPS_BG_COLOR] = updated.allAppsBgColorHex
            prefs[Keys.ALL_APPS_BG_GRAD_END] = updated.allAppsBgGradientEndHex
            if (updated.allAppsBgImagePath != null) prefs[Keys.ALL_APPS_BG_IMAGE] = updated.allAppsBgImagePath
            else prefs.remove(Keys.ALL_APPS_BG_IMAGE)
            prefs[Keys.ALL_APPS_BG_BLUR] = updated.allAppsBgBlurDp

            prefs[Keys.DRAWER_LAYOUT_MODE] = updated.drawerLayoutMode
            prefs[Keys.DRAWER_COLUMNS] = updated.drawerColumns
            prefs[Keys.DRAWER_ICON_SIZE_DP] = updated.drawerIconSizeDp
            prefs[Keys.DRAWER_LABEL_SIZE_SP] = updated.drawerLabelSizeSp
            prefs[Keys.DRAWER_SORT_ORDER] = updated.drawerSortOrder
            prefs[Keys.DRAWER_CARD_OPACITY] = updated.drawerCardOpacity
            prefs[Keys.DRAWER_CORNER_RADIUS] = updated.drawerCornerRadiusDp
            prefs[Keys.DRAWER_SEARCH_BAR_VISIBLE] = updated.drawerSearchBarVisible
            prefs[Keys.DRAWER_SEARCH_BAR_POSITION] = updated.drawerSearchBarPosition
            prefs[Keys.DRAWER_CARD_SHADOW] = updated.drawerCardShadowDp
            prefs[Keys.DRAWER_CARD_BORDER_WIDTH] = updated.drawerCardBorderWidthDp
            prefs[Keys.DRAWER_CARD_BORDER_COLOR] = updated.drawerCardBorderColorHex

            prefs[Keys.NIAGARA_WAVE_ENABLED] = updated.niagaraWaveEnabled
            prefs[Keys.NIAGARA_INDEX_VISIBLE] = updated.niagaraIndexVisible
            prefs[Keys.NIAGARA_EDGE] = updated.niagaraEdge
            prefs[Keys.NIAGARA_WAVE_INTENSITY] = updated.niagaraWaveIntensity
            prefs[Keys.NIAGARA_BASE_FONT] = updated.niagaraBaseFontSp
            prefs[Keys.NIAGARA_SELECTED_FONT] = updated.niagaraSelectedFontSp
            prefs[Keys.NIAGARA_ACCENT] = updated.niagaraAccentColorHex
            prefs[Keys.NIAGARA_INDEX_WIDTH] = updated.niagaraIndexWidthDp
            prefs[Keys.NIAGARA_INDEX_COLOR] = updated.niagaraIndexColorHex
            prefs[Keys.NIAGARA_HAPTIC] = updated.niagaraHapticFeedback

            prefs[Keys.GRID_SNAP] = updated.gridSnapEnabled
            prefs[Keys.ALIGNMENT_GUIDES] = updated.alignmentGuidesEnabled
            prefs[Keys.ALIGNMENT_GUIDE_COLOR] = updated.alignmentGuideColorHex
            prefs[Keys.GRID_SNAP_STEP] = updated.gridSnapStepDp
            prefs[Keys.EDIT_GRID_VISIBLE] = updated.editGridVisible
            prefs[Keys.EDIT_GRID_OPACITY] = updated.editGridOpacity
            prefs[Keys.EDIT_GRID_COLOR] = updated.editGridColorHex
            prefs[Keys.LAYOUT_LOCKED] = updated.layoutLocked
            prefs[Keys.PAGE_TRANSITION] = updated.pageTransitionStyle
            prefs[Keys.WIDGET_FEED_ENABLED] = updated.widgetFeedEnabled
            prefs[Keys.WIDGET_FEED_TITLE_COLOR] = updated.widgetFeedTitleColor
            prefs[Keys.PAGE_INDICATOR_STYLE] = updated.pageIndicatorStyle
            prefs[Keys.PAGE_INDICATOR_COLOR] = updated.pageIndicatorColorHex
            prefs[Keys.PAGE_INDICATOR_SIZE] = updated.pageIndicatorSizeDp
            prefs[Keys.PAGE_INDICATOR_ACTIVE_WIDTH] = updated.pageIndicatorActiveWidthDp
            prefs[Keys.STATUS_STRIP_ENABLED] = updated.statusStripEnabled
            prefs[Keys.STATUS_STRIP_SHOW_DATE] = updated.statusStripShowDate
            prefs[Keys.STATUS_STRIP_SHOW_BATTERY] = updated.statusStripShowBattery
            prefs[Keys.STATUS_STRIP_ACCENT] = updated.statusStripAccentColorHex
            prefs[Keys.EDGE_HANDLES_VISIBLE] = updated.edgeHandlesVisible
            prefs[Keys.EDGE_HANDLE_OPACITY] = updated.edgeHandleOpacity
            prefs[Keys.EDGE_HANDLE_WIDTH] = updated.edgeHandleWidthDp
            prefs[Keys.EDGE_HANDLE_HEIGHT] = updated.edgeHandleHeightDp

            prefs[Keys.ANIMATION_SPEED_SCALE] = updated.animationSpeedScale
            prefs[Keys.GLOBAL_CORNER_RADIUS_DP] = updated.globalCornerRadiusDp
            prefs[Keys.GLOBAL_ICON_SHAPE] = updated.globalIconShape
            prefs[Keys.SHOW_DOCK_BAR] = updated.showDockBar
            prefs[Keys.SHOW_TAG_STRIP] = updated.showTagStripOnHome
            prefs[Keys.SHOW_DEFAULT_HOME_BANNER] = updated.showDefaultHomeBanner
            prefs[Keys.SHOW_STATUS_BAR] = updated.showStatusBar
            prefs[Keys.SHOW_NAV_BAR] = updated.showNavBar
            prefs[Keys.HAPTIC_FEEDBACK] = updated.hapticFeedbackEnabled
            prefs[Keys.NOTIFICATIONS_BADGE] = updated.notificationsBadgeHint
            prefs[Keys.RECENT_SEARCHES] = updated.recentSearchesCsv
            prefs[Keys.ICON_PACK_ORDER_CSV] = updated.iconPackOrderCsv
            prefs[Keys.BLACK_ICON_STYLE_ENABLED] = updated.blackIconStyleEnabled
            prefs[Keys.BLACK_ICON_MODE] = updated.blackIconMode
            prefs[Keys.BLACK_ICON_FILL_COLOR] = updated.blackIconFillColorHex
            prefs[Keys.BLACK_ICON_OUTLINE_COLOR] = updated.blackIconOutlineColorHex
            prefs[Keys.BLACK_ICON_OUTLINE_WIDTH] = updated.blackIconOutlineWidthDp
            prefs[Keys.BLACK_ICON_DARKNESS_THRESHOLD] = updated.blackIconDarknessThreshold
            prefs[Keys.LINE_ICON_PROFILES] = updated.lineIconProfilesJson
            prefs[Keys.LINE_ICONS_ENABLED] = updated.lineIconsEnabled
            prefs[Keys.LINE_ICON_DEFAULT_COLOR] = updated.lineIconDefaultColorHex
            prefs[Keys.LINE_ICON_DEFAULT_WIDTH] = updated.lineIconDefaultLineWidthPx
            prefs[Keys.LINE_ICON_DEFAULT_OUTLINE_ENABLED] = updated.lineIconDefaultOutlineEnabled
            prefs[Keys.LINE_ICON_DEFAULT_OUTLINE_COLOR] = updated.lineIconDefaultOutlineColorHex
            prefs[Keys.LINE_ICON_DEFAULT_OUTLINE_WIDTH] = updated.lineIconDefaultOutlineWidthPx
            prefs[Keys.LOCKED_APPS] = updated.lockedAppsCsv
            prefs[Keys.CLOUD_BACKUP_ENABLED] = updated.cloudBackupEnabled
            if (updated.cloudBackupTreeUri != null) prefs[Keys.CLOUD_BACKUP_TREE_URI] = updated.cloudBackupTreeUri
            else prefs.remove(Keys.CLOUD_BACKUP_TREE_URI)
            prefs[Keys.SEEDED_DEFAULTS] = updated.seededDefaults
        }
    }
}
