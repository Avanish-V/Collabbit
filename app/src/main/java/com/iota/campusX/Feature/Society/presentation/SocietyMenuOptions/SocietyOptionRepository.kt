package com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions

import com.iota.campusX.Feature.Society.domain.repository.SocietyInterface
import com.iota.campusX.Feature.Society.domain.repository.SocietyRepository
import com.iota.campusX.Navigation.Routes

class SocietyOptionRepository(
    private val societyRepository: SocietyRepository,
    private val societyInterface: SocietyInterface
): SocietyOptionsInterface {

    override suspend fun getMenuOptions(content: SocietyData): List<SocietyMenuOptions> {
        return if (content.isOwner) {
            listOf( SocietyMenuOptions.Delete)
        } else {
            listOf(SocietyMenuOptions.Notify)
        }
    }

    override suspend fun executeAction(action: SocietyMenuOptions, content: SocietyData,hasAlreadySubscribe: Boolean?): Result<Unit> {

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
                content.navHostController?.navigate(Routes.Main.CreateSociety.routes)
                Result.success(Unit)
            }
            SocietyMenuOptions.Notify -> {

                if (hasAlreadySubscribe == null) return Result.failure(
                    Exception("hasAlreadySubscribe is null")
                )

              val result =   if (hasAlreadySubscribe){
                    societyInterface.unsubscribeRoom(
                        roomId = content.roomId,
                    )
                }else{
                    societyInterface.subscribeRoom(
                        roomId = content.roomId,
                    )
                }

                result.fold(
                    onSuccess = {

                        Result.success(Unit)
                    },
                    onFailure = {
                        Result.failure(it)
                    }

                )

            }
        }
    }

}