package com.iota.campusX.Feature.Opportunities.domain.usecase

import com.iota.campusX.Feature.Opportunities.data.model.CourseResponse
import com.iota.campusX.Feature.Opportunities.domain.repository.OpportunitiesRepository

class GetCoursesUseCase(private val repository: OpportunitiesRepository) {
    suspend operator fun invoke(): Result<List<CourseResponse>> {
        return repository.getCourses()
    }
}
