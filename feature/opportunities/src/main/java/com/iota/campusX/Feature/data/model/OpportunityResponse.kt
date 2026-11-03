package com.iota.campusX.Feature.Opportunities.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpportunityResponse(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("location") val location: String,
    @SerialName("type") val type: String,                        // "Job" or "Internship"
    @SerialName("stipend") val stipend: String,
    @SerialName("posted") val posted: String,
    @SerialName("applicants") val applicants: Int,
    @SerialName("status") val status: String,
    @SerialName("description") val description: String,
    @SerialName("skills") val skills: List<String> = emptyList(),
    @SerialName("recruiterUid") val recruiterUid: String,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("posterType") val posterType: String,            // "COMPANY_PROFILE"
    @SerialName("postingSource") val postingSource: String,      // "COMPANY_PROFILE"
    @SerialName("companyName") val companyName: String? = null,
    @SerialName("companyLogoUrl") val companyLogoUrl: String? = null,
    @SerialName("applyUrl") val applyUrl: String? = null,
    @SerialName("deadline") val deadline: String? = null,        // ISO-8601 date string
    @SerialName("durationMonths") val durationMonths: Int? = null,
    
    // UI helpers or fields potentially missing from simplified DTO but present in some responses
    @SerialName("responsibilities") val responsibilities: List<String> = emptyList(),
    @SerialName("requirements") val requirements: List<String> = emptyList(),
    @SerialName("companyWebsite") val companyWebsite: String? = null,
    @SerialName("companyIndustry") val companyIndustry: String? = null,
    @SerialName("companyCity") val companyCity: String? = null
) {
    val displayCompanyName: String get() = companyName ?: "Unknown Company"
}
