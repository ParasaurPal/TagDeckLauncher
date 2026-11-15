package com.example.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun OutlinedLabelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: androidx.compose.ui.text.style.TextAlign? = null,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    outlineEnabled: Boolean = false,
    outlineColor: Color = Color.Black,
    outlineWidth: Float = 0.5f,
) {
    val baseStyle = style.merge(TextStyle(color = color))
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        if (outlineEnabled) {
            Text(
                text = text,
                style = baseStyle.copy(color = outlineColor, drawStyle = Stroke(width = outlineWidth.coerceIn(0.1f, 3f)),)
                maxLines = maxLines,
                overflow = overflow,
                textAlign = textAlign,
                lineHeight = lineHeight,
            )
        }
        Text(
            text = text,
            style = baseStyle.copy(color = color, drawStyle = Fill),
            maxLines = maxLines,
            overflow = overflow,
            textAlign = textAlign,
            lineHeight = lineHeight,
        )
    }
}
