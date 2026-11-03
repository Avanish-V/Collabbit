package com.iota.campusX.Feature.UserProfile.data.remote.response

import androidx.annotation.Keep
import com.iota.campusX.core.common.FlexibleStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class SkillResponse(
    @SerialName("id")
    @Serializable(with = FlexibleStringSerializer::class)
    val id: String = "",

    @SerialName("name")
    @Serializable(with = FlexibleStringSerializer::class)
    val name: String = "",

    @SerialName("category")
    @Serializable(with = FlexibleStringSerializer::class)
    val category: String = ""
)
