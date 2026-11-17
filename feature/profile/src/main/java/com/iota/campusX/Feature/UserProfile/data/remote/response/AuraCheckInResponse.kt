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
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Response from POST /users/me/aura/award or POST aura/check-in.
 *
 * @param aura             Updated aura state after the award.
 * @param pointsEarnedToday Points added in this call.
 * @param transaction      The detailed transaction record (optional, for local caching).
 */
@Keep
@Serializable(with = AuraCheckInResponseSerializer::class)
data class AuraCheckInResponse(
    @SerialName("aura") val aura: AuraInfoResponse = AuraInfoResponse(),
    @SerialName("pointsEarnedToday") val pointsEarnedToday: Int = 0,
    @SerialName("transaction") val transaction: AuraTransactionResponse? = null
)

@Keep
@OptIn(ExperimentalSerializationApi::class)
object AuraCheckInResponseSerializer : KSerializer<AuraCheckInResponse> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("com.iota.campusX.Feature.UserProfile.data.remote.response.AuraCheckInResponse")

    override fun deserialize(decoder: Decoder): AuraCheckInResponse {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return AuraCheckInResponse()

        val element = jsonDecoder.decodeJsonElement()
        if (element !is JsonObject) {
            return AuraCheckInResponse()
        }

        val json = jsonDecoder.json

        // Extract aura: search "aura", "aura_info", "auraInfo", "user_aura", "userAura"
        val auraObj = element["aura"]
            ?: element["aura_info"]
            ?: element["auraInfo"]
            ?: element["user_aura"]
            ?: element["userAura"]

        val aura = if (auraObj != null && auraObj is JsonObject) {
            runCatching {
                json.decodeFromJsonElement(AuraInfoResponse.serializer(), auraObj)
            }.getOrDefault(AuraInfoResponse())
        } else {
            val points = (element["auraPoints"] as? JsonPrimitive)?.intOrNull
                ?: (element["aura_points"] as? JsonPrimitive)?.intOrNull
                ?: 0
            val levelStr = (element["level"] as? JsonPrimitive)?.contentOrNull
            val level = AuraLevelResponse.entries.find { it.name.equals(levelStr, ignoreCase = true) }
                ?: AuraLevelResponse.NEWCOMER
            AuraInfoResponse(auraPoints = points, level = level)
        }

        // Extract pointsEarnedToday: search "pointsEarnedToday", "points_earned_today", "points_earned", "pointsEarned", "points", "amount"
        val pointsPrimitive = (element["pointsEarnedToday"] as? JsonPrimitive)
            ?: (element["points_earned_today"] as? JsonPrimitive)
            ?: (element["points_earned"] as? JsonPrimitive)
            ?: (element["pointsEarned"] as? JsonPrimitive)
            ?: (element["points"] as? JsonPrimitive)
            ?: (element["amount"] as? JsonPrimitive)

        val pointsEarnedToday = pointsPrimitive?.intOrNull ?: 0

        // Extract transaction: search "transaction", "aura_transaction", "auraTransaction"
        val txObj = element["transaction"]
            ?: element["aura_transaction"]
            ?: element["auraTransaction"]

        val transaction = if (txObj != null && txObj !is JsonNull) {
            runCatching {
                json.decodeFromJsonElement(AuraTransactionResponse.serializer(), txObj)
            }.getOrNull()
        } else null

        return AuraCheckInResponse(
            aura = aura,
            pointsEarnedToday = pointsEarnedToday,
            transaction = transaction
        )
    }

    override fun serialize(encoder: Encoder, value: AuraCheckInResponse) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(descriptor, 0, AuraInfoResponse.serializer(), value.aura)
        composite.encodeIntElement(descriptor, 1, value.pointsEarnedToday)
        composite.encodeNullableSerializableElement(descriptor, 2, AuraTransactionResponse.serializer(), value.transaction)
        composite.endStructure(descriptor)
    }
}
