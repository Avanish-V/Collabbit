package com.iota.campusX.Screens.Chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Chats.data.UserChatsDTO
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.AppLabelText
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.LightBlack
import com.iota.campusX.ui.theme.LightTheme_Gray
import com.iota.campusX.ui.theme.LightTheme_Black
import com.iota.campusX.ui.theme.White
import com.iota.campusX.ui.theme.LightTheme_Blue
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
                LoadingUI(isLoading = true)
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
                    items(usersChat){
                        MentorSingleCard(chatItem = it) {
                            navHostController.navigate(Routes.Main.SendMessage.routes).apply {
                                navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_ID",it.receiverId)
                                navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_NAME",it.userName)
                                navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_IMAGE",it.userImage)
                                navHostController.currentBackStackEntry?.savedStateHandle?.set("ROOM_ID",it.roomId)
                            }
                        }
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
fun MentorSingleCard(chatItem: UserChatsDTO, onClick: () -> Unit) {

    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = {
                        onClick.invoke()
                    },
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
                    Text(
                        text = chatItem.userName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis

                    )

                    AppLabelText(
                        text = convertTimestampToTime(chatItem.lastMessage.timeStamp)
                    )

                }

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){

                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(6.dp)) {

                        if (chatItem.lastMessage.lastMessageBy){
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.baseline_done_all_24),
                                contentDescription = null,
                                tint = if (chatItem.lastMessage.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            modifier = Modifier,
                            text = chatItem.lastMessage.lastMessage,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (chatItem.lastMessage.unreadCount > 0){
                        Box(
                            modifier = Modifier.size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    color = MaterialTheme.colorScheme.primary
                                ),
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = chatItem.lastMessage.unreadCount.toString(),
                                color = White,
                                style = MaterialTheme.typography.labelMedium
                            )
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