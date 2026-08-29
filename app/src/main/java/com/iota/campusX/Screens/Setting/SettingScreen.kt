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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    val profileState = userProfileViewModel.uiState.collectAsState().value
    var screenValue by rememberSaveable { mutableStateOf(Setting.SETTING_SCREEN) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    
    var showAlert by remember { mutableStateOf(false) }
    var showLogoutAlert by remember { mutableStateOf(false) }

    when (screenValue) {
        Setting.SETTING_SCREEN -> {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { 
                            Text(
                                "Settings",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            ) 
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {


                    // General Section
                    item {
                        SettingSection(title = "General") {
                            settingList.forEach { item ->
                                SettingItem(
                                    title = item.title,
                                    icon = item.icon,
                                    onClick = {
                                        if (item.url != null) {
                                            uriHandler.openUri(item.url)
                                        } else {
                                            screenValue = item.destination
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Account Section
                    item {
                        SettingSection(title = "Account") {
                            SettingItem(
                                title = "Sign Out",
                                icon = R.drawable.undo__1_, // Using an undo icon as fallback or standard logout
                                contentColor = MaterialTheme.colorScheme.error,
                                showArrow = false,
                                onClick = { showLogoutAlert = true }
                            )
                            SettingItem(
                                title = "Delete Account",
                                icon = R.drawable.trash,
                                contentColor = MaterialTheme.colorScheme.error,
                                showArrow = false,
                                onClick = { showAlert = true }
                            )
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Version 1.0.0 (Finder)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
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
                            uriHandler.openUri("https://campuscircle.in/account/delete")
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
                Setting.ABOUT_SCREEN -> "https://www.campuscircle.in/about"
                Setting.PRIVACY_POLICY -> "https://www.campuscircle.in/privacy"
                else -> "https://www.campuscircle.in/term-condition"
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

data class ProfileSetting(
    val title: String,
    val icon: Int,
    val destination: Setting,
    val url: String? = null
)

val settingList = listOf(
    ProfileSetting(
        title = "About",
        icon = R.drawable.info,
        destination = Setting.ABOUT_SCREEN,
        url = "https://www.campuscircle.in/about"
    ),

    ProfileSetting(
        title = "Privacy Policy",
        icon = R.drawable.user_lock,
        destination = Setting.PRIVACY_POLICY,
        url = "https://www.campuscircle.in/privacy"
    ),

    ProfileSetting(
        title = "Term & Conditions",
        icon = R.drawable.memo_circle_check,
        destination = Setting.TERMS_AND_CONDITIONS,
        url = "https://www.campuscircle.in/term-condition"
    ),

    ProfileSetting(
        title = "Feedback",
        icon = R.drawable.feedback_hand,
        destination = Setting.FEEDBACK,
        url = "https://www.campuscircle.in/feedback"
    ),
)

@Composable
fun ProfileHeaderItem(
    name: String,
    email: String,
    imageUrl: String?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                UserAvatar(
                    modifier = Modifier.fillMaxSize(),
                    imageUrl = imageUrl
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
    icon: Int,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = contentColor.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = contentColor,
            modifier = Modifier.weight(1f)
        )
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
