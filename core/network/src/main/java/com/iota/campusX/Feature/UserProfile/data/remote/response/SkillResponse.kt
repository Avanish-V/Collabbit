package com.iota.campusX.Feature.UserProfile.data.remote.response

import kotlinx.serialization.Serializable

@Serializable
data class SkillResponse(
    val id: Long,
    val name: String,
    val category: String
)
