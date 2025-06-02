package com.iota.campusX.Feature.Post.domain

import android.os.Parcelable
import com.iota.campusX.Screens.Post.Poll
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
data class CreatePostDTO(
    val postId: String = "",
    val type: String = "",
    val postedAt: Long = 0L,
    val creatorId: String = "",
    val reference: Reference = Reference(),
    val campusId: String? = null,
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
    val postType: String = "",
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

