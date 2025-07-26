package com.iota.campusX.Feature.UserProfile.data

import kotlinx.serialization.Serializable

@Serializable
data class UniversityDTO(
    val name: String = "",
    val domain: String = "",
    val logo: String = ""
)
