package com.iota.campusX.Feature.Society.data

import android.content.Context
import android.util.Log
import com.iota.campusX.Feature.Society.domain.models.MatchStatus
import com.iota.campusX.Feature.Society.domain.models.RtcConnectionStatus
import com.iota.campusX.Feature.Society.domain.models.State
import com.iota.campusX.Feature.Society.domain.repository.StreamRepository
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class StreamImplementation : StreamRepository {

    private var mRtcEngine: RtcEngine? = null


    override fun initialize(baseContext:Context): Flow<State> = callbackFlow{


        if (mRtcEngine != null) {

        }
        val myAppId = "8be7292d7bb44b46b541bc72316ebc5a"

        val iRtcEngineEventHandler = object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {

                trySend(State.Channel_joined)
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {

                trySend(State.Room_Joined)
            }

            override fun onAudioVolumeIndication(
                speakers: Array<out AudioVolumeInfo?>?,
                totalVolume: Int
            ) {
                super.onAudioVolumeIndication(speakers, totalVolume)
                trySend(State.isSpeaking(totalVolume))
               // Log.d("AgoraRepoImpl", "volume- $totalVolume")
            }

            override fun onUserMuteAudio(uid: Int, muted: Boolean) {
                super.onUserMuteAudio(uid, muted)
                trySend(State.isMicrophone(muted))

            }
            override fun onUserOffline(uid: Int, reason: Int) {

            }

            override fun onLeaveChannel(stats: RtcStats?) {

                trySend(State.ChannelLeave)
            }

            override fun onError(err: Int) {

                trySend(State.error(err.toString()))
            }

            override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
                super.onNetworkQuality(uid, txQuality, rxQuality)
            }


        }


        try {
            val config = RtcEngineConfig().apply {
                mContext = baseContext
                mAppId = myAppId
                mEventHandler = iRtcEngineEventHandler
            }
            mRtcEngine = RtcEngine.create(config)
            mRtcEngine?.enableAudioVolumeIndication(300, 3, true)


        } catch (e: Exception) {
            throw RuntimeException("Error initializing RTC engine: ${e.message}")
        }

        awaitClose {
            close()
        }
    }

    override  fun joinChannel(channelId: String, token: String, uid: Int,role: String) {

        val options = ChannelMediaOptions().apply {
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            publishMicrophoneTrack = true;
        }

        mRtcEngine?.joinChannel(token, channelId, uid, options)
    }

    override fun muteAudio(muted: Boolean) {
        mRtcEngine?.muteLocalAudioStream(muted)
    }

    override fun enableAudio() {
        mRtcEngine?.enableAudio()
    }

    override fun disableAudio() {
        mRtcEngine?.disableAudio()
    }

    override fun muteLocalAudioStream(muted: Boolean) {
        mRtcEngine?.muteLocalAudioStream(muted)
    }

    override fun muteAllRemoteAudioStream(muted: Boolean) {
        mRtcEngine?.muteAllRemoteAudioStreams(muted)
    }

    override fun muteRemoteUserAudio(uid: Int,muted: Boolean) {
        mRtcEngine?.muteRemoteAudioStream(uid,muted)
    }

    override fun leaveChannel() {
        mRtcEngine?.leaveChannel()
        if (mRtcEngine != null){
            mRtcEngine = null
        }
    }

    override fun enableLoudSpeaker(isLoud: Boolean) {
        mRtcEngine?.setEnableSpeakerphone(isLoud)
    }


}
