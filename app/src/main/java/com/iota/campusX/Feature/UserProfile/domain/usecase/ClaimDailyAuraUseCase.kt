package com.iota.campusX.Feature.UserProfile.domain.usecase

import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraCheckInResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.AuraRepository

/**
 * Use case for claiming daily aura check-in points.
 * 
 * This is the primary use case for the daily aura system.
 * Call this when the app launches or when user manually checks in.
 * 
 * Features:
 * - Idempotent: Safe to call multiple times per day
 * - Awards base points (5) + streak bonuses
 * - Returns whether points were awarded (first call of the day)
 * 
 * Usage:
 * ```
 * val result = claimDailyAuraUseCase()
 * result.fold(
 *     onSuccess = { response ->
 *         if (response.pointsEarnedToday > 0) {
 *             // Show success dialog with points earned
 *         }
 *     },
 *     onFailure = { error ->
 *         // Handle error silently or show message
 *     }
 * )
 * ```
 * 
 * @return Result with AuraCheckInResponse containing updated aura state and points earned
 */
class ClaimDailyAuraUseCase(
    private val auraRepository: AuraRepository
) {
    suspend operator fun invoke(userId: String): Result<AuraCheckInResponse> {
        return auraRepository.awardPoints(userId, "daily_check_in")
    }
}
