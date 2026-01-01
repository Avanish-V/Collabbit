package com.iota.campusX.Screens.Setting

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.UserProfileViewModel
import com.iota.campusX.R
import com.iota.campusX.Utils.Setting
import com.iota.campusX.Utils.ThemeMode.ThemePreference
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.AlertDialogWidget
import com.iota.campusX.ui.UIComponents.AppLabelText
//import com.iota.campusX.ui.theme.Black800
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel,
    themePreference: ThemePreference = koinInject()
) {
    val profileState = userProfileViewModel.userBaseProfile.collectAsState().value
    var screenValue by rememberSaveable { mutableStateOf(Setting.SETTING_SCREEN) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val currentThemeMode =
        themePreference.getThemeMode(context).collectAsState(initial = ThemeMode.LIGHT)
    var showAlert by remember { mutableStateOf(false) }

    val userData = when(profileState){
        is UiState.Success<*> -> {
            (profileState as UiState.Success<BaseProfileDTO>).data
        }
        else -> {
            null
        }
    }


    when (screenValue) {

        Setting.SETTING_SCREEN -> {

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Settings") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                        navigationIcon = {
                            IconButton(onClick = {
                                navController.popBackStack()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }

                    )
                },
            ) { paddingValues ->

                Column(modifier = Modifier.padding(paddingValues)) {

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                    ) {
                        items(settingList) {
                            Row(
                                modifier = Modifier
                                    .clickable(
                                        onClick = {
                                            it.url?.let { uri -> uriHandler.openUri(uri) }
                                        }
                                    )
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {

                                Icon(
                                    modifier = Modifier.size(22.dp),
                                    painter = painterResource(it.icon),
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = it.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                            }
                        }

                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        showAlert = true
                                    }
                                },
                            ) {
                                Icon(
                                    modifier = Modifier.size(22.dp),
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Delete account")
                            }

                            userData?.let { AppLabelText(text = it.email) }
                        }
                    }
                }
                val uriHandler = LocalUriHandler.current

                if (showAlert) {
                    AlertDialogWidget(
                        onDismiss = {
                            showAlert = false
                        },
                        title = "Delete Account",
                        description = "Are you sure you want to delete your account?",
                        positiveButtonText = "Delete",
                        negativeButtonText = "Cancel",
                        onPositiveClick = {
                            showAlert = false
                            uriHandler.openUri("https://campuscircle.in/account/delete")
                        },
                        showLoading = false
                    )
                }


            }
        }

        Setting.ABOUT_SCREEN -> {

            SettingPage(
                title = "About",
                onBackClick = {
                    screenValue = Setting.SETTING_SCREEN
                },
                content = {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webViewClient = WebViewClient()
                                loadUrl("file:///android_asset/About.html")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
        }

        Setting.PRIVACY_POLICY -> {

            SettingPage(
                title = "Privacy Policy",
                onBackClick = {
                    screenValue = Setting.SETTING_SCREEN
                },
                content = {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webViewClient = WebViewClient()
                                loadUrl("file:///android_asset/PrivecyPolicy.html")
                                // loadUrl("https://byteappstudiopvt.blogspot.com/2025/05/campusx-privacy-policy.html")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

            )
        }

        Setting.TERMS_AND_CONDITIONS -> {

            SettingPage(
                title = "Terms & Conditions",
                onBackClick = {
                    screenValue = Setting.SETTING_SCREEN
                },
                content = {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webViewClient = WebViewClient()
                                loadUrl("https://byteappstudiopvt.blogspot.com/2025/05/campusx-term-and-condition.html")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )

        }

        Setting.FEEDBACK -> {

        }
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(
    title: String,
    onBackClick: (Setting) -> Unit,
    content: @Composable () -> Unit,
    contentDescription: String? = null
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        onBackClick(Setting.SETTING_SCREEN)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }

            )
        },
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            content()

        }

    }


}

@Composable
fun ThemeSwitch(
    currentMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    isDynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit
) {

    Column(modifier = Modifier.padding(horizontal = 18.dp)) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                modifier = Modifier.size(22.dp),
                painter = painterResource(R.drawable.dark_mode_alt),
                contentDescription = null,
            )
            Text(text = "Theme", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(12.dp))

        themeModeList.forEach { it ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = it.theme,
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = currentMode == it.mode,
                    onCheckedChange = { isChecked ->
                        onThemeChange(it.mode)
                    },
                )
            }

        }
    }
}

val themeModeList = listOf<Theme>(
    Theme(theme = "Light", mode = ThemeMode.LIGHT),
    Theme(theme = "Dark", mode = ThemeMode.DARK)
)


