package com.iota.campusX.Feature.UserProfile.data.remote.dtos

import com.iota.campusX.Feature.UserProfile.ui.viewmodels.UpdateProfileViewModel
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val uid: String,
    val name: String,
    val email: String,
    val image: String?,
    val about: String?,
    val tagline: String?,
    val gender: Gender,
    val lastLogin: Long,
    val campus: Campus?,
    val followersCount: Long,
    val connectionsCount: Long
)
