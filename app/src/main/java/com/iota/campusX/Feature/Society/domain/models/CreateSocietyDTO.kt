package com.iota.campusX.Feature.Society.domain.models

import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.UserDetail

data class CreateSocietyDTO(
    val societyName: String = "",
    val description: String = "",
    val createdBy: String = "",
    val roomId: String = "",
    val joined: List<String> = emptyList(),
    val mode: FeedMode = FeedMode.GLOBAL,
    val campusId: String? = null,
    val isActive: Boolean = false,


)

data class GetSocietyDTO(
    val societyName: String = "",
    val description: String = "",
    val roomId: String = "",
    val createdBy: UserDetail = UserDetail(),
    val joined: List<String> = emptyList(),
    val mode: FeedMode = FeedMode.GLOBAL,
    val campusId: String? = null,
    val isActive: Boolean = false,
    val isCurrentUser: Boolean = false
)

