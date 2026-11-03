package com.iota.campusX.Feature.ExploreSwipe.domain.model

import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse

data class SwipeItem(
    val id: String,
    val matchPercentage: Int,
    val profile: ProfileResponse
)
