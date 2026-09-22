package com.incoteam.realsaathi.data.model.auth

data class UploadHostMediaResponse(
    val bucket: String,
    val path: String,
    val publicUrl: String,
    val mediaType: String
)
