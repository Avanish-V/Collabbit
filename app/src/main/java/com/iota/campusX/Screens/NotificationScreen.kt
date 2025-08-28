package com.iota.campusX.Screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Notification.domain.ContentType
import com.iota.campusX.Feature.Notification.domain.NotificationDTO
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.CircularLoading
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.ErrorScreen
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
    val notificationCount by notificationViewModel.notificationCount.collectAsState()

    LaunchedEffect(Unit) {
        if (state.data.isEmpty()){
            notificationViewModel.fetchNotifications()
        }
        if (notificationCount > 0){
            notificationViewModel.fetchNotifications()
        }
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
                    Text(text="Notification", style = MaterialTheme.typography.titleLarge)
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
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        items(postByOrder) {

                            NotificationItem(
                                notificationDTO = it,
                                scope = scope,
                                userProfileViewModel = userProfileViewModel,
                                notificationViewModel = notificationViewModel,
                                onNotificationClick = {
                                    navHostController.navigate(Routes.Main.ReplyPost.routes).apply {
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set<String>(
                                            "POST_ID",
                                            it.payload?.get("postId").toString()
                                        )
                                    }
                                },
                                geToUserProfile = {
                                    if (it.payload?.get("visibilityMode") == VisibilityMode.ANONYMOUS.name) return@NotificationItem
                                    navHostController.navigate(Routes.Main.ProfileByID.routes).apply {
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set<String>(
                                            "USER_ID",
                                            it.userDetail.id
                                        )
                                    }
                                },
                                snackbarHostState = snackBarHostState
                            )

                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }


                }

                state.data.isEmpty()->{
                    StatusScreen(
                        text = "No Notification!",
                        image = R.drawable.undraw_my_notifications_fy5v
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
            notificationDTO.userDetail.id
        )
    },
    onRejectRequestClick:()-> Unit = {
        userProfileViewModel.rejectLinkUpRequest(
            notificationDTO.userDetail.id
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
                notificationViewModel.deleteNotificationFromList(notificationDTO)
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


    val text = notificationDTO.payload.let {
        if (it?.get("contentType") == ContentType.LIKE_POST.name)
            "Liked your Post"
        else if (it?.get("contentType") == ContentType.LIKE_REPLY.name)
            "Liked your reply"
        else if (it?.get("contentType") == ContentType.REPLY_POST.name)
            "Replied your post"
        else if (it?.get("contentType") == ContentType.CONNECTION_REQUEST.name)
            "Sent you a link request"
        else ""
    }


    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {

        Box(
            modifier = Modifier.clickable(
                onClick = {
                    geToUserProfile.invoke()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
            contentAlignment = Alignment.BottomEnd
        ){


            CircleImage(
                image = notificationDTO.userDetail.userImage,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                onClick = {
                    geToUserProfile.invoke()
                },
                visibility =  VisibilityMode.USER

            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(x = (8).dp, y = (0).dp)
                    .shadow(2.dp, clip = true,shape = CircleShape)
                    .clip(CircleShape)
                    .size(20.dp)

                    .background(MaterialTheme.colorScheme.background,shape = CircleShape)
            ){

                val icon = when(notificationDTO.type){
                    NotificationType.LIKE -> {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(R.drawable.heart_sharp),
                            tint = Color.Red,
                            contentDescription = "Like"
                        )
                    }
                    NotificationType.COMMENT -> {
                        Icon(
                            modifier = Modifier.size(12.dp),
                            painter = painterResource(R.drawable.chatbubble_outline),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Like"
                        )
                    }
                    NotificationType.CONNECTION_REQUEST -> {
                        Icon(
                            modifier = Modifier.size(12.dp),
                            painter = painterResource(R.drawable.user_add),
                            tint = Color.Green,
                            contentDescription = "Like"
                        )
                    }
                    else -> {}
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f)
                .clickable(
                    onClick = {
                        if (notificationDTO.type == NotificationType.CONNECTION_REQUEST)return@clickable
                        onNotificationClick.invoke()
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notificationDTO.userDetail.userName,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    modifier = Modifier.alpha(0.6f),
                    text = " ● ",
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    modifier = Modifier.alpha(0.6f),
                    text = text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                modifier = Modifier.alpha(0.6f),
                text = getTimeAgo(notificationDTO.createdAt?.toDate()?.time ?: 0L),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            when(notificationDTO.type){

                NotificationType.LIKE -> {
                    Row(
                        modifier = Modifier.background(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            shape = MaterialTheme.shapes.small

                        ).padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        if (notificationDTO.content.image.isNullOrEmpty() && notificationDTO.content.text.isNullOrEmpty()){

                            Text(
                                text = "Content unavailable",
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                        }
                        else{

                            notificationDTO.content.image?.let {
                                AsyncImage(
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)),
                                    model = notificationDTO.content.image,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                )
                            }

                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = notificationDTO.content.text.toString(),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                        }
                    }
                }

                NotificationType.COMMENT -> {

                    if (notificationDTO.content.text.isNullOrEmpty()){

                        Text(
                            modifier = Modifier.background(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                shape = MaterialTheme.shapes.small
                            ).padding(6.dp),
                            text = "Content unavailable",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }else{

                        Text(
                            modifier = Modifier.background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.small
                            ).padding(6.dp),
                            text = " ${notificationDTO.content.text}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                    }

                }

                NotificationType.CONNECTION_REQUEST -> {

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
                                    CircularLoading(
                                        color = Color.White
                                    )
                                }
                                is UiState.Success->{
                                    Text("Accepted")
                                    isAccepted = false
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
                                    Text("✔ Accept")
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                onRejectRequestClick.invoke()
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            when(rejectState){
                                is UiState.Loading -> {
                                    CircularLoading(
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
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
                                    Text("✖ Decline")
                                }
                            }
                        }
                    }

                }

                else -> {}

            }
        }
    }
}