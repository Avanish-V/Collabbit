package com.iota.campusX.Feature.Post.domain.attachment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("DOCUMENT")
data class DocumentAttachmentDto(
    val url: String,
    val name: String,
    val size: Long,
    val thumbnailUrl: String? = null,
    val pageCount: Int = 0
): AttachmentDto
