package com.iota.campusX.Feature.UserProfile.data

import androidx.annotation.Keep
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class ConnectionsDTO(
    val userName: String = "",
    val id: String = "",
    val userImage:String = "",
    val userBio:String = "",
    val isCurrentProfile: Boolean = false,
    val isCurrentUser: Boolean = false,
)
