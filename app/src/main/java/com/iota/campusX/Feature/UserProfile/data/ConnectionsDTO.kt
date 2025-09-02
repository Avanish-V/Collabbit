package com.iota.campusX.Feature.UserProfile.data

import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionsDTO(
    val user: UserBasicDetail
)
