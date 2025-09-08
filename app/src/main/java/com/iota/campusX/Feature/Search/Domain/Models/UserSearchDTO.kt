package com.iota.campusX.Feature.Search.Domain.Models

import androidx.annotation.Keep

@Keep
data class UserSearchDTO(
    val userName: String = "",
    val userImage: String = "",
    val userBio: String = "",
    val id: String = ""
)
