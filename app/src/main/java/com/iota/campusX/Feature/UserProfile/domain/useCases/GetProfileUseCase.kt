package com.iota.campusX.Feature.UserProfile.domain.useCases

import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import org.koin.java.KoinJavaComponent.getKoin

class GetProfileUseCase(
    private val repository: UserProfileRepository
) {
    suspend operator fun invoke(): Result<BaseProfileDTO> {
        return try {
            val profile = repository.getUserProfile().first()

            if (profile == null) {
                Result.failure(Exception("Profile not found"))
            } else {
                Result.success(profile)
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
