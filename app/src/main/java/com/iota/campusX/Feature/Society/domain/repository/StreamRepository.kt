package com.iota.campusX.Feature.Society.domain.repository

import android.content.Context
import com.iota.campusX.Feature.Society.domain.models.RtcConnectionStatus
import com.iota.campusX.Feature.Society.domain.models.State
import io.getstream.video.android.core.Call
import io.getstream.video.android.model.User
import kotlinx.coroutines.flow.Flow

interface StreamRepository {

    fun initialize(baseContext:Context): Flow<State>

    fun joinChannel(channelId: String,token:String,uid: Int,role: String)

    fun muteAudio(muted: Boolean)

    fun enableAudio()

    fun disableAudio()

    fun muteLocalAudioStream(muted: Boolean)

    fun muteAllRemoteAudioStream(muted: Boolean)

    fun muteRemoteUserAudio(uid: Int,muted: Boolean)

    fun leaveChannel()

    suspend fun startCall(user:User,context: android.content.Context): Result<Call>


}