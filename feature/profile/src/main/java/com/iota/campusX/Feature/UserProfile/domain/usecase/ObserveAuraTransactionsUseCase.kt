package com.iota.campusX.Feature.UserProfile.domain.usecase

import com.iota.campusX.Feature.UserProfile.data.local.entities.AuraTransactionEntity
import com.iota.campusX.Feature.UserProfile.domain.repository.AuraRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe all aura transactions for a specific user.
 * 
 * Typically used to display an audit log or history of how aura
 * points were earned (daily check-in, profile completion, etc.).
 */
class ObserveAuraTransactionsUseCase(
    private val repository: AuraRepository
) {
    operator fun invoke(userId: String): Flow<List<AuraTransactionEntity>> {
        return repository.observeTransactions(userId)
    }
}
