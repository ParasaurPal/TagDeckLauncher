package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.db.AppWithTags
import com.example.data.db.BackgroundPresetEntity
import com.example.data.db.CategoryEntity
import com.example.data.db.HomeElementEntity
import com.example.data.db.ImageResourceEntity
import com.example.data.db.LauncherWindowEntity
import com.example.data.db.SidebarConfigEntity
import com.example.data.db.TagEntity
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

fun buildElementColorFilter(element: HomeElementEntity): ColorFilter? {
    val hasAdjustment = element.imageGrayscale ||
        element.imageBrightness != 0f ||
        element.imageContrast != 1f ||
        element.imageSaturation != 1f ||
        element.imageHueDeg != 0f ||
        element.imageTintHex != null

    if (!hasAdjustment) return null
    if (element.imageTintHex != null && !element.imageGrayscale && element.imageBrightness == 0f && element.imageContrast == 1f && element.imageSaturation == 1f) {
        return ColorFilter.tint(Color(element.imageTintHex).copy(alpha = 0.55f), androidx.compose.ui.graphics.BlendMode.SrcAtop)
    }

    val matrix = ColorMatrix()
    val sat = if (element.imageGrayscale) 0f else element.imageSaturation
    matrix.setToSaturation(sat)

    val c = element.imageContrast
    val t = (1f - c) / 2f * 255f + element.imageBrightness * 255f
    val contrastMatrix = ColorMatrix(
        floatArrayOf(
            c, 0f, 0f, 0f, t,
            0f, c, 0f, 0f, t,
            0f, 0f, c, 0f, t,
            0f, 0f, 0f, 1f, 0f,
        )
    )
    matrix.timesAssign(contrastMatrix)

    if (element.imageHueDeg != 0f) {
        val rad = Math.toRadians(element.imageHueDeg.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()
        val hueMatrix = ColorMatrix(
            floatArrayOf(
                0.213f + cosA * 0.787f - sinA * 0.213f, 0.715f - cosA * 0.715f - sinA * 0.715f, 0.072f - cosA * 0.072f + sinA * 0.928f, 0f, 0f,
                0.213f - cosA * 0.213f + sinA * 0.143f, 0.715f + cosA * 0.285f + sinA * 0.140f, 0.072f - cosA * 0.072f - sinA * 0.283f, 0f, 0f,
                0.213f - cosA * 0.213f - sinA * 0.787f, 0.715f - cosA * 0.715f + sinA * 0.715f, 0.072f + cosA * 0.928f + sinA * 0.072f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            )
        )
        matrix.timesAssign(hueMatrix)
    }

    return ColorFilter.colorMatrix(matrix)
}

@Composable
fun ImageLibraryDialog(
    resources: List<ImageResourceEntity>,
    onImportUri: suspend (Uri, String, String?) -> String?,
    onSelectPath: ((String) -> Unit)? = null,
    onRenameResource: (ImageResourceEntity, String, String) -> Unit,
    onDeleteResource: (ImageResourceEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var activeFolder by remember { mutableStateOf("All") }
    var editingRes by remember { mutableStateOf<ImageResourceEntity?>(null) }
    var targetFolderForImport by remember { mutableStateOf("Imported") }

    val folders = remember(resources) {
        listOf("All") + resources.map { it.folderName }.distinct().sorted()
    }
    val filtered = remember(resources, activeFolder) {
        if (activeFolder == "All") resources else resources.filter { it.folderName == activeFolder }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportUri(uri, targetFolderForImport, null)
                if (path != null && onSelectPath != null) {
                    onSelectPath(path)
                }
            }
        }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = onImportUri(uri, targetFolderForImport, null)
                if (path != null && onSelectPath != null) {
                    onSelectPath(path)
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Image, null, tint = MaterialTheme.colorScheme.primary)
                Text("Image Resource Library")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Originals are stored intact (PNG, JPG, WEBP, transparent PNG, SVG). Transforms remain non-destructive.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f).testTag("img_lib_photo_pick"),
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Gallery")
                    }
                    OutlinedButton(
                        onClick = { filePicker.launch(arrayOf("image/*", "image/svg+xml")) },
                        modifier = Modifier.weight(1f).testTag("img_lib_file_pick"),
                    ) {
                        Icon(Icons.Rounded.FolderOpen, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("SVG / File")
                    }
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    folders.forEach { folder ->
                        FilterChip(
                            selected = activeFolder == folder,
                            onClick = {
                                activeFolder = folder
                                if (folder != "All") targetFolderForImport = folder
                            },
                            label = { Text(folder) },
                        )
                    }
                }

                if (filtered.isEmpty()) {
                    Text(
                        "No images in \"$activeFolder\" yet. Import images above.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                    ) {
                        items(filtered, key = { it.imageId }) { img ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .clickable {
                                        onSelectPath?.invoke(img.localPath)
                                        if (onSelectPath == null) editingRes = img
                                    },
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(72.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x22FFFFFF)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (File(img.localPath).exists()) {
                                            AsyncImage(
                                                model = File(img.localPath),
                                                contentDescription = img.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        ) {
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.clickable { editingRes = img },
                                            ) {
                                                Icon(Icons.Rounded.Edit, null, tint = Color.White, modifier = Modifier.padding(3.dp).size(12.dp))
                                            }
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.6f),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.clickable { onDeleteResource(img) },
                                            ) {
                                                Icon(Icons.Rounded.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.padding(3.dp).size(12.dp))
                                            }
                                        }
                                    }
                                    Text(
                                        img.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                    Text(
                                        "${img.fileFormat} • ${img.widthPx}x${img.heightPx}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
    )

    editingRes?.let { res ->
        var newName by remember(res.imageId) { mutableStateOf(res.name) }
        var newFolder by remember(res.imageId) { mutableStateOf(res.folderName) }
        AlertDialog(
            onDismissRequest = { editingRes = null },
            title = { Text("Edit Resource Metadata") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(value = newFolder, onValueChange = { newFolder = it }, label = { Text("Folder") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    onRenameResource(res, newName, newFolder)
                    editingRes = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editingRes = null }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ObjectImageAndTransformDialog(
    element: HomeElementEntity,
    imageResources: List<ImageResourceEntity>,
    allApps: List<AppWithTags>,
    allTags: List<TagEntity>,
    categories: List<CategoryEntity>,
    sidebars: List<SidebarConfigEntity>,
    windows: List<LauncherWindowEntity>,
    onImportUri: suspend (Uri, String, String?) -> String?,
    onSave: (HomeElementEntity) -> Unit,
    onDismiss: () -> Unit,
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    var working by remember { mutableStateOf(element) }
    var showImageLibrary by remember { mutableStateOf(false) }

    val extraLayers = remember(working.extraImageLayersJson) {
        if (working.extraImageLayersJson.isBlank()) emptyList()
        else try {
            val arr = JSONArray(working.extraImageLayersJson)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transform, Image & Actions") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = tabIndex) {
                    Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Geometry") })
                    Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Image FX") })
                    Tab(selected = tabIndex == 2, onClick = { tabIndex = 2 }, text = { Text("Actions") })
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 12.dp),
                ) {
                    when (tabIndex) {
                        0 -> {
                            // Live mini preview
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x22FFFFFF)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .graphicsLayer {
                                            rotationZ = working.rotationDeg
                                            scaleX = working.scaleX * (if (working.flipHorizontal) -1f else 1f)
                                            scaleY = working.scaleY * (if (working.flipVertical) -1f else 1f)
                                            alpha = working.alpha
                                        }
                                        .clip(RoundedCornerShape(working.cornerRadiusDp.dp))
                                        .background(Color(working.backgroundColorHex))
                                        .border(working.borderWidthDp.dp, Color(working.borderColorHex), RoundedCornerShape(working.cornerRadiusDp.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (!working.imagePath.isNullOrBlank() && File(working.imagePath!!).exists()) {
                                        AsyncImage(
                                            model = File(working.imagePath!!),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            colorFilter = buildElementColorFilter(working),
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    } else {
                                        Text(working.label.take(4), color = Color(working.textColorHex), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            Text("Scale X (${(working.scaleX * 100).roundToInt()}%) & Y (${(working.scaleY * 100).roundToInt()}%)")
                            Slider(value = working.scaleX, onValueChange = { working = working.copy(scaleX = it) }, valueRange = 0.4f..2.0f)
                            Slider(value = working.scaleY, onValueChange = { working = working.copy(scaleY = it) }, valueRange = 0.4f..2.0f)

                            Text("Rotation: ${working.rotationDeg.roundToInt()}°")
                            Slider(value = working.rotationDeg, onValueChange = { working = working.copy(rotationDeg = it) }, valueRange = -180f..180f)

                            Text("Skew X (${working.skewX.roundToInt()}°) / Skew Y (${working.skewY.roundToInt()}°)")
                            Slider(value = working.skewX, onValueChange = { working = working.copy(skewX = it) }, valueRange = -35f..35f)
                            Slider(value = working.skewY, onValueChange = { working = working.copy(skewY = it) }, valueRange = -35f..35f)

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = working.flipHorizontal,
                                    onClick = { working = working.copy(flipHorizontal = !working.flipHorizontal) },
                                    label = { Text("Flip H") },
                                    leadingIcon = { Icon(Icons.Rounded.Flip, null, Modifier.size(16.dp)) },
                                )
                                FilterChip(
                                    selected = working.flipVertical,
                                    onClick = { working = working.copy(flipVertical = !working.flipVertical) },
                                    label = { Text("Flip V") },
                                )
                            }
                        }

                        1 -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { showImageLibrary = true }, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Rounded.Image, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (working.imagePath == null) "Pick Image" else "Change Image")
                                }
                                if (working.imagePath != null) {
                                    OutlinedButton(onClick = { working = working.copy(imagePath = null) }) {
                                        Text("Clear")
                                    }
                                }
                            }

                            Text("Scale / Fit Mode", style = MaterialTheme.typography.labelLarge)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("CROP", "FIT", "FILL_XY", "ORIGINAL").forEach { mode ->
                                    FilterChip(
                                        selected = working.imageScaleType == mode,
                                        onClick = { working = working.copy(imageScaleType = mode) },
                                        label = { Text(mode) },
                                    )
                                }
                            }

                            Text("Mask Shape", style = MaterialTheme.typography.labelLarge)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("NONE", "CIRCLE", "SQUIRCLE", "DIAMOND", "HEXAGON").forEach { mask ->
                                    FilterChip(
                                        selected = working.imageMaskShape == mask,
                                        onClick = { working = working.copy(imageMaskShape = mask) },
                                        label = { Text(mask) },
                                    )
                                }
                            }

                            Text("Brightness: ${(working.imageBrightness * 100).roundToInt()}")
                            Slider(value = working.imageBrightness, onValueChange = { working = working.copy(imageBrightness = it) }, valueRange = -0.5f..0.5f)
                            Text("Contrast: ${String.format("%.2f", working.imageContrast)}x")
                            Slider(value = working.imageContrast, onValueChange = { working = working.copy(imageContrast = it) }, valueRange = 0.4f..1.8f)
                            Text("Saturation: ${String.format("%.2f", working.imageSaturation)}x")
                            Slider(value = working.imageSaturation, onValueChange = { working = working.copy(imageSaturation = it) }, valueRange = 0f..2f)
                            Text("Hue Shift: ${working.imageHueDeg.roundToInt()}°")
                            Slider(value = working.imageHueDeg, onValueChange = { working = working.copy(imageHueDeg = it) }, valueRange = -180f..180f)
                            Text("Blur: ${working.imageBlurDp.roundToInt()}dp")
                            Slider(value = working.imageBlurDp, onValueChange = { working = working.copy(imageBlurDp = it) }, valueRange = 0f..24f)

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Grayscale Mode")
                                Switch(checked = working.imageGrayscale, onCheckedChange = { working = working.copy(imageGrayscale = it) })
                            }

                            Text("Overlay Image Layers (${extraLayers.size})", style = MaterialTheme.typography.labelLarge)
                            Text(
                                "Stack multiple transparent PNG/SVG layers on top of this object.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = {
                                    val pickFirst = imageResources.firstOrNull()?.localPath ?: working.imagePath
                                    if (pickFirst != null) {
                                        val next = JSONArray(extraLayers + pickFirst).toString()
                                        working = working.copy(extraImageLayersJson = next)
                                    } else {
                                        showImageLibrary = true
                                    }
                                }) {
                                    Icon(Icons.Rounded.Add, null, Modifier.size(16.dp))
                                    Text("Add Layer")
                                }
                                if (extraLayers.isNotEmpty()) {
                                    OutlinedButton(onClick = { working = working.copy(extraImageLayersJson = "") }) {
                                        Text("Clear Layers")
                                    }
                                }
                            }
                        }

                        2 -> {
                            Text("Per-Gesture Action Assignment", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            var selectedTrigger by remember { mutableStateOf("TAP") }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("TAP", "DOUBLE_TAP", "LONG_PRESS", "SWIPE_UP", "SWIPE_DOWN", "SWIPE_LEFT", "SWIPE_RIGHT").forEach { trig ->
                                    FilterChip(
                                        selected = selectedTrigger == trig,
                                        onClick = { selectedTrigger = trig },
                                        label = { Text(trig.replace('_', ' ')) },
                                    )
                                }
                            }

                            val rootObj = remember(working.actionsConfigJson) {
                                try {
                                    if (working.actionsConfigJson.isBlank()) JSONObject() else JSONObject(working.actionsConfigJson)
                                } catch (_: Exception) {
                                    JSONObject()
                                }
                            }
                            val currentActionObj = rootObj.optJSONObject(selectedTrigger) ?: JSONObject()
                            var actType by remember(selectedTrigger, working.actionsConfigJson) {
                                mutableStateOf(currentActionObj.optString("actionType", "NONE"))
                            }
                            var actUrl by remember(selectedTrigger, working.actionsConfigJson) {
                                mutableStateOf(currentActionObj.optString("targetUrl", ""))
                            }
                            var actPkg by remember(selectedTrigger, working.actionsConfigJson) {
                                mutableStateOf(currentActionObj.optString("targetPackage", ""))
                            }
                            var actTagId by remember(selectedTrigger, working.actionsConfigJson) {
                                mutableStateOf(if (currentActionObj.has("targetTagId")) currentActionObj.optLong("targetTagId") else null)
                            }
                            var actCatId by remember(selectedTrigger, working.actionsConfigJson) {
                                mutableStateOf(if (currentActionObj.has("targetCategoryId")) currentActionObj.optLong("targetCategoryId") else null)
                            }
                            var actWinId by remember(selectedTrigger, working.actionsConfigJson) {
                                mutableStateOf(if (currentActionObj.has("targetWindowId")) currentActionObj.optLong("targetWindowId") else null)
                            }

                            fun persistTriggerAction() {
                                val nextRoot = try {
                                    if (working.actionsConfigJson.isBlank()) JSONObject() else JSONObject(working.actionsConfigJson)
                                } catch (_: Exception) { JSONObject() }
                                nextRoot.put(selectedTrigger, JSONObject().apply {
                                    put("actionType", actType)
                                    if (actPkg.isNotBlank()) put("targetPackage", actPkg)
                                    actTagId?.let { put("targetTagId", it) }
                                    actCatId?.let { put("targetCategoryId", it) }
                                    actWinId?.let { put("targetWindowId", it) }
                                    if (actUrl.isNotBlank()) put("targetUrl", actUrl)
                                })
                                working = working.copy(actionsConfigJson = nextRoot.toString())
                            }

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    "NONE" to "Default",
                                    "OPEN_APP" to "App",
                                    "OPEN_TAG" to "Tag",
                                    "OPEN_CATEGORY" to "Category",
                                    "OPEN_WINDOW" to "Window",
                                    "OPEN_DRAWER" to "Drawer",
                                    "OPEN_SEARCH" to "Search",
                                    "OPEN_WEBSITE" to "Website",
                                    "OPEN_SYSTEM_SETTING" to "Settings",
                                    "NEXT_PAGE" to "Next Pg",
                                    "PREV_PAGE" to "Prev Pg",
                                ).forEach { (k, lbl) ->
                                    FilterChip(
                                        selected = actType == k,
                                        onClick = {
                                            actType = k
                                            persistTriggerAction()
                                        },
                                        label = { Text(lbl) },
                                    )
                                }
                            }

                            when (actType) {
                                "OPEN_WEBSITE" -> {
                                    OutlinedTextField(
                                        value = actUrl,
                                        onValueChange = {
                                            actUrl = it
                                            persistTriggerAction()
                                        },
                                        label = { Text("URL (https://...)") },
                                        singleLine = true,
                                    )
                                }
                                "OPEN_TAG" -> {
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        allTags.forEach { t ->
                                            FilterChip(
                                                selected = actTagId == t.tagId,
                                                onClick = {
                                                    actTagId = t.tagId
                                                    persistTriggerAction()
                                                },
                                                label = { Text(t.name) },
                                            )
                                        }
                                    }
                                }
                                "OPEN_CATEGORY" -> {
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        categories.take(14).forEach { c ->
                                            FilterChip(
                                                selected = actCatId == c.categoryId,
                                                onClick = {
                                                    actCatId = c.categoryId
                                                    persistTriggerAction()
                                                },
                                                label = { Text(c.name) },
                                            )
                                        }
                                    }
                                }
                                "OPEN_WINDOW" -> {
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        windows.forEach { w ->
                                            FilterChip(
                                                selected = actWinId == w.windowId,
                                                onClick = {
                                                    actWinId = w.windowId
                                                    persistTriggerAction()
                                                },
                                                label = { Text(w.title) },
                                            )
                                        }
                                    }
                                }
                                "OPEN_APP" -> {
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        allApps.take(12).forEach { a ->
                                            FilterChip(
                                                selected = actPkg == a.app.packageName,
                                                onClick = {
                                                    actPkg = a.app.packageName
                                                    persistTriggerAction()
                                                },
                                                label = { Text(a.app.displayName, maxLines = 1) },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(working) }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )

    if (showImageLibrary) {
        ImageLibraryDialog(
            resources = imageResources,
            onImportUri = onImportUri,
            onSelectPath = { path ->
                working = working.copy(imagePath = path)
                showImageLibrary = false
            },
            onRenameResource = { _, _, _ -> },
            onDeleteResource = {},
            onDismiss = { showImageLibrary = false },
        )
    }
}

@Composable
fun BackgroundPresetDialog(
    presets: List<BackgroundPresetEntity>,
    onApplyPreset: (BackgroundPresetEntity) -> Unit,
    onSaveCurrentAsPreset: (String) -> Unit,
    onDeletePreset: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var presetName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Background Presets") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        label = { Text("Save Current As Preset") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        enabled = presetName.isNotBlank(),
                        onClick = {
                            onSaveCurrentAsPreset(presetName.trim())
                            presetName = ""
                        },
                    ) { Text("Save") }
                }
                if (presets.isEmpty()) {
                    Text("No saved presets yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.heightIn(max = 260.dp).verticalScroll(rememberScrollState()),
                    ) {
                        presets.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth().clickable { onApplyPreset(preset) },
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(preset.colorHex)),
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(preset.name, fontWeight = FontWeight.SemiBold)
                                        Text("${preset.backgroundType} • ${preset.ownerScope}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(onClick = { onDeletePreset(preset.presetId) }) {
                                        Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}
