package com.iota.campusX.Feature.Society.presentation.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Society.domain.models.GetSocietyDTO
import com.iota.campusX.Feature.Society.presentation.ViewModels.SocietyViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.White400
import org.koin.compose.koinInject
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Society(navHostController: NavHostController) {

    val societyViewModel = koinInject<SocietyViewModel>()

    val state = societyViewModel.getSocietyState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        societyViewModel.fetchSocieties(
            feedMode = FeedMode.GLOBAL,
            campusId = null
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title ={
                    Text(text = "Society")
                },
                actions = {
                    Row(modifier = Modifier.padding(end = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconButton(onClick = {}, colors = IconButtonDefaults.iconButtonColors(containerColor = White400)) {
                            Icon(
                                painter = painterResource(R.drawable.search_normal),
                                contentDescription = "Search"
                            )
                        }

                        TextButton(onClick = {navHostController.navigate(Routes.Main.CreateSociety.routes)}) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Search"
                            )
                            Text("Create")
                        }
                    }
                }
            )
        },
    ) {

        Box(modifier = Modifier.padding(it)){

            when(state.value){

                is UiState.Loading -> {
                    LoadingUI(isLoading = state.value is UiState.Loading)
                }

                is UiState.Success<*> -> {

                    val societyList = state.value as UiState.Success

                    LazyColumn(modifier = Modifier.fillMaxSize(),contentPadding = PaddingValues(12.dp),verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        items(societyList.data){
                            SocietyCard(
                                onCardClick = {
                                    navHostController.navigate(Routes.Main.JoinSociety.routes).apply {
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set("CREATOR_ID",it.createdBy.id)
                                        navHostController.currentBackStackEntry?.savedStateHandle?.set("ROOM_ID",it.roomId)
                                    }
                                },
                                getSocietyDTO = it
                            )
                        }


                    }

                }

                is UiState.Error -> {

                    ErrorScreen(
                        text = (state.value as UiState.Error).message,
                        buttonText = "Retry",
                        image = null,
                        onReTry = {
                            societyViewModel.fetchSocieties(
                                feedMode = FeedMode.GLOBAL,
                                campusId = null
                            )
                        }
                    )

                }

                else -> {}

            }



        }




    }

}


@Composable
fun SocietyCard(onCardClick: () -> Unit,getSocietyDTO: GetSocietyDTO) {

    ElevatedCard(
        onClick = {onCardClick.invoke()},
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        )
    ) {

        Row {
            Column (modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)){

                Column (verticalArrangement = Arrangement.spacedBy(6.dp)){
                    Text(
                        text = getSocietyDTO.societyName,
                        color = Black800,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = getSocietyDTO.description,
                        color = Black500
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Hosted by")
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        CircleImage(
                            image = getSocietyDTO.createdBy.userImage,
                            modifier = Modifier.size(28.dp),
                            onClick = {}
                        )
                        Text(getSocietyDTO.createdBy.userName)
                    }

                }

            }

        }

    }


}







@Composable
fun ScatteredImage(
    count: Int = 5,
    modifier: Modifier
) {


    val random = remember { Random(5) }

    Box(modifier = modifier) {
        repeat(count) {
            val randomX = random.nextInt(0, 50).dp
            val randomY = random.nextInt(0, 80).dp
            val size = random.nextInt(24, 30).dp

            Image(
                painter = painterResource(R.drawable.man),
                contentDescription = "Scattered part",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .graphicsLayer {
                        translationX = randomX.toPx()
                        translationY = randomY.toPx()
                        rotationZ = random.nextFloat() * 30f - 15f // slight rotation
                    }
                    .clip(CircleShape)
            )
        }
    }
}
