package com.incoteam.realsaathi.data.model.auth

data class SubmitFeedbackRequest(
    val category: String,
    val body: String
)

data class SubmitFeedbackResponse(
    val submitted: Boolean = false,
    val message: String = ""
)
