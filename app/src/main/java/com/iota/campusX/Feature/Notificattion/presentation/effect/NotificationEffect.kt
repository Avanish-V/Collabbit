package com.iota.campusX.Feature.Notificattion.presentation.effect

sealed interface NotificationEffect {

    data class NavigateToPost(
        val postId: String
    ) : NotificationEffect

    data class NavigateToProfile(
        val uid: String
    ) : NotificationEffect

    data class NavigateToChat(
        val chatId: String
    ) : NotificationEffect

    data class ShowSnackBar(
        val message: String
    ) : NotificationEffect
}