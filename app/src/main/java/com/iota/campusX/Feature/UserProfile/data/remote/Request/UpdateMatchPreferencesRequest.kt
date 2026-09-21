package com.iota.campusX.Feature.UserProfile.data.remote.Request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateMatchPreferencesRequest(
    val preferenceIds: List<Long>
)
