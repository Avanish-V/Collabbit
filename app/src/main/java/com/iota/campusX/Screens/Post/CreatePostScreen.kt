package com.iota.campusX.Screens.Post

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.iota.campusX.Feature.Post.domain.Models.CreatePostDTO
import com.iota.campusX.Feature.Post.domain.Models.CreatorDetail
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.Models.PostActions
import com.iota.campusX.Feature.Post.domain.Models.PostContent
import com.iota.campusX.Feature.Post.domain.Models.PostData
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.Models.Reference
import com.iota.campusX.Feature.Post.domain.Models.User
import com.iota.campusX.Feature.Post.presentation.PostCreationViewModel
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.UploadState
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.HomeViewModel
import com.iota.campusX.Utils.CustomTextField
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.generateUID
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.IconButtonWidget
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.background
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.secondary
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navHostController: NavHostController,
    userProfileViewModel: UserProfileViewModel,
    postCreationViewModel: PostCreationViewModel,
    feedViewModel: PostFeedViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
) {
    val postScreenViewModel = koinInject<PostScreenViewModel>()
    val pollViewModel = koinViewModel<PollViewModel>()
    val poll by pollViewModel.poll.collectAsState()
    val pollState = postCreationViewModel.createPollUiState
    val postOption = postScreenViewModel.post.collectAsState().value
    val uploadProgress by postCreationViewModel.uploadingProgress.collectAsState()
    val userProfile = userProfileViewModel.userBaseProfile.collectAsState().value.baseProfileData

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var selectedPod by remember { mutableStateOf<Reference?>(null) }
    var isExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var visibility by remember { mutableStateOf(PostVisibilityMode.USER) }
    var selectedImages by remember { mutableStateOf<Uri?>(null) }
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImages = uri }
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val feedMode = homeViewModel.switchState.collectAsState().value

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val navBackStackEntry = remember { navHostController.currentBackStackEntryFlow }.collectAsState(initial = null).value

    LaunchedEffect(navBackStackEntry) {
        text = ""
        selectedPod = null
        selectedImages = null
    }

    LaunchedEffect(postOption) {
        if (selectedImages != null) null
        if (text.isNotEmpty()) ""
    }

    LaunchedEffect(pollState) {

        when (pollState) {
            is UiState.Loading -> {
                isLoading = true
            }

            is UiState.Success -> {
                isLoading = false
                feedViewModel.updatePostLocally(
                    getPostDTO = GetPostDTO(
                        postId = generateUID(),
                        visibilityMode = visibility,
                        createdAt = getTimeMillis(),
                        reference = Reference(
                            title = selectedPod?.title ?: "",
                            icon = selectedPod?.icon ?: ""
                        ),
                        creatorDetail = CreatorDetail(
                            profile = User(
                                userName = userProfile.userName,
                                id = userProfile.id,
                                userImage = userProfile.userImage,
                                userBio = userProfile.userBio,
                            )
                        ),
                        postContent = PostContent(
                            postType = postScreenViewModel.post.value,
                            postData = PostData(
                                postText = text,
                                postImage = selectedImages.toString(),
                                poll = poll
                            )
                        ),
                        campusId = userProfile.campus?.campusCode,
                        postActions = PostActions(
                            isLiked = false,
                            likesCount = 0,
                            replies = emptyList(),
                            replyCount = 0,
                        ),
                    ),
                    feedMode = FeedMode.GLOBAL
                )
                navHostController.popBackStack()
            }

            is UiState.Error -> {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar(pollState.message)
                }
            }

            else -> {}
        }
    }

    LaunchedEffect(uploadProgress) {
        when (uploadProgress) {
            is UploadState.Loading -> {
                isLoading = true
            }

            is UploadState.Progress -> {
                isLoading = true
            }

            is UploadState.Success -> {
                isLoading = false
            }

            is UploadState.Error -> {
                isLoading = false
                // Optionally show error to user
            }

            is UploadState.Idle,
            is UploadState.Started -> {
                // Handle if needed
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {

                    Text(
                        modifier = Modifier.padding(end = 16.dp),
                        text = if (feedMode.savedIndex == 0) "Campus Mode" else "Global Mode",
                        style = MaterialTheme.typography.titleMedium
                    )

                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .imePadding()
                    .background(White900)
                    .padding(16.dp)
            ) {

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    IconButtonWidget(
                        onClick = {
                            postScreenViewModel.chooseOption(PostOptions.IMAGE)
                            singlePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        icon = R.drawable.image,
                        description = "Select image",
                        enabled = postOption == PostOptions.TEXT || postOption == PostOptions.IMAGE
                    )

                    IconButtonWidget(
                        modifier = Modifier.rotate(360f),
                        onClick = {
                            postScreenViewModel.chooseOption(PostOptions.POLL)
                            pollViewModel.createPoll("")
                        }, icon = R.drawable.graph,
                        description = "Select image",
                        enabled = true
                    )

                }
                Log.d("Posts", "CreatePostScreen: $postOption")

                Button(
                    modifier = Modifier.shadow(
                        ambientColor = primary,
                        spotColor = primary,
                        elevation = 20.dp,
                    ),
                    onClick = {

                        focusManager.clearFocus()
                        keyboardController?.hide()

                        when (postOption) {

                            PostOptions.POLL -> {

                                if (poll == null) return@Button

                                if (poll?.question.isNullOrEmpty()) {
                                    return@Button
                                }

                                poll?.options?.map {
                                    if (it.text.isEmpty()) {
                                        return@Button
                                    }
                                }


                                postCreationViewModel.createPoll(
                                    CreatePostDTO(
                                        postId = generateUID(),
                                        visibilityMode = PostVisibilityMode.USER,
                                        createdAt = getTimeMillis(),
                                        creatorId = authViewModel.userId(),
                                        reference = Reference(
                                            title = "Poll",
                                            icon = "https://cdn-icons-png.flaticon.com/128/741/741867.png"
                                        ),
                                        postContent = PostContent(
                                            postType = postScreenViewModel.post.value,
                                            postData = PostData(
                                                poll = poll
                                            )
                                        ),
                                        campusId = null,
                                        postActions = PostActions(
                                            isLiked = false,
                                            likesCount = 0,
                                            replies = emptyList(),
                                            replyCount = 0,
                                        )
                                    )
                                )
                            }

                            else -> {

                                if (selectedPod == null) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Select Pod")
                                    }
                                    return@Button
                                }

                                if (selectedImages == null && text.isEmpty()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Write something or select image")
                                    }
                                    return@Button
                                }

                                val postId = generateUID()

                                postCreationViewModel.createPost(
                                    dto = CreatePostDTO(
                                        postId = postId,
                                        visibilityMode = visibility,
                                        createdAt = getTimeMillis(),
                                        creatorId = userProfile.id,
                                        reference = selectedPod!!,
                                        postContent = PostContent(
                                            postType = postScreenViewModel.post.value,
                                            postData = PostData(
                                                postText = text,
                                            )
                                        ),
                                        campusId = if (feedMode.savedIndex == 1) userProfile.campus?.campusCode else null,
                                    ),
                                    feedMode = FeedMode.GLOBAL,
                                    imageUri = selectedImages,
                                    user = User(
                                        userName = userProfile.userName,
                                        id = userProfile.id,
                                        userImage = userProfile.userImage,
                                        userBio = userProfile.userBio,
                                    ),
                                    navHostController
                                )
                            }
                        }

                    },
                    enabled = true

                ) {

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = White900,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(text = "Post")
                            Icon(
                                painter = painterResource(R.drawable.send_2),
                                contentDescription = "Select image"
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(snackbarData = it)
            }
        },
        containerColor = secondary
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            item {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VanishModeButton(
                        selectedMode = {
                            visibility = it
                        },
                        userImage = userProfile.userImage
                    )
                }
            }

            item {

                AnimatedVisibility(visible = true) {
                    ExposedDropdownMenuBox(
                        modifier = Modifier,
                        expanded = false,
                        onExpandedChange = {}

                    ) {

                        Row(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = White400,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp)
                                .clickable(
                                    onClick = {
                                        isExpanded = true
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            selectedPod?.let {
                                AsyncImage(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape),
                                    model = it.icon,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(
                                text = selectedPod?.title ?: "Select Pod",
                                color = Black900
                            )

                            IconButton(
                                onClick = {
                                    if (selectedPod?.title.isNullOrEmpty()) {
                                        isExpanded = !isExpanded
                                    } else {
                                        selectedPod = null
                                    }
                                }
                            ) {
                                if (selectedPod?.title.isNullOrEmpty()) {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = isExpanded
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close"
                                    )
                                }

                            }

                        }

                        DropdownMenu(
                            modifier = Modifier
                                .wrapContentWidth()
                                .background(color = background),
                            shape = RoundedCornerShape(5.dp),
                            shadowElevation = 0.dp,
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false }
                        ) {
                            podListItems.forEach {
                                DropdownMenuItem(
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    text = { Text(text = it.title) },
                                    leadingIcon = {
                                        AsyncImage(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape),
                                            model = it.icon,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop
                                        )
                                    },
                                    onClick = {
                                        selectedPod = Reference(
                                            title = it.title,
                                            icon = it.icon
                                        )
                                        isExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

            }

            item {

                when (postOption) {

                    PostOptions.POLL -> {

                        poll?.let { poll ->

                            Column(horizontalAlignment = Alignment.End) {

                                IconButton(onClick = {
                                    postScreenViewModel.chooseOption(PostOptions.TEXT)
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.trash),
                                        contentDescription = "Add Option",
                                        tint = Color.Red
                                    )
                                }

                                BasicTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = White400,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .padding(12.dp),
                                    value = poll.question,
                                    onValueChange = {
                                        pollViewModel.updatePollQuestion(it)
                                    },
                                    textStyle = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Black900
                                    ),
                                    decorationBox = {

                                        if (poll.question.isEmpty()) {

                                            Text(
                                                text = "Write your question here.",
                                                color = Black300
                                            )

                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                                            it()

                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Text(
                                                    text = "${poll.options?.count()}/150",
                                                    color = Black300
                                                )
                                            }

                                            poll.options?.forEachIndexed { index, pollOption ->
                                                CustomTextField(
                                                    value = pollOption.text,
                                                    onValueChange = {
                                                        pollViewModel.updatePollOptionText(
                                                            pollOption.optionId,
                                                            it.toString()
                                                        )
                                                    },
                                                    label = "",
                                                    enabled = true,
                                                    placeHolder = pollOption.label,
                                                    trailingIcon = {
                                                        IconButton(onClick = {
                                                            pollViewModel.removePollOption(
                                                                pollOption.optionId
                                                            )
                                                        }) {
                                                            Icon(
                                                                imageVector = Icons.Default.Clear,
                                                                contentDescription = "Add Option",
                                                                tint = primary
                                                            )
                                                        }
                                                    },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                                    keyboardActions = KeyboardActions(onDone = {}),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }

                                            if (poll.options?.count() != 4) {
                                                TextButton(onClick = { pollViewModel.addPollOption() }) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            5.dp
                                                        )
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = null
                                                        )
                                                        Text(text = "Add Option")

                                                    }
                                                }
                                            }
                                        }

                                    },
                                    cursorBrush = Brush.verticalGradient(listOf(primary, primary))
                                )
                            }
                        }
                    }

                    else -> {
                        BasicTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = White400,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp),
                            value = text,
                            onValueChange = { text = it },
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Black900
                            ),
                            decorationBox = {
                                if (text.isEmpty()) {
                                    Text(
                                        text = "What's on your mind?",
                                        color = Black300
                                    )
                                }
                                Column {

                                    it()

                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(text = "${text.count()}/500", color = Black300)
                                    }

                                    if (postOption == PostOptions.IMAGE) {
                                        if (selectedImages != null) {
                                            AnimatedVisibility(visible = true) {
                                                Box(contentAlignment = Alignment.TopEnd) {
                                                    AsyncImage(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(5.dp))
                                                            .fillMaxWidth()
                                                            .height(250.dp),
                                                        model = selectedImages,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop
                                                    )

                                                    IconButton(
                                                        modifier = Modifier.size(24.dp),
                                                        onClick = {
                                                            selectedImages = null
                                                            postScreenViewModel.chooseOption(
                                                                PostOptions.TEXT
                                                            )
                                                        },
                                                        colors = IconButtonDefaults.iconButtonColors(
                                                            containerColor = secondary
                                                        )
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Delete Image",

                                                            )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            },
                            cursorBrush = Brush.verticalGradient(listOf(primary, primary))

                        )
                    }
                }
            }
        }
    }
}


