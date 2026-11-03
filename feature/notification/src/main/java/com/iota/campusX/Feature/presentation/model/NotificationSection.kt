package com.iota.campusX.Feature.Notificattion.presentation.model

sealed interface NotificationSection {


    data class Header(
        val title: String
    ) : NotificationSection


    data class Item(
        val notification: NotificationUi
    ) : NotificationSection

}