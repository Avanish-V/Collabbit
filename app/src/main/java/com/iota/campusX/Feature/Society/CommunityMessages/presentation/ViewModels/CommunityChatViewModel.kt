// feature-chat/ui/chat/CommunityChatViewModel.kt
package com.iota.campusX.Feature.Society.CommunityMessages.presentation.ViewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Society.CommunityMessages.data.model.MessageDto
import com.iota.campusX.Feature.Society.CommunityMessages.domain.UseCase.ObserveEnrichedMessagesUseCase
import com.iota.campusX.Feature.Society.CommunityMessages.domain.UseCase.SendMessageUseCase
import com.iota.campusX.Feature.Society.CommunityMessages.domain.models.Message
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.domain.useCases.GetProfileUseCase
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import kotlin.collections.emptyList

class CommunityChatViewModel(
    private val sendMessage: SendMessageUseCase,
    private val getUserProfileUseCase: GetProfileUseCase,
    private val observeMessages: ObserveEnrichedMessagesUseCase
) : ViewModel() {


    private val _messages: MutableStateFlow<UiState<List<Message>>> = MutableStateFlow(UiState.Loading)
    val messages: StateFlow<UiState<List<Message>>> = _messages.asStateFlow()

    private val _user = MutableStateFlow<BaseProfileDTO?>(null)
    val user = _user.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()


    var text by mutableStateOf("")
        private set

    var replyingTo by mutableStateOf<Message?>(null)
        private set

    fun onTextChanged(new: String) {
        text = new
    }

    fun onMessagePress(message: Message?) {
        replyingTo = message
    }

    fun cancelReply() {
        replyingTo = null
    }

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            getUserProfileUseCase()
                .fold(
                    onSuccess = { profile ->
                        _user.value = profile
                    },
                    onFailure = { e ->
                        _error.value = e.message
                    }
                )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onSend() {

        if (user.value == null) {
            _error.value = "User not found"
            return
        }

        val messageId = UUID.randomUUID().toString()

        val optimisticMessage =
            Message(
                id = messageId,
                text = text,
                replyToMessageId = replyingTo?.id,
                userName = user.value!!.name,
                senderId = user.value!!.uid,
                avatarUrl = user.value!!.image,
                timestamp = LocalDateTime.now(),
                bgColor = ""
            )

        val message =
            MessageDto(
                messageId = messageId,
                text = text,
                replyTo = replyingTo?.id,
            )


        _messages.update {
            UiState.Success((it as UiState.Success<List<Message>>).data + optimisticMessage) as UiState<List<Message>>

        }


        viewModelScope.launch {
            val result = sendMessage(
                groupId = "123456",
                message = message
            )
            result.fold(
                onSuccess = {
                    _messages.update {
                        UiState.Success((it as UiState.Success<List<Message>>).data.filter { it.id != messageId })
                    }
                },
                onFailure = {
                    _error.value = it.message
                    _messages.update {
                        UiState.Success((it as UiState.Success<List<Message>>).data.filter { it.id != messageId })
                    }

                }
            )
            text = ""
            replyingTo = null
        }
    }

    fun observerMessage(groupId: String){

        _messages.value = UiState.Loading

        viewModelScope.launch {

            observeMessages(groupId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = Result.success(emptyList())
                )
                .collect{
                    it.fold(
                        onSuccess = {
                            _messages.value = UiState.Success(it)
                        },
                        onFailure = {
                            _error.value = it.message
                        }
                    )
                }

        }

    }
}
