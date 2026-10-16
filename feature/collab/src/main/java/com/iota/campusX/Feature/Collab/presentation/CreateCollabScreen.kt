package com.iota.campusX.Feature.Collab.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Collab.data.model.ProjectStatus
import com.iota.campusX.Feature.Collab.presentation.components.ProjectStatusBadge
import com.iota.campusX.Navigation.Collab
import com.iota.campusX.Navigation.CollabDetail
import com.iota.campusX.feature.collab.R
import java.util.UUID

fun Modifier.dashedBorder(
    strokeWidth: Dp,
    color: Color,
    cornerRadius: Dp
) = drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

@Composable
fun HeroIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 130.dp, height = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(width = 95.dp, height = 70.dp)
                .align(Alignment.BottomEnd)
                .graphicsLayer(rotationZ = -6f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF5C56E6),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "</>",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Surface(
            modifier = Modifier
                .size(42.dp)
                .align(Alignment.TopEnd)
                .padding(end = 8.dp)
                .graphicsLayer(rotationZ = 12f),
            shape = CircleShape,
            color = Color(0xFFFEF08A),
            shadowElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.lightbulb_on),
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Surface(
            modifier = Modifier
                .size(width = 44.dp, height = 34.dp)
                .align(Alignment.TopStart)
                .padding(top = 8.dp)
                .graphicsLayer(rotationZ = -12f),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFDE047),
            shadowElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.code_simple),
                    contentDescription = null,
                    tint = Color(0xFF854D0E),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun FieldLabel(
    label: String,
    isRequired: Boolean = false,
    subtitle: String? = null
) {
    Column(modifier = Modifier.padding(bottom = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
            )
            if (isRequired) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        fontSize = 15.sp
                    )
                )
            }
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = if (isError) Color(0xFFEF4444) else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp
        ),
        cursorBrush = SolidColor(Color(0xFF5C56E6)),
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.TopStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun RoleCardItem(
    role: ProjectRoleUiModel,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    val (iconRes, colors) = getRoleIconAndBg(role.roleName)
    val (bgColor, iconColor) = colors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = role.roleName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                )
                if (role.skills.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = role.skills.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF5C56E6),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "x${role.peopleNeeded}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                )

                Text(
                    text = " | ",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                )

                Text(
                    text = "Edit",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF5C56E6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onEdit() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable { onRemove() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCollabScreen(
    navController: NavHostController,
    createCollabViewModel: CreateCollabViewModel
) {
    val state by createCollabViewModel.uiState.collectAsStateWithLifecycle()
    var editingRole by remember { mutableStateOf<ProjectRoleUiModel?>(null) }
    var showAddRoleSheet by remember { mutableStateOf(false) }
    var showProjectPreviewModal by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { err ->
            snackbarHostState.showSnackbar(err)
            createCollabViewModel.clearError()
        }
    }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                createCollabViewModel.updateLogoUri(uri.toString())
            }
        }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!state.isCreated) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Start a Project",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (!state.isCreated) {
                CreateProjectBottomBar(
                    state = state,
                    onCreateProjectClick = { createCollabViewModel.createProject() }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.isCreated) {
                ProjectCreatedSuccessScreen(
                    state = state,
                    onViewProject = {
                        val collabId = state.createdCollab?.id
                        if (collabId != null) {
                            createCollabViewModel.resetFlow()
                            navController.navigate(CollabDetail(collabId = collabId)) {
                                popUpTo(Collab) { inclusive = false }
                            }
                        } else {
                            showProjectPreviewModal = true
                        }
                    },
                    onBackToCollaborations = {
                        createCollabViewModel.resetFlow()
                        navController.popBackStack()
                    }
                )
            } else {
                CreateProjectFormContent(
                    state = state,
                    createCollabViewModel = createCollabViewModel,
                    logoPickerLauncher = logoPickerLauncher,
                    navController = navController,
                    onAddRoleClick = {
                        editingRole = null
                        showAddRoleSheet = true
                    },
                    onEditRoleClick = { role ->
                        editingRole = role
                        showAddRoleSheet = true
                    }
                )
            }
        }

        if (showAddRoleSheet) {
            AddRoleBottomSheet(
                initialRole = editingRole,
                onDismiss = { showAddRoleSheet = false },
                onSaveRole = { role ->
                    if (editingRole != null) {
                        createCollabViewModel.updateRole(role)
                    } else {
                        createCollabViewModel.addRole(role)
                    }
                    showAddRoleSheet = false
                }
            )
        }

        if (showProjectPreviewModal) {
            ProjectViewModal(
                state = state,
                onDismiss = { showProjectPreviewModal = false }
            )
        }
    }
}

