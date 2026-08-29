package com.iota.campusX.Feature.UserProfile.domain.repository

import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraCheckInResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraInfoResponse

interface AuraRepository {
    /** 
     * Awards points for a specific event (e.g. "daily_check_in").
     * Checks local cache first to prevent redundant server hits.
     */
    suspend fun awardPoints(userId: String, ruleCode: String): Result<AuraCheckInResponse>

    /** GET /users/me/aura — lightweight read, no side effects. */
    suspend fun getAura(): Result<AuraInfoResponse>
}
