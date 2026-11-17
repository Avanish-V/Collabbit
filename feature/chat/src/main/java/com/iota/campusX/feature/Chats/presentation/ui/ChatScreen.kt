package com.iota.campusX.Feature.Chats.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Chats.domain.ChatRequestStatus
import com.iota.campusX.Feature.Chats.data.UserChatsDTO
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.Navigation.SendMessage
import com.iota.campusX.core.designsystem.R
import com.iota.campusX.core.designsystem.LoadingScreen
import com.iota.campusX.core.designsystem.StatusScreen
import com.iota.campusX.core.common.UiState
import com.iota.campusX.ui.UIComponents.AppLabelText
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.ErrorScreen

//import com.iota.campusX.ui.theme.typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navHostController: NavHostController,
    chatsViewModel: ChatsViewModel
) {

    val chatList = chatsViewModel.userChats.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
       chatsViewModel.getChats()
    }

    Scaffold (
        topBar ={
            TopAppBar(
                title = { Text("Messages",) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = {navHostController.popBackStack()}) {
                        Icon(
                            imageVector = (Icons.Default.ArrowBack),
                            contentDescription = null
                        )
                    }
                }
            )
        },
    ){ paddingValues ->

        when(chatList){
            is UiState.Loading -> {
                LoadingScreen()
            }
            is UiState.Success<*> -> {

                val usersChat = (chatList as UiState.Success).data

                if (usersChat.isEmpty()) {
                    StatusScreen(
                        text = "No Messages!",
                        image = R.drawable.undraw_chatting_2b1g,
                    )
                    return@Scaffold
                }

                LazyColumn (modifier = Modifier.fillMaxSize().padding(paddingValues)){
                    items(usersChat){ chatItem ->
                        MentorSingleCard(
                            chatItem = chatItem,
                            onSwipeRightAccept = {
                                chatsViewModel.swipeRightAction(
                                    targetUserId = chatItem.receiverId,
                                    roomId = chatItem.roomId,
                                    action = "ACCEPT"
                                )
                            },
                            onSwipeRightReject = {
                                chatsViewModel.swipeRightAction(
                                    targetUserId = chatItem.receiverId,
                                    roomId = chatItem.roomId,
                                    action = "REJECT"
                                )
                            },
                            onClick = {
                                navHostController.navigate(
                                    SendMessage(
                                        userId = chatItem.receiverId,
                                        userName = chatItem.userName,
                                        userImage = chatItem.userImage,
                                    )
                                )
                            }
                        )
                        Divider()
                    }
                }

            }
            is UiState.Error -> {

                ErrorScreen(

                    text = chatList.message,
                    image = null,
                    onReTry = {
                        chatsViewModel.getChats()
                    },
                    buttonText =  "Retry"
                )

            }
            else -> {}
        }
    }
}

@Composable
fun MentorSingleCard(
    chatItem: UserChatsDTO,
    onSwipeRightAccept: () -> Unit,
    onSwipeRightReject: () -> Unit,
    onClick: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var showConfirmActions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    if (delta > 0 || offsetX > 0) {
                        offsetX = (offsetX + delta).coerceIn(0f, 300f)
                        if (offsetX > 150f) {
                            showConfirmActions = true
                        }
                    }
                },
                onDragStopped = {
                    if (offsetX < 150f) {
                        offsetX = 0f
                        showConfirmActions = false
                    }
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .clickable(
                    onClick = { onClick.invoke() },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                model = chatItem.userImage,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = chatItem.userName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (chatItem.status == ChatRequestStatus.PENDING.name) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    text = "Pending",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    AppLabelText(
                        text = convertTimestampToTime(chatItem.lastMessage.timeStamp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (chatItem.lastMessage.lastMessageBy) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.eye),
                                contentDescription = null,
                                tint = if (chatItem.lastMessage.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = chatItem.lastMessage.lastMessage,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (chatItem.lastMessage.unreadCount > 0) {
                        Surface(
                            modifier = Modifier
                                .height(20.dp)
                                .widthIn(min = 20.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (chatItem.lastMessage.unreadCount > 99) "99+" else chatItem.lastMessage.unreadCount.toString(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                if (showConfirmActions) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                offsetX = 0f
                                showConfirmActions = false
                                onSwipeRightAccept()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Accept")
                        }

                        OutlinedButton(
                            onClick = {
                                offsetX = 0f
                                showConfirmActions = false
                                onSwipeRightReject()
                            }
                        ) {
                            Text("Reject")
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.background
        )
    }
}