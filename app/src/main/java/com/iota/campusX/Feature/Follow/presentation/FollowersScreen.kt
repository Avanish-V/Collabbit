package com.iota.campusX.Feature.Follow.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.UserProfile.data.ConnectionsDTO
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.Screens.ConnectionsItemView
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.ErrorScreen
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Followers(
    navHostController: NavHostController,
    followersViewModel: FollowersViewModel = koinViewModel()
) {

    val followersState = followersViewModel.followersState.collectAsState().value
    val userId = navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("USER_ID")

    LaunchedEffect(userId) {
        userId?.let { followersViewModel.getFollowers(it) }
    }


    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text(text = "Followers") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ){ padding->

        when(followersState){
            is UiState.Success -> {

                val followers = followersState.data

                if (followers.isEmpty()){
                    StatusScreen(
                        text = "No Followers",
                        image = null
                    )
                }

                LazyColumn (modifier = Modifier.padding(padding)){
                    items(followers){
                        FollowersItemView(
                            onItemClick = {
                                if (it.isCurrentUser) return@FollowersItemView
                                navHostController.navigate(Routes.Main.ProfileByID.routes).apply {
                                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                        "USER_ID",
                                        it.id
                                    )
                                }
                            },
                            connectionData = it,
                            onRejectClick = {

                            },
                            title = "Unfollow"
                        )
                        Divider()
                    }
                }
            }
            is UiState.Loading -> {
                LoadingUI()
            }
            is UiState.Error -> {
                ErrorScreen(
                    text = followersState.message,
                    image = null,
                    onReTry = {},
                    buttonText = "Retry"
                )
            }
            else -> {}
        }
    }

}

@Composable
fun FollowersItemView(
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

        }
    }
}