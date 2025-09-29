package com.iota.campusX.Feature.Post.data.model

import com.google.firebase.Timestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class GetPostDTO(
    val postId: String = "",
    @Contextual
    val createdAt: Timestamp? = null, // or Date with Contextual
    val creatorDetail: CreatorDetail = CreatorDetail(),
    val feedMode: FeedMode = FeedMode.OPEN,
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
    val isFollow: Boolean = false,
    val isPremium: Boolean = false,
    val isAlumni: Boolean = false,
    val profile: UserBasicDetail? = null
)


@Serializable
data class UserBasicDetail(
    val userName: String = "",
    val id: String = "",
    val userImage: String = "",
    val userBio: String = "",
)



