package com.iota.campusX.Feature.Society.presentation.ViewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Society.domain.models.MatchStatus
import com.iota.campusX.Feature.Society.domain.models.RtcConnectionStatus
import com.iota.campusX.Feature.Society.domain.models.State
import com.iota.campusX.Feature.Society.domain.repository.StreamRepository
import com.iota.campusX.Utils.UiState
import io.getstream.video.android.core.Call
import io.getstream.video.android.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StreamViewModel(private val  streamRepository: StreamRepository): ViewModel() {

    private val _startRoomState = MutableStateFlow<UiState<Call>>(UiState.Idle)
    val startRoomState : StateFlow<UiState<Call>> = _startRoomState.asStateFlow()


    private val _audioRoomState = MutableStateFlow<State>(State.Idle)
    val audioRoomState : StateFlow<State> = _audioRoomState.asStateFlow()


    fun startRoom(user: User,context: Context){
        _startRoomState.value = UiState.Loading
        viewModelScope.launch {
            val result = streamRepository.startCall(user,context)
            _startRoomState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message.toString()) }
            )
        }
    }

    fun initializeAgora(context: Context){
        viewModelScope.launch {
            streamRepository.initialize(context).collect {
                _audioRoomState.value = it
            }
        }

    }
    fun joinChannel(channelId: String,token:String,uid: Int,role: String){
        streamRepository.joinChannel(channelId,token,uid,role)
    }

    fun muteAudio(muted: Boolean){
        streamRepository.muteAudio(muted)
    }

    fun disableAudio(){
        streamRepository.disableAudio()
    }

    fun enableAudio(){
        streamRepository.enableAudio()
    }

    fun muteLocalAudioStream(muted: Boolean){
        streamRepository.muteLocalAudioStream(muted)
    }

    fun muteAllRemoteAudioStream(muted: Boolean){
        streamRepository.muteAllRemoteAudioStream(muted)
    }

    fun muteRemoteUserAudio(uid: Int,muted: Boolean){
        streamRepository.muteRemoteUserAudio(uid,muted)
    }
    fun leaveChannel(){
        streamRepository.leaveChannel()
    }

}