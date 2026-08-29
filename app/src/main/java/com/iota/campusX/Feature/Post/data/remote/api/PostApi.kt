package com.iota.campusX.Feature.Post.data.remote.api

import android.util.Log
import com.iota.campusX.Feature.Post.data.remote.mapper.PostRes
import com.iota.campusX.Feature.Post.data.remote.response.Post
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val number: Int,
    val totalPages: Int,
    val totalElements: Int? = null,
    val size: Int? = null,
    val first: Boolean? = null,
    val last: Boolean
)

class PostApi(private val client: HttpClient) {

    private val TAG = "PostApi"

    suspend fun getPosts(page: Int = 0, size: Int = 10): Result<PageResponse<PostRes>> {
        return runCatching {
            val url = "feed/all/$page/$size"
            Log.d(TAG, "Fetching posts from: $url")
            val response = client.get(url)
            
            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                Log.e(TAG, "Error fetching posts: ${response.status.value} - $errorBody")
                throw Exception("API Error ${response.status.value}: $errorBody")
            }
            response.body<PageResponse<PostRes>>()
        }.onFailure {
            Log.e(TAG, "Exception in getPosts: ${it.message}", it)
        }
    }

    suspend fun getPostByUserId(userId: String? = null, page: Int = 0, size: Int = 10): Result<PageResponse<Post>> {
        return runCatching {
            val url = "feed/postById/$userId/$page/$size"
            Log.d(TAG, "Fetching user posts from: $url")
            val response = client.get(url)

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                Log.e(TAG, "Error fetching user posts: ${response.status.value} - $errorBody")
                throw Exception("API Error ${response.status.value}: $errorBody")
            }
            response.body<PageResponse<Post>>()
        }.onFailure {
            Log.e(TAG, "Exception in getPostByUserId: ${it.message}", it)
        }
    }
}
