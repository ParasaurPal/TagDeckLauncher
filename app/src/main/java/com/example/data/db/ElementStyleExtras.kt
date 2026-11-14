package com.example.data.db

import org.json.JSONObject

data class ElementStyleExtras(
    // Border & Per-Corner Radii
    val borderStyle: String = "SOLID", // SOLID, DASHED, DOTTED, DOUBLE, GRADIENT
    val borderGradientEndHex: Long? = null,
    val useCustomCorners: Boolean = false,
    val cornerTopStartDp: Float = 20f,
    val cornerTopEndDp: Float = 20f,
    val cornerBottomEndDp: Float = 20f,
    val cornerBottomStartDp: Float = 20f,
    val gradientDirection: String = "VERTICAL", // VERTICAL, HORIZONTAL, DIAGONAL, RADIAL

    // Colored Glow / Offset Drop Shadow
    val shadowColorHex: Long? = null,
    val shadowBlurDp: Float = 0f,
    val shadowOffsetXDp: Float = 0f,
    val shadowOffsetYDp: Float = 4f,

    // 3D Perspective
    val rotationXDeg: Float = 0f,
    val rotationYDeg: Float = 0f,
    val cameraDistance: Float = 12f,

    // Pressed State & Animations
    val pressedScale: Float = 0.94f,
    val pressedBgColorHex: Long? = null,
    val pressedBorderColorHex: Long? = null,
    val pressedTextColorHex: Long? = null,
    val pressedImagePath: String? = null,
    val entranceAnim: String = "NONE", // NONE, FADE, SCALE, SLIDE_UP, BOUNCE
    val tapAnim: String = "SCALE_DOWN", // NONE, SCALE_DOWN, BOUNCE, PULSE, SPIN
    val idleAnim: String = "NONE", // NONE, FLOAT, PULSE, BREATHE, SPIN_SLOW

    // Typography & Readability Overrides
    val fontRef: String = "INHERIT", // INHERIT, DEFAULT, SPACE_GROTESK, PLUS_JAKARTA, JETBRAINS_MONO, SYSTEM, CUSTOM:<fontId>
    val fontWeight: String = "INHERIT", // INHERIT, LIGHT, NORMAL, MEDIUM, SEMIBOLD, BOLD, EXTRABOLD
    val italic: Boolean = false,
    val textTransform: String = "NONE", // NONE, UPPERCASE, LOWERCASE
    val letterSpacingEm: Float = 0f,
    val lineHeightMultiplier: Float = 1.15f,
    val textAlign: String = "CENTER", // START, CENTER, END
    val textShadowOverride: Boolean? = null,
    val textOutlineOverride: Boolean? = null,
    val textBackdropOverride: Boolean? = null,
    val textBackdropColorHex: Long = 0xAA000000L,
    val textBackdropAlpha: Float = 0.7f,
    val marqueeEnabled: Boolean = false,
    val maxLines: Int = 2,

    // Icon Placement & Custom Badges
    val iconPlacement: String = "TOP", // TOP, BOTTOM, LEFT, RIGHT, OVERLAY, ICON_ONLY, LABEL_ONLY
    val badgeText: String = "",
    val badgeColorHex: Long = 0xFFEF4444L,
    val badgePosition: String = "TOP_END", // TOP_END, TOP_START, BOTTOM_END

    // Conditional Visibility
    val visibilityCondition: String = "ALWAYS", // ALWAYS, PORTRAIT_ONLY, LANDSCAPE_ONLY, CHARGING_ONLY, DARK_THEME_ONLY, LIGHT_THEME_ONLY, MORNING_ONLY, EVENING_ONLY

    // Graphic Gauge Config
    val gaugeStyle: String = "ARC", // ARC, RING, SEGMENTED, LINEAR, SPARKLINE
    val gaugeMetric: String = "BATTERY", // BATTERY, RAM, STORAGE, DAY_PROGRESS

    // Widget Stack Config
    val stackSwipeAxis: String = "VERTICAL", // VERTICAL, HORIZONTAL
    val stackAutoRotateSec: Int = 0, // 0 = off
    val stackIndicatorStyle: String = "DOTS", // DOTS, PILL, NUMBERS, HIDDEN
    val stackSmartPriority: Boolean = true,

    // Container Box & App Group Config
    val containerScrollAxis: String = "VERTICAL", // VERTICAL, HORIZONTAL
    val appGroupSource: String = "FAVORITES", // TAG, CATEGORY, FAVORITES, RECENT, MOST_USED
    val appGroupColumns: Int = 4,
    val appGroupIconSizeDp: Int = 44,

    // Interactive Utility State
    val quickNotesText: String = "Tap to add notes\n- Organize home deck\n- Try Widget Stacks",
    val deviceToggleAction: String = "TORCH", // TORCH, RINGER, WIFI_PANEL, BLUETOOTH_PANEL, VOLUME_PANEL, DISPLAY_SETTINGS
    val focusMinutes: Int = 25,
) {
    fun toJson(): String = JSONObject().apply {
        put("borderStyle", borderStyle)
        put("borderGradientEndHex", borderGradientEndHex ?: JSONObject.NULL)
        put("useCustomCorners", useCustomCorners)
        put("cornerTopStartDp", cornerTopStartDp.toDouble())
        put("cornerTopEndDp", cornerTopEndDp.toDouble())
        put("cornerBottomEndDp", cornerBottomEndDp.toDouble())
        put("cornerBottomStartDp", cornerBottomStartDp.toDouble())
        put("gradientDirection", gradientDirection)
        put("shadowColorHex", shadowColorHex ?: JSONObject.NULL)
        put("shadowBlurDp", shadowBlurDp.toDouble())
        put("shadowOffsetXDp", shadowOffsetXDp.toDouble())
        put("shadowOffsetYDp", shadowOffsetYDp.toDouble())
        put("rotationXDeg", rotationXDeg.toDouble())
        put("rotationYDeg", rotationYDeg.toDouble())
        put("cameraDistance", cameraDistance.toDouble())
        put("pressedScale", pressedScale.toDouble())
        put("pressedBgColorHex", pressedBgColorHex ?: JSONObject.NULL)
        put("pressedBorderColorHex", pressedBorderColorHex ?: JSONObject.NULL)
        put("pressedTextColorHex", pressedTextColorHex ?: JSONObject.NULL)
        put("pressedImagePath", pressedImagePath ?: JSONObject.NULL)
        put("entranceAnim", entranceAnim)
        put("tapAnim", tapAnim)
        put("idleAnim", idleAnim)
        put("fontRef", fontRef)
        put("fontWeight", fontWeight)
        put("italic", italic)
        put("textTransform", textTransform)
        put("letterSpacingEm", letterSpacingEm.toDouble())
        put("lineHeightMultiplier", lineHeightMultiplier.toDouble())
        put("textAlign", textAlign)
        put("textShadowOverride", textShadowOverride ?: JSONObject.NULL)
        put("textOutlineOverride", textOutlineOverride ?: JSONObject.NULL)
        put("textBackdropOverride", textBackdropOverride ?: JSONObject.NULL)
        put("textBackdropColorHex", textBackdropColorHex)
        put("textBackdropAlpha", textBackdropAlpha.toDouble())
        put("marqueeEnabled", marqueeEnabled)
        put("maxLines", maxLines)
        put("iconPlacement", iconPlacement)
        put("badgeText", badgeText)
        put("badgeColorHex", badgeColorHex)
        put("badgePosition", badgePosition)
        put("visibilityCondition", visibilityCondition)
        put("gaugeStyle", gaugeStyle)
        put("gaugeMetric", gaugeMetric)
        put("stackSwipeAxis", stackSwipeAxis)
        put("stackAutoRotateSec", stackAutoRotateSec)
        put("stackIndicatorStyle", stackIndicatorStyle)
        put("stackSmartPriority", stackSmartPriority)
        put("containerScrollAxis", containerScrollAxis)
        put("appGroupSource", appGroupSource)
        put("appGroupColumns", appGroupColumns)
        put("appGroupIconSizeDp", appGroupIconSizeDp)
        put("quickNotesText", quickNotesText)
        put("deviceToggleAction", deviceToggleAction)
        put("focusMinutes", focusMinutes)
    }.toString()

    companion object {
        fun fromJson(json: String?): ElementStyleExtras {
            if (json.isNullOrBlank()) return ElementStyleExtras()
            return try {
                val o = JSONObject(json)
                fun optNullLong(k: String): Long? = if (o.isNull(k) || !o.has(k)) null else o.optLong(k)
                fun optNullBool(k: String): Boolean? = if (o.isNull(k) || !o.has(k)) null else o.optBoolean(k)
                fun optNullStr(k: String): String? = if (o.isNull(k) || !o.has(k)) null else o.optString(k).takeIf { it.isNotBlank() }

                ElementStyleExtras(
                    borderStyle = o.optString("borderStyle", "SOLID"),
                    borderGradientEndHex = optNullLong("borderGradientEndHex"),
                    useCustomCorners = o.optBoolean("useCustomCorners", false),
                    cornerTopStartDp = o.optDouble("cornerTopStartDp", 20.0).toFloat(),
                    cornerTopEndDp = o.optDouble("cornerTopEndDp", 20.0).toFloat(),
                    cornerBottomEndDp = o.optDouble("cornerBottomEndDp", 20.0).toFloat(),
                    cornerBottomStartDp = o.optDouble("cornerBottomStartDp", 20.0).toFloat(),
                    gradientDirection = o.optString("gradientDirection", "VERTICAL"),
                    shadowColorHex = optNullLong("shadowColorHex"),
                    shadowBlurDp = o.optDouble("shadowBlurDp", 0.0).toFloat(),
                    shadowOffsetXDp = o.optDouble("shadowOffsetXDp", 0.0).toFloat(),
                    shadowOffsetYDp = o.optDouble("shadowOffsetYDp", 4.0).toFloat(),
                    rotationXDeg = o.optDouble("rotationXDeg", 0.0).toFloat(),
                    rotationYDeg = o.optDouble("rotationYDeg", 0.0).toFloat(),
                    cameraDistance = o.optDouble("cameraDistance", 12.0).toFloat(),
                    pressedScale = o.optDouble("pressedScale", 0.94).toFloat(),
                    pressedBgColorHex = optNullLong("pressedBgColorHex"),
                    pressedBorderColorHex = optNullLong("pressedBorderColorHex"),
                    pressedTextColorHex = optNullLong("pressedTextColorHex"),
                    pressedImagePath = optNullStr("pressedImagePath"),
                    entranceAnim = o.optString("entranceAnim", "NONE"),
                    tapAnim = o.optString("tapAnim", "SCALE_DOWN"),
                    idleAnim = o.optString("idleAnim", "NONE"),
                    fontRef = o.optString("fontRef", "INHERIT"),
                    fontWeight = o.optString("fontWeight", "INHERIT"),
                    italic = o.optBoolean("italic", false),
                    textTransform = o.optString("textTransform", "NONE"),
                    letterSpacingEm = o.optDouble("letterSpacingEm", 0.0).toFloat(),
                    lineHeightMultiplier = o.optDouble("lineHeightMultiplier", 1.15).toFloat(),
                    textAlign = o.optString("textAlign", "CENTER"),
                    textShadowOverride = optNullBool("textShadowOverride"),
                    textOutlineOverride = optNullBool("textOutlineOverride"),
                    textBackdropOverride = optNullBool("textBackdropOverride"),
                    textBackdropColorHex = o.optLong("textBackdropColorHex", 0xAA000000L),
                    textBackdropAlpha = o.optDouble("textBackdropAlpha", 0.7).toFloat(),
                    marqueeEnabled = o.optBoolean("marqueeEnabled", false),
                    maxLines = o.optInt("maxLines", 2),
                    iconPlacement = o.optString("iconPlacement", "TOP"),
                    badgeText = o.optString("badgeText", ""),
                    badgeColorHex = o.optLong("badgeColorHex", 0xFFEF4444L),
                    badgePosition = o.optString("badgePosition", "TOP_END"),
                    visibilityCondition = o.optString("visibilityCondition", "ALWAYS"),
                    gaugeStyle = o.optString("gaugeStyle", "ARC"),
                    gaugeMetric = o.optString("gaugeMetric", "BATTERY"),
                    stackSwipeAxis = o.optString("stackSwipeAxis", "VERTICAL"),
                    stackAutoRotateSec = o.optInt("stackAutoRotateSec", 0),
                    stackIndicatorStyle = o.optString("stackIndicatorStyle", "DOTS"),
                    stackSmartPriority = o.optBoolean("stackSmartPriority", true),
                    containerScrollAxis = o.optString("containerScrollAxis", "VERTICAL"),
                    appGroupSource = o.optString("appGroupSource", "FAVORITES"),
                    appGroupColumns = o.optInt("appGroupColumns", 4),
                    appGroupIconSizeDp = o.optInt("appGroupIconSizeDp", 44),
                    quickNotesText = o.optString("quickNotesText", "Tap to add notes\n- Organize home deck\n- Try Widget Stacks"),
                    deviceToggleAction = o.optString("deviceToggleAction", "TORCH"),
                    focusMinutes = o.optInt("focusMinutes", 25),
                )
            } catch (_: Exception) {
                ElementStyleExtras()
            }
        }
    }
}
