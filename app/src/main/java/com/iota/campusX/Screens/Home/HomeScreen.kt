package com.iota.campusX.Screens.Home

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Society.presentation.Screens.CampusEmptyState
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.UserProfileViewModel
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
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuState
import com.iota.campusX.Utils.CircularLoading
import com.iota.campusX.Utils.LoadingScreen
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.AppLabelText
import com.iota.campusX.ui.UIComponents.BadgeItem
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.UIComponents.FeedUI.FeedItem
import com.iota.campusX.ui.UIComponents.UserAvatar
import kotlinx.coroutines.launch
import org.apache.http.client.methods.RequestBuilder.options
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
    notificationViewModel: NotificationViewModel,
) {

    NotificationPermissionRequester()

    LaunchedEffect(Unit) {
        profileViewModel.getUserProfile()
    }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val switchState = homeViewModel.mode.collectAsState().value
    val chatBadgeCount by notificationViewModel.chatCount.collectAsState()

    val tabs by remember { mutableStateOf(listOf("Open", "Campus")) }

    LaunchedEffect(Unit) {
        notificationViewModel.getChatCount()
    }

    val feedMode = (switchState as? UiState.Success<FeedMode>)?.data

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background ,
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(if (isSystemInDarkTheme()) R.drawable.app_logo else R.drawable.app_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(32.dp),
                       // colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BadgeItem(
                            modifier = Modifier.size(24.dp),
                            itemCount = chatBadgeCount,
                            onBadgeClick = {
                                navHostController.navigate(Routes.Main.Notification.routes)
                            },
                            badgeIcon = R.drawable.notification_normal
                        )
                        BadgeItem(
                            modifier = Modifier .rotate(-45f).size(20.dp),
                            itemCount = chatBadgeCount,
                            onBadgeClick = {
                                navHostController.navigate(Routes.Main.ChatList.routes)
                            },
                            badgeIcon = R.drawable.send_regular
                        )
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

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            when {
                feedMode != null -> {

                    val currentPage = if (feedMode == FeedMode.OPEN) 0 else 1

                    val pagerState = rememberPagerState(
                        initialPage = currentPage,
                        pageCount = { tabs.size }
                    )

                    LaunchedEffect(pagerState.currentPage) {
                        val selectedMode = if (pagerState.currentPage == 0) FeedMode.OPEN else FeedMode.CAMPUS
                        homeViewModel.saveSwitchState(selectedMode)
                    }

                    Box(modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .background(color = MaterialTheme.colorScheme.surface)
                        .clip(MaterialTheme.shapes.small))
                    {
                        SingleChoiceSegmentedButtonRow (modifier = Modifier.fillMaxWidth().padding( 6.dp)){

                            tabs.forEachIndexed { index, label ->
                                SegmentedButton(
                                    shape = MaterialTheme.shapes.small,
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    icon = {},
                                    selected = index == pagerState.currentPage,
                                    label = { Text(label) },
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = MaterialTheme.colorScheme.primary,
                                        activeContentColor = Color.White
                                    ),
                                    border = BorderStroke(width = 0.dp, color = Color.Transparent)
                                )
                            }
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
            UserAvatar(
                imageUrl = profileImage,
                bgColor = "ghhgjhghjgjhghjg",
                modifier = Modifier.size(42.dp)
            )
//            AsyncImage(
//                modifier = Modifier
//                    .size(40.dp)
//                    .clip(CircleShape),
//                model = profileImage,
//                contentDescription = null,
//                contentScale = ContentScale.Crop
//            )
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


    val lazyState = rememberLazyListState()

    val globalPostState = postFeedViewModel.globalPosts.collectAsLazyPagingItems()
    val campusPostState = postFeedViewModel.campusPosts.collectAsLazyPagingItems()

    val profileState = userProfileViewModel.userBaseProfile.collectAsState().value

    val profileData = when(profileState){
        is UiState.Success<*> -> {
            (profileState as UiState.Success<BaseProfileDTO>).data
        }
        else -> {
            null
        }
    }

    HideBottomBar(
        navigationViewModel = navigationViewModel,
        lazyState = lazyState
    )

    LaunchedEffect(feedMode,profileData) {
        when (feedMode) {
            FeedMode.OPEN -> {
                if (globalPostState.itemCount == 0){
                    postFeedViewModel.fetchGlobalPost(
                        feedMode = FeedMode.OPEN,
                        campusId = null
                    )
                }
            }
            FeedMode.CAMPUS -> profileData?.campus?.code?.let {
                postFeedViewModel.fetchGlobalPost(
                    feedMode = FeedMode.CAMPUS,
                    campusId = it
                )

            }
        }
    }

    when (pageIndex) {
        0 -> FeedUiRenderer(
            feedData = globalPostState,
            navHostController = navHostController,
            lazyState = lazyState,
            scrollBehavior = scrollBehavior,
            userProfileImage = profileData?.image ?: "",
        )

        1 -> {
            when(profileState){
                is UiState.Loading -> {
                    LoadingScreen()
                }
                is UiState.Success->{
                    if (profileData?.campus?.code.isNullOrEmpty()){
                        CampusEmptyState(
                            onUpdateClick = {navHostController.navigate(Routes.Main.Profile.routes)}
                        )
                        return
                    }
                    FeedUiRenderer(
                        feedData  = campusPostState,
                        navHostController = navHostController,
                        lazyState = lazyState,
                        scrollBehavior = scrollBehavior,
                        userProfileImage = profileData.image?:"",
                    )
                }
                is UiState.Error -> {

                }
                else -> {}

            }

        }
    }

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshBox(
    modifier: Modifier = Modifier,
    pullToRefreshState: PullToRefreshState,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    content: @Composable () -> Unit
) {
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
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
                color = MaterialTheme.colorScheme.primary,
            )
        },
    ) {
        content()
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedUiRenderer(
    feedData: LazyPagingItems<GetPostDTO>,
    navHostController: NavHostController,
    lazyState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    userProfileImage: String,
    postMenuState: PostMenuState = koinInject(),
) {
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    val appNavigator: AppNavigator = remember { AppNavigatorImpl(navHostController) }
    //-----------------Pass parameter from outside------------
    val viewModel: PostActionViewModel = getKoin().get { parametersOf(appNavigator) }

    RefreshBox(
        pullToRefreshState = pullToRefreshState,
        onRefresh = {
            context.vibrate()
            feedData.refresh()
            scope.launch { lazyState.animateScrollToItem(0) }
        },
        isRefreshing = feedData.loadState.refresh is LoadState.Loading && feedData.itemCount > 0
    ) {

        PagingListHeader(
            items = feedData,
            emptyContent = {
                StatusScreen(
                    modifier =  Modifier.fillMaxSize(),
                    text = "No Posts Yet",
                    image = R.drawable.undraw_no_data_ig65,
                    description = "Share your thoughts or updates to let the world\nknow more about you!",
                    buttonText = "Create Post",
                    onClick = {
                        navHostController.navigate(Routes.Main.CreatePost.routes)
                        context.vibrate()
                    }
                )
            },
            showContent = {

                LazyColumn(
                    state = lazyState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                ) {

                    writePostComponent(navHostController, context, userProfileImage)

                    item {
                        HorizontalDivider(
                            thickness = 12.dp,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }

                    items(feedData.itemCount) { post ->
                        val item = feedData[post]
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
                                }
                            )
                            Divider()
                        }
                    }

                    item {
                        PagingListFooter(
                            items = feedData,
                            minItemsBeforeEnd = 16,
                            errorContent = {

                            }
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun <T : Any> PagingListFooter(
    items: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    minItemsBeforeEnd: Int = 0, // show "No more" only after some data is loaded
    loadingContent: @Composable (() -> Unit)? = {
        CircularLoading(MaterialTheme.colorScheme.primary)
    },
    errorContent: @Composable ((Throwable) -> Unit)? = { error ->
        AppLabelText(text = error.localizedMessage ?: "Something went wrong.")
    },
    endContent: @Composable (() -> Unit)? = {
        AppLabelText("🎉 You’ve reached the end!")
    }
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val append = items.loadState.append) {
            is LoadState.Loading -> {
                loadingContent?.invoke()
            }
            is LoadState.Error -> {
                errorContent?.invoke(append.error)
            }
            is LoadState.NotLoading -> {
                if (append.endOfPaginationReached && items.itemCount > minItemsBeforeEnd) {
                    endContent?.invoke()
                }
            }
        }
    }
}


@Composable
fun <T : Any> PagingListHeader(
    items: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    screenHeight: Dp? = null,
    loadingContent: @Composable (() -> Unit)? = {
        if (items.itemCount == 0){
            LoadingScreen(
                modifier = if (screenHeight == null) Modifier.fillMaxSize() else Modifier.height(screenHeight/2),
            )
        }
    },
    errorContent: @Composable ((Throwable) -> Unit)? = { error ->
        ErrorScreen(
            text = error.message.toString(),
            image = R.drawable.undraw_page_not_found_6wni,
            onReTry = {
                items.retry()
            },
            buttonText = "Retry"
        )

    },
    emptyContent: @Composable (() -> Unit)? = {},
    showContent: @Composable (() -> Unit)? = {},
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        when (val refresh = items.loadState.refresh) {
            is LoadState.Loading -> {
                loadingContent?.invoke()
            }
            is LoadState.Error -> {
                errorContent?.invoke(refresh.error)
                return
            }
            is LoadState.NotLoading -> {
                val endOfPaginationReached = items.loadState.append.endOfPaginationReached
                if (items.itemCount == 0 && endOfPaginationReached) {
                    emptyContent?.invoke()
                }else{
                    showContent?.invoke()
                }
            }

        }

    }
}
