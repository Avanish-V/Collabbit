package com.iota.campusX.Feature.Notificattion.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.iota.campusX.Feature.Notificattion.presentation.model.NotificationUi
import com.iotabuild.campuscircle.Notification.entity.NotificationType
import com.iota.campusX.R
import com.iota.campusX.Utils.getTimeAgo

@Composable
fun NotificationCard(
    notification: NotificationUi,
    modifier: Modifier = Modifier,
    onClick: (NotificationUi) -> Unit
) {
    val unreadColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(notification) },
        color = if (notification.isRead) MaterialTheme.colorScheme.background else unreadColor
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            NotificationAvatar(
                profileUrl = notification.senderProfileUrl,
                notificationType = notification.type
            )

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Sender Name and Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.senderName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.2).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = getTimeAgo(notification.createdAt),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Notification Content
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                        color = if (notification.isRead) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, top = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun NotificationAvatar(
    profileUrl: String?,
    notificationType: NotificationType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(52.dp)
    ) {
        AsyncImage(
            model = profileUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
        )

        // Type Badge
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 4.dp, y = 4.dp)
                .size(22.dp),
            shape = CircleShape,
            color = getNotificationColor(notificationType),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.background),
            tonalElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(getNotificationIcon(notificationType)),
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun getNotificationIcon(type: NotificationType): Int {
    return when (type) {
        NotificationType.LIKE -> R.drawable.heart_partner_handshake // Or a heart icon if available
        NotificationType.COMMENT -> R.drawable.message_circle
        NotificationType.REPLY -> R.drawable.baseline_done_all_24
        NotificationType.FOLLOW -> R.drawable.user // user icon
        NotificationType.MESSAGE -> R.drawable.messages
        NotificationType.MENTION -> R.drawable.user // user-at icon if available
        else -> R.drawable.bell
    }
}

@Composable
private fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.LIKE -> Color(0xFFF43F5E) // Rose 500
        NotificationType.COMMENT -> Color(0xFF0EA5E9) // Blue 500
        NotificationType.FOLLOW -> Color(0xFF8B5CF6) // Violet 500
        NotificationType.MESSAGE -> Color(0xFF10B981) // Emerald 500
        NotificationType.REPLY -> Color(0xFFF59E0B) // Amber 500
        else -> MaterialTheme.colorScheme.primary
    }
}
