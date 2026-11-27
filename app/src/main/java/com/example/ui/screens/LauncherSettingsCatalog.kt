package com.example.ui.screens

data class SettingEntrySpec(
    val id: String,
    val sectionId: String,
    val title: String,
    val subtitle: String,
    val sectionTitle: String,
    val keywords: List<String> = emptyList(),
)

data class SettingsSectionSpec(
    val id: String,
    val title: String,
    val keywords: List<String> = emptyList(),
    val items: List<SettingEntrySpec>,
)

object LauncherSettingsCatalog {

    const val SECTION_MANAGE = "manage"
    const val SECTION_CATEGORY_ICONS = "category_icons"
    const val SECTION_DEFAULT_HOME = "default_home"
    const val SECTION_MEDIA = "media"
    const val SECTION_THEME_TYPOGRAPHY = "theme_typography"
    const val SECTION_CUSTOM_FONTS_READABILITY = "custom_fonts_readability"
    const val SECTION_PALETTE_SURFACES = "palette_surfaces"
    const val SECTION_ICON_PACKS = "icon_packs"
    const val SECTION_ANYCONS = "anycons"
    const val SECTION_LINE_ICONS = "line_icons"
    const val SECTION_BLACK_ICONS = "black_icons"
    const val SECTION_BLACK_ICON_COLORS = "black_icon_colors"
    const val SECTION_WALLPAPER = "wallpaper"
    const val SECTION_DRAWER = "drawer"
    const val SECTION_NIAGARA = "niagara"
    const val SECTION_HOME_CANVAS = "home_canvas"
    const val SECTION_INDICATORS_HANDLES = "indicators_handles"
    const val SECTION_SYSTEM_UI = "system_ui"
    const val SECTION_GESTURES = "gestures"
    const val SECTION_BACKUP = "backup"
    const val SECTION_RESET = "reset"

