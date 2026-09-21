package com.iota.campusX.Feature.Opportunities.presentation

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Opportunities.presentation.components.CourseCard
import com.iota.campusX.Feature.Opportunities.presentation.components.OpportunityShimmerItem
import com.iota.campusX.Feature.Opportunities.presentation.components.OpportunityCard
import com.iota.campusX.Feature.ExploreSwipe.presentation.ExploreSwipeDeckScreen
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.rememberScrollContext
import com.iota.campusX.Utils.UiState
import com.iota.campusX.R
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpportunitiesScreen(
    navController: NavHostController,
    viewModel: OpportunitiesViewModel = koinViewModel(),
    navigationViewModel: NavigationViewModel = koinInject()
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val categories = listOf("For You", "Internships", "Live Sessions")
    
    val opportunitiesState by viewModel.opportunitiesState.collectAsStateWithLifecycle()
    val coursesState by viewModel.coursesState.collectAsStateWithLifecycle()

    val lazyListState = rememberLazyListState()
    HideBottomBar(navigationViewModel, lazyListState)

    LaunchedEffect(selectedTabIndex) {
        viewModel.selectedTabIndex = selectedTabIndex
        // Ensure bottom bar is visible when switching tabs
        navigationViewModel.isBottomBarVisible(true)
        if (selectedTabIndex == 1) {
            viewModel.fetchOpportunities("Internship")
        } else if (selectedTabIndex == 2) {
            viewModel.fetchCourses()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(), // Removed nestedScroll(scrollContext)
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "Explore",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.5).sp
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                // Modern Chip-based Navigation
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(categories.size) { index ->
                        val isSelected = selectedTabIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTabIndex = index },
                            label = {
                                Text(
                                    text = categories[index],
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedContainerColor = MaterialTheme.colorScheme.onBackground,
                                selectedLabelColor = MaterialTheme.colorScheme.background
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = Color.Transparent,
                                borderWidth = 1.dp
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    ExploreSwipeDeckScreen(navController = navController)
                }
                1 -> {
                    when (val state = opportunitiesState) {
                        is UiState.Loading -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                items(4) {
                                    OpportunityShimmerItem()
                                }
                            }
                        }
                        is UiState.Success -> {
                            if (state.data.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    com.iota.campusX.ui.UIComponents.ComingSoonWidget(
                                        title = "No Internships Yet",
                                        description = "We're working with partners to bring the best internships to you. Check back soon!",
                                        iconRes = R.drawable.briefcase__1_
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    state = lazyListState,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    items(state.data) { opportunity ->
                                        OpportunityCard(
                                            opportunity = opportunity,
                                            onClick = {
                                                navController.navigate(
                                                    com.iota.campusX.Navigation.OpportunityDetail(
                                                        opportunity.id
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        is UiState.Error -> {
                            ErrorScreen(text = state.message, onReTry = { viewModel.fetchOpportunities("Internship") })
                        }
                        else -> {}
                    }
                }
                2 -> {
                    when (val state = coursesState) {
                        is UiState.Loading -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                items(5) {
                                    OpportunityShimmerItem()
                                }
                            }
                        }
                        is UiState.Success -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = lazyListState,
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                items(state.data) { course ->
                                    CourseCard(
                                        course = course,
                                        onClick = {
                                            navController.navigate(com.iota.campusX.Navigation.CourseDetail(course.id))
                                        }
                                    )
                                }
                            }
                        }
                        is UiState.Error -> {
                            ErrorScreen(text = state.message, onReTry = { viewModel.fetchCourses() })
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
