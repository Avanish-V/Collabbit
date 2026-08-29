package com.iota.campusX.Feature.Notificattion.presentation.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.iota.campusX.Navigation.PostView
import com.iota.campusX.Navigation.Profile
import com.iota.campusX.Navigation.SendMessage
import com.iota.campusX.Feature.Notificattion.presentation.NotificationViewModel
import com.iota.campusX.Feature.Notificattion.presentation.effect.NotificationEffect

@Composable
 fun ObserveEffects(
    viewModel: NotificationViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState

){

    LaunchedEffect(Unit){

        viewModel.effect.collect{

            effect->

            when(effect){

                is NotificationEffect.NavigateToPost->{

                    navController.navigate(
                        PostView(postId = effect.postId.toString())
                    )

                }

                is NotificationEffect.NavigateToProfile->{

                    navController.navigate(
                        Profile(userId = effect.uid)
                    )

                }

                is NotificationEffect.NavigateToChat->{

                    navController.navigate(
                        SendMessage(userId = effect.chatId, userName = "") // We don't have username here, might need adjustment
                    )

                }

                is NotificationEffect.ShowSnackBar->{

                    snackbarHostState.showSnackbar(

                        effect.message

                    )

                }

            }

        }

    }

}
