package com.iota.campusX.Feature.Search.Data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.remote.PageResponse
import com.iota.campusX.Feature.Post.domain.models.PostResponse
import com.iota.campusX.Feature.Search.Domain.Models.UserSearchDTO
import com.iota.campusX.Koin.END_POINT
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.tasks.await
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class UserSearchApi(private val auth: FirebaseAuth, private val client: HttpClient) {

    suspend fun searchUser(query: String): Result<PageResponse<SearchResponse>> {

        val token = try {
            auth.currentUser?.getIdToken(true)?.await()?.token
                ?: throw IllegalStateException("Failed to obtain auth token")
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {

            val response: HttpResponse = client.get("$END_POINT/api/v1/search/users") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                parameter("q", query)
            }

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Failed: ${response.status}"))
            }
            val pageResponse: PageResponse<SearchResponse> = Json { ignoreUnknownKeys = true }.decodeFromString(response.bodyAsText())
            Result.success(pageResponse)
        } catch (e: IOException) {
            Result.failure(Exception("Network error!"))
        } catch (e: Exception) {
            Result.failure(Exception("Something went wrong!"))
        }

    }

    suspend fun getPosts(query: String, page: Int = 0, size: Int = 10): Result<PageResponse<PostResponse>> {


        val endpoint = "$END_POINT/api/v1/search/posts"

        val token = try {
            auth.currentUser?.getIdToken(true)?.await()?.token ?: throw IllegalStateException("Failed to obtain auth token")
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {

            val response: HttpResponse = client.get(endpoint) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                parameter("q", query)
            }

            when(response.status.value){

                200->{
                    val pageResponse: PageResponse<PostResponse> = Json { ignoreUnknownKeys = true }.decodeFromString(response.bodyAsText())
                    Result.success(pageResponse)
                }
                else -> {
                    return Result.failure(Exception("Failed: ${response.status}"))
                }

            }

        } catch (e: okio.IOException){
            Result.failure(Exception("Network error!"))
        } catch (e: Exception) {
            Result.failure(Exception("Something went wrong!"))
        }
    }

}

@Serializable
data class SearchResponse(
    val uid: String,
    val name: String,
    val image: String,
    val tagline: String,
    val about: String
)