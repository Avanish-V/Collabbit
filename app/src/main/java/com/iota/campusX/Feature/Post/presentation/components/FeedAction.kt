package com.iota.campusX.Feature.Post.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iota.campusX.R
import com.iota.campusX.ui.UIComponents.FeedUI.AnimatedLikeButton

@Composable
fun FeedAction(
    likesCount: Int,
    isLiked: Boolean,
    onLikeClick: (Boolean) -> Unit,
    onMoreVertClick: () -> Unit,
    otherActionContent: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, end = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {



            AnimatedLikeButton(
                onLike = { onLikeClick.invoke(it) },
                likesCount = likesCount,
                isLiked = isLiked
            )


            otherActionContent.invoke()
        }

        Icon(
            modifier = Modifier
                .size(20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onMoreVertClick.invoke() }
                ),
            painter = painterResource(R.drawable.baseline_more_vert_24),
            contentDescription = "More options",
            tint = MaterialTheme.colorScheme.outline
        )
    }
}
