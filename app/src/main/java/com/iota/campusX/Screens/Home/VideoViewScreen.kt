package com.iota.campusX.Screens.Home

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import com.iota.campusX.Navigation.VideoView
import com.iota.campusX.ui.UIComponents.VideoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoViewScreen(
    navHostController: NavHostController
) {
    val navEntry = remember(navHostController) {
        navHostController.currentBackStackEntry
    }
    val routeArgs = navEntry?.toRoute<VideoView>()
    val videoUrl = routeArgs?.videoUrl
    val thumbnailUrl = routeArgs?.thumbnailUrl

    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // Allow drawing into the notch area
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            
            onDispose {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (videoUrl != null) {
            VideoPlayer(
                videoUrl = videoUrl,
                thumbnailUrl = thumbnailUrl,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Video not found", color = Color.White)
            }
        }

        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(
                    onClick = { navHostController.popBackStack() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            ),
            windowInsets = WindowInsets(0, 0, 0, 0)
        )
    }
}
