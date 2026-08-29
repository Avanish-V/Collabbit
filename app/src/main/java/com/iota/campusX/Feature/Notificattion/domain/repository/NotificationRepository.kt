package com.iota.campusX.Feature.Notificattion.domain.repository

import androidx.paging.PagingData
import com.iota.campusX.Feature.Notificattion.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {

     fun getNotifications(): Flow<PagingData<Notification>>

    fun observeUnreadCount(): Flow<Long>

    suspend fun markRead(id: Long)

    suspend fun unreadCount(): Long

    suspend fun markAllRead()

    suspend fun syncNotification(
        notificationId: Long
    )
}