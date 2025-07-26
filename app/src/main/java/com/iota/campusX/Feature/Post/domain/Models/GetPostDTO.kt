package com.iota.campusX.Feature.Post.domain.Models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
data class GetPostDTO(
    val postId: String = "",
    val createdAt: Long = 0L,
    val creatorDetail: CreatorDetail,
    val feedMode: FeedMode = FeedMode.GLOBAL,
    val reference: Reference?=null,
    val visibilityMode: PostVisibilityMode,
    val campusId: String?=null,
    val postContent: PostContent,
    val postActions: PostActions
){
    @Keep
    constructor() : this(
        postId = "",
        createdAt = 0L,
        creatorDetail = CreatorDetail(),
        reference = Reference(),
        visibilityMode = PostVisibilityMode.USER,
        campusId = null,
        postContent = PostContent(),
        postActions = PostActions()
    )

}


@Serializable
data class CreatorDetail(
    val isCurrentUser: Boolean = false,
    val isVerified: Boolean = false,
    val isPremium: Boolean = false,
    val profile: UserDetail? = null
)


@Serializable
data class UserDetail(
    val userName: String = "",
    val id: String = "",
    val userImage: String = "",
    val userBio: String = "",
    val designation: String?=null,
)



