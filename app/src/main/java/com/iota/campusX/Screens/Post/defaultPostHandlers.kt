package com.iota.campusX.Screens.Post

import android.content.Context
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.Screens.Home.BottomSheet.SharedBottomSheetViewModel
import com.iota.campusX.Screens.Home.BottomSheet.Content
import com.iota.campusX.Screens.Home.BottomSheet.ContentType
import com.iota.campusX.Screens.Home.BottomSheet.SheetType
import com.iota.campusX.Utils.vibrate

fun defaultPostHandlers(
    context: Context,
    post: GetPostDTO,
    feedViewModel: PostFeedViewModel,
    bottomSheetSharedViewModel: SharedBottomSheetViewModel,
    navController: NavHostController,
): PostActionHandlers {
    return PostActionHandlers(
        onPostClick = {
            navController.navigate(Routes.Main.ReplyPost.routes).apply {
                navController.currentBackStackEntry?.savedStateHandle?.set("POST_ID", post.postId)
            }
        },
        onReplyClick = {
            navController.navigate(Routes.Main.ReplyPost.routes).apply {
                navController.currentBackStackEntry?.savedStateHandle?.set("POST_ID", post.postId)
            }
        },
        onLikeClick = {
            feedViewModel.toggleLike(
                userId = post.creatorDetail.profile?.id.orEmpty(),
                postId = post.postId,
                isLiked = post.postActions.isLiked,
                campusId = post.campusId,
                feedMode = post.feedMode
            )
            context.vibrate()
        },
        onDotMenuClick = {
            bottomSheetSharedViewModel.setBottomSheetState(
                state = true,
                isCurrentUser = post.creatorDetail.isCurrentUser,
                campusId = post.campusId,
                content = Content(
                    postId = post.postId,
                    text = post.postContent.postData.postText
                ),
                feedMode = post.feedMode,
                contentType = ContentType.POST,
                sheetType = SheetType.MENU_LIST
            )
        },
        onPollSelect = { optionId ->
            feedViewModel.voteOnPoll(
                postId = post.postId,
                optionId = optionId,
                campusId = post.campusId,
                feedMode = post.feedMode,
                userId = post.creatorDetail.profile?.id.orEmpty()
            )
        },
        onProfileClick = { userId ->
            navController.navigate(Routes.Main.ProfileByID.routes)
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("USER_ID", userId)
        },
        onPostImageClick = {
            navController.navigate(Routes.Main.PostViewScreen.routes).apply {
                navController.currentBackStackEntry?.savedStateHandle?.set("POST_IMAGE", it)
            }
        }
    )
}
