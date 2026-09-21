package com.iota.campusX.ui.UIComponents

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.UserProfile.domain.Model.Education
import com.iota.campusX.Navigation.Connections
import com.iota.campusX.Navigation.Followers
import com.iota.campusX.R
import kotlinx.coroutines.launch


@Composable
fun EditProfileIconButton(onClick: () -> Unit) {

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
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
            contentDescription = "Edit",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

}


@Composable
fun EmptyState(
    onClick:()-> Unit = {},
    title: String,
    isAppUser: Boolean
) {

    val color = MaterialTheme.colorScheme.primary


    Box(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .drawBehind(
                onDraw = {

                    val strokeWidth = 1.dp.toPx()
                    val dashLength = 6.dp.toPx()
                    val gapLength = 4.dp.toPx()

                    drawRoundRect(
                        color = color.copy(alpha = 0.4f),
                        size = size,
                        cornerRadius = CornerRadius(12.dp.toPx()),
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
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

    }
}



@Composable
fun CampusWidget(
    campus: Education
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(color = MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center){
            AsyncImage(
                model =  "",
                contentDescription = null,
                error = painterResource(R.drawable.school__1_),
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary)

            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text(
                text = campus.college,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${campus.course}",
                style = MaterialTheme.typography.bodyMedium,

            )

            Text(
                text = campus.specialization,
                style = MaterialTheme.typography.bodyMedium,

            )


//            if (campus?.isCurrent ?: false){
//                Text(
//                    text = "${campus.start } - Current",
//                    style = MaterialTheme.typography.bodyMedium
//                )
//            }

            Text(
                text = "${campus.start} - ${campus.end}",
                style = MaterialTheme.typography.bodyMedium,


            )

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTabRow(
    selectedIndex: Int,
    tabList: List<String> = emptyList(),
    isScrollable: Boolean = false,
    onTabSelected: (Int) -> Unit
) {
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        if (selectedIndex < tabPositions.size) {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                height = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        if (isScrollable) {
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                modifier = Modifier.fillMaxWidth(),
                divider = {},
                indicator = indicator,
                edgePadding = 16.dp,
                containerColor = Color.Transparent
            ) {
                TabItems(tabList, selectedIndex, onTabSelected)
            }
        } else {
            TabRow(
                selectedTabIndex = selectedIndex,
                modifier = Modifier.fillMaxWidth(),
                divider = {},
                indicator = indicator,
                containerColor = Color.Transparent
            ) {
                TabItems(tabList, selectedIndex, onTabSelected)
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun TabItems(
    tabList: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    tabList.forEachIndexed { index, title ->
        val isSelected = selectedIndex == index
        Tab(
            selected = isSelected,
            onClick = { onTabSelected(index) },
            text = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        letterSpacing = 0.sp
                    ),
                    maxLines = 1,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            interactionSource = remember { MutableInteractionSource() }
        )
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

@Composable
fun ConnectionComponent(
    navHostController: NavHostController,
    pagerState: PagerState,
    followersCount: Int,
    connectionCount: Int,
    postsCountCount: Int,
    userId: String
) {

    val scope = rememberCoroutineScope()


    Row {


        Column(
            modifier = Modifier.weight(1f)
                .clickable(
                    onClick = {
                        navHostController.navigate(Followers(userId = userId))
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = followersCount.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Followers", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        }

        VerticalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.height(56.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
                .clickable(
                    onClick = {
                        navHostController.navigate(Connections(userId = userId))
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = connectionCount.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Connections", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        VerticalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.height(56.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
                .clickable(
                    onClick = {
                        scope.launch {
                            pagerState.scrollToPage(1)
                        }
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
            ,horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = postsCountCount.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Posts", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        }


    }


}
