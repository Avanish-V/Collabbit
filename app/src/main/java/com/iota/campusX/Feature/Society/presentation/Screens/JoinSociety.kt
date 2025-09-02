package com.iota.campusX.Feature.Society.presentation.Screens

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Society.AgoraTokenBuilder.generateDynamicToken
import com.iota.campusX.Feature.Society.domain.models.*
import com.iota.campusX.Feature.Society.presentation.ViewModels.AudioRoomState
import com.iota.campusX.Feature.Society.presentation.ViewModels.AudioRoomViewModel
import com.iota.campusX.Feature.Society.presentation.ViewModels.StreamViewModel
import com.iota.campusX.Feature.Society.presentation.ViewModels.UiControls
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.theme.Light_Background_Red
import com.iota.campusX.ui.theme.Red
import com.iota.campusX.ui.theme.Yellow
import org.koin.compose.koinInject
import kotlin.Boolean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinSocietyScreen(
    navController: NavHostController,
    userProfileViewModel: UserProfileViewModel,
    streamViewModel: StreamViewModel = koinInject(),
    audioRoomViewModel: AudioRoomViewModel = koinInject()
) {

    //-----------------------------------NAV DATA---------------------------------------------------
    val roomId = navController.currentBackStackEntry?.savedStateHandle?.get<String>("ROOM_ID")
    val createdBy = navController.currentBackStackEntry?.savedStateHandle?.get<String>("CREATOR_ID")
    val title = navController.currentBackStackEntry?.savedStateHandle?.get<String>("TITLE")


    //-----------------------------------COMPOSE ITEMS----------------------------------------------

    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }

    //-----------------------------------STATES-----------------------------------------------------

    // States
    val profile = userProfileViewModel.userBaseProfile.collectAsStateWithLifecycle().value

    val joiningRequests by audioRoomViewModel.joinRequests.collectAsStateWithLifecycle()
    val agoraStates by streamViewModel.audioRoomState.collectAsStateWithLifecycle()
    val audioRoomState by audioRoomViewModel.society.collectAsStateWithLifecycle()

    val joiningRequestList = (joiningRequests as? UiState.Success<List<GetJoinRequestDTO>>)?.data

    val isHost = profile?.id == createdBy

    val userState by remember(joiningRequests, profile) {
        derivedStateOf {

            joiningRequestList?.find { it.requestId == profile?.id }
        }
    }

    var isDownVisible by remember { mutableStateOf(false) }
    //-------------------------------------EFFECTS--------------------------------------------------

    LaunchedEffect(audioRoomState) {
        when (audioRoomState) {
            is UiState.Loading -> {

            }
            is UiState.Success<*> -> {

            }
            is UiState.Error -> {
                snackBarHostState.showSnackbar("Error")
            }
            else -> {
            }

        }

    }

    LaunchMicrophonePermission {
        audioRoomViewModel.roomState(
            AudioRoomState.StartListening(roomId = roomId.orEmpty())
        )
    }

    LaunchedEffect(joiningRequests, profile?.id) {

        when(joiningRequests){
            is UiState.Loading -> {

            }
            is UiState.Success<*> -> {

                val joiningRequest = (joiningRequests as UiState.Success<List<GetJoinRequestDTO>>).data

                if (joiningRequest.isNotEmpty()){

                    val currentUser = joiningRequest.find { it.requestId == profile?.id }

                    if (currentUser == null) {
                        // Only send join request if backend has no record of this user
                        audioRoomViewModel.roomState(
                            AudioRoomState.SendJoinRequest(
                                roomId = roomId.orEmpty(),
                                role = if (isHost) "host" else "user",
                                status = if (isHost) Status.STAGE_UP else Status.IDLE,
                                feedMode = FeedMode.CAMPUS,
                                campusId = null
                            )
                        )

                    } else {
                        // Already exists → restore their previous status
                        if (currentUser.status == Status.STAGE_UP) {
                            streamViewModel.initializeAgora(context)
                            streamViewModel.joinChannel(
                                channelId = roomId.orEmpty(),
                                token = generateDynamicToken(roomId.orEmpty(), currentUser.uid!!),
                                uid = currentUser.uid,
                                role = if (isHost) "host" else "user"
                            )
                        }
                    }
                }
                else{
                    audioRoomViewModel.roomState(
                        AudioRoomState.SendJoinRequest(
                            roomId = roomId.orEmpty(),
                            role = if (isHost) "host" else "user",
                            status = if (isHost) Status.STAGE_UP else Status.IDLE,
                            feedMode = FeedMode.CAMPUS,
                            campusId = null
                        )
                    )
                }

            }
            is UiState.Error -> {
                snackBarHostState.showSnackbar("Error")
            }
            else -> {
            }
        }
    }

    LaunchedEffect(userState?.status) {

        when (userState?.status) {

            Status.STAGE_UP -> {

                streamViewModel.initializeAgora(context)

                roomId?.let {
                    userState?.uid?.let { uid ->
                        streamViewModel.joinChannel(
                            channelId = it,
                            token = generateDynamicToken(it, uid),
                            uid = uid,
                            role = if (isHost) "host" else "user"
                        )
                    }

                }
            }

            Status.STAGE_DOWN -> {
                streamViewModel.leaveChannel()
            }

            else -> {

            }

        }

    }

    LaunchedEffect(userState?.muted) {

        if (userState?.muted == true){
            streamViewModel.muteAudio(true)
            streamViewModel.disableAudio()
        }

        if (userState?.muted == false){
            streamViewModel.muteAudio(false)
            streamViewModel.enableAudio()
        }

    }

    LaunchedEffect(isHost) {
        if (isHost){
            audioRoomViewModel.roomState(
                AudioRoomState.IsRoomActive(
                    roomId = roomId.orEmpty(),
                    isActive = true
                )
            )
        }
    }

    LaunchedEffect(agoraStates) {
        when (val state = agoraStates) {
            is State.isSpeaking -> {
                profile?.id?.let { userId ->
                    audioRoomViewModel.uiControls(
                        UiControls.IsSpeaking(
                            roomId = roomId.orEmpty(),
                            isSpeaking = state.value > 0,
                            feedMode = FeedMode.GLOBAL,
                            campusId = null
                        )
                    )
                }
            }
            is State.ChannelLeave -> {
                audioRoomViewModel.roomState(
                    AudioRoomState.deleteJoinRequest(
                        roomId = roomId.orEmpty(),
                        feedMode = FeedMode.CAMPUS,
                        campusId = null
                    )
                )
                if (isHost){
                    audioRoomViewModel.roomState(
                        AudioRoomState.IsRoomActive(
                            roomId = roomId.orEmpty(),
                            isActive = false
                        )
                    )
                }
                navController.popBackStack()
            }
            else -> Unit
        }
    }



    //---------------------------------------FILTERS------------------------------------------------

    val stageUpParticipants = joiningRequestList?.filter { it.status == Status.STAGE_UP }
    val stageDownParticipants = joiningRequestList?.filter { it.status == Status.STAGE_DOWN || it.status == Status.IDLE }

    //---------------------------------------UI CONTENT---------------------------------------------

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(title?: "Society", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (userState?.status == Status.IDLE){
                                audioRoomViewModel.roomState(
                                    AudioRoomState.deleteJoinRequest(
                                        roomId = roomId.orEmpty(),
                                        feedMode = FeedMode.CAMPUS,
                                        campusId = null
                                    )
                                )
                            }
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.surface) {

                if (createdBy == profile?.id) {
                    HostControlUI(
                        onSessionEndClick = {
                            audioRoomViewModel.roomState(
                                AudioRoomState.ClearAudioRoom(
                                    roomId = roomId.orEmpty()
                                )
                            )
                            streamViewModel.leaveChannel()
                        },
                        isMicrophoneEnabled = userState?.muted ?: true,
                        enableMicrophone = {
                            audioRoomViewModel.uiControls(
                                UiControls.MuteMicrophone(
                                    roomId = roomId.orEmpty(),
                                    muted = it,
                                    feedMode = FeedMode.GLOBAL,
                                    campusId = null
                                )
                            )
                        },
                        enableSpeaker = {
                            streamViewModel.enableLoudSpeaker(it)
                        },
                    )
                }
                else{

                    ParticipantControlUI(
                        visibility = userState?.status == Status.STAGE_UP,
                        isMicrophoneEnabled = userState?.muted ?: true,
                        isHandRaise = userState?.raiseHand ?: false,
                        enableMicrophone = {
                            audioRoomViewModel.uiControls(
                                UiControls.MuteMicrophone(
                                    roomId = roomId.orEmpty(),
                                    muted = it,
                                    feedMode = FeedMode.GLOBAL,
                                    campusId = null
                                )
                            )
                        },
                        onRaiseHandClick = {
                            roomId?.let { it1 ->
                                audioRoomViewModel.uiControls(
                                    UiControls.AskToSpeak(
                                        roomId = it1,
                                        isRaiseHand = !it,
                                        feedMode = FeedMode.GLOBAL,
                                        campusId = null
                                    )
                                )
                            }
                        },
                        onLeaveClick = {
                            streamViewModel.leaveChannel()
                        },
                        enableSpeaker = {
                            streamViewModel.enableLoudSpeaker(it)
                        },

                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
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

            item(span = { GridItemSpan(3) }) {
                SectionHeader("Participants")
            }

            if (stageUpParticipants.isNullOrEmpty()) {
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
                        isSpeaking = request.speaking,
                        audioEnabled = request.muted,
                        modifier = Modifier,
                        isHost = profile?.id == createdBy,
                        onDownClick = {
                            audioRoomViewModel.roomState(
                                AudioRoomState.StageDown(
                                    roomId = roomId.orEmpty(),
                                    requestId = request.requestId,
                                    feedMode = FeedMode.GLOBAL,
                                    campusId = null
                                )
                            )
                        },
                    )
                }
            }


            item(span = { GridItemSpan(3) }) {
                SectionHeader("Stage")
            }

            if (stageDownParticipants.isNullOrEmpty())return@LazyVerticalGrid

            items(stageDownParticipants, span = { GridItemSpan(3) }) { request ->
                StageDownParticipantItem(
                    request = request,
                    isHost = createdBy == profile?.id,
                    onStageUp = {
                        roomId?.let {
                            audioRoomViewModel.roomState(
                                AudioRoomState.StageUp(
                                    roomId = it,
                                    requestId = request.requestId,
                                    feedMode = FeedMode.GLOBAL,
                                    campusId = null
                                )
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
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.small
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
                modifier = Modifier.size(42.dp),
                image = request.userImage,
                onClick = {},
                visibility = VisibilityMode.USER
            )
            Text(request.userName, style = MaterialTheme.typography.titleSmall)
        }


        if (request.raiseHand) {
            Icon(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(20.dp),
                painter = painterResource(R.drawable.hand_paper__1_),
                tint = Yellow,
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
    isHandRaise: Boolean,
    visibility: Boolean,
    onLeaveClick: () -> Unit,
    onRaiseHandClick: (Boolean) -> Unit,
    enableSpeaker: (Boolean) -> Unit
) {


    Row(modifier = modifier.fillMaxWidth()) {

        // Leave Button
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {

            if (visibility){
                TextButton(
                    onClick = onLeaveClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Red,
                        containerColor = Light_Background_Red
                    )
                ) {
                    Icon(
                        modifier = Modifier.rotate(180f),
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Exit"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Leave",)
                }
            }

        }

        // Raise Hand Button
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = {
                    onRaiseHandClick(isHandRaise)
                }
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = if (isHandRaise) painterResource(R.drawable.hand_paper__1_) else painterResource(R.drawable.hand_paper),
                    contentDescription = null,
                )
            }
        }

        // Microphone Toggle
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
            if (visibility) {
                IconButton(
                    onClick = { enableMicrophone(!isMicrophoneEnabled) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    )
                ) {
                    Icon(
                        painter = painterResource(
                            if (isMicrophoneEnabled)
                                R.drawable.round_mic_off_24
                            else
                                R.drawable.round_mic_24
                        ),
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                SpeakerPhoneToggle(
                    enableSpeaker = {
                        enableSpeaker(it)
                    }
                )

            }
        }
    }
}

@Composable
fun HostControlUI(
    onSessionEndClick:()-> Unit,
    isMicrophoneEnabled: Boolean = false,
    enableMicrophone: (Boolean) -> Unit = {},
    enableSpeaker: (Boolean) -> Unit
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
            IconButton(
                onClick = { enableMicrophone(!isMicrophoneEnabled) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            ) {
                Icon(
                    painter = painterResource(
                        if (isMicrophoneEnabled) R.drawable.round_mic_off_24 else R.drawable.round_mic_24
                    ),
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            SpeakerPhoneToggle(
                enableSpeaker = {enableSpeaker(it)}
            )
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
    isHost: Boolean,
    onDownClick:()-> Unit,
) {



    var isDownVisible by remember { mutableStateOf(false) }

    BackHandler {
        isDownVisible = false
    }

    Box(
        modifier = Modifier.combinedClickable(
        onClick = {},
        onLongClick = {
            isDownVisible = true
        },
        interactionSource = remember { MutableInteractionSource() },
        indication = null)
    ){

        Column(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(6.dp)
                )
                .fillMaxSize(),
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
                } else if (audioEnabled) {
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

            Spacer(modifier = Modifier.height(8.dp))

        }

        if (participant.raiseHand){

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(6.dp), contentAlignment = Alignment.TopEnd) {
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

        if (isHost && isDownVisible){
            IconButton(
                onClick = {
                    isDownVisible = false
                    onDownClick()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = 12.dp)
                    .rotate(-90f),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.outlineVariant,
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null
                )
            }
        }


    }


}


@Composable
fun SpeakerPhoneToggle(
    enableSpeaker: (Boolean) -> Unit
) {
    var isSpeakerEnabled by remember { mutableStateOf(false) }
    IconButton(
        onClick = {
        isSpeakerEnabled = !isSpeakerEnabled
        enableSpeaker(!isSpeakerEnabled)
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Icon(
            painter = painterResource(
                if (isSpeakerEnabled) R.drawable.outline_mobile_sound_24 else R.drawable.outline_mobile_sound_off_24
            ),
            contentDescription = null
        )
    }



}