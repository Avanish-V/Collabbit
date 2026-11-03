package com.iota.campusX.notifications

import android.Manifest
import androidx.annotation.RequiresPermission
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.iota.campusX.Feature.Notificattion.sync.NotificationSyncManager
import com.iota.campusX.core.network.websocket.connection.SocketConnectionState
import com.iota.campusX.core.network.websocket.socket.RealtimeSocketManager
import com.iota.campusX.notifications.sync.FcmTokenSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CampusFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val fcmTokenSyncManager: FcmTokenSyncManager by inject()
    private val realtimeSocketManager: RealtimeSocketManager by inject()
    private val notificationSyncManager by inject<NotificationSyncManager>()

    override fun onNewToken(token: String) {
        fcmTokenSyncManager.onNewToken(token)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(
        message: RemoteMessage
    ) {
        super.onMessageReceived(message)

        val id = message.data["notificationId"]
            ?.toLongOrNull()
            ?: return

        if (realtimeSocketManager.connectionState.value == SocketConnectionState.Connected) {
            com.iota.campusX.core.notifications.NotificationHelper.showNotification(
                applicationContext,
                message,
                com.iota.campusX.MainActivity::class.java
            )
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            notificationSyncManager.syncNotification(id)
        }

        com.iota.campusX.core.notifications.NotificationHelper.showNotification(
            applicationContext,
            message,
            com.iota.campusX.MainActivity::class.java
        )
    }
}
