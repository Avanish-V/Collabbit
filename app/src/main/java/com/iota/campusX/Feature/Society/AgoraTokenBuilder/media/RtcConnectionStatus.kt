package com.iota.campusX.Feature.Society.AgoraTokenBuilder.media

enum class MatchStatus {

    IDLE,
    MATCHING,
    MATCHED,
    LEAVED
}

data class RtcConnectionStatus(
    val status: MatchStatus,
    val channelId:String = ""
)