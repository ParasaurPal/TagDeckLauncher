package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.system.LauncherNotification
import com.example.data.system.NotificationAccess
import com.example.data.system.NotificationBadgeStore
import java.text.DateFormat
import java.util.Date

@Composable
fun NotificationFeedWidget() {
    val context = LocalContext.current
    val notifications by NotificationBadgeStore.feed.collectAsState()
    val enabled = NotificationAccess.isEnabled(context)
    Column(
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xD91A2233), Color(0xB9121928), Color(0xA6263042))))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Notifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("${notifications.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (notifications.isNotEmpty()) TextButton(onClick = { NotificationBadgeStore.clearFeed() }) { Text("Clear all") }
        }
        if (!enabled) {
            Icon(Icons.Rounded.NotificationsOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Notification access is off", fontWeight = FontWeight.Medium)
            Text("Enable access to show notification content in this feed.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { runCatching { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }) { Text("Enable notification access") }
        } else if (notifications.isEmpty()) {
            Text("You're all caught up.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(notifications, key = { it.key }) { item -> NotificationFeedCard(item) }
            }
        }
    }
}

@Composable
private fun NotificationFeedCard(item: LauncherNotification) {
    Surface(color = Color(0x663B465A), contentColor = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().clickable {
            runCatching {
                context.packageManager.getLaunchIntentForPackage(item.packageName)?.let { context.startActivity(it) }
            }
        }) {
        Row(modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.appLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.postTime)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(3.dp))
                Text(item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (item.text.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(item.text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 4, overflow = TextOverflow.Ellipsis)
                }
                if (item.subText.isNotBlank()) Text(item.subText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { NotificationBadgeStore.dismiss(item.key) }) { Icon(Icons.Rounded.Close, contentDescription = "Dismiss notification") }
        }
    }
}
