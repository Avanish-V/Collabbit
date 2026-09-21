package com.iota.campusX.Feature.Opportunities.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iota.campusX.R
import com.iota.campusX.ui.UIComponents.cardShadow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class SwipeOpportunity(
    val id: String,
    val title: String,
    val description: String,
    val tag: String,
    val matchPercentage: Int,
    val authorName: String,
    val authorRole: String,
    val authorImage: String?,
    val slots: Int,
    val date: String,
    val mainTag: String
)

val dummySwipeOpportunities = listOf(
    SwipeOpportunity(
        id = "1",
        title = "Finder Social Network",
        description = "Hey I am building social networking app for students.",
        tag = "Cobuilder",
        matchPercentage = 92,
        authorName = "Abhay Lohar",
        authorRole = "Project Lead",
        authorImage = null,
        slots = 1,
        date = "Aug 28, 2026",
        mainTag = "Android"
    ),
    SwipeOpportunity(
        id = "2",
        title = "Need team for Hack India",
        description = "Looking for developers to join my team for the upcoming national hackathon.",
        tag = "Hackathon",
        matchPercentage = 85,
        authorName = "Sneha Gupta",
        authorRole = "Team Lead",
        authorImage = null,
        slots = 4,
        date = "Jul 31, 2026",
        mainTag = "React Native"
    ),
    SwipeOpportunity(
        id = "3",
        title = "DSA Study Group",
        description = "Join us daily for solving LeetCode problems and preparing for interviews.",
        tag = "Study Group",
        matchPercentage = 78,
        authorName = "Rahul Sharma",
        authorRole = "Organizer",
        authorImage = null,
        slots = 6,
        date = "Sep 01, 2026",
        mainTag = "Algorithm"
    )
)

@Composable
fun OpportunitySwipeScreen(navController: androidx.navigation.NavController? = null) {
    var opportunities by remember { mutableStateOf(dummySwipeOpportunities) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController?.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(24.dp))
                }
                Text(
                    text = "Discover",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                IconButton(onClick = { /* Refresh */ }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(20.dp))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (opportunities.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "All caught up!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { opportunities = dummySwipeOpportunities }) {
                            Text("Reset Stack")
                        }
                    }
                }

                opportunities.reversed().forEach { opportunity ->
                    key(opportunity.id) {
                        SwipeCard(
                            opportunity = opportunity,
                            onSwipeLeft = { 
                                opportunities = opportunities.filter { it.id != opportunity.id }
                            },
                            onSwipeRight = { 
                                opportunities = opportunities.filter { it.id != opportunity.id }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(
                    icon = Icons.Default.Close,
                    label = "Pass",
                    color = Color.Black.copy(alpha = 0.6f),
                    onClick = {
                        // Potential imperative swipe implementation could go here
                    }
                )
                Spacer(modifier = Modifier.width(48.dp))
                ActionButton(
                    icon = Icons.Default.Favorite,
                    label = "Interested",
                    color = MaterialTheme.colorScheme.primary,
                    onClick = {
                        // Potential imperative swipe implementation could go here
                    }
                )
            }
        }
    }
}

@Composable
fun SwipeCard(
    opportunity: SwipeOpportunity,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Thresholds
    val swipeThreshold = 300f
    val dismissThreshold = 600f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .graphicsLayer(
                translationX = offsetX.value,
                rotationZ = rotation.value,
                alpha = (1f - (kotlin.math.abs(offsetX.value) / 1000f)).coerceIn(0.7f, 1f)
            )
            .pointerInput(opportunity.id) {
                detectDragGestures(
                    onDragCancel = {
                        scope.launch {
                            offsetX.animateTo(0f, tween(400))
                            rotation.animateTo(0f, tween(400))
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val newX = offsetX.value + dragAmount.x
                            offsetX.snapTo(newX)
                            // Smoother rotation mapping
                            rotation.snapTo(newX / 25f)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            when {
                                offsetX.value > swipeThreshold -> {
                                    // Swipe Right (Like)
                                    offsetX.animateTo(dismissThreshold * 2, tween(300))
                                    onSwipeRight()
                                }
                                offsetX.value < -swipeThreshold -> {
                                    // Swipe Left (Pass)
                                    offsetX.animateTo(-dismissThreshold * 2, tween(300))
                                    onSwipeLeft()
                                }
                                else -> {
                                    // Snap back
                                    launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy)) }
                                    launch { rotation.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy)) }
                                }
                            }
                        }
                    }
                )
            }
            .cardShadow(alpha = 0.2f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header: Match & Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, Color(0xFFFFEDD5))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.fire_flame_curved),
                            contentDescription = null,
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${opportunity.matchPercentage}% Match",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2410C)
                            )
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Bookmark",
                    tint = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Content
            Text(
                text = opportunity.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = opportunity.tag,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = opportunity.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black.copy(alpha = 0.6f),
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main Tag
                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color(0xFFDCFCE7))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = opportunity.mainTag,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF15803D)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Slots
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.user_normal),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Black.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${opportunity.slots} spots",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.calendar),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Black.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = opportunity.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(20.dp))

            // Author Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color.LightGray
                ) {
                    AsyncImage(
                        model = opportunity.authorImage,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = opportunity.authorName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                    Text(
                        text = opportunity.authorRole,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, color.copy(alpha = 0.1f)),
            shadowElevation = 8.dp,
            onClick = onClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black.copy(alpha = 0.4f)
        )
    }
}
