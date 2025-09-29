package com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.iota.campusX.R
import com.iota.campusX.Screens.Post.PostMenuActions.MenuAction

@Composable
fun SocietyOptionBar(
    societyOptionsViewModel: SocietyOptionsViewModel,
    societyData: SocietyData,
    showOptions: Boolean,
) {

    val menuOptions by societyOptionsViewModel.menuOptions.collectAsState()

    var pendingAction by remember { mutableStateOf<SocietyMenuOptions?>(null) }

    LaunchedEffect(societyData.isOwner) {
        societyOptionsViewModel.loadMenu(societyData)

    }

    AnimatedVisibility(
        visible = showOptions,
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

        Row {
            menuOptions.forEach {

                IconButton(
                    onClick = {
                        pendingAction = it
                    }
                ) {
                    Icon(
                        painter = painterResource(it.icon),
                        contentDescription = "Delete",
                        tint = if (it.label == "Delete") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                    )
                }

            }
        }

    }

    pendingAction?.let {

       SocietyConfirmationHandler(
           content = societyData,
           action = it,
           viewModel = societyOptionsViewModel,
           onDismiss = { pendingAction = null }
       )

    }


}