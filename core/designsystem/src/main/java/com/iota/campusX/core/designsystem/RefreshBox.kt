package com.iota.campusX.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshBox(
    modifier: Modifier = Modifier,
    pullToRefreshState: PullToRefreshState,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pullToRefresh(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                enabled = enabled,
                onRefresh = onRefresh
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        content()
        Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            color = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.onPrimary
        )
    }
}
