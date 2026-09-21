package com.iota.campusX.Feature.Opportunities.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Opportunities.data.model.CourseResponse
import com.iota.campusX.Feature.Opportunities.data.model.ModuleResponse
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.theme.*
import com.iota.campusX.Utils.shareCourse
import com.iota.campusX.Utils.UiState
import com.iota.campusX.R
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    navController: NavHostController,
    viewModel: CourseDetailViewModel = koinViewModel()
) {
    val courseState by viewModel.courseState.collectAsStateWithLifecycle()
    val modulesState by viewModel.modulesState.collectAsStateWithLifecycle()
    val isEnrolling by viewModel.enrollmentLoading.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(courseId) {
        viewModel.fetchCourseDetail(courseId)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is CourseUiEvent.EnrollSuccess -> {
                    showSuccessDialog = true
                }
                is CourseUiEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = success, modifier = Modifier.size(48.dp)) },
            title = { Text(stringResource(R.string.registration_successful), fontWeight = FontWeight.Black) },
            text = { Text(stringResource(R.string.registration_success_message)) },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.got_it), color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {

                    if (courseState is UiState.Success) {
                        val course = (courseState as UiState.Success<CourseResponse>).data
                        IconButton(onClick = { 
                            scope.launch {
                                shareCourse(context, course.id, course.title, course.thumbnail) 
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share), tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (courseState is UiState.Success) {
                val course = (courseState as UiState.Success<CourseResponse>).data
                WorkshopBottomBar(course, isEnrolling, uriHandler) {
                    viewModel.enrollInCourse(courseId)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = courseState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                    }
                }
                is UiState.Success -> {
                    WorkshopDetailContent(state.data, modulesState)
                }
                is UiState.Error -> {
                    ErrorScreen(text = state.message, onReTry = { viewModel.fetchCourseDetail(courseId) })
                }
                else -> {}
            }
        }
    }
}

@Composable
fun WorkshopDetailContent(course: CourseResponse, modulesState: UiState<List<ModuleResponse>>) {
    val learningPoints = remember(course.summary) {
        course.summary?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Hero Image
        item {
            Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                AsyncImage(
                    model = course.thumbnail,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
                )

                Surface(
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = course.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }

                Surface(
                    modifier = Modifier.padding(16.dp).align(Alignment.TopEnd),
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                ) {
                    val isFree = course.price.equals("Free", true) || course.price == "0"
                    Text(
                        text = if (isFree) stringResource(R.string.free) else stringResource(R.string.price_format, course.price),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        }

        // 2. Info Section
        item {
            Column(modifier = Modifier.padding(24.dp)) {
                val isEnded = course.sessionStatus.equals("ENDED", ignoreCase = true) || course.skillState.equals("ENDED", ignoreCase = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailCategoryChip(text = course.category, color = MaterialTheme.colorScheme.primaryContainer, textColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    DetailCategoryChip(text = course.level, color = MaterialTheme.colorScheme.secondaryContainer, textColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    if (isEnded) {
                        DetailCategoryChip(
                            text = stringResource(R.string.ended).uppercase(),
                            color = MaterialTheme.colorScheme.errorContainer,
                            textColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    } else if (course.isJoinLinkEnabled && !course.meetLink.isNullOrBlank()) {
                        DetailCategoryChip(
                            text = "LIVE",
                            color = Color(0xFFE8F5E9),
                            textColor = Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = course.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, fontSize = 24.sp, lineHeight = 32.sp, color = MaterialTheme.colorScheme.onSurface)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconDetail(R.drawable.calendar, course.date ?: stringResource(R.string.tba))
                    IconDetail(R.drawable.clock, course.time ?: stringResource(R.string.tba))
                    IconDetail(R.drawable.clock, stringResource(R.string.duration_min, course.duration))
                }

            }
        }

        // 3. Instructor Card
        item {
            Surface(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = Color.LightGray) {
                        AsyncImage(
                            model = course.instructorProfile?.avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.user_normal)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = course.instructorProfile?.name ?: course.instructor,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(
                            text = course.instructorProfile?.bio ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 4. Modern Stats Card
        item {
            ModernSeatAvailabilityCard(
                totalSeats = course.seats ?: 0,
                enrolled = course.enrolled ?: 0,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 5. What you'll learn
        if (learningPoints.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(text = stringResource(R.string.what_you_will_learn), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
                    Spacer(modifier = Modifier.height(16.dp))

                    learningPoints.forEach { point ->
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = success, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = point, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 6. Workshop Content
        item {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = stringResource(R.string.workshop_content), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface))
                Spacer(modifier = Modifier.height(20.dp))

                when (modulesState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    is UiState.Success -> {
                        modulesState.data.forEachIndexed { index, module ->
                            ExpandableModuleItem(index + 1, module)
                            if (index < modulesState.data.size - 1) Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                    is UiState.Error -> {
                        Text(text = modulesState.message, color = MaterialTheme.colorScheme.error)
                    }
                    else -> {}
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun ExpandableModuleItem(index: Int, module: ModuleResponse) {
    var expanded by remember { mutableStateOf(index == 1) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = index.toString(), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = module.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                )
                Text(
                    text = stringResource(R.string.topics_count, module.topics.size),
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 16.dp)) {
                    module.topics.forEach { topic ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                            Spacer(Modifier.width(12.dp))
                            Text(text = topic, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernSeatAvailabilityCard(
    totalSeats: Int,
    enrolled: Int,
    modifier: Modifier = Modifier
) {
    val remainingSeats = (totalSeats - enrolled).coerceAtLeast(0)
    val progress = if (totalSeats > 0) enrolled.toFloat() / totalSeats else 0f

    val statusColor = when {
        remainingSeats <= 0 -> MaterialTheme.colorScheme.error
        remainingSeats < 10 -> warning
        else -> success
    }

    Surface(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.availability),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = stringResource(R.string.total_seats_format, totalSeats),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (remainingSeats < 10 && remainingSeats > 0) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = warning.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text(
                        text = stringResource(R.string.filling_fast),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = warning
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = enrolled.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        text = stringResource(R.string.enrolled),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = remainingSeats.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = statusColor
                        )
                    )
                    Text(
                        text = stringResource(R.string.seats_left),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DetailCategoryChip(text: String, color: Color, textColor: Color) {
    Surface(color = color, shape = RoundedCornerShape(6.dp)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = textColor)
        )
    }
}

@Composable
fun IconDetail(icon: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painter = painterResource(icon), null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun WorkshopBottomBar(
    course: CourseResponse,
    isEnrolling: Boolean,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    onEnroll: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp).navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val isEnded = course.sessionStatus.equals("ENDED", ignoreCase = true) || course.skillState.equals("ENDED", ignoreCase = true)
            val canJoin = !isEnded && course.isJoinLinkEnabled && !course.meetLink.isNullOrBlank()

            if (isEnded) {
                Button(
                    onClick = { /* Disabled */ },
                    modifier = Modifier.height(48.dp).weight(1f),
                    shape = CircleShape,
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.session_ended),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (canJoin) {
                Button(
                    onClick = { course.meetLink?.let { uriHandler.openUri(it) } },
                    modifier = Modifier.height(48.dp).weight(1f),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.video_camera_alt), null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.join_live), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            } else {
                Button(
                    onClick = onEnroll,
                    modifier = Modifier.height(48.dp).weight(1f),
                    shape = CircleShape,
                    enabled = !isEnrolling,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isEnrolling) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(R.string.enroll_now), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}
