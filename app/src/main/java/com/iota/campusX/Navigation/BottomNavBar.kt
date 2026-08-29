package com.iota.campusX.Navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.iota.campusX.Feature.UserProfile.ui.screens.ProfileMain.UserProfileViewModel
import com.iota.campusX.ui.theme.collabIndigo
import kotlin.reflect.KClass


@Composable
fun BottomAppBar(
    navController: NavHostController,
    profileViewModel: UserProfileViewModel
) {

    val profileState = profileViewModel.uiState.collectAsState().value


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val destination = navBackStackEntry?.destination



    Column(horizontalAlignment = Alignment.End) {

        if (destination?.hasRoute(Collab::class) == true) {
            FloatingActionButton(
                modifier = Modifier.padding(16.dp),
                onClick = { navController.navigate(CreateCollab) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Collab")
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 1.dp,

        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.height(52.dp)
        ) {
            navBarItems.forEachIndexed { index, item ->
                val isSelected = destination?.hasRoute(item.route::class) == true

                NavigationBarItem(
                    icon = {
                        val iconToUse = if (isSelected) item.iconBold else item.icon
                        if (item.route is Profile) {
                            val profileImage = profileState.profile?.baseProfile?.image
                            if (profileImage != null) {
                                AsyncImage(
                                    model = profileImage,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = iconToUse),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        } else {
                            Icon(
                                painter = painterResource(id = iconToUse),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    },
                    //label = { Text(item.item, fontWeight = if (destination == item.route) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    selected = isSelected,
                    onClick = {

                        if (!isSelected) {
                            navController.navigate(item.route) {

                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }

                                launchSingleTop = true
                                restoreState = true
                            }
                        }


                    },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    )

                )
            }
        }
    }

}

fun isPollExpired(createdAt: Long, durationMillis: Long = 24 * 60 * 60 * 1000L): Boolean {
    val currentTime = System.currentTimeMillis()
    val expired = createdAt + durationMillis >= currentTime
    return expired
}

@Composable
fun HideBottomBar(
    navigationViewModel: NavigationViewModel,
    lazyState: LazyListState,
) {
    // We'll keep the LazyListState version for compatibility if needed, 
    // but NestedScroll is often better.
    // However, since we already have LazyListState in the screens, let's fix the logic.

    val isScrollingDown = remember {
        derivedStateOf {
            val firstVisibleItem = lazyState.firstVisibleItemIndex
            val scrollOffset = lazyState.firstVisibleItemScrollOffset
            firstVisibleItem to scrollOffset
        }
    }

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
                    Math.abs(currentOffset - previousScrollOffset)
                } else 100

                if (delta > 2) { // More sensitive
                    navigationViewModel.isBottomBarVisible(!scrollingDown)
                }

                previousIndex = currentIndex
                previousScrollOffset = currentOffset
            }
    }
}

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


inline fun <reified T : Any> NavGraphBuilder.navScreen(
    crossinline content: @Composable (NavBackStackEntry) -> Unit
) {

    composable<T>(
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(500)
            )
        },
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(500)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(500)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(500)
            )
        }
    ) { backStackEntry ->
        content(backStackEntry)
    }

}