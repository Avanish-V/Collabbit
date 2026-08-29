package com.iota.campusX.Feature.Opportunities.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpportunityApplicationRequest(
    @SerialName("externalUserId") val externalUserId: String,
    @SerialName("guestName") val guestName: String,
    @SerialName("guestEmail") val guestEmail: String,
    @SerialName("guestResumeUrl") val guestResumeUrl: String? = null,
    @SerialName("guestCoverLetter") val guestCoverLetter: String? = null
)
