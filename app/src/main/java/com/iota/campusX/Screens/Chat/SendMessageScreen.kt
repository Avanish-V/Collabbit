package com.iota.campusX.Screens.Chat

import android.net.Uri
import android.os.Build
import android.text.format.DateUtils.isToday
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.Chats.data.ChatMessage
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.ServerTimeStampViewModel
import com.iota.campusX.Utils.generateUID
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.theme.Black400
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.Green
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.secondary
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMessageScreen(
    navHostController: NavHostController,
    chatsViewModel: ChatsViewModel = koinViewModel(),
    serverTimeViewModel: ServerTimeStampViewModel = koinInject()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val navEntry = remember(navHostController) {
        navHostController.currentBackStackEntry
    }

    val userUUID = navEntry?.savedStateHandle?.get<String>("USER_ID").orEmpty()
    val userName = navEntry?.savedStateHandle?.get<String>("USER_NAME").orEmpty()
    val userImage = navEntry?.savedStateHandle?.get<String>("USER_IMAGE").orEmpty()

    val currentUser = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    val roomId by chatsViewModel.hasMessage.collectAsState()
    val chats by chatsViewModel.chats.collectAsStateWithLifecycle()
    val isActive by chatsViewModel.isActive.collectAsStateWithLifecycle()
    val isUserTyping by chatsViewModel.isUserTyping.collectAsStateWithLifecycle()
    val serverTime by serverTimeViewModel.timeStamp.collectAsState()


    var messageText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val typingAnimation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.typing))

    // --- Lifecycle Handling ---
    DisposableEffect(lifecycleOwner, roomId) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> chatsViewModel.updateIsUserActive(true, roomId)
                Lifecycle.Event.ON_PAUSE -> {
                    chatsViewModel.updateIsUserActive(false, roomId)
                    chatsViewModel.updateIsUserTyping(false, roomId)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // --- Initial Setup ---
    LaunchedEffect(userUUID) {
        chatsViewModel.fetchRoomID(userUUID)
    }

    LaunchedEffect(roomId) {
        chatsViewModel.receiveMessage(userUUID, roomId)
    }

    LaunchedEffect(roomId) {
        chatsViewModel.getIsActive(userUUID, roomId)
        chatsViewModel.getUserIsTyping(userUUID, roomId)
    }

    LaunchedEffect(roomId) {
        chatsViewModel.markMessagesAsReed(userUUID, roomId)
    }

    // --- Typing Detection ---
    LaunchedEffect(messageText) {
        snapshotFlow { messageText }
            .collectLatest {
                val currentlyTyping = it.isNotEmpty()
                if (isTyping != currentlyTyping) {
                    isTyping = currentlyTyping
                    chatsViewModel.updateIsUserTyping(currentlyTyping, roomId)
                }
            }
    }

    // --- Auto-scroll ---
    LaunchedEffect(chats.size, isUserTyping) {
        snapshotFlow { listState.layoutInfo.totalItemsCount }
            .collect {
                if (it > 0) listState.animateScrollToItem(it - 1)
            }
    }

    Scaffold(
        topBar = {
            ChatTopBar(userName, userImage, isActive,navHostController) {
                navHostController.navigate(Routes.Main.Profile.toString()).apply {
                    navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_ID", userUUID)
                }
            }
        },
        bottomBar = {

            Box(modifier = Modifier.background(color = White900)){
                MessageInputBar(
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSendClick = {
                        if (messageText.isBlank()) return@MessageInputBar

                        val messageId = generateUID()
                        val message = messageText
                        messageText = ""

                        scope.launch {
                            chatsViewModel.sendMessages(
                                message = message,
                                messageId = messageId,
                                roomId = roomId,
                                timestamp = 0L,
                                receiverId = userUUID
                            ).collect {
                                when(it){
                                    is ResultState.Loading -> {}
                                    is ResultState.Success -> {
                                        chatsViewModel.fetchRoomID(userUUID)
                                    }
                                    is ResultState.Error -> {}
                                }
                            }
                        }

                    }
                )
            }
        },
        containerColor = Color.White
    ) { padding ->
        ChatList(
            modifier = Modifier.padding(padding),
            chats = groupChatsByDate(chats),
            listState = listState,
            currentUser = currentUser,
            isUserTyping = isUserTyping,
            typingAnimation = typingAnimation,
            onDelete = { chat ->
                chatsViewModel.deleteChatRoomData(chat.messageId, roomId)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(name: String, image: String, isActive: Boolean, navHostController: NavHostController,onProfileClick: () -> Unit,) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.clickable(onClick = onProfileClick)
            ) {
                BadgedBox(
                    badge = {
                        if (isActive) Box(
                            Modifier
                                .size(12.dp)
                                .background(Green, CircleShape)
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                ) {
                    AsyncImage(
                        model = image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(40.dp).clip(CircleShape)
                    )
                }
                Column { Text(name) }
            }
        },
        navigationIcon = {
            IconButton(onClick = { navHostController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
        }
    )
}

@Composable
fun MessageInputBar(messageText: String, onMessageChange: (String) -> Unit, onSendClick: () -> Unit) {
    TextField(
        value = messageText,
        onValueChange = onMessageChange,
        modifier = Modifier.padding(12.dp).fillMaxWidth().imePadding(),
        placeholder = { Text("Write a text...") },
        trailingIcon = {
            IconButton(onClick = onSendClick, enabled = messageText.isNotBlank()) {
                Icon(painter = painterResource(R.drawable.send_2), contentDescription = null, tint = primary)
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = secondary,
            unfocusedContainerColor = secondary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = CircleShape
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatList(
    modifier: Modifier = Modifier,
    chats: List<ChatItem>,
    listState: LazyListState,
    currentUser: String,
    isUserTyping: Boolean,
    typingAnimation: LottieComposition?,
    onDelete: (ChatMessage) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(chats) { item ->
            when (item) {
                is ChatItem.Header -> HeaderLabel(item.label)
                is ChatItem.Message -> {
                    val chat = item.chat
                    if (chat.senderId == currentUser) {
                        ChatBubbleItem(chat = chat, onDelete = { onDelete(chat) })
                    } else {
                        ReceivedMessageBubble(chat)
                    }
                }
            }
        }
        item {
            AnimatedVisibility(visible = isUserTyping) {
                LottieAnimation(
                    composition = typingAnimation,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(48.dp).padding(start = 12.dp)
                )
            }
        }
    }
}

@Composable
fun HeaderLabel(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(Modifier.weight(1f).padding(horizontal = 12.dp))
        Box(Modifier.background(secondary, RoundedCornerShape(6.dp)).padding(horizontal = 16.dp, vertical = 4.dp)) {
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(Modifier.weight(1f).padding(horizontal = 12.dp))
    }
}

@Composable
fun ReceivedMessageBubble(chat: ChatMessage) {
    Column(
        Modifier.fillMaxWidth().padding(end = 40.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            Modifier.background(White400, RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp))
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(chat.text)
                Text(convertTimestampToTime(chat.timestamp), fontSize = 12.sp)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubbleItem(
    chat: ChatMessage,
    image: String ? = null,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .combinedClickable(
                    onClick = {}, // Regular click if needed
                    onLongClick = {
                        showMenu = true
                        context.vibrate()
                    } // Show menu on long press
                )
                .background(primary, RoundedCornerShape(12.dp, 0.dp, 12.dp, 12.dp))
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {

                if (image != null) {
                    AsyncImage(
                        modifier = Modifier.height(250.dp),
                        model = image,
                        contentDescription = null
                    )
                }

                Text(chat.text, color = White900)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_done_all_24),
                        contentDescription = null,
                        tint = if (chat.read) White900 else Black500
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        convertTimestampToTime(chat.timestamp),
                        fontSize = 12.sp,
                        color = White900
                    )
                }
            }

            // Long-press popup menu
            DropdownMenu(
                expanded = showMenu,
                offset = DpOffset(x = -100.dp, y = 0.dp),
                onDismissRequest = { showMenu = false },
                containerColor = Color.White,
            ) {

                DropDownItem {
                    showMenu = false
                    scope.launch {
                        delay(150) // Give time for the menu to dismiss
                        onDelete()
                    }
                }

            }
        }
    }
}

@Composable
fun DropDownItem(onDelete: () -> Unit) {

   Column {

            Row(
                modifier = Modifier
                    .clickable(
                        onClick = {onDelete.invoke()},
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    painter = painterResource(R.drawable.trash),
                    contentDescription = null,
                    tint = Color.Red
                )
                Text("Delete")
            }


        }

}


fun convertTimestampToTime(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault()) // Example: 02:30 PM
        sdf.format(Date(timestamp)).toString()
    } catch (e: Exception) {
        e.printStackTrace().toString()
    }

}

sealed class ChatItem {
    data class Header(val label: String) : ChatItem()
    data class Message(val chat: ChatMessage) : ChatItem()
}

@RequiresApi(Build.VERSION_CODES.O)
fun groupChatsByDate(chats: List<ChatMessage>): List<ChatItem> {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

    // Step 1: Sort all chats in ascending timestamp order
    val sortedChats = chats.sortedBy { it.timestamp }

    // Step 2: Group by date label
    val grouped = sortedChats.groupBy {
        val chatDate = Instant.ofEpochMilli(it.timestamp)
            .atZone(ZoneId.systemDefault()).toLocalDate()

        when {
            chatDate.isEqual(today) -> "Today"
            chatDate.isEqual(tomorrow) -> "Tomorrow"
            else -> chatDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        }
    }

    // Step 3: Flatten into list with headers and messages
    return grouped.entries
        .sortedBy { it.value.first().timestamp } // Ensure headers are also in order
        .flatMap { (label, messages) ->
            listOf(ChatItem.Header(label)) + messages.map { ChatItem.Message(it) }
        }
}

