package com.example.data.system

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LauncherNotification(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val subText: String,
    val postTime: Long,
    val ongoing: Boolean,
)

object NotificationBadgeStore {
    private val activeNotifications = LinkedHashMap<String, String>()
    private val _counts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val counts: StateFlow<Map<String, Int>> = _counts.asStateFlow()
    private val _feed = MutableStateFlow<List<LauncherNotification>>(emptyList())
    val feed: StateFlow<List<LauncherNotification>> = _feed.asStateFlow()
    @Volatile private var listener: TagDeckNotificationListenerService? = null

    @Synchronized
    fun attach(service: TagDeckNotificationListenerService) {
        listener = service
        service.activeNotifications?.let(::replace)
    }

    @Synchronized
    fun detach(service: TagDeckNotificationListenerService) {
        if (listener === service) listener = null
    }

    @Synchronized
    fun replace(notifications: Array<StatusBarNotification>?) {
        activeNotifications.clear()
        _feed.value = notifications.orEmpty().mapNotNull(::toFeedItem).sortedByDescending { it.postTime }
        notifications.orEmpty().forEach(::addIfCountable)
        publishCounts()
    }

    @Synchronized
    fun posted(notification: StatusBarNotification) {
        activeNotifications.remove(notification.key)
        addIfCountable(notification)
        val next = _feed.value.filterNot { it.key == notification.key } + listOfNotNull(toFeedItem(notification))
        _feed.value = next.sortedByDescending { it.postTime }.take(150)
        publishCounts()
    }

    @Synchronized
    fun removed(notification: StatusBarNotification) {
        activeNotifications.remove(notification.key)
        _feed.value = _feed.value.filterNot { it.key == notification.key }
        publishCounts()
    }

    fun dismiss(key: String) {
        listener?.dismissFromFeed(key)
    }

    @Synchronized
    fun clearFeed() {
        _feed.value = emptyList()
        listener?.cancelAllNotifications()
    }

    @Synchronized
    fun clear() {
        activeNotifications.clear()
        _feed.value = emptyList()
        publishCounts()
    }

    private fun toFeedItem(sbn: StatusBarNotification): LauncherNotification? {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return null
        val label = runCatching {
            listener?.packageManager?.getApplicationLabel(
                listener!!.packageManager.getApplicationInfo(sbn.packageName, 0)
            )?.toString()
        }.getOrNull().orEmpty().ifBlank { sbn.packageName.substringAfterLast('.') }
        return LauncherNotification(
            key = sbn.key,
            packageName = sbn.packageName,
            appLabel = label,
            title = title.ifBlank { label },
            text = text,
            subText = subText,
            postTime = sbn.postTime,
            ongoing = sbn.isOngoing,
        )
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
        NotificationBadgeStore.attach(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        NotificationBadgeStore.posted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationBadgeStore.removed(sbn)
    }

    fun dismissFromFeed(key: String) {
        runCatching { cancelNotification(key) }
    }

    override fun onListenerDisconnected() {
        NotificationBadgeStore.detach(this)
        NotificationBadgeStore.clear()
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        NotificationBadgeStore.detach(this)
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
