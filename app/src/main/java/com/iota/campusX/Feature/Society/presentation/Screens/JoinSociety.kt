package com.iota.campusX.Feature.Society.presentation.Screens

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Society.AgoraTokenBuilder.generateDynamicToken
import com.iota.campusX.Feature.Society.domain.models.*
import com.iota.campusX.Feature.Society.presentation.ViewModels.SocietyViewModel
import com.iota.campusX.Feature.Society.presentation.ViewModels.StreamViewModel
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.R
import com.iota.campusX.Screens.Chat.DropDownItem
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.AppLabelText
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.CircularLoading
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinSocietyScreen(
    navController: NavHostController,
    userProfileViewModel: UserProfileViewModel,
    societyViewModel: SocietyViewModel = koinInject(),
    streamViewModel: StreamViewModel = koinInject()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // States
    val userProfileState by userProfileViewModel.userBaseProfile.collectAsStateWithLifecycle()
    val joiningRequests by societyViewModel.joinRequests.collectAsStateWithLifecycle()
    val audioRoomState by streamViewModel.audioRoomState.collectAsStateWithLifecycle()
    val askToSpeak by societyViewModel.askToSpeak.collectAsStateWithLifecycle()
    val microphone by societyViewModel.microphone.collectAsStateWithLifecycle()

    val userData = (userProfileState as? UiState.Success)?.data
    val roomId = navController.currentBackStackEntry?.savedStateHandle?.get<String>("ROOM_ID")
    val createdBy = navController.currentBackStackEntry?.savedStateHandle?.get<String>("CREATOR_ID")
    val isHost = userData?.id == createdBy

    val joinRequest by remember(joiningRequests, userData) {
        derivedStateOf {
            joiningRequests.find { it.requestId == userData?.id }
        }
    }

    // 🔑 Microphone Permission
    LaunchMicrophonePermission {
        if (userData != null && !createdBy.isNullOrEmpty() && !roomId.isNullOrEmpty()) {
            societyViewModel.sendJoinRequest(
                roomId = roomId,
                role = if (isHost) "host" else "user",
                status = if (isHost) Status.STAGE_UP else Status.IDLE,
                feedMode = FeedMode.GLOBAL,
                campusId = null
            )
        }
    }

    // Agora Init
    LaunchedEffect(Unit) {
        streamViewModel.initializeAgora(context)
    }

    // Handle Stage Status
    LaunchedEffect(joinRequest?.status, roomId, joinRequest?.uid) {
        when (joinRequest?.status) {
            Status.STAGE_UP -> {
                roomId?.let {
                    joinRequest?.uid?.let { uid ->
                        streamViewModel.joinChannel(
                            channelId = it,
                            token = generateDynamicToken(it, uid),
                            uid = uid,
                            role = if (isHost) "host" else "user"
                        )
                    }
                }
            }
            Status.STAGE_DOWN -> streamViewModel.leaveChannel()
            else -> Unit
        }
    }

    // Microphone toggle from backend state
    LaunchedEffect(joinRequest?.microphone) {
        if (joinRequest?.status == Status.STAGE_UP) {
            streamViewModel.muteLocalAudioStream(!(joinRequest?.microphone ?: false))
        }
    }

    // Microphone error handling
    LaunchedEffect(microphone) {
        if (microphone is UiState.Error) {
            snackbarHostState.showSnackbar((microphone as UiState.Error).message)
        }
    }

    // Agora events handling
    LaunchedEffect(audioRoomState) {
        when (val state = audioRoomState) {
            is State.isSpeaking -> {
                userData?.id?.let { userId ->
                    societyViewModel.isSpeaking(
                        roomId = roomId.orEmpty(),
                        requestId = userId,
                        isSpeaking = state.value > 0,
                        feedMode = FeedMode.GLOBAL,
                        campusId = null
                    )
                }
            }
            is State.ChannelLeave -> navController.popBackStack()
            else -> Unit
        }
    }

    // Cleanup when leaving
    DisposableEffect(roomId, createdBy) {
        onDispose {
            roomId?.let {
                societyViewModel.deleteJoinRequest(
                    roomId = it,
                    feedMode = FeedMode.GLOBAL,
                    campusId = null
                )
            }
        }
    }

    // Default: reset raised hand
    LaunchedEffect(roomId) {
        if (!roomId.isNullOrBlank()) {
            societyViewModel.askToSpeak(
                roomId = roomId,
                isRaiseHand = false,
                requestId = userData?.id.orEmpty(),
                feedMode = FeedMode.GLOBAL,
                campusId = null
            )
        }
    }

    // ✅ UI Content
    JoinSocietyContent(
        navController = navController,
        userData = userData,
        joiningRequests = joiningRequests,
        isMicrophoneEnabled = joinRequest?.microphone ?: false,
        snackbarHostState = snackbarHostState,
        roomId = roomId,
        createdBy = createdBy,
        societyViewModel = societyViewModel,
        streamViewModel = streamViewModel,
        askToSpeakState = askToSpeak
    )
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JoinSocietyContent(
    navController: NavHostController,
    userData: BaseProfileDTO?,
    joiningRequests: List<GetJoinRequestDTO>,
    isMicrophoneEnabled: Boolean,
    snackbarHostState: SnackbarHostState,
    roomId: String?,
    createdBy: String?,
    societyViewModel: SocietyViewModel,
    streamViewModel: StreamViewModel,
    askToSpeakState: UiState<Unit>
) {
    val currentUserId = userData?.id.orEmpty()

    // ✅ FIXED: Don't use remember here unless you're certain it's necessary
    val stageUpParticipants = joiningRequests.filter { it.status == Status.STAGE_UP }
    val stageDownParticipants = joiningRequests.filter { it.status == Status.STAGE_DOWN || it.status == Status.IDLE }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Society Room", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.surface) {
                if (createdBy == currentUserId) {
                    HostControlUI(
                        onSessionEndClick = { streamViewModel.leaveChannel() },
                        isMicrophoneEnabled = isMicrophoneEnabled,
                        enableMicrophone = {
                            societyViewModel.isMicrophone(
                                roomId = roomId.orEmpty(),
                                isMicrophone = it,
                                requestId = currentUserId,
                                feedMode = FeedMode.GLOBAL,
                                campusId = null
                            )
                        }
                    )
                } else {
                    val status = joiningRequests.find { it.requestId == userData?.id }?.status
                    ParticipantControlUI(
                        isMicrophoneVisible = status == Status.STAGE_UP,
                        isMicrophoneEnabled = isMicrophoneEnabled,
                        enableMicrophone = {
                            societyViewModel.isMicrophone(
                                roomId = roomId.orEmpty(),
                                isMicrophone = it,
                                requestId = currentUserId,
                                feedMode = FeedMode.GLOBAL,
                                campusId = null
                            )
                        },
                        onRaiseHandClick = {
                            if (!roomId.isNullOrBlank()) {
                                societyViewModel.askToSpeak(
                                    roomId = roomId,
                                    isRaiseHand = it,
                                    requestId = currentUserId,
                                    feedMode = FeedMode.GLOBAL,
                                    campusId = null
                                )
                            }
                        },
                        onLeaveClick = {
                            streamViewModel.leaveChannel()
                        },
                        askToSpeak = askToSpeakState
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✅ Section 1: On-stage participants
            item(span = { GridItemSpan(3) }) {
                SectionHeader("Participants")
            }

            if (stageUpParticipants.isEmpty()) {
                item(span = { GridItemSpan(3) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Waiting for host...")
                    }
                }
            } else {
                items(stageUpParticipants, span = { GridItemSpan(1) }) { request ->
                    ParticipantAvatar(
                        participant = request,
                        isSpeaking = request.microphone,
                        audioEnabled = request.speaking,
                        modifier = Modifier,
                        onDownClick = {
                            societyViewModel.stageUp(
                                roomId = roomId.orEmpty(),
                                status = Status.STAGE_DOWN,
                                requestId = request.requestId,
                                feedMode = FeedMode.GLOBAL,
                                campusId = null
                            )
                        }
                    )
                }
            }

            // ✅ Section 2: Stage-down / waiting participants
            item(span = { GridItemSpan(3) }) {
                SectionHeader("Stage")
            }

            items(stageDownParticipants, span = { GridItemSpan(3) }) { request ->
                StageDownParticipantItem(
                    request = request,
                    isHost = createdBy == currentUserId,
                    onStageUp = {
                        if (!roomId.isNullOrBlank()) {
                            societyViewModel.stageUp(
                                roomId = roomId,
                                status = Status.STAGE_UP,
                                requestId = request.requestId,
                                feedMode = FeedMode.GLOBAL,
                                campusId = null
                            )
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun StageDownParticipantItem(
    request: GetJoinRequestDTO,
    isHost: Boolean,
    onStageUp: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircleImage(
                modifier = Modifier.size(48.dp),
                image = request.userImage,
                onClick = {},
                visibility = VisibilityMode.USER
            )
            Text(request.userName)
        }


        if (request.raiseHand) {
            Icon(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(20.dp),
                painter = painterResource(R.drawable.hand_paper__1_),
//                tint = Yellow,
                contentDescription = "Raised Hand"
            )
        }

        if (isHost) {
            IconButton(
                onClick = onStageUp,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_arrow_upward_24),
                    contentDescription = "Approve Stage Up"
                )
            }
        }
    }
}

@Composable
fun LaunchMicrophonePermission(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            Toast.makeText(context, "Microphone permission required to host", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
}

@Composable
fun ParticipantControlUI(
    modifier: Modifier = Modifier,
    enableMicrophone: (Boolean) -> Unit,
    isMicrophoneEnabled: Boolean,
    isMicrophoneVisible: Boolean,
    onLeaveClick: () -> Unit,
    onRaiseHandClick: (Boolean) -> Unit,
    askToSpeak: UiState<Unit>
) {
    var isRaiseHand by remember { mutableStateOf(false) }

    Row(modifier = modifier.fillMaxWidth()) {

        // Leave Button
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = onLeaveClick) {
                Text("Leave", color = Color.Red)
            }
        }

        // Raise Hand Button
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = {
                    isRaiseHand = !isRaiseHand
                    onRaiseHandClick(isRaiseHand)
                }
            ) {
                when (askToSpeak) {
                    is UiState.Loading -> CircularLoading()
                    is UiState.Success<*> -> {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(R.drawable.hand_paper),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Raise hand",
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                    else -> Unit
                }
            }
        }

        // Microphone Toggle
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
            if (isMicrophoneVisible) {
                IconButton(
                    onClick = { enableMicrophone(!isMicrophoneEnabled) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isMicrophoneEnabled) MaterialTheme.colorScheme.primary else Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(
                            if (isMicrophoneEnabled)
                                R.drawable.round_mic_24
                            else
                                R.drawable.round_mic_off_24
                        ),
                        contentDescription = null
                    )
                }
            }
        }
    }
}




