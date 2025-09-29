package com.iota.campusX.Feature.PushNotification

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.iota.campusX.Feature.PushNotification.Models.FcmRequest
import com.iota.campusX.Feature.PushNotification.Models.Message
import com.iota.campusX.Feature.PushNotification.Models.Notification
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class FcmNotificationSender(
    private val context: Context
) {

    val projectId = "campuscircle-ea7ca"

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun sendFcmNotification(userFcmToken: String,bodyText: String, title: String) {

        val accessToken = getAccessToken(context)

        val response = client.post("https://fcm.googleapis.com/v1/projects/$projectId/messages:send") {
            contentType(ContentType.Application.Json)
            bearerAuth(accessToken)
            setBody(
                FcmRequest(
                    message = Message(
                        token = userFcmToken,
                        notification = Notification(
                            title = title,
                            body = bodyText
                        ),
                        //data = mapOf("customKey" to "customValue")
                    )
                )
            )
        }

    }

    suspend fun sendToSubscriber(topic: String,bodyText: String,title: String){

        val accessToken = getAccessToken(context)

        val response = client.post("https://fcm.googleapis.com/v1/projects/$projectId/messages:send") {
            contentType(ContentType.Application.Json)
            bearerAuth(accessToken)
            setBody(
                FcmRequest(
                    message = Message(
                        notification = Notification(
                            title = title,
                            body = bodyText
                        ),
                        topic = topic,
                        //data = mapOf("customKey" to "customValue")
                    )
                )
            )

        }
    }
}


suspend fun getAccessToken(context: Context): String = withContext(Dispatchers.IO) {
    val stream = context.assets.open("campuscircle-ea7ca-firebase-adminsdk-fbsvc-d968d72f2f.json")
    val credentials = GoogleCredentials.fromStream(stream)
        .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
    credentials.refreshIfExpired()
    credentials.accessToken.tokenValue
}
