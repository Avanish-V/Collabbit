package com.iota.campusX.Feature.Reply.presentation

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.iota.campusX.Feature.Post.presentation.components.PostAction
import com.iota.campusX.Feature.Reply.data.remote.request.ReplyRequest
import com.iota.campusX.Feature.Reply.data.remote.response.ReplyResponse
import com.iota.campusX.Feature.Reply.domain.model.MentionBuilder
import com.iota.campusX.Feature.Reply.presentation.components.BottomTextInput
import com.iota.campusX.Feature.Reply.presentation.components.ReplyWidget
import com.iota.campusX.R
import com.iota.campusX.Utils.CircularLoading
import com.iota.campusX.Utils.UiState
import org.koin.compose.koinInject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyBottomSheet(
    postId: String,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    replyViewModel: ReplyViewModel = koinInject(),
    onMoreClick:(ReplyResponse)-> Unit,
    onAction:(PostAction)-> Unit
) {
    val postRepliesState by replyViewModel.postReplies.collectAsState()
    val childReplies by replyViewModel.childReplies.collectAsState()
    val childLoadingStates by replyViewModel.childLoadingStates.collectAsState()
    val replyText by replyViewModel.replyText.collectAsState()
    val pickedImage by replyViewModel.pickedImage.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> replyViewModel.onImagePicked(uri) }

    val createReplyState by replyViewModel.createReplyState.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val snackBarHostState = remember { SnackbarHostState() }
    var mentionBuilder by remember { mutableStateOf<MentionBuilder?>(null) }

    LaunchedEffect(postId) {
        replyViewModel.fetchPostReplies(postId)
    }
    LaunchedEffect(createReplyState) {
        if (createReplyState is UiState.Error) {
            snackBarHostState.showSnackbar((createReplyState as UiState.Error).message)
        }
        if (createReplyState is UiState.Success) {
            mentionBuilder = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.2.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Comments List
            Box(modifier = Modifier.weight(1f, fill = false)) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    when (val repliesState = postRepliesState) {
                        is UiState.Loading -> {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularLoading(MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        is UiState.Error -> {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = repliesState.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        is UiState.Success -> {
                            val orderedReplies = repliesState.data

                            if (orderedReplies.isEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 80.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "No comments yet",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        Text(
                                            text = "Be the first to share what you think!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.7f
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                items(orderedReplies) { reply ->
                                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                        ReplyWidget(
                                            reply = reply,
                                            childReplies = childReplies,
                                            childLoadingStates = childLoadingStates,
                                            onDotsClick = {
                                                onMoreClick.invoke(reply)
                                            },
                                            onReplyClick = {
                                                mentionBuilder = it
                                                focusRequester.requestFocus()
                                            },
                                            onLikeClick = { replyId, isLiked, authorId ->
                                                replyViewModel.toggleReplyLike(authorId, replyId, isLiked)
                                            },
                                            onLoadChildren = {
                                                replyViewModel.fetchChildReplies(it)
                                            }
                                        )
                                    }
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.padding(start = 62.dp)
                                    )
                                }
                            }
                        }

                        else -> {}
                    }
                }

                SnackbarHost(
                    hostState = snackBarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }

            // Sticky Input Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .imePadding()
            ) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )

                pickedImage?.let {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .size(120.dp, 160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.TopEnd,
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(it).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { replyViewModel.onImagePicked(null) },
                            modifier = Modifier
                                .padding(8.dp)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove image",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                BottomTextInput(
                    focusRequester = focusRequester,
                    onFocusChange = {},
                    text = replyText,
                    onTextChange = { replyViewModel.onReplyTextChange(it) },
                    onSubmitClick = {
                        replyViewModel.createReply(
                            replyRequest = ReplyRequest(
                                content = replyText,
                                imageUrl = null,
                                parentReplyId = mentionBuilder?.parentId,
                                mentionedUserId = mentionBuilder?.mentionedUserId
                            ),
                            feedId = postId
                        )
                    },
                    isLoading = createReplyState is UiState.Loading,
                    mentionBuilder = mentionBuilder,
                    onCancelMention = { mentionBuilder = null },
                    onImagePick = { launcher.launch("image/*") },
                )
            }
        }
    }
}
