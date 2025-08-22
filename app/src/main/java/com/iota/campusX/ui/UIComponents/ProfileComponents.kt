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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
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
import coil.compose.AsyncImage
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.theme.LightBlack
import com.iota.campusX.ui.theme.LightTheme_Gray
import com.iota.campusX.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun ProfileHeader(
    modifier: Modifier,
    headerHeight: (Dp) -> Unit,
    user: BaseProfileDTO?,
    connectionsCountState: UiState<Int>,
    onConnectionClick: () -> Unit = {},
    editProfile: @Composable () -> Unit = {},
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
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Row (
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ){

                Column (modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)){

                    Box(contentAlignment = Alignment.TopEnd){

                        AsyncImage(
                            modifier = Modifier
                                .size(80.dp)

                                .border(
                                    width = 6.dp,
                                    color = Color.White,
                                    shape = MaterialTheme.shapes.small
                                ).shadow(
                                    elevation = 6.dp,
                                    shape = MaterialTheme.shapes.small
                                )
                                .clip(
                                    MaterialTheme.shapes.large
                                ),
                            model = user?.userImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            fallback = painterResource(R.drawable.landscape_placeholder_svgrepo_com),

                        )

                        user?.let {
                            if (it.metaData.verified){
                                Icon(
                                    modifier = Modifier.size(20.dp)
                                        .offset(x = 8.dp,y = -4.dp),
                                    painter = painterResource(R.drawable.check_circle),
                                    contentDescription = "verified",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Text(
                        text = user?.userName ?: "",
                        style = MaterialTheme.typography.titleMedium
                    )

                }

                editProfile.invoke()

            }

        }

        TextButton(
            onClick = {
                onConnectionClick.invoke()
            },
        ) {
            when(connectionsCountState){
                is UiState.Loading->{
                    CircularLoading()
                }
                is UiState.Success -> {
                    val connectionsCount = connectionsCountState.data
                    Text(
                        text = "$connectionsCount Connections",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                else -> {

                }
            }
        }
    }

}

@Composable
fun ProfileAction(
    onLinkUpRequestClick: (() -> Unit)? = null,
    onMessageClick: (() -> Unit)? = null,
    hasConnectionState: UiState<Boolean?>,
    snackBarHostState: SnackbarHostState
) {

    Row (modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)){
        Button(
            onClick = { onLinkUpRequestClick?.invoke() },
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
            shape = RoundedCornerShape(6.dp)
        ) {
            when (hasConnectionState) {

                is UiState.Success -> {

                    val connectionText = when (hasConnectionState.data) {
                        null -> "Connect"
                        true -> "Remove"
                        false -> "Requested"
                    }


                    Text(
                        text = connectionText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if ( hasConnectionState.data == true) White else LightTheme_Gray
                    )
                }

                is UiState.Loading -> {
                    CircularLoading()
                }

                is UiState.Error -> {
                    LaunchedEffect(Unit) {
                        snackBarHostState.showSnackbar(hasConnectionState.message)
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


}


@Composable
fun EditProfileIconButton(onClick: () -> Unit) {

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
                    onClick.invoke()
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
        )
    }

}


@Composable
fun EmptyState(
    onClick:()-> Unit = {},
    title: String,
    isAppUser: Boolean
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

            if (isAppUser){
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
                    error = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(5.dp)),
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

                    campus.university?.let {
                        Text(
                            text = campus.university.university,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    campus.collegeName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleSmall,
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTabRow(pagerState: PagerState,tabList: List<String> = emptyList()) {

    val scope = rememberCoroutineScope()

    PrimaryTabRow(
        modifier = Modifier,
        selectedTabIndex = pagerState.currentPage,
        divider = { Divider() },
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(
                    selectedTabIndex = pagerState.currentPage,
                    matchContentSize = false
                ),
                width = 48.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
            )
        },
    ) {
       tabList.forEachIndexed { index, title ->
            Tab(
                text = {
                    Text(text = title)
                },
                selected = pagerState.currentPage == index,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
            )
        }
    }


}


@Composable
fun ProfileContents(
    pagerState: PagerState,
    screenHeight: Dp,
    headerPinned: Boolean,
    content: @Composable (Int) -> Unit
    ) {

    HorizontalPager(state = pagerState) { page ->
        content.invoke(page)
    }
}