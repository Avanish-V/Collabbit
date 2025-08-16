package com.iota.campusX.Feature.Post.domain

import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.Models.UserReplyDTO

interface ReplyRepository {

    suspend fun createReply(replyId: String, postId: String, content: String, postCreatorId: String, visibilityMode: PostVisibilityMode, mode: FeedMode, campusId: String?): Result<Unit>

    suspend fun getReplies(postId: String,campusId: String?,feedMode: FeedMode): Result<List<GetRepliesDTO>>

    suspend fun likeReply(repliedById: String, postId: String, replyId: String, isLiked: Boolean,campusId: String?, feedMode: FeedMode): Result<Unit>

    suspend fun deleteReply(postId: String, replyId: String, campusId: String?,feedMode: FeedMode): Result<Unit>

    suspend fun editReply(postId: String,replyId: String, content: String, campusId: String?, feedMode: FeedMode): Result<Unit>

    suspend fun fetchUserReplies(userId: String): Result<List<UserReplyDTO>>
}