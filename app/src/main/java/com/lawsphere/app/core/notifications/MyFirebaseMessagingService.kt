package com.lawsphere.app.core.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        message.notification?.let {
            GlobalNotificationManager.show(
                title = it.title ?: "LawSphere Update",
                message = it.body ?: "",
                type = NotificationType.INFO
            )
        }
    }
}