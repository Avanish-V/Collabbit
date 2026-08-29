package com.iota.campusX.Feature.UserProfile.domain.Model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class BaseProfile(
    var name: String = "",
    val image: String? = null,
    val summary: String = "",
    val tagline: String = "",
    val gender: Gender? = Gender.UNSPECIFIED,

)




