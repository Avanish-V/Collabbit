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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Chats.data.UserChatsDTO
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.ui.theme.Black400
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navHostController: NavHostController,
    chatsViewModel: ChatsViewModel
) {

    val chatList = chatsViewModel.userChats.collectAsState().value

    LaunchedEffect(Unit) {
       chatsViewModel.getChats()
    }

    Scaffold (
        topBar ={
            TopAppBar(
                title = { Text("Messages",) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White900
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
        containerColor = White900
    ){ paddingValues ->

        StatusScreen(
            isActive = chatList.userChats.isEmpty(),
            text = "No Messages!",
            image = R.drawable.anonymous
        )

        StatusScreen(
            isActive = chatList.error.isNotEmpty(),
            text = chatList.error.toString(),
            image = R.drawable.anonymous
        )

        LoadingUI(isLoading = chatList.isLoading)

        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),

        ){
            items(chatList.userChats){
                MentorSingleCard(chatItem = it) {
                    navHostController.navigate(Routes.Main.SendMessage.routes).apply {
                        navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_ID",it.receiverId)
                        navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_NAME",it.userName)
                        navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_IMAGE",it.userImage)
                        navHostController.currentBackStackEntry?.savedStateHandle?.set("ROOM_ID",it.roomId)
                    }
                }
            }
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
            verticalAlignment = Alignment.CenterVertically
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
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        chatItem.userName,
                        fontWeight = FontWeight.Bold,
                        color = Black900,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis

                    )

                    Text(
                        convertTimestampToTime(chatItem.lastMessage.timeStamp),
                        fontSize = 14.sp,
                        color = Black500
                    )

                }

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(
                        modifier = Modifier.weight(1f),
                        text = chatItem.lastMessage.lastMessage,
                        color = Black400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                                color = White900,
                                style = typography.labelMedium
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