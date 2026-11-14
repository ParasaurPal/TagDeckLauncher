package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val SpaceGroteskFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold),
)

val PlusJakartaSansFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
)

val JetBrainsMonoFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold),
)

fun buildTagDeckTypography(
    fontStyle: String = "DEFAULT",
    fontScale: Float = 1.0f,
    customFontPath: String? = null,
): Typography {
    val scale = fontScale.coerceIn(0.75f, 1.8f)
    val customFamily = if (fontStyle == "CUSTOM" && !customFontPath.isNullOrBlank()) {
        com.example.data.system.CustomFontRegistry.resolveFontFamily(customFontPath)
    } else null
    val displayFamily: FontFamily
    val bodyFamily: FontFamily
    val monoFamily: FontFamily
    when {
        customFamily != null -> {
            displayFamily = customFamily
            bodyFamily = customFamily
            monoFamily = JetBrainsMonoFamily
        }
        fontStyle == "SPACE_GROTESK" -> {
            displayFamily = SpaceGroteskFamily
            bodyFamily = SpaceGroteskFamily
            monoFamily = SpaceGroteskFamily
        }
        fontStyle == "PLUS_JAKARTA" -> {
            displayFamily = PlusJakartaSansFamily
            bodyFamily = PlusJakartaSansFamily
            monoFamily = PlusJakartaSansFamily
        }
        fontStyle == "JETBRAINS_MONO" -> {
            displayFamily = JetBrainsMonoFamily
            bodyFamily = JetBrainsMonoFamily
            monoFamily = JetBrainsMonoFamily
        }
        fontStyle == "SYSTEM" -> {
            displayFamily = FontFamily.Default
            bodyFamily = FontFamily.Default
            monoFamily = FontFamily.Monospace
        }
        else -> {
            displayFamily = SpaceGroteskFamily
            bodyFamily = PlusJakartaSansFamily
            monoFamily = JetBrainsMonoFamily
        }
    }
    return Typography(
        displayLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (52f * scale).sp,
            lineHeight = (58f * scale).sp,
            letterSpacing = (-1.0f * scale).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (36f * scale).sp,
            lineHeight = (42f * scale).sp,
            letterSpacing = (-0.5f * scale).sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (28f * scale).sp,
            lineHeight = (34f * scale).sp,
            letterSpacing = (-0.25f * scale).sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (22f * scale).sp,
            lineHeight = (28f * scale).sp,
        ),
        titleLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20f * scale).sp,
            lineHeight = (26f * scale).sp,
        ),
        titleMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16f * scale).sp,
            lineHeight = (22f * scale).sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (15f * scale).sp,
            lineHeight = (22f * scale).sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (13f * scale).sp,
            lineHeight = (18f * scale).sp,
        ),
        labelLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (13f * scale).sp,
            lineHeight = (18f * scale).sp,
        ),
        labelMedium = TextStyle(
            fontFamily = monoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11f * scale).sp,
            lineHeight = (14f * scale).sp,
            letterSpacing = 0.4.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11f * scale).sp,
            lineHeight = (14f * scale).sp,
        ),
    )
}

val TagDeckTypography = buildTagDeckTypography()
