package com.incoteam.frndzz.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build

fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = getSystemService(ConnectivityManager::class.java) ?: return false
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun Context.registerInternetAvailabilityCallback(
    onAvailabilityChanged: (Boolean) -> Unit
): ConnectivityManager.NetworkCallback? {
    val connectivityManager = getSystemService(ConnectivityManager::class.java) ?: return null
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            onAvailabilityChanged(true)
        }

        override fun onLost(network: Network) {
            onAvailabilityChanged(false)
        }

        override fun onUnavailable() {
            onAvailabilityChanged(false)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            onAvailabilityChanged(
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            )
        }
    }

    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } else {
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(),
                callback
            )
        }
        callback
    }.getOrNull()
}

fun Context.unregisterInternetAvailabilityCallback(
    callback: ConnectivityManager.NetworkCallback?
) {
    if (callback == null) return
    val connectivityManager = getSystemService(ConnectivityManager::class.java) ?: return
    runCatching {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}
