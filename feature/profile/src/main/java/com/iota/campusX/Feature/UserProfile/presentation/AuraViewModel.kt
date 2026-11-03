package com.iota.campusX.Feature.UserProfile.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraCheckInResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraInfoResponse
import com.iota.campusX.Feature.UserProfile.domain.usecase.ClaimDailyAuraUseCase
import com.iota.campusX.Feature.UserProfile.domain.usecase.GetAuraInfoUseCase
import com.iota.campusX.Feature.UserProfile.domain.usecase.ObserveAuraTransactionsUseCase
import com.iota.campusX.Feature.UserProfile.domain.useCases.ObserveProfileUseCase
import com.iota.campusX.core.common.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AuraViewModel(
    private val claimDailyAuraUseCase: ClaimDailyAuraUseCase,
    private val getAuraInfoUseCase: GetAuraInfoUseCase,
    private val observeAuraTransactionsUseCase: ObserveAuraTransactionsUseCase,
    private val observeProfile: ObserveProfileUseCase
) : ViewModel() {

    private val TAG = "AuraViewModel"

    // ── State Flows ─────────────────────────────────────────────────────────────


    private val _checkInState = MutableStateFlow<UiState<AuraCheckInResponse>>(UiState.Idle)
    val checkInState = _checkInState.asStateFlow()

    private val _auraInfoState = MutableStateFlow<UiState<AuraInfoResponse>>(UiState.Idle)
    val auraInfoState = _auraInfoState.asStateFlow()

    private val _checkInEvent = MutableSharedFlow<CheckInEvent>()
    val checkInEvent = _checkInEvent.asSharedFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions = observeProfile().flatMapLatest { profile ->
        observeAuraTransactionsUseCase(profile.uid)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var isCheckingIn = false

    // ── Public Methods ──────────────────────────────────────────────────────────

    fun claimDailyCheckIn(showAlreadyClaimedMessage: Boolean = false) {
        if (isCheckingIn) {
            Log.d(TAG, "Already checking in, ignoring duplicate request")
            return
        }

        viewModelScope.launch {
            try {
                isCheckingIn = true
                _checkInState.value = UiState.Loading
                Log.d(TAG, "Claiming daily check-in...")

                // Get current user ID to check local transaction
                val profile = observeProfile().firstOrNull()
                val userId = profile?.uid ?: run {
                    Log.e(TAG, "No user profile found, cannot claim check-in")
                    _checkInState.value = UiState.Error("User not logged in")
                    return@launch
                }

                val result = claimDailyAuraUseCase(userId)

                result.fold(
                    onSuccess = { response ->
                        _checkInState.value = UiState.Success(response)
                        
                        if (response.pointsEarnedToday > 0) {
                            // First check-in of the day - show success dialog
                            Log.i(TAG, "Daily check-in successful: ${response.pointsEarnedToday} points earned")
                            _checkInEvent.emit(
                                CheckInEvent.Success(
                                    pointsEarned = response.pointsEarnedToday,
                                    totalPoints = response.aura.auraPoints,
                                    level = response.aura.level.label
                                )
                            )
                        } else if (showAlreadyClaimedMessage) {
                            // Already claimed today - optionally show message
                            Log.d(TAG, "Daily check-in already claimed today")
                            _checkInEvent.emit(CheckInEvent.AlreadyClaimed)
                        } else {
                            // Already claimed but don't show message (silent check on app launch)
                            Log.d(TAG, "Daily check-in already claimed today (silent)")
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Daily check-in failed: ${error.message}", error)
                        _checkInState.value = UiState.Error(
                            error.message ?: "Failed to claim daily points"
                        )
                        _checkInEvent.emit(
                            CheckInEvent.Error(error.message ?: "Network error")
                        )
                    }
                )
            } finally {
                isCheckingIn = false
            }
        }
    }


    fun fetchAuraInfo() {
        viewModelScope.launch {
            try {
                _auraInfoState.value = UiState.Loading
                Log.d(TAG, "Fetching aura info...")

                val result = getAuraInfoUseCase()

                result.fold(
                    onSuccess = { auraInfo ->
                        _auraInfoState.value = UiState.Success(auraInfo)
                        Log.d(TAG, "Aura info fetched: ${auraInfo.auraPoints} points, level ${auraInfo.level.label}")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to fetch aura info: ${error.message}", error)
                        _auraInfoState.value = UiState.Error(
                            error.message ?: "Failed to load aura info"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching aura info: ${e.message}", e)
                _auraInfoState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }


    fun resetCheckInState() {
        _checkInState.value = UiState.Idle
    }

    fun retryCheckIn() {
        resetCheckInState()
        claimDailyCheckIn(showAlreadyClaimedMessage = true)
    }
}

// ── Events ──────────────────────────────────────────────────────────────────────


sealed class CheckInEvent {

    data class Success(
        val pointsEarned: Int,
        val totalPoints: Int,
        val level: String
    ) : CheckInEvent()

    data object AlreadyClaimed : CheckInEvent()

    data class Error(val message: String) : CheckInEvent()
}
