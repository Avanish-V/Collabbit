package com.iota.campusX.Feature.Chats.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SwipeActionResponse(
    @SerialName("status") val status: String = ChatRequestStatus.ACCEPTED.name,
    @SerialName("roomId") val roomId: String = ""
)
