package com.iota.campusX.Utils

import android.content.Context
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.utils.ObjectUtils

val cloudinary = Cloudinary(
    ObjectUtils.asMap(
    "cloud_name", "dni4h8jjy",
    "api_key", "188248912913531",
    "api_secret", "33aVFphaBuJoU7CIz4MGcbD2MRc"
))

fun initCloudinary(context: Context){

    val config: HashMap<String, String> = HashMap()
    config["cloud_name"] = "dni4h8jjy"
    config["api_key"] = "188248912913531"
    config["api_secret"] = "33aVFphaBuJoU7CIz4MGcbD2MRc"  // Not recommended for production apps
    MediaManager.init(context, config)


}
