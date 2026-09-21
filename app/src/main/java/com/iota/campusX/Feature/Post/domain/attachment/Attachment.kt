package com.iota.campusX.Feature.Post.domain.attachment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Attachment

@Serializable
@SerialName("IMAGE")
data class ImageAttachment(
    val images : List<String>,
    val widths: List<Int> = emptyList(),
    val heights: List<Int> = emptyList(),
    val aspectRatios: List<Float> = emptyList()
): Attachment

@Serializable
@SerialName("VIDEO")
data class VideoAttachment(
    val videoUri: String,
    val width: Int = 0,
    val height: Int = 0,
    val aspectRatio: Float = 1f
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

@Serializable
@SerialName("DOCUMENT")
data class DocumentAttachment(
    val uri: String,
    val name: String,
    val size: Long,
    val thumbnailUrl: String? = null,
    val pageCount: Int = 0
): Attachment
