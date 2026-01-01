package com.iota.campusX.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.ConnectionResponse
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.ConnectionsDTO
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.ConnectionRequestState
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.ConnectionRequestViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.PagingListFooter
import com.iota.campusX.Utils.CircularLoading
import com.iota.campusX.Utils.LoadingScreen
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.AlertDialogWidget
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.Divider
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    navHostController: NavHostController,
    connectionRequestViewModel: ConnectionRequestViewModel = koinInject(),
    notificationViewModel: NotificationViewModel = koinViewModel()
) {

    val snackBarHostState = remember { SnackbarHostState() }
    var isAlert by remember { mutableStateOf(false) }
    var deleteConnectionId by remember { mutableStateOf("") }


    LaunchedEffect(Unit) { connectionRequestViewModel.getPendingRequests()}
    val pendingRequests by connectionRequestViewModel.pendingRequests.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { connectionRequestViewModel.getConnectionsForCurrentUser()}
    val connections = connectionRequestViewModel.connections.collectAsLazyPagingItems()

    val acceptState by connectionRequestViewModel.acceptRequestState.collectAsStateWithLifecycle()
    val rejectState by connectionRequestViewModel.rejectRequestState.collectAsStateWithLifecycle()


    LaunchedEffect(acceptState) {
        when (acceptState) {
            is UiState.Success -> { snackBarHostState.showSnackbar("Request accepted") }
            is UiState.Error ->{ snackBarHostState.showSnackbar((acceptState as UiState.Error).message) }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        notificationViewModel.markRequestNotificationAsRead()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Connections")},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = {navHostController.popBackStack()}) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },

    ) {innerPadding->

        LazyColumn(modifier = Modifier.padding(innerPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            stickyHeader(
                title = "Requests",
                badge = {
                    Badge(
                        containerColor = if ((pendingRequests as? UiState.Success)?.data.isNullOrEmpty()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.error
                    ) {
                        Text("${(pendingRequests as? UiState.Success)?.data?.size?:0}")
                    }
                }
            )

            item { Divider() }

            when(pendingRequests){
                is UiState.Loading -> {}
                is UiState.Success<*> -> {
                    val requests = (pendingRequests as UiState.Success).data
                    if (requests.isNotEmpty()){
                        items(requests){
                            PendingConnectionItem(
                                onItemClick = {},
                                connectionData = it,
                                acceptUiState = acceptState,
                                rejectUiState = rejectState,
                                onRejectClick = {
                                    connectionRequestViewModel.request(
                                        ConnectionRequestState.RejectConnectionRequest(it)
                                    )
                                },
                                onAcceptClick = {
                                    connectionRequestViewModel.request(
                                        ConnectionRequestState.AcceptConnectionRequest(it)
                                    )
                                }
                            )
                        }
                    }
                }else -> {}
            }

            stickyHeader(
                title = "Connections",
                badge = {
                    Badge(containerColor = MaterialTheme.colorScheme.surface) {
                        Text("542")
                    }
                }
            )

            when(connections.loadState.append){
                is LoadState.Loading->{
                    item {
                        LoadingScreen()
                    }
                }
                is LoadState.Error ->{

                }
                is LoadState.NotLoading -> {
                    if (connections.itemCount == 0){
                        item {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ){
                                Column (
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ){

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {

                                        Image(
                                            modifier = Modifier.height(200.dp),
                                            painter = painterResource(R.drawable.connecion),
                                            contentDescription = null,
                                        )
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                modifier = Modifier.padding(start = 16.dp),
                                                text = "No Connections",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Explore and connect with people around you",
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                    }
                                }
                            }
                        }
                    }else{
                        items(connections.itemCount){item->
                            val data = connections[item] ?: return@items
                            ConnectionItem(
                                onItemClick = {},
                                connectionData = data,
                                acceptUiState = acceptState,
                                rejectUiState = UiState.Idle,
                                onDeleteConnection = {
                                    isAlert = true
                                    deleteConnectionId = it
                                },
                                onSendMessage = {
                                    navHostController.navigate(Routes.Main.SendMessage.routes).apply {
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_ID",data.uid)
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_NAME",data.name)
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_IMAGE",data.image)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item { Divider() }

            item {
                PagingListFooter(items = connections)
            }


        }


        if (isAlert){

            AlertDialog(
                onDismissRequest = {
                    isAlert = false
                },
                title = { Text(modifier = Modifier.fillMaxWidth(), text = "Remove Connection", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center) },
                text = {
                    Text(
                        text = "Are you sure you want to remove this connection?",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    TextButton(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),

                        onClick = {
                            connectionRequestViewModel.request(
                                ConnectionRequestState.DeleteConnection(deleteConnectionId)
                            )
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {isAlert = false},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = ButtonDefaults.outlinedButtonBorder,
                        shape = RoundedCornerShape(6.dp)
                    ) { Text("Cancel") }
                },
                shape = MaterialTheme.shapes.small,
                containerColor = MaterialTheme.colorScheme.background
            )

        }


    }
}

fun LazyListScope.stickyHeader(
    key: Any? = null,
    title: String,
    badge :@Composable ()-> Unit
) {
    stickyHeader(){
        Row (modifier = Modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.background).padding(vertical = 12.dp, horizontal = 12.dp ), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)){
            Text(title, style = typography.titleMedium)
            badge()
        }
    }

}

@Composable
fun PendingConnectionItem(
    onItemClick: () -> Unit,
    connectionData: ConnectionResponse,
    acceptUiState: UiState<Boolean> = UiState.Idle,
    rejectUiState: UiState<Boolean> = UiState.Idle,
    onRejectClick: (String) -> Unit,
    onAcceptClick: (String) -> Unit,
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
                image = connectionData.image?:"",
                modifier = Modifier.size(48.dp),
                onClick = {},
                visibility = VisibilityMode.USER
            )

            Column(modifier = Modifier.weight(1f),) {
                Text(
                    text = connectionData.name,
                    style = typography.titleSmall,
                    maxLines = 1
                )
                connectionData.tagline?.let {
                    Text(
                        text = it,
                        style = typography.bodyMedium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

            }

            Row (horizontalArrangement = Arrangement.spacedBy(12.dp)){

                ConnectionButton(
                    icon = Icons.Default.Clear,
                    isLoading = if (rejectUiState is UiState.Loading) true else false,
                    onClick = {onRejectClick.invoke(connectionData.requestId)}
                )

                ConnectionButton(
                    icon = Icons.Default.Check,
                    isLoading = if (acceptUiState is UiState.Loading) true else false,
                    onClick = {onAcceptClick.invoke(connectionData.requestId)}
                )

            }
        }
    }
}

@Composable
fun ConnectionItem(
    onItemClick: () -> Unit,
    connectionData: ConnectionResponse,
    acceptUiState: UiState<Boolean> = UiState.Idle,
    rejectUiState: UiState<Boolean> = UiState.Idle,
    onDeleteConnection: (String) -> Unit,
    onSendMessage: (String) -> Unit,
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
                image = connectionData.image?:"",
                modifier = Modifier.size(48.dp),
                onClick = {},
                visibility = VisibilityMode.USER
            )

            Column(modifier = Modifier.weight(1f),) {
                Text(
                    text = connectionData.name,
                    style = typography.titleSmall,
                    maxLines = 1
                )
                connectionData.tagline?.let {
                    Text(
                        text = it,
                        style = typography.bodyMedium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

            }

            Row (horizontalArrangement = Arrangement.spacedBy(12.dp)){

                ConnectionButton(
                    icon = Icons.Default.Delete,
                    isLoading = if (rejectUiState is UiState.Loading) true else false,
                    onClick = {onDeleteConnection.invoke(connectionData.requestId)}
                )

                ConnectionButton(
                    icon = Icons.Default.Send,
                    isLoading = if (acceptUiState is UiState.Loading) true else false,
                    onClick = {onSendMessage.invoke(connectionData.requestId)}
                )

            }
        }
    }
}









@Composable
fun ConnectionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {

    IconButton(
        modifier = Modifier.clip(MaterialTheme.shapes.small)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.small
            ),
        onClick = {onClick.invoke()},

    ) {
        if (isLoading){
            CircularLoading(color = MaterialTheme.colorScheme.primary)
        }else{
            Icon(
                imageVector = icon,
                contentDescription = ""
            )
        }

    }

}