package com.iota.campusX.Feature.Society.presentation.ViewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Society.domain.models.CreateSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.JoinRequests
import com.iota.campusX.Feature.Society.domain.repository.SocietyRepository
import com.iota.campusX.Utils.UiState
import io.getstream.video.android.core.Call
import io.getstream.video.android.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SocietyViewModel(private val societyRepository: SocietyRepository): ViewModel() {

    private val _createSocietyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val createSocietyState : StateFlow<UiState<Unit>> = _createSocietyState.asStateFlow()

    private val _getSocietyState = MutableStateFlow<UiState<List<GetSocietyDTO>>>(UiState.Idle)
    val getSocietyState : StateFlow<UiState<List<GetSocietyDTO>>> = _getSocietyState.asStateFlow()

    private val _startRoomState = MutableStateFlow<UiState<Call>>(UiState.Idle)
    val startRoomState : StateFlow<UiState<Call>> = _startRoomState.asStateFlow()

    private val _joinRequests = MutableStateFlow<List<JoinRequests>>(emptyList())
    val joinRequests: StateFlow<List<JoinRequests>> = _joinRequests


    private val _joinRequestState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val joinRequestState : StateFlow<UiState<Unit>> = _joinRequestState.asStateFlow()

    private val _stageUpState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val stageUpState : StateFlow<UiState<String>> = _stageUpState.asStateFlow()

    fun createSociety(societyName: String, description: String,mode: FeedMode){

        val createSocietyDTO = CreateSocietyDTO(
            societyName = societyName,
            description = description,
            mode = mode
        )

        _createSocietyState.value = UiState.Loading

        viewModelScope.launch {
            val result = societyRepository.createSociety(createSocietyDTO)
            _createSocietyState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message.toString()) }
            )
        }

    }

    fun fetchSocieties(feedMode: FeedMode,campusId: String?){

        _getSocietyState.value = UiState.Loading

        viewModelScope.launch {

            val result = societyRepository.fetchSocieties(feedMode,campusId)

            _getSocietyState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message.toString()) }
            )

        }
    }

    fun startRoom(user: User,context: Context){
        _startRoomState.value = UiState.Loading
        viewModelScope.launch {
            val result = societyRepository.startCall(user,context)
            _startRoomState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message.toString()) }
            )
        }
    }

    fun updateRoom(roomId: String,isActive:Boolean,feedMode: FeedMode,campusId: String?){
        viewModelScope.launch {
            societyRepository.updateRoom(roomId,isActive,feedMode,campusId)
        }
    }

    fun startListeningJoinRequests(roomId: String, feedMode: FeedMode, campusId: String?) {
        viewModelScope.launch {
            societyRepository.listenForApproval(roomId, feedMode, campusId)
                .collectLatest {
                    _joinRequests.value = it
                }
        }
    }

    fun sendJoinRequest(roomId: String, role:String,status:Boolean,feedMode: FeedMode, campusId: String?) {
        viewModelScope.launch {
            _joinRequestState.value = UiState.Loading
             val result = societyRepository.requestToJoin(roomId,role, status,feedMode, campusId)
            _joinRequestState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message.toString()) }
            )
        }

    }

    fun deleteJoinRequest(roomId: String, feedMode: FeedMode, campusId: String?) {
        viewModelScope.launch {
            societyRepository.deleteJoinRequest(roomId, feedMode, campusId)
        }
    }

    fun stageUp(roomId: String,status: Boolean,requestId:String, feedMode: FeedMode, campusId: String?) {
        viewModelScope.launch {
            _stageUpState.value = UiState.Loading
            val result = societyRepository.stageUpParticipant(roomId, status,requestId,feedMode, campusId)
            _stageUpState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message.toString()) }
            )
        }
    }

    fun isMicrophone(roomId: String,isMicrophone: Boolean,requestId:String, feedMode: FeedMode, campusId: String?) {
        viewModelScope.launch {
            val result = societyRepository.isMicrophoneEnabled(roomId, isMicrophone,requestId,feedMode, campusId)
        }
    }
    fun isSpeaking(roomId: String,isSpeaking: Boolean,requestId:String, feedMode: FeedMode, campusId: String?) {
        viewModelScope.launch {
            val result = societyRepository.isSpeaking(roomId, isSpeaking,requestId,feedMode, campusId)
        }
    }




}
