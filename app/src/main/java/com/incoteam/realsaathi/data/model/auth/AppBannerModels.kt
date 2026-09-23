package com.incoteam.realsaathi.data.model.auth

import com.google.gson.annotations.SerializedName

data class AppBannersResponse(val banners: List<RemoteAppBanner> = emptyList())
data class RemoteAppBanner(
    val placement: String = "",
    val slot: Int = 0,
    @SerializedName("image_url") val imageUrl: String = "",
    val version: Long = 0L
)
