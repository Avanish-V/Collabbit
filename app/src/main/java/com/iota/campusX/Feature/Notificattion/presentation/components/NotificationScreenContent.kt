package com.iota.campusX.Feature.Notificattion.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.iota.campusX.Feature.Notificattion.presentation.event.NotificationUiEvent
import com.iota.campusX.Feature.Notificattion.presentation.mapper.toDomain
import com.iota.campusX.Feature.Notificattion.presentation.model.NotificationUi
import com.iota.campusX.Feature.Notificattion.presentation.states.NotificationUiState
import com.iota.campusX.Feature.Notificattion.presentation.utils.NotificationDateGroup
import com.iota.campusX.Feature.Post.presentation.feedmenu.ContentType
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuActionViewModel
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuContext
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuController
import com.iota.campusX.Feature.Reply.presentation.ReplyBottomSheet
import com.iota.campusX.Utils.extractReason
import com.iota.campusX.R
import java.util.Calendar
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationScreenContent(
    modifier: Modifier,
    uiState: NotificationUiState,
    notifications: LazyPagingItems<NotificationUi>,
    snackbarHostState: SnackbarHostState,
    menuController: MenuController,
    menuActionViewModel: MenuActionViewModel,
    onEvent: (NotificationUiEvent) -> Unit,
    onBack: () -> Unit
) {
    val pullState = rememberPullToRefreshState()
    var pendingAcceptNotification by remember { mutableStateOf<NotificationUi?>(null) }
    
    val scope = rememberCoroutineScope()
    val replySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        if (uiState.unreadCount > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = if (uiState.unreadCount > 99) "99+" else uiState.unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.unreadCount > 0) {
                        TextButton(
                            onClick = { onEvent(NotificationUiEvent.MarkAllRead) }
                        ) {
                            Text(
                                text = "Mark all read",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->

        PullToRefreshBox(
            state = pullState,
            isRefreshing = notifications.loadState.refresh is LoadState.Loading && notifications.itemCount > 0,
            onRefresh = {
                notifications.refresh()
                onEvent(NotificationUiEvent.Refresh)
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 80.dp
                )
            ) {
                val refreshState = notifications.loadState.refresh

                // ── Shimmer loading ──────────────────────────────────────────
                if (refreshState is LoadState.Loading && notifications.itemCount == 0) {
                    items(10) {
                        NotificationShimmer()
                    }
                }

                // ── Error state ──────────────────────────────────────────────
                if (refreshState is LoadState.Error) {
                    item {
                        NotificationErrorState(
                            message = refreshState.error.extractReason("Failed to load notifications"),
                            onRetry = { notifications.refresh() }
                        )
                    }
                }

                // ── Empty state ──────────────────────────────────────────────
                if (notifications.itemCount == 0 && refreshState is LoadState.NotLoading) {
                    item { NotificationEmptyState() }
                }

                // ── Items grouped by date ────────────────────────────────────
                var lastGroup: NotificationDateGroup? = null

                for (index in 0 until notifications.itemCount) {
                    val notification = notifications[index]

                    if (notification != null) {
                        val currentGroup = notification.createdAt.toNotificationDateGroup()

                        if (currentGroup != lastGroup) {
                            stickyHeader(key = "header_${currentGroup.name}_$index") {
                                NotificationSectionHeader(currentGroup)
                            }
                            lastGroup = currentGroup
                        }

                        item(key = notification.id) {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(200)) + expandVertically(tween(200))
                            ) {
                                NotificationCard(
                                    notification = notification,
                                    modifier = Modifier.animateItem(),
                                    onAccept = {
                                        pendingAcceptNotification = it
                                    },
                                    onReject = {
                                        onEvent(NotificationUiEvent.RejectConnectRequest(it.toDomain()))
                                    },
                                    onMessage = {
                                        onEvent(NotificationUiEvent.NotificationClicked(it.toDomain()))
                                    },
                                    onClick = {
                                        onEvent(
                                            NotificationUiEvent.NotificationClicked(
                                                notification.toDomain()
                                            )
                                        )
                                    }
                                )
                            }

                            // Thin divider between cards
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }

                // ── Append loading spinner ───────────────────────────────────
                if (notifications.loadState.append is LoadState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal to write required message before accepting connection
    pendingAcceptNotification?.let { notif ->
        AcceptConnectBottomSheet(
            notification = notif,
            onDismiss = { pendingAcceptNotification = null },
            onAccept = { message ->
                onEvent(
                    NotificationUiEvent.AcceptConnectRequest(
                        notification = notif.toDomain(),
                        message = message
                    )
                )
                pendingAcceptNotification = null
            }
        )
    }

    // Modal for Reply Bottom Sheet (Directly from notification)
    uiState.selectedPostIdForComments?.let { postId ->
        ReplyBottomSheet(
            postId = postId,
            onDismiss = {
                scope.launch {
                    replySheetState.hide()
                    onEvent(NotificationUiEvent.DismissReplySheet)
                }
            },
            sheetState = replySheetState,
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
            onAction = { }
        )
    }
}

@Composable
private fun NotificationEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth().fillMaxHeight()
            .padding(top = 80.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.bell),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "All caught up!",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "You have no notifications right now.\nWe'll let you know when something happens.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun NotificationErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.bell),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Try Again",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

fun Long.toNotificationDateGroup(): NotificationDateGroup {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().also { it.timeInMillis = this }

    return when {
        now.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR) ->
            NotificationDateGroup.TODAY

        now.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - date.get(Calendar.DAY_OF_YEAR) == 1 ->
            NotificationDateGroup.YESTERDAY

        else -> NotificationDateGroup.EARLIER
    }
}
