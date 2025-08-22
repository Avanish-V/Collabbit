package com.iota.campusX.Screens.Post.PostActions

import com.iota.campusX.Screens.Post.DataModel.ContentId

sealed class PostAction {
    data class Like(val isLiked: Boolean,val contentId: ContentId,val userId: String) : PostAction()
    data class Comment(val postId: String) : PostAction()
    data class Share(val postId: String) : PostAction()
    data class OpenUserProfile(val userId: String,val isCurrentUser: Boolean) : PostAction()
    data class OpenPostDetail(val postId: String) : PostAction()
    data class VotePoll(val postId: String) : PostAction()
}
