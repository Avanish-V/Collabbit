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
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.GEO
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class StreamImplementation : StreamRepository {

    private var mRtcEngine: RtcEngine? = null


    override fun initialize(baseContext:Context): Flow<State> = callbackFlow{

        Log.d("AgoraRepoImpl", "Executed")
        if (mRtcEngine != null) {
            Log.d("AgoraRepoImpl", "RtcEngine already initialized")

        }
        val myAppId = "8be7292d7bb44b46b541bc72316ebc5a"

        val iRtcEngineEventHandler = object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                Log.d("AgoraRepoImpl", "Joined Channel: $channel, UID: $uid")
                trySend(State.Channel_joined)
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                Log.d("AgoraRepoImpl", "User Joined: UID - $uid")
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
                Log.d("AgoraRepoImpl", "Muted - $muted")
            }
            override fun onUserOffline(uid: Int, reason: Int) {
                Log.d("AgoraRepoImpl", "User Offline: UID - $uid")
            }

            override fun onLeaveChannel(stats: RtcStats?) {
                Log.d("AgoraRepoImpl", "Leaved Channel ${stats?.users}")
                trySend(State.ChannelLeave)
            }

            override fun onError(err: Int) {
                Log.e("AgoraRepoImpl", "Agora Error:}")
                trySend(State.error(err.toString()))
            }



        }


        try {
            val config = RtcEngineConfig().apply {
                mContext = baseContext
                mAppId = myAppId
                mEventHandler = iRtcEngineEventHandler
            }
            mRtcEngine = RtcEngine.create(config)
            mRtcEngine?.enableAudioVolumeIndication(200, 3, true)


        } catch (e: Exception) {
            Log.e("AgoraRepoImpl", "Agora Error: ${e.message}}")
            throw RuntimeException("Error initializing RTC engine: ${e.message}")
        }

        if (mRtcEngine == null) {
            Log.e("AgoraRepoImpl", "RtcEngine is not initialized!")
        } else {
            Log.d("AgoraRepoImpl", "RtcEngine created successfully")
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

    override suspend fun startCall(user: User,context: Context): Result<Call> {
        return try {

            StreamVideo.removeClient()

            val apiKey = "mmhfdzb5evj2"
            val userToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL3Byb250by5nZXRzdHJlYW0uaW8iLCJzdWIiOiJ1c2VyL0lHXzg4IiwidXNlcl9pZCI6IklHXzg4IiwidmFsaWRpdHlfaW5fc2Vjb25kcyI6NjA0ODAwLCJpYXQiOjE3NTA3ODI0MDQsImV4cCI6MTc1MTM4NzIwNH0.NmnpBx2LtjI_iuJkhqx_RQ3rFvUGNpxSJ043PwY0m24"
            val callId = "UgGc3W4fgzSR"

            val client = StreamVideoBuilder(
                context = context,
                apiKey = apiKey,
                geo = GEO.GlobalEdgeNetwork,
                user = user,
                token = userToken,
            ).build()

            val call = client.call("audio_room", callId)

            Result.success(call)

        }catch (e: Exception){
            Result.failure(e)
        }
    }


}
