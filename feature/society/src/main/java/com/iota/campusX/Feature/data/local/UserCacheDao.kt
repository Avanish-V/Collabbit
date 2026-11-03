package com.iota.campusX.Feature.Society.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCacheDao {
    @Query("SELECT * FROM user_cache WHERE uid = :uid")
    fun getUser(uid: String): Flow<UserCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserCacheEntity)

    @Query("DELETE FROM user_cache WHERE uid = :uid")
    suspend fun deleteUser(uid: String)
}
