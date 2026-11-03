package com.iota.campusX.Feature.Post.presentation.components

import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuContext

sealed class PostAction {
    data class Like(val isLiked: Boolean, val contentId: String) : PostAction()

    data class OpenUserProfile(val userId: String, val isCurrentUser: Boolean) : PostAction()
    data class OpenPostDetail(val post: Post) : PostAction()
    data class ViewPostVisualContent(val post: Post, val initialIndex: Int = 0) : PostAction()
    data class VotePoll(val postId: String, val optionId: String) : PostAction()
    data class Delete(val postId: String) : PostAction()
    data class Edit(val postId: String, val newCaption: String) : PostAction()
    data class Share(val post: Post) : PostAction()
}
