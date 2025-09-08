package com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions

import com.iota.campusX.Feature.Society.domain.repository.SocietyInterface
import com.iota.campusX.Feature.Society.domain.repository.SocietyRepository

class SocietyOptionRepository(private val societyRepository: SocietyRepository): SocietyOptionsInterface {

    override suspend fun getMenuOptions(content: SocietyData): List<SocietyMenuOptions> {
        return if (content.isOwner) {
            listOf( SocietyMenuOptions.Delete)
        } else {
            listOf(SocietyMenuOptions.Notify)
        }
    }

    override suspend fun executeAction(action: SocietyMenuOptions, content: SocietyData): Result<Unit> {

        return when (action) {
            SocietyMenuOptions.Delete -> {
                val result = societyRepository.deleteSociety(societyId = content.roomId)
                result.fold(
                    onSuccess = {
                        societyRepository.removeSocietyLocally(content.roomId)
                        Result.success(Unit)
                    },
                    onFailure = { Result.failure(it) }
                )
            }
            SocietyMenuOptions.Edit -> {
                // Handle edit action
                Result.success(Unit)
            }
            SocietyMenuOptions.Notify -> {
                // Handle notify action
                Result.success(Unit)
            }

        }
    }


}