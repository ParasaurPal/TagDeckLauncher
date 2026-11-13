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
) = darkColorScheme(
    primary = accent,
    onPrimary = Color(0xFF06131F),
    primaryContainer = accent.copy(alpha = 0.22f),
    onPrimaryContainer = TextOnDark,
    secondary = secondary,
    onSecondary = TextOnDark,
    secondaryContainer = ObsidianSurfaceBright,
    onSecondaryContainer = TextOnDark,
    tertiary = tertiary,
    onTertiary = TextOnDark,
    background = ObsidianBg,
    onBackground = TextOnDark,
    surface = ObsidianSurface,
    onSurface = TextOnDark,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianSurfaceElevated,
    surfaceContainerHigh = ObsidianSurfaceBright,
    outline = OutlineDark,
    outlineVariant = OutlineDark.copy(alpha = 0.5f),
)

private fun buildLightScheme(
    accent: Color,
    secondary: Color,
    tertiary: Color,
) = lightColorScheme(
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.16f),
    onPrimaryContainer = TextOnLight,
    secondary = secondary,
    onSecondary = Color.White,
    secondaryContainer = PorcelainSurfaceVariant,
    onSecondaryContainer = TextOnLight,
    tertiary = tertiary,
    onTertiary = Color.White,
    background = PorcelainBg,
    onBackground = TextOnLight,
    surface = PorcelainSurface,
    onSurface = TextOnLight,
    surfaceVariant = PorcelainSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainerLow = PorcelainBg,
    surfaceContainer = PorcelainSurface,
    surfaceContainerHigh = PorcelainSurfaceVariant,
    outline = OutlineLight,
    outlineVariant = OutlineLight.copy(alpha = 0.5f),
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

    val colorScheme = when {
        useDynamic -> {
            val context = LocalContext.current
            if (systemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDark -> buildDarkScheme(accent, secondary, tertiary)
        else -> buildLightScheme(accent, secondary, tertiary)
    }

    val radius = cornerRadiusDp.coerceIn(4f, 36f)
    val shapes = Shapes(
        small = RoundedCornerShape((radius * 0.6f).dp),
        medium = RoundedCornerShape(radius.dp),
        large = RoundedCornerShape((radius * 1.3f).dp),
        extraLarge = RoundedCornerShape((radius * 1.5f).dp)
    )

    val typography = buildTagDeckTypography(fontStyle = fontStyle, fontScale = fontScale)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
