package com.iota.campusX.Feature.ExploreSwipe.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iota.campusX.Feature.ExploreSwipe.domain.model.SwipeItem
import com.iota.campusX.Feature.ExploreSwipe.presentation.components.ProfileSwipeCard
import com.iota.campusX.Feature.ExploreSwipe.presentation.components.SendNoteBottomSheet
import com.iota.campusX.Feature.ExploreSwipe.presentation.components.SwipeCardShimmer
import com.iota.campusX.core.common.UiState
import com.iota.campusX.ui.UIComponents.ErrorScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreSwipeDeckScreen(
    onProfileClick: (String) -> Unit = {},
    viewModel: ExploreSwipeViewModel = koinViewModel()
) {
    val cardsState by viewModel.cardsState.collectAsStateWithLifecycle()
    val activeCards by viewModel.activeCards.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val notesSentToday by viewModel.notesSentToday.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingNoteProfile by remember { mutableStateOf<SwipeItem?>(null) }
    var recenterTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Collab",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1.5).sp
                        ),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = cardsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
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
                        SwipeCardShimmer(
                            modifier = Modifier.fillMaxSize()
                        )

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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            TopProgressBar(
                                notesSentToday = notesSentToday,
                                maxDailyNotes = viewModel.maxDailyNotes
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
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
                                            onSwipeRight = { pendingNoteProfile = item },
                                            onSuperLike = { viewModel.onSuperLike(item) },
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
                                else -> Icons.Default.Star
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
                                text = message.replace("⭐ ", ""),
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
}

@Composable
fun SwipeableCardContainer(
    item: SwipeItem,
    isTop: Boolean,
    stackIndex: Int,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSuperLike: () -> Unit,
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

    val targetScale = remember(stackIndex) { 1f - (stackIndex * 0.04f).coerceAtMost(0.08f) }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "StackScale"
    )

    val targetVerticalOffset = remember(stackIndex) { (stackIndex * 10).dp }
    val animatedVerticalOffset by animateDpAsState(
        targetValue = targetVerticalOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "StackOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = animatedVerticalOffset)
            .scale(animatedScale)
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
        ProfileSwipeCard(
            profile = item.profile,
            matchPercentage = item.matchPercentage,
            onClick = { onProfileClick(item.profile.uid) },
            dragOffsetX = if (isTop) offsetX.value else 0f
        )

        if (isTop) {
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
    onReload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "You're All Caught Up!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You've explored all active student profiles for now. Check back soon for new peer matches!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onReload,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reload Matches", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TopProgressBar(
    notesSentToday: Int = 0,
    maxDailyNotes: Int = 10
) {
    val notesLeft = (maxDailyNotes - notesSentToday).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        // Segmented Progress Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(maxDailyNotes) { index ->
                val isFilled = index < notesSentToday
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) Color(0xFFCEFF1A) else Color(0xFF2A2A3D)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Counter Text Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$notesSentToday of $maxDailyNotes notes sent today",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "$notesLeft notes left",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}
