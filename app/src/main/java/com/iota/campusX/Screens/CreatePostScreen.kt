package com.iota.campusX.Screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.iota.campusX.Feature.Post.domain.CreatePostDTO
import com.iota.campusX.Feature.Post.domain.CreatorDetail
import com.iota.campusX.Feature.Post.domain.Reference
import com.iota.campusX.Feature.Post.domain.PostActions
import com.iota.campusX.Feature.Post.domain.PostContent
import com.iota.campusX.Feature.Post.domain.PostDTO
import com.iota.campusX.Feature.Post.domain.PostData
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Feature.Post.presentation.PostViewModel
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.HomeViewModel
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.background
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import io.ktor.util.date.getTimeMillis
import java.util.UUID
import com.iota.campusX.Navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navHostController: NavHostController,
    userProfileViewModel: UserProfileViewModel,
    postViewModel: PostViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel
) {

    val userProfile = userProfileViewModel.userBaseProfile.collectAsState().value.baseProfileData
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var selectedPod by remember { mutableStateOf<Reference?>(null) }
    var isExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf("") }
    val postId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var selectedImages by remember {
        mutableStateOf<Uri?>(null)
    }
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImages = uri }
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val mode = homeViewModel.switchState.collectAsState().value.isActive


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
                        text = if (mode) "Campus Mode" else "Global Mode",
                        style = MaterialTheme.typography.titleMedium
                    )

                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .background(White900)
                    .padding(16.dp)
            ) {

                Row(modifier = Modifier.weight(1f)) {
                    IconButton(
                        onClick = {
                            singlePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = background
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.image),
                            contentDescription = "Select image"
                        )
                    }
                }

                Button(
                    modifier = Modifier.shadow(
                        ambientColor = primary,
                        spotColor = primary,
                        elevation = 20.dp,
                    ),
                    onClick = {

                        if (selectedPod == null) return@Button

                        if (selectedImages != null || text.isNotBlank()) {

                            postViewModel.createPost(
                                CreatePostDTO(
                                    postId = postId,
                                    type = selectedMode,
                                    postedAt = getTimeMillis(),
                                    creatorId = authViewModel.userId(),
                                    reference = Reference(
                                        title = selectedPod?.title ?: "",
                                        icon = selectedPod?.icon ?: ""
                                    ),
                                    postContent = PostContent(
                                        postType = "TEXT",
                                        postData = PostData(
                                            postText = text,
                                        )
                                    ),
                                    campusId = if (mode) userProfile?.campus?.campusCode else null,
                                    postActions = PostActions(
                                        isLiked = false,
                                    )
                                ),
                                postMode = mode,
                                imageUri = selectedImages,
                                user = User(
                                    userName = userProfile?.userName ?: "",
                                    _id = userProfile?._id ?: "",
                                    userImage = userProfile?.userImage ?: ""
                                ),
                                onCompletion = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Post Created")
                                        navHostController.popBackStack()
                                    }
                                }
                            )

                        }


                    }

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

        Column(modifier = Modifier.padding(innerPadding)) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                UserPostMode(
                    userName = userProfile?.userName ?: "",
                    userImage = userProfile?.userImage ?: "",
                    selectedMode = {
                        selectedMode = it.toString()
                    }
                )

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
                                .padding(10.dp),
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
                                            modifier = Modifier,
                                            onClick = { selectedImages = null },
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

                    },
                    cursorBrush = Brush.verticalGradient(listOf(primary, primary))

                )


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

enum class PostMode {

    USER,
    ANONYMOUS

}

@Composable
fun UserPostMode(
    userName: String,
    userImage: String,
    selectedMode: (PostMode) -> Unit
) {

    var mode by remember { mutableStateOf(PostMode.USER) }

    when (mode) {
        PostMode.USER -> {
            selectedMode(mode)
        }

        PostMode.ANONYMOUS -> {
            selectedMode(mode)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        AsyncImage(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            model = if (mode == PostMode.USER) userImage else R.drawable.incognoto,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Column {

            Row(
                verticalAlignment = Alignment.CenterVertically,

                ) {

                Text(
                    text = if (mode == PostMode.USER) userName else "Anonymous",
                    fontWeight = FontWeight.Bold,
                    color = Black900,

                    )

                IconButton(
                    onClick = {
                        if (mode == PostMode.USER) {
                            mode = PostMode.ANONYMOUS
                        } else {
                            mode = PostMode.USER
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.refresh_2),
                        contentDescription = null,
                        tint = primary
                    )
                }

            }

        }

    }

}

