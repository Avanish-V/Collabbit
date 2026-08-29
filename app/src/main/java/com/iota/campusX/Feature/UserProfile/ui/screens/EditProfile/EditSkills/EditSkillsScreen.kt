package com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditSkills

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.iota.campusX.Feature.UserProfile.data.remote.response.SkillResponse
import com.iota.campusX.Feature.UserProfile.ui.Components.EditPage
import com.iota.campusX.Feature.UserProfile.ui.Components.SearchableDropdown
import com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents.EditProfileActions
import com.iota.campusX.Utils.UiState

@Composable
fun EditSkillsScreen(
    editAction: EditProfileActions.EditSkills,
    navController: NavController,
    editSkillsViewModel: EditSkillsViewModel,
    snackBarHostState: SnackbarHostState,
) {


    val uiState by editSkillsViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        val initialSkills = editAction.skills?.map {
            SkillResponse(id = "", name = it.name, category = "")
        } ?: emptyList()
        editSkillsViewModel.setInitialSkills(initialSkills)
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            navController.popBackStack()
        } else if (uiState is UiState.Error) {
            snackBarHostState.showSnackbar((uiState as UiState.Error).message)
        }
    }

    val skills by editSkillsViewModel.skills.collectAsState()
    val skillsQuery by editSkillsViewModel.skillsQueryState.collectAsState()

    val skillList: List<SkillResponse> = when (val state = skillsQuery) {
        is UiState.Success -> state.data
        else -> emptyList()
    }

    var query by rememberSaveable {
        mutableStateOf("")
    }

    EditPage(
        onCancelClick = { navController.popBackStack() },
        onSubmitClick = {
            editSkillsViewModel.updateSkills()
        },
        isLoading = uiState is UiState.Loading,
        content = {

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SearchableDropdown(
                    label = "Search Skills",
                    items = skillList,
                    query = query,
                    onQueryChange = {
                        query = it
                        editSkillsViewModel.onSkillQueryChanged(it)
                    },
                    selectedItem = null,
                    itemText = { it.name },
                    onItemSelected = {
                        editSkillsViewModel.selectSkillFromDropdown(it)
                        query = "" // Clear after selection
                    },
                    isLoading = skillsQuery is UiState.Loading,
                    modifier = Modifier.fillMaxWidth()
                )
                skills.forEach {
                    AssistChip(
                        onClick = {

                        },
                        label = {
                            Text(
                                text = it.name,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        ),
                        trailingIcon = {
                            Icon(
                                modifier = Modifier.clickable(
                                    onClick = {
                                        editSkillsViewModel.removeSkill(it)
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ),
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    )
                }
            }
        },
        topBarTitle = "Interests",
        snackBarHostState = snackBarHostState
    )


}