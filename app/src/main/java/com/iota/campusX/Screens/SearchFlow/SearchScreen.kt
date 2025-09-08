package com.voxcii.voxcii.Screens.SearchFlow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Search.Domain.Models.UserSearchDTO
import com.iota.campusX.Feature.Search.Presentation.SearchViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.Divider
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class,)
@Composable
fun SearchScreen(navHostController: NavHostController) {

    val searchViewModel = koinViewModel<SearchViewModel>()

    val searchResults by searchViewModel.searchResults.collectAsState()

    var searchValue by remember { mutableStateOf("") }

    val snackBar = remember { SnackbarHostState() }


    Scaffold(
        topBar = {
            TextField(
                modifier = Modifier.statusBarsPadding().padding(horizontal = 10.dp, vertical = 10.dp).fillMaxWidth(),
                value = searchValue,
                onValueChange = {
                    searchValue = it.replaceFirstChar { it.uppercase()
                    }
                    searchViewModel.onSearchQuery(it)
                },
                placeholder = {
                    Text(
                        text = "Search...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                textStyle = TextStyle(
                    fontWeight = FontWeight.Bold,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(R.drawable.search_normal),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                shape = RoundedCornerShape(32.dp),
                maxLines = 1
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBar)
        },
        bottomBar = {}
    ) {

        Box(modifier = Modifier.padding(it)){
            when(searchResults){
                is UiState.Loading -> {
                    LoadingUI(isLoading = true)
                }
                is UiState.Success -> {

                    val result = (searchResults as UiState.Success<*>).data

                    LazyColumn{
                        items(result as List<*>){
                            MentorSingleCard(
                                user = it as UserSearchDTO,
                                onClick = {
                                    navHostController.navigate(Routes.Main.ProfileByID.routes).apply {
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_ID",it.id)
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                }
                is UiState.Error -> {
                    LaunchedEffect(Unit) {
                        snackBar.showSnackbar((searchResults as UiState.Error).message)
                    }
                }
                else -> {}
            }
        }

    }
}

@Composable
fun MentorSingleCard(user: UserSearchDTO, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(0.dp),
        onClick={onClick.invoke()},
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                model = user.userImage,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = user.userName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium

                )
                if (user.userBio.isNotEmpty()){
                    Spacer(modifier = Modifier.padding(6.dp))
                    Text(
                        user.userBio,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            }

        }
    }
}



