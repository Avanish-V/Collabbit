package com.voxcii.voxcii.Screens.SearchFlow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon

import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import com.iota.campusX.Feature.Chats.data.UserChatsDTO
import com.iota.campusX.R
import com.iota.campusX.Screens.Chat.convertTimestampToTime
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.White900

@OptIn(ExperimentalMaterial3Api::class,)
@Composable
fun SearchScreen(navHostController: NavHostController) {

    var searchValue by remember {
        mutableStateOf("")
    }

    Column(modifier = Modifier.background(color = White900)) {

        SearchBar(

            inputField = {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .weight(1f),
                        value = searchValue,
                        onValueChange = { searchValue = it },
                        placeholder = {
                            Text(
                                text = "Search",
                                color = Black900
                            )
                        },
                        textStyle = TextStyle(
                            fontWeight = FontWeight.Bold,
                        ),
                        colors = TextFieldDefaults.colors(

                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Black
                        ),
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(22.dp),
                                painter = painterResource(R.drawable.search_normal),
                                contentDescription = null
                            )
                        },
                        shape = RoundedCornerShape(32.dp)
                    )
                }

            },
            expanded = true,
            onExpandedChange = {

            },
            shape = RoundedCornerShape(10.dp),
            colors = SearchBarDefaults.colors(
                containerColor = White900,
                dividerColor = Color.Transparent
            )

        ) {



        }

    }
}

@Composable
fun MentorSingleCard(chatItem: UserChatsDTO, onClick: () -> Unit) {


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
                    .size(60.dp)
                    .clip(CircleShape),
                model = chatItem.userImage,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    chatItem.userName,
                    color = Black900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis

                )
                Text(
                    chatItem.lastMessage.lastMessage,
                    color = Black500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(convertTimestampToTime(chatItem.lastMessage.timeStamp))

        }
    }
}



