package com.iota.campusX.Feature.UserProfile.data

import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionsDTO(
    val user: UserDetail
)
