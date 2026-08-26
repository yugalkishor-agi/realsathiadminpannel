package com.incoteam.frndzz.data.model.auth

data class RemoteHostStory(
    val id: String,
    val ownerId: String,
    val ownerName: String,
    val title: String,
    val caption: String,
    val mediaUrl: String,
    val mediaType: String,
    val createdAtMillis: Long
)
