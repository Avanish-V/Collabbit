package com.iota.campusX.Feature.Post.domain.attachment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Attachment

@Serializable
@SerialName("IMAGE")
data class ImageAttachment(
    val images : List<String>
): Attachment

@Serializable
@SerialName("POLL")
data class PollAttachment(
    val options: List<String>
): Attachment

@Serializable
@SerialName("TEAM_FORMATION")
data class TeamFormationAttachment(
    val teamType: String,
    val requiredSkills: List<String>
): Attachment
