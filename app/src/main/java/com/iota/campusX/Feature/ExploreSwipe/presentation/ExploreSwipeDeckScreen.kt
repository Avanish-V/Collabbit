package com.iota.campusX.Feature.ExploreSwipe.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.ExploreSwipe.domain.model.SwipeItem
import com.iota.campusX.Feature.ExploreSwipe.presentation.components.CollabSwipeCard
import com.iota.campusX.Feature.ExploreSwipe.presentation.components.ProfileSwipeCard
import com.iota.campusX.Feature.ExploreSwipe.presentation.components.SendNoteBottomSheet
import com.iota.campusX.Feature.ExploreSwipe.presentation.components.SwipeCardShimmer
import com.iota.campusX.Navigation.CollabDetail
import com.iota.campusX.Navigation.ViewProfile
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.ErrorScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ExploreSwipeDeckScreen(
    navController: NavHostController,
    viewModel: ExploreSwipeViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val cardsState by viewModel.cardsState.collectAsStateWithLifecycle()
    val activeCards by viewModel.activeCards.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var pendingNoteProfile by remember { mutableStateOf<SwipeItem.ProfileItem?>(null) }
    var recenterTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearMessage()
        }
    }

    val isSwipeUnlocked by viewModel.isSwipeUnlocked.collectAsStateWithLifecycle()
    val currentUserScore by viewModel.currentUserScore.collectAsStateWithLifecycle()

    val onCollabClick: (String) -> Unit = remember(navController) {
        { collabId -> navController.navigate(CollabDetail(collabId = collabId)) }
    }
    val onProfileClick: (String) -> Unit = remember(navController) {
        { userId -> navController.navigate(ViewProfile(userId = userId)) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 64.dp), // Pushes deck up to avoid bottom bar
        contentAlignment = Alignment.Center
    ) {
        if (!isSwipeUnlocked) {
            SwipeLockedState(
                currentScore = currentUserScore,
                onCompleteProfile = {
                    navController.navigate(com.iota.campusX.Navigation.Profile())
                }
            )
        } else {
            when (val state = cardsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .height(540.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background cards shimmers to look like a stack
                        SwipeCardShimmer(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = 20.dp)
                                .scale(0.92f)
                                .alpha(0.5f)
                        )
                        SwipeCardShimmer(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = 10.dp)
                                .scale(0.96f)
                                .alpha(0.7f)
                        )
                        // Top card shimmer
                        SwipeCardShimmer(
                            modifier = Modifier.fillMaxSize()
                        )

                        // Loading Text Overlay
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 40.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = CircleShape,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Finding your best matches...",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                is UiState.Error -> {
                    ErrorScreen(
                        text = state.message,
                        onReTry = { viewModel.loadCards() }
                    )
                }

                else -> {
                    if (activeCards.isEmpty()) {
                        EmptySwipeDeckState(
                            onReload = { viewModel.loadCards() }
                        )
                    } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Swipe Deck Stack Area
                        Box(
                            modifier = Modifier
                                .height(540.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Render top 3 cards in reverse order so top is drawn last
                            val visibleCards = activeCards.take(3).reversed()
                            visibleCards.forEach { item ->
                                key(item.id) {
                                    val indexInStack = activeCards.indexOf(item)
                                    val isTop = indexInStack == 0

                                    SwipeableCardContainer(
                                        item = item,
                                        isTop = isTop,
                                        stackIndex = indexInStack,
                                        onSwipeLeft = { viewModel.onSwipeLeft(item) },
                                        onSwipeRight = {
                                            when (item) {
                                                is SwipeItem.ProfileItem -> {
                                                    pendingNoteProfile = item
                                                }
                                                is SwipeItem.CollabItem -> {
                                                    viewModel.onSwipeRight(item)
                                                }
                                            }
                                        },
                                        onSuperLike = { viewModel.onSuperLike(item) },
                                        onCollabClick = onCollabClick,
                                        onProfileClick = onProfileClick,
                                        recenterTrigger = recenterTrigger
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }
    }

        // Send Note Bottom Sheet when swiping right on a Profile
        pendingNoteProfile?.let { profileItem ->
            SendNoteBottomSheet(
                profile = profileItem.profile,
                matchPercentage = profileItem.matchPercentage,
                onDismiss = {
                    pendingNoteProfile = null
                    recenterTrigger++
                },
                onSendNote = { note ->
                    viewModel.onSwipeRight(profileItem, note)
                    pendingNoteProfile = null
                }
            )
        }

        // Snackbar Host for swipe notifications
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .zIndex(1f),
            snackbar = { data ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(0.9f)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val message = data.visuals.message
                        val icon = when {
                            message.contains("Super-liked") -> Icons.Default.Star
                            message.contains("restored") -> Icons.Default.Refresh
                            message.contains("sent") -> Icons.Default.Favorite
                            else -> Icons.Default.Star // Default icon
                        }
                        
                        val iconColor = when {
                            message.contains("Super-liked") -> Color(0xFFF59E0B)
                            message.contains("restored") -> MaterialTheme.colorScheme.primary
                            message.contains("sent") -> Color(0xFF22C55E)
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        Text(
                            text = message.replace("⭐ ", ""), // Remove emoji as we have icon
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun SwipeableCardContainer(
    item: SwipeItem,
    isTop: Boolean,
    stackIndex: Int,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSuperLike: () -> Unit,
    onCollabClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    recenterTrigger: Int = 0
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0 && isTop) {
            launch { offsetX.animateTo(0f, tween(250)) }
            launch { offsetY.animateTo(0f, tween(250)) }
            launch { rotation.animateTo(0f, tween(250)) }
        }
    }

    // Stack visual adjustments
    val scale = remember(stackIndex) { 1f - (stackIndex * 0.04f).coerceAtMost(0.08f) }
    val verticalOffset = remember(stackIndex) { (stackIndex * 10).dp }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = verticalOffset)
            .scale(scale)
            .graphicsLayer {
                if (isTop) {
                    translationX = offsetX.value
                    translationY = offsetY.value
                    rotationZ = rotation.value
                }
            }
            .then(
                if (isTop) {
                    Modifier.pointerInput(item.id) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                    offsetY.snapTo(offsetY.value + dragAmount.y)
                                    rotation.snapTo(offsetX.value / 18f)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (offsetX.value > 300f) {
                                        offsetX.animateTo(1200f, tween(250))
                                        onSwipeRight()
                                    } else if (offsetX.value < -300f) {
                                        offsetX.animateTo(-1200f, tween(250))
                                        onSwipeLeft()
                                    } else if (offsetY.value < -350f) {
                                        offsetY.animateTo(-1200f, tween(250))
                                        onSuperLike()
                                    } else {
                                        launch { offsetX.animateTo(0f, tween(250)) }
                                        launch { offsetY.animateTo(0f, tween(250)) }
                                        launch { rotation.animateTo(0f, tween(250)) }
                                    }
                                }
                            }
                        )
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (item) {
            is SwipeItem.CollabItem -> {
                CollabSwipeCard(
                    collab = item.collab,
                    matchPercentage = item.matchPercentage,
                    onClick = { onCollabClick(item.collab.id) },
                    onAuthorClick = { onProfileClick(item.collab.authorId) }
                )
            }
            is SwipeItem.ProfileItem -> {
                ProfileSwipeCard(
                    profile = item.profile,
                    matchPercentage = item.matchPercentage,
                    onClick = { onProfileClick(item.profile.uid) }
                )
            }
        }

        // Swipe Feedback Overlays
        if (isTop) {
            // Right Swipe Feedback (CONNECT)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(28.dp)
                    .graphicsLayer {
                        alpha = (offsetX.value / 250f).coerceIn(0f, 1f)
                    },
                color = Color(0xFF22C55E).copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Text(
                    text = "CONNECT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // Left Swipe Feedback (PASS)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(28.dp)
                    .graphicsLayer {
                        alpha = (-offsetX.value / 250f).coerceIn(0f, 1f)
                    },
                color = Color(0xFFEF4444).copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Text(
                    text = "PASS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun EmptySwipeDeckState(
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.heart_partner_handshake),
                    contentDescription = "Caught up",
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "You're All Caught Up!",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You've reviewed all available collaboration opportunities and peer profiles for now. Check back soon for fresh recommendations!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onReload,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Reload Feed",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun SwipeLockedState(
    currentScore: Int,
    onCompleteProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = CircleShape,
                color = Color(0xFFFEF3C7),
                border = BorderStroke(1.5.dp, Color(0xFFFDE68A))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.user_lock),
                        contentDescription = "Locked",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Requires >60% Completion",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Unlock Swipe Match",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Swipe Match requires at least 60% profile completion so you receive high-quality peer & project recommendations.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Score Progress Meter
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Score: $currentScore%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFD97706)
                    )
                    Text(
                        text = "Target: 60%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { (currentScore / 60f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFFD97706),
                    trackColor = Color(0xFFFEF3C7),
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val pointsNeeded = (60 - currentScore).coerceAtLeast(1)

            Button(
                onClick = onCompleteProfile,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.pencil),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Complete Profile (+$pointsNeeded% Needed)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
