package com.iota.campusX.Feature.Post.domain.UseCases

import androidx.paging.PagingData
import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import kotlinx.coroutines.flow.Flow

class GetPostsUseCase(private val repository: PostRepositoryInterface) {
     operator fun invoke() : Flow<PagingData<Post>> = repository.getPosts()
}