@Composable
fun CreateProjectBottomBar(
    state: CreateProjectUiState,
    onCreateProjectClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = onCreateProjectClick,
            enabled = state.isFormValid && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(54.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color(0xFFC7D2FE),
                contentColor = Color.White,
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            )
        ) {
            if (state.isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = if (state.isUploadingLogo) "Uploading logo..." else "Creating project...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            } else {
                Text(
                    text = "Create Project",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
fun CreateProjectFormContent(
    state: CreateProjectUiState,
    createCollabViewModel: CreateCollabViewModel,
    logoPickerLauncher: androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest>,
    navController: NavHostController,
    onAddRoleClick: () -> Unit,
    onEditRoleClick: (ProjectRoleUiModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Create something\ntogether",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 32.sp
                ),
                modifier = Modifier.weight(1f)
            )

            HeroIllustration()
        }

        ProjectLogoSection(
            state = state,
            onPickLogo = {
                logoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemoveLogo = { createCollabViewModel.updateLogoUri(null) }
        )

        ProjectTitleSection(
            title = state.title,
            isTouched = state.titleTouched,
            onTitleChange = { createCollabViewModel.updateTitle(it) }
        )

        ProjectIdeaSection(
            idea = state.idea,
            isTouched = state.ideaTouched,
            onIdeaChange = { createCollabViewModel.updateIdea(it) }
        )

        ProjectUrlSection(
            projectUrl = state.projectUrl,
            isTouched = state.urlTouched,
            isValid = state.isUrlValid,
            onUrlChange = { createCollabViewModel.updateProjectUrl(it) }
        )

        ProjectRolesSection(
            roles = state.roles,
            onAddRoleClick = onAddRoleClick,
            onEditRoleClick = onEditRoleClick,
            onRemoveRoleClick = { createCollabViewModel.removeRole(it) },
            onBackClick = { navController.popBackStack() }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProjectLogoSection(
    state: CreateProjectUiState,
    onPickLogo: () -> Unit,
    onRemoveLogo: () -> Unit
) {
    Column {
        FieldLabel(label = "Project Logo")

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
                    .dashedBorder(
                        strokeWidth = 1.5.dp,
                        color = Color(0xFFA5B4FC),
                        cornerRadius = 14.dp
                    )
                    .clickable(enabled = !state.isLoading, onClick = onPickLogo),
                contentAlignment = Alignment.Center
            ) {
                if (state.logoUri != null) {
                    AsyncImage(
                        model = state.logoUri,
                        contentDescription = "Project Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (state.isUploadingLogo) {
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
                if (state.logoUri != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_verified_24),
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (state.isUploadingLogo) "Uploading logo..." else "Logo Selected",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable(enabled = !state.isLoading, onClick = onPickLogo)
                        ) {
                            Text(
                                text = "Change",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.clickable(enabled = !state.isLoading, onClick = onRemoveLogo)
                        ) {
                            Text(
                                text = "Remove",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Upload project logo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Square image • PNG or JPG (Optional)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectTitleSection(
    title: String,
    isTouched: Boolean,
    onTitleChange: (String) -> Unit
) {
    Column {
        FieldLabel(label = "Project Title", isRequired = true)

        StyledTextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = "e.g. AI Resume Analyzer",
            isError = (isTouched && title.isBlank()) || title.length > 60
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isTouched && title.isBlank()) {
                Text(
                    text = "Project title is required",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFEF4444)
                )
            } else if (title.length > 60) {
                Text(
                    text = "Maximum 60 characters allowed",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFEF4444)
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            Text(
                text = "${title.length}/60",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun ProjectIdeaSection(
    idea: String,
    isTouched: Boolean,
    onIdeaChange: (String) -> Unit
) {
    Column {
        FieldLabel(label = "Project Idea", isRequired = true)

        StyledTextField(
            value = idea,
            onValueChange = onIdeaChange,
            placeholder = "Tell people what you're building and why.",
            singleLine = false,
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.heightIn(min = 110.dp),
            isError = (isTouched && idea.isBlank()) || idea.length > 500
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isTouched && idea.isBlank()) {
                Text(
                    text = "Project idea is required",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFEF4444)
                )
            } else if (idea.length > 500) {
                Text(
                    text = "Maximum 500 characters allowed",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFEF4444)
                )
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            Text(
                text = "${idea.length}/500",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun ProjectUrlSection(
    projectUrl: String,
    isTouched: Boolean,
    isValid: Boolean,
    onUrlChange: (String) -> Unit
) {
    Column {
        FieldLabel(label = "Project URL", subtitle = "Optional")

        StyledTextField(
            value = projectUrl,
            onValueChange = onUrlChange,
            placeholder = "https://github.com/...",
            isError = isTouched && !isValid
        )

        if (isTouched && !isValid) {
            Text(
                text = "Please enter a valid URL (e.g. https://github.com)",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFEF4444),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ProjectRolesSection(
    roles: List<ProjectRoleUiModel>,
    onAddRoleClick: () -> Unit,
    onEditRoleClick: (ProjectRoleUiModel) -> Unit,
    onRemoveRoleClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Column {
        Text(
            text = "Looking for",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            if (roles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.people_bold),
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "No roles added yet",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onAddRoleClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C56E6)),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "+ Add Role",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    roles.forEach { role ->
                        RoleCardItem(
                            role = role,
                            onEdit = { onEditRoleClick(role) },
                            onRemove = { onRemoveRoleClick(role.id) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onAddRoleClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        )
                    ) {
                        Text(
                            text = "+ Add Role",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2563EB))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.people_bold),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Back to Collaborations",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRoleBottomSheet(
    initialRole: ProjectRoleUiModel? = null,
    onDismiss: () -> Unit,
    onSaveRole: (ProjectRoleUiModel) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        var roleName by remember { mutableStateOf(initialRole?.roleName ?: "") }
        var selectedSkills by remember { mutableStateOf(initialRole?.skills ?: emptyList()) }
        var skillInput by remember { mutableStateOf("") }
        var peopleNeeded by remember { mutableIntStateOf(initialRole?.peopleNeeded ?: 1) }

        val presetSkills = remember {
            listOf(
                "Kotlin", "Jetpack Compose", "Java", "React", "Node.js", "Python",
                "AI / ML", "Figma", "Firebase", "Swift", "Flutter", "UI/UX",
                "Spring Boot", "PostgreSQL", "SQL", "Git"
            )
        }

        val filteredPresets = remember(skillInput, selectedSkills) {
            presetSkills.filter { preset ->
                !selectedSkills.contains(preset) &&
                (skillInput.isBlank() || preset.contains(skillInput.trim(), ignoreCase = true))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialRole == null) "Add Role" else "Edit Role",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column {
                FieldLabel(label = "Role name", isRequired = true)
                StyledTextField(
                    value = roleName,
                    onValueChange = { roleName = it },
                    placeholder = "e.g. Android Developer"
                )
            }

            if (filteredPresets.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredPresets.forEach { preset ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                if (!selectedSkills.contains(preset)) {
                                    selectedSkills = selectedSkills + preset
                                }
                            },
                            label = { Text("+ $preset", style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            if (selectedSkills.isNotEmpty()) {
                Column {
                    Text(
                        text = "Selected skills",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedSkills.forEach { skill ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clickable {
                                    selectedSkills = selectedSkills - skill
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = skill,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove skill",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column {
                FieldLabel(label = "People needed", isRequired = true)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable(enabled = peopleNeeded > 1) {
                                    if (peopleNeeded > 1) peopleNeeded--
                                },
                            color = if (peopleNeeded > 1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "-",
                                    color = if (peopleNeeded > 1) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        Text(
                            text = "$peopleNeeded",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                        )

                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { peopleNeeded++ },
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "+",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val newRole = ProjectRoleUiModel(
                        id = initialRole?.id ?: UUID.randomUUID().toString(),
                        roleName = roleName.trim(),
                        skills = selectedSkills,
                        peopleNeeded = peopleNeeded
                    )
                    onSaveRole(newRole)
                },
                enabled = roleName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = if (initialRole == null) "Add Role" else "Save Role",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun CreatedProjectCard(
    state: CreateProjectUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val logoUrl = state.createdCollab?.logoUrl ?: state.logoUri
                if (!logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = logoUrl,
                        contentDescription = "Project Logo",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = state.title.take(2).uppercase().ifBlank { "PR" },
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 20.sp
                                )
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "COBUILDER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color(0xFF2563EB)
                                ),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }

                        ProjectStatusBadge(
                            status = state.createdCollab?.status ?: ProjectStatus.LOOKING
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Managed by you • Just now",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Text(
                text = state.idea,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            if (state.projectUrl.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = Color(0xFF5C56E6),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.projectUrl,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF5C56E6),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Looking For",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                    )

                    val totalSpots = state.roles.sumOf { it.peopleNeeded }.coerceAtLeast(1)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "$totalSpots ${if (totalSpots == 1) "spot" else "spots"} open",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                state.roles.forEach { role ->
                    val (iconRes, colors) = getRoleIconAndBg(role.roleName)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = colors.first
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = null,
                                        tint = colors.second,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = role.roleName,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFE2E8F0)
                                    ) {
                                        Text(
                                            text = "×${role.peopleNeeded}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF475569),
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                if (role.skills.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = role.skills.joinToString(" • "),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF5C56E6),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCreatedSuccessScreen(
    state: CreateProjectUiState,
    onViewProject: () -> Unit,
    onBackToCollaborations: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF5C56E6).copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "PROJECT PUBLISHED!",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5C56E6),
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Text(
                text = "Your Project is Live!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Your project card is now published in the community feed. Collaborators can discover it and express interest.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        CreatedProjectCard(state = state)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onViewProject,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C56E6))
            ) {
                Text(
                    text = "View in Feed",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                )
            }

            OutlinedButton(
                onClick = onBackToCollaborations,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2563EB))
            ) {
                Text(
                    text = "Back to Collaborations",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectViewModal(
    state: CreateProjectUiState,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Project Preview",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            CreatedProjectCard(state = state)

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C56E6))
            ) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
