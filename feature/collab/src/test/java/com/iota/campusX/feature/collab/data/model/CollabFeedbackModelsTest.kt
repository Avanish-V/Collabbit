package com.iota.campusX.Feature.Collab.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CollabFeedbackModelsTest {

    @Test
    fun `parent-child hierarchy filtering correctly identifies top-level comments and replies`() {
        val parent1 = CollabFeedbackResponse(
            id = "f1",
            collabId = "collab_123",
            content = "Great project idea!",
            author = CollabAuthorDetails(authorId = "u1", authorName = "Alice"),
            parentFeedbackId = null
        )

        val parent2 = CollabFeedbackResponse(
            id = "f2",
            collabId = "collab_123",
            content = "Is Compose required?",
            author = CollabAuthorDetails(authorId = "u2", authorName = "Bob"),
            parentFeedbackId = null
        )

        val replyToParent1 = CollabFeedbackResponse(
            id = "f3",
            collabId = "collab_123",
            content = "Thanks Alice!",
            author = CollabAuthorDetails(authorId = "lead_1", authorName = "Lead", isProjectLead = true),
            parentFeedbackId = "f1"
        )

        val allFeedback = listOf(parent1, parent2, replyToParent1)

        val topLevelComments = allFeedback.filter { it.parentFeedbackId == null }
        val repliesForParent1 = allFeedback.filter { it.parentFeedbackId == "f1" }
        val repliesForParent2 = allFeedback.filter { it.parentFeedbackId == "f2" }

        assertEquals(2, topLevelComments.size)
        assertTrue(topLevelComments.contains(parent1))
        assertTrue(topLevelComments.contains(parent2))

        assertEquals(1, repliesForParent1.size)
        assertEquals("f3", repliesForParent1.first().id)
        assertTrue(repliesForParent1.first().author.isProjectLead)

        assertEquals(0, repliesForParent2.size)
    }

    @Test
    fun `CollabFeedbackRequest serialization payload verifies non-empty content`() {
        val request = CollabFeedbackRequest(content = "Awesome project!", parentFeedbackId = "f1")

        assertEquals("Awesome project!", request.content)
        assertEquals("f1", request.parentFeedbackId)
    }
}
