package com.iota.campusX.Screens.Post.PostActions

import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Screens.Post.DataModel.ContentId

sealed class PostAction {
    data class Like(val isLiked: Boolean,val contentId: ContentId,val userId: String) : PostAction()
    data class Comment(val postId: String) : PostAction()
    data class Share(val postId: String) : PostAction()
    data class OpenUserProfile(val userId: String,val isCurrentUser: Boolean) : PostAction()
    data class ViewPostVisualContent(val post: GetPostDTO) : PostAction()
    data class OpenPostDetail(val postId: String) : PostAction()
    data class VotePoll(val postId: String,val optionId: String,val feedMode: FeedMode) : PostAction()

    data class FollowUser(val userId: String) : PostAction()

    data class UnFollowUser(val userId: String) : PostAction()


}
