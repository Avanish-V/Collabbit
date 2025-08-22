package com.iota.campusX.Screens.Profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.UserReplyDTO
import com.iota.campusX.Feature.Reply.AppUserReplyViewModel
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.AppNavigator
import com.iota.campusX.Navigation.AppNavigatorImpl
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Post.DataModel.ContentId
import com.iota.campusX.Screens.Post.DataModel.ContentType
import com.iota.campusX.Screens.Post.DataModel.FeedContent
import com.iota.campusX.Screens.Post.PostActions.PostActionViewModel
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuState
import com.iota.campusX.Screens.Post.PostManupulation.AppUserPostViewModel
import com.iota.campusX.Screens.ReplyWidget
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.ProfileEdit
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.AppTabRow
import com.iota.campusX.ui.UIComponents.CampusWidget
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.EditProfileIconButton
import com.iota.campusX.ui.UIComponents.EmptyState
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.UIComponents.PostCard
import com.iota.campusX.ui.UIComponents.ProfileContents
import com.iota.campusX.ui.UIComponents.ProfileHeader
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

// AppUserProfileScreen.kt
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppUserProfile(
    navHostController: NavHostController,
    appUserPostViewModel: AppUserPostViewModel = koinInject(),
    appUserReplyViewModel: AppUserReplyViewModel = koinInject(),
    profileViewModel: UserProfileViewModel,
    navigationViewModel: NavigationViewModel,
) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val snackBarHostState = remember { SnackbarHostState() }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val postLazyColumnState = rememberLazyListState()
    HideBottomBar(navigationViewModel, postLazyColumnState)
    val headerPinned by remember {
        derivedStateOf {
            postLazyColumnState.firstVisibleItemIndex >= 2 // item index where stickyHeader is placed
        }
    }

    val tabList by remember {mutableStateOf(listOf("About", "Posts", "Replies"))}


    // ✅ Collect profile states
    val userBaseProfile by profileViewModel.userBaseProfile.collectAsState()
    val connectionsCountState by profileViewModel.connectionCount.collectAsState()
    val appUserReplyState by appUserReplyViewModel.userReplies.collectAsState()
    val postState by appUserPostViewModel.userPosts.collectAsState()


    val profile = when(userBaseProfile){
        is UiState.Success -> (userBaseProfile as UiState.Success<BaseProfileDTO>).data
        else -> null
    }

    LaunchedEffect(Unit) {
        profile?.id?.let {
            profileViewModel.getConnectionCount(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile") },
                actions = {
                    IconButton(
                        onClick = { navHostController.navigate("SETTING") },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )

                    ) {
                        Icon(painterResource(R.drawable.setting), contentDescription = null)
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
                    modifier = Modifier.fillMaxSize(),
                    headerHeight = { },
                    user = profile,
                    connectionsCountState = connectionsCountState,
                    onConnectionClick = {
                        navHostController.navigate(Routes.Main.Connections.routes).apply {
                            navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                "USER_ID",
                                profile?.id ?: ""
                            )
                        }
                    },
                    editProfile = {
                        EditProfileIconButton(
                            onClick = {
                                navHostController.navigate(Routes.Main.EditProfile.routes).apply {
                                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                        "PROFILE_EDIT",
                                        ProfileEdit.PROFILE_SCREEN
                                    )
                                }
                            }
                        )
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            stickyHeader {
                AppTabRow(
                    pagerState = pagerState,
                    tabList = tabList
                )
            }

            item {
                ProfileContents(
                    pagerState = pagerState,
                    screenHeight = screenHeight,
                    headerPinned = headerPinned,
                    content = {
                        when (it) {

                            0 -> {
                                profile?.let { userBasicProfileDTO ->
                                    UserAbout(
                                        screenHeight = screenHeight,
                                        pinned = headerPinned,
                                        userBasicProfileDTO = userBasicProfileDTO,
                                        navHostController = navHostController
                                    )
                                }
                            }

                            1 ->{

                                PostScreenComponent(
                                    screenHeight = screenHeight,
                                    pinned = headerPinned,
                                    navHostController = navHostController,
                                    postState = postState,
                                    onPageActive = {
                                        appUserPostViewModel.fetchUserPosts(
                                            userId = profile?.id ?: "",
                                        )
                                    },
                                    onRetryClick = {
                                        appUserPostViewModel.fetchUserPosts(
                                            userId = profile?.id ?: "",
                                        )
                                    }
                                )
                            }

                            2 -> {

                                RepliesComponent(
                                    screenHeight = screenHeight,
                                    pinned = headerPinned,
                                    userId = profile?.id ?: "",
                                    repliesState = appUserReplyState,
                                    navHostController = navHostController,
                                    onRetryClick = {
                                        appUserReplyViewModel.getUserReplies(profile?.id ?: "")
                                    },
                                    onPageActive = {
                                        appUserReplyViewModel.getUserReplies(profile?.id ?: "")
                                    }
                                )

                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RepliesComponent(
    screenHeight: Dp,
    pinned: Boolean,
    userId: String,
    repliesState: UiState<List<UserReplyDTO>>,
    navHostController: NavHostController,
    onRetryClick: () -> Unit,
    onPageActive: () -> Unit,

) {

    val postMenuState : PostMenuState = koinInject()

    val appNavigator: AppNavigator = remember { AppNavigatorImpl(navHostController) }
    val postActionsViewModel: PostActionViewModel = getKoin().get { parametersOf(appNavigator) }

    LaunchedEffect(Unit) {
        onPageActive()
    }

    LazyColumn (
        modifier = Modifier.height(height = screenHeight),
        userScrollEnabled = pinned
    ){
        when (repliesState) {

            is UiState.Loading -> {
                item {
                    LoadingUI(modifier = Modifier.height(screenHeight/2))
                }
            }

            is UiState.Success<*> -> {

                val data = (repliesState as UiState.Success<*>).data as List<UserReplyDTO>

                item {
                    if (data.isEmpty()) {
                        StatusScreen(
                            modifier = Modifier.height(300.dp),
                            text = "No Replies Yet",
                            image = null
                        )
                    }
                }

                items(data) {

                    Column {
                        PostCard(
                            post = it.post,
                            handlers = {
                                postActionsViewModel.onAction(it)
                            },
                            onDotMenuClick = { postData ->

                            },
                        )
                        Divider(modifier = Modifier.padding(start = 50.dp))
                        Row(
                            modifier = Modifier.padding(start = 50.dp)

                        ) {
                            ReplyWidget(
                                repliesDTO = it.reply,
                                onDotsClick = {
                                    postMenuState.open(
                                        FeedContent(
                                            id = ContentId.Reply(postId = it.reply.postId,replyId = it.reply.replyId),
                                            text = it.reply.content,
                                            isOwner = it.reply.creatorDetail.isCurrentUser,
                                            type = ContentType.REPLY
                                        )
                                    )
                                },
                                handler = {
                                    postActionsViewModel.onAction(it)
                                },

                            )
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

}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserAbout(
    screenHeight: Dp,
    pinned: Boolean,
    userBasicProfileDTO: BaseProfileDTO,
    navHostController: NavHostController,
) {

    LazyColumn (
        modifier = Modifier.height(height = screenHeight),
        userScrollEnabled = pinned
    ){
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
                                    navHostController.navigate(Routes.Main.EditProfile.routes)
                                        .apply {
                                            navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                                "PROFILE_EDIT",
                                                ProfileEdit.EDIT_ABOUT_SCREEN
                                            )
                                        }
                                },
                                title = "Add bio",
                                isAppUser = true
                            )
                        }
                    },
                    contentDescription = "Bio",
                    editIconVisible = userBasicProfileDTO.userBio.isEmpty()
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
                                    navHostController.navigate(Routes.Main.EditProfile.routes).apply {
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                            "PROFILE_EDIT",
                                            ProfileEdit.EDIT_INTERESTS
                                        )
                                    }
                                },
                                title = "Add Interests",
                                isAppUser = true
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
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        ),
                                    )
                                }
                            }
                        }
                    },
                    contentDescription = "INTERESTS",
                    editIconVisible = userBasicProfileDTO.interests.isEmpty()
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
                                    navHostController.navigate(Routes.Main.EditProfile.routes)
                                        .apply {
                                            navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                                "PROFILE_EDIT",
                                                ProfileEdit.EDIT_CAMPUS
                                            )
                                        }
                                },
                                title = "Tap to add campus details",
                                isAppUser = true

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
                    editIconVisible = userBasicProfileDTO.campus == null
                )
            }
        }
    }

}


