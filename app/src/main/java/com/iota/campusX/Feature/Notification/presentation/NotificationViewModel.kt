package com.iota.campusX.Feature.Notification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.iota.campusX.Feature.Notification.domain.GetNotification
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationViewModel(private val notificationRepository: NotificationRepository): ViewModel() {

    private val _notification: MutableStateFlow<PagingData<GetNotification>> = MutableStateFlow(PagingData.empty())
    val notification : StateFlow<PagingData<GetNotification>> = _notification.asStateFlow()

    private val _notificationCount: MutableStateFlow<Int> = MutableStateFlow(0)
    val notificationCount: StateFlow<Int> = _notificationCount.asStateFlow()

    private val _chatCount: MutableStateFlow<Int> = MutableStateFlow(0)
    val chatCount: StateFlow<Int> = _chatCount.asStateFlow()

    private val _deleteNotificationState: MutableStateFlow<UiState<Unit>> = MutableStateFlow(UiState.Idle)
    val deleteNotificationState: StateFlow<UiState<Unit>> = _deleteNotificationState.asStateFlow()


    init {
        fetchNotifications()
    }
    fun fetchNotifications() {
        viewModelScope.launch {
            notificationRepository.fetchPagedNotification()
                .cachedIn(viewModelScope)
                .collect {
                    _notification.value = it
                }
        }
    }

    fun deleteNotificationFromList(notificationId: String) {
        _notification.update { pagingData ->
            pagingData.filter { it.notificationId != notificationId }
        }
    }

    fun markNotificationAsRead() {
        viewModelScope.launch {
            notificationRepository.markNotificationAsRead()
        }

    }

    fun getNotificationCount() {
        viewModelScope.launch {
            notificationRepository.getNotificationCount().collectLatest {
                when (it) {
                    is ResultState.Loading -> {
                        _notificationCount.value = 0
                    }
                    is ResultState.Success -> {
                        _notificationCount.value = it.data
                    }
                    is ResultState.Error -> {
                        _notificationCount.value = 0
                    }
                }
            }
        }
    }

    fun getChatCount() {
        viewModelScope.launch {
            notificationRepository.observeTotalUnreadCount().collectLatest {
                _chatCount.value = it
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            _deleteNotificationState.value = UiState.Loading
            val result = notificationRepository.deleteNotification(notificationId)
            _deleteNotificationState.value = result.fold(
                onSuccess = {
                    deleteNotificationFromList(notificationId)
                    UiState.Success(it)
                },
                onFailure = { UiState.Error(it.message.toString()) }
            )
            resetState()
        }
    }

    suspend fun resetState(){
        delay(2000)
        _deleteNotificationState.value = UiState.Idle
    }

}
