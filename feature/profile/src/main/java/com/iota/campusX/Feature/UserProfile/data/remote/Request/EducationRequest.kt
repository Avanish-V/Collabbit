package com.iota.campusX.Feature.UserProfile.data.remote.Request

import kotlinx.serialization.Serializable

@Serializable
data class EducationRequest(
    val college: String,
    val course: String,
    val specialization:String,
    val cgpa: String,
    val start: String,
    val end: String,
)
