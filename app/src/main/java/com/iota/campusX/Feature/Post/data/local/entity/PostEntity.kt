package com.iota.campusX.Feature.Post.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val postId: String,
    val caption: String,
    val authorId: String,
    val authorName: String,
    val authorProfileUrl: String,
    val authorTagline: String? = null,
    val isCurrentUser: Boolean = false,
    val isLiked: Boolean = false,
    val attachmentJson: String? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: String,
    val updatedAt: String,
)
