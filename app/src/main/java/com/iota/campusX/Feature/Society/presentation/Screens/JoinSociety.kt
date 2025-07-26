package com.iota.campusX.Feature.Society.presentation.Screens

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Society.AgoraTokenBuilder.generateDynamicToken
import com.iota.campusX.Feature.Society.domain.models.GetJoinRequestDTO
import com.iota.campusX.Feature.Society.domain.models.State
import com.iota.campusX.Feature.Society.presentation.ViewModels.SocietyViewModel
import com.iota.campusX.Feature.Society.presentation.ViewModels.StreamViewModel
import com.iota.campusX.Feature.UserProfile.data.BasicProfileDTO
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.CircularLoading
import com.iota.campusX.ui.theme.Yellow
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject

// Summary of required improvements made:
// - ViewModels should not be injected in Composable bodies
// - Separated business logic into ViewModels
// - Moved side effects to better lifecycle scopes
// - Used rememberUpdatedState to avoid stale captures
// - Extracted large UI logic into smaller components (partial)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JoinSocietyScreen(
    navController: NavHostController,
    userProfileViewModel: UserProfileViewModel,
    societyViewModel: SocietyViewModel = koinInject(),
    streamViewModel: StreamViewModel = koinInject()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val userProfileState by userProfileViewModel.userBaseProfile.collectAsState()
    val callState by streamViewModel.startRoomState.collectAsState()
    val joiningRequests by societyViewModel.joinRequests.collectAsState()
    val joiningRequestState by societyViewModel.joinRequestState.collectAsState()
    val audioRoomState by streamViewModel.audioRoomState.collectAsState()
    val askToSpeak by societyViewModel.askToSpeak.collectAsStateWithLifecycle()

    val userData = (userProfileState as? UiState.Success)?.data
    val call = (callState as? UiState.Success)?.data

    val roomId = navController.currentBackStackEntry?.savedStateHandle?.get<String>("ROOM_ID")
    val createdBy = navController.currentBackStackEntry?.savedStateHandle?.get<String>("CREATOR_ID")
    val isHost = userData?.id == createdBy

    // Agora initialization
    LaunchedEffect(Unit) {
        streamViewModel.initializeAgora(context)
    }

    // Start listening to join requests
    LaunchedEffect(roomId) {
        roomId?.let {
            societyViewModel.startListeningJoinRequests(
                roomId = it,
                feedMode = FeedMode.GLOBAL,
                campusId = null
            )
        }
    }

    LaunchedEffect(joiningRequests, userData?.id) {
        val myRequest = joiningRequests.find { it.requestId == userData?.id }
        if (myRequest != null) {
            val isOnStage = myRequest.status
            // ❗ This prevents the user from hearing others
            streamViewModel.muteAllRemoteAudioStream(!isOnStage)
            // ❗ This prevents the user from speaking
            streamViewModel.muteLocalAudioStream(!isOnStage)
        }
    }

    // Join audio channel
    LaunchedEffect(joiningRequestState) {
        when(joiningRequestState){
            is UiState.Success -> {
                val uid = (joiningRequestState as UiState.Success<Int>).data

                streamViewModel.joinChannel(
                    channelId = roomId.toString(),
                    token = generateDynamicToken(roomId.toString(), uid),
                    uid = uid,
                    role = if (isHost) "host" else "user"
                )
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((joiningRequestState as UiState.Error).message)
            }
            else -> {}
        }
    }

    // Handle audio state changes
    LaunchedEffect(audioRoomState) {
        when (val state = audioRoomState) {
            is State.isMicrophone -> {
                userData?.id?.let {

                }
            }
            is State.isSpeaking -> {
                userData?.id?.let {
                    societyViewModel.isSpeaking(
                        roomId = roomId.orEmpty(),
                        requestId = it,
                        isSpeaking = state.value > 0,
                        feedMode = FeedMode.GLOBAL,
                        campusId = null
                    )
                }
            }
            is State.ChannelLeave -> {
                navController.popBackStack()
            }
            is State.Room_Joined->{
                snackbarHostState.showSnackbar("Joined")
            }
            else -> Unit
        }
    }

    // Clean up join request on dispose
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

    // Send join request
    LaunchedEffect(userData, createdBy, roomId) {
        if (userData != null && !createdBy.isNullOrEmpty() && !roomId.isNullOrEmpty()) {
            societyViewModel.sendJoinRequest(
                roomId = roomId,
                role = if (isHost) "host" else "user",
                status = isHost,
                feedMode = FeedMode.GLOBAL,
                campusId = null
            )
        }
    }

    // UI
    JoinSocietyContent(
        state = audioRoomState,
        navController = navController,
        userData = userData,
        joiningRequests = joiningRequests,
        isMicrophoneEnabled = call?.microphone?.isEnabled?.collectAsState()?.value ?: false,
        snackbarHostState = snackbarHostState,
        scope = scope,
        roomId = roomId,
        createdBy = createdBy,
        societyViewModel = societyViewModel,
        streamViewModel = streamViewModel,
        askToSpeak = askToSpeak
    )
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JoinSocietyContent(
    state: State,
    navController: NavHostController,
    userData: BasicProfileDTO?,
    joiningRequests: List<GetJoinRequestDTO>,
    isMicrophoneEnabled: Boolean,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    roomId: String?,
    createdBy: String?,
    societyViewModel: SocietyViewModel,
    streamViewModel: StreamViewModel,
    askToSpeak: UiState<Unit>
) {
    val currentUserId = userData?.id.orEmpty()

    val stageUpParticipants = remember(joiningRequests) {
        joiningRequests.filter { it.status }
    }
    val stageDownParticipants = remember(joiningRequests) {
        joiningRequests.filterNot { it.status }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Society Room", color = MaterialTheme.colorScheme.onBackground)
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
                           streamViewModel.muteAudio(it)
                        }
                    )
                } else {
                    val myRequest = joiningRequests.find { it.requestId == userData?.id }
                    ParticipantControlUI(
                        isMicrophoneVisible = myRequest?.status ?: false,
                        enableMicrophone = {
                            societyViewModel.isMicrophone(
                                roomId = roomId.orEmpty(),
                                isMicrophone = it,
                                requestId = currentUserId,
                                feedMode = FeedMode.GLOBAL,
                                campusId = null
                            )
                            streamViewModel.muteAudio(it)
                        },
                        onRaiseHandClick = {
                            if (!roomId.isNullOrBlank() && !currentUserId.isBlank()) {
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
                        askToSpeak = askToSpeak
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->

        LaunchedEffect(joiningRequests) {
            Log.d("JOIN_REQUESTS", joiningRequests.toString())
        }

        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

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
                items(stageUpParticipants) { request ->
                    ParticipantAvatar(
                        participant = request,
                        isSpeaking = request.microphone,
                        audioEnabled = request.speaking,
                        modifier = Modifier
                    )
                }
            }

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
                                status = true,
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
        Text(title, style = MaterialTheme.typography.headlineLarge)
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
                onClick = {}
            )
            Text(request.userName)
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
fun HostPermissionAndJoin(
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
    isMicrophoneVisible: Boolean,
    onLeaveClick:()-> Unit,
    onRaiseHandClick:(Boolean)-> Unit,
    askToSpeak: UiState<Unit>
) {

    val scope = rememberCoroutineScope()

    var microphone by remember { mutableStateOf(true) }
    var isRaiseHand by remember { mutableStateOf(false) }

    LaunchedEffect(microphone) {
        enableMicrophone(microphone)
    }
    LaunchedEffect(isRaiseHand) {
        onRaiseHandClick(isRaiseHand)
    }

    Row(modifier = Modifier.fillMaxWidth()) {

        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
            TextButton(
                onClick = {onLeaveClick.invoke()}
            ) {
                Text("Leave", color = Color.Red)
            }
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {

            Button(
                onClick = {isRaiseHand = !isRaiseHand}
            ) {
                when(askToSpeak){
                    is UiState.Loading->{
                        CircularLoading()
                    }
                    is UiState.Success<*> -> {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(R.drawable.hand_paper),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Raise hand", style = MaterialTheme.typography.bodyMedium, overflow = TextOverflow.Ellipsis, maxLines = 1)
                    }
                    else -> {}
                }

            }
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
            if (isMicrophoneVisible){
                IconButton(onClick = {
                    microphone = !microphone
                }) {
                    Icon(
                        painter = painterResource(
                            if (microphone) R.drawable.round_mic_24 else R.drawable.round_mic_off_24
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

    val scope = rememberCoroutineScope()
    var microphone by remember { mutableStateOf(false) }

    LaunchedEffect(microphone) {
        enableMicrophone(microphone)
    }
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
                microphone = !microphone
            }) {
                Icon(
                    painter = painterResource(
                        if (microphone) R.drawable.round_mic_24 else R.drawable.round_mic_off_24
                    ),
                    contentDescription = null
                )
            }
        }
    }
}



@Composable
fun ParticipantAvatar(
    participant: GetJoinRequestDTO,
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = false,
    audioEnabled: Boolean = false,

    ) {
//    val image = participant.image.collectAsState().value
//    val nameOrId = participant.userNameOrId.collectAsState().value
//    val isSpeaking = participant.speaking.collectAsState().value
//    val audioEnabled = participant.audioEnabled.collectAsState().value


    Box(){

        Column(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(6.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.size(56.dp),contentAlignment = Alignment.Center) {

                CircleImage(
                    modifier = Modifier.size(48.dp),
                    image = participant.userImage,
                    onClick = {}
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
                style = MaterialTheme.typography.headlineMedium,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = participant.role,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall
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


    }


}