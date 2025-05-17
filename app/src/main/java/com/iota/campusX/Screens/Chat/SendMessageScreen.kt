package com.iota.campusX.Screens.Chat

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.R
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMessageScreen(navHostController: NavHostController) {

    val lifecycleOwner = LocalLifecycleOwner.current


    val chatsViewModel = koinInject<ChatsViewModel>()
    val scope = rememberCoroutineScope()

    val userUUID by remember {
        mutableStateOf(navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("USER_ID"))
    }
    val userName by remember {
        mutableStateOf(navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("USER_NAME"))
    }
    val userImage by remember {
        mutableStateOf(navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("USER_IMAGE"))
    }

    val roomId by remember {
        mutableStateOf(navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("ROOM_ID"))
    }

    val currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid.toString()) }

    val chats = chatsViewModel.chats.collectAsStateWithLifecycle().value
    val isActive = chatsViewModel.isActive.collectAsStateWithLifecycle().value
    val isUserTyping = chatsViewModel.isUserTyping.collectAsStateWithLifecycle().value


    var messageText by remember { mutableStateOf("") }

    var isTyping by remember { mutableStateOf(false) }  // Track typing state

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {

                chatsViewModel.updateIsUserActive(
                    isActive = false,
                    roomId = roomId.toString(),
                )
                chatsViewModel.updateIsUserTyping(
                    false,
                    roomId.toString()
                )

            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    LaunchedEffect(messageText) {
        if (messageText.isNotEmpty()) {
            if (!isTyping) {
                isTyping = true
                chatsViewModel.updateIsUserTyping(true, roomId.toString())
            }
            delay(2000L)  // Wait for 2 seconds of inactivity
            if (messageText == messageText) { // Still same text after delay?
                isTyping = false
                chatsViewModel.updateIsUserTyping(false, roomId.toString())
            }
        } else {
            if (isTyping) {
                isTyping = false
                chatsViewModel.updateIsUserTyping(false, roomId.toString())
            }
        }
    }

    LaunchedEffect(Unit) {
        chatsViewModel.markMessagesAsReed(
            roomId = roomId.toString(),
            participantId = userUUID.toString()
        )
    }

    LaunchedEffect(Unit) {
        chatsViewModel.receiveMessage(
            roomId = roomId.toString()
        )
    }


    LaunchedEffect(Unit) {
        chatsViewModel.updateIsUserActive(
            isActive = true,
            roomId = roomId.toString(),
        )
    }

    LaunchedEffect(Unit) {
        chatsViewModel.getIsActive(
            roomId = roomId.toString(),
            receiverId = userUUID.toString()
        )
    }

    LaunchedEffect(Unit) {
        chatsViewModel.getUserIsTyping(
            roomId = roomId.toString(),
            participantId = userUUID.toString()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            chatsViewModel.updateIsUserActive(
                isActive = false,
                roomId = roomId.toString()
            )
        }
    }


    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.typing))

    val listState = rememberLazyListState()


    LaunchedEffect(chats.size) {
        if (chats.isNotEmpty()) {
            delay(100) // Let Compose finish recomposition
            listState.animateScrollToItem(chats.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(elevation = 0.5.dp),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        BadgedBox(
                            badge = {
                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = 4.dp, y = 22.dp)
                                            .size(12.dp)
                                            .background(color = primary, shape = CircleShape)
                                            .border(
                                                width = 1.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        ) {
                            AsyncImage(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape),
                                model = userImage,
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )

                        }
                        Column(horizontalAlignment = Alignment.Start) {

                            userName?.let {
                                Text(
                                    it,
                                    color = Black900,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            AnimatedVisibility(visible = isUserTyping) {
                                LottieAnimation(
                                    modifier = Modifier.size(32.dp),
                                    composition = composition,
                                    iterations = LottieConstants.IterateForever,
                                    speed = 1f,
                                    restartOnPlay = true,
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "",
                            tint = primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White900,
                    titleContentColor = White900
                ),

                )
        },
        containerColor = White900
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(PaddingValues()) // Prevents screen shifting
                .imePadding()
        ) {

            var isTodayDividerShown = false

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chats) { chat ->

                    // 🟰 Insert "Today" divider before first today's message
                    if (isToday(chat.timestamp.toLong()) && !isTodayDividerShown) {
                        isTodayDividerShown = true

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(color = White400, shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Today",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            }
                        }

                    }

                    // 🟰 Now your normal chat message item
                    if (chat.senderId == currentUser) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 40.dp),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier.background(
                                    color = primary,
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 0.dp,
                                        bottomStart = 12.dp,
                                        bottomEnd = 12.dp
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = chat.text,
                                        fontWeight = FontWeight.Normal,
                                        color = White900,
                                        fontSize = 14.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_done_all_24),
                                            contentDescription = null,
                                            tint = if (chat.read) White900 else Black300
                                        )
                                        Text(
                                            text = convertTimestampToTime(chat.timestamp.toLong()),
                                            fontSize = 12.sp,
                                            color = White900
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 40.dp),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier.background(
                                    color = White400,
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 12.dp
                                    )
                                )
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                                    Text(
                                        text = chat.text,
                                        fontWeight = FontWeight.Normal
                                    )
                                    Text(
                                        text = convertTimestampToTime(chat.timestamp.toLong()),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }


                }
            }

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = messageText,
                onValueChange = {
                    messageText = it
                },
                placeholder = {
                    Text("Write a text...")
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {

                                chatsViewModel.sendMessages(
                                    message = messageText,
                                    receiverId = userUUID.toString(),
                                ).collect {
                                    when (it) {
                                        is ResultState.Loading -> {

                                        }

                                        is ResultState.Success -> {
                                            messageText = ""
                                        }

                                        is ResultState.Error -> {

                                        }
                                    }
                                }


                            }

                        },
                        enabled = messageText.isNotEmpty()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.send_2),
                            contentDescription = null
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = White900,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
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

fun isToday(timestamp: Long): Boolean {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    val today = Calendar.getInstance()
    return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}
