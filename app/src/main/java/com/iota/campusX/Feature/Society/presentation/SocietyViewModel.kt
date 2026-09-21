package com.iota.campusX.Feature.Society.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.Society.domain.model.Community
import com.iota.campusX.Feature.Society.domain.model.SocietyMessage
import com.iota.campusX.Feature.Society.domain.model.MessageType
import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository
import com.iota.campusX.Feature.Society.domain.repository.SocietyUser
import com.iota.campusX.Feature.Society.domain.usecase.CreateCommunityUseCase
import com.iota.campusX.Feature.Society.domain.usecase.GetCommunitiesUseCase
import com.iota.campusX.Feature.Society.domain.usecase.JoinCommunityUseCase
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class SocietyViewModel(
    private val createCommunityUseCase: CreateCommunityUseCase,
    private val getCommunitiesUseCase: GetCommunitiesUseCase,
    private val joinCommunityUseCase: JoinCommunityUseCase,
    private val repository: CommunityRepository,
    private val userProfileRepository: UserProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val currentUserProfile: StateFlow<ProfileResponse?> = userProfileRepository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun refreshCommunities() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.syncCommunities(userId)
            _isRefreshing.value = false
        }
    }

    private val _createState = MutableStateFlow<SocietyUiState>(SocietyUiState.Idle)
    val createState: StateFlow<SocietyUiState> = _createState.asStateFlow()

    private val _joinState = MutableStateFlow<SocietyUiState>(SocietyUiState.Idle)
    val joinState: StateFlow<SocietyUiState> = _joinState.asStateFlow()

    private val _joinEvent = MutableSharedFlow<SocietyUiEvent>()
    val joinEvent = _joinEvent.asSharedFlow()

    val allCommunities = getCommunitiesUseCase.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentUserId: String? get() = auth.currentUser?.uid

    val joinedCommunities: StateFlow<List<Community>> = repository.getJoinedCommunities(auth.currentUser?.uid ?: "")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Used to avoid UI flashing while membership is being resolved from local DB
    private val _isMembershipResolved = MutableStateFlow(false)
    val isMembershipResolved = _isMembershipResolved.asStateFlow()

    init {
        viewModelScope.launch {
            // Wait for the repository flow to emit its first value from the database
            repository.getJoinedCommunities(auth.currentUser?.uid ?: "").collect {
                _isMembershipResolved.value = true
            }
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
            
            val result = repository.updateCommunity(updatedCommunity, newLogoUri)
            
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

    fun joinCommunity(community: Community) {
        val userId = currentUserId ?: run {
            Log.e("SOCIETY_DEBUG", "joinCommunity failed: currentUserId is null")
            return
        }
        
        // Optimistic UI update: Insert into local database immediately
        viewModelScope.launch {
            repository.joinCommunity(community, userId)
        }

        viewModelScope.launch {
            Log.d("SOCIETY_DEBUG", "Joining community: ${community.name} (ID: ${community.id}) for user: $userId")
            _joinState.value = SocietyUiState.Loading
            
            // This triggers the remote join via Firestore
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
                
                // Rollback optimistic update on failure
                repository.leaveCommunity(community.id, userId)
                refreshCommunities()
            }
        }
    }

    fun leaveCommunity(community: Community) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.leaveCommunity(community.id, userId)
            refreshCommunities()
        }
    }

    fun deleteCommunity(community: Community) {
        val userId = currentUserId ?: return
        if (community.creatorId != userId) return
        
        viewModelScope.launch {
            repository.deleteCommunity(community.id)
            refreshCommunities()
        }
    }

    // --- Message Operations ---

    private val _messages = MutableStateFlow<List<SocietyMessage>>(emptyList())
    val messages: StateFlow<List<SocietyMessage>> = _messages.asStateFlow()
    private var messageCollectionJob: kotlinx.coroutines.Job? = null

    fun startListeningToMessages(societyId: String) {
        val userId = currentUserId ?: return
        messageCollectionJob?.cancel()
        messageCollectionJob = repository.listenToSocietyMessages(societyId, userId)
            .onEach { _messages.value = it }
            .launchIn(viewModelScope)
    }

    fun markMessagesAsRead(societyId: String) {
        viewModelScope.launch {
            repository.markMessagesAsRead(societyId)
        }
    }

    fun stopListeningToMessages() {
        messageCollectionJob?.cancel()
        messageCollectionJob = null
    }

    fun updatePresence(societyId: String, isOnline: Boolean) {
        val userId = currentUserId ?: return
        repository.setPresence(societyId, userId, isOnline)
    }

    fun getOnlineCount(societyId: String): StateFlow<Int> {
        return repository.getOnlineCount(societyId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    }

    fun listenToCommunity(societyId: String): Flow<Community?> {
        return repository.listenToCommunity(societyId)
    }

    private val _userCache = MutableStateFlow<Map<String, SocietyUser>>(emptyMap())
    val userCache = _userCache.asStateFlow()

    fun getUser(userId: String): Flow<SocietyUser?> {
        if (userId == currentUserId) {
            return currentUserProfile.map { profile ->
                profile?.let { SocietyUser(it.uid, it.baseProfile.name, it.baseProfile.image) }
            }
        }
        return repository.getCachedUser(userId).onEach { cached ->
            if (cached == null) {
                viewModelScope.launch {
                    repository.resolveUser(userId)
                }
            }
        }
    }

    fun reactToMessage(societyId: String, messageId: String, emoji: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.reactToMessage(societyId, messageId, userId, emoji)
        }
    }

    fun openSnap(societyId: String, messageId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.openSnap(societyId, messageId, userId)
        }
    }

    fun cancelUpload(messageId: String) {
        viewModelScope.launch {
            repository.cancelMessageUpload(messageId)
        }
    }

    fun deleteMessage(societyId: String, messageId: String) {
        viewModelScope.launch {
            repository.deleteSocietyMessage(societyId, messageId)
        }
    }

    fun pinMessage(societyId: String, message: SocietyMessage) {
        viewModelScope.launch {
            val previewText = when (message.type) {
                MessageType.IMAGE -> "📷 Image"
                MessageType.VIDEO -> "🎥 Video"
                MessageType.FILE -> "📄 PDF"
                else -> message.text
            }
            repository.pinMessage(societyId, message.id, previewText)
        }
    }

    fun unpinMessage(societyId: String) {
        viewModelScope.launch {
            repository.unpinMessage(societyId)
        }
    }

    fun sendSocietyMessage(
        societyId: String, 
        text: String, 
        replyingTo: SocietyMessage? = null,
        mediaUri: String? = null,
        type: MessageType = MessageType.TEXT,
        width: Int = 0,
        height: Int = 0,
        aspectRatio: Float = 0f,
        fileName: String? = null,
        thumbnailUrl: String? = null
    ) {
        val userId = currentUserId ?: return
        
        // Use custom profile as source of truth for name/avatar
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
            type = type,
            mediaUrl = mediaUri, // Optimistic: use local URI if available
            thumbnailUrl = thumbnailUrl,
            width = width,
            height = height,
            aspectRatio = aspectRatio,
            replyToId = replyingTo?.id,
            replyToName = replyingTo?.senderName,
            replyToText = replyingTo?.text,
            fileName = fileName
        )

        viewModelScope.launch {
            repository.sendSocietyMessage(message, mediaUri)
        }
    }

    init {
        refreshCommunities()
        // Ensure my own user cache entry is always up to date for chat display
        viewModelScope.launch {
            currentUserProfile.collect { profile ->
                profile?.let {
                    repository.resolveUser(it.uid)
                }
            }
        }
    }
}

sealed class SocietyUiState {
    object Idle : SocietyUiState()
    object Loading : SocietyUiState()
    object Success : SocietyUiState()
    data class Error(val message: String) : SocietyUiState()
}

sealed class SocietyUiEvent {
    object JoinSuccess : SocietyUiEvent()
    data class Error(val message: String) : SocietyUiEvent()
}
