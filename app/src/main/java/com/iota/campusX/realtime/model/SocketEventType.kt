package com.iota.campusX.realtime.model

import kotlinx.serialization.Serializable

@Serializable
enum class SocketEventType {

    NOTIFICATION_CREATED,

    CHAT_MESSAGE,

    MESSAGE_SENT,

    CHAT_UPDATED,

    USER_TYPING,

    USER_ONLINE,

    USER_OFFLINE,

    PING,

    PONG

}