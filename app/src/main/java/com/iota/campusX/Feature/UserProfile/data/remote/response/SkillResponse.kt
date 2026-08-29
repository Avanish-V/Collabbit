package com.iota.campusX.Feature.UserProfile.data.remote.response

import kotlinx.serialization.Serializable

@Serializable
data class SkillResponse(
    val id: String,
    val name: String,
    val category: String
)
