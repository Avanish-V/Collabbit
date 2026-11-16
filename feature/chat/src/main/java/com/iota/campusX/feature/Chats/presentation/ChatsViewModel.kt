package com.iota.campusX.Feature.Chats.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Chats.data.ChatMessage
import com.iota.campusX.Feature.Chats.data.UserChatsDTO
import com.iota.campusX.Feature.Chats.domain.ChatRepository
import com.iota.campusX.core.common.ResultState
import com.iota.campusX.core.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ChatsViewModel(private val chatRepository: ChatRepository):ViewModel() {

    init {
        getChats()
    }

    private var receiveMessageJob: Job? = null
    private var isActiveJob: Job? = null
    private var isTypingJob: Job? = null


    private val _hasMessage: MutableStateFlow<String> = MutableStateFlow("")
    val hasMessage: StateFlow<String> = _hasMessage.asStateFlow()

    private val _textMessage:MutableStateFlow<String> = MutableStateFlow("")
    val textMessage:StateFlow<String> = _textMessage.asStateFlow()

    private val _userChats = MutableStateFlow<UiState<List<UserChatsDTO>>>(UiState.Loading)
    val userChats: StateFlow<UiState<List<UserChatsDTO>>> = _userChats.asStateFlow()

    val unreadMessageCount: StateFlow<Int> = chatRepository
        .observeUnreadCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _chats: MutableStateFlow<List<ChatMessage>> = MutableStateFlow(emptyList())
    val chats:StateFlow<List<ChatMessage>> = _chats.asStateFlow()

    private val _isActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isActive:StateFlow<Boolean> = _isActive.asStateFlow()

    private val _isUserTyping: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isUserTyping:StateFlow<Boolean> = _isUserTyping.asStateFlow()


    fun textMessageInput(inputText: String){
        _textMessage.value = inputText
    }

    fun sendMessages(
        message: String,
        receiverId: String,
        roomId: String
    ){
        viewModelScope.launch {
            chatRepository.sendMessage(message,receiverId,roomId)
        }
    }

    fun receiveMessage(participantId: String,roomId: String){
        receiveMessageJob?.cancel()
        _chats.value = emptyList() // Clear messages immediately when room changes
        receiveMessageJob = viewModelScope.launch {
            chatRepository.receiveMessage(participantId = participantId,roomId = roomId).collect{
                when(it){
                    is ResultState.Loading->{}
                    is ResultState.Success->{
                        _chats.value = it.data
                    }
                    is ResultState.Error->{}
                }

            }
        }
    }

    fun updateIsUserActive(isActive:Boolean,roomId: String) = chatRepository.updateIsUserActive(isActive,roomId)

    fun getIsActive(receiverId: String,roomId: String){
        isActiveJob?.cancel()
        isActiveJob = viewModelScope.launch {
            chatRepository.getIsUserActive(receiverId,roomId).collect{
                _isActive.value = it
            }
        }
    }

    fun updateIsUserTyping(isActive:Boolean,roomId: String) = chatRepository.updateIsUserTyping(isActive,roomId)

    fun getUserIsTyping(participantId: String,roomId: String){
        isTypingJob?.cancel()
        isTypingJob = viewModelScope.launch {
            chatRepository.getIsUserTyping(participantId,roomId).collect{
                _isUserTyping.value = it
            }
        }
    }

    fun getChats() {
        viewModelScope.launch {
            chatRepository.getChats().collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _userChats.value = UiState.Loading
                    }
                    is ResultState.Success -> {
                        _userChats.value = UiState.Success(result.data)
                    }
                    is ResultState.Error -> {
                        _userChats.value = UiState.Error(result.message)
                    }
                }
            }
        }
    }


    fun updateChatRoomData(chatMessage: ChatMessage) {
        _chats.value = _chats.value.toMutableList().apply {
            add(chatMessage)
        }
    }

    fun deleteChatRoomData(chatId: String, roomId: String) {
        viewModelScope.launch {
            chatRepository.deleteChat(chatId, roomId)
                .onSuccess {
                    Log.d("ChatImpl", "deleteChatRoomData: $it")
                }
                .onFailure {
                    Log.d("ChatImpl", "deleteChatRoomData: ${it.message}")
                }
        }
    }

    suspend fun markMessagesAsReed(participantId: String, roomId: String) {
        chatRepository.markMessagesAsReed(participantId, roomId)
    }

    fun fetchRoomID(userId: String) {
        viewModelScope.launch {
            chatRepository.fetchChatRoomId(userId)
                .onSuccess { roomId ->
                    _hasMessage.value = roomId
                }
                .onFailure { error ->
                    Log.e("ChatsViewModel", "fetchRoomID failed", error)
                }
        }
    }

    fun resetChatState() {
        _hasMessage.value = ""
        _chats.value = emptyList()
    }
}



