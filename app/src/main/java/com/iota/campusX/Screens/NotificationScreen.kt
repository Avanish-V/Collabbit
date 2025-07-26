package com.iota.campusX.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Notification.domain.NotificationDTO
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.CircularLoading
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.LightTheme_Gray
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.LightTheme_Black
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White
import com.iota.campusX.ui.theme.LightTheme_White
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navigationViewModel: NavigationViewModel,
    userProfileViewModel: UserProfileViewModel,
    navHostController: NavHostController
) {

    val notificationViewModel = koinInject<NotificationViewModel>()
    val state by notificationViewModel.notification.collectAsState()


    LaunchedEffect(Unit) {
        notificationViewModel.fetchNotifications()
    }
    LaunchedEffect(Unit) {
        notificationViewModel.markNotificationAsRead()
    }

    val lazyState = rememberLazyListState()

    HideBottomBar(
        lazyState = lazyState,
        navigationViewModel = navigationViewModel
    )
    val scope = rememberCoroutineScope()

    val snackBarHostState = SnackbarHostState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text="Notification")
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.padding(bottom = 40.dp)
            )
        }

        ) { innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) {

            when {

                state.isLoading -> {
                    LoadingUI(true)
                }

                state.error.isNotEmpty()->{

                    ErrorScreen(
                        text = state.error.toString(),
                        image = null,
                        onReTry = {
                            notificationViewModel.fetchNotifications()
                        },
                        buttonText = "Try again"
                    )
                }

                state.data.isNotEmpty() -> {

                    val postByOrder by remember {
                        derivedStateOf {
                            state.data.sortedByDescending { it.createdAt}
                        }
                    }

                    LazyColumn(
                        state = lazyState,
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {

                        items(postByOrder) {

                            if (it.actionBy.userName.isEmpty() || it.actionBy.userImage.isEmpty()) return@items
                            if (it.content?.text.isNullOrEmpty() )return@items

                            NotificationItem(
                                notificationDTO = it,
                                scope = scope,
                                userProfileViewModel = userProfileViewModel,
                                notificationViewModel = notificationViewModel,
                                onNotificationClick = {
                                    navHostController.navigate(Routes.Main.ReplyPost.routes).apply {
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set<String>(
                                            "POST_ID",
                                            it.postId
                                        )
                                    }
                                },
                                geToUserProfile = {
                                    if (it.visibilityMode != PostVisibilityMode.USER) return@NotificationItem
                                    navHostController.navigate(Routes.Main.Profile.routes).apply {
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set<String>(
                                            "USER_ID",
                                            it.actionBy.id
                                        )
                                    }
                                },
                                snackbarHostState = snackBarHostState

                            )
                        }
                    }


                }

                state.data.isEmpty()->{
                    StatusScreen(
                        isActive = true,
                        text = "No Notification!",
                        image = null
                    )
                }

            }
        }

    }

}

@Composable
fun NotificationItem(
    notificationDTO: NotificationDTO,
    snackbarHostState: SnackbarHostState,
    userProfileViewModel: UserProfileViewModel,
    notificationViewModel: NotificationViewModel,
    scope: CoroutineScope,
    onNotificationClick: () -> Unit,
    geToUserProfile:()-> Unit,
    onAcceptRequestClick:()-> Unit = {
        userProfileViewModel.acceptLinkUpRequest(
            notificationDTO.actionBy.id
        )
    },
    onRejectRequestClick:()-> Unit = {
        userProfileViewModel.rejectLinkUpRequest(
            notificationDTO.actionBy.id
        )
    },

) {

    val acceptState by userProfileViewModel.acceptState.collectAsState()
    val rejectState by userProfileViewModel.rejectState.collectAsState()
    val deleteState by notificationViewModel.deleteNotificationState.collectAsState()
    //val rejectState = notificationViewModel.deleteNotificationState.collectAsState().value

    LaunchedEffect(deleteState) {
        when(deleteState){
            is UiState.Loading -> {}
            is UiState.Success<*> -> {
              //  notificationViewModel.deleteNotificationFromList(notificationDTO)
            }
            is UiState.Error ->{
                snackbarHostState.showSnackbar(
                    (deleteState as UiState.Error).message
                )
            }
            else -> {}
        }
    }

    var isAccepted by remember { mutableStateOf(true) }

    val text = if (notificationDTO.type == NotificationType.LIKE_POST)
        "Liked your Post"
    else if (notificationDTO.type == NotificationType.LIKE_REPLY)
        "Liked your reply"
    else if (notificationDTO.type == NotificationType.COMMENTED)
        "Replied your post"
    else if (notificationDTO.type == NotificationType.REQUEST)
        "Sent you a link request"
    else ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable(
            onClick = { onNotificationClick.invoke() },
            indication = null,
            interactionSource = remember { MutableInteractionSource() })
    ) {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {

            Row (
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ){

                CircleImage(
                    image = notificationDTO.actionBy.userImage,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    onClick = {
                        geToUserProfile.invoke()
                    }

                )

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notificationDTO.actionBy.userName,
                            maxLines = 2,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = " ● ",
                            maxLines = 2,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = text,
                            maxLines = 2,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = getTimeAgo(notificationDTO.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column {

                    if (!notificationDTO.content?.image.isNullOrEmpty()) {
                        AsyncImage(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            model = notificationDTO.content?.image,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                        )

                    }
                }
            }
        }



        Column(modifier = Modifier.padding(start = 58.dp)) {

            if (!notificationDTO.content?.text.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    text = notificationDTO.content!!.text.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            if (notificationDTO.type == NotificationType.REQUEST) {

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        enabled = isAccepted,
                        onClick = {
                            onAcceptRequestClick.invoke()
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        when(acceptState){
                            is UiState.Loading -> {
                                CircularLoading()
                            }
                            is UiState.Success->{
                                Text("Accepted")
                                notificationViewModel.deleteNotification(notificationDTO.notificationId.toString())

                            }
                            is UiState.Error->{
                                LaunchedEffect(Unit) {
                                    snackbarHostState.showSnackbar(
                                        (acceptState as UiState.Error).message
                                    )
                                }
                            }
                            else -> {
                                Text("Accept")
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            onRejectRequestClick.invoke()
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        when(rejectState){
                            is UiState.Loading -> {
                               CircularLoading()
                            }
                            is UiState.Success->{
                                notificationViewModel.deleteNotification(notificationDTO.notificationId)

                            }
                            is UiState.Error->{
                                LaunchedEffect(Unit) {
                                    snackbarHostState.showSnackbar(
                                        (rejectState as UiState.Error).message
                                    )
                                }
                            }
                            else -> {
                                Text("Reject")
                            }
                        }
                    }
                }
            }
        }
    }

}