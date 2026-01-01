package com.iota.campusX.Screens


import ConsentAgreeViewModel
import ConsentBottomSheet
import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.ReplyRequest
import com.iota.campusX.Feature.Post.data.model.ReplyResponse
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Reply.ReplyViewModel
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.UserProfileViewModel
import com.iota.campusX.Navigation.AppNavigator
import com.iota.campusX.Navigation.AppNavigatorImpl
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.HomeViewModel
import com.iota.campusX.Screens.Post.DataModel.ContentId
import com.iota.campusX.Screens.Post.DataModel.ContentType
import com.iota.campusX.Screens.Post.DataModel.FeedContent
import com.iota.campusX.Screens.Post.PostActions.PostAction
import com.iota.campusX.Screens.Post.PostActions.PostActionViewModel
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuState
import com.iota.campusX.Screens.Post.VisibilityModeChanger
import com.iota.campusX.Utils.CircularLoading
import com.iota.campusX.Utils.FirestoreIdGenerator
import com.iota.campusX.Utils.LoadingScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.AnonymousImage
import com.iota.campusX.ui.UIComponents.AppLabelText
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.FeedUI.FeedAction
import com.iota.campusX.ui.UIComponents.FeedUI.FeedHeader
import com.iota.campusX.ui.UIComponents.FeedUI.FeedItem
import kotlinx.coroutines.launch
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

