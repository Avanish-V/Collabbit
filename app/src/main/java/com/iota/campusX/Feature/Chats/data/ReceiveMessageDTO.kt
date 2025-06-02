package com.iota.campusX.Feature.Chats.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class ReceiveMessageDTO(
    val message:String = "",
    @Transient val timestamp: Any? = null, // Will be set by Firebase
    val messageId:String = "",
    val user:String = ""
)
