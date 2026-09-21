package com.iota.campusX.Feature.Opportunities.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Opportunities.data.model.CourseResponse
import com.iota.campusX.R
import com.iota.campusX.ui.UIComponents.cardShadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCard(
    course: CourseResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .cardShadow(alpha = 0.5f),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // 1. Thumbnail Banner
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                AsyncImage(
                    model = course.thumbnail,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
                )

//                // Intensive Badge (Optional based on data)
//                Surface(
//                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
//                    color = Color.Black.copy(alpha = 0.6f),
//                    shape = RoundedCornerShape(8.dp),
//                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
//                ) {
//                    Text(
//                        text = "10-DAY INTENSIVE",
//                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                        style = MaterialTheme.typography.labelSmall.copy(
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White,
//                            fontSize = 9.sp
//                        )
//                    )
//                }
                
//                // Price Badge
//                val isFree = course.price.equals("Free", ignoreCase = true) || course.price == "0"
//                Surface(
//                    modifier = Modifier.padding(12.dp).align(Alignment.TopEnd),
//                    color = Color.White,
//                    shape = CircleShape,
//                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
//                ) {
//                    Text(
//                        text = if (isFree) "Free" else "₹${course.price}",
//                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                        style = MaterialTheme.typography.labelMedium.copy(
//                            fontWeight = FontWeight.Black,
//                            color = Color.Black
//                        )
//                    )
//                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // 2. Category & Level Chips
                val isEnded = course.sessionStatus.equals("ENDED", ignoreCase = true) || course.skillState.equals("ENDED", ignoreCase = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryChip(text = course.category, color = MaterialTheme.colorScheme.primaryContainer, textColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    CategoryChip(text = course.level, color = MaterialTheme.colorScheme.secondaryContainer, textColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    if (isEnded) {
                        CategoryChip(text = "ENDED", color = MaterialTheme.colorScheme.errorContainer, textColor = MaterialTheme.colorScheme.onErrorContainer)
                    } else if (course.isJoinLinkEnabled && !course.meetLink.isNullOrBlank()) {
                        CategoryChip(text = "LIVE", color = Color(0xFFE8F5E9), textColor = Color(0xFF2E7D32))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Title
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Details (Date, Time, Duration)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CourseDetailItem(icon = R.drawable.calendar, text = course.date ?: "TBA")
                    CourseDetailItem(icon = R.drawable.clock, text = "${course.duration} min")
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // 5. Instructor & Enrollment Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val instructorName = course.instructorProfile?.name ?: course.instructor
                    val instructorAvatar = course.instructorProfile?.avatarUrl

                    Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = Color.LightGray) {
                        AsyncImage(
                            model = instructorAvatar,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.user_normal)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = instructorName,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = course.instructorProfile?.bio ?: "Host",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${course.enrolled ?: 0} / ${course.seats ?: 0}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Enrolled",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(text: String, color: Color, textColor: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun CourseDetailItem(icon: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
