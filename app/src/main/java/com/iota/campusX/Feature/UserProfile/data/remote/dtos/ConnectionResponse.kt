package com.iota.campusX.Feature.UserProfile.data.remote.dtos

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ConnectionResponse(
    val requestId: String,
    val uid: String,
    val name: String,
    val image: String?,
    val tagline: String?
)

@Serializable
data class ApiResponse<T>(
    val headers: Map<String, String> = emptyMap(),
    val body: T,
    val statusCode: String? = null,
    val statusCodeValue: Int? = null
)