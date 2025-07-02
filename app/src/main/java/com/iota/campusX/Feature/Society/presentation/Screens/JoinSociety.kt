package com.iota.campusX.Feature.Society.presentation.Screens

import android.Manifest
import android.util.Log
import android.webkit.PermissionRequest
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Society.domain.models.JoinRequests
import com.iota.campusX.Feature.Society.presentation.ViewModels.SocietyViewModel
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.R
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.CircleImage
import io.getstream.android.video.generated.models.MemberRequest
import io.getstream.video.android.compose.permission.LaunchMicrophonePermissions
import io.getstream.video.android.compose.ui.components.call.controls.actions.ToggleMicrophoneAction
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.CreateCallOptions
import io.getstream.video.android.core.ParticipantState
import io.getstream.video.android.model.User
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JoinSociety(
    navHostController: NavHostController,
    userProfileViewModel: UserProfileViewModel
) {

    val societyViewModel = koinInject<SocietyViewModel>()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val roomId = navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("ROOM_ID")
    val createdBy = navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("CREATOR_ID")

    val userProfileState = userProfileViewModel.userBaseProfile.collectAsState().value
    val callState = societyViewModel.startRoomState.collectAsState().value
    val joiningRequests = societyViewModel.joinRequests.collectAsState().value
    val stageUpState = societyViewModel.stageUpState.collectAsState().value

    val joiningRequestState = societyViewModel.joinRequestState.collectAsState().value

    val context = LocalContext.current

    val userData = (userProfileState as UiState.Success).data


    val call = when(callState){
        is UiState.Success-> callState.data
       else -> null
    }

    LaunchedEffect(createdBy) {
        roomId?.let {
            societyViewModel.startListeningJoinRequests(
                roomId = it,
                feedMode = FeedMode.GLOBAL,
                campusId = null
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            societyViewModel.deleteJoinRequest(
                roomId = roomId ?: "",
                feedMode = FeedMode.GLOBAL,
                campusId = null
            )
            call?.leave()
        }
    }

    LaunchedEffect(roomId,createdBy) {

        if (createdBy != null && userData.id.isNotEmpty()){
            roomId?.let {
                societyViewModel.sendJoinRequest(
                    roomId = it,
                    role = if (createdBy == userData.id) "host" else "user",
                    status = createdBy == userData.id,
                    feedMode = FeedMode.GLOBAL,
                    campusId = null
                )
            }
        }

    }

    LaunchedEffect(callState) {

        when(callState){
            is UiState.Loading -> {}
            is UiState.Success<*> -> {

                Log.d("StreamCall", "Joining started")

                val result =  if (createdBy == userData.id){
                    call?.join(
                        create = true,
                        ring = false,
                        createOptions = CreateCallOptions(
                            members = listOf(
                                MemberRequest(
                                    userId = userData.id,
                                    role = "host"
                                )
                            )
                        )
                    )
                }else{
                    call?.join(
                        create = false,
                        ring = false,
                    )
                }


                result?.onSuccess {
                    Log.d("StreamCall", "Host joined successfully")
                }
                result?.onError { error ->
                    Log.e("StreamCall", "Failed to join as host: ${error.message}")
                }

            }
            is UiState.Error -> {}
            else -> {}
        }

    }

    LaunchedEffect(joiningRequestState) {

        when(joiningRequestState){
            is UiState.Loading -> {}
            is UiState.Success<*> ->{

                Log.d("StreamCall", "Initiated")

                societyViewModel.startRoom(
                    user = User(
                        id = userData.id,
                        name = userData.userName,
                        image = userData.userImage,
                        role = if (createdBy == userData.id) "host" else "user"
                    ),
                    context = context
                )


            }
            is UiState.Error -> {}
            else -> {}

        }

    }

    LaunchedEffect(stageUpState) {
        when(stageUpState){
            is UiState.Loading -> {}
            is UiState.Success<*> -> {
                val requestId = stageUpState.data as String
                call?.grantPermissions(
                    userId = requestId,
                    listOf("send-audio","receive-audio")
                )
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = stageUpState.message,
                    actionLabel = "Dismiss"
                )
            }
            else -> {}
        }
    }

    HostPermissionAndJoin {

    }


    val participants = call?.state?.participants?.collectAsState()?.value
    val backstage = call?.state?.backstage?.collectAsState()?.value
    val isMicrophoneEnabled = call?.microphone?.isEnabled?.collectAsState()?.value


    val localParticipant = participants?.firstOrNull {
        val userId  = it.userNameOrId.collectAsState().value
        Log.d("StreamCall", "userId for speaking: $userId")
        userId == userData.userName
    }

    val isSpeaking = localParticipant?.speaking?.collectAsState()?.value

    LaunchedEffect(isMicrophoneEnabled) {
        societyViewModel.isMicrophone(
            roomId = roomId ?: "",
            requestId = userData.id,
            isMicrophone = isMicrophoneEnabled ?: false,
            feedMode = FeedMode.GLOBAL,
            campusId = null
        )
    }

    LaunchedEffect(isSpeaking ) {
        societyViewModel.isSpeaking(
            roomId = roomId ?: "",
            requestId = userData.id,
            isSpeaking = isSpeaking?:false,
            feedMode = FeedMode.GLOBAL,
            campusId = null
        )
    }

    LaunchedEffect(callState) {
        Log.d("StreamCall", "callState: $participants")
        Log.d("StreamCall", "callState: ${call?.user}")
        Log.d("StreamCall", "backstage: ${backstage}")

    }
    LaunchedEffect(isMicrophoneEnabled) {
        Log.d("StreamCall", "microphone: ${isMicrophoneEnabled}")
    }
    LaunchedEffect(isSpeaking) {
        Log.d("StreamCall", "speaking: ${isSpeaking}")
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Society Room") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.White) {

                call?.let {

                    if (call.user.role == "host"){
                        backstage?.let {
                            HostControlUI(
                                call = call,
                                backstage = backstage,
                                isMicrophoneEnabled = isMicrophoneEnabled?:false,
                                enableMicrophone = {
                                    scope.launch {
                                        call.microphone.setEnabled(it)
                                    }
                                }
                            )
                        }
                    }
                    else

                        ParticipantControlUI(
                            call = call,
                            isMicrophoneEnabled = isMicrophoneEnabled?:false,
                            enableMicrophone = {
                                scope.launch {
                                    call.microphone.setEnabled(it)
                                }
                            }

                        )

                }

            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->



        LazyColumn(modifier = Modifier.padding(paddingValues),contentPadding = PaddingValues(16.dp)) {

            item {
                RoomHeader("Participant")
            }

            joiningRequests.let { participants ->

               val stageUpParticipants =  participants.filter { it.status }
               val stageDownParticipants =  participants.filter { !it.status }

                if (stageUpParticipants.isEmpty()){
                    item {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp), contentAlignment = Alignment.Center){
                            Text("Waiting for host..")
                        }
                    }
                } else{
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            maxItemsInEachRow = 3
                        ) {
                            stageUpParticipants.forEach {request->
                                ParticipantAvatar(
                                    participant = request,
                                    isSpeaking = request.isSpeaking,
                                    audioEnabled = request.isMicrophone,
                                    modifier = Modifier
                                )

                            }
                        }
                    }
                }

                item {
                    RoomHeader("Stage")
                }

                item {
                    FlowRow {
                        stageDownParticipants.forEach { request ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircleImage(
                                    modifier = Modifier.size(48.dp),
                                    image = request.userImage,
                                    onClick = {

                                    }
                                )
                                Text( request.userName)

                                if (createdBy == userData.id){
                                    TextButton(onClick = {
                                        societyViewModel.stageUp(
                                            roomId = roomId ?: "",
                                            status = true,
                                            requestId = request.requestId,
                                            feedMode = FeedMode.GLOBAL,
                                            campusId = null
                                        )
                                    }){
                                        Text("StageUP")
                                    }
                                }
                            }
                        }
                    }
                }
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
fun RoomHeader(header: String) {
    Row (modifier = Modifier.fillMaxWidth().height(60.dp),verticalAlignment = Alignment.CenterVertically){
        Text(header)
    }
}


