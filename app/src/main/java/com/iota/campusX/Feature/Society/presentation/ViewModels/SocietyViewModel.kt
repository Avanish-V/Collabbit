package com.iota.campusX.Feature.Society.presentation.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Society.domain.models.CreateSocietyDTO
import com.iota.campusX.Feature.Society.domain.models.GetSocietyDTO
import com.iota.campusX.Feature.Society.domain.repository.SocietyInterface
import com.iota.campusX.Feature.Society.domain.repository.SocietyRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SocietyViewModel(private val societyRepository: SocietyRepository): ViewModel() {


    val createSocietyState  = societyRepository.createSocietyState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = UiState.Idle
    )

    val getSocietyState : StateFlow<UiState<List<GetSocietyDTO>>> = societyRepository.getSocietyState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = UiState.Idle
    )

    val userSocietyState : StateFlow<UiState<List<GetSocietyDTO>>> = societyRepository.userSocietyState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = UiState.Idle
    )

    private val _deleteSocietyState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteSocietyState : StateFlow<UiState<Unit>> = _deleteSocietyState.asStateFlow()

    //---------------------------------------------ROOM MANIPULATION-----------------------------------------------------------------------------

    fun createSociety(createSocietyDTO: CreateSocietyDTO){

        viewModelScope.launch {
            societyRepository.createSociety(createSocietyDTO)
        }


    }

    fun deleteSociety(societyId: String){
        viewModelScope.launch {
            societyRepository.deleteSociety(societyId)
        }
    }

    fun fetchSocieties(feedMode: FeedMode,campusId: String?){

        viewModelScope.launch {
            societyRepository.fetchSocieties(feedMode, campusId)
        }
    }

    fun fetchUserSocieties(userId: String){

        viewModelScope.launch {
            societyRepository.fetchUserSocieties(userId)
        }

    }


    //-----------------------------------------UPDATE DATA LOCALLY--------------------------------------------------




}
