package com.iota.campusX.Feature.UserProfile.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Campus
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.SetCampus
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.UpdateProfileViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE uid = :id LIMIT 1")
    fun getProfile(id: String): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)


    @Query("UPDATE user_profile SET name = :name")
    suspend fun updateName(id: String, name: String) {
        val profile = getProfile(id).first()
        profile?.let {
            insertProfile(it.copy(name = name))
        }
    }

    @Query("UPDATE user_profile SET about = :about")
    suspend fun updateAbout(id: String, about: String) {
        val profile = getProfile(id).first()
        profile?.let {
            insertProfile(it.copy(about = about))
        }
    }

    @Query("UPDATE user_profile SET tagline = :tagline")
    suspend fun updateTagline(id: String, tagline: String) {
        val profile = getProfile(id).first()
        profile?.let {
            insertProfile(it.copy(tagline = tagline))
        }
    }

    @Query("UPDATE user_profile SET campus = :campus")
    suspend fun updateCampus(campus: Campus, id: String) {
        val profile = getProfile(id).first()
        profile?.let {
            insertProfile(it.copy(campus = campus))
        }

    }


}