package com.iota.campusX.Feature.Follow.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Follow.domain.FollowRepositoryInterface
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.ConnectionsDTO
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FollowersViewModel(private val followersRepository: FollowRepositoryInterface): ViewModel() {

    private val _followersState : MutableStateFlow<UiState<List<ConnectionsDTO>>> = MutableStateFlow(UiState.Idle)
    val followersState : StateFlow<UiState<List<ConnectionsDTO>>> = _followersState.asStateFlow()

    private val followState : MutableState<UiState<Boolean>> = mutableStateOf(UiState.Idle)


    fun followUser(userId: String) = viewModelScope.launch {

        followState.value = UiState.Loading

        val result = followersRepository.follow(userId)

        result.fold(
            onSuccess = {
                followState.value = UiState.Success(it)
            },
            onFailure = {
                followState.value = UiState.Error(it.message.toString())
            }
        )

    }
    fun unfollowUser(userId: String) = viewModelScope.launch {

        followState.value = UiState.Loading

        val result = followersRepository.unfollow(userId)

        result.fold(
            onSuccess = {
                followState.value = UiState.Success(it)
            },
            onFailure = {
                followState.value = UiState.Error(it.message.toString())
            }
        )

    }

    fun getFollowers(userId: String) = viewModelScope.launch {
        _followersState.value = UiState.Loading
        val result = followersRepository.getFollowers(userId)
        result.fold(
            onSuccess = {
                _followersState.value = UiState.Success(it)
            },
            onFailure = {
                _followersState.value = UiState.Error(it.message.toString())
            }
        )
    }

}