package com.iota.campusX.Screens.Home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Badge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Share
import com.iota.campusX.Utils.shareSociety
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.Feature.Notificattion.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.presentation.feed.FeedUiRenderer
import com.iota.campusX.Feature.Post.presentation.feed.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.feedmenu.ContentType
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuActionViewModel
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuContext
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuController
import com.iota.campusX.Feature.Society.presentation.SocietyViewModel
import com.iota.campusX.Feature.Reply.presentation.ReplyBottomSheet
import com.iota.campusX.Feature.UserProfile.ui.screens.ProfileMain.UserProfileViewModel
import com.iota.campusX.Navigation.ChatList
import com.iota.campusX.Navigation.CreatePost
import com.iota.campusX.Navigation.SocietyHub
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Notification
import com.iota.campusX.Feature.Post.domain.attachment.AttachmentDto
import com.iota.campusX.Feature.Post.domain.attachment.DocumentAttachmentDto
import com.iota.campusX.Feature.Post.domain.attachment.ImageAttachmentDto
import com.iota.campusX.Feature.Post.domain.attachment.VideoAttachmentDto
import com.iota.campusX.Navigation.PostView
import com.iota.campusX.Utils.sharePost
import com.iota.campusX.Navigation.VideoView
import com.iota.campusX.Navigation.PdfView
import com.iota.campusX.Navigation.Society
import com.iota.campusX.Navigation.CommunityChat
import com.iota.campusX.Navigation.CreateSociety
import com.iota.campusX.Navigation.ViewProfile
import com.iota.campusX.Permissions.NotificationPermissionRequester
import com.iota.campusX.R
import com.iota.campusX.Utils.CircularLoading
import com.iota.campusX.Utils.LoadingScreen
import com.iota.campusX.ui.UIComponents.AppLabelText
import com.iota.campusX.ui.UIComponents.BadgeItem
import com.iota.campusX.ui.UIComponents.ErrorScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


