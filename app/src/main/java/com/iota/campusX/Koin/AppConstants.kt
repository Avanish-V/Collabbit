package com.iota.campusX.Koin

import com.google.firebase.auth.FirebaseAuth

object AppConstants {
    // Current Environment
    private const val IS_PRODUCTION = false

    // API Base URLs
    val BASE_URL = if (IS_PRODUCTION) {
        "https://iv52bugou5xppexnhffgj53hwq0rorrh.lambda-url.ap-south-1.on.aws/api/v1/"
    } else {
        "http://192.168.1.14:8080/api/v1/"
    }

    val OPPORTUNITIES_BASE_URL = if (IS_PRODUCTION) {
        "https://bmo6sd3nhbgp4akoqmgoamd3ja0cmyyn.lambda-url.ap-south-1.on.aws/api/jobs"
    } else {
        "http://192.168.1.14:8081/api/jobs"
    }

    val COURSES_BASE_URL = if (IS_PRODUCTION) {
        "https://bmo6sd3nhbgp4akoqmgoamd3ja0cmyyn.lambda-url.ap-south-1.on.aws/api/courses"
    } else {
        "http://192.168.1.14:8081/api/courses"
    }
}
