package com.iota.campusX.Feature.Society.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.Society.domain.model.Community
import com.iota.campusX.Feature.Society.domain.model.SocietyMessage
import com.iota.campusX.Feature.Society.domain.model.MessageType
import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository
import com.iota.campusX.Feature.Society.domain.usecase.UpdateCommunityUseCase
import com.iota.campusX.Feature.Society.domain.usecase.DeleteCommunityUseCase
import com.iota.campusX.Feature.Society.domain.usecase.GetCurrentUserIdUseCase
import com.iota.campusX.Feature.Society.domain.usecase.SendSocietyMessageUseCase
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class SocietyInfoViewModel(
    private val updateCommunityUseCase: UpdateCommunityUseCase,
    private val deleteCommunityUseCase: DeleteCommunityUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val sendSocietyMessageUseCase: SendSocietyMessageUseCase,
    private val repository: CommunityRepository,
    private val userProfileRepository: UserProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val currentUserId: String? get() = getCurrentUserIdUseCase()

    private val currentUserProfile: StateFlow<ProfileResponse?> = userProfileRepository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _createState = MutableStateFlow<SocietyUiState>(SocietyUiState.Idle)
    val createState: StateFlow<SocietyUiState> = _createState.asStateFlow()

    private val _joinState = MutableStateFlow<SocietyUiState>(SocietyUiState.Idle)
    val joinState: StateFlow<SocietyUiState> = _joinState.asStateFlow()

    private val _joinEvent = MutableSharedFlow<SocietyUiEvent>()
    val joinEvent = _joinEvent.asSharedFlow()

    val allCommunities = repository.getAllCommunities()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val joinedCommunities: StateFlow<List<Community>> = repository.getJoinedCommunities(currentUserId ?: "")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun listenToCommunity(societyId: String): Flow<Community?> {
        return repository.listenToCommunity(societyId)
    }

    fun joinCommunity(community: Community) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _joinState.value = SocietyUiState.Loading
            val result = repository.joinCommunity(community, userId)
            if (result.isSuccess) {
                _joinState.value = SocietyUiState.Success
                _joinEvent.emit(SocietyUiEvent.JoinSuccess)
                repository.syncCommunities(userId)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to join society"
                _joinState.value = SocietyUiState.Error(error)
                _joinEvent.emit(SocietyUiEvent.Error(error))
            }
        }
    }

    fun leaveCommunity(community: Community) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.leaveCommunity(community.id, userId)
            repository.syncCommunities(userId)
        }
    }

    fun refreshCommunities() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.syncCommunities(userId)
        }
    }

    fun updateCommunity(
        community: Community,
        newName: String,
        newDescription: String,
        newLogoUri: String?
    ) {
        val userId = currentUserId ?: return
        if (community.creatorId != userId) return

        viewModelScope.launch {
            _createState.value = SocietyUiState.Loading
            val nameChanged = community.name != newName
            val updatedCommunity = community.copy(
                name = newName,
                description = newDescription
            )
            
            val result = updateCommunityUseCase(updatedCommunity, newLogoUri)
            
            if (result.isSuccess) {
                if (nameChanged) {
                    sendSocietyMessage(
                        societyId = community.id,
                        text = "📢 Society name has been changed to '$newName'"
                    )
                }
                _createState.value = SocietyUiState.Success
                refreshCommunities()
            } else {
                _createState.value = SocietyUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun deleteCommunity(community: Community) {
        val userId = currentUserId ?: return
        if (community.creatorId != userId) return
        
        viewModelScope.launch {
            deleteCommunityUseCase(community.id)
            refreshCommunities()
        }
    }

    private fun sendSocietyMessage(societyId: String, text: String) {
        val userId = currentUserId ?: return
        val profile = currentUserProfile.value
        val userName = profile?.baseProfile?.name ?: auth.currentUser?.displayName ?: "User"
        val userAvatar = profile?.baseProfile?.image ?: auth.currentUser?.photoUrl?.toString()

        val message = SocietyMessage(
            id = UUID.randomUUID().toString(),
            societyId = societyId,
            senderId = userId,
            senderName = userName,
            senderAvatarUrl = userAvatar,
            text = text,
            type = MessageType.TEXT
        )

        viewModelScope.launch {
            sendSocietyMessageUseCase(message, null)
        }
    }
}
