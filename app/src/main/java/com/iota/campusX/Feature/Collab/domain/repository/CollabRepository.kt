package com.iota.campusX.Feature.Collab.domain.repository

import androidx.paging.PagingData
import com.iota.campusX.Feature.Collab.data.model.CollabConnectRequestResponse
import com.iota.campusX.Feature.Collab.data.model.CollabRequestStatus
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.Collab.data.model.CreateCollabRequest
import com.iota.campusX.Feature.Collab.data.remote.response.CollabPageResponse
import kotlinx.coroutines.flow.Flow

interface CollabRepository {
    suspend fun createCollab(request: CreateCollabRequest): Result<CollabResponse>

    suspend fun getCollabs(type: String, query: String = "", page: Int, size: Int): Result<CollabPageResponse>
    fun getCollabsPaging(type: String, query: String = ""): Flow<PagingData<CollabResponse>>

    suspend fun getCollabById(collabId: String): Result<CollabResponse>
    suspend fun deleteCollab(collabId: String): Result<Boolean>

    suspend fun requestToCollab(collabId: String): Result<CollabRequestStatus>

    suspend fun getCollabRequests(collabId: String): Result<List<CollabConnectRequestResponse>>

    suspend fun updateCollabRequestStatus(requestId: Long, status: String): Result<CollabRequestStatus>

    suspend fun hasAlreadyApplied(collabId: String): Result<CollabRequestStatus>

    suspend fun getCollabsByUserId(userId:String): Result<List<CollabResponse>>
}