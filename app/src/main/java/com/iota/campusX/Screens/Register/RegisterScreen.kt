package com.iota.campusX.Screens.Register

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthResult
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.GoogleSignInViewModel
import com.iota.campusX.Authentication.GoogleAuthentication.Onboarding.CustomSegmentedProgressBar
import com.iota.campusX.Authentication.GoogleAuthentication.Onboarding.OnBoardingContent
import com.iota.campusX.Authentication.GoogleAuthentication.Onboarding.OnBoardingScreen
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.ui.UIComponents.CircularLoading
import org.koin.compose.koinInject

@Composable
fun SignInScreen(navHostController: NavHostController) {

    val googleSignInViewModel: GoogleSignInViewModel = koinInject()
    val authState = googleSignInViewModel.state.collectAsStateWithLifecycle()
    val onboardingList by remember {
        mutableStateOf(
            listOf(
                OnBoardingContent(
                    image = R.drawable.undraw_anonymous_feedback_wbrj,
                    heading = "Embrace Anonymity,\nExplore Freedom in Identity",
                    description = "Express yourself freely being anonymous, offering a shield of anonymity while engaging."
                ),
                OnBoardingContent(
                    image = R.drawable.undraw_conference_call_ccsp, // Replace with Clubhouse-style image if available
                    heading = "Real-time Audio Chats,\nConnect through Conversation",
                    description = "Join live audio rooms to share ideas, collaborate, or just hang out — all anonymously and effortlessly."
                ),
                OnBoardingContent(
                    image = R.drawable.undraw_graduation_u7uc, // Replace with more relevant illustration if needed
                    heading = "Simplify Campus Life,\nAll-in-One Student Hub",
                    description = "From events to communities, manage everything campus-related with a single sign-in using your college ID."
                )
            )
        )
    }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = {onboardingList.count()}
    )
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            androidx.compose.material3.SnackbarHost(hostState = snackBarHostState)
        }
    ) {padding->

        Column ( modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(color = MaterialTheme.colorScheme.background),verticalArrangement = Arrangement.SpaceBetween) {


            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                Icon(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(if (isSystemInDarkTheme()) R.drawable.app_logo else R.drawable.app_logo),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column (Modifier.weight(1f)){

                OnBoardingScreen(
                    pagerState = pagerState,
                    onboardingContent = onboardingList
                )

            }

            CustomSegmentedProgressBar(
                pagerState.currentPage,
                onboardingList = onboardingList
            )

            Column(
                modifier = Modifier.padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){

                Button(
                    modifier = Modifier
                        .padding(horizontal = 40.dp)
                        .fillMaxWidth()
                        .height(48.dp),
                    onClick = {
                        if (pagerState.currentPage != onboardingList.count()-1){
                            pagerState.requestScrollToPage(pagerState.currentPage+1)
                        }else{
                            googleSignInViewModel.signIn()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                ) {
                    if (pagerState.currentPage != onboardingList.count()-1){
                        Text("Next")
                    }else{
                        when(val value = authState.value){

                            is AuthResult.Idle -> {
                                LoginButtonText()
                            }

                            is AuthResult.Loading -> {
                                CircularLoading(
                                    color = Color.White
                                )
                            }
                            is AuthResult.SignedIn -> {
                                navHostController.navigate(Routes.Main.Home.routes)
                            }
                            is AuthResult.Error -> {
                                LoginButtonText()
                                LaunchedEffect(Unit) {
                                    snackBarHostState.showSnackbar(value.message)
                                }
                            }
                            else -> {

                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.height(40.dp)) {

                    val uriHandler = LocalUriHandler.current

                    TermsAndPrivacyText(
                        modifier = Modifier.padding(horizontal = 60.dp),
                        onTermsClick = {
                            uriHandler.openUri("https://www.campuscircle.in/term-condition")
                        },
                        onPrivacyClick = {
                            uriHandler.openUri("https://www.campuscircle.in/privacy")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TermsAndPrivacyText(
    modifier: Modifier = Modifier,
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
) {
    val primary = MaterialTheme.colorScheme.primary
    val bodyStyle = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    // Build annotated text
    val annotated = buildAnnotatedString {
        append("By continuing, you agree to our\n")

        pushStringAnnotation(tag = "TERMS", annotation = "terms")
        withStyle(
            SpanStyle(
                color = primary,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline
            )
        ) { append("Terms & Conditions") }
        pop()

        append(" and ")

        pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
        withStyle(
            SpanStyle(
                color = primary,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline
            )
        ) { append("Privacy Policy") }
        pop()

        append(".")
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offsetPosition ->
                textLayoutResult?.let { layoutResult ->
                    val offset = layoutResult.getOffsetForPosition(offsetPosition)
                    annotated.getStringAnnotations(offset, offset)
                        .firstOrNull()?.let { annotation ->
                            when (annotation.tag) {
                                "TERMS" -> onTermsClick()
                                "PRIVACY" -> onPrivacyClick()
                            }
                        }
                }
            }
        },
        text = annotated,
        style = bodyStyle,
        textAlign = TextAlign.Center,
        softWrap = true,
        overflow = TextOverflow.Clip,
        onTextLayout = { textLayoutResult = it }
    )
}

@Composable
fun LoginButtonText(modifier: Modifier = Modifier) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = R.drawable.google),
            contentDescription = null,
        )

        Text(
            text = "Continue with Google",
            fontWeight = FontWeight.Bold
        )

    }

}