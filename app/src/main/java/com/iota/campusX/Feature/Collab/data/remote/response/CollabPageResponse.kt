package com.iota.campusX.Feature.Collab.data.remote.response

import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import kotlinx.serialization.Serializable

@Serializable
data class CollabPageResponse(
    val content: List<CollabResponse>,
    val empty: Boolean,
    val first: Boolean,
    val last: Boolean,
    val number: Int,
    val numberOfElements: Int,
    val totalElements: Int,
    val totalPages: Int
)
