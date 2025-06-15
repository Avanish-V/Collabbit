package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.PostRepository
import kotlinx.coroutines.flow.flow

class GetPostsUseCase(private val repository: PostRepository) {
    suspend operator fun invoke() = flow {
        emit(repository.getPosts())
    }
}
