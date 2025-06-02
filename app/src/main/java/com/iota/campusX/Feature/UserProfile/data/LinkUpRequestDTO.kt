package com.iota.campusX.Feature.UserProfile.data

import kotlinx.serialization.Serializable

@Serializable
data class LinkUpRequestDTO(
    var senderId: String = "",
    var status: Boolean = false,
    var createdAt: Long = 0L
) {
    constructor() : this("", false)
}