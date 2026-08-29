package com.iota.campusX.Feature.UserProfile.domain.Model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class Gender {
    MALE, FEMALE, OTHER, UNSPECIFIED
}
