package com.iota.campusX.Feature.UserProfile.data

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the base profile data for a user.
 */
@Keep
@Serializable
data class BasicProfileDTO(
    @SerialName("id") val id: String = "",
    @SerialName("token") val token: String = "",
    @SerialName("userName") val userName: String = "",
    @SerialName("userImage") val userImage: String = "",
    @SerialName("userEmail") val userEmail: String = "",
    @SerialName("userBio") val userBio: String = "",
    @SerialName("userGender") val userGender: Gender = Gender.UNSPECIFIED,
    @SerialName("interests") val interests: List<String> = emptyList(),
    @SerialName("metaData") val metaData: MetaData = MetaData(),
    @SerialName("campus") val campus: Campus? = null,
    @SerialName("isRequestSent") val isRequestSent: Boolean? = null
)

/**
 * Metadata for profile state tracking.
 */
@Keep
@Serializable
data class MetaData(
    @SerialName("isFirstUser") val isFirstUser: Boolean = false,
    @SerialName("createdAt") val createdAt: Long? = null, // Use timestamp (e.g., from Firebase)
    @SerialName("updatedAt") val updatedAt: Long? = null
)

/**
 * Represents user's educational campus details.
 */
@Keep
@Serializable
data class Campus(
    @SerialName("university") val university: University? = null,
    @SerialName("collegeName") val collegeName: String = "",
    @SerialName("campusCode") val campusCode: String = "",
    @SerialName("fieldOfStudy") val fieldOfStudy: String = "",
    @SerialName("courseStart") val courseStart: Long? = null,
    @SerialName("courseEnd") val courseEnd: Long? = null
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
