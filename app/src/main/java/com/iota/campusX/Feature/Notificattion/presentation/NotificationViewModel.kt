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
import com.iotabuild.campuscircle.Notification.entity.EntityType
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

        repository

            .observeUnreadCount()

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

        }

    }

    private fun markAllRead() {

        viewModelScope.launch {

            runCatching {

                useCases.markAllRead()

            }

            _uiState.update {

                it.copy(
                    unreadCount = 0
                )

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
                openNotification(
                    event.notification
                )

            is NotificationUiEvent.DeleteNotification ->{}


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

            when(notification.entityType) {

                EntityType.POST ->

                    _effect.emit(

                        NotificationEffect.NavigateToPost(

                            notification.entityId

                        )

                    )

                EntityType.USER ->

                    _effect.emit(

                        NotificationEffect.NavigateToProfile(

                            notification.senderUid

                        )

                    )

                EntityType.CHAT ->

                    _effect.emit(

                        NotificationEffect.NavigateToChat(

                            notification.entityId.toString()

                        )

                    )

                else -> {}
            }
        }
    }

}