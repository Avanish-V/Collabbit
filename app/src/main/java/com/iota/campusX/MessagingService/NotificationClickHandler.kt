package com.iota.campusX.MessagingService

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.iota.campusX.MainActivity

object NotificationClickHandler {

    fun createPendingIntent(
        context: Context,
        data: Map<String, String>
    ): PendingIntent {

        val deepLink = data["deepLink"]

        val intent = Intent(
            Intent.ACTION_VIEW,
            deepLink?.toUri(),
            context,
            MainActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            context,
            deepLink.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }
}