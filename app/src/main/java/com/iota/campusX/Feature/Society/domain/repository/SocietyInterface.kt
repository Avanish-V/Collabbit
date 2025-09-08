package com.iota.campusX.Feature.Society.domain.repository

import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Society.domain.models.CreateSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetChatMessage
import com.iota.campusX.Feature.Society.domain.models.GetSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetJoinRequestDTO
import com.iota.campusX.Feature.Society.domain.models.SetChatMessage
import com.iota.campusX.Feature.Society.domain.models.Status
import kotlinx.coroutines.flow.Flow

interface SocietyInterface {

    suspend fun createSociety(createSocietyDTO: CreateSocietyDTO): Result<Unit>

    suspend fun fetchSocieties(feedMode: FeedMode,campusId: String?): Result<List<GetSocietyDTO>>

    suspend fun fetchUserSocieties(userId: String): Result<List<GetSocietyDTO>>

    suspend fun updateRoom( roomId: String,isActive:Boolean,feedMode: FeedMode,campusId: String?) : Result<Unit>

    suspend fun requestToJoin(roomId: String, role:String, status: Status, feedMode: FeedMode, campusId: String?) : Result<Int>

    suspend fun deleteJoinRequest( roomId: String,feedMode: FeedMode,campusId: String?) : Result<Unit>

    suspend fun clearAudioRoom(roomId: String,feedMode: FeedMode,campusId: String?) : Result<Unit>

    suspend fun listenForApproval(roomId: String, feedMode: FeedMode, campusId: String?) : Flow<Result<List<GetJoinRequestDTO>>>

    suspend fun stageUpParticipant(roomId: String, status: Status,requestId:String,feedMode: FeedMode, campusId: String?): Result<String>

    suspend fun isMicrophoneEnabled(roomId: String, isMicrophone: Boolean,requestId:String,feedMode: FeedMode, campusId: String?): Result<Unit>

    suspend fun isSpeaking(roomId: String, isSpeaking: Boolean,requestId:String,feedMode: FeedMode, campusId: String?): Result<Unit>

    suspend fun askToSpeak(roomId: String, isRaiseHand: Boolean,requestId:String,feedMode: FeedMode, campusId: String?): Result<Unit>

    suspend fun deleteRoom(roomId: String): Result<Unit>

    suspend fun audioRoomStatus(isActive: Boolean,roomId: String): Result<Unit>


    suspend fun sendMessage(roomId: String, message: SetChatMessage): Result<Unit>

    suspend fun listenForMessages(roomId: String): Flow<Result<List<GetChatMessage>>>

    suspend fun deleteMessageRoom(roomId: String): Result<Unit>

}