@Composable
fun HostControlUI(
    onSessionEndClick:()-> Unit,
    isMicrophoneEnabled: Boolean = false,
    enableMicrophone: (Boolean) -> Unit = {}
) {

    Row(modifier = Modifier.fillMaxWidth()) {

        Row (modifier = Modifier.weight(1f)){

        }
        Row (modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center){
            Button(
                onClick = {
                    onSessionEndClick.invoke()
                }
            ) {
                Text(text = "End")
            }
        }
        Row (modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center){
            IconButton(onClick = {
               enableMicrophone(!isMicrophoneEnabled)
            }) {
                Icon(
                    painter = painterResource(
                        if (isMicrophoneEnabled) R.drawable.round_mic_24 else R.drawable.round_mic_off_24
                    ),
                    contentDescription = null
                )
            }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ParticipantAvatar(
    participant: GetJoinRequestDTO,
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = false,
    audioEnabled: Boolean = false,
    onDownClick:()-> Unit
    ) {
//    val image = participant.image.collectAsState().value
//    val nameOrId = participant.userNameOrId.collectAsState().value
//    val isSpeaking = participant.speaking.collectAsState().value
//    val audioEnabled = participant.audioEnabled.collectAsState().value


    var isMenuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.combinedClickable(
        onClick = {},
        onLongClick = { isMenuExpanded = true },
        interactionSource = remember { MutableInteractionSource() },
        indication = null)
    ){

        Column(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.size(56.dp),contentAlignment = Alignment.Center) {

                CircleImage(
                    modifier = Modifier.size(48.dp),
                    image = participant.userImage,
                    onClick = {},
                    visibility = VisibilityMode.USER
                )

                if (isSpeaking) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(BorderStroke(2.dp, Color.Gray), CircleShape)
                    )
                } else if (!audioEnabled) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black)
                                .size(16.dp)
                        ) {
                            Icon(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(3.dp),
                                painter = painterResource(id = R.drawable.round_mic_off_24),
                                tint = Color.White,
                                contentDescription = null
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                modifier = Modifier.padding(horizontal = 6.dp),
                text = participant.userName,
                maxLines = 1,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis
            )

            AppLabelText(
                text = participant.role,
            )
            Spacer(modifier = Modifier.height(8.dp))

        }

        if (participant.raiseHand){

            Box(modifier = Modifier.fillMaxSize().padding(6.dp), contentAlignment = Alignment.TopEnd) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(3.dp),
                    painter = painterResource(id = R.drawable.hand_paper__1_),
                    tint = Yellow,
                    contentDescription = null
                )
            }

        }


        DropdownMenu(
            expanded = isMenuExpanded,
            offset = DpOffset(x = (-0).dp, y = 20.dp),
            onDismissRequest = { isMenuExpanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {

            DropDownItem(
                onDelete = {
                    isMenuExpanded = false
                    scope.launch {
                        delay(150) // Give time for the menu to dismiss
                        onDownClick()
                    }
                },
                title = "Down",
                icon = R.drawable.round_arrow_upward_24
            )


        }


    }


}