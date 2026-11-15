package com.example.data.system

import com.example.data.db.AppCategoryCrossRef
import com.example.data.db.AppEntity
import com.example.data.db.AppTagCrossRef
import com.example.data.db.BackgroundPresetEntity
import com.example.data.db.CategoryEntity
import com.example.data.db.CustomFontEntity
import com.example.data.db.ThemeProfileEntity
import com.example.data.db.GestureBindingEntity
import com.example.data.db.HomeElementEntity
import com.example.data.db.HomePageEntity
import com.example.data.db.ImageResourceEntity
import com.example.data.db.LauncherWindowEntity
import com.example.data.db.SavedTagRuleEntity
import com.example.data.db.SidebarConfigEntity
import com.example.data.db.TagEntity
import com.example.data.prefs.LauncherSettings
import com.example.ui.components.IconStyleConfig
import org.json.JSONArray
import org.json.JSONObject

data class FullBackupPayload(
    val apps: List<AppEntity>,
    val categories: List<CategoryEntity> = emptyList(),
    val appCategories: List<AppCategoryCrossRef> = emptyList(),
    val tags: List<TagEntity>,
    val crossRefs: List<AppTagCrossRef>,
    val rules: List<SavedTagRuleEntity>,
    val pages: List<HomePageEntity>,
    val windows: List<LauncherWindowEntity> = emptyList(),
    val elements: List<HomeElementEntity>,
    val sidebars: List<SidebarConfigEntity>,
    val gestures: List<GestureBindingEntity>,
    val imageResources: List<ImageResourceEntity> = emptyList(),
    val embeddedImagesBase64: Map<String, String> = emptyMap(),
    val backgroundPresets: List<BackgroundPresetEntity> = emptyList(),
    val customFonts: List<CustomFontEntity> = emptyList(),
    val themeProfiles: List<ThemeProfileEntity> = emptyList(),
    val embeddedFontsBase64: Map<String, String> = emptyMap(),
    val settings: LauncherSettings,
)

object BackupManager {

