package com.iota.campusX.Feature.UserProfile.data.remote.repository

import com.iota.campusX.Feature.UserProfile.data.local.dao.AuraTransactionDao
import com.iota.campusX.Feature.UserProfile.data.local.dao.UserProfileDao
import com.iota.campusX.Feature.UserProfile.data.local.entities.AuraTransactionEntity
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraCheckInResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraInfoResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraLevelResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraTransactionResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.AuraRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
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
                // Return a success result indicating 0 points earned today to prevent UI double-trigger
                val currentProfile = userProfileDao.observeProfile().firstOrNull()
                return Result.success(
                    AuraCheckInResponse(
                        aura = AuraInfoResponse(
                            auraPoints = currentProfile?.auraPoints ?: 0,
                            level = AuraLevelResponse.valueOf(currentProfile?.auraLevel ?: "NEWCOMER")
                        ),
                        pointsEarnedToday = 0
                    )
                )
            }

            // 2. Hit server if not found locally
            val response = httpClient.post("aura/check-in") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("rule_code" to ruleCode))
            }

            if (response.status.value in 200..299) {
                val transaction = response.body<AuraTransactionResponse>()
                
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

                // 4. Fetch updated aura info to refresh cache and UI
                val auraResult = getAura()
                val auraInfo = auraResult.getOrNull() ?: run {
                    // Fallback to local profile if fetch fails
                    val currentProfile = userProfileDao.observeProfile().firstOrNull()
                    AuraInfoResponse(
                        auraPoints = (currentProfile?.auraPoints ?: 0) + transaction.amount,
                        level = AuraLevelResponse.valueOf(currentProfile?.auraLevel ?: "NEWCOMER")
                    )
                }

                // 5. Update User Profile Cache
                userProfileDao.updateAura(
                    auraPoints = auraInfo.auraPoints,
                    auraLevel  = auraInfo.level.name
                )

                Result.success(
                    AuraCheckInResponse(
                        aura = auraInfo,
                        pointsEarnedToday = transaction.amount,
                        transaction = transaction
                    )
                )
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
                Result.success(response.body<AuraInfoResponse>())
            } else {
                Result.failure(Exception("Failed to fetch aura: HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
