package com.iota.campusX.Feature.Opportunities.presentation

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Opportunities.data.model.CourseResponse
import com.iota.campusX.Feature.Opportunities.data.model.ModuleResponse
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    navController: NavHostController,
    viewModel: OpportunitiesViewModel = koinViewModel()
) {
    val courseState by viewModel.selectedCourseState.collectAsStateWithLifecycle()
    val modulesState by viewModel.courseModulesState.collectAsStateWithLifecycle()
    val enrollmentState by viewModel.enrollmentState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(courseId) {
        viewModel.fetchCourseDetail(courseId)
        viewModel.fetchCourseModules(courseId)
    }

    LaunchedEffect(enrollmentState) {
        when (enrollmentState) {
            is UiState.Success -> {
                showSuccessDialog = true
                viewModel.resetEnrollmentState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (enrollmentState as UiState.Error).message,
                    actionLabel = "Retry",
                    duration = SnackbarDuration.Long
                ).let { result ->
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.enrollInCourse(courseId)
                    }
                }
                viewModel.resetEnrollmentState()
            }
            else -> {}
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp)) },
            title = { Text("Registration Successful!", fontWeight = FontWeight.Black) },
            text = { Text("You've successfully registered for this workshop. We've sent the details to your email.") },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {Text("Live Skill")},
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    if (courseState is UiState.Success) {
                        val course = (courseState as UiState.Success<CourseResponse>).data
                        IconButton(onClick = {
                            shareCourse(context, course)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share Course")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (courseState is UiState.Success) {
                val course = (courseState as UiState.Success<CourseResponse>).data
                CourseBottomBar(course, enrollmentState) {
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
                    CourseDetailContent(state.data, modulesState, navController, uriHandler)
                }
                is UiState.Error -> {
                    ErrorScreen(text = state.message, onReTry = { viewModel.fetchCourseDetail(courseId) })
                }
                else -> {}
            }
        }
    }
}

private fun shareCourse(context: android.content.Context, course: CourseResponse) {
    // Option B: Custom Scheme link (finder://) to bypass the browser entirely
    val deepLink = "finder://course/${course.id}"
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Check out this skill course: ${course.title}")
        putExtra(Intent.EXTRA_TEXT, "Hey! I found this interesting skill course on Finder: ${course.title}\n\nOpen directly in app: $deepLink")
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Course via"))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseDetailContent(
    course: CourseResponse,
    modulesState: UiState<List<ModuleResponse>>,
    navController: NavHostController,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Hero Banner
        item {
            AsyncImage(
                model = course.thumbnail,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
            )
        }

        // 2. Title & Category
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = course.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        lineHeight = 32.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }

        // 3. Instructor Profile
        item {
            Surface(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val instructorName = course.instructorProfile?.name ?: course.instructor
                    val instructorAvatar = course.instructorProfile?.avatarUrl

                    if (instructorAvatar != null) {
                        AsyncImage(
                            model = instructorAvatar,
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = instructorName.firstOrNull()?.toString()?.uppercase() ?: "F",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = instructorName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = course.instructorProfile?.bio ?: "Expert Industry Professional",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 4. Key Highlights (Naukri Style Grid)
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
                Text(
                    text = "Key Highlights",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(20.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 2,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HighlightBox(Icons.Default.Info, "Level", course.level, Modifier.weight(1f))
                    HighlightBox(Icons.Default.DateRange, "Duration", "${course.duration} mins", Modifier.weight(1f))
                    
                    course.date?.let { HighlightBox(Icons.Default.DateRange, "Date", it, Modifier.weight(1f)) }
                    course.time?.let { HighlightBox(Icons.Default.PlayArrow, "Time", it, Modifier.weight(1f)) }
                }
            }
        }

        // 4b. Seat Availability Progress Bar
        if (course.seats != null && course.enrolled != null) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    val filled = course.enrolled.toFloat()
                    val total = course.seats.toFloat()
                    val progress = if (total > 0) filled / total else 0f
                    val remaining = course.seats - course.enrolled
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Registration Progress",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (remaining > 0) "$remaining spots left" else "Fully Booked",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (remaining < 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (remaining < 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${course.enrolled} professionals already registered",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 5. About / Description
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                Text(
                    text = "About this Workshop",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // 6. Live CTA Card
        course.meetLink?.let { link ->
            item {
                Surface(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(20.dp),
                    onClick = { uriHandler.openUri(link) }
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Join Live Session",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = "Starts at ${course.time ?: "scheduled time"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.onPrimary, CircleShape)
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        // 7. Syllabus / Curriculum
        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    text = "Syllabus",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                when (modulesState) {
                    is UiState.Loading -> {
                         LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                    }
                    is UiState.Success -> {
                        val apiModules = modulesState.data
                        val displayModules = if (apiModules.isNotEmpty()) {
                            apiModules.sortedBy { it.order }
                        } else {
                            // Fallback to nested modules if API call returned empty but they exist in course object
                            course.modules
                        }

                        if (displayModules.isNotEmpty()) {
                            displayModules.forEachIndexed { index, module ->
                                NaukriModuleItem(index + 1, module, index == displayModules.size - 1)
                            }
                        } else {
                            Text(
                                "Detailed syllabus will be shared soon.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is UiState.Error -> {
                        // On error, still try to show nested modules if available
                        if (course.modules.isNotEmpty()) {
                            course.modules.forEachIndexed { index, module ->
                                NaukriModuleItem(index + 1, module, index == course.modules.size - 1)
                            }
                        } else {
                            Text("Curriculum currently unavailable", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    else -> {}
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
fun HighlightBox(
    icon: ImageVector, 
    label: String, 
    value: String, 
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, Modifier.size(18.dp), tint = contentColor)
            Spacer(Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NaukriModuleItem(index: Int, module: ModuleResponse, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (!isLast) {
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight().width(1.dp).padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = module.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            if (module.description.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun CourseBottomBar(
    course: CourseResponse,
    enrollmentState: UiState<Unit>,
    onEnroll: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp).navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                val isEnrolling = enrollmentState is UiState.Loading
                Button(
                    onClick = onEnroll,
                    modifier = Modifier.height(48.dp).fillMaxWidth(),
                    shape = CircleShape ,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isEnrolling
                ) {
                    if (isEnrolling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Enroll Now",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        }
    }
}
