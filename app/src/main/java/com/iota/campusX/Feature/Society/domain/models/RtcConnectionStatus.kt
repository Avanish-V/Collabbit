package com.iota.campusX.Feature.Society.domain.models

enum class MatchStatus {

    IDLE,
    CHANNEL_JOINED,
    ROOM_JOINED,
    LEAVED
}

data class RtcConnectionStatus(
    val status: MatchStatus,
    val channelId:String = ""
)

sealed class State{
    object Idle: State()
    object Channel_joined: State()
    object Room_Joined: State()

    data class ChannelLeave(val roomId: String):State()
    data class isSpeaking(val value: Int): State()
    data class isMicrophone(val value: Boolean): State()

    data class error(val error: String) : State()
}