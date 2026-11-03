package com.iota.campusX.Feature.UserProfile.data.remote.response

import androidx.annotation.Keep
import com.iota.campusX.Feature.UserProfile.domain.Model.BaseProfile
import com.iota.campusX.Feature.UserProfile.domain.Model.Contact
import com.iota.campusX.Feature.UserProfile.domain.Model.Education
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Aura points + derived level — no streak data. */
@Keep
@Serializable
data class AuraInfoResponse(
    @SerialName("auraPoints") val auraPoints: Int = 0,
    @SerialName("level") val level: AuraLevelResponse = AuraLevelResponse.NEWCOMER
)

@Keep
@Serializable
enum class AuraLevelResponse(val label: String, val minPoints: Int) {
    @SerialName("NEWCOMER") NEWCOMER("Newcomer",      0),
    @SerialName("SPARK") SPARK("Spark",           100),
    @SerialName("RISING") RISING("Rising",         300),
    @SerialName("GLOWING") GLOWING("Glowing",       700),
    @SerialName("RADIANT") RADIANT("Radiant",      1500),
    @SerialName("BLAZING") BLAZING("Blazing",      3000),
    @SerialName("LEGENDARY") LEGENDARY("Legendary", 6000)
}

@Keep
@Serializable
data class ProfileResponse(
    @SerialName("uid") val uid: String = "",
    @SerialName("baseProfile") val baseProfile: BaseProfile = BaseProfile(),
    @SerialName("contact") val contact: Contact = Contact(),
    @SerialName("education") val education: Education? = null,
    @SerialName("skills") val skills: List<SkillResponse>? = emptyList(),
    @SerialName("matchPreferences") val matchPreferences: List<MatchPreferenceResponse>? = emptyList(),
    @SerialName("isCurrentUser") val isCurrentUser: Boolean = false,
    @SerialName("aura") val aura: AuraInfoResponse = AuraInfoResponse()
)
