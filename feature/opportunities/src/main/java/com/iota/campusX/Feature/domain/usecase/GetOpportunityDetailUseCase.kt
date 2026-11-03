package com.iota.campusX.Feature.Opportunities.domain.usecase

import com.iota.campusX.Feature.Opportunities.data.model.OpportunityResponse
import com.iota.campusX.Feature.Opportunities.domain.repository.OpportunitiesRepository

class GetOpportunityDetailUseCase(
    private val repository: OpportunitiesRepository
) {
    suspend operator fun invoke(id: String): Result<OpportunityResponse> {
        return repository.getOpportunityById(id)
    }
}
