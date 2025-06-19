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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.background
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navigationViewModel: NavigationViewModel,
    userProfileViewModel: UserProfileViewModel,
    navHostController: NavHostController
) {

    val notificationViewModel = koinInject<NotificationViewModel>()
    val state = notificationViewModel.notification.collectAsState().value
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Notification", fontWeight = FontWeight.Medium, color = Black900)
                },
            )
        },

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

                            NotificationItem(
                                notificationDTO =  it,
                                scope = scope,
                                userProfileViewModel = userProfileViewModel,
                                notificationViewModel = notificationViewModel,
                                onNotificationClick = {
                                    navHostController.navigate(Routes.Main.ReplyPost.routes).apply{
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set<String>("POST_ID",it.postId)
                                    }
                                },
                                geToUserProfile = {
                                    if (it.visibilityMode != PostVisibilityMode.USER) return@NotificationItem
                                    navHostController.navigate(Routes.Main.Profile.routes).apply{
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set<String>("USER_ID",it.actionBy.id)
                                    }
                                }
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
    userProfileViewModel: UserProfileViewModel,
    notificationViewModel: NotificationViewModel,
    scope: CoroutineScope,
    onNotificationClick: () -> Unit,
    geToUserProfile:()-> Unit
) {

    var isAccepted by remember { mutableStateOf(true) }

    val text = if (notificationDTO.type == "LIKE")
        "Liked your Post"
    else if (notificationDTO.type == "LIKE_REPLY")
        "Liked your reply"
    else if (notificationDTO.type == "POST_REPLY")
        "Replied your post"
    else if (notificationDTO.type == "LINK_REQUEST")
        "Sent you a link request"
    else ""

    val annotatedText = buildAnnotatedString {

        withStyle(style = SpanStyle(color = Black800, fontWeight = FontWeight.Medium)) {
            append(notificationDTO.actionBy.userName)
        }
        withStyle(style = SpanStyle(color = White400)) {
            append(" ● ")
        }

        withStyle(style = SpanStyle(color = Black300, fontWeight = FontWeight.Normal)) {
            append(text)
        }
        withStyle(style = SpanStyle(color = White400)) {
            append(" ● ")
        }

        withStyle(style = SpanStyle(color = Black300, fontWeight = FontWeight.Normal)) {
            append(getTimeAgo(notificationDTO.createdAt))
        }

    }


    Column(modifier = Modifier
        .fillMaxWidth()
        .background(color = White900)
        .padding(12.dp)
        .clickable(
            onClick = { onNotificationClick.invoke() },
            indication = null,
            interactionSource = remember { MutableInteractionSource() })) {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row (
                verticalAlignment = Alignment.CenterVertically,
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

                Column(modifier = Modifier
                    .weight(1f)
                    .height(48.dp)) {

                    Text(
                        text = annotatedText,
                        fontSize = 14.sp,
                        maxLines = 2,
                    )
                }

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



        Column(modifier = Modifier.padding(start = 58.dp)) {

            if (!notificationDTO.content?.text.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    modifier = Modifier
                        .background(
                            color = background,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .padding(horizontal = 12.dp),
                    text = notificationDTO.content!!.text.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (notificationDTO.postId.isEmpty()) Black300 else Black800
                )
            }


            if (notificationDTO.type == "LINK_REQUEST") {

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                userProfileViewModel.rejectLinkUpRequest(
                                    notificationDTO.actionBy.id
                                )
                            }
                            notificationViewModel.deleteNotificationFromList(notificationDTO)
                        },
                    ) {
                        Text("Reject")
                    }
                    TextButton(
                        enabled = isAccepted,
                        onClick = {
                            scope.launch {
                                userProfileViewModel.acceptLinkUpRequest(
                                    notificationDTO.actionBy.id
                                )
                            }
                        },
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }

}