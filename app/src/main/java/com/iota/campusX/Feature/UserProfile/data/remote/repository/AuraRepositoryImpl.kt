package com.iota.campusX.Feature.UserProfile.data.remote.repository

import com.iota.campusX.Feature.UserProfile.data.local.dao.AuraTransactionDao
import com.iota.campusX.Feature.UserProfile.data.local.dao.UserProfileDao
import com.iota.campusX.Feature.UserProfile.data.local.entities.AuraTransactionEntity
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraCheckInResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraInfoResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraLevelResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.AuraRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuraRepositoryImpl(
    private val httpClient: HttpClient,
    private val userProfileDao: UserProfileDao,
    private val auraTransactionDao: AuraTransactionDao
) : AuraRepository {

    /**
     * Awards points for a specific event.
     * Prevents redundant server hits by checking local transactions.
     */
    override suspend fun awardPoints(userId: String, ruleCode: String): Result<AuraCheckInResponse> {
        return try {
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // 1. Check local DB to prevent duplicate request
            val existing = auraTransactionDao.getTransactionByTypeAndDate(userId, ruleCode, dateKey)
            if (existing != null) {
                return Result.failure(Throwable("Already claimed!"))
            }

            // 2. Hit server if not found locally
            val response = httpClient.post("aura/check-in") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("rule_code" to ruleCode))
            }

            if (response.status.value in 200..299) {
                val checkInResponse = response.body<AuraCheckInResponse>()
                val transaction = checkInResponse.transaction ?: return Result.failure(Exception("No transaction in response"))
                
                // 3. Cache the transaction locally
                auraTransactionDao.insertTransaction(
                    AuraTransactionEntity(
                        id = transaction.id,
                        userId = transaction.userId,
                        amount = transaction.amount,
                        type = transaction.type.name,
                        referenceId = transaction.referenceId,
                        createdAt = transaction.createdAt,
                        dateKey = dateKey
                    )
                )

                // 4. Update local cache using the server's authoritative aura state.
                // This fixes the drift where local would increase but server wouldn't.
                // We only update if points were actually earned to avoid redundant DB writes.
                if (checkInResponse.pointsEarnedToday > 0) {
                    userProfileDao.updateAura(
                        auraPoints = checkInResponse.aura.auraPoints,
                        auraLevel  = checkInResponse.aura.level.name
                    )
                }

                Result.success(checkInResponse)
            } else {
                Result.failure(Exception("Award failed: HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * GET /users/me/aura — returns current aura snapshot, no side effects.
     */
    override suspend fun getAura(): Result<AuraInfoResponse> {
        return try {
            val response = httpClient.get("aura/me")
            if (response.status.value in 200..299) {
                val auraInfo = response.body<AuraInfoResponse>()
                
                // Update local profile cache to keep UI in sync
                userProfileDao.updateAura(
                    auraPoints = auraInfo.auraPoints,
                    auraLevel = auraInfo.level.name
                )
                
                Result.success(auraInfo)
            } else {
                Result.failure(Exception("Failed to fetch aura: HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeTransactions(userId: String): Flow<List<AuraTransactionEntity>> {
        return auraTransactionDao.observeTransactions(userId)
    }
}
