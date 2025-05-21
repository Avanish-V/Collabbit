package com.iota.campusX.Screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.domain.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.PostActions
import com.iota.campusX.Feature.Post.domain.PostContent
import com.iota.campusX.Feature.Post.domain.PostData
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Feature.Post.presentation.PostViewModel
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.BottomSheetSharedViewModel
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.generateUID
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.AlertDialogWidget
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.PostActionsComponent
import com.iota.campusX.ui.UIComponents.PostBody
import com.iota.campusX.ui.UIComponents.PostCard
import com.iota.campusX.ui.UIComponents.PostDotOptionBottomSheet
import com.iota.campusX.ui.UIComponents.PostHeader
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.White900
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostReplyScreen(
    navHostController: NavHostController,
    profileViewModel: UserProfileViewModel,
    postViewModel: PostViewModel,
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

    val keyboard = LocalSoftwareKeyboardController.current
    var replyText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var interactionSource = remember { MutableInteractionSource() }

    val bottomSheetViewModel: BottomSheetSharedViewModel = viewModel()
    val bottomSheetData = bottomSheetViewModel.bottomSheetState.collectAsState().value
    val isAlertDialogVisible = remember { mutableStateOf(false) }


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
                            modifier = Modifier
                                .fillMaxWidth()
                                .imePadding(),
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = {
                                Text("Write your comment...")
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {

                                        if (replyText.isNotEmpty()) {

                                            val docID = generateUID()

                                            keyboard?.hide()

                                            scope.launch {

                                                postData.let { post ->
                                                    postViewModel.createReply(
                                                        replyId = docID,
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
                                                                        replyId = docID,
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
                                                                        ),
                                                                        repliedAt = getTimeMillis()
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

                       postData?.let {
                           PostCard(
                               onPostClick = {},
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
                               },
                               onDotMenuClick = {
                                   bottomSheetViewModel.setBottomSheetState(
                                       state = true,
                                       isCurrentUser = postData.creatorDetail.isCurrentUser,
                                       postId = postData.postId,
                                       campusId = postData.campusId.toString()
                                   )
                               },
                               goToProfile ={
                                   if (postData.postMode != "USER") return@PostCard

                                   navHostController.navigate(Routes.Main.Profile.routes)
                                       .apply {
                                           navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                               "USER_ID",
                                               postData.creatorDetail.profile?._id
                                           )
                                       }

                               },
                               post = it,
                               navHostController = navHostController
                           )
                       }
                    }

                    item {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .background(color = secondary)
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

                            val orderedData = repliesData.data.sortedByDescending { it.repliedAt }

                            items(orderedData) {
                                ReplyWidget(
                                    repliesDTO = it,
                                    navHostController = navHostController,
                                    onLikeClick = {
                                        if (userProfile != null) {
                                            postViewModel.likeReply(
                                                creatorId = userProfile._id,
                                                postId = it.postId,
                                                replyId = it.replyId,
                                                isLiked = it.actions.isLiked
                                            )
                                        }
                                    },
                                    onDotsClick = {
                                        bottomSheetViewModel.setBottomSheetState(
                                            state = true,
                                            isCurrentUser = it.user.isCurrentUser == true,
                                            postId = it.postId,
                                            replyId = it.replyId,
                                            campusId = ""
                                        )

                                    },
                                )
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


                PostDotOptionBottomSheet(
                    isBottomSheet = bottomSheetData.isBottomSheet,
                    onDismiss = { bottomSheetViewModel.hideBottomSheet(false) },
                    isCurrentUser = bottomSheetData.isCurrentUser,
                    onDeleteClick = {
                        isAlertDialogVisible.value = !isAlertDialogVisible.value
                    },
                    onEditClick = {

                    }
                )

                AlertDialogWidget(
                    isVisible = isAlertDialogVisible.value,
                    onDismiss = { isAlertDialogVisible.value = it },
                    title = "Delete Reply",
                    description = "Are you sure you want to delete this reply?",
                    positiveButtonText = "Delete",
                    negativeButtonText = "Cancel",
                    onPositiveClick = {

                        scope.launch {
                            postViewModel.deleteReply(
                                bottomSheetData.postId,
                                replyId = bottomSheetData.replyId,
                                bottomSheetData.campusId
                            )
                                .collect {
                                    when (it) {
                                        is ResultState.Success -> {
                                            delay(1000)
                                            isLoading = false
                                            isAlertDialogVisible.value =
                                                false
                                            bottomSheetData.isBottomSheet =
                                                false
                                            postViewModel.updateDeleteReply(
                                                postId = bottomSheetData.postId,
                                                replyId = bottomSheetData.replyId
                                            )
                                        }

                                        is ResultState.Error -> {
                                            bottomSheetData.isBottomSheet = false
                                            isLoading = false

                                            Toast.makeText(
                                                context,
                                                it.message,
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }

                                        is ResultState.Loading -> {
                                            isLoading = true
                                        }
                                    }
                                }
                        }

                        context.vibrate()

                    },
                    showLoading = isLoading

                )
            }
        }
    }

}

@Composable
fun ReplyWidget(
    repliesDTO: GetRepliesDTO,
    navHostController: NavHostController,
    onLikeClick:()-> Unit,
    onDotsClick:()-> Unit
) {

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
                image = repliesDTO.user.userImage,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                onClick = {

                }
            )

            PostHeader(
                user = repliesDTO.user,
                onNameClick = {
                    navHostController.navigate(Routes.Main.Profile.routes)
                        .apply {
                            navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                "USER_ID",
                                repliesDTO.user._id
                            )
                        }
                },
                postedAt = getTimeAgo(repliesDTO.repliedAt)
            )
        }

        PostBody(
            postContent = PostContent(
                postType = "Text",
                postData = PostData(
                    postText = repliesDTO.content
                )
            ),
            navHostController = navHostController
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {

                Text(text = repliesDTO.actions.likesCount.toString(), color = Black500)

                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onLikeClick.invoke()
                            }
                        ),
                    painter = painterResource(if (repliesDTO.actions.isLiked) R.drawable.heart_bold else R.drawable.heart_outline),
                    contentDescription = "Like",
                    tint = if (repliesDTO.actions.isLiked) Color.Red else Black500
                )

            }

            Icon(
                modifier = Modifier
                    .rotate(90f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onDotsClick.invoke()
                        }
                    ),
                painter = painterResource(R.drawable.dots_menu),
                contentDescription = "Dots",
                tint = Black500
            )

        }

    }
}