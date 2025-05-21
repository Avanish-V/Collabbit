package com.iota.campusX.Feature.Chats.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Chats.data.ChatMessage
import com.iota.campusX.Feature.Chats.data.UserChatsDTO
import com.iota.campusX.Feature.Chats.domain.ChatRepository
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatsViewModel(private val chatRepository: ChatRepository):ViewModel() {


    private val _textMessage:MutableStateFlow<String> = MutableStateFlow("")
    val textMessage:StateFlow<String> = _textMessage.asStateFlow()

    private val _userChats:MutableStateFlow<UserChatsResultState> = MutableStateFlow(UserChatsResultState())
    val userChats:StateFlow<UserChatsResultState> = _userChats.asStateFlow()

    private val _chats: MutableStateFlow<List<ChatMessage>> = MutableStateFlow(emptyList())
    val chats:StateFlow<List<ChatMessage>> = _chats.asStateFlow()

    private val _isActive: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isActive:StateFlow<Boolean> = _isActive.asStateFlow()

    private val _isUserTyping: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isUserTyping:StateFlow<Boolean> = _isUserTyping.asStateFlow()


    fun textMessageInput(inputText: String){
        _textMessage.value = inputText
    }

    fun sendMessages(message: String,messageId:String,receiverId: String) = chatRepository.sendMessage(message,messageId,receiverId)

    fun receiveMessage(participantId: String){
        viewModelScope.launch {
            chatRepository.receiveMessage(participantId = participantId).collect{
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

    fun updateIsUserActive(isActive:Boolean,participantId: String) = chatRepository.updateIsUserActive(isActive,participantId)

    fun getIsActive(receiverId: String){
        viewModelScope.launch {
            chatRepository.getIsUserActive(receiverId).collect{
                _isActive.value = it
            }
        }
    }

    fun updateIsUserTyping(isActive:Boolean,participantId: String) = chatRepository.updateIsUserTyping(isActive,participantId)

    fun getUserIsTyping(participantId: String){
        viewModelScope.launch {
            chatRepository.getIsUserTyping(participantId).collect{
                _isUserTyping.value = it
            }
        }
    }

    fun getChats(){

        viewModelScope.launch {
            chatRepository.getChats().collect{
                when(it) {
                    is ResultState.Loading -> {
                        _userChats.value = UserChatsResultState(isLoading = true)
                    }

                    is ResultState.Success -> {
                        _userChats.value = UserChatsResultState(userChats = it.data)

                    }

                    is ResultState.Error -> {
                        _userChats.value = UserChatsResultState(error = it.message)
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

    fun markMessagesAsReed(participantId: String) = chatRepository.markMessagesAsReed(participantId)

}

data class UserChatsResultState(
    val isLoading:Boolean = false,
    val userChats:List<UserChatsDTO> = emptyList(),
    val error:String = ""
)

