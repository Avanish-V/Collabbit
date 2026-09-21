package com.iota.campusX.Feature.Opportunities.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Opportunities.data.model.OpportunityResponse
import com.iota.campusX.R
import com.iota.campusX.ui.UIComponents.cardShadow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OpportunityCard(
    opportunity: OpportunityResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).cardShadow(alpha = 0.5f),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Section: Logo, Title, Tag, Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (opportunity.companyLogoUrl != null) {
                            AsyncImage(
                                model = opportunity.companyLogoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().padding(8.dp).clip(RoundedCornerShape(6.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.briefcase),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = opportunity.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = opportunity.displayCompanyName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF3B82F6) // Blue verified check
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.land_layer_location),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF888888)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = opportunity.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF888888)
                        )
                        VerticalDivider(modifier = Modifier.height(18.dp).padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.outline)
                        Icon(
                            painter = painterResource(R.drawable.briefcase),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF888888)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hybrid", // Placeholder or dynamic if available
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF5F5F5))
            Spacer(modifier = Modifier.height(16.dp))

            // Details Row: Apply by, Stipend, Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OpportunityDetailItem(
                    icon = R.drawable.calendar,
                    label = "Apply by",
                    value = opportunity.deadline ?: "--:--"
                )
                OpportunityDetailItem(
                    icon = R.drawable.indian_rupee_sign,
                    label = "Stipend",
                    value = "${opportunity.stipend}/Month"
                )
                OpportunityDetailItem(
                    icon = R.drawable.clock,
                    label = "Duration",
                    value = if (opportunity.durationMonths != null) "${opportunity.durationMonths} Months" else "--:--"
                )
            }

            if (opportunity.skills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3,
                    maxLines = 1
                ) {
                    opportunity.skills.take(3).forEach { skill ->
                        SkillChipLight(text = skill)
                    }
                }
            }
        }
    }
}

@Composable
fun OpportunityDetailItem(icon: Int, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(32.dp).border(width = 0.5.dp, shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.outline),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFF666666)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF888888)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun SkillChipLight(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1976D2)
            )
        )
    }
}
