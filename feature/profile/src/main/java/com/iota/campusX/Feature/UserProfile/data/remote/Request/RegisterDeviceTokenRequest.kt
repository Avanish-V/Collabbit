package com.iota.campusX.Feature.UserProfile.data.remote.Request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DevicePlatform {
    @SerialName("ANDROID") ANDROID,
    @SerialName("IOS") IOS,
    @SerialName("WEB") WEB
}

@Serializable
data class RegisterDeviceTokenRequest(
    @SerialName("fcmToken") val fcmToken: String,
    @SerialName("deviceId") val deviceId: String? = null,
    @SerialName("appVersion") val appVersion: String? = null
)
