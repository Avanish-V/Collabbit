package com.iota.campusX.Feature.Society.presentation.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Society.presentation.ViewModels.SocietyViewModel
import com.iota.campusX.Utils.CustomTextField
import com.iota.campusX.Utils.UiState
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSociety(navHostController: NavHostController) {

    //ViewModels
    val societyViewModel = koinInject<SocietyViewModel>()

    //States

    val createSocietyState by societyViewModel.createSocietyState.collectAsState()

    var societyName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val snackBarHostState = SnackbarHostState()

    LaunchedEffect(createSocietyState) {
        when(createSocietyState){
            is UiState.Loading -> {isLoading = true}
            is UiState.Success -> {
                navHostController.popBackStack()
            }
            is UiState.Error -> {
                isLoading = false
                snackBarHostState.showSnackbar((createSocietyState as UiState.Error).message)
            }
            else -> {
                isLoading = false
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title ={
                    Text(text = "Create")
                },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { societyViewModel.createSociety(societyName,description,FeedMode.GLOBAL) }) {
                        if (isLoading){
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        }else{
                            Text(text = "Save")
                        }

                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState)
        }
    ) {

        Column (modifier = Modifier.padding(it).padding(12.dp),verticalArrangement = Arrangement.spacedBy(12.dp)){

            CustomTextField(
                modifier = Modifier.fillMaxWidth(),
                value = societyName,
                onValueChange = {societyName = it.toString()},
                label = "Society Name",
                enabled = true,
                placeHolder = "Enter Society Name",
                trailingIcon = null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )

            CustomTextField(
                modifier = Modifier.fillMaxWidth(),
                value = description,
                onValueChange = {description = it.toString()},
                label = "Description",
                enabled = true,
                placeHolder = "About the Society",
                trailingIcon = null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )


        }

    }

}