package com.iota.campusX.Feature.Opportunities.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModuleResponse(
    @SerialName("id") val id: String? = null,
    @SerialName("course_id") val courseId: String? = null,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("duration") val duration: String? = null,
    @SerialName("detail") val detail: String? = null,
    @SerialName("order") val order: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)
