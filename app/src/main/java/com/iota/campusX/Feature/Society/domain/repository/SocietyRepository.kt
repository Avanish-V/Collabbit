package com.iota.campusX.Feature.Society.domain.repository

import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Society.domain.models.CreateSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetSocietyDTO
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocietyRepository(private val societyInterface: SocietyInterface) {

    private val _createSocietyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val createSocietyState: StateFlow<UiState<Unit>> = _createSocietyState.asStateFlow()

    private val _getSocietyState = MutableStateFlow<UiState<List<GetSocietyDTO>>>(UiState.Idle)
    val getSocietyState: StateFlow<UiState<List<GetSocietyDTO>>> = _getSocietyState.asStateFlow()

    private val _userSocietyState = MutableStateFlow<UiState<List<GetSocietyDTO>>>(UiState.Idle)
    val userSocietyState: StateFlow<UiState<List<GetSocietyDTO>>> = _userSocietyState.asStateFlow()

    private val _deleteSocietyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteSocietyState: StateFlow<UiState<Unit>> = _deleteSocietyState.asStateFlow()


    suspend fun createSociety(createSocietyDTO: CreateSocietyDTO) {

        _createSocietyState.value = UiState.Loading

        val result = societyInterface.createSociety(createSocietyDTO)
        _createSocietyState.value = result.fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.message.toString()) }
        )
        delay(2000)
        _createSocietyState.value = UiState.Idle

    }

    suspend fun deleteSociety(societyId: String): Result<Unit>{

        val result = societyInterface.deleteRoom(societyId)

        return result

    }

    suspend fun fetchSocieties(feedMode: FeedMode, campusId: String?) {

        _getSocietyState.value = UiState.Loading

        val result = societyInterface.fetchSocieties(feedMode, campusId)

        _getSocietyState.value = result.fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.message.toString()) }
        )

    }

    suspend fun fetchUserSocieties(userId: String) {

        _userSocietyState.value = UiState.Loading
        val result = societyInterface.fetchUserSocieties(userId)
        _userSocietyState.value = result.fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.message.toString()) }
        )

    }

    fun removeSocietyLocally(roomId: String) {
        val currentList = (_userSocietyState.value as? UiState.Success)?.data
        val societyList = (_getSocietyState.value as? UiState.Success)?.data
        if (currentList != null) {
            val newList = currentList.filter { it.roomId != roomId }
            _userSocietyState.value = UiState.Success(newList)
        }
        if (societyList != null) {
            val newList = societyList.filter { it.roomId != roomId }
            _getSocietyState.value = UiState.Success(newList)
        }
    }

}