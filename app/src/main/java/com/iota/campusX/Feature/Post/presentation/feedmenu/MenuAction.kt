package com.iota.campusX.Feature.Post.presentation.feedmenu

sealed interface MenuAction {

    data class Edit(
        val context: MenuContext
    ) : MenuAction

    data class Delete(
        val context: MenuContext
    ) : MenuAction

    data class Share(
        val context: MenuContext
    ) : MenuAction

    data class Report(
        val context: MenuContext
    ) : MenuAction

    data class CopyLink(
        val context: MenuContext
    ) : MenuAction
}