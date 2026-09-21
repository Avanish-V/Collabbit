package com.iota.campusX.Feature.Notificattion.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Notificattion.presentation.model.NotificationUi
import com.iotabuild.campuscircle.Notification.entity.NotificationType
import com.iota.campusX.R
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.theme.*

@Composable
fun NotificationCard(
    notification: NotificationUi,
    modifier: Modifier = Modifier,
    onAccept: (NotificationUi) -> Unit = {},
    onReject: (NotificationUi) -> Unit = {},
    onMessage: (NotificationUi) -> Unit = {},
    onClick: (NotificationUi) -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (notification.isRead)
            MaterialTheme.colorScheme.background
        else
            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
        animationSpec = tween(300),
        label = "notifBg"
    )

    val typeColor = getNotificationTypeColor(notification.type)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(notification) },
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar with type badge
            NotificationAvatar(
                profileUrl = notification.senderProfileUrl,
                notificationType = notification.type,
                senderName = notification.senderName,
                upvoters = notification.upvoters
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = notification.senderName,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.3).sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Type pill badge
                        Surface(
                            color = typeColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = getNotificationLabel(notification.type),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = typeColor
                                ),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Timestamp
                    Text(
                        text = getTimeAgo(notification.createdAt),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(Modifier.height(5.dp))

                // Body message
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 19.sp,
                        color = if (notification.isRead)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!notification.note.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${notification.note}\"",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (notification.type == NotificationType.CONNECT_REQUEST) {
                    Spacer(Modifier.height(12.dp))
                    if (notification.isActionDone) {
                        Button(
                            onClick = { onMessage(notification) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.messages),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Send Message",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onAccept(notification) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "Accept",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            OutlinedButton(
                                onClick = { onReject(notification) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "Reject",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Unread dot indicator or Post Thumbnail
            if (!notification.postThumbnail.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(12.dp))
                AsyncImage(
                    model = notification.postThumbnail,
                    contentDescription = "Post preview",
                    modifier = Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp, top = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(typeColor, typeColor.copy(alpha = 0.6f))
                            )
                        )
                )
            } else {
                Spacer(modifier = Modifier.width(18.dp))
            }
        }
    }
}

@Composable
fun NotificationAvatar(
    profileUrl: String?,
    notificationType: NotificationType,
    senderName: String,
    upvoters: List<com.iota.campusX.Feature.Notificattion.domain.model.Upvoter> = emptyList(),
    modifier: Modifier = Modifier
) {
    val typeColor = getNotificationTypeColor(notificationType)
    
    Box(modifier = modifier.size(50.dp)) {
        if (notificationType == NotificationType.LIKE && upvoters.size > 1) {
            // Overlapping avatars for multiple likes
            val displayUpvoters = upvoters.take(2)
            displayUpvoters.forEachIndexed { index, upvoter ->
                AvatarItem(
                    profileUrl = upvoter.image,
                    name = upvoter.name,
                    color = typeColor,
                    modifier = Modifier
                        .size(38.dp)
                        .offset(x = (index * 12).dp, y = (index * 12).dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                        .zIndex(1f - index)
                )
            }
        } else {
            AvatarItem(
                profileUrl = profileUrl,
                name = senderName,
                color = typeColor,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Notification type badge (bottom-right)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 3.dp, y = 3.dp)
                .size(20.dp)
                .zIndex(2f),
            shape = CircleShape,
            color = typeColor,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.background)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(getNotificationIconRes(notificationType)),
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun AvatarItem(
    profileUrl: String?,
    name: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val initials = name.trim().split(" ")
        .take(2)
        .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }

    if (!profileUrl.isNullOrBlank()) {
        AsyncImage(
            model = profileUrl,
            contentDescription = null,
            modifier = modifier.clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
        )
    } else {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
            color = color.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initials.ifBlank { "?" },
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = color
                    )
                )
            }
        }
    }
}

private fun getNotificationIconRes(type: NotificationType): Int = when (type) {
    NotificationType.LIKE                  -> R.drawable.up
    NotificationType.COMMENT               -> R.drawable.message_circle
    NotificationType.REPLY                 -> R.drawable.baseline_done_all_24
    NotificationType.FOLLOW                -> R.drawable.user
    NotificationType.MESSAGE               -> R.drawable.messages
    NotificationType.MENTION               -> R.drawable.user
    NotificationType.COLLABORATION_REQUEST -> R.drawable.heart_partner_handshake
    NotificationType.CONNECT_REQUEST -> R.drawable.user
    else                                   -> R.drawable.bell
}

private fun getNotificationLabel(type: NotificationType): String = when (type) {
    NotificationType.LIKE                  -> "Upvoted"
    NotificationType.COMMENT               -> "Commented"
    NotificationType.REPLY                 -> "Replied"
    NotificationType.FOLLOW                -> "Followed"
    NotificationType.MESSAGE               -> "Message"
    NotificationType.MENTION               -> "Mention"
    NotificationType.COLLABORATION_REQUEST -> "Collab Request"
    NotificationType.CONNECT_REQUEST -> "Connect"
    else                                   -> "Notification"
}

fun getNotificationTypeColor(type: NotificationType): Color = when (type) {
    NotificationType.LIKE                  -> brandRose
    NotificationType.COMMENT               -> brandBlue
    NotificationType.FOLLOW                -> brandViolet
    NotificationType.MESSAGE               -> brandEmerald
    NotificationType.REPLY                 -> brandAmber
    NotificationType.MENTION               -> brandPink
    NotificationType.COLLABORATION_REQUEST -> brandIndigo
    NotificationType.CONNECT_REQUEST       -> brandBlue
    else                                   -> brandIndigo
}
