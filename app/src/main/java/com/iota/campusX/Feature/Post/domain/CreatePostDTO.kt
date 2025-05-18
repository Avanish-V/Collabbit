package com.iota.campusX.Feature.Post.domain

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

