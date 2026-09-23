package com.incoteam.realsaathi.core.calling

import android.app.Activity
import android.app.Application
import com.incoteam.realsaathi.core.session.SessionManager
import com.incoteam.realsaathi.data.repository.AuthRepository
import com.incoteam.realsaathi.ui.home.ActiveCallSession
import com.zegocloud.uikit.plugin.common.PluginCallbackListener
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType
import com.zegocloud.uikit.ZegoUIKit
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.config.DurationUpdateListener
import com.zegocloud.uikit.prebuilt.call.config.ZegoCallDurationConfig
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider
import com.zegocloud.uikit.prebuilt.call.invite.internal.IncomingCallButtonListener
import com.zegocloud.uikit.prebuilt.call.invite.internal.OutgoingCallButtonListener
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallType
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallUser
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInvitationCallListener
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
    private var latestCallDurationSeconds = 0L
    private var pendingOutgoingCall: ActiveCallSession? = null
    private var pendingIncomingCall: PendingIncomingCall? = null
    private var activeCall: TrackedCall? = null
    private var callEventListener: ((CallLifecycleEvent) -> Unit)? = null

    fun setCallEventListener(listener: ((CallLifecycleEvent) -> Unit)?) {
        callEventListener = listener
    }

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
                ZegoUIKit.useFrontFacingCamera(true)
                ZegoUIKitPrebuiltCallService.events.callEvents.setCallEndListener { _, _ ->
                    finishTrackedCall()
                }
                ZegoUIKitPrebuiltCallService.events.invitationEvents.setInvitationListener(invitationListener())
                ZegoUIKitPrebuiltCallService.events.invitationEvents
                    .setIncomingCallButtonListener(incomingCallButtonListener())
                ZegoUIKitPrebuiltCallService.events.invitationEvents
                    .setOutgoingCallButtonListener(outgoingCallButtonListener())
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
            pendingOutgoingCall = call
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
        pendingOutgoingCall = null
        pendingIncomingCall = null
        activeCall = null
    }

    private fun invitationConfig() = ZegoUIKitPrebuiltCallInvitationConfig().apply {
        showDeclineButton = true
        endCallWhenInitiatorLeave = true
        incomingCallBackground = application.getDrawable(com.incoteam.realsaathi.R.drawable.bg_call_waiting)
        outgoingCallBackground = application.getDrawable(com.incoteam.realsaathi.R.drawable.bg_call_waiting)
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
        config.durationConfig = ZegoCallDurationConfig().apply {
            isVisible = true
            durationUpdateListener = DurationUpdateListener { seconds ->
            latestCallDurationSeconds = seconds.coerceAtLeast(0L)
            }
        }
        config.leaveCallListener = ZegoUIKitPrebuiltCallFragment.LeaveCallListener {
            finishTrackedCall()
        }
        config.bottomMenuBarConfig.buttons = if (sessionManager.isHost()) {
            listOf(ZegoMenuBarButtonName.HANG_UP_BUTTON)
        } else if (isVideo) {
            listOf(
                ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
                ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON,
                ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON,
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

    private fun invitationListener() = object : ZegoInvitationCallListener {
        override fun onIncomingCallReceived(
            callID: String,
            caller: ZegoCallUser,
            type: ZegoCallType,
            invitees: MutableList<ZegoCallUser>
        ) {
            pendingIncomingCall = PendingIncomingCall(
                callId = callID,
                userId = caller.id,
                name = caller.name,
                isVideo = type == ZegoCallType.VIDEO_CALL
            )
        }

        override fun onIncomingCallCanceled(callID: String, caller: ZegoCallUser) {
            pendingIncomingCall?.takeIf { it.callId == callID }?.let {
                emitEvent(it, "missed", 0L)
                pendingIncomingCall = null
            }
        }

        override fun onIncomingCallTimeout(callID: String, caller: ZegoCallUser) {
            pendingIncomingCall?.takeIf { it.callId == callID }?.let {
                emitEvent(it, "missed", 0L)
                pendingIncomingCall = null
            }
        }

        override fun onOutgoingCallAccepted(callID: String, callee: ZegoCallUser) {
            pendingOutgoingCall?.let { call ->
                activeCall = TrackedCall(
                    callId = callID,
                    userId = call.userId,
                    name = call.name.ifBlank { callee.name },
                    isVideo = call.isVideo,
                    ratePerMinute = call.ratePerMinute,
                    startedAtMillis = System.currentTimeMillis()
                )
                pendingOutgoingCall = null
            }
        }

        override fun onOutgoingCallRejectedCauseBusy(callID: String, callee: ZegoCallUser) {
            pendingOutgoingCall?.let {
                emitEvent(it, "canceled", 0L, callID, callee.id, callee.name)
                pendingOutgoingCall = null
            }
        }

        override fun onOutgoingCallDeclined(callID: String, callee: ZegoCallUser) {
            pendingOutgoingCall?.let {
                emitEvent(it, "canceled", 0L, callID, callee.id, callee.name)
                pendingOutgoingCall = null
            }
        }

        override fun onOutgoingCallTimeout(callID: String, callees: MutableList<ZegoCallUser>) {
            pendingOutgoingCall?.let {
                val callee = callees.firstOrNull()
                emitEvent(it, "canceled", 0L, callID, callee?.id ?: "", callee?.name ?: "")
                pendingOutgoingCall = null
            }
        }
    }

    private fun incomingCallButtonListener() = object : IncomingCallButtonListener {
        override fun onIncomingCallDeclineButtonPressed() {
            pendingIncomingCall?.let {
                emitEvent(it, "declined", 0L)
                pendingIncomingCall = null
            }
        }

        override fun onIncomingCallAcceptButtonPressed() {
            pendingIncomingCall?.let {
                activeCall = TrackedCall(
                    callId = it.callId,
                    userId = it.userId,
                    name = it.name,
                    isVideo = it.isVideo,
                    ratePerMinute = 0,
                    startedAtMillis = System.currentTimeMillis()
                )
                pendingIncomingCall = null
            }
        }
    }

    private fun outgoingCallButtonListener() = object : OutgoingCallButtonListener {
        override fun onOutgoingCallCancelButtonPressed() {
            pendingOutgoingCall?.let {
                emitEvent(it, "canceled", 0L)
                pendingOutgoingCall = null
            }
        }
    }

    private fun emitEvent(
        call: PendingIncomingCall,
        status: String,
        durationSeconds: Long,
        callId: String = call.callId,
        counterpartyId: String = call.userId,
        counterpartyName: String = call.name
    ) {
        callEventListener?.invoke(
            CallLifecycleEvent(
                callId = callId,
                counterpartyId = counterpartyId,
                counterpartyName = counterpartyName,
                isVideo = call.isVideo,
                status = status,
                durationSeconds = durationSeconds,
                ratePerMinute = 0
            )
        )
    }

    private fun emitEvent(
        call: ActiveCallSession,
        status: String,
        durationSeconds: Long,
        callId: String = pendingOutgoingCall?.let { "" } ?: "",
        counterpartyId: String = call.userId,
        counterpartyName: String = call.name
    ) {
        callEventListener?.invoke(
            CallLifecycleEvent(
                callId = callId,
                counterpartyId = counterpartyId,
                counterpartyName = counterpartyName,
                isVideo = call.isVideo,
                status = status,
                durationSeconds = durationSeconds,
                ratePerMinute = call.ratePerMinute
            )
        )
    }

    private fun emitEvent(call: TrackedCall, status: String, durationSeconds: Long) {
        callEventListener?.invoke(
            CallLifecycleEvent(
                callId = call.callId,
                counterpartyId = call.userId,
                counterpartyName = call.name,
                isVideo = call.isVideo,
                status = status,
                durationSeconds = durationSeconds,
                ratePerMinute = call.ratePerMinute
            )
        )
    }

    private fun finishTrackedCall() {
        val tracked = activeCall ?: return
        val elapsedSeconds = ((System.currentTimeMillis() - tracked.startedAtMillis) / 1000L)
            .coerceAtLeast(1L)
        emitEvent(tracked, "completed", maxOf(latestCallDurationSeconds, elapsedSeconds))
        activeCall = null
        pendingIncomingCall = null
        pendingOutgoingCall = null
        latestCallDurationSeconds = 0L
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

data class CallLifecycleEvent(
    val callId: String,
    val counterpartyId: String,
    val counterpartyName: String,
    val isVideo: Boolean,
    val status: String,
    val durationSeconds: Long,
    val ratePerMinute: Int
)

private data class PendingIncomingCall(
    val callId: String,
    val userId: String,
    val name: String,
    val isVideo: Boolean
)

private data class TrackedCall(
    val callId: String,
    val userId: String,
    val name: String,
    val isVideo: Boolean,
    val ratePerMinute: Int,
    val startedAtMillis: Long
)
