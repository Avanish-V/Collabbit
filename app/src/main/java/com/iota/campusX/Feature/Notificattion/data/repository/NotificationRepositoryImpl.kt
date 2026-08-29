package com.iota.campusX.Feature.Notificattion.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.iota.campusX.Feature.Notificattion.data.api.NotificationApi
import com.iota.campusX.Feature.Notificattion.data.local.NotificationDatabase
import com.iota.campusX.Feature.Notificattion.data.local.dao.NotificationDao
import com.iota.campusX.Feature.Notificattion.data.local.dao.NotificationSyncDao
import com.iota.campusX.Feature.Notificattion.data.local.entity.NotificationEntity
import com.iota.campusX.Feature.Notificattion.data.local.entity.NotificationSyncEntity
import com.iota.campusX.Feature.Notificattion.data.local.mapper.toDomain
import com.iota.campusX.Feature.Notificattion.data.local.mapper.toEntity
import com.iota.campusX.Feature.Notificattion.data.paging.NotificationRemoteMediator
import com.iota.campusX.Feature.Notificattion.data.pagingsource.NotificationPagingSource
import com.iota.campusX.Feature.Notificattion.domain.model.Notification
import com.iota.campusX.Feature.Notificattion.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepositoryImpl(
    private val api: NotificationApi,
    private val dao: NotificationDao,
    private val syncDao: NotificationSyncDao,
    private val database: NotificationDatabase

    ) : NotificationRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getNotifications(): Flow<PagingData<Notification>> {

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 2,
                enablePlaceholders = false
            ),
            remoteMediator = NotificationRemoteMediator(
                api = api,
                dao = dao,
                syncDao = syncDao,
                database = database
            ),
            pagingSourceFactory = {
                dao.pagingSource()
            }
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                entity.toDomain()
            }
        }
    }

    override fun observeUnreadCount() = dao.observeUnreadCount()

    override suspend fun syncNotification(
        notificationId: Long
    ) {
        try {
            if (dao.exists(notificationId)) {
                return
            }

            val notification = api.getNotification(notificationId)

            dao.insert(
                notification.toEntity()
            )

            Log.d("NotificationRepository", "Synced notification $notificationId")
        } catch (e: Exception) {
            // Log the error but don't crash the app
            println("NotificationRepository: Error syncing notification $notificationId: ${e.message}")
        }
    }

    override suspend fun unreadCount(): Long {
        return api.unreadCount()
    }

    override suspend fun markRead(id: Long) {
        api.markRead(id)
        dao.markRead(id)
    }

    override suspend fun markAllRead() {

        api.markAllRead()
        dao.markAllRead()

    }

}