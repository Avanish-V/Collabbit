package com.iota.campusX.Feature.UserProfile.data.remote.api

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.Post.data.remote.PageResponse
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.ApiResponse
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.ConnectionResponse
import com.iota.campusX.Koin.END_POINT
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.tasks.await
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

class ConnectionApi(private val client: HttpClient, private val auth: FirebaseAuth) {

    suspend fun getPagedConnection(userId: String, page: Int = 0, size: Int = 10): Result<PageResponse<ConnectionResponse>> {

        val token = try {
            auth.currentUser?.getIdToken(true)?.await()?.token ?: throw IllegalStateException("Failed to obtain auth token")
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {

            val response = client.get("$END_POINT/connections/$userId") {
                header("Authorization", "Bearer $token")
            }

            Log.d("CONNECTION_LIST","${response.bodyAsText()}")

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Failed: ${response.status}"))
            }

            val apiResponse: ApiResponse<PageResponse<ConnectionResponse>> =
                Json { ignoreUnknownKeys = true }
                    .decodeFromString(response.bodyAsText())

            val pageResponse: PageResponse<ConnectionResponse> = apiResponse.body

// Access the list of connections
            val connections: List<ConnectionResponse> = pageResponse.content

            Log.d("CONNECTION_LIST","${pageResponse}")

            Result.success(pageResponse)

        } catch (e: IOException){
            Result.failure(Exception("Network error!"))
        } catch (e: Exception) {
            Log.d("CONNECTION_LIST","error - ${e.message}")
            Result.failure(Exception("Something went wrong!"))
        }
    }
}