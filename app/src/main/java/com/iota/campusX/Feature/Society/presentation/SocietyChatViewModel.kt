package com.iota.campusX.Feature.Society.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.Society.domain.model.Community
import com.iota.campusX.Feature.Society.domain.model.SocietyMessage
import com.iota.campusX.Feature.Society.domain.model.MessageType
import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository
import com.iota.campusX.Feature.Society.domain.repository.SocietyUser
import com.iota.campusX.Feature.Society.domain.usecase.ListenToMessagesUseCase
import com.iota.campusX.Feature.Society.domain.usecase.SendSocietyMessageUseCase
import com.iota.campusX.Feature.Society.domain.usecase.MessageOperationsUseCase
import com.iota.campusX.Feature.Society.domain.usecase.PresenceUseCase
import com.iota.campusX.Feature.Society.domain.usecase.ResolveUserUseCase
import com.iota.campusX.Feature.Society.domain.usecase.GetCurrentUserIdUseCase
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class SocietyChatViewModel(
    private val listenToMessagesUseCase: ListenToMessagesUseCase,
    private val sendSocietyMessageUseCase: SendSocietyMessageUseCase,
    private val messageOperationsUseCase: MessageOperationsUseCase,
    private val presenceUseCase: PresenceUseCase,
    private val resolveUserUseCase: ResolveUserUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val repository: CommunityRepository,
    private val userProfileRepository: UserProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val currentUserId: String? get() = getCurrentUserIdUseCase()

    private val currentUserProfile: StateFlow<ProfileResponse?> = userProfileRepository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _messages = MutableStateFlow<List<SocietyMessage>>(emptyList())
    val messages: StateFlow<List<SocietyMessage>> = _messages.asStateFlow()
    private var messageCollectionJob: kotlinx.coroutines.Job? = null

    private val _userCache = MutableStateFlow<Map<String, SocietyUser>>(emptyMap())
    val userCache = _userCache.asStateFlow()

    val joinedCommunities: StateFlow<List<Community>> = repository.getJoinedCommunities(currentUserId ?: "")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isMembershipResolved = MutableStateFlow(false)
    val isMembershipResolved = _isMembershipResolved.asStateFlow()

    init {
        // Ensure own user cache entry is always up to date for chat display
        viewModelScope.launch {
            currentUserProfile.collect { profile ->
                profile?.let {
                    resolveUserUseCase.resolveUser(it.uid)
                }
            }
        }
        viewModelScope.launch {
            val userId = currentUserId
            if (userId != null) {
                repository.syncCommunities(userId)
            }
            _isMembershipResolved.value = true
        }
    }

    fun startListeningToMessages(societyId: String) {
        val userId = currentUserId ?: return
        messageCollectionJob?.cancel()
        messageCollectionJob = listenToMessagesUseCase(societyId, userId)
            .onEach { _messages.value = it }
            .launchIn(viewModelScope)
    }

    fun markMessagesAsRead(societyId: String) {
        viewModelScope.launch {
            messageOperationsUseCase.markMessagesAsRead(societyId)
        }
    }

    fun stopListeningToMessages() {
        messageCollectionJob?.cancel()
        messageCollectionJob = null
    }

    fun updatePresence(societyId: String, isOnline: Boolean) {
        val userId = currentUserId ?: return
        presenceUseCase.setPresence(societyId, userId, isOnline)
    }

    fun getOnlineCount(societyId: String): StateFlow<Int> {
        return presenceUseCase.getOnlineCount(societyId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    }

    fun listenToCommunity(societyId: String): Flow<Community?> {
        return repository.listenToCommunity(societyId)
    }

    fun getUser(userId: String): Flow<SocietyUser?> {
        if (userId == currentUserId) {
            return currentUserProfile.map { profile ->
                profile?.let { SocietyUser(it.uid, it.baseProfile.name, it.baseProfile.image) }
            }
        }
        return resolveUserUseCase.getCachedUser(userId).onEach { cached ->
            if (cached == null) {
                viewModelScope.launch {
                    resolveUserUseCase.resolveUser(userId)
                }
            }
        }
    }

    fun reactToMessage(societyId: String, messageId: String, emoji: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            messageOperationsUseCase.reactToMessage(societyId, messageId, userId, emoji)
        }
    }

    fun openSnap(societyId: String, messageId: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            messageOperationsUseCase.openSnap(societyId, messageId, userId)
        }
    }

    fun cancelUpload(messageId: String) {
        viewModelScope.launch {
            messageOperationsUseCase.cancelMessageUpload(messageId)
        }
    }

    fun deleteMessage(societyId: String, messageId: String) {
        viewModelScope.launch {
            messageOperationsUseCase.deleteSocietyMessage(societyId, messageId)
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
            messageOperationsUseCase.pinMessage(societyId, message.id, previewText)
        }
    }

    fun unpinMessage(societyId: String) {
        viewModelScope.launch {
            messageOperationsUseCase.unpinMessage(societyId)
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
            mediaUrl = mediaUri,
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
            sendSocietyMessageUseCase(message, mediaUri)
        }
    }

    fun leaveCommunity(community: Community) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.leaveCommunity(community.id, userId)
        }
    }
}
