package com.iota.campusX.Feature.Notificattion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iota.campusX.Feature.Notificattion.data.local.entity.NotificationSyncEntity

@Dao
interface NotificationSyncDao {

    @Query("""
        SELECT *
        FROM notification_sync
        WHERE id = 1
    """)
    suspend fun get(): NotificationSyncEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(
        entity: NotificationSyncEntity
    )

}