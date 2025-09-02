package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.iota.campusX.R

@Composable
fun AnimatedStatus(modifier : Modifier= Modifier, file: Int, description: String) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(file))
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying = true,
        iterations =100
    )
    LottieAnimation(
        modifier = modifier,
        composition = composition,
        progress = { progress },
    )
}