package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UserResponse(
    @SerialName("status")val status: Boolean,
    @SerialName("message")val message: String? = null,
    @SerialName("error")val error: String? = null
)

