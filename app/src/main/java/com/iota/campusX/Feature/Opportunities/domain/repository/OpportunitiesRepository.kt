package com.iota.campusX.Feature.Opportunities.domain.repository

import com.iota.campusX.Feature.Opportunities.data.model.CourseEnrollmentRequest
import com.iota.campusX.Feature.Opportunities.data.model.CourseResponse
import com.iota.campusX.Feature.Opportunities.data.model.ModuleResponse
import com.iota.campusX.Feature.Opportunities.data.model.OpportunityApplicationRequest
import com.iota.campusX.Feature.Opportunities.data.model.OpportunityResponse

interface OpportunitiesRepository {
    suspend fun getOpportunities(): Result<List<OpportunityResponse>>
    suspend fun getOpportunityById(id: String): Result<OpportunityResponse>
    suspend fun getCourses(): Result<List<CourseResponse>>
    suspend fun getCourseById(id: String): Result<CourseResponse>
    suspend fun getCourseModules(courseId: String): Result<List<ModuleResponse>>
    suspend fun enrollInCourse(courseId: String, request: CourseEnrollmentRequest): Result<Unit>
    suspend fun applyForOpportunity(opportunityId: String, request: OpportunityApplicationRequest): Result<Unit>
}
