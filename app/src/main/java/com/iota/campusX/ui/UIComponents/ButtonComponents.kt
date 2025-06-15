package com.iota.campusX.ui.UIComponents

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.background

@Composable
fun IconButtonWidget(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: Int,
    description: String,
    enabled: Boolean
) {

    IconButton(
        modifier = modifier,
        onClick = {
            onClick.invoke()
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = background,
            contentColor = Black800
        ),
        enabled = enabled
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = description
        )
    }

}

@Composable
fun FeedModeSwitch(modifier: Modifier = Modifier) {

    Switch(
        checked = false,
        onCheckedChange = { isChecked ->
//            val newMode = if (isChecked) FeedMode.CAMPUS else FeedMode.GLOBAL
//            homeViewModel.saveSwitchState(newMode)
//            context.vibrate()
        },
        colors = SwitchDefaults.colors(
            uncheckedThumbColor = Black500,
            uncheckedIconColor = White400,
            uncheckedTrackColor = White900,
            uncheckedBorderColor = Black500
        )
    )

}