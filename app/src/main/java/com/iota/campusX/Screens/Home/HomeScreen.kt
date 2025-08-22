package com.iota.campusX.Screens.Home

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.AppNavigator
import com.iota.campusX.Navigation.AppNavigatorImpl
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.Permissions.NotificationPermissionRequester
import com.iota.campusX.R
import com.iota.campusX.Screens.Post.DataModel.ContentId
import com.iota.campusX.Screens.Post.DataModel.ContentType
import com.iota.campusX.Screens.Post.DataModel.FeedContent
import com.iota.campusX.Screens.Post.PostActions.PostActionViewModel
import com.iota.campusX.Screens.Post.PostManupulation.PostFeedViewModel
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuState
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.UIComponents.PostCard
import com.iota.campusX.ui.theme.LightTheme_Blue
import kotlinx.coroutines.launch
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navHostController: NavHostController,
    navigationViewModel: NavigationViewModel,
    profileViewModel: UserProfileViewModel,
    homeViewModel: HomeViewModel,
    notificationViewModel: NotificationViewModel
) {

    NotificationPermissionRequester()

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val switchState = homeViewModel.mode.collectAsState().value
    val chatBadgeCount by notificationViewModel.chatCount.collectAsState()

    val tabs by remember { mutableStateOf(listOf("Global", "Campus")) }

    LaunchedEffect(Unit) {
        notificationViewModel.getChatCount()
    }

    val feedMode = (switchState as? UiState.Success<FeedMode>)?.data

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface ,
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(if (isSystemInDarkTheme()) R.drawable.mentor_logo else R.drawable.mentor_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(80.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BadgedBox(
                            badge = {
                                if (chatBadgeCount != 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(Color.Red, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
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
                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                            ) {
                                Icon(

                                    modifier = Modifier
                                        .size(22.dp)
                                        .rotate(-45f),
                                    painter = painterResource(R.drawable.send_solid),
                                    contentDescription = "Message"
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.padding(bottom = 100.dp),
                hostState = snackBarHostState
            )
        },
    ) { innerPadding ->

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                feedMode != null -> {

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
                        divider = { Divider() },
                        containerColor = MaterialTheme.colorScheme.background,
                        indicator = {
                            TabRowDefaults.PrimaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                                width = 48.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }

                                },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                selectedContentColor = MaterialTheme.colorScheme.onBackground,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    HorizontalPager(state = pagerState) { page ->
                        FeedComponent(
                            navHostController = navHostController,
                            scrollBehavior = scrollBehavior,
                            pageIndex = page,
                            userProfileViewModel = profileViewModel,
                            feedMode = feedMode,
                            navigationViewModel = navigationViewModel
                        )
                    }
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun LazyListScope.writePostComponent(
    navHostController: NavHostController,
    context: Context,
    profileImage: String
) {
    item {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall
                )

                Icon(
                    painter = painterResource(R.drawable.write),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedComponent(
    navHostController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    pageIndex: Int,
    userProfileViewModel: UserProfileViewModel,
    feedMode: FeedMode,
    navigationViewModel: NavigationViewModel,
    postFeedViewModel: PostFeedViewModel = koinInject()

) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    val lazyState = rememberLazyListState()

    val globalPostState by postFeedViewModel.globalPosts.collectAsState()
    val campusPostState by postFeedViewModel.campusPosts.collectAsState()

    val profileState = userProfileViewModel.userBaseProfile.collectAsState().value
    val profileData = (profileState as? UiState.Success)?.data

    HideBottomBar(
        navigationViewModel = navigationViewModel,
        lazyState = lazyState
    )

    LaunchedEffect(feedMode,profileData) {
        when (feedMode) {
            FeedMode.GLOBAL -> postFeedViewModel.fetchGlobalPosts()
            FeedMode.CAMPUS -> profileData?.campus?.campusCode?.let { postFeedViewModel.fetchCampusPosts(it) }
        }
    }

    RefreshBox(
        pullToRefreshState = pullToRefreshState,
        onRefresh = {
            context.vibrate()
            when (feedMode) {
                FeedMode.GLOBAL -> postFeedViewModel.fetchGlobalPosts()
                FeedMode.CAMPUS -> profileData?.campus?.campusCode?.let { postFeedViewModel.fetchCampusPosts(it) }
            }
            scope.launch { lazyState.animateScrollToItem(0) }
        },
        isRefreshing = globalPostState is UiState.Loading || campusPostState is UiState.Loading
    ) {
        Column {
            when (pageIndex) {
                0 -> FeedUiRenderer(
                    state = globalPostState,
                    navHostController = navHostController,
                    postFeedViewModel = postFeedViewModel,
                    lazyState = lazyState,
                    scrollBehavior = scrollBehavior,
                    userProfileImage = profileData?.userImage ?: "",

                )

                1 -> FeedUiRenderer(
                    state = campusPostState,
                    navHostController = navHostController,
                    postFeedViewModel = postFeedViewModel,
                    lazyState = lazyState,
                    scrollBehavior = scrollBehavior,
                    userProfileImage = profileData?.userImage ?: "",
                )
            }
        }
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
                color = LightTheme_Blue,
                containerColor = Color.White
            )
        },
    ) {
        content()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PostFeedList(
    postData: List<GetPostDTO>,
    navHostController: NavHostController,
    context: Context,
    scrollBehavior: TopAppBarScrollBehavior,
    lazyState: LazyListState,
    userProfileImage: String,
    postMenuState: PostMenuState = koinInject(),
) {

    //-----------------Do inject navHostController----
    val appNavigator: AppNavigator = remember { AppNavigatorImpl(navHostController) }
    //-----------------Pass parameter from outside------------
    val viewModel: PostActionViewModel = getKoin().get { parametersOf(appNavigator) }

    LazyColumn(
        state = lazyState,
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
    ) {
        writePostComponent(navHostController, context, userProfileImage)

        item {
            HorizontalDivider(
                thickness = 12.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )
        }

        items(postData, key = { it.postId }) { post ->
            PostCard(
                post = post,
                handlers = {
                    viewModel.onAction(it)
                },
                onDotMenuClick = {
                    postMenuState.open(
                        FeedContent(
                            id = ContentId.Post(postId = post.postId),
                            text = post.postContent.postData.postText,
                            isOwner = post.creatorDetail.isCurrentUser,
                            type = ContentType.POST
                        )
                    )
                }
            )
            Divider()
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedUiRenderer(
    state: UiState<List<GetPostDTO>>,
    navHostController: NavHostController,
    postFeedViewModel: PostFeedViewModel,
    lazyState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    userProfileImage: String,
) {
    when (state) {
        is UiState.Loading -> LoadingUI(isLoading = true)

        is UiState.Error -> {
            Log.e("FeedUiRenderer", "FeedUiRenderer: ${state.message}")
            ErrorScreen(
                text = state.message,
                image = null,
                onReTry = { postFeedViewModel.fetchGlobalPosts() },
                buttonText = "Retry"
            )
        }

        is UiState.Success<*> -> {
            val posts = state.data as List<GetPostDTO>
            PostFeedList(
                postData = posts.sortedByDescending { it.createdAt },
                navHostController = navHostController,
                context = LocalContext.current,
                scrollBehavior = scrollBehavior,
                lazyState = lazyState,
                userProfileImage = userProfileImage,
            )
        }

        UiState.Idle -> {}
    }
}

