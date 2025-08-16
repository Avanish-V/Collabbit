package com.iota.campusX.Navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.ui.theme.White


@Composable
fun BottomAppBar(
    navController: NavHostController,
    notificationViewModel: NotificationViewModel
) {

    val badgeCount = notificationViewModel.notificationCount.collectAsState().value


    LaunchedEffect(Unit) {
        notificationViewModel.getNotificationCount()
    }


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val destination = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
    ) {

        navBarItems.forEachIndexed { index, item ->

            NavigationBarItem(
                icon = {
                    if (item.item == "Notification") {
                        BadgedBox(
                            badge = {
                                if (badgeCount != 0) {
                                    Box(modifier = Modifier.size(12.dp).background(Color.Red, CircleShape),contentAlignment = Alignment.Center){
                                        Text(
                                            badgeCount.toString(),
                                            fontSize = 8.sp,
                                            lineHeight = 10.sp,
                                            color = White
                                        )
                                    }

                                }
                            }
                        ) {
                            Icon(

                                painter = painterResource(id = if (destination == item.route) item.iconBold else item.icon),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(id = if (destination == item.route) item.iconBold else item.icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                },
                label = { Text(item.item, fontWeight = if (destination == item.route) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                selected = destination == item.route,
                onClick = {

                    if (destination != item.route) {
                        navController.navigate(item.route) {

                            popUpTo(navController.graph.findStartDestination().id) {

                                saveState = false
                            }

                            launchSingleTop = true
                            restoreState = true
                        }
                    }


                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                )

            )
        }
    }

}

@Composable
fun HideBottomBar(
    navigationViewModel: NavigationViewModel,
    lazyState: LazyListState,
) {

    val isScrollingDown = remember {
        derivedStateOf {
            val firstVisibleItem = lazyState.firstVisibleItemIndex
            val scrollOffset = lazyState.firstVisibleItemScrollOffset
            firstVisibleItem to scrollOffset
        }
    }

    var previousIndex by remember { mutableStateOf(0) }
    var previousScrollOffset by remember { mutableStateOf(0) }
    var bottomBarVisible by remember { mutableStateOf(true) }

    LaunchedEffect(isScrollingDown.value) {
        val (currentIndex, currentOffset) = isScrollingDown.value
        bottomBarVisible = if (currentIndex > previousIndex ||
            (currentIndex == previousIndex && currentOffset > previousScrollOffset)
        ) {
            false // scrolling down
        } else {
            true // scrolling up
        }

        previousIndex = currentIndex
        previousScrollOffset = currentOffset
    }

    LaunchedEffect(bottomBarVisible) {
        navigationViewModel.isBottomBarVisible(bottomBarVisible)
    }
}