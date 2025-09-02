package com.iota.campusX.Feature.Society.presentation.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Society.domain.models.GetJoinRequestDTO
import com.iota.campusX.Feature.Society.domain.models.Status
import com.iota.campusX.Feature.Society.domain.repository.SocietyRepository
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.UiState.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AudioRoomViewModel(private val societyRepository: SocietyRepository): ViewModel() {


    private val _joinRequests = MutableStateFlow<UiState<List<GetJoinRequestDTO>>>(UiState.Idle)
    val joinRequests: StateFlow<UiState<List<GetJoinRequestDTO>>> = _joinRequests.asStateFlow()



    private val _society = MutableStateFlow<UiState<Unit>>(Idle)
    val society: StateFlow<UiState<Unit>> = _society.asStateFlow()

    //--------------------------------------------Audio Room State----------------------------------------------------------------------

    fun roomState(audioRoomState: AudioRoomState) = viewModelScope.launch {

        when(audioRoomState){

            is AudioRoomState.IsRoomActive -> {
                val result = societyRepository.audioRoomStatus(roomId = audioRoomState.roomId, isActive = audioRoomState.isActive)
            }

            is AudioRoomState.StartListening -> {

                _joinRequests.value = UiState.Loading

               val result =  societyRepository.listenForApproval(roomId = audioRoomState.roomId, feedMode = FeedMode.CAMPUS, campusId = null)

                result.collectLatest {
                    _joinRequests.value = it.fold(
                        onSuccess = {
                            UiState.Success(it)
                        },
                        onFailure = {
                            UiState.Error(it.message.toString())
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

    data class IsRoomActive(val roomId: String,val isActive: Boolean) : AudioRoomState()

    data class StartListening(val roomId: String) : AudioRoomState()

    data class SendJoinRequest(val roomId: String, val role:String, val status: Status, val feedMode: FeedMode, val campusId: String?) : AudioRoomState()

    data class StageUp(val roomId: String, val requestId: String, val feedMode: FeedMode, val campusId: String?) : AudioRoomState()

    data class StageDown(val roomId: String, val requestId: String, val feedMode: FeedMode, val campusId: String?) : AudioRoomState()

    data class deleteJoinRequest(val roomId: String, val feedMode: FeedMode, val campusId: String?) : AudioRoomState()

    data class ClearAudioRoom(val roomId: String) : AudioRoomState()

}

sealed class UiControls{

    data class MuteMicrophone(val roomId: String, val muted: Boolean, val feedMode: FeedMode, val campusId: String?) : UiControls()

    data class AskToSpeak(val roomId: String, val isRaiseHand: Boolean,val feedMode: FeedMode, val campusId: String?) : UiControls()

    data class IsSpeaking(val roomId: String, val isSpeaking: Boolean, val feedMode: FeedMode, val campusId: String?) : UiControls()

}