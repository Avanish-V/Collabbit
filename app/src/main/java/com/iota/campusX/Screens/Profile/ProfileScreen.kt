package com.iota.campusX.Screens.Profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.Feature.Post.domain.Models.UserReplyDTO
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.ReplyViewModel
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.BottomSheet.SharedBottomSheetViewModel
import com.iota.campusX.Screens.Home.BottomSheet.PostDotOptionBottomSheet
import com.iota.campusX.Screens.Post.defaultPostHandlers
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.ProfileEdit
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.timeMillsToString
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.CircularLoading
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.UIComponents.ImageWithDynamicRatio
import com.iota.campusX.ui.UIComponents.PostCard
import com.iota.campusX.ui.theme.LightBlack
import com.iota.campusX.ui.theme.LightTheme_Blue
import com.iota.campusX.ui.theme.LightTheme_Gray
import com.iota.campusX.ui.theme.White
import kotlinx.coroutines.launch

// ProfileScreen.kt
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    navHostController: NavHostController,
    postViewModel: PostFeedViewModel,
    profileViewModel: UserProfileViewModel,
    googleSignInViewModel: AuthViewModel,
    navigationViewModel: NavigationViewModel,
    replyViewModel: ReplyViewModel,
) {


    val useridByFeed =
        navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("USER_ID")
    val currentDestination = navHostController.currentDestination?.route


    LaunchedEffect(Unit) {
        profileViewModel.getProfileIdByPost(
            userIdByFeed = useridByFeed ?: "",
            loggedInUserId = googleSignInViewModel.userId(),
            currentDestination = currentDestination ?: ""
        )
    }


    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }


    // ✅ Use shared ViewModel properly
    val bottomSheetViewModel: SharedBottomSheetViewModel = viewModel()
    val bottomSheetData by bottomSheetViewModel.bottomSheetState.collectAsState()
    val userType by profileViewModel.userType.collectAsState()


    // ✅ Collect profile states
    val userBaseProfile by profileViewModel.userBaseProfile.collectAsState()
    val profileByIdState by profileViewModel.profileById.collectAsState()
    val connectionsCountState by profileViewModel.connectionCount.collectAsState()
    val hasConnection by profileViewModel.hasConnection.collectAsState()
    val linkupRequestState by profileViewModel.sendLinkUpRequestState.collectAsState()

    val replyState by replyViewModel.userRepliesState.collectAsState()
    val postState by postViewModel.postById.collectAsState()



    LaunchedEffect(linkupRequestState) {
        when (linkupRequestState) {
            is UiState.Loading -> {}
            is UiState.Success -> {
                useridByFeed?.let { profileViewModel.hasConnection(it) }
            }

            is UiState.Error -> {
                snackBarHostState.showSnackbar((linkupRequestState as UiState.Error).message)
                profileViewModel.resetModifyState()
            }

            else -> {}
        }
    }


    // ✅ Derive profile data and loading state
    val profileData =
        when (userType) {
            UserType.Owner -> (userBaseProfile as? UiState.Success)?.data
            UserType.User -> (profileByIdState as? UiState.Success)?.data
            else -> null
        }


    val isProfileLoading = remember(userType, userBaseProfile, profileByIdState) {
        when (userType) {
            UserType.User -> profileByIdState is UiState.Loading
            else -> false

        }
    }


    // ✅ Other UI states
    val isConnected = (hasConnection as? UiState.Success)?.data
    val isLoading = remember { mutableStateOf(false) }
    val isAlertDialogVisible = remember { mutableStateOf(false) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current

    var headerHeightDp by remember { mutableStateOf(0.dp) }
    var tabRowHeightDp by remember { mutableStateOf(0.dp) }

    val horizontalPagerHeight by remember {
        derivedStateOf {
            screenHeight - (headerHeightDp + tabRowHeightDp + 12.dp + 52.dp)
        }
    }


    val postLazyColumnState = rememberLazyListState()
    HideBottomBar(navigationViewModel, postLazyColumnState)

    val headerPinned by remember {
        derivedStateOf {
            postLazyColumnState.firstVisibleItemIndex >= 2 // item index where stickyHeader is placed
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile") },
                actions = {
                    if (userType == UserType.Owner) {
                        IconButton(
                            onClick = { navHostController.navigate("SETTING") },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )

                        ) {
                            Icon(painterResource(R.drawable.setting), contentDescription = null)
                        }
                    }
                },
                navigationIcon = {
                    if (userType == UserType.User) {
                        IconButton(onClick = { navHostController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = postLazyColumnState
        ) {

            item {
                ProfileHeader(
                    snackbarHostState = snackBarHostState,
                    modifier = Modifier.fillMaxSize(),
                    headerHeight = { },
                    navHostController = navHostController,
                    user = UserDetail(
                        userName = profileData?.userName.orEmpty(),
                        userImage = profileData?.userImage.orEmpty(),
                        id = profileData?.id.orEmpty()
                    ),
                    userType = userType,
                    onLinkUpRequestClick = {
                        scope.launch {
                            profileData?.let {
                                profileViewModel.sendLinkUpRequest(
                                    requestUserId = it.id,
                                    currentState = isConnected
                                )
                            }
                        }
                    },
                    onMessageClick = {
                        navHostController.navigate(Routes.Main.SendMessage.routes).apply {
                            navHostController.currentBackStackEntry?.savedStateHandle?.apply {
                                set("USER_ID", profileData?.id)
                                set("USER_NAME", profileData?.userName)
                                set("USER_IMAGE", profileData?.userImage)
                            }
                        }
                    },
                    connectionsCount = (connectionsCountState as? UiState.Success)?.data ?: 0,
                    hasConnection = hasConnection,
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            stickyHeader {
                PrimaryTabRow(
                    modifier = Modifier,
                    selectedTabIndex = pagerState.currentPage,
                    divider = { Divider() },
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(
                                selectedTabIndex = pagerState.currentPage,
                                matchContentSize = false
                            ),
//                            width = 48.dp,
//                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
                        )
                    },
//                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    listOf("Profile", "Posts", "Replies").forEachIndexed { index, title ->
                        Tab(
                            text = {
                                Text(text = title)
                            },
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
//                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                            selectedContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                HorizontalPager(state = pagerState) { page ->

                    LazyColumn(
                        modifier = Modifier.height(screenHeight),
                        userScrollEnabled = headerPinned,
                    ) {

                        when (page) {

                            0 -> profileData?.let {

                                userAbout(
                                    userBasicProfileDTO = it,
                                    navHostController = navHostController,
                                    isCurrentUser = userType == UserType.Owner
                                )
                            }

                            1 -> postScreenComponent(
                                navHostController = navHostController,
                                postViewModel = postViewModel,
                                postState = postState,
                                bottomSheetSharedViewModel = bottomSheetViewModel,
                                currentUser = profileData?.id ?: "",
                                campusId = profileData?.campus?.campusCode,
                                userType = userType,
                            )

                            2 -> {
                                repliesComponent(
                                    userId = profileData?.id ?: "",
                                    replyViewModel = replyViewModel,
                                    repliesState = replyState,
                                    onRetryClick = { }
                                )
                            }

                        }
                    }
                }
            }
        }

        PostDotOptionBottomSheet(
            isBottomSheet = bottomSheetData.isBottomSheet,
            bottomSheetViewModel = bottomSheetViewModel,
            replyViewModel = replyViewModel,
            postFeedViewModel = postViewModel,
            onDismiss = {},
            isCurrentUser = bottomSheetData.isCurrentUser,
            onDeleteClick = { isAlertDialogVisible.value = true },
            onEditClick = {},
            onHideBottomSheet = {}
        )

        if (isAlertDialogVisible.value) {
            BasicAlertDialog(
                onDismissRequest = { isAlertDialogVisible.value = false },
            ) {
                Surface(shape = RoundedCornerShape(6.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Delete Post",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        Text(
                            "Are you sure you want to delete this post?",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                        HorizontalDivider()
                        Row(Modifier.fillMaxWidth()) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clickable(
                                        onClick = { isAlertDialogVisible.value = false },
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() })
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Cancel")
                            }
                            VerticalDivider(modifier = Modifier.height(48.dp))
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clickable(
                                        onClick = {
                                            context.vibrate()
                                            // Delete post logic here
                                        },
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() })
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading.value)
                                    CircularProgressIndicator(
                                        color = LightTheme_Blue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                else
                                    Text("Delete", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        LoadingUI(isLoading = isProfileLoading)
    }
}


fun LazyListScope.repliesComponent(
    userId: String,
    repliesState: UiState<List<UserReplyDTO>>,
    replyViewModel: ReplyViewModel,
    onRetryClick: () -> Unit
) {
    item {
        LaunchedEffect(Unit) { replyViewModel.getUserReplies(userId) }
    }

    when (repliesState) {

        is UiState.Loading -> {
            item {
                LoadingUI(isLoading = true)
            }
        }

        is UiState.Success<*> -> {

            val data = (repliesState as UiState.Success<*>).data as List<UserReplyDTO>

            item {
                if (data.isEmpty()) {
                    StatusScreen(
                        text = "No Replies Yet",
                        isActive = true,
                        image = null
                    )
                }
            }

            items(data) {
                Column {

                    Row(modifier = Modifier.fillMaxWidth()) {

                        AsyncImage(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape),
                            model = it.post.creatorDetail.profile?.userImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    it.post.creatorDetail.profile?.userName.orEmpty(),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    timeMillsToString(it.post.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                it.post.postContent.postData.postText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ImageWithDynamicRatio(it.post.postContent.postData.postImage.toString()) {

                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                VerticalDivider(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(1.dp)
                                        .padding(vertical = 12.dp)
                                )
                                AsyncImage(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    model = it.reply.creatorDetail.profile?.userImage,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            it.reply.creatorDetail.profile?.userName.orEmpty(),
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                        Text(
                                            timeMillsToString(it.reply.repliedAt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        it.reply.content,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
//                                        PostActionsComponent(
//                                            postAction = it.reply.actions,
//                                            user = it.reply.creatorDetail.profile,
//                                            onLikeClick = {},
//                                            onReplyClick = {},
//                                            onDotMenuClick = {}
//                                        )
                                }
                            }
                        }
                    }

                }
                Divider(modifier = Modifier.padding(vertical = 12.dp))
            }

        }

        is UiState.Error -> {

            item {
                ErrorScreen(
                    text = repliesState.message,
                    onReTry = {
                        onRetryClick.invoke()
                    },
                    image = R.drawable.ic_launcher_foreground,
                    buttonText = "Retry",
                )
            }
        }

        else -> {}
    }
}


@Composable
fun ProfileHeader(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier,
    headerHeight: (Dp) -> Unit,
    navHostController: NavHostController,
    user: UserDetail,
    userType: UserType,
    onLinkUpRequestClick: (() -> Unit)? = null,
    onMessageClick: (() -> Unit)? = null,
    connectionsCount: Int = 0,
    hasConnection: UiState<Boolean?>,
) {

    val density = LocalDensity.current
    var headerHeightDp by remember { mutableStateOf(0.dp) }


    Column(
        modifier = modifier
            .onGloballyPositioned {
                val heightPx = it.size.height
                headerHeightDp = with(density) { heightPx.toDp() }
                headerHeight(headerHeightDp)
            }
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Box(
                modifier = Modifier,
                contentAlignment = Alignment.BottomEnd
            ) {

                AsyncImage(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    model = user.userImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )

                if (userType == UserType.Owner) {

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.background,
                                shape = CircleShape
                            )
                            .background(color = MaterialTheme.colorScheme.surface)
                            .clickable(
                                onClick = {
                                    navHostController.navigate(Routes.Main.EditProfile.routes)
                                        .apply {
                                            navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                                "PROFILE_EDIT",
                                                ProfileEdit.PROFILE_SCREEN
                                            )
                                        }
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Back",
//                            tint = LightTheme_Blue
                        )
                    }
                }
            }


            Text(
                text = user.userName,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        TextButton(
            onClick = {
                navHostController.navigate(Routes.Main.Connections.routes).apply {
                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                        "USER_ID",
                        user.id
                    )
                }
            },
        ) {
            Text(
                text = "$connectionsCount Connections",
            )
        }

        if (userType == UserType.User) {
            Row {
                Button(
                    onClick = { onLinkUpRequestClick?.invoke() },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    when (hasConnection) {

                        is UiState.Success -> {

                            val connectionText = when (hasConnection.data) {
                                null -> "Connect"
                                true -> "Remove"
                                false -> "Requested"
                            }


                            Text(
                                text = connectionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (hasConnection.data == null || hasConnection.data == true) White else LightTheme_Gray
                            )
                        }

                        is UiState.Loading -> {
                            CircularLoading()
                        }

                        is UiState.Error -> {
                            LaunchedEffect(Unit) {
                                snackbarHostState.showSnackbar(hasConnection.message)
                            }
                        }

                        else -> {}
                    }
                }
                Spacer(
                    modifier = Modifier.width(12.dp)
                )
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .align(Alignment.CenterVertically),
                    onClick = { onMessageClick?.invoke() },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                    )
                ) {
                    Text(
                        text = "Message",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

        }

    }

}

@OptIn(ExperimentalLayoutApi::class)
fun LazyListScope.userAbout(
    userBasicProfileDTO: BaseProfileDTO,
    navHostController: NavHostController,
    isCurrentUser: Boolean
) {

    item {
        Column() {
            ProfileComponent(
                title = "About",
                onEditClick = {
                    navHostController.navigate(Routes.Main.EditProfile.routes).apply {
                        navHostController.currentBackStackEntry?.savedStateHandle?.set(
                            "PROFILE_EDIT",
                            ProfileEdit.EDIT_ABOUT_SCREEN
                        )
                    }
                },
                body = {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userBasicProfileDTO.userBio,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                contentDescription = "Bio",
                isCurrentUser = isCurrentUser,
                isContentExist = userBasicProfileDTO.userBio.isNotEmpty()
            )


            Divider()
            ProfileComponent(
                title = "Interests",
                onEditClick = {
                    navHostController.navigate(Routes.Main.EditProfile.routes).apply {
                        navHostController.currentBackStackEntry?.savedStateHandle?.set(
                            "PROFILE_EDIT",
                            ProfileEdit.EDIT_INTERESTS
                        )
                    }
                },
                body = {
                    if (userBasicProfileDTO.interests.isEmpty()) return@ProfileComponent
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        userBasicProfileDTO.interests.forEach {
                            AssistChip(
                                onClick = {
                                    null
                                },
                                label = {
                                    Text(
                                        it,
                                        modifier = Modifier.padding(10.dp),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline
                                ),
                            )
                        }
                    }
                },
                contentDescription = "INTERESTS",
                isCurrentUser = isCurrentUser,
                isContentExist = userBasicProfileDTO.interests.isEmpty()
            )

            Divider()

            ProfileComponent(
                title = "Campus Detail",
                onEditClick = {
                    navHostController.navigate(Routes.Main.EditProfile.routes).apply {
                        navHostController.currentBackStackEntry?.savedStateHandle?.set(
                            "PROFILE_EDIT",
                            ProfileEdit.EDIT_CAMPUS
                        )
                    }
                },
                body = {

                    if (userBasicProfileDTO.campus == null) return@ProfileComponent
                    Spacer(modifier = Modifier.height(12.dp))
                    CampusWidget(
                        campus = userBasicProfileDTO.campus
                    )
                },
                contentDescription = "CAMPUS",
                isCurrentUser = isCurrentUser,
                isContentExist = userBasicProfileDTO.campus == null
            )
        }
    }
}

@Composable
fun CampusWidget(campus: Campus?) {

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        if (campus == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Update Campus",
                    modifier = Modifier.align(Alignment.Center),
                    color = LightBlack
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AsyncImage(
                    model = campus.university?.logo ?: "",
                    contentDescription = null,
                    placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(5.dp)),
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = campus.university?.university ?: "",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    campus.collegeName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                    campus.fieldOfStudy?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }


                    if (campus.courseStart != null && campus.courseEnd != null) {
                        Text(
                            text = "${campus.courseStart.month + campus.courseStart.year} - ${campus.courseEnd.month + campus.courseEnd.year}",
                            style = MaterialTheme.typography.bodyMedium

                        )
                    }

                    campus.campusCode?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

    }
}

fun LazyListScope.postScreenComponent(
    navHostController: NavHostController,
    postViewModel: PostFeedViewModel,
    postState: UiState<List<GetPostDTO>>,
    bottomSheetSharedViewModel: SharedBottomSheetViewModel,
    currentUser: String,
    campusId: String?,
    userType: UserType
) {


    item {
        LaunchedEffect(Unit) {
            postViewModel.fetchPostById(
                userId = currentUser,
                campusId = campusId,
                feedMode = FeedMode.GLOBAL,
                userType = userType
            )
        }
    }

    when (postState) {
        is UiState.Loading -> {
            item {
                LoadingUI(true)
            }
        }

        is UiState.Success -> {

            val sortedPost = postState.data.sortedByDescending { it.createdAt }

            item {
                if (sortedPost.isEmpty()) {
                    StatusScreen(
                        isActive = true,
                        text = "No Posts"
                    )
                    return@item
                }
            }

            items(sortedPost, key = { it.postId }) {
                PostCard(
                    post = it,
                    handlers = defaultPostHandlers(
                        context = LocalContext.current,
                        post = it,
                        feedViewModel = postViewModel,
                        bottomSheetSharedViewModel = bottomSheetSharedViewModel,
                        navController = navHostController,
                    ),
                    isCurrentUser = currentUser == it.creatorDetail.profile?.id
                )
                Divider()

            }

        }

        is UiState.Error -> {

            item {
                ErrorScreen(
                    text = "Something went wrong",
                    image = R.drawable.landscape_placeholder_svgrepo_com,
                    buttonText = "Try again",
                    onReTry = {
                        postViewModel.fetchPostById(
                            currentUser,
                            campusId,
                            feedMode = FeedMode.GLOBAL,
                            userType = userType
                        )
                    }
                )
            }

        }

        is UiState.Idle -> {}
    }
}
