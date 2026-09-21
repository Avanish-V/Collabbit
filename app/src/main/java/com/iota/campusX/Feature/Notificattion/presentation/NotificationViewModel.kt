package com.iota.campusX.Feature.Notificattion.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.iota.campusX.Feature.Notificattion.domain.model.Notification
import com.iota.campusX.Feature.Notificattion.domain.repository.NotificationRepository
import com.iota.campusX.Feature.Notificattion.domain.usecase.NotificationUseCases
import com.iota.campusX.Feature.Notificattion.presentation.effect.NotificationEffect
import com.iota.campusX.Feature.Notificattion.presentation.event.NotificationUiEvent
import com.iota.campusX.Feature.Notificattion.presentation.mapper.toUi
import com.iota.campusX.Feature.Notificattion.presentation.states.NotificationUiState
import com.iota.campusX.Utils.extractReason
import com.iotabuild.campuscircle.Notification.entity.EntityType
import com.iotabuild.campuscircle.Notification.entity.NotificationType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationViewModel(

    private val useCases: NotificationUseCases,
    private val repository: NotificationRepository

) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState = _uiState.asStateFlow()

    private val _notificationBadge = MutableStateFlow<Int?>(null)
    val notificationBadge : MutableStateFlow<Int?> = _notificationBadge


    private val _effect = MutableSharedFlow<NotificationEffect>()
    val effect = _effect.asSharedFlow()

    val unreadCount =
        repository.observeUnreadCount()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )

    val notifications = useCases.getNotifications()
        .onEach { Log.d("NotificationVM", "New notifications paging data emitted") }
        .map { pagingData ->
            pagingData.map {
                it.toUi()
            }
        }.cachedIn(viewModelScope)


    fun markRead(id: Long) {
        viewModelScope.launch {
            runCatching {
                useCases.markRead(id)
            }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            unreadCount =
                            maxOf(
                                0,
                                it.unreadCount - 1
                            )
                        )
                    }
                }
                .onFailure { error ->
                    _effect.emit(NotificationEffect.ShowSnackBar(error.extractReason()))
                }
        }
    }

    private fun markAllRead() {
        viewModelScope.launch {
            runCatching {
                useCases.markAllRead()
            }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            unreadCount = 0
                        )
                    }
                }
                .onFailure { error ->
                    _effect.emit(NotificationEffect.ShowSnackBar(error.extractReason()))
                }
        }
    }

    private fun refresh() {  }

    fun onEvent(
        event: NotificationUiEvent
    ) {

        when(event) {

            NotificationUiEvent.Refresh ->
                refresh()

            NotificationUiEvent.MarkAllRead ->
                markAllRead()

            is NotificationUiEvent.NotificationClicked ->
                openNotification(event.notification)

            is NotificationUiEvent.DeleteNotification -> {
                viewModelScope.launch {
                    repository.deleteNotification(event.notificationId)
                }
            }

            is NotificationUiEvent.AcceptConnectRequest -> {
                viewModelScope.launch {
                    repository.respondToConnectRequest(event.notification.entityId, "ACCEPTED", event.message)
                        .onSuccess {
                            repository.deleteNotification(event.notification.id)
                        }
                        .onFailure { error ->
                            _effect.emit(NotificationEffect.ShowSnackBar(error.extractReason()))
                        }
                }
            }

            is NotificationUiEvent.RejectConnectRequest -> {
                viewModelScope.launch {
                    repository.respondToConnectRequest(event.notification.entityId, "REJECTED")
                        .onSuccess {
                            repository.deleteNotification(event.notification.id)
                        }
                        .onFailure { error ->
                            _effect.emit(NotificationEffect.ShowSnackBar(error.extractReason()))
                        }
                }
            }

            NotificationUiEvent.DismissReplySheet -> {
                _uiState.update { it.copy(selectedPostIdForComments = null) }
            }

            is NotificationUiEvent.Retry ->{}

        }
    }

    private fun openNotification(
        notification: Notification
    ) {

        viewModelScope.launch {

            runCatching {

                useCases.markRead(notification.id)

            }

            _uiState.update {

                it.copy(

                    unreadCount =
                        maxOf(
                            0,
                            it.unreadCount - 1
                        )

                )

            }

            if (notification.type == NotificationType.CONNECT_REQUEST && notification.isActionDone) {
                _effect.emit(
                    NotificationEffect.NavigateToSendMessage(
                        userId = notification.senderUid,
                        userName = notification.senderName,
                        userImage = notification.senderProfileUrl
                    )
                )
                return@launch
            }

            when(notification.entityType) {

                EntityType.POST -> {
                    if (notification.type == NotificationType.COMMENT || 
                        notification.type == NotificationType.REPLY) {
                        _uiState.update { it.copy(selectedPostIdForComments = notification.entityId) }
                    } else if (notification.type != NotificationType.LIKE) {
                        _effect.emit(NotificationEffect.NavigateToPost(notification.entityId))
                    }
                }

                EntityType.USER ->

                    _effect.emit(

                        NotificationEffect.NavigateToProfile(

                            notification.senderUid

                        )

                    )

                EntityType.CHAT ->

                    _effect.emit(

                        NotificationEffect.NavigateToChat(

                            chatId = notification.senderUid,
                            name = notification.senderName,
                            image = notification.senderProfileUrl

                        )

                    )

                else -> {}
            }
        }
    }

}