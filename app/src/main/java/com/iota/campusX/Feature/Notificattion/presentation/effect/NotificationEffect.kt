package com.iota.campusX.Feature.Notificattion.presentation.effect

sealed interface NotificationEffect {

    data class NavigateToPost(
        val postId: String
    ) : NotificationEffect

    data class NavigateToProfile(
        val uid: String
    ) : NotificationEffect

    data class NavigateToChat(
        val chatId: String,
        val name: String,
        val image: String?
    ) : NotificationEffect

    data class NavigateToSendMessage(
        val userId: String,
        val userName: String,
        val userImage: String?
    ) : NotificationEffect

    data class ShowSnackBar(
        val message: String
    ) : NotificationEffect
}