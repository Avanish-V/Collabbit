package com.iota.campusX.Screens.Profile

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.UserReplyDTO
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.Reply.AppUserReplyViewModel
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.AppNavigator
import com.iota.campusX.Navigation.AppNavigatorImpl
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.PagingListFooter
import com.iota.campusX.Screens.Home.PagingListHeader
import com.iota.campusX.Screens.Home.RefreshBox
import com.iota.campusX.Screens.Post.DataModel.ContentId
import com.iota.campusX.Screens.Post.DataModel.ContentType
import com.iota.campusX.Screens.Post.DataModel.FeedContent
import com.iota.campusX.Screens.Post.PostActions.PostAction
import com.iota.campusX.Screens.Post.PostActions.PostActionViewModel
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuState
import com.iota.campusX.Screens.ReplyWidget
import com.iota.campusX.Utils.CircularLoading
import com.iota.campusX.Utils.LoadingScreen
import com.iota.campusX.Utils.ProfileEdit
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.UIComponents.AppLabelText
import com.iota.campusX.ui.UIComponents.AppTabRow
import com.iota.campusX.ui.UIComponents.CampusWidget
import com.iota.campusX.ui.UIComponents.ConnectionComponent
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.EditProfileIconButton
import com.iota.campusX.ui.UIComponents.EmptyState
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.UIComponents.FeedUI.Avatar
import com.iota.campusX.ui.UIComponents.FeedUI.FeedBody
import com.iota.campusX.ui.UIComponents.FeedUI.FeedHeader
import com.iota.campusX.ui.UIComponents.FeedUI.FeedItem
import com.iota.campusX.ui.UIComponents.ProfileContents
import com.iota.campusX.ui.UIComponents.ProfileHeader
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import kotlin.div

// AppUserProfileScreen.kt
@SuppressLint("ConfigurationScreenWidthHeight")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppUserProfile(
    navHostController: NavHostController,
    postFeedViewModel: PostFeedViewModel = koinInject(),
    appUserReplyViewModel: AppUserReplyViewModel = koinInject(),
    navigationViewModel: NavigationViewModel,
) {

   val  profileViewModel: UserProfileViewModel = koinInject()
    val pullToRefreshState = rememberPullToRefreshState()
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
    val profileState = profileViewModel.userBaseProfile.collectAsState().value
    val isLoading = profileViewModel.isLoading.collectAsState().value

    val appUserReplyState by appUserReplyViewModel.userReplies.collectAsState()
    val postState = postFeedViewModel.userPosts.collectAsLazyPagingItems()

    LaunchedEffect(isLoading) {
        if (isLoading is UiState.Error) {
            snackBarHostState.showSnackbar(isLoading.message)
        }
    }

    val profile = when(profileState){
        is UiState.Success<*> -> {
            (profileState as UiState.Success<BaseProfileDTO>).data
        }
        else -> {
            null
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
        snackbarHost = { SnackbarHost(modifier = Modifier.padding(bottom = 80.dp), hostState = snackBarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->

        RefreshBox(
            modifier = Modifier.padding(innerPadding),
            pullToRefreshState = pullToRefreshState,
            isRefreshing = isLoading is UiState.Loading,
            onRefresh = {
                profileViewModel.refreshProfile()
            },
        ) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = postLazyColumnState
            ) {

                item {
                    ProfileHeader(
                        modifier = Modifier.fillMaxSize(),
                        headerHeight = { },
                        user = profile,
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

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    profile?.let {
                        ConnectionComponent(
                            navHostController = navHostController,
                            pagerState = pagerState,
                            followersCount = profile.count?.followers?:0,
                            connectionCount = profile.count?.connections?:0,
                            postsCountCount = profile.count?.posts?:0,
                            userId = it.id
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(
                        thickness = 12.dp,
                        color = MaterialTheme.colorScheme.surface
                    )
                }

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
                                        lazyPagingItems = postState,
                                        onPageActive = {
                                            if (postState.itemCount != 0) return@PostScreenComponent
                                            postFeedViewModel.fetchUserPost(
                                                userId = profile?.id ?: "",
                                            )
                                        },
                                        onRetryClick = {
                                            postFeedViewModel.fetchUserPost(
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
        userScrollEnabled = pinned,
        contentPadding = PaddingValues(12.dp)
    ){
        when (repliesState) {

            is UiState.Loading -> {
                item {
                    LoadingScreen(modifier = Modifier.height(screenHeight/2))
                }
            }

            is UiState.Success<*> -> {

                val data = (repliesState as UiState.Success<*>).data as List<*>

                item {
                    if (data.isEmpty()) {
                        StatusScreen(
                            modifier =  Modifier.height(screenHeight/2),
                            text = "No replies yet.",
                            description = "The posts are waiting for your input.",
                            image = R.drawable.undraw_no_data_ig65,
                        )
                    }
                }

                items(data) {

                    UserReplyItem(
                        userReplyDTO = it as UserReplyDTO,
                        handleAction = {
                            postActionsViewModel.onAction(it)
                        },
                        postMenuState = postMenuState
                    )

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
                                title = "Tap to add bio",
                                isAppUser = true
                            )
                        }
                    },
                    contentDescription = "Bio",
                    editIconVisible = userBasicProfileDTO.userBio.isNotEmpty()
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
                                title = "Tap to add interests",
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
                                            width = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        ),
                                    )
                                }
                            }
                        }
                    },
                    contentDescription = "INTERESTS",
                    editIconVisible = userBasicProfileDTO.interests.isNotEmpty()
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
                    editIconVisible = userBasicProfileDTO.campus != null
                )
            }
        }
    }

}


