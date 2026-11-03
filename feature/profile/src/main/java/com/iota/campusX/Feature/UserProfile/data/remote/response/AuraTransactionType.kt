package com.iota.campusX.Feature.UserProfile.data.remote.response

import kotlinx.serialization.Serializable

@Serializable
enum class AuraTransactionType {
    DAILY_CHECK_IN,
    DAILY_LOGIN,
    STREAK_BONUS,
    POST_LIKED,
    MISSION_COMPLETED,
    UNKNOWN
}
