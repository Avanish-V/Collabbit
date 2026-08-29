package com.iota.campusX.Feature.UserProfile.data.local.mapper

import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun ProfileResponse.toEntity() = UserProfileEntity(
    uid           = uid,
    baseProfile   = json.encodeToString(baseProfile),
    contact       = json.encodeToString(contact),
    education     = education?.let { json.encodeToString(it) },
    skills        = skills?.let { json.encodeToString(it) },
    summary       = summary,
    isCurrentUser = isCurrentUser,
    auraPoints    = aura.auraPoints,
    auraLevel     = aura.level.name
)
