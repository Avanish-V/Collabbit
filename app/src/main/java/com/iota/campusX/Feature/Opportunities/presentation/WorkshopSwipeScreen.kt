package com.iota.campusX.Feature.Opportunities.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iota.campusX.R
import com.iota.campusX.ui.UIComponents.cardShadow
import kotlinx.coroutines.launch

data class SwipeWorkshop(
    val id: String,
    val title: String,
    val instructor: String,
    val category: String,
    val matchPercentage: Int,
    val description: String,
    val price: String,
    val seats: Int,
    val date: String,
    val level: String,
    val thumbnail: String?
)

val dummySwipeWorkshops = listOf(
    SwipeWorkshop(
        id = "2",
        title = "Master Jetpack Compose Bootcamp",
        instructor = "Victor is back",
        category = "Live Workshop",
        matchPercentage = 95,
        description = "10-Day Intensive Bootcamp covering Modern Declarative UI, Material Design 3, and SDUI. Build a real-world Book Library App.",
        price = "Free",
        seats = 60,
        date = "Aug 22, 2026",
        level = "Beginner",
        thumbnail = "https://campuscircle.s3.ap-south-1.amazonaws.com/live-skills/thumbnails/1788102153719_9c086c18_69eec3fe8e494_1777255422.png"
    ),
    SwipeWorkshop(
        id = "1",
        title = "System Design",
        instructor = "Avanish (SDE at Google)",
        category = "Live Workshop",
        matchPercentage = 88,
        description = "Master the art of building scalable systems. Learn about load balancing, microservices, and high-level architecture.",
        price = "Free",
        seats = 10,
        date = "Aug 28, 2026",
        level = "Beginner",
        thumbnail = "https://campuscircle.s3.ap-south-1.amazonaws.com/live-skills/thumbnails/1788029752973_de22c156_ChatGPT_Image_Apr_23__2026__12_06_14_PM.png"
    )
)

@Composable
fun WorkshopSwipeScreen() {
    var workshops by remember { mutableStateOf(dummySwipeWorkshops) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                workshops.reversed().forEach { workshop ->
                    WorkshopSwipeCard(
                        workshop = workshop,
                        onSwipeLeft = { workshops = workshops.filter { it.id != workshop.id } },
                        onSwipeRight = { workshops = workshops.filter { it.id != workshop.id } }
                    )
                }
                
                if (workshops.isEmpty()) {
                    Text(
                        text = "No more workshops available!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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
                    onClick = { /* Handle button click */ }
                )
                Spacer(modifier = Modifier.width(48.dp))
                ActionButton(
                    icon = Icons.Default.Favorite,
                    label = "Join",
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { /* Handle button click */ }
                )
            }
        }
    }
}

@Composable
fun WorkshopSwipeCard(
    workshop: SwipeWorkshop,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .graphicsLayer(
                translationX = offsetX.value,
                rotationZ = rotation.value
            )
            .pointerInput(workshop.id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            rotation.snapTo(offsetX.value / 20f)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            if (offsetX.value > 400f) {
                                offsetX.animateTo(1000f, tween(300))
                                onSwipeRight()
                            } else if (offsetX.value < -400f) {
                                offsetX.animateTo(-1000f, tween(300))
                                onSwipeLeft()
                            } else {
                                launch { offsetX.animateTo(0f, tween(300)) }
                                launch { rotation.animateTo(0f, tween(300)) }
                            }
                        }
                    }
                )
            }
            .cardShadow(alpha = 0.3f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header: Image/Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
            ) {
                AsyncImage(
                    model = workshop.thumbnail,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Match Badge on top of image
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.fire_flame_curved),
                            contentDescription = null,
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${workshop.matchPercentage}% Match",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Content
            Text(
                text = workshop.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.Black,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "by ${workshop.instructor}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = workshop.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.6f),
                maxLines = 3,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.calendar),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = workshop.date,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Black.copy(alpha = 0.4f)
                    )
                }

                Surface(
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = workshop.price,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF15803D)
                    )
                }
            }
        }
    }
}
