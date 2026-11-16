package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

/**
 * Visual crop surface shared by every background editor.
 * Drag with one finger to reposition; pinch with two fingers to zoom and reposition.
 */
@Composable
fun ImageCropEditor(
    imagePath: String?,
    cropScale: Float,
    cropX: Float,
    cropY: Float,
    onCropChange: (scale: Float, x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentScale by rememberUpdatedState(cropScale)
    val currentX by rememberUpdatedState(cropX)
    val currentY by rememberUpdatedState(cropY)
    val onChange by rememberUpdatedState(onCropChange)
    val imageFile = imagePath?.let(::File)?.takeIf { it.exists() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black)
            .pointerInput(imagePath) {
                detectTransformGestures { _, pan, zoomChange, _ ->
                    val nextScale = (currentScale * zoomChange).coerceIn(1f, 4f)
                    val maxPanX = size.width * (nextScale - 1f) / 2f
                    val maxPanY = size.height * (nextScale - 1f) / 2f
                    val nextX = if (maxPanX > 0f) {
                        (currentX + pan.x / maxPanX).coerceIn(-1f, 1f)
                    } else 0f
                    val nextY = if (maxPanY > 0f) {
                        (currentY + pan.y / maxPanY).coerceIn(-1f, 1f)
                    } else 0f
                    onChange(nextScale, nextX, nextY)
                }
            },
    ) {
        if (imageFile != null) {
            AsyncImage(
                model = imageFile,
                contentDescription = "Drag or pinch to crop background image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    val zoom = cropScale.coerceIn(1f, 4f)
                    scaleX = zoom
                    scaleY = zoom
                    translationX = size.width * cropX.coerceIn(-1f, 1f) * (zoom - 1f) / 2f
                    translationY = size.height * cropY.coerceIn(-1f, 1f) * (zoom - 1f) / 2f
                },
            )
        }
    }
}
