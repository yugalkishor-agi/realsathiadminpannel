package com.incoteam.frndzz.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.app.PictureInPictureParams
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Rational
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.incoteam.frndzz.FrndzzApp
import com.incoteam.frndzz.core.network.isInternetAvailable
import com.incoteam.frndzz.core.network.registerInternetAvailabilityCallback
import com.incoteam.frndzz.core.network.unregisterInternetAvailabilityCallback
import com.incoteam.frndzz.core.session.SessionManager
import com.incoteam.frndzz.core.ui.StableMobileUi
import com.incoteam.frndzz.core.ui.withStableFontScale
import com.incoteam.frndzz.ui.auth.login.LoginActivity
import com.incoteam.frndzz.ui.notifications.EngagementReminderScheduler
import com.incoteam.frndzz.ui.theme.FrndzzTheme

enum class CallAudioRoute {
    SPEAKER,
    EARPIECE,
    BLUETOOTH
}

class HomeActivity : ComponentActivity() {

    companion object {
        private const val CALL_PREFS = "frndzzz_call_session"
        private const val APP_PREFS = "frndzzz_app_prefs"
        const val EXTRA_FORCE_PROFILE_SETUP = "extra_force_profile_setup"
        private const val KEY_NOTIFICATION_TEST_SHOWN = "notification_test_shown"
        private const val KEY_CALL_USER_ID = "call_user_id"
        private const val KEY_CALL_NAME = "call_name"
        private const val KEY_CALL_PUBLIC_ID = "call_public_id"
        private const val KEY_CALL_IS_VIDEO = "call_is_video"
        private const val KEY_CALL_RATE = "call_rate"
        private const val KEY_CALL_INCLUDED_SECONDS = "call_included_seconds"
        private const val KEY_CALL_STARTED_AT = "call_started_at"
        private const val KEY_CALL_SPEAKER_ON = "call_speaker_on"
        private const val KEY_CALL_MIC_MUTED = "call_mic_muted"
        private const val KEY_CALL_AUDIO_ROUTE = "call_audio_route"
    }

