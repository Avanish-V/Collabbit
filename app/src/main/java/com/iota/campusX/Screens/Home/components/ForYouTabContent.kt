package com.iota.campusX.Screens.Home.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.iota.campusX.Feature.Post.domain.attachment.DocumentAttachmentDto
import com.iota.campusX.Feature.Post.domain.attachment.ImageAttachmentDto
import com.iota.campusX.Feature.Post.domain.attachment.VideoAttachmentDto
import com.iota.campusX.Feature.Post.presentation.feed.FeedUiRenderer
import com.iota.campusX.Feature.Post.presentation.feed.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.feedmenu.ContentType
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuActionViewModel
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuContext
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuController
import com.iota.campusX.Feature.Reply.presentation.ReplyBottomSheet
import com.iota.campusX.Navigation.*
import com.iota.campusX.Utils.sharePost
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForYouTabContent(
    navHostController: NavHostController,
    lazyState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    targetPostId: String? = null,
    postFeedViewModel: PostFeedViewModel = koinInject(),
    menuController: MenuController = koinInject(),
    menuActionViewModel: MenuActionViewModel = koinInject()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allPostState = postFeedViewModel.allPosts.collectAsLazyPagingItems()
    val replyBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showReplyBottomSheet by remember { mutableStateOf(false) }
    var replyPostId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(targetPostId) {
        if (targetPostId != null) {
            postFeedViewModel.fetchSinglePost(targetPostId, setAsFocused = true)
        }
    }

    val onImageClick: (com.iota.campusX.Feature.Post.data.remote.response.Post, Int) -> Unit = remember(navHostController) {
        { post, index ->
            handlePostImageClick(navHostController, post, index)
        }
    }

    val onProfileClick = remember(navHostController) { { authorId: String -> navHostController.navigate(ViewProfile(authorId)) } }

    Box {
        FeedUiRenderer(
            feed = allPostState,
            lazyState = lazyState,
            scrollBehavior = scrollBehavior,
            feedViewModel = postFeedViewModel,
            onReplyClick = { post ->
                replyPostId = post.postId
                showReplyBottomSheet = true
                scope.launch { replyBottomSheetState.show() }
            },
            onShareClick = { post ->
                val imageUrl = when (val attachment = post.attachment) {
                    is ImageAttachmentDto -> attachment.images.firstOrNull()
                    is VideoAttachmentDto -> attachment.thumbnailUrl
                    is DocumentAttachmentDto -> attachment.thumbnailUrl
                    else -> null
                }
                scope.launch {
                    sharePost(context, post.postId, post.caption, imageUrl)
                }
            },
            onMoreClick = {
                menuActionViewModel.showMenu(
                    menuController = menuController,
                    context = MenuContext(
                        id = it.postId,
                        type = ContentType.POST,
                        isOwner = it.author.isCurrentUser
                    )
                )
            },
            onImageClick = onImageClick,
            onProfileClick = onProfileClick
        )

        if (showReplyBottomSheet) {
            replyPostId?.let { postId ->
                ReplyBottomSheet(
                    postId = postId,
                    onDismiss = {
                        showReplyBottomSheet = false
                        replyPostId = null
                    },
                    sheetState = replyBottomSheetState,
                    onMoreClick = {
                        menuActionViewModel.showMenu(
                            menuController = menuController,
                            context = MenuContext(
                                id = it.id,
                                type = ContentType.REPLY,
                                isOwner = it.author.isCurrentUser
                            )
                        )
                    },
                    onAction = { }
                )
            }
        }
    }
}

private fun handlePostImageClick(
    navHostController: NavHostController,
    post: com.iota.campusX.Feature.Post.data.remote.response.Post,
    index: Int
) {
    val attachment = post.attachment
    when (attachment) {
        is VideoAttachmentDto -> {
            navHostController.navigate(
                VideoView(
                    videoUrl = attachment.videoUrl,
                    thumbnailUrl = attachment.thumbnailUrl
                )
            )
        }
        is DocumentAttachmentDto -> {
            navHostController.navigate(
                PdfView(
                    pdfUrl = attachment.url,
                    fileName = attachment.name,
                    thumbnailUrl = attachment.thumbnailUrl
                )
            )
        }
        is ImageAttachmentDto -> {
            val imageUrl = attachment.images.getOrNull(index)
            navHostController.navigate(
                PostView(
                    postId = post.postId,
                    postImage = imageUrl,
                    initialIndex = index
                )
            )
        }
        else -> {
            navHostController.navigate(PostView(postId = post.postId, initialIndex = index))
        }
    }
}
