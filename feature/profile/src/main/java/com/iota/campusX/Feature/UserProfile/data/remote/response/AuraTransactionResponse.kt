package com.iota.campusX.Feature.UserProfile.data.remote.response

import androidx.annotation.Keep
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

@Keep
@Serializable(with = AuraTransactionResponseSerializer::class)
data class AuraTransactionResponse(
    @SerialName("id") val id: String = "",
    @SerialName("userId") val userId: String = "",
    @SerialName("amount") val amount: Int = 0,
    @SerialName("type") val type: AuraTransactionType = AuraTransactionType.DAILY_CHECK_IN,
    @SerialName("referenceId") val referenceId: String? = null,
    @SerialName("createdAt") val createdAt: String = ""
)

@Keep
@OptIn(ExperimentalSerializationApi::class)
object AuraTransactionResponseSerializer : KSerializer<AuraTransactionResponse> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("com.iota.campusX.Feature.UserProfile.data.remote.response.AuraTransactionResponse")

    override fun deserialize(decoder: Decoder): AuraTransactionResponse {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return AuraTransactionResponse()

        val element = jsonDecoder.decodeJsonElement() as? JsonObject
            ?: return AuraTransactionResponse()

        val id = (element["id"] as? JsonPrimitive)?.contentOrNull ?: ""
        val userId = (element["userId"] as? JsonPrimitive)?.contentOrNull
            ?: (element["user_id"] as? JsonPrimitive)?.contentOrNull
            ?: ""
        val amount = (element["amount"] as? JsonPrimitive)?.intOrNull
            ?: (element["points"] as? JsonPrimitive)?.intOrNull
            ?: 0
        val typeStr = (element["type"] as? JsonPrimitive)?.contentOrNull
        val type = AuraTransactionType.fromString(typeStr)
        val referenceId = (element["referenceId"] as? JsonPrimitive)?.contentOrNull
            ?: (element["reference_id"] as? JsonPrimitive)?.contentOrNull
        val createdAt = (element["createdAt"] as? JsonPrimitive)?.contentOrNull
            ?: (element["created_at"] as? JsonPrimitive)?.contentOrNull
            ?: (element["date"] as? JsonPrimitive)?.contentOrNull
            ?: ""

        return AuraTransactionResponse(
            id = id,
            userId = userId,
            amount = amount,
            type = type,
            referenceId = referenceId,
            createdAt = createdAt
        )
    }

    override fun serialize(encoder: Encoder, value: AuraTransactionResponse) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeStringElement(descriptor, 0, value.id)
        composite.encodeStringElement(descriptor, 1, value.userId)
        composite.encodeIntElement(descriptor, 2, value.amount)
        composite.encodeSerializableElement(descriptor, 3, AuraTransactionTypeSerializer, value.type)
        composite.encodeNullableSerializableElement(descriptor, 4, kotlinx.serialization.serializer<String?>(), value.referenceId)
        composite.encodeStringElement(descriptor, 5, value.createdAt)
        composite.endStructure(descriptor)
    }
}
