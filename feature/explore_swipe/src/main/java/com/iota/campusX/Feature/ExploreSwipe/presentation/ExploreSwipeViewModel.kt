package com.iota.campusX.Feature.ExploreSwipe.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.ExploreSwipe.data.model.SwipeCardType
import com.iota.campusX.Feature.ExploreSwipe.domain.model.SwipeItem
import com.iota.campusX.Feature.ExploreSwipe.domain.repository.SwipeMatchRepository
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Feature.UserProfile.utils.ProfileCompletionCalculator
import com.iota.campusX.core.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExploreSwipeViewModel(
    private val repository: SwipeMatchRepository,
    private val userProfileRepository: UserProfileRepository,
    context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("explore_notes_prefs", Context.MODE_PRIVATE)

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

    val maxDailyNotes = 10
    private val _notesSentToday = MutableStateFlow(0)
    val notesSentToday: StateFlow<Int> = _notesSentToday.asStateFlow()

    // Undo stack for rewinding swiped cards
    private val swipedHistory = mutableListOf<SwipeItem>()

    init {
        observeCurrentUser()
        checkAndResetDailyNotes()
        loadCards()
    }

    private fun checkAndResetDailyNotes() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastDate = prefs.getString("last_note_date", "")
        if (lastDate != today) {
            prefs.edit()
                .putString("last_note_date", today)
                .putInt("notes_sent_today", 0)
                .apply()
            _notesSentToday.value = 0
        } else {
            _notesSentToday.value = prefs.getInt("notes_sent_today", 0)
        }
    }

    private fun incrementDailyNotes() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val newCount = _notesSentToday.value + 1
        _notesSentToday.value = newCount
        prefs.edit()
            .putString("last_note_date", today)
            .putInt("notes_sent_today", newCount)
            .apply()
    }

    private fun observeCurrentUser() {
        userProfileRepository.observeProfile()
            .onEach { profile ->
                val completion = ProfileCompletionCalculator.calculate(profile)
                _currentUserScore.value = completion.score
                _isSwipeUnlocked.value = true
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
            repository.swipeAction(item.id, SwipeCardType.PROFILE, "PASS")
        }
    }

    fun onSwipeRight(item: SwipeItem, note: String? = null) {
        if (_currentUserScore.value < 80) {
            _userMessage.value = "First complete your profile (at least 80%) to send notes & connect."
            return
        }

        if (_notesSentToday.value >= maxDailyNotes) {
            _userMessage.value = "Daily limit reached! You can send up to 10 notes per day. Try again tomorrow."
            return
        }

        val currentList = _activeCards.value.toMutableList()
        currentList.remove(item)
        _activeCards.value = currentList
        swipedHistory.add(item)

        incrementDailyNotes()

        val notesLeft = (maxDailyNotes - _notesSentToday.value).coerceAtLeast(0)
        viewModelScope.launch {
            _userMessage.value = "Sent note & request to ${item.profile.baseProfile.name} ($notesLeft notes left today)"
            repository.swipeAction(item.id, SwipeCardType.PROFILE, "CONNECT", note = note)
        }
    }

    fun onSuperLike(item: SwipeItem) {
        if (_currentUserScore.value < 80) {
            _userMessage.value = "First complete your profile (at least 80%) to super-like & connect."
            return
        }

        if (_notesSentToday.value >= maxDailyNotes) {
            _userMessage.value = "Daily limit reached! You can send up to 10 notes per day. Try again tomorrow."
            return
        }

        val currentList = _activeCards.value.toMutableList()
        currentList.remove(item)
        _activeCards.value = currentList
        swipedHistory.add(item)

        incrementDailyNotes()

        val notesLeft = (maxDailyNotes - _notesSentToday.value).coerceAtLeast(0)
        viewModelScope.launch {
            _userMessage.value = "⭐ Super-liked ${item.profile.baseProfile.name}! ($notesLeft notes left today)"
            repository.swipeAction(item.id, SwipeCardType.PROFILE, "SUPER_LIKE")
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
