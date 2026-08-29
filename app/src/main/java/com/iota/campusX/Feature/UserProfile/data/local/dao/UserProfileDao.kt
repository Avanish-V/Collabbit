package com.iota.campusX.Feature.UserProfile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile")
    fun observeProfile(): Flow<UserProfileEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    /**
     * Partial update — only overwrites the two aura columns.
     * Runs without re-serialising the base profile JSON blobs.
     */
    @Query("""
        UPDATE user_profile
           SET aura_points = :auraPoints,
               aura_level  = :auraLevel
         WHERE uid = (SELECT uid FROM user_profile LIMIT 1)
    """)
    suspend fun updateAura(auraPoints: Int, auraLevel: String)
}
