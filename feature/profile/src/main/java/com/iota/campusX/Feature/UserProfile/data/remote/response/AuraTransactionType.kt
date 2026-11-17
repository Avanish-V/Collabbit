package com.iota.campusX.Feature.UserProfile.data.remote.response

import androidx.annotation.Keep
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Keep
@Serializable(with = AuraTransactionTypeSerializer::class)
enum class AuraTransactionType {
    @SerialName("DAILY_CHECK_IN") DAILY_CHECK_IN,
    @SerialName("DAILY_LOGIN") DAILY_LOGIN,
    @SerialName("STREAK_BONUS") STREAK_BONUS,
    @SerialName("POST_LIKED") POST_LIKED,
    @SerialName("MISSION_COMPLETED") MISSION_COMPLETED,
    @SerialName("UNKNOWN") UNKNOWN;

    companion object {
        fun fromString(value: String?): AuraTransactionType {
            if (value.isNullOrEmpty()) return UNKNOWN
            val clean = value.replace("-", "_").lowercase()
            return entries.find {
                it.name.lowercase() == clean ||
                it.name.replace("_", "").lowercase() == clean.replace("_", "")
            } ?: UNKNOWN
        }
    }
}

@Keep
object AuraTransactionTypeSerializer : KSerializer<AuraTransactionType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AuraTransactionType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): AuraTransactionType {
        val value = runCatching { decoder.decodeString() }.getOrNull()
        return AuraTransactionType.fromString(value)
    }

    override fun serialize(encoder: Encoder, value: AuraTransactionType) {
        encoder.encodeString(value.name)
    }
}
