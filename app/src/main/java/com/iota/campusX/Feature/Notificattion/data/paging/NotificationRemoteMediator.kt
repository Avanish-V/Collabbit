package com.iota.campusX.Feature.Notificattion.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.iota.campusX.Feature.Notificattion.data.api.NotificationApi
import com.iota.campusX.Feature.Notificattion.data.local.NotificationDatabase
import com.iota.campusX.Feature.Notificattion.data.local.dao.NotificationDao
import com.iota.campusX.Feature.Notificattion.data.local.dao.NotificationSyncDao
import com.iota.campusX.Feature.Notificattion.data.local.entity.NotificationEntity
import com.iota.campusX.Feature.Notificattion.data.local.entity.NotificationSyncEntity
import com.iota.campusX.Feature.Notificattion.data.local.mapper.toEntity
import okio.IOException

@OptIn(ExperimentalPagingApi::class)
class NotificationRemoteMediator(

    private val api: NotificationApi,
    private val database: NotificationDatabase,
    private val dao: NotificationDao,
    private val syncDao: NotificationSyncDao

) : RemoteMediator<Int, NotificationEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NotificationEntity>
    ): MediatorResult {

        return try {

            when (loadType) {

                LoadType.REFRESH -> {

                    val response = api.getNotifications(
                        page = 0,
                        size = state.config.pageSize
                    )

                    database.withTransaction {

                        dao.clearAll()

                        dao.insertAll(
                            response.content.map { it.toEntity() }
                        )

                        syncDao.save(
                            NotificationSyncEntity(
                                currentPage = 0,
                                lastSyncTime = System.currentTimeMillis()
                            )
                        )
                    }

                    if (response.content.isEmpty()) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }

                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                }

                LoadType.APPEND -> {

                    val sync = syncDao.get()
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = true
                        )

                    val nextPage = sync.currentPage + 1

                    val response = api.getNotifications(
                        page = nextPage,
                        size = state.config.pageSize
                    )

                    dao.insertAll(
                        response.content.map { it.toEntity() }
                    )

                    syncDao.save(
                        sync.copy(
                            currentPage = nextPage,
                            lastSyncTime = System.currentTimeMillis()
                        )
                    )

                    return MediatorResult.Success(
                        endOfPaginationReached = response.last
                    )
                }
            }

            MediatorResult.Success(
                endOfPaginationReached = true
            )

        } catch (e: Exception) {

            MediatorResult.Error(e)

        }
        catch (e: IOException){
            MediatorResult.Error(e)
        }
    }
}