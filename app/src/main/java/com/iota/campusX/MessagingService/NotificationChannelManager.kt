package com.iota.campusX.MessagingService

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannelManager {

    const val CHANNEL_ID = "campus_circle_notification"

    private const val CHANNEL_NAME = "Campus Circle"

    private const val CHANNEL_DESCRIPTION =
        "Likes, comments, follows and messages"

    fun create(
        context: Context
    ) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return

        val channel = NotificationChannel(

            CHANNEL_ID,

            CHANNEL_NAME,

            NotificationManager.IMPORTANCE_HIGH

        ).apply {

            description = CHANNEL_DESCRIPTION

            enableLights(true)

            enableVibration(true)
        }

        val manager =
            context.getSystemService(
                NotificationManager::class.java
            )

        manager.createNotificationChannel(channel)
    }
}