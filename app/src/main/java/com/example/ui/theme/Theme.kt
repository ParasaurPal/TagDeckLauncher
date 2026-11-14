package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private fun buildDarkScheme(
    accent: Color,
    secondary: Color,
    tertiary: Color,
    customSurface: Color? = null,
    customCard: Color? = null,
    customOutline: Color? = null,
) = darkColorScheme(
    primary = accent,
    onPrimary = Color(0xFF06131F),
    primaryContainer = accent.copy(alpha = 0.22f),
    onPrimaryContainer = TextOnDark,
    secondary = secondary,
    onSecondary = TextOnDark,
    secondaryContainer = customCard ?: ObsidianSurfaceBright,
    onSecondaryContainer = TextOnDark,
    tertiary = tertiary,
    onTertiary = TextOnDark,
    background = customSurface ?: ObsidianBg,
    onBackground = TextOnDark,
    surface = customSurface ?: ObsidianSurface,
    onSurface = TextOnDark,
    surfaceVariant = customCard ?: ObsidianSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLow = customSurface ?: ObsidianSurface,
    surfaceContainer = customCard ?: ObsidianSurfaceElevated,
    surfaceContainerHigh = customCard ?: ObsidianSurfaceBright,
    outline = customOutline ?: OutlineDark,
    outlineVariant = (customOutline ?: OutlineDark).copy(alpha = 0.5f),
)

private fun buildLightScheme(
    accent: Color,
    secondary: Color,
    tertiary: Color,
    customSurface: Color? = null,
    customCard: Color? = null,
    customOutline: Color? = null,
) = lightColorScheme(
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.16f),
    onPrimaryContainer = TextOnLight,
    secondary = secondary,
    onSecondary = Color.White,
    secondaryContainer = customCard ?: PorcelainSurfaceVariant,
    onSecondaryContainer = TextOnLight,
    tertiary = tertiary,
    onTertiary = Color.White,
    background = customSurface ?: PorcelainBg,
    onBackground = TextOnLight,
    surface = customSurface ?: PorcelainSurface,
    onSurface = TextOnLight,
    surfaceVariant = customCard ?: PorcelainSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainerLow = customSurface ?: PorcelainBg,
    surfaceContainer = customCard ?: PorcelainSurface,
    surfaceContainerHigh = customCard ?: PorcelainSurfaceVariant,
    outline = customOutline ?: OutlineLight,
    outlineVariant = (customOutline ?: OutlineLight).copy(alpha = 0.5f),
)

@Composable
fun TagDeckTheme(
    themeMode: String = "DARK",
    accentColorHex: Long = 0xFF38BDF8L,
    secondaryColorHex: Long = 0xFF6366F1L,
    tertiaryColorHex: Long = 0xFFFB7185L,
    fontStyle: String = "DEFAULT",
    fontScale: Float = 1.0f,
    cornerRadiusDp: Float = 20f,
    customFontPath: String? = null,
    customSurfaceColorHex: Long? = null,
    customCardColorHex: Long? = null,
    customOutlineColorHex: Long? = null,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> systemDark
    }
    val useDynamic = themeMode == "DYNAMIC" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val accent = Color(accentColorHex)
    val secondary = Color(secondaryColorHex)
    val tertiary = Color(tertiaryColorHex)
    val customSurface = customSurfaceColorHex?.let { Color(it) }
    val customCard = customCardColorHex?.let { Color(it) }
    val customOutline = customOutlineColorHex?.let { Color(it) }

    val baseScheme = when {
        useDynamic -> {
            val context = LocalContext.current
            if (systemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDark -> buildDarkScheme(accent, secondary, tertiary, customSurface, customCard, customOutline)
        else -> buildLightScheme(accent, secondary, tertiary, customSurface, customCard, customOutline)
    }
    val colorScheme = if (useDynamic && (customSurface != null || customCard != null || customOutline != null)) {
        baseScheme.copy(
            background = customSurface ?: baseScheme.background,
            surface = customSurface ?: baseScheme.surface,
            surfaceVariant = customCard ?: baseScheme.surfaceVariant,
            surfaceContainer = customCard ?: baseScheme.surfaceContainer,
            outline = customOutline ?: baseScheme.outline,
        )
    } else baseScheme

    val radius = cornerRadiusDp.coerceIn(4f, 36f)
    val shapes = Shapes(
        small = RoundedCornerShape((radius * 0.6f).dp),
        medium = RoundedCornerShape(radius.dp),
        large = RoundedCornerShape((radius * 1.3f).dp),
        extraLarge = RoundedCornerShape((radius * 1.5f).dp)
    )

    val typography = buildTagDeckTypography(
        fontStyle = fontStyle,
        fontScale = fontScale,
        customFontPath = customFontPath,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
