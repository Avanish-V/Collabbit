package com.iota.campusX.Feature.UserProfile.data.remote.response

import kotlinx.serialization.Serializable

/**
 * Response from POST /users/me/aura/award.
 *
 * @param aura             Updated aura state after the award.
 * @param pointsEarnedToday Points added in this call.
 * @param transaction      The detailed transaction record (optional, for local caching).
 */
@Serializable
data class AuraCheckInResponse(
    val aura: AuraInfoResponse,
    val pointsEarnedToday: Int,
    val transaction: AuraTransactionResponse? = null
)
