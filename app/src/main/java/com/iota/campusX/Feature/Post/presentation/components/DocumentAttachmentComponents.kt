package com.iota.campusX.Feature.Post.presentation.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iota.campusX.R
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import android.os.ParcelFileDescriptor

@Composable
fun DocumentAttachmentCard(
    name: String,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null,
    headerActions: @Composable RowScope.() -> Unit = {},
    body: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .clickable(enabled = onCardClick != null) { onCardClick?.invoke() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.file_text),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis
                )
                headerActions()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp, max = 420.dp)
                    .background(Color.White),
                contentAlignment = Alignment.TopCenter
            ) {
                body()
            }
        }
    }
}

@Composable
fun DocumentHorizontalPager(
    pageCount: Int,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    onPageClick: (() -> Unit)? = null,
    pageContent: @Composable (Int) -> Unit
) {
    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = userScrollEnabled,
            pageSpacing = 0.dp,
            beyondViewportPageCount = 1
        ) { pageIndex ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (onPageClick != null) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onPageClick() }
                        } else Modifier
                    )
            ) {
                pageContent(pageIndex)
            }
        }

        if (pageCount > 1) {
            Surface(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / $pageCount",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun rememberPdfRenderer(uri: Uri): PdfRenderer? {
    val context = LocalContext.current
    var renderer by remember(uri) { mutableStateOf<PdfRenderer?>(null) }

    DisposableEffect(uri) {
        val pfd = try {
            context.contentResolver.openFileDescriptor(uri, "r")
        } catch (e: Exception) {
            Log.e("rememberPdfRenderer", "Error opening FD for $uri", e)
            null
        }

        if (pfd != null) {
            val r = PdfRenderer(pfd)
            renderer = r
            onDispose {
                r.close()
                pfd.close()
            }
        } else {
            onDispose {}
        }
    }

    return renderer
}

@Composable
fun rememberRemotePdfRenderer(url: String, httpClient: HttpClient): PdfRenderer? {
    val context = LocalContext.current
    var renderer by remember(url) { mutableStateOf<PdfRenderer?>(null) }
    var localFile by remember(url) { mutableStateOf<File?>(null) }

    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "pdf_cache_${url.hashCode()}.pdf")
                if (!file.exists()) {
                    val response = httpClient.get(url)
                    val bytes = response.readBytes()
                    FileOutputStream(file).use { out ->
                        out.write(bytes)
                    }
                }
                localFile = file
            } catch (e: Exception) {
                Log.e("rememberRemotePdf", "Error downloading PDF: ${e.message}")
            }
        }
    }

    DisposableEffect(localFile) {
        val file = localFile ?: return@DisposableEffect onDispose {}
        val pfd = try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (e: Exception) {
            null
        }

        if (pfd != null) {
            val r = PdfRenderer(pfd)
            renderer = r
            onDispose {
                r.close()
                pfd.close()
            }
        } else {
            onDispose {}
        }
    }

    return renderer
}

@Composable
fun PdfPageImage(
    renderer: PdfRenderer?,
    pageIndex: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth,
    highQuality: Boolean = false
) {
    var bitmap by remember(renderer, pageIndex) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(renderer, pageIndex) {
        if (renderer != null && pageIndex < renderer.pageCount) {
            withContext(Dispatchers.IO) {
                try {
                    val page = renderer.openPage(pageIndex)
                    val scale = if (highQuality) 2 else 1
                    val b = Bitmap.createBitmap(page.width * scale, page.height * scale, Bitmap.Config.ARGB_8888)
                    page.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap = b
                    page.close()
                } catch (e: Exception) {
                    Log.e("PdfPageImage", "Error rendering page $pageIndex", e)
                }
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun ZoomableBox(
    modifier: Modifier = Modifier,
    maxScale: Float = 5f,
    onZoomChange: (Float) -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()

                        // Only consume and handle if we're actually zooming or panned away from base scale
                        if (scale > 1.01f || zoomChange != 1f) {
                            val newScale = (scale * zoomChange).coerceIn(1f, maxScale)
                            
                            if (newScale > 1f) {
                                event.changes.forEach { it.consume() }
                                scale = newScale
                                offset += panChange
                            } else {
                                scale = 1f
                                offset = Offset.Zero
                            }
                            onZoomChange(scale)
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 3f
                        }
                        onZoomChange(scale)
                    }
                )
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                
                if (scale > 1f) {
                    val maxOffsetHorizontal = (scale - 1) * size.width / 2
                    val maxOffsetVertical = (scale - 1) * size.height / 2
                    translationX = offset.x.coerceIn(-maxOffsetHorizontal, maxOffsetHorizontal)
                    translationY = offset.y.coerceIn(-maxOffsetVertical, maxOffsetVertical)
                } else {
                    translationX = 0f
                    translationY = 0f
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
