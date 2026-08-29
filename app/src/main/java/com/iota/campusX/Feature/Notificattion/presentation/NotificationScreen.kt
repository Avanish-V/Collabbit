package com.iota.campusX.Feature.Notificattion.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.iota.campusX.Feature.Notificattion.presentation.components.NotificationScreenContent
import com.iota.campusX.Feature.Notificattion.presentation.components.ObserveEffects
import com.iota.campusX.Feature.Notificattion.presentation.event.NotificationUiEvent
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = koinViewModel(),
    navController: NavController
) {

    LaunchedEffect(Unit) {
        viewModel.onEvent(NotificationUiEvent.MarkAllRead)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val notifications = viewModel.notifications.collectAsLazyPagingItems()

    val snackbarHostState = remember { SnackbarHostState() }

    NotificationScreenContent(
        modifier = modifier,
        uiState = uiState,
        notifications = notifications,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onBack = {navController.popBackStack()}
    )

    ObserveEffects(
        viewModel = viewModel,
        navController = navController,
        snackbarHostState = snackbarHostState
    )

}