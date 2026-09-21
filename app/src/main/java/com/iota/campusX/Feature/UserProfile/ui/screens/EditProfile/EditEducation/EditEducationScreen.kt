package com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditEducation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.iota.campusX.Feature.UserProfile.domain.Model.Education
import com.iota.campusX.Feature.UserProfile.ui.Components.EditPage
import com.iota.campusX.Feature.UserProfile.ui.Components.UniversityDropdown
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEducationScreen(
    initialEducation: Education?,
    navController: NavController,
    editEducationViewModel: EditEducationViewModel,
    snackBarHostState: SnackbarHostState
) {
    val query by editEducationViewModel.searchQuery.collectAsState()
    val collegeList by editEducationViewModel.universityData.collectAsState()
    val educationState by editEducationViewModel.editingEducation.collectAsState()
    val profileUpdateState by editEducationViewModel.uiState.collectAsState()

    var specializationExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        initialEducation?.let { editEducationViewModel.setEducation(it) }
    }

    LaunchedEffect(profileUpdateState) {
        if (profileUpdateState is UiState.Success) {
            navController.popBackStack()
        }
    }

    EditPage(
        onCancelClick = { navController.popBackStack() },
        onSubmitClick = { editEducationViewModel.educationUpdate() },
        isLoading = profileUpdateState is UiState.Loading,
        topBarTitle = "Edit Education",
        snackBarHostState = snackBarHostState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            EducationHeader()

            // Institution Card
            EducationSectionCard(title = "Institution") {
                UniversityDropdown(
                    placeHolder = "Search your college",
                    label = "College / University",
                    value = query,
                    universityListState = collegeList,
                    onUniversitySelected = { editEducationViewModel.onCollegeSelect(it) },
                    onFieldChange = { editEducationViewModel.onUniversityQueryChanged(it) },
                    onClearClick = { editEducationViewModel.onCollegeSelect(null) }
                )
            }

            // Program Details Card
            EducationSectionCard(title = "Program Details") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = educationState?.course ?: "",
                        onValueChange = { editEducationViewModel.setCourse(it) },
                        label = { Text("Degree / Course") },
                        placeholder = { Text("e.g. Bachelor of Technology") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.user_graduate),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    ExposedDropdownMenuBox(
                        expanded = specializationExpanded,
                        onExpandedChange = { specializationExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = educationState?.specialization ?: "",
                            onValueChange = { editEducationViewModel.setSpecialization(it) },
                            label = { Text("Field of Study") },
                            placeholder = { Text("e.g. Computer Science") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable, true),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.briefcase),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, null)
                            },
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        val filteredOptions = editEducationViewModel.fieldsOfStudy.filter {
                            it.contains(educationState?.specialization ?: "", ignoreCase = true)
                        }

                        if (filteredOptions.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = specializationExpanded,
                                onDismissRequest = { specializationExpanded = false }
                            ) {
                                filteredOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            editEducationViewModel.setSpecialization(option)
                                            specializationExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = educationState?.cgpa ?: "",
                        onValueChange = { editEducationViewModel.setCgpa(it) },
                        label = { Text("CGPA / Percentage") },
                        placeholder = { Text("e.g. 8.5") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.graph),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        )
                    )
                }
            }

            // Timeline Card
            EducationSectionCard(title = "Academic Period") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = educationState?.start ?: "",
                        onValueChange = { editEducationViewModel.setStart(it) },
                        label = { Text("Start Year") },
                        placeholder = { Text("YYYY") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.calendar),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        value = educationState?.end ?: "",
                        onValueChange = { editEducationViewModel.setEnd(it) },
                        label = { Text("End Year") },
                        placeholder = { Text("YYYY") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.calendar),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }

            // Pro-tip Section
            ProTipSection()
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProTipSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.info),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Pro-tip: Keeping your academic record updated helps our algorithm match you with the most relevant project opportunities.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun EducationHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Your Academic Journey",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Showcase your educational background to build trust and find better collaborations.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun EducationSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}
