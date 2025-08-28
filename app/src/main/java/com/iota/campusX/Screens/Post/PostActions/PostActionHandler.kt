package com.iota.campusX.Screens.Post.PostActions

import android.content.Context
import com.iota.campusX.Feature.Post.domain.repository.PostRepository
import com.iota.campusX.Feature.Reply.ReplyRepository
import com.iota.campusX.Navigation.AppNavigator
import com.iota.campusX.Screens.Post.DataModel.ContentId
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import com.iota.campusX.Screens.Post.SharedVisualContentViewModel
import com.iota.campusX.Utils.vibrate

class PostActionHandler(
    private val repository: PostRepository,
    private val replyRepository: ReplyRepository,
    private val context: Context,
    private val navHostController: AppNavigator,
    private val sharedVisualContentViewModel: SharedVisualContentViewModel,
) {

    suspend fun handle(action: PostAction) {

        when (action) {

            is PostAction.Like -> {
                context.vibrate()
                when(val id = action.contentId){
                    is ContentId.Post -> {
                        repository.likePost(userId = action.userId, postId = id.postId, isLiked = action.isLiked)
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

            is PostAction.ViewPostVisualContent -> {
                navHostController.navigateToViewPostVisualContent(action.post.postContent.postImage).apply {
                    sharedVisualContentViewModel.setPost(action.post)
                }
            }

            is PostAction.Comment -> {
                goToReplyScreen(action.postId)
            }

            is PostAction.VotePoll -> {
                context.vibrate()
                repository.votePoll(action.postId, action.optionId, feedMode = action.feedMode)
            }

            else -> {}

        }
    }

    private  fun goToReplyScreen(postId: String){
        navHostController.navigateToPostDetail(postId)
    }

}
