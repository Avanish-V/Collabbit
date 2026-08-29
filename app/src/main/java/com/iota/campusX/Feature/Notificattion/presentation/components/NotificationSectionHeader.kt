package com.iota.campusX.Feature.Notificattion.presentation.components

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
fun NotificationSectionHeader(

    group: NotificationDateGroup

){

    val title = when(group){

        NotificationDateGroup.TODAY ->
            "Today"


        NotificationDateGroup.YESTERDAY ->
            "Yesterday"


        NotificationDateGroup.EARLIER ->
            "Earlier"

    }


    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        ),
        modifier = Modifier
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 8.dp
            )
    )

}