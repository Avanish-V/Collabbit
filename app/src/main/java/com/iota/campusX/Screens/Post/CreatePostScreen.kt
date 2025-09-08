package com.iota.campusX.Screens.Post

//import com.iota.campusX.ui.theme.secondary
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Post.data.model.MediaType
import com.iota.campusX.Feature.Post.presentation.PostCreationViewModel
import com.iota.campusX.Feature.Post.data.model.Type
import com.iota.campusX.Feature.Post.presentation.UploadState
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.HomeViewModel
import com.iota.campusX.Utils.CustomTextField
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.AnimatedStatus
import com.iota.campusX.ui.UIComponents.AnonymousImage
import com.iota.campusX.ui.UIComponents.AppLabelText
import com.iota.campusX.ui.UIComponents.CircularLoading
import com.iota.campusX.ui.UIComponents.IconButtonWidget
import com.iota.campusX.ui.UIComponents.SimpleDropDown
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.LightTheme_Blue
import com.iota.campusX.ui.theme.White
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
    homeViewModel: HomeViewModel,
) {
    val postScreenViewModel = koinInject<PostScreenViewModel>()
    val pollViewModel = koinViewModel<PollViewModel>()
    val consentAgreeViewModel = koinInject<ConsentAgreeViewModel>()


    val poll by pollViewModel.poll.collectAsState()
    val currentMode = postScreenViewModel.currentMode.collectAsState().value
    val uploadState by postCreationViewModel.uploadState.collectAsState()
    val isConsentAgree by consentAgreeViewModel.isAgree.collectAsState()
    val profileState = userProfileViewModel.userBaseProfile.collectAsState().value
    val feedModeState = homeViewModel.mode.collectAsState().value

    val userProfile = when(profileState){
        is UiState.Success<*> -> {
            (profileState as UiState.Success<BaseProfileDTO>).data
        }
        else -> {
            null
        }
    }

    val feedMode = (feedModeState as? UiState.Success)?.data


    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var selectedFeedMode by remember { mutableStateOf("") }
    var isBottomSheetVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var visibility by remember { mutableStateOf(VisibilityMode.USER) }

    var selectedImages by remember { mutableStateOf<Uri?>(null) }
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImages = uri }
    )

    val keyboardController = LocalSoftwareKeyboardController.current

    val snackbarHostState = remember { SnackbarHostState() }


    val navBackStackEntry = remember { navHostController.currentBackStackEntryFlow }.collectAsState(initial = null).value

    LaunchedEffect(navBackStackEntry) {
        text = ""
        selectedImages = null
    }

    LaunchedEffect(uploadState) {
        when(uploadState){
            is UploadState.Loading -> {
                isLoading = true
            }
            is UploadState.Success -> {
                navHostController.popBackStack()
            }
            is UploadState.Error -> {
                isLoading = false
                snackbarHostState.showSnackbar((uploadState as UploadState.Error).message)
            }

            else -> {}
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

    LaunchedEffect(selectedImages) {
        selectedImages?.let {
            postScreenViewModel.chooseOption(CreatePostMode.Media)
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
                actions = {
                    feedMode?.let {
                        SimpleDropDown(
                            modifier = Modifier.padding(end = 12.dp),
                            fieldOptions = listOf("Campus", "Global"),
                            currentMode = it,
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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            BottomBarComponent(
                onImageClick = {
                    if (currentMode == CreatePostMode.Poll) return@BottomBarComponent
                    singlePhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onPollClick = {
                    pollViewModel.createPoll("")
                    postScreenViewModel.chooseOption(CreatePostMode.Poll)
                },
                onPostClick = {

                    keyboardController?.hide()

                    val postType = when(currentMode){

                        is CreatePostMode.Poll -> {

                            feedMode?.let {
                                postCreationViewModel.createPoll(
                                    creatorId = userProfile?.id ?: "",
                                    campusId = userProfile?.campus?.campusCode ?: "",
                                    feedMode = it,
                                    visibility = visibility,
                                    type = Type.Poll,
                                    poll = poll
                                )
                            }
                        }
                        else -> {
                            feedMode?.let {
                                postCreationViewModel.createMediaPost(
                                    creatorId = userProfile?.id ?: "",
                                    campusId = userProfile?.campus?.campusCode ?: "",
                                    feedMode = it,
                                    visibility = visibility,
                                    imageUri = selectedImages,
                                    postText = text,
                                    type = Type.Media,
                                    mediaType = MediaType.Image
                                )
                            }
                        }
                    }

                    postType?.let { postCreationViewModel.uploadPost(it) }

                },
                isLoading = isLoading,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(snackbarData = it)
            }
        },

    ) { innerPadding ->


        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            item {

                Column(
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Visibility", style = MaterialTheme.typography.titleMedium)
                    AppLabelText(text = if (feedMode?.name == FeedMode.GLOBAL.name) "This post will be visible to all campuses." else "Only campus users can see.",)
                }
            }

            item {
                VisibilityModeChanger(
                    modifier = Modifier.size(38.dp),
                    selectedVisibility = visibility,
                    onVisibilityModeChange = {

                        when(isConsentAgree){
                            is UiState.Success -> {
                                if (it == VisibilityMode.ANONYMOUS){
                                    if ((isConsentAgree as UiState.Success<Boolean>).data){
                                        visibility = it
                                    }else{
                                        isBottomSheetVisible = true
                                        visibility = VisibilityMode.USER
                                    }
                                }else{
                                    visibility = it
                                }
                            }
                            else -> {}
                        }

                    },
                    userImage = userProfile?.userImage ?: "",
                )

            }

            item {

                when (currentMode) {

                    is CreatePostMode.Text->{

                        Column {
                            InputBox(
                                text = text,
                                onValueChange = { text = it },
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "${text.count()}/1000",
                                    color = if (text.count() > 1000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }

                    is CreatePostMode.Media -> {

                        InputBox(
                            text = text,
                            onValueChange = { text = it },
                        ) {
                            if (selectedImages != null){
                                Spacer(modifier = Modifier.height(12.dp))
                            }
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
                                                CreatePostMode.Text
                                            )
                                        },
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

                    is CreatePostMode.Poll -> {

                        Column(horizontalAlignment = Alignment.End) {

                            IconButton(onClick = {
                                postScreenViewModel.chooseOption(CreatePostMode.Text)
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
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                decorationBox = {

                                    if (poll.question.isEmpty()) {

                                        Text(
                                            text = "Write your question here.",
                                            color = MaterialTheme.colorScheme.outline
                                        )

                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                                        it()

                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Text(
                                                text = "${poll.options.count()}/150",
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        poll.options.forEachIndexed { index, pollOption ->
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
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                                keyboardActions = KeyboardActions(onDone = {}),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        if (poll.options.count() != 4) {
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
                visibility = VisibilityMode.USER
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


@Composable
fun VisibilityModeChanger(
    modifier: Modifier = Modifier,
    selectedVisibility: VisibilityMode,
    onVisibilityModeChange: (VisibilityMode) -> Unit,
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
                .fillMaxWidth()
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
                                        VisibilityMode.USER -> VisibilityMode.ANONYMOUS
                                        VisibilityMode.ANONYMOUS -> VisibilityMode.USER
                                    }
                                    onVisibilityModeChange(newMode)
                                    context.vibrate()

                                }
                                offsetX.animateTo(0f, animationSpec = spring())
                            }
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            if (selectedVisibility == VisibilityMode.USER){
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = userImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
            else{
                AnonymousImage(
                    modifier = Modifier.size(42.dp)
                )
            }

            AnimatedStatus(
                modifier = Modifier.height(48.dp),
                file = R.raw.swipe_right,
                description = "SWIPE_RIGHT"
            )
        }


    }
}


@Composable
fun BottomBarComponent(
    onImageClick: () -> Unit,
    onPollClick: () -> Unit,
    onPostClick: () -> Unit,
    isLoading: Boolean,
    ) {

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
                    onImageClick.invoke()
                },
                icon = R.drawable.image,
                description = "Select image",
                modifier = Modifier,
                enabled = true,
            )

            IconButtonWidget(
                modifier = Modifier.rotate(360f),
                onClick = {
                    onPollClick.invoke()
                }, icon = R.drawable.graph,
                description = "Select image",
                enabled = true
            )

        }



        Button(
            onClick = {
                onPostClick.invoke()
            },
            enabled = true,
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White
            )

        ) {
            if (isLoading){
                CircularLoading()
            }
            else{
                Text(text = "Post")
            }
        }
    }

}



@Composable
fun InputBox(
    text: String,
    onValueChange: (String) -> Unit,
    mediaContent: @Composable ()  -> Unit = {},
) {


    BasicTextField(
        modifier = Modifier.fillMaxWidth()
            .defaultMinSize(minHeight = 100.dp),
        value = text,
        onValueChange = { onValueChange(it) },
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        ),
        maxLines = 6,
        decorationBox = {

            if (text.isEmpty()) {
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "What's on your mind?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
            Column(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(12.dp)
            ) {

                it()

                mediaContent()

            }

        },
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    )
}