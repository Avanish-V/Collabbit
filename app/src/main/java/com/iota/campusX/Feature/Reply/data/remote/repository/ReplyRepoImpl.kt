package com.iota.campusX.Feature.Reply.data.remote.repository

import android.net.Uri
import android.util.Log
import com.iota.campusX.Feature.Post.data.remote.S3Uploader
import com.iota.campusX.Feature.Reply.data.local.dao.ReplyDao
import com.iota.campusX.Feature.Reply.data.local.mapper.toEntity
import com.iota.campusX.Feature.Reply.data.local.mapper.toResponse
import com.iota.campusX.Feature.Reply.data.remote.request.ReplyRequest
import com.iota.campusX.Feature.Reply.data.remote.response.ReplyResponse
import com.iota.campusX.Feature.Reply.domain.repository.ReplyRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReplyRepoImpl(
    private val httpClient: HttpClient,
    private val s3Uploader: S3Uploader,
    private val replyDao: ReplyDao
) : ReplyRepository {

    override suspend fun createReply(replyRequest: ReplyRequest, feedId: String, uploadImage: Uri?): Result<ReplyResponse> {
        return try {
            var updatedRequest = replyRequest

            if (uploadImage != null) {
                Log.d("ReplyRepo", "Uploading image...")
                val imageUrl = s3Uploader.UploadImageToS3(uploadImage)
                    ?: return Result.failure(Exception("Failed to upload image"))
                
                updatedRequest = replyRequest.copy(imageUrl = imageUrl)
                Log.d("ReplyRepo", "Image uploaded successfully: $imageUrl")
            }

            val response = httpClient.post("feed/$feedId/replies") {
                setBody(updatedRequest)
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                val reply = response.body<ReplyResponse>()
                replyDao.insertReply(reply.toEntity(feedId))
                Result.success(reply)
            } else {
                val errorMsg = when (response.status.value) {
                    401 -> "Unauthorized: Please sign in again."
                    403 -> "Forbidden: Permission denied."
                    444 -> "Post not found."
                    500 -> "Server error."
                    else -> "Error ${response.status.value}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("ReplyRepo", "Network error creating reply: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getReplies(feedId: String): Result<List<ReplyResponse>> {
        return try {
            val response: HttpResponse = httpClient.get("feed/$feedId/replies")

            if (response.status.isSuccess()) {
                val replies = response.body<List<ReplyResponse>>()
                replyDao.insertReplies(replies.map { it.toEntity(feedId) })
                Result.success(replies)
            } else {
                Result.failure(Exception("Failed to get replies: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("ReplyRepo", "Error fetching replies: ${e.message}")
            Result.failure(e)
        }
    }

    override fun observeReplies(feedId: String): Flow<List<ReplyResponse>> {
        return replyDao.getRepliesForPost(feedId).map { entities ->
            entities.map { it.toResponse() }
        }
    }

    override suspend fun getChildReplies(parentId: String): Result<List<ReplyResponse>> {
        return try {
            val response: HttpResponse = httpClient.get("replies/$parentId/children")

            if (response.status.isSuccess()) {
                val replies = response.body<List<ReplyResponse>>()
                // Note: feedId is hard to know here, but maybe it doesn't matter for children if we query by parentId
                // We'll try to find the parent to get its postId
                val parent = replyDao.getReplyById(parentId)
                val postId = parent?.postId ?: ""
                replyDao.insertReplies(replies.map { it.toEntity(postId) })
                Result.success(replies)
            } else {
                Result.failure(Exception("Failed to get child replies: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("ReplyRepo", "Error fetching child replies: ${e.message}")
            Result.failure(e)
        }
    }

    override fun observeChildReplies(parentId: String): Flow<List<ReplyResponse>> {
        return replyDao.getChildReplies(parentId).map { entities ->
            entities.map { it.toResponse() }
        }
    }

    override suspend fun likeReply(
        repliedById: String,
        postId: String,
        replyId: String,
        isLiked: Boolean
    ): Result<Unit> {

        // Optimistic update in DB
        val currentReply = replyDao.getReplyById(replyId)
        if (currentReply != null) {
            val newCount = if (isLiked) currentReply.likesCount + 1 else maxOf(0, currentReply.likesCount - 1)
            replyDao.updateLikeState(replyId, isLiked, newCount)
        }

        return try {
            val response = if (isLiked) {
                httpClient.post("replies/$replyId/like")
            } else {
                httpClient.delete("replies/$replyId/like")
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                // Rollback on failure
                if (currentReply != null) {
                    replyDao.updateLikeState(replyId, currentReply.isLiked, currentReply.likesCount)
                }
                Result.failure(Exception("Failed to update reply like: ${response.status}"))
            }
        } catch (e: Exception) {
            // Rollback on exception
            if (currentReply != null) {
                replyDao.updateLikeState(replyId, currentReply.isLiked, currentReply.likesCount)
            }
            Log.e("ReplyRepo", "Error updating reply like", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteReply(postId: String, replyId: String?): Result<Unit> {
        if (replyId == null) return Result.failure(Exception("ReplyId is null"))

        val currentReply = replyDao.getReplyById(replyId)

        // Optimistic delete
        replyDao.deleteReply(replyId)

        return try {
            val response: HttpResponse = httpClient.delete("replies/$replyId")

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                // Rollback if possible (insert back)
                currentReply?.let { replyDao.insertReply(it) }
                Result.failure(Exception("Failed to delete reply: ${response.status}"))
            }
        } catch (e: Exception) {
            currentReply?.let { replyDao.insertReply(it) }
            Log.e("ReplyRepo", "Error delete reply: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun editReply(replyId: String, content: String): Result<Unit> {
        val currentReply = replyDao.getReplyById(replyId)
        
        // Optimistic update
        if (currentReply != null) {
            replyDao.insertReply(currentReply.copy(content = content))
        }

        return try {
            val response: HttpResponse = httpClient.put("replies/$replyId"){
                setBody(content)
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                // Rollback
                currentReply?.let { replyDao.insertReply(it) }
                Result.failure(Exception("Failed to edit reply: ${response.status}"))
            }
        } catch (e: Exception) {
            currentReply?.let { replyDao.insertReply(it) }
            Log.e("ReplyRepo", "Error edit reply: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getReplyById(replyId: String): Result<ReplyResponse> {
        return try {
            val entity = replyDao.getReplyById(replyId)
            if (entity != null) {
                Result.success(entity.toResponse())
            } else {
                Result.failure(Exception("Reply not found in database"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