    val sections: List<SettingsSectionSpec> = listOf(
        section(
            id = SECTION_MANAGE,
            title = "Manage Your Launcher",
            keywords = listOf("hub", "editors", "managers"),
            entries = listOf(
                entry("manage_tags", SECTION_MANAGE, "Tags & Categories", "Manage tags, Play categories, and smart rules", listOf("tag", "category", "rules", "collections")),
                entry("manage_apps", SECTION_MANAGE, "App Icons & Visibility", "Rename, hide, lock apps behind device authentication, set custom icons, or create black-and-white line icons", listOf("app", "rename", "hide", "lock", "app lock", "device authentication", "custom icon", "manager", "work profile", "managed apps", "line icon", "line art", "arcticons", "draw", "edge snapping")),
                entry("manage_sidebars", SECTION_MANAGE, "Sidebars & Floating Windows", "Configure 4-edge sidebars and floating widget windows", listOf("sidebar", "window", "floating", "edge", "panel")),
                entry("manage_custom_gestures", SECTION_MANAGE, "Custom Home Screen Gestures", "Configure custom swipe up/down/left/right and double-tap home actions", listOf("swipe", "gesture", "double tap", "two finger", "shortcut", "home action")),
            ),
        ),
        section(
            id = SECTION_CATEGORY_ICONS,
            title = "Category Icons & Open-State Badges",
            keywords = listOf("category icon", "open icon", "closed icon", "app drawer", "emoji", "svg", "badge"),
            entries = listOf(
                entry("category_icons_picker", SECTION_CATEGORY_ICONS, "Closed & Open Category Icons", "Customize built-in icons, emojis, or PNG/SVG badges for every drawer category when closed or open", listOf("category", "open icon", "closed icon", "drawer chip", "header", "emoji", "custom image")),
            ),
        ),
        section(
            id = SECTION_DEFAULT_HOME,
            title = "Default Home Application",
            keywords = listOf("home app", "launcher", "default"),
            entries = listOf(
                entry("default_home_app", SECTION_DEFAULT_HOME, "Set as Default Launcher", "Make TagDeck your device's default Home app", listOf("home button", "default launcher")),
            ),
        ),
        section(
            id = SECTION_MEDIA,
            title = "Media Library & Background Presets",
            keywords = listOf("images", "photos", "presets", "assets"),
            entries = listOf(
                entry("media_images", SECTION_MEDIA, "Image Library", "Browse, rename, or delete imported non-destructive images", listOf("photo", "library", "custom image")),
                entry("media_presets", SECTION_MEDIA, "BG Presets", "Save and apply reusable wallpaper background presets", listOf("background", "preset", "save")),
            ),
        ),
        section(
            id = SECTION_THEME_TYPOGRAPHY,
            title = "Theme & Typography",
            keywords = listOf("appearance", "style", "text", "fonts"),
            entries = listOf(
                entry("theme_mode", SECTION_THEME_TYPOGRAPHY, "Theme Mode", "Switch between Dark, Light, System, or Dynamic color", listOf("dark", "light", "system", "dynamic", "material you")),
                entry("font_family", SECTION_THEME_TYPOGRAPHY, "Font Family", "Choose Space Grotesk, Plus Jakarta, JetBrains Mono, System, or Custom uploaded font", listOf("font", "typography", "monospace", "grotesk", "jakarta", "ttf", "otf")),
                entry("font_scale", SECTION_THEME_TYPOGRAPHY, "Global Font Scale", "Scale typography size from 0.80x to 1.80x", listOf("text size", "font size", "zoom", "scale")),
                entry("text_color", SECTION_THEME_TYPOGRAPHY, "Home Element Default Text Color", "Choose default label and widget text color", listOf("label color", "text color", "foreground")),
            ),
        ),
        section(
            id = SECTION_CUSTOM_FONTS_READABILITY,
            title = "Custom Fonts & Readability Studio",
            keywords = listOf("ttf", "otf", "upload font", "readable", "legibility", "backdrop", "outline", "letter spacing", "contrast"),
            entries = listOf(
                entry("custom_font_upload", SECTION_CUSTOM_FONTS_READABILITY, "Upload & Manage Custom Fonts (.ttf / .otf)", "Import local font files and apply globally or per widget", listOf("upload", "custom font", "ttf", "otf", "typeface")),
                entry("font_readability_studio", SECTION_CUSTOM_FONTS_READABILITY, "Font Readability Studio", "High-readability toggle, bold labels, pill backdrop, stroke outline, letter spacing, line height, and auto contrast", listOf("readability", "contrast", "pill", "backdrop", "stroke", "outline", "shadow", "letter spacing", "line height")),
            ),
        ),
        section(
            id = SECTION_PALETTE_SURFACES,
            title = "Palette, Surfaces & Motion",
            keywords = listOf("colors", "cards", "shapes", "animations"),
            entries = listOf(
                entry("accent_colors", SECTION_PALETTE_SURFACES, "Primary, Secondary & Tertiary Colors", "Customize theme accent, secondary, tertiary, surface, card, and outline swatches", listOf("accent", "palette", "primary", "secondary", "tertiary", "surface", "card", "outline", "color")),
                entry("theme_profiles", SECTION_PALETTE_SURFACES, "Saved Theme Profiles", "Save and switch full color, font, and atmosphere profiles", listOf("theme profile", "preset", "save theme")),
                entry("surface_opacity", SECTION_PALETTE_SURFACES, "UI Surface Opacity", "Adjust transparency of cards, toolbars, and overlays", listOf("opacity", "alpha", "transparency", "glass")),
                entry("border_width", SECTION_PALETTE_SURFACES, "Global Border Width", "Adjust outline thickness on cards and controls", listOf("border", "stroke", "outline")),
                entry("icon_shape", SECTION_PALETTE_SURFACES, "Icon Shape", "Squircle, Circle, or Rounded Square app icon masks", listOf("squircle", "circle", "rounded square", "shape")),
                entry("corner_radius", SECTION_PALETTE_SURFACES, "Global Corner Radius", "Adjust roundness of cards, dialogs, and search bars", listOf("roundness", "corners", "radius")),
                entry("anim_speed", SECTION_PALETTE_SURFACES, "Animation Speed", "Control launcher transition and drawer animation speed", listOf("motion", "speed", "duration", "fast", "slow")),
            ),
        ),
        section(
            id = SECTION_ICON_PACKS,
            title = "Icon Packs & Loading Hierarchy",
            keywords = listOf("icons", "packs", "priority", "theme", "line icons", "monochrome"),
            entries = listOf(
                entry("icon_pack_order", SECTION_ICON_PACKS, "Active Icon Pack Hierarchy", "Reorder installed icon packs or rescan ADW/Nova packs; line-icon editing also works on pack artwork", listOf("icon pack", "apk", "priority", "order", "rescan", "adw", "nova", "line icon", "monochrome")),
            ),
        ),
        section(
            id = SECTION_ANYCONS,
            title = "Anycons Icon Styles",
            keywords = listOf("anycons", "niagara", "outline", "material", "sticker", "marble", "watercolor", "bright", "stellar", "chroma", "dots", "doodle", "clicks", "minimalist", "glyphs"),
            entries = listOf(
                entry("anycons_style", SECTION_ANYCONS, "Niagara-Style Anycons Collections", "Apply Outline, Material, Sticker, Marble, Watercolor, Bright, Stellar, Outline Chroma, Dots, Doodle, or Clicks icons", listOf("anycons", "niagara", "outline", "material", "sticker", "marble", "watercolor", "bright", "stellar", "chroma", "dots", "doodle", "clicks")),
                entry("anycons_customization", SECTION_ANYCONS, "Anycons Glyph Source, Color & Stroke", "Switch between minimalist glyphs, hybrid, or app artwork and tune color mode and stroke weight", listOf("glyphs", "app artwork", "hybrid", "stroke", "tint", "color")),
            ),
        ),
        section(
            id = SECTION_LINE_ICONS,
            title = "Arcticons-Style Line Icons",
            keywords = listOf("line art", "monochrome", "white icons", "black icons", "drawing", "edge snap"),
            entries = listOf(
                entry("line_icon_toggle", SECTION_LINE_ICONS, "Generate Line Icons for All Apps", "Automatically convert installed app icons to clean black or white contours", listOf("line icons", "all apps", "arcticons", "monochrome", "automatic")),
                entry("line_icon_style", SECTION_LINE_ICONS, "Line Color, Thickness & Contrast Outline", "Choose white or black lines and an optional thin contrast outline", listOf("line color", "white", "black", "outline", "thickness", "contrast")),
                entry("line_icon_drawing", SECTION_LINE_ICONS, "Draw and Snap Custom Icon Lines", "Refine individual app icons by drawing over detected edges", listOf("draw", "edge snap", "custom lines", "per app")),
            ),
        ),
        section(
            id = SECTION_BLACK_ICONS,
            title = "Black Icon Contrast & Outline",
            keywords = listOf("black icons", "white icons", "contrast", "outline"),
            entries = listOf(
                entry("black_icon_toggle", SECTION_BLACK_ICONS, "White + Black Outline for Black Icons", "Turn dark/black launcher icons white with a crisp black border", listOf("black icon", "white", "border", "outline", "legibility")),
                entry("black_icon_mode", SECTION_BLACK_ICONS, "Black Icon Detection Mode", "Black Icons only, All Monochrome, or Outline All", listOf("monochrome", "detection", "outline all")),
            ),
        ),
        section(
            id = SECTION_BLACK_ICON_COLORS,
            title = "Black Icon Outline & Colors",
            keywords = listOf("outline thickness", "threshold", "fill color"),
            entries = listOf(
                entry("black_icon_thickness", SECTION_BLACK_ICON_COLORS, "Outline Thickness & Blackness Threshold", "Tune border width (dp) and dark pixel threshold", listOf("thickness", "width", "threshold", "sensitivity")),
                entry("black_icon_swatches", SECTION_BLACK_ICON_COLORS, "Icon Fill & Outline Colors", "Customize icon fill tint, border color, or reset to white+black", listOf("fill", "outline color", "swatch", "reset")),
            ),
        ),
        section(
            id = SECTION_WALLPAPER,
            title = "Wallpaper & Atmosphere",
            keywords = listOf("background", "prism", "topo", "gradient", "parallax", "vignette", "aurora", "bokeh", "scrolling"),
            entries = listOf(
                entry("wallpaper_type", SECTION_WALLPAPER, "Wallpaper Style", "Choose Android system/live wallpaper, Prism, Topo, Gradient, Solid, or Custom Image", listOf("wallpaper", "live wallpaper", "system wallpaper", "prism", "topo", "gradient", "solid", "custom image")),
                entry("wallpaper_scrolling", SECTION_WALLPAPER, "Wallpaper Scrolling", "Scroll wallpaper horizontally across home pages", listOf("wallpaper", "scroll", "parallax", "paging")),
                entry("wallpaper_effects", SECTION_WALLPAPER, "Wallpaper Blur, Dim, Parallax & Color Grading", "Adjust background blur, dim tint, parallax depth, vignette, saturation, contrast, brightness, and ambient effects", listOf("blur", "dim", "darken", "tint", "parallax", "vignette", "saturation", "contrast", "brightness", "aurora", "bokeh", "ambient")),
            ),
        ),
        section(
            id = SECTION_DRAWER,
            title = "Universal All-Apps Drawer",
            keywords = listOf("all apps", "drawer", "grid", "list", "sort", "search position", "auto focus"),
            entries = listOf(
                entry("drawer_edge_size", SECTION_DRAWER, "Drawer Opening Side & Width", "Open from Right, Left, or Bottom and adjust drawer width", listOf("edge", "side", "width", "drawer")),
                entry("drawer_layout_sort", SECTION_DRAWER, "Drawer Layout Mode & App Sort", "Switch Grid vs List and sort A-Z, Most Used, Recent, Manual", listOf("grid", "list", "sort", "alphabetical", "most used", "recent", "manual")),
                entry("drawer_search_toggle", SECTION_DRAWER, "Drawer Search Bar & Position", "Toggle the search bar and place it at Top or Bottom", listOf("drawer search", "search bar", "top", "bottom")),
                entry("drawer_behavior", SECTION_DRAWER, "Drawer Launch & Auto-Focus Behavior", "Close drawer automatically on app launch and auto-focus drawer search", listOf("close on launch", "auto focus", "keyboard", "search")),
                entry("drawer_appearance", SECTION_DRAWER, "Drawer Background, Columns, Icon & Label Size", "Customize drawer background, blur, card opacity, columns, icon size", listOf("columns", "icon size", "label size", "card opacity", "corner radius", "blur")),
            ),
        ),
        section(
            id = SECTION_NIAGARA,
            title = "Niagara Wave Alphabet Index",
            keywords = listOf("alphabet", "a-z", "rail", "wave", "scrubber"),
            entries = listOf(
                entry("niagara_toggle", SECTION_NIAGARA, "Alphabet Index Rail & Wave", "Show A-Z rail, wave magnification, and scrub haptics", listOf("niagara", "alphabet", "index", "wave", "haptic")),
                entry("niagara_style", SECTION_NIAGARA, "Rail Edge, Intensity & Letter Sizes", "Pick Left/Right edge, wave intensity, font sizes, and accent color", listOf("rail", "letter size", "intensity", "accent")),
            ),
        ),
        section(
            id = SECTION_HOME_CANVAS,
            title = "Home Canvas, Grid & Folders",
            keywords = listOf("home screen", "dock", "grid", "snap", "lock", "folder", "status strip", "margins", "overlap", "landscape", "double tap"),
            entries = listOf(
                entry("home_dock_strip", SECTION_HOME_CANVAS, "Dock Bar, Tag Strip, Status Strip & Folder Cover Style", "Toggle dock, tag strip, custom HUD status strip, shortcut labels, and folder cover layout", listOf("dock", "search bar", "tag strip", "labels", "status strip", "hud", "folder", "fan", "stack", "grid")),
                entry("canvas_total_layout", SECTION_HOME_CANVAS, "Layout Behavior, Margins, Overlap & Dock Size", "Close folders on launch, return to default page on Home, separate landscape layout, prevent overlap, canvas padding, default shortcut dimensions, dock height/alpha, and double-tap page action", listOf("margins", "padding", "overlap", "landscape", "folder", "home button", "default page", "dock height", "dock alpha", "shortcut size", "double tap")),
                entry("layout_lock", SECTION_HOME_CANVAS, "Lock Entire Home Layout", "Prevent accidental moves or edits across all home pages", listOf("lock", "freeze", "protect")),
                entry("grid_snap", SECTION_HOME_CANVAS, "Grid Snap, Edit Grid Lines & Alignment Guides", "Toggle grid snap, snap step (dp), edit grid overlay, magnetic guides", listOf("grid", "snap", "step", "guidelines", "magnetic", "alignment")),
            ),
        ),
        section(
            id = SECTION_INDICATORS_HANDLES,
            title = "Page Indicators & Edge Handles",
            keywords = listOf("pages", "transitions", "dots", "handles", "widget feed", "loop", "3d"),
            entries = listOf(
                entry("page_transitions", SECTION_INDICATORS_HANDLES, "3D Page Transitions, Infinite Loop & Indicators", "Choose 11 transitions (Cube, Flip, Stack, Windmill, etc.), infinite page loop, indicator placement, and Widget Feed", listOf("transition", "depth", "fade", "slide", "cube", "flip", "windmill", "loop", "infinite", "widget feed", "indicator", "dots", "pill", "numbers", "progress", "tabs", "top", "bottom")),
                entry("edge_handles", SECTION_INDICATORS_HANDLES, "Edge Sidebar Handles", "Toggle edge handles and customize opacity, thickness, and length", listOf("edge handle", "sidebar handle", "thickness", "length")),
            ),
        ),
        section(
            id = SECTION_SYSTEM_UI,
            title = "System UI & Feedback",
            keywords = listOf("status bar", "navigation bar", "vibration", "notifications", "orientation", "keep screen on"),
            entries = listOf(
                entry("system_bars", SECTION_SYSTEM_UI, "Show Status Bar & Navigation Bar", "Toggle Android status bar and system navigation bar visibility", listOf("status bar", "nav bar", "immersive", "fullscreen")),
                entry("system_display_touch", SECTION_SYSTEM_UI, "Screen Orientation, Keep Screen On & Bar Icon Style", "Set System/Portrait/Landscape/Sensor orientation, keep screen awake, and force auto/light/dark status & navigation bar icons", listOf("orientation", "portrait", "landscape", "sensor", "keep screen on", "status bar icons", "nav bar icons", "light", "dark")),
                entry("feedback_notifs", SECTION_SYSTEM_UI, "Haptic Feedback & Notification Badges", "Toggle haptics and access Android notification counts", listOf("haptic", "vibrate", "notification", "badge")),
                entry("usage_access", SECTION_SYSTEM_UI, "App Usage Access", "Grant Android Usage Access for the screen-time widget", listOf("usage", "screen time", "app time", "statistics", "privacy")),
                entry("calendar_access", SECTION_SYSTEM_UI, "Calendar Event Access", "Allow the at-a-glance widget to show upcoming events", listOf("calendar", "events", "agenda", "at a glance", "permission")),
            ),
        ),
        section(
            id = SECTION_GESTURES,
            title = "Gestures & Shortcuts",
            keywords = listOf("swipe", "tap", "double tap", "long press", "pinch", "macro", "custom gesture", "sensitivity"),
            entries = listOf(
                entry("gesture_bindings", SECTION_GESTURES, "Home Screen Gestures", "Configure Swipe Up/Down/Left/Right, Pinch In/Out, Two-Finger Tap, Double Tap, Torch, Ringer, and Launcher actions", listOf("gesture", "custom swipe", "pinch", "two finger", "double tap", "torch", "ringer", "macro", "app action", "launcher function")),
                entry("gesture_touch_tuning", SECTION_GESTURES, "Touch Sensitivity, Long-Press Delay & Press Scale", "Tune swipe sensitivity, long-press hold delay (ms), and shortcut press animation scale", listOf("sensitivity", "long press delay", "press scale", "touch", "feedback")),
            ),
        ),
        section(
            id = SECTION_BACKUP,
            title = "Portable Local Backup & Restore",
            keywords = listOf("json", "export", "import", "save", "restore"),
            entries = listOf(
                entry("backup_restore", SECTION_BACKUP, "Export & Restore JSON Backup", "Save or restore pages, widgets, windows, tags, and settings", listOf("backup", "restore", "export", "import", "json")),
                entry("cloud_backup", SECTION_BACKUP, "Automatic Cloud Backup", "Choose any connected cloud document provider and schedule backups every 12 hours", listOf("cloud", "automatic", "sync", "drive", "dropbox", "onedrive", "backup schedule")),
            ),
        ),
        section(
            id = SECTION_RESET,
            title = "Reset Preferences",
            keywords = listOf("factory", "defaults", "clear"),
            entries = listOf(
                entry("reset_all", SECTION_RESET, "Reset All Launcher Preferences", "Restore launcher theme, layout, and visual settings to defaults", listOf("reset", "factory", "default")),
            ),
        ),
    )

