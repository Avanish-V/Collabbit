package com.iota.campusX.Feature.Opportunities.domain.usecase

import com.iota.campusX.Feature.Opportunities.data.model.CourseEnrollmentRequest
import com.iota.campusX.Feature.Opportunities.domain.repository.OpportunitiesRepository

class EnrollInCourseUseCase(
    private val repository: OpportunitiesRepository
) {
    suspend operator fun invoke(courseId: String, request: CourseEnrollmentRequest): Result<Unit> {
        return repository.enrollInCourse(courseId, request)
    }
}
