package com.incoteam.frndzz

import android.app.Application
import com.incoteam.frndzz.core.network.ApiClient
import com.incoteam.frndzz.core.session.SessionManager
import com.incoteam.frndzz.data.repository.AuthRepository
import com.incoteam.frndzz.ui.notifications.NetworkReminderMonitor

class FrndzzApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NetworkReminderMonitor.start(this)
    }

    val sessionManager: SessionManager by lazy {
        SessionManager(this)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(ApiClient.authApiService, sessionManager)
    }
}
