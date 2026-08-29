package com.iota.campusX.Feature.Reply.data.remote.repository

import android.net.Uri
import android.util.Log
import com.iota.campusX.Feature.Post.data.remote.S3Uploader
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

class ReplyRepoImpl(
    private val httpClient: HttpClient,
    private val s3Uploader: S3Uploader
) : ReplyRepository {

    override suspend fun createReply(replyRequest: ReplyRequest, feedId: String, uploadImage: Uri?): Result<ReplyResponse> {
        return try {
            var updatedRequest = replyRequest

            if (uploadImage != null) {
                Log.d("ReplyRepo", "Uploading image...")
                val imageUrl = s3Uploader.UploadImageToS3(uploadImage)
                    ?: return Result.failure(Exception("Failed to upload image"))
                
                updatedRequest = replyRequest.copy(mediaUrl = imageUrl)
                Log.d("ReplyRepo", "Image uploaded successfully: $imageUrl")
            }

            val response = httpClient.post("feed/$feedId/replies") {
                setBody(updatedRequest)
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
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
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to get replies: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("ReplyRepo", "Error fetching replies: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getChildReplies(parentId: String): Result<List<ReplyResponse>> {
        return try {
            val response: HttpResponse = httpClient.get("replies/$parentId/children")

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to get child replies: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("ReplyRepo", "Error fetching child replies: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun likeReply(
        repliedById: String,
        postId: String,
        replyId: String,
        isLiked: Boolean
    ): Result<Unit> {

        return try {

            val response = if (isLiked) {

                httpClient.post(
                    "replies/$replyId/like"
                )

            } else {

                httpClient.delete(
                    "replies/$replyId/like"
                )
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        "Failed to update reply like: ${response.status}"
                    )
                )
            }

        } catch (e: Exception) {

            Log.e(
                "ReplyRepo",
                "Error updating reply like",
                e
            )

            Result.failure(e)
        }
    }

    override suspend fun deleteReply(postId: String, replyId: String?): Result<Unit> {
        return try {
            val response: HttpResponse = httpClient.delete("replies/$replyId")

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete reply: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("ReplyRepo", "Error delete reply: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun editReply(replyId: String, content: String): Result<Unit> {
        return try {
            val response: HttpResponse = httpClient.put("replies/$replyId"){
                setBody(content)
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to edit reply: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("ReplyRepo", "Error edit reply: ${e.message}")
            Result.failure(e)
        }
    }
}
