package com.iota.campusX.Feature.Collab.presentation

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Utils.UiState
import org.koin.androidx.compose.koinViewModel

