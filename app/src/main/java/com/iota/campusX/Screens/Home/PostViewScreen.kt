package com.iota.campusX.Screens.Home

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.WindowManager
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.domain.attachment.ImageAttachmentDto
import com.iota.campusX.Feature.Post.domain.attachment.VideoAttachmentDto
import com.iota.campusX.ui.UIComponents.VideoPlayer
import com.iota.campusX.Feature.Post.presentation.components.ZoomableBox
import com.iota.campusX.Feature.Post.presentation.components.PostAction
import com.iota.campusX.Feature.Post.presentation.feed.PostFeedViewModel
import com.iota.campusX.Feature.Reply.presentation.ReplyBottomSheet
import com.iota.campusX.Feature.Reply.presentation.components.ReplyButtonComponent
import com.iota.campusX.Navigation.PostView
import com.iota.campusX.R
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.FeedUI.AnimatedLikeButton
import com.iota.campusX.ui.UIComponents.FeedUI.Avatar
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostViewScreen(
    routeArgs: PostView,
    navHostController: NavHostController,
    postFeedViewModel: PostFeedViewModel = koinViewModel(),
) {
    val postId = routeArgs.postId
    val initialImage = routeArgs.postImage
    val initialIndex = routeArgs.initialIndex

    val singlePostState by postFeedViewModel.singlePost.collectAsState()

    val scope = rememberCoroutineScope()
    var showReplyBottomSheet by remember { mutableStateOf(value = false) }
    val replyBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(postId) {
        if (postId != null) {
            postFeedViewModel.fetchSinglePost(postId)
        } else {
            postFeedViewModel.clearSinglePost()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            postFeedViewModel.clearSinglePost()
        }
    }

    var userScrollEnabled by remember { mutableStateOf(value = true) }

    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // Allow drawing into the notch area
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            // Hide system bars for immersive experience
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            
            onDispose {
                // Restore system bars when leaving
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Content Area
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = singlePostState) {
                is UiState.Loading -> {
                    // Show initial image while loading post details
                    if (initialImage != null) {
                        ZoomableImage(image = initialImage)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }
                is UiState.Success -> {
                    val post = state.data
                    val attachment = post.attachment
                    if (attachment is VideoAttachmentDto) {
                        VideoPlayer(
                            videoUrl = attachment.videoUrl,
                            thumbnailUrl = attachment.thumbnailUrl,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val images = (attachment as? ImageAttachmentDto)?.images ?: listOfNotNull(initialImage)
                        
                        PostViewerContent(
                            post = post,
                            images = images,
                            initialIndex = initialIndex,
                            userScrollEnabled = userScrollEnabled,
                            onZoomChange = { userScrollEnabled = it <= 1.01f },
                            onPostAction = { postFeedViewModel.onPostEvent(it) },
                            onReplyClick = {
                                showReplyBottomSheet = true
                                scope.launch { replyBottomSheetState.show() }
                            }
                        )
                    }

                    if (showReplyBottomSheet) {
                        ReplyBottomSheet(
                            postId = post.postId,
                            onDismiss = {
                                scope.launch {
                                    replyBottomSheetState.hide()
                                    showReplyBottomSheet = false
                                }
                            },
                            sheetState = replyBottomSheetState,
                            onMoreClick = { },
                            onAction = { postFeedViewModel.onPostEvent(it) }
                        )
                    }
                }
                is UiState.Error -> {
                    if (initialImage != null) {
                        ZoomableImage(image = initialImage)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = state.message, color = Color.White)
                        }
                    }
                }
                else -> {
                    initialImage?.let { ZoomableImage(image = it) }
                }
            }
        }

        // Overlay TopAppBar
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(
                    onClick = { navHostController.popBackStack() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            ),
            windowInsets = WindowInsets(0, 0, 0, 0)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PostViewerContent(
    post: Post,
    images: List<String>,
    initialIndex: Int,
    userScrollEnabled: Boolean,
    onZoomChange: (Float) -> Unit,
    onPostAction: (PostAction) -> Unit,
    onReplyClick: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { images.size }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = userScrollEnabled,
            verticalAlignment = Alignment.CenterVertically
        ) { page ->
            ZoomableImage(
                image = images[page],
                onZoomChange = onZoomChange
            )
        }

        // Overlay Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .padding(16.dp)
        ) {
            // Pager Indicator
            if (images.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(images.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(image: String, onZoomChange: (Float) -> Unit = {}) {
    ZoomableBox(
        modifier = Modifier.fillMaxSize(),
        onZoomChange = onZoomChange
    ) {
        AsyncImage(
            model = image,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
