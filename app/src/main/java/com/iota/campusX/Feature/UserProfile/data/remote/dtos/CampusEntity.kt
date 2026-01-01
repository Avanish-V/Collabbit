package com.iota.campusX.Feature.UserProfile.data.remote.dtos


data class SetCampus(
    val collegeName: String? = null,
    val university: String? = null,
    val logo: String? = null,
    val fieldOfStudy: String? = null,
    val code: String? = null,
    val degree: String? = null,
    val courseStart: String? = null,
    val startTimestamp: Long? = null,
    val courseEnd: String? = null,
    val endTimestamp: Long? = null,
    val isCurrent: Boolean = false,

)