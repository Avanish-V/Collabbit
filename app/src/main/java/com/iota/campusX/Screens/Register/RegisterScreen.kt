package com.iota.campusX.Screens.Register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
import com.iota.campusX.Navigation.Home
import com.iota.campusX.R
import com.iota.campusX.Utils.CircularLoading
import org.koin.compose.koinInject

@Composable
fun SignInScreen(navHostController: NavHostController) {

    val context = androidx.compose.ui.platform.LocalContext.current
    val googleSignInViewModel: GoogleSignInViewModel = koinInject()
    val authState = googleSignInViewModel.state.collectAsStateWithLifecycle()
    val onboardingList by remember {
        mutableStateOf(
            listOf(
                OnBoardingContent(
                    image = R.drawable.first_onboarding,
                    heading = "Your Next Project Starts Here \uD83D\uDE80",
                    description = "Have an idea but don't know what to build? Discover exciting project ideas created by students and find opportunities to turn your ideas into something real."
                ),
                OnBoardingContent(
                    image = R.drawable.second_onboarding, // Replace with Clubhouse-style image if available
                    heading = "Build Together, Not Alone \uD83E\uDD1D",
                    description = "Find students with the skills you need. Collaborate with developers, designers, content creators, and creators to build projects together."
                ),
                OnBoardingContent(
                    image = R.drawable.third_onboarding
                    , // Replace with more relevant illustration if needed
                    heading = "Learn Skills. Build Projects. \uD83C\uDF93",
                    description = "Join live skill sessions, learn practical technologies, and immediately apply what you learn to real projects with your team."
                ),
                OnBoardingContent(
                    image = R.drawable.fourth_onboarding, // Replace with more relevant illustration if needed
                    heading = "Don't Just Copy. Create. \uD83D\uDD25",
                    description = "Move beyond tutorial projects. Build your own ideas, collaborate with others, launch real products, and gain experience that actually matters."
                ),
                OnBoardingContent(
                    image = R.drawable.fifth_onboarding, // Replace with more relevant illustration if needed
                    heading = "Build Your Future \uD83D\uDCBC",
                    description = "Showcase your projects, skills, and collaborations. Discover student internships and opportunities that match what you're building and learning."
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


            Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center){
//                Image(
//                    painter = painterResource(R.drawable.finder_icon),
//                    contentDescription = "CampusX",
//                    modifier = Modifier.size(60.dp)
//                )
                val gilroyFontFamily = FontFamily(
                    Font(R.font.gilroy_extrabold, weight = FontWeight.ExtraBold)
                )

                Text(
                    text = "Collabbit",
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = gilroyFontFamily,
                    color = MaterialTheme.colorScheme.primary

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
                    modifier = Modifier.padding(horizontal = 40.dp).fillMaxWidth().height(48.dp),
                    onClick = {
                        if (pagerState.currentPage != onboardingList.count()-1){
                            pagerState.requestScrollToPage(pagerState.currentPage+1)
                        }else{
                            googleSignInViewModel.signIn(context)
                        }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (pagerState.currentPage != onboardingList.count()-1){
                        Text("Next")
                    }else{
                        when(val value = authState.value){

                            is AuthResult.Idle -> {
                                LoginButtonText()
                            }

                            is AuthResult.Loading -> {
                                CircularLoading(Color.White)
                            }
                            is AuthResult.SignedIn -> {
                                navHostController.navigate(Home())
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