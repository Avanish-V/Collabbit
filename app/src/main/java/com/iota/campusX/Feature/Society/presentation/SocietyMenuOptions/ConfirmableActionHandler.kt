package com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iota.campusX.Utils.CircularLoading
import com.iota.campusX.Utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocietyConfirmationHandler(
    content: SocietyData,
    action: SocietyMenuOptions,
    viewModel: SocietyOptionsViewModel,
    onDismiss: () -> Unit,

) {

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    when (action) {
        SocietyMenuOptions.Delete -> {
            val actionResult by viewModel.actionResult.collectAsState()
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Delete Society") },
                text = { Text("Are you sure you want to delete this Society?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onActionSelected(action, content)
                        onDismiss()
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                },
                shape = MaterialTheme.shapes.small,
                containerColor = MaterialTheme.colorScheme.background
            )
        }
        SocietyMenuOptions.Notify -> {
            val subscriptionState by viewModel.hasSubscribed.collectAsState()
            val actionResult by viewModel.actionResult.collectAsState()

            // Current subscription state (null until loaded)
            val isSubscribed = (subscriptionState as? UiState.Success<Boolean>)?.data ?: false

            // Trigger subscription check only when roomId changes
            LaunchedEffect(content.roomId) {
                viewModel.hasSubscribed(content.roomId)
            }

            // Refresh subscription after successful action
            LaunchedEffect(actionResult) {
                if (actionResult is UiState.Success) {
                    viewModel.hasSubscribed(content.roomId)
                }
            }

            ModalBottomSheet(
                onDismissRequest = {
                    onDismiss()
                    viewModel.showMenu(false)
                },
                shape = MaterialTheme.shapes.small,
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Icon box
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = action.icon),
                            contentDescription = "Notification",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Title & description
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Notification",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Get notified when this room goes live",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Actions
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.onActionSelected(action, content, isSubscribed)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            when {
                                subscriptionState is UiState.Loading  -> {
                                    CircularLoading(Color.White)
                                }
                                else -> {
                                    val buttonText = if (isSubscribed) {
                                        "🔕 Cancel notification"
                                    } else {
                                        "🔔 Notify me"
                                    }
                                    Text(text = buttonText)
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                onDismiss()
                                viewModel.showMenu(false)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Cancel")
                        }
                    }
                }
            }
        }


        else -> {}
    }
}

