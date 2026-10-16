package com.iota.campusX.Feature.Collab.presentation

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.Collab.data.model.ProjectRole
import com.iota.campusX.Feature.Collab.data.model.UpdateCollabRequest
import com.iota.campusX.feature.collab.R
import com.iota.campusX.core.common.UiState
import kotlinx.coroutines.launch
import java.util.UUID

@Immutable
data class EditCollabUiState(
    val title: String = "",
    val description: String = "",
    val projectUrl: String = "",
    val logoUri: String? = null,
    val roles: List<ProjectRoleUiModel> = emptyList(),
    val isUploadingLogo: Boolean = false,
    val titleError: Boolean = false,
    val descError: Boolean = false
) {
    val isTitleValid get() = title.isNotBlank() && title.length <= 60
    val isDescValid get() = description.isNotBlank() && description.length <= 500
    val isFormValid get() = isTitleValid && isDescValid && roles.isNotEmpty()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCollabScreen(
    collabId: String,
    navController: NavHostController,
    viewModel: CollabViewModel
) {
    val selectedCollabState by viewModel.selectedCollabState.collectAsStateWithLifecycle()
    val updateState by viewModel.updateCollabState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(collabId) {
        if (selectedCollabState !is UiState.Success) {
            viewModel.fetchCollabById(collabId)
        }
    }

    val collab = (selectedCollabState as? UiState.Success<CollabResponse>)?.data
    var form by remember { mutableStateOf(EditCollabUiState()) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(collab) {
        if (collab != null && !initialized) {
            form = EditCollabUiState(
                title = collab.title,
                description = collab.description,
                projectUrl = collab.projectUrl ?: "",
                logoUri = collab.logoUrl,
                roles = collab.roles.map {
                    ProjectRoleUiModel(
                        id = it.id,
                        roleName = it.roleName,
                        skills = it.skills,
                        peopleNeeded = it.peopleNeeded
                    )
                }
            )
            initialized = true
        }
    }

    LaunchedEffect(updateState) {
        when (updateState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Project updated successfully!")
                viewModel.resetUpdateCollabState()
                navController.popBackStack()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((updateState as UiState.Error).message)
                viewModel.resetUpdateCollabState()
            }
            else -> {}
        }
    }

    var showAddRoleSheet by remember { mutableStateOf(false) }
    var editingRole by remember { mutableStateOf<ProjectRoleUiModel?>(null) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                form = form.copy(logoUri = uri.toString())
            }
        }
    )

    if (showAddRoleSheet) {
        AddRoleBottomSheet(
            initialRole = editingRole,
            onDismiss = {
                showAddRoleSheet = false
                editingRole = null
            },
            onSaveRole = { saved ->
                form = if (editingRole != null) {
                    form.copy(roles = form.roles.map { if (it.id == saved.id) saved else it })
                } else {
                    form.copy(roles = form.roles + saved)
                }
                showAddRoleSheet = false
                editingRole = null
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Project",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (updateState is UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = Color(0xFF2563EB)
                        )
                    } else {
                        TextButton(
                            onClick = {
                                form = form.copy(
                                    titleError = !form.isTitleValid,
                                    descError = !form.isDescValid
                                )
                                if (!form.isFormValid) return@TextButton

                                val currentCollab = collab ?: return@TextButton
                                viewModel.updateCollabWithLogo(
                                    collabId = collabId,
                                    title = form.title,
                                    description = form.description,
                                    projectUrl = form.projectUrl,
                                    logoUri = form.logoUri,
                                    roles = form.roles,
                                    originalCollab = currentCollab
                                )
                            },
                            enabled = updateState !is UiState.Loading
                        ) {
                            Text(
                                text = "Save",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (selectedCollabState is UiState.Loading && !initialized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            FieldLabel(label = "Project Logo")
            LogoPickerRow(
                logoUri = form.logoUri,
                isUploading = form.isUploadingLogo,
                onPickClick = {
                    logoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemoveClick = { form = form.copy(logoUri = null) }
            )

            Column {
                FieldLabel(label = "Project Title", isRequired = true)
                StyledTextField(
                    value = form.title,
                    onValueChange = {
                        form = form.copy(title = it.take(60), titleError = false)
                    },
                    placeholder = "e.g. AI Campus Buddy",
                    isError = form.titleError
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnimatedVisibility(visible = form.titleError) {
                        Text(
                            text = "Title is required (max 60 characters)",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Text(
                        text = "${form.title.length}/60",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.Bottom)
                    )
                }
            }

            Column {
                FieldLabel(
                    label = "Project Description",
                    isRequired = true,
                    subtitle = "What are you building? What problem does it solve?"
                )
                StyledTextField(
                    value = form.description,
                    onValueChange = {
                        form = form.copy(description = it.take(500), descError = false)
                    },
                    placeholder = "Describe your project idea, goals, and what kind of collaborators you're looking for...",
                    singleLine = false,
                    minLines = 4,
                    maxLines = 8,
                    isError = form.descError
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnimatedVisibility(visible = form.descError) {
                        Text(
                            text = "Description is required (max 500 characters)",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "${form.description.length}/500",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.Bottom)
                    )
                }
            }

            Column {
                FieldLabel(
                    label = "Project Link",
                    subtitle = "GitHub, Figma, website, or any relevant URL (optional)"
                )
                StyledTextField(
                    value = form.projectUrl,
                    onValueChange = { form = form.copy(projectUrl = it) },
                    placeholder = "https://github.com/you/project",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FieldLabel(
                        label = "Roles Needed",
                        isRequired = true,
                        subtitle = "Who do you need on your team?"
                    )
                    if (form.roles.isNotEmpty()) {
                        TextButton(onClick = {
                            editingRole = null
                            showAddRoleSheet = true
                        }) {
                            Text(
                                text = "+ Add Role",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5C56E6),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (form.roles.isEmpty()) {
                    AddRoleEmptyCard(onClick = {
                        editingRole = null
                        showAddRoleSheet = true
                    })
                } else {
                    form.roles.forEach { role ->
                        RoleCardItem(
                            role = role,
                            onEdit = {
                                editingRole = role
                                showAddRoleSheet = true
                            },
                            onRemove = {
                                form = form.copy(roles = form.roles.filter { it.id != role.id })
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LogoPickerRow(
    logoUri: String?,
    isUploading: Boolean,
    onPickClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                .dashedBorder(1.5.dp, Color(0xFFA5B4FC), 14.dp)
                .clickable(onClick = onPickClick),
            contentAlignment = Alignment.Center
        ) {
            if (!logoUri.isNullOrBlank()) {
                AsyncImage(
                    model = logoUri,
                    contentDescription = "Project Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.image),
                        contentDescription = null,
                        tint = Color(0xFF5C56E6),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "+ Add",
                        color = Color(0xFF5C56E6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (!logoUri.isNullOrBlank()) {
                Text(
                    text = "Project logo added",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPickClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF5C56E6),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change", fontSize = 12.sp, color = Color(0xFF5C56E6))
                    }
                    OutlinedButton(
                        onClick = onRemoveClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove", fontSize = 12.sp, color = Color(0xFFEF4444))
                    }
                }
            } else {
                Text(
                    text = "Add a project logo",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Square image recommended.\nWill be shown on your project card.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun AddRoleEmptyCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = Color(0xFFEDE9FE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.people_bold),
                        contentDescription = null,
                        tint = Color(0xFF5C56E6),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = "Add team roles",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "Specify who you need — Developer, Designer, Marketer, etc.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.5.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C56E6)),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("+ Add a Role", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }
        }
    }
}
