package com.iota.campusX.Feature.ExploreSwipe.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.ExploreSwipe.data.model.SwipeCardType
import com.iota.campusX.Feature.ExploreSwipe.domain.model.SwipeItem
import com.iota.campusX.Feature.ExploreSwipe.domain.repository.SwipeMatchRepository
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Feature.UserProfile.utils.ProfileCompletionCalculator
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ExploreSwipeViewModel(
    private val repository: SwipeMatchRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _cardsState = MutableStateFlow<UiState<List<SwipeItem>>>(UiState.Idle)
    val cardsState: StateFlow<UiState<List<SwipeItem>>> = _cardsState.asStateFlow()

    private val _activeCards = MutableStateFlow<List<SwipeItem>>(emptyList())
    val activeCards: StateFlow<List<SwipeItem>> = _activeCards.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _currentUserScore = MutableStateFlow(100)
    val currentUserScore: StateFlow<Int> = _currentUserScore.asStateFlow()

    private val _isSwipeUnlocked = MutableStateFlow(true)
    val isSwipeUnlocked: StateFlow<Boolean> = _isSwipeUnlocked.asStateFlow()

    // Undo stack for rewinding swiped cards
    private val swipedHistory = mutableListOf<SwipeItem>()

    init {
        observeCurrentUser()
        loadCards()
    }

    private fun observeCurrentUser() {
        userProfileRepository.observeProfile()
            .onEach { profile ->
                val completion = ProfileCompletionCalculator.calculate(profile)
                _currentUserScore.value = completion.score
                _isSwipeUnlocked.value = completion.score > 60
            }
            .launchIn(viewModelScope)
    }

    fun loadCards() {
        viewModelScope.launch {
            _cardsState.value = UiState.Loading
            repository.getSwipeFeed()
                .onSuccess { items ->
                    _activeCards.value = items
                    _cardsState.value = UiState.Success(items)
                    swipedHistory.clear()
                }
                .onFailure { error ->
                    _cardsState.value = UiState.Error(error.localizedMessage ?: "Failed to load swipe cards")
                }
        }
    }

    fun onSwipeLeft(item: SwipeItem) {
        val currentList = _activeCards.value.toMutableList()
        currentList.remove(item)
        _activeCards.value = currentList
        swipedHistory.add(item)

        viewModelScope.launch {
            val type = when (item) {
                is SwipeItem.CollabItem -> SwipeCardType.COLLABORATION
                is SwipeItem.ProfileItem -> SwipeCardType.PROFILE
            }
            repository.swipeAction(item.id, type, "PASS")
        }
    }

    fun onSwipeRight(item: SwipeItem, note: String? = null) {
        val currentList = _activeCards.value.toMutableList()
        currentList.remove(item)
        _activeCards.value = currentList
        swipedHistory.add(item)

        viewModelScope.launch {
            when (item) {
                is SwipeItem.CollabItem -> {
                    _userMessage.value = "Request sent to join \"${item.collab.title}\""
                    repository.swipeAction(item.id, SwipeCardType.COLLABORATION, "CONNECT")
                }
                is SwipeItem.ProfileItem -> {
                    _userMessage.value = "Sent note & request to ${item.profile.baseProfile.name}"
                    repository.swipeAction(item.id, SwipeCardType.PROFILE, "CONNECT", note = note)
                }
            }
        }
    }

    fun onSuperLike(item: SwipeItem) {
        val currentList = _activeCards.value.toMutableList()
        currentList.remove(item)
        _activeCards.value = currentList
        swipedHistory.add(item)

        viewModelScope.launch {
            when (item) {
                is SwipeItem.CollabItem -> {
                    _userMessage.value = "⭐ Super-liked \"${item.collab.title}\"!"
                    repository.swipeAction(item.id, SwipeCardType.COLLABORATION, "SUPER_LIKE")
                }
                is SwipeItem.ProfileItem -> {
                    _userMessage.value = "⭐ Super-liked ${item.profile.baseProfile.name}!"
                    repository.swipeAction(item.id, SwipeCardType.PROFILE, "SUPER_LIKE")
                }
            }
        }
    }

    fun undoLastSwipe() {
        if (swipedHistory.isNotEmpty()) {
            val lastItem = swipedHistory.removeAt(swipedHistory.size - 1)
            val currentList = _activeCards.value.toMutableList()
            currentList.add(0, lastItem)
            _activeCards.value = currentList
            _userMessage.value = "Card restored"
        }
    }

    fun clearMessage() {
        _userMessage.value = null
    }
}
