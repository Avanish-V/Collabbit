package com.iota.campusX.Feature.Post.domain.repository

import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetRepliesDTO
import com.iota.campusX.Feature.Post.data.model.UserReplyDTO
import com.iota.campusX.Feature.Post.data.model.VisibilityMode

interface ReplyRepositoryInterface {

    suspend fun createReply(replyId: String, postId: String, content: String, postCreatorId: String, visibilityMode: VisibilityMode): Result<Unit>

    suspend fun getReplies(postId: String,campusId: String?,feedMode: FeedMode): Result<List<GetRepliesDTO>>

    suspend fun likeReply(repliedById: String, postId: String, replyId: String, isLiked: Boolean): Result<Unit>

    suspend fun deleteReply(postId: String, replyId: String?): Result<Unit>

    suspend fun editReply(postId: String, replyId: String?, content: String): Result<Unit>

    suspend fun fetchUserReplies(userId: String): Result<List<UserReplyDTO>>
}