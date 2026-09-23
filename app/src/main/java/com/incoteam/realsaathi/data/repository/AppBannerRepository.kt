package com.incoteam.realsaathi.data.repository

import android.content.Context
import com.google.gson.Gson
import com.incoteam.realsaathi.BuildConfig
import com.incoteam.realsaathi.data.model.auth.AppBannersResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

data class CachedAppBanner(val placement: String, val slot: Int, val localPath: String)

class AppBannerRepository(private val context: Context) {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val cacheDir get() = File(context.filesDir, "app_banners").apply { mkdirs() }
    private val versions get() = context.getSharedPreferences("app_banner_versions", Context.MODE_PRIVATE)

    fun cached(): List<CachedAppBanner> = cacheDir.listFiles()?.mapNotNull { file ->
        val parts = file.nameWithoutExtension.split("_"); if (parts.size != 2) null
        else CachedAppBanner(parts[0], parts[1].toIntOrNull() ?: return@mapNotNull null, file.absolutePath)
    } ?: emptyList()

    fun refresh(): List<CachedAppBanner> {
        val request = Request.Builder().url(BuildConfig.CONFIG_BANNERS_URL).build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return cached()
        val body = response.body?.string().orEmpty()
        val payload = gson.fromJson(body, AppBannersResponse::class.java) ?: return cached()
        val activeKeys = payload.banners.filter { it.placement.isNotBlank() && it.slot in 1..3 && it.imageUrl.isNotBlank() }.map { "${it.placement}_${it.slot}" }.toSet()
        cacheDir.listFiles()?.filter { it.nameWithoutExtension !in activeKeys }?.forEach(File::delete)
        payload.banners.filter { it.placement.isNotBlank() && it.slot in 1..3 && it.imageUrl.isNotBlank() }.forEach { banner ->
            val target = File(cacheDir, "${banner.placement}_${banner.slot}.jpg")
            if (!target.exists() || target.length() == 0L || versions.getLong(target.nameWithoutExtension, -1L) != banner.version) {
                if (download(banner.imageUrl, target)) versions.edit().putLong(target.nameWithoutExtension, banner.version).apply()
            }
        }
        return cached()
    }

    private fun download(url: String, target: File): Boolean = runCatching { client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (response.isSuccessful) response.body?.byteStream()?.use { input -> target.outputStream().use(input::copyTo) }
            response.isSuccessful
        } }.getOrDefault(false)
}
