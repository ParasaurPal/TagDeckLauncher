package com.example.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.system.LauncherNotification
import com.example.data.system.NotificationAccess
import com.example.data.system.NotificationBadgeStore
import com.example.data.system.TagDeckNotificationListenerService
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterOverlay(
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val notifications by NotificationBadgeStore.notifications.collectAsState()
    val accessEnabled = NotificationAccess.isEnabled(context)

    Box(
        modifier = Modifier
            .padding(top = 54.dp, end = 14.dp),
        contentAlignment = Alignment.TopEnd,
    ) {
        Surface(
            onClick = { onVisibleChange(true) },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            tonalElevation = 5.dp,
            shadowElevation = 5.dp,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(46.dp)) {
                Icon(Icons.Rounded.Notifications, contentDescription = "Open notification center")
                if (notifications.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.TopEnd).size(18.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = notifications.size.coerceAtMost(99).toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }

    if (visible) {
        ModalBottomSheet(onDismissRequest = { onVisibleChange(false) }) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("Notifications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        if (accessEnabled) "${notifications.size} active" else "Notification access is off",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { TagDeckNotificationListenerService.dismissAll() }) {
                            Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear all notifications")
                        }
                    }
                    IconButton(onClick = { onVisibleChange(false) }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (!accessEnabled) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Allow TagDeck notification access to show and manage notifications here. Notification content stays in memory and is not added to backups.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(onClick = {
                        runCatching { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                    }) { Text("Enable notification access") }
                }
            } else if (notifications.isEmpty()) {
                Text(
                    "You're all caught up.",
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(notifications, key = { it.key }) { item ->
                        NotificationCard(
                            item = item,
                            onOpen = {
                                runCatching { item.contentIntent?.send() }
                                onVisibleChange(false)
                            },
                            onDismiss = { TagDeckNotificationListenerService.dismiss(item.key) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun NotificationCard(
    item: LauncherNotification,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onOpen).padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.text.isNotBlank()) {
                    Text(item.text, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    "${item.packageName.substringAfterLast('.') } • ${DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.postedAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = "Dismiss notification")
            }
        }
    }
}
