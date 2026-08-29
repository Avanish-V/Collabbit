package com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditSummary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditSummaryScreen(
    snackBarHostState: SnackbarHostState,
    editAction: EditProfileActions.EditSummary,
    navController: NavController,
    editSummaryViewModel: EditSummaryViewModel = koinViewModel(),
) {

    val uiState by editSummaryViewModel.summaryUiState.collectAsState()
    val summary by editSummaryViewModel.summary.collectAsState()

    LaunchedEffect(Unit) {
        editAction.summary?.let {
            editSummaryViewModel.setSummary(it)
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
            editSummaryViewModel.updateSummary(summary)
        },
        isLoading = uiState is UiState.Loading,
        topBarTitle = "Profile Summary",
        snackBarHostState = snackBarHostState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            SummaryHeader()

            // Summary Card
            EducationSectionCard(title = "Your Story") {
                OutlinedTextField(
                    value = summary,
                    onValueChange = { editSummaryViewModel.setSummary(it) },
                    placeholder = { Text("Describe your educational background, career goals, and what you're passionate about...") },
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

            // Storytelling Tips
            TipsSection()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SummaryHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tell Your Story",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "A great summary helps you stand out and connect with like-minded collaborators.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp).padding(top = 8.dp)
        )
    }
}

@Composable
fun TipsSection() {
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
                "Highlight your core academic interests.",
                "Mention the types of projects you'd love to join.",
                "Keep it concise but impactful (3-5 sentences)."
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
