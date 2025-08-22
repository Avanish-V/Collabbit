package com.iota.campusX.Screens.Post.PostMenuActions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.iota.campusX.Screens.Post.DataModel.FeedContent

class PostMenuState {
    var showSheet by mutableStateOf(false)
    var isOwner: Boolean by mutableStateOf(false)
    var currentContent: FeedContent? by mutableStateOf(null)

    fun open(content: FeedContent) {
        currentContent = content
        showSheet = true
    }

    fun close() {
        showSheet = false
        currentContent = null
    }
}
