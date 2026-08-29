package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Extension function to apply a soft, diffused card shadow.
 */
fun Modifier.cardShadow(
    elevation: Dp = 12.dp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    alpha: Float = 0.1f
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    clip = false,
    ambientColor = Color.Black.copy(alpha = alpha),
    spotColor = Color.Black.copy(alpha = alpha)
)
