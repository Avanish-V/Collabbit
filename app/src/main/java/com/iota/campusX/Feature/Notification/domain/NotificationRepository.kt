package com.iota.campusX.Feature.Notification.domain

import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {

    fun fetchNotification(): Flow<ResultState<List<NotificationDTO>>>

    fun markNotificationAsRead()

    fun getNotificationCount(): Flow<ResultState<Int>>

    suspend fun deleteNotification(notificationId: String) : Result<Unit>

     fun observeTotalUnreadCount(): Flow<Int>

}