package com.iota.campusX.Feature.UserProfile.data.remote.response

import androidx.annotation.Keep
import com.iota.campusX.core.common.FlexibleStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class MatchPreferenceResponse(
    @SerialName("id")
    @Serializable(with = FlexibleStringSerializer::class)
    val id: String = "",
    @SerialName("code") val code: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String? = null,
    @SerialName("icon") val icon: String? = null,
    @SerialName("active") val active: Boolean = false,
)
