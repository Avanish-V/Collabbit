package com.iota.campusX.Screens.Home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.Feature.Notificattion.presentation.NotificationViewModel
import com.iota.campusX.Feature.UserProfile.ui.screens.ProfileMain.UserProfileViewModel
import com.iota.campusX.Navigation.*
import com.iota.campusX.Permissions.NotificationPermissionRequester
import com.iota.campusX.R
import com.iota.campusX.ui.UIComponents.*
import com.iota.campusX.Screens.Home.components.ForYouTabContent
import com.iota.campusX.Screens.Home.components.SocietyTabContent
import org.koin.compose.koinInject
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator

private val GilroyFontFamily = FontFamily(
    Font(R.font.gilroy_extrabold, weight = FontWeight.ExtraBold)
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navHostController: NavHostController,
    profileViewModel: UserProfileViewModel,
    notificationViewModel: NotificationViewModel,
    navigationViewModel: NavigationViewModel = koinInject(),
    chatsViewModel: ChatsViewModel = koinInject(),
    targetPostId: String? = null
) {
    NotificationPermissionRequester()

    val lazyState = rememberLazyListState()
    HideBottomBar(navigationViewModel, lazyState)

    val notificationBadge by notificationViewModel.unreadCount.collectAsStateWithLifecycle()
    val chatBadge by chatsViewModel.unreadMessageCount.collectAsStateWithLifecycle()

    val unreadNotifications = remember { derivedStateOf { notificationBadge.toInt() } }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackBarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val onChatClick = remember(navHostController) { { navHostController.navigate(ChatList) } }
    val onNotificationClick =
        remember(navHostController) { { navHostController.navigate(Notification) } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HomeTopBar(
                chatCount = chatBadge,
                notificationCount = unreadNotifications.value,
                onChatClick = onChatClick,
                onNotificationClick = onNotificationClick,
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.padding(bottom = 100.dp),
                hostState = snackBarHostState
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            HomeTabRow(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            when (selectedTabIndex) {
                0 -> ForYouTabContent(
                    navHostController = navHostController,
                    lazyState = lazyState,
                    scrollBehavior = scrollBehavior,
                    targetPostId = targetPostId
                )

                1 -> SocietyTabContent(
                    navHostController = navHostController
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    chatCount: Int,
    notificationCount: Int,
    onChatClick: () -> Unit,
    onNotificationClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = "Collabbit",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = GilroyFontFamily,
                color = MaterialTheme.colorScheme.primary
            )
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BadgeItem(
                    modifier = Modifier.size(20.dp),
                    itemCount = chatCount,
                    onBadgeClick = onChatClick,
                    badgeIcon = R.drawable.messages__1_,
                    contentDescription = "Messages"
                )

                BadgeItem(
                    modifier = Modifier.size(22.dp),
                    itemCount = notificationCount,
                    onBadgeClick = onNotificationClick,
                    badgeIcon = R.drawable.bell,
                    contentDescription = "Notifications"
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.background,
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun HomeTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabRow(
            modifier = Modifier.weight(1f),
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            divider = { Divider() },
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { onTabSelected(0) },
                text = {
                    Text(
                        text = "For You",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { onTabSelected(1) },
                text = {
                    Text(
                        text = "Society",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    }
}
