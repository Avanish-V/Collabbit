package com.iota.campusX.Feature.Society.presentation.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.PushNotification.FcmNotificationSender
import com.iota.campusX.Feature.Society.domain.models.GetJoinRequestDTO
import com.iota.campusX.Feature.Society.domain.models.Status
import com.iota.campusX.Feature.Society.domain.models.GetChatMessage
import com.iota.campusX.Feature.Society.domain.models.SetChatMessage
import com.iota.campusX.Feature.Society.domain.repository.SocietyInterface
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.UiState.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AudioRoomViewModel(
    private val societyRepository: SocietyInterface,
    private val notificationSender: FcmNotificationSender
): ViewModel() {


    private val _joinRequests = MutableStateFlow<UiState<List<GetJoinRequestDTO>>>(Idle)
    val joinRequests: StateFlow<UiState<List<GetJoinRequestDTO>>> = _joinRequests.asStateFlow()

    private val _society = MutableStateFlow<UiState<Unit>>(Idle)
    val society: StateFlow<UiState<Unit>> = _society.asStateFlow()

    private val _chatMessages = MutableStateFlow<UiState<List<GetChatMessage>>>(Idle)
    val chatMessages: StateFlow<UiState<List<GetChatMessage>>> = _chatMessages.asStateFlow()

    private val _sendMessageState : MutableStateFlow<UiState<Unit>> = MutableStateFlow(Idle)
    val sendMessageState : StateFlow<UiState<Unit>> = _sendMessageState.asStateFlow()

    private val _chatsCount : MutableStateFlow<UiState<Int>> = MutableStateFlow(Idle)
    val chatsCount : StateFlow<UiState<Int>> = _chatsCount.asStateFlow()


    private val _userChatsCount : MutableStateFlow<UiState<Int>> = MutableStateFlow(Idle)
    val userChatsCount : StateFlow<UiState<Int>> = _userChatsCount.asStateFlow()


    private val _subscribers : MutableStateFlow<UiState<List<String>>> = MutableStateFlow(Idle)
    val subscribers : StateFlow<UiState<List<String>>> = _subscribers.asStateFlow()

    private val _isRoomActive : MutableStateFlow<UiState<Boolean>> = MutableStateFlow(Idle)
    val isRoomActive : StateFlow<UiState<Boolean>> = _isRoomActive.asStateFlow()


    //--------------------------------------------Audio Room State----------------------------------------------------------------------

    fun sendChatMessage(chatMessage: SetChatMessage, roomId: String){
        viewModelScope.launch {
            _sendMessageState.value = Loading
             val result =   societyRepository.sendMessage(roomId = roomId, message = chatMessage)
            _sendMessageState.value = result.fold(
                onSuccess = {
                    Success(Unit)
                },
                onFailure = {
                    Error(it.message.toString())
                }
            )
            delay(2000)
            _sendMessageState.value = Idle
        }
    }

    fun fetchChatMessages(roomId: String){
        viewModelScope.launch {
            _chatMessages.value = Loading
            val result = societyRepository.listenForMessages(roomId)
             result.collect { chat->
                chat.fold(
                    onSuccess = {chatMessage->
                       _chatMessages.value = Success(chatMessage.sortedByDescending { it.createdAt})
                    },
                    onFailure = {
                        _chatMessages.value = Error(it.message.toString())
                    }
                )
            }
        }
    }

    fun listenChatCount(roomId: String){
        viewModelScope.launch {
            _chatsCount.value = Loading

            val result = societyRepository.getChatsCount(roomId)
            result.collect {
                _chatsCount.value = it.fold(
                    onSuccess = {
                        Success(it)
                    },
                    onFailure = {
                        Error(it.message.toString())
                    }
                )
            }
        }
    }

    fun updateChatsCount(roomId: String,chatCount: Int){
        viewModelScope.launch {
            societyRepository.updateUserChatsCount(roomId, chatCount)
        }
    }

    fun getUserChatsCount(roomId: String,currentChatCount: Int){
        viewModelScope.launch {
            _userChatsCount.value = Loading
            val result = societyRepository.getUserChatCount(roomId,currentChatCount)
            _userChatsCount.value = result.fold(
                onSuccess = {
                    Success(it)
                },
                onFailure = {
                    Error(it.message.toString())
                }
            )
        }

    }

    fun roomState(audioRoomState: AudioRoomState) = viewModelScope.launch {

        when(audioRoomState){

            is AudioRoomState.SetRoomActive -> {
                val result = societyRepository.audioRoomStatus(roomId = audioRoomState.roomId, isActive = audioRoomState.isActive)
                _society.value = result.fold(
                    onSuccess = {
                        Success(Unit)
                    },
                    onFailure = {
                        Error(it.message.toString())
                    }
                )
            }

            is AudioRoomState.StartListening -> {

                _joinRequests.value = Loading

               val result =  societyRepository.listenForApproval(roomId = audioRoomState.roomId, feedMode = FeedMode.CAMPUS, campusId = null)

                result.collectLatest {
                    _joinRequests.value = it.fold(
                        onSuccess = {
                            Success(it)
                        },
                        onFailure = {
                            Error(it.message.toString())
                        }
                    )
                }


            }

            is AudioRoomState.SendJoinRequest -> {
               val result =  societyRepository.requestToJoin(audioRoomState.roomId, audioRoomState.role, audioRoomState.status, audioRoomState.feedMode, audioRoomState.campusId)
                _society.value = result.fold(
                    onSuccess = {
                        Success(Unit)
                    },
                    onFailure = {
                        Error(it.message.toString())
                    }
                )
            }

            is AudioRoomState.StageUp -> {
               val result =  societyRepository.stageUpParticipant(roomId = audioRoomState.roomId, status =  Status.STAGE_UP, requestId = audioRoomState.requestId, feedMode = audioRoomState.feedMode, campusId = audioRoomState.campusId)
                _society.value = result.fold(
                    onSuccess = {
                        Success(Unit)
                    },
                    onFailure = {
                        Error(it.message.toString())
                    }
                )
            }

            is AudioRoomState.StageDown -> {
               val result = societyRepository.stageUpParticipant(audioRoomState.roomId, Status.STAGE_DOWN, audioRoomState.requestId, audioRoomState.feedMode, audioRoomState.campusId)
                _society.value = result.fold(
                    onSuccess = {
                        Success(Unit)
                    },
                    onFailure = {
                        Error(it.message.toString())
                    }
                )
            }

            is AudioRoomState.deleteJoinRequest ->{
                val result = societyRepository.deleteJoinRequest(audioRoomState.roomId, FeedMode.CAMPUS, audioRoomState.campusId)
            }

            is AudioRoomState.ClearAudioRoom -> {
                val result = societyRepository.clearAudioRoom(audioRoomState.roomId, feedMode = FeedMode.CAMPUS, campusId = null)
            }

            is AudioRoomState.DeleteMessageRoom -> {
                val result = societyRepository.deleteMessageRoom(audioRoomState.roomId)
            }

            is AudioRoomState.GetSubscribers -> {

                val result = societyRepository.subscribers(audioRoomState.roomId)
                _subscribers.value = result.fold(
                    onSuccess = {
                        Success(it)
                    },
                    onFailure = {
                         Error(it.message.toString())
                    }
                )
            }

            is AudioRoomState.SendNotificationTOSubscriber -> {

                if (audioRoomState.subscriber.isNotEmpty()){

                    audioRoomState.subscriber.forEach { subscriber->
                        notificationSender.sendFcmNotification(
                            userFcmToken = subscriber,
                            bodyText = audioRoomState.message,
                            title = audioRoomState.roomTitle
                        )
                    }
                }
            }

            is AudioRoomState.IsRoomActive -> {
                val result = societyRepository.isRoomActive(audioRoomState.roomId)
                result.fold(
                    onSuccess = {
                        _isRoomActive.value = Success(it)
                    },
                    onFailure = {
                        _isRoomActive.value = Error(it.message.toString())
                    }
                )
            }
        }

    }

    fun uiControls(uiControls: UiControls) = viewModelScope.launch {

        when(uiControls){

            is UiControls.MuteMicrophone -> {
               val result =  societyRepository.isMicrophoneEnabled(uiControls.roomId, uiControls.muted, "", uiControls.feedMode, uiControls.campusId)
            }

            is UiControls.AskToSpeak -> {
                val result = societyRepository.askToSpeak(uiControls.roomId, uiControls.isRaiseHand, "",uiControls.feedMode, uiControls.campusId)
            }

            is UiControls.IsSpeaking -> {
                val result = societyRepository.isSpeaking(uiControls.roomId, uiControls.isSpeaking, "", uiControls.feedMode, uiControls.campusId)
            }

        }
    }

}

