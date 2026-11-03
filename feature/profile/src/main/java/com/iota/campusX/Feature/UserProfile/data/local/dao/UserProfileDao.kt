package com.iota.campusX.Feature.UserProfile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE isCurrentUser = 1 LIMIT 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getProfile(): UserProfileEntity?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _insertProfile(profile: UserProfileEntity)

    /**
     * Smart insert that prevents aura point regressions from stale general profile syncs.
     * @param force If true, bypasses the regression check and overwrites points with authoritative server data.
     */
    @Transaction
    suspend fun insertProfile(profile: UserProfileEntity, force: Boolean = false) {
        val existing = getProfile()
        if (existing != null && existing.uid == profile.uid && !force) {
            // If the incoming aura points are less than what we have locally, 
            // we assume the incoming data is stale and preserve our local state.
            val finalProfile = if (profile.auraPoints < existing.auraPoints) {
                profile.copy(
                    auraPoints = existing.auraPoints,
                    auraLevel = existing.auraLevel
                )
            } else {
                profile
            }
            _insertProfile(finalProfile)
        } else {
            _insertProfile(profile)
        }
    }

    /**
     * Partial update — only overwrites the two aura columns.
     * Prevents regressions from stale aggregator endpoints (e.g. aura/me).
     */
    @Transaction
    suspend fun updateAura(auraPoints: Int, auraLevel: String) {
        val existing = getProfile()
        if (existing != null && auraPoints < existing.auraPoints) {
            // Ignore stale updates that would cause a point regression jump in UI
            return
        }
        _updateAura(auraPoints, auraLevel)
    }

    @Query("""
        UPDATE user_profile
           SET aura_points = :auraPoints,
               aura_level  = :auraLevel
         WHERE isCurrentUser = 1
    """)
    suspend fun _updateAura(auraPoints: Int, auraLevel: String)
}

