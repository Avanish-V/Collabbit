package com.iota.campusX.Screens.Post.PostActions

import android.content.Context
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Reply.ReplyRepository
import com.iota.campusX.Navigation.AppNavigator
import com.iota.campusX.Screens.Post.DataModel.ContentId
import com.iota.campusX.Screens.Post.PostManupulation.PostRepository
import com.iota.campusX.Utils.vibrate

class PostActionHandler(
    private val postRepository: PostRepository,
    private val replyRepository: ReplyRepository,
    private val context: Context,
    private val navHostController: AppNavigator // abstract navigation, for profile or detail
) {

    suspend fun handle(action: PostAction) {
        when (action) {
            is PostAction.Like -> {
                context.vibrate()
                when(val id = action.contentId){
                    is ContentId.Post -> {
                        postRepository.likePost(userId = action.userId, postId = id.postId, isLiked = action.isLiked)
                    }
                    is ContentId.Reply -> {
                        replyRepository.likeReply(userId = action.userId, replyId = id.replyId, isLiked = action.isLiked, postId = id.postId)
                    }
                }
            }
            is PostAction.OpenPostDetail -> {
                goToReplyScreen(action.postId)
            }
            is PostAction.OpenUserProfile -> {
                if (action.isCurrentUser){
                    navHostController.navigateToOwnerProfile()
                }else{
                    navHostController.navigateToViewUserProfile(action.userId)
                }
            }

            else -> {}
        }
    }

    private suspend fun handleLike(userId: String, postId: String, isLiked: Boolean, campusId: String?, feedMode: FeedMode) {
         // Optimistic + API
    }

    private suspend fun goToReplyScreen(postId: String){
        navHostController.navigateToPostDetail(postId)
    }

}
