package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContactPage
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class DeviceContactResult(val id: Long, val lookupKey: String, val name: String, val phones: List<String>)
private data class DeviceFileResult(val uri: Uri, val name: String)

@Composable
fun DeviceContentSearchSection(query: String, scope: String) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("tagdeck_device_search", 0) }
    val contactsAllowed = remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED)
    }
    var contacts by remember { mutableStateOf<List<DeviceContactResult>>(emptyList()) }
    var files by remember { mutableStateOf<List<DeviceFileResult>>(emptyList()) }
    var fileRefresh by remember { mutableIntStateOf(0) }

    val requestContacts = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        contactsAllowed.value = it
    }
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val existing = prefs.getStringSet("saved_uris", emptySet()).orEmpty().toMutableSet()
            existing.add(uri.toString())
            prefs.edit().putStringSet("saved_uris", existing).apply()
            fileRefresh++
        }
    }

    LaunchedEffect(query, contactsAllowed.value, scope) {
        if (scope !in listOf("ALL", "CONTACTS") || query.isBlank() || !contactsAllowed.value) {
            contacts = emptyList()
        } else {
            contacts = withContext(Dispatchers.IO) {
                runCatching {
                    val result = mutableListOf<DeviceContactResult>()
                    val resolver = context.contentResolver
                    // Read phone rows once, rather than issuing one provider query per contact.
                    val phonesByContact = mutableMapOf<Long, MutableList<String>>()
                    resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ),
                        null, null, null,
                    )?.use { phoneCursor ->
                        val contactIdCol = phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                        val numberCol = phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        while (phoneCursor.moveToNext()) {
                            val contactId = phoneCursor.getLong(contactIdCol)
                            val number = phoneCursor.getString(numberCol).orEmpty()
                            phonesByContact.getOrPut(contactId) { mutableListOf() }.add(number)
                        }
                    }
                    resolver.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY,
                            ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER),
                        null, null, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE NOCASE ASC"
                    )?.use { cursor ->
                        val idCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                        val keyCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)
                        val nameCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
                        val phoneCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        while (cursor.moveToNext() && result.size < 250) {
                            val id = cursor.getLong(idCol)
                            val key = cursor.getString(keyCol) ?: continue
                            val name = cursor.getString(nameCol) ?: continue
                            val phones = if (cursor.getInt(phoneCol) > 0) {
                                phonesByContact[id].orEmpty()
                            } else emptyList()
                            val matches = if (query.all { it.isDigit() }) {
                                T9Search.matches(query, name) || phones.any { T9Search.matches(query, it) }
                            } else {
                                val normalized = query.trim().lowercase()
                                name.lowercase().contains(normalized) || phones.any { it.contains(normalized, true) }
                            }
                            if (matches) result.add(DeviceContactResult(id, key, name, phones))
                            if (result.size >= 18) break
                        }
                    }
                    result
                }.getOrDefault(emptyList())
            }
        }
    }

    LaunchedEffect(query, scope, prefs, fileRefresh) {
        if (scope !in listOf("ALL", "FILES")) {
            files = emptyList()
        } else {
            files = withContext(Dispatchers.IO) {
                prefs.getStringSet("saved_uris", emptySet()).orEmpty().mapNotNull { raw ->
                    runCatching {
                        val uri = Uri.parse(raw)
                        var name = uri.lastPathSegment ?: "Saved file"
                        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                            if (c.moveToFirst()) name = c.getString(0) ?: name
                        }
                        DeviceFileResult(uri, name)
                    }.getOrNull()
                }.filter { query.isBlank() || it.name.contains(query.trim(), true) ||
                    (query.all { ch -> ch.isDigit() } && T9Search.matches(query, it.name)) }.take(30)
            }
        }
    }

    if (scope in listOf("ALL", "CONTACTS")) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Rounded.ContactPage, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
            Text("Contacts", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            if (!contactsAllowed.value) {
                AssistChip(onClick = { requestContacts.launch(Manifest.permission.READ_CONTACTS) },
                    label = { Text("Allow contact search") }, leadingIcon = {
                        Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    })
            }
        }
        if (!contactsAllowed.value) {
            Text("Contacts stay on this device. Access is only requested when you enable contact search.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else contacts.forEach { contact ->
            Surface(shape = RoundedCornerShape(14.dp), tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().clickable {
                    val uri = ContactsContract.Contacts.getLookupUri(contact.id, contact.lookupKey)
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ContactPage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(contact.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (contact.phones.isNotEmpty()) Text(contact.phones.first(), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (scope in listOf("ALL", "FILES")) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Rounded.FolderOpen, tint = MaterialTheme.colorScheme.primary, contentDescription = null)
            Text("Your Files", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            AssistChip(onClick = { pickFile.launch(arrayOf("*/*")) }, label = { Text("Add files") })
        }
        Text("Search files you choose here. TagDeck only sees files you explicitly add.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        files.forEach { file ->
            Surface(shape = RoundedCornerShape(14.dp), tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().clickable {
                    val intent = Intent(Intent.ACTION_VIEW).setDataAndType(file.uri, context.contentResolver.getType(file.uri) ?: "*/*")
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    runCatching { context.startActivity(intent) }
                }) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(file.name, modifier = Modifier.weight(1f).padding(start = 10.dp),
                        fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
