package com.iota.campusX.Feature.Society.presentation.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Society.domain.models.CreateSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetJoinRequestDTO
import com.iota.campusX.Feature.Society.domain.models.Status
import com.iota.campusX.Feature.Society.domain.repository.SocietyRepository
import com.iota.campusX.Utils.UiState
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

    private val _joinRequests = MutableStateFlow<List<GetJoinRequestDTO>>(emptyList())
    val joinRequests: StateFlow<List<GetJoinRequestDTO>> = _joinRequests.asStateFlow()


    private val _joinRequestState = MutableStateFlow<UiState<Int>>(UiState.Idle)
    val joinRequestState : StateFlow<UiState<Int>> = _joinRequestState.asStateFlow()

    private val _stageUpState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val stageUpState : StateFlow<UiState<String>> = _stageUpState.asStateFlow()

    private val _askToSpeak = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val askToSpeak : StateFlow<UiState<Unit>> = _askToSpeak.asStateFlow()

    private val _microphone = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val microphone : StateFlow<UiState<Unit>> = _microphone.asStateFlow()


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

        val isEmpty = (_getSocietyState.value as? UiState.Success)?.data
        if (isEmpty?.isNotEmpty() ?: false) return

        _getSocietyState.value = UiState.Loading

        viewModelScope.launch {

            val result = societyRepository.fetchSocieties(feedMode,campusId)

            _getSocietyState.value = result.fold(
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
                    Log.d("JOIN_REQUESTS", it.toString())
                }
        }
    }

    fun sendJoinRequest(roomId: String, role:String, status: Status, feedMode: FeedMode, campusId: String?) {
        viewModelScope.launch {
            _joinRequestState.value = UiState.Loading
             val result = societyRepository.requestToJoin(roomId,role, status,feedMode, campusId)
            _joinRequestState.value = result.fold(
                onSuccess = { UiState.Success(data = it) },
                onFailure = { UiState.Error(it.message.toString()) }
            )
        }

    }

    fun deleteJoinRequest(roomId: String, feedMode: FeedMode, campusId: String?) {
        viewModelScope.launch {
            societyRepository.deleteJoinRequest(roomId, feedMode, campusId)
        }
    }

    fun stageUp(roomId: String,status: Status,requestId:String, feedMode: FeedMode, campusId: String?) {
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
            _microphone.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message.toString())}
            )
        }
    }
    fun isSpeaking(roomId: String,isSpeaking: Boolean,requestId:String, feedMode: FeedMode, campusId: String?) {
        viewModelScope.launch {
            val result = societyRepository.isSpeaking(roomId, isSpeaking,requestId,feedMode, campusId)
        }
    }

    fun askToSpeak(roomId: String,isRaiseHand: Boolean,requestId:String, feedMode: FeedMode, campusId: String?) {
        viewModelScope.launch {
            _askToSpeak.value = UiState.Loading
            val result = societyRepository.askToSpeak(roomId, isRaiseHand,requestId,feedMode, campusId)
            _askToSpeak.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message.toString())}
            )
        }
    }




}
