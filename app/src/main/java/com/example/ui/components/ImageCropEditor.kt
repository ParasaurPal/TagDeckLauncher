package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File
import kotlin.math.abs

/**
 * Shared interactive crop surface.
 * Drag with one finger to reposition; pinch with two fingers to zoom and reposition.
 * A small automatic zoom is applied when dragging at 1x so a drag always has room to move.
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
                    val requestedScale = (currentScale * zoomChange).coerceIn(1f, 4f)
                    // At exactly 1x there is no overflow to pan. Give a small zoom headroom
                    // automatically when the user starts dragging instead of silently ignoring it.
                    val draggingAtMinimumZoom = requestedScale <= 1.001f &&
                        (abs(pan.x) > 0.5f || abs(pan.y) > 0.5f)
                    val nextScale = if (draggingAtMinimumZoom) 1.2f else requestedScale

                    val oldMaxPanX = size.width * (currentScale - 1f) / 2f
                    val oldMaxPanY = size.height * (currentScale - 1f) / 2f
                    val maxPanX = size.width * (nextScale - 1f) / 2f
                    val maxPanY = size.height * (nextScale - 1f) / 2f

                    // Keep the existing image offset in pixels as zoom changes, then add
                    // the finger movement. This avoids the large jumps caused by changing
                    // the normalized crop percentage at low zoom levels.
                    val scaledOffsetX = if (currentScale > 0f) currentX * oldMaxPanX * (nextScale / currentScale) else 0f
                    val scaledOffsetY = if (currentScale > 0f) currentY * oldMaxPanY * (nextScale / currentScale) else 0f
                    val nextX = if (maxPanX > 0f) {
                        (scaledOffsetX + pan.x).coerceIn(-maxPanX, maxPanX) / maxPanX
                    } else 0f
                    val nextY = if (maxPanY > 0f) {
                        (scaledOffsetY + pan.y).coerceIn(-maxPanY, maxPanY) / maxPanY
                    } else 0f
                    onChange(nextScale, nextX, nextY)
                }
            },
    ) {
        if (imageFile != null) {
            AsyncImage(
                model = imageFile,
                contentDescription = "Background crop preview. Drag to reposition and pinch to zoom.",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    val zoom = cropScale.coerceIn(1f, 4f)
                    scaleX = zoom
                    scaleY = zoom
                    translationX = size.width * cropX.coerceIn(-1f, 1f) * (zoom - 1f) / 2f
                    translationY = size.height * cropY.coerceIn(-1f, 1f) * (zoom - 1f) / 2f
                },
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Choose a background image to preview its crop", color = Color.White)
            }
        }

        // Persistent on-preview affordance: makes the image itself look and feel draggable.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.72f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("✥", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            androidx.compose.foundation.layout.Column {
                Text(
                    "DRAG IMAGE TO REPOSITION",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Pinch to zoom • Drag with one finger",
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
