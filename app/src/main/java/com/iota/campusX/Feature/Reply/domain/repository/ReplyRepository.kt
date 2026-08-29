package com.iota.campusX.Feature.Reply.domain.repository

import android.net.Uri
import com.iota.campusX.Feature.Reply.data.remote.request.ReplyRequest
import com.iota.campusX.Feature.Reply.data.remote.response.ReplyResponse

interface ReplyRepository {

    suspend fun createReply(replyRequest: ReplyRequest,feedId: String, uploadImage: Uri?): Result<ReplyResponse>

    suspend fun getReplies(feedId: String): Result<List<ReplyResponse>>

    suspend fun getChildReplies(parentId: String): Result<List<ReplyResponse>>

    suspend fun likeReply(repliedById: String, postId: String, replyId: String, isLiked: Boolean): Result<Unit>

    suspend fun deleteReply(postId: String, replyId: String?): Result<Unit>

    suspend fun editReply(replyId: String, content: String): Result<Unit>

}