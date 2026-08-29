package com.iota.campusX.Feature.Post.domain.attachment

import com.iota.campusX.Feature.Post.data.remote.request.AttachmentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("IMAGE")
data class ImageAttachmentDto(
    val images : List<String>
): AttachmentDto
