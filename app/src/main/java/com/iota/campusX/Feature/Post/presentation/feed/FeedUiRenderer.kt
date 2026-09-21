package com.iota.campusX.Feature.Post.presentation.feed

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.presentation.components.FeedItem
import com.iota.campusX.Feature.Post.presentation.components.FeedShimmerItem
import com.iota.campusX.Feature.Post.presentation.components.PostAction
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.PagingListFooter
import com.iota.campusX.Screens.Home.PagingListHeader
import com.iota.campusX.Screens.Home.RefreshBox
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.vibrate
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedUiRenderer(
    feed: LazyPagingItems<Post>,
    lazyState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    feedViewModel: PostFeedViewModel,
    onMoreClick: (Post) -> Unit = {},
    onReplyClick: (Post) -> Unit = {},
    onShareClick: (Post) -> Unit = {},
    onImageClick: (Post, Int) -> Unit = { _, _ -> },
    onProfileClick: (String) -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusedPost by feedViewModel.focusedPost.collectAsState()

    val itemHandlers: (PostAction) -> Unit = { action ->
        when (action) {
            is PostAction.ViewPostVisualContent -> {
                onImageClick(action.post, action.initialIndex)
            }
            is PostAction.Share -> {
                onShareClick(action.post)
            }
            else -> {
                feedViewModel.onPostEvent(event = action)
            }
        }
    }

    // Handle initial scroll/pointing to focused post
    LaunchedEffect(focusedPost, feed.loadState.refresh) {
        if (feed.loadState.refresh is LoadState.NotLoading) {
            focusedPost?.let { focused ->
                val index = feed.itemSnapshotList.indexOfFirst { it?.postId == focused.postId }
                if (index != -1) {
                    lazyState.animateScrollToItem(index)
                    // Once pointed, we can clear it if we want to avoid re-scrolling
                    // but keeping it helps in the "show at top if not in list" logic.
                } else if (feed.itemCount > 0) {
                    lazyState.animateScrollToItem(0)
                }
            }
        }
    }

    RefreshBox(
        pullToRefreshState = pullToRefreshState,
        onRefresh = {
            context.vibrate()
            feed.refresh()
            scope.launch { lazyState.animateScrollToItem(0) }
        },
        isRefreshing = feed.loadState.refresh is LoadState.Loading
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            PagingListHeader(
                items = feed,
                loadingContent = {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(5) {
                            FeedShimmerItem()
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                emptyContent = {
                    StatusScreen(
                        modifier = Modifier.fillMaxSize(),
                        text = "No Posts Yet",
                        description = "Share your thoughts or updates to let the world\nknow more about you!",
                        buttonText = "Create Post",
                        onClick = {}
                    )
                },
                showContent = {
                    LazyColumn(
                        state = lazyState,
                        modifier = Modifier
                            .testTag("feed_list")
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection),
                    ) {
                        focusedPost?.let { focused ->
                            val isAlreadyInList = feed.itemSnapshotList.any { it?.postId == focused.postId }
                            if (!isAlreadyInList) {
                                item(key = focused.postId) {
                                    FeedItem(
                                        feedItem = focused,
                                        handlers = itemHandlers,
                                        onMoreClick = onMoreClick,
                                        onReplyClick = onReplyClick,
                                        onProfileClick = onProfileClick
                                    )
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        items(
                            count = feed.itemCount,
                            key = { index -> feed.peek(index)?.postId ?: index }
                        ) { index ->
                            val item = feed[index]
                            item?.let {
                                FeedItem(
                                    feedItem = it,
                                    handlers = itemHandlers,
                                    onMoreClick = onMoreClick,
                                    onReplyClick = onReplyClick,
                                    onProfileClick = { authorId ->
                                        onProfileClick(authorId)
                                    }
                                )
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        item {
                            PagingListFooter(
                                items = feed,
                                minItemsBeforeEnd = 5,
                                errorContent = {}
                            )
                        }
                    }
                }
            )
        }
    }
}
