package com.iota.campusX.Feature.Collab.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.Collab.domain.repository.CollabRepository

class CollabPagingSource(
    private val repository: CollabRepository,
    private val type: String,
    private val query: String = ""
) : PagingSource<Int, CollabResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CollabResponse> {
        val page = params.key ?: 0
        val result = repository.getCollabs(
            type = type,
            query = query,
            page = page,
            size = params.loadSize
        )

        return result.fold(
            onSuccess = { pageResponse ->
                LoadResult.Page(
                    data = pageResponse.content,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (pageResponse.last || pageResponse.content.isEmpty()) null else page + 1
                )
            },
            onFailure = { LoadResult.Error(it) }
        )
    }

    override fun getRefreshKey(state: PagingState<Int, CollabResponse>): Int? {
        return state.anchorPosition?.let { position ->
            val anchorPage = state.closestPageToPosition(position)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
