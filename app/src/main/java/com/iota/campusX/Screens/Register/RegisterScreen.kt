package com.iota.campusX.Screens.Register

import android.R.attr.offset
import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.iota.campusX.Authentication.GoogleAuthentication.Onboarding.CustomSegmentedProgressBar
import com.iota.campusX.Authentication.GoogleAuthentication.Onboarding.OnBoardingContent
import com.iota.campusX.Authentication.GoogleAuthentication.Onboarding.OnBoardingScreen
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.ui.theme.LightTheme_Blue
import com.iota.campusX.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SignInScreen(navHostController: NavHostController) {

    val googleAuthViewModel:AuthViewModel = koinInject()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

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

    val state = googleAuthViewModel.state.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {result->
            if (result.resultCode == RESULT_OK) {
                coroutineScope.launch {
                    googleAuthViewModel.onSignInResult(
                        intent = result.data?:return@launch
                    )
                }
            }
        }
    )

    LaunchedEffect(key1 = state.value.isSignInSuccessful) {
        if (state.value.isSignInSuccessful){
            googleAuthViewModel.verifyUser(state.value.userId,state.value.userToken)
                .collect{
                    when(it){
                        is ResultState.Loading->{
                            isLoading = true
                        }
                        is ResultState.Success->{
                            navHostController.navigate(Routes.Main.Home.routes){
                                popUpTo(Routes.Register.routes)
                            }
                        }
                        is ResultState.Error->{
                            isLoading = false
                            FirebaseAuth.getInstance().signOut()
                        }
                    }
                }
        }
    }



    Column ( modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background),verticalArrangement = Arrangement.SpaceBetween) {


        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
            Image(
                modifier = Modifier.height(80.dp).width(120.dp),
                painter = painterResource(if (isSystemInDarkTheme()) R.drawable.logo_dark else R.drawable.campora_logo),
                contentDescription = null,
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
            modifier = Modifier.height(200.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){

            Button(
                modifier = Modifier.padding(horizontal = 40.dp).fillMaxWidth().height(48.dp),
                onClick = {
                    if (pagerState.currentPage != onboardingList.count()-1){
                        pagerState.requestScrollToPage(pagerState.currentPage+1)
                    }else{
                        coroutineScope.launch {
                            val signInIntentSender = googleAuthViewModel.startSignIn()
                            launcher.launch(
                                IntentSenderRequest.Builder(
                                    signInIntentSender?:return@launch
                                ).build()
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = LightTheme_Blue
                )
            ) {
                if (pagerState.currentPage != onboardingList.count()-1){
                        Text("Next")
                }else{

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = null,
                            tint = White
                        )
                        Text(
                            text = "Continue with Google",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.height(40.dp)) {
                if (pagerState.currentPage == onboardingList.count()-1){
                    val uriHandler = LocalUriHandler.current
                    TermsAndPrivacyText(
                        modifier = Modifier.padding(horizontal = 60.dp),
                        onTermsClick = {
                            uriHandler.openUri("https://example.com/terms")
                        },
                        onPrivacyClick = {
                            // Handle privacy click
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