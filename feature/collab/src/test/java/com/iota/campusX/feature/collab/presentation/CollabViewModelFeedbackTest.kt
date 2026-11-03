package com.iota.campusX.Feature.Collab.presentation

import com.iota.campusX.Feature.Collab.data.model.CollabAuthorDetails
import com.iota.campusX.Feature.Collab.data.model.CollabFeedbackResponse
import com.iota.campusX.Feature.Collab.domain.repository.CollabFeedbackRepository
import com.iota.campusX.Feature.Collab.domain.repository.CollabRepository
import com.iota.campusX.Feature.Collab.domain.usecase.CreateCollabUseCase
import com.iota.campusX.Feature.Post.data.remote.S3Uploader
import com.iota.campusX.core.common.UiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollabViewModelFeedbackTest {

    private val testDispatcher = StandardTestDispatcher()
    private val collabRepository = mockk<CollabRepository>(relaxed = true)
    private val feedbackRepository = mockk<CollabFeedbackRepository>(relaxed = true)
    private val createCollabUseCase = mockk<CreateCollabUseCase>(relaxed = true)
    private val s3Uploader = mockk<S3Uploader>(relaxed = true)

    private lateinit var viewModel: CollabViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CollabViewModel(
            repository = collabRepository,
            feedbackRepository = feedbackRepository,
            createCollabUseCase = createCollabUseCase,
            s3Uploader = s3Uploader
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchFeedback updates state to Success when repository returns feedback list`() = runTest {
        val mockList = listOf(
            CollabFeedbackResponse(
                id = "f1",
                collabId = "c1",
                content = "Great idea!",
                author = CollabAuthorDetails(authorId = "u1", authorName = "Alice")
            )
        )
        coEvery { feedbackRepository.getCollabFeedback("c1") } returns Result.success(mockList)

        viewModel.fetchFeedback("c1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.feedbackState.value
        assertTrue(state is UiState.Success)
        assertEquals(mockList, (state as UiState.Success).data)
    }

    @Test
    fun `addFeedback submits feedback and refreshes feedback list`() = runTest {
        val newFeedback = CollabFeedbackResponse(id = "f1", collabId = "c1", content = "Looking forward to this")
        coEvery { feedbackRepository.addCollabFeedback("c1", "Looking forward to this") } returns Result.success(newFeedback)
        coEvery { feedbackRepository.getCollabFeedback("c1") } returns Result.success(listOf(newFeedback))

        viewModel.addFeedback("c1", "Looking forward to this")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { feedbackRepository.addCollabFeedback("c1", "Looking forward to this") }
        coVerify { feedbackRepository.getCollabFeedback("c1") }
    }

    @Test
    fun `addFeedback ignores blank content`() = runTest {
        viewModel.addFeedback("c1", "   ")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { feedbackRepository.addCollabFeedback(any(), any()) }
    }

    @Test
    fun `replyToFeedback submits lead response and refreshes list`() = runTest {
        val replyFeedback = CollabFeedbackResponse(
            id = "f2",
            collabId = "c1",
            content = "Thanks!",
            parentFeedbackId = "f1"
        )
        coEvery { feedbackRepository.replyToCollabFeedback("c1", "f1", "Thanks!") } returns Result.success(replyFeedback)
        coEvery { feedbackRepository.getCollabFeedback("c1") } returns Result.success(listOf(replyFeedback))

        viewModel.replyToFeedback("c1", "f1", "Thanks!")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { feedbackRepository.replyToCollabFeedback("c1", "f1", "Thanks!") }
        coVerify { feedbackRepository.getCollabFeedback("c1") }
    }
}
