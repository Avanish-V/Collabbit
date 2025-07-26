package com.iota.campusX.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LightTheme_Blue,
    onPrimary = Color.White,
    background = DarkTheme_Black,
    onBackground = White,
    surface = DarkTheme_LightBlack,
    onSurface = White,
    onSurfaceVariant = DarkTheme_Gray,
    outline = DarkTheme_LightBlack,

)

private val LightColorScheme = lightColorScheme(
    primary = LightTheme_Blue,
    onPrimary = Color.White,
    background = LightTheme_White,
    onBackground = LightTheme_Black,
    surface = LightTheme_LightGray,
    onSurface = LightTheme_Black,
    onSurfaceVariant = LightTheme_DarkGray,
    outline = LightTheme_Gray,

)

@Composable
fun CampusXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val window = (context as Activity).window

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply status bar color
    SideEffect {
        window.statusBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
