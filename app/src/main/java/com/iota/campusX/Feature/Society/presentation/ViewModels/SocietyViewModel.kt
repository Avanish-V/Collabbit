package com.iota.campusX.Feature.Society.presentation.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Society.domain.models.CreateSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetSocietyDTO
import com.iota.campusX.Feature.Society.domain.repository.SocietyRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocietyViewModel(private val societyRepository: SocietyRepository): ViewModel() {

    private val _createSocietyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val createSocietyState : StateFlow<UiState<Unit>> = _createSocietyState.asStateFlow()

    private val _getSocietyState = MutableStateFlow<UiState<List<GetSocietyDTO>>>(UiState.Idle)
    val getSocietyState : StateFlow<UiState<List<GetSocietyDTO>>> = _getSocietyState.asStateFlow()

    private val _userSocietyState = MutableStateFlow<UiState<List<GetSocietyDTO>>>(UiState.Idle)
    val userSocietyState : StateFlow<UiState<List<GetSocietyDTO>>> = _userSocietyState.asStateFlow()

    private val _deleteSocietyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteSocietyState : StateFlow<UiState<Unit>> = _deleteSocietyState.asStateFlow()

    //---------------------------------------------ROOM MANUPULATION-----------------------------------------------------------------------------

    fun createSociety(createSocietyDTO: CreateSocietyDTO){

        _createSocietyState.value = UiState.Loading

        viewModelScope.launch {
            val result = societyRepository.createSociety(createSocietyDTO)
            _createSocietyState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message.toString()) }
            )
        }

    }

    fun deleteSociety(societyId: String){
        viewModelScope.launch {
            val result = societyRepository.deleteRoom(societyId)
            _deleteSocietyState.value = result.fold(
                onSuccess = {
                    removeSocietyLocally(roomId = societyId)
                    UiState.Success(it)
                },
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

    fun fetchUserSocieties(userId: String){
        if (_userSocietyState.value is UiState.Success) return
        viewModelScope.launch {
            _userSocietyState.value = UiState.Loading
            val result = societyRepository.fetchUserSocieties(userId)
            _userSocietyState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message.toString()) }
            )
        }
    }


    //-----------------------------------------UPDATE DATA LOCALLY--------------------------------------------------

    fun removeSocietyLocally(roomId: String){
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
