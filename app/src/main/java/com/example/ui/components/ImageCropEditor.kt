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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import java.io.File
import kotlin.math.max
import kotlin.math.min

/**
 * Shows the complete source image with an adjustable frame indicating the portion
 * that will be used as the background. Drag to move the frame; pinch to zoom.
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
    val imageFile = remember(imagePath) { imagePath?.let(::File)?.takeIf { it.exists() } }
    var imageAspect by remember(imagePath) { mutableFloatStateOf(16f / 9f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF17151D))
            .pointerInput(imagePath, imageAspect) {
                detectTransformGestures { _, pan, zoomChange, _ ->
                    val viewportAspect = size.width / size.height
                    val fittedWidth: Float
                    val fittedHeight: Float
                    if (imageAspect > viewportAspect) {
                        fittedWidth = size.width.toFloat()
                        fittedHeight = fittedWidth / imageAspect
                    } else {
                        fittedHeight = size.height.toFloat()
                        fittedWidth = fittedHeight * imageAspect
                    }
                    val baseFrameWidth: Float
                    val baseFrameHeight: Float
                    if (imageAspect > viewportAspect) {
                        baseFrameWidth = fittedHeight * viewportAspect
                        baseFrameHeight = fittedHeight
                    } else {
                        baseFrameWidth = fittedWidth
                        baseFrameHeight = fittedWidth / viewportAspect
                    }

                    val requestedScale = (currentScale * zoomChange).coerceIn(1f, 4f)
                    val isDragging = kotlin.math.abs(pan.x) > 0.5f || kotlin.math.abs(pan.y) > 0.5f
                    val nextScale = if (requestedScale <= 1.001f && isDragging) 1.2f else requestedScale
                    val frameWidth = baseFrameWidth / nextScale
                    val frameHeight = baseFrameHeight / nextScale
                    val maxTravelX = max(0f, fittedWidth - frameWidth) / 2f
                    val maxTravelY = max(0f, fittedHeight - frameHeight) / 2f
                    val nextX = if (maxTravelX > 0f) {
                        (currentX + pan.x / maxTravelX).coerceIn(-1f, 1f)
                    } else currentX.coerceIn(-1f, 1f)
                    val nextY = if (maxTravelY > 0f) {
                        (currentY + pan.y / maxTravelY).coerceIn(-1f, 1f)
                    } else currentY.coerceIn(-1f, 1f)
                    onChange(nextScale, nextX, nextY)
                }
            },
    ) {
        if (imageFile != null) {
            AsyncImage(
                model = imageFile,
                contentDescription = "Full background image with crop selection frame. Drag to reposition and pinch to zoom.",
                contentScale = ContentScale.Fit,
                onSuccess = { state: AsyncImagePainter.State.Success ->
                    val source = state.painter.intrinsicSize
                    if (source.width.isFinite() && source.height.isFinite() &&
                        source.width > 0f && source.height > 0f
                    ) imageAspect = source.width / source.height
                },
                modifier = Modifier.fillMaxSize(),
            )

            Canvas(Modifier.fillMaxSize()) {
                val viewportAspect = size.width / size.height
                val fittedWidth: Float
                val fittedHeight: Float
                val imageLeft: Float
                val imageTop: Float
                if (imageAspect > viewportAspect) {
                    fittedWidth = size.width
                    fittedHeight = fittedWidth / imageAspect
                    imageLeft = 0f
                    imageTop = (size.height - fittedHeight) / 2f
                } else {
                    fittedHeight = size.height
                    fittedWidth = fittedHeight * imageAspect
                    imageLeft = (size.width - fittedWidth) / 2f
                    imageTop = 0f
                }

                val baseFrameWidth: Float
                val baseFrameHeight: Float
                if (imageAspect > viewportAspect) {
                    baseFrameWidth = fittedHeight * viewportAspect
                    baseFrameHeight = fittedHeight
                } else {
                    baseFrameWidth = fittedWidth
                    baseFrameHeight = fittedWidth / viewportAspect
                }
                val zoom = cropScale.coerceIn(1f, 4f)
                val frameWidth = baseFrameWidth / zoom
                val frameHeight = baseFrameHeight / zoom
                val travelX = max(0f, fittedWidth - frameWidth) / 2f
                val travelY = max(0f, fittedHeight - frameHeight) / 2f
                val left = (imageLeft + (fittedWidth - frameWidth) / 2f - cropX.coerceIn(-1f, 1f) * travelX)
                    .coerceIn(imageLeft, imageLeft + fittedWidth - frameWidth)
                val top = (imageTop + (fittedHeight - frameHeight) / 2f - cropY.coerceIn(-1f, 1f) * travelY)
                    .coerceIn(imageTop, imageTop + fittedHeight - frameHeight)
                val right = left + frameWidth
                val bottom = top + frameHeight
                val shade = Color.Black.copy(alpha = 0.58f)

                // Dim everything outside the selected output frame.
                drawRect(shade, androidx.compose.ui.geometry.Offset(imageLeft, imageTop), androidx.compose.ui.geometry.Size(fittedWidth, max(0f, top - imageTop)))
                drawRect(shade, androidx.compose.ui.geometry.Offset(imageLeft, bottom), androidx.compose.ui.geometry.Size(fittedWidth, max(0f, imageTop + fittedHeight - bottom)))
                drawRect(shade, androidx.compose.ui.geometry.Offset(imageLeft, top), androidx.compose.ui.geometry.Size(max(0f, left - imageLeft), frameHeight))
                drawRect(shade, androidx.compose.ui.geometry.Offset(right, top), androidx.compose.ui.geometry.Size(max(0f, imageLeft + fittedWidth - right), frameHeight))
                drawRect(
                    color = Color.White,
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(frameWidth, frameHeight),
                    style = Stroke(width = 2.dp.toPx()),
                )
                // Corner markers make the crop boundary easy to spot.
                val corner = 14.dp.toPx()
                val stroke = 4.dp.toPx()
                drawLine(Color.White, androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(left + corner, top), stroke)
                drawLine(Color.White, androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(left, top + corner), stroke)
                drawLine(Color.White, androidx.compose.ui.geometry.Offset(right, top), androidx.compose.ui.geometry.Offset(right - corner, top), stroke)
                drawLine(Color.White, androidx.compose.ui.geometry.Offset(right, top), androidx.compose.ui.geometry.Offset(right, top + corner), stroke)
                drawLine(Color.White, androidx.compose.ui.geometry.Offset(left, bottom), androidx.compose.ui.geometry.Offset(left + corner, bottom), stroke)
                drawLine(Color.White, androidx.compose.ui.geometry.Offset(left, bottom), androidx.compose.ui.geometry.Offset(left, bottom - corner), stroke)
                drawLine(Color.White, androidx.compose.ui.geometry.Offset(right, bottom), androidx.compose.ui.geometry.Offset(right - corner, bottom), stroke)
                drawLine(Color.White, androidx.compose.ui.geometry.Offset(right, bottom), androidx.compose.ui.geometry.Offset(right, bottom - corner), stroke)
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Choose a background image to preview its crop", color = Color.White)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.82f))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("✥", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            androidx.compose.foundation.layout.Column {
                Text(
                    "FULL IMAGE • CROP AREA OUTLINED",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Drag to move the frame • Pinch to zoom",
                    color = Color.White.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
