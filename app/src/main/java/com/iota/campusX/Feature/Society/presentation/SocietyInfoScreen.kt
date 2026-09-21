package com.iota.campusX.Feature.Society.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.ui.UIComponents.AlertDialogWidget
import org.koin.compose.koinInject
import androidx.compose.ui.platform.LocalContext
import com.iota.campusX.Navigation.CommunityChat
import com.iota.campusX.Navigation.SocietyInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocietyInfoScreen(
    societyId: String,
    openJoinSheet: Boolean = false,
    navHostController: NavHostController,
    viewModel: SocietyViewModel = koinInject()
) {
    val allCommunities by viewModel.allCommunities.collectAsStateWithLifecycle()
    val societyFromList = allCommunities.find { it.id == societyId }
    val societyByListener by viewModel.listenToCommunity(societyId).collectAsState(initial = societyFromList)
    val society = societyByListener ?: societyFromList
    val joinedCommunities by viewModel.joinedCommunities.collectAsStateWithLifecycle()
    val isJoined = joinedCommunities.any { it.id == societyId }
    val isOwner = society?.creatorId == viewModel.currentUserId
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val joinState by viewModel.joinState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showJoinSheet by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val joinSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var editName by remember(society) { mutableStateOf(society?.name ?: "") }
    var editDesc by remember(society) { mutableStateOf(society?.description ?: "") }
    var editLogoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> editLogoUri = uri }
    )

    LaunchedEffect(openJoinSheet, isJoined) {
        if (openJoinSheet && !isJoined) {
            showJoinSheet = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.joinEvent.collect { event ->
            when (event) {
                is SocietyUiEvent.JoinSuccess -> {
                    showJoinSheet = false
                    navHostController.navigate(CommunityChat(id = societyId)) {
                        popUpTo<SocietyInfo> { inclusive = true }
                        launchSingleTop = true
                    }
                }
                is SocietyUiEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(createState) {
        if (createState is SocietyUiState.Success) {
            showEditSheet = false
        }
    }

    if (showDeleteDialog) {
        AlertDialogWidget(
            onDismiss = { showDeleteDialog = false },
            title = "Delete Society",
            description = "Are you sure you want to delete this society? This action cannot be undone.",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                society?.let { viewModel.deleteCommunity(it) }
                showDeleteDialog = false
                navHostController.popBackStack()
            },
            showLoading = false
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Society Info") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (society != null && !isOwner && !isJoined) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.joinCommunity(society) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape,
                        enabled = joinState !is SocietyUiState.Loading
                    ) {
                        if (joinState is SocietyUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Join Society", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (society == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (society.logoUrl != null) {
                        AsyncImage(
                            model = society.logoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = society.name.take(1).uppercase(),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = society.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${society.memberCount} members",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = society.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isOwner) {
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                        Row(
                            modifier = Modifier.padding(16.dp).clickable(onClick = { showEditSheet = true}, indication = null, interactionSource = remember { MutableInteractionSource() }),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Edit Society Details", fontWeight = FontWeight.SemiBold)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth())
                        Row(
                            modifier = Modifier.padding(16.dp).clickable(onClick = { showDeleteDialog = true},indication = null, interactionSource = remember { MutableInteractionSource() }),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(10.dp))
                            Text("Delete Society", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showEditSheet && society != null) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "Edit Society",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Logo Picker
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val displayImage = editLogoUri ?: society.logoUrl
                    if (displayImage != null) {
                        AsyncImage(
                            model = displayImage,
                            contentDescription = "Society Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Logo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text("Tap to change logo", style = MaterialTheme.typography.labelSmall)

                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Society Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = editDesc,
                    onValueChange = { editDesc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        viewModel.updateCommunity(
                            community = society,
                            newName = editName,
                            newDescription = editDesc,
                            newLogoUri = editLogoUri?.toString()
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = CircleShape,
                    enabled = editName.isNotBlank() && createState !is SocietyUiState.Loading
                ) {
                    if (createState is SocietyUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
                
                if (createState is SocietyUiState.Error) {
                    Text(
                        text = (createState as SocietyUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    if (showJoinSheet && society != null) {
        ModalBottomSheet(
            onDismissRequest = { showJoinSheet = false },
            sheetState = joinSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "Join Society",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (society.logoUrl != null) {
                        AsyncImage(
                            model = society.logoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = society.name.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = society.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${society.memberCount} members • ${society.category}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = society.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 4
                )

                Button(
                    onClick = {
                        viewModel.joinCommunity(society)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = CircleShape,
                    enabled = joinState !is SocietyUiState.Loading
                ) {
                    if (joinState is SocietyUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Confirm & Join", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
