package com.iota.campusX.Feature.PushNotification.Models

import androidx.annotation.Keep
import com.iota.campusX.Feature.PushNotification.Models.Notification
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Message(
    val token: String? = null,
    val notification: Notification? = null,
    val topic: String? = null,
    val data: Map<String, String>? = null
)