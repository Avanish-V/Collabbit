package com.iota.campusX.Feature.UserProfile.ui.Components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraInfoResponse

/**
 * Displays the user's current aura points in a large animated card as per the old design.
 */
@Composable
fun AuraStreakCard(
    aura: AuraInfoResponse,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column (
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        AnimatedContent(
            targetState = aura.auraPoints,
            transitionSpec = {
                (fadeIn(tween(300)) + scaleIn(initialScale = 0.85f)) togetherWith
                        fadeOut(tween(200))
            },
            label = "pointsCounter"
        ) { pts ->
            Text(
                text = "$pts ✦",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        Text(
            modifier = Modifier
                .border(width = 0.5.dp, shape = CircleShape, color = MaterialTheme.colorScheme.onSurfaceVariant)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            text = "Aura Points",
            style = MaterialTheme.typography.bodyMedium,
        )

    }
}
