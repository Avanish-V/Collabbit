package com.iota.campusX.Feature.Notificattion.data.pagingsource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.iota.campusX.Feature.Notificattion.data.api.NotificationApi
import com.iota.campusX.Feature.Notificattion.data.mapper.toDomain
import com.iota.campusX.Feature.Notificattion.domain.model.Notification

class NotificationPagingSource(
    private val api: NotificationApi
) : PagingSource<Int, Notification>() {

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, Notification> {

        return try {

            val page = params.key ?: 0
            Log.d("NotificationPaging", "Loading page $page with size ${params.loadSize}")

            val response = api.getNotifications(
                page = page,
                size = params.loadSize
            )

            Log.d("NotificationPaging", "Loaded ${response.content} notifications. Last: ${response.last}")

            LoadResult.Page(
                data = response.content.map { it.toDomain() },
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (response.last) null else page + 1
            )

        } catch (e: Exception) {
            Log.e("NotificationPaging", "Error loading notifications: ${e.message}", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(
        state: PagingState<Int, Notification>
    ): Int? {

        return state.anchorPosition?.let { anchor ->

            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}