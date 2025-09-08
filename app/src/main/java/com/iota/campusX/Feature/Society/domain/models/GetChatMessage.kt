package com.iota.campusX.Feature.Society.domain.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetChatMessage(
    @SerialName("message") val message: String = "",
    @Contextual
    @SerialName("createdAt") val createdAt: Timestamp? = null,
    @SerialName("senderId") val senderId: String = "",
    @SerialName("senderName") val senderName: String = "",
    @SerialName("senderProfile") val senderProfile: String = "",
)

@Serializable
data class SetChatMessage(
    @SerialName("message") val message: String = "",
    @Contextual
    @SerialName("createdAt") val createdAt: FieldValue? = null,
    @SerialName("senderId") val senderId: String = "",
    @SerialName("senderName") val senderName: String = "",
    @SerialName("senderProfile") val senderProfile: String = "",
)