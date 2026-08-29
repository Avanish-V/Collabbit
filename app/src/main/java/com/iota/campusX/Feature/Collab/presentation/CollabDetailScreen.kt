package com.iota.campusX.Feature.Collab.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Collab.data.model.CollabRequestStatus
import com.iota.campusX.Feature.Collab.data.model.CollabType
import com.iota.campusX.Navigation.Profile
import com.iota.campusX.Navigation.SendMessage
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.UIComponents.FeedUI.Avatar
import com.iota.campusX.ui.theme.collabCobuilder
import com.iota.campusX.ui.theme.collabHackathon
import com.iota.campusX.ui.theme.collabIndigo
import com.iota.campusX.ui.theme.collabStartup

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

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
    val collabRequests by viewModel.collabRequestsState.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteCollabState.collectAsStateWithLifecycle()

    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(collabId) {
        viewModel.fetchCollabById(collabId)
        viewModel.hasRequested(collabId)
        viewModel.fetchCollabRequests(collabId)
    }

    LaunchedEffect(connectState) {
        if (connectState is UiState.Success) {
            snackbarHostState.showSnackbar("Application sent successfully!")
            viewModel.resetConnectState()
        } else if (connectState is UiState.Error) {
            snackbarHostState.showSnackbar("Failed to send application: ${(connectState as UiState.Error).message}")
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
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Collaboration") },
            text = { Text("Are you sure you want to delete this collaboration? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCollab(collabId)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { collab?.let { Text(text = it.title) } },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (collab != null && !collab.isCurrentUser) {
                val status = (hasRequested as? UiState.Success<CollabRequestStatus>)?.data ?: CollabRequestStatus.NOT_REQUESTED
                Surface(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp),
                    color = Color.Transparent
                ) {
                    Button(
                        onClick = {
                            if (status == CollabRequestStatus.NOT_REQUESTED) {
                                viewModel.connectToCollab(collabId)
                            } else if (status == CollabRequestStatus.ACCEPTED) {
                                navController.navigate(
                                    SendMessage(
                                        userId = collab.authorId,
                                        userName = collab.authorName,
                                        userImage = collab.authorImage ?: ""
                                    )
                                )
                            }
                        },
                        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(48.dp),
                        shape = CircleShape,
                        enabled = hasRequested is UiState.Success && (status == CollabRequestStatus.NOT_REQUESTED || status == CollabRequestStatus.ACCEPTED)
                    ) {
                        when(hasRequested){
                            is UiState.Loading->{
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            is UiState.Success<*> -> {
                                Text(
                                    text = when (status) {
                                        CollabRequestStatus.NOT_REQUESTED -> "Express Interest"
                                        CollabRequestStatus.PENDING -> "Request Pending"
                                        CollabRequestStatus.ACCEPTED -> "Message Team"
                                        CollabRequestStatus.DECLINED -> "Position Filled"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }else -> {}
                        }

                    }
                }
            }
        }
    ) { innerPadding ->
        when (state) {
            is UiState.Loading -> com.iota.campusX.Feature.Collab.presentation.components.CollabDetailShimmer()
            is UiState.Error -> ErrorScreen(
                text = (state as UiState.Error).message,
                onReTry = { viewModel.fetchCollabById(collabId) }
            )
            is UiState.Success -> {
                if (collab != null) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 24.dp)
                    ) {

                        item {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {

                                Spacer(modifier = Modifier.height(12.dp))

                                CreatorCard(collab, navController)

                                SectionHeader("Overview")

                                Text(
                                    text = collab.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    MetricCard(
                                        modifier = Modifier.weight(1f),
                                        icon = R.drawable.calendar,
                                        label = "Deadline",
                                        value = if (collab.deadline > 0L) {
                                            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(collab.deadline))
                                        } else "Open",

                                    )
                                    MetricCard(
                                        modifier = Modifier.weight(1f),
                                        icon = R.drawable.people_bold,
                                        label = "Open Slots",
                                        value = "${collab.participantsNeeded} spots",

                                    )
                                }

                                if (collab.requirements.isNotEmpty()) {
                                    SectionHeader("Looking for")
                                    RequirementTags(collab.requirements)
                                }

                                if (!collab.projectUrl.isNullOrBlank()) {
                                    ProjectLinkCard(collab.projectUrl, uriHandler)
                                }
                            }
                        }

                        if (collab.isCurrentUser) {
                            item {
                                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                    SectionHeader("Applicant Dashboard", count = (collabRequests as? UiState.Success)?.data?.size)
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            when (collabRequests) {
                                is UiState.Success -> {
                                    val data = (collabRequests as UiState.Success<List<RequestUiState>>).data
                                    if (data.isEmpty()) {
                                        item { EmptyApplicationsState() }
                                    } else {
                                        items(data) { requestState ->
                                            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                                ModernRequestItem(
                                                    requestState = requestState,
                                                    onAccept = { viewModel.updateCollabRequestStatus(requestState.request.id, CollabRequestStatus.ACCEPTED, collabId) },
                                                    onReject = { viewModel.updateCollabRequestStatus(requestState.request.id, CollabRequestStatus.DECLINED, collabId) },
                                                    onChat = { navController.navigate(SendMessage(requestState.request.senderId, requestState.request.senderName, requestState.request.senderImage ?: "")) },
                                                    onProfileClick = { navController.navigate(Profile(userId = requestState.request.senderId)) }
                                                )
                                            }
                                        }
                                    }
                                }
                                is UiState.Loading -> {
                                    items(3) {
                                        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                            com.iota.campusX.Feature.Collab.presentation.components.CollabRequestShimmer()
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun CreatorCard(collab: com.iota.campusX.Feature.Collab.data.model.CollabResponse, navController: NavHostController) {
    Surface(
        onClick = { navController.navigate(Profile(userId = collab.authorId)) },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                imageUrl = collab.authorImage,
                onAvatarClick = { navController.navigate(Profile(userId = collab.authorId)) }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = collab.authorName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Project Lead • ${collab.timeAgo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    icon: Int,
    label: String,
    value: String,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant

            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RequirementTags(requirements: List<String>,) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        requirements.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun ProjectLinkCard(url: String, uriHandler: androidx.compose.ui.platform.UriHandler) {
    Surface(
        onClick = {
            var finalUrl = url
            if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                finalUrl = "https://" + finalUrl
            }
            try { uriHandler.openUri(finalUrl) } catch (e: Exception) {}
        },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.user_link_reguler),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "External Resource",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                painter = painterResource(R.drawable.globe),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ModernRequestItem(
    requestState: RequestUiState,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onChat: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Avatar(
                    imageUrl = requestState.request.senderImage ?: "",
                    onAvatarClick = onProfileClick
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = requestState.request.senderName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    requestState.request.tagline?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (requestState.request.status == CollabRequestStatus.ACCEPTED.name) {
                    Surface(
                        color = collabCobuilder.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Accepted",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = collabCobuilder
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (requestState.request.status == CollabRequestStatus.ACCEPTED.name) {
                    Button(
                        onClick = onChat,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(painter = painterResource(R.drawable.messages), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Discussion")
                    }
                } else {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                        enabled = !requestState.isRejectLoading && !requestState.isAcceptLoading
                    ) {
                        if (requestState.isRejectLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Decline")
                        }
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !requestState.isRejectLoading && !requestState.isAcceptLoading
                    ) {
                        if (requestState.isAcceptLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text("Accept Team")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = collabIndigo)
    }
}

@Composable
fun EmptyApplicationsState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No applications yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "When people express interest, they will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
