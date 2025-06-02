package com.iota.campusX.Screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.UserProfile.data.ConnectionsDTO
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.typography

import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsScreen(navHostController: NavHostController) {

    val scope = rememberCoroutineScope()
    val profileViewModel = koinInject<UserProfileViewModel>()
    val connections = profileViewModel.connections.collectAsStateWithLifecycle().value
    val user = navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("USER_ID")

    LaunchedEffect(Unit) {
        user?.let { profileViewModel.getConnections(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                       text =  "Connections",
                       fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White900
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
        containerColor = secondary
    ) { padding ->

        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            when{
                connections.isLoading->{

                    LoadingUI(isLoading = connections.isLoading)
                }
                connections.error.isNotEmpty()->{

                    ErrorScreen(
                        isActive = true,
                        text = "Something went wrong!",
                        image = R.drawable.undraw_voice_assistant_k27k,
                        onReTry = {
                            scope.launch {
                                user?.let { profileViewModel.getConnections(it) }
                            }
                        }
                    )
                }
                connections.connectionList.isEmpty()->{
                    StatusScreen(
                        isActive = true,
                        text = "No Connections!",
                    )
                }
                else->{

                    LazyColumn() {
                        items(connections.connectionList) { connections ->
                            ConnectionsItemView(
                                onItemClick = {
                                    navHostController.navigate(Routes.Main.ProfileByID.routes).apply {
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_ID",connections.user.id)
                                    }
                                },
                                connectionData = connections,
                                onRejectClick = {
                                    scope.launch {
                                        profileViewModel.rejectLinkUpRequest(connections.user.id)
                                            .collect {
                                                when(it){
                                                    is ResultState.Loading -> {

                                                    }
                                                    is ResultState.Success->{
                                                        profileViewModel.updateConnectionDeleted(connections.user.id)
                                                    }
                                                    is ResultState.Error -> {

                                                    }

                                                }
                                            }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ConnectionsItemView(
    onItemClick: () -> Unit,
    connectionData: ConnectionsDTO,
    onRejectClick: () -> Unit
) {
    Card(
        onClick = {
            onItemClick.invoke()
        },
        colors = CardDefaults.cardColors(
            containerColor = White900
        ),
        shape = RoundedCornerShape(0.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleImage(
                image = connectionData.user.userImage,
                modifier = Modifier.size(48.dp),
                onClick = {

                }
            )

            Column(modifier = Modifier.weight(1f),) {
                Text(text = connectionData.user.userName, style = typography.headingMedium)
                if (connectionData.user.about.isNotEmpty()){
                    Text(
                        text = connectionData.user.about,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                }

            }

            if (connectionData.user.isCurrentUser == true) {
                TextButton (
                    onClick = { onRejectClick.invoke() },
                    border = BorderStroke(
                        width = 1.dp,
                        color = White400
                    ),
                    shape = RoundedCornerShape(6.dp)

                ) {
                    Text("Remove")
                }
            }
        }
    }
}

