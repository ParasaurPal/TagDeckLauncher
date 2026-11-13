package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.AppEntity
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.LineIconPoint
import com.example.data.system.LineIconProcessor
import com.example.data.system.LineIconProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun LineIconStudioDialog(
    app: AppEntity,
    appDiscovery: AppDiscoveryManager,
    iconPackOrder: List<String>,
    initialProfile: LineIconProfile?,
    onSave: (LineIconProfile?) -> Unit,
    onDismiss: () -> Unit,
) {
    BackHandler(onBack = onDismiss)
    val sourceIcon: ImageBitmap? by produceState<ImageBitmap?>(
        initialValue = null,
        app.packageName,
        app.componentName,
        app.customIconPath,
        iconPackOrder,
    ) {
        value = app.customIconPath?.let { path ->
            withContext(Dispatchers.IO) {
                runCatching { BitmapFactory.decodeFile(File(path).absolutePath)?.asImageBitmap() }.getOrNull()
            }
        }
        if (value == null) {
            value = appDiscovery.iconPackManager.getCachedIconFromHierarchy(
                app.packageName, app.componentName, iconPackOrder
            ) ?: appDiscovery.iconPackManager.resolveIconFromHierarchy(
                app.packageName, app.componentName, iconPackOrder
            ) ?: appDiscovery.getCachedIcon(app.packageName)
        }
    }
    val edgeSnapper = remember(sourceIcon) { sourceIcon?.let { LineIconProcessor.createSnapper(it) } }
    var profile by remember(app.packageName, initialProfile) {
        mutableStateOf(initialProfile ?: LineIconProfile())
    }
    var strokes by remember(app.packageName, initialProfile) { mutableStateOf(profile.strokes) }
    val autoLinePreview: ImageBitmap? by produceState<ImageBitmap?>(null, sourceIcon, profile) {
        value = if (sourceIcon == null || !profile.autoEdgesEnabled) {
            null
        } else {
            withContext(Dispatchers.Default) {
                runCatching { LineIconProcessor.render(sourceIcon!!, profile.copy(strokes = emptyList())).asImageBitmap() }.getOrNull()
            }
        }
    }
    var activeStroke by remember { mutableStateOf<List<LineIconPoint>>(emptyList()) }
    var showOriginalUnderlay by remember { mutableStateOf(true) }

    fun pointFrom(offset: Offset, width: Float, height: Float): LineIconPoint {
        var point = LineIconPoint(
            (offset.x / width.coerceAtLeast(1f)).coerceIn(0f, 1f),
            (offset.y / height.coerceAtLeast(1f)).coerceIn(0f, 1f),
        )
        if (profile.snapToEdges) point = edgeSnapper?.invoke(point) ?: point
        return point
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Line Icon Studio", style = MaterialTheme.typography.headlineSmall)
                        Text(app.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = onDismiss) { Text("Close") }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        .background(Color(0xFF252532), RoundedCornerShape(22.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (sourceIcon == null) {
                        Text("Loading app icon…", color = Color.White)
                    } else {
                        Canvas(
                            modifier = Modifier.fillMaxSize().pointerInput(
                                profile.snapToEdges, sourceIcon
                            ) {
                                detectDragGestures(
                                    onDragStart = { start ->
                                        activeStroke = listOf(pointFrom(start, size.width.toFloat(), size.height.toFloat()))
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val next = pointFrom(change.position, size.width.toFloat(), size.height.toFloat())
                                        val last = activeStroke.lastOrNull()
                                        if (last == null || kotlin.math.hypot(next.x - last.x, next.y - last.y) > 0.002f) {
                                            activeStroke = activeStroke + next
                                        }
                                    },
                                    onDragEnd = {
                                        if (activeStroke.isNotEmpty()) strokes = strokes + listOf(activeStroke)
                                        activeStroke = emptyList()
                                    },
                                    onDragCancel = { activeStroke = emptyList() },
                                )
                            },
                        ) {
                            if (showOriginalUnderlay) {
                                drawImage(
                                    sourceIcon!!,
                                    dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                                    alpha = 0.18f,
                                )
                            }
                            if (profile.autoEdgesEnabled && edgePreview != null) {
                                drawImage(
                                    edgePreview!!,
                                    dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                                    colorFilter = ColorFilter.tint(Color(profile.lineColorHex)),
                                )
                            }
                            val allStrokes = strokes + listOfNotNull(activeStroke.takeIf { it.isNotEmpty() })
                            allStrokes.forEach { points ->
                                if (points.size < 2) return@forEach
                                val path = Path().apply {
                                    moveTo(points.first().x * size.width, points.first().y * size.height)
                                    points.drop(1).forEach { lineTo(it.x * size.width, it.y * size.height) }
                                }
                                if (profile.outlineEnabled) {
                                    drawPath(
                                        path,
                                        color = Color(profile.outlineColorHex),
                                        style = Stroke(
                                            width = (profile.lineWidthPx + profile.outlineWidthPx * 2f) * size.width / 192f,
                                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                            join = androidx.compose.ui.graphics.StrokeJoin.Round,
                                        ),
                                    )
                                }
                                drawPath(
                                    path,
                                    color = Color(profile.lineColorHex),
                                    style = Stroke(
                                        width = profile.lineWidthPx * size.width / 192f,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                        join = androidx.compose.ui.graphics.StrokeJoin.Round,
                                    ),
                                )
                            }
                        }
                    }
                }

                Text("Draw directly over the icon. Snapping pulls your strokes onto detected edges.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = profile.lineColorHex == 0xFFFFFFFFL,
                        onClick = { profile = profile.copy(lineColorHex = 0xFFFFFFFFL, outlineColorHex = 0xFF000000L) },
                        label = { Text("White lines") },
                    )
                    FilterChip(
                        selected = profile.lineColorHex == 0xFF000000L,
                        onClick = { profile = profile.copy(lineColorHex = 0xFF000000L, outlineColorHex = 0xFFFFFFFFL) },
                        label = { Text("Black lines") },
                    )
                }
                SettingSwitch("Auto-detect icon edges", profile.autoEdgesEnabled) {
                    profile = profile.copy(autoEdgesEnabled = it)
                }
                SettingSwitch("Snap drawn lines to edges", profile.snapToEdges) {
                    profile = profile.copy(snapToEdges = it)
                }
                SettingSwitch("Add a thin contrast outline", profile.outlineEnabled) {
                    profile = profile.copy(outlineEnabled = it)
                }
                if (profile.outlineEnabled) {
                    Text("Outline thickness: ${"%.1f".format(profile.outlineWidthPx)} px")
                    Slider(
                        value = profile.outlineWidthPx,
                        onValueChange = { profile = profile.copy(outlineWidthPx = it) },
                        valueRange = 0.5f..3f,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = profile.outlineColorHex == 0xFF000000L,
                            onClick = { profile = profile.copy(outlineColorHex = 0xFF000000L) },
                            label = { Text("Black outline") },
                        )
                        FilterChip(
                            selected = profile.outlineColorHex == 0xFFFFFFFFL,
                            onClick = { profile = profile.copy(outlineColorHex = 0xFFFFFFFFL) },
                            label = { Text("White outline") },
                        )
                    }
                }
                Text("Line thickness: ${"%.1f".format(profile.lineWidthPx)} px")
                Slider(
                    value = profile.lineWidthPx,
                    onValueChange = { profile = profile.copy(lineWidthPx = it) },
                    valueRange = 0.7f..4f,
                )
                SettingSwitch("Show original icon faintly while drawing", showOriginalUnderlay) {
                    showOriginalUnderlay = it
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        if (strokes.isNotEmpty()) strokes = strokes.dropLast(1)
                    }, modifier = Modifier.weight(1f)) { Text("Undo line") }
                    OutlinedButton(onClick = { strokes = emptyList(); activeStroke = emptyList() }, modifier = Modifier.weight(1f)) { Text("Clear drawing") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onSave(null) }, modifier = Modifier.weight(1f)) {
                        Text("Remove line icon")
                    }
                    Button(
                        onClick = { onSave(profile.copy(strokes = strokes)) },
                        modifier = Modifier.weight(1f),
                        enabled = sourceIcon != null,
                    ) { Text("Save icon") }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
