package com.iota.campusX.Feature.Society.presentation.Screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FieldValue
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Society.AgoraTokenBuilder.generateDynamicToken
import com.iota.campusX.Feature.Society.domain.models.*
import com.iota.campusX.Feature.Society.domain.models.GetChatMessage
import com.iota.campusX.Feature.Society.domain.models.SetChatMessage
import com.iota.campusX.Feature.Society.presentation.ViewModels.AudioRoomState
import com.iota.campusX.Feature.Society.presentation.ViewModels.AudioRoomViewModel
import com.iota.campusX.Feature.Society.presentation.ViewModels.StreamViewModel
import com.iota.campusX.Feature.Society.presentation.ViewModels.UiControls
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.UserProfileViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.BottomTextInput
import com.iota.campusX.Screens.Chat.MessageInputBar
import com.iota.campusX.Screens.Chat.convertTimestampToTime
import com.iota.campusX.Utils.CircularLoading
import com.iota.campusX.Utils.LoadingScreen
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.FeedUI.Avatar
import com.iota.campusX.ui.UIComponents.FeedUI.LinkPreviewCard
import com.iota.campusX.ui.UIComponents.FeedUI.extractUrlFromText
import com.iota.campusX.ui.UIComponents.FeedUI.normalizeUrl
import com.iota.campusX.ui.UIComponents.FeedUI.toMillis
import com.iota.campusX.ui.UIComponents.UserAvatar
import com.iota.campusX.ui.theme.Red
import com.iota.campusX.ui.theme.White
import com.iota.campusX.ui.theme.Yellow
import com.voxcii.voxcii.Screens.SearchFlow.PostSearchList
import com.voxcii.voxcii.Screens.SearchFlow.SearchTabs
import com.voxcii.voxcii.Screens.SearchFlow.UserSearchList
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.Boolean

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHomeScreen(
    navController: NavHostController,
    userProfileViewModel: UserProfileViewModel,
    streamViewModel: StreamViewModel = koinInject(),
    audioRoomViewModel: AudioRoomViewModel = koinInject()
) {

    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackBar = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })


    //-----------------------------------NAV DATA---------------------------------------------------
    val roomId = navController.currentBackStackEntry?.savedStateHandle?.get<String>("ROOM_ID")
    val createdBy = navController.currentBackStackEntry?.savedStateHandle?.get<String>("CREATOR_ID")
    val title = navController.currentBackStackEntry?.savedStateHandle?.get<String>("TITLE")


    //-----------------------------------COMPOSE ITEMS----------------------------------------------

    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }

    //-----------------------------------STATES-----------------------------------------------------


    //---------------------------------------FILTERS------------------------------------------------


    //---------------------------------------UI CONTENT---------------------------------------------

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(title?: "Society", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(
                        onClick = {

                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {

        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {

        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->

        LazyVerticalGrid(
            modifier = Modifier.padding(paddingValues),
            columns = GridCells.Fixed(1),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp),
        ) {

            item(span = { GridItemSpan(1) }) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("+ Create")
                }
            }


            item(span = { GridItemSpan(1) }) {
                SectionHeader("Text Channels")
            }

            item {

                Row (modifier = Modifier.clickable(onClick = {navController.navigate(Routes.Main.CommunityChat.routes)}), horizontalArrangement = Arrangement.spacedBy(12.dp)){
                    UserAvatar(
                        modifier = Modifier.size(48.dp),
                        imageUrl = "",
                        bgColor = "",


                    )
                    Column {
                        Text("OpenSpace Collaboration", style = MaterialTheme.typography.bodyMedium)
                        Text("30 Members", color = MaterialTheme.colorScheme.onSurfaceVariant)

                    }
                }

            }

            item(span = { GridItemSpan(1) }) {
                SectionHeader("Events")
            }

            item {
                StatusScreen(
                    text = "No Events Yet"
                )
            }


        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TextChannel(modifier: Modifier = Modifier) {


    Column {





    }
}