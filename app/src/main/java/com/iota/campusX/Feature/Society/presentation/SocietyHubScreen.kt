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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.R
import com.iota.campusX.Navigation.SocietyInfo
import com.iota.campusX.Navigation.CommunityChat
import kotlinx.coroutines.launch

import com.iota.campusX.Feature.Society.domain.model.Community
import com.iota.campusX.Screens.Home.HomeViewModel
import org.koin.compose.koinInject

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iota.campusX.Feature.Society.presentation.SocietyViewModel
import com.iota.campusX.Feature.Society.presentation.SocietyUiState
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.ui.UIComponents.cardShadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocietyHubScreen(
    navHostController: NavHostController,
    societyViewModel: SocietyViewModel = koinInject(),
    navigationViewModel: NavigationViewModel = koinInject()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateSheet by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val lazyListState = rememberLazyListState()
    HideBottomBar(navigationViewModel, lazyListState)

    var communityName by remember { mutableStateOf("") }
    var communityDesc by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    
    val joinedCommunities by societyViewModel.joinedCommunities.collectAsStateWithLifecycle()
    val allCommunities by societyViewModel.allCommunities.collectAsStateWithLifecycle()
    val createState by societyViewModel.createState.collectAsStateWithLifecycle()
    val joinState by societyViewModel.joinState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> logoUri = uri }
    )

    LaunchedEffect(createState) {
        if (createState is SocietyUiState.Success) {
            showCreateSheet = false
            communityName = ""
            communityDesc = ""
            logoUri = null
        }
    }

    LaunchedEffect(joinState) {
        if (joinState is SocietyUiState.Error) {
            snackbarHostState.showSnackbar((joinState as SocietyUiState.Error).message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Societies", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Search Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search for societies...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )
            }

            // Create Community Card (New Design)
            item {
                Card(
                    onClick = { showCreateSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .cardShadow(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(52.dp)
                        ) {
                            // Icon Background (Squircle)
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.people),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = Color.White
                                )
                            }

                            // Green Plus Badge
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(22.dp)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                shape = CircleShape,
                                color = Color(0xFF1EBE71) // Nice green
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.padding(2.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Create Community",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Start your own society and invite friends",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Explore Communities Section
            item {
                Text(
                    text = "Explore Societies",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            val filteredCommunities = allCommunities.filter { 
                it.name.contains(searchQuery, ignoreCase = true) 
            }

            items(filteredCommunities) { community ->
                val isJoined = joinedCommunities.any { it.id == community.id }
                CommunityCard(
                    community = community,
                    isCreator = community.creatorId == societyViewModel.currentUserId,
                    isJoined = isJoined,
                    onClick = { 
                        if (isJoined) {
                            navHostController.navigate(CommunityChat(community.id))
                        } else {
                            navHostController.navigate(SocietyInfo(community.id))
                        }
                    }
                )
            }
            
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
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
                    "Create Community",
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
                    if (logoUri != null) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = "Community Logo",
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
                Text("Tap to add logo", style = MaterialTheme.typography.labelSmall)

                OutlinedTextField(
                    value = communityName,
                    onValueChange = { communityName = it },
                    label = { Text("Community Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = communityDesc,
                    onValueChange = { communityDesc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        societyViewModel.createCommunity(
                            name = communityName,
                            description = communityDesc,
                            category = "General", // Default for now
                            logoUri = logoUri?.toString()
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = CircleShape,
                    enabled = communityName.isNotBlank() && createState !is SocietyUiState.Loading
                ) {
                    if (createState is SocietyUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create", fontWeight = FontWeight.Bold)
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
}

@Composable
fun CommunityCard(
    community: Community,
    isCreator: Boolean,
    isJoined: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().cardShadow(alpha = 0.5f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),

    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (community.logoUrl != null) {
                    AsyncImage(
                        model = community.logoUrl,
                        contentDescription = community.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = community.name.take(1),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = community.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    if (isJoined && community.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ) {
                            Text(
                                text = if (community.unreadCount > 99) "99+" else community.unreadCount.toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${community.memberCount} members", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = community.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            
            if (isCreator) {
                Text(
                    text = "Admin",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else if (isJoined) {
                Text(
                    text = "Joined",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
