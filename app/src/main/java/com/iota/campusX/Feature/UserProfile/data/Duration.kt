package com.iota.campusX.Feature.UserProfile.data

import androidx.annotation.Keep
import kotlinx.serialization.Serializable


@Keep
@Serializable
data class Duration(
    val start: String = "",
    val startTimestamp: Long? = null,
    val end: String? = null,
    val endTimestamp: Long? = null,
    val current: Boolean = false
)