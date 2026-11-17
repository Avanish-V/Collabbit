package com.iota.campusX.Feature.ExploreSwipe.data.model

import androidx.annotation.Keep
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.Chats.domain.SwipeActionRequest
import com.iota.campusX.Feature.Chats.domain.SwipeActionResponse
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
    @SerialName("targetUserId") val targetUserId: String = "",
    @SerialName("action") val action: String = "CONNECT",
    @SerialName("roomId") val roomId: String? = null,
    @SerialName("note") val note: String? = null
)

@Keep
@Serializable
data class SwipeActionResponse(
    @SerialName("status") val status: String = "",
    @SerialName("roomId") val roomId: String = ""
)

@Keep
@Serializable
data class SwipeActionResult(
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String = "",
    @SerialName("status") val status: String? = null,
    @SerialName("roomId") val roomId: String? = null
)
