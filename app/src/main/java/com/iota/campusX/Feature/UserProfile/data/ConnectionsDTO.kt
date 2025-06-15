package com.iota.campusX.Feature.UserProfile.data

import com.iota.campusX.Feature.Post.domain.Models.User
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionsDTO(
    val user: User
)