    fun encodeFullBackup(payload: FullBackupPayload): String {
        val root = JSONObject()
        root.put("version", 3)
        root.put("exportedAt", System.currentTimeMillis())

        val appsArr = JSONArray()
        payload.apps.forEach { app ->
            appsArr.put(JSONObject().apply {
                put("packageName", app.packageName)
                put("componentName", app.componentName)
                put("appName", app.appName)
                put("customName", app.customName ?: JSONObject.NULL)
                put("customIconPath", app.customIconPath ?: JSONObject.NULL)
                put("iconScale", app.iconScale.toDouble())
                put("iconOffsetX", app.iconOffsetX.toDouble())
                put("iconOffsetY", app.iconOffsetY.toDouble())
                put("iconAlpha", app.iconAlpha.toDouble())
                put("primaryCategoryId", app.primaryCategoryId)
                put("automaticCategoryId", app.automaticCategoryId)
                put("manualCategoryOverride", app.manualCategoryOverride)
                put("categoryConfidence", app.categoryConfidence)
                put("isGame", app.isGame)
                put("hidden", app.hidden)
                put("favorite", app.favorite)
                put("manualOrder", app.manualOrder)
            })
        }
        root.put("apps", appsArr)

        val catsArr = JSONArray()
        payload.categories.forEach { c ->
            catsArr.put(JSONObject().apply {
                put("categoryId", c.categoryId)
                put("name", c.name)
                put("iconKey", c.iconKey)
                put("customIconPath", c.customIconPath ?: JSONObject.NULL)
                put("customEmoji", c.customEmoji ?: JSONObject.NULL)
                put("colorHex", c.colorHex)
                put("backgroundColorHex", c.backgroundColorHex)
                put("backgroundType", c.backgroundType)
                put("backgroundGradientEndHex", c.backgroundGradientEndHex ?: JSONObject.NULL)
                put("backgroundImagePath", c.backgroundImagePath ?: JSONObject.NULL)
                put("backgroundBlurDp", c.backgroundBlurDp.toDouble())
                put("iconSizeDp", c.iconSizeDp)
                put("iconOpenStateEnabled", c.iconOpenStateEnabled)
                put("openIconKey", c.openIconKey ?: JSONObject.NULL)
                put("openCustomIconPath", c.openCustomIconPath ?: JSONObject.NULL)
                put("openCustomEmoji", c.openCustomEmoji ?: JSONObject.NULL)
                put("openColorHex", c.openColorHex ?: JSONObject.NULL)
                put("parentCategoryId", c.parentCategoryId ?: JSONObject.NULL)
                put("sortOrder", c.sortOrder)
                put("categoryType", c.categoryType)
                put("isCustom", c.isCustom)
            })
        }
        root.put("categories", catsArr)

        val appCatsArr = JSONArray()
        payload.appCategories.forEach { ac ->
            appCatsArr.put(JSONObject().apply {
                put("packageName", ac.packageName)
                put("categoryId", ac.categoryId)
                put("isManualAssignment", ac.isManualAssignment)
                put("isExcluded", ac.isExcluded)
            })
        }
        root.put("appCategories", appCatsArr)

        val tagsArr = JSONArray()
        payload.tags.forEach { tag ->
            tagsArr.put(JSONObject().apply {
                put("tagId", tag.tagId)
                put("name", tag.name)
                put("description", tag.description)
                put("colorHex", tag.colorHex)
                put("backgroundColorHex", tag.backgroundColorHex)
                put("backgroundImagePath", tag.backgroundImagePath ?: JSONObject.NULL)
                put("iconKey", tag.iconKey)
                put("customImagePath", tag.customImagePath ?: JSONObject.NULL)
                put("sortOrder", tag.sortOrder)
                put("parentTagId", tag.parentTagId ?: JSONObject.NULL)
                put("isVisible", tag.isVisible)
                put("sidebarLayoutMode", tag.sidebarLayoutMode)
                put("sidebarIconSizeDp", tag.sidebarIconSizeDp)
                put("sidebarColumns", tag.sidebarColumns)
                put("showCountInSidebar", tag.showCountInSidebar)
            })
        }
        root.put("tags", tagsArr)

        val refsArr = JSONArray()
        payload.crossRefs.forEach { ref ->
            refsArr.put(JSONObject().apply {
                put("packageName", ref.packageName)
                put("tagId", ref.tagId)
            })
        }
        root.put("appTags", refsArr)

        val rulesArr = JSONArray()
        payload.rules.forEach { r ->
            rulesArr.put(JSONObject().apply {
                put("ruleId", r.ruleId)
                put("name", r.name)
                put("colorHex", r.colorHex)
                put("iconKey", r.iconKey)
                put("matchMode", r.matchMode)
                put("includeTagIdsCsv", r.includeTagIdsCsv)
                put("excludeTagIdsCsv", r.excludeTagIdsCsv)
                put("includeNestedTags", r.includeNestedTags)
                put("sortOrder", r.sortOrder)
            })
        }
        root.put("rules", rulesArr)

        val pagesArr = JSONArray()
        payload.pages.forEach { p ->
            pagesArr.put(encodePage(p))
        }
        root.put("pages", pagesArr)

        val windowsArr = JSONArray()
        payload.windows.forEach { w ->
            windowsArr.put(JSONObject().apply {
                put("windowId", w.windowId)
                put("title", w.title)
                put("fullScreen", w.fullScreen)
                put("widthFraction", w.widthFraction.toDouble())
                put("heightFraction", w.heightFraction.toDouble())
                put("alignment", w.alignment)
                put("backgroundColorHex", w.backgroundColorHex)
                put("backgroundImagePath", w.backgroundImagePath ?: JSONObject.NULL)
                put("blurBehindDp", w.blurBehindDp.toDouble())
                put("cornerRadiusDp", w.cornerRadiusDp.toDouble())
                put("animationStyle", w.animationStyle)
            })
        }
        root.put("windows", windowsArr)

        val elemsArr = JSONArray()
        payload.elements.forEach { elemsArr.put(encodeElement(it)) }
        root.put("elements", elemsArr)

        val sidebarsArr = JSONArray()
        payload.sidebars.forEach { s ->
            sidebarsArr.put(JSONObject().apply {
                put("sidebarId", s.sidebarId)
                put("title", s.title)
                put("side", s.side)
                put("sourceType", s.sourceType)
                put("targetTagId", s.targetTagId ?: JSONObject.NULL)
                put("targetCategoryId", s.targetCategoryId ?: JSONObject.NULL)
                put("targetRuleId", s.targetRuleId ?: JSONObject.NULL)
                put("customPackagesCsv", s.customPackagesCsv)
                put("widthFraction", s.widthFraction.toDouble())
                put("heightFraction", s.heightFraction.toDouble())
                put("backgroundColorHex", s.backgroundColorHex)
                put("backgroundGradientEndHex", s.backgroundGradientEndHex ?: JSONObject.NULL)
                put("backgroundImagePath", s.backgroundImagePath ?: JSONObject.NULL)
                put("backgroundBlurDp", s.backgroundBlurDp.toDouble())
                put("cornerRadiusDp", s.cornerRadiusDp.toDouble())
                put("layoutMode", s.layoutMode)
                put("iconSizeDp", s.iconSizeDp)
                put("columns", s.columns)
                put("animationStyle", s.animationStyle)
                put("edgeZoneEnabled", s.edgeZoneEnabled)
                put("showHandle", s.showHandle)
                put("pinned", s.pinned)
                put("sortOrder", s.sortOrder)
            })
        }
        root.put("sidebars", sidebarsArr)

        val gesturesArr = JSONArray()
        payload.gestures.forEach { g ->
            gesturesArr.put(JSONObject().apply {
                put("gestureType", g.gestureType)
                put("actionType", g.actionType)
                put("targetPackageName", g.targetPackageName ?: JSONObject.NULL)
                put("targetTagId", g.targetTagId ?: JSONObject.NULL)
                put("targetCategoryId", g.targetCategoryId ?: JSONObject.NULL)
                put("targetSidebarId", g.targetSidebarId ?: JSONObject.NULL)
                put("targetWindowId", g.targetWindowId ?: JSONObject.NULL)
                put("targetRuleId", g.targetRuleId ?: JSONObject.NULL)
                put("targetUrl", g.targetUrl ?: JSONObject.NULL)
                put("macroStepsJson", g.macroStepsJson ?: JSONObject.NULL)
            })
        }
        root.put("gestures", gesturesArr)

        val imgsArr = JSONArray()
        payload.imageResources.forEach { img ->
            imgsArr.put(JSONObject().apply {
                put("imageId", img.imageId)
                put("name", img.name)
                put("folderName", img.folderName)
                put("localPath", img.localPath)
                put("sourceUri", img.sourceUri)
                put("fileFormat", img.fileFormat)
                put("widthPx", img.widthPx)
                put("heightPx", img.heightPx)
                val b64 = payload.embeddedImagesBase64[img.localPath]
                if (b64 != null) put("base64", b64)
            })
        }
        root.put("imageResources", imgsArr)

        val presetsArr = JSONArray()
        payload.backgroundPresets.forEach { pr ->
            presetsArr.put(JSONObject().apply {
                put("presetId", pr.presetId)
                put("name", pr.name)
                put("ownerScope", pr.ownerScope)
                put("backgroundType", pr.backgroundType)
                put("colorHex", pr.colorHex)
                put("gradientEndHex", pr.gradientEndHex)
                put("imagePath", pr.imagePath ?: JSONObject.NULL)
                put("blurDp", pr.blurDp.toDouble())
                put("alpha", pr.alpha.toDouble())
            })
        }
        root.put("backgroundPresets", presetsArr)

        val fontsArr = JSONArray()
        payload.customFonts.forEach { f ->
            fontsArr.put(JSONObject().apply {
                put("fontId", f.fontId)
                put("displayName", f.displayName)
                put("localPath", f.localPath)
                put("familyName", f.familyName)
                put("weight", f.weight)
                put("isItalic", f.isItalic)
                put("isFavorite", f.isFavorite)
                put("createdAt", f.createdAt)
                payload.embeddedFontsBase64[f.localPath]?.let { put("base64", it) }
            })
        }
        root.put("customFonts", fontsArr)

        val themesArr = JSONArray()
        payload.themeProfiles.forEach { t ->
            themesArr.put(JSONObject().apply {
                put("profileId", t.profileId)
                put("name", t.name)
                put("description", t.description)
                put("settingsJson", t.settingsJson)
                put("createdAt", t.createdAt)
                put("updatedAt", t.updatedAt)
                put("isBuiltIn", t.isBuiltIn)
            })
        }
        root.put("themeProfiles", themesArr)

        val s = payload.settings
        root.put("settings", JSONObject().apply {
            put("themeMode", s.themeMode)
            put("accentColorHex", s.accentColorHex)
            put("secondaryColorHex", s.secondaryColorHex)
            put("tertiaryColorHex", s.tertiaryColorHex)
            put("fontStyle", s.fontStyle)
            put("fontScale", s.fontScale.toDouble())
            put("highReadabilityMode", s.highReadabilityMode)
            put("surfaceOpacity", s.surfaceOpacity.toDouble())
            put("globalBorderWidthDp", s.globalBorderWidthDp.toDouble())
            put("globalTextColorHex", s.globalTextColorHex)
            put("showHomeAppLabels", s.showHomeAppLabels)
            put("wallpaperType", s.wallpaperType)
            put("wallpaperCustomPath", s.wallpaperCustomPath ?: JSONObject.NULL)
            put("wallpaperGradientStartHex", s.wallpaperGradientStartHex)
            put("wallpaperGradientEndHex", s.wallpaperGradientEndHex)
            put("wallpaperGradientDirection", s.wallpaperGradientDirection)
            put("wallpaperSolidHex", s.wallpaperSolidHex)
            put("wallpaperBlurDp", s.wallpaperBlurDp.toDouble())
            put("wallpaperDimAlpha", s.wallpaperDimAlpha.toDouble())
            put("wallpaperDimColorHex", s.wallpaperDimColorHex)
            put("wallpaperVignette", s.wallpaperVignette.toDouble())
            put("wallpaperSaturation", s.wallpaperSaturation.toDouble())
            put("wallpaperParallaxIntensity", s.wallpaperParallaxIntensity.toDouble())
            put("wallpaperTintHex", s.wallpaperTintHex ?: JSONObject.NULL)
            put("wallpaperAmbientMotion", s.wallpaperAmbientMotion)
            put("allAppsSidebarEdge", s.allAppsSidebarEdge)
            put("allAppsSidebarWidthFraction", s.allAppsSidebarWidthFraction.toDouble())
            put("allAppsBgType", s.allAppsBgType)
            put("allAppsBgColorHex", s.allAppsBgColorHex)
            put("allAppsBgGradientEndHex", s.allAppsBgGradientEndHex)
            put("allAppsBgImagePath", s.allAppsBgImagePath ?: JSONObject.NULL)
            put("allAppsBgBlurDp", s.allAppsBgBlurDp.toDouble())
            put("drawerLayoutMode", s.drawerLayoutMode)
            put("drawerColumns", s.drawerColumns)
            put("drawerIconSizeDp", s.drawerIconSizeDp)
            put("drawerLabelSizeSp", s.drawerLabelSizeSp)
            put("drawerSortOrder", s.drawerSortOrder)
            put("drawerCardOpacity", s.drawerCardOpacity.toDouble())
            put("drawerCornerRadiusDp", s.drawerCornerRadiusDp.toDouble())
            put("drawerSearchBarVisible", s.drawerSearchBarVisible)
            put("drawerSearchBarPosition", s.drawerSearchBarPosition)
            put("drawerCardShadowDp", s.drawerCardShadowDp.toDouble())
            put("drawerCardBorderWidthDp", s.drawerCardBorderWidthDp.toDouble())
            put("drawerCardBorderColorHex", s.drawerCardBorderColorHex)
            put("niagaraWaveEnabled", s.niagaraWaveEnabled)
            put("niagaraIndexVisible", s.niagaraIndexVisible)
            put("niagaraEdge", s.niagaraEdge)
            put("niagaraWaveIntensity", s.niagaraWaveIntensity.toDouble())
            put("niagaraBaseFontSp", s.niagaraBaseFontSp.toDouble())
            put("niagaraSelectedFontSp", s.niagaraSelectedFontSp.toDouble())
            put("niagaraAccentColorHex", s.niagaraAccentColorHex)
            put("niagaraIndexWidthDp", s.niagaraIndexWidthDp.toDouble())
            put("niagaraIndexColorHex", s.niagaraIndexColorHex)
            put("niagaraHapticFeedback", s.niagaraHapticFeedback)
            put("gridSnapEnabled", s.gridSnapEnabled)
            put("alignmentGuidesEnabled", s.alignmentGuidesEnabled)
            put("alignmentGuideColorHex", s.alignmentGuideColorHex)
            put("gridSnapStepDp", s.gridSnapStepDp)
            put("editGridVisible", s.editGridVisible)
            put("editGridOpacity", s.editGridOpacity.toDouble())
            put("editGridColorHex", s.editGridColorHex)
            put("layoutLocked", s.layoutLocked)
            put("pageTransitionStyle", s.pageTransitionStyle)
            put("pageIndicatorStyle", s.pageIndicatorStyle)
            put("pageIndicatorColorHex", s.pageIndicatorColorHex)
            put("pageIndicatorSizeDp", s.pageIndicatorSizeDp.toDouble())
            put("pageIndicatorActiveWidthDp", s.pageIndicatorActiveWidthDp.toDouble())
            put("statusStripEnabled", s.statusStripEnabled)
            put("statusStripShowDate", s.statusStripShowDate)
            put("statusStripShowBattery", s.statusStripShowBattery)
            put("statusStripAccentColorHex", s.statusStripAccentColorHex)
            put("edgeHandlesVisible", s.edgeHandlesVisible)
            put("edgeHandleOpacity", s.edgeHandleOpacity.toDouble())
            put("edgeHandleWidthDp", s.edgeHandleWidthDp.toDouble())
            put("edgeHandleHeightDp", s.edgeHandleHeightDp.toDouble())
            put("animationSpeedScale", s.animationSpeedScale.toDouble())
            put("globalCornerRadiusDp", s.globalCornerRadiusDp.toDouble())
            put("globalIconShape", s.globalIconShape)
            put("showDockBar", s.showDockBar)
            put("showTagStripOnHome", s.showTagStripOnHome)
            put("showDefaultHomeBanner", s.showDefaultHomeBanner)
            put("showStatusBar", s.showStatusBar)
            put("showNavBar", s.showNavBar)
            put("hapticFeedbackEnabled", s.hapticFeedbackEnabled)
            put("iconPackOrderCsv", s.iconPackOrderCsv)
            put("blackIconStyleEnabled", s.blackIconStyleEnabled)
            put("blackIconMode", s.blackIconMode)
            put("blackIconFillColorHex", s.blackIconFillColorHex)
            put("blackIconOutlineColorHex", s.blackIconOutlineColorHex)
            put("blackIconOutlineWidthDp", s.blackIconOutlineWidthDp.toDouble())
            put("blackIconDarknessThreshold", s.blackIconDarknessThreshold.toDouble())
            put("lineIconProfilesJson", s.lineIconProfilesJson)
            put("lineIconsEnabled", s.lineIconsEnabled)
            put("lineIconDefaultColorHex", s.lineIconDefaultColorHex)
            put("lineIconDefaultLineWidthPx", s.lineIconDefaultLineWidthPx.toDouble())
            put("lineIconDefaultOutlineEnabled", s.lineIconDefaultOutlineEnabled)
            put("lineIconDefaultOutlineColorHex", s.lineIconDefaultOutlineColorHex)
            put("lineIconDefaultOutlineWidthPx", s.lineIconDefaultOutlineWidthPx.toDouble())
            put("lockedAppsCsv", s.lockedAppsCsv)
        })

        return root.toString(2)
    }

