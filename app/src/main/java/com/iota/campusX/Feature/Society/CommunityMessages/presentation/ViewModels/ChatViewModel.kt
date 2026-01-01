package com.iota.campusX.Feature.Society.CommunityMessages.presentation.ViewModels

import androidx.lifecycle.ViewModel
import com.iota.campusX.Feature.Society.CommunityMessages.domain.models.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CommunityChatMenuViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState = _uiState.asStateFlow()

    fun onMessageLongPress(message: Message) {
        _uiState.update {
            it.copy(
                selectedMessage = message,
                isMenuVisible = true
            )
        }
    }

    fun dismissMenu() {
        _uiState.update {
            it.copy(
                selectedMessage = null,
                isMenuVisible = false
            )
        }
    }
}