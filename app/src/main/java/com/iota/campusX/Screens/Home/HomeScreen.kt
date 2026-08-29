package com.iota.campusX.Screens.Home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.iota.campusX.Feature.Notificattion.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.presentation.feed.FeedUiRenderer
import com.iota.campusX.Feature.Post.presentation.feed.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.feedmenu.ContentType
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuActionViewModel
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuContext
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuController
import com.iota.campusX.Feature.Reply.presentation.ReplyBottomSheet
import com.iota.campusX.Feature.UserProfile.ui.screens.ProfileMain.UserProfileViewModel
import com.iota.campusX.Navigation.ChatList
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Notification
import com.iota.campusX.Navigation.PostView
import com.iota.campusX.Navigation.ViewProfile
import com.iota.campusX.Navigation.rememberScrollContext
import com.iota.campusX.Permissions.NotificationPermissionRequester
import com.iota.campusX.R
import com.iota.campusX.Utils.CircularLoading
import com.iota.campusX.Utils.LoadingScreen
import com.iota.campusX.ui.UIComponents.AppLabelText
import com.iota.campusX.ui.UIComponents.BadgeItem
import com.iota.campusX.ui.UIComponents.ErrorScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navHostController: NavHostController,
    profileViewModel: UserProfileViewModel,
    postFeedViewModel: PostFeedViewModel = koinInject(),
    menuController : MenuController,
    menuActionViewModel: MenuActionViewModel,
    notificationViewModel: NotificationViewModel,
    navigationViewModel: NavigationViewModel = koinInject()
) {

    NotificationPermissionRequester()

    val lazyState = rememberLazyListState()
    val scrollContext = rememberScrollContext(navigationViewModel)
    HideBottomBar(navigationViewModel, lazyState)

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        profileViewModel.getUserProfile()
    }

    val notificationBadge by notificationViewModel.unreadCount.collectAsStateWithLifecycle()

    var showReplyBottomSheet by remember { mutableStateOf(false) }
    var replyPostId by remember { mutableStateOf<String?>(null) }


    val snackBarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val allPostState = postFeedViewModel.allPosts.collectAsLazyPagingItems()
    val replyBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = if(isSystemInDarkTheme()) painterResource(R.drawable.finder_logo_dark) else painterResource(R.drawable.finder_logo),
                        contentDescription = "CampusX",
                        modifier = Modifier.size(80.dp)
                    )
                },
                actions = {

                    Row(modifier = Modifier.padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {

                        IconButton(onClick = { navHostController.navigate(ChatList) }) {
                            Icon(
                                painter = painterResource(R.drawable.messages__1_),
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.padding(12.dp))

                        BadgeItem(
                            modifier = Modifier,
                            itemCount = notificationBadge.toInt(),
                            onBadgeClick = {
                                navHostController.navigate(Notification)
                            },
                            badgeIcon = R.drawable.bell
                        )

                    }

                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    containerColor = MaterialTheme.colorScheme.background
                ),

                )
        },
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.padding(bottom = 100.dp),
                hostState = snackBarHostState
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).nestedScroll(scrollContext)
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            FeedUiRenderer(
                feed = allPostState,
                lazyState = lazyState,
                scrollBehavior = scrollBehavior,
                feedViewModel = postFeedViewModel,
                onReplyClick = { post ->
                    replyPostId = post.postId
                    showReplyBottomSheet = true
                    scope.launch { replyBottomSheetState.show() }
                },
                onMoreClick = {

                    menuController.show(
                        MenuContext(
                            id = it.postId,
                            type = ContentType.POST,
                            isOwner = it.author.isCurrentUser
                        )
                    )

                    menuActionViewModel.loadMenu(
                            MenuContext(
                            id = it.postId,
                            type = ContentType.POST,
                            isOwner = it.author.isCurrentUser
                        ),
                    )
                },
                onImageClick = { post ->
                    navHostController.navigate(PostView(postId = post.postId))
                },
                onProfileClick = {navHostController.navigate(ViewProfile(it))}
            )
        }
        if (showReplyBottomSheet) {
            replyPostId?.let { postId ->
                ReplyBottomSheet(
                    postId = postId,
                    onDismiss = {
                        showReplyBottomSheet = false
                        replyPostId = null
                    },
                    sheetState = replyBottomSheetState,
                    onMoreClick = {
                        menuController.show(
                            MenuContext(
                                id = it.id,
                                type = ContentType.REPLY,
                                isOwner = it.author.isCurrentUser
                            )
                        )
                        menuActionViewModel.loadMenu(
                            MenuContext(
                                id = it.id,
                                type = ContentType.REPLY,
                                isOwner = it.author.isCurrentUser
                            ),
                        )
                    },
                    onAction = {

                    }
                )
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
                containerColor = MaterialTheme.colorScheme.onPrimary
            )
        },
    ) {
        content()
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
        LoadingScreen(
            modifier = if (screenHeight == null) Modifier.fillMaxSize() else Modifier.height(screenHeight / 2),
        )
    },
    errorContent: @Composable ((String) -> Unit)? = { error ->
        ErrorScreen(
            text = error,
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
        
        when {
            items.loadState.refresh is LoadState.Loading -> {
                loadingContent?.invoke()
            }
            items.loadState.refresh is LoadState.Error -> {
                errorContent?.invoke("Something went wrong")
            }
            else -> {
                val endOfPaginationReached = items.loadState.append.endOfPaginationReached
                if (items.itemCount == 0 && endOfPaginationReached) {
                    emptyContent?.invoke()
                } else {
                    showContent?.invoke()
                }
            }
        }
    }
}

@Composable
fun CampusEmptyState(onUpdateClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // A fun icon
            Icon(
                painter = painterResource(R.drawable.school__1_),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(64.dp)
            )

            // Main message
            Text(
                "No Campus Found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Catchy subtext
            Text(
                "Update your campus detail to join the societies",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick = { onUpdateClick.invoke() },
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(top = 8.dp),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text("Update")
            }
        }
    }
}