    fun decodeFullBackup(json: String): FullBackupPayload {
        val root = JSONObject(json)

        val apps = mutableListOf<AppEntity>()
        val appsArr = root.optJSONArray("apps") ?: JSONArray()
        for (i in 0 until appsArr.length()) {
            val o = appsArr.getJSONObject(i)
            apps.add(
                AppEntity(
                    packageName = o.getString("packageName"),
                    componentName = o.optString("componentName", ""),
                    appName = o.optString("appName", o.getString("packageName")),
                    customName = o.optNullableString("customName"),
                    customIconPath = o.optNullableString("customIconPath"),
                    iconScale = o.optDouble("iconScale", 1.0).toFloat(),
                    iconOffsetX = o.optDouble("iconOffsetX", 0.0).toFloat(),
                    iconOffsetY = o.optDouble("iconOffsetY", 0.0).toFloat(),
                    iconAlpha = o.optDouble("iconAlpha", 1.0).toFloat(),
                    primaryCategoryId = o.optLong("primaryCategoryId", 29L),
                    automaticCategoryId = o.optLong("automaticCategoryId", 29L),
                    manualCategoryOverride = o.optBoolean("manualCategoryOverride", false),
                    categoryConfidence = o.optString("categoryConfidence", "HEURISTIC"),
                    isGame = o.optBoolean("isGame", false),
                    hidden = o.optBoolean("hidden", false),
                    favorite = o.optBoolean("favorite", false),
                    manualOrder = o.optInt("manualOrder", 0),
                    isInstalled = true,
                )
            )
        }

        val categories = mutableListOf<CategoryEntity>()
        val catsArr = root.optJSONArray("categories") ?: JSONArray()
        for (i in 0 until catsArr.length()) {
            val o = catsArr.getJSONObject(i)
            categories.add(
                CategoryEntity(
                    categoryId = o.getLong("categoryId"),
                    name = o.getString("name"),
                    iconKey = o.optString("iconKey", "apps"),
                    customIconPath = o.optNullableString("customIconPath"),
                    customEmoji = o.optNullableString("customEmoji"),
                    colorHex = o.optLong("colorHex", 0xFF38BDF8L),
                    backgroundColorHex = o.optLong("backgroundColorHex", 0x2238BDF8L),
                    backgroundType = o.optString("backgroundType", "INHERIT"),
                    backgroundGradientEndHex = o.optNullableLong("backgroundGradientEndHex"),
                    backgroundImagePath = o.optNullableString("backgroundImagePath"),
                    backgroundBlurDp = o.optDouble("backgroundBlurDp", 0.0).toFloat(),
                    iconSizeDp = o.optInt("iconSizeDp", 24),
                    iconOpenStateEnabled = o.optBoolean("iconOpenStateEnabled", false),
                    openIconKey = o.optNullableString("openIconKey"),
                    openCustomIconPath = o.optNullableString("openCustomIconPath"),
                    openCustomEmoji = o.optNullableString("openCustomEmoji"),
                    openColorHex = o.optNullableLong("openColorHex"),
                    parentCategoryId = o.optNullableLong("parentCategoryId"),
                    sortOrder = o.optInt("sortOrder", i),
                    categoryType = o.optString("categoryType", "APP"),
                    isCustom = o.optBoolean("isCustom", false),
                )
            )
        }

        val appCategories = mutableListOf<AppCategoryCrossRef>()
        val appCatsArr = root.optJSONArray("appCategories") ?: JSONArray()
        for (i in 0 until appCatsArr.length()) {
            val o = appCatsArr.getJSONObject(i)
            appCategories.add(
                AppCategoryCrossRef(
                    packageName = o.getString("packageName"),
                    categoryId = o.getLong("categoryId"),
                    isManualAssignment = o.optBoolean("isManualAssignment", false),
                    isExcluded = o.optBoolean("isExcluded", false),
                )
            )
        }

        val tags = mutableListOf<TagEntity>()
        val tagsArr = root.optJSONArray("tags") ?: JSONArray()
        for (i in 0 until tagsArr.length()) {
            val o = tagsArr.getJSONObject(i)
            tags.add(
                TagEntity(
                    tagId = o.getLong("tagId"),
                    name = o.getString("name"),
                    description = o.optString("description", ""),
                    colorHex = o.optLong("colorHex", 0xFF38BDF8L),
                    backgroundColorHex = o.optLong("backgroundColorHex", 0xEA0C1220L),
                    backgroundImagePath = o.optNullableString("backgroundImagePath"),
                    iconKey = o.optString("iconKey", "tag"),
                    customImagePath = o.optNullableString("customImagePath"),
                    sortOrder = o.optInt("sortOrder", i),
                    parentTagId = o.optNullableLong("parentTagId"),
                    isVisible = o.optBoolean("isVisible", true),
                    sidebarLayoutMode = o.optString("sidebarLayoutMode", "GRID"),
                    sidebarIconSizeDp = o.optInt("sidebarIconSizeDp", 52),
                    sidebarColumns = o.optInt("sidebarColumns", 3),
                    showCountInSidebar = o.optBoolean("showCountInSidebar", true),
                )
            )
        }

        val refs = mutableListOf<AppTagCrossRef>()
        val refsArr = root.optJSONArray("appTags") ?: JSONArray()
        for (i in 0 until refsArr.length()) {
            val o = refsArr.getJSONObject(i)
            refs.add(AppTagCrossRef(o.getString("packageName"), o.getLong("tagId")))
        }

        val rules = mutableListOf<SavedTagRuleEntity>()
        val rulesArr = root.optJSONArray("rules") ?: JSONArray()
        for (i in 0 until rulesArr.length()) {
            val o = rulesArr.getJSONObject(i)
            rules.add(
                SavedTagRuleEntity(
                    ruleId = o.getLong("ruleId"),
                    name = o.getString("name"),
                    colorHex = o.optLong("colorHex", 0xFF6366F1L),
                    iconKey = o.optString("iconKey", "filter"),
                    matchMode = o.optString("matchMode", "AND"),
                    includeTagIdsCsv = o.optString("includeTagIdsCsv", ""),
                    excludeTagIdsCsv = o.optString("excludeTagIdsCsv", ""),
                    includeNestedTags = o.optBoolean("includeNestedTags", false),
                    sortOrder = o.optInt("sortOrder", i),
                )
            )
        }

        val pages = mutableListOf<HomePageEntity>()
        val pagesArr = root.optJSONArray("pages") ?: JSONArray()
        for (i in 0 until pagesArr.length()) {
            pages.add(decodePage(pagesArr.getJSONObject(i), i))
        }

        val windows = mutableListOf<LauncherWindowEntity>()
        val windowsArr = root.optJSONArray("windows") ?: JSONArray()
        for (i in 0 until windowsArr.length()) {
            val o = windowsArr.getJSONObject(i)
            windows.add(
                LauncherWindowEntity(
                    windowId = o.getLong("windowId"),
                    title = o.optString("title", "Window"),
                    fullScreen = o.optBoolean("fullScreen", false),
                    widthFraction = o.optDouble("widthFraction", 0.88).toFloat(),
                    heightFraction = o.optDouble("heightFraction", 0.68).toFloat(),
                    alignment = o.optString("alignment", "CENTER"),
                    backgroundColorHex = o.optLong("backgroundColorHex", 0xEE0D1526L),
                    backgroundImagePath = o.optNullableString("backgroundImagePath"),
                    blurBehindDp = o.optDouble("blurBehindDp", 8.0).toFloat(),
                    cornerRadiusDp = o.optDouble("cornerRadiusDp", 28.0).toFloat(),
                    animationStyle = o.optString("animationStyle", "SCALE"),
                )
            )
        }

        val elements = mutableListOf<HomeElementEntity>()
        val elemsArr = root.optJSONArray("elements") ?: JSONArray()
        for (i in 0 until elemsArr.length()) {
            elements.add(decodeElement(elemsArr.getJSONObject(i)))
        }

        val sidebars = mutableListOf<SidebarConfigEntity>()
        val sidebarsArr = root.optJSONArray("sidebars") ?: JSONArray()
        for (i in 0 until sidebarsArr.length()) {
            val o = sidebarsArr.getJSONObject(i)
            sidebars.add(
                SidebarConfigEntity(
                    sidebarId = o.getLong("sidebarId"),
                    title = o.getString("title"),
                    side = o.optString("side", "LEFT"),
                    sourceType = o.optString("sourceType", "FAVORITES"),
                    targetTagId = o.optNullableLong("targetTagId"),
                    targetCategoryId = o.optNullableLong("targetCategoryId"),
                    targetRuleId = o.optNullableLong("targetRuleId"),
                    customPackagesCsv = o.optString("customPackagesCsv", ""),
                    widthFraction = o.optDouble("widthFraction", 0.80).toFloat(),
                    heightFraction = o.optDouble("heightFraction", 0.72).toFloat(),
                    backgroundColorHex = o.optLong("backgroundColorHex", 0xEA0C1220L),
                    backgroundGradientEndHex = o.optNullableLong("backgroundGradientEndHex"),
                    backgroundImagePath = o.optNullableString("backgroundImagePath"),
                    backgroundBlurDp = o.optDouble("backgroundBlurDp", 0.0).toFloat(),
                    cornerRadiusDp = o.optDouble("cornerRadiusDp", 28.0).toFloat(),
                    layoutMode = o.optString("layoutMode", "GRID"),
                    iconSizeDp = o.optInt("iconSizeDp", 52),
                    columns = o.optInt("columns", 3),
                    animationStyle = o.optString("animationStyle", "SLIDE"),
                    edgeZoneEnabled = o.optBoolean("edgeZoneEnabled", true),
                    showHandle = o.optBoolean("showHandle", true),
                    pinned = o.optBoolean("pinned", false),
                    sortOrder = o.optInt("sortOrder", i),
                )
            )
        }

        val gestures = mutableListOf<GestureBindingEntity>()
        val gesturesArr = root.optJSONArray("gestures") ?: JSONArray()
        for (i in 0 until gesturesArr.length()) {
            val o = gesturesArr.getJSONObject(i)
            gestures.add(
                GestureBindingEntity(
                    gestureType = o.getString("gestureType"),
                    actionType = o.getString("actionType"),
                    targetPackageName = o.optNullableString("targetPackageName"),
                    targetTagId = o.optNullableLong("targetTagId"),
                    targetCategoryId = o.optNullableLong("targetCategoryId"),
                    targetSidebarId = o.optNullableLong("targetSidebarId"),
                    targetWindowId = o.optNullableLong("targetWindowId"),
                    targetRuleId = o.optNullableLong("targetRuleId"),
                    targetUrl = o.optNullableString("targetUrl"),
                    macroStepsJson = o.optNullableString("macroStepsJson"),
                )
            )
        }

        val images = mutableListOf<ImageResourceEntity>()
        val embeddedMap = mutableMapOf<String, String>()
        val imgsArr = root.optJSONArray("imageResources") ?: JSONArray()
        for (i in 0 until imgsArr.length()) {
            val o = imgsArr.getJSONObject(i)
            val lp = o.getString("localPath")
            images.add(
                ImageResourceEntity(
                    imageId = o.optLong("imageId", 0),
                    name = o.optString("name", "Image"),
                    folderName = o.optString("folderName", "Imported"),
                    localPath = lp,
                    sourceUri = o.optString("sourceUri", ""),
                    fileFormat = o.optString("fileFormat", "PNG"),
                    widthPx = o.optInt("widthPx", 0),
                    heightPx = o.optInt("heightPx", 0),
                )
            )
            o.optNullableString("base64")?.let { embeddedMap[lp] = it }
        }

        val presets = mutableListOf<BackgroundPresetEntity>()
        val presetsArr = root.optJSONArray("backgroundPresets") ?: JSONArray()
        for (i in 0 until presetsArr.length()) {
            val o = presetsArr.getJSONObject(i)
            presets.add(
                BackgroundPresetEntity(
                    presetId = o.optLong("presetId", 0),
                    name = o.optString("name", "Preset"),
                    ownerScope = o.optString("ownerScope", "GLOBAL"),
                    backgroundType = o.optString("backgroundType", "GRADIENT"),
                    colorHex = o.optLong("colorHex", 0xFF0A1124L),
                    gradientEndHex = o.optLong("gradientEndHex", 0xFF1A1038L),
                    imagePath = o.optNullableString("imagePath"),
                    blurDp = o.optDouble("blurDp", 0.0).toFloat(),
                    alpha = o.optDouble("alpha", 1.0).toFloat(),
                )
            )
        }

        val customFonts = mutableListOf<CustomFontEntity>()
        val embeddedFonts = mutableMapOf<String, String>()
        val fontsArr = root.optJSONArray("customFonts") ?: JSONArray()
        for (i in 0 until fontsArr.length()) {
            val o = fontsArr.getJSONObject(i)
            customFonts.add(CustomFontEntity(
                fontId = o.optLong("fontId", 0),
                displayName = o.optString("displayName", "Custom font"),
                localPath = o.optString("localPath", ""),
                familyName = o.optString("familyName", ""),
                weight = o.optInt("weight", 400),
                isItalic = o.optBoolean("isItalic", false),
                isFavorite = o.optBoolean("isFavorite", false),
                createdAt = o.optLong("createdAt", System.currentTimeMillis()),
            ))
            o.optNullableString("base64")?.let { embeddedFonts[o.optString("localPath", "")] = it }
        }

        
        val themeProfiles = mutableListOf<ThemeProfileEntity>()
        val themesArr = root.optJSONArray("themeProfiles") ?: JSONArray()
        for (i in 0 until themesArr.length()) {
            val o = themesArr.getJSONObject(i)
            themeProfiles.add(ThemeProfileEntity(
                profileId = o.optLong("profileId", 0),
                name = o.optString("name", "Theme"),
                description = o.optString("description", ""),
                settingsJson = o.optString("settingsJson", "{}"),
                createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                isBuiltIn = o.optBoolean("isBuiltIn", false),
            ))
        }

        val so = root.optJSONObject("settings") ?: JSONObject()
        val settings = LauncherSettings(
            themeMode = so.optString("themeMode", "DARK"),
            accentColorHex = so.optLong("accentColorHex", 0xFF38BDF8L),
            secondaryColorHex = so.optLong("secondaryColorHex", 0xFF6366F1L),
            tertiaryColorHex = so.optLong("tertiaryColorHex", 0xFFFB7185L),
            fontStyle = so.optString("fontStyle", "DEFAULT"),
            fontScale = so.optDouble("fontScale", 1.0).toFloat(),
            highReadabilityMode = so.optBoolean("highReadabilityMode", false),
            surfaceOpacity = so.optDouble("surfaceOpacity", 0.86).toFloat(),
            globalBorderWidthDp = so.optDouble("globalBorderWidthDp", 1.0).toFloat(),
            globalTextColorHex = so.optLong("globalTextColorHex", 0xFFF8FAFC),
            showHomeAppLabels = so.optBoolean("showHomeAppLabels", true),
            wallpaperType = so.optString("wallpaperType", "BUILTIN_PRISM"),
            wallpaperCustomPath = so.optNullableString("wallpaperCustomPath"),
            wallpaperGradientStartHex = so.optLong("wallpaperGradientStartHex", 0xFF0A1124L),
            wallpaperGradientEndHex = so.optLong("wallpaperGradientEndHex", 0xFF1A1038L),
            wallpaperGradientDirection = so.optString("wallpaperGradientDirection", "VERTICAL"),
            wallpaperSolidHex = so.optLong("wallpaperSolidHex", 0xFF090D16L),
            wallpaperBlurDp = so.optDouble("wallpaperBlurDp", 0.0).toFloat(),
            wallpaperDimAlpha = so.optDouble("wallpaperDimAlpha", 0.25).toFloat(),
            wallpaperDimColorHex = so.optLong("wallpaperDimColorHex", 0xFF000000L),
            wallpaperVignette = so.optDouble("wallpaperVignette", 0.0).toFloat(),
            wallpaperSaturation = so.optDouble("wallpaperSaturation", 1.0).toFloat(),
            wallpaperParallaxIntensity = so.optDouble("wallpaperParallaxIntensity", 0.0).toFloat(),
            wallpaperTintHex = so.optNullableLong("wallpaperTintHex"),
            wallpaperAmbientMotion = so.optBoolean("wallpaperAmbientMotion", false),
            allAppsSidebarEdge = so.optString("allAppsSidebarEdge", "RIGHT"),
            allAppsSidebarWidthFraction = so.optDouble("allAppsSidebarWidthFraction", 0.90).toFloat(),
            allAppsBgType = so.optString("allAppsBgType", "SOLID"),
            allAppsBgColorHex = so.optLong("allAppsBgColorHex", 0xF2090D16L),
            allAppsBgGradientEndHex = so.optLong("allAppsBgGradientEndHex", 0xF2151E34L),
            allAppsBgImagePath = so.optNullableString("allAppsBgImagePath"),
            allAppsBgBlurDp = so.optDouble("allAppsBgBlurDp", 0.0).toFloat(),
            drawerLayoutMode = so.optString("drawerLayoutMode", "GRID"),
            drawerColumns = so.optInt("drawerColumns", 4),
            drawerIconSizeDp = so.optInt("drawerIconSizeDp", 50),
            drawerLabelSizeSp = so.optInt("drawerLabelSizeSp", 12),
            drawerSortOrder = so.optString("drawerSortOrder", "ALPHABETICAL"),
            drawerCardOpacity = so.optDouble("drawerCardOpacity", 0.88).toFloat(),
            drawerCornerRadiusDp = so.optDouble("drawerCornerRadiusDp", 26.0).toFloat(),
            drawerSearchBarVisible = so.optBoolean("drawerSearchBarVisible", true),
            drawerSearchBarPosition = so.optString("drawerSearchBarPosition", "TOP"),
            drawerCardShadowDp = so.optDouble("drawerCardShadowDp", 4.0).toFloat(),
            drawerCardBorderWidthDp = so.optDouble("drawerCardBorderWidthDp", 0.0).toFloat(),
            drawerCardBorderColorHex = so.optLong("drawerCardBorderColorHex", 0x55FFFFFFL),
            niagaraWaveEnabled = so.optBoolean("niagaraWaveEnabled", true),
            niagaraIndexVisible = so.optBoolean("niagaraIndexVisible", true),
            niagaraEdge = so.optString("niagaraEdge", "RIGHT"),
            niagaraWaveIntensity = so.optDouble("niagaraWaveIntensity", 1.0).toFloat(),
            niagaraBaseFontSp = so.optDouble("niagaraBaseFontSp", 11.0).toFloat(),
            niagaraSelectedFontSp = so.optDouble("niagaraSelectedFontSp", 24.0).toFloat(),
            niagaraAccentColorHex = so.optLong("niagaraAccentColorHex", 0xFF38BDF8L),
            niagaraIndexWidthDp = so.optDouble("niagaraIndexWidthDp", 28.0).toFloat(),
            niagaraIndexColorHex = so.optLong("niagaraIndexColorHex", 0xFF38BDF8L),
            niagaraHapticFeedback = so.optBoolean("niagaraHapticFeedback", true),
            gridSnapEnabled = so.optBoolean("gridSnapEnabled", false),
            alignmentGuidesEnabled = so.optBoolean("alignmentGuidesEnabled", true),
            alignmentGuideColorHex = so.optLong("alignmentGuideColorHex", 0xFF38BDF8L),
            gridSnapStepDp = so.optInt("gridSnapStepDp", 16),
            editGridVisible = so.optBoolean("editGridVisible", true),
            editGridOpacity = so.optDouble("editGridOpacity", 0.08).toFloat(),
            editGridColorHex = so.optLong("editGridColorHex", 0xFFFFFFFFL),
            layoutLocked = so.optBoolean("layoutLocked", false),
            pageTransitionStyle = so.optString("pageTransitionStyle", "SLIDE"),
            pageIndicatorStyle = so.optString("pageIndicatorStyle", "DOTS"),
            pageIndicatorColorHex = so.optLong("pageIndicatorColorHex", 0xFF38BDF8L),
            pageIndicatorSizeDp = so.optDouble("pageIndicatorSizeDp", 7.0).toFloat(),
            pageIndicatorActiveWidthDp = so.optDouble("pageIndicatorActiveWidthDp", 22.0).toFloat(),
            statusStripEnabled = so.optBoolean("statusStripEnabled", false),
            statusStripShowDate = so.optBoolean("statusStripShowDate", true),
            statusStripShowBattery = so.optBoolean("statusStripShowBattery", true),
            statusStripAccentColorHex = so.optLong("statusStripAccentColorHex", 0xFF38BDF8L),
            edgeHandlesVisible = so.optBoolean("edgeHandlesVisible", true),
            edgeHandleOpacity = so.optDouble("edgeHandleOpacity", 0.45).toFloat(),
            edgeHandleWidthDp = so.optDouble("edgeHandleWidthDp", 4.0).toFloat(),
            edgeHandleHeightDp = so.optDouble("edgeHandleHeightDp", 64.0).toFloat(),
            animationSpeedScale = so.optDouble("animationSpeedScale", 1.0).toFloat(),
            globalCornerRadiusDp = so.optDouble("globalCornerRadiusDp", 20.0).toFloat(),
            globalIconShape = so.optString("globalIconShape", "SQUIRCLE"),
            showDockBar = so.optBoolean("showDockBar", true),
            showTagStripOnHome = so.optBoolean("showTagStripOnHome", true),
            showDefaultHomeBanner = so.optBoolean("showDefaultHomeBanner", true),
            showStatusBar = so.optBoolean("showStatusBar", true),
            showNavBar = so.optBoolean("showNavBar", true),
            hapticFeedbackEnabled = so.optBoolean("hapticFeedbackEnabled", true),
            iconPackOrderCsv = so.optString("iconPackOrderCsv", ""),
            blackIconStyleEnabled = so.optBoolean("blackIconStyleEnabled", true),
            blackIconMode = so.optString("blackIconMode", IconStyleConfig.MODE_BLACK_ICONS),
            blackIconFillColorHex = so.optLong("blackIconFillColorHex", IconStyleConfig.DEFAULT_FILL_COLOR_HEX),
            blackIconOutlineColorHex = so.optLong("blackIconOutlineColorHex", IconStyleConfig.DEFAULT_OUTLINE_COLOR_HEX),
            blackIconOutlineWidthDp = so.optDouble("blackIconOutlineWidthDp", IconStyleConfig.DEFAULT_OUTLINE_WIDTH_DP.toDouble()).toFloat(),
            blackIconDarknessThreshold = so.optDouble("blackIconDarknessThreshold", IconStyleConfig.DEFAULT_DARKNESS_THRESHOLD.toDouble()).toFloat(),
            lineIconProfilesJson = so.optString("lineIconProfilesJson", ""),
            lineIconsEnabled = so.optBoolean("lineIconsEnabled", false),
            lineIconDefaultColorHex = so.optLong("lineIconDefaultColorHex", 0xFFFFFFFFL),
            lineIconDefaultLineWidthPx = so.optDouble("lineIconDefaultLineWidthPx", 1.8).toFloat(),
            lineIconDefaultOutlineEnabled = so.optBoolean("lineIconDefaultOutlineEnabled", false),
            lineIconDefaultOutlineColorHex = so.optLong("lineIconDefaultOutlineColorHex", 0xFF000000L),
            lineIconDefaultOutlineWidthPx = so.optDouble("lineIconDefaultOutlineWidthPx", 1.2).toFloat(),
            lockedAppsCsv = so.optString("lockedAppsCsv", ""),
            seededDefaults = true,
        )

        return FullBackupPayload(
            apps = apps,
            categories = categories,
            appCategories = appCategories,
            tags = tags,
            crossRefs = refs,
            rules = rules,
            pages = pages,
            windows = windows,
            elements = elements,
            sidebars = sidebars,
            gestures = gestures,
            imageResources = images,
            embeddedImagesBase64 = embeddedMap,
            backgroundPresets = presets,
            customFonts = customFonts,
            themeProfiles = themeProfiles,
            embeddedFontsBase64 = embeddedFonts,
            settings = settings,
        )
    }

