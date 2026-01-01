package com.iota.campusX.Feature.Society.CommunityMessages.presentation.ViewModels

import androidx.lifecycle.ViewModel
import com.iota.campusX.Feature.Society.CommunityMessages.domain.models.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MessageUiState(
    val selectedMessage: Message? = null,
    val isMenuVisible: Boolean = false
)


