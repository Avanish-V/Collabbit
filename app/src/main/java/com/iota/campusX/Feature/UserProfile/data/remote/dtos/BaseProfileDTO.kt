package com.iota.campusX.Feature.UserProfile.data.remote.dtos

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the base profile data for a user.
 */
@Keep
@Serializable
data class BaseProfileDTO(
    @SerialName("id") val uid: String = "",
    @SerialName("token") val token: String = "",
    @SerialName("name") var name: String = "",
    @SerialName("image") val image: String? = null,
    @SerialName("email") val email: String = "",
    @SerialName("about") val about: String = "",
    @SerialName("tagline") val tagline: String = "",
    @SerialName("bgColor") val bgColor: String = "",
    @SerialName("phoneNumber") val phoneNumber: String = "",
    @SerialName("gender") val gender: Gender? = Gender.UNSPECIFIED,
    @SerialName("interests") val interests: List<String>? = emptyList(),
    @SerialName("metaData") val metaData: MetaData? = MetaData(),
    @SerialName("campus") val campus: Campus? = null,
    @SerialName("isRequestSent") val isRequestSent: Boolean? = null,
    @SerialName("count") val count: Counts? = null,
)

/**
 * Metadata for profile state tracking.
 */
@Keep
@Serializable
data class MetaData(
    @SerialName("firstUser") val firstUser: Boolean = false,
    @SerialName("verified") val verified: Boolean = false,
    @SerialName("premium") val premium: Boolean = false,
    @SerialName("createdAt") val createdAt: Long? = null, // Use timestamp (e.g., from Firebase)
    @SerialName("updatedAt") val updatedAt: Long? = null
)

/**
 * Represents user's educational campus details.
 */
@Keep
@Serializable
data class Campus(
    @SerialName("university") val university: String? = null,
    @SerialName("logo")val logo: String? = null,
    @SerialName("collegeName") val collegeName: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("degree") val degree: String? = null,
    @SerialName("fieldOfStudy") val fieldOfStudy: String? = null,
    @SerialName("courseStart")val courseStart: String? = null,
    @SerialName("startTimestamp")val startTimestamp: Long? = null,
    @SerialName("courseEnd")val courseEnd: String? = null,
    @SerialName("endTimestamp")val endTimestamp: Long? = null,
    @SerialName("isCurrent")val isCurrent: Boolean = false
)

/**
 * Represents a university.
 */
@Keep
@Serializable
data class University(
    @SerialName("university") val university: String = "",
    @SerialName("logo") val logo: String = ""
)

/**
 * Gender enum with stable serial names.
 */
@Keep
@Serializable
enum class Gender {
    @SerialName("male") MALE,
    @SerialName("female") FEMALE,
    @SerialName("other") OTHER,
    @SerialName("unspecified") UNSPECIFIED
}

@Serializable
data class Counts(
    val followers:Int = 0,
    val connections:Int = 0,
    val posts:Int = 0
)
