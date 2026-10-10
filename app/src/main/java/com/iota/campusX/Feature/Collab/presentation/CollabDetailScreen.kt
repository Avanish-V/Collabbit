package com.iota.campusX.Feature.Collab.presentation

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.iota.campusX.Feature.Collab.data.model.CollabMemberResponse
import com.iota.campusX.Feature.Collab.data.model.CollabRequestStatus
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.Collab.data.model.ProjectRole
import com.iota.campusX.Feature.Collab.presentation.components.CollabDetailShimmer
import com.iota.campusX.Feature.Collab.presentation.components.ProjectStatusBadge
import com.iota.campusX.Navigation.CollabRequests
import com.iota.campusX.Navigation.ManageCollab
import com.iota.campusX.Navigation.Profile
import com.iota.campusX.Navigation.SendMessage
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState

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

    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(collabId) {
        viewModel.fetchCollabById(collabId)
        viewModel.hasRequested(collabId)
        viewModel.fetchCollabRequests(collabId)
        viewModel.fetchCollabMembers(collabId)
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {

                },
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                Text(
                                    text = "Manage Project",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
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
                                        colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFFE2E8F0))
                                    ) {
                                        Text("Application Pending", color = Color(0xFF64748B))
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
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                    ) {
                                        Text("Accepted - Message Lead", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = { viewModel.connectToCollab(collabId) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                    ) {
                                        Text(
                                            text = "Connect",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.White
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

                // Proper Material3 TabRow for Overview / Team / Activity Tabs
                item {
                    ProjectOverviewTabs(
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it }
                    )
                }

                // Dynamic Tab Content
                when (selectedTabIndex) {

                    0 -> {
                        // --- OVERVIEW TAB CONTENT ---
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
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Project Idea Card
                        item {
                            DetailCardItem(
                                iconRes = R.drawable.lightbulb_on,
                                iconTint = Color(0xFF2563EB),
                                title = "Project Idea",
                                content = collab.description
                            )
                        }

                        // Project URL Card
                        collab.projectUrl?.let {
                            item {

                                DetailCardItem(
                                    iconRes = R.drawable.globe,
                                    iconTint = Color(0xFF2563EB),
                                    title = "Project URL",
                                    content = it,
                                    isClickableUrl = true,
                                    onUrlClick = { uriHandler.openUri(it) }
                                )
                            }
                        }
                        // Section: "Looking for"
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
                                                skills = emptyList(),
                                                peopleNeeded = 1
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
                        // --- TEAM TAB CONTENT ---
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
                                                    color = Color(0xFF2563EB),
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
                                            CircularProgressIndicator(color = Color(0xFF2563EB))
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
                        // --- ACTIVITY TAB CONTENT ---
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "Recent Activity",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )

                                    ActivityTimelineItem(
                                        time = collab.timeAgo,
                                        title = "Project Launched",
                                        description = "${collab.authorName} started '${collab.title}' and opened applications for team members."
                                    )

                                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                                    ActivityTimelineItem(
                                        time = "Active Now",
                                        title = "Accepting Applications",
                                        description = "Looking for ${collab.requirements.joinToString(", ").ifEmpty { "team members" }}."
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun ProjectOverviewTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Overview", "Team")

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.Transparent,
        contentColor = Color(0xFF2563EB),
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 3.dp,
                    color = Color(0xFF2563EB)
                )
            }
        },
        divider = {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        }
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.5.sp,
                            color = if (isSelected) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun ActivityTimelineItem(
    time: String,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp),
            shape = CircleShape,
            color = Color(0xFF2563EB)
        ) {}

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.5.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                )
            )
        }
    }
}

@Composable
fun ProjectSummaryCard(
    collab: CollabResponse,
    currentMembers: Int = 1,
    onViewProjectClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Project Logo
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
                                fontSize = 18.sp,
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
                                color = Color(0xFF2563EB)
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Text(
                text = collab.description,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MemberAvatarsRow(
                        authorImage = collab.authorImage,
                        memberImages = collab.memberImages,
                        joinedCount = currentMembers
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$currentMembers/${collab.participantsNeeded} joined",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

            }
        }
    }
}

@Composable
fun DetailCardItem(
    iconRes: Int,
    iconTint: Color,
    title: String,
    content: String,
    isClickableUrl: Boolean = false,
    onUrlClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            if (isClickableUrl) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.clickable { onUrlClick() }
                )
            } else {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
fun RoleItemCard(
    index: Int,
    role: ProjectRole,
    onClick: () -> Unit
) {
    val (iconRes, colors) = getRoleIconAndBg(role.roleName)
    val (iconBg, iconTint) = colors

    val skillsSubtitle = if (role.skills.isNotEmpty()) {
        role.skills.joinToString(" • ")
    } else {
        "Skills specified on demand"
    }

    val peopleText = if (role.peopleNeeded <= 1) "1 person needed" else "${role.peopleNeeded} people needed"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = role.roleName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = skillsSubtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = peopleText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF5C56E6),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp
                    )
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TeamMemberItemCard(
    member: CollabMemberResponse,
    onMemberClick: () -> Unit,
    onMessageClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onMemberClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!member.image.isNullOrBlank()) {
                AsyncImage(
                    model = member.image,
                    contentDescription = member.name,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = Color(0xFF2563EB).copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = member.name.take(1).uppercase().ifBlank { "U" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (member.isLead) Color(0xFF2563EB).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (member.isLead) "Lead" else "Member",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp,
                                color = if (member.isLead) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                val subtitle = member.tagline?.ifBlank { null } ?: member.role
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!member.isCurrentUser && onMessageClick != null) {
                IconButton(onClick = onMessageClick) {
                    Icon(
                        painter = painterResource(R.drawable.messages__1_),
                        contentDescription = "Message Teammate",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else if (!member.isCurrentUser) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View Profile",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun calculateMemberCount(membersState: UiState<List<CollabMemberResponse>>, authorId: String): Int {
    val membersList = (membersState as? UiState.Success)?.data.orEmpty()
    return membersList.count { !it.isLead && it.userId != authorId }
}
