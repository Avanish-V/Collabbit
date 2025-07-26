package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iota.campusX.ui.theme.LightTheme_Gray
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White
import com.iota.campusX.ui.theme.LightTheme_White

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
            containerColor = MaterialTheme.colorScheme.surface,
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
            uncheckedThumbColor = LightTheme_Gray,
            uncheckedIconColor = White400,
            uncheckedTrackColor = White,
            uncheckedBorderColor = LightTheme_Gray
        )
    )

}

@Composable
fun SubmitButton (onClick: () -> Unit) {
    TextButton(onClick = {onClick.invoke()}) {
        Text("Save", color = MaterialTheme.colorScheme.primary)
    }
}


@Composable
fun PrimaryButton(modifier: Modifier = Modifier,onClick: () -> Unit,buttonText: String) {

    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 20.dp),
        onClick = {onClick.invoke()},
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = buttonText, style = MaterialTheme.typography.headlineMedium)
    }


}