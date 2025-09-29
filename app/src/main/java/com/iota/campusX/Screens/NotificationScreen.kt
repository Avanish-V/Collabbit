package com.iota.campusX.Screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Notification.data.PostContent
import com.iota.campusX.Feature.Notification.domain.GetNotification
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.UserProfile.presentation.ConnectionRequestState
import com.iota.campusX.Feature.UserProfile.presentation.ConnectionRequestViewModel
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.PagingListFooter
import com.iota.campusX.Screens.Home.PagingListHeader
import com.iota.campusX.Screens.Home.RefreshBox
import com.iota.campusX.Utils.CircularLoading
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.FeedUI.LikeRail
import com.iota.campusX.ui.UIComponents.FeedUI.toMillis
import kotlinx.coroutines.CoroutineScope
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@SuppressLint("UnrememberedGetBackStackEntry")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navigationViewModel: NavigationViewModel,
    userProfileViewModel: UserProfileViewModel,
    connectionRequestViewModel: ConnectionRequestViewModel = koinInject(),
    notificationViewModel: NotificationViewModel = koinViewModel(),
    navHostController: NavHostController,
) {

    val notificationPageData = notificationViewModel.notification.collectAsLazyPagingItems()
    val notificationCount by notificationViewModel.notificationCount.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState()
    val showBottomSheet = remember { mutableStateOf(false) }

    val acceptState by connectionRequestViewModel.acceptRequestState.collectAsState()
    val rejectState by connectionRequestViewModel.rejectRequestState.collectAsState()
    val deleteState by notificationViewModel.deleteNotificationState.collectAsState()

    var deleteNotification by remember { mutableStateOf<GetNotification?>(null) }

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
       // notificationViewModel.fetchNotifications()
        notificationViewModel.markNotificationAsRead()
    }

    val lazyState = rememberLazyListState()

    HideBottomBar(
        lazyState = lazyState,
        navigationViewModel = navigationViewModel
    )

    val scope = rememberCoroutineScope()

    val snackBarHostState = SnackbarHostState()

    LaunchedEffect(acceptState) {
        when(acceptState){
            is UiState.Success->{
                deleteNotification?.let { notificationViewModel.deleteNotification(it.notificationId) }
            }
            is UiState.Error->{
                snackBarHostState.showSnackbar("Something went wrong!")
            }
            else -> {}
        }
    }
    LaunchedEffect(rejectState) {
        when(rejectState){
            is UiState.Success->{
                deleteNotification?.let { notificationViewModel.deleteNotification(it.notificationId) }
            }
            is UiState.Error->{
                snackBarHostState.showSnackbar("Something went wrong!")
            }
            else -> {}
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Notification", style = MaterialTheme.typography.titleLarge)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.padding(bottom = 80.dp)
            )
        }

    ) { innerPadding ->

        RefreshBox(
            modifier = Modifier.padding(innerPadding),
            isRefreshing = notificationPageData.loadState.refresh is LoadState.Loading,
            onRefresh = {
                notificationPageData.refresh()
            },
            pullToRefreshState = pullToRefreshState,
        ) {


            PagingListHeader(
                items = notificationPageData,
                emptyContent = {
                    StatusScreen(
                        modifier =  Modifier.fillMaxSize(),
                        text = "No Notifications.",
                        image = R.drawable.undraw_my_notifications_fy5v,
                    )
                }
            )

            LazyColumn(
                state = lazyState,
                contentPadding = PaddingValues(12.dp)
            ) {

                items(notificationPageData.itemCount) {notification->


                    notificationPageData[notification]?.let { notification ->

                        NotificationItem(
                            notificationItem = notification,
                            geToPost = {
                                navHostController.navigate(Routes.Main.ReplyPost.routes).apply {
                                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                        "POST_ID",
                                        it
                                    )
                                }
                            },
                            geToUserProfile = {
                                navHostController.navigate(Routes.Main.ProfileByID.routes).apply {
                                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                        "USER_ID",
                                        it
                                    )
                                }
                            },
                            onAccept = {
                                connectionRequestViewModel.request(
                                    state = ConnectionRequestState.AcceptConnectionRequest(it)
                                )
                                deleteNotification = notification
                            },
                            onReject = {
                                connectionRequestViewModel.request(
                                    state = ConnectionRequestState.RejectConnectionRequest(it)
                                )
                                deleteNotification = notification
                            },
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                    }

                }

                item {
                    PagingListFooter(
                        items = notificationPageData,
                        minItemsBeforeEnd = 16, // don’t show "No more" too early
                    )
                }
            }

        }

        Box(modifier = Modifier.padding(innerPadding)) {

            if (showBottomSheet.value) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet.value = false },
                    sheetState = bottomSheetState,
                ) {


                }
            }

        }
    }
}

