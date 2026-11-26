package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class PhoneSearchHit(val id: String, val title: String, val detail: String, val uri: Uri, val mimeType: String)

internal fun toT9Digits(value: String): String = buildString {
    value.lowercase().forEach { char ->
        append(when (char) {
            in "abc" -> "2"; in "def" -> "3"; in "ghi" -> "4"; in "jkl" -> "5"
            in "mno" -> "6"; in "pqrs" -> "7"; in "tuv" -> "8"; in "wxyz" -> "9"
            else -> if (char.isDigit()) char.toString() else ""
        })
    }
}

private fun matchesT9(text: String, query: String): Boolean {
    if (query.isBlank()) return false
    return if (query.all(Char::isDigit)) toT9Digits(text).contains(query)
    else text.contains(query, ignoreCase = true) || toT9Digits(text).contains(toT9Digits(query))
}

@Composable
internal fun PhoneSearchResults(query: String, scope: String, onOpen: (Uri, String) -> Unit) {
    val context = LocalContext.current
    val wantsContacts = scope == "ALL" || scope == "CONTACTS"
    val wantsFiles = scope == "ALL" || scope == "FILES"
    val contactsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    val filePermissions = remember {
        if (Build.VERSION.SDK_INT >= 33) listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
        else listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val hasFilePermission = filePermissions.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    var contacts by remember { mutableStateOf(emptyList<PhoneSearchHit>()) }
    var files by remember { mutableStateOf(emptyList<PhoneSearchHit>()) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    LaunchedEffect(query, wantsContacts, wantsFiles, contactsPermission, hasFilePermission) {
        if (query.isBlank()) {
            contacts = emptyList(); files = emptyList()
        } else withContext(Dispatchers.IO) {
            if (wantsContacts && contactsPermission) {
                val found = mutableListOf<PhoneSearchHit>()
                runCatching {
                    val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER)
                    context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC")?.use { cursor ->
                        val idCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                        val nameCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val numberCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val seen = hashSetOf<String>()
                        while (cursor.moveToNext() && found.size < 30) {
                            val id = cursor.getString(idCol) ?: continue
                            val name = cursor.getString(nameCol).orEmpty()
                            val number = cursor.getString(numberCol).orEmpty()
                            val digits = query.filter(Char::isDigit)
                            if (seen.add(id) && (matchesT9(name, query) || (digits.isNotEmpty() && number.filter(Char::isDigit).contains(digits)))) {
                                found += PhoneSearchHit(id, name, number, Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id), "vnd.android.cursor.item/contact")
                            }
                        }
                    }
                }
                contacts = found
            } else contacts = emptyList()

            if (wantsFiles && hasFilePermission) {
                val found = mutableListOf<PhoneSearchHit>()
                runCatching {
                    val collection = MediaStore.Files.getContentUri("external")
                    val pathColumn = if (Build.VERSION.SDK_INT >= 29) MediaStore.Files.FileColumns.RELATIVE_PATH else MediaStore.Files.FileColumns.DATA
                    val projection = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.MIME_TYPE, pathColumn)
                    context.contentResolver.query(collection, projection, null, null, MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC")?.use { cursor ->
                        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                        val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                        val pathCol = cursor.getColumnIndex(pathColumn)
                        while (cursor.moveToNext() && found.size < 40) {
                            val name = cursor.getString(nameCol).orEmpty()
                            if (!matchesT9(name, query)) continue
                            val id = cursor.getLong(idCol)
                            val mime = cursor.getString(mimeCol) ?: "*/*"
                            val path = if (pathCol >= 0) cursor.getString(pathCol).orEmpty() else ""
                            found += PhoneSearchHit(id.toString(), name, path, Uri.withAppendedPath(collection, id.toString()), mime)
                        }
                    }
                }
                files = found
            } else files = emptyList()
        }
    }

    if (query.isNotBlank() && (wantsContacts || wantsFiles)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (wantsContacts) {
                Text("Contacts", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                if (!contactsPermission) PermissionSearchCard("Allow contact search", "Grant Contacts access to find people by name or phone number.") { permissionLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS)) }
                else if (contacts.isEmpty()) Text("No matching contacts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                else contacts.forEach { hit -> SearchHitRow(hit.title, hit.detail) { onOpen(hit.uri, hit.mimeType) } }
            }
            if (wantsFiles) {
                Text("Files on this device", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                if (!hasFilePermission) PermissionSearchCard("Allow file search", "Grant media access to search indexed shared-storage files. Android restricts access to other documents and app-private folders.") { permissionLauncher.launch(filePermissions.toTypedArray()) }
                else if (files.isEmpty()) Text("No matching accessible files", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                else files.forEach { hit -> SearchHitRow(hit.title, hit.detail) { onOpen(hit.uri, hit.mimeType) } }
            }
        }
    }
}

@Composable
private fun PermissionSearchCard(title: String, description: String, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onClick) { Text("Grant access") }
        }
    }
}

@Composable
private fun SearchHitRow(title: String, detail: String, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onClick) { Text("Open") }
        }
    }
}
