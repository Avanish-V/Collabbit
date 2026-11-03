package com.iota.campusX.Feature.Post.data.mediator


import androidx.paging.PagingSource
import androidx.paging.PagingState

import com.iota.campusX.Feature.Post.data.remote.request.posts
import com.iota.campusX.Feature.Post.data.remote.api.PostApi
import com.iota.campusX.Feature.Post.data.remote.response.Post

class PostsPagingSource(
    private val api: PostApi,
    private val userId: String?= null,
) : PagingSource<Int, Post>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {

        val fixedPageSize = 10

        val page = params.key ?: 0

        return try {

            val result = api.getPostByUserId(userId, page,fixedPageSize)

            result.fold(
                onSuccess = { pageResponse ->
                    val data = pageResponse.content

                    LoadResult.Page(
                        data = data,
                        prevKey = if (page == 0) null else page - 1,
                        nextKey = if (pageResponse.last) null else page + 1
                    )
                },
                onFailure = { LoadResult.Error(it) }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        return state.anchorPosition?.let { position ->
            val anchorPage = state.closestPageToPosition(position)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}



