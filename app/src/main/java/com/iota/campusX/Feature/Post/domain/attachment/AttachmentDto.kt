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

@Serializable
@SerialName("TEAM_FORMATION")
data class TeamFormationAttachmentDto(
    val teamType: String,
    val requiredSkills: List<String>
): AttachmentDto
