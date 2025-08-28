package com.iota.campusX.Feature.Post.data.model

import com.iota.campusX.Screens.Post.Poll
import kotlinx.serialization.Serializable

import com.google.firebase.Timestamp
import com.iota.campusX.Screens.Post.MediaType
import com.iota.campusX.Screens.Post.Type
import kotlinx.serialization.Contextual

@Serializable
data class CreatePostDTO(
    val postId: String = "",
    val visibilityMode: VisibilityMode = VisibilityMode.USER,
    @Contextual
    val createdAt: Timestamp = Timestamp.now(), // ✅ This line fixes the issue
    val creatorId: String = "",
    val reference: Reference? = null,
    val campusId: String? = null,
    val feedMode: FeedMode = FeedMode.GLOBAL,
    val type: Type = Type.Media,
    val mediaType: MediaType = MediaType.Image,
    val postText: String = "",
    val image: String ?= null,
    val poll: Poll ?= null

)


data class Campus(
    val campusId: String = "",
    val campusName: String = "",
    val campusImage: String = ""
)


@Serializable
data class PostContent(
    val postText: String = "",
    val postImage: String ?= null,
    val poll: Poll ?= null
)

@Serializable
data class PostActions(
    var isLiked: Boolean = false,
    val likesCount: Int = 0,
    val replies: List<String> = emptyList(),
    val replyCount: Int = 0
)

@Serializable
data class Reference(
    val icon: String= "",
    val title: String = ""
)

enum class VisibilityMode {
    USER, ANONYMOUS
}

