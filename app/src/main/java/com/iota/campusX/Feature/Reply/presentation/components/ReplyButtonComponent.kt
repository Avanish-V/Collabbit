package com.iota.campusX.Feature.Reply.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.iota.campusX.R

@Composable
fun ReplyButtonComponent(
    replyCount: String, 
    onReplyClick: () -> Unit, 
    enableText: Boolean = false,
    tint: Color? = null
) {
    val contentColor = tint ?: MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onReplyClick
        )
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(R.drawable.message_circle),
            contentDescription = "Replies",
            tint = contentColor
        )
        
        Spacer(modifier = Modifier.width(6.dp))
        
        Text(
            text = replyCount, 
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
        
        if (enableText) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Reply",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
