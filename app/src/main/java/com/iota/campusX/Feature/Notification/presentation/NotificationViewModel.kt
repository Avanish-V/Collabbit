package com.iota.campusX.Feature.Notification.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Notification.domain.NotificationDTO
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Post.domain.GetRepliesDTO
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NotificationViewModel(private val notificationRepository: NotificationRepository): ViewModel() {

    private val _notification: MutableStateFlow<NotificationResultState> = MutableStateFlow(NotificationResultState())
    val notification : StateFlow<NotificationResultState> = _notification.asStateFlow()


    fun fetchNotifications() {
        viewModelScope.launch {
            combine(
                notificationRepository.fetchNotification(),
                notificationRepository.fetchLinkUpRequest()
            ) { notificationsResult, linkUpRequestsResult ->
                Pair(notificationsResult, linkUpRequestsResult)
            }.collect { (notificationsResult, linkUpRequestsResult) ->

                when {
                    notificationsResult is ResultState.Loading || linkUpRequestsResult is ResultState.Loading -> {
                        _notification.value = NotificationResultState(isLoading = true)
                    }
                    notificationsResult is ResultState.Error -> {
                        _notification.value = NotificationResultState(error = notificationsResult.message)
                    }
                    linkUpRequestsResult is ResultState.Error -> {
                        _notification.value = NotificationResultState(error = linkUpRequestsResult.message)
                    }
                    notificationsResult is ResultState.Success && linkUpRequestsResult is ResultState.Success -> {
                        // Here you can merge both data
                        val combinedData = notificationsResult.data + linkUpRequestsResult.data // assuming it's List or similar
                        _notification.value = NotificationResultState(data = combinedData)
                    }
                }
            }
        }
    }

    fun deleteNotificationFromList(notificationDTO: NotificationDTO) {
        _notification.value = NotificationResultState(data = _notification.value.data - notificationDTO)
    }

}

data class NotificationResultState(
    val isLoading: Boolean = false,
    val data: List<NotificationDTO> = emptyList(),
    val error: String = ""
)