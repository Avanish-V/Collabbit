package com.iota.campusX.Screens.Post

import ConsentAgreeViewModel
import ConsentBottomSheet
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.google.firebase.Timestamp
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
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
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
import com.iota.campusX.ui.UIComponents.SimpleDropDown
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.White
import com.iota.campusX.ui.theme.LightTheme_Blue
import com.iota.campusX.ui.theme.secondary
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
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
    val consentAgreeViewModel = koinInject<ConsentAgreeViewModel>()
    val poll by pollViewModel.poll.collectAsState()
    val pollState = postCreationViewModel.createPollUiState
    val postOption = postScreenViewModel.post.collectAsState().value
    val uploadProgress by postCreationViewModel.uploadingProgress.collectAsState()
    val isConsentAgree by consentAgreeViewModel.isAgree.collectAsState()
    val userProfileState = userProfileViewModel.userBaseProfile.collectAsState().value

    val userProfile = userProfileState as UiState.Success

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var selectedFeedMode by remember { mutableStateOf("") }
    var selectedPod by remember { mutableStateOf<Reference?>(null) }
    var isExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isBottomSheetVisible by remember { mutableStateOf(false) }
    var visibility by remember { mutableStateOf(PostVisibilityMode.USER) }
    var selectedImages by remember { mutableStateOf<Uri?>(null) }
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImages = uri }
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val feedMode = homeViewModel.mode.collectAsState().value



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
                val feedMode = feedMode as UiState.Success<FeedMode>
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
                            profile = UserDetail(
                                userName = userProfile.data.userName,
                                id = userProfile.data.id,
                                userImage = userProfile.data.userImage,
                                userBio = userProfile.data.userBio,
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
                        campusId = userProfile.data.campus?.campusCode,
                        postActions = PostActions(
                            isLiked = false,
                            likesCount = 0,
                            replies = emptyList(),
                            replyCount = 0,
                        ),
                        feedMode = feedMode.data
                    ),
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
                scope.launch {
                    snackbarHostState.showSnackbar((uploadProgress as UploadState.Error).message)
                }
            }

            is UploadState.Idle,
            is UploadState.Started -> {
                // Handle if needed
            }

            is UploadState.ImageUploadFailed -> {

            }
        }
    }

    LaunchedEffect(isConsentAgree) {
        when (isConsentAgree) {
            is UiState.Loading -> {

            }
            is UiState.Success -> {
                isBottomSheetVisible = false
            }
            else -> {

            }

        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .imePadding()
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



                Button(
                    modifier = Modifier.shadow(
                        ambientColor = LightTheme_Blue,
                        spotColor = LightTheme_Blue,
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
                                        createdAt = Timestamp.now(),
                                        creatorId = authViewModel.userId(),
                                        reference = null,
                                        postContent = PostContent(
                                            postType = postScreenViewModel.post.value,
                                            postData = PostData(
                                                poll = poll
                                            )
                                        ),
                                        campusId = null,
                                    )
                                )
                            }

                            else -> {

                                val feedMode = feedMode as UiState.Success<FeedMode>

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
                                        createdAt = Timestamp.now(),
                                        creatorId = userProfile.data.id,
                                        reference = selectedPod,
                                        postContent = PostContent(
                                            postType = postScreenViewModel.post.value,
                                            postData = PostData(
                                                postText = text,
                                            )
                                        ),
                                        campusId =  userProfile.data.campus?.campusCode,
                                        feedMode = feedMode.data
                                    ),
                                    imageUri = selectedImages,
                                    user = UserDetail(
                                        userName = userProfile.data.userName,
                                        id = userProfile.data.id,
                                        userImage = userProfile.data.userImage,
                                        userBio = userProfile.data.userBio,
                                    ),
                                    navHostController =  navHostController,
                                    postFeedViewModel = feedViewModel
                                )
                            }
                        }

                    },
                    enabled = true

                ) {

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(text = "Post")
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
    ) { innerPadding ->

        val feedMode = feedMode as UiState.Success<FeedMode>

        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            item {

                Row (verticalAlignment = Alignment.CenterVertically){
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Visibility", style = MaterialTheme.typography.headlineMedium)
                        Text(text = "This post will be visible to all campuses.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    SimpleDropDown(
                        modifier = Modifier.weight(1f),
                        fieldOptions = listOf("Campus","Global"),
                        currentMode = feedMode.data,
                        selectedField = selectedFeedMode,
                        onFieldChange = {
                            selectedFeedMode = it
                            val newMode = if (it == "Campus") FeedMode.CAMPUS else FeedMode.GLOBAL
                            homeViewModel.saveSwitchState(newMode)
                            context.vibrate()
                        },
                        label = "Visibility"
                    )
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
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .padding(12.dp),
                                    value = poll.question,
                                    onValueChange = {
                                        pollViewModel.updatePollQuestion(it)
                                    },
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
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
                                                                tint = LightTheme_Blue
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
                                    cursorBrush = Brush.verticalGradient(listOf(LightTheme_Blue, LightTheme_Blue))
                                )
                            }
                        }
                    }

                    else -> {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                VisibilityModeChanger(
                                    modifier = Modifier.size(38.dp),
                                    selectedVisibility = visibility,
                                    onVisibilityModeChange = {

                                        when(isConsentAgree){
                                            is UiState.Success -> {
                                                if (it == PostVisibilityMode.ANONYMOUS){
                                                    if ((isConsentAgree as UiState.Success<Boolean>).data){
                                                        visibility = it
                                                    }else{
                                                        isBottomSheetVisible = true
                                                        visibility = PostVisibilityMode.USER
                                                    }
                                                }else{
                                                    visibility = it
                                                }
                                            }
                                            else -> {}
                                        }

                                    },
                                    userImage = userProfile.data.userImage,

                                )

//                                AnimatedVisibility(visible = true) {
//                                    ExposedDropdownMenuBox(
//                                        modifier = Modifier,
//                                        expanded = false,
//                                        onExpandedChange = {}
//
//                                    ) {
//
//                                        Row(
//                                            modifier = Modifier
//                                                .border(
//                                                    width = 1.dp,
//                                                    color = White400,
//                                                    shape = RoundedCornerShape(10.dp)
//                                                )
//                                                .padding(4.dp)
//                                                .clickable(
//                                                    onClick = {
//                                                        isExpanded = true
//                                                    },
//                                                    indication = null,
//                                                    interactionSource = remember { MutableInteractionSource() }
//                                                ),
//                                            verticalAlignment = Alignment.CenterVertically,
//                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
//                                        ) {
//                                            selectedPod?.let {
//
//                                                AsyncImage(
//                                                    modifier = Modifier
//                                                        .size(42.dp)
//                                                        .clip(CircleShape),
//                                                    model = it.icon,
//                                                    contentDescription = null,
//                                                    contentScale = ContentScale.Crop
//                                                )
//
//                                            }
//
//                                            Text(
//                                                text = selectedPod?.title ?: "Select Pod",
//                                                color = LightTheme_Black
//                                            )
//
//                                            IconButton(
//                                                onClick = {
//                                                    if (selectedPod?.title.isNullOrEmpty()) {
//                                                        isExpanded = !isExpanded
//                                                    } else {
//                                                        selectedPod = null
//                                                    }
//                                                }
//                                            ) {
//                                                if (selectedPod?.title.isNullOrEmpty()) {
//                                                    ExposedDropdownMenuDefaults.TrailingIcon(
//                                                        expanded = isExpanded
//                                                    )
//                                                } else {
//                                                    Icon(
//                                                        imageVector = Icons.Default.Close,
//                                                        contentDescription = "Close"
//                                                    )
//                                                }
//
//                                            }
//
//                                        }
//
//                                        DropdownMenu(
//                                            modifier = Modifier
//                                                .wrapContentWidth()
//                                                .background(color = LightTheme_White),
//                                            shape = RoundedCornerShape(5.dp),
//                                            shadowElevation = 0.dp,
//                                            expanded = isExpanded,
//                                            onDismissRequest = { isExpanded = false }
//                                        ) {
//                                            podListItems.forEach {
//                                                DropdownMenuItem(
//                                                    modifier = Modifier.padding(vertical = 5.dp),
//                                                    text = { Text(text = it.title) },
//                                                    leadingIcon = {
//                                                        AsyncImage(
//                                                            modifier = Modifier
//                                                                .size(42.dp)
//                                                                .clip(CircleShape),
//                                                            model = it.icon,
//                                                            contentDescription = null,
//                                                            contentScale = ContentScale.Crop
//                                                        )
//                                                    },
//                                                    onClick = {
//                                                        selectedPod = Reference(
//                                                            title = it.title,
//                                                            icon = it.icon
//                                                        )
//                                                        isExpanded = false
//                                                    }
//                                                )
//                                            }
//                                        }
//                                    }
//                                }


                            }

                            BasicTextField(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                value = text,
                                onValueChange = { text = it },
                                textStyle = LocalTextStyle.current.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                ),
                                decorationBox = {
                                    if (text.isEmpty()) {
                                        Text(
                                            text = "What's on your mind?",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column {

                                        it()

                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Text(
                                                text = "${text.count()}/500",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.labelMedium
                                            )
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
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .padding(12.dp),
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
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            )
                        }

                    }
                }
            }
        }

        ConsentBottomSheet(
            isVisible = isBottomSheetVisible,
            onDismiss = {
                isBottomSheetVisible = false
                visibility = PostVisibilityMode.USER
            },
            onAgree = {
                consentAgreeViewModel.saveSwitchState(
                    true
                )
                isBottomSheetVisible = false
            }
        )


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

@Composable
fun VisibilityModeChanger(
    modifier: Modifier = Modifier,
    selectedVisibility: PostVisibilityMode,
    onVisibilityModeChange: (PostVisibilityMode) -> Unit,
    userImage: String
) {
    val context = LocalContext.current
    val offsetX = remember { Animatable(0f) }
    val threshold = 200f
    val coroutineScope = rememberCoroutineScope()

    // Reset animation when visibility changes
    LaunchedEffect(selectedVisibility) {
        offsetX.snapTo(0f)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .clip(CircleShape)
                .background(Color.Gray)
                .pointerInput(selectedVisibility) { // Add selectedVisibility as key
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            coroutineScope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount)
                            }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                if (abs(offsetX.value) > threshold) {
                                    // Always toggle to opposite state
                                    val newMode = when (selectedVisibility) {
                                        PostVisibilityMode.USER -> PostVisibilityMode.ANONYMOUS
                                        PostVisibilityMode.ANONYMOUS -> PostVisibilityMode.USER
                                    }
                                    onVisibilityModeChange(newMode)
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
                model = if (selectedVisibility == PostVisibilityMode.USER) userImage else R.drawable.incognoto,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }
    }
}