@Composable
fun ParticipantControlUI(
    modifier: Modifier = Modifier,
    call: Call,
    isMicrophoneEnabled: Boolean = false,
    enableMicrophone: (Boolean) -> Unit = {}

) {

    val scope = rememberCoroutineScope()

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {

        TextButton(onClick = {
            scope.launch { call.leave() }
        }) {
            Text("Leave", color = Color.Red)
        }

        Button(onClick = {
            scope.launch {
                call.requestPermissions("send-audio")
            }
        }) {
            Text("Ask to Speak")
        }

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



@Composable
fun HostControlUI(
    call: Call,
    backstage: Boolean = false,
    isMicrophoneEnabled: Boolean = false,
    enableMicrophone: (Boolean) -> Unit = {}
) {

    val scope = rememberCoroutineScope()

    Row(modifier = Modifier.fillMaxWidth()) {

        Button(
            onClick = {
                scope.launch {
                    if (backstage) call.goLive() else call.stopLive()
                }
            }
        ) {
            Text(text = if (backstage) "Go Live" else "End")
        }

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



@Composable
fun ParticipantAvatar(
    participant: JoinRequests,
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = false,
    audioEnabled: Boolean = false,

) {
//    val image = participant.image.collectAsState().value
//    val nameOrId = participant.userNameOrId.collectAsState().value
//    val isSpeaking = participant.speaking.collectAsState().value
//    val audioEnabled = participant.audioEnabled.collectAsState().value


    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

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
            text = participant.userName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
        )

        Text(
            text = participant.role,
            fontSize = 11.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
        )


    }
}