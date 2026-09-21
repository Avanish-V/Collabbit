package com.iota.campusX.Screens.Home

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.iota.campusX.Feature.Post.presentation.components.DocumentHorizontalPager
import com.iota.campusX.Feature.Post.presentation.components.PdfPageImage
import com.iota.campusX.Feature.Post.presentation.components.ZoomableBox
import com.iota.campusX.Feature.Post.presentation.components.rememberPdfRenderer
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import com.iota.campusX.Navigation.PdfView
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewScreen(
    navHostController: NavHostController,
    httpClient: HttpClient = koinInject()
) {
    val navEntry = remember(navHostController) {
        navHostController.currentBackStackEntry
    }
    val routeArgs = navEntry?.toRoute<PdfView>()
    val pdfUrl = routeArgs?.pdfUrl
    val fileName = routeArgs?.fileName
    val thumbnailUrl = routeArgs?.thumbnailUrl

    val context = LocalContext.current
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            
            onDispose {
                insetsController.show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    LaunchedEffect(pdfUrl) {
        if (pdfUrl != null) {
            try {
                withContext(Dispatchers.IO) {
                    val file = File(context.cacheDir, "temp_view.pdf")
                    val response = httpClient.get(pdfUrl)
                    val bytes = response.readBytes()
                    FileOutputStream(file).use { it.write(bytes) }
                    pdfFile = file
                }
                isLoading = false
            } catch (e: Exception) {
                error = e.localizedMessage
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading && thumbnailUrl == null -> CircularProgressIndicator(color = Color.White)
                isLoading && thumbnailUrl != null -> {
                    // Show single thumbnail while downloading
                    coil.compose.AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopEnd) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color.White)
                    }
                }
                error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Failed to load PDF", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = error!!, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
                pdfFile != null -> {
                    PdfPager(file = pdfFile!!)
                }
            }
        }

        TopAppBar(
            title = {
                Text(
                    text = fileName ?: "PDF Document",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navHostController.popBackStack() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            },
            actions = {
                IconButton(onClick = { 
                    pdfUrl?.let { url ->
                        val request = DownloadManager.Request(Uri.parse(url))
                            .setTitle(fileName ?: "Document")
                            .setDescription("Downloading PDF...")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName ?: "document.pdf")
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true)
                        
                        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        dm.enqueue(request)
                    }
                }) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            ),
            windowInsets = WindowInsets(0, 0, 0, 0)
        )
    }
}

@Composable
fun PdfPager(file: File) {
    val renderer = rememberPdfRenderer(Uri.fromFile(file))
    val pageCount = renderer?.pageCount ?: 0
    val pagerState = rememberPagerState(pageCount = { pageCount })
    var userScrollEnabled by remember { mutableStateOf(true) }

    DocumentHorizontalPager(
        pageCount = pageCount,
        pagerState = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = userScrollEnabled
    ) { pageIndex ->
        ZoomableBox(
            modifier = Modifier.fillMaxSize(),
            onZoomChange = { scale -> userScrollEnabled = scale <= 1f }
        ) {
            PdfPageImage(
                renderer = renderer,
                pageIndex = pageIndex,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                highQuality = true
            )
        }
    }
}