    val allEntries: List<SettingEntrySpec> = sections.flatMap { it.items }

    fun tokenize(query: String): List<String> =
        query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }

    fun entryMatches(entry: SettingEntrySpec, tokens: List<String>, sectionKeywords: List<String> = emptyList()): Boolean {
        if (tokens.isEmpty()) return true
        val haystack = buildString {
            append(entry.title.lowercase()).append(' ')
            append(entry.subtitle.lowercase()).append(' ')
            append(entry.sectionTitle.lowercase()).append(' ')
            entry.keywords.forEach { append(it.lowercase()).append(' ') }
            sectionKeywords.forEach { append(it.lowercase()).append(' ') }
        }
        return tokens.all { haystack.contains(it) }
    }

    fun searchSettingsEntries(query: String): List<SettingEntrySpec> {
        val tokens = tokenize(query)
        if (tokens.isEmpty()) return emptyList()
        val q = query.trim().lowercase()
        val sectionKwMap = sections.associate { it.id to it.keywords }
        return allEntries
            .filter { entryMatches(it, tokens, sectionKwMap[it.sectionId].orEmpty()) }
            .sortedWith(
                compareByDescending<SettingEntrySpec> { it.title.lowercase().startsWith(q) }
                    .thenByDescending { it.title.lowercase().contains(q) }
                    .thenBy { it.title.lowercase() }
            )
    }

    /**
     * Returns a map of matching sectionId -> matching SettingEntrySpec list inside that section.
     * When [query] is blank, every section ID is included with an empty match list.
     */
    fun filterSections(query: String): Map<String, List<SettingEntrySpec>> {
        val tokens = tokenize(query)
        if (tokens.isEmpty()) {
            return sections.associate { it.id to emptyList() }
        }
        val result = linkedMapOf<String, List<SettingEntrySpec>>()
        sections.forEach { sec ->
            val secHaystack = (sec.title + " " + sec.keywords.joinToString(" ")).lowercase()
            val sectionMatches = tokens.all { secHaystack.contains(it) }
            val matchedItems = sec.items.filter { entryMatches(it, tokens, sec.keywords) }
            if (sectionMatches || matchedItems.isNotEmpty()) {
                result[sec.id] = if (matchedItems.isNotEmpty()) matchedItems else sec.items.take(2)
            }
        }
        return result
    }

    private fun section(
        id: String,
        title: String,
        keywords: List<String>,
        entries: List<SettingEntrySpec>,
    ): SettingsSectionSpec {
        return SettingsSectionSpec(
            id = id,
            title = title,
            keywords = keywords,
            items = entries.map { it.copy(sectionTitle = title) },
        )
    }

    private fun entry(
        id: String,
        sectionId: String,
        title: String,
        subtitle: String,
        keywords: List<String>,
    ): SettingEntrySpec {
        return SettingEntrySpec(
            id = id,
            sectionId = sectionId,
            title = title,
            subtitle = subtitle,
            sectionTitle = "",
            keywords = keywords,
        )
    }
}
