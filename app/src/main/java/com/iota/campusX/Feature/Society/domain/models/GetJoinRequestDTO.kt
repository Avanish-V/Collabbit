package com.iota.campusX.Feature.Society.domain.models

data class GetJoinRequestDTO(
    val requestId: String = "",
    val role : String = "",
    val status: Boolean = false,
    val uid: Int = 0,
    val speaking: Boolean = false,
    val microphone: Boolean = false,
    val raiseHand: Boolean,
    val userName: String = "",
    val userImage: String = "",
)



data class SetJoinRequestDTO(

    var requestId: String = "",
    var status: Boolean = false,
    var role: String = "",
    var speaking: Boolean = false,
    var microphone: Boolean = false,
    var raiseHand: Boolean = false,
    var uid: Int = 0
)
