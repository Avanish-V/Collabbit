package com.iota.campusX.Feature.Post.domain

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
data class PostDTO(
    val postId: String = "",
    val postedAt: Long = 0L,
    val creatorDetail: CreatorDetail,
    val reference: Reference,
    val postMode: String?=null,
    val campusId: String?=null,
    val postContent: PostContent,
    val postActions: PostActions
){
    @Keep
    constructor() : this(
        postId = "",
        postedAt = 0L,
        creatorDetail = CreatorDetail(),
        reference = Reference(),
        postMode = null,
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
    val type : String = "",
    val profile: User? = null
)


@Serializable
data class User(
    val userName: String = "",
    val id: String = "",
    val userImage: String = "",
    val about: String = "",
    val designation: String?=null,
    val isCurrentUser: Boolean?=false,
)



