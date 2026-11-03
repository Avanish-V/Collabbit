package com.iota.campusX.Feature.UserProfile.data.remote.response

import kotlinx.serialization.Serializable

@Serializable
data class AuraTransactionResponse(
    val id: String,
    val userId: String,
    val amount: Int,
    val type: AuraTransactionType,
    val referenceId: String?,
    val createdAt: String
)