@Composable
fun NotificationItem(
    notificationItem: GetNotification,
    geToPost: (String) -> Unit,
    geToUserProfile: (String) -> Unit,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
) {
    when (notificationItem) {

        is GetNotification.LikeNotification -> {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                LikeRail(notificationItem.likes)

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(verticalAlignment = Alignment.Top) {

                        val names = remember(notificationItem.likes) {
                            notificationItem.likes.take(2).map { it.userName }
                        }

                        UserNamesText(
                            names = names,
                            othersCount = notificationItem.likesCount,
                            actionText = stringResource(R.string.upvoted_your_post)
                        )

                        if (!notificationItem.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    PostPreview(
                        postContent = notificationItem.postContent,
                        onClick = { geToPost(notificationItem.postId) }
                    )

                    NotificationTimeText(notificationItem.createdAt.toMillis())
                }
            }
        }

        is GetNotification.CommentNotification -> {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                LikeRail(notificationItem.replyUsers)

                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val names = remember(notificationItem.replyUsers) {
                            notificationItem.replyUsers.take(2).map { it.userName }
                        }

                        UserNamesText(
                            names = names,
                            othersCount = notificationItem.replyUsers.size - names.size,
                            actionText = stringResource(R.string.replied_your_post)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    PostPreview(
                        postContent = notificationItem.postContent,
                        onClick = { geToPost(notificationItem.postId) }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    NotificationTimeText(notificationItem.createdAt.toMillis())
                }
            }
        }

        is GetNotification.ConnectionRequestNotification -> {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircleImage(
                    image = notificationItem.actionBy.profile?.userImage ?: "",
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    onClick = {
                        notificationItem.actionBy.profile?.id?.let(geToUserProfile)
                    },
                    visibility = VisibilityMode.USER
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = notificationItem.actionBy.profile?.userName.orEmpty(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (notificationItem.actionBy.isVerified) {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                painter = painterResource(R.drawable.baseline_verified_24),
                                contentDescription = stringResource(R.string.verified),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                notificationItem.actionBy.profile?.id?.let(onAccept)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(stringResource(R.string.accept))
                        }

                        Button(
                            onClick = {
                                notificationItem.actionBy.profile?.id?.let(onReject)
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(stringResource(R.string.declined))
                        }
                    }

                    NotificationTimeText(notificationItem.createdAt.toMillis())
                }
            }
        }
    }
}

@Composable
fun UserNamesText(
    names: List<String>,
    othersCount: Int,
    actionText: String
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold
                )
            ) {
                append(names.joinToString(", "))
            }
            if (othersCount > names.count()) {
                append(" " + stringResource(R.string.and) + " ")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append("$othersCount " + stringResource(R.string.others))
                }
            }
            append(" $actionText")
        },
        maxLines = 2,
        textAlign = TextAlign.Start,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun PostPreview(
    postContent: PostContent?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.small
            )
            .padding(6.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when {
            postContent == null -> {
                Text(
                    stringResource(R.string.content_unavailable),
                    color = MaterialTheme.colorScheme.outline
                )
            }
            postContent.image.isNotEmpty() -> {
                AsyncImage(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)),
                    model = postContent.image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
        }

        if (!postContent?.text.isNullOrEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = postContent.text,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun NotificationTimeText(timeMillis: Long) {
    Text(
        modifier = Modifier.fillMaxWidth().alpha(0.6f),
        text = getTimeAgo(timeMillis),
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.End
    )
}
