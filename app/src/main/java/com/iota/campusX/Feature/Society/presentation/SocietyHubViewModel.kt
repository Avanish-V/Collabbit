package com.iota.campusX.Feature.Society.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Society.domain.model.Community
import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository
import com.iota.campusX.Feature.Society.domain.usecase.CreateCommunityUseCase
import com.iota.campusX.Feature.Society.domain.usecase.GetCommunitiesUseCase
import com.iota.campusX.Feature.Society.domain.usecase.JoinCommunityUseCase
import com.iota.campusX.Feature.Society.domain.usecase.LeaveCommunityUseCase
import com.iota.campusX.Feature.Society.domain.usecase.GetCurrentUserIdUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SocietyHubViewModel(
    private val createCommunityUseCase: CreateCommunityUseCase,
    private val getCommunitiesUseCase: GetCommunitiesUseCase,
    private val joinCommunityUseCase: JoinCommunityUseCase,
    private val leaveCommunityUseCase: LeaveCommunityUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val repository: CommunityRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val currentUserId: String? get() = getCurrentUserIdUseCase()

    private val _createState = MutableStateFlow<SocietyUiState>(SocietyUiState.Idle)
    val createState: StateFlow<SocietyUiState> = _createState.asStateFlow()

    private val _joinState = MutableStateFlow<SocietyUiState>(SocietyUiState.Idle)
    val joinState: StateFlow<SocietyUiState> = _joinState.asStateFlow()

    private val _joinEvent = MutableSharedFlow<SocietyUiEvent>()
    val joinEvent = _joinEvent.asSharedFlow()

    val allCommunities = getCommunitiesUseCase.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val joinedCommunities: StateFlow<List<Community>> = repository.getJoinedCommunities(currentUserId ?: "")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isMembershipResolved = MutableStateFlow(false)
    val isMembershipResolved = _isMembershipResolved.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getJoinedCommunities(currentUserId ?: "").collect {
                _isMembershipResolved.value = true
            }
        }
        refreshCommunities()
    }

    fun refreshCommunities() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.syncCommunities(userId)
            _isRefreshing.value = false
        }
    }

    fun createCommunity(name: String, description: String, category: String, logoUri: String?) {
        val userId = currentUserId ?: return
        
        viewModelScope.launch {
            _createState.value = SocietyUiState.Loading
            val community = Community(
                name = name,
                description = description,
                category = category,
                creatorId = userId
            )
            val result = createCommunityUseCase(community, logoUri)
            if (result.isSuccess) {
                _createState.value = SocietyUiState.Success
                refreshCommunities()
            } else {
                _createState.value = SocietyUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun joinCommunity(community: Community) {
        val userId = currentUserId ?: run {
            Log.e("SOCIETY_DEBUG", "joinCommunity failed: currentUserId is null")
            return
        }
        
        viewModelScope.launch {
            repository.joinCommunity(community, userId)
        }

        viewModelScope.launch {
            Log.d("SOCIETY_DEBUG", "Joining community: ${community.name} (ID: ${community.id}) for user: $userId")
            _joinState.value = SocietyUiState.Loading
            
            val result = joinCommunityUseCase(community, userId)
            
            if (result.isSuccess) {
                Log.d("SOCIETY_DEBUG", "Successfully joined community locally & remotely")
                _joinState.value = SocietyUiState.Success
                _joinEvent.emit(SocietyUiEvent.JoinSuccess)
                refreshCommunities()
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to join society"
                Log.e("SOCIETY_DEBUG", "Failed to join community: $error")
                _joinState.value = SocietyUiState.Error(error)
                _joinEvent.emit(SocietyUiEvent.Error(error))
                
                leaveCommunityUseCase(community.id, userId)
                refreshCommunities()
            }
        }
    }
}
