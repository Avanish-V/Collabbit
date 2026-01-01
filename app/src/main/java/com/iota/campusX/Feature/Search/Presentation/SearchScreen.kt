package com.voxcii.voxcii.Screens.SearchFlow

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Search.Data.SearchResponse
import com.iota.campusX.Feature.Search.Presentation.SearchViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.PagingListHeader
import com.iota.campusX.Utils.LoadingScreen
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.FeedUI.Avatar
import com.iota.campusX.ui.UIComponents.FeedUI.FeedItem
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class,)
@Composable
fun SearchScreen(navHostController: NavHostController) {

    val searchViewModel = koinViewModel<SearchViewModel>()

    val searchResults = searchViewModel.searchResults.collectAsLazyPagingItems()
    val postSearchResults = searchViewModel.postSearchResults.collectAsLazyPagingItems()

    var query = searchViewModel.searchQuery.collectAsStateWithLifecycle()

    val snackBar = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedTabIndex) {
        searchViewModel.onTabChanged(selectedTabIndex)
    }

    Scaffold(
        topBar = {
            TextField(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    ),
                value = query.value,
                onValueChange = {
                    searchViewModel.onSearchQuery(it)
                },
                placeholder = {
                    Text(
                        text = "Search...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                textStyle = TextStyle(
                    fontWeight = FontWeight.Bold,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(R.drawable.search_normal),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                trailingIcon = {
                    if (query.value.isNotEmpty()){
                        IconButton(modifier = Modifier.padding(end = 10.dp), onClick = {
                            searchViewModel.onSearchQuery("")
                        }
                        ) {
                            Icon(
                                modifier = Modifier.size(22.dp),
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        searchViewModel.onSearchButtonClick()
                    }
                ),
                shape = RoundedCornerShape(32.dp),
                maxLines = 1
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBar)
        },
        bottomBar = {}
    ) {innerPadding->

        Column(modifier = Modifier.padding(innerPadding)) {

            SearchTabs(selectedTabIndex = selectedTabIndex) {tab->
                scope.launch {
                    pagerState.animateScrollToPage(tab)
                }
            }

            HorizontalPager(state = pagerState, overscrollEffect = null) { page ->
                selectedTabIndex = page
                when (page) {
                    0 -> UserSearchList(searchResults, query.value, navHostController)
                    1 -> PostSearchList(postSearchResults, query.value, navHostController)
                }
            }
        }
    }
}

@Composable
fun MentorSingleCard(user: SearchResponse, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(0.dp),
        onClick={onClick.invoke()},
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                imageUrl = user.image,
                visibilityMode = VisibilityMode.USER
            ) { }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = user.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium

                )
                if ( user.tagline.isNotEmpty()){
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = user.tagline,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


@Composable
fun SearchTabs(
    modifier: Modifier = Modifier,
    tabs: List<String> = listOf("Users", "Posts"),
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = Color.Transparent,
        edgePadding = 0.dp,
        contentColor = MaterialTheme.colorScheme.onBackground,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier
                    .tabIndicatorOffset(tabPositions[selectedTabIndex])
                    .height(3.dp)
                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun PostSearchList(
    posts: LazyPagingItems<GetPostDTO>,
    query: String,
    navController: NavHostController
) {
    when {
        query.isBlank() -> {IdleScreen("Search for posts")}
        posts.loadState.refresh is LoadState.Loading -> LoadingScreen()
        posts.itemCount == 0 -> StatusScreen(text = "No posts found", image = R.drawable.undraw_no_data_ig65)
        else -> {
            LazyColumn{
                items(posts.itemCount){item->
                    val data = posts[item]
                    data?.let {
                        FeedItem(
                            feedItem = it,
                            handlers = {

                            },
                            onDotMenuClick = {

                            },
                            enableFeedMode = false
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchList(
    users: LazyPagingItems<SearchResponse>,
    query: String,
    navController: NavHostController
) {
    when {
        query.isBlank() -> {IdleScreen("Search for users")}
        users.loadState.refresh is LoadState.Loading -> LoadingScreen()
        users.itemCount == 0 -> StatusScreen(text = "No users found", image = R.drawable.undraw_no_data_ig65)
        else -> {
            LazyColumn{
                items(users.itemCount){item->
                    val data = users[item]
                    data?.let {
                        MentorSingleCard(
                            user = it,
                            onClick = {
                                navController.navigate(Routes.Main.ProfileByID.routes).apply {
                                    navController.currentBackStackEntry?.savedStateHandle?.set("USER_ID",it.uid)
                                }
                            }
                        )
                    }
                    Divider()

                }
            }
        }
    }
}

@Composable
fun IdleScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
