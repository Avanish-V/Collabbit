package com.iota.campusX.Feature.Post.domain.models

import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.MediaType
import com.iota.campusX.Feature.Post.data.model.Type
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import kotlinx.serialization.Serializable

@Serializable
data class PostResponse(
    val postId: Long = 0,
    val authorDetails: AuthorDetails? =null,
    val createdAt: Long = 0L,
    val visibility: VisibilityMode = VisibilityMode.ANONYMOUS,
    val postType: Type = Type.TEXT,
    val text: String?= null,
    val poll: Poll?= null,
    val mediaPost: List<MediaPost> = emptyList(),
    val feedMode: FeedMode = FeedMode.OPEN,
    val likes: Int = 0,
    val isLiked: Boolean =false,
    val comments: Int = 0
)

@Serializable
data class AuthorDetails(
    val authorId: String,
    val authorName: String ? = null,
    val authorImage: String? = null,
    val authorTagline: String? = null,
    val isVerified: Boolean ? = null,
    val isCurrentUser: Boolean ? = null,
)


@Serializable
data class Poll(
    val question: String? = null,
    val options: List<String>? = null
)

@Serializable
data class MediaPost(
    val mediaUrl: String = "",
    val mediaType: MediaType? = null,
)