    fun encodeLayoutSnapshot(pages: List<HomePageEntity>, elements: List<HomeElementEntity>): String {
        val root = JSONObject()
        val pArr = JSONArray()
        pages.forEach { pArr.put(encodePage(it)) }
        val eArr = JSONArray()
        elements.forEach { eArr.put(encodeElement(it)) }
        root.put("pages", pArr)
        root.put("elements", eArr)
        return root.toString()
    }

    fun decodeLayoutSnapshot(json: String): Pair<List<HomePageEntity>, List<HomeElementEntity>> {
        val root = JSONObject(json)
        val pages = mutableListOf<HomePageEntity>()
        val pArr = root.optJSONArray("pages") ?: JSONArray()
        for (i in 0 until pArr.length()) {
            pages.add(decodePage(pArr.getJSONObject(i), i))
        }
        val elements = mutableListOf<HomeElementEntity>()
        val eArr = root.optJSONArray("elements") ?: JSONArray()
        for (i in 0 until eArr.length()) {
            elements.add(decodeElement(eArr.getJSONObject(i)))
        }
        return pages to elements
    }

    private fun encodePage(p: HomePageEntity): JSONObject = JSONObject().apply {
        put("pageId", p.pageId)
        put("title", p.title)
        put("pageOrder", p.pageOrder)
        put("isDefaultHome", p.isDefaultHome)
        put("isHidden", p.isHidden)
        put("isLocked", p.isLocked)
        put("verticallyScrollable", p.verticallyScrollable)
        put("paddingDp", p.paddingDp.toDouble())
        put("backgroundType", p.backgroundType)
        put("backgroundColorHex", p.backgroundColorHex ?: JSONObject.NULL)
        put("backgroundGradientEndHex", p.backgroundGradientEndHex ?: JSONObject.NULL)
        put("backgroundImagePath", p.backgroundImagePath ?: JSONObject.NULL)
        put("landscapeBackgroundImagePath", p.landscapeBackgroundImagePath ?: JSONObject.NULL)
        put("backgroundBlurDp", p.backgroundBlurDp.toDouble())
        put("backgroundAlpha", p.backgroundAlpha.toDouble())
        put("pageTapActionJson", p.pageTapActionJson ?: JSONObject.NULL)
        put("pageGesturesJson", p.pageGesturesJson ?: JSONObject.NULL)
        put("transitionOverride", p.transitionOverride ?: JSONObject.NULL)
        put("transitionDurationMs", p.transitionDurationMs)
        put("loopNavigation", p.loopNavigation)
        put("parallaxEnabled", p.parallaxEnabled)
        put("showDockOverride", p.showDockOverride ?: JSONObject.NULL)
    }

