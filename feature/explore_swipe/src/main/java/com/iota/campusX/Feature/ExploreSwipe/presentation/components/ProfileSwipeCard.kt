package com.iota.campusX.Feature.ExploreSwipe.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iota.campusX.Feature.UserProfile.data.remote.response.MatchPreferenceResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.SkillResponse
import com.iota.campusX.core.designsystem.CampusWidget
import com.iota.campusX.core.designsystem.R

@Composable
fun ProfileSwipeCard(
    profile: ProfileResponse,
    matchPercentage: Int,
    onClick: () -> Unit,
    dragOffsetX: Float = 0f,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Collapse progress: 0 = expanded at top (175dp, 90dp avatar), 1 = shrunk/collapsed when scrolled (64dp, 42dp avatar)
    val collapseProgress = remember {
        derivedStateOf {
            (scrollState.value / 150f).coerceIn(0f, 1f)
        }
    }

    val headerHeight by animateDpAsState(
        targetValue = androidx.compose.ui.unit.lerp(175.dp, 64.dp, collapseProgress.value),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "HeaderHeight"
    )

    val avatarSize by animateDpAsState(
        targetValue = androidx.compose.ui.unit.lerp(90.dp, 42.dp, collapseProgress.value),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "AvatarSize"
    )

    val nameFontSize by animateFloatAsState(
        targetValue = 26f - (8f * collapseProgress.value),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "NameFontSize"
    )

    val taglineAlpha by animateFloatAsState(
        targetValue = 1f - collapseProgress.value,
        animationSpec = tween(durationMillis = 180),
        label = "TaglineAlpha"
    )

    val dragProgress = (dragOffsetX / 250f).coerceIn(-1f, 1f)
    val dynamicBorderColor = when {
        dragProgress > 0.05f -> Color(0xFF22C55E).copy(alpha = (dragProgress * 1f).coerceIn(0.3f, 1f))
        dragProgress < -0.05f -> Color(0xFFEF4444).copy(alpha = (-dragProgress * 1f).coerceIn(0.3f, 1f))
        else -> Color.Transparent
    }
    val dynamicBorderWidth = if (kotlin.math.abs(dragProgress) > 0.05f) 3.5.dp else 0.dp

    Card(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
            .border(
                width = dynamicBorderWidth,
                color = dynamicBorderColor,
                shape = RoundedCornerShape(32.dp)
            )
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Section (Purple Gradient Area with Animated Collapse/Expand)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    )

            ) {
                // Scattered Collabbit Logos Background Pattern
                ScatteredCollabbitLogosBackground(modifier = Modifier.matchParentSize())

                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Bottom Row: Name, Tagline & Animated Avatar Logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Name & Tagline
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = profile.baseProfile.name.ifBlank { "Deleted User" },
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = nameFontSize.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (taglineAlpha > 0.1f && profile.baseProfile.tagline.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = profile.baseProfile.tagline,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.White.copy(alpha = taglineAlpha * 0.9f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Expanding/Shrinking Circle Avatar Logo
                        Surface(
                            modifier = Modifier.size(avatarSize),
                            shape = CircleShape,
                            color = Color(0xFFCEFF1A),
                            border = androidx.compose.foundation.BorderStroke(
                                (1.5 + (1f - collapseProgress.value) * 1.5).dp,
                                Color.White.copy(alpha = 0.4f)
                            )
                        ) {
                            if (!profile.baseProfile.image.isNullOrBlank()) {
                                AsyncImage(
                                    model = profile.baseProfile.image,
                                    contentDescription = profile.baseProfile.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = profile.baseProfile.name.take(1).uppercase().ifBlank { "A" },
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = (avatarSize.value * 0.45f).sp
                                        ),
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Body Section (White Curved Area)
            Surface(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // User Quote / Headline
                        if (profile.baseProfile.summary.isNotBlank()){
                            Text(
                                text = "ABOUT",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                ),
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = profile.baseProfile.summary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (profile.baseProfile.lookingFor.isNotBlank()){
                            Text(
                                text = "LOOKING FOR",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                ),
                                color = Color(0xFF64748B)

                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = profile.baseProfile.lookingFor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))


                        profile.skills?.let {
                            if (it.isEmpty())return@let

                            Text(
                                text = "SKILLS",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                ),
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                it.forEach { skill ->
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0F172A))
                                    ) {
                                        Text(
                                            text = skill.name,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            ),
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        profile.matchPreferences?.let{
                            Text(
                                text = "OPEN TO",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                ),
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                it.forEachIndexed { index, pref ->
                                    val (bgColor, textColor, icon) = getChipStyle(pref.code, index)
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = bgColor
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = icon, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = pref.title,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                ),
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        profile.education?.let {
                            Text(
                                text = "EDUCATION",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                ),
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            CampusWidget(
                                college = it.college,
                                course = it.course,
                                specialization = it.specialization,
                                startYear = it.start,
                                endYear = it.end
                            )
                        }
                    }

                }
            }
        }
    }
}

private fun getChipStyle(code: String, index: Int): Triple<Color, Color, String> {
    return when (code.uppercase()) {
        "CO_FOUNDER", "CO-FOUNDER" -> Triple(Color(0xFFCEFF1A), Color(0xFF0F172A), "⚡")
        "HACKATHON_PARTNER", "HACKATHON PARTNER" -> Triple(Color(0xFFFFE6D9), Color(0xFF7A3518), "🏆")
        "PROJECT_COLLABORATOR", "PROJECT COLLABORATOR" -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), "💻")
        "STUDY_BUDDY", "STUDY BUDDY" -> Triple(Color(0xFFEDE9FE), Color(0xFF5B21B6), "📚")
        "MENTOR" -> Triple(Color(0xFFFEF08A), Color(0xFF713F12), "💡")
        else -> {
            if (index % 2 == 0) {
                Triple(Color(0xFFCEFF1A), Color(0xFF0F172A), "⚡")
            } else {
                Triple(Color(0xFFFFE6D9), Color(0xFF7A3518), "🏆")
            }
        }
    }
}

