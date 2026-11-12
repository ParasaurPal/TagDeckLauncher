package com.example.data.system

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.webkit.MimeTypeMap
import com.example.data.db.ImageResourceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ImageStorageManager(private val context: Context) {

    private val imagesDir: File
        get() = File(context.filesDir, "imported_images").apply {
            if (!exists()) mkdirs()
        }

    /**
     * Imports a user-selected image (PNG, JPG, WEBP, SVG) into app-private storage,
     * preserving the original imported file untouched and never overwriting it during edits.
     */
    suspend fun importImagePreservingOriginal(uri: Uri): String? = withContext(Dispatchers.IO) {
        importImageResource(uri)?.localPath
    }

    suspend fun importImageResource(
        uri: Uri,
        name: String? = null,
        folderName: String = "Imported",
    ): ImageResourceEntity? = withContext(Dispatchers.IO) {
        try {
            val mimeType = context.contentResolver.getType(uri)
            val ext = when {
                mimeType?.contains("svg", ignoreCase = true) == true -> "svg"
                mimeType?.contains("png", ignoreCase = true) == true -> "png"
                mimeType?.contains("webp", ignoreCase = true) == true -> "webp"
                mimeType?.contains("jpeg", ignoreCase = true) == true ||
                    mimeType?.contains("jpg", ignoreCase = true) == true -> "jpg"
                else -> MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "png"
            }
            val uuid = UUID.randomUUID().toString().take(8)
            val destFile = File(imagesDir, "orig_${System.currentTimeMillis()}_$uuid.$ext")
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            if (ext != "svg") {
                BitmapFactory.decodeFile(destFile.absolutePath, bounds)
            }

            val resolvedName = name?.takeIf { it.isNotBlank() }
                ?: uri.lastPathSegment?.substringAfterLast('/')?.take(40)
                ?: "Image $uuid"

            ImageResourceEntity(
                name = resolvedName,
                folderName = folderName,
                localPath = destFile.absolutePath,
                sourceUri = uri.toString(),
                fileFormat = ext.uppercase(),
                widthPx = bounds.outWidth.coerceAtLeast(0),
                heightPx = bounds.outHeight.coerceAtLeast(0),
            )
        } catch (_: Exception) {
            null
        }
    }

    fun encodeImageBase64IfSmall(localPath: String, maxBytes: Int = 512 * 1024): String? {
        return try {
            val f = File(localPath)
            if (f.exists() && f.length() <= maxBytes) {
                Base64.encodeToString(f.readBytes(), Base64.NO_WRAP)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun restoreImageFromBase64(fileNameHint: String, ext: String, base64: String): String? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val dest = File(imagesDir, "restored_${fileNameHint}_${UUID.randomUUID().toString().take(6)}.${ext.lowercase()}")
            dest.writeBytes(bytes)
            dest.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun deleteImageFile(localPath: String) {
        try {
            File(localPath).takeIf { it.exists() && it.parentFile == imagesDir }?.delete()
        } catch (_: Exception) {
        }
    }
}
