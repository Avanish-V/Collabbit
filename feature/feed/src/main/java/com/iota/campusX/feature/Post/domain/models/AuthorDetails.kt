package com.iota.campusX.Feature.Post.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthorDetails(
    val authorId: String,
    val authorName: String,
    val authorImage: String,
    val authorTagline: String? = null,
    val isVerified: Boolean ? = null,
    val isCurrentUser: Boolean = false ,
)
