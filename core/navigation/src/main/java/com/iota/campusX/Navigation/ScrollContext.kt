package com.iota.campusX.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

@Composable
fun rememberScrollContext(navigationViewModel: NavigationViewModel): NestedScrollConnection {
    return remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -10f) { // Scrolling down
                    navigationViewModel.isBottomBarVisible(false)
                } else if (delta > 10f) { // Scrolling up
                    navigationViewModel.isBottomBarVisible(true)
                }
                return Offset.Zero
            }
        }
    }
}
