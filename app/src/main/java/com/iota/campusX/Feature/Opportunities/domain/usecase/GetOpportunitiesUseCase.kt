package com.iota.campusX.Feature.Opportunities.domain.usecase

import com.iota.campusX.Feature.Opportunities.data.model.OpportunityResponse
import com.iota.campusX.Feature.Opportunities.domain.repository.OpportunitiesRepository

class GetOpportunitiesUseCase(
    private val repository: OpportunitiesRepository
) {
    suspend operator fun invoke(): Result<List<OpportunityResponse>> {
        return repository.getOpportunities()
    }
}
