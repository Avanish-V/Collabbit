package com.iota.campusX.Feature.Notificattion.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun NotificationTopBar(
    unreadCount: Long,
    onMarkAllRead: () -> Unit,
    modifier: Modifier = Modifier
) {

    TopAppBar(

        modifier = modifier,

        title = {

            NotificationTitle(
                unreadCount
            )

        },

        actions = {

            NotificationActions(

                unreadCount,

                onMarkAllRead

            )

        }

    )

}

@Composable
fun NotificationTitle(

    unreadCount: Long

) {

    Row(

        verticalAlignment = Alignment.CenterVertically

    ) {



        if (unreadCount > 0) {

            Spacer(

                Modifier.width(8.dp)

            )

            Badge(

                containerColor =
                    MaterialTheme.colorScheme.primary

            ) {

                Text(

                    unreadCount.toString()

                )

            }

        }

    }

}

@Composable
fun NotificationActions(

    unreadCount: Long,

    onMarkAllRead: () -> Unit

) {

    if (unreadCount > 0) {

        TextButton(

            onClick = onMarkAllRead

        ) {

            Text(

                "Read all"

            )

        }

    }

}