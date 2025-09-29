package com.iota.campusX.Feature.PushNotification.Models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class FcmRequest(val message: Message)