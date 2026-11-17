package com.iota.campusX.Feature.Chats.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SwipeActionRequest(
    @SerialName("targetUserId") val targetUserId: String = "",
    @SerialName("action") val action: String = "CONNECT",
    @SerialName("roomId") val roomId: String? = null,
    @SerialName("note") val note: String? = null
)
