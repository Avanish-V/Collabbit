package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Extension function to apply a soft, diffused card shadow.
 * In dark mode, shadows are subtle or transparent to match Material 3 elevation.
 */
fun Modifier.cardShadow(
    elevation: Dp = 12.dp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    alpha: Float = 0.1f
): Modifier = composed {
    val isDark = isSystemInDarkTheme()
    val shadowAlpha = if (isDark) alpha * 0.2f else alpha
    
    this.shadow(
        elevation = if (isDark) elevation / 2 else elevation,
        shape = shape,
        clip = false,
        ambientColor = Color.Black.copy(alpha = shadowAlpha),
        spotColor = Color.Black.copy(alpha = shadowAlpha)
    )
}
