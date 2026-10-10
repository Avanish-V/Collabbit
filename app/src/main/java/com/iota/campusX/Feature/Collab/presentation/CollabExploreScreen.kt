package com.iota.campusX.Feature.Collab.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.Collab.presentation.components.CollabShimmerItem
import com.iota.campusX.Feature.Collab.presentation.components.ProjectStatusBadge
import com.iota.campusX.Navigation.CollabDetail
import com.iota.campusX.Navigation.CreateCollab
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.R
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.UIComponents.RefreshBox
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollabExploreScreen(
    navController: NavHostController,
    viewModel: CollabViewModel = koinViewModel(),
    navigationViewModel: NavigationViewModel = koinInject()
) {
    val lazyCollabs = viewModel.collabPagingData.collectAsLazyPagingItems()
    val pullToRefreshState = rememberPullToRefreshState()
    val lazyListState = rememberLazyListState()
    HideBottomBar(navigationViewModel, lazyListState)

    LaunchedEffect(Unit) {
        viewModel.fetchCollabs(type = "COBUILDER")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        RefreshBox(
            modifier = Modifier.padding(padding),
            pullToRefreshState = pullToRefreshState,
            onRefresh = { lazyCollabs.refresh() },
            isRefreshing = lazyCollabs.loadState.refresh is LoadState.Loading && lazyCollabs.itemCount > 0
        ) {
            val refreshState = lazyCollabs.loadState.refresh
            val isInitialLoading = refreshState is LoadState.Loading && lazyCollabs.itemCount == 0
            val isError = refreshState is LoadState.Error && lazyCollabs.itemCount == 0

            if (isInitialLoading) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(6) { CollabShimmerItem() }
                }
            } else if (isError) {
                ErrorScreen(
                    text = (refreshState as LoadState.Error).error.localizedMessage ?: "Failed to load collaborations",
                    onReTry = { lazyCollabs.retry() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    // Header Title
                    item {
                        ExploreHeader()
                    }

                    // Hero Banner: "Turn your ideas into real projects"
                    item {
                        TurnIdeasHeroBanner(
                            onStartProjectClick = { navController.navigate(CreateCollab) }
                        )
                    }

                    // Section Title: "Featured Projects" + "See all"
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Featured Projects",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                    }

                    if (lazyCollabs.itemCount == 0) {
                        item {
                            EmptyCollabState(
                                onStartProjectClick = { navController.navigate(CreateCollab) }
                            )
                        }
                    } else {
                        items(
                            count = lazyCollabs.itemCount,
                            key = lazyCollabs.itemKey { it.id }
                        ) { index ->
                            val collab = lazyCollabs[index]
                            if (collab != null) {
                                FeaturedProjectCard(
                                    collab = collab,
                                    onClick = {
                                        navController.navigate(CollabDetail(collabId = collab.id))
                                    }
                                )
                            }
                        }

                        if (lazyCollabs.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF2563EB))
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun ExploreHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Collaborations",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Discover projects. Find your people.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        )
    }
}

private val HeroBannerGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF1E3A8A), // Deep Royal Blue
        Color(0xFF2563EB), // Vibrant Electric Blue
        Color(0xFF4F46E5)  // Indigo Purple Accent
    )
)

