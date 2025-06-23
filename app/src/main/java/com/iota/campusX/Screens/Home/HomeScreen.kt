package com.iota.campusX.Screens.Home

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.data.Error
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.ReplyViewModel
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.Permissions.NotificationPermissionRequester
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.BottomSheet.BottomSheetSharedViewModel
import com.iota.campusX.Screens.Home.BottomSheet.Content
import com.iota.campusX.Screens.Home.BottomSheet.ContentType
import com.iota.campusX.Screens.Home.BottomSheet.PostDotOptionBottomSheet
import com.iota.campusX.Screens.Home.BottomSheet.SheetType
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.AlertDialogWidget
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.UIComponents.PostCard
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.Black400
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.background
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.secondary
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navHostController: NavHostController,
    postViewModel: PostFeedViewModel,
    navigationViewModel: NavigationViewModel,
    profileViewModel: UserProfileViewModel,
    homeViewModel: HomeViewModel,
    notificationViewModel: NotificationViewModel
) {
    NotificationPermissionRequester()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val userProfileState = profileViewModel.userBaseProfile.collectAsState().value
    val switchState = homeViewModel.mode.collectAsState().value
    val chatBadgeCount by notificationViewModel.chatCount.collectAsState()

    val tabs = listOf("Global", "Campus")

    LaunchedEffect(Unit) {
        profileViewModel.getUserProfile()
        notificationViewModel.getChatCount()
    }

    val userProfile = (userProfileState as? UiState.Success)?.data
    val feedMode = (switchState as? UiState.Success<FeedMode>)?.data

    // Only fetch posts when both userProfile and switchState are loaded
    LaunchedEffect(userProfile, feedMode) {
        if (userProfile != null && feedMode != null) {
            if (feedMode == FeedMode.CAMPUS) {
                postViewModel.fetchCampusPosts(
                    feedMode = FeedMode.CAMPUS,
                    campusId = userProfile.campus?.campusCode.orEmpty()
                )
            } else {
                postViewModel.fetchGlobalPosts()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(R.drawable.campusx),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .height(60.dp)
                            .width(140.dp)
                    )
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (chatBadgeCount != 0) {
                                    Box(modifier = Modifier.size(12.dp).background(Color.Red, CircleShape),contentAlignment = Alignment.Center){
                                        Text(
                                            chatBadgeCount.toString(),
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            lineHeight = 10.sp
                                        )
                                    }

                                }
                            }
                        ) {
                            IconButton(
                                onClick = { navHostController.navigate(Routes.Main.ChatList.routes) },
                                modifier = Modifier.border(1.dp, background, CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.messages_normal),
                                    contentDescription = "Message"
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.padding(bottom = 100.dp),
                hostState = snackbarHostState
            )
        },
        containerColor = White900
    ) { innerPadding ->

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                userProfileState is UiState.Loading || userProfileState is UiState.Idle || switchState is UiState.Loading || switchState is UiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                userProfileState is UiState.Error -> {
                    Text("Error loading user profile: ${userProfileState.message}")
                }

                switchState is UiState.Error -> {
                    Text("Error loading switch state: ${switchState.message}")
                }

                userProfile != null && feedMode != null -> {
                    val currentPage = if (feedMode == FeedMode.GLOBAL) 0 else 1

                    val pagerState = rememberPagerState(
                        initialPage = currentPage,
                        pageCount = { tabs.size }
                    )

                    LaunchedEffect(pagerState.currentPage) {
                        val selectedMode = if (pagerState.currentPage == 0) FeedMode.GLOBAL else FeedMode.CAMPUS
                        homeViewModel.saveSwitchState(selectedMode)
                    }

                    PrimaryTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = White900,
                        divider = { HorizontalDivider(color = White400) },
                        indicator = {
                            TabRowDefaults.PrimaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                                width = 48.dp,
                                color = primary,
                                shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                icon = {
                                    Icon(
                                        modifier = Modifier.size(20.dp),
                                        painter = painterResource(
                                            if (index == 0) R.drawable.globe else R.drawable.school
                                        ),
                                        contentDescription = null
                                    )
                                },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                selectedContentColor = Black800,
                                unselectedContentColor = Black400
                            )
                        }
                    }

                    HorizontalPager(state = pagerState) { page ->
                        GlobalPosts(
                            navHostController = navHostController,
                            postFeedViewModel = postViewModel,
                            navigationViewModel = navigationViewModel,
                            profileImage = userProfile.userImage,
                            scrollBehavior = scrollBehavior,
                            pageIndex = page
                        )
                    }
                }
            }
        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
