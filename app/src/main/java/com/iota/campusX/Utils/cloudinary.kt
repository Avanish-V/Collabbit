package com.iota.campusX.Utils

import android.content.Context
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.utils.ObjectUtils

private var isCloudinaryInitialized = false

fun initCloudinary(context: Context) {
    if (!isCloudinaryInitialized) {

        MediaManager.init(context.applicationContext, config)
        isCloudinaryInitialized = true
    }
}

val config = hashMapOf(
    "cloud_name" to "dni4h8jjy",
    "api_key" to "188248912913531",
    "api_secret" to "33aVFphaBuJoU7CIz4MGcbD2MRc" // ⚠️ Not secure for production
)