@Composable
fun PostScreenComponent(
    screenHeight: Dp,
    pinned: Boolean,
    postState: UiState<List<GetPostDTO>>,
    onPageActive: () -> Unit,
    onRetryClick: () -> Unit = {onPageActive.invoke()},
    postMenuState: PostMenuState = koinInject(),
    navHostController: NavHostController
) {

    val appNavigator: AppNavigator = remember { AppNavigatorImpl(navHostController) }
    //-----------------Pass parameter from outside------------
    val viewModel: PostActionViewModel = getKoin().get { parametersOf(appNavigator) }

    LaunchedEffect(Unit) {
        onPageActive.invoke()
    }

    LazyColumn (
        modifier = Modifier.height(height = screenHeight),
        userScrollEnabled = pinned
    ){
        when (postState) {
            is UiState.Loading -> {
                item {
                    LoadingUI(modifier = Modifier.height(screenHeight/2))
                }
            }

            is UiState.Success -> {

                val sortedPost = postState.data.sortedByDescending { it.createdAt }

                item {
                    if (sortedPost.isEmpty()) {
                        StatusScreen(
                            modifier = Modifier.height(300.dp),
                            text = "No Posts"
                        )
                        return@item
                    }
                }

                items(sortedPost, key = { it.postId }) {
                    PostCard(
                        post = it,
                        handlers = {
                            viewModel.onAction(it)
                        },
                        onDotMenuClick = {postData->
                            postMenuState.open(
                                FeedContent(
                                    id = ContentId.Post(postId = postData.postId),
                                    text = postData.postContent.postData.postText,
                                    isOwner = postData.creatorDetail.isCurrentUser,
                                    type = ContentType.POST
                                )
                            )
                        }
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
                            onRetryClick.invoke()
                        }
                    )
                }

            }

            is UiState.Idle -> {}
        }
    }

}
