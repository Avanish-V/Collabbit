package com.iota.campusX.Feature.Post.domain.UseCases

import androidx.paging.PagingData
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import kotlinx.coroutines.flow.Flow

class GetPostByIdUseCase(private val repository: PostRepositoryInterface) {
    suspend operator fun invoke(userId: String): Flow<PagingData<GetPostDTO>>  = repository.getPostsById(userId)

}