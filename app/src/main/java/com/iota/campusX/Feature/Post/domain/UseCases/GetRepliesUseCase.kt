package com.iota.campusX.Feature.Post.domain.UseCases

import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.PostRepository
import com.iota.campusX.Feature.Post.domain.ReplyRepository

class GetRepliesUseCase(private val repository: ReplyRepository) {
    suspend operator fun invoke(postId: String,campusId:String?,feedMode: FeedMode): Result<List<GetRepliesDTO>> {
        return repository.getReplies(postId,campusId,feedMode)
    }
}
