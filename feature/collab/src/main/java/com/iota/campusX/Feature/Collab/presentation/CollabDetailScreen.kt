package com.iota.campusX.Feature.Collab.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Collab.data.model.CollabMemberResponse
import com.iota.campusX.Feature.Collab.data.model.ProjectStatus
import com.iota.campusX.Feature.Collab.data.model.CollabRequestStatus
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.Collab.data.model.ProjectRole
import com.iota.campusX.Feature.Collab.presentation.components.CollabDetailShimmer
import com.iota.campusX.Feature.Collab.presentation.components.DetailCardItem
import com.iota.campusX.Feature.Collab.presentation.components.FeedbackBottomInputBar
import com.iota.campusX.Feature.Collab.presentation.components.FeedbackTabContent
import com.iota.campusX.Feature.Collab.presentation.components.ProjectOverviewTabs
import com.iota.campusX.Feature.Collab.presentation.components.ProjectSummaryCard
import com.iota.campusX.Feature.Collab.presentation.components.RoleItemCard
import com.iota.campusX.Feature.Collab.presentation.components.TeamMemberItemCard
import com.iota.campusX.Navigation.CollabRequests
import com.iota.campusX.Navigation.ManageCollab
import com.iota.campusX.Navigation.Profile
import com.iota.campusX.Navigation.SendMessage
import com.iota.campusX.core.common.UiState
import com.iota.campusX.core.designsystem.AlertDialogWidget
import com.iota.campusX.feature.collab.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollabDetailScreen(
    collabId: String,
    navController: NavHostController,
    viewModel: CollabViewModel
) {
    val state by viewModel.selectedCollabState.collectAsStateWithLifecycle()
    val connectState by viewModel.connectState.collectAsStateWithLifecycle()
    val hasRequested by viewModel.hasRequested.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteCollabState.collectAsStateWithLifecycle()
    val membersState by viewModel.collabMembersState.collectAsStateWithLifecycle()
    val feedbackState by viewModel.feedbackState.collectAsStateWithLifecycle()
    val addFeedbackState by viewModel.addFeedbackState.collectAsStateWithLifecycle()

    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(collabId) {
        viewModel.fetchCollabById(collabId)
        viewModel.hasRequested(collabId)
        viewModel.fetchCollabRequests(collabId)
        viewModel.fetchCollabMembers(collabId)
        viewModel.fetchFeedback(collabId)
    }

    LaunchedEffect(connectState) {
        if (connectState is UiState.Success) {
            snackbarHostState.showSnackbar("Application sent successfully!")
            viewModel.resetConnectState()
        } else if (connectState is UiState.Error) {
            val errorMsg = (connectState as UiState.Error).message
            val formattedMsg = if (
                errorMsg.contains("profile", ignoreCase = true) ||
                errorMsg.contains("incomplete", ignoreCase = true) ||
                errorMsg.contains("80", ignoreCase = true) ||
                errorMsg.contains("complete", ignoreCase = true)
            ) {
                "First complete your profile (at least 80%) to send collaboration requests."
            } else {
                "Failed to send application: $errorMsg"
            }
            snackbarHostState.showSnackbar(formattedMsg)
            viewModel.resetConnectState()
        }
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
            title = "Delete Collaboration",
            description = "Are you sure you want to delete this project? This action cannot be undone.",
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                viewModel.deleteCollab(collabId)
            },
            showLoading = deleteState is UiState.Loading
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (collab != null) {
                when (selectedTabIndex) {
                    0 -> {
                        // Main Action Button ON Overview Tab ONLY
                        Surface(
                            modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                            shadowElevation = 8.dp,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                if (collab.isCurrentUser) {
                                    Button(
                                        onClick = { navController.navigate(ManageCollab(collabId)) },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text(
                                            text = "Manage Project",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                } else {
                                    val status = (hasRequested as? UiState.Success)?.data
                                    when (status) {
                                        CollabRequestStatus.PENDING -> {
                                            Button(
                                                onClick = { },
                                                enabled = false,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp),
                                                shape = CircleShape,
                                            ) {
                                                Text("Application Pending")
                                            }
                                        }
                                        CollabRequestStatus.ACCEPTED -> {
                                            Button(
                                                onClick = {
                                                    navController.navigate(
                                                        SendMessage(
                                                            userId = collab.authorId,
                                                            userName = collab.authorName,
                                                            userImage = collab.authorImage
                                                        )
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp),
                                                shape = CircleShape,
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Text("Accepted - Message Lead", style = MaterialTheme.typography.titleMedium)
                                            }
                                        }
                                        else -> {
                                            val isLooking = collab.status == ProjectStatus.LOOKING
                                            Button(
                                                onClick = {
                                                    if (isLooking) {
                                                        viewModel.connectToCollab(collabId)
                                                    }
                                                },
                                                enabled = isLooking,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp),
                                                shape = CircleShape,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            ) {
                                                Text(
                                                    text = if (isLooking) "Connect" else "Applications Closed (${collab.status.displayName})",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = if (isLooking) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        val replyingTarget by viewModel.replyingTarget.collectAsStateWithLifecycle()

                        FeedbackBottomInputBar(
                            collabId = collab.id,
                            replyingTarget = replyingTarget,
                            addFeedbackState = addFeedbackState,
                            onCancelReply = { viewModel.setReplyingTarget(null) },
                            onSendFeedback = { content ->
                                if (replyingTarget != null) {
                                    viewModel.replyToFeedback(collab.id, replyingTarget!!.feedbackId, content)
                                } else {
                                    viewModel.addFeedback(collab.id, content)
                                }
                            }
                        )
                    }

                    else -> {} // No bottom bar for Team tab (1)
                }
            }
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
                // Top Summary Header Card
                item {
                    val currentMemberCount = calculateMemberCount(membersState, collab.authorId)
                    ProjectSummaryCard(
                        collab = collab,
                        currentMembers = currentMemberCount,
                        onViewProjectClick = {
                            if (!collab.projectUrl.isNullOrBlank()) {
                                uriHandler.openUri(collab.projectUrl)
                            }
                        }
                    )
                }

                item {
                    val currentMemberCount = calculateMemberCount(membersState, collab.authorId)
                    val feedbackCount = (feedbackState as? UiState.Success)?.data?.size ?: 0
                    ProjectOverviewTabs(
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it },
                        teamCount = if (currentMemberCount > 0) currentMemberCount else null,
                        feedbackCount = if (feedbackCount > 0) feedbackCount else null
                    )
                }

                when (selectedTabIndex) {
                    0 -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Project Details",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                )

                                if (collab.isCurrentUser) {
                                    IconButton(onClick = { navController.navigate(ManageCollab(collabId)) }) {
                                        Icon(
                                            painter = painterResource(R.drawable.pencil),
                                            contentDescription = "Edit Details",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            DetailCardItem(
                                iconRes = R.drawable.lightbulb_on,
                                iconTint = MaterialTheme.colorScheme.primary,
                                title = "Project Idea",
                                content = collab.description
                            )
                        }

                        collab.projectUrl?.let {
                            item {
                                DetailCardItem(
                                    iconRes = R.drawable.globe,
                                    iconTint = MaterialTheme.colorScheme.primary,
                                    title = "Project URL",
                                    content = it,
                                    isClickableUrl = true,
                                    onUrlClick = { uriHandler.openUri(it) }
                                )
                            }
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Looking for",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    )
                                }

                                if (collab.roles.isNotEmpty()) {
                                    collab.roles.forEachIndexed { index, role ->
                                        RoleItemCard(
                                            index = index,
                                            role = role,
                                            onClick = {
                                                if (collab.isCurrentUser) {
                                                    navController.navigate(CollabRequests(collabId))
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    val fallbackRoles = if (collab.requirements.isNotEmpty()) {
                                        collab.requirements
                                    } else {
                                        listOf("Team Member")
                                    }
                                    fallbackRoles.forEachIndexed { index, reqName ->
                                        RoleItemCard(
                                            index = index,
                                            role = ProjectRole(
                                                id = index.toString(),
                                                roleName = reqName,
                                                description = ""
                                            ),
                                            onClick = {
                                                if (collab.isCurrentUser) {
                                                    navController.navigate(CollabRequests(collabId))
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Project Lead & Members",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    )

                                    if (collab.isCurrentUser) {
                                        TextButton(onClick = { navController.navigate(CollabRequests(collabId)) }) {
                                            Text(
                                                text = "Manage Requests",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 12.5.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                val currentStatus = (hasRequested as? UiState.Success)?.data
                                val hasJoined = collab.isCurrentUser || currentStatus == CollabRequestStatus.ACCEPTED

                                when (val mState = membersState) {
                                    is UiState.Loading -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    is UiState.Success -> {
                                        val members = mState.data
                                        if (members.isNotEmpty()) {
                                            members.forEach { member ->
                                                TeamMemberItemCard(
                                                    member = member,
                                                    onMemberClick = {
                                                        navController.navigate(Profile(userId = member.userId))
                                                    },
                                                    onMessageClick = if (hasJoined) {
                                                        {
                                                            navController.navigate(
                                                                SendMessage(
                                                                    userId = member.userId,
                                                                    userName = member.name,
                                                                    userImage = member.image
                                                                )
                                                            )
                                                        }
                                                    } else null
                                                )
                                            }
                                        } else {
                                            TeamMemberItemCard(
                                                member = CollabMemberResponse(
                                                    userId = collab.authorId,
                                                    name = collab.authorName,
                                                    image = collab.authorImage,
                                                    tagline = "Project Lead & Creator",
                                                    role = "Project Lead",
                                                    isLead = true,
                                                    isCurrentUser = collab.isCurrentUser
                                                ),
                                                onMemberClick = {
                                                    navController.navigate(Profile(userId = collab.authorId))
                                                },
                                                onMessageClick = if (hasJoined) {
                                                    {
                                                        navController.navigate(
                                                            SendMessage(
                                                                userId = collab.authorId,
                                                                userName = collab.authorName,
                                                                userImage = collab.authorImage
                                                            )
                                                        )
                                                    }
                                                } else null
                                            )
                                        }
                                    }
                                    else -> {
                                        TeamMemberItemCard(
                                            member = CollabMemberResponse(
                                                userId = collab.authorId,
                                                name = collab.authorName,
                                                image = collab.authorImage,
                                                tagline = "Project Lead & Creator",
                                                role = "Project Lead",
                                                isLead = true,
                                                isCurrentUser = collab.isCurrentUser
                                            ),
                                            onMemberClick = {
                                                navController.navigate(Profile(userId = collab.authorId))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        item {
                            FeedbackTabContent(
                                collab = collab,
                                viewModel = viewModel
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

private fun calculateMemberCount(
    membersState: UiState<List<CollabMemberResponse>>,
    leadUserId: String
): Int {
    return when (membersState) {
        is UiState.Success -> {
            val members = membersState.data
            if (members.isNotEmpty()) {
                members.size
            } else {
                1
            }
        }
        else -> 1
    }
}
