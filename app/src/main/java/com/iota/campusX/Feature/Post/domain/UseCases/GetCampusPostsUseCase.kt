package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.domain.PostRepository
import kotlinx.coroutines.flow.flow

class GetCampusPostsUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(feedMode: FeedMode, campusId: String?) = flow {
        emit(repository.fetchCampusPosts(feedMode, campusId))
    }
}
