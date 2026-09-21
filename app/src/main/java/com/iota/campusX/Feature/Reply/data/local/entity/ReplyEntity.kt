package com.iota.campusX.Feature.Reply.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "replies")
data class ReplyEntity(
    @PrimaryKey
    val id: String,
    val postId: String,
    val content: String,
    val imageUrl: String? = null,
    val authorId: String,
    val authorName: String,
    val authorProfileUrl: String?,
    val authorTagline: String? = null,
    val mentionedUserId: String? = null,
    val mentionedUserName: String? = null,
    val mentionedUserProfileUrl: String? = null,
    val parentReplyId: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val childCount: Int = 0,
    val isAuthor: Boolean = false,
    val deleted: Boolean = false
)
