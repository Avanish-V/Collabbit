package com.iota.campusX.Feature.Post.domain.Models

import com.iota.campusX.Screens.Post.Poll
import com.iota.campusX.Screens.Post.PostOptions
import kotlinx.serialization.Serializable

@Serializable
data class CreatePostDTO(
    val postId: String = "",
    val visibilityMode: PostVisibilityMode = PostVisibilityMode.USER,
    val createdAt: Long = 0L,
    val creatorId: String = "",
    val reference: Reference? = null,
    val campusId: String? = null,
    val feedMode: FeedMode = FeedMode.GLOBAL,
    val postContent: PostContent = PostContent(),
    val postActions: PostActions = PostActions()
)

data class Campus(
    val campusId: String = "",
    val campusName: String = "",
    val campusImage: String = ""
)



@Serializable
data class PostData(
    val postText: String = "",
    val postImage: String ?= null,
    val poll: Poll ?= null
)



@Serializable
data class PostContent(
    val postType: PostOptions = PostOptions.TEXT,
    val postData: PostData = PostData()
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

enum class PostVisibilityMode {
    USER, ANONYMOUS
}

