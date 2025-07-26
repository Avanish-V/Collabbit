package com.iota.campusX.Screens.Post

data class PostActionHandlers(
    val onPostClick: () -> Unit = {},
    val onLikeClick: () -> Unit = {},
    val onReplyClick: () -> Unit = {},
    val onDotMenuClick: () -> Unit = {},
    val onPollSelect: (String) -> Unit = {},
    val onProfileClick: (String) -> Unit = {},
    val onPostImageClick: (String) -> Unit = {}
)
