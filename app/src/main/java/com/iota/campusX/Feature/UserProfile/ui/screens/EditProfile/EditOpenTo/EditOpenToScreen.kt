package com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditOpenTo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.iota.campusX.Feature.UserProfile.data.remote.response.MatchPreferenceResponse
import com.iota.campusX.Feature.UserProfile.ui.Components.EditPage
import com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents.EditProfileActions
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.ErrorScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditOpenToScreen(
    editAction: EditProfileActions.EditOpenTo,
    navController: NavController,
    viewModel: EditOpenToViewModel,
    snackBarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val matchPreferencesState by viewModel.matchPreferences.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchMatchPreferences()
        viewModel.fetchUserPreferences()
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            navController.popBackStack()
        } else if (uiState is UiState.Error) {
            snackBarHostState.showSnackbar((uiState as UiState.Error).message)
        }
    }

    EditPage(
        onCancelClick = { navController.popBackStack() },
        onSubmitClick = { viewModel.updateOpenTo() },
        isLoading = uiState is UiState.Loading,
        topBarTitle = "Open to",
        snackBarHostState = snackBarHostState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Who do you want\nto meet?",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "The kind of builders you want in your circle.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                when (val state = matchPreferencesState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Success -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.data) { preference ->
                                PreferenceCard(
                                    preference = preference,
                                    isSelected = selectedIds.contains(preference.id),
                                    onClick = { viewModel.toggleOption(preference.id) }
                                )
                            }
                        }
                    }
                    is UiState.Error -> {
                        ErrorScreen(
                            text = state.message,
                            onReTry = { viewModel.fetchMatchPreferences() }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceCard(
    preference: MatchPreferenceResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f)
    val borderWidth = if (isSelected) 1.dp else 0.5.dp

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = getIconForCode(preference.code),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = preference.title,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
fun getIconForCode(code: String): Painter {
    return when (code.uppercase()) {
        "CO_BUILDER" -> painterResource(R.drawable.hands_together)
        "HACKATHON_PARTNER" -> painterResource(R.drawable.trophy_star)
        "STUDY_BUDDY" -> painterResource(R.drawable.discussion_group)
        "PROJECT_COLLABORATOR" -> painterResource(R.drawable.code_simple)
        "MENTOR" -> painterResource(R.drawable.lightbulb_on)
        "ACCOUNTABILITY_PARTNER" -> painterResource(R.drawable.bullseye_arrow)
        "PEOPLE_IN_DOMAIN" -> painterResource(R.drawable.user_skill_gear)
        else -> painterResource(R.drawable.user)
    }
}
