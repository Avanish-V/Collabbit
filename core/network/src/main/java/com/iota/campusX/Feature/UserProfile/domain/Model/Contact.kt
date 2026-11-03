package com.iota.campusX.Feature.UserProfile.domain.Model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Contact(
    @SerialName("email") val email: String = "",
    @SerialName("phoneNumber") val phoneNumber: String = "",
)
