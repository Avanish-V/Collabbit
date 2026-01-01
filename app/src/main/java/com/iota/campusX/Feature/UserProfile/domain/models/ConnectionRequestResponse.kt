package com.iota.campusX.Feature.UserProfile.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionRequestResponse(
    val requestStatus: ConnectionViewStatus,
    val id: String
)
enum class ConnectionViewStatus {
    CONNECTED,
    REQUEST_SENT,
    REQUEST_RECEIVED,
    NOT_CONNECTED
}