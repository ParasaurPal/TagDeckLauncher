package com.example.data.system

import android.app.Notification
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Notification data stays in memory only. Nothing is written to launcher storage or backups.
 * Notification access is explicitly granted by the user in Android Settings.
 */
data class LauncherNotification(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val postedAt: Long,
    val contentIntent: PendingIntent?,
)

object NotificationBadgeStore {
    private val activeNotifications = LinkedHashMap<String, LauncherNotification>()
    private val _notifications = MutableStateFlow<List<LauncherNotification>>(emptyList())
    val notifications: StateFlow<List<LauncherNotification>> = _notifications.asStateFlow()

    private val _counts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val counts: StateFlow<Map<String, Int>> = _counts.asStateFlow()

    @Synchronized
    fun replace(notifications: Array<StatusBarNotification>?) {
        activeNotifications.clear()
        notifications.orEmpty().forEach(::add)
        publish()
    }

    @Synchronized
    fun posted(notification: StatusBarNotification) {
        activeNotifications.remove(notification.key)
        add(notification)
        publish()
    }

    @Synchronized
    fun removed(notification: StatusBarNotification) {
        activeNotifications.remove(notification.key)
        publish()
    }

    @Synchronized
    fun clear() {
        activeNotifications.clear()
        publish()
    }

    @Synchronized
    private fun add(sbn: StatusBarNotification) {
        val notification = sbn.notification
        if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        val extras = notification.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = (
            extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: extras?.getCharSequence(Notification.EXTRA_TEXT)
                ?: extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)
            )?.toString()?.trim().orEmpty()
        if (title.isBlank() && text.isBlank()) return
        activeNotifications[sbn.key] = LauncherNotification(
            key = sbn.key,
            packageName = sbn.packageName,
            title = title.ifBlank { "Notification" },
            text = text,
            postedAt = sbn.postTime,
            contentIntent = notification.contentIntent,
        )
    }

    @Synchronized
    private fun publish() {
        _notifications.value = activeNotifications.values.sortedByDescending { it.postedAt }
        _counts.value = activeNotifications.values.groupingBy { it.packageName }.eachCount()
    }
}

class TagDeckNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        NotificationBadgeStore.replace(activeNotifications?.filterNot { it.packageName == packageName }?.toTypedArray())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != packageName) NotificationBadgeStore.posted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationBadgeStore.removed(sbn)
    }

    override fun onListenerDisconnected() {
        if (instance === this) instance = null
        NotificationBadgeStore.clear()
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        NotificationBadgeStore.clear()
        super.onDestroy()
    }

    companion object {
        @Volatile private var instance: TagDeckNotificationListenerService? = null

        fun dismiss(key: String) {
            instance?.cancelNotification(key)
        }

        fun dismissAll() {
            instance?.cancelAllNotifications()
        }
    }
}

object NotificationAccess {
    fun isEnabled(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        val component = ComponentName(
            context,
            TagDeckNotificationListenerService::class.java,
        ).flattenToString()
        return enabledListeners.split(':').any { it.equals(component, ignoreCase = true) }
    }
}
