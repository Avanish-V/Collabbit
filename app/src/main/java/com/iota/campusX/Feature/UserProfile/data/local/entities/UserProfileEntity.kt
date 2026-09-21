package com.iota.campusX.Feature.UserProfile.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val uid: String,
    val baseProfile: String,
    val contact: String,
    val education: String?,
    val skills: String?,
    val matchPreferences: String?,
    val isCurrentUser: Boolean,
    @ColumnInfo(defaultValue = "''") val tagline: String = "",
    // Aura fields
    @ColumnInfo(name = "aura_points", defaultValue = "0") val auraPoints: Int = 0,
    @ColumnInfo(name = "aura_level", defaultValue = "'NEWCOMER'")  val auraLevel: String = "NEWCOMER"
)
