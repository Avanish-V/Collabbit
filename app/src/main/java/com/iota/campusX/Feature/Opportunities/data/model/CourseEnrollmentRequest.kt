package com.iota.campusX.Feature.Opportunities.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseEnrollmentRequest(
    @SerialName("externalUserId") val externalUserId: String,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String
)
