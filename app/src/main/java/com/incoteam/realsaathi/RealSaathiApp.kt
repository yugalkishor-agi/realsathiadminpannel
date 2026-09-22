package com.incoteam.realsaathi

import android.app.Application
import com.incoteam.realsaathi.core.network.ApiClient
import com.incoteam.realsaathi.core.calling.ZegoCallManager
import com.incoteam.realsaathi.core.session.SessionManager
import com.incoteam.realsaathi.data.repository.AuthRepository
import com.incoteam.realsaathi.ui.notifications.NetworkReminderMonitor

class RealSaathiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NetworkReminderMonitor.start(this)
    }

    val sessionManager: SessionManager by lazy {
        SessionManager(this)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(ApiClient.authApiService, sessionManager, this)
    }

    val zegoCallManager: ZegoCallManager by lazy {
        ZegoCallManager(this, authRepository, sessionManager)
    }
}
