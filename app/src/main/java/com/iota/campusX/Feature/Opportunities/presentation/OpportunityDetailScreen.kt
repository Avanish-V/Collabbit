package com.iota.campusX.Feature.Opportunities.presentation

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Opportunities.data.model.OpportunityResponse
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.Utils.shareOpportunity
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.launch
import com.iota.campusX.ui.theme.success
import com.iota.campusX.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpportunityDetailScreen(
    opportunityId: String,
    navController: NavHostController,
    viewModel: OpportunityDetailViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val opportunityState by viewModel.opportunityState.collectAsStateWithLifecycle()
    val isApplying by viewModel.applicationLoading.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(opportunityId) {
        viewModel.fetchOpportunityDetail(opportunityId)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is OpportunityUiEvent.ApplySuccess -> {
                    showSuccessDialog = true
                }
                is OpportunityUiEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = success, modifier = Modifier.size(48.dp)) },
            title = { Text("Application Sent", fontWeight = FontWeight.Black) },
            text = { Text("Your profile has been successfully shared with the recruitment team.") },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("Awesome", fontWeight = FontWeight.Bold, color = success)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (opportunityState is UiState.Success) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.applyForOpportunity(opportunityId) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = !isApplying
                        ) {
                            if (isApplying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text = "Apply Now",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        
                        OutlinedButton(
                            onClick = { 
                                val opportunity = (opportunityState as? UiState.Success)?.data
                                if (opportunity != null) {
                                    scope.launch {
                                        shareOpportunity(context, opportunityId, opportunity.title, opportunity.companyLogoUrl)
                                    }
                                }
                            },
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Icon(Icons.Outlined.Share, "Share", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = opportunityState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Success -> {
                    OpportunityContent(state.data)
                }
                is UiState.Error -> {
                    ErrorScreen(text = state.message, onReTry = { viewModel.fetchOpportunityDetail(opportunityId) })
                }
                else -> {}
            }
        }
    }
}

@Composable
fun OpportunityContent(opportunity: OpportunityResponse) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            HeaderSection(opportunity)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            SectionHeader("About the role")
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = opportunity.description,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (opportunity.responsibilities.isNotEmpty()) {
            item {
                SectionHeader("What you'll do")
                Spacer(modifier = Modifier.height(16.dp))
                opportunity.responsibilities.forEach { item ->
                    ListItem(text = item, icon = Icons.Default.CheckCircle, iconTint = success)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (opportunity.requirements.isNotEmpty()) {
            item {
                SectionHeader("Requirements")
                Spacer(modifier = Modifier.height(16.dp))
                opportunity.requirements.forEach { item ->
                    ListItem(text = item, isDot = true)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        } else if (opportunity.skills.isNotEmpty()) {
            item {
                SectionHeader("Requirements")
                Spacer(modifier = Modifier.height(16.dp))
                opportunity.skills.forEach { skill ->
                    ListItem(text = "Familiarity with $skill", isDot = true)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        item {
            CompanySection(opportunity)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            ApplicationDetailsCard(opportunity)
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CompanySection(opportunity: OpportunityResponse) {
    Column {
        SectionHeader("About Company")
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (opportunity.companyLogoUrl != null) {
                        AsyncImage(
                            model = opportunity.companyLogoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(8.dp).clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.briefcase),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = opportunity.displayCompanyName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                val subtitle = buildString {
                    opportunity.companyIndustry?.let { append(it) }
                    if (!opportunity.companyIndustry.isNullOrEmpty() && !opportunity.companyCity.isNullOrEmpty()) {
                        append(" • ")
                    }
                    opportunity.companyCity?.let { append(it) }
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (opportunity.companyWebsite != null) {
            Spacer(modifier = Modifier.height(20.dp))
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            OutlinedButton(
                onClick = { uriHandler.openUri(opportunity.companyWebsite) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.globe),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Visit Website",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun HeaderSection(opportunity: OpportunityResponse) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (opportunity.companyLogoUrl != null) {
                        AsyncImage(
                            model = opportunity.companyLogoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(12.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.briefcase),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = opportunity.displayCompanyName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = success.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = success
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verified",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = success
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = opportunity.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoChip(icon = R.drawable.land_layer_location, text = opportunity.location)
            InfoChip(icon = R.drawable.briefcase, text = opportunity.type)
            InfoChip(icon = R.drawable.indian_rupee_sign, text = "${opportunity.stipend}/Month")
        }
    }
}

@Composable
fun InfoChip(icon: Int, text: String) {
    Surface(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun ListItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconTint: Color = success,
    isDot: Boolean = false
) {
    Row(verticalAlignment = Alignment.Top) {
        if (isDot) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, start = 4.dp, end = 12.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary) // Blue dot as per image
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp).padding(top = 2.dp),
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ApplicationDetailsCard(opportunity: OpportunityResponse) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Application Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Posted on", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = opportunity.posted,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Apply by", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = opportunity.deadline ?: "Open",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (opportunity.durationMonths != null) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Duration", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${opportunity.durationMonths} Months",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
