package com.iota.campusX.Feature.Opportunities.domain.usecase

import com.iota.campusX.Feature.Opportunities.data.model.CourseResponse
import com.iota.campusX.Feature.Opportunities.domain.repository.OpportunitiesRepository

class GetCourseDetailUseCase(private val repository: OpportunitiesRepository) {
    suspend operator fun invoke(id: String): Result<CourseResponse> {
        return repository.getCourseById(id)
    }
}
