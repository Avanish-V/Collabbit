package com.iota.campusX.Feature.UserProfile.domain.Model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class BaseProfile(
    @SerialName("name") var name: String = "",
    @SerialName("image") val image: String? = null,
    @SerialName("summary") val summary: String = "",
    @SerialName("tagline") val tagline: String = "",
    @SerialName("lookingFor") val lookingFor: String = "",
    @SerialName("gender") val gender: Gender? = Gender.UNSPECIFIED
)
