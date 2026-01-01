package com.iota.campusX.Feature.UserProfile.data.repository

import SendPushNotification
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.iota.campusX.Feature.Notification.data.CreateNotification
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Post.data.mapper.PostsPagingSource
import com.iota.campusX.Feature.Post.data.remote.PageResponse
import com.iota.campusX.Feature.Post.data.remote.PostApi
import com.iota.campusX.Feature.Post.domain.models.PostResponse
import com.iota.campusX.Feature.UserProfile.data.remote.api.ConnectionApi
import com.iota.campusX.Feature.UserProfile.data.remote.api.ConnectionsPagingSource
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.ConnectionResponse
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.ConnectionsDTO
import com.iota.campusX.Feature.UserProfile.domain.models.ConnectionRequestResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.UserConnectionsRepository
import com.iota.campusX.Koin.END_POINT
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json

class UserConnectionImpl (
    private val sendPushNotification: SendPushNotification,
    private val notificationRepository: NotificationRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val httpClient: HttpClient
): UserConnectionsRepository {

    override suspend fun sendLinkUpRequest(requestUserId: String, currentState: Boolean?): Result<ConnectionRequestResponse> {

        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        if (userId == requestUserId) return Result.failure(Exception("You cannot send a request to yourself"))
        if (requestUserId.isEmpty()) return Result.failure(Exception("Invalid user ID"))

        return try {

            if (userId.isBlank()) return Result.failure(Exception("Invalid user ID"))

            val tokenResult = auth.currentUser?.getIdToken(true)?.await()

            val token = tokenResult?.token ?: run {
                return Result.failure(Exception("Failed to get Firebase ID token"))
            }

            val response =
                httpClient.post("$END_POINT/connections/request/$requestUserId") {
                    header("Authorization", "Bearer $token")
                }

            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                val data = Gson().fromJson(body, ConnectionRequestResponse::class.java)
                Result.success(data)
            } else {
                Result.failure(Exception("Something went wrong!"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptLinkUpRequest(requestUserId: String): Result<Boolean> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        return try {

            if (userId.isBlank()) return Result.failure(Exception("Invalid user ID"))

            val tokenResult = auth.currentUser?.getIdToken(true)?.await()

            val token = tokenResult?.token ?: run {
                return Result.failure(Exception("Failed to get Firebase ID token"))
            }

            val response =
                httpClient.post("$END_POINT/connections/request/$requestUserId/accept") {
                    header("Authorization", "Bearer $token")
                }

            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                val data = Gson().fromJson(body, ConnectionRequestResponse::class.java)
                Log.d("CONNECTION_STATE", "rejectLinkUpRequest: $data")
                Result.success(true)
            } else {
                Result.failure(Exception("Something went wrong!"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectLinkUpRequest(requestUserId: String?): Result<ConnectionRequestResponse> {

        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

        if (requestUserId.isNullOrEmpty()) return Result.failure(Exception("Something went wrong"))

        return try {

            if (userId.isBlank()) return Result.failure(Exception("Invalid user ID"))

            val tokenResult = auth.currentUser?.getIdToken(true)?.await()

            val token = tokenResult?.token ?: run {
                return Result.failure(Exception("Failed to get Firebase ID token"))
            }

            val response =
                httpClient.delete("$END_POINT/connections/request/$requestUserId") {
                    header("Authorization", "Bearer $token")
                }

            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                val data = Gson().fromJson(body, ConnectionRequestResponse::class.java)
                Result.success(data)
            } else {
                Result.failure(Exception("Something went wrong!"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteConnection(requestUserId: String?): Result<ConnectionRequestResponse> {

        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

        if (requestUserId.isNullOrEmpty()) return Result.failure(Exception("Something went wrong"))

        return try {

            if (userId.isBlank()) return Result.failure(Exception("Invalid user ID"))

            val tokenResult = auth.currentUser?.getIdToken(true)?.await()

            val token = tokenResult?.token ?: run {
                return Result.failure(Exception("Failed to get Firebase ID token"))
            }

            val response =
                httpClient.delete("$END_POINT/connections/$requestUserId") {
                    header("Authorization", "Bearer $token")
                }

            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                val data = Gson().fromJson(body, ConnectionRequestResponse::class.java)
                Log.d("CONNECTION_STATE", "rejectLinkUpRequest: $data")
                Result.success(data)
            } else {
                Result.failure(Exception("Something went wrong!"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConnectionsCount(userId: String): Result<Int> {
        return try {
            val snapshot = firestore.collection("Users").document(userId)
                .collection("Connections")
                .whereEqualTo("status", true)
                .count()
                .get(AggregateSource.SERVER)
                .await()
            Result.success(snapshot.count.toInt())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConnections(userId: String): Flow<PagingData<ConnectionResponse>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                ConnectionsPagingSource(
                    api = ConnectionApi(client = httpClient, auth = auth),
                    userId = userId
                )
            }
        ).flow
    }

    override suspend fun getConnectionsForCurrentUser(): Flow<PagingData<ConnectionResponse>> {
        Log.d("CONNECTION_LIST","Called ")
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                ConnectionsPagingSource(
                    api = ConnectionApi(client = httpClient, auth = auth),
                    userId = auth.currentUser?.uid ?: ""
                )
            }
        ).flow
    }

    override suspend fun getPendingRequests(): Result<List<ConnectionResponse>> {
        return try {

            val tokenResult = auth.currentUser?.getIdToken(true)?.await()

            val token = tokenResult?.token ?: run {
                return Result.failure(Exception("Failed to get Firebase ID token"))
            }

            val response =
                httpClient.get("$END_POINT/connections/requests/pending") {
                    header("Authorization", "Bearer $token")
                }

            if (response.status.value in 200..299) {
                val data = Gson().fromJson(response.bodyAsText(), Array<ConnectionResponse>::class.java).toList()
                Result.success(data)
            } else {
                Result.failure(Exception("Something went wrong!"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasConnection(userId: String): Result<ConnectionRequestResponse> {
        return try {

            if (userId.isBlank()) return Result.failure(Exception("Invalid user ID"))

            val tokenResult = auth.currentUser?.getIdToken(true)?.await()

            val token = tokenResult?.token ?: run {
                return Result.failure(Exception("Failed to get Firebase ID token"))
            }

            val response = httpClient.get("$END_POINT/connections/status/$userId") {
                    header("Authorization", "Bearer $token")
            }

            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                val data = Gson().fromJson(body, ConnectionRequestResponse::class.java)
                Result.success(data)
            } else {

                Result.failure(Exception("Something went wrong!"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}