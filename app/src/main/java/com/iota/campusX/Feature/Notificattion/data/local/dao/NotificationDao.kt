package com.iota.campusX.Feature.Notificattion.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iota.campusX.Feature.Notificattion.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("""
        SELECT *
        FROM notifications
        ORDER BY createdAt DESC
    """)
    fun pagingSource(): PagingSource<Int, NotificationEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        notifications: List<NotificationEntity>
    )


    @Query("""
        UPDATE notifications
        SET isRead = 1
        WHERE id = :id
    """)
    suspend fun markRead(id: Long)


    @Query("""
        UPDATE notifications
        SET isRead = 1
    """)
    suspend fun markAllRead()


    @Query("""
        DELETE FROM notifications
    """)
    suspend fun clear()


    @Query("""
        SELECT COUNT(*)
        FROM notifications
        WHERE isRead = 0
    """)
    fun observeUnreadCount(): Flow<Long>


    @Query("""
        SELECT COUNT(*)
        FROM notifications
        WHERE isRead = 0
    """)
    suspend fun unreadCount(): Long

    @Query("DELETE FROM notifications")
    suspend fun clearAll()

    @Query("""
SELECT EXISTS(
    SELECT 1
    FROM notifications
    WHERE id = :id
)
""")
    suspend fun exists(
        id: Long
    ): Boolean

}