@Composable
fun TurnIdeasHeroBanner(
    onStartProjectClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onStartProjectClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = HeroBannerGradient)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Tag Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(6.dp),
                            shape = CircleShape,
                            color = Color(0xFF34D399) // Live green indicator
                        ) {}
                        Text(
                            text = "BUILD & COLLABORATE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Headline Title
                Text(
                    text = "Turn Your Ideas Into\nReal Projects",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 21.sp,
                        lineHeight = 26.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = "Find developers, designers & creators to form your dream team.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    ),
                    modifier = Modifier.fillMaxWidth(0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Row: CTA Pill + Floating Icon Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Action Pill Button
                    Surface(
                        onClick = onStartProjectClick,
                        shape = RoundedCornerShape(30.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Start a Project",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF1E3A8A),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeaturedProjectCard(
    collab: CollabResponse,
    onClick: () -> Unit,
    onActionClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {},
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            verticalAlignment = Alignment.Top
        ) {
            // ── Left: Project Logo ──────────────────────────────────
            if (!collab.logoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = collab.logoUrl,
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(14.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Gradient-like accent background with initials
                val accentColor = remember(collab.collabType) {
                    when (collab.collabType.name) {
                        "HACKATHON" -> Color(0xFF1E3A8A)
                        "COBUILDER" -> Color(0xFF1D4ED8)
                        "STUDY"     -> Color(0xFF0369A1)
                        "RESEARCH"  -> Color(0xFF065F46)
                        else        -> Color(0xFF374151)
                    }
                }
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = accentColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = collab.title.take(2).uppercase().ifBlank { "CO" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // ── Centre: Content ─────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title + Status badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = collab.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    ProjectStatusBadge(status = collab.status)
                }

                // Type chip  (e.g. "AI / ML", "App / Web")
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = collab.collabType.name
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = Color(0xFF2563EB)
                        ),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    )
                }

                // Description — max 2 lines
                Text(
                    text = collab.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Members row
                val actualJoinedCount = collab.memberImages.filter { it.isNotBlank() && it != collab.authorImage }.size

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onAvatarClick() }
                ) {
                    MemberAvatarsRow(
                        authorImage = collab.authorImage,
                        memberImages = collab.memberImages,
                        joinedCount = actualJoinedCount
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$actualJoinedCount/${collab.participantsNeeded} joined",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // Tech / Skills chips row
                val displayTags = when {
                    collab.roles.isNotEmpty() -> collab.roles.map { it.roleName }
                    collab.requirements.isNotEmpty() -> collab.requirements
                    else -> emptyList()
                }
                if (displayTags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        displayTags.take(4).forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    0.5.dp,
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(
                                        horizontal = 7.dp,
                                        vertical = 3.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ── Right: Chevron ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CollabCard(
    collab: CollabResponse,
    onActionClick: () -> Unit,
    onClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FeaturedProjectCard(
        collab = collab,
        onClick = onClick,
        onActionClick = onActionClick,
        onAvatarClick = onAvatarClick,
        onDeleteClick = onDeleteClick,
        modifier = modifier
    )
}

@Composable
fun MemberAvatarsRow(
    authorImage: String?,
    memberImages: List<String> = emptyList(),
    joinedCount: Int = 0
) {
    // Build the final avatar list: author first, then joined members (deduplicated)
    val avatars = buildList {
        if (!authorImage.isNullOrBlank()) add(authorImage)
        memberImages.filter { it.isNotBlank() && it != authorImage }.forEach { add(it) }
    }

    val maxVisible = 3
    val visibleAvatars = avatars.take(maxVisible)
    val overflowCount = (joinedCount - visibleAvatars.size).coerceAtLeast(0)
    val overlapOffset = 14.dp  // how much each avatar overlaps the previous
    val avatarSize = 24.dp

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.height(avatarSize)
                .width(avatarSize + overlapOffset * (visibleAvatars.size - 1).coerceAtLeast(0))
        ) {
            visibleAvatars.forEachIndexed { index, imageUrl ->
                val xOffset = overlapOffset * index
                Box(
                    modifier = Modifier
                        .offset(x = xOffset)
                        .zIndex((visibleAvatars.size - index).toFloat())
                        .size(avatarSize)
                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.placeholder),
                        placeholder = painterResource(R.drawable.placeholder)
                    )
                }
            }

            // Show fallback placeholder if no avatars
            if (visibleAvatars.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .size(avatarSize)
                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {}
            }
        }

        // +N overflow badge
        if (overflowCount > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.size(avatarSize)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "+$overflowCount",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCollabState(onStartProjectClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.heart_partner_handshake),
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No collaborations found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onStartProjectClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("Start First Project")
        }
    }
}
