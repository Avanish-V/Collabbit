package com.iota.campusX.Screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.UserProfile.data.ConnectionsDTO
import com.iota.campusX.Feature.UserProfile.presentation.ConnectionRequestState
import com.iota.campusX.Feature.UserProfile.presentation.ConnectionRequestViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.LoadingScreen
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.ErrorScreen
//import com.iota.campusX.ui.theme.secondary
//import com.iota.campusX.ui.theme.typography

import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsScreen(
    navHostController: NavHostController,
    connectionRequestViewModel: ConnectionRequestViewModel = koinInject(),
) {

    val scope = rememberCoroutineScope()
    val connections = connectionRequestViewModel.connections.collectAsStateWithLifecycle().value
    val rejectConnectionState = connectionRequestViewModel.rejectRequestState.collectAsStateWithLifecycle().value
    val user = navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("USER_ID")

    LaunchedEffect(Unit) {
        user?.let { connectionRequestViewModel.getConnections(it) }
    }

    LaunchedEffect(rejectConnectionState) {

        when(rejectConnectionState){

            is UiState.Loading -> {

            }
            is UiState.Success<*> -> {

            }
            is UiState.Error -> {

            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                       text =  "Connections",
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigation"
                        )
                    }
                },

            )
        },
    ) { padding ->

        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            when(connections){

                is UiState.Loading -> {
                    LoadingScreen()
                }
                is UiState.Success<*> ->{

                    val connectionList = (connections as UiState.Success<List<ConnectionsDTO>>).data

                    if (connectionList.isEmpty()){

                        StatusScreen(
                            text = "No Connections",
                            image = null
                        )
                        return@Scaffold
                    }

                    LazyColumn() {
                        items(connectionList) { connections ->
                            ConnectionsItemView(
                                onItemClick = {
                                    if (connections.isCurrentProfile) return@ConnectionsItemView
                                    navHostController.navigate(Routes.Main.ProfileByID.routes).apply {
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_ID",connections.id)
                                    }
                                },
                                connectionData = connections,
                                onRejectClick = {
                                    connectionRequestViewModel.request(
                                        ConnectionRequestState.RejectConnectionRequest(
                                            connections.id
                                        )
                                    )
                                },
                                title = "Remove"
                            )

                            Divider()
                        }
                    }

                }
                is UiState.Error -> {
                    ErrorScreen(
                        text = "${connections.message}Something went wrong!",
                        image = R.drawable.undraw_voice_assistant_k27k,
                        onReTry = {
                            scope.launch {
                                user?.let { connectionRequestViewModel.getConnections(it) }
                            }
                        },
                        buttonText = "Try again"
                    )
                }
                else -> {}

            }
        }
    }
}


@Composable
fun ConnectionsItemView(
    onItemClick: () -> Unit,
    connectionData: ConnectionsDTO,
    onRejectClick: () -> Unit,
    title : String
) {
    Card(
        onClick = {
            onItemClick.invoke()
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(0.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleImage(
                image = connectionData.userImage,
                modifier = Modifier.size(48.dp),
                onClick = {},
                visibility = VisibilityMode.USER
            )

            Column(modifier = Modifier.weight(1f),) {
                Text(
                    text = connectionData.userName,
                    style = typography.titleSmall,
                    maxLines = 1
                )
                if (connectionData.userBio.isNotEmpty()) {
                    Text(
                        text = connectionData.userBio,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }

            }

            if (connectionData.isCurrentUser){
                TextButton (
                    onClick = { onRejectClick.invoke() },
                    shape = RoundedCornerShape(6.dp)

                ) {
                    Text(title,style = MaterialTheme.typography.bodyMedium)
                }
            }

        }
    }
}

