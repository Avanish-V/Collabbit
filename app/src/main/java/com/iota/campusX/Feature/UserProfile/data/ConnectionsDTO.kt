package com.iota.campusX.Feature.UserProfile.data

import com.iota.campusX.Feature.Post.domain.User
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionsDTO(
    val user: User
)
