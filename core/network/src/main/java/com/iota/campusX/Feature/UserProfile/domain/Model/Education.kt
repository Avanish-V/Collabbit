package com.iota.campusX.Feature.UserProfile.domain.Model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Education(
    @SerialName("college") val college: String = "",
    @SerialName("course") val course: String = "",
    @SerialName("specialization") val specialization: String = "",
    @SerialName("cgpa") val cgpa: String = "",
    @SerialName("start") val start: String = "",
    @SerialName("end") val end: String = "",
)
