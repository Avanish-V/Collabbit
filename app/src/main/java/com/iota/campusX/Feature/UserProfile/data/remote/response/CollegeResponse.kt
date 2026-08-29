package com.iota.campusX.Feature.UserProfile.data.remote.response

import kotlinx.serialization.Serializable

@Serializable
data class College(
    val name: String = "",
    val domain: String = "",
    val logo: String?=null
)

@Serializable
data class CollegeResponse(
    val college: List<College> = emptyList()
)
