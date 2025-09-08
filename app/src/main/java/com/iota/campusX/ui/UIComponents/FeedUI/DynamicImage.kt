package com.iota.campusX.ui.UIComponents.FeedUI

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.iota.campusX.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageSizeViewModel : ViewModel() {
    val imageSizeCache = mutableStateMapOf<String, Pair<Int, Int>>()
}

@Composable
fun ImageWithDynamicRatio(
    imageUrl: String,
    modifier: Modifier = Modifier,
    viewModel: ImageSizeViewModel = viewModel(),
    onImageClick:()-> Unit
) {
    val context = LocalContext.current
    val imageSizeCache = viewModel.imageSizeCache
    var imageSize by remember(imageUrl) { mutableStateOf<Pair<Int, Int>?>(imageSizeCache[imageUrl]) }

    LaunchedEffect(imageUrl) {
        if (imageSize == null && !imageSizeCache.containsKey(imageUrl)) {
            val size = getImageSize(context, imageUrl)
            if (size != null) {
                imageSizeCache[imageUrl] = size
                imageSize = size
            }
        }
    }

    val dynamicHeight = remember(imageSize) {
        when (val size = imageSize) {
            null -> 200.dp // fallback
            else -> {
                val (width, height) = size
                if (width > height) 180.dp else 400.dp
            }
        }
    }

    AsyncImage(
        modifier = Modifier
            .height(dynamicHeight)
            .border(
                width = 0.1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )

            .padding(3.dp)

            .clickable(
                onClick = {
                    onImageClick()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .clip(RoundedCornerShape(13.dp)),
        model = imageUrl,
        placeholder = painterResource(R.drawable.placeholder),
        contentDescription = "Post Image",
        contentScale = ContentScale.Crop,
    )

}

suspend fun getImageSize(context: Context, imageUrl: String): Pair<Int, Int>? {
    return withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            result?.intrinsicWidth?.let { w ->
                result.intrinsicHeight.let { h -> w to h }
            }
        } catch (e: Exception) {
            null
        }
    }
}