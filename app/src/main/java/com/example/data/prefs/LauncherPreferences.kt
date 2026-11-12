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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(name = "tagdeck_prefs")

data class LauncherSettings(
    val themeMode: String = "DARK", // DARK, LIGHT, SYSTEM, DYNAMIC
    val accentColorHex: Long = 0xFF38BDF8L,
    val wallpaperType: String = "BUILTIN_PRISM", // BUILTIN_PRISM, BUILTIN_TOPO, CUSTOM_IMAGE, GRADIENT, SOLID
    val wallpaperCustomPath: String? = null,
    val wallpaperGradientStartHex: Long = 0xFF0A1124L,
    val wallpaperGradientEndHex: Long = 0xFF1A1038L,
    val wallpaperSolidHex: Long = 0xFF090D16L,
    val wallpaperBlurDp: Float = 0f,
    val wallpaperDimAlpha: Float = 0.25f,
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
    // Niagara Wave Alphabet Navigation
    val niagaraWaveEnabled: Boolean = true,
    val niagaraIndexVisible: Boolean = true,
    val niagaraEdge: String = "RIGHT", // RIGHT or LEFT
    val niagaraWaveIntensity: Float = 1.0f, // 0f..1.8f
    val niagaraBaseFontSp: Float = 11f,
    val niagaraSelectedFontSp: Float = 24f,
    val niagaraAccentColorHex: Long = 0xFF38BDF8L,
    val niagaraHapticFeedback: Boolean = true,
    // Edit Mode & Canvas
    val gridSnapEnabled: Boolean = false,
    val alignmentGuidesEnabled: Boolean = true,
    val gridSnapStepDp: Int = 16,
    val layoutLocked: Boolean = false,
    val pageTransitionStyle: String = "SLIDE", // SLIDE, DEPTH, FADE
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
    val notificationsBadgeHint: Boolean = true,
    val recentSearchesCsv: String = "",
    val seededDefaults: Boolean = false,
)

class LauncherPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = longPreferencesKey("accent_color")
        val WALLPAPER_TYPE = stringPreferencesKey("wallpaper_type")
        val WALLPAPER_CUSTOM_PATH = stringPreferencesKey("wallpaper_custom_path")
        val WALLPAPER_GRADIENT_START = longPreferencesKey("wallpaper_gradient_start")
        val WALLPAPER_GRADIENT_END = longPreferencesKey("wallpaper_gradient_end")
        val WALLPAPER_SOLID = longPreferencesKey("wallpaper_solid")
        val WALLPAPER_BLUR_DP = floatPreferencesKey("wallpaper_blur_dp")
        val WALLPAPER_DIM_ALPHA = floatPreferencesKey("wallpaper_dim_alpha")

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

        val NIAGARA_WAVE_ENABLED = booleanPreferencesKey("niagara_wave_enabled")
        val NIAGARA_INDEX_VISIBLE = booleanPreferencesKey("niagara_index_visible")
        val NIAGARA_EDGE = stringPreferencesKey("niagara_edge")
        val NIAGARA_WAVE_INTENSITY = floatPreferencesKey("niagara_wave_intensity")
        val NIAGARA_BASE_FONT = floatPreferencesKey("niagara_base_font")
        val NIAGARA_SELECTED_FONT = floatPreferencesKey("niagara_selected_font")
        val NIAGARA_ACCENT = longPreferencesKey("niagara_accent")
        val NIAGARA_HAPTIC = booleanPreferencesKey("niagara_haptic")

        val GRID_SNAP = booleanPreferencesKey("grid_snap")
        val ALIGNMENT_GUIDES = booleanPreferencesKey("alignment_guides")
        val GRID_SNAP_STEP = intPreferencesKey("grid_snap_step")
        val LAYOUT_LOCKED = booleanPreferencesKey("layout_locked")
        val PAGE_TRANSITION = stringPreferencesKey("page_transition")

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
        val SEEDED_DEFAULTS = booleanPreferencesKey("seeded_defaults")
    }

    private fun fromPrefs(prefs: Preferences): LauncherSettings = LauncherSettings(
        themeMode = prefs[Keys.THEME_MODE] ?: "DARK",
        accentColorHex = prefs[Keys.ACCENT_COLOR] ?: 0xFF38BDF8L,
        wallpaperType = prefs[Keys.WALLPAPER_TYPE] ?: "BUILTIN_PRISM",
        wallpaperCustomPath = prefs[Keys.WALLPAPER_CUSTOM_PATH],
        wallpaperGradientStartHex = prefs[Keys.WALLPAPER_GRADIENT_START] ?: 0xFF0A1124L,
        wallpaperGradientEndHex = prefs[Keys.WALLPAPER_GRADIENT_END] ?: 0xFF1A1038L,
        wallpaperSolidHex = prefs[Keys.WALLPAPER_SOLID] ?: 0xFF090D16L,
        wallpaperBlurDp = prefs[Keys.WALLPAPER_BLUR_DP] ?: 0f,
        wallpaperDimAlpha = prefs[Keys.WALLPAPER_DIM_ALPHA] ?: 0.25f,
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
        niagaraWaveEnabled = prefs[Keys.NIAGARA_WAVE_ENABLED] ?: true,
        niagaraIndexVisible = prefs[Keys.NIAGARA_INDEX_VISIBLE] ?: true,
        niagaraEdge = prefs[Keys.NIAGARA_EDGE] ?: "RIGHT",
        niagaraWaveIntensity = prefs[Keys.NIAGARA_WAVE_INTENSITY] ?: 1.0f,
        niagaraBaseFontSp = prefs[Keys.NIAGARA_BASE_FONT] ?: 11f,
        niagaraSelectedFontSp = prefs[Keys.NIAGARA_SELECTED_FONT] ?: 24f,
        niagaraAccentColorHex = prefs[Keys.NIAGARA_ACCENT] ?: 0xFF38BDF8L,
        niagaraHapticFeedback = prefs[Keys.NIAGARA_HAPTIC] ?: true,
        gridSnapEnabled = prefs[Keys.GRID_SNAP] ?: false,
        alignmentGuidesEnabled = prefs[Keys.ALIGNMENT_GUIDES] ?: true,
        gridSnapStepDp = prefs[Keys.GRID_SNAP_STEP] ?: 16,
        layoutLocked = prefs[Keys.LAYOUT_LOCKED] ?: false,
        pageTransitionStyle = prefs[Keys.PAGE_TRANSITION] ?: "SLIDE",
        animationSpeedScale = prefs[Keys.ANIMATION_SPEED_SCALE] ?: 1f,
        globalCornerRadiusDp = prefs[Keys.GLOBAL_CORNER_RADIUS_DP] ?: 20f,
        globalIconShape = prefs[Keys.GLOBAL_ICON_SHAPE] ?: "SQUIRCLE",
        showDockBar = prefs[Keys.SHOW_DOCK_BAR] ?: true,
        showTagStripOnHome = prefs[Keys.SHOW_TAG_STRIP] ?: true,
        showDefaultHomeBanner = prefs[Keys.SHOW_DEFAULT_HOME_BANNER] ?: true,
        showStatusBar = prefs[Keys.SHOW_STATUS_BAR] ?: true,
        showNavBar = prefs[Keys.SHOW_NAV_BAR] ?: true,
        hapticFeedbackEnabled = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
        notificationsBadgeHint = prefs[Keys.NOTIFICATIONS_BADGE] ?: true,
        recentSearchesCsv = prefs[Keys.RECENT_SEARCHES] ?: "",
        seededDefaults = prefs[Keys.SEEDED_DEFAULTS] ?: false,
    )

    val settingsFlow: Flow<LauncherSettings> = context.launcherDataStore.data.map(::fromPrefs)

    suspend fun getSettingsOnce(): LauncherSettings = settingsFlow.first()

    suspend fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        context.launcherDataStore.edit { prefs ->
            val updated = transform(fromPrefs(prefs))
            prefs[Keys.THEME_MODE] = updated.themeMode
            prefs[Keys.ACCENT_COLOR] = updated.accentColorHex
            prefs[Keys.WALLPAPER_TYPE] = updated.wallpaperType
            if (updated.wallpaperCustomPath != null) prefs[Keys.WALLPAPER_CUSTOM_PATH] = updated.wallpaperCustomPath
            else prefs.remove(Keys.WALLPAPER_CUSTOM_PATH)
            prefs[Keys.WALLPAPER_GRADIENT_START] = updated.wallpaperGradientStartHex
            prefs[Keys.WALLPAPER_GRADIENT_END] = updated.wallpaperGradientEndHex
            prefs[Keys.WALLPAPER_SOLID] = updated.wallpaperSolidHex
            prefs[Keys.WALLPAPER_BLUR_DP] = updated.wallpaperBlurDp
            prefs[Keys.WALLPAPER_DIM_ALPHA] = updated.wallpaperDimAlpha

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

            prefs[Keys.NIAGARA_WAVE_ENABLED] = updated.niagaraWaveEnabled
            prefs[Keys.NIAGARA_INDEX_VISIBLE] = updated.niagaraIndexVisible
            prefs[Keys.NIAGARA_EDGE] = updated.niagaraEdge
            prefs[Keys.NIAGARA_WAVE_INTENSITY] = updated.niagaraWaveIntensity
            prefs[Keys.NIAGARA_BASE_FONT] = updated.niagaraBaseFontSp
            prefs[Keys.NIAGARA_SELECTED_FONT] = updated.niagaraSelectedFontSp
            prefs[Keys.NIAGARA_ACCENT] = updated.niagaraAccentColorHex
            prefs[Keys.NIAGARA_HAPTIC] = updated.niagaraHapticFeedback

            prefs[Keys.GRID_SNAP] = updated.gridSnapEnabled
            prefs[Keys.ALIGNMENT_GUIDES] = updated.alignmentGuidesEnabled
            prefs[Keys.GRID_SNAP_STEP] = updated.gridSnapStepDp
            prefs[Keys.LAYOUT_LOCKED] = updated.layoutLocked
            prefs[Keys.PAGE_TRANSITION] = updated.pageTransitionStyle

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
            prefs[Keys.SEEDED_DEFAULTS] = updated.seededDefaults
        }
    }
}
