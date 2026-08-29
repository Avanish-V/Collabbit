package com.iota.campusX.Feature.Opportunities.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompanyInfoResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("logoUrl") val logoUrl: String? = null,
    @SerialName("website") val website: String? = null,
    @SerialName("industry") val industry: String? = null,
    @SerialName("city") val city: String? = null
)

@Serializable
data class OpportunityResponse(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("company") val company: String? = null,
    @SerialName("companyInfo") val companyInfo: CompanyInfoResponse? = null,
    @SerialName("location") val location: String,
    @SerialName("type") val type: String, // "Job" or "Internship"
    @SerialName("stipend") val stipend: String,
    @SerialName("posted") val posted: String,
    @SerialName("applicants") val applicants: Int = 0,
    @SerialName("status") val status: String,
    @SerialName("description") val description: String,
    @SerialName("skills") val skills: List<String> = emptyList(),
    @SerialName("recruiterUid") val recruiterUid: String,
    @SerialName("createdAt") val createdAt: String,
    // Legacy fields for backward compatibility
    @SerialName("min_salary") val minSalary: String? = null,
    @SerialName("max_salary") val maxSalary: String? = null,
    @SerialName("deadline") val deadline: String? = null,
    @SerialName("duration") val duration: String? = null,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("posted_by") val postedBy: String? = null,
    @SerialName("created_at") val createdAtLegacy: String? = null
) {
    val displayCompanyName: String
        get() = companyInfo?.name ?: company ?: "Unknown Company"
    
    val companyLogoUrl: String?
        get() = companyInfo?.logoUrl
}
