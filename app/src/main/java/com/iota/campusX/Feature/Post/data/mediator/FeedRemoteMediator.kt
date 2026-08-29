package com.iota.campusX.Feature.Post.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.iota.campusX.Feature.Post.data.local.database.CampusDatabase
import com.iota.campusX.Feature.Post.data.local.entity.PostEntity
import com.iota.campusX.Feature.Post.data.local.entity.RemoteKeys
import com.iota.campusX.Feature.Post.data.local.mapper.toEntity
import com.iota.campusX.Feature.Post.data.remote.api.PostApi

@OptIn(ExperimentalPagingApi::class)
class FeedRemoteMediator(

    private val api: PostApi,

    private val database: CampusDatabase

) : RemoteMediator<Int, PostEntity>() {

    private val postDao = database.postDao()

    private val remoteKeysDao = database.remoteKeysDao()

    override suspend fun load(loadType: LoadType, state: PagingState<Int, PostEntity>): MediatorResult {

        return try {

            val page = when (loadType) {

                LoadType.REFRESH -> 0

                LoadType.PREPEND -> {

                    return MediatorResult.Success(
                        endOfPaginationReached = true
                    )

                }

                LoadType.APPEND -> {

                    val keys = remoteKeysDao.getRemoteKeys()

                    keys?.nextPage
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                }
            }

            val result = api.getPosts(
                page = page,
                size = state.config.pageSize
            )

            if (result.isFailure) {

                return MediatorResult.Error(
                    result.exceptionOrNull()!!
                )

            }

            val response = result.getOrThrow()

            val entities = response.content.map {

                it.toEntity()

            }

            database.withTransaction {

                if (loadType == LoadType.REFRESH) {

                    postDao.clear()

                    remoteKeysDao.clear()

                }

                postDao.insert(entities)

                remoteKeysDao.insert(

                    RemoteKeys(

                        nextPage = if (response.last)
                            null
                        else
                            page + 1

                    )

                )

            }

            MediatorResult.Success(endOfPaginationReached = response.last)

        } catch (e: Exception) {

            MediatorResult.Error(e)

        }

    }
}