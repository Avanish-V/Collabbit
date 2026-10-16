package com.iota.campusX.Feature.Collab.presentation

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.core.common.UiState
import org.koin.androidx.compose.koinViewModel

@Composable
fun CollabProfileTabScreen(
    userId: String,
    viewModel: CollabViewModel = koinViewModel(),
    onCollabClick: (String) -> Unit
) {
    val state by viewModel.userCollabsState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.getCollabsByUserId(userId)
    }

    when (val current = state) {
        is UiState.Loading -> {
            Text("Loading collaborations...")
        }
        is UiState.Success -> {
            LazyColumn {
                items(current.data) { collab ->
                    CollabCard(
                        collab = collab,
                        onActionClick = { onCollabClick(collab.id) },
                        onClick = { onCollabClick(collab.id) },
                        onAvatarClick = {}
                    )
                }
            }
        }
        is UiState.Error -> {
            Text("Error loading collaborations")
        }
        else -> {}
    }
}
