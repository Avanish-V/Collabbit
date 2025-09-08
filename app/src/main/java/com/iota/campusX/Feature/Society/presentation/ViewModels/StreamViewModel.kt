package com.iota.campusX.Feature.Society.presentation.ViewModels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Society.domain.models.State
import com.iota.campusX.Feature.Society.domain.repository.SocietyInterface
import com.iota.campusX.Feature.Society.domain.repository.StreamRepository
import com.iota.campusX.feature.society.backgroundservice.AudioForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StreamViewModel(
    private val  streamRepository: StreamRepository,
    private val societyRepository: SocietyInterface,
    private val appContext: Context
): ViewModel() {

    private val _audioRoomState = MutableStateFlow<State>(State.Idle)
    val audioRoomState : StateFlow<State> = _audioRoomState.asStateFlow()


    init {



    }
    fun initializeAgora(context: Context){
        viewModelScope.launch {
            streamRepository.initialize(context).collect {
                _audioRoomState.value = it
               // Log.d("CHANNEL_LEAVE","$it")
                when(val data = it){
                    is State.Channel_joined -> {
                        Log.d("CHANNEL_LEAVE","Joined")
                    }
                    is State.ChannelLeave -> {
                        Log.d("CHANNEL_LEAVE","Leave ${data.roomId}")
                        viewModelScope.launch {
                            societyRepository.deleteJoinRequest(
                                roomId = data.roomId,
                                feedMode = FeedMode.CAMPUS,
                                campusId = null
                            )
                            stopForegroundKeepAlive()
                        }
                    }
                    else -> {}
                }
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
     fun leaveChannel(roomId: String){
        viewModelScope.launch {
            streamRepository.leaveChannel()
        }

    }

    fun enableLoudSpeaker(isLoud: Boolean){
        streamRepository.enableLoudSpeaker(isLoud)

    }


    fun startForegroundKeepAlive(title: String, text: String) {
        val intent = Intent(appContext, AudioForegroundService::class.java).apply {
            putExtra(AudioForegroundService.EXTRA_TITLE, title)
            putExtra(AudioForegroundService.EXTRA_TEXT, text)
        }
        ContextCompat.startForegroundService(appContext, intent)
    }

    fun stopForegroundKeepAlive() {
        appContext.stopService(Intent(appContext, AudioForegroundService::class.java))
    }


}