data class MentionBuilder(
    val mentionUserName: String,
    val mentionedUserId: String,
    val parentId:Long,
    val postId:Long
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostReplyScreen(
    navHostController: NavHostController,
    profileViewModel: UserProfileViewModel,
    postViewModel: PostFeedViewModel = koinInject(),
    homeViewModel: HomeViewModel,
    replyViewModel: ReplyViewModel = koinInject()
) {

    val appNavigator: AppNavigator = remember { AppNavigatorImpl(navHostController) }
    //-----------------Pass parameter from outside------------
    val postActionsViewModel: PostActionViewModel = getKoin().get { parametersOf(appNavigator) }


    val postMenuState: PostMenuState = koinInject()

    val consentAgreeViewModel = koinInject<ConsentAgreeViewModel>()
    val isConsentAgree by consentAgreeViewModel.isAgree.collectAsState()
    val consentBottomSheet = remember { mutableStateOf(false) }


    val profileState = profileViewModel.userBaseProfile.collectAsState().value
    val postRepliesState = replyViewModel.postReplies.collectAsState().value

    var pickedImage by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> pickedImage = uri }

    val userProfile = when(profileState){
        is UiState.Success<*> -> {
            (profileState as UiState.Success<BaseProfileDTO>).data
        }
        else -> {
            null
        }
    }

    val createReplyState = replyViewModel.createReplyState.collectAsState()

    val singlePost = postViewModel.singlePost.collectAsState().value

    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val scope = rememberCoroutineScope()

    var replyText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var visibilityMode by remember { mutableStateOf(VisibilityMode.USER) }
    val snackBarHostState = remember { SnackbarHostState() }
    var mentionBuilder = remember { mutableStateOf<MentionBuilder?>(null) }


    val postId = navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("POST_ID")
    var postData by remember { mutableStateOf<GetPostDTO?>(null) }

    LaunchedEffect(postId) {
        postId.let {
            postViewModel.fetchSinglePost(it ?: "")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            postViewModel.clearSinglePost()
            replyViewModel.clearPostReplies()
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
                mentionBuilder.value = null
                pickedImage = null

            }

            is UiState.Error -> {
                isLoading = false
                snackBarHostState.showSnackbar("Something went wrong!")
            }

            else -> {}
        }
    }

    LaunchedEffect(mentionBuilder) {
        Log.d("MentionBuilder", mentionBuilder.value.toString())
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


            Column(
                modifier = Modifier.navigationBarsPadding().imePadding(),
            ){

                Column {

                    pickedImage?.let {


                        Box(
                            modifier = Modifier.height(150.dp).width(200.dp).padding(start = 12.dp)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)

                                ),
                            contentAlignment = Alignment.TopEnd,

                        ) {

                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(it)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            IconButton(onClick = {pickedImage = null}) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null
                                )
                            }

                        }


                    }

                    BottomTextInput(
                        focusRequester = focusRequester,
                        onFocusChange = {
                        },
                        text = replyText,
                        onTextChange = {
                            replyText = it
                        },
                        onSubmitClick = {
                            if (replyText.isNotEmpty()) {

                                val docID = FirestoreIdGenerator.generate()

                                scope.launch {

                                    keyboard?.hide()

                                    postData?.let {
                                        replyViewModel.createReply(
                                            replyRequest = ReplyRequest(
                                                postId = it.postId.toLong(),
                                                visibility = visibilityMode,
                                                text = replyText,
                                                mediaUrl = "",
                                                parentId = mentionBuilder.value?.parentId

                                            ),
                                            uploadImage = pickedImage,
                                            postDTO = it
                                        )
                                    }

                                }
                            }
                        },
                        isLoading = isLoading,
                        userImage = userProfile?.image ?: "",
                        selectedVisibility = visibilityMode,
                        onVisibilityChange = {
                            visibilityMode = it
                            when (isConsentAgree) {
                                is UiState.Success -> {
                                    if (it == VisibilityMode.ANONYMOUS) {
                                        if ((isConsentAgree as UiState.Success<Boolean>).data) {
                                            visibilityMode = it
                                        } else {
                                            consentBottomSheet.value = true
                                            visibilityMode = VisibilityMode.USER
                                        }
                                    } else {
                                        visibilityMode = it
                                    }
                                }

                                else -> {}
                            }
                        },
                        mentionBuilder = mentionBuilder.value,
                        onImagePick = {
                            launcher.launch("image/*")
                        }
                    )

                }

            }

        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { innerPadding ->

        when (singlePost) {

            is UiState.Loading -> {
                LoadingScreen()
            }

            is UiState.Success -> {

                postData = singlePost.data

                LaunchedEffect(Unit) {
                    postData?.let {
                        replyViewModel.fetchPostReplies(
                            postId = it.postId,
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding), // prevents double-inset,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {

                    postData?.let {
                        item {
                            FeedItem(
                                feedItem = it,
                                handlers = {
                                    postActionsViewModel.onAction(it)
                                },
                                onDotMenuClick = {
                                    postData?.let { data ->
                                        postMenuState.open(
                                            FeedContent(
                                                id = ContentId.Post(postId = data.postId),
                                                text = data.postContent.postText,
                                                isOwner = data.creatorDetail.isCurrentUser,
                                                type = ContentType.POST
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }

                    // Replies Header
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Divider()
                            Text(
                                modifier = Modifier.padding(start = 12.dp),
                                text = "Replies",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Divider()
                        }
                    }


                    when (postRepliesState) {
                        is UiState.Loading -> {
                            item {
                                LoadingScreen()
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

                                LaunchedEffect(postRepliesState.message) {
                                    scope.launch {
                                        snackBarHostState.showSnackbar(
                                            postRepliesState.message
                                        )
                                    }
                                }
                            }
                        }

                        is UiState.Success -> {

                            val orderedReplies = postRepliesState.data

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
                                    Column {
                                        ReplyWidget(
                                            repliesDTO = reply,
                                            onDotsClick = {
                                                postMenuState.open(
                                                    FeedContent(
                                                        id = ContentId.Reply(
                                                            postId = reply.postId.toString(),
                                                            replyId = reply.replyId.toString()
                                                        ),
                                                        text = reply.text,
                                                        isOwner = reply.author.isCurrentUser == true,
                                                        type = ContentType.REPLY
                                                    )
                                                )
                                            },
                                            handler = { action ->
                                                postActionsViewModel.onAction(action)
                                            },
                                            onReplyClick = {
                                                Log.d("MentionBuilder", it.toString())
                                                mentionBuilder.value = it
                                            },

                                        )
                                    }
                                }
                            }
                        }

                        UiState.Idle -> {
                            // Optional: No UI for idle
                        }
                    }
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp), contentAlignment = Alignment.Center
                ) {
                    AppLabelText(
                        text = "Content no longer available"
                    )
                }
            }

            else -> {}
        }

        ConsentBottomSheet(
            isVisible = consentBottomSheet.value,
            onDismiss = {
                consentBottomSheet.value = false
                visibilityMode = VisibilityMode.USER
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

@Composable
fun ReplyWidget(
    repliesDTO: ReplyResponse,
    handler: (PostAction) -> Unit,
    onDotsClick: () -> Unit,
    mentionedUser: MentionBuilder?=null,
    onReplyClick: (MentionBuilder?) -> Unit = { },
) {
    // Store column height dynamically (dp)
    var columnHeight by remember { mutableStateOf(0.dp) }

    val density = LocalDensity.current

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ---------------- LEFT COLUMN ----------------
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (repliesDTO.visibility == VisibilityMode.USER) {
                    CircleImage(
                        image = repliesDTO.author.authorImage ?: "",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape),
                        onClick = {
                            handler.invoke(
                                PostAction.OpenUserProfile(
                                    userId = repliesDTO.author.authorId,
                                    isCurrentUser = repliesDTO.author.isCurrentUser ?: false
                                )
                            )
                        },
                        visibility = repliesDTO.visibility
                    )
                } else {
                    AnonymousImage(modifier = Modifier.size(42.dp))
                }

                // 🟦 Dynamic Vertical Divider
                VerticalDivider(modifier = Modifier.height(columnHeight-42.dp), thickness = 0.5.dp,color = MaterialTheme.colorScheme.outline)

            }

            // ---------------- RIGHT COLUMN ----------------
            Column(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        // measure height in dp
                        columnHeight = with(density) {
                            coordinates.size.height.toDp()
                        }
                    }
            ) {
                // ---- Header ----
                FeedHeader(
                    creator = CreatorDetail(
                        profile = UserBasicDetail(
                            name = repliesDTO.author.authorName ?: "",
                            id = repliesDTO.author.authorId,
                            image = repliesDTO.author.authorImage ?: "",
                            tagline = ""
                        ),
                        isCurrentUser = repliesDTO.author.isCurrentUser ?: false,
                        isVerified = repliesDTO.author.isVerified ?: false
                    ),
                    feedMode = FeedMode.OPEN,
                    postedAt = getTimeAgo(repliesDTO.createdAt),
                    visibilityMode = repliesDTO.visibility,
                    trailingComponent = { }
                )

                // ---- Text ----

                Column() {

                    ReplyText(
                        text = repliesDTO.text,
                        mentionedUser = mentionedUser,
                        onMentionClick = {
                            handler(PostAction.OpenUserProfile(userId = it, isCurrentUser = repliesDTO.author.isCurrentUser?:false))
                        }
                    )

                    repliesDTO.mediaUrl?.let {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(repliesDTO.mediaUrl)
                                .build(),
                            contentDescription = "",
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                        )
                    }

                }



                // ---- Actions ----
                FeedAction(
                    likesCount = repliesDTO.actions.likesCount,
                    isLiked = repliesDTO.actions.isLiked,
                    onLikeClick = {
                        handler.invoke(
                            PostAction.Like(
                                isLiked = it,
                                contentId = ContentId.Reply(
                                    replyId = repliesDTO.replyId.toString(),
                                    postId = repliesDTO.postId.toString()
                                ),
                                userId = repliesDTO.author.authorId
                            )
                        )
                    },
                    onMoreVertClick = {
                        onDotsClick.invoke()
                    },
                    otherActionContent = {
                        ReplyButtonComponent(
                            replyCount = "10",
                            onReplyClick = {
                                onReplyClick.invoke(
                                    MentionBuilder(
                                        mentionUserName = repliesDTO.author.authorName ?: "",
                                        parentId = repliesDTO.replyId,
                                        postId = repliesDTO.postId,
                                        mentionedUserId = repliesDTO.author.authorId
                                    )
                                )
                            },
                            enableText = true
                        )
                    }
                )

                // ---- Recursive replies ----
                repliesDTO.children.forEach { child ->
                    Spacer(modifier = Modifier.height(12.dp))
                    ReplyWidget(
                        repliesDTO = child,
                        onDotsClick = onDotsClick,
                        handler = handler,
                        mentionedUser =  MentionBuilder(
                            mentionUserName = repliesDTO.author.authorName ?: "",
                            parentId = repliesDTO.replyId,
                            postId = repliesDTO.postId,
                            mentionedUserId = repliesDTO.author.authorId

                        ),
                        onReplyClick = {
                            onReplyClick.invoke(
                                MentionBuilder(
                                    mentionUserName = child.author.authorName ?: "",
                                    parentId = child.replyId,
                                    postId = child.postId,
                                    mentionedUserId = child.author.authorId
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomTextInput(
    focusRequester: FocusRequester,
    onFocusChange: (FocusState) -> Unit,
    onTextChange: (String) -> Unit,
    selectedVisibility: VisibilityMode,
    onVisibilityChange: (VisibilityMode) -> Unit,
    text: String,
    onSubmitClick: () -> Unit,
    isLoading: Boolean,
    userImage: String,
    mentionBuilder: MentionBuilder? = null,
    onImagePick:()->Unit
) {

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

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

                    Row (modifier = Modifier){

                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            if (mentionBuilder!= null){
                                focusRequester.requestFocus()
                                keyboardController?.show()
                                context.vibrate()
                                Text(
                                    text = "@${mentionBuilder.mentionUserName} ",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                innerTextField()
                            }
                            else{

                                Row {
                                    if (text.isEmpty()) {

                                        Text(
                                            text = "write a reply...",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        }

                        Icon(
                            modifier = Modifier.size(22.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        onImagePick.invoke()
                                    }
                                ),
                            painter = painterResource(R.drawable.image),
                            contentDescription = null
                        )
                    }

                }
            )


            IconButton(
                onClick = {
                    onSubmitClick.invoke()
                }
            ) {
                if (isLoading) {
                    CircularLoading(MaterialTheme.colorScheme.primary)
                } else {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(R.drawable.send_solid),
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
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
            userImage = userImage,
        )


    }


}


@Composable
fun ReplyButtonComponent(
    replyCount: String,
    onReplyClick:()-> Unit,
    enableText: Boolean
) {

    Row(verticalAlignment = Alignment.CenterVertically) {

        Icon(
            modifier = Modifier
                .size(16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {

                    }
                ),
            painter = painterResource( R.drawable.chatbubble_outline),
            contentDescription = "Like",
            tint = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = replyCount,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.width(12.dp))

        if (enableText){
            Text(
                modifier = Modifier.clickable(
                    onClick = {onReplyClick.invoke()},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
                text = "Reply",
                style = MaterialTheme.typography.labelMedium,
                textDecoration = TextDecoration.Underline
            )
        }



    }




}






@Composable
fun ReplyText(
    text: String,
    mentionedUser: MentionBuilder? = null,
    onMentionClick: (String) -> Unit = {}
) {
    val color = MaterialTheme.colorScheme.primary
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val annotatedText = remember(text, mentionedUser) {
        buildAnnotatedString {
            if (mentionedUser != null) {
                pushStringAnnotation(tag = "MENTION", annotation = mentionedUser.mentionUserName)
                withStyle(
                    style = SpanStyle(
                        color = color,
                    )
                ) {
                    append("@${mentionedUser.mentionUserName}")
                }
                pop()
                append(" ") // space after mention
            }
            append(text)
        }
    }

    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .padding(vertical = 12.dp)
            .then(
                Modifier.pointerInput(annotatedText) {
                    detectTapGestures { tapOffset ->
                        layoutResult?.let { layout ->
                            val position = layout.getOffsetForPosition(tapOffset)
                            annotatedText
                                .getStringAnnotations("MENTION", position, position)
                                .firstOrNull()
                                ?.let { annotation ->
                                    onMentionClick(annotation.item)
                                }
                        }
                    }
                }
            ),
        onTextLayout = { layoutResult = it }
    )
}

@Composable
fun RichCommentInput(
    onSendText: (String) -> Unit,
    onGifReceived: (Uri, String?) -> Unit
) {
    AndroidView(
        factory = { context ->
            RichInputEditText(context).apply {
                hint = "Write a comment..."
                imeOptions = EditorInfo.IME_ACTION_SEND
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                this.onGifReceived = onGifReceived
            }
        },
        modifier = Modifier.fillMaxWidth()

    )
}



class RichInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    // ✅ Must be a var (so Compose can assign it)
    var onGifReceived: ((Uri, String?) -> Unit)? = null

    override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection? {
        val ic = super.onCreateInputConnection(editorInfo)
        if (ic == null) return null

        // Tell the IME (keyboard) what MIME types this input accepts
        EditorInfoCompat.setContentMimeTypes(
            editorInfo,
            arrayOf("image/gif", "image/png", "image/webp")
        )

        // Wrap to intercept committed GIF/sticker data
        return InputConnectionCompat.createWrapper(
            ic,
            editorInfo,
            InputConnectionCompat.OnCommitContentListener { inputContentInfo, _, _ ->
                try {
                    val uri = inputContentInfo.contentUri
                    val mimeType = inputContentInfo.description.getMimeType(0)
                    inputContentInfo.requestPermission()
                    onGifReceived?.invoke(uri, mimeType)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        )
    }
}

@Composable
fun GifPreview(uri: Uri?) {
    uri?.let {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(it)
                .crossfade(true)
                .build(),
            contentDescription = "GIF or Sticker",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

@Composable
fun CommentSection() {
    var gifUri by remember { mutableStateOf<Uri?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        RichCommentInput(
            onSendText = { text -> Log.d("Comment", "Text: $text") },
            onGifReceived = { uri, mimeType ->
                Log.d("Comment", "GIF received: $uri, mimeType=$mimeType")
                gifUri = uri
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        GifPreview(gifUri)
    }
}
