package com.iota.campusX.Feature.Society.presentation

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Society.domain.model.Community
import com.iota.campusX.Feature.Society.domain.model.SocietyMessage
import com.iota.campusX.Feature.Society.domain.model.MessageType
import com.iota.campusX.Feature.Society.domain.repository.SocietyUser
import com.iota.campusX.Utils.FileUtils
import com.iota.campusX.Navigation.VideoView
import com.iota.campusX.Navigation.PdfView
import com.iota.campusX.Navigation.SocietyInfo
import com.iota.campusX.Navigation.CommunityChat
import com.iota.campusX.ui.UIComponents.VideoPlayer
import com.iota.campusX.ui.UIComponents.PdfThumbnail
import com.iota.campusX.ui.UIComponents.AlertDialogWidget
import com.iota.campusX.Navigation.Profile
import com.iota.campusX.R
import com.iota.campusX.ui.UIComponents.FeedUI.LinkPreviewCard
import com.iota.campusX.ui.UIComponents.FeedUI.extractUrlFromText
import com.iota.campusX.ui.theme.attachmentDocument
import com.iota.campusX.ui.theme.attachmentImage
import com.iota.campusX.ui.theme.attachmentVideo
import com.iota.campusX.Navigation.CourseDetail
import com.iota.campusX.Feature.Opportunities.presentation.OpportunitiesViewModel
import com.iota.campusX.Utils.UiState
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocietyChatScreen(
    societyId: String,
    navHostController: NavHostController,
    viewModel: SocietyViewModel = koinInject()
) {
    val society by viewModel.listenToCommunity(societyId).collectAsStateWithLifecycle(initialValue = null)
    val joinedCommunities by viewModel.joinedCommunities.collectAsStateWithLifecycle()
    val isJoined = joinedCommunities.any { it.id == societyId }
    val isMembershipResolved by viewModel.isMembershipResolved.collectAsStateWithLifecycle()
    
    val currentUserId = viewModel.currentUserId
    if (currentUserId == null) return

    LaunchedEffect(isMembershipResolved, isJoined) {
        if (isMembershipResolved && !isJoined) {
            navHostController.navigate(SocietyInfo(id = societyId, openJoinSheet = true)) {
                popUpTo<CommunityChat> { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    var messageText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<SocietyMessage?>(null) }
    var selectedAttachments by remember { mutableStateOf<List<SelectedAttachment>>(emptyList()) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    
    var reactingTo by remember { mutableStateOf<SocietyMessage?>(null) }
    var viewingImageUrl by remember { mutableStateOf<String?>(null) }
    var isViewingSnap by remember { mutableStateOf(false) }
    var highlightedMessageId by remember { mutableStateOf<String?>(null) }
    
    var showOptionsSheet by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showCoursePickerSheet by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    
    val opportunitiesViewModel: OpportunitiesViewModel = koinViewModel()
    val coursesState by opportunitiesViewModel.coursesState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        opportunitiesViewModel.fetchCourses()
    }
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val onlineCount by viewModel.getOnlineCount(societyId).collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> 
            uris.forEach { uri ->
                try {
                    val context = navHostController.context
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(inputStream, null, options)
                    inputStream?.close()

                    var width = options.outWidth
                    var height = options.outHeight

                    context.contentResolver.openInputStream(uri)?.use { exifStream ->
                        val exifInterface = ExifInterface(exifStream)
                        val orientation = exifInterface.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || 
                            orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                            val temp = width
                            width = height
                            height = temp
                        }
                    }
                    
                    val aspectRatio = if (height > 0) width.toFloat() / height else 1f
                    
                    selectedAttachments = selectedAttachments + SelectedAttachment(
                        uri = uri,
                        type = MessageType.IMAGE,
                        width = width,
                        height = height,
                        aspectRatio = aspectRatio
                    )
                } catch (e: Exception) {
                    selectedAttachments = selectedAttachments + SelectedAttachment(
                        uri = uri,
                        type = MessageType.IMAGE,
                        aspectRatio = 1f
                    )
                }
            }
        }
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> 
            uris.forEach { uri ->
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(navHostController.context, uri)
                    val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
                    val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
                    val aspectRatio = if (height > 0) width.toFloat() / height else 1f
                    retriever.release()
                    
                    selectedAttachments = selectedAttachments + SelectedAttachment(
                        uri = uri,
                        type = MessageType.VIDEO,
                        width = width,
                        height = height,
                        aspectRatio = aspectRatio
                    )
                } catch (e: Exception) {
                    selectedAttachments = selectedAttachments + SelectedAttachment(
                        uri = uri,
                        type = MessageType.VIDEO,
                        aspectRatio = 1f
                    )
                }
            }
        }
    )

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris -> 
            uris.forEach { uri ->
                val fileName = try {
                    val cursor = navHostController.context.contentResolver.query(uri, null, null, null, null)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            if (nameIndex != -1) it.getString(nameIndex) else "Document.pdf"
                        } else "Document.pdf"
                    } ?: "Document.pdf"
                } catch (e: Exception) {
                    "Document.pdf"
                }
                
                selectedAttachments = selectedAttachments + SelectedAttachment(
                    uri = uri,
                    type = MessageType.FILE,
                    name = fileName
                )
            }
        }
    )

    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempCameraUri?.let { uri ->
                    var width = 1080
                    var height = 1920
                    var ratio = 9f / 16f
                    
                    try {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeStream(input, null, options)
                            if (options.outWidth > 0 && options.outHeight > 0) {
                                width = options.outWidth
                                height = options.outHeight
                            }
                        }
                        
                        context.contentResolver.openInputStream(uri)?.use { exifStream ->
                            val exifInterface = ExifInterface(exifStream)
                            val orientation = exifInterface.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )
                            if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || 
                                orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                                val temp = width
                                width = height
                                height = temp
                            }
                        }
                        ratio = if (height > 0) width.toFloat() / height else 9f/16f
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    selectedAttachments = selectedAttachments + SelectedAttachment(
                        uri = uri,
                        type = MessageType.SNAP,
                        width = width,
                        height = height,
                        aspectRatio = ratio
                    )
                }
            }
        }
    )

    val chatItems = remember(messages) {
        val items = mutableListOf<ChatListItem>()
        if (messages.isEmpty()) return@remember items

        val sortedMessages = messages.sortedByDescending { it.timestamp }
        
        for (i in sortedMessages.indices) {
            val currentMessage = sortedMessages[i]
            items.add(ChatListItem.MessageItem(currentMessage))
            
            val nextMessage = if (i + 1 < sortedMessages.size) sortedMessages[i + 1] else null
            
            if (nextMessage == null || !isSameDay(currentMessage.timestamp, nextMessage.timestamp)) {
                items.add(ChatListItem.DateHeader(currentMessage.timestamp))
            }
        }
        items
    }

    DisposableEffect(societyId, isJoined) {
        if (isJoined) {
            viewModel.startListeningToMessages(societyId)
            viewModel.updatePresence(societyId, true)
            viewModel.markMessagesAsRead(societyId)
        }
        onDispose {
            if (isJoined) {
                viewModel.stopListeningToMessages()
                viewModel.updatePresence(societyId, false)
            }
        }
    }

    if (society == null || !isMembershipResolved) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { 
                            navHostController.navigate(SocietyInfo(id = societyId))
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (society?.logoUrl != null) {
                                AsyncImage(
                                    model = society?.logoUrl?:"",
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = society?.name?.take(1)?.uppercase() ?: "#",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = society?.name ?: "general",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.sp
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = if (isJoined) "${society?.memberCount ?: 0} members, $onlineCount online" else "${society?.memberCount ?: 0} members",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navHostController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isJoined) {
                        IconButton(onClick = { showOptionsSheet = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (isJoined) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .navigationBarsPadding()
                        .padding(vertical = 8.dp)
                ) {
                    if (selectedAttachments.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(end = 12.dp)
                        ) {
                            items(selectedAttachments) { attachment ->
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    when (attachment.type) {
                                        MessageType.IMAGE -> {
                                            AsyncImage(
                                                model = attachment.uri,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        MessageType.VIDEO -> {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.video_camera_alt),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        MessageType.FILE -> {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.memo_circle_check),
                                                    contentDescription = null,
                                                    tint = Color(0xFFFF9800),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text(
                                                    text = attachment.name ?: "PDF",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }
                                        else -> {}
                                    }

                                    IconButton(
                                        onClick = {
                                            selectedAttachments = selectedAttachments.filter { it != attachment }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(20.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (replyingTo != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = replyingTo!!.senderName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = replyingTo!!.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { replyingTo = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { showAttachmentSheet = true },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = {
                                Text(
                                    text = "Message ${society?.name ?: "general"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            trailingIcon = {
                                Surface(
                                    onClick = {
                                        val file = java.io.File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
                                        val uri = androidx.core.content.FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            file
                                        )
                                        tempCameraUri = uri
                                        cameraLauncher.launch(uri)
                                    },
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(36.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFFFEB3B),
                                    shadowElevation = 4.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(R.drawable.outline_camera_alt_24),
                                            contentDescription = "Snap",
                                            tint = Color.Black,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = false,
                            maxLines = 4
                        )

                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank() || selectedAttachments.isNotEmpty()) {
                                    if (selectedAttachments.isEmpty()) {
                                        viewModel.sendSocietyMessage(
                                            societyId = societyId,
                                            text = messageText,
                                            replyingTo = replyingTo,
                                            type = MessageType.TEXT
                                        )
                                    } else {
                                        selectedAttachments.forEachIndexed { index, attachment ->
                                            viewModel.sendSocietyMessage(
                                                societyId = societyId,
                                                text = if (index == 0) messageText else "",
                                                replyingTo = if (index == 0) replyingTo else null,
                                                mediaUri = attachment.uri.toString(),
                                                type = attachment.type,
                                                width = attachment.width,
                                                height = attachment.height,
                                                aspectRatio = attachment.aspectRatio,
                                                fileName = attachment.name
                                            )
                                        }
                                    }
                                    messageText = ""
                                    replyingTo = null
                                    selectedAttachments = emptyList()
                                }
                            },
                            enabled = messageText.isNotBlank() || selectedAttachments.isNotEmpty(),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (messageText.isNotBlank() || selectedAttachments.isNotEmpty())
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.send_solid),
                                contentDescription = "Send",
                                tint = if (messageText.isNotBlank() || selectedAttachments.isNotEmpty())
                                    MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val listState = rememberLazyListState()

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isJoined) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(
                        top = if (!society?.pinnedMessageId.isNullOrBlank()) 84.dp else 16.dp,
                        bottom = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    items(chatItems, key = { 
                        when(it) {
                            is ChatListItem.MessageItem -> it.message.id
                            is ChatListItem.DateHeader -> "header_${it.timestamp}"
                        }
                    }) { item ->
                        when(item) {
                            is ChatListItem.MessageItem -> {
                                val message = item.message
                                val user by viewModel.getUser(message.senderId).collectAsState(initial = null)
                                SocietyMessageItem(
                                    message = message,
                                    resolvedUser = user,
                                    navHostController = navHostController,
                                    onProfileClick = { 
                                        navHostController.navigate(Profile(userId = message.senderId))
                                    },
                                    onLongClick = { reactingTo = message },
                                    currentUserId = viewModel.currentUserId ?: "",
                                    onReact = { emoji ->
                                        viewModel.reactToMessage(societyId, message.id, emoji)
                                    },
                                    onImageClick = { 
                                        viewingImageUrl = it 
                                        isViewingSnap = message.type == MessageType.SNAP
                                        if (message.type == MessageType.SNAP) {
                                            viewModel.openSnap(societyId, message.id)
                                        }
                                    },
                                    onVideoClick = { videoUrl ->
                                        navHostController.navigate(VideoView(videoUrl = videoUrl, thumbnailUrl = message.thumbnailUrl))
                                    },
                                    onPdfClick = { pdfUrl, fileName ->
                                        navHostController.navigate(PdfView(pdfUrl = pdfUrl, fileName = fileName))
                                    },
                                    onCancelUpload = { viewModel.cancelUpload(message.id) },
                                    isHighlighted = highlightedMessageId == message.id
                                )
                            }
                            is ChatListItem.DateHeader -> {
                                DateHeaderItem(timestamp = item.timestamp)
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                if (!society?.pinnedMessageId.isNullOrBlank()) {
                    val pinnedId = society!!.pinnedMessageId!!
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        PinnedMessageHeader(
                            text = society!!.pinnedMessageText ?: "Pinned Message",
                            onClick = {
                                val index = chatItems.indexOfFirst { 
                                    it is ChatListItem.MessageItem && it.message.id == pinnedId 
                                }
                                if (index != -1) {
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                        highlightedMessageId = pinnedId
                                        delay(2000)
                                        if (highlightedMessageId == pinnedId) {
                                            highlightedMessageId = null
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Join this society to see messages",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navHostController.navigate(SocietyInfo(id = societyId)) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View Society Details")
                    }
                }
            }
        }
    }

    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Add Attachment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                
                AttachmentListItem(
                    icon = painterResource(R.drawable.image),
                    title = "Photo",
                    subtitle = "Select photos from your gallery",
                    color = attachmentImage,
                    onClick = {
                        showAttachmentSheet = false
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
                AttachmentListItem(
                    icon = painterResource(R.drawable.film),
                    title = "Video",
                    subtitle = "Select videos from your gallery",
                    color = attachmentVideo,
                    onClick = {
                        showAttachmentSheet = false
                        videoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    }
                )
                AttachmentListItem(
                    icon = painterResource(R.drawable.file_text),
                    title = "Document",
                    subtitle = "Share PDF files",
                    color = attachmentDocument,
                    onClick = {
                        showAttachmentSheet = false
                        filePickerLauncher.launch("application/pdf")
                    }
                )
            }
        }
    }

    SocietyOptionsBottomSheet(
        showOptionsSheet = showOptionsSheet,
        sheetState = sheetState,
        onDismiss = { showOptionsSheet = false },
        onLeaveClick = {
            showOptionsSheet = false
            showLeaveDialog = true
        },
        onShareClick = {
            showOptionsSheet = false
            coroutineScope.launch {
                society?.let {
                    com.iota.campusX.Utils.shareSociety(
                        context = context,
                        societyId = it.id,
                        societyName = it.name,
                        imageUrl = it.logoUrl
                    )
                }
            }
        }
    )

    if (showLeaveDialog) {
        AlertDialogWidget(
            onDismiss = { showLeaveDialog = false },
            title = "Leave Society?",
            description = "Are you sure you want to leave ${society?.name}? You will no longer be able to see or send messages in this group.",
            positiveButtonText = "Leave",
            negativeButtonText = "Cancel",
            onPositiveClick = {
                society?.let { viewModel.leaveCommunity(it) }
                showLeaveDialog = false
                navHostController.popBackStack()
            },
            showLoading = false
        )
    }

    if (reactingTo != null) {
        ModalBottomSheet(
            onDismissRequest = { reactingTo = null },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "React to message",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val emojis = listOf("❤️", "😂", "😮", "😢", "😡", "👍", "🔥")
                    emojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable {
                                    viewModel.reactToMessage(societyId, reactingTo!!.id, emoji)
                                    reactingTo = null
                                }
                                .padding(8.dp),
                            fontSize = 24.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ListItem(
                    headlineContent = { Text("Reply") },
                    leadingContent = { Icon(painter = painterResource(R.drawable.outline_reply_24), contentDescription = null, modifier = Modifier.size(24.dp)) },
                    modifier = Modifier.clickable {
                        replyingTo = reactingTo
                        reactingTo = null
                    }
                )

                val isAdmin = society?.creatorId == currentUserId
                if (isAdmin) {
                    val isAlreadyPinned = society?.pinnedMessageId == reactingTo?.id
                    ListItem(
                        headlineContent = { Text(if (isAlreadyPinned) "Unpin Message" else "Pin Message") },
                        leadingContent = { 
                            Icon(
                                imageVector = Icons.Default.PushPin, 
                                contentDescription = null, 
                                modifier = Modifier.size(24.dp).rotate(45f)
                            ) 
                        },
                        modifier = Modifier.clickable {
                            if (isAlreadyPinned) {
                                viewModel.unpinMessage(societyId)
                            } else {
                                reactingTo?.let { viewModel.pinMessage(societyId, it) }
                            }
                            reactingTo = null
                        }
                    )
                }

                if (reactingTo?.senderId == currentUserId) {
                    ListItem(
                        headlineContent = { Text("Delete Message", color = MaterialTheme.colorScheme.error) },
                        leadingContent = { 
                            Icon(
                                painter = painterResource(id = R.drawable.trash), 
                                contentDescription = null, 
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.error
                            ) 
                        },
                        modifier = Modifier.clickable {
                            reactingTo?.let { viewModel.deleteMessage(societyId, it.id) }
                            reactingTo = null
                        }
                    )
                }
            }
        }
    }

    if (showCoursePickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCoursePickerSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Select Course to Share",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                when (val state = coursesState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.data) { course ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.sendSocietyMessage(
                                                societyId = societyId,
                                                text = course.title,
                                                type = MessageType.COURSE,
                                                mediaUri = course.id,
                                                thumbnailUrl = course.thumbnail
                                            )
                                            showCoursePickerSheet = false
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = course.thumbnail,
                                            contentDescription = null,
                                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop,
                                            error = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = course.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = course.instructor,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is UiState.Error -> {
                        Text(
                            text = "Error loading courses: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    else -> {}
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (viewingImageUrl != null) {
        ZoomableImageViewer(
            imageUrl = viewingImageUrl!!,
            isSnap = isViewingSnap,
            onDismiss = { 
                viewingImageUrl = null 
                isViewingSnap = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocietyOptionsBottomSheet(
    showOptionsSheet: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onLeaveClick: () -> Unit,
    onShareClick: () -> Unit
) {
    if (showOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.background)
                        .clickable { onShareClick() }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Society",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Share Society",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(1.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.background)
                        .clickable { onLeaveClick() }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Leave Society",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Leave Society",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun AttachmentTypeInfo(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AttachmentListItem(
    icon: Any,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is ImageVector -> Icon(icon as ImageVector, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                is Painter -> Icon(icon as Painter, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ZoomableImageViewer(
    imageUrl: String,
    isSnap: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    if (isSnap) {
        DisposableEffect(Unit) {
            val activity = context as? Activity
            val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            onDispose {
                activity?.requestedOrientation = originalOrientation
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
            var rotationAngle by remember { mutableStateOf(0f) }

            if (isSnap) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(40.dp),
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f
                )
            }

            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                onSuccess = { state ->
                    val size = state.painter.intrinsicSize
                    if (isSnap && size.width > size.height) {
                        rotationAngle = 90f
                    }
                },
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotationAngle)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1f) {
                                    scale = 1f
                                    offset = androidx.compose.ui.geometry.Offset.Zero
                                } else {
                                    scale = 3f
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale *= zoom
                            scale = scale.coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offset += pan
                            } else {
                                offset = androidx.compose.ui.geometry.Offset.Zero
                            }
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

data class SelectedAttachment(
    val uri: Uri,
    val type: MessageType,
    val name: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val aspectRatio: Float = 0f
)

sealed class ChatListItem {
    data class MessageItem(val message: SocietyMessage) : ChatListItem()
    data class DateHeader(val timestamp: Long) : ChatListItem()
}

@Composable
fun DateHeaderItem(timestamp: Long) {
    val dateText = remember(timestamp) {
        when {
            DateUtils.isToday(timestamp) -> "Today"
            DateUtils.isToday(timestamp + DateUtils.DAY_IN_MILLIS) -> "Yesterday"
            else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = dateText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun isSameDay(ts1: Long, ts2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = ts1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = ts2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun PinnedMessageHeader(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pinned Message",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun UploadProgressOverlay(
    progress: Float,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = Color.White,
                strokeWidth = 2.dp,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
            IconButton(
                onClick = onCancel,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SocietyMessageItem(
    message: SocietyMessage,
    resolvedUser: SocietyUser?,
    currentUserId: String,
    navHostController: NavHostController,
    onProfileClick: () -> Unit,
    onLongClick: () -> Unit,
    onReact: (String) -> Unit,
    onImageClick: (String) -> Unit,
    onVideoClick: (String) -> Unit,
    onPdfClick: (String, String?) -> Unit,
    onCancelUpload: () -> Unit,
    isHighlighted: Boolean = false
) {
    val displayName = resolvedUser?.name ?: message.senderName
    val displayAvatar = resolvedUser?.avatarUrl ?: message.senderAvatarUrl
    val nameColor = remember(message.senderId) {
        val colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF009688), Color(0xFF4CAF50), Color(0xFFFF9800))
        colors[Math.abs(message.senderId.hashCode()) % colors.size]
    }

    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            if (displayAvatar != null) {
                AsyncImage(
                    model = displayAvatar,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        val bubbleColor = if (isHighlighted) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }

        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(bubbleColor)
                .combinedClickable(
                    onClick = { },
                    onLongClick = onLongClick
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = nameColor
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    
                    if (message.isSending) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Sending",
                            modifier = Modifier
                                .size(12.dp)
                                .clickable { onCancelUpload() },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    } else if (message.isFailed) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Failed",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (message.replyToId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(24.dp)
                            .clip(CircleShape)
                            .background(nameColor.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = message.replyToName ?: "User",
                            style = MaterialTheme.typography.labelSmall,
                            color = nameColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = message.replyToText ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val uriHandler = LocalUriHandler.current
            if (message.text.isNotBlank()) {
                val annotatedText = remember(message.text) {
                    buildAnnotatedString {
                        val urlMatcher = android.util.Patterns.WEB_URL.matcher(message.text)
                        var lastIndex = 0
                        while (urlMatcher.find()) {
                            append(message.text.substring(lastIndex, urlMatcher.start()))
                            val url = urlMatcher.group()
                            pushStringAnnotation(tag = "URL", annotation = url)
                            withStyle(
                                style = SpanStyle(
                                    color = Color(0xFF3897F0),
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(url)
                            }
                            pop()
                            lastIndex = urlMatcher.end()
                        }
                        append(message.text.substring(lastIndex))
                    }
                }

                ClickableText(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    ),
                    onClick = { offset ->
                        val annotations = annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        if (annotations.isNotEmpty()) {
                            val annotation = annotations.first()
                            val url = if (!annotation.item.startsWith("http")) "https://${annotation.item}" else annotation.item
                            try { uriHandler.openUri(url) } catch (_: Exception) { }
                        }
                    }
                )
            }

            when (message.type) {
                MessageType.IMAGE -> {
                    message.mediaUrl?.let { url ->
                        Spacer(modifier = Modifier.height(8.dp))
                        val ratio = if (message.aspectRatio > 0) message.aspectRatio.coerceIn(0.5f, 2f) else 1f
                        Box(
                            modifier = Modifier
                                .aspectRatio(ratio)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Image message",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .combinedClickable(
                                        onClick = { onImageClick(url) },
                                        onLongClick = onLongClick
                                    ),
                                contentScale = ContentScale.Crop
                            )
                            if (message.isSending) {
                                UploadProgressOverlay(progress = message.uploadProgress, onCancel = onCancelUpload)
                            }
                        }
                    }
                }
                MessageType.SNAP -> {
                    message.mediaUrl?.let { url ->
                        val isOpenedByMe = message.openedBy[currentUserId] == true
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (isOpenedByMe) {
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        painter = painterResource(R.drawable.eye),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Opened",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            val portraitRatio = 9f / 16f
                            Box(
                                modifier = Modifier
                                    .width(180.dp)
                                    .aspectRatio(portraitRatio)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black)
                                    .combinedClickable(
                                        onClick = { onImageClick(url) },
                                        onLongClick = onLongClick
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Snap message",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .blur(radius = 30.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.eye),
                                        contentDescription = null,
                                        modifier = Modifier.size(42.dp),
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                Surface(
                                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                                    color = Color(0xFFFFEB3B),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "SNAP",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                if (message.isSending) {
                                    UploadProgressOverlay(progress = message.uploadProgress, onCancel = onCancelUpload)
                                }
                            }
                        }
                    }
                }
                MessageType.VIDEO -> {
                    message.mediaUrl?.let { url ->
                        Spacer(modifier = Modifier.height(8.dp))
                        val ratio = if (message.aspectRatio > 0) message.aspectRatio.coerceIn(0.5f, 2f) else 16f/9f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .aspectRatio(ratio)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().combinedClickable(
                                    onClick = { onVideoClick(url) },
                                    onLongClick = onLongClick
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                message.thumbnailUrl?.let { thumb ->
                                    AsyncImage(
                                        model = thumb,
                                        contentDescription = "Video thumbnail",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Box(
                                    modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            if (message.isSending) {
                                UploadProgressOverlay(progress = message.uploadProgress, onCancel = onCancelUpload)
                            }
                        }
                    }
                }
                MessageType.FILE -> {
                    message.mediaUrl?.let { url ->
                        Spacer(modifier = Modifier.height(8.dp))
                        val fileName = message.fileName
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
                            Surface(
                                modifier = Modifier.combinedClickable(
                                    onClick = { onPdfClick(url, fileName) },
                                    onLongClick = onLongClick
                                ),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Column {
                                    PdfThumbnail(
                                        url = url,
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    )
                                    Row(
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(32.dp).background(Color(0xFFF44336).copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = "PDF",
                                                tint = Color(0xFFF44336),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = fileName ?: "Document.pdf",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "PDF Document",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            if (message.isSending) {
                                UploadProgressOverlay(progress = message.uploadProgress, onCancel = onCancelUpload)
                            }
                        }
                    }
                }
                MessageType.COURSE -> {
                    message.mediaUrl?.let { courseId ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(0.85f).combinedClickable(
                                onClick = { navHostController.navigate(CourseDetail(courseId = courseId)) },
                                onLongClick = onLongClick
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column {
                                Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                                    AsyncImage(
                                        model = message.thumbnailUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        error = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
                                    )
                                    Surface(
                                        modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "COURSE",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = message.text,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { navHostController.navigate(CourseDetail(courseId = courseId)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 0.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    ) {
                                        Text("View Course", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
            }

            val url = remember(message.text) { extractUrlFromText(message.text) }
            if (url != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinkPreviewCard(url = url) {
                    try { uriHandler.openUri(url) } catch (_: Exception) {}
                }
            }

            if (message.reactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    message.reactions.forEach { (emoji, users) ->
                        val isMe = users.contains(currentUserId)
                        Surface(
                            modifier = Modifier.clickable { onReact(emoji) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 14.sp)
                                if (users.size > 1) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = users.size.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.clickable { onLongClick() },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mood,
                            contentDescription = "React",
                            modifier = Modifier.padding(4.dp).size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
