package com.iota.campusX.ui.UIComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BadgeItem(
    modifier: Modifier = Modifier,
    itemCount: Int,
    onBadgeClick: () -> Unit,
    badgeIcon: Int,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String? = null
) {
    BadgedBox(
        badge = {
            AnimatedVisibility(
                visible = itemCount > 0,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                Badge(
                    containerColor = badgeColor,
                    contentColor = Color.White,
                    modifier = Modifier.padding(top = 2.dp, end = 2.dp)
                ) {
                    Text(
                        text = if (itemCount > 99) "99+" else itemCount.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    ) {
        IconButton(
            onClick = { onBadgeClick.invoke() }
        ) {
            Icon(
                modifier = modifier.size(20.dp),
                painter = painterResource(badgeIcon),
                contentDescription = contentDescription ?: "Action",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}