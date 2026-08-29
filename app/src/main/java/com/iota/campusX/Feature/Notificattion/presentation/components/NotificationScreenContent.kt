package com.iota.campusX.Feature.Notificattion.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.iota.campusX.Feature.Notificattion.presentation.event.NotificationUiEvent
import com.iota.campusX.Feature.Notificattion.presentation.mapper.toDomain
import com.iota.campusX.Feature.Notificattion.presentation.model.NotificationUi
import com.iota.campusX.Feature.Notificattion.presentation.states.NotificationUiState
import com.iota.campusX.Feature.Notificattion.presentation.utils.NotificationDateGroup
import com.iota.campusX.R
import com.iota.campusX.Utils.StatusScreen
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
 fun NotificationScreenContent(
    modifier: Modifier,
    uiState: NotificationUiState,
    notifications: LazyPagingItems<NotificationUi>,
    snackbarHostState: SnackbarHostState,
    onEvent: (NotificationUiEvent) -> Unit,
    onBack: () -> Unit
) {

    val pullState = rememberPullToRefreshState()


    Scaffold(

        modifier = modifier,

        snackbarHost = {

            SnackbarHost(snackbarHostState)

        },

        topBar = {

            TopAppBar(
                title = {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onBack.invoke() }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.unreadCount > 0) {
                        TextButton(onClick = { onEvent(NotificationUiEvent.MarkAllRead) }) {
                            Text("Read all")
                        }
                    }
                }
            )

        }

    ) { padding ->

        PullToRefreshBox(

            state = pullState,

            isRefreshing = notifications.loadState.refresh is LoadState.Loading && notifications.itemCount > 0,

            onRefresh = {
                notifications.refresh()
                onEvent(NotificationUiEvent.Refresh)
            }

        ) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                val refreshState = notifications.loadState.refresh

                if (refreshState is LoadState.Loading && notifications.itemCount == 0) {
                    items(10) {
                        NotificationShimmer()
                    }
                }

                if (refreshState is LoadState.Error) {
                    item {
                        Column(
                            modifier = Modifier.fillParentMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Failed to load notifications: ${refreshState.error.localizedMessage}",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                if (notifications.itemCount == 0 && refreshState is LoadState.NotLoading) {
                    item {
                        StatusScreen(
                            text = "No notifications yet",
                            modifier = Modifier.fillParentMaxSize()
                        )
                    }
                }

                var lastGroup: NotificationDateGroup? = null
                
                for (index in 0 until notifications.itemCount) {
                    val notification = notifications[index]
                    
                    if (notification != null) {
                        val currentGroup = notification.createdAt.toNotificationDateGroup()
                        
                        if (currentGroup != lastGroup) {
                            item(key = "header_$index") {
                                NotificationSectionHeader(currentGroup)
                            }
                            lastGroup = currentGroup
                        }

                        item(key = notification.id) {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically() + scaleIn()
                            ) {
                                NotificationCard(
                                    notification = notification,
                                    modifier = Modifier.animateItem(),
                                    onClick = {
                                        onEvent(NotificationUiEvent.NotificationClicked(notification.toDomain()))
                                    }
                                )
                            }
                        }
                    }
                }
                
                if (notifications.loadState.append is LoadState.Loading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

fun Long.toNotificationDateGroup(): NotificationDateGroup {

    val now = Calendar.getInstance()

    val date = Calendar.getInstance()

    date.timeInMillis = this


    return when {


        now.get(Calendar.YEAR)
                == date.get(Calendar.YEAR)
                &&
                now.get(Calendar.DAY_OF_YEAR)
                == date.get(Calendar.DAY_OF_YEAR)
            -> {

            NotificationDateGroup.TODAY

        }


        now.get(Calendar.YEAR)
                == date.get(Calendar.YEAR)
                &&
                now.get(Calendar.DAY_OF_YEAR)
                -
                date.get(Calendar.DAY_OF_YEAR)
                == 1
            -> {

            NotificationDateGroup.YESTERDAY

        }


        else -> {

            NotificationDateGroup.EARLIER

        }

    }

}
