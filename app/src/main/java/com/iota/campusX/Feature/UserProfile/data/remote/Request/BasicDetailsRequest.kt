package com.iota.campusX.Feature.UserProfile.data.remote.Request

import com.iota.campusX.Feature.UserProfile.domain.Model.Gender
import kotlinx.serialization.Serializable

@Serializable
data class BasicDetailsRequest(
    val name: String,
    var photo: String,
    val tagline: String,
    val gender: Gender
)
