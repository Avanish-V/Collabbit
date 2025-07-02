package com.iota.campusX.Feature.Society.domain.models

data class JoinRequests(
    val requestId: String,
    val role : String,
    val status: Boolean,
    val isMicrophone: Boolean,
    val isSpeaking: Boolean,
    val userName: String,
    val userImage: String,
)
