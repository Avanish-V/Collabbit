package com.iota.campusX.Feature.Society.presentation.Screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Society.domain.models.GetSocietyDTO
import com.iota.campusX.Feature.Society.presentation.ViewModels.SocietyViewModel
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.AnimatedStatus
import com.iota.campusX.ui.UIComponents.AppTabRow
import com.iota.campusX.ui.UIComponents.CircleImage
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SocietyScreen(
    navHostController: NavHostController,
    societyViewModel: SocietyViewModel = koinInject(),
    userProfileViewModel: UserProfileViewModel = koinInject()
) {
    val state = societyViewModel.getSocietyState.collectAsStateWithLifecycle()
    val userSociety = societyViewModel.userSocietyState.collectAsStateWithLifecycle()
    val profile = userProfileViewModel.userBaseProfile.collectAsStateWithLifecycle().value
    val context = LocalContext.current


    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val tabList = listOf("Society", "By You")

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteButton by remember { mutableStateOf(false) }
    var selectedRoomId by remember { mutableStateOf("") }

    // Initial fetch
    LaunchedEffect(Unit) {
        societyViewModel.fetchSocieties(feedMode = FeedMode.CAMPUS, campusId = profile?.campus?.campusCode)
    }


    BackHandler() {
        showDeleteButton = false
        selectedRoomId = ""
        context.vibrate()
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Society", style = MaterialTheme.typography.headlineMedium) },
                actions = {
//                    IconButton(onClick = { /* TODO: Add search */ }) {
//                        Icon(
//                            painter = painterResource(R.drawable.search_normal),
//                            contentDescription = "Search"
//                        )
//                    }

                    AnimatedVisibility(
                        visible = showDeleteButton,
                        enter = slideInHorizontally(
                            initialOffsetX = { it }, // starts from right side
                            animationSpec = spring(
                                stiffness = Spring.StiffnessMedium, // adjust bounce
                                dampingRatio = Spring.DampingRatioMediumBouncy
                            )
                        ),
                        exit = slideOutHorizontally(
                            targetOffsetX = { it }, // slides out to right side
                            animationSpec = spring(
                                stiffness = Spring.StiffnessMedium,
                                dampingRatio = Spring.DampingRatioNoBouncy
                            )
                        )
                    ) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                painter = painterResource(R.drawable.trash),
                                contentDescription = "Delete"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (pagerState.currentPage == 1) {
                FloatingActionButton(onClick = { navHostController.navigate(Routes.Main.CreateSociety.routes) }) {
                    Icon(Icons.Default.Add, contentDescription = "Create Society")
                }
            }
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {

            AppTabRow(pagerState = pagerState, tabList = tabList)

            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> SocietyList(
                        state = state.value,
                        campusId = profile?.campus?.campusCode,
                        navHostController = navHostController,
                        onLongClick = { roomId ->
                            showDeleteButton = true
                            selectedRoomId = roomId
                            context.vibrate()
                            Log.d("SocietyScreen", "Clicked")
                        }
                    )
                    1 -> MySocietyList(
                        state = userSociety.value,
                        navHostController = navHostController,
                        onLongClick = { roomId ->
                            Log.d("SocietyScreen", "Clicked")
                            showDeleteButton = true
                            selectedRoomId = roomId
                            context.vibrate()
                        },
                        fetchUserSociety = {
                            profile?.id?.let {
                                societyViewModel.fetchUserSocieties(userId = it)
                            }
                        }
                    )
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        showDeleteButton = false
                        selectedRoomId = ""
                    },
                    title = { Text("Delete Society") },
                    text = { Text("Are you sure you want to delete this society?") },
                    confirmButton = {
                        TextButton(onClick = {
                            societyViewModel.deleteSociety(selectedRoomId)
                            showDeleteDialog = false
                            showDeleteButton = false
                            selectedRoomId = ""
                        }) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            showDeleteButton = false
                            selectedRoomId = ""
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }

        }


    }
}

