package com.iota.campusX.Feature.Collab.domain.repository

import androidx.paging.PagingData
import com.iota.campusX.Feature.Collab.data.model.CollabConnectRequestResponse
import com.iota.campusX.Feature.Collab.data.model.CollabMemberResponse
import com.iota.campusX.Feature.Collab.data.model.CollabRequestStatus
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.Collab.data.model.CreateCollabRequest
import com.iota.campusX.Feature.Collab.data.model.ProjectStatus
import com.iota.campusX.Feature.Collab.data.model.UpdateCollabRequest
import com.iota.campusX.Feature.Collab.data.remote.response.CollabPageResponse
import kotlinx.coroutines.flow.Flow

interface CollabRepository {
    suspend fun createCollab(request: CreateCollabRequest): Result<CollabResponse>

    suspend fun getCollabs(type: String, query: String = "", page: Int, size: Int): Result<CollabPageResponse>
    fun getCollabsPaging(type: String, query: String = ""): Flow<PagingData<CollabResponse>>

    suspend fun getCollabById(collabId: String): Result<CollabResponse>
    suspend fun updateProjectStatus(collabId: String, status: ProjectStatus): Result<CollabResponse>
    suspend fun updateCollab(collabId: String, request: UpdateCollabRequest): Result<CollabResponse>
    suspend fun deleteCollab(collabId: String): Result<Boolean>

    suspend fun requestToCollab(collabId: String): Result<CollabRequestStatus>

    suspend fun getCollabRequests(collabId: String): Result<List<CollabConnectRequestResponse>>

    suspend fun updateCollabRequestStatus(requestId: String, status: String): Result<CollabRequestStatus>

    suspend fun hasAlreadyApplied(collabId: String): Result<CollabRequestStatus>

    suspend fun getCollabsByUserId(userId: String): Result<List<CollabResponse>>
    suspend fun getJoinedCollabsByUserId(userId: String): Result<List<CollabResponse>>

    suspend fun getCollabMembers(collabId: String): Result<List<CollabMemberResponse>>

    suspend fun removeCollabMember(collabId: String, memberUserId: String): Result<Boolean>
}
