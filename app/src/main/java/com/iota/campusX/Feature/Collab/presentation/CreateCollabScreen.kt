package com.iota.campusX.Feature.Collab.presentation

import androidx.annotation.ColorRes
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Collab.data.model.CollabType
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.cardShadow
import com.iota.campusX.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateCollabScreen(
    navController: NavHostController,
    viewModel: CollabViewModel,
    createCollabViewModel: CreateCollabViewModel
) {
    var currentStep by rememberSaveable { mutableStateOf(1) }
    val collabDraft by createCollabViewModel.collabDraftState.collectAsStateWithLifecycle()
    val createStatus by viewModel.createCollabState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(createStatus) {
        if (createStatus is UiState.Success) {
            createCollabViewModel.resetDraft()
            navController.popBackStack()
            viewModel.resetCreateState()
        } else if (createStatus is UiState.Error) {
            snackbarHostState.showSnackbar((createStatus as UiState.Error).message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    val title = when(currentStep) {
                        1 -> "Choose Type"
                        2 -> when(collabDraft.collabType) {
                            CollabType.HACKATHON -> "Hackathon"
                            CollabType.COBUILDER -> "Co-Build"
                            CollabType.STUDY -> "Study"
                            CollabType.RESEARCH->"Research"
                            else -> "Project Details"
                        }
                        else -> "Preview"
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) currentStep-- else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Progress Header
            StepIndicator(currentStep = currentStep)

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "StepTransition"
            ) { step ->
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                    when (step) {
                        1 -> TypePickerStep(
                            selectedType = collabDraft.collabType,
                            onTypeSelected = {
                                createCollabViewModel.setCollabType(it)
                                currentStep = 2
                            }
                        )
                        2 -> FillDetailsStep(
                            type = collabDraft.collabType,
                            title = collabDraft.title,
                            onTitleChange = { createCollabViewModel.setCollabTitle(it) },
                            description = collabDraft.description,
                            onDescriptionChange = { createCollabViewModel.setCollabDescription(it) },
                            roles = collabDraft.requirements.joinToString(", "),
                            onRolesChange = { createCollabViewModel.setCollabRequirements(it.split(",").filter { it.isNotBlank() }.map { it.trim() }) },
                            spots = collabDraft.participantsNeeded,
                            onSpotsChange = { createCollabViewModel.setCollabParticipantsNeeded(it) },
                            deadline = collabDraft.deadline,
                            onDeadlineChange = { createCollabViewModel.setDeadline(it) },
                            projectUrl = collabDraft.projectUrl ?: "",
                            onProjectUrlChange = { createCollabViewModel.setCollabUrl(it) },
                            onNext = { 
                                if (collabDraft.title.isBlank() || collabDraft.description.isBlank()) {
                                    // Could show snackbar here
                                } else {
                                    currentStep = 3 
                                }
                            }
                        )
                        3 -> PreviewStep(
                            title = collabDraft.title,
                            description = collabDraft.description,
                            type = collabDraft.collabType,
                            requirements = collabDraft.requirements,
                            spots = collabDraft.participantsNeeded,
                            deadline = collabDraft.deadline,
                            projectUrl = collabDraft.projectUrl,
                            isLoading = createStatus is UiState.Loading,
                            onPost = {
                                viewModel.createCollab(
                                    title = collabDraft.title,
                                    description = collabDraft.description,
                                    type = collabDraft.collabType,
                                    requirements = collabDraft.requirements,
                                    needed = collabDraft.participantsNeeded,
                                    deadline = collabDraft.deadline,
                                    projectUrl = collabDraft.projectUrl?.ifBlank { null }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val step = index + 1
            val isActive = currentStep >= step
            val isCurrent = currentStep == step

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentStep > step) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = Color.White)
                    } else {
                        Text(
                            text = step.toString(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            if (index < 2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 8.dp)
                        .background(if (currentStep > step) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}

@Composable
fun TypePickerStep(
    selectedType: CollabType,
    onTypeSelected: (CollabType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "What are you looking for?",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        TypeOptionCard(
            title = "Hackathon Squad",
            description = "Form a dream team for an upcoming competition.",
            icon = R.drawable.lightbulb_on,
            isSelected = selectedType == CollabType.HACKATHON,
            onClick = { onTypeSelected(CollabType.HACKATHON) }
        )

        TypeOptionCard(
            title = "Collaborative Build",
            description = "Build a next big thing.",
            icon = R.drawable.hands_together,
            isSelected = selectedType == CollabType.COBUILDER,
            onClick = { onTypeSelected(CollabType.COBUILDER) }
        )

        TypeOptionCard(
            title = "Study Group",
            description = "Find a study buddy.",
            icon = R.drawable.discussion_group,
            isSelected = selectedType == CollabType.STUDY,
            onClick = { onTypeSelected(CollabType.STUDY) }
        )
        TypeOptionCard(
            title = "Research",
            description = "Find research partners.",
            icon = R.drawable.microscope_bacteria,
            isSelected = selectedType == CollabType.STUDY,
            onClick = { onTypeSelected(CollabType.STUDY) }
        )
    }
}

@Composable
fun TypeOptionCard(
    title: String,
    description: String,
    icon: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().cardShadow(alpha = 0.3f),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check, 
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillDetailsStep(
    type: CollabType,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    roles: String,
    onRolesChange: (String) -> Unit,
    spots: Int,
    onSpotsChange: (Int) -> Unit,
    deadline: Long?,
    onDeadlineChange: (Long?) -> Unit,
    projectUrl: String,
    onProjectUrlChange: (String) -> Unit,
    onNext: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "The Essentials",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Help others understand your vision.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                placeholder = { Text(if (type == CollabType.HACKATHON) "e.g. AI for Healthcare Squad" else "e.g. Finder Mobile App") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Pitch") },
                placeholder = { Text("What are you building? Why should someone join you?") },
                minLines = 4,
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            OutlinedTextField(
                value = roles,
                onValueChange = onRolesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Roles looking for") },
                placeholder = { Text("UI/UX, Frontend, Backend...") },
                supportingText = { Text("Separate roles with commas") },
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(painterResource(R.drawable.people_bold), null, Modifier.size(20.dp)) }
            )
        }

        item {
            OutlinedTextField(
                value = projectUrl,
                onValueChange = onProjectUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Project URL (Optional)") },
                placeholder = { Text("https://github.com/your-repo") },
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(painterResource(R.drawable.user_link_reguler), null, Modifier.size(20.dp)) },
                singleLine = true
            )
        }

        if (type != CollabType.COBUILDER) {
            item {
                SpotsStepper(spots = spots, onSpotsChanged = onSpotsChange)
            }
        }

        item {
            DeadlinePicker(selectedDate = deadline, onDateSelected = onDeadlineChange)
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Continue to Preview", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeadlinePicker(
    selectedDate: Long?,
    onDateSelected: (Long?) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
    )

    val formattedDate = if (selectedDate != null && selectedDate > 0L) {
        val date = java.util.Date(selectedDate)
        val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        format.format(date)
    } else {
        "Rolling Basis (No Fixed Deadline)"
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showDialog = false
                }) {
                    Text("Select", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    onDateSelected(null)
                    showDialog = false 
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    }

    Surface(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.calendar),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "Submission Deadline", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formattedDate, 
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpotsStepper(
    spots: Int,
    onSpotsChanged: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Participants Needed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$spots ${if (spots == 1) "Spot" else "Spots"}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledIconButton(
                        onClick = { if (spots > 1) onSpotsChanged(spots - 1) },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }

                    FilledIconButton(
                        onClick = { if (spots < 20) onSpotsChanged(spots + 1) },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            // Visual Grid of Spots
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 10
            ) {
                repeat(10) { index ->
                    val isFilled = index < spots
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            )
                            .clickable { onSpotsChanged(index + 1) },
                        contentAlignment = Alignment.Center
                    ){
                            Icon(
                                painter = painterResource(R.drawable.user),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (isFilled) Color.White else MaterialTheme.colorScheme.outlineVariant
                            )
                    }
                }
            }
            
            Text(
                text = "Tap a dot to quick-select or use buttons for precision.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun PreviewStep(
    title: String,
    description: String,
    type: CollabType,
    requirements: List<String>,
    spots: Int,
    deadline: Long?,
    projectUrl: String?,
    isLoading: Boolean,
    onPost: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Almost there!",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Take a final look before we share it with the community.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Mock Collab Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = collabIndigo.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = type.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = collabIndigo,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    
                    Icon(
                        Icons.Default.Info, 
                        null, 
                        Modifier.size(20.dp), 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = description, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                if (requirements.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        requirements.take(3).forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.people_bold), null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$spots spots", 
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (deadline != null) {
                        val date = java.util.Date(deadline)
                        val format = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                        Text(
                            text = "Due ${format.format(date)}", 
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onPost,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Launch Collaboration", style = MaterialTheme.typography.titleSmall)
                }
            }
            
            Text(
                text = "By launching, you agree to our community guidelines.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
