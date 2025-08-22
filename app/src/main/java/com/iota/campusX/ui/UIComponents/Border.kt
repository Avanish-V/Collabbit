package com.iota.campusX.ui.UIComponents

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.ashedBorder(modifier: Modifier = Modifier) {

    val color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)

    drawBehind(
        onDraw = {

            val strokeWidth = 1.dp.toPx()
            val dashLength = 6.dp.toPx()
            val gapLength = 4.dp.toPx()

            drawRoundRect(
                color = color,
                size = size,
                cornerRadius = CornerRadius(8.dp.toPx()),
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(dashLength, gapLength), 0f
                    )
                )
            )
        }
    )
}