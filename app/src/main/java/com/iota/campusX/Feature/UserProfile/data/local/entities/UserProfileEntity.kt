package com.iota.campusX.Feature.UserProfile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Campus
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.SetCampus
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.UpdateProfileViewModel

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val uid: String,         // must always exist
    val token: String? = null,              // fallback to ""
    val name: String = "",               // fallback to ""
    val image: String? = null,           // optional
    val email: String = "",              // fallback to ""
    val about: String? = null, // optional
    val tagline: String? = null,
    val campus: Campus? = null,
)