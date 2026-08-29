package com.iota.campusX.Feature.UserProfile.data.local.mapper

import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraInfoResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraLevelResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun UserProfileEntity.toDomain() = ProfileResponse(
    uid           = uid,
    baseProfile   = json.decodeFromString(baseProfile),
    contact       = json.decodeFromString(contact),
    education     = education?.let { json.decodeFromString(it) },
    skills        = skills?.let { json.decodeFromString(it) },
    summary       = summary,
    isCurrentUser = isCurrentUser,
    aura = AuraInfoResponse(
        auraPoints = auraPoints,
        level      = runCatching { AuraLevelResponse.valueOf(auraLevel) }
                         .getOrDefault(AuraLevelResponse.NEWCOMER)
    )
)
