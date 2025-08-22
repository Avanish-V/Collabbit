package com.iota.campusX.Feature.Post.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import kotlinx.coroutines.tasks.await

class PostPagingSource(
    private val query: Query
) : PagingSource<DocumentSnapshot, GetPostDTO>() {

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, GetPostDTO> {
        return try {
            var currentQuery = query.limit(params.loadSize.toLong())

            // If we already have a "key", continue after it
            params.key?.let {
                currentQuery = currentQuery.startAfter(it)
            }

            val snapshot = currentQuery.get().await()
            val posts = snapshot.toObjects(GetPostDTO::class.java)

            val lastDoc = snapshot.documents.lastOrNull()

            LoadResult.Page(
                data = posts,
                prevKey = null, // only forward paging
                nextKey = lastDoc
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, GetPostDTO>): DocumentSnapshot? {
        // Called for refresh: return closest doc to anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.let {
                // we can’t easily map back to DocumentSnapshot, so just refresh from scratch
                null
            }
        }
    }
}
