package com.example.data.system

import android.content.ComponentName
import android.content.Context
import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Keeps only per-package active notification counts. Notification text and content are never read
 * or retained by TagDeck.
 */
object NotificationBadgeStore {
    private val activeNotifications = LinkedHashMap<String, String>()
    private val _counts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val counts: StateFlow<Map<String, Int>> = _counts.asStateFlow()

    @Synchronized
    fun replace(notifications: Array<StatusBarNotification>?) {
        activeNotifications.clear()
        notifications.orEmpty().forEach(::addIfCountable)
        publishCounts()
    }

    @Synchronized
    fun posted(notification: StatusBarNotification) {
        activeNotifications.remove(notification.key)
        addIfCountable(notification)
        publishCounts()
    }

    @Synchronized
    fun removed(notification: StatusBarNotification) {
        activeNotifications.remove(notification.key)
        publishCounts()
    }

    @Synchronized
    fun clear() {
        activeNotifications.clear()
        publishCounts()
    }

    private fun addIfCountable(notification: StatusBarNotification) {
        if (notification.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        activeNotifications[notification.key] = notification.packageName
    }

    private fun publishCounts() {
        _counts.value = activeNotifications.values.groupingBy { it }.eachCount()
    }
}

class TagDeckNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationBadgeStore.replace(activeNotifications)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        NotificationBadgeStore.posted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationBadgeStore.removed(sbn)
    }

    override fun onListenerDisconnected() {
        NotificationBadgeStore.clear()
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        NotificationBadgeStore.clear()
        super.onDestroy()
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
