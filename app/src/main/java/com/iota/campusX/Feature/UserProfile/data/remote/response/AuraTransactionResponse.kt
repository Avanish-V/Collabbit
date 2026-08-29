package com.iota.campusX.Feature.UserProfile.data.remote.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuraTransactionResponse(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val amount: Int,
    val type: AuraTransactionType,
    @SerialName("reference_id")
    val referenceId: String,
    @SerialName("created_at")
    val createdAt: String
)
