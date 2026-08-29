package com.iota.campusX.Feature.Post.domain.UseCases


import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface

class DeletePostUseCase(private val repository: PostRepositoryInterface) {
    suspend operator fun invoke(postId: String) =
        repository.deletePost(postId)
}

