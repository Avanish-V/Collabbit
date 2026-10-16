package com.iota.campusX.Feature.Collab.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
enum class ProjectStatus(val displayName: String) {
    LOOKING("Looking"),
    DEVELOPING("Developing"),
    ONGOING("Ongoing"),
    LIVE("Live")
}

@Immutable
@Serializable
data class ProjectRole(
    val id: String = "",
    val roleName: String = "",
    val skills: List<String> = emptyList(),
    val peopleNeeded: Int = 1
)

@Immutable
@Serializable
data class CreateCollabRequest(
    val title: String,
    val description: String,
    val collabType: CollabType,
    val requirements: List<String> = emptyList(),
    val roles: List<ProjectRole> = emptyList(),
    val participantsNeeded: Int,
    val deadline: Long? = null,
    val projectUrl: String? = null,
    val logoUrl: String? = null,
    val status: ProjectStatus = ProjectStatus.LOOKING
)

@Serializable
enum class CollabType {
    HACKATHON, COBUILDER, STUDY,RESEARCH,NONE
}

@Immutable
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
    val requirements: List<String> = emptyList(),
    val roles: List<ProjectRole> = emptyList(),
    val deadline: Long = 0L,
    val participantsNeeded: Int = 1,
    val timeAgo: String = "",
    val projectUrl: String? = null,
    val logoUrl: String? = null,
    val status: ProjectStatus = ProjectStatus.LOOKING,
    val memberImages: List<String> = emptyList(),  // joined member avatar URLs
    val joinedMemberCount: Int = 0                 // total joined members count
)

@Immutable
@Serializable
data class UpdateCollabRequest(
    val title: String? = null,
    val description: String? = null,
    val requirements: List<String>? = null,
    val roles: List<ProjectRole>? = null,
    val participantsNeeded: Int? = null,
    val projectUrl: String? = null,
    val logoUrl: String? = null
)

@Immutable
@Serializable
data class CollabConnectRequestResponse(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderImage: String?,
    val tagline : String?,
    val status: String,
    val createdAt: String
)

@Serializable
enum class CollabRequestStatus {
    PENDING, ACCEPTED, DECLINED, NOT_REQUESTED
}

@Immutable
@Serializable
data class CollabMemberResponse(
    val userId: String,
    val name: String,
    val image: String? = null,
    val tagline: String? = null,
    val role: String = "Team Member",
    val isLead: Boolean = false,
    val joinedAt: String = "",
    val isCurrentUser: Boolean = false
)
