package com.example.data.system

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.data.db.CustomFontEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object CustomFontRegistry {
    private val cache = ConcurrentHashMap<String, FontFamily>()

    fun resolveFontFamily(localPath: String?): FontFamily? {
        if (localPath.isNullOrBlank()) return null
        cache[localPath]?.let { return it }
        val file = File(localPath)
        if (!file.exists() || !file.isFile) return null
        return try {
            // Verify Android Typeface can decode it cleanly before registering
            Typeface.createFromFile(file)
            val family = FontFamily(
                Font(file, weight = FontWeight.Normal),
                Font(file, weight = FontWeight.Medium),
                Font(file, weight = FontWeight.SemiBold),
                Font(file, weight = FontWeight.Bold),
            )
            cache[localPath] = family
            family
        } catch (_: Throwable) {
            null
        }
    }

    fun resolveFontFamilyByRef(
        fontRef: String,
        customFonts: List<CustomFontEntity>,
    ): FontFamily? {
        if (!fontRef.startsWith("CUSTOM:")) return null
        val id = fontRef.removePrefix("CUSTOM:").toLongOrNull() ?: return null
        val entity = customFonts.find { it.fontId == id } ?: return null
        return resolveFontFamily(entity.localPath)
    }

    fun invalidate(localPath: String) {
        cache.remove(localPath)
    }
}

class FontStorageManager(private val context: Context) {

    private val fontsDir: File
        get() = File(context.filesDir, "custom_fonts").apply { if (!exists()) mkdirs() }

    suspend fun importFontFromUri(uri: Uri, preferredName: String? = null): CustomFontEntity? = withContext(Dispatchers.IO) {
        try {
            val rawDisplayName = queryFileName(uri) ?: "Custom Font"
            val ext = rawDisplayName.substringAfterLast('.', "").lowercase()
            val normalizedFormat = when (ext) {
                "otf" -> "OTF"
                "ttf" -> "TTF"
                else -> "TTF"
            }
            val cleanBase = (preferredName?.takeIf { it.isNotBlank() } ?: rawDisplayName.substringBeforeLast('.'))
                .trim()
                .ifBlank { "Custom Font" }

            val targetFile = File(fontsDir, "font_${UUID.randomUUID()}.${normalizedFormat.lowercase()}")
            context.contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null

            if (targetFile.length() < 64L || targetFile.length() > 25L * 1024L * 1024L) {
                targetFile.delete()
                return@withContext null
            }

            // Strict validation with Typeface.createFromFile
            try {
                Typeface.createFromFile(targetFile)
            } catch (_: Throwable) {
                targetFile.delete()
                return@withContext null
            }

            CustomFontEntity(
                name = cleanBase,
                fileFormat = normalizedFormat,
                localPath = targetFile.absolutePath,
            )
        } catch (_: Throwable) {
            null
        }
    }

    fun encodeFontBase64IfSmall(localPath: String, maxBytes: Int = 4 * 1024 * 1024): String? {
        return try {
            val f = File(localPath)
            if (!f.exists() || f.length() > maxBytes) return null
            Base64.encodeToString(f.readBytes(), Base64.NO_WRAP)
        } catch (_: Throwable) {
            null
        }
    }

    fun restoreFontFromBase64(name: String, fileFormat: String, base64: String): String? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val ext = if (fileFormat.equals("OTF", ignoreCase = true)) "otf" else "ttf"
            val outFile = File(fontsDir, "font_${UUID.randomUUID()}.$ext")
            outFile.writeBytes(bytes)
            Typeface.createFromFile(outFile)
            outFile.absolutePath
        } catch (_: Throwable) {
            null
        }
    }

    fun deleteFontFile(localPath: String) {
        try {
            CustomFontRegistry.invalidate(localPath)
            File(localPath).delete()
        } catch (_: Throwable) {
        }
    }

    private fun queryFileName(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        } catch (_: Throwable) {
            uri.lastPathSegment
        }
    }
}
