package com.iota.campusX.Feature.Collab.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.iota.campusX.Feature.Opportunities.presentation.OpportunitiesViewModel
import com.iota.campusX.Feature.Opportunities.presentation.components.CourseCard
import com.iota.campusX.Feature.Opportunities.presentation.components.OpportunityShimmerItem
import com.iota.campusX.Navigation.CourseDetail
import com.iota.campusX.core.common.UiState
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.text.buildSpannedString
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
import com.iota.campusX.core.designsystem.ErrorScreen
import com.iota.campusX.core.designsystem.RefreshBox
import com.iota.campusX.feature.collab.R
import com.iota.campusX.ui.UIComponents.cardShadow
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollabExploreScreen(
    navController: NavHostController,
    viewModel: CollabViewModel = koinViewModel(),
    opportunitiesViewModel: OpportunitiesViewModel = koinViewModel(),
    navigationViewModel: NavigationViewModel = koinInject()
) {
    val lazyCollabs = viewModel.collabPagingData.collectAsLazyPagingItems()
    val coursesState by opportunitiesViewModel.coursesState.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()
    val lazyListState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    HideBottomBar(navigationViewModel, lazyListState)

    LaunchedEffect(Unit) {
        viewModel.fetchCollabs(type = "COBUILDER")
        opportunitiesViewModel.fetchCourses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ExploreHeader()
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
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
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }


                    // Hero Banner: "Turn your ideas into real projects"
                    item {
                        TurnIdeasHeroBanner(
                            onStartProjectClick = { navController.navigate(CreateCollab) }
                        )
                    }

                    // Section Title: "Featured Projects" + "See all"
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 20.dp),
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

                    // Live Sessions Section Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live Sessions",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                    }

                    // Render Live Sessions Items
                    when (val cState = coursesState) {
                        is UiState.Loading -> {
                            items(2) {
                                OpportunityShimmerItem()
                            }
                        }
                        is UiState.Success -> {
                            if (cState.data.isEmpty()) {
                                item {
                                    Text(
                                        text = "No live sessions scheduled right now.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )
                                }
                            } else {
                                items(
                                    items = cState.data,
                                    key = { "course_${it.id}" }
                                ) { course ->
                                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                                        CourseCard(
                                            course = course,
                                            onClick = {
                                                navController.navigate(CourseDetail(course.id))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        is UiState.Error -> {
                            item {
                                ErrorScreen(
                                    text = "Something went wrong!",
                                    image = null,
                                    onReTry = { opportunitiesViewModel.fetchCourses(forceRefresh = true) },
                                    buttonText = "ReTry"
                                )
                            }

                        }
                        else -> {}
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun ExploreHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Explore",
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

@Composable
fun TurnIdeasHeroBanner(
    onStartProjectClick: () -> Unit
) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            // Background Student Group Image (ideas.png)
            Image(
                painter = painterResource(R.drawable.ideas),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(290.dp)
                    .align(Alignment.TopEnd)
            )

            // Main Banner Content
            Column(
                modifier = Modifier.fillMaxWidth()

            ) {
                // Top Tag Pill: "Build & Collaborate"
                Surface(
                    modifier = Modifier.padding(20.dp),
                    shape = CircleShape,
                    color = Color(0xFFE0F7F6),
                    border = BorderStroke(1.dp, Color(0xFFB2F0EC))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.people_bold),
                            contentDescription = null,
                            tint = Color(0xFF007A87),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Build & Collaborate",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF006873)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Headline Title
                Column(
                    modifier = Modifier.fillMaxWidth(0.58f).padding(start = 20.dp)
                ) {

                    val colorTitle = buildAnnotatedString {
                        // append "Hello" with red text color
                        append("Turn Your Ideas Into\n")
                        // append "Hello" with blue text color
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Real Projects") }
                    }
                    Text(
                        text = colorTitle,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            letterSpacing = (-0.5).sp,
                        ),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Button: "Start a Project →"
                    Button(
                        onClick = onStartProjectClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Start a Project",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Value Proposition Features Bar
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),

                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Item 1: Find Collaborators
                        FeatureValueItem(
                            iconRes = R.drawable.people_bold,
                            title = "Find\nCollaborators"
                        )

                        // Divider 1
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp)
                                .background(Color(0xFFCBD5E1))
                        )

                        // Item 2: Work on Real Projects
                        FeatureValueItem(
                            iconRes = R.drawable.code_simple,
                            title = "Work on\nReal Projects"
                        )

                        // Divider 2
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp)
                                .background(Color(0xFFCBD5E1))
                        )

                        // Item 3: Grow Your Skills
                        FeatureValueItem(
                            iconRes = com.iota.campusX.core.designsystem.R.drawable.trophy_star,
                            title = "Grow Your\nSkills"
                        )
                    }
                }
            }
        }
}

@Composable
private fun FeatureValueItem(
    iconRes: Int,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color(0xFF007A87),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                lineHeight = 14.sp
            ),
            color = Color(0xFF1E293B)
        )
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
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .cardShadow(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = {
            onClick()
        }

    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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

                }
                ProjectStatusBadge(status = collab.status)


//                Surface(
//                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
//                    shape = RoundedCornerShape(6.dp)
//                ) {
//                    Text(
//                        text = collab.collabType.name
//                            .lowercase()
//                            .replaceFirstChar { it.uppercase() },
//                        style = MaterialTheme.typography.labelSmall.copy(
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 10.5.sp,
//                            color = Color(0xFF2563EB)
//                        ),
//                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
//                    )
//                }

                // Description — max 2 lines
                Text(
                    text = collab.description,
                    style = MaterialTheme.typography.bodySmall,
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
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    0.5.dp,
                                    MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                    modifier = Modifier.padding(
                                        horizontal = 6.dp,
                                        vertical = 6.dp
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
