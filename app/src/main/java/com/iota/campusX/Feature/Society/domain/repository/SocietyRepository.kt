package com.iota.campusX.Feature.Society.domain.repository

import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Society.domain.models.CreateSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetJoinRequestDTO
import com.iota.campusX.Feature.Society.domain.models.Status
import kotlinx.coroutines.flow.Flow

interface SocietyRepository {

    suspend fun createSociety(createSocietyDTO: CreateSocietyDTO): Result<Unit>

    suspend fun fetchSocieties(feedMode: FeedMode,campusId: String?): Result<List<GetSocietyDTO>>

    suspend fun updateRoom( roomId: String,isActive:Boolean,feedMode: FeedMode,campusId: String?) : Result<Unit>

    suspend fun requestToJoin(roomId: String, role:String, status: Status, feedMode: FeedMode, campusId: String?) : Result<Int>

    suspend fun deleteJoinRequest( roomId: String,feedMode: FeedMode,campusId: String?) : Result<Unit>

    suspend fun listenForApproval(roomId: String, feedMode: FeedMode, campusId: String?) : Flow<List<GetJoinRequestDTO>>

    suspend fun stageUpParticipant(roomId: String, status: Status,requestId:String,feedMode: FeedMode, campusId: String?): Result<String>

    suspend fun isMicrophoneEnabled(roomId: String, isMicrophone: Boolean,requestId:String,feedMode: FeedMode, campusId: String?): Result<Unit>

    suspend fun isSpeaking(roomId: String, isSpeaking: Boolean,requestId:String,feedMode: FeedMode, campusId: String?): Result<Unit>

    suspend fun askToSpeak(roomId: String, isRaiseHand: Boolean,requestId:String,feedMode: FeedMode, campusId: String?): Result<Unit>







}
