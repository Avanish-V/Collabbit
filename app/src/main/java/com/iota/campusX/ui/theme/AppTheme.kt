package com.iota.campusX.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat


data class AppTypography(
    val h5:TextStyle,
    val headingMedium:TextStyle,
    val headingRegular:TextStyle,
    val bodyRegular:TextStyle,
    val bodyMedium:TextStyle,
    val labelMedium:TextStyle,
    val labelRegular:TextStyle,
)

data class AppShape(
    val container:Shape,
    val button:Shape
)


val LocalAppTypography = staticCompositionLocalOf {
    AppTypography(
        h5 = TextStyle.Default,
        headingMedium = TextStyle.Default,
        headingRegular = TextStyle.Default,
        bodyRegular = TextStyle.Default,
        bodyMedium = TextStyle.Default,
        labelMedium = TextStyle.Default,
        labelRegular = TextStyle.Default,
    )
}

 val typography = AppTypography(
    h5 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    headingMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
    ),
    headingRegular = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),

    bodyRegular = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelRegular = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)

private val shape = AppShape(
    container = RoundedCornerShape(12.dp),
    button = RoundedCornerShape(12.dp)
)
val LocalAppShape = staticCompositionLocalOf {
    AppShape(
        container = RectangleShape,
        button = RectangleShape
    )
}