val podListItems = listOf<Reference>(
    Reference(
        title = "Love",
        icon = "https://cdn-icons-png.flaticon.com/128/3670/3670159.png"
    ),
    Reference(
        title = "Meme",
        icon = "https://cdn-icons-png.flaticon.com/128/742/742920.png"
    ),
    Reference(
        title = "Search",
        icon = "https://cdn-icons-png.flaticon.com/128/200/200941.png"
    ), Reference(
        title = "News",
        icon = "https://cdn-icons-png.flaticon.com/128/741/741867.png"
    ), Reference(
        title = "Hiring",
        icon = "https://cdn-icons-png.flaticon.com/128/14946/14946635.png"
    )

)

enum class PostVisibilityMode { USER, ANONYMOUS }

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VanishModeButton(
    modifier: Modifier = Modifier,
    selectedMode: (PostVisibilityMode) -> Unit,
    userImage: String
) {

    var mode by remember { mutableStateOf(PostVisibilityMode.USER) }

    LaunchedEffect(mode) {
        when (mode) {
            PostVisibilityMode.USER -> {
                selectedMode(mode)
            }

            PostVisibilityMode.ANONYMOUS -> {
                selectedMode(mode)
            }
        }
    }

    val context = LocalContext.current
    val offsetX = remember { Animatable(0f) }
    val threshold = 200f // Distance to trigger vanish mode horizontally
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {

        Text(
            text = "Swipe right to change visibility",
            color = Black300,
            modifier = Modifier
                .padding(start = 85.dp)
                .alpha(100 / offsetX.value)
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(62.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            if (dragAmount > 0) { // Only allow dragging to the right
                                coroutineScope.launch {
                                    val newOffset = offsetX.value + dragAmount
                                    offsetX.snapTo(newOffset)
                                }
                            }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                if (offsetX.value > threshold) {
                                    if (mode == PostVisibilityMode.USER) {
                                        mode = PostVisibilityMode.ANONYMOUS
                                    } else {
                                        mode = PostVisibilityMode.USER
                                    }
                                    context.vibrate()
                                }
                                offsetX.animateTo(0f, animationSpec = spring())
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = if (mode == PostVisibilityMode.USER) userImage else R.drawable.incognoto,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
}
