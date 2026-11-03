package com.iota.campusX.Feature.Society.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_cache")
data class UserCacheEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val avatarUrl: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)
