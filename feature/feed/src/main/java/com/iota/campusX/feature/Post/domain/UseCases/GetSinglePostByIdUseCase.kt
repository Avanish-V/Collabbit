package com.iota.campusX.Feature.Post.domain.UseCases


import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface

class GetSinglePostByIdUseCase(private val repository: PostRepositoryInterface) {
    suspend operator fun invoke(postId: String): Result<Post> = repository.fetchSinglePost(postId)

}