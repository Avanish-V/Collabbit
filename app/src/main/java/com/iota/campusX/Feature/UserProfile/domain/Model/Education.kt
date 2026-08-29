package com.iota.campusX.Feature.UserProfile.domain.Model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Education(
    val college: String,
    val course: String,
    val specialization:String,
    val cgpa: String,
    val start: String,
    val end: String,
)
