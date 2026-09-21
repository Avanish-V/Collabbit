package com.iota.campusX.Feature.Opportunities.data.model

import com.iota.campusX.Utils.FlexibleStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstructorProfileResponse(
    @SerialName("uid") val uid: String,
    @SerialName("name") val name: String,
    @SerialName("avatarUrl") val avatarUrl: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("location") val location: String? = null
)

@Serializable
data class CourseResponse(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("instructor") val instructor: String,
    @SerialName("host") val host: String? = null,
    @SerialName("instructorProfile") val instructorProfile: InstructorProfileResponse? = null,
    @SerialName("description") val description: String,
    @SerialName("duration") val duration: String,
    @Serializable(with = FlexibleStringSerializer::class)
    @SerialName("price") val price: String,
    @SerialName("thumbnail") val thumbnail: String? = null,
    @SerialName("sessionStatus") val sessionStatus: String? = null,
    @SerialName("skillState") val skillState: String? = null,
    @SerialName("liveUrl") val liveUrl: String? = null,
    @SerialName("active") val active: Boolean,
    @SerialName("isJoinLinkEnabled") val isJoinLinkEnabled: Boolean = false,
    @SerialName("category") val category: String,
    @SerialName("level") val level: String,
    @SerialName("summary") val summary: String? = null,
    @SerialName("seats") val seats: Int? = null,
    @SerialName("enrolled") val enrolled: Int? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("time") val time: String? = null,
    @SerialName("meetLink") val meetLink: String? = null,
    @SerialName("modules") val modules: List<ModuleResponse> = emptyList(),
    @SerialName("posted_by") val postedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
