package com.iota.campusX.MessagingService

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import com.iota.campusX.R

object NotificationHelper {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(
        context: Context,
        message: RemoteMessage
    ) {

        val title = message.notification?.title ?: "Campus Circle"

        val body = message.notification?.body ?: ""

        val pendingIntent =
            NotificationClickHandler.createPendingIntent(
                context,
                message.data
            )



        val builder =
            NotificationCompat.Builder(
                context,
                NotificationChannelManager.CHANNEL_ID
            )
                .setSmallIcon(R.drawable.splash_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        val notificationId =
            message.data["notificationId"]?.toIntOrNull()
                ?: System.currentTimeMillis().toInt()

        NotificationManagerCompat
            .from(context)
            .notify(
                notificationId,
                builder.build()
            )

    }
}