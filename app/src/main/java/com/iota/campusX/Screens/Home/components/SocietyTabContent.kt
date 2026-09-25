package com.iota.campusX.Screens.Home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Society.presentation.SocietyHubViewModel
import com.iota.campusX.Feature.Society.presentation.components.SocietyList
import com.iota.campusX.Navigation.CommunityChat
import com.iota.campusX.Navigation.CreateSociety
import com.iota.campusX.Navigation.SocietyHub
import org.koin.compose.koinInject

@Composable
fun SocietyTabContent(
    navHostController: NavHostController,
    societyViewModel: SocietyHubViewModel = koinInject()
) {
    val joinedCommunities by societyViewModel.joinedCommunities.collectAsStateWithLifecycle()
    val isSocietyRefreshing by societyViewModel.isRefreshing.collectAsStateWithLifecycle()

    val onRefreshSociety = remember(societyViewModel) { { societyViewModel.refreshCommunities() } }
    val onCreateSociety = remember(navHostController) { { navHostController.navigate(CreateSociety) } }
    val onDiscoverSociety = remember(navHostController) { { navHostController.navigate(SocietyHub) } }
    val onCommunityClick = remember(navHostController) { { community: com.iota.campusX.Feature.Society.domain.model.Community -> 
        navHostController.navigate(CommunityChat(community.id)) 
    } }

    SocietyList(
        communities = joinedCommunities,
        currentUserId = societyViewModel.currentUserId,
        isRefreshing = isSocietyRefreshing,
        onRefresh = onRefreshSociety,
        onCreateClick = onCreateSociety,
        onDiscoverClick = onDiscoverSociety,
        onCommunityClick = onCommunityClick
    )
}
