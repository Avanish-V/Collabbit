package com.iota.campusX.Screens.Home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Society.domain.model.Community
import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: CommunityRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val joinedCommunities: StateFlow<List<Community>> = repository.getJoinedCommunities(auth.currentUser?.uid ?: "")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refresh() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.syncCommunities(userId)
            _isRefreshing.value = false
        }
    }

    fun joinCommunity(community: Community) {
        viewModelScope.launch {
            repository.joinCommunity(community, auth.currentUser?.uid ?: "")
        }
    }
}
