package com.iota.campusX.MessagingService

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object NotificationPermission {

    fun hasPermission(
        context: Context
    ): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            ContextCompat.checkSelfPermission(

                context,

                Manifest.permission.POST_NOTIFICATIONS

            ) == PackageManager.PERMISSION_GRANTED

        } else {

            true
        }
    }
}