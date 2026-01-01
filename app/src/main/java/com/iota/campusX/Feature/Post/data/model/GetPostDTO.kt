package com.iota.campusX.Feature.Post.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GetPostDTO(
    val postId: String = "",
    val createdAt: Long? = null, // or Date with Contextual
    val creatorDetail: CreatorDetail = CreatorDetail(),
    val feedMode: FeedMode = FeedMode.OPEN,
    val reference: Reference?=null,
    val visibilityMode: VisibilityMode = VisibilityMode.USER,
    val campusId: String?=null,
    val postContent: PostContent = PostContent(),
    val postActions: PostActions = PostActions(),
    val type: Type = Type.MEDIA,
)


@Serializable
data class CreatorDetail(
    val isCurrentUser: Boolean = false,
    val isVerified: Boolean = false,
    val isFollow: Boolean = false,
    val isPremium: Boolean = false,
    val isAlumni: Boolean = false,
    val profile: UserBasicDetail? = null
)


@Serializable
data class UserBasicDetail(
    val name: String = "",
    val id: String = "",
    val image: String? = "",
    val tagline: String = "",
)



