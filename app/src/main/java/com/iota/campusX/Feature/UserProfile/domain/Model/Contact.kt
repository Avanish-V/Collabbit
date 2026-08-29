package com.iota.campusX.Feature.UserProfile.domain.Model

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val email: String = "",
    val phoneNumber: String = "",
)
