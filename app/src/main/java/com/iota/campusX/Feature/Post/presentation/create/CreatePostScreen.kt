package com.iota.campusX.Feature.Post.presentation.create

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Post.domain.attachment.DocumentAttachment
import com.iota.campusX.Feature.Post.presentation.components.DocumentAttachmentCard
import com.iota.campusX.Feature.Post.presentation.components.DocumentHorizontalPager
import com.iota.campusX.Feature.Post.presentation.components.PdfPageImage
import com.iota.campusX.Feature.Post.presentation.components.rememberPdfRenderer
import com.iota.campusX.Feature.Post.domain.attachment.ImageAttachment
import com.iota.campusX.Feature.Post.domain.attachment.TeamFormationAttachment
import com.iota.campusX.Feature.Post.domain.attachment.VideoAttachment
import com.iota.campusX.Feature.UserProfile.data.remote.response.SkillResponse
import com.iota.campusX.Feature.UserProfile.ui.Components.SearchableDropdown
import com.iota.campusX.Feature.UserProfile.ui.screens.ProfileMain.UserProfileViewModel
import com.iota.campusX.R
import com.iota.campusX.Utils.CircularLoading
import com.iota.campusX.Utils.CustomTextField
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.VideoPlayer
import com.iota.campusX.ui.theme.attachmentDocument
import com.iota.campusX.ui.theme.attachmentImage
import com.iota.campusX.ui.theme.attachmentLocation
import com.iota.campusX.ui.theme.attachmentVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen(
    navHostController: NavHostController,
    userProfileViewModel: UserProfileViewModel,
    postCreationViewModel: PostCreationViewModel,
) {


    val draft by postCreationViewModel.draft.collectAsState()
    val uiState by postCreationViewModel.uiState.collectAsState()
    val profileState = userProfileViewModel.uiState.collectAsState().value
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4),
        onResult = { uris -> 
            if (uris.isNotEmpty()) {
                postCreationViewModel.onImagesSelected(context, uris)
            }
        }
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> 
            if (uri != null) {
                postCreationViewModel.onVideoSelected(context, uri)
            }
        }
    )

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                postCreationViewModel.onDocumentSelected(context, uri)
            }
        }
    )

    LaunchedEffect(profileState) {
        userProfileViewModel.load(null)
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
        }
        if (uiState is UiState.Success) {
            navHostController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Create Post",
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            postCreationViewModel.publishPost()
                        },
                        enabled = (draft.caption.isNotBlank() || draft.attachment != null) && uiState !is UiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        if (uiState is UiState.Loading) {
                            CircularLoading(Color.White)
                        } else {
                            Text("Post", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.Top) {
                        AsyncImage(
                            model = profileState.profile?.baseProfile?.image,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profileState.profile?.baseProfile?.name ?: "User",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))

                            BasicTextField(
                                value = draft.caption,
                                onValueChange = { if (it.length <= 1000) postCreationViewModel.onCaptionChanged(it) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (draft.caption.isEmpty()) {
                                            Text(
                                                text = "What's on your mind?",
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )

                            // Media Preview
                            AnimatedVisibility(
                                visible = draft.attachment is ImageAttachment || draft.attachment is VideoAttachment || draft.attachment is DocumentAttachment,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                when (val attachment = draft.attachment) {
                                    is ImageAttachment -> {
                                        FlowRow(
                                            modifier = Modifier.padding(top = 16.dp).clip(RoundedCornerShape(12.dp)),
                                            maxItemsInEachRow = 2,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            attachment.images.forEachIndexed { index, uri ->
                                                val ratio = attachment.aspectRatios.getOrNull(index) ?: 1f
                                                Box(modifier = Modifier.weight(1f)) {
                                                    MediaPreviewItem(
                                                        uri = uri,
                                                        ratio = ratio,
                                                        onRemove = { 
                                                            context.vibrate()
                                                            postCreationViewModel.removeAttachment() 
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    is VideoAttachment -> {
                                        Box(modifier = Modifier.padding(top = 16.dp)) {
                                            MediaPreviewItem(
                                                uri = attachment.videoUri,
                                                ratio = attachment.aspectRatio,
                                                isVideo = true,
                                                onRemove = { 
                                                    context.vibrate()
                                                    postCreationViewModel.removeAttachment() 
                                                }
                                            )
                                        }
                                    }
                                    is DocumentAttachment -> {
                                        Box(modifier = Modifier.padding(top = 16.dp)) {
                                            val renderer = rememberPdfRenderer(Uri.parse(attachment.uri))
                                            val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { attachment.pageCount })
                                            
                                            DocumentAttachmentCard(
                                                name = attachment.name,
                                                headerActions = {
                                                    IconButton(
                                                        onClick = {
                                                            context.vibrate()
                                                            postCreationViewModel.removeAttachment()
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Remove",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            ) {
                                                DocumentHorizontalPager(
                                                    pageCount = attachment.pageCount,
                                                    pagerState = pagerState
                                                ) { pageIndex ->
                                                    PdfPageImage(
                                                        renderer = renderer,
                                                        pageIndex = pageIndex,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    else -> {}
                                }
                            }

                            if (draft.attachment is TeamFormationAttachment) {
                                TeamFormationSection(
                                    attachment = draft.attachment as TeamFormationAttachment,
                                    creationViewModel = postCreationViewModel,
                                    onTypeChanged = { postCreationViewModel.onTeamTypeChanged(it) },
                                    onSkillsChanged = { },
                                    onRemove = { postCreationViewModel.removeAttachment() }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // Bottom Toolbar
            Surface(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {


                        AttachmentSelectorButton(
                            icon = painterResource(R.drawable.image),
                            tint = attachmentImage,
                            onClick = {
                                context.vibrate()
                                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        )

                        AttachmentSelectorButton(
                            icon = painterResource(R.drawable.film),
                            tint = attachmentVideo,
                            onClick = {
                                context.vibrate()
                                videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                            }
                        )

                        AttachmentSelectorButton(
                            icon = painterResource(R.drawable.file_text),
                            tint = attachmentDocument,
                            onClick = {
                                context.vibrate()
                                documentPickerLauncher.launch("application/pdf")
                            }
                        )

                    }

                    // Character Count / Progress
                    val progress = draft.caption.length / 2000f
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 3.dp,
                            color = if (draft.caption.length > 2000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = (2000 - draft.caption.length).toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaPreviewItem(
    uri: String,
    ratio: Float,
    isVideo: Boolean = false,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .heightIn(max = 420.dp) // Height cap for Threads style
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.05f))
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        val isPortrait = ratio < 1f
        val constrainedModifier = if (isPortrait) {
            Modifier
                .heightIn(max = 420.dp)
                .aspectRatio(ratio.coerceIn(0.6f, 1f))
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(ratio.coerceIn(1f, 1.91f))
        }

        if (isVideo) {
            VideoPlayer(
                videoUrl = uri,
                modifier = constrainedModifier
            )
        } else {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = constrainedModifier,
                contentScale = ContentScale.Crop
            )
        }

        // Aspect Ratio Label
        Surface(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomStart),
            shape = RoundedCornerShape(4.dp),
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Text(
                text = getAspectRatioText(ratio),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Surface(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopEnd)
                .size(24.dp)
                .clickable { onRemove() },
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.6f),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

fun getAspectRatioText(ratio: Float): String {
    return when {
        abs(ratio - 1f) < 0.05f -> "1:1"
        abs(ratio - 0.5625f) < 0.05f -> "9:16"
        abs(ratio - 1.777f) < 0.05f -> "16:9"
        abs(ratio - 0.8f) < 0.05f -> "4:5"
        abs(ratio - 1.25f) < 0.05f -> "5:4"
        abs(ratio - 0.75f) < 0.05f -> "3:4"
        abs(ratio - 1.333f) < 0.05f -> "4:3"
        else -> "%.2f".format(ratio)
    }
}

@Composable
fun AttachmentSelectorButton(
    icon: Any,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (icon) {
            is ImageVector -> Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            is Painter -> Icon(
                painter = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamFormationSection(
    attachment: TeamFormationAttachment,
    creationViewModel: PostCreationViewModel,
    onTypeChanged: (String) -> Unit,
    onSkillsChanged: (List<String>) -> Unit,
    onRemove: () -> Unit
) {

    var title by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Team Formation",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Post Type", 
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("Hackathon", "Project", "Startup").forEach { type ->
                    val selected = attachment.teamType == type
                    FilterChip(
                        selected = selected,
                        onClick = { onTypeChanged(type) },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = MaterialTheme.colorScheme.outline,
                            selectedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = {title = it.toString()},
                label = "Title",
                enabled = true,
                maxLines = 3,
                placeHolder = "Project Title",
                trailingIcon = {},
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Required Skills", 
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            val query by creationViewModel.skillQuery.collectAsStateWithLifecycle()
            val skillsQueryState by creationViewModel.skillsQueryState.collectAsState()
            var selectedQuery by remember { mutableStateOf<SkillResponse?>(null) }
            val skills by creationViewModel.skills.collectAsState()

            val skillList: List<SkillResponse> = when (val state = skillsQueryState) {
                is UiState.Success -> state.data
                else -> emptyList()
            }


            SearchableDropdown(
                label = "Search skills...",
                items = skillList,
                query = query,
                onQueryChange = {
                    creationViewModel.onSkillQueryChanged(it)
                },
                selectedItem = selectedQuery,
                itemText = { it.name},
                onItemSelected = {
                    creationViewModel.addSkill(it)
                    selectedQuery = it
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                skills.forEach {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        },
                        border = BorderStroke(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = {
                            Icon(
                                modifier = Modifier.size(16.dp).clickable(
                                    onClick = { creationViewModel.removeSkill(it) },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ),
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }
    }
}
