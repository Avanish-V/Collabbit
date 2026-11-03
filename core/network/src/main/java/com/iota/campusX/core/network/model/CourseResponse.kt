package com.iota.campusX.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstructorDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("imageUrl") val imageUrl: String? = null
)

@Serializable
data class CourseResponse(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("instructorId") val instructorId: String? = null,
    @SerialName("instructor") val instructor: InstructorDto? = null,
    @SerialName("thumbnail") val thumbnail: String? = null,
    @SerialName("liveUrl") val liveUrl: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("active") val active: Boolean = true,
    @SerialName("isJoinLinkEnabled") val isJoinLinkEnabled: Boolean = false,
    @SerialName("seats") val seats: Int? = null,
    @SerialName("enrolledCount") val enrolledCount: Long? = null,
    @SerialName("scheduledDate") val scheduledDate: String? = null,
    @SerialName("startTime") val startTime: String? = null,
    @SerialName("endTime") val endTime: String? = null,
    @SerialName("enrolled") val enrolled: Boolean = false
) {
    val isEnrolled: Boolean get() = enrolled
    val meetLink: String? get() = liveUrl
    val date: String get() = scheduledDate.orEmpty()
    val time: String get() = buildString {
        if (!startTime.isNullOrBlank()) append(startTime)
        if (!endTime.isNullOrBlank()) {
            if (isNotEmpty()) append(" - ")
            append(endTime)
        }
    }
}
