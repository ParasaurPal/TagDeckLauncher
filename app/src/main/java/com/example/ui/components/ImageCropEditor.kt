package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import java.io.File
import kotlin.math.min

/**
 * Full-screen portrait crop workspace shared by every background editor.
 * The source image is fitted in its entirety; drag and pinch directly on it.
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
    var editorOpen by remember(imagePath) { androidx.compose.runtime.mutableStateOf(false) }

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
            Button(
                onClick = { editorOpen = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Open Full-Screen Image Editor")
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
    var imageAspect by remember(imageFile.absolutePath) { mutableFloatStateOf(9f / 16f) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
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
                Text("Position Background Image", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onClose) { Text("Done") }
            }

            Text(
                "The whole original image is shown. Drag to move it; pinch to zoom.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .pointerInput(imageFile.absolutePath, imageAspect) {
                        detectTransformGestures { _, pan, zoomChange, _ ->
                            val nextScale = (currentScale * zoomChange).coerceIn(1f, 4f)
                            val fitWidth: Float
                            val fitHeight: Float
                            val sourceAspect = imageAspect.coerceAtLeast(0.01f)
                            if (size.width.toFloat() / size.height.toFloat() > sourceAspect) {
                                fitHeight = size.height.toFloat()
                                fitWidth = fitHeight * sourceAspect
                            } else {
                                fitWidth = size.width.toFloat()
                                fitHeight = fitWidth / sourceAspect
                            }
                            val maxPanX = ((fitWidth * nextScale) - size.width).coerceAtLeast(0f) / 2f
                            val maxPanY = ((fitHeight * nextScale) - size.height).coerceAtLeast(0f) / 2f
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
                    contentDescription = "Full image crop preview. Drag to reposition or pinch to zoom.",
                    contentScale = ContentScale.Fit,
                    onSuccess = { state: AsyncImagePainter.State.Success ->
                        val width = state.result.drawable.intrinsicWidth.toFloat()
                        val height = state.result.drawable.intrinsicHeight.toFloat()
                        if (width > 0f && height > 0f) imageAspect = width / height
                    },
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        val zoom = cropScale.coerceIn(1f, 4f)
                        scaleX = zoom
                        scaleY = zoom
                        translationX = size.width * cropX.coerceIn(-1f, 1f) * (zoom - 1f) / 2f
                        translationY = size.height * cropY.coerceIn(-1f, 1f) * (zoom - 1f) / 2f
                    },
                )
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
                TextButton(onClick = { onCropChange(1f, 0f, 0f) }) { Text("Reset crop") }
                Button(onClick = onClose) { Text("Apply") }
            }
        }
    }
}
