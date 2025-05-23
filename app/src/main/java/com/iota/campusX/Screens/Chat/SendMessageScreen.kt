package com.iota.campusX.Screens.Chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.firebase.Timestamp
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.iota.campusX.Feature.Chats.data.ChatMessage
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.generateUID
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMessageScreen(navHostController: NavHostController) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val navEntry = remember(navHostController) {
        navHostController.currentBackStackEntry
    }

    val userUUID = navEntry?.savedStateHandle?.get<String>("USER_ID").orEmpty()
    val userName = navEntry?.savedStateHandle?.get<String>("USER_NAME").orEmpty()
    val userImage = navEntry?.savedStateHandle?.get<String>("USER_IMAGE").orEmpty()
    val roomId = navEntry?.savedStateHandle?.get<String>("ROOM_ID").orEmpty()

    val currentUser = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    val chatsViewModel: ChatsViewModel = koinViewModel {
        parametersOf(currentUser)
    }

    val chats = chatsViewModel.chats.collectAsStateWithLifecycle().value
    val isActive = chatsViewModel.isActive.collectAsStateWithLifecycle().value
    val isUserTyping = chatsViewModel.isUserTyping.collectAsStateWithLifecycle().value

    var messageText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    // Typing state effect
    LaunchedEffect(messageText) {
        if (messageText.isNotEmpty()) {
            if (!isTyping) {
                isTyping = true
                chatsViewModel.updateIsUserTyping(true, roomId = roomId)
            }
            delay(2000L)
            isTyping = false
            chatsViewModel.updateIsUserTyping(false, roomId = roomId)
        } else if (isTyping) {
            isTyping = false
            chatsViewModel.updateIsUserTyping(false, roomId = roomId)
        }
    }

    // Lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                chatsViewModel.updateIsUserActive(false, roomId)
                chatsViewModel.updateIsUserTyping(false, roomId = roomId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }


    LaunchedEffect(Unit) {
        chatsViewModel.markMessagesAsReed(participantId = userUUID, roomId = roomId)
    }

    // Load user and chat states
    LaunchedEffect(Unit) {

        chatsViewModel.receiveMessage(participantId = userUUID, roomId = roomId)
        chatsViewModel.updateIsUserActive(true, roomId)
        chatsViewModel.getIsActive(userUUID, roomId)
        chatsViewModel.getUserIsTyping(participantId = userUUID, roomId = roomId)
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.typing))
    val listState = rememberLazyListState()

    LaunchedEffect(chats.size) {
        if (chats.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(chats.size - 1)
        }
    }

    // Pre-processed sorted chat list
    val sortedChats = remember(chats) {
        chats.sortedBy { it.timestamp }.mapIndexed { index, chat ->
            val showToday = index == 0 || !isToday(chats[index - 1].timestamp,)
            chat to showToday
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(0.5.dp),
                title = {
                    Row(
                        modifier = Modifier.clickable(
                            onClick = {
                                navHostController.navigate(Routes.Main.Profile.toString()).apply {
                                    navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_ID", userUUID)
                                }
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (isActive) Box(
                                    Modifier
                                        .offset(4.dp, 22.dp)
                                        .size(12.dp)
                                        .background(primary, CircleShape)
                                        .border(1.dp, Color.White, CircleShape)
                                )
                            }
                        ) {
                            AsyncImage(
                                model = userImage,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column {
                            Text(userName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            AnimatedVisibility(visible = isUserTyping) {
                                LottieAnimation(
                                    modifier = Modifier.size(32.dp),
                                    composition = composition,
                                    iterations = LottieConstants.IterateForever
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White900)
            )
        },
        bottomBar = {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Write a text...") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val messageId = generateUID()
                            val message = messageText
                            messageText = ""
                            scope.launch {
                                chatsViewModel.sendMessages(
                                    message = message,
                                    messageId = messageId,
                                    roomId = roomId,
                                    timestamp = System.currentTimeMillis(),
                                    receiverId = userUUID
                                ).collect {}
                            }
                        },
                        enabled = messageText.isNotEmpty()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.send_2),
                            contentDescription = null,
                            tint = primary
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = White900
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(sortedChats) { (chat, showTodayDivider) ->

                if (showTodayDivider && isToday(chat.timestamp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp))
                        Box(
                            Modifier
                                .background(secondary, RoundedCornerShape(6.dp))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text("Today", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        HorizontalDivider(Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp))
                    }
                }

                if (chat.senderId == currentUser) {

                    AnimatedVisibility(
                        visible = chats.contains(chat),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ChatBubbleItem(chat = chat, onDelete = { chatsViewModel.deleteChatRoomData(
                            chatId = chat.messageId,
                            roomId = roomId
                        ) })
                    }

                } else {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 40.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            Modifier.background(White400, RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp))
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(chat.text)
                                Text(convertTimestampToTime(chat.timestamp), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubbleItem(
    chat: ChatMessage,
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
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
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
                offset = _root_ide_package_.androidx.compose.ui.unit.DpOffset(x = -100.dp, y = 0.dp),
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

@RequiresApi(Build.VERSION_CODES.O)
fun isToday(timestamp: Long): Boolean {
    val today = LocalDate.now()
    val messageDate = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    return messageDate == today
}
