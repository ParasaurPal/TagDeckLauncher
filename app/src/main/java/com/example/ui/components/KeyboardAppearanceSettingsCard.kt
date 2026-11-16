package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.prefs.LauncherSettings
import kotlin.math.roundToInt

@Composable
fun KeyboardAppearanceSettingsCard(
    settings: LauncherSettings,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onPickWallpaper: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Choose how the T9 search keypad looks and responds. Changes are saved automatically.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text("Keyboard Surface Theme", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text("Select the material used for every key.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                "MINIMALISM" to "Minimalism",
                "SKEUOMORPHISM" to "Skeuomorphism",
            ).forEach { (key, label) ->
                FilterChip(
                    selected = settings.keyboardSurfaceTheme == key,
                    onClick = { onUpdateSettings { it.copy(keyboardSurfaceTheme = key) } },
                    label = { Text(label, fontSize = 11.sp) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("BLACKBERRY" to "BlackBerry", "GLASS" to "Glass").forEach { (key, label) ->
                FilterChip(
                    selected = settings.keyboardSurfaceTheme == key,
                    onClick = { onUpdateSettings { it.copy(keyboardSurfaceTheme = key) } },
                    label = { Text(label, fontSize = 11.sp) },
                )
            }
        }
        T9KeyboardSurfacePreview(settings)
        HorizontalDivider()
        Text("Key Touch Effect", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        ToggleSettingRow(
            label = "Enable key touch effects",
            checked = settings.keyboardTouchEffectEnabled,
            onCheckedChange = { value -> onUpdateSettings { it.copy(keyboardTouchEffectEnabled = value) } },
        )
        if (settings.keyboardTouchEffectEnabled) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("STANDARD" to "Standard", "RIPPLE" to "Ripple", "GLOW" to "Glow").forEach { (key, label) ->
                    FilterChip(
                        selected = settings.keyboardTouchEffect == key,
                        onClick = { onUpdateSettings { it.copy(keyboardTouchEffect = key) } },
                        label = { Text(label, fontSize = 11.sp) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("BOUNCE" to "Bounce", "NONE" to "None").forEach { (key, label) ->
                    FilterChip(
                        selected = settings.keyboardTouchEffect == key,
                        onClick = { onUpdateSettings { it.copy(keyboardTouchEffect = key) } },
                        label = { Text(label, fontSize = 11.sp) },
                    )
                }
            }
        }
        ToggleSettingRow(
            label = "Haptic feedback",
            checked = settings.keyboardHapticEnabled,
            onCheckedChange = { value -> onUpdateSettings { it.copy(keyboardHapticEnabled = value) } },
        )
        ToggleSettingRow(
            label = "Key sounds",
            checked = settings.keyboardTouchSoundEnabled,
            onCheckedChange = { value -> onUpdateSettings { it.copy(keyboardTouchSoundEnabled = value) } },
        )
        HorizontalDivider()
        Text("Glass & Liquid Glass", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text("These controls apply when the Glass surface theme is selected.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ToggleSettingRow(
            label = "Experimental Liquid Glass",
            checked = settings.keyboardGlassLiquidEnabled,
            onCheckedChange = { value -> onUpdateSettings { it.copy(keyboardGlassLiquidEnabled = value) } },
        )
        ToggleSettingRow(
            label = "Use launcher wallpaper",
            checked = settings.keyboardGlassUseWallpaper,
            onCheckedChange = { value -> onUpdateSettings { it.copy(keyboardGlassUseWallpaper = value) } },
        )
        Text("Keyboard wallpaper", style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (settings.keyboardWallpaperPath.isNullOrBlank()) "No separate keyboard wallpaper selected" else "Custom keyboard wallpaper selected",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onPickWallpaper) { Text("Choose") }
        }
        Text("Glass opacity: ${(settings.keyboardGlassOpacity * 100).roundToInt()}%")
        Slider(
            value = settings.keyboardGlassOpacity,
            onValueChange = { value -> onUpdateSettings { it.copy(keyboardGlassOpacity = value) } },
            valueRange = 0.15f..1f,
        )
        Text("Background blur: ${settings.keyboardGlassBlurDp.roundToInt()}dp")
        Slider(
            value = settings.keyboardGlassBlurDp,
            onValueChange = { value -> onUpdateSettings { it.copy(keyboardGlassBlurDp = value) } },
            valueRange = 0f..40f,
        )
        Text("Refraction intensity: ${(settings.keyboardGlassRefraction * 100).roundToInt()}%")
        Slider(
            value = settings.keyboardGlassRefraction,
            onValueChange = { value -> onUpdateSettings { it.copy(keyboardGlassRefraction = value) } },
            valueRange = 0f..1f,
        )
    }
}

@Composable
private fun ToggleSettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun T9KeyboardSurfacePreview(settings: LauncherSettings) {
    val glass = settings.keyboardSurfaceTheme == "GLASS"
    val containerShape = RoundedCornerShape(22.dp)
    val backgroundBrush = when (settings.keyboardSurfaceTheme) {
        "SKEUOMORPHISM" -> Brush.verticalGradient(listOf(Color(0xFF566273), Color(0xFF202A39), Color(0xFF111925)))
        "BLACKBERRY" -> Brush.verticalGradient(listOf(Color(0xFF233B57), Color(0xFF0C1727)))
        "GLASS" -> Brush.verticalGradient(listOf(Color(0xBFFFFFFF).copy(alpha = settings.keyboardGlassOpacity), Color(0x553D6C91).copy(alpha = settings.keyboardGlassOpacity)))
        else -> Brush.verticalGradient(listOf(Color(0xFF242833), Color(0xFF171B23)))
    }
    val keyBrush = when (settings.keyboardSurfaceTheme) {
        "SKEUOMORPHISM" -> Brush.verticalGradient(listOf(Color(0xFF758397), Color(0xFF3A4657), Color(0xFF242D3B)))
        "BLACKBERRY" -> Brush.verticalGradient(listOf(Color(0xFF405773), Color(0xFF1B2B40)))
        "GLASS" -> Brush.verticalGradient(listOf(Color.White.copy(alpha = settings.keyboardGlassOpacity * 0.72f), Color(0xFF7AA7CC).copy(alpha = settings.keyboardGlassOpacity * 0.22f)))
        else -> Brush.verticalGradient(listOf(Color(0xFF343A47), Color(0xFF252B36)))
    }
    val keyBorder = if (settings.keyboardSurfaceTheme == "MINIMALISM") Color.Transparent else Color.White.copy(alpha = if (glass) 0.42f else 0.28f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = containerShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Column(
            modifier = Modifier
                .background(backgroundBrush, containerShape)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("T9 KEYBOARD PREVIEW", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.72f), fontWeight = FontWeight.Bold)
                Text(settings.keyboardSurfaceTheme.replace('_', ' '), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.78f))
            }
            val rows = listOf(
                listOf("1" to "", "2" to "ABC", "3" to "DEF"),
                listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
                listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
                listOf("⌫" to "DELETE", "0" to "+", "↵" to "SEARCH"),
            )
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { (digit, letters) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .then(if (settings.keyboardSurfaceTheme == "SKEUOMORPHISM") Modifier.shadow(3.dp, RoundedCornerShape(11.dp)) else Modifier)
                                .clip(RoundedCornerShape(if (settings.keyboardSurfaceTheme == "BLACKBERRY") 7.dp else 11.dp))
                                .background(keyBrush)
                                .border(BorderStroke(1.dp, keyBorder), RoundedCornerShape(if (settings.keyboardSurfaceTheme == "BLACKBERRY") 7.dp else 11.dp))
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(digit, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 18.sp)
                                if (letters.isNotEmpty()) Text(letters, color = Color.White.copy(alpha = 0.68f), fontSize = 8.sp, lineHeight = 9.sp)
                            }
                        }
                    }
                }
            }
            Text(
                if (!settings.keyboardTouchEffectEnabled) "Touch feedback off" else "Touch effect: ${settings.keyboardTouchEffect.lowercase().replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.72f),
            )
            if (glass && settings.keyboardGlassLiquidEnabled) {
                Text("Liquid Glass enabled • ${(settings.keyboardGlassRefraction * 100).roundToInt()}% refraction", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}


@Composable
fun T9SearchKeyboardPad(
    settings: LauncherSettings,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onSearch: () -> Unit,
) {
    val view = LocalView.current
    val theme = settings.keyboardSurfaceTheme
    val panelBrush = when (theme) {
        "SKEUOMORPHISM" -> Brush.verticalGradient(listOf(Color(0xFF536174), Color(0xFF172131)))
        "BLACKBERRY" -> Brush.verticalGradient(listOf(Color(0xFF1E3854), Color(0xFF0A1422)))
        "GLASS" -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = settings.keyboardGlassOpacity.coerceIn(0.15f, 1f)),
                Color(0xFF3D6C91).copy(alpha = settings.keyboardGlassOpacity.coerceIn(0.15f, 1f) * 0.58f),
            ),
        )
        else -> Brush.verticalGradient(listOf(Color(0xFF292F3A), Color(0xFF171C25)))
    }
    val keyBrush = when (theme) {
        "SKEUOMORPHISM" -> Brush.verticalGradient(listOf(Color(0xFF718096), Color(0xFF303C4D)))
        "BLACKBERRY" -> Brush.verticalGradient(listOf(Color(0xFF3A526E), Color(0xFF172A40)))
        "GLASS" -> Brush.verticalGradient(listOf(Color.White.copy(alpha = settings.keyboardGlassOpacity * 0.72f), Color(0xFF8AB6D9).copy(alpha = settings.keyboardGlassOpacity * 0.22f)))
        else -> Brush.verticalGradient(listOf(Color(0xFF343B48), Color(0xFF252B36)))
    }
    val keyShape = RoundedCornerShape(if (theme == "BLACKBERRY") 7.dp else 12.dp)
    val keys = listOf(
        listOf("1" to "", "2" to "ABC", "3" to "DEF"),
        listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
        listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
        listOf("⌫" to "DELETE", "0" to "+", "↵" to "SEARCH"),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(panelBrush)
            .border(1.dp, Color.White.copy(alpha = if (theme == "GLASS") 0.46f else 0.18f), RoundedCornerShape(22.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        keys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                row.forEach { (digit, letters) ->
                    val glow = settings.keyboardTouchEffectEnabled && settings.keyboardTouchEffect == "GLOW"
                    val raised = theme == "SKEUOMORPHISM" || (settings.keyboardTouchEffectEnabled && settings.keyboardTouchEffect == "BOUNCE")
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .then(if (raised) Modifier.shadow(if (theme == "SKEUOMORPHISM") 4.dp else 2.dp, keyShape) else Modifier)
                            .clip(keyShape)
                            .background(keyBrush)
                            .border(
                                BorderStroke(
                                    if (glow) 1.6.dp else 0.8.dp,
                                    if (glow) MaterialTheme.colorScheme.primary.copy(alpha = 0.9f) else Color.White.copy(alpha = if (theme == "MINIMALISM") 0.08f else 0.3f),
                                ),
                                keyShape,
                            )
                            .clickable {
                                if (settings.keyboardHapticEnabled) view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                if (settings.keyboardTouchSoundEnabled) view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                                when (digit) {
                                    "⌫" -> onDelete()
                                    "↵" -> onSearch()
                                    else -> onDigit(digit)
                                }
                            }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(digit, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 21.sp)
                            if (letters.isNotEmpty()) Text(letters, color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp, lineHeight = 9.sp)
                        }
                    }
                }
            }
        }
    }
}
