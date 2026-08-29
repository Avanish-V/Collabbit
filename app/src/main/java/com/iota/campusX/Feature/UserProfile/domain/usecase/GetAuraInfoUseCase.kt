package com.iota.campusX.Feature.UserProfile.domain.usecase

import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraInfoResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.AuraRepository

/**
 * Use case for fetching current aura information.
 * 
 * This is a read-only operation that doesn't award any points.
 * Use this for displaying aura status in UI without side effects.
 * 
 * Returns:
 * - Total aura points
 * - Current level (Newcomer, Spark, Rising, etc.)
 * 
 * Usage:
 * ```
 * val result = getAuraInfoUseCase()
 * result.fold(
 *     onSuccess = { auraInfo ->
 *         // Display points and level in UI
 *         Text("${auraInfo.auraPoints} ✦")
 *         Text(auraInfo.level.label)
 *     },
 *     onFailure = { error ->
 *         // Handle error or use cached data
 *     }
 * )
 * ```
 * 
 * @return Result with AuraInfoResponse containing points and level
 */
class GetAuraInfoUseCase(
    private val auraRepository: AuraRepository
) {
    suspend operator fun invoke(): Result<AuraInfoResponse> {
        return auraRepository.getAura()
    }
}
