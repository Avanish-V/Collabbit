package com.iota.campusX.Feature.Post.domain.attachment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("VIDEO")
data class VideoAttachmentDto(
    val videoUrl: String,
    val thumbnailUrl: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val aspectRatio: Float? = null
): AttachmentDto