private fun getMatchInsight(profile: ProfileResponse): String {
    val skills = profile.skills.orEmpty().map { it.name.lowercase() }
    val isMl = skills.any { it.contains("ml") || it.contains("machine") || it.contains("python") || it.contains("ai") }
    return if (isMl) {
        "He builds the ML, you bring the design."
    } else {
        "Great peer match for building projects & collaborating."
    }
}

@Composable
fun ScatteredCollabbitLogosBackground(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.clipToBounds()) {
        // Logo 1 - Top Left
        Image(
            painter = painterResource(R.drawable.logo_collabbit_g),
            contentDescription = null,
            modifier = Modifier
                .offset(x = (-16).dp, y = (-12).dp)
                .size(68.dp)
                .graphicsLayer {
                    rotationZ = -22f
                    alpha = 0.12f
                }
        )

        // Logo 2 - Top Right
        Image(
            painter = painterResource(R.drawable.logo_collabbit_g),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (-10).dp)
                .size(92.dp)
                .graphicsLayer {
                    rotationZ = 16f
                    alpha = 0.10f
                }
        )

        // Logo 3 - Center Right Watermark
        Image(
            painter = painterResource(R.drawable.logo_collabbit_g),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-35).dp, y = 8.dp)
                .size(64.dp)
                .graphicsLayer {
                    rotationZ = -14f
                    alpha = 0.14f
                }
        )

        // Logo 4 - Center Left
        Image(
            painter = painterResource(R.drawable.logo_collabbit_g),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 75.dp, y = (-20).dp)
                .size(42.dp)
                .graphicsLayer {
                    rotationZ = 28f
                    alpha = 0.15f
                }
        )

        // Logo 5 - Bottom Left
        Image(
            painter = painterResource(R.drawable.logo_collabbit_g),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 10.dp, y = 16.dp)
                .size(54.dp)
                .graphicsLayer {
                    rotationZ = 18f
                    alpha = 0.08f
                }
        )

        // Logo 6 - Bottom Right
        Image(
            painter = painterResource(R.drawable.logo_collabbit_g),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-90).dp, y = 18.dp)
                .size(46.dp)
                .graphicsLayer {
                    rotationZ = -26f
                    alpha = 0.11f
                }
        )
    }
}
