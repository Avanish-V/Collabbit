package com.iota.campusX.Feature.Post.domain

data class UploadResponse(
    val status: String = "",
    val progress: Int? = null,
    val uploadId: String = ""
)
