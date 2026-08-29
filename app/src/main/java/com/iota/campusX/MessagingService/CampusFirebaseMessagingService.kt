package com.iota.campusX.MessagingService

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.iota.campusX.Feature.Notificattion.sync.NotificationSyncManager
import com.iota.campusX.Feature.UserProfile.domain.useCases.UpdateFcmTokenUseCase
import com.iota.campusX.realtime.connection.SocketConnectionState
import com.iota.campusX.realtime.socket.RealtimeSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// Remove if not using Hilt
class CampusFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val updateFcmTokenUseCase: UpdateFcmTokenUseCase by inject()


    private val realtimeSocketManager : RealtimeSocketManager by inject()
    private val notificationSyncManager by inject<NotificationSyncManager>()

    override fun onNewToken(token: String) {

        CoroutineScope(Dispatchers.IO).launch {

            updateFcmTokenUseCase(token)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(
        message: RemoteMessage
    ) {

        super.onMessageReceived(message)

        val id =

            message.data["notificationId"]
                ?.toLongOrNull()
                ?: return

        if (realtimeSocketManager.connectionState.value == SocketConnectionState.Connected) {

            NotificationHelper.showNotification(
                applicationContext,
                message
            )

            return
        }

        CoroutineScope(
            Dispatchers.IO
        ).launch {

            notificationSyncManager.syncNotification(id)

        }

        NotificationHelper.showNotification(
            applicationContext,
            message
        )
    }
}