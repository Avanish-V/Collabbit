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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Notification.domain.GetNotification
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.UserProfile.presentation.ConnectionRequestState
import com.iota.campusX.Feature.UserProfile.presentation.ConnectionRequestViewModel
import com.iota.campusX.Feature.UserProfile.presentation.ConnectionState
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.RefreshBox
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.CircularLoading
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.LikeRail
import com.iota.campusX.ui.UIComponents.PostHeader
import com.iota.campusX.ui.UIComponents.toMillis
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

    LaunchedEffect(rejectState) {
        when (rejectState) {
            is UiState.Loading -> {}
            is UiState.Success -> {
                snackBarHostState.showSnackbar("Connection rejected")
                notificationViewModel.deleteNotification(deleteNotification?.notificationId.toString())
                deleteNotification?.let { notificationViewModel.deleteNotificationFromList(it) }
            }
            is UiState.Error -> {
                snackBarHostState.showSnackbar("Something went wrong!")
            }
            else -> {

            }
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
            isRefreshing = notificationPageData.loadState.refresh == LoadState.Loading && notificationPageData.itemCount > 0,
            onRefresh = {
                notificationPageData.refresh()
            },
            pullToRefreshState = pullToRefreshState,
        ) {


            if (notificationPageData.loadState.refresh == LoadState.Loading && notificationPageData.itemCount == 0 ){
                LoadingUI()
            }


            LazyColumn(
                state = lazyState,
                contentPadding = PaddingValues(12.dp)
            ) {

                items(notificationPageData.itemCount) {notification->

                    notificationPageData[notification]?.let { notification ->

                        NotificationItem(
                            notificationItem = notification,
                            scope = scope,
                            notificationViewModel = notificationViewModel,
                            onNotificationClick = {

                            },
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
                            snackBarHostState = snackBarHostState,
                            acceptState = acceptState,
                            rejectState = rejectState,
                            onAccept = {
                                connectionRequestViewModel.request(
                                    ConnectionRequestState.AcceptConnectionRequest(it)
                                )
                                deleteNotification = notification
                            },
                            onReject = {
                                connectionRequestViewModel.request(
                                    ConnectionRequestState.RejectConnectionRequest(it)
                                )
                                deleteNotification = notification
                            }
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                    }

                }
                item {
                    if (notificationPageData.loadState.append == LoadState.Loading) {
                        CircularLoading(color = MaterialTheme.colorScheme.primary)
                    }
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
    snackBarHostState: SnackbarHostState,
    notificationViewModel: NotificationViewModel,
    scope: CoroutineScope,
    onNotificationClick: () -> Unit,
    geToPost: (String) -> Unit,
    geToUserProfile: (String) -> Unit,
    acceptState: UiState<Boolean>? = null,
    rejectState: UiState<Boolean>? = null,
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Row(verticalAlignment = Alignment.Top) {

                        val names = mutableListOf<String>()
                        notificationItem.likes.take(2).forEach { names.add(it.userName) }

                        Text(
                            modifier = Modifier.weight(1f),
                            text = buildAnnotatedString {

                                withStyle(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                ) {
                                    append(names.joinToString(", "))
                                }

                                if (notificationItem.likes.count() >= 3) {
                                    append(" and ")
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    ) {
                                        append("${notificationItem.likesCount-names.count()} others")
                                    }
                                }

                                append(" upvoted your post")

                            },
                            maxLines = 2,
                            textAlign = TextAlign.Start,
                            color = Color.Gray
                        )

                        if (!notificationItem.isRead){
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color = MaterialTheme.colorScheme.primary))

                        }


                    }

                    Row(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.small
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(6.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { geToPost.invoke(notificationItem.postId) }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        if (notificationItem.postContent?.text.isNullOrEmpty() && notificationItem.postContent?.text.isNullOrEmpty()){
                            Text("Content no longer available.",color = MaterialTheme.colorScheme.outline)
                            return
                        }

                        if (notificationItem.postContent.image.isNotEmpty()) {

                            AsyncImage(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                model = notificationItem.postContent.image.toString(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                            )

                        }

                        if (notificationItem.postContent.text.isNotEmpty()) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = notificationItem.postContent.text.toString(),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                    }
                    Text(
                        modifier = Modifier
                            .alpha(0.6f)
                            .align(Alignment.End),
                        text = getTimeAgo(notificationItem.createdAt.toMillis()),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

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

                        val names = mutableListOf<String>()
                        notificationItem.replyUsers.take(2).forEach { names.add(it.userName) }

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

                                if (notificationItem.replyUsers.count() >= 3) {
                                    append(" and ")
                                    withStyle(
                                        style = SpanStyle(
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    ) {
                                        append("${notificationItem.replyUsers.count()-names.count()} others")
                                    }
                                }

                                append(" replied your post")

                            },
                            textAlign = TextAlign.Start,
                            color = Color.Gray
                        )

                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.small
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(6.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    geToPost.invoke(notificationItem.postId)
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        if (!notificationItem.postContent?.image.isNullOrEmpty()) {

                            AsyncImage(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                model = notificationItem.postContent.image.toString(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                            )

                        }

                        if (!notificationItem.postContent?.text.isNullOrEmpty()) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = notificationItem.postContent.text.toString(),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        modifier = Modifier
                            .alpha(0.6f)
                            .align(Alignment.End),
                        text = getTimeAgo(notificationItem.createdAt.toMillis()),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                }

            }

        }

        is GetNotification.ConnectionRequestNotification -> {

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                CircleImage(
                    image = notificationItem.actionBy.profile?.userImage ?: "",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    onClick = {
                        notificationItem.actionBy.profile?.id?.let { geToUserProfile.invoke(it) }
                    },
                    visibility = VisibilityMode.USER

                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Column {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = notificationItem.actionBy.profile?.userName ?: "",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (notificationItem.actionBy.isVerified) {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    painter = painterResource(R.drawable.baseline_verified_24),
                                    contentDescription = "Verified",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Button(
                            enabled = acceptState !is UiState.Success,
                            onClick = {
                                notificationItem.actionBy.profile?.let { onAccept.invoke(it.id) }
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            when (acceptState) {
                                is UiState.Loading -> {
                                    CircularLoading(Color.White)
                                }

                                is UiState.Success -> {

                                    Text("Accepted")
                                    notificationViewModel.deleteNotification(notificationItem.notificationId.toString())
                                    notificationViewModel.deleteNotificationFromList(notificationItem)

                                }

                                is UiState.Error -> {
                                    LaunchedEffect(Unit) {
                                        snackBarHostState.showSnackbar(acceptState.message)
                                    }
                                }

                                else -> {
                                    Text("✔ Accept")
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                notificationItem.actionBy.profile?.id?.let { onReject.invoke(it) }
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text("✖ Decline")
                        }
                    }

                    Text(
                        modifier = Modifier.alpha(0.6f).align(Alignment.End),
                        text = getTimeAgo(notificationItem.createdAt.toMillis()),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

}
