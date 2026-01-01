package com.iota.campusX.Feature.UserProfile.data.remote.dtos

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class LinkUpRequestDTO(
    var senderId: String = "",
    var status: Boolean = false,

    @Contextual
    @ServerTimestamp
    val createdAt: Timestamp? = null
)