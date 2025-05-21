package com.iota.campusX.Screens.Setting

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.Setting
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.White900
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel,
) {

    var screenValue by rememberSaveable { mutableStateOf(Setting.SETTING_SCREEN) }
    val scope = rememberCoroutineScope()
    var isLoading by rememberSaveable { mutableStateOf(false) }

    when (screenValue) {

        Setting.SETTING_SCREEN -> {

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Setting") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = White900,
                            titleContentColor = Black900
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
                containerColor = White900
            ) { paddingValues ->

                Column(modifier = Modifier.padding(paddingValues)) {

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(settingList) {

                            TextButton(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    screenValue = it.destination
                                },
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        modifier = Modifier.size(22.dp),
                                        painter = painterResource(it.icon),
                                        contentDescription = null,
                                        tint = primary

                                    )
                                    Text(
                                        text = it.title,
                                        fontWeight = FontWeight.Bold,
                                        color = Black800
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        TextButton(
                            onClick = {
                                scope.launch {
                                    userProfileViewModel.deleteUserProfile().collect {
                                        when(it){
                                            is ResultState.Loading -> {
                                                isLoading = true
                                            }
                                            is ResultState.Success -> {
                                                FirebaseAuth.getInstance().signOut()
                                                    .apply {
                                                        navController.navigate(Routes.Register.routes) {
                                                            popUpTo(Routes.Register.routes) {
                                                                inclusive = true
                                                            }
                                                        }
                                                    }
                                            }
                                            is ResultState.Error -> {
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                contentColor = Black800,
                                containerColor = Color.Transparent
                            )
                        ) {
                            Icon(
                                modifier = Modifier.size(22.dp),
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Delete account")
                        }
                    }
                }

                LoadingUI(isLoading)
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
    val destination: Setting
)

val settingList = listOf(
    ProfileSetting(
        title = "About",
        icon = R.drawable.info,
        destination = Setting.ABOUT_SCREEN,
    ),

    ProfileSetting(
        title = "Privacy Policy",
        icon = R.drawable.user_lock,
        destination = Setting.PRIVACY_POLICY
    ),

    ProfileSetting(
        title = "Term & Conditions",
        icon = R.drawable.memo_circle_check,
        destination = Setting.TERMS_AND_CONDITIONS
    ),

    ProfileSetting(
        title = "Feedback",
        icon = R.drawable.feedback_hand,
        destination = Setting.FEEDBACK
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
                        color = Black900
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White900,
                    titleContentColor = Black900
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
        containerColor = White900
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            content()

        }

    }


}