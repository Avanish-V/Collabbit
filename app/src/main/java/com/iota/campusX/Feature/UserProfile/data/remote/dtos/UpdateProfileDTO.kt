package com.iota.campusX.Feature.UserProfile.data.remote.dtos

import android.net.Uri

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileDTO(
    var updateUserName: String? = null,
    val updateAbout: String? = null,
    val updateTagline: String? = null,
    val updateGender: Gender? = null,
)
