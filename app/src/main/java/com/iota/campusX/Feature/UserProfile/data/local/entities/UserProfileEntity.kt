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
    val summary: String?,
    val isCurrentUser: Boolean,
    // Aura fields — added in schema version 4
    @ColumnInfo(name = "aura_points") val auraPoints: Int = 0,
    @ColumnInfo(name = "aura_level")  val auraLevel: String = "NEWCOMER"
)
