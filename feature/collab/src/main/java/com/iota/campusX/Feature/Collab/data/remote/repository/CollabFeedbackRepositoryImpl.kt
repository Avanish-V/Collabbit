package com.iota.campusX.Feature.Collab.data.remote.repository

import android.util.Log
import com.iota.campusX.Feature.Collab.data.model.CollabAuthorDetails
import com.iota.campusX.Feature.Collab.data.model.CollabFeedbackRequest
import com.iota.campusX.Feature.Collab.data.model.CollabFeedbackResponse
import com.iota.campusX.Feature.Collab.domain.repository.CollabFeedbackRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class CollabFeedbackRepositoryImpl(
    private val httpClient: HttpClient
) : CollabFeedbackRepository {

    private val TAG = "CollabFeedbackRepo"
    private val feedbackCache = ConcurrentHashMap<String, MutableList<CollabFeedbackResponse>>()

    override suspend fun getCollabFeedback(collabId: String): Result<List<CollabFeedbackResponse>> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = httpClient.get("collabs/$collabId/feedback")
            if (response.status.isSuccess()) {
                val list = response.body<List<CollabFeedbackResponse>>()
                feedbackCache[collabId] = list.toMutableList()
                list
            } else {
                feedbackCache[collabId] ?: emptyList()
            }
        }.onFailure {
            Log.e(TAG, "getCollabFeedback failed: ${it.message}")
        }
    }

    override suspend fun addCollabFeedback(
        collabId: String,
        content: String
    ): Result<CollabFeedbackResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = httpClient.post("collabs/$collabId/feedback") {
                contentType(ContentType.Application.Json)
                setBody(CollabFeedbackRequest(content = content))
            }
            if (response.status.isSuccess()) {
                val created = response.body<CollabFeedbackResponse>()
                val list = feedbackCache.getOrPut(collabId) { mutableListOf() }
                list.add(0, created)
                created
            } else {
                val created = CollabFeedbackResponse(
                    id = UUID.randomUUID().toString(),
                    collabId = collabId,
                    content = content,
                    author = CollabAuthorDetails(
                        authorId = "user_me",
                        authorName = "You",
                        isCurrentUser = true
                    ),
                    timeAgo = "Just now"
                )
                val list = feedbackCache.getOrPut(collabId) { mutableListOf() }
                list.add(0, created)
                created
            }
        }.onFailure {
            Log.e(TAG, "addCollabFeedback failed: ${it.message}")
        }
    }

    override suspend fun replyToCollabFeedback(
        collabId: String,
        feedbackId: String,
        replyText: String
    ): Result<CollabFeedbackResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = httpClient.post("collabs/$collabId/feedback/$feedbackId/reply") {
                contentType(ContentType.Application.Json)
                setBody(CollabFeedbackRequest(content = replyText, parentFeedbackId = feedbackId))
            }
            if (response.status.isSuccess()) {
                val created = response.body<CollabFeedbackResponse>()
                val list = feedbackCache.getOrPut(collabId) { mutableListOf() }
                list.add(created)
                created
            } else {
                val createdReply = CollabFeedbackResponse(
                    id = UUID.randomUUID().toString(),
                    collabId = collabId,
                    content = replyText,
                    author = CollabAuthorDetails(
                        authorId = "user_me",
                        authorName = "You",
                        isCurrentUser = true
                    ),
                    parentFeedbackId = feedbackId,
                    timeAgo = "Just now"
                )
                val list = feedbackCache.getOrPut(collabId) { mutableListOf() }
                list.add(createdReply)
                createdReply
            }
        }.onFailure {
            Log.e(TAG, "replyToCollabFeedback failed: ${it.message}")
        }
    }
}
