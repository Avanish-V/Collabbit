package com.iota.campusX.Feature.Post.data.local.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RemoteKeysDao {

    @Query("SELECT * FROM remote_keys WHERE id='feed'")
    suspend fun getRemoteKeys(): RemoteKeys?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: RemoteKeys)

    @Query("DELETE FROM remote_keys")
    suspend fun clear()
}