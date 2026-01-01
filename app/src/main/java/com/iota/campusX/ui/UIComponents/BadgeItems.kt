package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun BadgeItem(
    modifier: Modifier = Modifier,
    itemCount: Int,
    onBadgeClick:()-> Unit,
    badgeIcon: Int
) {

    BadgedBox(
        badge = {
            if (itemCount != 0) {
                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                    Text(
                        text = itemCount.toString(),
                    )
                }
            }
        }
    ) {
        IconButton(
            onClick = {
                onBadgeClick.invoke()
            },
        ) {
            Icon(
                modifier = modifier,
                painter = painterResource(badgeIcon),
                contentDescription = "Message",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}