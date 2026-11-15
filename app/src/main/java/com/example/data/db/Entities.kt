package com.example.data.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val packageName: String,
    val componentName: String = "",
    val appName: String,
    val customName: String? = null,
    val customIconPath: String? = null,
    val iconScale: Float = 1f,
    val iconOffsetX: Float = 0f,
    val iconOffsetY: Float = 0f,
    val iconAlpha: Float = 1f,
    val primaryCategoryId: Long = 29L,
    val automaticCategoryId: Long = 29L,
    val manualCategoryOverride: Boolean = false,
    val categoryConfidence: String = "HEURISTIC", // AUTHORITATIVE, HEURISTIC, UNCERTAIN
    val isGame: Boolean = false,
    val hidden: Boolean = false,
    val favorite: Boolean = false,
    val manualOrder: Int = 0,
    val isInstalled: Boolean = true,
    val installTimeMillis: Long = 0L,
    val lastLaunchedAt: Long = 0L,
    val launchCount: Int = 0,
) {
    val displayName: String get() = customName?.takeIf { it.isNotBlank() } ?: appName
}

@Entity(
    tableName = "categories",
    indices = [Index(value = ["parentCategoryId"])]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val categoryId: Long = 0,
    val name: String,
    val iconKey: String = "apps",
    val customIconPath: String? = null,
    val customEmoji: String? = null,
    val colorHex: Long = 0xFF38BDF8L,
    val backgroundColorHex: Long = 0x2238BDF8L,
    val backgroundType: String = "INHERIT", // INHERIT, SOLID, GRADIENT, IMAGE
    val backgroundGradientEndHex: Long? = null,
    val backgroundImagePath: String? = null,
    val backgroundBlurDp: Float = 0f,
    val backgroundAlpha: Float = 1f,
    val showInMainBar: Boolean = true,
    val iconSizeDp: Int = 24,
    val iconOpenStateEnabled: Boolean = false,
    val openIconKey: String? = null,
    val openCustomIconPath: String? = null,
    val openCustomEmoji: String? = null,
    val openColorHex: Long? = null,
    val parentCategoryId: Long? = null,
    val sortOrder: Int = 0,
    val categoryType: String = "APP", // SYSTEM_ALL, SYSTEM_FAVORITES, APP, GAMES_PARENT, GAME_SUB, CUSTOM
    val isCustom: Boolean = false,
) {
    val isSystemDockTab: Boolean
        get() = categoryId < 0L || categoryType == "SYSTEM_ALL" || categoryType == "SYSTEM_FAVORITES"
}

