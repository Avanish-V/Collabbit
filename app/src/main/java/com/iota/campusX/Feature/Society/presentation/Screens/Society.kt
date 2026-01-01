
package com.iota.campusX.Feature.Society.presentation.Screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Society.domain.models.GetSocietyDTO
import com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions.SocietyData
import com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions.SocietyOptionBar
import com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions.SocietyOptionsViewModel
import com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions.SocietyState
import com.iota.campusX.Feature.Society.presentation.ViewModels.SocietyViewModel
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.UserProfileViewModel
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.RefreshBox
import com.iota.campusX.Utils.LoadingScreen
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
    userProfileViewModel: UserProfileViewModel = koinInject(),
    navigationViewModel: NavigationViewModel = koinInject(),
    societyOptionsViewModel: SocietyOptionsViewModel = koinInject()
) {
    val lazyListState = rememberLazyListState()
    val state = societyViewModel.getSocietyState.collectAsStateWithLifecycle()
    val userSociety = societyViewModel.userSocietyState.collectAsStateWithLifecycle()
    val profileState = userProfileViewModel.userBaseProfile.collectAsStateWithLifecycle().value
    val showMenuOptions by societyOptionsViewModel.showMenu.collectAsState()
    val isRefreshing by societyViewModel.isRefreshing.collectAsState()
    val context = LocalContext.current

    val profile = when(profileState){
        is UiState.Success<*> -> {
            (profileState as UiState.Success<BaseProfileDTO>).data
        }
        else -> {
            null
        }
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val tabList = listOf("Society", "By You")


    var showSocietyOptions by remember { mutableStateOf<SocietyState?>(null) }

    // Initial fetch
    LaunchedEffect(profile?.campus?.code) {
        societyViewModel.fetchSocieties(feedMode = FeedMode.CAMPUS, campusId = profile?.campus?.code)
    }

    BackHandler {
        societyOptionsViewModel.showMenu(false)
    }

    HideBottomBar(
        navigationViewModel = navigationViewModel,
        lazyState = lazyListState
    )



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Society", style = MaterialTheme.typography.headlineMedium) },
                actions = {

                    if (showMenuOptions){
                        SocietyOptionBar(
                            showOptions = showSocietyOptions!!.showSocietyOptions,
                            societyData = showSocietyOptions!!.societyData!!,
                            societyOptionsViewModel = societyOptionsViewModel
                        )
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            when(userSociety.value){
                is UiState.Success<*> -> {
                    val userSocieties = (userSociety.value as UiState.Success<List<GetSocietyDTO>>).data
                    if (!userSocieties.isEmpty()){
                        AnimatedVisibility(
                            visible = pagerState.currentPage == 1 ,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            FloatingActionButton(
                                modifier = Modifier.padding(bottom = 80.dp),
                                onClick = { navHostController.navigate(Routes.Main.CreateSociety.routes) }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Create Society")
                            }
                        }
                    }
                }
                else -> {}
            }

        }
    ) { padding ->

        val pullToRefreshState = rememberPullToRefreshState()

        Column(modifier = Modifier.padding(padding)) {

            AppTabRow(pagerState = pagerState, tabList = tabList)

            HorizontalPager(state = pagerState) { page ->

                RefreshBox (
                    pullToRefreshState = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        if (page == 0){
                            societyViewModel.refreshSocieties(feedMode = FeedMode.CAMPUS, campusId = profile?.campus?.code)
                        }
                        else{
                            societyViewModel.refreshUserSocieties(userId = profile?.uid.orEmpty())
                        }
                    },

                ) {
                    when (page) {
                        0 -> SocietyUIRender(
                            state = state.value,
                            lazyListState = lazyListState,
                            campusId = profile?.campus?.code,
                            navHostController = navHostController,
                            onLongClick = { data ->
                                societyOptionsViewModel.showMenu(
                                    true
                                )
                                showSocietyOptions = SocietyState(
                                    showSocietyOptions = true,
                                    societyData = SocietyData(
                                        roomId =  data.roomId,
                                        isOwner = data.createdBy.id == profile?.uid,
                                        ownerId = data.createdBy.id,
                                        navHostController = navHostController
                                    )
                                )
                                context.vibrate()
                            }
                        )
                        1 -> {

                            LaunchedEffect(Unit) {
                                profile?.uid?.let { societyViewModel.fetchUserSocieties(userId = it) }
                            }

                            SocietyUIRender(
                                state = userSociety.value,
                                lazyListState = lazyListState,
                                campusId = profile?.campus?.code,
                                navHostController = navHostController,
                                onLongClick = { data ->
                                    societyOptionsViewModel.showMenu(
                                        true
                                    )
                                    showSocietyOptions = SocietyState(
                                        showSocietyOptions = true,
                                        societyData = SocietyData(
                                            roomId =  data.roomId,
                                            isOwner = data.createdBy.id == profile?.uid,
                                            ownerId = data.createdBy.id,
                                            navHostController = navHostController
                                        )
                                    )
                                    context.vibrate()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun SocietyUIRender(
    state: UiState<List<GetSocietyDTO>>,
    lazyListState: LazyListState,
    campusId: String?,
    navHostController: NavHostController,
    onLongClick: (GetSocietyDTO) -> Unit
) {


    when (state) {

        is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingScreen()
        }

        is UiState.Success<*> -> {

            if (campusId.isNullOrEmpty()){
                CampusEmptyState(
                    onUpdateClick = {navHostController.navigate(Routes.Main.Profile.routes)}
                )
                return
            }

            val societies = state.data as List<*>

            if (societies.isEmpty()){
                EmptyState(navHostController)
                return
            }
            else
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(societies) { society ->
                    SocietyCard(
                        getSocietyDTO = society as GetSocietyDTO,
                        onCardClick = {
                            navHostController.navigate(Routes.Main.JoinSociety.routes).apply {
                                navHostController.currentBackStackEntry?.savedStateHandle?.set("CREATOR_ID", society.createdBy.id)
                                navHostController.currentBackStackEntry?.savedStateHandle?.set("ROOM_ID", society.roomId)
                                navHostController.currentBackStackEntry?.savedStateHandle?.set("TITLE", society.societyName)
                            }
                        },
                        onLongClick = {
                            onLongClick(society)
                        }
                    )
                }
                item {
                    Spacer(Modifier.height(80.dp))
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

            .combinedClickable(
                onClick = { onCardClick.invoke() },
                onLongClick = {
                    onLongClick.invoke()
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .shadow(
                elevation = 4.dp,
                spotColor = MaterialTheme.colorScheme.primary,
                ambientColor = MaterialTheme.colorScheme.primary
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Row (verticalAlignment = Alignment.Top,horizontalArrangement = Arrangement.spacedBy(12.dp)){

                AsyncImage(
                    model = getSocietyDTO.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp)
                        .clip(MaterialTheme.shapes.small)
                        .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.small
                    ),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(getSocietyDTO.societyName, style = MaterialTheme.typography.titleMedium)
                    Text(getSocietyDTO.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

            }


            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                Text("Hosted by", style = MaterialTheme.typography.titleSmall)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(12.dp)) {


                        Box(contentAlignment = Alignment.Center){
//                            if (getSocietyDTO.active){
//                                AnimatedStatus(
//                                    modifier = Modifier.size(48.dp),
//                                    file = R.raw.wave_animation,
//                                    description = "Online"
//                                )
//                            }

                            CircleImage(
                                image = getSocietyDTO.createdBy.image?:"",
                                modifier = Modifier.size(28.dp),
                                onClick = {},
                                visibility = VisibilityMode.USER
                            )
                        }
                        Text(getSocietyDTO.createdBy.name, style = MaterialTheme.typography.bodyMedium)
                    }

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
                tint = MaterialTheme.colorScheme.surface,
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
            OutlinedButton(
                onClick = { onUpdateClick.invoke() },
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(top = 8.dp),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text("Update")
            }
        }
    }
}
