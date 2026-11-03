package com.iota.campusX.Feature.Notificattion.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iota.campusX.Feature.Notificattion.presentation.utils.NotificationDateGroup

@Composable
fun NotificationSectionHeader(group: NotificationDateGroup) {
    val title = when (group) {
        NotificationDateGroup.TODAY     -> "Today"
        NotificationDateGroup.YESTERDAY -> "Yesterday"
        NotificationDateGroup.EARLIER   -> "Earlier"
    }

    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 20.dp,
                bottom = 6.dp
            )
    )
}