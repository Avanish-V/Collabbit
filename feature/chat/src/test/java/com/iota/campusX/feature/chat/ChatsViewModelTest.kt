package com.iota.campusX.feature.chat

import com.iota.campusX.Feature.Chats.data.ChatMessage
import com.iota.campusX.Feature.Chats.data.UserChatsDTO
import com.iota.campusX.Feature.Chats.domain.ChatRequestStatus
import com.iota.campusX.Feature.Chats.domain.ChatRepository
import com.iota.campusX.Feature.Chats.domain.SwipeActionResponse
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.core.common.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeChatRepository : ChatRepository {
    var lastSwipeTargetUserId: String? = null
    var lastSwipeAction: String? = null

    override suspend fun sendMessage(message: String, receiverId: String, roomId: String): Result<Boolean> = Result.success(true)
    override fun updateIsUserActive(isActive: Boolean, roomId: String) {}
    override fun getIsUserActive(receiverId: String, roomId: String): Flow<Boolean> = flowOf(true)
    override fun updateIsUserTyping(isActive: Boolean, roomId: String) {}
    override fun getIsUserTyping(participantId: String, roomId: String): Flow<Boolean> = flowOf(false)
    override fun receiveMessage(participantId: String, roomId: String): Flow<ResultState<List<ChatMessage>>> = flowOf(ResultState.Success(emptyList()))
    override fun getChats(): Flow<ResultState<List<UserChatsDTO>>> = flowOf(ResultState.Success(emptyList()))
    override suspend fun markMessagesAsReed(participantId: String, roomId: String) {}
    override suspend fun getRoomId(participantId: String): Result<String> = Result.success("room1")
    override suspend fun deleteChat(chatId: String, roomId: String): Result<Boolean> = Result.success(true)
    override suspend fun fetchChatRoomId(userId: String): Result<String> = Result.success("room1")
    override suspend fun fetchAndSaveIncomingMessage(messageId: String, roomId: String) {}
    override fun observeUnreadCount(): Flow<Int> = flowOf(0)
    override fun observeRoomStatus(roomId: String): Flow<String?> = flowOf("ACCEPTED")
    override suspend fun fetchRoomStatus(roomId: String): Result<String> = Result.success("ACCEPTED")
    override suspend fun swipeRightAction(
        targetUserId: String,
        roomId: String?,
        action: String,
        note: String?
    ): Result<SwipeActionResponse> {
        lastSwipeTargetUserId = targetUserId
        lastSwipeAction = action
        return Result.success(SwipeActionResponse(status = "ACCEPTED", roomId = roomId ?: "room1"))
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ChatsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = FakeChatRepository()
    private lateinit var viewModel: ChatsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ChatsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun observeRoomStatus_updatesState() = runTest {
        viewModel.observeRoomStatus("room1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(ChatRequestStatus.ACCEPTED, viewModel.roomStatus.value)
    }

    @Test
    fun swipeRightAction_callsRepository() = runTest {
        viewModel.swipeRightAction("user1", "room1", "ACCEPT")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("user1", repository.lastSwipeTargetUserId)
        assertEquals("ACCEPT", repository.lastSwipeAction)
    }
}
