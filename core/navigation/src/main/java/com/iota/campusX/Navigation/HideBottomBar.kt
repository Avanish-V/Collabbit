package com.iota.campusX.Navigation

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlin.math.abs

@Composable
fun HideBottomBar(
    navigationViewModel: NavigationViewModel,
    lazyState: LazyListState,
) {
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(lazyState) {
        snapshotFlow { lazyState.firstVisibleItemIndex to lazyState.firstVisibleItemScrollOffset }
            .collect { (currentIndex, currentOffset) ->
                val scrollingDown = if (currentIndex > previousIndex) {
                    true
                } else if (currentIndex < previousIndex) {
                    false
                } else {
                    currentOffset > previousScrollOffset
                }

                val delta = if (currentIndex == previousIndex) {
                    abs(currentOffset - previousScrollOffset)
                } else 300

                if (delta > 20) {
                    navigationViewModel.isBottomBarVisible(!scrollingDown)
                }

                previousIndex = currentIndex
                previousScrollOffset = currentOffset
            }
    }
}
