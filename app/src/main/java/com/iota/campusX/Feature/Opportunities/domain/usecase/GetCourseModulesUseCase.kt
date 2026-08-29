package com.iota.campusX.Feature.Opportunities.domain.usecase

import com.iota.campusX.Feature.Opportunities.data.model.ModuleResponse
import com.iota.campusX.Feature.Opportunities.domain.repository.OpportunitiesRepository

class GetCourseModulesUseCase(
    private val repository: OpportunitiesRepository
) {
    suspend operator fun invoke(courseId: String): Result<List<ModuleResponse>> {
        return repository.getCourseModules(courseId)
    }
}