    private fun decodePage(o: JSONObject, idx: Int): HomePageEntity = HomePageEntity(
        pageId = o.getLong("pageId"),
        title = o.optString("title", "Page ${idx + 1}"),
        pageOrder = o.optInt("pageOrder", idx),
        isDefaultHome = o.optBoolean("isDefaultHome", idx == 0),
        isHidden = o.optBoolean("isHidden", false),
        isLocked = o.optBoolean("isLocked", false),
        verticallyScrollable = o.optBoolean("verticallyScrollable", false),
        paddingDp = o.optDouble("paddingDp", 0.0).toFloat(),
        backgroundType = o.optString("backgroundType", "INHERIT"),
        backgroundColorHex = o.optNullableLong("backgroundColorHex"),
        backgroundGradientEndHex = o.optNullableLong("backgroundGradientEndHex"),
        backgroundImagePath = o.optNullableString("backgroundImagePath"),
        landscapeBackgroundImagePath = o.optNullableString("landscapeBackgroundImagePath"),
        backgroundBlurDp = o.optDouble("backgroundBlurDp", 0.0).toFloat(),
        backgroundAlpha = o.optDouble("backgroundAlpha", 0.85).toFloat(),
        pageTapActionJson = o.optNullableString("pageTapActionJson"),
        pageGesturesJson = o.optNullableString("pageGesturesJson"),
        transitionOverride = o.optNullableString("transitionOverride"),
        transitionDurationMs = o.optInt("transitionDurationMs", 320),
        loopNavigation = o.optBoolean("loopNavigation", false),
        parallaxEnabled = o.optBoolean("parallaxEnabled", false),
        showDockOverride = if (!o.has("showDockOverride") || o.isNull("showDockOverride")) null else o.optBoolean("showDockOverride"),
    )

