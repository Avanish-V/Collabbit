package com.iota.campusX.Screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.domain.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.PostActions
import com.iota.campusX.Feature.Post.domain.PostContent
import com.iota.campusX.Feature.Post.domain.PostData
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Feature.Post.presentation.PostViewModel
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.CircleImage
import com.iota.campusX.Screens.Home.PostBody
import com.iota.campusX.Screens.Home.PostHeader
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.White900
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.launch
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostReplyScreen(
    navHostController: NavHostController,
    profileViewModel: UserProfileViewModel,
    postViewModel: PostViewModel
) {

    val userProfile = profileViewModel.userBaseProfile.collectAsState().value.baseProfileData
    val repliesData = postViewModel.repliesState.collectAsState().value

    val postId by remember {
        mutableStateOf(navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("POST_ID"))
    }

    val postState = postViewModel.postState.collectAsState().value

    LaunchedEffect(Unit) {
        postId?.let { postViewModel.getReplies(postId = it) }
    }
    val uid by remember { mutableStateOf(UUID.randomUUID().toString()) }
    val keyboard = LocalSoftwareKeyboardController.current
    var replyText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var interactionSource = remember { MutableInteractionSource() }



    when{
        postState.isLoading->{

        }
        postState.postData.isNotEmpty() -> {

            val postData = postState.postData.find { it.postId == postId }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Reply") },
                        navigationIcon = {
                            IconButton(onClick = { navHostController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },

                        )
                },
                bottomBar = {

                    Column {

                        HorizontalDivider(
                            color = Black500
                        )

                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = {
                                Text("Write your reply...")
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {

                                        if (replyText.isNotEmpty()) {

                                            keyboard?.hide()

                                            scope.launch {

                                                postData.let { post ->
                                                    postViewModel.createReply(
                                                        replyId = uid,
                                                        postId = postData?.postId ?: "",
                                                        content = replyText,
                                                        repliedAt = getTimeMillis(),
                                                        createrId = postData?.creatorDetail?.profile?._id ?: ""
                                                    ).collect {
                                                        when(it){
                                                            is ResultState.Success -> {
                                                                isLoading = false
                                                                postViewModel.updateReply(
                                                                    GetRepliesDTO(
                                                                        replyId = uid,
                                                                        postId = postData!!.postId,
                                                                        user = User(
                                                                            _id = userProfile?._id ?: "",
                                                                            userName = userProfile?.userName ?: "",
                                                                            userImage = userProfile?.userImage ?: ""
                                                                        ),
                                                                        content = replyText,
                                                                        actions = PostActions(
                                                                            isLiked = false,
                                                                            likesCount = 0,
                                                                            replies = emptyList(),
                                                                            replyCount = 0

                                                                        )
                                                                    )
                                                                )
                                                                replyText = ""
                                                            }

                                                            is ResultState.Error -> {
                                                                isLoading = false
                                                            }

                                                            is ResultState.Loading -> {
                                                                isLoading = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    if (isLoading){
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            trackColor = secondary,
                                            color = primary
                                        )
                                    }else{
                                        Icon(
                                            painter = painterResource(R.drawable.send_2),
                                            contentDescription = "Send",
                                            tint = primary
                                        )
                                    }
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = White900,
                                unfocusedContainerColor = White900,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTrailingIconColor = primary
                            )
                        )

                    }

                },
                containerColor = secondary
            ) { innerPadding ->

                LazyColumn(
                    Modifier.padding(paddingValues = innerPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    item {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = White900)
                                .padding(12.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                CircleImage(
                                    image = postData?.creatorDetail?.profile?.userImage ?: "",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                )
                                PostHeader(
                                    user = postData!!.creatorDetail.profile,
                                    pod = postData.reference,
                                    postedAt = getTimeAgo(postData.postedAt),
                                    onNameClick = {
                                        navHostController.navigate("PROFILE_By_Id")
                                    }
                                )
                            }


                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                                PostBody(
                                    postContent = PostContent(
                                        postType = postData?.postContent?.postType ?: "",
                                        postData = PostData(
                                            postText = postData?.postContent?.postData?.postText ?: "",
                                            postImage = postData?.postContent?.postData?.postImage?:""
                                        )
                                    ),
                                    navHostController = navHostController
                                )

                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            com.iota.campusX.Screens.Home.PostActions(
                                postAction = postData!!.postActions,
                                user = postData.creatorDetail.profile,
                                onLikeClick = {
                                    postViewModel.toggleLike(
                                        userId = postData.creatorDetail.profile?._id ?: "",
                                        postId = postData.postId,
                                        isLiked = postData.postActions.isLiked
                                    )
                                    context.vibrate()
                                },
                                onReplyClick = {
                                    keyboard?.show()
                                }
                            )
                        }
                    }

                    item {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)) {
                            Text(text = "Replies", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    when {
                        repliesData.isLoading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        trackColor = secondary,
                                        color = primary
                                    )
                                }
                            }
                        }

                        repliesData.data.isNotEmpty() -> {

                            items(repliesData.data.reversed()) {

                                Column(
                                    modifier = Modifier
                                        .background(color = White900)
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                ) {

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {

                                        CircleImage(
                                            image = it.user.userImage,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                        )

                                        PostHeader(
                                            user = it.user,
                                            onNameClick = {
                                                navHostController.navigate("PROFILE_By_Id")
                                            }
                                        )
                                    }

                                    PostBody(
                                        postContent = PostContent(
                                            postType = "Text",
                                            postData = PostData(
                                                postText = it.content
                                            )
                                        ),
                                        navHostController = navHostController
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {

                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {

                                            Text(text = it.actions.likesCount.toString(), color = Black500)

                                            Icon(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clickable(
                                                        interactionSource = interactionSource,
                                                        indication = null,
                                                        onClick = {

                                                            if (userProfile != null) {
                                                                postViewModel.likeReply(
                                                                    userId = userProfile._id,
                                                                    postId = postData!!.postId,
                                                                    replyId = it.replyId,
                                                                    isLiked = it.actions.isLiked
                                                                )
                                                            }

                                                            context.vibrate()

                                                        }
                                                    ),
                                                painter = painterResource(if (it.actions.isLiked) R.drawable.heart_bold else R.drawable.heart_outline),
                                                contentDescription = "Like",
                                                tint = if (it.actions.isLiked) Color.Red else Black500
                                            )

                                        }
                                        
                                    }

                                }

                            }
                        }

                        repliesData.data.isEmpty() -> {

                            item {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No Replies", color = Black500)
                                }
                            }
                        }

                        repliesData.error.isNotEmpty() -> {}
                    }

                }

            }

        }

    }



}