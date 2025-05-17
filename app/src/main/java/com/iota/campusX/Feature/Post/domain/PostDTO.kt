package com.iota.campusX.Feature.Post.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
data class PostDTO(
    val postId: String,
    val postedAt: Long,
    val creatorDetail: CreatorDetail,
    val reference: Reference,
    val postMode: String?=null,
    val postContent: PostContent,
    val postActions: PostActions
):Parcelable


@Serializable
@Parcelize
data class CreatorDetail(
    val isCurrentUser: Boolean = false,
    val isVerified: Boolean = false,
    val isPremium: Boolean = false,
    val type : String = "",
    val profile: User? = null
): Parcelable


@Parcelize
@Serializable
data class PostData(
    val postText: String = "",
    val postImage: String = "",
): Parcelable


@Parcelize
@Serializable
data class PostContent(
    val postType: String = "",
    val postData: PostData = PostData()
):Parcelable


@Parcelize
@Serializable
data class User(
    val userName: String = "",
    val _id: String = "",
    val userImage: String = "",
    val designation: String?=null,
):Parcelable


@Parcelize
@Serializable
data class PostActions(
    var isLiked: Boolean = false,
    val likesCount: Int = 0,
    val replies: List<String> = emptyList(),
    val replyCount: Int = 0
):Parcelable


@Parcelize
@Serializable
data class Reference(
    val icon: String= "",
    val title: String = ""
):Parcelable

