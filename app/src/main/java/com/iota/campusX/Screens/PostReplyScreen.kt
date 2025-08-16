package com.iota.campusX.Screens

import ConsentAgreeViewModel
import ConsentBottomSheet
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.Models.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.Models.PostContent
import com.iota.campusX.Feature.Post.domain.Models.PostData
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.ReplyViewModel
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.BottomSheet.SharedBottomSheetViewModel
import com.iota.campusX.Screens.Home.BottomSheet.Content
import com.iota.campusX.Screens.Home.BottomSheet.ContentType
import com.iota.campusX.Screens.Home.BottomSheet.PostDotOptionBottomSheet
import com.iota.campusX.Screens.Home.BottomSheet.SheetType
import com.iota.campusX.Screens.Home.HomeViewModel
import com.iota.campusX.Screens.Post.PostOptions
import com.iota.campusX.Screens.Post.VisibilityModeChanger
import com.iota.campusX.Screens.Post.defaultPostHandlers
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.generateUID
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.AlertDialogWidget
import com.iota.campusX.ui.UIComponents.AnimatedLikeButton
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.CircularLoading
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.PostBody
import com.iota.campusX.ui.UIComponents.PostCard
import com.iota.campusX.ui.UIComponents.PostHeader
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.LightTheme_Blue
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostReplyScreen(
    navHostController: NavHostController,
    profileViewModel: UserProfileViewModel,
    postViewModel: PostFeedViewModel,
    homeViewModel: HomeViewModel,
    replyViewModel: ReplyViewModel
) {

    val consentAgreeViewModel = koinInject<ConsentAgreeViewModel>()
    val isConsentAgree by consentAgreeViewModel.isAgree.collectAsState()
    var consentBottomSheet = remember { mutableStateOf(false) }

    val userProfileState = profileViewModel.userBaseProfile.collectAsState().value
    val repliesState = replyViewModel.repliesState.collectAsState().value

    val editPostState = postViewModel.editPostState
    val deleteReplyState = replyViewModel.deleteReplyState.collectAsState()
    val createReplyState = replyViewModel.createReplyState.collectAsState()
    val deletePostState = postViewModel.deletePostState.collectAsState()

    val globalPostState = postViewModel.globalPosts.collectAsState().value
    val campusPostState = postViewModel.campusPosts.collectAsState().value

    val mode = homeViewModel.mode.collectAsState().value

    val userProfile = when (userProfileState) {
        is UiState.Success -> userProfileState.data
        else -> null // Or handle accordingly
    }


    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var replyText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    var visibilityMode by remember { mutableStateOf(PostVisibilityMode.USER) }
    val snackBarHostState = remember { SnackbarHostState() }

    val bottomSheetViewModel: SharedBottomSheetViewModel = viewModel()
    val bottomSheetData = bottomSheetViewModel.bottomSheetState.collectAsState().value
    val modificationRequest = bottomSheetViewModel.modificationRequest.collectAsState().value
    val isAlertDialogVisible = remember { mutableStateOf(false) }

    val postId = navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("POST_ID")

    var postData by remember { mutableStateOf<GetPostDTO?>(null) }


    LaunchedEffect(postData) {
        postData?.let {
            replyViewModel.getReplies(
                postId = it.postId,
                campusId = it.campusId,
                feedMode = it.feedMode
            )
        }
    }

    LaunchedEffect(editPostState) {
        when (editPostState) {
            is UiState.Loading -> {
                isLoading = true
            }

            is UiState.Success -> {
                replyText = ""
                isLoading = false
                snackBarHostState.showSnackbar("Done!")
            }

            is UiState.Error -> {
                snackBarHostState.showSnackbar("Something went wrong!")
                isLoading = false
            }

            else -> {}
        }
    }

    LaunchedEffect(deleteReplyState.value) {
        when (deleteReplyState.value) {
            is UiState.Loading -> {
               bottomSheetViewModel.isLoading(true)
            }

            is UiState.Success -> {
                bottomSheetViewModel.isLoading(false)
                isAlertDialogVisible.value = false
                replyViewModel.removeReplyOnDelete(replyId = bottomSheetData.content.replyId)
            }

            is UiState.Error -> {
                bottomSheetViewModel.isLoading(false)
                snackBarHostState.showSnackbar("Something went wrong!")
            }

            else -> {}
        }
    }

    LaunchedEffect(createReplyState.value) {

        when (createReplyState.value) {
            is UiState.Loading -> {
                isLoading = true
                keyboard?.hide()
            }

            is UiState.Success -> {
                isLoading = false
                replyText = ""
            }

            is UiState.Error -> {
                isLoading = false
                snackBarHostState.showSnackbar("Something went wrong!")
            }

            else -> {}
        }
    }

    LaunchedEffect(deletePostState.value) {
        when (deletePostState.value) {

            is UiState.Loading -> {
                bottomSheetViewModel.isLoading(true)
            }
            is UiState.Success -> {
                bottomSheetViewModel.isLoading(false)
                isAlertDialogVisible.value = false
                navHostController.popBackStack()
            }
            is UiState.Error -> {
                bottomSheetViewModel.isLoading(false)
                snackBarHostState.showSnackbar("Something went wrong!")
            }
            else -> {}
        }

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reply") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Column {
                if (modificationRequest == "EDIT") {
                    Box(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("Edit reply", color = LightTheme_Blue, fontWeight = FontWeight.Bold)
                    }
                }

                Divider()

                BottomTextInput(
                    focusRequester = focusRequester,
                    onFocusChange = {
                        isFocused = it.isFocused
                    },
                    text = replyText,
                    onTextChange = {
                        replyText = it
                    },
                    onSubmitClick = {
                        if (replyText.isNotEmpty()) {
                            val docID = generateUID()
                            scope.launch {
                                keyboard?.hide()
                                postData?.let {
                                    replyViewModel.createReply(
                                        replyId = docID,
                                        postId = it.postId,
                                        content = replyText,
                                        postCreatorId = it.creatorDetail.profile?.id ?: "",
                                        visibilityMode = visibilityMode,
                                        user = UserDetail(
                                            id = userProfile?.id ?: "",
                                            userImage = userProfile?.userImage ?: "",
                                            userName = userProfile?.userName ?: "",
                                        ),
                                        mode = it.feedMode,
                                        campusId = it.campusId
                                    )
                                }
                            }
                        }
                    },
                    isLoading = isLoading,
                    userImage = userProfile?.userImage ?: "",
                    selectedVisibility = visibilityMode,
                    onVisibilityChange = {

                        visibilityMode = it
                        when(isConsentAgree){
                            is UiState.Success -> {
                                if (it == PostVisibilityMode.ANONYMOUS){
                                    if ((isConsentAgree as UiState.Success<Boolean>).data){
                                        visibilityMode = it
                                    }else{
                                        consentBottomSheet.value = true
                                        visibilityMode = PostVisibilityMode.USER
                                    }
                                }else{
                                    visibilityMode = it
                                }
                            }
                            else -> {}
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            when(globalPostState){
                is UiState.Success -> {
                    items(globalPostState.data.filter { it.postId == postId }) {
                        PostCard(
                            post = it,
                            handlers = defaultPostHandlers(
                                context = context,
                                post = it,
                                feedViewModel = postViewModel,
                                bottomSheetSharedViewModel = bottomSheetViewModel,
                                navController = navHostController,
                            )
                        )
                        postData = it
                    }
                }
                else -> {}

            }

            when(campusPostState){
                is UiState.Success -> {
                    items(campusPostState.data.filter { it.postId == postId }) {
                        PostCard(
                            post = it,
                            handlers = defaultPostHandlers(
                                context = context,
                                post = it,
                                feedViewModel = postViewModel,
                                bottomSheetSharedViewModel = bottomSheetViewModel,
                                navController = navHostController,
                            )
                        )
                        postData = it
                    }
                }
                else -> {}

            }
            // Replies Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Divider()
                    Text(text = "Replies", style = MaterialTheme.typography.titleLarge)
                    Divider()
                }
            }

            // Replies State Handling
            when (repliesState) {
                is UiState.Loading -> {
                    item {
                        LoadingUI(true)
                    }
                }

                is UiState.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Failed to load replies.")
                        }

                        LaunchedEffect(repliesState.message) {
                            scope.launch {
                                snackBarHostState.showSnackbar(
                                    repliesState.message
                                )
                            }
                        }
                    }
                }

                is UiState.Success -> {

                    val orderedReplies = repliesState.data.sortedByDescending { it.repliedAt }

                    if (orderedReplies.isEmpty()) {

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No replies yet.")
                            }
                        }

                    } else {

                        items(orderedReplies) { reply ->
                            ReplyWidget(
                                repliesDTO = reply,
                                navHostController = navHostController,
                                onLikeClick = {
                                    replyViewModel.likeReply(
                                        repliedById = reply.creatorDetail.profile?.id ?: "",
                                        postId = reply.postId,
                                        replyId = reply.replyId,
                                        isLiked = reply.actions.isLiked,
                                        campusId = postData?.campusId,
                                        feedMode = postData?.feedMode ?: FeedMode.GLOBAL
                                    )
                                },
                                onDotsClick = {
                                    postData?.let {
                                        bottomSheetViewModel.setBottomSheetState(
                                            state = true,
                                            isCurrentUser = reply.creatorDetail.isCurrentUser,
                                            content = Content(
                                                postId = reply.postId,
                                                text = reply.content,
                                                replyId = reply.replyId,
                                            ),
                                            contentType = ContentType.REPLY,
                                            sheetType = SheetType.MENU_LIST,
                                            feedMode = it.feedMode,
                                            campusId = it.campusId
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                UiState.Idle -> {
                    // Optional: No UI for idle
                }
            }


        }


        PostDotOptionBottomSheet(
            isBottomSheet = bottomSheetData.isBottomSheet,
            bottomSheetViewModel = bottomSheetViewModel,
            replyViewModel = replyViewModel,
            postFeedViewModel = postViewModel,
            onDismiss = {
                bottomSheetViewModel.dismissBottomSheet()
            },
            isCurrentUser = bottomSheetData.isCurrentUser,
            onDeleteClick = {
                bottomSheetViewModel.dismissBottomSheet()
                isAlertDialogVisible.value = true
            },
            onEditClick = {},
            onHideBottomSheet = {}
        )

        AlertDialogWidget(
            isVisible = isAlertDialogVisible.value,
            onDismiss = { isAlertDialogVisible.value = false },
            title = "Delete Post",
            description = "Are you sure you want to delete this post?",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel",
            onPositiveClick = {

                if (bottomSheetData.contentType == ContentType.POST) {
                    postViewModel.deletePost(
                        postId = bottomSheetData.content.postId,
                        isCampus = bottomSheetData.campusId,
                        feedMode = bottomSheetData.feedMode
                    )
                }

                if (bottomSheetData.contentType == ContentType.REPLY) {
                    replyViewModel.deleteReply(
                        postId = bottomSheetData.content.postId,
                        replyId = bottomSheetData.content.replyId,
                        campusId = bottomSheetData.campusId,
                        feedMode = bottomSheetData.feedMode
                    )
                }
                context.vibrate()

            },
            showLoading = bottomSheetViewModel.isLoading.collectAsState().value
        )

        ConsentBottomSheet(
            isVisible = consentBottomSheet.value,
            onDismiss = {
                consentBottomSheet.value = false
                visibilityMode = PostVisibilityMode.USER
            },
            onAgree = {
                consentAgreeViewModel.saveSwitchState(
                    true
                )
                consentBottomSheet.value = false
            }
        )

    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DragTopButton(
    modifier: Modifier = Modifier,
    onTrigger: () -> Unit
) {
    val context = LocalContext.current

    // 1. Get screen height in pixels to calculate 1/4 height drag threshold
    val configuration = LocalConfiguration.current
    with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val maxDrag = 200F // Max drag allowed (1/4th of screen)

    // 2. Remember the current drag offset (Y axis)
    val offsetY = remember { Animatable(0f) }

    // 3. CoroutineScope to launch animations
    val coroutineScope = rememberCoroutineScope()

    // 4. Calculate image size based on upward/downward drag
    val imageSize by remember {
        derivedStateOf {
            val dragProgress = (-offsetY.value / maxDrag).coerceIn(0f, 1f)
            // When dragged upward → grow from 0.dp to 60.dp
            // When dragged downward → shrink back
            lerp(0.dp, 60.dp, dragProgress)
        }
    }

    // 5. Main container to capture full screen gestures
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // 6. Detect vertical drag gestures
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        coroutineScope.launch {
                            // 7. Update offset — allow both upward and downward drag
                            val newOffset = (offsetY.value + dragAmount)
                                .coerceIn(
                                    -maxDrag,
                                    0f
                                ) // Only allow dragging upward max, and downward back to rest
                            offsetY.snapTo(newOffset)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            // 8. If fully dragged up to max threshold, trigger the action
                            if (-offsetY.value >= maxDrag) {
                                onTrigger()
                                context.vibrate()
                            }
                            // 9. Animate back to original position regardless
                            offsetY.animateTo(0f, animationSpec = spring())
                        }
                    }
                )
            },
        contentAlignment = Alignment.BottomCenter // 10. Align content at bottom of screen
    ) {
        // 11. Dragging indicator (invisible handle or layout box)
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Drag up",
            modifier = Modifier
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .size(imageSize)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}


enum class PostType { USER, ANONYMOUS }



@Composable
fun ReplyWidget(
    repliesDTO: GetRepliesDTO,
    navHostController: NavHostController,
    onLikeClick:()-> Unit,
    onDotsClick:()-> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
    ) {

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            CircleImage(
                image = repliesDTO.creatorDetail.profile?.userImage ?: "",
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
                onClick = {
                    if (repliesDTO.visibility == PostVisibilityMode.USER) {
                        navHostController.navigate(Routes.Main.ProfileByID.routes)
                            .apply {
                                navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                    "USER_ID",
                                    repliesDTO.creatorDetail.profile?.id
                                )
                            }
                    }
                }
            )

            Column {

                PostHeader(
                    user = repliesDTO.creatorDetail,
                    postedAt = getTimeAgo(repliesDTO.repliedAt)
                )

                PostBody(
                    postContent = PostContent(
                        postType = PostOptions.TEXT,
                        postData = PostData(
                            postText = repliesDTO.content
                        )
                    ),
                    onPollSelect = { },
                    onPostImageClick = {},
                    onBodyClick = {

                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    AnimatedLikeButton(
                        onLike = {onLikeClick.invoke()},
                        likesCount = repliesDTO.actions.likesCount,
                        isLiked = repliesDTO.actions.isLiked
                    )

                    Row (verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)){

                        Log.d("IS_EDITED",repliesDTO.edited.toString())
                        if (repliesDTO.edited){
                            Text("Edited", color = Black300, fontSize = 12.sp)
                        }

                        Icon(
                            modifier = Modifier
                                .rotate(180f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        onDotsClick.invoke()
                                    }
                                ),
                            painter = painterResource(R.drawable.baseline_more_vert_24),
                            contentDescription = "Dots",
                            tint =  MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomTextInput(
    focusRequester: FocusRequester,
    onFocusChange:(FocusState)->Unit,
    onTextChange:(String)-> Unit,
    selectedVisibility: PostVisibilityMode,
    onVisibilityChange:(PostVisibilityMode)-> Unit,
    text: String,
    onSubmitClick:()-> Unit,
    isLoading: Boolean,
    userImage: String
) {

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),contentAlignment = Alignment.CenterStart){

        Row (verticalAlignment = Alignment.CenterVertically){

            BasicTextField(
                value = text,
                onValueChange = { onTextChange.invoke(it) },
                modifier = Modifier
                    .weight(1f)
                    .height(IntrinsicSize.Min)
                    .padding(start = 52.dp) // Adjusted padding
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .imePadding()
                    .focusRequester(focusRequester)
                    .onFocusChanged { onFocusChange.invoke(it) }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = "Write.....",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )


            IconButton(
                onClick = {
                    onSubmitClick.invoke()
                }
            ) {
                if (isLoading) {
                    CircularLoading()
                } else {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(R.drawable.send_solid),
                        contentDescription = "Send",
                    )
                }
            }

        }


        VisibilityModeChanger(
            selectedVisibility = selectedVisibility,
            onVisibilityModeChange = {
                onVisibilityChange(it)
            },
            modifier = Modifier.size(42.dp),
            userImage = userImage
        )



    }


}

