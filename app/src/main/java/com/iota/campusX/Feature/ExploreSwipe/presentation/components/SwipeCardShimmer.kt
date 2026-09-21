package com.iota.campusX.Feature.ExploreSwipe.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.iota.campusX.ui.UIComponents.shimmerEffect

@Composable
fun SwipeCardShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp)
        ) {
            // Header Row: Type banner + Match score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Pill
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .width(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )

                // Match Score Pill
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .width(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description lines
            repeat(3) {
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Box(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tag Header
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(80.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tags Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(60.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .shimmerEffect()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Metadata row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(110.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shimmerEffect()
                )
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(90.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shimmerEffect()
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .shimmerEffect()
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .height(14.dp)
                                .width(80.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .width(60.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                    }
                }

                // Action Link
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmerEffect()
                )
            }
        }
    }
}
