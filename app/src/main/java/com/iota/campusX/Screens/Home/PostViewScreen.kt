package com.iota.campusX.Screens.Home

//import com.iota.campusX.ui.theme.DarkTheme_Black
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Screens.Post.PostActions.PostActionViewModel
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuState
import com.iota.campusX.Screens.Post.SharedVisualContentViewModel
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.UIComponents.FeedUI.Avatar
import com.iota.campusX.ui.UIComponents.FeedUI.FeedHeader
import io.ktor.websocket.Frame.Text
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostViewScreen(navHostController: NavHostController) {

    val postMenuState: PostMenuState = koinInject()
    val postActionViewModel: PostActionViewModel = koinInject()
    val sharedVisualContentViewModel: SharedVisualContentViewModel = koinInject()
    val postImage = navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("POST_IMAGE")
    val post = sharedVisualContentViewModel.post.collectAsState()

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { post.value?.postContent?.postImage?.size ?: 0 })

    var userScrollEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Post View")
                },
                navigationIcon = {
                    IconButton(onClick = {navHostController.popBackStack()}) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {

        },
    ) { padding->

        Column (modifier = Modifier.padding(padding)){
            Column {
                post.value?.postContent?.postImage?.let {

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        userScrollEnabled = userScrollEnabled
                    ) { currentPage ->

                        ZoomableImage(
                            image =  it[currentPage].mediaUrl,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        it.forEachIndexed { index, item ->

                            HorizontalDivider(
                                modifier = Modifier.width(30.dp).clip(CircleShape),
                                thickness = 4.dp,
                                color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                        }
                    }

                }

            }
            Column (modifier = Modifier.padding(24.dp)){

                post.value?.let {

                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        Avatar(
                            imageUrl = it.creatorDetail.profile?.image ?: "",
                            visibilityMode = it.visibilityMode,
                            onAvatarClick = {

                            }
                        )

                        it.createdAt?.let { timestamp ->
                            FeedHeader(
                                creator = it.creatorDetail,
                                postedAt = getTimeAgo(timestamp),
                                visibilityMode = it.visibilityMode,
                                feedMode = it.feedMode
                            )
                        }
                    }

                    it.postContent.postText?.let { text -> Text(text = text) }

//                    PostActionsComponent(
//                        postAction = it.postActions,
//                        user = it.creatorDetail.profile,
//                        onLikeClick = {
//                            postActionViewModel.onAction(
//                                PostAction.Like(
//                                    isLiked = it.postActions.isLiked,
//                                    contentId = ContentId.Post(postId = it.postId),
//                                    userId = it.creatorDetail.profile?.id ?: ""
//                                ),
//                            )
//                        },
//                        onReplyClick = {
//                            navHostController.popBackStack()
//                        },
//                        onDotMenuClick = {
//                            postMenuState.open(
//                                content = FeedContent(
//                                    id = ContentId.Post(postId = it.postId),
//                                    text = it.postContent.postData.postText,
//                                    isOwner = it.creatorDetail.isCurrentUser,
//                                    type = ContentType.POST
//
//                                )
//                            )
//                        }
//                    )

                }
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomableImage(image: String, onZoomChange: (Float) -> Unit = {}) {
    var scale by remember { mutableStateOf(1f) }
    val minScale = 1f
    val maxScale = 4f

    val transformState = rememberTransformableState { zoomChange, _, _ ->
        val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
        scale = newScale
        onZoomChange(newScale)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // 👇 Allow pager to scroll with one finger; this only reacts to multi-touch
            .transformable(transformState)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > minScale) minScale else 2f
                        onZoomChange(scale)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = image,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth()
        )
    }
}





