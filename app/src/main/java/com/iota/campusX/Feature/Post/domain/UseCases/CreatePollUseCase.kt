package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface

class CreatePollUseCase(
    private val pollRepository: PostRepositoryInterface
) {
//    suspend operator fun invoke(postType: PostPayload): Result<Unit> {
//        return try {
//            pollRepository.createPoll(postType)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
}
