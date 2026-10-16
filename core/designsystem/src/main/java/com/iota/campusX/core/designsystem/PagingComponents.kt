package com.iota.campusX.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

@Composable
fun <T : Any> PagingListFooter(
    items: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    minItemsBeforeEnd: Int = 0,
    loadingContent: @Composable (() -> Unit)? = { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) },
    errorContent: @Composable ((Throwable) -> Unit)? = { error ->
        Text(text = error.localizedMessage ?: "Something went wrong.")
    },
    endContent: @Composable (() -> Unit)? = { Text("🎉 You’ve reached the end!") }
) {
    Box(
        modifier = modifier.fillMaxWidth().height(62.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val append = items.loadState.append) {
            is LoadState.Loading -> loadingContent?.invoke()
            is LoadState.Error -> errorContent?.invoke(append.error)
            is LoadState.NotLoading -> {
                if (append.endOfPaginationReached && items.itemCount > minItemsBeforeEnd) {
                    endContent?.invoke()
                }
            }
        }
    }
}

@Composable
fun <T : Any> PagingListHeader(
    items: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    screenHeight: Dp? = null,
    loadingContent: @Composable (() -> Unit)? = {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    },
    errorContent: @Composable ((String) -> Unit)? = { error ->
        ErrorScreen(text = error, onReTry = { items.retry() }, buttonText = "Retry")
    },
    emptyContent: @Composable (() -> Unit)? = {},
    showContent: @Composable (() -> Unit)? = {},
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        when {
            items.loadState.refresh is LoadState.Loading -> loadingContent?.invoke()
            items.loadState.refresh is LoadState.Error -> {
                val error = (items.loadState.refresh as LoadState.Error).error
                errorContent?.invoke(error.localizedMessage ?: "Something went wrong")
            }
            else -> {
                if (items.itemCount == 0 && items.loadState.append.endOfPaginationReached) {
                    emptyContent?.invoke()
                } else {
                    showContent?.invoke()
                }
            }
        }
    }
}