sealed class AudioRoomState {

    data class SetRoomActive(val roomId: String, val isActive: Boolean) : AudioRoomState()

    data class IsRoomActive(val roomId: String) : AudioRoomState()

    data class StartListening(val roomId: String) : AudioRoomState()

    data class SendJoinRequest(val roomId: String, val role:String, val status: Status, val feedMode: FeedMode, val campusId: String?) : AudioRoomState()

    data class StageUp(val roomId: String, val requestId: String, val feedMode: FeedMode, val campusId: String?) : AudioRoomState()

    data class StageDown(val roomId: String, val requestId: String, val feedMode: FeedMode, val campusId: String?) : AudioRoomState()

    data class deleteJoinRequest(val roomId: String, val feedMode: FeedMode, val campusId: String?) : AudioRoomState()

    data class ClearAudioRoom(val roomId: String) : AudioRoomState()

    data class DeleteMessageRoom(val roomId: String) : AudioRoomState()

    data class GetSubscribers(val roomId: String) : AudioRoomState()

    data class SendNotificationTOSubscriber(val subscriber: List<String>,val roomTitle: String, val message: String) : AudioRoomState()

}

sealed class UiControls{

    data class MuteMicrophone(val roomId: String, val muted: Boolean, val feedMode: FeedMode, val campusId: String?) : UiControls()

    data class AskToSpeak(val roomId: String, val isRaiseHand: Boolean,val feedMode: FeedMode, val campusId: String?) : UiControls()

    data class IsSpeaking(val roomId: String, val isSpeaking: Boolean, val feedMode: FeedMode, val campusId: String?) : UiControls()

}