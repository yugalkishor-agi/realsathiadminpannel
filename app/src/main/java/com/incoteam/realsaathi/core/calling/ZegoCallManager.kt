package com.incoteam.realsaathi.core.calling

import android.app.Activity
import android.app.Application
import com.incoteam.realsaathi.core.session.SessionManager
import com.incoteam.realsaathi.data.repository.AuthRepository
import com.incoteam.realsaathi.ui.home.ActiveCallSession
import com.zegocloud.uikit.plugin.common.PluginCallbackListener
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ZegoCallManager(
    private val application: Application,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
) {
    private val initializationLock = Mutex()
    private var initializedUserId = ""

    suspend fun initialize(): Result<Unit> = runCatching {
        initializationLock.withLock {
            val accountId = sessionManager.getUserId().trim()
            if (accountId.isBlank()) error("Please login again.")
            if (initializedUserId == accountId) return@withLock

            val credentials = authRepository.getZegoToken(sessionManager.getAccessToken()).getOrThrow()
            require(credentials.appId > 0L && credentials.token.isNotBlank() && credentials.userId.isNotBlank()) {
                "Calling configuration is incomplete."
            }
            val displayName = sessionManager.getDisplayName().trim().ifBlank { "RealSaathi User" }

            withContext(Dispatchers.Main.immediate) {
                if (initializedUserId.isNotBlank()) {
                    ZegoUIKitPrebuiltCallService.unInit()
                }
                ZegoUIKitPrebuiltCallService.initWithToken(
                    application,
                    credentials.appId,
                    credentials.token,
                    credentials.userId,
                    displayName,
                    invitationConfig(),
                )
            }
            initializedUserId = accountId
        }
    }

    suspend fun startCall(
        activity: Activity,
        call: ActiveCallSession,
        onFailure: (String) -> Unit,
    ) {
        initialize().getOrElse { error ->
            onFailure(error.message ?: "Calling is unavailable right now.")
            return
        }

        val inviteeId = toZegoUserId(call.userId)
        if (inviteeId.isBlank()) {
            onFailure("This user cannot receive calls right now.")
            return
        }

        withContext(Dispatchers.Main.immediate) {
            val invitationType = if (call.isVideo) {
                ZegoInvitationType.VIDEO_CALL
            } else {
                ZegoInvitationType.VOICE_CALL
            }
            ZegoUIKitPrebuiltCallService.sendInvitationWithUIChange(
                activity,
                listOf(ZegoUIKitUser(inviteeId, call.name.ifBlank { "RealSaathi User" })),
                invitationType,
                PluginCallbackListener { result ->
                    val code = (result["code"] as? Number)?.toInt()
                        ?: (result["errorCode"] as? Number)?.toInt()
                        ?: 0
                    if (code != 0) {
                        val message = result["message"]?.toString()
                            ?: result["errorMessage"]?.toString()
                            ?: "Call connect nahi ho payi."
                        onFailure(message)
                    }
                },
            )
        }
    }

    fun uninitialize() {
        if (initializedUserId.isNotBlank()) {
            ZegoUIKitPrebuiltCallService.unInit()
            initializedUserId = ""
        }
    }

    private fun invitationConfig() = ZegoUIKitPrebuiltCallInvitationConfig().apply {
        showDeclineButton = true
        endCallWhenInitiatorLeave = true
        provider = ZegoUIKitPrebuiltCallConfigProvider { invitation ->
            val isVideo = invitation.type == ZegoInvitationType.VIDEO_CALL.value
            polishedCallConfig(isVideo)
        }
    }

    private fun polishedCallConfig(isVideo: Boolean): ZegoUIKitPrebuiltCallConfig {
        val config = if (isVideo) {
            ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
        } else {
            ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()
        }
        config.turnOnCameraWhenJoining = isVideo
        config.turnOnMicrophoneWhenJoining = true
        config.useSpeakerWhenJoining = isVideo
        config.topMenuBarConfig.isVisible = false
        config.topMenuBarConfig.buttons = emptyList()
        config.bottomMenuBarConfig.buttons = if (isVideo) {
            listOf(
                ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
                ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON,
                ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON,
                ZegoMenuBarButtonName.SWITCH_CAMERA_BUTTON,
                ZegoMenuBarButtonName.HANG_UP_BUTTON,
            )
        } else {
            listOf(
                ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
                ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON,
                ZegoMenuBarButtonName.HANG_UP_BUTTON,
            )
        }
        config.bottomMenuBarConfig.maxCount = config.bottomMenuBarConfig.buttons.size
        config.bottomMenuBarConfig.hideAutomatically = false
        config.bottomMenuBarConfig.hideByClick = false
        return config
    }

    companion object {
        fun toZegoUserId(accountId: String): String {
            return accountId
                .trim()
                .replace(Regex("[^A-Za-z0-9_]"), "")
                .take(32)
        }
    }
}
