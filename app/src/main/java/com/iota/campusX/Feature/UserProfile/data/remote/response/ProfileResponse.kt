package com.iota.campusX.Feature.UserProfile.data.remote.response

import com.iota.campusX.Feature.UserProfile.domain.Model.BaseProfile
import com.iota.campusX.Feature.UserProfile.domain.Model.Contact
import com.iota.campusX.Feature.UserProfile.domain.Model.Education
import kotlinx.serialization.Serializable

/** Aura points + derived level — no streak data. */
@Serializable
data class AuraInfoResponse(
    val auraPoints: Int = 0,
    val level: AuraLevelResponse = AuraLevelResponse.NEWCOMER
)

@Serializable
enum class AuraLevelResponse(val label: String, val minPoints: Int) {
    NEWCOMER("Newcomer",      0),
    SPARK("Spark",           100),
    RISING("Rising",         300),
    GLOWING("Glowing",       700),
    RADIANT("Radiant",      1500),
    BLAZING("Blazing",      3000),
    LEGENDARY("Legendary", 6000)
}

@Serializable
data class ProfileResponse(
    val uid: String,
    val baseProfile: BaseProfile,
    val contact: Contact,
    val education: Education?,
    val skills: List<SkillResponse>?,
    val matchPreferences: List<MatchPreferenceResponse>? = emptyList(),
    val isCurrentUser: Boolean,
    val aura: AuraInfoResponse = AuraInfoResponse()
)
