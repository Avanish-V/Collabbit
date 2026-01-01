package com.iota.campusX.Feature.Society.CommunityMessages.presentation.Screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Society.CommunityMessages.domain.models.Message
import com.iota.campusX.Feature.Society.CommunityMessages.domain.models.MessageStatus
import com.iota.campusX.Feature.Society.CommunityMessages.presentation.ViewModels.CommunityChatMenuViewModel
import com.iota.campusX.Feature.Society.CommunityMessages.presentation.ViewModels.CommunityChatViewModel
import com.iota.campusX.R
import com.iota.campusX.Screens.BottomTextInput
import com.iota.campusX.Utils.LoadingScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.UIComponents.UserAvatar
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityChatScreen(
    navHostController: NavHostController,
    communityChatViewModel: CommunityChatViewModel = koinInject(),
    communityChatMenuViewModel: CommunityChatMenuViewModel = koinInject()
) {
    val snackBarHostState = remember { SnackbarHostState() }


    val uiState by communityChatMenuViewModel.uiState.collectAsState()

    val messageList by communityChatViewModel.messages.collectAsStateWithLifecycle()
    val error by communityChatViewModel.error.collectAsState()
    val text = communityChatViewModel.text
    val replyingTo = communityChatViewModel.replyingTo

    LaunchedEffect(error) {
        if (error == null) return@LaunchedEffect
        snackBarHostState.showSnackbar("Something went wrong!")
    }

    LaunchedEffect(Unit) {
        communityChatViewModel.observerMessage("123456")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Space Collaboration") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                replyingTo?.let {
                    Row(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                            Text("Replying to: ${replyingTo.userName}")
                            Text(modifier = Modifier.padding(12.dp).background(color = MaterialTheme.colorScheme.background).clip(RoundedCornerShape(6.dp)), text = " ${replyingTo.text}")
                        }
                        IconButton(onClick = { communityChatViewModel.cancelReply() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Back")
                        }
                    }
                }
                BottomTextInput(
                    focusRequester = FocusRequester.Default,
                    onFocusChange ={},
                    onTextChange = communityChatViewModel::onTextChanged,
                    selectedVisibility = VisibilityMode.USER,
                    onVisibilityChange = {},
                    text = text,
                    onSubmitClick = {
                        communityChatViewModel.onSend()
                    },
                    isLoading = false,
                    userImage = "",
                    mentionBuilder = null,
                    onImagePick = {

                    }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) {innerPadding->

        Box (modifier = Modifier.padding(innerPadding)){


            when(messageList){
                is UiState.Success<*> -> {

                   val messageList = (messageList as UiState.Success<*>).data as List<Message>

                    LazyColumn(
                        modifier = Modifier,
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messageList){chat->

                            if (chat.replyToMessageId != null){
                                val reply = messageList.find { it.id == chat.replyToMessageId }
                                Text(reply?.text ?: "not found")

                            }

                            Row (
                                modifier = Modifier.combinedClickable(
                                    onLongClick = {
                                        communityChatMenuViewModel.onMessageLongPress(chat)
                                    },
                                    onClick = {},
                                    indication = null,
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                ),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ){
                                UserAvatar(
                                    modifier = Modifier.size(38.dp).clip(CircleShape),
                                    imageUrl = chat.avatarUrl ?: "",
                                    bgColor = chat.bgColor
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row (verticalAlignment = Alignment.CenterVertically){
                                        Text(text = chat.userName, style = MaterialTheme.typography.titleMedium)
                                        Text(text = " ● "+ getTimeAgo(parseDateToMillis(chat.timestamp.toString())), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(text = chat.text, style = MaterialTheme.typography.bodyMedium)
                                }
                                when(chat.messageStatus){
                                    MessageStatus.SENDING -> {

                                    }
                                    MessageStatus.FAILED -> {
                                        Text(text = "Failed", style = MaterialTheme.typography.labelSmall)
                                    }
                                    else -> {}
                                }
                            }

                        }
                    }


                }
                is UiState.Loading -> {
                    LoadingScreen()
                }
                is UiState.Error -> {
                    ErrorScreen(
                        text = (messageList as UiState.Error).message,
                        image = R.drawable.undraw_page_not_found_6wni,
                        onReTry = {},
                        buttonText = "Try to reload",
                    )
                }
                else -> {}
            }

            if (uiState.isMenuVisible) {
                MessageActionMenu(
                    message = uiState.selectedMessage,
                    onDismiss = { communityChatMenuViewModel.dismissMenu() },
                    onReply = { communityChatViewModel.onMessagePress(message = uiState.selectedMessage) },
                    onCopy = { /* clipboard logic */ },
                    onDelete = { /* delete msg */ }
                )
            }
        }


    }

}

fun parseDateToMillis(dateStr: String?): Long {
    if (dateStr.isNullOrBlank() || dateStr == "0") return 0L

    return try {
        val format = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm:ss a 'UTC'XXX", Locale.ENGLISH)
        format.parse(dateStr)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}

@Composable
fun MessageActionMenu(
    message: Message?,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    if (message == null) return

    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .shadow(1.dp, clip = true)
        ) {
            MenuItem("Reply", R.drawable.outline_reply_24,onReply)

            MenuItem("Copy", R.drawable.chatbubble_outline,onCopy)

            MenuItem("Delete", R.drawable.trash,onDelete)
        }
    }
}

@Composable
fun MenuItem(text: String, icon: Int, onClick: () -> Unit) {
    Row(modifier = Modifier.height(48.dp).padding(start = 10.dp),verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            modifier = Modifier.size(22.dp),
            painter = painterResource(icon),
            contentDescription = null
        )
        Text(
            text,
            modifier = Modifier
                .clickable { onClick() }
                .padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

