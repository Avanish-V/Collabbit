package com.iota.campusX.Feature.Post.data.model

import com.google.firebase.Timestamp
import com.iota.campusX.Screens.Post.MediaType
import com.iota.campusX.Screens.Post.Type
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class GetPostDTO(
    val postId: String = "",
    @Contextual
    val createdAt: Timestamp? = null, // or Date with Contextual
    val creatorDetail: CreatorDetail = CreatorDetail(),
    val feedMode: FeedMode = FeedMode.GLOBAL,
    val reference: Reference?=null,
    val visibilityMode: VisibilityMode = VisibilityMode.USER,
    val campusId: String?=null,
    val postContent: PostContent = PostContent(),
    val postActions: PostActions = PostActions(),
    val type: Type = Type.Media,
    val mediaType: MediaType = MediaType.Image,
)


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



