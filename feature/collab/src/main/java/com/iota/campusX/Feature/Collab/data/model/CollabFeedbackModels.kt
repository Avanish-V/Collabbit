package com.iota.campusX.Feature.Collab.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class CollabAuthorDetails(
    @SerialName("authorId") val authorId: String = "",
    @SerialName("authorName") val authorName: String = "",
    @SerialName("authorImage") val authorImage: String? = null,
    @SerialName("authorTagline") val authorTagline: String? = null,
    @SerialName("isProjectLead") val isProjectLead: Boolean = false,
    @SerialName("isCurrentUser") val isCurrentUser: Boolean = false
)

@Immutable
@Serializable
data class CollabFeedbackResponse(
    @SerialName("id") val id: String = "",
    @SerialName("collabId") val collabId: String = "",
    @SerialName("content") val content: String = "",
    @SerialName("author") val author: CollabAuthorDetails = CollabAuthorDetails(),
    @SerialName("parentFeedbackId") val parentFeedbackId: String? = null,
    @SerialName("createdAt") val createdAt: String = "",
    @SerialName("timeAgo") val timeAgo: String = "Just now",
    @SerialName("childCount") val childCount: Int = 0
)

typealias CollabFeedback = CollabFeedbackResponse

@Serializable
data class CollabFeedbackRequest(
    @SerialName("content") val content: String,
    @SerialName("parentFeedbackId") val parentFeedbackId: String? = null
)
