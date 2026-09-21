package com.iota.campusX.Feature.Collab.data.remote.repository

import android.util.Log
import com.iota.campusX.Feature.Collab.data.model.CollabConnectRequestResponse
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.Collab.data.model.CreateCollabRequest
import com.iota.campusX.Feature.Collab.data.remote.response.CollabPageResponse
import com.iota.campusX.Feature.Collab.domain.repository.CollabRepository
import com.iota.campusX.Feature.Collab.data.paging.CollabPagingSource
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.iota.campusX.Feature.Collab.data.model.CollabRequestStatus
import kotlinx.coroutines.flow.Flow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.util.concurrent.ConcurrentHashMap

class CollabRepoImpl(
    private val httpClient: HttpClient
): CollabRepository {

    private val TAG = "CollabRepoImpl"
    private val collabCache = ConcurrentHashMap<String, CollabResponse>()

    override suspend fun createCollab(request: CreateCollabRequest): Result<CollabResponse> {
        return runCatching {
            val response: HttpResponse = httpClient.post("collabs") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (!response.status.isSuccess()) {
                val error = response.bodyAsText()
                Log.e(TAG, "createCollab failed: ${response.status.value} - $error")
                throw Exception("Failed to create collaboration: ${response.status}")
            }
            response.body<CollabResponse>().also { collab ->
                collabCache[collab.id] = collab
            }
        }.onFailure {
            Log.e(TAG, "Exception in createCollab: ${it.message}", it)
        }
    }

    override suspend fun getCollabs(type: String, query: String, page: Int, size: Int): Result<CollabPageResponse> {
        return runCatching {
            val response: HttpResponse = httpClient.get("collabs") {
                parameter("type", type)
                parameter("query", query)
                parameter("page", page)
                parameter("size", size)
            }
            if (!response.status.isSuccess()) {
                val error = response.bodyAsText()
                Log.e(TAG, "getCollabs failed: ${response.status.value} - $error")
                throw Exception("Failed to fetch collabs: ${response.status}")
            }
            response.body<CollabPageResponse>().also { pageResponse ->
                pageResponse.content.forEach { collabCache[it.id] = it }
            }
        }.onFailure {
            Log.e(TAG, "Exception in getCollabs: ${it.message}", it)
        }
    }

    override fun getCollabsPaging(type: String, query: String): Flow<PagingData<CollabResponse>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { CollabPagingSource(repository = this, type = type, query = query) }
        ).flow
    }

    override suspend fun getCollabById(collabId: String): Result<CollabResponse> {
        collabCache[collabId]?.let { return Result.success(it) }
        
        return runCatching {
            val response: HttpResponse = httpClient.get("collabs/$collabId")
            if (!response.status.isSuccess()) {
                val error = response.bodyAsText()
                Log.e(TAG, "getCollabById failed: ${response.status.value} - $error")
                throw Exception("Failed to fetch collab: ${response.status}")
            }
            response.body<CollabResponse>().also { collab ->
                collabCache[collabId] = collab
            }
        }.onFailure {
            Log.e(TAG, "Exception in getCollabById: ${it.message}", it)
        }
    }

    override suspend fun deleteCollab(collabId: String): Result<Boolean> {
        return runCatching {
            val response: HttpResponse = httpClient.delete("collabs/$collabId")
            if (!response.status.isSuccess()) {
                val error = response.bodyAsText()
                Log.e(TAG, "deleteCollab failed: ${response.status.value} - $error")
            }
            response.status in listOf(HttpStatusCode.OK, HttpStatusCode.NoContent)
        }.onFailure {
            Log.e(TAG, "Exception in deleteCollab: ${it.message}", it)
        }
    }

    override suspend fun requestToCollab(collabId: String): Result<CollabRequestStatus> {
        return runCatching {
            val response: HttpResponse = httpClient.post("collabs/$collabId/connect") {
                contentType(ContentType.Application.Json)
            }
            if (!response.status.isSuccess()) {
                val error = response.bodyAsText()
                Log.e(TAG, "requestToCollab failed: ${response.status.value} - $error")
                throw Exception("Failed to request collaboration: ${response.status}")
            }
            response.body<CollabRequestStatus>()
        }.onFailure {
            Log.e(TAG, "Exception in requestToCollab: ${it.message}", it)
        }
    }

    override suspend fun getCollabRequests(collabId: String): Result<List<CollabConnectRequestResponse>> {
        return runCatching {
            val response: HttpResponse = httpClient.get("collabs/$collabId/requests")
            if (!response.status.isSuccess()) {
                val error = response.bodyAsText()
                Log.e(TAG, "getCollabRequests failed: ${response.status.value} - $error")
                throw Exception("Failed to fetch requests: ${response.status}")
            }
            response.body<List<CollabConnectRequestResponse>>()
        }.onFailure {
            Log.e(TAG, "Exception in getCollabRequests: ${it.message}", it)
        }
    }

    override suspend fun updateCollabRequestStatus(requestId: String, status: String): Result<CollabRequestStatus> {
        return runCatching {
            val response: HttpResponse = httpClient.patch("collabs/requests/$requestId") {
                parameter("status", status)
            }
            if (!response.status.isSuccess()) {
                val error = response.bodyAsText()
                Log.e(TAG, "updateCollabRequestStatus failed: ${response.status.value} - $error")
                throw Exception("Failed to update status: ${response.status}")
            }
            response.body<CollabRequestStatus>()
        }.onFailure {
            Log.e(TAG, "Exception in updateCollabRequestStatus: ${it.message}", it)
        }
    }

    override suspend fun hasAlreadyApplied(collabId: String): Result<CollabRequestStatus> {
        return runCatching {
            val response: HttpResponse = httpClient.get("collabs/has-requested") {
                parameter("collabId", collabId)
            }
            if (!response.status.isSuccess()) {
                val error = response.bodyAsText()
                Log.e(TAG, "hasAlreadyApplied failed: ${response.status.value} - $error")
                throw Exception("Failed to check status: ${response.status}")
            }
            response.body<CollabRequestStatus>()
        }.onFailure {
            Log.e(TAG, "Exception in hasAlreadyApplied: ${it.message}", it)
        }
    }

    override suspend fun getCollabsByUserId(userId: String): Result<List<CollabResponse>> {
        return runCatching {
            val response = httpClient.get("collabs/user"){
                parameter("userId",userId)
            }
            if (!response.status.isSuccess()) {
                val error = response.bodyAsText()
                Log.e(TAG, "getCollabsByUserId failed: ${response.status.value} - $error")
                throw Exception("Failed to fetch user collaborations: ${response.status}")
            }
            response.body<List<CollabResponse>>()
        }.onFailure {
            Log.e(TAG, "Exception in getCollabsByUserId: ${it.message}", it)
        }
    }
}