    private fun encodeElement(e: HomeElementEntity): JSONObject = JSONObject().apply {
        put("elementId", e.elementId)
        put("pageId", e.pageId)
        put("ownerType", e.ownerType)
        put("ownerWindowId", e.ownerWindowId ?: JSONObject.NULL)
        put("type", e.type)
        put("xFraction", e.xFraction.toDouble())
        put("yFraction", e.yFraction.toDouble())
        put("landscapeXFraction", e.landscapeXFraction?.toDouble() ?: JSONObject.NULL)
        put("landscapeYFraction", e.landscapeYFraction?.toDouble() ?: JSONObject.NULL)
        put("widthDp", e.widthDp.toDouble())
        put("heightDp", e.heightDp.toDouble())
        put("rotationDeg", e.rotationDeg.toDouble())
        put("scaleX", e.scaleX.toDouble())
        put("scaleY", e.scaleY.toDouble())
        put("skewX", e.skewX.toDouble())
        put("skewY", e.skewY.toDouble())
        put("flipHorizontal", e.flipHorizontal)
        put("flipVertical", e.flipVertical)
        put("zIndex", e.zIndex)
        put("alpha", e.alpha.toDouble())
        put("locked", e.locked)
        put("visible", e.visible)
        put("groupId", e.groupId ?: JSONObject.NULL)
        put("parentContainerId", e.parentContainerId ?: JSONObject.NULL)
        put("label", e.label)
        put("targetPackageName", e.targetPackageName ?: JSONObject.NULL)
        put("targetTagId", e.targetTagId ?: JSONObject.NULL)
        put("targetCategoryId", e.targetCategoryId ?: JSONObject.NULL)
        put("targetRuleId", e.targetRuleId ?: JSONObject.NULL)
        put("targetSidebarId", e.targetSidebarId ?: JSONObject.NULL)
        put("targetWindowId", e.targetWindowId ?: JSONObject.NULL)
        put("folderAppPackagesCsv", e.folderAppPackagesCsv)
        put("nestedFolderIdsCsv", e.nestedFolderIdsCsv)
        put("folderLayoutMode", e.folderLayoutMode)
        put("appWidgetId", e.appWidgetId ?: JSONObject.NULL)
        put("imagePath", e.imagePath ?: JSONObject.NULL)
        put("imageScaleType", e.imageScaleType)
        put("imageMaskShape", e.imageMaskShape)
        put("imageBrightness", e.imageBrightness.toDouble())
        put("imageContrast", e.imageContrast.toDouble())
        put("imageSaturation", e.imageSaturation.toDouble())
        put("imageHueDeg", e.imageHueDeg.toDouble())
        put("imageGrayscale", e.imageGrayscale)
        put("imageBlurDp", e.imageBlurDp.toDouble())
        put("imageTintHex", e.imageTintHex ?: JSONObject.NULL)
        put("extraImageLayersJson", e.extraImageLayersJson)
        put("actionsConfigJson", e.actionsConfigJson)
        put("backgroundColorHex", e.backgroundColorHex)
        put("backgroundGradientEndHex", e.backgroundGradientEndHex ?: JSONObject.NULL)
        put("textColorHex", e.textColorHex)
        put("borderColorHex", e.borderColorHex)
        put("borderWidthDp", e.borderWidthDp.toDouble())
        put("cornerRadiusDp", e.cornerRadiusDp.toDouble())
        put("paddingDp", e.paddingDp.toDouble())
        put("shadowElevationDp", e.shadowElevationDp.toDouble())
        put("fontSizeSp", e.fontSizeSp.toDouble())
        put("showLabel", e.showLabel)
        put("iconSizeDp", e.iconSizeDp.toDouble())
        put("styleExtrasJson", e.styleExtrasJson)
        put("rotationXDeg", e.rotationXDeg.toDouble())
        put("rotationYDeg", e.rotationYDeg.toDouble())
        put("perspective", e.perspective.toDouble())
        put("widgetStackItemsJson", e.widgetStackItemsJson)
        put("widgetStackIndex", e.widgetStackIndex)
        put("widgetStackSwipeEnabled", e.widgetStackSwipeEnabled)
        put("pinnedToViewport", e.pinnedToViewport)
        put("pinnedToAllPages", e.pinnedToAllPages)
        put("visibilityGroupId", e.visibilityGroupId ?: JSONObject.NULL)
        put("landscapeWidthDp", e.landscapeWidthDp?.toDouble() ?: JSONObject.NULL)
        put("landscapeHeightDp", e.landscapeHeightDp?.toDouble() ?: JSONObject.NULL)
        put("pinEdge", e.pinEdge)
        put("pinOffsetDp", e.pinOffsetDp.toDouble())
    }

