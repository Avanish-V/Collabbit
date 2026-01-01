package com.iota.campusX.Feature.Search.Data


import androidx.paging.PagingSource
import androidx.paging.PagingState

class SearchPagingSource(
    private val api: UserSearchApi,
    private val query: String
) : PagingSource<Int, SearchResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResponse> {

        val fixedPageSize = 10

        val page = params.key ?: 0

        return try {

            val result = api.searchUser(query)
                .fold(
                    onSuccess = { it },
                    onFailure = { return LoadResult.Error(it) }
                )

            LoadResult.Page(
                data = result.content,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (result.last) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, SearchResponse>): Int? {
        return state.anchorPosition?.let { position ->
            val anchorPage = state.closestPageToPosition(position)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}



