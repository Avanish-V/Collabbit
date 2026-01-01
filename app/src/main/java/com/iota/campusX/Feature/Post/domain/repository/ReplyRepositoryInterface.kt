package com.iota.campusX.Feature.Post.domain.repository

import android.net.Uri
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetRepliesDTO
import com.iota.campusX.Feature.Post.data.model.ReplyRequest
import com.iota.campusX.Feature.Post.data.model.ReplyResponse
import com.iota.campusX.Feature.Post.data.model.UserReplyDTO
import com.iota.campusX.Feature.Post.data.model.VisibilityMode

interface ReplyRepositoryInterface {

    suspend fun createReply(replyRequest: ReplyRequest,uploadImage: Uri?): Result<ReplyResponse>

    suspend fun getReplies(postId: String,campusId: String?,feedMode: FeedMode): Result<List<ReplyResponse>>

    suspend fun likeReply(repliedById: String, postId: String, replyId: String, isLiked: Boolean): Result<Unit>

    suspend fun deleteReply(postId: String, replyId: String?): Result<Unit>

    suspend fun editReply(postId: String, replyId: String?, content: String?): Result<Unit>

    suspend fun fetchUserReplies(userId: String): Result<List<UserReplyDTO>>
}