    private fun decodeElement(o: JSONObject): HomeElementEntity = HomeElementEntity(
        elementId = o.optLong("elementId", 0),
        pageId = o.getLong("pageId"),
        ownerType = o.optString("ownerType", "PAGE"),
        ownerWindowId = o.optNullableLong("ownerWindowId"),
        type = o.getString("type"),
        xFraction = o.optDouble("xFraction", 0.1).toFloat(),
        yFraction = o.optDouble("yFraction", 0.1).toFloat(),
        landscapeXFraction = if (o.isNull("landscapeXFraction") || !o.has("landscapeXFraction")) null else o.optDouble("landscapeXFraction").toFloat(),
        landscapeYFraction = if (o.isNull("landscapeYFraction") || !o.has("landscapeYFraction")) null else o.optDouble("landscapeYFraction").toFloat(),
        widthDp = o.optDouble("widthDp", 88.0).toFloat(),
        heightDp = o.optDouble("heightDp", 96.0).toFloat(),
        rotationDeg = o.optDouble("rotationDeg", 0.0).toFloat(),
        scaleX = o.optDouble("scaleX", 1.0).toFloat(),
        scaleY = o.optDouble("scaleY", 1.0).toFloat(),
        skewX = o.optDouble("skewX", 0.0).toFloat(),
        skewY = o.optDouble("skewY", 0.0).toFloat(),
        flipHorizontal = o.optBoolean("flipHorizontal", false),
        flipVertical = o.optBoolean("flipVertical", false),
        zIndex = o.optInt("zIndex", 0),
        alpha = o.optDouble("alpha", 1.0).toFloat(),
        locked = o.optBoolean("locked", false),
        visible = o.optBoolean("visible", true),
        groupId = o.optNullableString("groupId"),
        parentContainerId = o.optNullableLong("parentContainerId"),
        label = o.optString("label", ""),
        targetPackageName = o.optNullableString("targetPackageName"),
        targetTagId = o.optNullableLong("targetTagId"),
        targetCategoryId = o.optNullableLong("targetCategoryId"),
        targetRuleId = o.optNullableLong("targetRuleId"),
        targetSidebarId = o.optNullableLong("targetSidebarId"),
        targetWindowId = o.optNullableLong("targetWindowId"),
        folderAppPackagesCsv = o.optString("folderAppPackagesCsv", ""),
        nestedFolderIdsCsv = o.optString("nestedFolderIdsCsv", ""),
        folderLayoutMode = o.optString("folderLayoutMode", "GRID"),
        appWidgetId = if (o.isNull("appWidgetId")) null else o.optInt("appWidgetId"),
        imagePath = o.optNullableString("imagePath"),
        imageScaleType = o.optString("imageScaleType", "CROP"),
        imageMaskShape = o.optString("imageMaskShape", "NONE"),
        imageBrightness = o.optDouble("imageBrightness", 0.0).toFloat(),
        imageContrast = o.optDouble("imageContrast", 1.0).toFloat(),
        imageSaturation = o.optDouble("imageSaturation", 1.0).toFloat(),
        imageHueDeg = o.optDouble("imageHueDeg", 0.0).toFloat(),
        imageGrayscale = o.optBoolean("imageGrayscale", false),
        imageBlurDp = o.optDouble("imageBlurDp", 0.0).toFloat(),
        imageTintHex = o.optNullableLong("imageTintHex"),
        extraImageLayersJson = o.optString("extraImageLayersJson", ""),
        actionsConfigJson = o.optString("actionsConfigJson", ""),
        backgroundColorHex = o.optLong("backgroundColorHex", 0x33161D2FL),
        backgroundGradientEndHex = o.optNullableLong("backgroundGradientEndHex"),
        textColorHex = o.optLong("textColorHex", 0xFFF8FAFC),
        borderColorHex = o.optLong("borderColorHex", 0x38FFFFFF),
        borderWidthDp = o.optDouble("borderWidthDp", 1.0).toFloat(),
        cornerRadiusDp = o.optDouble("cornerRadiusDp", 20.0).toFloat(),
        paddingDp = o.optDouble("paddingDp", 6.0).toFloat(),
        shadowElevationDp = o.optDouble("shadowElevationDp", 4.0).toFloat(),
        fontSizeSp = o.optDouble("fontSizeSp", 13.0).toFloat(),
        showLabel = o.optBoolean("showLabel", true),
        iconSizeDp = o.optDouble("iconSizeDp", 48.0).toFloat(),
        styleExtrasJson = o.optString("styleExtrasJson", ""),
        rotationXDeg = o.optDouble("rotationXDeg", 0.0).toFloat(),
        rotationYDeg = o.optDouble("rotationYDeg", 0.0).toFloat(),
        perspective = o.optDouble("perspective", 0.0).toFloat(),
        widgetStackItemsJson = o.optString("widgetStackItemsJson", ""),
        widgetStackIndex = o.optInt("widgetStackIndex", 0),
        widgetStackSwipeEnabled = o.optBoolean("widgetStackSwipeEnabled", true),
        pinnedToViewport = o.optBoolean("pinnedToViewport", false),
        pinnedToAllPages = o.optBoolean("pinnedToAllPages", false),
        visibilityGroupId = o.optNullableString("visibilityGroupId"),
        landscapeWidthDp = if (!o.has("landscapeWidthDp") || o.isNull("landscapeWidthDp")) null else o.optDouble("landscapeWidthDp").toFloat(),
        landscapeHeightDp = if (!o.has("landscapeHeightDp") || o.isNull("landscapeHeightDp")) null else o.optDouble("landscapeHeightDp").toFloat(),
        pinEdge = o.optString("pinEdge", "NONE"),
        pinOffsetDp = o.optDouble("pinOffsetDp", 0.0).toFloat(),
    )

    private fun JSONObject.optNullableString(key: String): String? =
        if (isNull(key) || !has(key)) null else optString(key).takeIf { it.isNotEmpty() }

    private fun JSONObject.optNullableLong(key: String): Long? =
        if (isNull(key) || !has(key)) null else optLong(key)
}
