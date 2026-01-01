package com.iota.campusX.Feature.UserProfile.data.remote.api

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.ConnectionResponse

class ConnectionsPagingSource(
    private val api: ConnectionApi,
    private val userId: String,
) : PagingSource<Int, ConnectionResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ConnectionResponse> {

        val fixedPageSize = 10

        val page = params.key ?: 0

        return try {

            val result =  api.getPagedConnection(userId, page, fixedPageSize)

            result.fold(
                onSuccess = { pageResponse ->
                    Log.d("CONNECTION_LIST","${pageResponse.content}")
                    LoadResult.Page(
                        data = pageResponse.content,
                        prevKey = if (page == 0) null else page - 1,
                        nextKey = if (pageResponse.last) null else page + 1
                    )
                },
                onFailure = { LoadResult.Error(it) }
            )
        } catch (e: Exception) {
            Log.d("CONNECTION_LIST","${e.message}")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ConnectionResponse>): Int? {
        return state.anchorPosition?.let { position ->
            val anchorPage = state.closestPageToPosition(position)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}