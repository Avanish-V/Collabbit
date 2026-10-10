package com.iota.campusX.Feature.Collab.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iota.campusX.Feature.Collab.data.model.ProjectStatus

data class ProjectStatusUiConfig(
    val displayName: String,
    val badgeBg: Color,
    val textColor: Color,
    val dotColor: Color,
    val description: String
)

fun getProjectStatusUi(status: ProjectStatus): ProjectStatusUiConfig {
    return when (status) {
        ProjectStatus.LOOKING -> ProjectStatusUiConfig(
            displayName = "Looking",
            badgeBg = Color(0xFFEDE9FE),
            textColor = Color(0xFF4F46E5),
            dotColor = Color(0xFF6366F1),
            description = "Actively looking for teammates & contributors"
        )
        ProjectStatus.DEVELOPING -> ProjectStatusUiConfig(
            displayName = "Developing",
            badgeBg = Color(0xFFFEF3C7),
            textColor = Color(0xFFD97706),
            dotColor = Color(0xFFF59E0B),
            description = "In active development and building"
        )
        ProjectStatus.ONGOING -> ProjectStatusUiConfig(
            displayName = "Ongoing",
            badgeBg = Color(0xFFE0F2FE),
            textColor = Color(0xFF0284C7),
            dotColor = Color(0xFF0EA5E9),
            description = "Project is active and progressing smoothly"
        )
        ProjectStatus.LIVE -> ProjectStatusUiConfig(
            displayName = "Live",
            badgeBg = Color(0xFFDCFCE7),
            textColor = Color(0xFF16A34A),
            dotColor = Color(0xFF22C55E),
            description = "Project has been launched and is live"
        )
    }
}

@Composable
fun ProjectStatusBadge(
    status: ProjectStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val config = getProjectStatusUi(status)
    Surface(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ),
        color = config.badgeBg,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(config.dotColor)
            )
            Text(
                text = config.displayName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    color = config.textColor
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeProjectStatusBottomSheet(
    currentStatus: ProjectStatus,
    onDismiss: () -> Unit,
    onStatusSelected: (ProjectStatus) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Update Project Status",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Let teammates and viewers know the project stage",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.5.sp
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Status Options
            ProjectStatus.entries.forEach { statusOption ->
                val isSelected = statusOption == currentStatus
                val config = getProjectStatusUi(statusOption)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onStatusSelected(statusOption)
                            onDismiss()
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) config.badgeBg.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) config.textColor else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(12.dp),
                            shape = CircleShape,
                            color = config.dotColor
                        ) {}

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = config.displayName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isSelected) config.textColor else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                ProjectStatusBadge(status = statusOption)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = config.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.5.sp
                                )
                            )
                        }

                        if (isSelected) {
                            Surface(
                                modifier = Modifier.size(26.dp),
                                shape = CircleShape,
                                color = config.textColor
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
