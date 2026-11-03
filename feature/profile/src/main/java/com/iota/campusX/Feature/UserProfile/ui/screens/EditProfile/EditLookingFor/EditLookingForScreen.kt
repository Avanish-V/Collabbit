package com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditLookingFor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.iota.campusX.Feature.UserProfile.ui.Components.EditPage
import com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents.EditProfileActions
import com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditEducation.EducationSectionCard
import com.iota.campusX.feature.profile.R
import com.iota.campusX.core.common.UiState
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditLookingForScreen(
    snackBarHostState: SnackbarHostState,
    editAction: EditProfileActions.EditLookingFor,
    navController: NavController,
    editLookingForViewModel: EditLookingForViewModel = koinViewModel(),
) {
    val uiState by editLookingForViewModel.lookingForUiState.collectAsState()
    val lookingFor by editLookingForViewModel.lookingFor.collectAsState()

    LaunchedEffect(Unit) {
        editAction.lookingFor?.let {
            editLookingForViewModel.setLookingFor(it)
        }
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
        onSubmitClick = {
            editLookingForViewModel.updateLookingFor(lookingFor)
        },
        isLoading = uiState is UiState.Loading,
        topBarTitle = "What You're Looking For",
        snackBarHostState = snackBarHostState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            LookingForHeader()

            // Text Input Card
            EducationSectionCard(title = "In Your Words") {
                OutlinedTextField(
                    value = lookingFor,
                    onValueChange = { editLookingForViewModel.setLookingFor(it) },
                    placeholder = { Text("Describe what kind of projects, collaborators, mentors, or opportunities you're looking for...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )
            }

            // Quick Tips
            LookingForTipsSection()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LookingForHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What Are You Looking For?",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Express in your own words what kind of connections, projects, or team members you want.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .padding(top = 8.dp)
        )
    }
}

@Composable
fun LookingForTipsSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.pencil),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Quick Tips",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val tips = listOf(
                "Specify roles you need (e.g. Android dev, UI designer, Co-founder).",
                "Describe the project vibe, goals, or upcoming hackathons.",
                "Mention if you're open to learning together or casual networking."
            )

            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
