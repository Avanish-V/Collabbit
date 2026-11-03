package com.iota.campusX.Feature.Opportunities.domain.usecase

import com.iota.campusX.Feature.Opportunities.data.model.OpportunityApplicationRequest
import com.iota.campusX.Feature.Opportunities.domain.repository.OpportunitiesRepository

class ApplyForOpportunityUseCase(
    private val repository: OpportunitiesRepository
) {
    suspend operator fun invoke(opportunityId: String, request: OpportunityApplicationRequest): Result<Unit> {
        return repository.applyForOpportunity(opportunityId, request)
    }
}
