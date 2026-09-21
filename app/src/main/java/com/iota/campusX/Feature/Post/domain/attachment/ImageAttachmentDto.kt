package com.iota.campusX.Feature.Post.domain.attachment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("IMAGE")
data class ImageAttachmentDto(
    val images: List<String>,
    val widths: List<Int>? = null,
    val heights: List<Int>? = null,
    val aspectRatios: List<Float>? = null
): AttachmentDto
