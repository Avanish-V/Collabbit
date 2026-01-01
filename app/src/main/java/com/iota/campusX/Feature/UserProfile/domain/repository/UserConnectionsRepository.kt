package com.iota.campusX.Feature.UserProfile.domain.repository

import androidx.paging.PagingData
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.ConnectionResponse
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.ConnectionsDTO
import com.iota.campusX.Feature.UserProfile.domain.models.ConnectionRequestResponse
import kotlinx.coroutines.flow.Flow

interface UserConnectionsRepository {
    suspend fun sendLinkUpRequest(requestUserId: String, currentState: Boolean?): Result<ConnectionRequestResponse>
    suspend fun acceptLinkUpRequest(requestUserId: String): Result<Boolean>
    suspend fun rejectLinkUpRequest(requestUserId: String?): Result<ConnectionRequestResponse>

    suspend fun deleteConnection(requestUserId: String?) : Result<ConnectionRequestResponse>

    suspend fun getConnectionsCount(userId: String): Result<Int>
    suspend fun getConnections(userId: String): Flow<PagingData<ConnectionResponse>>

    suspend fun getConnectionsForCurrentUser(): Flow<PagingData<ConnectionResponse>>

    suspend fun getPendingRequests(): Result<List<ConnectionResponse>>

    suspend fun hasConnection(userId: String): Result<ConnectionRequestResponse>
}