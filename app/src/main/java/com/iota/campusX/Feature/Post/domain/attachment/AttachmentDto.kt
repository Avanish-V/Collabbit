package com.iota.campusX.Feature.Post.domain.attachment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface AttachmentDto

@Serializable
@SerialName("POLL")
data class PollAttachmentDto(
    val options: List<String>
): AttachmentDto
