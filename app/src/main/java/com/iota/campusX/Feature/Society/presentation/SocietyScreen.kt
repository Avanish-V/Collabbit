package com.iota.campusX.Feature.Society.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Navigation.CommunityChat
import com.iota.campusX.Navigation.CreateSociety
import com.iota.campusX.Navigation.SocietyHub
import com.iota.campusX.Screens.Home.SocietyList
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocietyScreen(
    navHostController: NavHostController,
    viewModel: SocietyViewModel = koinInject()
) {
    val joinedCommunities by viewModel.joinedCommunities.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Societies",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navHostController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        SocietyList(
            modifier = Modifier.padding(padding),
            communities = joinedCommunities,
            currentUserId = viewModel.currentUserId,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshCommunities() },
            onCreateClick = { navHostController.navigate(CreateSociety) },
            onDiscoverClick = { navHostController.navigate(SocietyHub) },
            onCommunityClick = { community -> 
                navHostController.navigate(CommunityChat(community.id)) 
            }
        )
    }
}
