package com.iota.campusX.Feature.UserProfile.data.remote.dtos

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class Duration(
    val courseStart: String ?= null,
    val startTimestamp: Long? = null,
    val courseEnd: String? = null,
    val endTimestamp: Long? = null,
    val isCurrent: Boolean = false
)