@Composable
private fun SocietyList(
    state: UiState<List<GetSocietyDTO>>,
    campusId: String?,
    navHostController: NavHostController,
    onLongClick: (String) -> Unit
) {
    when (state) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingUI()
        }

        is UiState.Success<*> -> {

            if (campusId.isNullOrEmpty()){
                CampusEmptyState(
                    onUpdateClick = {navHostController.navigate(Routes.Main.Profile.routes)}
                )
                return
            }

            val societies = state.data as List<GetSocietyDTO>

            if (societies.isEmpty()){
                EmptyState(navHostController)
                return
            }
            else
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(societies) { society ->
                    SocietyCard(
                        getSocietyDTO = society,
                        onCardClick = {
                            navHostController.navigate(Routes.Main.JoinSociety.routes).apply {
                                navHostController.currentBackStackEntry?.savedStateHandle?.set("CREATOR_ID", society.createdBy.id)
                                navHostController.currentBackStackEntry?.savedStateHandle?.set("ROOM_ID", society.roomId)
                            }
                        },
                        onLongClick = {
                            if (society.isCurrentUser) onLongClick(society.roomId)

                        }
                    )
                }
            }

        }

        is UiState.Error -> ErrorState()
        else -> {}
    }
}

@Composable
private fun MySocietyList(
    state: UiState<List<GetSocietyDTO>>,
    navHostController: NavHostController,
    onLongClick: (String) -> Unit,
    fetchUserSociety:()-> Unit
) {

    LaunchedEffect(Unit){
        fetchUserSociety()
    }

    when (state) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is UiState.Success<*> -> {

            val societies = state.data as? List<GetSocietyDTO> ?: emptyList()

            if (societies.isEmpty())

                EmptyState(navHostController)

            else

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(societies) { society ->
                        SocietyCard(
                            getSocietyDTO = society,
                            onCardClick = {
                                navHostController.navigate(Routes.Main.JoinSociety.routes).apply {
                                    navHostController.currentBackStackEntry?.savedStateHandle?.set("CREATOR_ID", society.createdBy.id)
                                    navHostController.currentBackStackEntry?.savedStateHandle?.set("ROOM_ID", society.roomId)
                                }
                            },
                            onLongClick = {
                                if (society.isCurrentUser) onLongClick(society.roomId)

                            }
                        )
                    }
                }

        }

        is UiState.Error -> ErrorState()
        else -> {}
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SocietyCard(
    onCardClick: () -> Unit,
    onLongClick: () -> Unit,
    getSocietyDTO: GetSocietyDTO
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .combinedClickable(
                onClick = { onCardClick.invoke() },
                onLongClick = {
                    onLongClick.invoke()

                },
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(getSocietyDTO.societyName, style = MaterialTheme.typography.bodyLarge)
            Text(getSocietyDTO.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Hosted by", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircleImage(
                        image = getSocietyDTO.createdBy.userImage,
                        modifier = Modifier.size(28.dp),
                        onClick = {},
                        visibility = VisibilityMode.USER
                    )
                    Text(getSocietyDTO.createdBy.userName, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(navHostController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AnimatedStatus(modifier = Modifier.size(120.dp), file = R.raw.audio_room, description = "Empty Box")
            Text("No Societies Yet", style = MaterialTheme.typography.titleMedium)
            Text(
                "Create your first society and start connecting\nwith your audience",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = { navHostController.navigate(Routes.Main.CreateSociety.routes) }) {
                Icon(Icons.Default.Add, contentDescription = "Create")
                Spacer(Modifier.width(6.dp))
                Text("Create")
            }
        }
    }
}

@Composable
private fun ErrorState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Something went wrong", color = MaterialTheme.colorScheme.error)
    }
}


@Composable
fun CampusEmptyState(onUpdateClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // A fun icon
            Icon(
                painter = painterResource(R.drawable.school__1_),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(64.dp)
            )

            // Main message
            Text(
                "No Campus Found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Catchy subtext
            Text(
                "Update your campus detail to join the societies",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // (Optional) A retry / action button
            TextButton(
                onClick = { onUpdateClick.invoke() },
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Update")
            }
        }
    }
}
