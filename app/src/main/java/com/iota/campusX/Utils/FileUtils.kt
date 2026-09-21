package com.iota.campusX.Utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FileUtils {
    fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "snaps")
            cachePath.mkdirs()
            val file = File(cachePath, "${UUID.randomUUID()}.jpg")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
}
