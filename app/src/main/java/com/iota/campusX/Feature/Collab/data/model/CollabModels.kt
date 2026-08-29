package com.iota.campusX.Feature.Collab.data.model

import com.iota.campusX.Feature.UserProfile.data.remote.response.SkillResponse
import kotlinx.serialization.Serializable

@Serializable
data class CreateCollabRequest(
    val title: String,
    val description: String,
    val collabType: CollabType,
    val requirements: List<String>,
    val participantsNeeded: Int,
    val deadline: Long? = null,
    val projectUrl: String? = null
)

@Serializable
enum class CollabType {
    HACKATHON, COBUILDER, STUDY,RESEARCH,NONE
}
@Serializable
data class CollabResponse(
    val id: String,
    val title: String,
    val description: String,
    val collabType: CollabType,
    val authorName: String,
    val authorId: String,
    val isCurrentUser: Boolean,
    val authorImage: String? = null,
    val requirements: List<String>,
    val deadline: Long,
    val participantsNeeded: Int,
    val timeAgo: String,
    val projectUrl: String? = null
)

@Serializable
data class CollabConnectRequestResponse(
    val id: Long,
    val senderId: String,
    val senderName: String,
    val senderImage: String?,
    val tagline : String?,
    var status: String,
    val createdAt: String
)

@Serializable
enum class CollabRequestStatus {
    PENDING, ACCEPTED, DECLINED, NOT_REQUESTED
}
