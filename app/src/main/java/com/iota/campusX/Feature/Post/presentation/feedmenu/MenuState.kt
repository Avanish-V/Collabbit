package com.iota.campusX.Feature.Post.presentation.feedmenu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MenuState {

    var context by mutableStateOf<MenuContext?>(null)
        private set

    var showDeleteDialog by mutableStateOf(false)
        private set

    fun show(context: MenuContext) {
        this.context = context
    }

    fun dismiss() {
        context = null
        showDeleteDialog = false
    }

    fun showDeleteDialog() {
        showDeleteDialog = true
    }
}