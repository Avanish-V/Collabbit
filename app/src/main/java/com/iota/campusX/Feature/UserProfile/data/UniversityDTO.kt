package com.iota.campusX.Feature.UserProfile.data

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class UniversityDTO(
    val name: String = "",
    val domain: String = "",
    val logo: String = ""
)
