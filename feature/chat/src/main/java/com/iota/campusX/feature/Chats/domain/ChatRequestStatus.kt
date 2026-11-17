package com.iota.campusX.Feature.Chats.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ChatRequestStatus(val value: String) {
    @SerialName("PENDING") PENDING("PENDING"),
    @SerialName("ACCEPTED") ACCEPTED("ACCEPTED"),
    @SerialName("REJECTED") REJECTED("REJECTED");

    companion object {
        fun fromString(status: String?): ChatRequestStatus {
            return when (status?.uppercase()) {
                "PENDING" -> PENDING
                "REJECTED" -> REJECTED
                else -> ACCEPTED
            }
        }
    }
}
