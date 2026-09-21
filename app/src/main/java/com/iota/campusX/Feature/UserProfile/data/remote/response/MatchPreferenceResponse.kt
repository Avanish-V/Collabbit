package com.iota.campusX.Feature.UserProfile.data.remote.response

import kotlinx.serialization.Serializable

@Serializable
data class MatchPreferenceResponse(
    val id: Long,
    val code: String,
    val title: String,
    val description: String?,
    val icon: String?,
    val active: Boolean,
)
