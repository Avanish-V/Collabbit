package com.iota.campusX.MessagingService

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun NotificationPermissionRequester() {

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
        return

    val context = LocalContext.current

    val launcher =
        rememberLauncherForActivityResult(

            ActivityResultContracts.RequestPermission()

        ) { }

    LaunchedEffect(Unit) {

        if (!NotificationPermission.hasPermission(context)) {

            launcher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }
}