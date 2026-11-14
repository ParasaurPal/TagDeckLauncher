package com.example.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.data.db.CustomFontEntity
import com.example.data.db.ElementStyleExtras
import com.example.data.prefs.LauncherSettings
import com.example.data.system.CustomFontRegistry
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.SpaceGroteskFamily

data class ReadabilityConfig(
    val fontStyle: String = "DEFAULT",
    val customFontId: Long? = null,
    val customFontPath: String? = null,
    val fontScale: Float = 1.0f,
    val highReadabilityMode: Boolean = false,
    val boldLabelsEnabled: Boolean = false,
    val labelBackdropEnabled: Boolean = false,
    val labelBackdropColorHex: Long = 0xAA000000L,
    val labelBackdropAlpha: Float = 0.72f,
    val textOutlineEnabled: Boolean = false,
    val textOutlineColorHex: Long = 0xFF000000L,
    val textOutlineWidthDp: Float = 1.5f,
    val textShadowEnabled: Boolean = true,
    val textShadowRadiusDp: Float = 4f,
    val letterSpacingEm: Float = 0f,
    val lineHeightMultiplier: Float = 1.15f,
    val minLabelFontSp: Float = 11f,
    val labelMaxLines: Int = 2,
    val autoContrastText: Boolean = true,
) {
    companion object {
        fun fromSettings(s: LauncherSettings): ReadabilityConfig = ReadabilityConfig(
            fontStyle = s.fontStyle,
            customFontId = s.customFontId,
            customFontPath = s.customFontPath,
            fontScale = s.fontScale,
            highReadabilityMode = s.highReadabilityMode,
            boldLabelsEnabled = s.boldLabelsEnabled,
            labelBackdropEnabled = s.labelBackdropEnabled,
            labelBackdropColorHex = s.labelBackdropColorHex,
            labelBackdropAlpha = s.labelBackdropAlpha,
            textOutlineEnabled = s.textOutlineEnabled,
            textOutlineColorHex = s.textOutlineColorHex,
            textOutlineWidthDp = s.textOutlineWidthDp,
            textShadowEnabled = s.textShadowEnabled,
            textShadowRadiusDp = s.textShadowRadiusDp,
            letterSpacingEm = s.letterSpacingEm,
            lineHeightMultiplier = s.lineHeightMultiplier,
            minLabelFontSp = s.minLabelFontSp,
            labelMaxLines = s.labelMaxLines,
            autoContrastText = s.autoContrastText,
        )
    }
}

val LocalReadabilityConfig = compositionLocalOf { ReadabilityConfig() }
val LocalCustomFonts = compositionLocalOf<List<CustomFontEntity>> { emptyList() }

@Composable
fun OutlinedLabelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    outlineEnabled: Boolean = false,
) {
    ReadableLauncherText(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
        lineHeight = lineHeight,
        forceOutline = if (outlineEnabled) true else null,
    )
}

