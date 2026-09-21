package com.iota.campusX.ui.UIComponents

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.io.File

@Composable
fun PdfThumbnail(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val httpClient = koinInject<HttpClient>()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            try {
                val cacheKey = url.hashCode().toString()
                val cacheFile = File(context.cacheDir, "pdf_thumb_$cacheKey.png")
                
                if (cacheFile.exists()) {
                    bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                } else {
                    val response = httpClient.get(url)
                    val bytes = response.readBytes()
                    val tempFile = File(context.cacheDir, "temp_thumb_${cacheKey}.pdf")
                    tempFile.writeBytes(bytes)
                    
                    val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(pfd)
                    if (renderer.pageCount > 0) {
                        val page = renderer.openPage(0)
                        val newBitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                        page.render(newBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        
                        cacheFile.outputStream().use { newBitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
                        
                        bitmap = newBitmap
                        page.close()
                    }
                    renderer.close()
                    pfd.close()
                    tempFile.delete()
                }
            } catch (e: Exception) {
                Log.e("PDF_THUMB", "Error generating thumb for $url", e)
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = modifier.background(Color.White.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = Color.Gray
            )
        } else {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
