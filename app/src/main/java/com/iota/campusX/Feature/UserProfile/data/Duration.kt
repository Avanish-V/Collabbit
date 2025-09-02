package com.iota.campusX.Feature.UserProfile.data

import kotlinx.serialization.Serializable

@Serializable
data class Duration(
    val start: String = "",
    val startTimestamp: Long? = null,
    val end: String? = null,
    val endTimestamp: Long? = null,
    val current: Boolean = false
)