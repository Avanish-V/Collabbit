package com.iota.campusX.Feature.UserProfile.OfflineSupport

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.Counts
import com.iota.campusX.Feature.UserProfile.data.Gender
import com.iota.campusX.Feature.UserProfile.data.MetaData

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val token: String,
    val userName: String,
    val userImage: String,
    val userEmail: String,
    val userBio: String,
    val userGender: Gender,
    val interests: List<String>,
    val metaData: MetaData,
    val campus: Campus?,
    val isRequestSent: Boolean?,
    val count: Counts?
)
