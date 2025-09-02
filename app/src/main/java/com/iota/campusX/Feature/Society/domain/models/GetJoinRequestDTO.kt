package com.iota.campusX.Feature.Society.domain.models

data class GetJoinRequestDTO(
    var requestId: String = "",
    val role : String = "",
    var status: Status = Status.IDLE,
    val uid: Int = 0,
    val speaking: Boolean = false,
    val muted: Boolean = true,
    val raiseHand: Boolean,
    val userName: String = "",
    val userImage: String = "",
)



data class SetJoinRequestDTO(
    var requestId: String = "",
    var status: Status = Status.IDLE,
    var role: String = "",
    var speaking: Boolean = false,
    var muted: Boolean = true,
    var raiseHand: Boolean = false,
    var uid: Int = 0
)

enum class Status{
    IDLE,
    STAGE_UP,
    STAGE_DOWN
}