@Composable
fun PostScreenComponent(
    screenHeight: Dp,
    pinned: Boolean,
    lazyPagingItems: LazyPagingItems<GetPostDTO>,
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

        item {
            PagingListHeader(
                items = lazyPagingItems,
                emptyContent = {
                    StatusScreen(
                        modifier =  Modifier.height(screenHeight/2),
                        text = "No posts yet.",
                        image = R.drawable.undraw_no_data_ig65,
                        description = "Nothing here yet — share your thoughts and updates!"
                    )
                },
                screenHeight = screenHeight
            )
        }


        items(lazyPagingItems.itemCount) { post ->
            val item = lazyPagingItems[post]
            item?.let {
                FeedItem(
                    feedItem = item,
                    handlers = {
                        viewModel.onAction(it)
                    },
                    onDotMenuClick = {
                        postMenuState.open(
                            FeedContent(
                                id = ContentId.Post(postId = item.postId),
                                text = item.postContent.postText,
                                isOwner = item.creatorDetail.isCurrentUser,
                                type = ContentType.POST
                            )
                        )
                    },
                    enableFeedMode = true
                )
                Divider()
            }
        }

        item {
            PagingListFooter(
                items = lazyPagingItems,
                minItemsBeforeEnd = 16, // don’t show "No more" too early
                errorContent = { error -> AppLabelText("Error: ${error.message}") },
                endContent = { AppLabelText("🎉 You’ve reached the end!") }
            )
        }
    }

}


@Composable
fun UserReplyItem(
    userReplyDTO: UserReplyDTO,
    handleAction: (PostAction) -> Unit,
    postMenuState: PostMenuState
) {

    Column (
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                handleAction.invoke(
                    PostAction.OpenPostDetail(postId = userReplyDTO.post.postId)
                )
            }
        )
    ){

        Row {

            Avatar(
                imageUrl = userReplyDTO.post.creatorDetail.profile?.userImage ?: "",
                visibilityMode = userReplyDTO.post.visibilityMode,
                onAvatarClick = {
                    Log.d("REPLY_USER", "UserReplyItem: ${userReplyDTO.post.creatorDetail.isCurrentUser}")
                    Log.d("REPLY_USER", "UserReplyItem: ${userReplyDTO.post.creatorDetail.profile?.id}")
                    if (userReplyDTO.post.creatorDetail.isCurrentUser) return@Avatar
                    userReplyDTO.post.creatorDetail.profile?.let {
                        handleAction.invoke(
                            PostAction.OpenUserProfile(
                                userId = it.id,
                                isCurrentUser = userReplyDTO.post.creatorDetail.isCurrentUser
                            )
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {

                FeedHeader(
                    creator = userReplyDTO.post.creatorDetail,
                    postedAt = getTimeAgo(userReplyDTO.post.createdAt?.toDate()?.time ?: 0L),
                    visibilityMode = userReplyDTO.post.visibilityMode,
                    feedMode = userReplyDTO.post.feedMode,
                    trailingComponent = {

                    }
                )

                FeedBody(
                    type = userReplyDTO.post.type,
                    mediaType = userReplyDTO.post.mediaType,
                    postContent = userReplyDTO.post.postContent,
                    onPollSelect = {optionId->
                        handleAction.invoke(
                            PostAction.VotePoll(
                                postId = userReplyDTO.post.postId,
                                optionId = optionId,
                                feedMode = userReplyDTO.post.feedMode
                            )
                        )
                    },
                    goToFeedViewer = {

                    },
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

        }

        Divider(modifier = Modifier.padding(start = 50.dp))

        Row(
            modifier = Modifier.padding(start = 50.dp)

        ) {
            ReplyWidget(
                repliesDTO = userReplyDTO.reply,
                onDotsClick = {
                    postMenuState.open(
                        FeedContent(
                            id = ContentId.Reply(postId = userReplyDTO.reply.postId,replyId = userReplyDTO.reply.replyId),
                            text = userReplyDTO.reply.content,
                            isOwner = userReplyDTO.reply.creatorDetail.isCurrentUser,
                            type = ContentType.REPLY
                        )
                    )
                },
                handler = {
                    handleAction.invoke(it)
                },

            )
        }
    }
}