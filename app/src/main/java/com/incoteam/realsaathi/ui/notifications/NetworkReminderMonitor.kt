package com.incoteam.realsaathi.ui.notifications

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

object NetworkReminderMonitor {

    @Volatile
    private var started = false

    fun start(context: Context) {
        if (started) return
        val appContext = context.applicationContext
        val connectivityManager = appContext.getSystemService(ConnectivityManager::class.java) ?: return

        EngagementReminderScheduler.syncCurrentNetworkState(
            appContext,
            isCurrentlyOnline(connectivityManager)
        )

        connectivityManager.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    EngagementReminderScheduler.onConnectivityChanged(
                        appContext,
                        isCurrentlyOnline(connectivityManager)
                    )
                }

                override fun onLost(network: Network) {
                    EngagementReminderScheduler.onConnectivityChanged(
                        appContext,
                        isCurrentlyOnline(connectivityManager)
                    )
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    EngagementReminderScheduler.onConnectivityChanged(
                        appContext,
                        isCurrentlyOnline(connectivityManager)
                    )
                }
            }
        )

        started = true
    }

    private fun isCurrentlyOnline(connectivityManager: ConnectivityManager): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
