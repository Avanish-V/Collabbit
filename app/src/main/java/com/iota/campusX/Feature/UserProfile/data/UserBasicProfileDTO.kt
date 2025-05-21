package com.iota.campusX.Feature.UserProfile.data

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep  // Prevent ProGuard from stripping the class
//@IgnoreExtraProperties  // Allows Firebase to ignore unknown fields
@Serializable
data class UserBasicProfileDTO(
    @SerialName("_id") val _id: String = "",
    @SerialName("token") val token: String = "",
    @SerialName("userName") val userName: String = "",
    @SerialName("userImage") val userImage: String = "",
    @SerialName("userEmail") val userEmail: String = "",
    @SerialName("userBio") val userBio: String ? = "",
    @SerialName("userGender") val userGender: String ? = "",
    @SerialName("social") val social: String = "",
    @SerialName("metaData") val metaData: MetaData = MetaData(),
    @SerialName("campus") val campus: Campus? = null,
    @SerialName("isRequestSent") val isRequestSent: Boolean ?= null

)

@Keep
@Serializable
data class MetaData @JvmOverloads constructor(
    @SerialName("isFirstUser") val isFirstUser: Boolean = false,
    @SerialName("createdAt") val createdAt: String? = "",
    @SerialName("updatedAt") val updatedAt: String? = ""
)

@Serializable
@Keep
data class Campus(
    val university: University? = null,
    val collegeName: String = "",
    val campusCode: String = "",
    val fieldOfStudy: String = "",
    val courseStart: Long ?= null,
    val courseEnd: Long ?= null
)

@Serializable
@Keep
data class University(
    val university: String="",
    val logo: String=""
)
