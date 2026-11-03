package com.iota.campusX.Feature.UserProfile.ui.Components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraInfoResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraLevelResponse
import com.iota.campusX.Feature.UserProfile.domain.Model.BaseProfile
import com.iota.campusX.core.designsystem.UserAvatar
import com.iota.campusX.feature.profile.R

val HexagonShape = GenericShape { size, _ ->
    val width = size.width
    val height = size.height
    val radius = width.coerceAtMost(height) / 2f
    val centerX = width / 2f
    val centerY = height / 2f

    for (i in 0 until 6) {
        val angleRad = Math.toRadians((60 * i - 30).toDouble())
        val x = centerX + radius * Math.cos(angleRad).toFloat()
        val y = centerY + radius * Math.sin(angleRad).toFloat()
        if (i == 0) {
            moveTo(x, y)
        } else {
            lineTo(x, y)
        }
    }
    close()
}

data class ProfileBadgeItem(
    val level: AuraLevelResponse,
    val minPoints: Int,
    val color: Color,
    val iconRes: Int
)

private val profileBadges = listOf(
    ProfileBadgeItem(AuraLevelResponse.NEWCOMER, 0, Color(0xFF10B981), R.drawable.new_comer_badge),
    ProfileBadgeItem(AuraLevelResponse.SPARK, 300, Color(0xFFF59E0B), R.drawable.spark_logo),
    ProfileBadgeItem(AuraLevelResponse.RISING, 600, Color(0xFF06B6D4), R.drawable.rising),
    ProfileBadgeItem(AuraLevelResponse.GLOWING, 1200, Color(0xFF8B5CF6), R.drawable.glowing),
    ProfileBadgeItem(AuraLevelResponse.RADIANT, 1500, Color(0xFFEC4899), R.drawable.new_comer_badge)
)

@Composable
fun RedesignedProfileHeader(
    modifier: Modifier = Modifier,
    user: BaseProfile?,
    auraPoints: AuraInfoResponse?,
    onEditNameClick: (BaseProfile) -> Unit = {},
    onAddPhotoClick: () -> Unit = {},
    onAuraClick: () -> Unit = {},
    isCurrentUser: Boolean = false
) {
    val currentPoints = auraPoints?.auraPoints ?: 0
    val lazyListState = rememberLazyListState()


    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 20.dp, end = 0.dp, top = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            UserAvatar(
                imageUrl = user?.image,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Hexagon Badge Carousel
            LazyRow(
                state = lazyListState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(profileBadges) { index, badge ->
                    val isUnlocked = currentPoints >= badge.minPoints

                    AnimatedProfileBadgeItem(
                        badge = badge,
                        isUnlocked = isUnlocked,
                        currentPoints = currentPoints,
                        onClick = onAuraClick
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            user?.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (isCurrentUser) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { user?.let { onEditNameClick(it) } },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.edit_2),
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        user?.tagline?.let {
            if (it.isNotEmpty()) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
fun AnimatedProfileBadgeItem(
    badge: ProfileBadgeItem,
    isUnlocked: Boolean,
    currentPoints: Int,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BadgeGlow")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgeScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(82.dp)
        ) {
            // Glowing Hexagon Aura Ring
            if (isUnlocked) {
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .scale(scale)
                        .clip(HexagonShape)
                        .background(badge.color.copy(alpha = glowAlpha * 0.45f))
                )
            }

            // Hexagon Badge Surface Container
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .scale(if (isUnlocked ) scale else 1f),
                shape = HexagonShape,
                color = if (isUnlocked) badge.color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
//                border = BorderStroke(
//                    width = if (isUnlocked && isCurrentActive) 2.dp else 1.dp,
//                    color = if (isUnlocked) badge.color else MaterialTheme.colorScheme.outlineVariant
//                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = badge.iconRes),
                        contentDescription = badge.level.label,
                        modifier = Modifier
                            .size(70.dp)
                            .graphicsLayer {
                                if (!isUnlocked) {
                                    alpha = 0.35f
                                }
                            },
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Closed/Locked Hexagon Overlay
            if (!isUnlocked) {
                Surface(
                    shape = HexagonShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Badge Level Label
        Surface(
            color = if (isUnlocked) badge.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = badge.level.label,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (isUnlocked) badge.color else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Points Text
        Text(
            text = if (isUnlocked) "$currentPoints pts" else "${badge.minPoints} a-pts",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