@Entity(
    tableName = "app_categories",
    primaryKeys = ["packageName", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = AppEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"]), Index(value = ["packageName"])]
)
data class AppCategoryCrossRef(
    val packageName: String,
    val categoryId: Long,
    val isManualAssignment: Boolean = false,
    val isExcluded: Boolean = false,
)

@Entity(
    tableName = "tags",
    indices = [Index(value = ["parentTagId"])]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val tagId: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: Long = 0xFF38BDF8L,
    val backgroundColorHex: Long = 0xEA0C1220L,
    val backgroundImagePath: String? = null,
    val iconKey: String = "tag",
    val customImagePath: String? = null,
    val sortOrder: Int = 0,
    val parentTagId: Long? = null,
    val isVisible: Boolean = true,
    val sidebarLayoutMode: String = "GRID", // GRID or LIST
    val sidebarIconSizeDp: Int = 52,
    val sidebarColumns: Int = 3,
    val showCountInSidebar: Boolean = true,
    val showOnHome: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "app_tags",
    primaryKeys = ["packageName", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = AppEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["tagId"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tagId"]), Index(value = ["packageName"])]
)
data class AppTagCrossRef(
    val packageName: String,
    val tagId: Long,
    val assignedAt: Long = System.currentTimeMillis(),
)

data class TagWithApps(
    @Embedded val tag: TagEntity,
    @Relation(
        parentColumn = "tagId",
        entityColumn = "packageName",
        associateBy = Junction(AppTagCrossRef::class)
    )
    val apps: List<AppEntity>,
)

data class AppWithTags(
    @Embedded val app: AppEntity,
    @Relation(
        parentColumn = "packageName",
        entityColumn = "tagId",
        associateBy = Junction(AppTagCrossRef::class)
    )
    val tags: List<TagEntity>,
    @Relation(
        parentColumn = "packageName",
        entityColumn = "categoryId",
        associateBy = Junction(AppCategoryCrossRef::class)
    )
    val categories: List<CategoryEntity> = emptyList(),
)

@Entity(tableName = "saved_tag_rules")
data class SavedTagRuleEntity(
    @PrimaryKey(autoGenerate = true) val ruleId: Long = 0,
    val name: String,
    val colorHex: Long = 0xFF6366F1L,
    val iconKey: String = "filter",
    val matchMode: String = "AND", // AND or OR
    val includeTagIdsCsv: String = "",
    val excludeTagIdsCsv: String = "",
    val includeNestedTags: Boolean = false,
    val sortOrder: Int = 0,
) {
    fun includeIds(): List<Long> =
        includeTagIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }

    fun excludeIds(): List<Long> =
        excludeTagIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
}

@Entity(tableName = "home_pages")
data class HomePageEntity(
    @PrimaryKey(autoGenerate = true) val pageId: Long = 0,
    val title: String,
    val pageOrder: Int = 0,
    val isDefaultHome: Boolean = false,
    val isHidden: Boolean = false,
    val isLocked: Boolean = false,
    val verticallyScrollable: Boolean = false,
    val paddingDp: Float = 0f,
    val backgroundType: String = "INHERIT", // INHERIT, SOLID, GRADIENT, IMAGE, TRANSPARENT
    val backgroundColorHex: Long? = null,
    val backgroundGradientEndHex: Long? = null,
    val backgroundImagePath: String? = null,
    val landscapeBackgroundImagePath: String? = null,
    val backgroundBlurDp: Float = 0f,
    val backgroundAlpha: Float = 0.85f,
    val pageTapActionJson: String? = null,
    val pageGesturesJson: String? = null,
    val transitionOverride: String? = null,
    val transitionDurationMs: Int = 320,
    val loopNavigation: Boolean = false,
    val parallaxEnabled: Boolean = false,
    val showDockOverride: Boolean? = null,
)

@Entity(tableName = "launcher_windows")
data class LauncherWindowEntity(
    @PrimaryKey(autoGenerate = true) val windowId: Long = 0,
    val title: String,
    val fullScreen: Boolean = false,
    val widthFraction: Float = 0.88f,
    val heightFraction: Float = 0.68f,
    val alignment: String = "CENTER", // CENTER, TOP, BOTTOM, LEFT, RIGHT
    val backgroundColorHex: Long = 0xEE0D1526L,
    val backgroundImagePath: String? = null,
    val blurBehindDp: Float = 8f,
    val cornerRadiusDp: Float = 28f,
    val animationStyle: String = "SCALE", // SCALE, SLIDE, FADE
)

@Entity(tableName = "home_elements", indices = [Index(value = ["parentContainerId"])])
data class HomeElementEntity(
    @PrimaryKey(autoGenerate = true) val elementId: Long = 0,
    val pageId: Long,
    val ownerType: String = "PAGE", // PAGE or WINDOW
    val ownerWindowId: Long? = null,
    val type: String, // APP_SHORTCUT, TAG_SHORTCUT, CATEGORY_SHORTCUT, SAVED_RULE_SHORTCUT, SIDEBAR_SHORTCUT, WINDOW_SHORTCUT, FOLDER, TEXT_LABEL, IMAGE, SHAPE, CUSTOM_BUTTON, CLOCK_WIDGET, ANALOG_CLOCK, CALENDAR_WIDGET, BATTERY_WIDGET, STORAGE_RAM_WIDGET, COMPASS_WIDGET, MEDIA_WIDGET, PHOTO_SLIDESHOW, TAG_COLLECTION_WIDGET, APP_WIDGET
    val xFraction: Float = 0.1f,
    val yFraction: Float = 0.1f,
    val landscapeXFraction: Float? = null,
    val landscapeYFraction: Float? = null,
    val widthDp: Float = 88f,
    val heightDp: Float = 96f,
    val rotationDeg: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val skewX: Float = 0f,
    val skewY: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val zIndex: Int = 0,
    val alpha: Float = 1f,
    val locked: Boolean = false,
    val visible: Boolean = true,
    val groupId: String? = null,
    val parentContainerId: Long? = null,
    val label: String = "",
    val targetPackageName: String? = null,
    val targetTagId: Long? = null,
    val targetCategoryId: Long? = null,
    val targetRuleId: Long? = null,
    val targetSidebarId: Long? = null,
    val targetWindowId: Long? = null,
    val folderAppPackagesCsv: String = "",
    val nestedFolderIdsCsv: String = "",
    val folderLayoutMode: String = "GRID",
    val appWidgetId: Int? = null,
    val imagePath: String? = null,
    val imageScaleType: String = "CROP", // CROP, FIT, FILL_XY, FIT_START, FIT_END, ORIGINAL
    val imageMaskShape: String = "NONE", // NONE, CIRCLE, SQUIRCLE, DIAMOND, HEXAGON
    val imageBrightness: Float = 0f,
    val imageContrast: Float = 1f,
    val imageSaturation: Float = 1f,
    val imageHueDeg: Float = 0f,
    val imageGrayscale: Boolean = false,
    val imageBlurDp: Float = 0f,
    val imageTintHex: Long? = null,
    val extraImageLayersJson: String = "",
    val actionsConfigJson: String = "",
    val backgroundColorHex: Long = 0x33161D2FL,
    val backgroundGradientEndHex: Long? = null,
    val textColorHex: Long = 0xFFF8FAFC,
    val borderColorHex: Long = 0x38FFFFFF,
    val borderWidthDp: Float = 1f,
    val cornerRadiusDp: Float = 20f,
    val paddingDp: Float = 6f,
    val shadowElevationDp: Float = 4f,
    val fontSizeSp: Float = 13f,
    val showLabel: Boolean = true,
    val iconSizeDp: Float = 48f,
    val styleExtrasJson: String = "",
    val rotationXDeg: Float = 0f,
    val rotationYDeg: Float = 0f,
    val perspective: Float = 0f,
    val widgetStackItemsJson: String = "",
    val widgetStackIndex: Int = 0,
    val widgetStackSwipeEnabled: Boolean = true,
    val pinnedToViewport: Boolean = false,
    val pinnedToAllPages: Boolean = false,
    val visibilityGroupId: String? = null,
    val landscapeWidthDp: Float? = null,
    val landscapeHeightDp: Float? = null,
    val pinEdge: String = "NONE",
    val pinOffsetDp: Float = 0f,
) {
    fun folderPackages(): List<String> =
        folderAppPackagesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun nestedFolderIds(): List<Long> =
        nestedFolderIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
}

@Entity(tableName = "layout_snapshots")
data class LayoutSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val snapshotId: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val payloadJson: String,
)

@Entity(tableName = "sidebar_configs")
data class SidebarConfigEntity(
    @PrimaryKey(autoGenerate = true) val sidebarId: Long = 0,
    val title: String,
    val side: String = "LEFT", // LEFT, RIGHT, TOP, BOTTOM
    val sourceType: String = "FAVORITES", // FAVORITES, ALL_APPS, TAG, CATEGORY, RULE, CUSTOM_COLLECTION
    val targetTagId: Long? = null,
    val targetCategoryId: Long? = null,
    val targetRuleId: Long? = null,
    val customPackagesCsv: String = "",
    val widthFraction: Float = 0.80f,
    val heightFraction: Float = 0.72f,
    val backgroundColorHex: Long = 0xEA0C1220L,
    val backgroundGradientEndHex: Long? = null,
    val backgroundImagePath: String? = null,
    val backgroundBlurDp: Float = 0f,
    val cornerRadiusDp: Float = 28f,
    val layoutMode: String = "GRID", // GRID or LIST
    val iconSizeDp: Int = 52,
    val columns: Int = 3,
    val animationStyle: String = "SLIDE", // SLIDE, SCALE, FADE
    val edgeZoneEnabled: Boolean = true,
    val showHandle: Boolean = true,
    val pinned: Boolean = false,
    val sortOrder: Int = 0,
) {
    fun customPackages(): List<String> =
        customPackagesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "gesture_bindings")
data class GestureBindingEntity(
    @PrimaryKey val gestureType: String,
    val actionType: String,
    val targetPackageName: String? = null,
    val targetTagId: Long? = null,
    val targetCategoryId: Long? = null,
    val targetSidebarId: Long? = null,
    val targetWindowId: Long? = null,
    val targetRuleId: Long? = null,
    val targetUrl: String? = null,
    val macroStepsJson: String? = null,
)

@Entity(tableName = "image_resources")
data class ImageResourceEntity(
    @PrimaryKey(autoGenerate = true) val imageId: Long = 0,
    val name: String,
    val folderName: String = "Imported",
    val localPath: String,
    val sourceUri: String = "",
    val fileFormat: String = "png",
    val widthPx: Int = 0,
    val heightPx: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "background_presets")
data class BackgroundPresetEntity(
    @PrimaryKey(autoGenerate = true) val presetId: Long = 0,
    val name: String,
    val ownerScope: String = "GLOBAL", // PAGE, DRAWER, SIDEBAR, OBJECT, GLOBAL
    val backgroundType: String = "GRADIENT", // SOLID, GRADIENT, IMAGE
    val colorHex: Long = 0xFF0A1124L,
    val gradientEndHex: Long = 0xFF1A1038L,
    val imagePath: String? = null,
    val blurDp: Float = 0f,
    val alpha: Float = 1f,
)


@Entity(tableName = "custom_fonts")
data class CustomFontEntity(
    @PrimaryKey(autoGenerate = true) val fontId: Long = 0,
    val displayName: String,
    val localPath: String,
    val familyName: String = "",
    val weight: Int = 400,
    val isItalic: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "theme_profiles")
data class ThemeProfileEntity(
    @PrimaryKey(autoGenerate = true) val profileId: Long = 0,
    val name: String,
    val description: String = "",
    val settingsJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isBuiltIn: Boolean = false,
)
