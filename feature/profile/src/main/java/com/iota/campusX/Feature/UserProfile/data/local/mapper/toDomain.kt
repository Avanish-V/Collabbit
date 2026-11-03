package com.iota.campusX.Feature.UserProfile.data.local.mapper

import com.iota.campusX.Feature.UserProfile.domain.Model.BaseProfile
import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraInfoResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraLevelResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.let

private val json = Json { ignoreUnknownKeys = true }

fun UserProfileEntity.toDomain() = ProfileResponse(
    uid           = uid,
    baseProfile   = json.decodeFromString<BaseProfile>(baseProfile).copy(tagline = tagline),
    contact       = json.decodeFromString(contact),
    education     = education?.let { json.decodeFromString(it) },
    skills        = skills?.let { json.decodeFromString(it) },
    matchPreferences = matchPreferences?.let { json.decodeFromString(it) },
    isCurrentUser = isCurrentUser,
    aura = AuraInfoResponse(
        auraPoints = auraPoints,
        level      = runCatching { AuraLevelResponse.valueOf(auraLevel) }
                         .getOrDefault(AuraLevelResponse.NEWCOMER)
    )
)