    private lateinit var sessionManager: SessionManager
    private val callAudioController by lazy {
        CallAudioController(this) { resolvedRoute, hasBluetooth ->
            val wasBluetoothConnected = bluetoothCallAudioConnected
            bluetoothCallAudioConnected = hasBluetooth
            if (hasBluetooth && !wasBluetoothConnected && resolvedRoute != CallAudioRoute.BLUETOOTH) {
                manualCallAudioRoute = persistManualCallAudioRoute(CallAudioRoute.BLUETOOTH)
                selectedCallAudioRoute = CallAudioRoute.BLUETOOTH
                syncCallAudioState()
            } else {
                selectedCallAudioRoute = resolvedRoute
            }
        }
    }
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            EngagementReminderScheduler.showReminderNotification(this)
            markNotificationPreviewShown()
        }
    }
    private val mandatoryPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        handleMandatoryPermissionResult()
    }
    private var activeCallSession by mutableStateOf<ActiveCallSession?>(null)
    private var callMicMuted by mutableStateOf(false)
    private var manualCallAudioRoute by mutableStateOf(CallAudioRoute.SPEAKER)
    private var selectedCallAudioRoute by mutableStateOf(CallAudioRoute.SPEAKER)
    private var bluetoothCallAudioConnected by mutableStateOf(false)
    private var isInPipMode by mutableStateOf(false)
    private var isActivityStarted = false
    private var contentShown = false
    private var shouldForceProfileSetup = false
    private var hasInternetConnection by mutableStateOf(false)
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var sensitiveContentVisible = false
    private var secureWindowEnabled = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withStableFontScale())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasInternetConnection = isInternetAvailable()
        networkCallback = registerInternetAvailabilityCallback { available ->
            runOnUiThread {
                hasInternetConnection = available
            }
        }

        sessionManager = (application as FrndzzApp).sessionManager
        if (!sessionManager.hasActiveSession()) {
            updateActiveCallSession(null)
            navigateToLogin()
            return
        }
        shouldForceProfileSetup =
            intent.getBooleanExtra(EXTRA_FORCE_PROFILE_SETUP, false) || sessionManager.needsProfileSetup()

        activeCallSession = restorePersistedCallSession()
        callMicMuted = restorePersistedMicState()
        manualCallAudioRoute = restorePersistedCallAudioRoute()
        selectedCallAudioRoute = manualCallAudioRoute
        enableEdgeToEdge()
        syncKeepScreenOnState()
        syncSecureWindowProtection()

        if (hasMandatoryPermissions()) {
            syncCallAudioState()
            showMainContentIfNeeded()
            requestNotificationPermissionIfNeeded()
        } else {
            requestMandatoryPermissions()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (activeCallSession != null) {
            enterCallPictureInPicture()
        }
    }

    override fun onPictureInPictureRequested(): Boolean {
        if (activeCallSession == null) return false
        enterCallPictureInPicture()
        return true
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
        syncKeepScreenOnState()
        if (!isInPictureInPictureMode && activeCallSession == null) {
            moveTaskToBack(false)
        }
    }

    private fun enterCallPictureInPicture() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || activeCallSession == null || isInPipMode) {
            return
        }

        val paramsBuilder = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            paramsBuilder
                .setAutoEnterEnabled(true)
                .setSeamlessResizeEnabled(true)
        }

        enterPictureInPictureMode(paramsBuilder.build())
    }

    override fun onStart() {
        super.onStart()
        isActivityStarted = true
        syncKeepScreenOnState()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !super.isInPictureInPictureMode) {
            isInPipMode = false
        }
        if (!contentShown && hasMandatoryPermissions()) {
            syncCallAudioState()
            showMainContentIfNeeded()
            requestNotificationPermissionIfNeeded()
        } else if (contentShown) {
            syncCallAudioState()
        }
        syncKeepScreenOnState()
    }

    override fun onStop() {
        isActivityStarted = false
        syncKeepScreenOnState()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        unregisterInternetAvailabilityCallback(networkCallback)
        callAudioController.clear()
    }

    private fun requestMandatoryPermissions() {
        mandatoryPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun handleMandatoryPermissionResult() {
        if (hasMandatoryPermissions()) {
            syncCallAudioState()
            requestNotificationPermissionIfNeeded()
        } else {
            Toast.makeText(
                this,
                "Camera, mic aur location permissions call ke time required hongi.",
                Toast.LENGTH_LONG
            ).show()
        }
        showMainContentIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        maybeShowNotificationPreview()
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasMandatoryPermissions(): Boolean {
        return hasPermission(Manifest.permission.CAMERA) &&
            hasPermission(Manifest.permission.RECORD_AUDIO) &&
            hasLocationAccess()
    }

    private fun hasLocationAccess(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private fun maybeShowNotificationPreview() {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) return
        val prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE)
        if (prefs.getBoolean(KEY_NOTIFICATION_TEST_SHOWN, false)) return
        EngagementReminderScheduler.showReminderNotification(this)
        markNotificationPreviewShown()
    }

    private fun markNotificationPreviewShown() {
        getSharedPreferences(APP_PREFS, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_NOTIFICATION_TEST_SHOWN, true)
            .apply()
    }

    private fun showMainContentIfNeeded() {
        if (contentShown) return
        contentShown = true
        syncKeepScreenOnState()
        setContent {
            StableMobileUi {
                FrndzzTheme(darkTheme = true, dynamicColor = false) {
                    if (!hasInternetConnection) {
                        NoInternetBlockingScreen(
                            title = "No internet connection",
                            subtitle = "Turn on internet to continue.",
                            onTap = ::showNoInternetMessage
                        )
                    } else if (isInPipMode && activeCallSession != null) {
                        PipCallSurface(activeCall = requireNotNull(activeCallSession))
                    } else if (sessionManager.isHost()) {
                        HostPreviewApp(
                            sessionManager = sessionManager,
                            onLogout = {
                                updateActiveCallSession(null)
                                sessionManager.logout()
                                navigateToLogin()
                            },
                            onExitApp = ::showExitConfirmation,
                            onSwitchToCustomerMode = { recreate() },
                            onSensitiveContentVisible = ::updateSensitiveContentVisibility
                        )
                    } else {
                        FrndzzHomeApp(
                            sessionManager = sessionManager,
                            activeCall = activeCallSession,
                            isInPictureInPictureMode = isInPipMode,
                            startInProfileSetup = shouldForceProfileSetup,
                            onStartCall = ::updateActiveCallSession,
                            onEndCall = { updateActiveCallSession(null) },
                            onEnterPictureInPicture = { enterCallPictureInPicture() },
                            onExitApp = ::showExitConfirmation,
                            onSwitchToHostMode = { recreate() },
                            onProfileSetupCompleted = {
                                shouldForceProfileSetup = false
                                sessionManager.markProfileSetupCompleted()
                                if (sessionManager.isHost()) {
                                    recreate()
                                }
                            },
                            onSensitiveContentVisible = ::updateSensitiveContentVisibility,
                            onLogout = {
                                updateActiveCallSession(null)
                                sessionManager.logout()
                                navigateToLogin()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Exit application")
            .setMessage("Do you want to exit from application?")
            .setPositiveButton("Confirm") { _, _ ->
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateActiveCallSession(session: ActiveCallSession?) {
        activeCallSession = session
        val prefs = getSharedPreferences(CALL_PREFS, MODE_PRIVATE)
        if (session == null) {
            callMicMuted = false
            manualCallAudioRoute = CallAudioRoute.SPEAKER
            selectedCallAudioRoute = CallAudioRoute.SPEAKER
            bluetoothCallAudioConnected = false
            prefs.edit().clear().apply()
            syncCallAudioState()
            syncKeepScreenOnState()
            syncSecureWindowProtection()
            return
        }

        manualCallAudioRoute = sanitizeStoredCallAudioRoute(manualCallAudioRoute, session.isVideo)
        selectedCallAudioRoute = manualCallAudioRoute

        prefs.edit()
            .putString(KEY_CALL_USER_ID, session.userId)
            .putString(KEY_CALL_NAME, session.name)
            .putString(KEY_CALL_PUBLIC_ID, session.publicId)
            .putBoolean(KEY_CALL_IS_VIDEO, session.isVideo)
            .putInt(KEY_CALL_RATE, session.ratePerMinute)
            .putLong(KEY_CALL_INCLUDED_SECONDS, session.includedSecondsAtStart ?: -1L)
            .putLong(KEY_CALL_STARTED_AT, session.startedAtMillis)
            .putBoolean(KEY_CALL_MIC_MUTED, callMicMuted)
            .putBoolean(KEY_CALL_SPEAKER_ON, manualCallAudioRoute == CallAudioRoute.SPEAKER)
            .putString(KEY_CALL_AUDIO_ROUTE, manualCallAudioRoute.name)
            .apply()
        syncCallAudioState()
        syncKeepScreenOnState()
        syncSecureWindowProtection()
    }

    private fun syncKeepScreenOnState() {
        val shouldKeepScreenOn = (contentShown && isActivityStarted) || isInPipMode
        if (shouldKeepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun updateSensitiveContentVisibility(visible: Boolean) {
        sensitiveContentVisible = visible
        syncSecureWindowProtection()
    }

    private fun syncSecureWindowProtection() {
        val shouldSecureWindow = activeCallSession != null || sensitiveContentVisible
        if (shouldSecureWindow == secureWindowEnabled) return
        secureWindowEnabled = shouldSecureWindow
        if (shouldSecureWindow) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun showNoInternetMessage() {
        Toast.makeText(this, "No internet connection. Turn on internet.", Toast.LENGTH_SHORT).show()
    }

    private fun restorePersistedCallSession(): ActiveCallSession? {
        val prefs = getSharedPreferences(CALL_PREFS, MODE_PRIVATE)
        val userId = prefs.getString(KEY_CALL_USER_ID, null) ?: return null
        val name = prefs.getString(KEY_CALL_NAME, null) ?: return null
        val publicId = prefs.getString(KEY_CALL_PUBLIC_ID, null)
        val includedSeconds = prefs.getLong(KEY_CALL_INCLUDED_SECONDS, -1L)
            .takeIf { it >= 0L }
        val startedAtMillis = prefs.getLong(KEY_CALL_STARTED_AT, 0L)
            .takeIf { it > 0L }
            ?: System.currentTimeMillis()

        return ActiveCallSession(
            userId = userId,
            name = name,
            isVideo = prefs.getBoolean(KEY_CALL_IS_VIDEO, false),
            publicId = publicId,
            ratePerMinute = prefs.getInt(KEY_CALL_RATE, 0),
            includedSecondsAtStart = includedSeconds,
            startedAtMillis = startedAtMillis
        )
    }

    private fun restorePersistedCallAudioRoute(): CallAudioRoute {
        val prefs = getSharedPreferences(CALL_PREFS, MODE_PRIVATE)
        val persistedRoute = prefs.getString(KEY_CALL_AUDIO_ROUTE, null)
            ?.let { routeName -> runCatching { CallAudioRoute.valueOf(routeName) }.getOrNull() }
        if (persistedRoute != null) return persistedRoute
        return if (prefs.getBoolean(KEY_CALL_SPEAKER_ON, true)) {
            CallAudioRoute.SPEAKER
        } else {
            CallAudioRoute.EARPIECE
        }
    }

    private fun restorePersistedMicState(): Boolean {
        return getSharedPreferences(CALL_PREFS, MODE_PRIVATE)
            .getBoolean(KEY_CALL_MIC_MUTED, false)
    }

    fun isCallMicMuted(): Boolean = callMicMuted

    fun setCallMicMuted(muted: Boolean): Boolean {
        callMicMuted = muted
        getSharedPreferences(CALL_PREFS, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_CALL_MIC_MUTED, muted)
            .apply()
        syncCallAudioState()
        return callMicMuted
    }

    fun isCallSpeakerEnabled(): Boolean = getCallAudioRoute() == CallAudioRoute.SPEAKER

    fun getCallAudioRoute(): CallAudioRoute {
        return selectedCallAudioRoute
    }

    fun hasBluetoothCallAudioDevice(): Boolean = bluetoothCallAudioConnected

    fun shouldShowCallAudioRouteControl(): Boolean {
        return activeCallSession != null
    }

    fun setCallSpeakerEnabled(enabled: Boolean): Boolean {
        val targetRoute = if (activeCallSession?.isVideo == true || bluetoothCallAudioConnected) {
            CallAudioRoute.SPEAKER
        } else if (enabled) {
            CallAudioRoute.SPEAKER
        } else {
            CallAudioRoute.EARPIECE
        }
        manualCallAudioRoute = persistManualCallAudioRoute(targetRoute)
        syncCallAudioState()
        return getCallAudioRoute() == CallAudioRoute.SPEAKER
    }

    fun cycleCallAudioRoute(): CallAudioRoute {
        val session = activeCallSession ?: return getCallAudioRoute()
        val nextRoute = callAudioController.getNextRoute(
            currentRoute = manualCallAudioRoute,
            isVideo = session.isVideo
        )
        manualCallAudioRoute = persistManualCallAudioRoute(nextRoute)
        syncCallAudioState()
        return getCallAudioRoute()
    }

    private fun syncCallAudioState() {
        val session = activeCallSession
        if (session == null) {
            callAudioController.clear()
            return
        }
        callAudioController.bind(
            activeCall = session,
            micMuted = callMicMuted,
            preferredRoute = manualCallAudioRoute
        )
    }

    private fun sanitizeStoredCallAudioRoute(route: CallAudioRoute, isVideo: Boolean): CallAudioRoute {
        return when {
            route == CallAudioRoute.BLUETOOTH -> CallAudioRoute.BLUETOOTH
            !isVideo && route == CallAudioRoute.EARPIECE -> CallAudioRoute.EARPIECE
            else -> CallAudioRoute.SPEAKER
        }
    }

    private fun persistManualCallAudioRoute(route: CallAudioRoute): CallAudioRoute {
        val resolvedRoute = sanitizeStoredCallAudioRoute(route, activeCallSession?.isVideo == true)
        getSharedPreferences(CALL_PREFS, MODE_PRIVATE)
            .edit()
            .putString(KEY_CALL_AUDIO_ROUTE, resolvedRoute.name)
            .putBoolean(KEY_CALL_SPEAKER_ON, resolvedRoute == CallAudioRoute.SPEAKER)
            .apply()
        return resolvedRoute
    }

    private fun navigateToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}

private class CallAudioController(
    private val activity: ComponentActivity,
    private val onAudioRouteResolved: (CallAudioRoute, Boolean) -> Unit = { _, _ -> }
) {
    private val audioManager =
        activity.getSystemService(AudioManager::class.java)
    private val sensorManager =
        activity.getSystemService(SensorManager::class.java)
    private val powerManager =
        activity.getSystemService(PowerManager::class.java)
    private val proximitySensor: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    private val proximityWakeLock: PowerManager.WakeLock? =
        if (powerManager != null) {
            powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "${activity.packageName}:call_proximity"
            )
        } else {
            null
        }

    private var isBound = false
    private var proximityListening = false
    private var currentCallIsVideo = false
    private var currentMicMuted = false
    private var currentPreferredRoute = CallAudioRoute.SPEAKER
    private var audioDeviceCallbackRegistered = false

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            if (isBound) {
                applyAudioRoute()
            }
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            if (isBound) {
                applyAudioRoute()
            }
        }
    }

    private val proximityListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val sensor = proximitySensor ?: return
            val distance = event.values.firstOrNull() ?: return
            val isNear = distance < sensor.maximumRange
            if (isNear) {
                acquireProximityWakeLock()
            } else {
                releaseProximityWakeLock()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun bind(
        activeCall: ActiveCallSession,
        micMuted: Boolean,
        preferredRoute: CallAudioRoute
    ) {
        currentCallIsVideo = activeCall.isVideo
        currentMicMuted = micMuted
        currentPreferredRoute = preferredRoute
        isBound = true
        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
        registerAudioDeviceCallbackIfNeeded()
        applyAudioRoute()
    }

    fun clear() {
        isBound = false
        currentCallIsVideo = false
        currentPreferredRoute = CallAudioRoute.SPEAKER
        stopProximityMonitoring()
        releaseProximityWakeLock()
        unregisterAudioDeviceCallbackIfNeeded()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager?.clearCommunicationDevice()
        }
        @Suppress("DEPRECATION")
        audioManager?.stopBluetoothSco()
        @Suppress("DEPRECATION")
        audioManager?.isBluetoothScoOn = false
        audioManager?.isSpeakerphoneOn = false
        audioManager?.mode = AudioManager.MODE_NORMAL
        audioManager?.isMicrophoneMute = false
        onAudioRouteResolved(CallAudioRoute.SPEAKER, false)
    }

    fun getNextRoute(currentRoute: CallAudioRoute, isVideo: Boolean): CallAudioRoute {
        val routes = availableRoutes(isVideo)
        if (routes.isEmpty()) return CallAudioRoute.SPEAKER
        val resolvedCurrent = normalizeRoute(currentRoute, isVideo)
        val currentIndex = routes.indexOf(resolvedCurrent).takeIf { it >= 0 } ?: 0
        return routes[(currentIndex + 1) % routes.size]
    }

    private fun applyAudioRoute() {
        val manager = audioManager ?: return
        manager.isMicrophoneMute = currentMicMuted
        val hasBluetooth = hasBluetoothDevice(manager)
        val resolvedRoute = normalizeRoute(currentPreferredRoute, currentCallIsVideo)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching {
                val targetDevice = resolveCommunicationDevice(manager, resolvedRoute)
                if (targetDevice != null) {
                    manager.setCommunicationDevice(targetDevice)
                } else {
                    manager.clearCommunicationDevice()
                }
            }.onFailure {
                manager.clearCommunicationDevice()
            }
        }

        if (resolvedRoute == CallAudioRoute.BLUETOOTH) {
            @Suppress("DEPRECATION")
            manager.startBluetoothSco()
            @Suppress("DEPRECATION")
            manager.isBluetoothScoOn = true
            manager.isSpeakerphoneOn = false
        } else {
            @Suppress("DEPRECATION")
            manager.stopBluetoothSco()
            @Suppress("DEPRECATION")
            manager.isBluetoothScoOn = false
            manager.isSpeakerphoneOn = resolvedRoute == CallAudioRoute.SPEAKER
        }

        if (isBound && resolvedRoute == CallAudioRoute.EARPIECE && !currentCallIsVideo) {
            startProximityMonitoring()
        } else {
            stopProximityMonitoring()
        }

        onAudioRouteResolved(resolvedRoute, hasBluetooth)
    }

    private fun normalizeRoute(route: CallAudioRoute, isVideo: Boolean): CallAudioRoute {
        val routes = availableRoutes(isVideo)
        return when {
            routes.contains(route) -> route
            routes.contains(CallAudioRoute.SPEAKER) -> CallAudioRoute.SPEAKER
            routes.isNotEmpty() -> routes.first()
            else -> CallAudioRoute.SPEAKER
        }
    }

    private fun availableRoutes(isVideo: Boolean): List<CallAudioRoute> {
        val routes = mutableListOf(CallAudioRoute.SPEAKER)
        if (!isVideo && hasEarpiece(audioManager)) {
            routes += CallAudioRoute.EARPIECE
        }
        if (hasBluetoothDevice(audioManager)) {
            routes += CallAudioRoute.BLUETOOTH
        }
        return routes.distinct()
    }

    private fun hasEarpiece(manager: AudioManager?): Boolean {
        val deviceManager = manager ?: return activity.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching {
                deviceManager.availableCommunicationDevices.any {
                    it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                }
            }.getOrDefault(activity.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
        } else {
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        }
    }

    private fun hasBluetoothDevice(manager: AudioManager?): Boolean {
        val deviceManager = manager ?: return false
        return runCatching {
            deviceManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { device ->
                device.type in bluetoothDeviceTypes()
            }
        }.getOrDefault(false)
    }

    private fun resolveCommunicationDevice(
        manager: AudioManager,
        route: CallAudioRoute
    ): AudioDeviceInfo? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        val preferredTypes = when (route) {
            CallAudioRoute.SPEAKER -> listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
            CallAudioRoute.EARPIECE -> listOf(AudioDeviceInfo.TYPE_BUILTIN_EARPIECE)
            CallAudioRoute.BLUETOOTH -> bluetoothDeviceTypes().toList()
        }
        return manager.availableCommunicationDevices.firstOrNull { device ->
            device.type in preferredTypes
        }
    }

    private fun bluetoothDeviceTypes(): Set<Int> {
        return setOf(
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_BLE_SPEAKER,
            AudioDeviceInfo.TYPE_HEARING_AID
        )
    }

    private fun registerAudioDeviceCallbackIfNeeded() {
        val manager = audioManager ?: return
        if (audioDeviceCallbackRegistered) return
        manager.registerAudioDeviceCallback(audioDeviceCallback, null)
        audioDeviceCallbackRegistered = true
    }

    private fun unregisterAudioDeviceCallbackIfNeeded() {
        val manager = audioManager ?: return
        if (!audioDeviceCallbackRegistered) return
        manager.unregisterAudioDeviceCallback(audioDeviceCallback)
        audioDeviceCallbackRegistered = false
    }

    private fun startProximityMonitoring() {
        val sensor = proximitySensor ?: return
        val manager = sensorManager ?: return
        if (proximityListening) return
        manager.registerListener(
            proximityListener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        proximityListening = true
    }

    private fun stopProximityMonitoring() {
        if (!proximityListening) return
        sensorManager?.unregisterListener(proximityListener)
        proximityListening = false
        releaseProximityWakeLock()
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireProximityWakeLock() {
        val wakeLock = proximityWakeLock ?: return
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }
    }

    private fun releaseProximityWakeLock() {
        val wakeLock = proximityWakeLock ?: return
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}
