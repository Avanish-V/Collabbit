package com.iota.campusX.Feature.ExploreSwipe.data.model

import androidx.annotation.Keep
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class SwipeCardType {
    @SerialName("COLLABORATION") COLLABORATION,
    @SerialName("PROFILE") PROFILE
}

@Keep
@Serializable
data class SwipeCardDto(
    @SerialName("id") val id: String = "",
    @SerialName("type") val type: SwipeCardType = SwipeCardType.PROFILE,
    @SerialName("matchPercentage") val matchPercentage: Int = 85,
    @SerialName("collab") val collab: CollabResponse? = null,
    @SerialName("profile") val profile: ProfileResponse? = null
)

@Keep
@Serializable
data class SwipeMatchFeedResponse(
    @SerialName("cards") val cards: List<SwipeCardDto> = emptyList(),
    @SerialName("totalCollabs") val totalCollabs: Int = 0,
    @SerialName("totalProfiles") val totalProfiles: Int = 0
)

@Keep
@Serializable
data class SwipeActionRequest(
    @SerialName("cardId") val cardId: String,
    @SerialName("type") val type: SwipeCardType,
    @SerialName("action") val action: String,
    @SerialName("note") val note: String? = null
)

@Keep
@Serializable
data class SwipeActionResult(
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String = "",
    @SerialName("status") val status: String? = null
)
