package com.iota.campusX.Feature.Collab.presentation

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import com.iota.campusX.ui.UIComponents.AlertDialogWidget
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import androidx.compose.material3.CircularProgressIndicator
import com.iota.campusX.Feature.Collab.data.model.CollabMemberResponse
import com.iota.campusX.Feature.Collab.data.model.CollabRequestStatus
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.Collab.data.model.ProjectStatus
import com.iota.campusX.Feature.Collab.presentation.components.ChangeProjectStatusBottomSheet
import com.iota.campusX.Feature.Collab.presentation.components.CollabDetailShimmer
import com.iota.campusX.Feature.Collab.presentation.components.CollabRequestShimmer
import com.iota.campusX.Feature.Collab.presentation.components.ProjectStatusBadge
import com.iota.campusX.Feature.Collab.presentation.components.getProjectStatusUi
import com.iota.campusX.Navigation.CollabDetail
import com.iota.campusX.Navigation.CollabRequests
import com.iota.campusX.Navigation.EditCollab
import com.iota.campusX.Navigation.Profile
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCollabScreen(
    collabId: String,
    navController: NavHostController,
    viewModel: CollabViewModel
) {
    val state by viewModel.selectedCollabState.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteCollabState.collectAsStateWithLifecycle()
    val requestsState by viewModel.collabRequestsState.collectAsStateWithLifecycle()
    val membersState by viewModel.collabMembersState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChangeStatusSheet by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val updateStatusState by viewModel.updateProjectStatusState.collectAsStateWithLifecycle()

    LaunchedEffect(updateStatusState) {
        if (updateStatusState is UiState.Success) {
            val updated = (updateStatusState as UiState.Success<CollabResponse>).data
            snackbarHostState.showSnackbar("Project status updated to ${updated.status.displayName}")
            viewModel.resetUpdateProjectStatusState()
        } else if (updateStatusState is UiState.Error) {
            snackbarHostState.showSnackbar((updateStatusState as UiState.Error).message)
            viewModel.resetUpdateProjectStatusState()
        }
    }

    LaunchedEffect(collabId) {
        viewModel.fetchCollabById(collabId)
        viewModel.fetchCollabRequests(collabId)
        viewModel.fetchCollabMembers(collabId)
    }

    LaunchedEffect(deleteState) {
        if (deleteState is UiState.Success) {
            navController.popBackStack()
            viewModel.resetDeleteState()
        } else if (deleteState is UiState.Error) {
            snackbarHostState.showSnackbar((deleteState as UiState.Error).message)
            viewModel.resetDeleteState()
        }
    }

    val collab = (state as? UiState.Success)?.data

    if (showDeleteDialog) {
        AlertDialogWidget(
            onDismiss = { showDeleteDialog = false },
            title = "Delete Project",
            description = "Are you sure you want to delete this project? This action cannot be undone and all associated requests will be removed.",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                viewModel.deleteCollab(collabId)
            },
            showLoading = deleteState is UiState.Loading
        )
    }

    if (showChangeStatusSheet && collab != null) {
        ChangeProjectStatusBottomSheet(
            currentStatus = collab.status,
            onDismiss = { showChangeStatusSheet = false },
            onStatusSelected = { newStatus ->
                viewModel.updateProjectStatus(collabId, newStatus)
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manage Project",
                        style = MaterialTheme.typography.titleMedium.copy(
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
//                    IconButton(onClick = { /* Open project settings */ }) {
//                        Icon(
//                            imageVector = Icons.Default.Settings,
//                            contentDescription = "Settings",
//                            tint = MaterialTheme.colorScheme.onSurface
//                        )
//                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (state is UiState.Loading || collab == null) {
            Box(modifier = Modifier.padding(innerPadding)) {
                CollabDetailShimmer()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Project Summary Card
                item {
                    ManageProjectHeaderCard(collab = collab)
                }

                // Segmented Tab Bar (Details / Team / Settings)
                item {
                    ManageSegmentedTabs(
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it }
                    )
                }

                // Dynamic Tab Content
                when (selectedTabIndex) {
                    0 -> {
                        // --- DETAILS TAB ---
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Project Status",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    )
                                    TextButton(onClick = { showChangeStatusSheet = true }) {
                                        Text(
                                            text = "Change Status",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                val statusConfig = remember(collab.status) { getProjectStatusUi(collab.status) }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showChangeStatusSheet = true },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(12.dp),
                                            shape = CircleShape,
                                            color = statusConfig.dotColor
                                        ) {}

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = statusConfig.displayName,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = statusConfig.textColor
                                                    )
                                                )
                                                ProjectStatusBadge(status = collab.status)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = statusConfig.description,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 12.5.sp
                                                )
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Edit Status",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Quick Actions",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        QuickActionRow(
                                            iconRes = R.drawable.pencil,
                                            iconTint = Color(0xFF7C3AED),
                                            iconBg = MaterialTheme.colorScheme.surfaceVariant,
                                            title = "Edit Project Details",
                                            subtitle = "Update title, idea, link or roles",
                                            onClick = {
                                                navController.navigate(EditCollab(collabId))
                                            }
                                        )

                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                                        QuickActionRow(
                                            iconRes = R.drawable.people_bold,
                                            iconTint = Color(0xFF4F46E5),
                                            iconBg = MaterialTheme.colorScheme.surfaceVariant,
                                            title = "Manage Roles & Requests",
                                            subtitle = "Review join requests and manage team",
                                            onClick = {
                                                navController.navigate(CollabRequests(collabId))
                                            }
                                        )

                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                                        QuickActionRow(
                                            iconRes = R.drawable.share,
                                            iconTint = Color(0xFF2563EB),
                                            iconBg = MaterialTheme.colorScheme.surfaceVariant,
                                            title = "Share Project",
                                            subtitle = "Get more people to join",
                                            onClick = {
                                                val shareIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        "Check out my project '${collab.title}' on Collabbit! https://collabbit.in/collab/${collab.id}"
                                                    )
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Project"))
                                            }
                                        )

                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                                        QuickActionRow(
                                            iconRes = R.drawable.eye,
                                            iconTint = Color(0xFF0284C7),
                                            iconBg = MaterialTheme.colorScheme.surfaceVariant,
                                            title = "View Project",
                                            subtitle = "See how it appears to others",
                                            onClick = {
                                                navController.navigate(CollabDetail(collabId))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // --- TEAM TAB (Manage Requests + Team Members) ---
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Pending Join Requests",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        )

                                        TextButton(onClick = { navController.navigate(CollabRequests(collabId)) }) {
                                            Text(
                                                text = "View Full Screen",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 12.5.sp
                                                )
                                            )
                                        }
                                    }

                                    when (val rState = requestsState) {
                                        is UiState.Loading -> {
                                            CollabRequestShimmer()
                                        }
                                        is UiState.Success -> {
                                            val pendingRequests = rState.data.filter { it.request.status == "PENDING" }
                                            if (pendingRequests.isEmpty()) {
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(20.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "No pending join requests",
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        )
                                                    }
                                                }
                                            } else {
                                                pendingRequests.forEach { reqUiState ->
                                                    RequestCardItem(
                                                        reqUiState = reqUiState,
                                                        onUserClick = {
                                                            navController.navigate(Profile(userId = reqUiState.request.senderId))
                                                        },
                                                        onAccept = {
                                                            viewModel.updateCollabRequestStatus(
                                                                requestId = reqUiState.request.id,
                                                                status = CollabRequestStatus.ACCEPTED,
                                                                collabId = collabId
                                                            )
                                                        },
                                                        onDecline = {
                                                            viewModel.updateCollabRequestStatus(
                                                                requestId = reqUiState.request.id,
                                                                status = CollabRequestStatus.DECLINED,
                                                                collabId = collabId
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Current Team Members",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    )

                                    when (val mState = membersState) {
                                        is UiState.Loading -> {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        is UiState.Success -> {
                                            val members = mState.data
                                            if (members.isEmpty()) {
                                                MemberManageCardItem(
                                                    member = CollabMemberResponse(
                                                        userId = collab.authorId,
                                                        name = collab.authorName,
                                                        image = collab.authorImage,
                                                        tagline = "Project Lead & Creator",
                                                        role = "Project Lead",
                                                        isLead = true,
                                                        isCurrentUser = collab.isCurrentUser
                                                    ),
                                                    isOwner = false,
                                                    onUserClick = {
                                                        navController.navigate(Profile(userId = collab.authorId))
                                                    },
                                                    onRemove = {}
                                                )
                                            } else {
                                                members.forEach { member ->
                                                    MemberManageCardItem(
                                                        member = member,
                                                        isOwner = collab.isCurrentUser,
                                                        onUserClick = {
                                                            navController.navigate(Profile(userId = member.userId))
                                                        },
                                                        onRemove = {
                                                            viewModel.removeCollabMember(collabId, member.userId)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // --- SETTINGS TAB ---
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Project Settings",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        QuickActionRow(
                                            iconRes = R.drawable.trash,
                                            iconTint = Color(0xFFDC2626),
                                            iconBg = Color(0xFFFEF2F2),
                                            title = "Delete Project",
                                            subtitle = "Permanently remove this project and all associated data",
                                            onClick = { showDeleteDialog = true }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ManageProjectHeaderCard(collab: CollabResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Project Icon Logo
            if (!collab.logoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = collab.logoUrl,
                    contentDescription = "Logo",
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
                            text = collab.title.take(2).uppercase().ifBlank { "AI" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 18.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = collab.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    ProjectStatusBadge(status = collab.status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = collab.collabType.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = collab.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ManageSegmentedTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Details", "Team", "Settings")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(index) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionRow(
    iconRes: Int,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            color = iconBg
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
