package com.iota.campusX.Feature.PushNotification

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class TokenServices(private val context: Context) {

    @Throws(IOException::class)
    fun getAccessToken(callBack: (String?) -> Unit) {
        val MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging"
        val SCOPES = listOf(MESSAGING_SCOPE)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load from assets instead of raw
                val inputStream = context.assets.open("campuscircle-ea7ca-firebase-adminsdk-fbsvc-d968d72f2f.json")

                val googleCredentials = GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped(SCOPES)

                googleCredentials.refresh()
                val serverKey = googleCredentials.accessToken.tokenValue

                withContext(Dispatchers.Main) {
                    callBack(serverKey)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callBack(null)
                }
            }
        }
    }

    fun getDeviceId(): String {
        var fcmToke: String = ""
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                 fcmToke = task.result  // <-- This is the correct userFcmToken
                Log.d("FCM_TOKEN", "getDeviceId: $fcmToke")

            }
        }
        return fcmToke
    }
}
