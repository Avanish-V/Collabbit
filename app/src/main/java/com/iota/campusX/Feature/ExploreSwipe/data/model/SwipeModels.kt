package com.iota.campusX.Feature.ExploreSwipe.data.model

import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import kotlinx.serialization.Serializable

@Serializable
enum class SwipeCardType {
    COLLABORATION,
    PROFILE
}

@Serializable
data class SwipeCardDto(
    val id: String,
    val type: SwipeCardType,
    val matchPercentage: Int = 85,
    val collab: CollabResponse? = null,
    val profile: ProfileResponse? = null
)

@Serializable
data class SwipeMatchFeedResponse(
    val cards: List<SwipeCardDto> = emptyList(),
    val totalCollabs: Int = 0,
    val totalProfiles: Int = 0
)

@Serializable
data class SwipeActionRequest(
    val cardId: String,
    val type: SwipeCardType,
    val action: String, // "CONNECT", "PASS", "SUPER_LIKE"
    val note: String? = null
)

@Serializable
data class SwipeActionResult(
    val success: Boolean,
    val message: String,
    val status: String? = null
)
