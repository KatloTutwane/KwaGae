package com.example.kwagae.notifications

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives FCM push notifications.
 * - onMessageReceived: shows a local notification via NotificationHelper
 * - onNewToken: saves the refreshed FCM token to Firestore under /users/{uid}
 */
class KwaGaeMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "KwaGae"
        val body  = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: return   // nothing to show

        NotificationHelper.sendAlert(
            context        = this,
            title          = title,
            body           = body,
            notificationId = System.currentTimeMillis().toInt()
        )
    }

    override fun onNewToken(token: String) {
        val prefs       = getSharedPreferences("kwagae_prefs", Context.MODE_PRIVATE)
        val firebaseUid = prefs.getString("firebase_uid", "") ?: ""
        if (firebaseUid.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(firebaseUid)
                .update("fcmToken", token)
                .addOnFailureListener { /* non-critical — token will update on next login */ }
        }
    }
}
