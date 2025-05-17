package com.iota.campusX.Screens.Profile

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Feature.Post.presentation.PostViewModel
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.LinkUpRequestDTO
import com.iota.campusX.Feature.UserProfile.data.UserBasicProfileDTO
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.PostCard
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.timeMillsToString
import com.iota.campusX.ui.theme.Black400
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navHostController: NavHostController,
    postViewModel: PostViewModel,
    profileViewModel: UserProfileViewModel,
    googleSignInViewModel: AuthViewModel
) {

    val profileState = profileViewModel.userBaseProfile.collectAsState().value.baseProfileData
    val currentUser = googleSignInViewModel.userId()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White900
                ),
                actions = {
                    Row(horizontalArrangement = Arrangement.End) {

                        IconButton(onClick = {
                            navHostController.navigate("SETTING")
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.setting),
                                contentDescription = null
                            )
                        }

                    }
                }
            )
        },
        containerColor = secondary
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {

            ProfileHeader(
                navHostController = navHostController,
                user = User(
                    userName = profileState?.userName.toString(),
                    userImage = profileState?.userImage.toString(),
                    _id = profileState?._id.toString()
                ),
                currentUser = currentUser,


                )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalTabComponent { page ->

                when (page) {
                    0 -> {
                        UserAbout(
                            userBasicProfileDTO = profileState
                        )
                    }

                    1 -> {
                        PostScreenComponent(
                            navHostController,
                            postViewModel,
                            currentUser = currentUser.toString(),
                            campusId = profileState?.campus?.campusCode ?: ""
                        )
                    }

                }
            }

        }

    }

}


@Composable
fun ProfileHeader(
    navHostController: NavHostController,
    user: User,
    currentUser: String,
    onLinkUpRequestClick: (() -> Unit)? = null,
    isLinkUpRequestSent: Boolean? = null
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White900)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        Row(modifier = Modifier.fillMaxWidth()) {

            AsyncImage(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = secondary,
                        shape = CircleShape
                    ),
                model = user.userImage,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = user.userName ?: "😁",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            IconButton(
                onClick = {
                    navHostController.navigate("EDIT_PROFILE")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = primary
                )
            }

        }


        if (user._id != currentUser) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) // ✅ Makes dividers take height of tallest Column
                    .border(
                        width = 1.dp,
                        color = White400,
                        shape = RoundedCornerShape(5.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {

                Column(
                    modifier = Modifier.clickable(
                        onClick = {
                            onLinkUpRequestClick?.invoke()
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.user_add),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(
                            color = if (isLinkUpRequestSent == null) Black900 else Black500
                        )
                    )
                    Text(
                        text = if (isLinkUpRequestSent == true) "Remove Connection"
                        else if (isLinkUpRequestSent == false) "Withdraw request"
                        else if (isLinkUpRequestSent == null) "Connect" else "",
                        style = typography.labelMedium,
                        color = if (isLinkUpRequestSent == null) Black900 else Black500
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = White400
                )

                Column(
                    modifier = Modifier.clickable(
                        onClick = {
                            navHostController.navigate(Routes.Main.SendMessage.routes).apply {
                                navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                    "USER_ID",
                                    user._id
                                )
                                navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                    "USER_NAME",
                                    user.userName
                                )
                                navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                    "USER_IMAGE",
                                    user.userImage
                                )
                            }
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.chatbubble_outline),
                        contentDescription = null
                    )
                    Text(
                        text = "Message",
                        style = typography.labelMedium
                    )
                }

            }
        }

        Spacer(modifier = Modifier.height(12.dp))

    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalTabComponent(pageIndex: @Composable (Int) -> Unit) {

    val tabs = listOf("About", "Posts")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    PrimaryTabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = White900,
        divider = {
            HorizontalDivider(
                color = White400
            )
        },
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(
                    selectedTabIndex = pagerState.currentPage,
                    matchContentSize = false
                ),
                color = primary,
                height = 1.dp
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                text = {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                selected = pagerState.currentPage == index,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                selectedContentColor = Black800,
                unselectedContentColor = Black400
            )
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->

        pageIndex(page)

    }


}


@Composable
fun UserAbout(userBasicProfileDTO: UserBasicProfileDTO?) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = White900)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Bio", fontWeight = FontWeight.Bold)
            Text(text = userBasicProfileDTO?.userBio ?: "")
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = White400
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Interest", fontWeight = FontWeight.Bold)
            InterestComponent(
                interestList = interestList
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = White400
        )

        Campus(
            userBasicProfileDTO?.campus
        )


    }


}

@Composable
fun Campus(campus: Campus?) {

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AsyncImage(
                model = campus?.university?.logo ?: "",
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(5.dp)),
            )
            Column {
                Text(
                    text = campus?.university?.university ?: "",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = campus?.collegeName ?: "",
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(text = campus?.fieldOfStudy ?: "")

                Text(
                    text = "${timeMillsToString(campus?.courseStart ?: 0L)} to ${
                        timeMillsToString(
                            campus?.courseEnd ?: 0L
                        )
                    }"
                )

                Text(text = campus?.campusCode ?: "")
            }
        }

    }
}

@Composable
fun PostScreenComponent(
    navHostController: NavHostController,
    postViewModel: PostViewModel,
    currentUser: String,
    campusId: String
) {

    LaunchedEffect(Unit) {
        postViewModel.fetchPostById(currentUser, campusId)
    }
    val postState = postViewModel.postsById.collectAsState().value

    when {
        postState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = primary
                )
            }
        }

        postState.postData.isNotEmpty() -> {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(postState.postData) {
                    PostCard(
                        onPostClick = {
                            navHostController.navigate(Routes.Main.ReplyPost.routes).apply {
                                navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                    "post",
                                    it
                                )
                            }
                        },
                        onLikeClick = {
                            postViewModel.toggleLike(
                                userId = currentUser,
                                postId = it.postId,
                                isLiked = it.postActions.isLiked
                            )
                        },
                        onReplyClick = {
                            navHostController.navigate(Routes.Main.ReplyPost.routes)
                        },
                        post = it,
                        navHostController = navHostController,
                        onDotMenuClick = {

                        }
                    )
                }
            }
        }

        postState.error.isNotEmpty() -> {

        }

    }
    StatusScreen(
        isActive = postState.postData.isEmpty(),
        text = "No Posts Yet"
    )

}