import androidx.compose.runtime.mutableIntStateOf
import com.iota.campusX.Feature.Society.domain.model.Community
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.cardShadow

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
    navigationViewModel: NavigationViewModel = koinInject(),
    societyViewModel: SocietyViewModel = koinInject(),
    chatsViewModel: ChatsViewModel = koinInject(),
    targetPostId: String? = null
) {
    val context = LocalContext.current

    NotificationPermissionRequester()

    val lazyState = rememberLazyListState()
    HideBottomBar(navigationViewModel, lazyState)

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        profileViewModel.getUserProfile()
    }

    val notificationBadge by notificationViewModel.unreadCount.collectAsStateWithLifecycle()
    val chatBadge by chatsViewModel.unreadMessageCount.collectAsStateWithLifecycle()
    val joinedCommunities by societyViewModel.joinedCommunities.collectAsStateWithLifecycle()
    val isSocietyRefreshing by societyViewModel.isRefreshing.collectAsStateWithLifecycle()

    var showReplyBottomSheet by remember { mutableStateOf(false) }
    var replyPostId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(targetPostId) {
        if (targetPostId != null) {
            postFeedViewModel.fetchSinglePost(targetPostId, setAsFocused = true)
        }
    }

    val snackBarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val allPostState = postFeedViewModel.allPosts.collectAsLazyPagingItems()
    val replyBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    val gilroyFontFamily = FontFamily(
                        Font(R.font.gilroy_extrabold, weight = FontWeight.ExtraBold)
                    )

                    Text(
                        text = "Collabbit",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = gilroyFontFamily,
                        color = MaterialTheme.colorScheme.primary

                    )

                },
                actions = {

                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        BadgeItem(
                            modifier = Modifier.size(20.dp),
                            itemCount = chatBadge,
                            onBadgeClick = {
                                navHostController.navigate(ChatList)
                            },
                            badgeIcon = R.drawable.messages__1_,
                            contentDescription = "Messages"
                        )

                        BadgeItem(
                            modifier = Modifier.size(22.dp),
                            itemCount = notificationBadge.toInt(),
                            onBadgeClick = {
                                navHostController.navigate(Notification)
                            },
                            badgeIcon = R.drawable.bell,
                            contentDescription = "Notifications"
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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabRow(
                    modifier = Modifier.weight(1f),
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    divider = {
                        Divider()
                    },
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = MaterialTheme.colorScheme.primary,
                                height = 3.dp
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                text = "For You",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                text = "Society",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                }
            }

            if (selectedTabIndex == 0) {
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
                    onShareClick = { post ->
                        val imageUrl = when (val attachment = post.attachment) {
                            is ImageAttachmentDto -> attachment.images.firstOrNull()
                            is VideoAttachmentDto -> attachment.thumbnailUrl
                            is DocumentAttachmentDto -> attachment.thumbnailUrl
                            else -> null
                        }
                        scope.launch {
                            sharePost(context, post.postId, post.caption, imageUrl)
                        }
                    },
                    onMoreClick = {
                        menuActionViewModel.showMenu(
                            menuController = menuController,
                            context = MenuContext(
                                id = it.postId,
                                type = ContentType.POST,
                                isOwner = it.author.isCurrentUser
                            )
                        )
                    },
                    onImageClick = { post, index ->
                        val attachment = post.attachment
                        when (attachment) {
                            is VideoAttachmentDto -> {
                                navHostController.navigate(VideoView(videoUrl = attachment.videoUrl, thumbnailUrl = attachment.thumbnailUrl))
                            }
                            is DocumentAttachmentDto -> {
                                navHostController.navigate(
                                    PdfView(
                                        pdfUrl = attachment.url, 
                                        fileName = attachment.name,
                                        thumbnailUrl = attachment.thumbnailUrl
                                    )
                                )
                            }
                            is ImageAttachmentDto -> {
                                val imageUrl = attachment.images.getOrNull(index)
                                navHostController.navigate(PostView(postId = post.postId, postImage = imageUrl, initialIndex = index))
                            }
                            else -> {
                                navHostController.navigate(PostView(postId = post.postId, initialIndex = index))
                            }
                        }
                    },
                    onProfileClick = { navHostController.navigate(ViewProfile(it)) }
                )
            } else if (selectedTabIndex == 1) {
                SocietyList(
                    communities = joinedCommunities,
                    currentUserId = societyViewModel.currentUserId,
                    isRefreshing = isSocietyRefreshing,
                    onRefresh = { societyViewModel.refreshCommunities() },
                    onCreateClick = { navHostController.navigate(CreateSociety) },
                    onDiscoverClick = { navHostController.navigate(SocietyHub) },
                    onCommunityClick = { community -> navHostController.navigate(CommunityChat(community.id)) }
                )
            }
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
                        menuActionViewModel.showMenu(
                            menuController = menuController,
                            context = MenuContext(
                                id = it.id,
                                type = ContentType.REPLY,
                                isOwner = it.author.isCurrentUser
                            )
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
fun SocietyList(
    modifier: Modifier = Modifier,
    communities: List<Community>,
    currentUserId: String?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onCreateClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    onCommunityClick: (Community) -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()
    
    RefreshBox(
        pullToRefreshState = pullToRefreshState,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Create or Discover Society Card
            item {
                Card(
                    onClick = { onDiscoverClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .cardShadow(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(52.dp)
                        ) {
                            // Icon Background (Squircle)
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.people),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color.White
                                )
                            }

                            // Green Plus Badge
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(22.dp)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                shape = CircleShape,
                                color = Color(0xFF1EBE71)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.padding(2.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "New community",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Create or Discover societies",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (communities.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "You haven't joined any societies yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(communities, key = { it.id }) { community ->
                    SocietyItem(
                        community = community,
                        onClick = { onCommunityClick(community) }
                    )
                }
            }
        }
    }
}

@Composable
fun SocietyItem(
    community: Community,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().cardShadow(alpha = 0.5f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (community.logoUrl != null) {
                    AsyncImage(
                        model = community.logoUrl,
                        contentDescription = community.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = community.name.take(1).uppercase(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = community.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${community.memberCount} members",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = community.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (community.unreadCount > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = if (community.unreadCount > 99) "99+" else community.unreadCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
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
                val error = (items.loadState.refresh as LoadState.Error).error
                errorContent?.invoke(error.localizedMessage ?: "Something went wrong")
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
