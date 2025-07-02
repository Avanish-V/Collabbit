package com.iota.campusX.Feature.Society.domain.repository

import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Society.domain.models.CreateSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.JoinRequests
import com.iota.campusX.Utils.UiState
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.model.User
import kotlinx.coroutines.flow.Flow

interface SocietyRepository {

    suspend fun createSociety(createSocietyDTO: CreateSocietyDTO): Result<Unit>

    suspend fun fetchSocieties(feedMode: FeedMode,campusId: String?): Result<List<GetSocietyDTO>>

    suspend fun updateRoom( roomId: String,isActive:Boolean,feedMode: FeedMode,campusId: String?) : Result<Unit>

    suspend fun requestToJoin( roomId: String,role:String,status:Boolean,feedMode: FeedMode,campusId: String?) : Result<Unit>

    suspend fun deleteJoinRequest( roomId: String,feedMode: FeedMode,campusId: String?) : Result<Unit>


    suspend fun startCall(user:User,context: android.content.Context): Result<Call>

    suspend fun listenForApproval(roomId: String, feedMode: FeedMode, campusId: String?) : Flow<List<JoinRequests>>

    suspend fun stageUpParticipant(roomId: String, status: Boolean,requestId:String,feedMode: FeedMode, campusId: String?): Result<String>

    suspend fun isMicrophoneEnabled(roomId: String, isMicrophone: Boolean,requestId:String,feedMode: FeedMode, campusId: String?): Result<Unit>

    suspend fun isSpeaking(roomId: String, isSpeaking: Boolean,requestId:String,feedMode: FeedMode, campusId: String?): Result<Unit>



}
