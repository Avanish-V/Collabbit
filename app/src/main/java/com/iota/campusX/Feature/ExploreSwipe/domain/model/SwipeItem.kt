package com.iota.campusX.Feature.ExploreSwipe.domain.model

import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse

sealed interface SwipeItem {
    val id: String
    val matchPercentage: Int

    data class CollabItem(
        override val id: String,
        override val matchPercentage: Int,
        val collab: CollabResponse
    ) : SwipeItem

    data class ProfileItem(
        override val id: String,
        override val matchPercentage: Int,
        val profile: ProfileResponse
    ) : SwipeItem
}
