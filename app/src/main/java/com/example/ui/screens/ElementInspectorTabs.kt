package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AlignHorizontalCenter
import androidx.compose.material.icons.rounded.AlignHorizontalLeft
import androidx.compose.material.icons.rounded.AlignHorizontalRight
import androidx.compose.material.icons.rounded.AlignVerticalBottom
import androidx.compose.material.icons.rounded.AlignVerticalCenter
import androidx.compose.material.icons.rounded.AlignVerticalTop
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.ElementStyleExtras
import com.example.data.db.HomeElementEntity
import com.example.ui.components.ColorSwatchRow
import com.example.ui.components.StudioDarkSurfaceSwatches
import com.example.ui.components.StudioNeutralAndAccentSwatches
import com.example.ui.state.LauncherUiState
import kotlin.math.roundToInt

@Composable
internal fun StepperSliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    onValueChange: (Float) -> Unit,
    sliderTag: String? = null,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("$label: $valueText", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = { onValueChange((value - step).coerceIn(range.start, range.endInclusive)) },
                    modifier = Modifier.height(32.dp),
                ) { Text("-") }
                OutlinedButton(
                    onClick = { onValueChange((value + step).coerceIn(range.start, range.endInclusive)) },
                    modifier = Modifier.height(32.dp),
                ) { Text("+") }
            }
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            modifier = if (sliderTag != null) Modifier.testTag(sliderTag) else Modifier,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InspectorLayoutTab(
    edited: HomeElementEntity,
    onUpdateEdited: (HomeElementEntity) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("inspector_coordinates_summary"),
    ) {
        Text(
            text = "Coords: X ${(edited.xFraction * 100f).roundToInt()}% · Y ${(edited.yFraction * 100f).roundToInt()}%  |  Size: ${edited.widthDp.roundToInt()} × ${edited.heightDp.roundToInt()} dp",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
    Text("Quick Align on Canvas", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedButton(
            onClick = { onUpdateEdited(edited.copy(xFraction = 0f)) },
            modifier = Modifier.testTag("inspector_align_left"),
        ) {
            Icon(Icons.Rounded.AlignHorizontalLeft, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Left")
        }
        OutlinedButton(
            onClick = { onUpdateEdited(edited.copy(xFraction = 0.42f)) },
            modifier = Modifier.testTag("inspector_align_center_x"),
        ) {
            Icon(Icons.Rounded.AlignHorizontalCenter, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Center X")
        }
        OutlinedButton(
            onClick = { onUpdateEdited(edited.copy(xFraction = 0.80f)) },
            modifier = Modifier.testTag("inspector_align_right"),
        ) {
            Icon(Icons.Rounded.AlignHorizontalRight, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Right")
        }
        OutlinedButton(
            onClick = { onUpdateEdited(edited.copy(yFraction = 0f)) },
            modifier = Modifier.testTag("inspector_align_top"),
        ) {
            Icon(Icons.Rounded.AlignVerticalTop, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Top")
        }
        OutlinedButton(
            onClick = { onUpdateEdited(edited.copy(yFraction = 0.42f)) },
            modifier = Modifier.testTag("inspector_align_center_y"),
        ) {
            Icon(Icons.Rounded.AlignVerticalCenter, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Middle Y")
        }
        OutlinedButton(
            onClick = { onUpdateEdited(edited.copy(yFraction = 0.80f)) },
            modifier = Modifier.testTag("inspector_align_bottom"),
        ) {
            Icon(Icons.Rounded.AlignVerticalBottom, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Bottom")
        }
    }
    StepperSliderRow(
        label = "Width",
        valueText = "${edited.widthDp.roundToInt()}dp",
        value = edited.widthDp,
        range = 48f..380f,
        step = 4f,
        onValueChange = { onUpdateEdited(edited.copy(widthDp = it)) },
        sliderTag = "inspector_width_slider",
    )
    StepperSliderRow(
        label = "Height",
        valueText = "${edited.heightDp.roundToInt()}dp",
        value = edited.heightDp,
        range = 48f..440f,
        step = 4f,
        onValueChange = { onUpdateEdited(edited.copy(heightDp = it)) },
        sliderTag = "inspector_height_slider",
    )
    StepperSliderRow(
        label = "X Position",
        valueText = "${(edited.xFraction * 100).roundToInt()}%",
        value = edited.xFraction,
        range = 0f..0.95f,
        step = 0.01f,
        onValueChange = { onUpdateEdited(edited.copy(xFraction = it)) },
    )
    StepperSliderRow(
        label = "Y Position",
        valueText = "${(edited.yFraction * 100).roundToInt()}%",
        value = edited.yFraction,
        range = 0f..0.95f,
        step = 0.01f,
        onValueChange = { onUpdateEdited(edited.copy(yFraction = it)) },
    )
    val landW = edited.landscapeWidthDp ?: edited.widthDp
    val landH = edited.landscapeHeightDp ?: edited.heightDp
    StepperSliderRow(
        label = "Landscape Width",
        valueText = if (edited.landscapeWidthDp != null) "${landW.roundToInt()}dp" else "Auto (${landW.roundToInt()}dp)",
        value = landW,
        range = 48f..420f,
        step = 4f,
        onValueChange = { onUpdateEdited(edited.copy(landscapeWidthDp = it)) },
    )
    StepperSliderRow(
        label = "Landscape Height",
        valueText = if (edited.landscapeHeightDp != null) "${landH.roundToInt()}dp" else "Auto (${landH.roundToInt()}dp)",
        value = landH,
        range = 48f..420f,
        step = 4f,
        onValueChange = { onUpdateEdited(edited.copy(landscapeHeightDp = it)) },
    )
    if (edited.landscapeWidthDp != null || edited.landscapeHeightDp != null) {
        TextButton(onClick = { onUpdateEdited(edited.copy(landscapeWidthDp = null, landscapeHeightDp = null)) }) {
            Text("Reset Landscape Size to Match Portrait")
        }
    }
    Text("Rotation: ${edited.rotationDeg.roundToInt()}°")
    Slider(value = edited.rotationDeg, onValueChange = { onUpdateEdited(edited.copy(rotationDeg = it)) }, valueRange = -180f..180f)
    Text("Scale X: ${"%.2f".format(edited.scaleX)}x")
    Slider(value = edited.scaleX, onValueChange = { onUpdateEdited(edited.copy(scaleX = it)) }, valueRange = 0.4f..2.0f)
    Text("Scale Y: ${"%.2f".format(edited.scaleY)}x")
    Slider(value = edited.scaleY, onValueChange = { onUpdateEdited(edited.copy(scaleY = it)) }, valueRange = 0.4f..2.0f)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Flip Horizontal")
        Switch(checked = edited.flipHorizontal, onCheckedChange = { onUpdateEdited(edited.copy(flipHorizontal = it)) })
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Flip Vertical")
        Switch(checked = edited.flipVertical, onCheckedChange = { onUpdateEdited(edited.copy(flipVertical = it)) })
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Pin Across All Pages (Sticky)")
        Switch(
            checked = edited.pinnedToAllPages,
            onCheckedChange = { onUpdateEdited(edited.copy(pinnedToAllPages = it)) },
        )
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Z-Index: ${edited.zIndex}", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { onUpdateEdited(edited.copy(zIndex = (edited.zIndex - 1).coerceAtLeast(0))) },
                modifier = Modifier.testTag("inspector_z_minus"),
            ) { Text("-") }
            OutlinedButton(
                onClick = { onUpdateEdited(edited.copy(zIndex = edited.zIndex + 1)) },
                modifier = Modifier.testTag("inspector_z_plus"),
            ) { Text("+") }
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Visible on Canvas")
        Switch(checked = edited.visible, onCheckedChange = { onUpdateEdited(edited.copy(visible = it)) })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InspectorStyleFxTab(
    edited: HomeElementEntity,
    extras: ElementStyleExtras,
    onUpdateEdited: (HomeElementEntity) -> Unit,
    onUpdateExtras: (ElementStyleExtras) -> Unit,
    onPickBackgroundImage: () -> Unit,
) {
    Text("Shape Mask (11 Shapes)", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("NONE", "SQUIRCLE", "CIRCLE", "DIAMOND", "HEXAGON", "OCTAGON", "PILL", "ARCH", "CUT_CORNER", "STAR", "TRIANGLE").forEach { m ->
            FilterChip(
                selected = edited.imageMaskShape == m,
                onClick = { onUpdateEdited(edited.copy(imageMaskShape = m)) },
                label = { Text(m) },
                modifier = Modifier.testTag("inspector_mask_$m"),
            )
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Independent Corner Radii")
        Switch(checked = extras.useCustomCorners, onCheckedChange = { onUpdateExtras(extras.copy(useCustomCorners = it)) })
    }
    if (!extras.useCustomCorners) {
        Text("Corner Radius: ${edited.cornerRadiusDp.roundToInt()}dp")
        Slider(value = edited.cornerRadiusDp, onValueChange = { onUpdateEdited(edited.copy(cornerRadiusDp = it)) }, valueRange = 0f..74f)
    } else {
        Text("Top-Start: ${extras.cornerTopStartDp.roundToInt()}dp")
        Slider(value = extras.cornerTopStartDp, onValueChange = { onUpdateExtras(extras.copy(cornerTopStartDp = it)) }, valueRange = 0f..74f)
        Text("Top-End: ${extras.cornerTopEndDp.roundToInt()}dp")
        Slider(value = extras.cornerTopEndDp, onValueChange = { onUpdateExtras(extras.copy(cornerTopEndDp = it)) }, valueRange = 0f..74f)
        Text("Bottom-End: ${extras.cornerBottomEndDp.roundToInt()}dp")
        Slider(value = extras.cornerBottomEndDp, onValueChange = { onUpdateExtras(extras.copy(cornerBottomEndDp = it)) }, valueRange = 0f..74f)
        Text("Bottom-Start: ${extras.cornerBottomStartDp.roundToInt()}dp")
        Slider(value = extras.cornerBottomStartDp, onValueChange = { onUpdateExtras(extras.copy(cornerBottomStartDp = it)) }, valueRange = 0f..74f)
    }

    Text("Transparency: ${(edited.alpha * 100).roundToInt()}%")
    Slider(value = edited.alpha, onValueChange = { onUpdateEdited(edited.copy(alpha = it)) }, valueRange = 0.1f..1f)
    Text("Inner Padding: ${edited.paddingDp.roundToInt()}dp")
    Slider(value = edited.paddingDp, onValueChange = { onUpdateEdited(edited.copy(paddingDp = it)) }, valueRange = 0f..32f)

    Text("Border Style", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("SOLID", "DASHED", "DOTTED", "DOUBLE", "GRADIENT").forEach { bs ->
            FilterChip(
                selected = extras.borderStyle == bs,
                onClick = { onUpdateExtras(extras.copy(borderStyle = bs)) },
                label = { Text(bs) },
                modifier = Modifier.testTag("inspector_border_style_$bs"),
            )
        }
    }
    Text("Border Width: ${"%.1f".format(edited.borderWidthDp)}dp")
    Slider(
        value = edited.borderWidthDp,
        onValueChange = { onUpdateEdited(edited.copy(borderWidthDp = it)) },
        valueRange = 0f..8f,
        modifier = Modifier.testTag("inspector_border_width_slider"),
    )
    Text("Border Color", style = MaterialTheme.typography.labelLarge)
    ColorSwatchRow(
        selectedColorHex = edited.borderColorHex,
        colors = StudioNeutralAndAccentSwatches,
        onSelect = { c -> onUpdateEdited(edited.copy(borderColorHex = c)) },
        tagPrefix = "inspector_border_color",
    )
    if (extras.borderStyle == "GRADIENT") {
        Text("Border Gradient End Color", style = MaterialTheme.typography.labelLarge)
        ColorSwatchRow(
            selectedColorHex = extras.borderGradientEndHex,
            colors = StudioNeutralAndAccentSwatches,
            allowClear = true,
            onClear = { onUpdateExtras(extras.copy(borderGradientEndHex = null)) },
            onSelect = { c -> onUpdateExtras(extras.copy(borderGradientEndHex = c)) },
            tagPrefix = "inspector_border_grad_end",
        )
    }

    Text("Background Color / Tint", style = MaterialTheme.typography.labelLarge)
    ColorSwatchRow(
        selectedColorHex = edited.backgroundColorHex,
        colors = StudioDarkSurfaceSwatches,
        allowClear = true,
        onClear = { onUpdateEdited(edited.copy(backgroundColorHex = 0x00000000L)) },
        onSelect = { c -> onUpdateEdited(edited.copy(backgroundColorHex = c)) },
        tagPrefix = "inspector_bg_color",
    )
    Text("Gradient End Color (Optional)", style = MaterialTheme.typography.labelLarge)
    ColorSwatchRow(
        selectedColorHex = edited.backgroundGradientEndHex,
        colors = StudioDarkSurfaceSwatches,
        allowClear = true,
        onClear = { onUpdateEdited(edited.copy(backgroundGradientEndHex = null)) },
        onSelect = { c -> onUpdateEdited(edited.copy(backgroundGradientEndHex = c)) },
        tagPrefix = "inspector_grad_end",
    )
    if (edited.backgroundGradientEndHex != null) {
        Text("Gradient Direction", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("VERTICAL", "HORIZONTAL", "DIAGONAL", "RADIAL").forEach { dir ->
                FilterChip(
                    selected = extras.gradientDirection == dir,
                    onClick = { onUpdateExtras(extras.copy(gradientDirection = dir)) },
                    label = { Text(dir) },
                )
            }
        }
    }

    HorizontalDivider()
    Text("Drop Shadow & Colored Glow", style = MaterialTheme.typography.labelLarge)
    Text("Elevation: ${edited.shadowElevationDp.roundToInt()}dp")
    Slider(value = edited.shadowElevationDp, onValueChange = { onUpdateEdited(edited.copy(shadowElevationDp = it)) }, valueRange = 0f..24f)
    Text("Custom Glow Color", style = MaterialTheme.typography.labelMedium)
    ColorSwatchRow(
        selectedColorHex = extras.shadowColorHex,
        colors = StudioNeutralAndAccentSwatches,
        allowClear = true,
        onClear = { onUpdateExtras(extras.copy(shadowColorHex = null, shadowBlurDp = 0f)) },
        onSelect = { c -> onUpdateExtras(extras.copy(shadowColorHex = c, shadowBlurDp = extras.shadowBlurDp.coerceAtLeast(8f))) },
        tagPrefix = "inspector_glow_color",
    )
    if (extras.shadowColorHex != null) {
        Text("Glow Radius: ${extras.shadowBlurDp.roundToInt()}dp")
        Slider(value = extras.shadowBlurDp, onValueChange = { onUpdateExtras(extras.copy(shadowBlurDp = it)) }, valueRange = 0f..28f)
        Text("Shadow Offset X: ${extras.shadowOffsetXDp.roundToInt()}dp")
        Slider(value = extras.shadowOffsetXDp, onValueChange = { onUpdateExtras(extras.copy(shadowOffsetXDp = it)) }, valueRange = -16f..16f)
        Text("Shadow Offset Y: ${extras.shadowOffsetYDp.roundToInt()}dp")
        Slider(value = extras.shadowOffsetYDp, onValueChange = { onUpdateExtras(extras.copy(shadowOffsetYDp = it)) }, valueRange = -16f..16f)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onPickBackgroundImage) { Text("Pick Background Image") }
        if (edited.imagePath != null) {
            TextButton(onClick = { onUpdateEdited(edited.copy(imagePath = null)) }) { Text("Clear Image") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InspectorAnim3DTab(
    extras: ElementStyleExtras,
    onUpdateExtras: (ElementStyleExtras) -> Unit,
) {
    Text("3D Perspective Tilt", style = MaterialTheme.typography.labelLarge)
    Text("Tilt X (Pitch): ${extras.rotationXDeg.roundToInt()}°")
    Slider(value = extras.rotationXDeg, onValueChange = { onUpdateExtras(extras.copy(rotationXDeg = it)) }, valueRange = -45f..45f)
    Text("Tilt Y (Yaw): ${extras.rotationYDeg.roundToInt()}°")
    Slider(value = extras.rotationYDeg, onValueChange = { onUpdateExtras(extras.copy(rotationYDeg = it)) }, valueRange = -45f..45f)

    HorizontalDivider()
    Text("Entrance Animation", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("NONE", "FADE", "SCALE", "SLIDE_UP", "BOUNCE").forEach { a ->
            FilterChip(selected = extras.entranceAnim == a, onClick = { onUpdateExtras(extras.copy(entranceAnim = a)) }, label = { Text(a) })
        }
    }
    Text("Tap Animation", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("NONE", "SCALE_DOWN", "BOUNCE", "PULSE", "SPIN").forEach { a ->
            FilterChip(selected = extras.tapAnim == a, onClick = { onUpdateExtras(extras.copy(tapAnim = a)) }, label = { Text(a) })
        }
    }
    Text("Idle Motion", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("NONE", "FLOAT", "PULSE", "BREATHE", "SPIN_SLOW").forEach { a ->
            FilterChip(selected = extras.idleAnim == a, onClick = { onUpdateExtras(extras.copy(idleAnim = a)) }, label = { Text(a) })
        }
    }

    HorizontalDivider()
    Text("Pressed-State Visual Overrides", style = MaterialTheme.typography.labelLarge)
    Text("Pressed Scale: ${"%.2f".format(extras.pressedScale)}x")
    Slider(value = extras.pressedScale, onValueChange = { onUpdateExtras(extras.copy(pressedScale = it)) }, valueRange = 0.75f..1.15f)
    Text("Pressed Background Color", style = MaterialTheme.typography.labelMedium)
    ColorSwatchRow(
        selectedColorHex = extras.pressedBgColorHex,
        colors = StudioDarkSurfaceSwatches,
        allowClear = true,
        onClear = { onUpdateExtras(extras.copy(pressedBgColorHex = null)) },
        onSelect = { c -> onUpdateExtras(extras.copy(pressedBgColorHex = c)) },
        tagPrefix = "inspector_pressed_bg",
    )
    Text("Pressed Border Color", style = MaterialTheme.typography.labelMedium)
    ColorSwatchRow(
        selectedColorHex = extras.pressedBorderColorHex,
        colors = StudioNeutralAndAccentSwatches,
        allowClear = true,
        onClear = { onUpdateExtras(extras.copy(pressedBorderColorHex = null)) },
        onSelect = { c -> onUpdateExtras(extras.copy(pressedBorderColorHex = c)) },
        tagPrefix = "inspector_pressed_border",
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InspectorTextPlaceTab(
    edited: HomeElementEntity,
    extras: ElementStyleExtras,
    state: LauncherUiState,
    onUpdateEdited: (HomeElementEntity) -> Unit,
    onUpdateExtras: (ElementStyleExtras) -> Unit,
) {
    if (edited.type != "APP_WIDGET") {
        OutlinedTextField(
            value = edited.label,
            onValueChange = { onUpdateEdited(edited.copy(label = it)) },
            label = { Text("Label ({TIME}, {BATTERY}, {NETWORK}, {GREETING}, {PAGE_NAME})") },
            singleLine = false,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth().testTag("inspector_label_input"),
        )
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Show Label")
        Switch(checked = edited.showLabel, onCheckedChange = { onUpdateEdited(edited.copy(showLabel = it)) })
    }

    Text("Font Size: ${edited.fontSizeSp.roundToInt()}sp")
    Slider(
        value = edited.fontSizeSp,
        onValueChange = { onUpdateEdited(edited.copy(fontSizeSp = it)) },
        valueRange = 8f..48f,
        modifier = Modifier.testTag("inspector_font_size_slider"),
    )

    Text("Text Color", style = MaterialTheme.typography.labelLarge)
    ColorSwatchRow(
        selectedColorHex = edited.textColorHex,
        colors = StudioNeutralAndAccentSwatches,
        onSelect = { c -> onUpdateEdited(edited.copy(textColorHex = c)) },
        tagPrefix = "inspector_text_color",
    )

    Text("Font Family (Built-in & Uploaded)", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val fontChoices = listOf(
            "INHERIT" to "Global",
            "SPACE_GROTESK" to "Geometric",
            "PLUS_JAKARTA" to "Clean Sans",
            "JETBRAINS_MONO" to "Mono",
            "SYSTEM" to "System",
        ) + state.customFonts.map { "CUSTOM:${it.fontId}" to it.name }
        fontChoices.forEach { (key, lbl) ->
            FilterChip(
                selected = extras.fontRef == key,
                onClick = { onUpdateExtras(extras.copy(fontRef = key)) },
                label = { Text(lbl) },
            )
        }
    }

    Text("Font Weight & Case", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("INHERIT", "LIGHT", "NORMAL", "MEDIUM", "SEMIBOLD", "BOLD", "EXTRABOLD").forEach { w ->
            FilterChip(selected = extras.fontWeight == w, onClick = { onUpdateExtras(extras.copy(fontWeight = w)) }, label = { Text(w) })
        }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(selected = extras.italic, onClick = { onUpdateExtras(extras.copy(italic = !extras.italic)) }, label = { Text("Italic") })
        listOf("NONE", "UPPERCASE", "LOWERCASE").forEach { tr ->
            FilterChip(selected = extras.textTransform == tr, onClick = { onUpdateExtras(extras.copy(textTransform = tr)) }, label = { Text(tr) })
        }
        FilterChip(selected = extras.marqueeEnabled, onClick = { onUpdateExtras(extras.copy(marqueeEnabled = !extras.marqueeEnabled)) }, label = { Text("Marquee") })
    }

    Text("Text Alignment", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("START", "CENTER", "END").forEach { al ->
            FilterChip(selected = extras.textAlign == al, onClick = { onUpdateExtras(extras.copy(textAlign = al)) }, label = { Text(al) })
        }
    }

    Text("Readability Overrides", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(
            selected = extras.textOutlineOverride == true,
            onClick = { onUpdateExtras(extras.copy(textOutlineOverride = if (extras.textOutlineOverride == true) null else true)) },
            label = { Text("Force Outline") },
        )
        FilterChip(
            selected = extras.textBackdropOverride == true,
            onClick = { onUpdateExtras(extras.copy(textBackdropOverride = if (extras.textBackdropOverride == true) null else true)) },
            label = { Text("Force Backdrop") },
        )
        FilterChip(
            selected = extras.textShadowOverride == false,
            onClick = { onUpdateExtras(extras.copy(textShadowOverride = if (extras.textShadowOverride == false) null else false)) },
            label = { Text("Disable Shadow") },
        )
    }

    HorizontalDivider()
    Text("Icon Placement", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("TOP", "BOTTOM", "LEFT", "RIGHT", "OVERLAY", "ICON_ONLY", "LABEL_ONLY").forEach { pl ->
            FilterChip(selected = extras.iconPlacement == pl, onClick = { onUpdateExtras(extras.copy(iconPlacement = pl)) }, label = { Text(pl) })
        }
    }
    Text("Icon Size: ${edited.iconSizeDp.roundToInt()}dp")
    Slider(value = edited.iconSizeDp, onValueChange = { onUpdateEdited(edited.copy(iconSizeDp = it)) }, valueRange = 20f..120f)

    OutlinedTextField(
        value = extras.badgeText,
        onValueChange = { onUpdateExtras(extras.copy(badgeText = it.take(16))) },
        label = { Text("Custom Badge Pill (optional, e.g. NEW or 3)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    if (extras.badgeText.isNotBlank()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("TOP_END", "TOP_START", "BOTTOM_END").forEach { pos ->
                FilterChip(selected = extras.badgePosition == pos, onClick = { onUpdateExtras(extras.copy(badgePosition = pos)) }, label = { Text(pos) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InspectorWidgetsAndRulesTab(
    edited: HomeElementEntity,
    extras: ElementStyleExtras,
    state: LauncherUiState,
    onUpdateEdited: (HomeElementEntity) -> Unit,
    onUpdateExtras: (ElementStyleExtras) -> Unit,
    onAddChildToContainer: (Long, HomeElementEntity) -> Unit,
    onMoveChildInParent: (Long, Int) -> Unit,
    onInspectChild: (HomeElementEntity) -> Unit,
    onDeleteChild: (HomeElementEntity) -> Unit,
    onOpenMacroBuilder: () -> Unit,
) {
    when (edited.type) {
        "WIDGET_STACK", "CONTAINER_BOX" -> {
            if (edited.type == "WIDGET_STACK") {
                Text("Widget Stack Options", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("VERTICAL", "HORIZONTAL").forEach { axis ->
                        FilterChip(selected = extras.stackSwipeAxis == axis, onClick = { onUpdateExtras(extras.copy(stackSwipeAxis = axis)) }, label = { Text("Swipe $axis") })
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("DOTS", "PILL", "NUMBERS", "HIDDEN").forEach { ind ->
                        FilterChip(selected = extras.stackIndicatorStyle == ind, onClick = { onUpdateExtras(extras.copy(stackIndicatorStyle = ind)) }, label = { Text(ind) })
                    }
                }
                Text("Auto-Rotate: ${if (extras.stackAutoRotateSec == 0) "Off" else "${extras.stackAutoRotateSec}s"}")
                Slider(
                    value = extras.stackAutoRotateSec.toFloat(),
                    onValueChange = { onUpdateExtras(extras.copy(stackAutoRotateSec = it.roundToInt())) },
                    valueRange = 0f..30f,
                )
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Smart Context Priority")
                    Switch(checked = extras.stackSmartPriority, onCheckedChange = { onUpdateExtras(extras.copy(stackSmartPriority = it)) })
                }
            } else {
                Text("Container Scroll Axis", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("VERTICAL", "HORIZONTAL").forEach { axis ->
                        FilterChip(selected = extras.containerScrollAxis == axis, onClick = { onUpdateExtras(extras.copy(containerScrollAxis = axis)) }, label = { Text(axis) })
                    }
                }
            }

            val children = state.homeElements
                .filter { it.parentContainerId == edited.elementId }
                .sortedWith(compareBy<HomeElementEntity> { it.zIndex }.thenBy { it.elementId })

            Text("Child Items (${children.size})", style = MaterialTheme.typography.labelLarge)
            children.forEachIndexed { idx, child ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${idx + 1}. ${child.label.ifBlank { child.type }}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        IconButton(onClick = { onMoveChildInParent(child.elementId, -1) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Rounded.ArrowUpward, "Move up", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onMoveChildInParent(child.elementId, +1) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Rounded.ArrowDownward, "Move down", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onInspectChild(child) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Rounded.Tune, "Edit child", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onDeleteChild(child) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Rounded.Delete, "Remove child", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Text("Quick Add Widget Child", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    "DIGITAL_CLOCK" to "Clock",
                    "GAUGE_WIDGET" to "Gauge",
                    "BATTERY_WIDGET" to "Battery",
                    "CALENDAR_WIDGET" to "Calendar",
                    "MEDIA_WIDGET" to "Media",
                    "QUICK_NOTES_WIDGET" to "Notes",
                    "FOCUS_TIMER_WIDGET" to "Focus",
                    "DEVICE_TOGGLE_WIDGET" to "Torch",
                ).forEach { (wType, wLbl) ->
                    OutlinedButton(
                        onClick = {
                            onAddChildToContainer(
                                edited.elementId,
                                HomeElementEntity(
                                    pageId = edited.pageId,
                                    ownerType = edited.ownerType,
                                    ownerWindowId = edited.ownerWindowId,
                                    type = wType,
                                    label = wLbl,
                                    widthDp = (edited.widthDp - 12f).coerceAtLeast(100f),
                                    heightDp = (edited.heightDp - 12f).coerceAtLeast(80f),
                                    backgroundColorHex = 0x00000000L,
                                    borderWidthDp = 0f,
                                ),
                            )
                        },
                    ) {
                        Icon(Icons.Rounded.Add, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(wLbl)
                    }
                }
            }
        }
        "GAUGE_WIDGET" -> {
            Text("Gauge Style", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("ARC", "RING", "SEGMENTED", "LINEAR", "SPARKLINE").forEach { gs ->
                    FilterChip(selected = extras.gaugeStyle == gs, onClick = { onUpdateExtras(extras.copy(gaugeStyle = gs)) }, label = { Text(gs) })
                }
            }
            Text("Metric Source", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("BATTERY", "RAM", "STORAGE", "DAY_PROGRESS").forEach { gm ->
                    FilterChip(selected = extras.gaugeMetric == gm, onClick = { onUpdateExtras(extras.copy(gaugeMetric = gm)) }, label = { Text(gm) })
                }
            }
        }
        "APP_GROUP_WIDGET" -> {
            Text("App Source", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("FAVORITES", "RECENT", "MOST_USED", "TAG", "CATEGORY").forEach { src ->
                    FilterChip(selected = extras.appGroupSource == src, onClick = { onUpdateExtras(extras.copy(appGroupSource = src)) }, label = { Text(src) })
                }
            }
            Text("Columns: ${extras.appGroupColumns}")
            Slider(value = extras.appGroupColumns.toFloat(), onValueChange = { onUpdateExtras(extras.copy(appGroupColumns = it.roundToInt())) }, valueRange = 2f..6f)
        }
        "DEVICE_TOGGLE_WIDGET" -> {
            Text("Device Control Action", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("TORCH", "RINGER", "WIFI_PANEL", "BLUETOOTH_PANEL", "VOLUME_PANEL", "DISPLAY_SETTINGS").forEach { act ->
                    FilterChip(selected = extras.deviceToggleAction == act, onClick = { onUpdateExtras(extras.copy(deviceToggleAction = act)) }, label = { Text(act) })
                }
            }
        }
        "FOCUS_TIMER_WIDGET" -> {
            Text("Focus Duration: ${extras.focusMinutes} min")
            Slider(value = extras.focusMinutes.toFloat(), onValueChange = { onUpdateExtras(extras.copy(focusMinutes = it.roundToInt())) }, valueRange = 5f..90f)
        }
    }

    HorizontalDivider()
    Text("Conditional Visibility Rule", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(
            "ALWAYS" to "Always",
            "PORTRAIT_ONLY" to "Portrait",
            "LANDSCAPE_ONLY" to "Landscape",
            "CHARGING_ONLY" to "Charging",
            "DARK_THEME_ONLY" to "Dark Mode",
            "LIGHT_THEME_ONLY" to "Light Mode",
            "MORNING_ONLY" to "Morning",
            "EVENING_ONLY" to "Evening",
        ).forEach { (cond, lbl) ->
            FilterChip(
                selected = extras.visibilityCondition == cond,
                onClick = { onUpdateExtras(extras.copy(visibilityCondition = cond)) },
                label = { Text(lbl) },
            )
        }
    }
    OutlinedTextField(
        value = edited.visibilityGroupId.orEmpty(),
        onValueChange = { onUpdateEdited(edited.copy(visibilityGroupId = it.ifBlank { null })) },
        label = { Text("Visibility Group Name (toggle via buttons/macros)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(4.dp))
    Button(
        onClick = onOpenMacroBuilder,
        modifier = Modifier.fillMaxWidth().testTag("inspector_open_macro_builder"),
    ) {
        Icon(Icons.Rounded.Bolt, null, Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Tap Action Macro Builder")
    }
}
