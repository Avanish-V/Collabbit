package com.iota.campusX.Feature.Notification.domain

import androidx.paging.PagingData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.iota.campusX.Feature.Notification.data.CreateNotification
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {

    suspend fun fetchPagedNotification(): Flow<PagingData<GetNotification>>

    fun markNotificationAsRead()

    fun markRequestNotificationAsRead()

    fun getNotificationCount(): Flow<ResultState<Int>>

    fun getRequestNotificationCount(): Flow<ResultState<Int>>

    suspend fun deleteNotification(notificationId: String) : Result<Unit>

     fun observeTotalUnreadCount(): Flow<Int>

    suspend fun createNotification(createNotification: CreateNotification, creatorId: String): Result<Unit>


}
