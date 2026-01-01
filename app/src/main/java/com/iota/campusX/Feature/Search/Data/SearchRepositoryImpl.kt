package com.iota.campusX.Feature.Search.Data

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.remote.PageResponse
import com.iota.campusX.Feature.Post.domain.models.PostResponse
import com.iota.campusX.Feature.Search.Domain.Models.UserSearchDTO
import com.iota.campusX.Feature.Search.Domain.SearchRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import okio.IOException
import kotlin.coroutines.cancellation.CancellationException

class SearchRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val httpClient: HttpClient
): SearchRepository {

    override fun userSearch(query: String): Flow<PagingData<SearchResponse>> {

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                SearchPagingSource(
                    api = UserSearchApi(auth = auth, client = httpClient),
                    query = query
                )
            }
        ).flow

    }

    override fun postSearch(query: String): Flow<PagingData<GetPostDTO>> {

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                SearchPostsPagingSource(
                    api = UserSearchApi(auth = auth, client = httpClient),
                    query = query
                )
            }
        ).flow

    }


}