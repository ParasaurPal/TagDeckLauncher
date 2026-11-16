package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import java.io.File

/**
 * Crop workspace for image-backed surfaces. The crop viewport uses the same
 * ContentScale.Crop and normalized zoom/pan transform as SurfaceBackgroundLayer.
 * This keeps the editor result and the All Apps drawer rendering in sync.
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
    val imageFile = imagePath?.let(::File)?.takeIf { it.exists() }
    var editorOpen by remember(imagePath) { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (imageFile != null) {
            AsyncImage(
                model = imageFile,
                contentDescription = "Selected background image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
            )
            Button(onClick = { editorOpen = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Open Traditional Crop Editor")
            }
        } else {
            Text("The selected image could not be found.", color = MaterialTheme.colorScheme.error)
        }
    }

    if (editorOpen && imageFile != null) {
        FullScreenImageCropDialog(
            imageFile = imageFile,
            cropScale = cropScale,
            cropX = cropX,
            cropY = cropY,
            onCropChange = onCropChange,
            onClose = { editorOpen = false },
        )
    }
}

@Composable
private fun FullScreenImageCropDialog(
    imageFile: File,
    cropScale: Float,
    cropX: Float,
    cropY: Float,
    onCropChange: (Float, Float, Float) -> Unit,
    onClose: () -> Unit,
) {
    val currentScale by rememberUpdatedState(cropScale)
    val currentX by rememberUpdatedState(cropX)
    val currentY by rememberUpdatedState(cropY)
    val onChange by rememberUpdatedState(onCropChange)

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Crop Drawer Background", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onClose) { Text("Done") }
            }
            Text(
                "Drag the image to choose the visible area. Pinch to zoom. The frame shows the final drawer crop.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Keep the complete source visible as a reference while editing the
            // exact cropped viewport below.
            AsyncImage(
                model = imageFile,
                contentDescription = "Entire original image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.22f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black),
            )

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                val previewWidth = minOf(maxWidth, maxHeight * (9f / 16f))
                Box(
                    modifier = Modifier
                        .width(previewWidth)
                        .aspectRatio(9f / 16f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black)
                        .pointerInput(imageFile.absolutePath) {
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
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = "Live cropped drawer image. Drag to reposition or pinch to zoom.",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().graphicsLayer {
                            val zoom = cropScale.coerceIn(1f, 4f)
                            scaleX = zoom
                            scaleY = zoom
                            translationX = size.width * cropX.coerceIn(-1f, 1f) * (zoom - 1f) / 2f
                            translationY = size.height * cropY.coerceIn(-1f, 1f) * (zoom - 1f) / 2f
                        },
                    )
                    CropGridOverlay()
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Zoom: ${String.format("%.1f", cropScale)}×")
                Slider(
                    value = cropScale.coerceIn(1f, 4f),
                    onValueChange = { onCropChange(it, cropX, cropY) },
                    valueRange = 1f..4f,
                )
                Text("Horizontal position: ${(cropX * 100).toInt()}%")
                Slider(
                    value = cropX.coerceIn(-1f, 1f),
                    onValueChange = { onCropChange(cropScale, it, cropY) },
                    valueRange = -1f..1f,
                )
                Text("Vertical position: ${(cropY * 100).toInt()}%")
                Slider(
                    value = cropY.coerceIn(-1f, 1f),
                    onValueChange = { onCropChange(cropScale, cropX, it) },
                    valueRange = -1f..1f,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { onCropChange(1f, 0f, 0f) }) { Text("Reset Crop") }
                Button(onClick = onClose) { Text("Apply Crop") }
            }
        }
    }
}

@Composable
private fun CropGridOverlay() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val lineColor = Color.White.copy(alpha = 0.72f)
        val thirdsX = size.width / 3f
        val thirdsY = size.height / 3f
        for (i in 1..2) {
            drawLine(lineColor, Offset(thirdsX * i, 0f), Offset(thirdsX * i, size.height), 1.dp.toPx())
            drawLine(lineColor, Offset(0f, thirdsY * i), Offset(size.width, thirdsY * i), 1.dp.toPx())
        }
        drawRect(Color.White, style = Stroke(width = 2.dp.toPx()))
        val corner = size.minDimension * 0.055f
        val stroke = 3.dp.toPx()
        val points = listOf(
            Offset(0f, 0f), Offset(size.width, 0f),
            Offset(0f, size.height), Offset(size.width, size.height),
        )
        points.forEachIndexed { index, point ->
            val path = Path().apply {
                when (index) {
                    0 -> { moveTo(point.x, point.y + corner); lineTo(point.x, point.y); lineTo(point.x + corner, point.y) }
                    1 -> { moveTo(point.x - corner, point.y); lineTo(point.x, point.y); lineTo(point.x, point.y + corner) }
                    2 -> { moveTo(point.x, point.y - corner); lineTo(point.x, point.y); lineTo(point.x + corner, point.y) }
                    else -> { moveTo(point.x - corner, point.y); lineTo(point.x, point.y); lineTo(point.x, point.y - corner) }
                }
            }
            drawPath(path, Color.White, style = Stroke(width = stroke))
        }
    }
}
