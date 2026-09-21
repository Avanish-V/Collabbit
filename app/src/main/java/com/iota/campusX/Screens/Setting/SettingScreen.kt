package com.iota.campusX.Screens.Setting

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.GoogleSignInViewModel
import com.iota.campusX.Feature.UserProfile.ui.screens.ProfileMain.UserProfileViewModel
import com.iota.campusX.Navigation.AuthGraph
import com.iota.campusX.R
import com.iota.campusX.Utils.Setting
import com.iota.campusX.ui.UIComponents.AlertDialogWidget
import com.iota.campusX.ui.UIComponents.UserAvatar
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel,
    googleSignInViewModel: GoogleSignInViewModel = koinInject()
) {
    val uiState by userProfileViewModel.uiState.collectAsState()
    val profile = uiState.profile
    var screenValue by rememberSaveable { mutableStateOf(Setting.SETTING_SCREEN) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    
    var showAlert by remember { mutableStateOf(false) }
    var showLogoutAlert by remember { mutableStateOf(false) }

    when (screenValue) {
        Setting.SETTING_SCREEN -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                },
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // System Section
                    item {
                        SettingSection {
                            SettingItem(
                                title = "About",
                                icon = R.drawable.info,
                                onClick = { screenValue = Setting.ABOUT_SCREEN }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            SettingItem(
                                title = "Privacy Policy",
                                icon = R.drawable.user_lock,
                                onClick = { screenValue = Setting.PRIVACY_POLICY }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                        Spacer(modifier = Modifier.size(24.dp))
                    }

                    // App Actions
                    item {
                        SettingSection(title = "App Actions") {

                            SettingItem(
                                title = "Sign Out",
                                icon = R.drawable.undo__1_,
                                titleColor = MaterialTheme.colorScheme.error,
                                showArrow = false,
                                onClick = { showLogoutAlert = true }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            SettingItem(
                                title = "Delete Account",
                                icon = R.drawable.trash,
                                titleColor = MaterialTheme.colorScheme.error,
                                showArrow = false,
                                onClick = { showAlert = true }
                            )
                        }
                        Spacer(modifier = Modifier.size(32.dp))
                    }

                    item {
                        Text(
                            text = "Version 1.2.4 (Collabbit)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                    }
                }

                // Dialogs
                if (showAlert) {
                    AlertDialogWidget(
                        onDismiss = { showAlert = false },
                        title = "Delete Account",
                        description = "Are you sure you want to delete your account? This action is irreversible.",
                        positiveButtonText = "Delete",
                        negativeButtonText = "Cancel",
                        onPositiveClick = {
                            showAlert = false
                            uriHandler.openUri("https://collabbit.in/account/delete")
                        },
                        showLoading = false
                    )
                }

                if (showLogoutAlert) {
                    AlertDialogWidget(
                        onDismiss = { showLogoutAlert = false },
                        title = "Sign Out",
                        description = "Are you sure you want to sign out of your account?",
                        positiveButtonText = "Sign Out",
                        negativeButtonText = "Cancel",
                        onPositiveClick = {
                            showLogoutAlert = false
                            scope.launch {
                                googleSignInViewModel.signOut()
                                navController.navigate(AuthGraph) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        showLoading = false
                    )
                }
            }
        }

        Setting.ABOUT_SCREEN, Setting.PRIVACY_POLICY, Setting.TERMS_AND_CONDITIONS -> {
            val title = when(screenValue) {
                Setting.ABOUT_SCREEN -> "About"
                Setting.PRIVACY_POLICY -> "Privacy Policy"
                else -> "Terms & Conditions"
            }
            val url = when(screenValue) {
                Setting.ABOUT_SCREEN -> "https://www.collabbit.in/about"
                Setting.PRIVACY_POLICY -> "https://www.collabbit.in/privacy"
                else -> "https://www.collabbit.in/term-condition"
            }

            SettingPage(
                title = title,
                onBackClick = { screenValue = Setting.SETTING_SCREEN },
                content = {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webViewClient = WebViewClient()
                                loadUrl(url)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
        }
        else -> {}
    }
}

@Composable
fun ProfileHeaderCentered(
    name: String,
    imageUrl: String?,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(100.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    UserAvatar(
                        modifier = Modifier.fillMaxSize(),
                        imageUrl = imageUrl
                    )
                } else {
                    val initials = if (name.isNotEmpty()) {
                        name.split(" ").filter { it.isNotEmpty() }.take(2).map { it[0] }.joinToString("").uppercase()
                    } else "U"
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
            
            // Edit Icon
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clickable(onClick = onEditClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    modifier = Modifier.padding(6.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SettingSection(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String? = null,
    icon: Int,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    showArrow: Boolean = false,
    showChevronDown: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
        
        if (showChevronDown) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(
    title: String,
    onBackClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            content()
        }
    }
}
