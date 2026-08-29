package com.iota.campusX.Screens.Home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.domain.attachment.ImageAttachmentDto
import com.iota.campusX.Feature.Post.presentation.components.PostAction
import com.iota.campusX.Feature.Post.presentation.feed.PostFeedViewModel
import com.iota.campusX.Feature.Reply.presentation.components.ReplyButtonComponent
import com.iota.campusX.Navigation.PostView
import com.iota.campusX.R
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.FeedUI.AnimatedLikeButton
import com.iota.campusX.ui.UIComponents.FeedUI.Avatar
import com.iota.campusX.ui.UIComponents.FeedUI.ExpandableText
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostViewScreen(
    navHostController: NavHostController,
    postFeedViewModel: PostFeedViewModel = koinViewModel()
) {
    val navEntry = remember(navHostController) {
        navHostController.currentBackStackEntry
    }
    val routeArgs = navEntry?.toRoute<PostView>()
    val postId = routeArgs?.postId
    val initialImage = routeArgs?.postImage

    val singlePostState by postFeedViewModel.singlePost.collectAsState()

    LaunchedEffect(postId) {
        postId?.let { postFeedViewModel.fetchSinglePost(it) }
    }

    var userScrollEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
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
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black)) {
            when (val state = singlePostState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                is UiState.Success -> {
                    val post = state.data
                    val images = (post.attachment as? ImageAttachmentDto)?.images ?: listOfNotNull(initialImage)
                    
                    PostViewerContent(
                        post = post,
                        images = images,
                        userScrollEnabled = userScrollEnabled,
                        onZoomChange = { userScrollEnabled = it <= 1.01f },
                        onPostAction = { postFeedViewModel.onPostEvent(it) }
                    )
                }
                is UiState.Error -> {
                    // If we have an initial image, show it even if post details fail to load
                    if (initialImage != null) {
                        ZoomableImage(image = initialImage)
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = state.message, color = Color.White)
                        }
                    }
                }
                else -> {
                    if (initialImage != null) {
                        ZoomableImage(image = initialImage)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PostViewerContent(
    post: Post,
    images: List<String>,
    userScrollEnabled: Boolean,
    onZoomChange: (Float) -> Unit,
    onPostAction: (PostAction) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { images.size })

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

            // Author and Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(
                    imageUrl = post.author.authorImage,
                    onAvatarClick = {}
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = post.author.authorName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Posted ${getTimeAgo(post.createdAt.toLongOrNull() ?: System.currentTimeMillis())}",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Caption
            Text(
                text = post.caption,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedLikeButton(
                    isLiked = post.isLiked,
                    likesCount = post.likesCount,
                    onLike = { isLiked ->
                        onPostAction(PostAction.Like(isLiked, post.postId))
                    },
                    tint = Color.White
                )

                ReplyButtonComponent(
                    replyCount = post.commentCount.toString(),
                    onReplyClick = { },
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp)) // Padding for bottom system bars
        }
    }
}

@Composable
fun ZoomableImage(image: String, onZoomChange: (Float) -> Unit = {}) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale > 1f) {
                        offset += pan
                    } else {
                        offset = Offset.Zero
                    }
                    onZoomChange(scale)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 3f
                        }
                        onZoomChange(scale)
                    }
                )
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                
                if (scale > 1f) {
                    val maxOffsetHorizontal = (scale - 1) * size.width / 2
                    val maxOffsetVertical = (scale - 1) * size.height / 2
                    translationX = offset.x.coerceIn(-maxOffsetHorizontal, maxOffsetHorizontal)
                    translationY = offset.y.coerceIn(-maxOffsetVertical, maxOffsetVertical)
                } else {
                    translationX = 0f
                    translationY = 0f
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = image,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
