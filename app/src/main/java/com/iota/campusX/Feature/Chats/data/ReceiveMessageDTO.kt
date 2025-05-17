package com.iota.campusX.Feature.Chats.data

import kotlinx.serialization.Serializable


@Serializable
data class ReceiveMessageDTO(
    val message:String = "",
    val timestamp:String = "",
    val messageId:String = "",
    val user:String = ""
)
