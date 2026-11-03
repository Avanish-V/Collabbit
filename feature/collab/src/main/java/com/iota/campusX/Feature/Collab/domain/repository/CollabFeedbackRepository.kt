package com.iota.campusX.Feature.Collab.domain.repository

import com.iota.campusX.Feature.Collab.data.model.CollabFeedbackResponse

interface CollabFeedbackRepository {
    suspend fun getCollabFeedback(collabId: String): Result<List<CollabFeedbackResponse>>
    suspend fun addCollabFeedback(collabId: String, content: String): Result<CollabFeedbackResponse>
    suspend fun replyToCollabFeedback(collabId: String, feedbackId: String, replyText: String): Result<CollabFeedbackResponse>
}
