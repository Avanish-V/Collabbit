package com.iota.campusX.ui.UIComponents

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.Post.domain.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.PostActions
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Feature.Post.presentation.PostViewModel
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.BottomSheetSharedViewModel
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.generateUID
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.secondary
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDotOptionBottomSheet(
    isBottomSheet: Boolean,
    bottomSheetSharedViewModel: BottomSheetSharedViewModel,
    postViewModel: PostViewModel,
    onDismiss: () -> Unit,
    isCurrentUser: Boolean,
    onDeleteClick:()-> Unit,
    onEditClick:()-> Unit,
    onHideBottomSheet:(Boolean)-> Unit
) {

    val modificationRequest = bottomSheetSharedViewModel.modificationRequest.collectAsState().value
    val bottomSheetData = bottomSheetSharedViewModel.bottomSheetState.collectAsState().value

    val modifyType by remember {
        derivedStateOf {
            if (modificationRequest == "EDIT_POST") "Edit Post"
            else if (modificationRequest == "EDIT_REPLY") "Edit Reply"
            else ""
        }
    }

    if (isBottomSheet) {

        ModalBottomSheet(
            onDismissRequest = { onDismiss.invoke() },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            containerColor = Color.White,
        ) {



            if (modificationRequest == "EDIT_REPLY" || modificationRequest == "EDIT_POST"){

                val focusRequester = remember { FocusRequester() }
                var replyText  =  remember { mutableStateOf("") }
                var isLoading  =  remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current
                val keyboard = LocalSoftwareKeyboardController.current

                LaunchedEffect(Unit) {
                    if (modificationRequest == "EDIT_POST"){
                        replyText.value = bottomSheetData.postText
                    }
                }
                LaunchedEffect(Unit) {
                    if (modificationRequest == "EDIT_REPLY"){
                        replyText.value = bottomSheetData.replyText.toString()
                    }
                }

                Column {

                    Box(modifier = Modifier.fillMaxWidth().padding(12.dp),contentAlignment = Alignment.CenterStart){
                        Text(modifyType, color = primary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start)
                    }

                    HorizontalDivider(
                        color = Black500
                    )

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .focusRequester(focusRequester)
                            .onFocusChanged { focusState ->

                            },
                        value = replyText.value,
                        onValueChange = { replyText.value = it },
                        placeholder = {
                            Text("Write your comment...")
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    when (modificationRequest) {
                                        "EDIT_POST" -> {

                                            if (replyText.value.isEmpty()) return@IconButton

                                            scope.launch {
                                                postViewModel.editPost(
                                                    postId = bottomSheetData.postId,
                                                    campusId = bottomSheetData.campusId.toString(),
                                                    editedText = replyText.value
                                                ).collect {
                                                    when (it) {
                                                        is ResultState.Success -> {
                                                            postViewModel.updateEditPost(
                                                                postId = bottomSheetData.postId,
                                                                editedText = replyText.value ,
                                                            )
                                                            delay(1000)
                                                            isLoading.value = false
                                                            keyboard?.hide()
                                                            onHideBottomSheet(false)
                                                            bottomSheetSharedViewModel.setModificationRequest("")
                                                            replyText.value = ""
                                                        }

                                                        is ResultState.Error -> {
                                                            bottomSheetData.isBottomSheet = false
                                                            isLoading.value = false
                                                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                                                        }

                                                        is ResultState.Loading -> {
                                                            isLoading.value = true
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        "EDIT_REPLY" -> {

                                            if (replyText.value.isNotEmpty()) {

                                                scope.launch {
                                                    postViewModel.editReply(
                                                        postId = bottomSheetData.postId,
                                                        replyId = bottomSheetData.replyId.toString(),
                                                        campusId = bottomSheetData.campusId.toString(),
                                                        content = replyText.value
                                                    ).collect {
                                                        when (it) {
                                                            is ResultState.Success -> {
                                                                postViewModel.updateEditReply(
                                                                    postId = bottomSheetData.postId,
                                                                    replyId = bottomSheetData.replyId.toString(),
                                                                    content = replyText.value
                                                                )

                                                                isLoading.value = false
                                                                keyboard?.hide()
                                                                delay(1000)
                                                                onHideBottomSheet(false)
                                                                bottomSheetSharedViewModel.setModificationRequest("")
                                                                replyText.value = ""
                                                            }

                                                            is ResultState.Error -> {
                                                                bottomSheetData.isBottomSheet = false
                                                                isLoading.value = false
                                                                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                                                            }

                                                            is ResultState.Loading -> {
                                                                isLoading.value = true
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            ) {
                                if (isLoading.value){
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        trackColor = secondary,
                                        color = primary
                                    )
                                }else{
                                    Icon(
                                        painter = painterResource(R.drawable.send_2),
                                        contentDescription = "Send",
                                        tint = primary
                                    )
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = White900,
                            unfocusedContainerColor = White900,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTrailingIconColor = primary
                        )

                    )

                }

            } else{

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    if (isCurrentUser){

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.height(48.dp).fillMaxWidth().padding(start = 10.dp)
                                .background(
                                    color = secondary,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable(
                                    onClick = {
                                        onEditClick.invoke()
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                        ) {

                            Icon(
                                modifier = Modifier.size(22.dp),
                                painter = painterResource(R.drawable.edit),
                                contentDescription = null,
                            )

                            Text("Edit")

                        }


                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.height(48.dp).fillMaxWidth().padding(start = 10.dp)
                                .background(
                                    color = secondary,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable(
                                    onClick = {
                                        onDeleteClick.invoke()
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                        ) {

                            Icon(
                                modifier = Modifier.size(22.dp),
                                painter = painterResource(R.drawable.trash),
                                contentDescription = null,
                                tint = Color.Red
                            )

                            Text("Delete")

                        }


                    }

                    HorizontalDivider(
                        color = White400
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                            .padding(start = 10.dp)
                            .background(
                                color = secondary,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable(
                                onClick = {

                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )

                    ) {

                        Icon(
                            modifier = Modifier.size(22.dp),
                            painter = painterResource(R.drawable.warning_2),
                            contentDescription = null,
                            tint = Color.Red
                        )

                        Text("Report (Work in progress)", color = Color.Red)

                    }

                }

            }
        }

    }

}

enum class ReportReason(val displayName: String) {
    SPAM("Spam"),
    FALSE_INFO("False Information"),
    HATE_SPEECH("Hate Speech or Symbols"),
    HARASSMENT("Harassment or Bullying"),
    SEXUAL_CONTENT("Sexual Content"),
    VIOLENCE("Violence or Threats"),
    SELF_HARM("Suicide or Self-Injury"),
    IP_VIOLATION("Intellectual Property Violation"),
    FRAUD("Scam or Fraud"),
    OTHER("Other")
}
