package com.iota.campusX.Feature.Reply

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppUserReplyViewModel(
    private val replyRepository: ReplyRepository
) : ViewModel() {

    val userReplies = replyRepository.userRepliesState
        .map { it }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, UiState.Idle)

    fun getUserReplies(userId: String) {
        if (userReplies.value is UiState.Success) return
        viewModelScope.launch { replyRepository.getUserReplies(userId) }
    }

}