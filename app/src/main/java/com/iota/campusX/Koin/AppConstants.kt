package com.iota.campusX.Koin

import com.google.firebase.auth.FirebaseAuth

object AppConstants {
    // Current Environment
     const val IS_PRODUCTION = false

    // API Base URLs
    val BASE_URL = if (IS_PRODUCTION) {
        "https://iv52bugou5xppexnhffgj53hwq0rorrh.lambda-url.ap-south-1.on.aws/api/v1/"
    } else {
        "http://192.168.1.13:8080/api/v1/"
    }

    val OPPORTUNITIES_BASE_URL = if (IS_PRODUCTION) {
        "https://bmo6sd3nhbgp4akoqmgoamd3ja0cmyyn.lambda-url.ap-south-1.on.aws/api/jobs"
    } else {
        "http://192.168.1.13:8081/api/jobs"
    }

    val COURSES_BASE_URL = if (IS_PRODUCTION) {
        "https://bmo6sd3nhbgp4akoqmgoamd3ja0cmyyn.lambda-url.ap-south-1.on.aws/api/courses"
    } else {
        "http://192.168.1.13:8081/api/courses"
    }

    // Sharing and Deep Links
    const val DEEP_LINK_DOMAIN = "collabbit.in"
    const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.iota.campusX"
}
