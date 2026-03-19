package com.lawsphere.app.core.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppNotification(
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.INFO
)

enum class NotificationType { INFO, SUCCESS, WARNING, EMERGENCY }

object GlobalNotificationManager {
    private val _notification = MutableStateFlow<AppNotification?>(null)
    val notification = _notification.asStateFlow()

    fun show(title: String, message: String, type: NotificationType = NotificationType.INFO) {
        _notification.value = AppNotification(title, message, type)
    }

    fun dismiss() {
        _notification.value = null
    }
}