fun LazyListScope.writePost(
    navHostController: NavHostController,
    context: Context,
    profileImage: String
) {
    item {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = White900
                )
                .padding(12.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        navHostController.navigate(Routes.Main.CreatePost.routes)
                        context.vibrate()
                    }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            AsyncImage(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                model = profileImage,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "What's on your mind?",
                    color = Black500
                )

                Icon(
                    painter = painterResource(R.drawable.write),
                    contentDescription = null,
                    tint = Black300
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun LazyListScope.postsLazyColumn(
    postData: List<GetPostDTO>,
    navHostController: NavHostController,
    postFeedViewModel: PostFeedViewModel,
    bottomSheetSharedViewModel: BottomSheetSharedViewModel,
    context: Context,
) {

    if (postData.isNotEmpty()) {

        val sortedPost = postData.sortedByDescending { it.createdAt }

        items(sortedPost, key = {it.postId}) {


            PostCard(
                postFeedViewModel,
                bottomSheetSharedViewModel = bottomSheetSharedViewModel,
                onPostClick = {
                    navHostController.navigate(Routes.Main.ReplyPost.routes).apply {
                        navHostController.currentBackStackEntry?.savedStateHandle?.set<String>(
                            "POST_ID",
                            it.postId
                        )
                    }
                },
                onReplyClick = {
                    navHostController.navigate(Routes.Main.ReplyPost.routes).apply {
                        navHostController.currentBackStackEntry?.savedStateHandle?.set<String>(
                            "POST_ID",
                            it.postId
                        )
                    }
                },
                post = it,
                navHostController = navHostController,
                onPollSelect = { optionId ->
                    postFeedViewModel.voteOnPoll(
                        postId = it.postId,
                        optionId = optionId,
                        userId = "dfd",
                        campusId = it.campusId,
                        feedMode = it.feedMode
                    )
                }
            )

            HorizontalDivider(
                thickness = 12.dp,
                color = secondary
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalPosts(
    navHostController: NavHostController,
    postFeedViewModel: PostFeedViewModel,
    navigationViewModel: NavigationViewModel,
    profileImage: String,
    scrollBehavior: TopAppBarScrollBehavior,
    pageIndex: Int
) {
    val replyViewModel = koinInject<ReplyViewModel>()
    val bottomSheetViewModel: BottomSheetSharedViewModel = viewModel()
    val bottomSheetData = bottomSheetViewModel.bottomSheetState.collectAsState().value

    val deletePostState = postFeedViewModel.deletePostState.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val globalPostState = postFeedViewModel.globalPosts.collectAsStateWithLifecycle().value
    val campusPostState = postFeedViewModel.campusPosts.collectAsState().value

    val pullToRefreshState = rememberPullToRefreshState()
    val lazyState = rememberLazyListState()
    var isLoading by remember { mutableStateOf(false) }
    var isAlertDialogVisible by remember { mutableStateOf(false) }


    var isRefreshing by remember { mutableStateOf(false) }

    HideBottomBar(navigationViewModel, lazyState)

    LaunchedEffect(deletePostState.value) {
        when (deletePostState.value) {
            is UiState.Loading -> {
                isLoading = true
            }

            is UiState.Success -> {
                isAlertDialogVisible = false
                isLoading = false
            }

            is UiState.Error -> {
                false
                isAlertDialogVisible = false
            }

            else -> {
                false
                isAlertDialogVisible = false
            }
        }
    }


    RefreshBox(
        pullToRefreshState = pullToRefreshState,
        onRefresh = {

            context.vibrate()

            scope.launch {
                isRefreshing = true
                lazyState.animateScrollToItem(0)
                isRefreshing = false
            }
        },
        isRefreshing = isRefreshing
    ) {

        Column {

            when (pageIndex) {
                0 -> {

                    when (globalPostState) {
                        is UiState.Loading -> {
                            Loader()
                        }

                        is UiState.Error -> {
                            StatusScreen(true, globalPostState.message)
                        }

                        is UiState.Success -> {

                            val sortedPosts = globalPostState.data.sortedByDescending { it.createdAt }

                            PostFeedList(
                                postData = sortedPosts,
                                navHostController = navHostController,
                                profileImage = profileImage,
                                postFeedViewModel = postFeedViewModel,
                                bottomSheetSharedViewModel = bottomSheetViewModel,
                                context = context,
                                scrollBehavior = scrollBehavior,
                                lazyState = lazyState,
                            )
                        }

                        is UiState.Idle -> {
                            // Optional: show empty state or nothing
                        }
                    }


                }

                1 -> {
                    when (campusPostState) {

                        is UiState.Loading -> {
                            Loader()
                        }

                        is UiState.Error -> {
                            if (campusPostState.message == Error.CAMPUS_NOT_FOUND.name) {
                                ErrorScreen(
                                    text = "It seems you have not updated your campus details.",
                                    image = null,
                                    onReTry = {
                                        navHostController.navigate(Routes.Main.Profile.routes)
                                    },
                                    buttonText = "Update"
                                )
                            } else {
                                StatusScreen(true, campusPostState.message)
                            }
                        }

                        is UiState.Success -> {

                            val sortedPosts = campusPostState.data.sortedByDescending { it.createdAt }

                            PostFeedList(
                                postData = sortedPosts,
                                navHostController = navHostController,
                                profileImage = profileImage,
                                postFeedViewModel = postFeedViewModel,
                                bottomSheetSharedViewModel = bottomSheetViewModel,
                                context = context,
                                scrollBehavior = scrollBehavior,
                                lazyState = lazyState,
                            )
                        }

                        is UiState.Idle -> {
                            // Optional: Add a placeholder or keep empty
                        }
                    }

                }
            }
        }

        PostDotOptionBottomSheet(
            isBottomSheet = bottomSheetData.isBottomSheet,
            bottomSheetViewModel = bottomSheetViewModel,
            replyViewModel = replyViewModel,
            postFeedViewModel = postFeedViewModel,
            onDismiss = {
                bottomSheetViewModel.dismissBottomSheet()
            },
            isCurrentUser = bottomSheetData.isCurrentUser,
            onDeleteClick = {
                isAlertDialogVisible = true
                bottomSheetViewModel.dismissBottomSheet()
            },
            onEditClick = {

            },
            onHideBottomSheet = {

            }
        )

        AlertDialogWidget(
            isVisible = isAlertDialogVisible,
            onDismiss = { isAlertDialogVisible = it },
            title = "Delete Post",
            description = "Are you sure you want to delete this post?",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel",
            onPositiveClick = {

                if (bottomSheetData.contentType == ContentType.POST) {
                    postFeedViewModel.deletePost(
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
            showLoading = isLoading
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshBox(
    pullToRefreshState: PullToRefreshState,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    content: @Composable () -> Unit
) {
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isRefreshing,
        onRefresh = {
            onRefresh.invoke()
        },
        state = pullToRefreshState,
        contentAlignment = Alignment.TopCenter,
        indicator = {
            Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                color = primary,
                containerColor = Color.White
            )
        },
    ) {
        content()
    }
}


@Composable
fun Loader() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = primary)
    }
}

@Composable
fun StatusScreen(isActive: Boolean, text: String) {
    if (isActive) {
        StatusScreen(
            isActive = true,
            text = text,
            image = null
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFeedList(
    postData: List<GetPostDTO>,
    navHostController: NavHostController,
    profileImage: String,
    postFeedViewModel: PostFeedViewModel,
    bottomSheetSharedViewModel: BottomSheetSharedViewModel,
    context: Context,
    scrollBehavior: TopAppBarScrollBehavior,
    lazyState: LazyListState,
) {
    LazyColumn(
        state = lazyState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        // verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        writePost(navHostController, context, profileImage)

        item {
            HorizontalDivider(
                thickness = 12.dp,
                color = secondary
            )
        }
        
        postsLazyColumn(
            postData = postData,
            navHostController = navHostController,
            postFeedViewModel = postFeedViewModel,
            bottomSheetSharedViewModel = bottomSheetSharedViewModel,
            context = context,
        )
    }
}


