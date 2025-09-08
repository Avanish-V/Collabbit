package com.iota.campusX.Screens.Home

//import com.iota.campusX.ui.theme.DarkTheme_Black
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Screens.Post.DataModel.ContentId
import com.iota.campusX.Screens.Post.DataModel.ContentType
import com.iota.campusX.Screens.Post.DataModel.FeedContent
import com.iota.campusX.Screens.Post.PostActions.PostAction
import com.iota.campusX.Screens.Post.PostActions.PostActionViewModel
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuState
import com.iota.campusX.Screens.Post.SharedVisualContentViewModel
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.FeedUI.Avatar
import com.iota.campusX.ui.UIComponents.FeedUI.FeedHeader
import com.iota.campusX.ui.UIComponents.FeedUI.toMillis
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
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center){
                ZoomableImage(
                    image = post.value?.postContent?.postImage.toString()
                )
            }
            Column (modifier = Modifier.padding(24.dp)){

                post.value?.let {

                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        Avatar(
                            imageUrl = it.creatorDetail.profile?.userImage ?: "",
                            visibilityMode = it.visibilityMode,
                            onAvatarClick = {

                            }
                        )

                        FeedHeader(
                            creator = it.creatorDetail,
                            postedAt = getTimeAgo(it.createdAt.toMillis()),
                            visibilityMode = it.visibilityMode,
                            feedMode = it.feedMode
                        )
                    }

                    Text(text = it.postContent.postText)

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
fun ZoomableImage(image: String) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val minScale = 1f
    val maxScale = 4f

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)

        val limitedOffset = if (newScale > minScale) {
            val newOffset = offset + offsetChange

            // Set move threshold — how far you can drag
            val moveLimit = 1000f * (newScale - 1f) // adjust multiplier as needed

            Offset(
                x = newOffset.x.coerceIn(-moveLimit, moveLimit),
                y = newOffset.y.coerceIn(-moveLimit, moveLimit)
            )
        } else {
            Offset.Zero
        }

        scale = newScale
        offset = limitedOffset
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .transformable(state)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > minScale) {
                            scale = minScale
                            offset = Offset.Zero
                        } else {
                            scale = 2f
                        }
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



