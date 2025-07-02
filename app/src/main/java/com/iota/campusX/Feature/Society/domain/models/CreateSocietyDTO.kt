package com.iota.campusX.Feature.Society.domain.models

import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.UserDetail

data class CreateSocietyDTO(
    val societyName: String = "",
    val description: String = "",
    val createdBy: String = "",
    val roomId: String = "",
    val joined: List<String> = emptyList(),
    val mode: FeedMode = FeedMode.GLOBAL,
    val campusId: String? = null,
    val isActive: Boolean = false

)

data class GetSocietyDTO(
    val societyName: String = "",
    val description: String = "",
    val roomId: String = "",
    val createdBy: UserDetail = UserDetail(),
    val joined: List<String> = emptyList(),
    val mode: FeedMode = FeedMode.GLOBAL,
    val campusId: String? = null,
    val isActive: Boolean = false
)
