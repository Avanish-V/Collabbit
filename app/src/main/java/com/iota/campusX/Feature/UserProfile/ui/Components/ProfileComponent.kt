package com.iota.campusX.Feature.UserProfile.ui.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iota.campusX.ui.UIComponents.EditProfileIconButton

@Composable
fun ProfileComponent(
    title: String,
    onEditClick: () -> Unit = {},
    body: @Composable () -> Unit,
    contentDescription: String,
    editIconVisible: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header row (title + edit button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            if (editIconVisible){
                EditProfileIconButton {
                    onEditClick.invoke()
                }
            }

        }

        Spacer(modifier = Modifier.height(8.dp))

        body()

    }
}