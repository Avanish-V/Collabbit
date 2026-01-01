package com.iota.campusX.Feature.Post.data.remote


import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.domain.models.PostResponse
import com.iota.campusX.Koin.END_POINT
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.IOException

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


class PostApi(private val client: HttpClient, private val auth: FirebaseAuth) {
    suspend fun getPosts(mode: FeedMode?= null, campusId: String? = null, page: Int = 0, size: Int = 10): Result<PageResponse<PostResponse>> {

        if (mode == null){
            return Result.failure(IllegalArgumentException("Invalid feed mode"))
        }

        val endpoint = when (mode) {
            FeedMode.CAMPUS -> "$END_POINT/posts/$mode/$page/$size"
            FeedMode.OPEN -> "$END_POINT/posts/${mode.name.uppercase()}/$page/$size"
        }

        val token = try {
            auth.currentUser?.getIdToken(true)?.await()?.token ?: throw IllegalStateException("Failed to obtain auth token")
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {
            val response: HttpResponse = client.get(endpoint) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
            }
            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Failed: ${response.status}"))
            }
            val pageResponse: PageResponse<PostResponse> = Json { ignoreUnknownKeys = true }.decodeFromString(response.bodyAsText())
            Result.success(pageResponse)
        } catch (e: IOException){
            Log.d("CONNECTION_ERROR",e.message.toString())
            Result.failure(Exception("Network error!"))
        } catch (e: Exception) {
            Result.failure(Exception("Something went wrong!"))
        }
    }
    suspend fun getPostByUserId(userId: String? = null, page: Int = 0, size: Int = 10): Result<PageResponse<PostResponse>> {

        Log.e("PostApi", "Exception fetching posts: ${userId}")

        val token = try {
            auth.currentUser?.getIdToken(true)?.await()?.token
                ?: throw IllegalStateException("Failed to obtain auth token")
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {

            val response: HttpResponse = client.get("$END_POINT/posts/postById/$userId/$page/$size") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
            }

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Failed to get posts"))
            }

            val postResponse: PageResponse<PostResponse> = Json { ignoreUnknownKeys = true }.decodeFromString(response.bodyAsText())

            if (response.status.value == 200){
                Result.success(postResponse)
            }else{
                Result.failure(Exception("Failed to get posts: ${response.status}"))
            }

        } catch (e: Exception) {

            return Result.failure(e)

        }
    }

}