package com.incoteam.realsaathi.core.device

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.Locale
import java.util.UUID

data class DeviceMetadata(
    val deviceId: String,
    val deviceBrand: String,
    val countryCode: String,
    val appBrand: String = "RealSaathi"
)

object DeviceMetadataProvider {
    private const val PREFS = "realsaathi_device_metadata"
    private const val FALLBACK_ID = "fallback_device_id"

    fun read(context: Context): DeviceMetadata {
        val appContext = context.applicationContext
        val androidId = Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ANDROID_ID
        ).orEmpty().trim()
        val fallbackId = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(FALLBACK_ID, null)
            ?.trim()
            .orEmpty()
        val deviceId = androidId.ifBlank {
            fallbackId.ifBlank {
                UUID.randomUUID().toString().also {
                    appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                        .edit().putString(FALLBACK_ID, it).apply()
                }
            }
        }
        val brand = listOf(Build.MANUFACTURER, Build.BRAND)
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
        val country = Locale.getDefault().country.trim().uppercase(Locale.ROOT).ifBlank { "UN" }
        return DeviceMetadata(deviceId, brand, country)
    }
}
