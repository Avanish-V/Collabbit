package com.iota.campusX.Feature.Notificattion.domain.usecase

data class NotificationUseCases(

    val getNotifications: GetNotificationsUseCase,

    val unreadCount: GetUnreadCountUseCase,

    val markRead: MarkNotificationReadUseCase,

    val markAllRead: MarkAllNotificationReadUseCase
)