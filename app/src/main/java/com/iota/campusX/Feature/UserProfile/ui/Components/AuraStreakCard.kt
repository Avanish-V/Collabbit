package com.iota.campusX.Feature.UserProfile.ui.Components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraInfoResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.AuraLevelResponse

// ── Level style metadata ──────────────────────────────────────────────────────

private data class LevelStyle(
    val gradient: List<Color>,
    val accentColor: Color,
    val emoji: String
)

private fun levelStyle(level: AuraLevelResponse): LevelStyle = when (level) {
    AuraLevelResponse.NEWCOMER  -> LevelStyle(listOf(Color(0xFF9E9E9E), Color(0xFF757575)), Color(0xFF9E9E9E), "🌱")
    AuraLevelResponse.SPARK     -> LevelStyle(listOf(Color(0xFF42A5F5), Color(0xFF1E88E5)), Color(0xFF42A5F5), "✨")
    AuraLevelResponse.RISING    -> LevelStyle(listOf(Color(0xFF66BB6A), Color(0xFF43A047)), Color(0xFF66BB6A), "⚡")
    AuraLevelResponse.GLOWING   -> LevelStyle(listOf(Color(0xFFFFCA28), Color(0xFFFFB300)), Color(0xFFFFCA28), "🔥")
    AuraLevelResponse.RADIANT   -> LevelStyle(listOf(Color(0xFFFF7043), Color(0xFFE64A19)), Color(0xFFFF7043), "💫")
    AuraLevelResponse.BLAZING   -> LevelStyle(listOf(Color(0xFFAB47BC), Color(0xFF8E24AA)), Color(0xFFAB47BC), "🌟")
    AuraLevelResponse.LEGENDARY -> LevelStyle(listOf(Color(0xFFFFD700), Color(0xFFFF8C00)), Color(0xFFFFD700), "👑")
}

private fun nextLevelThreshold(level: AuraLevelResponse): Int? {
    val entries = AuraLevelResponse.entries
    val idx = entries.indexOf(level)
    return if (idx < entries.lastIndex) entries[idx + 1].minPoints else null
}

private fun nextLevelLabel(level: AuraLevelResponse): String {
    val entries = AuraLevelResponse.entries
    val idx = entries.indexOf(level)
    return if (idx < entries.lastIndex) entries[idx + 1].label else level.label
}

// ── Public composable ─────────────────────────────────────────────────────────

/**
 * Displays the user's current aura points, level badge, and progress
 * toward the next level. No streak or check-in button — points are
 * awarded automatically by the backend on meaningful actions.
 *
 * @param aura     Current aura state from the profile or award response.
 * @param modifier Optional modifier.
 */
@Composable
fun AuraStreakCard(
    aura: AuraInfoResponse,
    modifier: Modifier = Modifier
) {
    Column (
        modifier = modifier,
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
