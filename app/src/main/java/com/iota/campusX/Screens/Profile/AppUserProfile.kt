package com.iota.campusX.Screens.Profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BasicAlertDialog
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.Feature.Post.domain.Models.UserReplyDTO
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.ReplyViewModel
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.BottomSheet.SharedBottomSheetViewModel
import com.iota.campusX.Screens.Home.BottomSheet.PostDotOptionBottomSheet
import com.iota.campusX.Screens.Post.defaultPostHandlers
import com.iota.campusX.Screens.ReplyWidget
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.ProfileEdit
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.CampusWidget
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.EmptyState
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.UIComponents.PostCard
import com.iota.campusX.ui.UIComponents.ProfileHeader
import com.iota.campusX.ui.theme.LightTheme_Blue
import kotlinx.coroutines.launch

// AppUserProfile.kt
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppUserProfile(
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
                                    onRetryClick = { },
                                    navHostController = navHostController,
                                    postViewModel = postViewModel,
                                    sharedBottomSheetViewModel = bottomSheetViewModel,
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
    postViewModel: PostFeedViewModel,
    navHostController: NavHostController,
    sharedBottomSheetViewModel: SharedBottomSheetViewModel,
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
                    PostCard(
                        post = it.post,
                        handlers = defaultPostHandlers(
                            context = LocalContext.current,
                            post = it.post,
                            feedViewModel = postViewModel,
                            bottomSheetSharedViewModel = sharedBottomSheetViewModel,
                            navController = navHostController,
                        ),
                        isCurrentUser = it.post.creatorDetail.isCurrentUser,
                    )

                    ReplyWidget(
                        repliesDTO = it.reply,
                        navHostController = navHostController,
                        onLikeClick = {

                        },
                        onDotsClick = {

                        },
                    )
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
                    if (userBasicProfileDTO.userBio.isNotEmpty()) {
                        // Show Bio Text
                        Text(
                            text = userBasicProfileDTO.userBio,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        // Show dotted placeholder box
                        EmptyState(
                            onClick = {
                                if (!isCurrentUser)return@EmptyState
                                navHostController.navigate(Routes.Main.EditProfile.routes)
                                    .apply {
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                            "PROFILE_EDIT",
                                            ProfileEdit.EDIT_ABOUT_SCREEN
                                        )
                                    }
                            },
                            title = "Add bio",
                            isCurrentUser = isCurrentUser
                        )
                    }
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

                    if (userBasicProfileDTO.interests.isEmpty()) {

                        EmptyState(
                            onClick = {
                                if (!isCurrentUser)return@EmptyState
                                navHostController.navigate(Routes.Main.EditProfile.routes).apply {
                                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                        "PROFILE_EDIT",
                                        ProfileEdit.EDIT_INTERESTS
                                    )
                                }
                            },
                            title = "Add Interests",
                            isCurrentUser = isCurrentUser
                        )

                    } else {
                        // Normal case: show chips
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            userBasicProfileDTO.interests.forEach {
                                AssistChip(
                                    onClick = { /* could trigger something later */ },
                                    label = {
                                        Text(
                                            text = it,
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
                    }
                },
                contentDescription = "INTERESTS",
                isCurrentUser = isCurrentUser,
                isContentExist = userBasicProfileDTO.interests.isNotEmpty() // ✅ fixed
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
                    if (userBasicProfileDTO.campus == null) {

                        EmptyState(
                            onClick = {
                                if (!isCurrentUser)return@EmptyState
                                navHostController.navigate(Routes.Main.EditProfile.routes)
                                    .apply {
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                            "PROFILE_EDIT",
                                            ProfileEdit.EDIT_CAMPUS
                                        )
                                    }
                            },
                            title = if (isCurrentUser) "Tap to add campus details" else "Not yet updated.",
                            isCurrentUser = isCurrentUser
                        )

                    } else {
                        // Normal campus widget
                        Spacer(modifier = Modifier.height(12.dp))
                        CampusWidget(
                            campus = userBasicProfileDTO.campus
                        )
                    }
                },
                contentDescription = "CAMPUS",
                isCurrentUser = isCurrentUser,
                isContentExist = userBasicProfileDTO.campus != null // ✅ fixed
            )

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
