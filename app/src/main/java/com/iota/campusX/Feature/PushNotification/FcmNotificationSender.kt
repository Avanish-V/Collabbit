package com.iota.campusX.Feature.PushNotification

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class FcmNotificationSender(
    private val userFcmToken: String,
    private val title: String,
    private val body: String,
) {
    private val projectName = "campusx-87d84"
    private val baseUrl = "https://fcm.papayacoders.in/api/send-token-notice"

    suspend fun sendNotification() {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }

        val urlBuilder = URLBuilder(baseUrl).apply {
            parameters.append("projectname", projectName)
            parameters.append("title", title)
            parameters.append("message", body)
            parameters.append("token", userFcmToken)
        }

        try {
            val response: HttpResponse = client.post(urlBuilder.build()) {
                headers {
                    append(HttpHeaders.Accept, "*/*")
                }
            }

            val responseText = response.bodyAsText()
            Log.d("FCM", "Notification sent: $responseText")

        } catch (e: Exception) {
            Log.e("FCM", "Error sending notification", e)
        } finally {
            client.close()
        }
    }
}
