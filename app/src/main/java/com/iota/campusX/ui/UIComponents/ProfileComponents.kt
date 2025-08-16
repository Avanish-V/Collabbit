package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Profile.UserType
import com.iota.campusX.Utils.ProfileEdit
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.theme.LightBlack
import com.iota.campusX.ui.theme.LightTheme_Gray
import com.iota.campusX.ui.theme.White

@Composable
fun ProfileHeader(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier,
    headerHeight: (Dp) -> Unit,
    navHostController: NavHostController,
    user: UserDetail,
    userType: UserType,
    onLinkUpRequestClick: (() -> Unit)? = null,
    onMessageClick: (() -> Unit)? = null,
    connectionsCount: Int = 0,
    hasConnection: UiState<Boolean?>,
) {

    val density = LocalDensity.current
    var headerHeightDp by remember { mutableStateOf(0.dp) }


    Column(
        modifier = modifier
            .onGloballyPositioned {
                val heightPx = it.size.height
                headerHeightDp = with(density) { heightPx.toDp() }
                headerHeight(headerHeightDp)
            }
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Box(
                modifier = Modifier,
                contentAlignment = Alignment.BottomEnd
            ) {

                AsyncImage(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    model = user.userImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )

                if (userType == UserType.Owner) {

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.background,
                                shape = CircleShape
                            )
                            .background(color = MaterialTheme.colorScheme.surface)
                            .clickable(
                                onClick = {
                                    navHostController.navigate(Routes.Main.EditProfile.routes)
                                        .apply {
                                            navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                                "PROFILE_EDIT",
                                                ProfileEdit.PROFILE_SCREEN
                                            )
                                        }
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Back",
//                            tint = LightTheme_Blue
                        )
                    }
                }
            }


            Text(
                text = user.userName,
                style = MaterialTheme.typography.titleMedium
            )
        }

        TextButton(
            onClick = {
                navHostController.navigate(Routes.Main.Connections.routes).apply {
                    navHostController.currentBackStackEntry?.savedStateHandle?.set(
                        "USER_ID",
                        user.id
                    )
                }
            },
        ) {
            Text(
                text = "$connectionsCount Connections",
            )
        }

        if (userType == UserType.User) {
            Row {
                Button(
                    onClick = { onLinkUpRequestClick?.invoke() },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    when (hasConnection) {

                        is UiState.Success -> {

                            val connectionText = when (hasConnection.data) {
                                null -> "Connect"
                                true -> "Remove"
                                false -> "Requested"
                            }


                            Text(
                                text = connectionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (hasConnection.data == null || hasConnection.data == true) White else LightTheme_Gray
                            )
                        }

                        is UiState.Loading -> {
                            CircularLoading()
                        }

                        is UiState.Error -> {
                            LaunchedEffect(Unit) {
                                snackbarHostState.showSnackbar(hasConnection.message)
                            }
                        }

                        else -> {}
                    }
                }
                Spacer(
                    modifier = Modifier.width(12.dp)
                )
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .align(Alignment.CenterVertically),
                    onClick = { onMessageClick?.invoke() },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                    )
                ) {
                    Text(
                        text = "Message",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

        }

    }

}


@Composable
fun EmptyState(
    onClick:()-> Unit,
    title: String,
    isCurrentUser: Boolean
) {

    val color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)


    Box(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .drawBehind(
                onDraw = {

                    val strokeWidth = 1.dp.toPx()
                    val dashLength = 6.dp.toPx()
                    val gapLength = 4.dp.toPx()

                    drawRoundRect(
                        color = color,
                        size = size,
                        cornerRadius = CornerRadius(8.dp.toPx()),
                        style = Stroke(
                            width = strokeWidth,
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(dashLength, gapLength), 0f
                            )
                        )
                    )
                }
            )
            .clickable(
                onClick = {
                    onClick.invoke()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Row (verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(4.dp)){
            if (isCurrentUser){
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = color
                )
            }
            Text(
                modifier = Modifier.alpha(0.5f),
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

    }
}



@Composable
fun CampusWidget(
    campus: Campus?
) {

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        if (campus == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Update Campus",
                    modifier = Modifier.align(Alignment.Center),
                    color = LightBlack
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AsyncImage(
                    model = campus.university?.logo ?: "",
                    contentDescription = null,
                    placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(5.dp)),
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = campus.university?.university ?: "",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    campus.collegeName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                    campus.fieldOfStudy?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }


                    if (campus.courseStart != null && campus.courseEnd != null) {
                        Text(
                            text = "${campus.courseStart.month + campus.courseStart.year} - ${campus.courseEnd.month + campus.courseEnd.year}",
                            style = MaterialTheme.typography.bodyMedium

                        )
                    }

                    campus.campusCode?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

    }
}