@Composable
fun ReadableLauncherText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int? = null,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    extras: ElementStyleExtras? = null,
    surfaceHintColor: Color? = null,
    forceOutline: Boolean? = null,
) {
    val cfg = LocalReadabilityConfig.current
    val customFonts = LocalCustomFonts.current
    val density = LocalDensity.current

    val effectiveHighReadability = cfg.highReadabilityMode
    val effectiveOutline = forceOutline
        ?: extras?.textOutlineOverride
        ?: (cfg.textOutlineEnabled || effectiveHighReadability)
    val effectiveBackdrop = extras?.textBackdropOverride
        ?: (cfg.labelBackdropEnabled || effectiveHighReadability)
    val effectiveShadow = extras?.textShadowOverride ?: cfg.textShadowEnabled
    val effectiveBold = cfg.boldLabelsEnabled || effectiveHighReadability

    val resolvedBackdropColor = Color(extras?.textBackdropColorHex ?: cfg.labelBackdropColorHex)
        .copy(alpha = (extras?.textBackdropAlpha ?: cfg.labelBackdropAlpha).coerceIn(0.15f, 1f))

    val effectiveColor = if (cfg.autoContrastText) {
        val referenceBg = when {
            effectiveBackdrop -> resolvedBackdropColor
            surfaceHintColor != null && surfaceHintColor.alpha > 0.35f -> surfaceHintColor
            else -> null
        }
        if (referenceBg != null) {
            val bgLum = referenceBg.luminance()
            val fgLum = color.luminance()
            if (kotlin.math.abs(bgLum - fgLum) < 0.28f) {
                if (bgLum > 0.5f) Color(0xFF0F172A) else Color(0xFFF8FAFC)
            } else color
        } else color
    } else color

    val resolvedFamily: FontFamily? = when (val ref = extras?.fontRef ?: "INHERIT") {
        "INHERIT" -> if (cfg.fontStyle == "CUSTOM" && !cfg.customFontPath.isNullOrBlank()) {
            CustomFontRegistry.resolveFontFamily(cfg.customFontPath)
        } else style.fontFamily
        "SPACE_GROTESK" -> SpaceGroteskFamily
        "PLUS_JAKARTA" -> PlusJakartaSansFamily
        "JETBRAINS_MONO" -> JetBrainsMonoFamily
        "SYSTEM" -> FontFamily.Default
        "DEFAULT" -> SpaceGroteskFamily
        else -> if (ref.startsWith("CUSTOM:")) {
            CustomFontRegistry.resolveFontFamilyByRef(ref, customFonts)
        } else style.fontFamily
    }

    val resolvedWeight: FontWeight = when (extras?.fontWeight ?: "INHERIT") {
        "LIGHT" -> FontWeight.Light
        "NORMAL" -> FontWeight.Normal
        "MEDIUM" -> FontWeight.Medium
        "SEMIBOLD" -> FontWeight.SemiBold
        "BOLD" -> FontWeight.Bold
        "EXTRABOLD" -> FontWeight.ExtraBold
        else -> when {
            effectiveBold && (style.fontWeight?.weight ?: 400) < 700 -> FontWeight.Bold
            effectiveBold -> FontWeight.ExtraBold
            else -> style.fontWeight ?: FontWeight.Medium
        }
    }

    val baseSp = if (style.fontSize != TextUnit.Unspecified) style.fontSize.value else 12f
    val minSp = if (effectiveHighReadability) maxOf(cfg.minLabelFontSp, 13f) else cfg.minLabelFontSp
    val adjustedSp = (baseSp * if (effectiveHighReadability) 1.06f else 1f).coerceAtLeast(minSp)

    val effectiveLetterSpacing = when {
        extras != null && extras.letterSpacingEm != 0f -> extras.letterSpacingEm.em
        cfg.letterSpacingEm != 0f -> cfg.letterSpacingEm.em
        effectiveHighReadability -> 0.03.em
        else -> style.letterSpacing
    }

    val effectiveLineHeightMult = when {
        extras != null -> extras.lineHeightMultiplier
        effectiveHighReadability -> maxOf(cfg.lineHeightMultiplier, 1.28f)
        else -> cfg.lineHeightMultiplier
    }
    val resolvedLineHeight = if (lineHeight != TextUnit.Unspecified) {
        lineHeight
    } else {
        (adjustedSp * effectiveLineHeightMult.coerceIn(0.9f, 2.0f)).sp
    }

    val resolvedTextAlign = when (extras?.textAlign) {
        "START" -> TextAlign.Start
        "END" -> TextAlign.End
        "CENTER" -> TextAlign.Center
        else -> textAlign
    }

    val transformedText = when (extras?.textTransform) {
        "UPPERCASE" -> text.uppercase()
        "LOWERCASE" -> text.lowercase()
        else -> text
    }

    val resolvedMaxLines = maxLines ?: extras?.maxLines ?: cfg.labelMaxLines
    val shadowObj = if (effectiveShadow) {
        Shadow(
            color = if (effectiveColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.5f),
            offset = Offset(0f, with(density) { 1.dp.toPx() }),
            blurRadius = with(density) { cfg.textShadowRadiusDp.coerceIn(1f, 16f).dp.toPx() },
        )
    } else null

    val baseStyle = style.merge(
        TextStyle(
            color = effectiveColor,
            fontFamily = resolvedFamily,
            fontWeight = resolvedWeight,
            fontStyle = if (extras?.italic == true) FontStyle.Italic else style.fontStyle,
            fontSize = adjustedSp.sp,
            letterSpacing = effectiveLetterSpacing,
            lineHeight = resolvedLineHeight,
            shadow = shadowObj,
        )
    )

    val containerModifier = if (effectiveBackdrop) {
        modifier
            .background(resolvedBackdropColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    } else {
        modifier
    }

    val textModifier = if (extras?.marqueeEnabled == true) {
        Modifier.basicMarquee()
    } else Modifier

    val outlineStrokePx = with(density) { cfg.textOutlineWidthDp.coerceIn(0.5f, 5f).dp.toPx() }
    val outlineColor = if (effectiveColor.luminance() < 0.25f) Color.White else Color(cfg.textOutlineColorHex)

    Box(modifier = containerModifier) {
        if (effectiveOutline) {
            Text(
                text = transformedText,
                modifier = textModifier,
                style = baseStyle.copy(
                    color = outlineColor,
                    drawStyle = Stroke(width = outlineStrokePx),
                    shadow = null,
                ),
                maxLines = resolvedMaxLines,
                overflow = if (extras?.marqueeEnabled == true) TextOverflow.Clip else overflow,
                textAlign = resolvedTextAlign,
            )
        }
        Text(
            text = transformedText,
            modifier = textModifier,
            style = baseStyle.copy(color = effectiveColor, drawStyle = Fill),
            maxLines = resolvedMaxLines,
            overflow = if (extras?.marqueeEnabled == true) TextOverflow.Clip else overflow,
            textAlign = resolvedTextAlign,
        )
    }
}
