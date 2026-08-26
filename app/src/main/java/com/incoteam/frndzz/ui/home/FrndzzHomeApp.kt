@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)

package com.incoteam.frndzz.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.incoteam.frndzz.R
import com.incoteam.frndzz.FrndzzApp
import com.incoteam.frndzz.core.session.SessionManager
import com.incoteam.frndzz.data.model.auth.DiscoveryHost
import com.incoteam.frndzz.data.model.auth.RandomMatchRequest
import com.incoteam.frndzz.data.model.auth.RecordWalletTransactionRequest
import com.incoteam.frndzz.data.model.auth.ReportUserRequest
import com.incoteam.frndzz.data.model.auth.RemoteWalletTransaction
import com.incoteam.frndzz.data.model.auth.SupportChatMessage
import com.incoteam.frndzz.data.model.auth.SupportChatRequest
import com.incoteam.frndzz.data.model.auth.UnblockUserRequest
import com.incoteam.frndzz.data.repository.AuthRepository
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun FrndzzHomeApp(
    sessionManager: SessionManager,
    activeCall: ActiveCallSession?,
    isInPictureInPictureMode: Boolean,
    startInProfileSetup: Boolean,
    onStartCall: (ActiveCallSession) -> Unit,
    onEndCall: () -> Unit,
    onEnterPictureInPicture: () -> Unit,
    onExitApp: () -> Unit,
    onSwitchToHostMode: () -> Unit,
    onProfileSetupCompleted: () -> Unit,
    onSensitiveContentVisible: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as FrndzzApp }
    val authRepository = remember(app) { app.authRepository }
    val scope = rememberCoroutineScope()
    val coinsState = remember { mutableStateOf(UserPrefs.getCoins(context)) }
    val homeUsers = remember {
        mutableStateListOf<User>().apply {
            addAll(defaultHomeUsers())
        }
    }
    var isHomeRefreshing by remember { mutableStateOf(false) }
    val offlineReturnAt = remember {
        mutableStateMapOf<String, Long>().apply {
            val now = System.currentTimeMillis()
            homeUsers.forEach { user ->
                if (user.presence == UserPresence.OFFLINE) {
                    this[user.id] = now + OfflineHostReturnMillis
                }
            }
        }
    }
    val busyReturnAt = remember {
        mutableStateMapOf<String, Long>().apply {
            val now = System.currentTimeMillis()
            homeUsers.forEach { user ->
                if (user.presence == UserPresence.BUSY && user.busyForMinutes != null) {
                    this[user.id] = now + (user.busyForMinutes * 60_000L)
                }
            }
        }
    }

    val markHostOnline: (String) -> Unit = { userId ->
        offlineReturnAt.remove(userId)
        busyReturnAt.remove(userId)
        val userIndex = homeUsers.indexOfFirst { it.id == userId }
        if (userIndex >= 0) {
            val current = homeUsers[userIndex]
            homeUsers[userIndex] = current.copy(
                presence = UserPresence.ONLINE,
                busyForMinutes = null
            )
        }
    }

    val markHostBusy: (String, Int?) -> Unit = { userId, busyMinutes ->
        offlineReturnAt.remove(userId)
        if (busyMinutes != null && busyMinutes > 0) {
            busyReturnAt[userId] = System.currentTimeMillis() + (busyMinutes * 60_000L)
        } else {
            busyReturnAt.remove(userId)
        }
        val userIndex = homeUsers.indexOfFirst { it.id == userId }
        if (userIndex >= 0) {
            val current = homeUsers[userIndex]
            homeUsers[userIndex] = current.copy(
                presence = UserPresence.BUSY,
                busyForMinutes = busyMinutes?.takeIf { it > 0 }
            )
        }
    }

    LaunchedEffect(Unit) {
        UserPrefs.unlockSeededProfile(context)
        if (UserPrefs.getNickname(context).isBlank()) {
            val seededName = sessionManager.getDisplayName()
                .trim()
                .takeIf { it.isNotBlank() }
                ?.replace("\\s+".toRegex(), "_")
                ?.lowercase()
                ?: UserPrefs.generateRandomNickname(context)
            UserPrefs.seedNickname(context, seededName)
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            val now = System.currentTimeMillis()
            for (index in homeUsers.indices) {
                val user = homeUsers[index]
                when (user.presence) {
                    UserPresence.OFFLINE -> {
                        val onlineAt = offlineReturnAt[user.id] ?: continue
                        if (now >= onlineAt) {
                            homeUsers[index] = user.copy(
                                presence = UserPresence.ONLINE,
                                busyForMinutes = null
                            )
                            offlineReturnAt.remove(user.id)
                        }
                    }

                    UserPresence.BUSY -> {
                        val availableAt = busyReturnAt[user.id] ?: continue
                        val remainingMillis = availableAt - now
                        if (remainingMillis <= 0L) {
                            homeUsers[index] = user.copy(
                                presence = UserPresence.ONLINE,
                                busyForMinutes = null
                            )
                            busyReturnAt.remove(user.id)
                        } else {
                            val remainingMinutes =
                                ((remainingMillis + 59_999L) / 60_000L).toInt().coerceAtLeast(1)
                            if (user.busyForMinutes != remainingMinutes) {
                                homeUsers[index] = user.copy(busyForMinutes = remainingMinutes)
                            }
                        }
                    }

                    UserPresence.ONLINE -> Unit
                }
            }
            delay(30_000L)
        }
    }

    LaunchedEffect(activeCall?.userId) {
        activeCall?.let { currentCall ->
            markHostBusy(currentCall.userId, null)
        }
    }

    LaunchedEffect(coinsState.value) {
        UserPrefs.saveCoins(context, coinsState.value)
    }

    suspend fun loadRemoteDiscoveryHosts(): Boolean {
        val accessToken = sessionManager.getAccessToken().trim()
        if (accessToken.isBlank()) return false
        val previousUsers = homeUsers.associateBy { it.id }
        val response = authRepository.getDiscoveryHosts(
            accessToken = accessToken,
            liveOnly = false
        )
        val remoteUsers = response
            .getOrNull()
            ?.hosts
            .orEmpty()
            .map { remoteHost ->
                val remoteUser = remoteHost.toHomeUser()
                val previousUser = previousUsers[remoteUser.id]
                when (previousUser?.presence) {
                    UserPresence.BUSY -> remoteUser.copy(
                        presence = UserPresence.BUSY,
                        busyForMinutes = previousUser.busyForMinutes
                    )
                    UserPresence.OFFLINE -> remoteUser.copy(presence = UserPresence.OFFLINE)
                    else -> remoteUser
                }
            }
        if (remoteUsers.isEmpty()) return false
        homeUsers.clear()
        homeUsers.addAll(remoteUsers)
        offlineReturnAt.clear()
        busyReturnAt.clear()
        val now = System.currentTimeMillis()
        remoteUsers.forEach { user ->
            if (user.presence == UserPresence.OFFLINE) {
                offlineReturnAt[user.id] = now + OfflineHostReturnMillis
            }
            if (user.presence == UserPresence.BUSY && user.busyForMinutes != null) {
                busyReturnAt[user.id] = now + (user.busyForMinutes * 60_000L)
            }
        }
        return true
    }

    LaunchedEffect(Unit) {
        loadRemoteDiscoveryHosts()
    }

    fun refreshHomeFeed() {
        if (isHomeRefreshing) return
        scope.launch {
            isHomeRefreshing = true
            val loadedRemoteHosts = loadRemoteDiscoveryHosts()
            if (!loadedRemoteHosts) {
                delay(500L)
                val currentOrder = homeUsers.map { it.id }
                val shuffledUsers = homeUsers.toList().shuffled()
                val nextUsers = if (shuffledUsers.map { it.id } == currentOrder && shuffledUsers.size > 1) {
                    shuffledUsers.drop(1) + shuffledUsers.first()
                } else {
                    shuffledUsers
                }
                homeUsers.clear()
                homeUsers.addAll(nextUsers)
            }
            isHomeRefreshing = false
        }
    }


    CompositionLocalProvider(LocalCoins provides coinsState) {
        val startCallWithSharedLogic: (ActiveCallSession) -> Unit = { session ->
            launchSharedCall(
                session = session,
                availableCoins = coinsState.value,
                onCoinsUsed = { cost ->
                    coinsState.value = (coinsState.value - cost).coerceAtLeast(0)
                },
                onInsufficientCoins = {
                    toast(context, "Not enough coins")
                },
                onCallStarted = { startedSession ->
                    markHostBusy(startedSession.userId, null)
                },
                onStartCall = onStartCall
            )
        }
        MainScaffold(
            sessionManager = sessionManager,
            homeUsers = homeUsers,
            isHomeRefreshing = isHomeRefreshing,
            activeCall = activeCall,
            isInPictureInPictureMode = isInPictureInPictureMode,
            startInProfileSetup = startInProfileSetup,
            onStartCall = startCallWithSharedLogic,
            onEndCall = onEndCall,
            onRefreshHome = ::refreshHomeFeed,
            onUserReturnedOnline = markHostOnline,
            onEnterPictureInPicture = onEnterPictureInPicture,
            onExitApp = onExitApp,
            onSwitchToHostMode = onSwitchToHostMode,
            onProfileSetupCompleted = onProfileSetupCompleted,
            onSensitiveContentVisible = onSensitiveContentVisible,
            onLogout = onLogout
        )
    }
}

private const val ChatUnlockMinDurationSeconds = 5L * 60L
private const val ChatUnlockWindowMillis = 7L * 24L * 60L * 60L * 1000L
private const val FaceMissingCameraTimeoutMillis = 5_000L
private const val OfflineHostReturnMillis = 10L * 60L * 1000L
private const val CallLowTimeWarningSeconds = 2L * 60L
private const val SensitiveChatPlaceholder = "Sensitive message"
private const val AudioCallRateCoinsPerMinute = 14
private const val VideoCallRateCoinsPerMinute = 66
private const val ChatMessageCostCoins = 1

private data class PendingVoiceNoteDraft(
    val uri: String,
    val durationSeconds: Int
)

private data class CallEndSummary(
    val name: String,
    val isVideo: Boolean,
    val durationSeconds: Long,
    val coinsSpent: Int,
    val hostEarnedEstimate: Int,
    val giftHistory: String?,
    val chatUnlocked: Boolean,
    val reportedAndBlocked: Boolean = false
)

private fun resolveCallRatePerMinute(userId: String, isVideo: Boolean): Int {
    return if (isVideo) VideoCallRateCoinsPerMinute else AudioCallRateCoinsPerMinute
}

private fun computeCallAllowanceSeconds(availableCoins: Int, ratePerMinute: Int): Long? {
    if (ratePerMinute <= 0 || availableCoins <= 0) return null
    return ((availableCoins.toLong() * 60L) / ratePerMinute).coerceAtLeast(60L)
}

private fun computeGiftTimeDeductionSeconds(coins: Int, ratePerMinute: Int): Long {
    if (ratePerMinute <= 0 || coins <= 0) return 0L
    return ((coins.toLong() * 60L) / ratePerMinute).coerceAtLeast(1L)
}

private fun estimateCallCoins(durationSeconds: Long, ratePerMinute: Int): Int {
    if (durationSeconds <= 0L || ratePerMinute <= 0) return 0
    val billedMinutes = ((durationSeconds + 59L) / 60L).coerceAtLeast(1L)
    return (billedMinutes * ratePerMinute).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}

private fun enrichActiveCallSession(
    session: ActiveCallSession,
    availableCoins: Int
): ActiveCallSession {
    val resolvedRate = session.ratePerMinute.takeIf { it > 0 }
        ?: resolveCallRatePerMinute(session.userId, session.isVideo)
    val resolvedAllowance = session.includedSecondsAtStart
        ?: computeCallAllowanceSeconds(availableCoins, resolvedRate)
    return session.copy(
        ratePerMinute = resolvedRate,
        includedSecondsAtStart = resolvedAllowance
    )
}

private fun launchSharedCall(
    session: ActiveCallSession,
    availableCoins: Int,
    onCoinsUsed: (Int) -> Unit,
    onInsufficientCoins: () -> Unit,
    onCallStarted: (ActiveCallSession) -> Unit = {},
    onStartCall: (ActiveCallSession) -> Unit
) {
    val enrichedSession = enrichActiveCallSession(session, availableCoins)
    val startCost = enrichedSession.ratePerMinute.coerceAtLeast(0)
    if (startCost > 0 && availableCoins < startCost) {
        onInsufficientCoins()
        return
    }
    if (startCost > 0) {
        onCoinsUsed(startCost)
    }
    onCallStarted(enrichedSession)
    onStartCall(enrichedSession)
}

private val PhoneSharePattern = Regex("""(?<!\d)(?:\+?\d[\d\s\-]{7,}\d)(?!\d)""")
private val SocialSharePattern = Regex(
    """(?i)(instagram|insta|snapchat|snap|telegram|whatsapp|wa\.me|facebook|fb|discord|signal|gmail|email|mail id|t\.me|https?://|www\.|@[\w._]{3,})"""
)

private fun isSensitiveChatContent(text: String): Boolean {
    val trimmed = text.trim()
    if (trimmed.isBlank()) return false
    return PhoneSharePattern.containsMatchIn(trimmed) || SocialSharePattern.containsMatchIn(trimmed)
}

private fun sanitizeChatMessage(text: String, allowSensitive: Boolean = false): String {
    val trimmed = text.trim()
    if (trimmed.isBlank()) return trimmed
    return if (!allowSensitive && isSensitiveChatContent(trimmed)) SensitiveChatPlaceholder else trimmed
}

private fun buildSupportThread(now: Long = System.currentTimeMillis()): ChatThread {
    val supportMessageAt = now - (35L * 60L * 1000L)
    return ChatThread(
        id = "support",
        title = "Frndzzz AI",
        subtitle = "App help aur quick replies ke liye yahin message karo.",
        isPinned = true,
        unreadCount = 1,
        isOnline = true,
        lastSeenAtMillis = now,
        messages = listOf(
            ChatMessage(
                id = "support-1",
                text = "Hi! Main Frndzzz AI hoon. App, profile, calls, coins, aur account questions me help kar sakta hoon.",
                fromUser = false,
                timestampMillis = supportMessageAt
            )
        )
    )
}

private fun buildInitialCallHistory(now: Long = System.currentTimeMillis()): List<CallHistory> {
    return listOf(
        CallHistory(
            userId = "1",
            name = "Aisha",
            startedAtMillis = now - (2L * 60L * 60L * 1000L),
            durationSeconds = 6L * 60L + 14L,
            isVideo = true
        ),
        CallHistory(
            userId = "3",
            name = "Pooja",
            startedAtMillis = now - (22L * 60L * 60L * 1000L),
            durationSeconds = 5L * 60L + 8L,
            isVideo = false
        ),
        CallHistory(
            userId = "5",
            name = "Neha",
            startedAtMillis = now - (3L * 60L * 60L * 1000L),
            durationSeconds = 3L * 60L + 22L,
            isVideo = true
        )
    )
}

private fun callEndedAt(call: CallHistory): Long {
    return call.startedAtMillis + (call.durationSeconds * 1000L)
}

private fun isChatUnlocked(call: CallHistory, now: Long = System.currentTimeMillis()): Boolean {
    val unlockExpiresAt = callEndedAt(call) + ChatUnlockWindowMillis
    return call.durationSeconds >= ChatUnlockMinDurationSeconds && unlockExpiresAt > now
}

private fun defaultUnlockedMessage(call: CallHistory): String {
    return if (call.isVideo) {
        "Video call ke baad chat unlock ho gayi. Next 7 days tak yahin connect reh sakte ho."
    } else {
        "Audio call ke baad chat unlock ho gayi. Next 7 days tak yahin baat continue kar sakte ho."
    }
}

private fun buildUnlockedThread(call: CallHistory): ChatThread {
    val endedAt = callEndedAt(call)
    return ChatThread(
        id = call.userId,
        title = call.name,
        subtitle = defaultUnlockedMessage(call),
        unreadCount = 1,
        lastSeenAtMillis = endedAt,
        unlockedUntilMillis = endedAt + ChatUnlockWindowMillis,
        messages = listOf(
            ChatMessage(
                id = "${call.userId}-unlock",
                text = defaultUnlockedMessage(call),
                fromUser = false,
                timestampMillis = endedAt
            )
        )
    )
}

private fun syncUnlockedChatThreads(
    chatThreads: SnapshotStateList<ChatThread>,
    callHistoryList: List<CallHistory>,
    blockedThreadIds: Set<String> = emptySet(),
    now: Long = System.currentTimeMillis()
) {
    if (chatThreads.none { it.id == "support" }) {
        chatThreads.add(0, buildSupportThread(now))
    }

    val latestUnlockedCalls = callHistoryList
        .filter { isChatUnlocked(it, now) && it.userId !in blockedThreadIds }
        .groupBy { it.userId }
        .mapValues { (_, entries) -> entries.maxBy { callEndedAt(it) } }

    for (index in chatThreads.lastIndex downTo 0) {
        val item = chatThreads[index]
        if (!item.isPinned && (item.id in blockedThreadIds || item.id !in latestUnlockedCalls.keys)) {
            chatThreads.removeAt(index)
        }
    }

    latestUnlockedCalls.values.forEach { call ->
        val threadIndex = chatThreads.indexOfFirst { it.id == call.userId }
        val endedAt = callEndedAt(call)
        val unlockedUntil = endedAt + ChatUnlockWindowMillis
        if (threadIndex >= 0) {
            val existing = chatThreads[threadIndex]
            chatThreads[threadIndex] = existing.copy(
                title = call.name,
                subtitle = existing.messages.lastOrNull()?.text ?: defaultUnlockedMessage(call),
                lastSeenAtMillis = maxOf(existing.lastSeenAtMillis ?: 0L, endedAt),
                unlockedUntilMillis = unlockedUntil,
                unreadCount = if (existing.isPinned) existing.unreadCount else existing.unreadCount.coerceAtLeast(1)
            )
        } else {
            chatThreads.add(buildUnlockedThread(call))
        }
    }
}

private fun formatClockTime(timestampMillis: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestampMillis))
}

private fun formatListTime(timestampMillis: Long): String {
    val pattern = if (isSameDay(timestampMillis, System.currentTimeMillis())) "h:mm a" else "dd MMM"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestampMillis))
}

private fun formatMessageTime(timestampMillis: Long): String {
    return formatClockTime(timestampMillis)
}

private fun formatLastSeen(timestampMillis: Long?): String {
    if (timestampMillis == null) return "Last seen recently"
    return when {
        isSameDay(timestampMillis, System.currentTimeMillis()) -> "Last seen today at ${formatClockTime(timestampMillis)}"
        isYesterday(timestampMillis) -> "Last seen yesterday at ${formatClockTime(timestampMillis)}"
        else -> {
            val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestampMillis))
            "Last seen $date at ${formatClockTime(timestampMillis)}"
        }
    }
}

private fun formatCompactRelativeTime(timestampMillis: Long?): String {
    if (timestampMillis == null) return "now"
    val diff = (System.currentTimeMillis() - timestampMillis).coerceAtLeast(0L)
    val minutes = diff / (60L * 1000L)
    val hours = diff / (60L * 60L * 1000L)
    val days = diff / (24L * 60L * 60L * 1000L)
    return when {
        minutes < 1L -> "now"
        minutes < 60L -> "${minutes}m"
        hours < 24L -> "${hours}h"
        days < 7L -> "${days}d"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestampMillis))
    }
}

private fun formatSeenRelativeTime(timestampMillis: Long?): String {
    if (timestampMillis == null) return "Sent"
    val diff = (System.currentTimeMillis() - timestampMillis).coerceAtLeast(0L)
    val minutes = diff / (60L * 1000L)
    val hours = diff / (60L * 60L * 1000L)
    val days = diff / (24L * 60L * 60L * 1000L)
    return when {
        minutes < 1L -> "Seen just now"
        minutes < 60L -> "Seen ${minutes}m ago"
        hours < 24L -> "Seen ${hours}h ago"
        days < 7L -> "Seen ${days}d ago"
        else -> "Seen ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestampMillis))}"
    }
}

private fun formatCallDuration(durationSeconds: Long): String {
    val minutes = durationSeconds / 60L
    val seconds = durationSeconds % 60L
    return if (seconds == 0L) {
        String.format(Locale.getDefault(), "%02d min", minutes)
    } else {
        String.format(Locale.getDefault(), "%02d min %02d sec", minutes, seconds)
    }
}

private fun isSameDay(firstMillis: Long, secondMillis: Long): Boolean {
    val first = Calendar.getInstance().apply { timeInMillis = firstMillis }
    val second = Calendar.getInstance().apply { timeInMillis = secondMillis }
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(timestampMillis: Long, nowMillis: Long = System.currentTimeMillis()): Boolean {
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val target = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    return yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
}

private fun defaultHomeUsers(): List<User> {
    return listOf(
        User("1", "Aisha", listOf("Talk", "Music"), "Hindi", UserPresence.ONLINE, 5),
        User("2", "Riya", listOf("Fun", "Chat"), "English", UserPresence.OFFLINE, 4),
        User("3", "Pooja", listOf("Music", "Vibes"), "Hindi", UserPresence.ONLINE, 6),
        User("4", "Simran", listOf("Talk", "Friendship"), "Marathi", UserPresence.BUSY, 3, 9),
        User("5", "Neha", listOf("Fun", "Chill"), "English", UserPresence.OFFLINE, 7),
        User("6", "Kajal", listOf("Music", "Chat"), "Hindi", UserPresence.ONLINE, 5),
        User("7", "Anjali", listOf("Fun", "Talk"), "Marathi", UserPresence.ONLINE, 4),
        User("8", "Snezha", listOf("Chill", "Music"), "Hindi", UserPresence.BUSY, 6, 14),
        User("9", "Meera", listOf("Talk", "Vibes"), "English", UserPresence.ONLINE, 5),
        User("10", "Tanya", listOf("Chat", "Fun"), "Hindi", UserPresence.ONLINE, 4),
        User("11", "Nupur", listOf("Poetry", "Talk"), "Hindi", UserPresence.BUSY, 4, 6),
        User("12", "Ishita", listOf("Music", "Late Night"), "English", UserPresence.ONLINE, 6),
        User("13", "Mahi", listOf("Fun", "Roast"), "Punjabi", UserPresence.OFFLINE, 5),
        User("14", "Zoya", listOf("Vibes", "Chill"), "Hindi", UserPresence.ONLINE, 7),
        User("15", "Shruti", listOf("Talk", "Movies"), "Marathi", UserPresence.BUSY, 4, 18),
        User("16", "Diya", listOf("Music", "Coffee"), "Gujarati", UserPresence.ONLINE, 5),
        User("17", "Sakshi", listOf("Chat", "Friendship"), "Hindi", UserPresence.OFFLINE, 3),
        User("18", "Khushi", listOf("Chill", "Fun"), "English", UserPresence.ONLINE, 6),
        User("19", "Priya", listOf("Travel", "Talk"), "Hindi", UserPresence.BUSY, 5, 11),
        User("20", "Aarohi", listOf("Music", "Books"), "Marathi", UserPresence.ONLINE, 4),
        User("21", "Pallavi", listOf("Chat", "Gaming"), "Hindi", UserPresence.ONLINE, 5),
        User("22", "Radhika", listOf("Fun", "Music"), "Bengali", UserPresence.OFFLINE, 6),
        User("23", "Komal", listOf("Talk", "Tea"), "Hindi", UserPresence.BUSY, 4, 22),
        User("24", "Heena", listOf("Movies", "Chill"), "Gujarati", UserPresence.ONLINE, 5),
        User("25", "Lavanya", listOf("Music", "Night Owl"), "Tamil", UserPresence.ONLINE, 7),
        User("26", "Payal", listOf("Chat", "Vibes"), "Hindi", UserPresence.OFFLINE, 4),
        User("27", "Trisha", listOf("Fun", "Coffee"), "English", UserPresence.BUSY, 5, 8),
        User("28", "Sonal", listOf("Talk", "Poetry"), "Hindi", UserPresence.ONLINE, 3),
        User("29", "Bhavna", listOf("Music", "Friendship"), "Punjabi", UserPresence.ONLINE, 6),
        User("30", "Reet", listOf("Chill", "Talk"), "Hindi", UserPresence.BUSY, 5, 16)
    )
}

private fun DiscoveryHost.toHomeUser(): User {
    val livePresence = when {
        hostAudioLive || hostVideoLive -> UserPresence.ONLINE
        else -> UserPresence.OFFLINE
    }
    val tags = topicTags.ifEmpty {
        listOfNotNull(preferredLanguage.takeIf { it.isNotBlank() && it != "All" }, "Talk")
    }
    return User(
        id = id,
        name = nickname.ifBlank { username.orEmpty().ifBlank { "Frndzz User" } },
        interests = tags,
        language = nativeLanguages.firstOrNull().orEmpty().ifBlank { preferredLanguage.ifBlank { "All" } },
        presence = livePresence,
        ratePerMinute = hostAudioRate.takeIf { it > 0 } ?: AudioCallRateCoinsPerMinute,
        publicId = publicId
    )
}

private fun String.toReportReasonCode(): String {
    val normalized = lowercase(Locale.getDefault())
    return when {
        "harass" in normalized || "abuse" in normalized || "bad" in normalized -> "harassment"
        "fake" in normalized || "fraud" in normalized -> "fake_profile"
        "inappropriate" in normalized || "sexual" in normalized || "nude" in normalized -> "inappropriate_content"
        else -> "other"
    }
}

private fun userMatchesDiscoveryTopic(user: User, topic: String): Boolean {
    val normalizedTopic = topic.trim().lowercase()
    if (normalizedTopic == "all") return true
    val aliases = when (normalizedTopic) {
        "relationships" -> setOf("relationship", "friendship", "talk", "chat")
        "career" -> setOf("career", "growth", "ambition", "study")
        "travel" -> setOf("travel", "culture")
        "music" -> setOf("music", "films", "movies", "poetry")
        "food" -> setOf("food", "coffee", "tea")
        "friendship" -> setOf("friendship", "talk", "chat", "vibes")
        "movies" -> setOf("movies", "films", "music")
        else -> setOf(normalizedTopic)
    }
    return user.interests.any { interest ->
        val normalizedInterest = interest.lowercase()
        aliases.any { alias ->
            normalizedInterest.contains(alias) || alias.contains(normalizedInterest)
        }
    }
}

private fun buildHostStories(
    context: Context,
    users: List<User>,
    nowMillis: Long = System.currentTimeMillis()
): List<HostStory> {
    val frndzzzStoryUser = User(
        id = "support-story",
        name = "Frndzzz",
        interests = listOf("Support"),
        language = "Hindi",
        presence = UserPresence.ONLINE,
        ratePerMinute = 0
    )
    val uploadedStories = UserPrefs.getHostUploadedStories(context, nowMillis)
    val uploadedHostStory = uploadedStories.takeIf { it.isNotEmpty() }?.let { stories ->
        HostStory(
            user = User(
                id = stories.first().ownerId.ifBlank { "host-preview-story" },
                name = stories.first().ownerName.ifBlank {
                    UserPrefs.getHostDisplayName(context).ifBlank { "Aisha" }
                },
                interests = listOf("Host"),
                language = "Hindi",
                presence = UserPresence.ONLINE,
                ratePerMinute = 0
            ),
            moments = stories.map { story ->
                HostStoryMoment(
                    vibeTitle = story.title,
                    caption = story.caption,
                    mediaUri = story.mediaUri,
                    mediaType = story.mediaType
                )
            },
            gradientStart = Color(0xFF6F2BFF),
            gradientEnd = Color(0xFFEF476F),
            accent = Color(0xFFF8F272)
        )
    }

    return buildList {
        add(
            HostStory(
                user = frndzzzStoryUser,
                moments = listOf(
                    HostStoryMoment(
                        vibeTitle = "Frndzzz is live",
                        caption = "App updates, support vibe aur latest highlights yahin milenge."
                    ),
                    HostStoryMoment(
                        vibeTitle = "Support is one tap away",
                        caption = "Account help, recharge help aur call issues ke liye support team active hai."
                    ),
                    HostStoryMoment(
                        vibeTitle = "Stay in the vibe",
                        caption = "Naye hosts, better calls aur smoother experience ke updates yahin milenge."
                    )
                ),
                gradientStart = Color(0xFFFF5E92),
                gradientEnd = Color(0xFFA855F7),
                accent = Color(0xFFBEF264)
            )
        )
        uploadedHostStory?.let { add(it) }
        addAll(
            listOf(
                HostStory(
                    user = users.first { it.id == "1" },
                    moments = listOf(
                        HostStoryMoment(
                            vibeTitle = "Late-night confessions",
                            caption = "Aaj lo-fi songs aur deep talk mood me hoon."
                        )
                    ),
                    gradientStart = Accent1,
                    gradientEnd = Accent2,
                    accent = Accent3
                ),
                HostStory(
                    user = users.first { it.id == "3" },
                    moments = listOf(
                        HostStoryMoment(
                            vibeTitle = "Soft music room",
                            caption = "Old Bollywood aur soft vibe wali call line open hai."
                        )
                    ),
                    gradientStart = Color(0xFFFF7A59),
                    gradientEnd = Accent2,
                    accent = Color(0xFFFFC857)
                ),
                HostStory(
                    user = users.first { it.id == "6" },
                    moments = listOf(
                        HostStoryMoment(
                            vibeTitle = "Coffee chat energy",
                            caption = "Chill mood, music aur random life talk ke liye free hoon."
                        )
                    ),
                    gradientStart = Accent2,
                    gradientEnd = Color(0xFF6E2D8F),
                    accent = Accent1
                ),
                HostStory(
                    user = users.first { it.id == "14" },
                    moments = listOf(
                        HostStoryMoment(
                            vibeTitle = "After-hours vibe",
                            caption = "Night owl logon ke liye clean private convo slot open hai."
                        )
                    ),
                    gradientStart = Color(0xFFFC466B),
                    gradientEnd = Color(0xFF3F5EFB),
                    accent = Accent3
                ),
                HostStory(
                    user = users.first { it.id == "16" },
                    moments = listOf(
                        HostStoryMoment(
                            vibeTitle = "Calm coffee check-in",
                            caption = "No rush, bas relaxed call aur cozy company mood."
                        )
                    ),
                    gradientStart = Color(0xFFEF476F),
                    gradientEnd = Color(0xFF8338EC),
                    accent = Color(0xFFF8F272)
                ),
                HostStory(
                    user = users.first { it.id == "25" },
                    moments = listOf(
                        HostStoryMoment(
                            vibeTitle = "Night owl roll call",
                            caption = "Late-night host line live hai, fun aur music dono milenge."
                        )
                    ),
                    gradientStart = Color(0xFF00C2FF),
                    gradientEnd = Accent2,
                    accent = Color(0xFFFF7A59)
                )
            )
        )
    }
}

@Composable
private fun MainScaffold(
    sessionManager: SessionManager,
    homeUsers: SnapshotStateList<User>,
    isHomeRefreshing: Boolean,
    activeCall: ActiveCallSession?,
    isInPictureInPictureMode: Boolean,
    startInProfileSetup: Boolean,
    onStartCall: (ActiveCallSession) -> Unit,
    onEndCall: () -> Unit,
    onRefreshHome: () -> Unit,
    onUserReturnedOnline: (String) -> Unit,
    onEnterPictureInPicture: () -> Unit,
    onExitApp: () -> Unit,
    onSwitchToHostMode: () -> Unit,
    onProfileSetupCompleted: () -> Unit,
    onSensitiveContentVisible: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as FrndzzApp }
    val authRepository = remember(app) { app.authRepository }
    val scope = rememberCoroutineScope()
    val coinsState = LocalCoins.current
    var pendingProfileSetup by rememberSaveable { mutableStateOf(startInProfileSetup) }
    var tab by rememberSaveable { mutableStateOf(if (startInProfileSetup) 3 else 0) }
    var showWallet by rememberSaveable { mutableStateOf(false) }
    var walletStartScreen by rememberSaveable { mutableStateOf("wallet") }
    var hideBottomBar by rememberSaveable { mutableStateOf(startInProfileSetup) }
    val walletTransactions = remember {
        mutableStateListOf<WalletTransactionEntry>().apply {
            addAll(defaultWalletTransactions())
        }
    }
    val blockedThreadIds = remember { mutableStateListOf<String>() }
    val callHistoryList = remember {
        mutableStateListOf<CallHistory>().apply {
            addAll(buildInitialCallHistory())
        }
    }
    val chatThreads = remember {
        mutableStateListOf<ChatThread>().apply {
            add(buildSupportThread())
        }
    }
    val unreadChatCount = chatThreads
        .filter { it.id !in blockedThreadIds }
        .sumOf { it.unreadCount.coerceAtLeast(0) }
    val shouldBlockScreenshots = activeCall != null || (tab == 0 && !showWallet)
    val blockedUsers = blockedThreadIds.map { blockedId ->
        val latestCall = callHistoryList
            .filter { it.userId == blockedId }
            .maxByOrNull { callEndedAt(it) }
        val homeUser = homeUsers.firstOrNull { it.id == blockedId }
        BlockedProfileUser(
            id = blockedId,
            name = homeUser?.displayLabel() ?: latestCall?.name ?: blockedId,
            subtitle = when {
                latestCall != null -> if (latestCall.isVideo) {
                    "Blocked after video call"
                } else {
                    "Blocked after audio call"
                }

                homeUser != null -> homeUser.language
                else -> "Chat hidden from inbox"
            }
        )
    }.sortedBy { it.name.lowercase(Locale.getDefault()) }

    fun blockUserEverywhere(userId: String) {
        if (userId !in blockedThreadIds) {
            blockedThreadIds.add(userId)
        }
        callHistoryList.removeAll { it.userId == userId }
        chatThreads.removeAll { !it.isPinned && it.id == userId }
        syncUnlockedChatThreads(chatThreads, callHistoryList, blockedThreadIds.toSet())
    }

    fun recordCompletedCallTransaction(
        call: ActiveCallSession,
        durationSeconds: Long,
        totalCoins: Int
    ) {
        if (totalCoins <= 0) return
        val ratePerMinute = call.ratePerMinute.takeIf { it > 0 }
            ?: resolveCallRatePerMinute(call.userId, call.isVideo)
        val remainingCharge = (totalCoins - ratePerMinute.coerceAtLeast(0)).coerceAtLeast(0)
        if (remainingCharge > 0) {
            coinsState.value = (coinsState.value - remainingCharge).coerceAtLeast(0)
        }

        val transaction = WalletTransactionEntry(
            icon = if (call.isVideo) null else Icons.Default.Call,
            iconRes = if (call.isVideo) R.drawable.ic_video_call_modern else null,
            title = call.displayLabel(),
            detail = "${if (call.isVideo) "Video" else "Audio"} call completed",
            amountText = "-$totalCoins",
            positive = false,
            kind = if (call.isVideo) WalletTransactionKind.VIDEO_CALL else WalletTransactionKind.AUDIO_CALL,
            timestampMillis = System.currentTimeMillis(),
            coinsDelta = -totalCoins,
            counterpartyName = call.displayLabel(),
            durationSeconds = durationSeconds,
            syncStatus = WalletSyncStatus.PENDING
        )
        walletTransactions.add(0, transaction)

        scope.launch {
            val synced = authRepository.recordWalletTransaction(
                accessToken = sessionManager.getAccessToken(),
                request = transaction.toRecordWalletTransactionRequest()
            ).map { it.recorded }.getOrDefault(false)
            val transactionIndex = walletTransactions.indexOfFirst { item ->
                item.timestampMillis == transaction.timestampMillis &&
                    item.kind == transaction.kind &&
                    item.coinsDelta == transaction.coinsDelta &&
                    item.counterpartyName == transaction.counterpartyName
            }
            if (transactionIndex >= 0) {
                walletTransactions[transactionIndex] = transaction.copy(
                    syncStatus = if (synced) WalletSyncStatus.SYNCED else WalletSyncStatus.FAILED,
                    detail = if (synced) {
                        transaction.detail
                    } else {
                        "${transaction.detail}. Backend ledger sync failed."
                    }
                )
            }
        }
    }

    fun recordGiftWalletTransaction(
        call: ActiveCallSession,
        giftHistory: String?,
        giftCoins: Int
    ) {
        if (giftCoins <= 0 || giftHistory.isNullOrBlank()) return
        val transaction = WalletTransactionEntry(
            icon = Icons.Default.CardGiftcard,
            title = call.displayLabel(),
            detail = "Gift sent during ${if (call.isVideo) "video" else "audio"} call",
            amountText = "-$giftCoins",
            positive = false,
            kind = WalletTransactionKind.GIFT,
            timestampMillis = System.currentTimeMillis(),
            coinsDelta = -giftCoins,
            counterpartyName = call.displayLabel(),
            giftName = giftHistory.removePrefix("Gifts:").trim(),
            giftCount = 1,
            giftContextLabel = if (call.isVideo) "Video call" else "Audio call",
            syncStatus = WalletSyncStatus.PENDING
        )
        walletTransactions.add(0, transaction)

        scope.launch {
            val synced = authRepository.recordWalletTransaction(
                accessToken = sessionManager.getAccessToken(),
                request = transaction.toRecordWalletTransactionRequest()
            ).map { it.recorded }.getOrDefault(false)
            val transactionIndex = walletTransactions.indexOfFirst { item ->
                item.timestampMillis == transaction.timestampMillis &&
                    item.kind == transaction.kind &&
                    item.coinsDelta == transaction.coinsDelta &&
                    item.counterpartyName == transaction.counterpartyName
            }
            if (transactionIndex >= 0) {
                walletTransactions[transactionIndex] = transaction.copy(
                    syncStatus = if (synced) WalletSyncStatus.SYNCED else WalletSyncStatus.FAILED,
                    detail = if (synced) {
                        transaction.detail
                    } else {
                        "${transaction.detail}. Backend ledger sync failed."
                    }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        syncUnlockedChatThreads(chatThreads, callHistoryList, blockedThreadIds.toSet())
    }

    LaunchedEffect(Unit) {
        authRepository.getWalletSummary(sessionManager.getAccessToken())
            .onSuccess { summary ->
                coinsState.value = summary.balanceCoins.coerceAtLeast(0)
                val remoteTransactions = summary.transactions.map { it.toWalletTransactionEntry() }
                if (remoteTransactions.isNotEmpty()) {
                    walletTransactions.clear()
                    walletTransactions.addAll(remoteTransactions)
                }
            }
    }

    DisposableEffect(shouldBlockScreenshots) {
        onSensitiveContentVisible(shouldBlockScreenshots)
        onDispose {
            if (shouldBlockScreenshots) {
                onSensitiveContentVisible(false)
            }
        }
    }

    BackHandler {
        when {
            activeCall != null -> onEnterPictureInPicture()
            showWallet -> {
                showWallet = false
                hideBottomBar = false
            }
            pendingProfileSetup -> onExitApp()
            hideBottomBar -> hideBottomBar = false
            tab != 0 -> tab = 0
            else -> onExitApp()
        }
    }

    Scaffold(
        containerColor = AppBg,
        bottomBar = {
            if (!hideBottomBar && activeCall == null) {
                NavigationBar(
                    containerColor = AppBg,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(84.dp)
                ) {
                    CustomBottomItem(tab == 0, Icons.Default.Home, "Home") { tab = 0 }
                    CustomBottomItem(tab == 1, Icons.Default.History, "Logs") { tab = 1 }
                    CustomBottomItem(
                        selected = tab == 2,
                        icon = Icons.Default.ChatBubbleOutline,
                        label = "Chat",
                        badgeCount = unreadChatCount
                    ) { tab = 2 }
                    CustomBottomItem(tab == 3, Icons.Default.Person, "Profile") { tab = 3 }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBg)
                .padding(padding)
        ) {
            when (tab) {
                0 -> HomeScreen(
                    users = homeUsers.filter { it.id !in blockedThreadIds },
                    isRefreshing = isHomeRefreshing,
                    onWalletClick = {
                        walletStartScreen = "wallet"
                        showWallet = true
                        hideBottomBar = true
                    },
                    onRefreshHosts = onRefreshHome,
                    onStartCall = onStartCall
                )
                1 -> CallHistoryScreen(
                    historyList = callHistoryList,
                    blockedUserIds = blockedThreadIds.toSet(),
                    sessionManager = sessionManager,
                    authRepository = authRepository,
                    onStartCall = onStartCall,
                    onUserBlocked = { userId ->
                        blockUserEverywhere(userId)
                    }
                )
                2 -> ChatScreen(
                    chatThreads = chatThreads,
                    blockedThreadIds = blockedThreadIds,
                    sessionManager = sessionManager,
                    authRepository = authRepository,
                    onStartCall = onStartCall,
                    onUserBlocked = { userId ->
                        blockUserEverywhere(userId)
                    },
                    onInnerNavigate = { hideBottomBar = true },
                    onInnerBack = { hideBottomBar = false }
                )
                3 -> ProfileScreen(
                    sessionManager = sessionManager,
                    blockedUsers = blockedUsers,
                    forceEditProfileOnLaunch = pendingProfileSetup,
                    onUnblockUser = { userId ->
                        blockedThreadIds.remove(userId)
                        syncUnlockedChatThreads(chatThreads, callHistoryList, blockedThreadIds.toSet())
                        scope.launch {
                            val accessToken = sessionManager.getAccessToken().trim()
                            if (accessToken.isNotBlank()) {
                                authRepository.unblockUser(
                                    accessToken = accessToken,
                                    request = UnblockUserRequest(blockedUserId = userId)
                                )
                            }
                        }
                        toast(context, "User unblocked")
                    },
                    onOpenWallet = {
                        walletStartScreen = "wallet"
                        showWallet = true
                        hideBottomBar = true
                    },
                    onOpenTransactions = {
                        walletStartScreen = "transactions"
                        showWallet = true
                        hideBottomBar = true
                    },
                    onInnerNavigate = { hideBottomBar = true },
                    onInnerBack = {
                        if (!pendingProfileSetup) {
                            hideBottomBar = false
                        }
                    },
                    onLanguageAppliedToHome = {
                        hideBottomBar = false
                        tab = 0
                    },
                    onSwitchToListenerMode = onSwitchToHostMode,
                    onProfileUpdated = {
                        if (pendingProfileSetup) {
                            tab = 0
                        }
                        hideBottomBar = false
                        pendingProfileSetup = false
                        onProfileSetupCompleted()
                    },
                    onLogout = onLogout
                )
            }

            if (showWallet) {
                Surface(color = AppBg, modifier = Modifier.fillMaxSize()) {
                    WalletScreen(
                        walletTransactions = walletTransactions,
                        initialScreen = walletStartScreen,
                        onTransactionRecorded = { transaction ->
                            authRepository.recordWalletTransaction(
                                accessToken = sessionManager.getAccessToken(),
                                request = transaction.toRecordWalletTransactionRequest()
                            ).map { it.recorded }.getOrDefault(false)
                        },
                        onBack = {
                            showWallet = false
                            hideBottomBar = false
                            walletStartScreen = "wallet"
                        }
                    )
                }
            }

            if (activeCall != null && !isInPictureInPictureMode) {
                Surface(color = AppBg, modifier = Modifier.fillMaxSize()) {
                    LiveCallSurface(
                        activeCall = activeCall,
                        onEndCall = { durationSeconds, giftHistory, giftCoins ->
                            val callCoins = estimateCallCoins(
                                durationSeconds = durationSeconds,
                                ratePerMinute = activeCall.ratePerMinute.takeIf { it > 0 }
                                    ?: resolveCallRatePerMinute(activeCall.userId, activeCall.isVideo)
                            )
                            recordCompletedCallTransaction(activeCall, durationSeconds, callCoins)
                            recordGiftWalletTransaction(activeCall, giftHistory, giftCoins)
                            val finishedCall = CallHistory(
                                userId = activeCall.userId,
                                name = activeCall.displayLabel(),
                                startedAtMillis = activeCall.startedAtMillis,
                                durationSeconds = durationSeconds,
                                isVideo = activeCall.isVideo,
                                giftHistory = giftHistory
                            )
                            callHistoryList.add(0, finishedCall)
                            syncUnlockedChatThreads(chatThreads, callHistoryList, blockedThreadIds.toSet())
                            val chatUnlocked = durationSeconds >= ChatUnlockMinDurationSeconds
                        if (chatUnlocked) {
                            toast(context, "${activeCall.displayLabel()} ka chat 7 days ke liye unlock ho gaya.")
                        } else {
                            toast(context, "Chat unlock ke liye ${activeCall.displayLabel()} se minimum 5 min call complete karni hogi.")
                        }
                            onUserReturnedOnline(activeCall.userId)
                            onEndCall()
                        },
                        onReportAndBlock = { reason, durationSeconds, giftHistory, giftCoins ->
                            val callCoins = estimateCallCoins(
                                durationSeconds = durationSeconds,
                                ratePerMinute = activeCall.ratePerMinute.takeIf { it > 0 }
                                    ?: resolveCallRatePerMinute(activeCall.userId, activeCall.isVideo)
                            )
                            recordCompletedCallTransaction(activeCall, durationSeconds, callCoins)
                            recordGiftWalletTransaction(activeCall, giftHistory, giftCoins)
                            val finishedCall = CallHistory(
                                userId = activeCall.userId,
                                name = activeCall.displayLabel(),
                                startedAtMillis = activeCall.startedAtMillis,
                                durationSeconds = durationSeconds,
                                isVideo = activeCall.isVideo,
                                giftHistory = giftHistory
                            )
                            callHistoryList.add(0, finishedCall)
                            blockUserEverywhere(activeCall.userId)
                            scope.launch {
                                val accessToken = sessionManager.getAccessToken().trim()
                                if (accessToken.isNotBlank()) {
                                    authRepository.reportUser(
                                        accessToken = accessToken,
                                        request = ReportUserRequest(
                                            reportedUserId = activeCall.userId,
                                            reason = reason.toReportReasonCode(),
                                            context = if (activeCall.isVideo) "call" else "call",
                                            note = reason,
                                            block = true
                                        )
                                    )
                                }
                            }
                            toast(context, "${activeCall.displayLabel()} reported and blocked.")
                            onUserReturnedOnline(activeCall.userId)
                            onEndCall()
                        },
                        onEnterPictureInPicture = onEnterPictureInPicture
                    )
                }
            }

        }
    }
}

@Composable
private fun CallEndSummaryOverlay(
    summary: CallEndSummary,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.58f))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = CardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            if (summary.reportedAndBlocked) {
                                DangerRed.copy(alpha = 0.18f)
                            } else {
                                Accent3.copy(alpha = 0.16f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (summary.reportedAndBlocked) Icons.Default.Block else Icons.Default.Check,
                        contentDescription = null,
                        tint = if (summary.reportedAndBlocked) DangerRed else Accent3,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Text(
                    text = if (summary.reportedAndBlocked) "Call ended and blocked" else "Call summary",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = summary.name,
                    color = TextSubtle,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CallEndSummaryRow("Call type", if (summary.isVideo) "Video call" else "Audio call")
                    CallEndSummaryRow("Duration", formatCallDuration(summary.durationSeconds))
                    CallEndSummaryRow("Coins spent", "${summary.coinsSpent} coins est.")
                    CallEndSummaryRow("Host earned", "${summary.hostEarnedEstimate} coins est.")
                    summary.giftHistory?.let { CallEndSummaryRow("Gifts", it.removePrefix("Gifts:").trim()) }
                    CallEndSummaryRow(
                        "Chat unlock",
                        if (summary.chatUnlocked) "Unlocked for 7 days" else "Needs 5 min call"
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() },
                    shape = RoundedCornerShape(18.dp),
                    color = Accent1
                ) {
                    Text(
                        text = "Done",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CallEndSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSubtle,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ChatScreen(
    chatThreads: SnapshotStateList<ChatThread>,
    blockedThreadIds: SnapshotStateList<String>,
    sessionManager: SessionManager,
    authRepository: AuthRepository,
    onStartCall: (ActiveCallSession) -> Unit,
    onUserBlocked: (String) -> Unit,
    onInnerNavigate: () -> Unit,
    onInnerBack: () -> Unit
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as FrndzzApp }
    val authRepository = remember(app) { app.authRepository }
    val sessionManager = remember(app) { app.sessionManager }
    val coinsState = LocalCoins.current
    val scope = rememberCoroutineScope()
    val previewUsers = remember { defaultHomeUsers().associateBy { it.id } }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedThreadId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedIndex = chatThreads.indexOfFirst { it.id == selectedThreadId }

    fun buildSupportHistory(messages: List<ChatMessage>, limit: Int = 8): List<SupportChatMessage> {
        return messages
            .filterNot { it.id.startsWith("typing-") }
            .filter { it.text.isNotBlank() }
            .takeLast(limit)
            .map { item ->
                SupportChatMessage(
                    role = if (item.fromUser) "user" else "assistant",
                    content = sanitizeChatMessage(item.text, allowSensitive = true)
                )
            }
    }

    fun queueAutoReply(threadId: String) {
        scope.launch {
            delay(800L)
            val repliedAt = System.currentTimeMillis()
            val reply = "Seen, main thodi der me yahin reply karti hoon."
            val refreshedIndex = chatThreads.indexOfFirst { it.id == threadId }
            if (refreshedIndex >= 0) {
                val refreshed = chatThreads[refreshedIndex]
                val seenMessages = refreshed.messages.map { item ->
                    if (item.fromUser && item.deliveryStatus != ChatDeliveryStatus.SEEN) {
                        item.copy(
                            deliveryStatus = ChatDeliveryStatus.SEEN,
                            seenAtMillis = repliedAt
                        )
                    } else {
                        item
                    }
                }
                chatThreads[refreshedIndex] = refreshed.copy(
                    subtitle = reply,
                    lastSeenAtMillis = repliedAt,
                    messages = seenMessages + ChatMessage(
                        id = "${refreshed.id}-${System.currentTimeMillis()}-reply",
                        text = reply,
                        fromUser = false,
                        timestampMillis = repliedAt
                    )
                )
            }
        }
    }

    fun queueSupportAiReply(threadId: String, prompt: String) {
        scope.launch {
            val placeholderId = "typing-${System.currentTimeMillis()}"
            val placeholderAt = System.currentTimeMillis()
            val currentIndex = chatThreads.indexOfFirst { it.id == threadId }
            if (currentIndex < 0) return@launch

            val sourceMessages = chatThreads[currentIndex].messages
            chatThreads[currentIndex] = chatThreads[currentIndex].copy(
                messages = chatThreads[currentIndex].messages + ChatMessage(
                    id = placeholderId,
                    text = "Typing...",
                    fromUser = false,
                    timestampMillis = placeholderAt
                )
            )

            val accessToken = sessionManager.getAccessToken().trim()
            if (accessToken.isBlank()) {
                val refreshedIndex = chatThreads.indexOfFirst { it.id == threadId }
                if (refreshedIndex >= 0) {
                    val refreshed = chatThreads[refreshedIndex]
                    chatThreads[refreshedIndex] = refreshed.copy(
                        messages = refreshed.messages.filterNot { it.id == placeholderId } + ChatMessage(
                            id = "support-fallback-${System.currentTimeMillis()}",
                            text = "Please login again, phir support chat try karo.",
                            fromUser = false,
                            timestampMillis = System.currentTimeMillis()
                        )
                    )
                }
                return@launch
            }

            val response = authRepository.sendSupportChat(
                accessToken = accessToken,
                request = SupportChatRequest(
                    message = prompt,
                    history = buildSupportHistory(sourceMessages.dropLast(1))
                )
            ).getOrNull()

            val assistantText = response?.answer?.ifBlank {
                "Mujhe abhi clear reply nahi mila. Thoda aur detail me batao."
            } ?: "Mujhe abhi reply nahi mila. Thoda baad me try karo."

            val refreshedIndex = chatThreads.indexOfFirst { it.id == threadId }
            if (refreshedIndex >= 0) {
                val refreshed = chatThreads[refreshedIndex]
                chatThreads[refreshedIndex] = refreshed.copy(
                    messages = refreshed.messages.filterNot { it.id == placeholderId } + ChatMessage(
                        id = "support-reply-${System.currentTimeMillis()}",
                        text = assistantText,
                        fromUser = false,
                        timestampMillis = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    BackHandler(enabled = selectedThreadId != null) {
        selectedThreadId = null
        onInnerBack()
    }

    if (selectedIndex >= 0) {
        ChatConversationScreen(
            thread = chatThreads[selectedIndex],
            previewUser = previewUsers[chatThreads[selectedIndex].id],
            onBack = {
                selectedThreadId = null
                onInnerBack()
            },
            onStartCall = onStartCall,
            onDeleteChat = {
                val current = chatThreads.getOrNull(selectedIndex) ?: return@ChatConversationScreen
                if (current.id == "support") {
                    chatThreads[selectedIndex] = buildSupportThread().copy(unreadCount = 0)
                } else {
                    chatThreads.removeAt(selectedIndex)
                }
                selectedThreadId = null
                onInnerBack()
            },
            onBlockChat = { reason ->
                val current = chatThreads.getOrNull(selectedIndex) ?: return@ChatConversationScreen
                if (current.id != "support") {
                    onUserBlocked(current.id)
                    selectedThreadId = null
                    onInnerBack()
                    scope.launch {
                        val accessToken = sessionManager.getAccessToken().trim()
                        if (accessToken.isNotBlank()) {
                            authRepository.reportUser(
                                accessToken = accessToken,
                                request = ReportUserRequest(
                                    reportedUserId = current.id,
                                    reason = reason.toReportReasonCode(),
                                    context = "chat",
                                    note = reason,
                                    block = true
                                )
                            )
                        }
                    }
                }
            },
            onSendMessage = { text ->
                val current = chatThreads[selectedIndex]
                if (current.id != "support") {
                    if (coinsState.value < ChatMessageCostCoins) {
                        toast(context, "Not enough coins")
                        return@ChatConversationScreen false
                    }
                    coinsState.value -= ChatMessageCostCoins
                }
                val sentAt = System.currentTimeMillis()
                val safeText = sanitizeChatMessage(text, allowSensitive = current.id == "support")
                val updatedMessages = current.messages + ChatMessage(
                    id = "${current.id}-${System.currentTimeMillis()}",
                    text = safeText,
                    fromUser = true,
                    timestampMillis = sentAt,
                    deliveryStatus = ChatDeliveryStatus.SENT
                )
                chatThreads[selectedIndex] = current.copy(
                    subtitle = safeText,
                    unreadCount = 0,
                    lastSeenAtMillis = current.lastSeenAtMillis,
                    messages = updatedMessages
                )
                if (current.id == "support") {
                    queueSupportAiReply(current.id, safeText)
                } else {
                    queueAutoReply(current.id)
                }
                true
            },
            onSendSupportImage = { imageUri ->
                val current = chatThreads[selectedIndex]
                if (current.id != "support") return@ChatConversationScreen
                val sentAt = System.currentTimeMillis()
                chatThreads[selectedIndex] = current.copy(
                    subtitle = "Photo shared",
                    unreadCount = 0,
                    lastSeenAtMillis = current.lastSeenAtMillis,
                    messages = current.messages + ChatMessage(
                        id = "${current.id}-$sentAt-image",
                        text = "",
                        fromUser = true,
                        timestampMillis = sentAt,
                        deliveryStatus = ChatDeliveryStatus.SENT,
                        imageUri = imageUri
                    )
                )
                queueSupportAiReply(
                    current.id,
                    "The user shared a screenshot. Please help with the issue shown and ask for any missing text details if needed."
                )
            },
            onSendSupportVoiceNote = { voiceNoteUri, durationSeconds ->
                val current = chatThreads[selectedIndex]
                if (current.id != "support") return@ChatConversationScreen
                val sentAt = System.currentTimeMillis()
                chatThreads[selectedIndex] = current.copy(
                    subtitle = "Voice message",
                    unreadCount = 0,
                    lastSeenAtMillis = current.lastSeenAtMillis,
                    messages = current.messages + ChatMessage(
                        id = "${current.id}-$sentAt-voice",
                        text = "",
                        fromUser = true,
                        timestampMillis = sentAt,
                        deliveryStatus = ChatDeliveryStatus.SENT,
                        voiceNoteDurationSeconds = durationSeconds,
                        voiceNoteUri = voiceNoteUri
                    )
                )
                queueSupportAiReply(
                    current.id,
                    "The user shared a voice note. Ask for a short text summary if needed and help with the app issue."
                )
            }
        )
    } else {
        val orderedThreads = chatThreads
            .filter { it.isPinned || (it.unlockedUntilMillis ?: Long.MAX_VALUE) > System.currentTimeMillis() }
            .sortedWith(
                compareByDescending<ChatThread> { it.isPinned }
                    .thenByDescending { it.messages.lastOrNull()?.timestampMillis ?: it.lastSeenAtMillis ?: 0L }
            )
        val filteredThreads = orderedThreads.filter { thread ->
            searchQuery.isBlank() ||
                thread.title.contains(searchQuery, ignoreCase = true) ||
                thread.subtitle.contains(searchQuery, ignoreCase = true)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(CardBgMuted, AppBg, AppBg)))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                AppSectionHeader(
                    title = "Chat",
                    subtitle = "Your conversations stay here",
                    showWalletChip = false
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(999.dp),
                    placeholder = { Text("Search chats", color = TextSubtle.copy(alpha = 0.82f)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSubtle)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Accent2
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (filteredThreads.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardBg)
                            .padding(horizontal = 18.dp, vertical = 22.dp)
                    ) {
                        Column {
                            Text(
                                text = "No chats found",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Support chat top par rahegi. Baaki chats 5 minute ya usse zyada call ke baad unlock hoti hain.",
                                color = TextSubtle,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        itemsIndexed(
                            items = filteredThreads,
                            key = { index, thread -> "${thread.id}-$index" }
                        ) { _, thread ->
                            ChatThreadCard(
                                thread = thread,
                                previewUser = previewUsers[thread.id],
                                onClick = {
                                    val index = chatThreads.indexOfFirst { it.id == thread.id }
                                    if (index >= 0) {
                                        chatThreads[index] = chatThreads[index].copy(unreadCount = 0)
                                    }
                                    selectedThreadId = thread.id
                                    onInnerNavigate()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatThreadCard(
    thread: ChatThread,
    previewUser: User?,
    onClick: () -> Unit
) {
    val isSupportThread = thread.id == "support"
    val lastMessageAt = thread.messages.lastOrNull()?.timestampMillis
    val previewText = sanitizeChatMessage(thread.subtitle, allowSensitive = isSupportThread)
    var showAvatarPreview by rememberSaveable(thread.id) { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSupportThread) {
            SupportChatAvatar(size = 52.dp)
        } else {
            ContactChatAvatar(
                name = thread.title,
                size = 52.dp,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    showAvatarPreview = true
                }
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = if (isSupportThread) Modifier else Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isSupportThread) 4.dp else 6.dp)
            ) {
                Text(
                    thread.title,
                    modifier = if (isSupportThread) Modifier.widthIn(max = 170.dp) else Modifier.weight(1f),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isSupportThread) 14.sp else 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isSupportThread) {
                    SupportVerifiedBadge(size = 14.dp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                previewText,
                color = TextSubtle,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!isSupportThread && lastMessageAt != null) {
                Text(
                    text = formatListTime(lastMessageAt),
                    color = TextSubtle.copy(alpha = 0.86f),
                    fontSize = 10.sp
                )
            }
            if (!isSupportThread && thread.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Accent2),
                    contentAlignment = Alignment.Center
                ) {
                    Text(thread.unreadCount.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAvatarPreview && previewUser != null) {
        HostAvatarPreviewDialog(
            user = previewUser,
            onDismiss = { showAvatarPreview = false }
        )
    }
}

@Composable
private fun ChatConversationScreen(
    thread: ChatThread,
    previewUser: User?,
    onBack: () -> Unit,
    onStartCall: (ActiveCallSession) -> Unit,
    onDeleteChat: () -> Unit,
    onBlockChat: (String) -> Unit,
    onSendMessage: (String) -> Boolean,
    onSendSupportImage: (String) -> Unit,
    onSendSupportVoiceNote: (String, Int) -> Unit
) {
    val context = LocalContext.current
    val isSupportThread = thread.id == "support"
    var message by rememberSaveable(thread.id) { mutableStateOf("") }
    var showMoreMenu by rememberSaveable(thread.id) { mutableStateOf(false) }
    var showBlockReasonDialog by rememberSaveable(thread.id) { mutableStateOf(false) }
    var showAvatarPreview by rememberSaveable(thread.id) { mutableStateOf(false) }
    var pendingVoiceNote by remember(thread.id) { mutableStateOf<PendingVoiceNoteDraft?>(null) }
    var isRecordingVoiceNote by remember(thread.id) { mutableStateOf(false) }
    var recordingStartedAtMillis by remember(thread.id) { mutableStateOf(0L) }
    var recordingDurationSeconds by remember(thread.id) { mutableIntStateOf(0) }
    val messageListState = rememberLazyListState()
    val supportVoiceRecorder = remember(context.applicationContext) {
        SupportVoiceNoteRecorder(context.applicationContext)
    }
    val galleryPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.toString()?.let(onSendSupportImage)
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            toast(context, "Press and hold mic to record")
        } else {
            toast(context, "Microphone permission required for Frndzzz AI voice note.")
        }
    }
    val lastOutgoingMessageId = remember(thread.messages) {
        thread.messages.lastOrNull { it.fromUser }?.id
    }
    val lastSeenLabel = if (thread.isPinned) {
        "Frndzzz AI"
    } else {
        formatLastSeen(thread.lastSeenAtMillis ?: thread.messages.lastOrNull()?.timestampMillis)
    }

    LaunchedEffect(thread.id) {
        if (thread.messages.isNotEmpty()) {
            messageListState.scrollToItem(thread.messages.lastIndex)
        }
    }

    LaunchedEffect(thread.messages.size) {
        if (thread.messages.isNotEmpty()) {
            messageListState.animateScrollToItem(thread.messages.lastIndex)
        }
    }

    LaunchedEffect(isRecordingVoiceNote, recordingStartedAtMillis) {
        while (isRecordingVoiceNote && isActive) {
            recordingDurationSeconds = ((System.currentTimeMillis() - recordingStartedAtMillis) / 1000L).toInt()
            delay(150L)
        }
    }

    DisposableEffect(thread.id) {
        onDispose {
            supportVoiceRecorder.release()
            pendingVoiceNote?.let { deleteLocalUriIfPossible(it.uri) }
        }
    }

    val startSupportVoiceRecording = {
        if (!isSupportThread || isRecordingVoiceNote) {
            false
        } else if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            false
        } else {
            pendingVoiceNote?.let { deleteLocalUriIfPossible(it.uri) }
            pendingVoiceNote = null
            if (supportVoiceRecorder.start()) {
                recordingStartedAtMillis = System.currentTimeMillis()
                recordingDurationSeconds = 0
                isRecordingVoiceNote = true
                true
            } else {
                toast(context, "Couldn't start voice note")
                false
            }
        }
    }

    val finishSupportVoiceRecording = {
        if (!isRecordingVoiceNote) {
            Unit
        } else {
            isRecordingVoiceNote = false
            val draft = supportVoiceRecorder.stop()
            if (draft != null) {
                pendingVoiceNote?.let { deleteLocalUriIfPossible(it.uri) }
                pendingVoiceNote = draft
                recordingDurationSeconds = draft.durationSeconds
            } else {
                recordingDurationSeconds = 0
                toast(context, "Voice note couldn't be saved")
            }
        }
    }
    val canSend = message.isNotBlank() || (isSupportThread && pendingVoiceNote != null)
    val composerBottomPadding = if (isSupportThread && (isRecordingVoiceNote || pendingVoiceNote != null)) 146.dp else 86.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IPhoneBackButton(onBack = onBack)
                if (thread.id == "support") {
                    SupportChatAvatar(size = 36.dp)
                } else {
                    ContactChatAvatar(
                        name = thread.title,
                        size = 36.dp,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            showAvatarPreview = true
                        }
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = if (thread.isPinned) Modifier else Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (thread.isPinned) 4.dp else 6.dp)
                    ) {
                        Text(
                            text = thread.title,
                            modifier = if (thread.isPinned) Modifier.widthIn(max = 176.dp) else Modifier.weight(1f),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (thread.isPinned) 14.sp else 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (thread.isPinned) {
                            SupportVerifiedBadge(size = 15.dp)
                        }
                    }
                    Text(
                        lastSeenLabel,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!thread.isPinned) {
                    InstagramChatHeaderAction(
                        iconPainter = painterResource(id = R.drawable.ic_audio_call_modern),
                        contentDescription = "Audio call"
                    ) {
                        onStartCall(
                            ActiveCallSession(
                                userId = thread.id,
                                name = thread.title,
                                isVideo = false
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    InstagramChatHeaderAction(
                        iconPainter = painterResource(id = R.drawable.ic_video_call_modern),
                        contentDescription = "Video call"
                    ) {
                        onStartCall(
                            ActiveCallSession(
                                userId = thread.id,
                                name = thread.title,
                                isVideo = true
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                if (!isSupportThread) {
                    Box {
                        InstagramChatHeaderAction(
                            icon = Icons.Default.MoreHoriz,
                            contentDescription = "More"
                        ) {
                            showMoreMenu = true
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Report & Block") },
                                onClick = {
                                    showMoreMenu = false
                                    showBlockReasonDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete chat") },
                                onClick = {
                                    showMoreMenu = false
                                    toast(context, "Chat deleted.")
                                    onDeleteChat()
                                }
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.06f))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .imePadding()
            ) {
                LazyColumn(
                    state = messageListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = composerBottomPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom)
                ) {
                    itemsIndexed(
                        items = thread.messages,
                        key = { index, item -> "${thread.id}-${item.id}-$index" }
                    ) { _, item ->
                        ChatBubble(
                            message = item,
                            allowSensitive = isSupportThread,
                            showStatus = item.id == lastOutgoingMessageId
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 46.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(Color(0xFF17171C))
                                    .border(0.8.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 7.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isSupportThread && (isRecordingVoiceNote || pendingVoiceNote != null)) {
                                        SupportVoiceComposerStrip(
                                            draft = pendingVoiceNote,
                                            isRecording = isRecordingVoiceNote,
                                            recordingDurationSeconds = recordingDurationSeconds,
                                            onCancelDraft = {
                                                pendingVoiceNote?.let { deleteLocalUriIfPossible(it.uri) }
                                                pendingVoiceNote = null
                                                recordingDurationSeconds = 0
                                            }
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 32.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BasicTextField(
                                            value = message,
                                            onValueChange = { message = it },
                                            modifier = Modifier.weight(1f),
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                lineHeight = 20.sp
                                            ),
                                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                                            decorationBox = { innerTextField ->
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 2.dp)
                                                ) {
                                                    if (message.isBlank()) {
                                                        Text(
                                                            text = if (pendingVoiceNote != null) "Tap send to share voice note" else "Message...",
                                                            color = Color.White.copy(alpha = 0.38f),
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                    innerTextField()
                                                }
                                            }
                                        )
                                        if (isSupportThread) {
                                            ChatComposerAction(
                                                icon = Icons.Default.Image,
                                                contentDescription = "Share screenshot"
                                            ) {
                                                galleryPickerLauncher.launch("image/*")
                                            }
                                            HoldToRecordChatAction(
                                                isRecording = isRecordingVoiceNote,
                                                onStartRecording = startSupportVoiceRecording,
                                                onStopRecording = finishSupportVoiceRecording
                                            )
                                        }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (canSend) {
                                            Brush.horizontalGradient(listOf(Accent2, Accent1))
                                        } else {
                                            Brush.horizontalGradient(listOf(Color(0xFF1B1B21), Color(0xFF1B1B21)))
                                        }
                                    )
                                    .clickable {
                                        if (message.isNotBlank()) {
                                            if (!isSupportThread && isSensitiveChatContent(message)) {
                                                toast(context, "Sensitive details sirf Frndzzz AI thread me share karo.")
                                            }
                                            if (onSendMessage(message.trim())) {
                                                message = ""
                                            }
                                        } else if (isSupportThread && pendingVoiceNote != null) {
                                            onSendSupportVoiceNote(
                                                pendingVoiceNote!!.uri,
                                                pendingVoiceNote!!.durationSeconds
                                            )
                                            pendingVoiceNote = null
                                            recordingDurationSeconds = 0
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Send message",
                                    tint = if (canSend) Color.White else Color.White.copy(alpha = 0.42f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAvatarPreview && previewUser != null) {
            HostAvatarPreviewDialog(
                user = previewUser,
                onDismiss = { showAvatarPreview = false }
            )
        }

        if (showBlockReasonDialog) {
            BlockReasonDialog(
                onReasonSelected = { reason ->
                    showBlockReasonDialog = false
                    toast(context, "${thread.title} reported and blocked.")
                    onBlockChat(reason)
                },
                onDismiss = { showBlockReasonDialog = false }
            )
        }
    }

@Composable
private fun InstagramChatHeaderAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) = InstagramChatHeaderAction(
    iconPainter = rememberVectorPainter(icon),
    contentDescription = contentDescription,
    onClick = onClick
)

@Composable
private fun InstagramChatHeaderAction(
    iconPainter: Painter,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = contentDescription,
            tint = Color.White.copy(alpha = 0.92f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ChatComposerAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun HoldToRecordChatAction(
    isRecording: Boolean,
    onStartRecording: () -> Boolean,
    onStopRecording: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isRecording) Accent1.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.06f))
            .pointerInput(isRecording) {
                detectTapGestures(
                    onPress = {
                        if (!onStartRecording()) return@detectTapGestures
                        try {
                            tryAwaitRelease()
                        } finally {
                            onStopRecording()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Hold to record",
            tint = Color.White.copy(alpha = if (isRecording) 1f else 0.8f),
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun SupportVoiceComposerStrip(
    draft: PendingVoiceNoteDraft?,
    isRecording: Boolean,
    recordingDurationSeconds: Int,
    onCancelDraft: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(0.8.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        if (isRecording) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Accent1)
                )
                Text(
                    text = "Recording... release to stop",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatChatVoiceDuration(recordingDurationSeconds),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else if (draft != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ChatVoiceNoteContent(
                    audioUri = draft.uri,
                    durationSeconds = draft.durationSeconds,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onCancelDraft() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Discard voice note",
                        tint = Color.White.copy(alpha = 0.78f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }

}

@Composable
private fun ChatHeaderAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SupportVerifiedBadge(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF1D9BF0)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = "Verified",
            tint = Color.White,
            modifier = Modifier.size(size * 0.62f)
        )
    }
}

@Composable
private fun SupportChatAvatar(
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color(0xFF0B060C))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Accent1.copy(alpha = 0.92f),
                            Accent2.copy(alpha = 0.92f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size * 0.12f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_brand_mark),
                    contentDescription = "Frndzzz logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ContactChatAvatar(
    name: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFFFD1D1D), Color(0xFFFCAF45), Accent2)))
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Accent2, CardBgMuted)))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(2).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    allowSensitive: Boolean = false,
    showStatus: Boolean = false
) {
    val displayText = sanitizeChatMessage(message.text, allowSensitive = allowSensitive)
    val statusText = when {
        !message.fromUser -> null
        message.deliveryStatus == ChatDeliveryStatus.SEEN -> formatSeenRelativeTime(message.seenAtMillis)
        else -> "Sent"
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.fromUser) Alignment.End else Alignment.Start
    ) {
        val bubbleShape = RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = if (message.fromUser) 18.dp else 6.dp,
            bottomEnd = if (message.fromUser) 6.dp else 18.dp
        )
        Box(
            modifier = Modifier
                .widthIn(max = 264.dp)
                .clip(bubbleShape)
                .background(
                    if (message.fromUser) {
                        Brush.horizontalGradient(listOf(Accent2, Accent1))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFF26262B), Color(0xFF26262B)))
                    }
                )
        ) {
            when {
                message.imageUri != null -> {
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .aspectRatio(3f / 4f)
                    ) {
                        AndroidView(
                            factory = { imageContext ->
                                ImageView(imageContext).apply {
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            update = { imageView ->
                                imageView.setImageURI(Uri.parse(message.imageUri))
                            }
                        )
                    }
                }

                message.voiceNoteDurationSeconds != null && message.voiceNoteUri != null -> {
                    ChatVoiceNoteContent(
                        audioUri = message.voiceNoteUri,
                        durationSeconds = message.voiceNoteDurationSeconds,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    )
                }

                else -> {
                    Text(
                        text = displayText,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
        if (message.fromUser && showStatus && statusText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                color = Color.White.copy(alpha = 0.54f),
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        } else if (!message.fromUser) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCompactRelativeTime(message.timestampMillis),
                color = Color.White.copy(alpha = 0.42f),
                fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

private fun formatChatVoiceDuration(durationSeconds: Int): String {
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

@Composable
private fun ChatVoiceNoteContent(
    audioUri: String,
    durationSeconds: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember(audioUri) { mutableStateOf(false) }
    var progress by remember(audioUri) { mutableStateOf(0f) }
    var mediaPlayer by remember(audioUri) { mutableStateOf<MediaPlayer?>(null) }

    fun resolvePlayer(): MediaPlayer? {
        mediaPlayer?.let { return it }
        return runCatching {
            MediaPlayer().apply {
                setDataSource(context, Uri.parse(audioUri))
                prepare()
                setOnCompletionListener {
                    isPlaying = false
                    progress = 0f
                    seekTo(0)
                }
            }
        }.getOrNull()?.also { mediaPlayer = it }
    }

    DisposableEffect(audioUri) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    LaunchedEffect(isPlaying, mediaPlayer) {
        while (isPlaying && isActive) {
            val player = mediaPlayer ?: break
            val safeDurationMs = player.duration.takeIf { it > 0 } ?: (durationSeconds.coerceAtLeast(1) * 1000)
            progress = (player.currentPosition.toFloat() / safeDurationMs.toFloat()).coerceIn(0f, 1f)
            if (!player.isPlaying) {
                isPlaying = false
            }
            delay(120L)
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
                .clickable {
                    val player = resolvePlayer()
                    if (player == null) {
                        toast(context, "Voice note unavailable")
                    } else if (player.isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else {
                        if (player.currentPosition >= player.duration - 150) {
                            player.seekTo(0)
                            progress = 0f
                        }
                        player.start()
                        isPlaying = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause voice note" else "Play voice note",
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceAtLeast(0.08f))
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.96f))
            )
        }
        Text(
            text = formatChatVoiceDuration(durationSeconds),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun deleteLocalUriIfPossible(uriString: String) {
    runCatching {
        val uri = Uri.parse(uriString)
        if (uri.scheme == "file") {
            File(requireNotNull(uri.path)).delete()
        }
    }
}

private class SupportVoiceNoteRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAtMillis: Long = 0L

    fun start(): Boolean {
        release()
        val file = File(context.cacheDir, "support-voice-${System.currentTimeMillis()}.m4a")
        return runCatching {
            MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96_000)
                setAudioSamplingRate(44_100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
        }.map { mediaRecorder ->
            recorder = mediaRecorder
            outputFile = file
            startedAtMillis = System.currentTimeMillis()
            true
        }.getOrElse {
            file.delete()
            false
        }
    }

    fun stop(): PendingVoiceNoteDraft? {
        val activeRecorder = recorder ?: return null
        val file = outputFile
        val durationSeconds = ((System.currentTimeMillis() - startedAtMillis) / 1000L).toInt().coerceAtLeast(1)
        return try {
            activeRecorder.stop()
            if (file == null || !file.exists()) {
                null
            } else {
                PendingVoiceNoteDraft(
                    uri = Uri.fromFile(file).toString(),
                    durationSeconds = durationSeconds
                )
            }
        } catch (_: Exception) {
            file?.delete()
            null
        } finally {
            release()
        }
    }

    fun release() {
        runCatching { recorder?.reset() }
        runCatching { recorder?.release() }
        recorder = null
        outputFile = null
        startedAtMillis = 0L
    }
}

@Composable
private fun RowScope.CustomBottomItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else TextSubtle,
                modifier = Modifier.fillMaxSize()
            )
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 5.dp, y = (-4).dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3040))
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else TextSubtle
        )
    }
}

@Composable
private fun HomeScreen(
    users: List<User>,
    isRefreshing: Boolean,
    onWalletClick: () -> Unit,
    onRefreshHosts: () -> Unit,
    onStartCall: (ActiveCallSession) -> Unit
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as FrndzzApp }
    val authRepository = remember(app) { app.authRepository }
    val sessionManager = remember(app) { app.sessionManager }
    val coinsState = LocalCoins.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var storyFeedClock by remember { mutableStateOf(System.currentTimeMillis()) }
    var activeStoryIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val viewedStoryIds = remember { mutableStateListOf<String>() }
    var showRandomCallTypeDialog by rememberSaveable { mutableStateOf(false) }
    var isRandomDiceRolling by rememberSaveable { mutableStateOf(false) }
    var rollingMatchName by rememberSaveable { mutableStateOf("") }
    var usedRandomHostIds by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var randomDiceFace by rememberSaveable { mutableIntStateOf(5) }
    var selectedDiscoveryTopic by rememberSaveable { mutableStateOf("All") }
    var selectedDiscoveryLanguage by rememberSaveable {
        mutableStateOf(UserPrefs.getLanguage(context).ifBlank { "All" })
    }
    val myCoins = coinsState.value
    var discoveryTopics by remember {
        mutableStateOf(listOf("All") + UserPrefs.getTopicOptions(context))
    }
    var discoveryLanguages by remember {
        mutableStateOf(UserPrefs.getConfiguredLanguages(context, includeAll = true))
    }
    val topicFilteredUsers = if (selectedDiscoveryTopic == "All") {
        users.toList()
    } else {
        users.filter { userMatchesDiscoveryTopic(it, selectedDiscoveryTopic) }
    }
    val prioritizedUsers = if (selectedDiscoveryLanguage == "All") {
        topicFilteredUsers
    } else {
        topicFilteredUsers.filter { it.language == selectedDiscoveryLanguage }
    }
    val onlineUsers = prioritizedUsers.filter { it.presence == UserPresence.ONLINE }
    val busyUsers = prioritizedUsers.filter { it.presence == UserPresence.BUSY }
    val offlineUsers = prioritizedUsers.filter { it.presence == UserPresence.OFFLINE }
    val groupedUsers = onlineUsers + busyUsers + offlineUsers
    val randomDiceUsers = onlineUsers

    fun startRandomDiceCall(isVideo: Boolean) {
        if (isRandomDiceRolling) return
        randomDiceFace = (1..6).random()
        showRandomCallTypeDialog = false
        isRandomDiceRolling = true
        rollingMatchName = "Finding your Dostt..."

        scope.launch {
            val remoteUser = sessionManager.getAccessToken().trim().takeIf { it.isNotBlank() }?.let { accessToken ->
                authRepository.requestRandomMatch(
                    accessToken = accessToken,
                    request = RandomMatchRequest(
                        topicTags = selectedDiscoveryTopic.takeIf { it != "All" }?.let(::listOf).orEmpty(),
                        languages = selectedDiscoveryLanguage.takeIf { it != "All" }?.let(::listOf).orEmpty()
                    )
                ).getOrNull()?.host?.toHomeUser()
            }

            val selectedUser = remoteUser ?: run {
                if (randomDiceUsers.isEmpty()) {
                    isRandomDiceRolling = false
                    rollingMatchName = ""
                    toast(context, "Abhi koi live host online nahi hai.")
                    return@launch
                }
                val freshPool = randomDiceUsers.filter { it.id !in usedRandomHostIds }
                val selectionPool = freshPool.ifEmpty { randomDiceUsers }
                selectionPool.shuffled().first().also { user ->
                    usedRandomHostIds = if (freshPool.isEmpty()) {
                        listOf(user.id)
                    } else {
                        usedRandomHostIds + user.id
                    }
                }
            }

            rollingMatchName = selectedUser.displayLabel()
            delay(900)
            onStartCall(
                ActiveCallSession(
                    userId = selectedUser.id,
                    name = selectedUser.name,
                    publicId = selectedUser.publicId,
                    isVideo = isVideo,
                    ratePerMinute = if (isVideo) {
                        VideoCallRateCoinsPerMinute
                    } else {
                        AudioCallRateCoinsPerMinute
                    }
                )
            )
            isRandomDiceRolling = false
            rollingMatchName = ""
        }
    }

    LaunchedEffect(isRandomDiceRolling) {
        if (!isRandomDiceRolling) return@LaunchedEffect
        while (isRandomDiceRolling) {
            randomDiceFace = (1..6).random()
            delay(90)
        }
    }

    LaunchedEffect(Unit) {
        authRepository.getTopicTags().onSuccess { response ->
            UserPrefs.saveTopicOptions(context, response.tags)
            discoveryTopics = listOf("All") + UserPrefs.getTopicOptions(context)
            if (selectedDiscoveryTopic !in discoveryTopics) {
                selectedDiscoveryTopic = "All"
            }
        }
        authRepository.getSupportedLanguages().onSuccess { response ->
            UserPrefs.saveLanguageOptions(context, response.languages)
            discoveryLanguages = UserPrefs.getConfiguredLanguages(context, includeAll = true)
            if (selectedDiscoveryLanguage !in discoveryLanguages.map { it.key }) {
                selectedDiscoveryLanguage = "All"
            }
        }
        while (isActive) {
            delay(60_000)
            storyFeedClock = System.currentTimeMillis()
        }
    }

    val hostStories = buildHostStories(context, users, storyFeedClock)
    val markStoryViewed: (String) -> Unit = { storyId ->
        if (storyId !in viewedStoryIds) {
            viewedStoryIds.add(storyId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(CardBgMuted, AppBg, AppBg)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                HomeHeader(myCoins = myCoins, onWalletClick = onWalletClick)
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefreshHosts,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { FrndzzSlider(animateSlides = !listState.isScrollInProgress) }
                    if (groupedUsers.isEmpty()) {
                        item {
                            DiscoveryEmptyStateCard(
                                showAllHosts = true,
                                onShowAllHosts = {},
                                onRetry = onRefreshHosts
                            )
                        }
                    }
                    item {
                        HostStoriesSection(
                            stories = hostStories,
                            viewedStoryIds = viewedStoryIds.toSet(),
                            onStoryClick = { story ->
                                markStoryViewed(story.user.id)
                                activeStoryIndex = hostStories.indexOf(story).takeIf { it >= 0 }
                            }
                        )
                    }
                    if (groupedUsers.isNotEmpty()) {
                        itemsIndexed(
                            items = groupedUsers,
                            key = { index, user -> "${user.id}-$index" }
                        ) { _, user ->
                            UserCard(
                                user = user,
                                onStartCall = onStartCall
                            )
                        }
                    }
                }
            }
        }

        activeStoryIndex?.let { startIndex ->
            HostStoryViewer(
                stories = hostStories,
                initialStoryIndex = startIndex,
                onStartCall = onStartCall,
                onStoryViewed = markStoryViewed,
                onClose = { activeStoryIndex = null }
            )
        }
    }
}

@Composable
private fun DiscoveryEmptyStateCard(
    showAllHosts: Boolean,
    onShowAllHosts: () -> Unit,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Accent2.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = if (showAllHosts) "No hosts match this filter" else "No live host for this filter",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (showAllHosts) {
                    "Try another topic/language or refresh the host list."
                } else {
                    "Try Show all, change filters, or refresh to check who came online."
                },
                color = TextSubtle,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!showAllHosts) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onShowAllHosts() },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "Show all",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onRetry() },
                    shape = RoundedCornerShape(16.dp),
                    color = Accent1
                ) {
                    Text(
                        text = "Refresh",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoveryTopicChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.12f)
        )
    ) {
        Text(
            text = label,
            color = if (selected) AppBg else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun HomeHeader(myCoins: Int, onWalletClick: () -> Unit) {
    AppSectionHeader(
        title = "FRNDZZZ",
        subtitle = "Find someone you vibe with",
        myCoins = myCoins,
        onWalletClick = onWalletClick
    )
}

@Composable
private fun RandomDiceFloatingButton(
    modifier: Modifier = Modifier,
    diceFace: Int,
    availableCount: Int,
    onClick: () -> Unit
) {
    val isEnabled = availableCount > 0
    val idleTilt by animateFloatAsState(
        targetValue = -10f,
        animationSpec = tween(durationMillis = 260),
        label = "randomDiceIdleTilt"
    )
    val density = LocalDensity.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopEnd
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .graphicsLayer {
                    rotationZ = idleTilt
                    rotationX = 10f
                    rotationY = -12f
                    cameraDistance = with(density) { 18.dp.toPx() } * 12f
                    shadowElevation = with(density) { 18.dp.toPx() }
                }
                .clickable(enabled = isEnabled) { onClick() }
        ) {
            RandomDiceVisual(
                face = diceFace,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = availableCount.toString(),
            modifier = Modifier
                .offset(x = (-2).dp, y = 2.dp)
                .clip(CircleShape)
                .background(if (availableCount > 0) SuccessGreen else WarningAmber)
                .padding(horizontal = 7.dp, vertical = 3.dp),
            color = Color(0xFF04110B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RandomDiceRollOverlay(
    currentMatchName: String,
    diceFace: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "randomDiceOverlay")
    val rollingRotationZ by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(animation = tween(620, easing = LinearEasing)),
        label = "randomDiceOverlayRotationZ"
    )
    val rollingRotationX by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 378f,
        animationSpec = infiniteRepeatable(animation = tween(680, easing = LinearEasing)),
        label = "randomDiceOverlayRotationX"
    )
    val rollingRotationY by infiniteTransition.animateFloat(
        initialValue = -22f,
        targetValue = 338f,
        animationSpec = infiniteRepeatable(animation = tween(560, easing = LinearEasing)),
        label = "randomDiceOverlayRotationY"
    )
    val rollingScalePulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(animation = tween(340, easing = LinearEasing)),
        label = "randomDiceOverlayScale"
    )
    val entranceScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 260),
        label = "randomDiceOverlayEntrance"
    )
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(148.dp)
                    .graphicsLayer {
                        rotationZ = rollingRotationZ
                        rotationX = rollingRotationX
                        rotationY = rollingRotationY
                        scaleX = entranceScale * rollingScalePulse
                        scaleY = entranceScale * rollingScalePulse
                        cameraDistance = with(density) { 18.dp.toPx() } * 12f
                        shadowElevation = with(density) { 28.dp.toPx() }
                    }
            ) {
                RandomDiceVisual(
                    face = diceFace,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = "Finding a match...",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Connecting to $currentMatchName",
                color = TextSubtle,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun RandomDiceVisual(
    face: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val depth = size.minDimension * 0.18f
        val frontLeft = depth * 0.2f
        val frontTop = depth * 0.78f
        val frontRight = size.width - depth * 0.86f
        val frontBottom = size.height - depth * 0.18f
        val topLiftX = depth * 0.74f
        val topLiftY = depth * 0.6f
        val corner = CornerRadius(depth * 0.34f, depth * 0.34f)
        val frontSize = androidx.compose.ui.geometry.Size(
            frontRight - frontLeft,
            frontBottom - frontTop
        )

        drawRoundRect(
            color = Color.Black.copy(alpha = 0.16f),
            topLeft = Offset(frontLeft + depth * 0.34f, frontTop + depth * 0.42f),
            size = frontSize,
            cornerRadius = corner
        )

        val topPath = Path().apply {
            moveTo(frontLeft, frontTop)
            lineTo(frontLeft + topLiftX, frontTop - topLiftY)
            lineTo(frontRight + topLiftX, frontTop - topLiftY)
            lineTo(frontRight, frontTop)
            close()
        }
        drawPath(
            brush = Brush.linearGradient(
                listOf(Color(0xFFFFFFFF), Color(0xFFFFE7F0), Color(0xFFF9BED3)),
                start = Offset(frontLeft, frontTop),
                end = Offset(frontRight + topLiftX, frontTop - topLiftY)
            ),
            path = topPath
        )

        val sidePath = Path().apply {
            moveTo(frontRight, frontTop)
            lineTo(frontRight + topLiftX, frontTop - topLiftY)
            lineTo(frontRight + topLiftX, frontBottom - topLiftY)
            lineTo(frontRight, frontBottom)
            close()
        }
        drawPath(
            brush = Brush.linearGradient(
                listOf(Color(0xFFF4A9C4), Color(0xFFD77E9E), Color(0xFF9B4664)),
                start = Offset(frontRight, frontTop),
                end = Offset(frontRight + topLiftX, frontBottom)
            ),
            path = sidePath
        )

        drawRoundRect(
            brush = Brush.linearGradient(
                listOf(Color.White, Color(0xFFFFEEF5), Color(0xFFFFD6E4)),
                start = Offset(frontLeft, frontTop),
                end = Offset(frontRight, frontBottom)
            ),
            topLeft = Offset(frontLeft, frontTop),
            size = frontSize,
            cornerRadius = corner
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.82f),
            topLeft = Offset(frontLeft, frontTop),
            size = frontSize,
            cornerRadius = corner,
            style = Stroke(width = depth * 0.06f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.42f),
            radius = depth * 0.34f,
            center = Offset(frontLeft + depth * 0.78f, frontTop + depth * 0.78f)
        )

        val pipRadius = frontSize.minDimension * 0.08f
        val positions = when (face.coerceIn(1, 6)) {
            1 -> listOf(Offset(0.5f, 0.5f))
            2 -> listOf(Offset(0.24f, 0.24f), Offset(0.76f, 0.76f))
            3 -> listOf(Offset(0.24f, 0.24f), Offset(0.5f, 0.5f), Offset(0.76f, 0.76f))
            4 -> listOf(
                Offset(0.24f, 0.24f),
                Offset(0.76f, 0.24f),
                Offset(0.24f, 0.76f),
                Offset(0.76f, 0.76f)
            )
            5 -> listOf(
                Offset(0.24f, 0.24f),
                Offset(0.76f, 0.24f),
                Offset(0.5f, 0.5f),
                Offset(0.24f, 0.76f),
                Offset(0.76f, 0.76f)
            )
            else -> listOf(
                Offset(0.24f, 0.24f),
                Offset(0.76f, 0.24f),
                Offset(0.24f, 0.5f),
                Offset(0.76f, 0.5f),
                Offset(0.24f, 0.76f),
                Offset(0.76f, 0.76f)
            )
        }
        positions.forEach { pip ->
            drawCircle(
                color = Color(0xFF3E1022),
                radius = pipRadius,
                center = Offset(
                    frontLeft + frontSize.width * pip.x,
                    frontTop + frontSize.height * pip.y
                )
            )
        }
    }
}

@Composable
private fun RandomDiceCallTypeDialog(
    onDismiss: () -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(CardBg)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Choose call type",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Choose audio or video. Dice will connect you with a fresh live host.",
                    color = TextSubtle,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CallIconButton(
                        iconPainter = painterResource(id = R.drawable.ic_audio_call_modern),
                        coinsText = "$AudioCallRateCoinsPerMinute",
                        modifier = Modifier.weight(1f),
                        onClick = onAudioCall
                    )
                    CallIconButton(
                        iconPainter = painterResource(id = R.drawable.ic_video_call_modern),
                        coinsText = "$VideoCallRateCoinsPerMinute",
                        modifier = Modifier.weight(1f),
                        onClick = onVideoCall
                    )
                }
                Text(
                    text = "Cancel",
                    color = TextSubtle.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { onDismiss() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AppSectionHeader(
    title: String,
    subtitle: String,
    myCoins: Int = 0,
    onWalletClick: () -> Unit = {},
    showWalletChip: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderTitleBlock(
                title = title,
                subtitle = subtitle,
                modifier = Modifier.weight(1f)
            )

            if (showWalletChip) {
                WalletHeaderCoinsChip(
                    myCoins = myCoins,
                    onClick = onWalletClick
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun HeaderTitleBlock(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                color = TextSubtle.copy(alpha = 0.96f),
                fontSize = 13.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun WalletHeaderCoinsChip(
    myCoins: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "\uD83E\uDE99",
                fontSize = 13.sp
            )
            Text(
                text = myCoins.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun IPhoneBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.035f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onBack() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.arrow_back_ios_40),
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun FrndzzzBackHeader(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IPhoneBackButton(onBack = onBack)
            } else {
                Spacer(modifier = Modifier.width(36.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            HeaderTitleBlock(
                title = title,
                subtitle = subtitle,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun HostAvatarPreviewDialog(
    user: User,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.93f))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                ChatHeaderAction(
                    icon = Icons.Default.Close,
                    contentDescription = "Close preview",
                    onClick = onDismiss
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Accent1, Accent2, Accent3)))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(CardBgMuted, CardBg))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 72.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = user.displayLabel(),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${user.language} • ${user.interests.joinToString(" • ")}",
                    color = TextSubtle,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    onStartCall: (ActiveCallSession) -> Unit
) {
    var showAvatarPreview by rememberSaveable(user.id) { mutableStateOf(false) }
    val isAvailable = user.presence == UserPresence.ONLINE
    val statusLabel = when (user.presence) {
        UserPresence.ONLINE -> "Online"
        UserPresence.BUSY -> "Busy"
        UserPresence.OFFLINE -> "Offline"
    }
    val statusColor = when (user.presence) {
        UserPresence.ONLINE -> SuccessGreen
        UserPresence.BUSY -> WarningAmber
        UserPresence.OFFLINE -> TextSubtle
    }
    val statusBg = when (user.presence) {
        UserPresence.ONLINE -> SuccessGreen.copy(alpha = 0.16f)
        UserPresence.BUSY -> WarningAmber.copy(alpha = 0.16f)
        UserPresence.OFFLINE -> Color.Gray.copy(alpha = 0.16f)
    }
    val unavailableTitle = when (user.presence) {
        UserPresence.BUSY -> "Currently busy on call"
        UserPresence.OFFLINE -> "Currently offline"
        UserPresence.ONLINE -> ""
    }
    val unavailableSubtitle = when (user.presence) {
        UserPresence.BUSY -> ""
        UserPresence.OFFLINE -> ""
        UserPresence.ONLINE -> ""
    }
    val busyDurationText = user.busyForMinutes?.let { "$it min" } ?: "On call"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardBg)
            .border(1.dp, Accent2.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Accent1, Accent3)))
                    .clickable { showAvatarPreview = true },
                contentAlignment = Alignment.Center
            ) {
                Text(user.name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.displayLabel(), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("Language: ${user.language}", color = TextSubtle, fontSize = 12.sp)
            }
            Text(
                text = statusLabel,
                color = statusColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            user.interests.forEach { interest ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(CardBgMuted)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(interest, color = Color.White, fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        if (isAvailable) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CallIconButton(
                    iconPainter = painterResource(id = R.drawable.ic_audio_call_modern),
                    coinsText = "$AudioCallRateCoinsPerMinute",
                    modifier = Modifier.weight(1f)
                ) {
                    onStartCall(
                        ActiveCallSession(
                            userId = user.id,
                            name = user.name,
                            publicId = user.publicId,
                            isVideo = false,
                            ratePerMinute = AudioCallRateCoinsPerMinute
                        )
                    )
                }
                CallIconButton(
                    iconPainter = painterResource(id = R.drawable.ic_video_call_modern),
                    coinsText = "$VideoCallRateCoinsPerMinute",
                    modifier = Modifier.weight(1f)
                ) {
                    onStartCall(
                        ActiveCallSession(
                            userId = user.id,
                            name = user.name,
                            publicId = user.publicId,
                            isVideo = true,
                            ratePerMinute = VideoCallRateCoinsPerMinute
                        )
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (user.presence == UserPresence.BUSY) WarningAmber.copy(alpha = 0.1f) else CardBgMuted
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                if (user.presence == UserPresence.BUSY) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                unavailableTitle,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (unavailableSubtitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    unavailableSubtitle,
                                    color = TextSubtle,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Text(
                            busyDurationText,
                            color = WarningAmber,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(WarningAmber.copy(alpha = 0.14f))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            unavailableTitle,
                            color = TextSubtle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (unavailableSubtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                unavailableSubtitle,
                                color = TextSubtle.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAvatarPreview) {
        HostAvatarPreviewDialog(
            user = user,
            onDismiss = { showAvatarPreview = false }
        )
    }
}

@Composable
private fun FrndzzSlider(animateSlides: Boolean) {
    val slides = remember {
        listOf(
            HomePromoSlide(
                badge = "LIVE NOW",
                titleTop = "Find your",
                highlight = "REAL",
                titleBottom = "late-night vibe",
                subtitle = "Jump into audio-first conversations with people who match your mood instantly.",
                footer = "Audio rooms • smooth entry",
                gradientStart = Accent1,
                gradientEnd = Accent2,
                glow = Accent3
            ),
            HomePromoSlide(
                badge = "PRIVATE FEEL",
                titleTop = "Dark mode.",
                highlight = "BOLD",
                titleBottom = "zero awkward energy",
                subtitle = "Clean profiles, soft glow cards, and a social space that feels modern instead of messy.",
                footer = "Neon comfort • clean UX",
                gradientStart = Accent2,
                gradientEnd = Color(0xFF6E2D8F),
                glow = Accent1
            ),
            HomePromoSlide(
                badge = "FAST CONNECT",
                titleTop = "OTP se",
                highlight = "DIRECT",
                titleBottom = "vibe mode on",
                subtitle = "Quick onboarding, sharp visuals, and instant momentum the moment someone enters the app.",
                footer = "Fast flow • Gen-Z energy",
                gradientStart = Accent1,
                gradientEnd = Color(0xFF3C1038),
                glow = Accent3
            )
        )
    }
    var currentPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(animateSlides, slides.size) {
        if (!animateSlides) return@LaunchedEffect
        while (isActive) {
            delay(3000)
            currentPage = (currentPage + 1) % slides.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(164.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(CardBg)
            .clipToBounds()
    ) {
        Crossfade(
            targetState = currentPage,
            animationSpec = tween(durationMillis = 500),
            modifier = Modifier.fillMaxSize(),
            label = "homePromoCrossfade"
        ) { page ->
            val slide = slides[page]

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                slide.gradientStart,
                                slide.gradientEnd,
                                CardBg
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(slide.glow.copy(alpha = 0.16f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, slide.glow.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = slide.badge,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 70.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = slide.titleTop,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = slide.highlight,
                                color = slide.glow,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                            Text(
                                text = slide.titleBottom,
                                modifier = Modifier.weight(1f),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = slide.subtitle,
                            color = Color.White.copy(alpha = 0.82f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Box(
                            modifier = Modifier
                                .widthIn(min = 104.dp, max = 160.dp)
                                .heightIn(min = 30.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = 0.18f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = slide.footer,
                                color = Color.White.copy(alpha = 0.92f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slides.forEachIndexed { index, _ ->
                val isSelected = currentPage == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(listOf(Accent1, Accent2))
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.16f),
                                        Color.White.copy(alpha = 0.16f)
                                    )
                                )
                            }
                        )
                        .size(width = if (isSelected) 24.dp else 8.dp, height = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun HostStoriesSection(
    stories: List<HostStory>,
    viewedStoryIds: Set<String>,
    onStoryClick: (HostStory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Stories",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            itemsIndexed(
                items = stories,
                key = { index, story -> "${story.user.id}-$index" }
            ) { _, story ->
                HostStoryBubble(
                    story = story,
                    viewed = story.user.id in viewedStoryIds,
                    onClick = { onStoryClick(story) }
                )
            }
        }
    }
}

@Composable
private fun StoryAvatarArt(
    story: HostStory,
    size: Dp,
    initialsFontSize: Int,
    viewed: Boolean = false,
    showRing: Boolean = true,
    modifier: Modifier = Modifier
) {
    val ringColors = if (viewed) {
        listOf(
            Color.White.copy(alpha = 0.28f),
            Color.White.copy(alpha = 0.16f),
            Color.White.copy(alpha = 0.28f)
        )
    } else {
        listOf(
            Color(0xFFFFC75F),
            Accent1,
            Accent2,
            Color(0xFFFF7F50),
            Color(0xFFFFC75F)
        )
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (showRing) {
                    Modifier
                        .background(Brush.sweepGradient(ringColors))
                        .padding(2.4.dp)
                } else {
                    Modifier
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                        .padding(2.dp)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(AppBg)
                .border(1.dp, Color.White.copy(alpha = 0.07f), CircleShape)
                .padding(2.5.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                story.gradientStart.copy(alpha = 0.92f),
                                story.gradientEnd.copy(alpha = 0.78f)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.09f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (story.user.id == "support-story") {
                    SupportChatAvatar(
                        size = size * 0.82f
                    )
                } else {
                    Text(
                        text = story.user.name.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = initialsFontSize.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HostStoryBubble(
    story: HostStory,
    viewed: Boolean,
    onClick: () -> Unit
) {
    val storyInteraction = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .width(76.dp)
            .clickable(
                indication = null,
                interactionSource = storyInteraction
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StoryAvatarArt(
            story = story,
            size = 70.dp,
            initialsFontSize = 16,
            viewed = viewed
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = story.user.displayLabel(),
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HostStoryViewer(
    stories: List<HostStory>,
    initialStoryIndex: Int,
    onStartCall: (ActiveCallSession) -> Unit,
    onStoryViewed: (String) -> Unit,
    onClose: () -> Unit
) {
    if (stories.isEmpty()) return

    val initialPage = initialStoryIndex.coerceIn(0, stories.lastIndex)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { stories.size }
    )
    val storyMomentIndices = remember(stories, initialPage) {
        mutableStateMapOf<Int, Int>().apply {
            put(initialPage, 0)
        }
    }
    var isStoryPaused by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val storyProgress = remember { Animatable(0f) }

    fun momentIndexFor(page: Int): Int {
        val safePage = page.coerceIn(0, stories.lastIndex)
        val maxMomentIndex = stories[safePage].moments.lastIndex
        return storyMomentIndices[safePage]?.coerceIn(0, maxMomentIndex) ?: 0
    }

    fun setMomentIndex(page: Int, momentIndex: Int) {
        val safePage = page.coerceIn(0, stories.lastIndex)
        val maxMomentIndex = stories[safePage].moments.lastIndex
        storyMomentIndices[safePage] = momentIndex.coerceIn(0, maxMomentIndex)
    }

    fun goToPreviousStory() {
        if (pagerState.isScrollInProgress) return
        scope.launch {
            val page = pagerState.currentPage.coerceIn(0, stories.lastIndex)
            val currentMomentIndex = momentIndexFor(page)
            when {
                currentMomentIndex > 0 -> setMomentIndex(page, currentMomentIndex - 1)
                page > 0 -> {
                    val previousPage = page - 1
                    setMomentIndex(previousPage, stories[previousPage].moments.lastIndex)
                    pagerState.animateScrollToPage(previousPage)
                }
                else -> onClose()
            }
        }
    }

    fun goToNextStory() {
        if (pagerState.isScrollInProgress) return
        scope.launch {
            val page = pagerState.currentPage.coerceIn(0, stories.lastIndex)
            val currentMomentIndex = momentIndexFor(page)
            when {
                currentMomentIndex < stories[page].moments.lastIndex -> {
                    setMomentIndex(page, currentMomentIndex + 1)
                }
                page < stories.lastIndex -> {
                    val nextPage = page + 1
                    setMomentIndex(nextPage, 0)
                    pagerState.animateScrollToPage(nextPage)
                }
                else -> onClose()
            }
        }
    }

    val currentStoryIndex = pagerState.currentPage.coerceIn(0, stories.lastIndex)
    val currentStory = stories[currentStoryIndex]
    val currentMomentIndex = momentIndexFor(currentStoryIndex)
    val currentMoment = currentStory.moments[currentMomentIndex]

    LaunchedEffect(currentStoryIndex, currentMomentIndex, currentMoment.mediaUri, stories.size) {
        onStoryViewed(currentStory.user.id)
        storyProgress.snapTo(0f)
        val totalDuration = if (currentMoment.mediaType == HostStoryMediaType.VIDEO) 6000L else 3200L
        val stepDuration = 40L
        var elapsed = 0L
        while (isActive && elapsed < totalDuration) {
            if (!isStoryPaused && !pagerState.isScrollInProgress) {
                delay(stepDuration)
                elapsed += stepDuration
                storyProgress.snapTo((elapsed.toFloat() / totalDuration).coerceIn(0f, 1f))
            } else {
                delay(stepDuration)
            }
        }
        if (isActive) {
            goToNextStory()
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.97f))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize(),
                key = { page -> "${stories[page].user.id}-$page" }
            ) { page ->
                val pageStory = stories[page]
                val pageMomentIndex = momentIndexFor(page)
                val pageMoment = pageStory.moments[pageMomentIndex]
                val isCurrentPage = page == currentStoryIndex
                val pageProgress = if (isCurrentPage) storyProgress.value else 0f
                val isFrndzzzStory = pageStory.user.id == "support-story"

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    InstagramStoryBackdrop(
                        story = pageStory,
                        moment = pageMoment,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.32f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.64f)
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier.matchParentSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .pointerInput(page, pagerState.currentPage, pageMomentIndex) {
                                    detectTapGestures(
                                        onPress = {
                                            if (page != pagerState.currentPage || pagerState.isScrollInProgress) {
                                                return@detectTapGestures
                                            }
                                            val pressStartedAt = System.currentTimeMillis()
                                            isStoryPaused = true
                                            val released = tryAwaitRelease()
                                            isStoryPaused = false
                                            if (released && System.currentTimeMillis() - pressStartedAt < 180L) {
                                                goToPreviousStory()
                                            }
                                        }
                                    )
                                }
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .pointerInput(page, pagerState.currentPage, pageMomentIndex) {
                                    detectTapGestures(
                                        onPress = {
                                            if (page != pagerState.currentPage || pagerState.isScrollInProgress) {
                                                return@detectTapGestures
                                            }
                                            val pressStartedAt = System.currentTimeMillis()
                                            isStoryPaused = true
                                            val released = tryAwaitRelease()
                                            isStoryPaused = false
                                            if (released && System.currentTimeMillis() - pressStartedAt < 180L) {
                                                goToNextStory()
                                            }
                                        }
                                    )
                                }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            pageStory.moments.forEachIndexed { index, _ ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.5.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color.White.copy(alpha = 0.22f))
                                ) {
                                    val progressFraction = when {
                                        index < pageMomentIndex -> 1f
                                        index == pageMomentIndex -> pageProgress
                                        else -> 0f
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(Color.White.copy(alpha = 0.96f))
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StoryAvatarArt(
                                story = pageStory,
                                size = 34.dp,
                                initialsFontSize = 11,
                                showRing = false
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pageStory.user.displayLabel(),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            ChatHeaderAction(
                                icon = Icons.Default.Close,
                                contentDescription = "Close story",
                                onClick = onClose
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = pageMoment.vibeTitle,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = pageMoment.caption,
                                color = Color.White.copy(alpha = 0.84f),
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(end = 14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!isFrndzzzStory) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CallIconButton(
                                    iconPainter = painterResource(id = R.drawable.ic_audio_call_modern),
                                    coinsText = "$AudioCallRateCoinsPerMinute",
                                    modifier = Modifier.weight(1f)
                                ) {
                                    onStartCall(
                                        ActiveCallSession(
                                            userId = pageStory.user.id,
                                            name = pageStory.user.name,
                                            publicId = pageStory.user.publicId,
                                            isVideo = false
                                        )
                                    )
                                    onClose()
                                }
                                CallIconButton(
                                    iconPainter = painterResource(id = R.drawable.ic_video_call_modern),
                                    coinsText = "$VideoCallRateCoinsPerMinute",
                                    modifier = Modifier.weight(1f)
                                ) {
                                    onStartCall(
                                        ActiveCallSession(
                                            userId = pageStory.user.id,
                                            name = pageStory.user.name,
                                            publicId = pageStory.user.publicId,
                                            isVideo = true
                                        )
                                    )
                                    onClose()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstagramStoryBackdrop(
    story: HostStory,
    moment: HostStoryMoment,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    story.gradientStart.copy(alpha = 0.96f),
                    story.gradientEnd.copy(alpha = 0.84f),
                    AppBg
                )
            )
        )
    ) {
        if (!moment.mediaUri.isNullOrBlank() && moment.mediaType != null) {
            StoryMomentMedia(
                mediaUri = moment.mediaUri,
                mediaType = moment.mediaType,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.16f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.34f)
                            )
                        )
                    )
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 74.dp, y = (-34).dp)
                .size(236.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            story.accent.copy(alpha = 0.32f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-52).dp, y = 62.dp)
                .size(226.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            story.gradientStart.copy(alpha = 0.24f),
                            Color.Transparent
                        )
                    )
                )
        )
        if (moment.mediaUri.isNullOrBlank()) {
            StoryAvatarArt(
                story = story,
                size = 292.dp,
                initialsFontSize = 86,
                showRing = false,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-28).dp)
                    .alpha(0.24f)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-22).dp)
                    .size(width = 286.dp, height = 404.dp)
                    .clip(RoundedCornerShape(42.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(42.dp))
            )
        }
    }
}

@Composable
private fun StoryMomentMedia(
    mediaUri: String,
    mediaType: HostStoryMediaType,
    modifier: Modifier = Modifier
) {
    val parsedUri = remember(mediaUri) { Uri.parse(mediaUri) }
    if (mediaType == HostStoryMediaType.VIDEO) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                android.widget.VideoView(context).apply {
                    setOnPreparedListener { player ->
                        player.isLooping = true
                        start()
                    }
                }
            },
            update = { view ->
                if (view.tag != mediaUri) {
                    view.tag = mediaUri
                    view.setVideoURI(parsedUri)
                    view.start()
                }
            }
        )
    } else {
        val context = LocalContext.current
        val imageBitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = mediaUri) {
            value = loadStoryImageBitmap(context, mediaUri)
        }
        imageBitmapState.value?.let { imageBitmap ->
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        }
    }
}

private suspend fun loadStoryImageBitmap(
    context: Context,
    mediaUri: String
): ImageBitmap? = withContext(Dispatchers.IO) {
    if (mediaUri.isBlank()) {
        return@withContext null
    }
    runCatching {
        if (mediaUri.startsWith("http://") || mediaUri.startsWith("https://")) {
            URL(mediaUri).openStream().use { input ->
                BitmapFactory.decodeStream(input)?.asImageBitmap()
            }
        } else {
            context.contentResolver.openInputStream(Uri.parse(mediaUri))?.use { input ->
                BitmapFactory.decodeStream(input)?.asImageBitmap()
            }
        }
    }.getOrNull()
}

@Composable
private fun StoryMetaChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(999.dp))
            .padding(horizontal = 11.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.94f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class HomePromoSlide(
    val badge: String,
    val titleTop: String,
    val highlight: String,
    val titleBottom: String,
    val subtitle: String,
    val footer: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val glow: Color
)

private data class HostStory(
    val user: User,
    val moments: List<HostStoryMoment>,
    val gradientStart: Color,
    val gradientEnd: Color,
    val accent: Color
)

private data class HostStoryMoment(
    val vibeTitle: String,
    val caption: String,
    val mediaUri: String? = null,
    val mediaType: HostStoryMediaType? = null
)

private data class WalletOfferSlide(
    val emoji: String,
    val tag: String,
    val title: String,
    val subtitle: String,
    val accentStart: Color,
    val accentEnd: Color
)

private enum class WalletTransactionKind {
    AUDIO_CALL,
    VIDEO_CALL,
    CHAT,
    GIFT,
    PAYMENT
}

private enum class WalletTransactionStatus {
    COMPLETED,
    REJECTED
}

private enum class WalletSyncStatus {
    SYNCED,
    PENDING,
    FAILED
}

private enum class WalletTransactionFilter(val label: String) {
    ALL("All"),
    AUDIO_CALL("Audio call"),
    VIDEO_CALL("Video call"),
    CHAT("Chat"),
    GIFT("Gift"),
    PAYMENT_COMPLETED("Payment completed"),
    PAYMENT_REJECTED("Payment rejected")
}

private data class WalletTransactionEntry(
    val icon: ImageVector? = null,
    val iconRes: Int? = null,
    val title: String,
    val detail: String,
    val amountText: String,
    val positive: Boolean,
    val kind: WalletTransactionKind,
    val status: WalletTransactionStatus = WalletTransactionStatus.COMPLETED,
    val timestampMillis: Long,
    val coinsDelta: Int,
    val rechargeAmountRupees: Int = 0,
    val counterpartyName: String? = null,
    val durationSeconds: Long? = null,
    val messageCount: Int? = null,
    val giftName: String? = null,
    val giftCount: Int = 0,
    val giftContextLabel: String? = null,
    val syncStatus: WalletSyncStatus = WalletSyncStatus.SYNCED
)

private data class WalletMonthSummary(
    val monthStartMillis: Long,
    val label: String,
    val rechargeRupees: Int,
    val boughtCoins: Int
)

private fun defaultWalletTransactions(
    walletNow: Long = System.currentTimeMillis()
): List<WalletTransactionEntry> = listOf(
    WalletTransactionEntry(
        icon = Icons.Default.Check,
        title = "Coins purchase",
        detail = "Recharge received via payment gateway",
        amountText = "+250",
        positive = true,
        kind = WalletTransactionKind.PAYMENT,
        timestampMillis = walletNow - (48L * 60L * 1000L),
        coinsDelta = 250,
        rechargeAmountRupees = 75
    ),
    WalletTransactionEntry(
        icon = Icons.Default.Call,
        title = "Aisha",
        detail = "Call completed successfully",
        amountText = "-50",
        positive = false,
        kind = WalletTransactionKind.AUDIO_CALL,
        timestampMillis = walletNow - (2L * 60L * 60L * 1000L),
        coinsDelta = -50,
        rechargeAmountRupees = 0,
        counterpartyName = "Aisha",
        durationSeconds = 7L * 60L + 12L
    ),
    WalletTransactionEntry(
        icon = Icons.Default.ChatBubbleOutline,
        title = "Samera",
        detail = "Chat coin deduction recorded",
        amountText = "-18",
        positive = false,
        kind = WalletTransactionKind.CHAT,
        timestampMillis = walletNow - (26L * 60L * 60L * 1000L),
        coinsDelta = -18,
        rechargeAmountRupees = 0,
        counterpartyName = "Samera",
        messageCount = 18
    ),
    WalletTransactionEntry(
        iconRes = R.drawable.ic_video_call_modern,
        title = "Alisha",
        detail = "Face-to-face call completed",
        amountText = "-100",
        positive = false,
        kind = WalletTransactionKind.VIDEO_CALL,
        timestampMillis = walletNow - (2L * 24L * 60L * 60L * 1000L),
        coinsDelta = -100,
        rechargeAmountRupees = 0,
        counterpartyName = "Alisha",
        durationSeconds = 6L * 60L + 14L
    ),
    WalletTransactionEntry(
        icon = Icons.Default.CardGiftcard,
        title = "Aisha",
        detail = "Gift sent during live call",
        amountText = "-45",
        positive = false,
        kind = WalletTransactionKind.GIFT,
        timestampMillis = walletNow - (4L * 24L * 60L * 60L * 1000L),
        coinsDelta = -45,
        rechargeAmountRupees = 0,
        counterpartyName = "Aisha",
        giftName = "Rose",
        giftCount = 3,
        giftContextLabel = "Video call"
    ),
    WalletTransactionEntry(
        icon = Icons.Default.Close,
        title = "Payment failed",
        detail = "Gateway timeout on recharge attempt",
        amountText = "0",
        positive = false,
        kind = WalletTransactionKind.PAYMENT,
        status = WalletTransactionStatus.REJECTED,
        timestampMillis = walletNow - (6L * 24L * 60L * 60L * 1000L),
        coinsDelta = 0,
        rechargeAmountRupees = 31
    )
)

private fun RemoteWalletTransaction.toWalletTransactionEntry(): WalletTransactionEntry {
    val normalizedKind = kind.trim().lowercase(Locale.getDefault())
    val mappedKind = when (normalizedKind) {
        "audio_call" -> WalletTransactionKind.AUDIO_CALL
        "video_call" -> WalletTransactionKind.VIDEO_CALL
        "chat" -> WalletTransactionKind.CHAT
        "gift" -> WalletTransactionKind.GIFT
        else -> WalletTransactionKind.PAYMENT
    }
    return WalletTransactionEntry(
        icon = when (mappedKind) {
            WalletTransactionKind.AUDIO_CALL -> Icons.Default.Call
            WalletTransactionKind.CHAT -> Icons.Default.ChatBubbleOutline
            WalletTransactionKind.GIFT -> Icons.Default.CardGiftcard
            WalletTransactionKind.PAYMENT -> if (coinsDelta >= 0) Icons.Default.Check else Icons.Default.Close
            WalletTransactionKind.VIDEO_CALL -> null
        },
        iconRes = if (mappedKind == WalletTransactionKind.VIDEO_CALL) R.drawable.ic_video_call_modern else null,
        title = title.ifBlank { "Wallet update" },
        detail = detail,
        amountText = amountText.ifBlank { if (coinsDelta >= 0) "+$coinsDelta" else "$coinsDelta" },
        positive = coinsDelta > 0,
        kind = mappedKind,
        status = if (status.equals("rejected", ignoreCase = true)) {
            WalletTransactionStatus.REJECTED
        } else {
            WalletTransactionStatus.COMPLETED
        },
        timestampMillis = createdAt.toWalletTimestampMillis(),
        coinsDelta = coinsDelta,
        rechargeAmountRupees = rechargeAmountRupees,
        counterpartyName = counterpartyName,
        durationSeconds = durationSeconds,
        messageCount = messageCount,
        giftName = giftName,
        giftCount = giftCount,
        giftContextLabel = giftContextLabel,
        syncStatus = WalletSyncStatus.SYNCED
    )
}

private fun WalletTransactionEntry.toRecordWalletTransactionRequest(): RecordWalletTransactionRequest {
    return RecordWalletTransactionRequest(
        kind = when (kind) {
            WalletTransactionKind.AUDIO_CALL -> "audio_call"
            WalletTransactionKind.VIDEO_CALL -> "video_call"
            WalletTransactionKind.CHAT -> "chat"
            WalletTransactionKind.GIFT -> "gift"
            WalletTransactionKind.PAYMENT -> "payment"
        },
        title = title,
        detail = detail,
        amountText = amountText,
        coinsDelta = coinsDelta,
        rechargeAmountRupees = rechargeAmountRupees,
        status = when (status) {
            WalletTransactionStatus.COMPLETED -> "completed"
            WalletTransactionStatus.REJECTED -> "rejected"
        },
        counterpartyName = counterpartyName,
        durationSeconds = durationSeconds,
        messageCount = messageCount,
        giftName = giftName,
        giftCount = giftCount,
        giftContextLabel = giftContextLabel
    )
}

private fun String?.toWalletTimestampMillis(): Long {
    val rawValue = this?.trim().orEmpty()
    if (rawValue.isBlank()) return System.currentTimeMillis()
    val normalized = rawValue.replace(Regex("""\.(\d{3})\d+"""), ".$1")
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd HH:mm:ssXXX"
    )
    return patterns.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).parse(normalized)?.time
        }.getOrNull()
    } ?: System.currentTimeMillis()
}

@Composable
private fun WalletScreen(
    onBack: () -> Unit,
    walletTransactions: SnapshotStateList<WalletTransactionEntry>,
    initialScreen: String = "wallet",
    onTransactionRecorded: suspend (WalletTransactionEntry) -> Boolean = { true }
) {
    val coinsState = LocalCoins.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var screen by rememberSaveable(initialScreen) { mutableStateOf(initialScreen) }
    val openedDirectTransactions = initialScreen == "transactions"

    BackHandler {
        when (screen) {
            "wallet" -> onBack()
            "transactions" -> if (openedDirectTransactions) onBack() else screen = "wallet"
            else -> screen = "wallet"
        }
    }

    when (screen) {
        "wallet" -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(CardBgMuted, AppBg, AppBg)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    WalletHeader(
                        title = "Wallet",
                        subtitle = null,
                        onBack = onBack
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    WalletOfferSlider(animateSlides = !listState.isScrollInProgress)
                    Spacer(Modifier.height(16.dp))
                    WalletBalanceHero(
                        currentCoins = coinsState.value,
                        onViewTransactions = { screen = "transactions" }
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = "Choose a pack",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Recharge coins for calls, chats and gifts.",
                        color = TextSubtle,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 28.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = walletCoinPacks,
                            key = { index, pack -> "${pack.coins}-${pack.price}-$index" }
                        ) { _, pack ->
                            SwipePayCoinHistoryItem(pack = pack, onPay = {
                                screen = "processing"
                                scope.launch {
                                    delay(1800)
                                    coinsState.value += pack.coins
                                    val transaction = WalletTransactionEntry(
                                        icon = Icons.Default.Check,
                                        title = "Coins purchase",
                                        detail = "Recharge received via payment gateway",
                                        amountText = "+${pack.coins}",
                                        positive = true,
                                        kind = WalletTransactionKind.PAYMENT,
                                        timestampMillis = System.currentTimeMillis(),
                                        coinsDelta = pack.coins,
                                        rechargeAmountRupees = pack.price,
                                        syncStatus = WalletSyncStatus.PENDING
                                    )
                                    walletTransactions.add(0, transaction)
                                    val transactionIndex = walletTransactions.indexOf(transaction)
                                    val synced = onTransactionRecorded(transaction)
                                    if (transactionIndex >= 0) {
                                        walletTransactions[transactionIndex] = transaction.copy(
                                            syncStatus = if (synced) WalletSyncStatus.SYNCED else WalletSyncStatus.FAILED,
                                            detail = if (synced) {
                                                "Recharge received via payment gateway"
                                            } else {
                                                "Recharge added locally. Backend ledger sync failed."
                                            }
                                        )
                                    }
                                    screen = "wallet"
                            }
                            })
                        }
                    }
                }
            }
        }
        "transactions" -> WalletTransactionsScreen(
            transactions = walletTransactions,
            onBack = {
                if (openedDirectTransactions) onBack() else screen = "wallet"
            }
        )
        else -> PaymentProcessingScreen()
    }
}

@Composable
private fun WalletOfferSlider(animateSlides: Boolean) {
    val slides = remember {
        listOf(
            WalletOfferSlide(
                emoji = "🪙",
                tag = "Wallet boost",
                title = "Recharge and jump back in fast",
                subtitle = "Coins land instantly so calls, chats and gifts stay unlocked.",
                accentStart = Accent1.copy(alpha = 0.92f),
                accentEnd = Accent2.copy(alpha = 0.90f)
            ),
            WalletOfferSlide(
                emoji = "✨",
                tag = "Smooth top-up",
                title = "Swipe once and keep the vibe going",
                subtitle = "Clean checkout flow with a quick wallet refill feel.",
                accentStart = Accent2.copy(alpha = 0.88f),
                accentEnd = Accent3.copy(alpha = 0.86f)
            ),
            WalletOfferSlide(
                emoji = "🎁",
                tag = "Ready for gifting",
                title = "Stay ready for gifts and on-call recharge",
                subtitle = "Top up once and use coins anywhere across the app.",
                accentStart = Accent3.copy(alpha = 0.88f),
                accentEnd = Accent1.copy(alpha = 0.84f)
            )
        )
    }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { slides.size }
    )

    LaunchedEffect(animateSlides, slides.size) {
        if (!animateSlides) return@LaunchedEffect
        while (isActive) {
            delay(3200)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % slides.size)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(CardBg)
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(28.dp))
            .clipToBounds()
    ) {
        ShineOverlay(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.30f)
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val slide = slides[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                CardBg,
                                slide.accentStart.copy(alpha = 0.36f),
                                slide.accentEnd.copy(alpha = 0.22f)
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 8.dp)
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    slide.accentEnd.copy(alpha = 0.34f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = slide.tag,
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = slide.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = 92.dp)
                    )

                    Text(
                        text = slide.subtitle,
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = 98.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                        .size(72.dp)
                        .graphicsLayer {
                            rotationZ = -10f
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = slide.emoji,
                        fontSize = 34.sp
                    )
                }
            }
        }

    }
}

@Composable
private fun PaymentProcessingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "walletProcessing")
    val coinScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 850, easing = LinearEasing)),
        label = "walletProcessingScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(CardBgMuted, AppBg, AppBg))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(CardBg)
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(30.dp))
                .padding(horizontal = 28.dp, vertical = 26.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .graphicsLayer {
                        scaleX = coinScale
                        scaleY = coinScale
                    }
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Accent2.copy(alpha = 0.36f), Color.Transparent))),
                contentAlignment = Alignment.Center
            ) {
                Text("🪙", fontSize = 34.sp)
            }
            Text(
                text = "Connecting to payment gateway",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Please wait while we secure your payment and prepare your recharge.",
                color = TextSubtle,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Do not close this page",
                color = Accent2.copy(alpha = 0.94f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WalletHeader(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit
) {
    FrndzzzBackHeader(
        title = title,
        subtitle = subtitle,
        onBack = onBack
    )
}

@Composable
private fun WalletBalanceHero(
    currentCoins: Int,
    onViewTransactions: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Accent1.copy(alpha = 0.88f),
                        Accent2.copy(alpha = 0.78f),
                        Color(0xFF56125A)
                    )
                )
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        ShineOverlay(
            modifier = Modifier
                .matchParentSize()
                .alpha(0.14f)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(92.dp)
                .offset(x = 18.dp, y = (-14).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(88.dp)
                .offset(x = (-24).dp, y = 18.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Accent2.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(
                    text = "Current balance",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🪙",
                            fontSize = 20.sp
                        )
                    }
                    Text(
                        text = "$currentCoins",
                        color = Color.White,
                        fontSize = 29.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(y = 8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(999.dp))
                    .clickable { onViewTransactions() }
                    .padding(horizontal = 11.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "View transactions",
                    color = Color.White.copy(alpha = 0.94f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.90f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun WalletTransactionsScreen(
    transactions: List<WalletTransactionEntry>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val transactionSnapshot = transactions.toList()
    val filters = remember {
        listOf(
            WalletTransactionFilter.ALL,
            WalletTransactionFilter.AUDIO_CALL,
            WalletTransactionFilter.VIDEO_CALL,
            WalletTransactionFilter.CHAT,
            WalletTransactionFilter.GIFT,
            WalletTransactionFilter.PAYMENT_COMPLETED,
            WalletTransactionFilter.PAYMENT_REJECTED
        )
    }
    var selectedFilterName by rememberSaveable { mutableStateOf(WalletTransactionFilter.ALL.name) }
    var selectedDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedMonthStartMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var showMonthMenu by remember { mutableStateOf(false) }
    val selectedFilter = WalletTransactionFilter.valueOf(selectedFilterName)
    val monthSummaries = remember(transactionSnapshot) {
        buildWalletMonthSummaries(transactionSnapshot)
    }
    val lifetimeSpendRupees = remember(transactionSnapshot) {
        transactionSnapshot
            .filter {
                it.kind == WalletTransactionKind.PAYMENT &&
                    it.status == WalletTransactionStatus.COMPLETED
            }
            .sumOf { it.rechargeAmountRupees }
    }
    val lifetimeBoughtCoins = remember(transactionSnapshot) {
        transactionSnapshot
            .filter {
                it.kind == WalletTransactionKind.PAYMENT &&
                    it.status == WalletTransactionStatus.COMPLETED
            }
            .sumOf { maxOf(it.coinsDelta, 0) }
    }
    val currentMonthStart = remember { monthStartMillis(System.currentTimeMillis()) }
    val summaryMonthAnchor = selectedMonthStartMillis
        ?: currentMonthStart
    val selectedMonthSummary = monthSummaries.firstOrNull { it.monthStartMillis == summaryMonthAnchor }
        ?: WalletMonthSummary(
            monthStartMillis = summaryMonthAnchor,
            label = formatWalletMonthLabel(summaryMonthAnchor),
            rechargeRupees = 0,
            boughtCoins = 0
        )
    val filteredTransactions = remember(transactionSnapshot, selectedFilter, selectedDateMillis) {
        transactionSnapshot
            .filter { it.matches(selectedFilter) }
            .filter { item ->
                selectedDateMillis?.let { pickedDate -> isSameDay(item.timestampMillis, pickedDate) } ?: true
            }
            .sortedByDescending { it.timestampMillis }
    }
    val groupedTransactions = remember(filteredTransactions, selectedDateMillis) {
        filteredTransactions.groupBy {
            walletTransactionSectionLabel(
                timestampMillis = it.timestampMillis,
                selectedDateMillis = selectedDateMillis
            )
        }
    }
    val selectedDateLabel = selectedDateMillis?.let(::formatWalletSelectedDate)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(CardBgMuted, AppBg, AppBg)))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            WalletHeader(
                title = "Transactions",
                subtitle = "Your wallet activity stays here",
                onBack = onBack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (selectedFilter == WalletTransactionFilter.ALL) {
                WalletTotalSpendCard(
                    totalRupees = lifetimeSpendRupees,
                    boughtCoins = lifetimeBoughtCoins
                )

                WalletMonthlySpendCard(
                    monthLabel = selectedMonthSummary.label,
                    rechargeRupees = selectedMonthSummary.rechargeRupees,
                    boughtCoins = selectedMonthSummary.boughtCoins,
                    onClick = { showMonthMenu = true }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Filters",
                    color = TextSubtle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        WalletTransactionFilterChip(
                            label = filter.label,
                            selected = filter == selectedFilter,
                            onClick = { selectedFilterName = filter.name }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WalletDateActionChip(
                        label = selectedDateLabel ?: "Select date",
                        selected = selectedDateMillis != null,
                        trailingIcon = Icons.Default.KeyboardArrowDown,
                        onClick = {
                            val initialCalendar = Calendar.getInstance().apply {
                                timeInMillis = selectedDateMillis ?: System.currentTimeMillis()
                            }
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val pickedCalendar = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    selectedDateMillis = pickedCalendar.timeInMillis
                                },
                                initialCalendar.get(Calendar.YEAR),
                                initialCalendar.get(Calendar.MONTH),
                                initialCalendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    if (selectedDateMillis != null) {
                        WalletDateActionChip(
                            label = "Clear date",
                            selected = false,
                            onClick = { selectedDateMillis = null }
                        )
                    }
                }
            }
        }

        if (showMonthMenu) {
            WalletMonthPickerDialog(
                months = monthSummaries,
                selectedMonthStartMillis = selectedMonthSummary.monthStartMillis,
                onDismiss = { showMonthMenu = false },
                onSelectMonth = { monthStartMillis ->
                    selectedMonthStartMillis = monthStartMillis
                    showMonthMenu = false
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardBg)
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.06f),
                                RoundedCornerShape(22.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "No transactions found",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = selectedDateLabel?.let {
                                    "Nothing is available in ${selectedFilter.label.lowercase()} on $it."
                                } ?: "Nothing is available in ${selectedFilter.label.lowercase()} yet.",
                                color = TextSubtle,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                groupedTransactions.forEach { (sectionTitle, sectionItems) ->
                    item(key = "section_$sectionTitle") {
                        Text(
                            text = sectionTitle,
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                        )
                    }

                    items(
                        items = sectionItems,
                        key = { "${it.title}_${it.timestampMillis}_${it.kind}_${it.amountText}" }
                    ) { item ->
                        val metadataLabels = item.metadataLabels()
                        val metadataRows = metadataLabels.chunked(2)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.045f),
                                            CardBg,
                                            CardBg
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.055f),
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(horizontal = 15.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.08f),
                                                Accent1.copy(alpha = 0.12f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.iconRes != null) {
                                    Icon(
                                        painter = painterResource(id = item.iconRes),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    item.icon?.let { icon ->
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = item.displayTitle(),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = item.displayDetail(),
                                            color = Color.White.copy(alpha = 0.70f),
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    WalletTransactionAmountBadge(
                                        amountText = item.amountText,
                                        positive = item.positive
                                    )
                                }
                                Spacer(modifier = Modifier.height(7.dp))
                                Text(
                                    text = item.cardTimestampLabel(),
                                    color = TextSubtle,
                                    fontSize = 11.sp
                                )
                                if (metadataRows.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(9.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        metadataRows.forEach { rowLabels ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                rowLabels.forEach { label ->
                                                    val labelIndex = metadataLabels.indexOf(label)
                                                    WalletTransactionMetaChip(
                                                        label = label,
                                                        highlighted = labelIndex == metadataLabels.lastIndex &&
                                                            (
                                                                item.status == WalletTransactionStatus.REJECTED ||
                                                                    item.syncStatus == WalletSyncStatus.FAILED
                                                                )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun WalletTransactionEntry.matches(filter: WalletTransactionFilter): Boolean =
    when (filter) {
        WalletTransactionFilter.ALL -> true
        WalletTransactionFilter.AUDIO_CALL -> kind == WalletTransactionKind.AUDIO_CALL
        WalletTransactionFilter.VIDEO_CALL -> kind == WalletTransactionKind.VIDEO_CALL
        WalletTransactionFilter.CHAT -> kind == WalletTransactionKind.CHAT
        WalletTransactionFilter.GIFT -> kind == WalletTransactionKind.GIFT
        WalletTransactionFilter.PAYMENT_COMPLETED ->
            kind == WalletTransactionKind.PAYMENT && status == WalletTransactionStatus.COMPLETED
        WalletTransactionFilter.PAYMENT_REJECTED ->
            kind == WalletTransactionKind.PAYMENT && status == WalletTransactionStatus.REJECTED
    }

private fun WalletTransactionEntry.primaryLabel(): String =
    when (kind) {
        WalletTransactionKind.AUDIO_CALL -> "Audio call"
        WalletTransactionKind.VIDEO_CALL -> "Video call"
        WalletTransactionKind.CHAT -> "Chat"
        WalletTransactionKind.GIFT -> "Gift"
        WalletTransactionKind.PAYMENT -> "Payment"
    }

private fun WalletTransactionEntry.statusLabel(): String? =
    if (kind == WalletTransactionKind.PAYMENT) {
        when (status) {
            WalletTransactionStatus.COMPLETED -> "Completed"
            WalletTransactionStatus.REJECTED -> "Rejected"
        }
    } else {
        null
    }

private fun WalletTransactionEntry.syncLabel(): String? =
    when (syncStatus) {
        WalletSyncStatus.SYNCED -> if (kind == WalletTransactionKind.PAYMENT) "Ledger synced" else null
        WalletSyncStatus.PENDING -> "Syncing ledger"
        WalletSyncStatus.FAILED -> "Ledger sync failed"
    }

private fun WalletTransactionEntry.displayTitle(): String =
    when (kind) {
        WalletTransactionKind.PAYMENT -> title
        else -> counterpartyName ?: title
    }

private fun WalletTransactionEntry.displayDetail(): String =
    when (kind) {
        WalletTransactionKind.AUDIO_CALL -> detail.ifBlank { "Audio call completed" }
        WalletTransactionKind.VIDEO_CALL -> detail.ifBlank { "Video call completed" }
        WalletTransactionKind.CHAT -> detail.ifBlank { "Chat coin deduction recorded" }
        WalletTransactionKind.GIFT -> detail.ifBlank { "Gift sent during live call" }
        WalletTransactionKind.PAYMENT -> detail
    }

private fun WalletTransactionEntry.giftSummaryLabel(): String? =
    giftName?.let { name ->
        if (giftCount > 1) "$name x$giftCount" else name
    }

private fun WalletTransactionEntry.cardTimestampLabel(): String =
    SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault()).format(Date(timestampMillis))

private fun WalletTransactionEntry.metadataLabels(): List<String> =
    buildList {
        add(primaryLabel())
        when (kind) {
            WalletTransactionKind.AUDIO_CALL,
            WalletTransactionKind.VIDEO_CALL -> {
                durationSeconds?.let { add(formatCallDuration(it)) }
            }

            WalletTransactionKind.CHAT -> {
                messageCount?.takeIf { it > 0 }?.let { add("$it messages") }
            }

            WalletTransactionKind.GIFT -> {
                giftSummaryLabel()?.let { add(it) }
                giftContextLabel?.let { add(it) }
            }

            WalletTransactionKind.PAYMENT -> Unit
        }
        statusLabel()?.let { add(it) }
        syncLabel()?.let { add(it) }
    }

@Composable
private fun WalletTransactionAmountBadge(
    amountText: String,
    positive: Boolean
) {
    val accentColor = if (positive) SuccessGreen else Color(0xFFFF8A80)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .border(1.dp, accentColor.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$amountText coins",
            color = accentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun isSameMonth(firstMillis: Long, secondMillis: Long): Boolean {
    val first = Calendar.getInstance().apply { timeInMillis = firstMillis }
    val second = Calendar.getInstance().apply { timeInMillis = secondMillis }
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.MONTH) == second.get(Calendar.MONTH)
}

private fun monthStartMillis(timestampMillis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestampMillis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun yearStartMonthMillis(timestampMillis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestampMillis
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun buildWalletMonthSummaries(transactions: List<WalletTransactionEntry>): List<WalletMonthSummary> {
    val paymentTransactions = transactions
        .filter {
            it.kind == WalletTransactionKind.PAYMENT &&
                it.status == WalletTransactionStatus.COMPLETED
        }

    val monthTotals = paymentTransactions
        .groupBy { monthStartMillis(it.timestampMillis) }
        .mapValues { (_, monthTransactions) ->
            WalletMonthSummary(
                monthStartMillis = monthStartMillis(monthTransactions.first().timestampMillis),
                label = formatWalletMonthLabel(monthStartMillis(monthTransactions.first().timestampMillis)),
                rechargeRupees = monthTransactions.sumOf { it.rechargeAmountRupees },
                boughtCoins = monthTransactions.filter { it.coinsDelta > 0 }.sumOf { it.coinsDelta }
            )
        }

    val currentMonthStart = monthStartMillis(System.currentTimeMillis())
    val startMonth = yearStartMonthMillis(currentMonthStart)
    val endMonth = currentMonthStart
    val calendar = Calendar.getInstance().apply { timeInMillis = startMonth }
    val summaries = mutableListOf<WalletMonthSummary>()

    while (calendar.timeInMillis <= endMonth) {
        val monthStart = calendar.timeInMillis
        summaries += monthTotals[monthStart] ?: WalletMonthSummary(
            monthStartMillis = monthStart,
            label = formatWalletMonthLabel(monthStart),
            rechargeRupees = 0,
            boughtCoins = 0
        )
        calendar.add(Calendar.MONTH, 1)
    }

    return summaries.sortedByDescending { it.monthStartMillis }
}

private fun walletTransactionSectionLabel(
    timestampMillis: Long,
    selectedDateMillis: Long?
): String {
    if (selectedDateMillis != null) {
        return formatWalletSelectedDate(timestampMillis)
    }
    return when {
        isSameDay(timestampMillis, System.currentTimeMillis()) -> "Today"
        isYesterday(timestampMillis) -> "Yesterday"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestampMillis))
    }
}

private fun formatWalletSelectedDate(timestampMillis: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestampMillis))
}

private fun formatWalletMonthLabel(timestampMillis: Long): String {
    return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(timestampMillis))
}

@Composable
private fun WalletTotalSpendCard(
    totalRupees: Int,
    boughtCoins: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF18161D),
                        Color(0xFF221A24),
                        Color(0xFF17141B)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Accent2.copy(alpha = 0.20f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Total spend",
                    color = TextSubtle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.4.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Lifetime recharge",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$boughtCoins coins bought till now",
                    color = TextSubtle,
                    fontSize = 11.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹$totalRupees",
                    color = Accent1.copy(alpha = 0.98f),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "all time",
                    color = TextSubtle,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun WalletMonthlySpendCard(
    monthLabel: String,
    rechargeRupees: Int,
    boughtCoins: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF251D29),
                        CardBg,
                        Color(0xFF1B171F)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Accent1.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.06f)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Monthly spend",
                        color = TextSubtle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.4.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = monthLabel,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "₹$rechargeRupees",
                        color = Accent1.copy(alpha = 0.96f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.80f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$boughtCoins coins bought",
                    color = TextSubtle,
                    fontSize = 11.sp
                )
                Text(
                    text = "Tap to select month",
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun WalletMonthPickerDialog(
    months: List<WalletMonthSummary>,
    selectedMonthStartMillis: Long,
    onDismiss: () -> Unit,
    onSelectMonth: (Long) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1822),
                            CardBg,
                            Color(0xFF141218)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            Accent1.copy(alpha = 0.16f),
                            Color.White.copy(alpha = 0.06f)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Select month",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Pick a month to see how much was spent and how many coins were bought.",
                    color = TextSubtle,
                    fontSize = 12.sp
                )
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(months, key = { it.monthStartMillis }) { month ->
                        val selected = month.monthStartMillis == selectedMonthStartMillis
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selected) {
                                        Brush.linearGradient(
                                            listOf(
                                                Accent1.copy(alpha = 0.20f),
                                                Accent2.copy(alpha = 0.14f),
                                                Accent1.copy(alpha = 0.10f)
                                            )
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.05f),
                                                Color.White.copy(alpha = 0.04f)
                                            )
                                        )
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (selected) Accent1.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.07f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { onSelectMonth(month.monthStartMillis) }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = month.label,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${month.boughtCoins} coins bought",
                                    color = TextSubtle,
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${month.rechargeRupees}",
                                    color = if (selected) Accent1.copy(alpha = 0.98f) else Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (selected) "Selected" else "Tap to view",
                                    color = if (selected) Accent1.copy(alpha = 0.90f) else TextSubtle,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletDateActionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Accent1.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                if (selected) Accent1.copy(alpha = 0.26f) else Color.White.copy(alpha = 0.07f),
                RoundedCornerShape(999.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        trailingIcon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.86f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun WalletTransactionFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Accent1.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                if (selected) Accent1.copy(alpha = 0.26f) else Color.White.copy(alpha = 0.07f),
                RoundedCornerShape(999.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.84f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun WalletTransactionMetaChip(
    label: String,
    highlighted: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (highlighted) {
                    Color(0xFFFF8A80).copy(alpha = 0.12f)
                } else {
                    Color.White.copy(alpha = 0.06f)
                }
            )
            .border(
                1.dp,
                if (highlighted) Color(0xFFFF8A80).copy(alpha = 0.24f) else Color.White.copy(alpha = 0.06f),
                RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (highlighted) Color(0xFFFFB3AD) else Color.White.copy(alpha = 0.82f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun OnCallRechargeDialog(
    currentCoins: Int,
    callRatePerMinute: Int,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onRechargePack: (CoinPack) -> Unit
) {
    val dismissInteraction = remember { MutableInteractionSource() }
    val cardInteraction = remember { MutableInteractionSource() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.76f))
                .clickable(
                    interactionSource = dismissInteraction,
                    indication = null,
                    onClick = onDismiss
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 18.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .clickable(
                        interactionSource = cardInteraction,
                        indication = null,
                        onClick = {}
                    )
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF14070F),
                                Color(0xFF0C0509)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(30.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recharge on call",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap a pack and keep talking without leaving the call.",
                            color = TextSubtle,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                    IconButton(
                        enabled = !isProcessing,
                        onClick = onDismiss
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close recharge",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.06f),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Wallet coins",
                                color = TextSubtle,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$currentCoins 🪙",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.06f),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Current balance",
                                color = TextSubtle,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${currentCoins.coerceAtLeast(0)} coins",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                walletCoinPacks.forEach { pack ->
                    val addedTimeLabel = computeCallAllowanceSeconds(pack.coins, callRatePerMinute)
                        ?.let(::formatCallTimerLabel)
                        ?: "00:00"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.06f),
                                RoundedCornerShape(22.dp)
                            )
                            .clickable(enabled = !isProcessing) { onRechargePack(pack) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${pack.coins} coins",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "+$addedTimeLabel on call",
                                    color = Accent2.copy(alpha = 0.94f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Rs ${pack.price}",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isProcessing) "Processing..." else "Recharge",
                                    color = TextSubtle,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallHistoryScreen(
    historyList: SnapshotStateList<CallHistory>,
    blockedUserIds: Set<String>,
    sessionManager: SessionManager,
    authRepository: AuthRepository,
    onStartCall: (ActiveCallSession) -> Unit,
    onUserBlocked: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sortedHistory = historyList
        .filter { it.userId !in blockedUserIds }
        .sortedByDescending { it.startedAtMillis }
    var selectedUser by remember { mutableStateOf<CallHistory?>(null) }
    var showReasonDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(CardBgMuted, AppBg, AppBg)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                AppSectionHeader(
                    title = "Logs",
                    subtitle = "History of people you talked to",
                    showWalletChip = false
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (sortedHistory.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(CardBg)
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.06f),
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Abhi koi call log nahi hai",
                                color = TextSubtle,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                items(sortedHistory, key = { "${it.userId}-${it.startedAtMillis}" }) { item ->
                    SwipeCallHistoryItem(
                        item = item,
                        onAudioCall = { onStartCall(ActiveCallSession(userId = item.userId, name = item.name, isVideo = false)) },
                        onVideoCall = { onStartCall(ActiveCallSession(userId = item.userId, name = item.name, isVideo = true)) },
                        onBlock = {
                            selectedUser = item
                            showReasonDialog = true
                        }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }

    if (showReasonDialog && selectedUser != null) {
        BlockReasonDialog(
            onReasonSelected = { reason ->
                val userToBlock = selectedUser ?: return@BlockReasonDialog
                onUserBlocked(userToBlock.userId)
                showReasonDialog = false
                selectedUser = null
                scope.launch {
                    val accessToken = sessionManager.getAccessToken().trim()
                    if (accessToken.isNotBlank()) {
                        authRepository.reportUser(
                            accessToken = accessToken,
                            request = ReportUserRequest(
                                reportedUserId = userToBlock.userId,
                                reason = reason.toReportReasonCode(),
                                context = "call",
                                note = reason,
                                block = true
                            )
                        )
                    }
                    snackbarHostState.showSnackbar("User reported and blocked everywhere")
                }
            },
            onDismiss = {
                showReasonDialog = false
                selectedUser = null
            }
        )
    }
}

@Composable
fun PipCallSurface(activeCall: ActiveCallSession) {
    val duration = rememberCallDuration(activeCall.startedAtMillis)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Accent2.copy(alpha = 0.96f),
                        Accent1.copy(alpha = 0.9f)
                    )
                )
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeCall.name.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activeCall.displayLabel(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${if (activeCall.isVideo) "Video" else "Audio"} call • $duration",
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class CallGift(
    val id: String,
    val label: String,
    val emoji: String,
    val cost: Int
)

private val callGiftOptions = listOf(
    CallGift("rose", "Rose", "🌹", 10),
    CallGift("teddy", "Teddy", "🧸", 35),
    CallGift("diamond", "Diamond", "💎", 100),
    CallGift("ring", "Ring", "💍", 80),
    CallGift("chocolate", "Chocolate", "🍫", 20),
    CallGift("lipstick", "Lipstick", "💄", 45),
    CallGift("kiss", "Kiss", "😘", 15),
    CallGift("hug", "Hug", "🤗", 18),
    CallGift("panipuri", "Pani Puri", "🥙", 12),
    CallGift("handshake", "Handshake", "🤝", 10)
)

private fun buildGiftHistoryLabel(giftIds: List<String>): String? {
    if (giftIds.isEmpty()) return null
    val counts = giftIds.groupingBy { it }.eachCount()
    val summary = giftIds
        .distinct()
        .mapNotNull { id ->
            val gift = callGiftOptions.firstOrNull { it.id == id } ?: return@mapNotNull null
            val count = counts[id] ?: 0
            if (count > 1) "${gift.emoji}x$count" else gift.emoji
        }
        .joinToString(" ")
        .trim()
    return summary.takeIf { it.isNotEmpty() }?.let { "Gifts: $it" }
}

private fun calculateGiftCoins(giftIds: List<String>): Int {
    return giftIds.sumOf { id -> callGiftOptions.firstOrNull { it.id == id }?.cost ?: 0 }
}

@Composable
private fun LiveCallSurface(
    activeCall: ActiveCallSession,
    onEndCall: (Long, String?, Int) -> Unit,
    onReportAndBlock: (String, Long, String?, Int) -> Unit,
    onEnterPictureInPicture: () -> Unit
) {
    val context = LocalContext.current
    val coinsState = LocalCoins.current
    val homeActivity = context as? HomeActivity
    val scope = rememberCoroutineScope()
    val duration = rememberCallDuration(activeCall.startedAtMillis)
    val callRatePerMinute = activeCall.ratePerMinute.takeIf { it > 0 }
        ?: resolveCallRatePerMinute(activeCall.userId, activeCall.isVideo)
    var giftDeductedSeconds by rememberSaveable(activeCall.startedAtMillis) { mutableStateOf(0L) }
    var rechargeAddedSeconds by rememberSaveable(activeCall.startedAtMillis) { mutableStateOf(0L) }
    var showOnCallRecharge by rememberSaveable(activeCall.startedAtMillis) { mutableStateOf(false) }
    var isRechargeProcessing by rememberSaveable(activeCall.startedAtMillis) { mutableStateOf(false) }
    val effectiveIncludedSeconds =
        if (activeCall.includedSecondsAtStart != null || rechargeAddedSeconds > 0L) {
            ((activeCall.includedSecondsAtStart ?: 0L) + rechargeAddedSeconds - giftDeductedSeconds)
                .coerceAtLeast(0L)
        } else {
            null
        }
    val remainingAllowanceSeconds = rememberRemainingCallSeconds(
        startedAtMillis = activeCall.startedAtMillis,
        includedSecondsAtStart = effectiveIncludedSeconds
    )
    val remainingAllowanceLabel = remainingAllowanceSeconds?.let(::formatCallTimerLabel)
    val isTimerCritical = remainingAllowanceSeconds != null &&
        remainingAllowanceSeconds in 0L..CallLowTimeWarningSeconds
    var sentGiftIds by rememberSaveable(activeCall.startedAtMillis) { mutableStateOf(emptyList<String>()) }
    var isMuted by rememberSaveable(activeCall.startedAtMillis) {
        mutableStateOf(homeActivity?.isCallMicMuted() ?: false)
    }
    val audioRoute = homeActivity?.getCallAudioRoute() ?: CallAudioRoute.SPEAKER
    val showAudioRouteControl = homeActivity?.shouldShowCallAudioRouteControl() == true
    var isCameraEnabled by rememberSaveable(activeCall.startedAtMillis) { mutableStateOf(true) }
    var isFaceVisible by remember(activeCall.startedAtMillis) { mutableStateOf(true) }
    var faceCountdownSeconds by remember(activeCall.startedAtMillis) { mutableStateOf<Int?>(null) }
    var cameraAutoDisabled by rememberSaveable(activeCall.startedAtMillis) { mutableStateOf(false) }
    var isSelfPreviewFocused by rememberSaveable(activeCall.startedAtMillis) { mutableStateOf(false) }
    var showReportDialog by rememberSaveable(activeCall.startedAtMillis) { mutableStateOf(false) }
    val endCall = {
        val durationSeconds = ((System.currentTimeMillis() - activeCall.startedAtMillis) / 1000L).coerceAtLeast(1L)
        onEndCall(durationSeconds, buildGiftHistoryLabel(sentGiftIds), calculateGiftCoins(sentGiftIds))
    }
    val reportAndBlock: (String) -> Unit = { reason ->
        val durationSeconds = ((System.currentTimeMillis() - activeCall.startedAtMillis) / 1000L).coerceAtLeast(1L)
        showReportDialog = false
        onReportAndBlock(reason, durationSeconds, buildGiftHistoryLabel(sentGiftIds), calculateGiftCoins(sentGiftIds))
    }
    val handleGiftSelected: (CallGift) -> Unit = { gift ->
        if (coinsState.value < gift.cost) {
            toast(context, "Not enough coins")
        } else {
            coinsState.value = (coinsState.value - gift.cost).coerceAtLeast(0)
            sentGiftIds = sentGiftIds + gift.id
            val deductedSeconds = computeGiftTimeDeductionSeconds(gift.cost, callRatePerMinute)
            if (deductedSeconds > 0L && activeCall.includedSecondsAtStart != null) {
                giftDeductedSeconds =
                    (giftDeductedSeconds + deductedSeconds).coerceAtMost(activeCall.includedSecondsAtStart)
            }
        }
    }
    val handleRechargePackSelected: (CoinPack) -> Unit = rechargeSelection@ { pack ->
        if (isRechargeProcessing) return@rechargeSelection
        isRechargeProcessing = true
        scope.launch {
            delay(1400L)
            coinsState.value += pack.coins
            val addedSeconds = computeCallAllowanceSeconds(pack.coins, callRatePerMinute) ?: 0L
            rechargeAddedSeconds += addedSeconds
            isRechargeProcessing = false
            showOnCallRecharge = false
            toast(context, "Recharge successful")
        }
    }

    LaunchedEffect(activeCall.startedAtMillis, effectiveIncludedSeconds) {
        val includedSeconds = effectiveIncludedSeconds ?: return@LaunchedEffect
        val elapsedMillis = System.currentTimeMillis() - activeCall.startedAtMillis
        val remainingMillis = (includedSeconds * 1000L) - elapsedMillis
        if (remainingMillis > 0L) {
            delay(remainingMillis)
        }
        if (isActive) {
            toast(context, "Call time khatam ho gaya.")
            endCall()
        }
    }

    LaunchedEffect(isCameraEnabled, isFaceVisible) {
        if (!isCameraEnabled || isFaceVisible) {
            faceCountdownSeconds = null
            return@LaunchedEffect
        }

        val countdownStartedAt = System.currentTimeMillis()
        while (isActive && isCameraEnabled && !isFaceVisible) {
            val elapsed = System.currentTimeMillis() - countdownStartedAt
            val remaining = FaceMissingCameraTimeoutMillis - elapsed
            if (remaining <= 0L) {
                faceCountdownSeconds = null
                cameraAutoDisabled = true
                isCameraEnabled = false
                isFaceVisible = true
                break
            }
            faceCountdownSeconds = ((remaining + 999L) / 1000L).toInt()
            delay(250L)
        }
    }

    if (activeCall.isVideo) {
        InstagramVideoCallSurface(
            activeCall = activeCall,
            duration = duration,
            remainingAllowanceLabel = remainingAllowanceLabel,
            isTimerCritical = isTimerCritical,
            onTimerClick = { showOnCallRecharge = true },
            isMuted = isMuted,
            onToggleMute = {
                isMuted = homeActivity?.setCallMicMuted(!isMuted) ?: !isMuted
            },
            audioRoute = audioRoute,
            showAudioRouteControl = showAudioRouteControl,
            onToggleSpeaker = { homeActivity?.cycleCallAudioRoute() },
            isCameraEnabled = isCameraEnabled,
            cameraAutoDisabled = cameraAutoDisabled,
            faceCountdownSeconds = faceCountdownSeconds,
            isSelfPreviewFocused = isSelfPreviewFocused,
            onFacePresenceChanged = { hasFace ->
                if (isCameraEnabled) {
                    isFaceVisible = hasFace
                    if (hasFace) {
                        faceCountdownSeconds = null
                        cameraAutoDisabled = false
                    }
                }
            },
            onToggleCamera = {
                isCameraEnabled = !isCameraEnabled
                isFaceVisible = true
                faceCountdownSeconds = null
                cameraAutoDisabled = false
            },
            onToggleSelfPreviewFocus = {
                isSelfPreviewFocused = !isSelfPreviewFocused
            },
            onGiftSelected = handleGiftSelected,
            onEnterPictureInPicture = onEnterPictureInPicture,
            onReportClick = { showReportDialog = true },
            onEndCall = endCall
        )
        if (showOnCallRecharge) {
            OnCallRechargeDialog(
                currentCoins = coinsState.value,
                callRatePerMinute = callRatePerMinute,
                isProcessing = isRechargeProcessing,
                onDismiss = {
                    if (!isRechargeProcessing) showOnCallRecharge = false
                },
                onRechargePack = handleRechargePackSelected
            )
        }
        if (showReportDialog) {
            BlockReasonDialog(
                onReasonSelected = reportAndBlock,
                onDismiss = { showReportDialog = false }
            )
        }
        return
    }

    InstagramAudioCallSurface(
        activeCall = activeCall,
        duration = duration,
        remainingAllowanceLabel = remainingAllowanceLabel,
        isTimerCritical = isTimerCritical,
        onTimerClick = { showOnCallRecharge = true },
        isMuted = isMuted,
        onToggleMute = {
            isMuted = homeActivity?.setCallMicMuted(!isMuted) ?: !isMuted
        },
        audioRoute = audioRoute,
        showAudioRouteControl = showAudioRouteControl,
        onToggleSpeaker = { homeActivity?.cycleCallAudioRoute() },
        onGiftSelected = handleGiftSelected,
        onEnterPictureInPicture = onEnterPictureInPicture,
        onReportClick = { showReportDialog = true },
        onEndCall = endCall
    )
    if (showOnCallRecharge) {
        OnCallRechargeDialog(
            currentCoins = coinsState.value,
            callRatePerMinute = callRatePerMinute,
            isProcessing = isRechargeProcessing,
            onDismiss = {
                if (!isRechargeProcessing) showOnCallRecharge = false
            },
            onRechargePack = handleRechargePackSelected
        )
    }
    if (showReportDialog) {
        BlockReasonDialog(
            onReasonSelected = reportAndBlock,
            onDismiss = { showReportDialog = false }
        )
    }
}

@Composable
private fun InstagramAudioCallSurface(
    activeCall: ActiveCallSession,
    duration: String,
    remainingAllowanceLabel: String?,
    isTimerCritical: Boolean,
    onTimerClick: () -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    audioRoute: CallAudioRoute,
    showAudioRouteControl: Boolean,
    onToggleSpeaker: () -> Unit,
    onGiftSelected: (CallGift) -> Unit,
    onEnterPictureInPicture: () -> Unit,
    onReportClick: () -> Unit,
    onEndCall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CallWaterFlowBackground(
            topColor = Color(0xFF10060C),
            middleColor = Color(0xFF180812),
            bottomColor = Color(0xFF070408),
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CallHeaderInfo(
                    title = activeCall.displayLabel(),
                    subtitle = "Connected on audio call",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                CallTimerChip(
                    text = remainingAllowanceLabel ?: duration,
                    isCritical = isTimerCritical,
                    onClick = onTimerClick
                )
            }

            Spacer(modifier = Modifier.weight(0.55f))

            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Accent1, Accent2)))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(CardBgMuted, AppBg)))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activeCall.name.take(2).uppercase(),
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            CallGiftStrip(
                onGiftSelected = onGiftSelected,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.Black.copy(alpha = 0.22f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VideoCallRoundControlButton(
                        icon = Icons.Default.KeyboardArrowDown,
                        active = false,
                        onClick = onEnterPictureInPicture
                    )
                    VideoCallRoundControlButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        active = isMuted,
                        onClick = onToggleMute
                    )
                    if (showAudioRouteControl) {
                        VideoCallRoundControlButton(
                            icon = callAudioRouteIcon(audioRoute),
                            active = audioRoute != CallAudioRoute.EARPIECE,
                            onClick = onToggleSpeaker
                        )
                    }
                    VideoCallRoundControlButton(
                        icon = Icons.Default.MoreHoriz,
                        active = false,
                        onClick = onReportClick
                    )
                    VideoCallRoundControlButton(
                        icon = Icons.Default.CallEnd,
                        active = true,
                        onClick = onEndCall,
                        containerColor = Color(0xFFF8577D),
                        iconTint = Color.White
                    )
                }
            }
        }
    }
    
}

@Composable
private fun InstagramVideoCallSurface(
    activeCall: ActiveCallSession,
    duration: String,
    remainingAllowanceLabel: String?,
    isTimerCritical: Boolean,
    onTimerClick: () -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    audioRoute: CallAudioRoute,
    showAudioRouteControl: Boolean,
    onToggleSpeaker: () -> Unit,
    isCameraEnabled: Boolean,
    cameraAutoDisabled: Boolean,
    faceCountdownSeconds: Int?,
    isSelfPreviewFocused: Boolean,
    onFacePresenceChanged: (Boolean) -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSelfPreviewFocus: () -> Unit,
    onGiftSelected: (CallGift) -> Unit,
    onEnterPictureInPicture: () -> Unit,
    onReportClick: () -> Unit,
    onEndCall: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val density = LocalDensity.current
        val previewWidth = 106.dp
        val previewHeight = previewWidth * (4f / 3f)
        val edgeMargin = 16.dp
        val initialEndPadding = 18.dp
        val initialTopPadding = 62.dp
        val previewTopBound = 96.dp
        val previewBottomReservedSpace = 224.dp
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val previewWidthPx = with(density) { previewWidth.toPx() }
        val previewHeightPx = with(density) { previewHeight.toPx() }
        val edgeMarginPx = with(density) { edgeMargin.toPx() }
        val safeTopInsetPx = with(density) { 34.dp.toPx() }
        val previewTopBoundPx = safeTopInsetPx + with(density) { previewTopBound.toPx() }
        val previewBottomReservedPx = with(density) { previewBottomReservedSpace.toPx() }
        val defaultPreviewX = screenWidthPx - previewWidthPx - with(density) { initialEndPadding.toPx() }
        val defaultPreviewY = previewTopBoundPx.coerceAtLeast(
            safeTopInsetPx + with(density) { initialTopPadding.toPx() }
        )
        var floatingPreviewX by rememberSaveable(activeCall.startedAtMillis) { mutableStateOf(Float.NaN) }
        var floatingPreviewY by rememberSaveable(activeCall.startedAtMillis) { mutableStateOf(Float.NaN) }
        val previewTapInteraction = remember(activeCall.startedAtMillis) { MutableInteractionSource() }

        val leftPreviewX = edgeMarginPx
        val rightPreviewX = (screenWidthPx - previewWidthPx - edgeMarginPx).coerceAtLeast(edgeMarginPx)

        fun clampPreviewX(rawX: Float): Float = rawX.coerceIn(leftPreviewX, rightPreviewX)

        fun snapPreviewX(rawX: Float): Float {
            val midpoint = (leftPreviewX + rightPreviewX) / 2f
            return if (rawX < midpoint) leftPreviewX else rightPreviewX
        }

        fun clampPreviewY(rawY: Float): Float {
            val minY = previewTopBoundPx
            val maxY = (screenHeightPx - previewHeightPx - previewBottomReservedPx).coerceAtLeast(minY)
            return rawY.coerceIn(minY, maxY)
        }

        LaunchedEffect(screenWidthPx, screenHeightPx, previewTopBoundPx, previewBottomReservedPx) {
            if (floatingPreviewX.isNaN() || floatingPreviewY.isNaN()) {
                floatingPreviewX = snapPreviewX(clampPreviewX(defaultPreviewX))
                floatingPreviewY = clampPreviewY(defaultPreviewY)
            } else {
                floatingPreviewX = snapPreviewX(clampPreviewX(floatingPreviewX))
                floatingPreviewY = clampPreviewY(floatingPreviewY)
            }
        }

        val floatingPreviewModifier = Modifier
            .offset {
                IntOffset(
                    floatingPreviewX.toInt(),
                    floatingPreviewY.toInt()
                )
            }
            .width(previewWidth)
            .aspectRatio(3f / 4f)
            .pointerInput(screenWidthPx, screenHeightPx, previewTopBoundPx, previewBottomReservedPx) {
                detectDragGestures(
                    onDragEnd = {
                        floatingPreviewX = snapPreviewX(clampPreviewX(floatingPreviewX))
                    }
                ) { change, dragAmount ->
                    change.consume()
                    floatingPreviewX = clampPreviewX(floatingPreviewX + dragAmount.x)
                    floatingPreviewY = clampPreviewY(floatingPreviewY + dragAmount.y)
                }
            }

        CallWaterFlowBackground(
            topColor = Color(0xFF12070F),
            middleColor = Color(0xFF1B0B18),
            bottomColor = Color(0xFF060507),
            modifier = Modifier.matchParentSize()
        )
        if (isSelfPreviewFocused) {
            VideoCallSelfPreviewCard(
                name = "You",
                cameraEnabled = isCameraEnabled,
                cameraAutoDisabled = cameraAutoDisabled,
                onFacePresenceChanged = onFacePresenceChanged,
                cornerRadius = 0.dp,
                showBorder = false,
                modifier = Modifier.matchParentSize()
            )
        } else {
            InstagramVideoRemoteStage(
                name = activeCall.displayLabel(),
                modifier = Modifier.matchParentSize()
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f),
                            Color.Black.copy(alpha = 0.54f)
                        )
                    )
                )
        )
        if (faceCountdownSeconds != null) {
            FaceCountdownOverlay(
                seconds = faceCountdownSeconds,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 28.dp)
                    .offset(y = (-18).dp)
            )
        }
        CallHeaderInfo(
            title = activeCall.displayLabel(),
            subtitle = "Connected on video call",
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 22.dp, top = 18.dp, end = 132.dp)
        )
        CallTimerChip(
            text = remainingAllowanceLabel ?: duration,
            isCritical = isTimerCritical,
            onClick = onTimerClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 18.dp, end = 22.dp)
        )
        if (isSelfPreviewFocused) {
            VideoCallRemoteMiniCard(
                name = activeCall.displayLabel(),
                onClick = onToggleSelfPreviewFocus,
                modifier = floatingPreviewModifier
            )
        } else {
            VideoCallSelfPreviewCard(
                name = "You",
                cameraEnabled = isCameraEnabled,
                cameraAutoDisabled = cameraAutoDisabled,
                onFacePresenceChanged = onFacePresenceChanged,
                modifier = floatingPreviewModifier
                    .clickable(
                        interactionSource = previewTapInteraction,
                        indication = null
                    ) { onToggleSelfPreviewFocus() }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            CallGiftStrip(
                onGiftSelected = onGiftSelected,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.Black.copy(alpha = 0.22f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VideoCallRoundControlButton(
                        icon = Icons.Default.KeyboardArrowDown,
                        active = false,
                        onClick = onEnterPictureInPicture
                    )
                    VideoCallRoundControlButton(
                        icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        active = isMuted,
                        onClick = onToggleMute
                    )
                    if (showAudioRouteControl) {
                        VideoCallRoundControlButton(
                            icon = callAudioRouteIcon(audioRoute),
                            active = true,
                            onClick = onToggleSpeaker
                        )
                    }
                    VideoCallRoundControlButton(
                        iconPainter = painterResource(
                            id = if (isCameraEnabled) {
                                R.drawable.ic_video_call_modern
                            } else {
                                R.drawable.ic_video_call_off_modern
                            }
                        ),
                        active = !isCameraEnabled,
                        onClick = onToggleCamera
                    )
                    VideoCallRoundControlButton(
                        icon = Icons.Default.MoreHoriz,
                        active = false,
                        onClick = onReportClick
                    )
                    VideoCallRoundControlButton(
                        icon = Icons.Default.CallEnd,
                        active = true,
                        containerColor = Color(0xFFE9425D),
                        iconTint = Color.White,
                        onClick = onEndCall
                    )
                }
            }
        }
    }
}

@Composable
private fun CallWaterFlowBackground(
    topColor: Color,
    middleColor: Color,
    bottomColor: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "callWaterFlow")
    val waveShift by transition.animateFloat(
        initialValue = -0.45f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7600, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "waveShift"
    )
    val secondaryWaveShift by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = -0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "secondaryWaveShift"
    )
    val highlightShift by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9800, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "highlightShift"
    )

    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(topColor, middleColor, bottomColor)
            )
        )

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.04f),
                    Accent2.copy(alpha = 0.09f),
                    Color.Transparent
                ),
                start = Offset(
                    x = size.width * waveShift,
                    y = size.height * 0.04f
                ),
                end = Offset(
                    x = size.width * (waveShift + 0.64f),
                    y = size.height
                )
            )
        )

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Accent2.copy(alpha = 0.08f),
                    Accent1.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                start = Offset(
                    x = size.width * (secondaryWaveShift - 0.28f),
                    y = size.height * 0.22f
                ),
                end = Offset(
                    x = size.width * (secondaryWaveShift + 0.52f),
                    y = size.height * 0.96f
                )
            )
        )

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Accent1.copy(alpha = 0.07f),
                    Color.White.copy(alpha = 0.03f),
                    Color.Transparent
                ),
                start = Offset(
                    x = size.width * (highlightShift - 0.2f),
                    y = size.height * 0.1f
                ),
                end = Offset(
                    x = size.width * (highlightShift + 0.38f),
                    y = size.height * 0.92f
                )
            )
        )
    }
}

@Composable
private fun CallGiftStrip(
    onGiftSelected: (CallGift) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        itemsIndexed(
            items = callGiftOptions,
            key = { index, gift -> "${gift.id}-$index" }
        ) { _, gift ->
            Column(
                modifier = Modifier
                    .clickable { onGiftSelected(gift) }
                    .padding(horizontal = 2.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = gift.emoji,
                    fontSize = 34.sp
                )
                Text(
                    text = "\uD83E\uDE99 ${gift.cost}",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CallHeaderInfo(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            color = TextSubtle,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InstagramVideoRemoteStage(
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(164.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Accent1, Accent2)))
                .border(2.dp, Color.White.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(2).uppercase(),
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.34f),
                            Color.Transparent,
                            AppBg.copy(alpha = 0.84f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun VideoCallSelfPreviewCard(
    name: String,
    cameraEnabled: Boolean,
    cameraAutoDisabled: Boolean,
    onFacePresenceChanged: (Boolean) -> Unit,
    cornerRadius: Dp = 26.dp,
    showBorder: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedAvatarId = UserPrefs.getAvatar(context)
    val selectedAvatar = remember(selectedAvatarId) {
        avatarList.firstOrNull { it.id == selectedAvatarId } ?: avatarList.first()
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.Black)
            .then(
                if (showBorder) {
                    Modifier.border(
                        1.dp,
                        Color.White.copy(alpha = 0.12f),
                        RoundedCornerShape(cornerRadius)
                    )
                } else {
                    Modifier
                }
            )
    ) {
        if (cameraEnabled) {
            VideoCallCameraPreview(
                modifier = Modifier.matchParentSize(),
                onFacePresenceChanged = onFacePresenceChanged
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.42f)
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                CardBgMuted.copy(alpha = 0.96f),
                                AppBg.copy(alpha = 0.98f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            selectedAvatar.colors.last().copy(alpha = 0.32f),
                                            selectedAvatar.colors.first().copy(alpha = 0.18f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        AvatarBubble(avatar = selectedAvatar, size = 72.dp, highlight = true)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.Black.copy(alpha = 0.34f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_video_call_off_modern),
                                contentDescription = "Camera off",
                                tint = Color.White.copy(alpha = 0.92f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (cameraAutoDisabled) {
                                    "Face not found • Turn on camera"
                                } else {
                                    "Camera off"
                                },
                                color = Color.White.copy(alpha = 0.94f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun FaceCountdownOverlay(
    seconds: Int,
    modifier: Modifier = Modifier
) {
    val countdownValue by animateFloatAsState(
        targetValue = seconds.coerceIn(0, 5).toFloat(),
        animationSpec = tween(durationMillis = 220, easing = LinearEasing),
        label = "faceCountdownValue"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = countdownValue.toInt().toString(),
                color = Color.White,
                fontSize = 88.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-2).sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Show your face or your camera will turn off",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun VideoCallRemoteMiniCard(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(Color.Black.copy(alpha = 0.88f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.08f),
                            Color.Black.copy(alpha = 0.24f),
                            Color.Black.copy(alpha = 0.48f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(66.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Accent1, Accent2)))
                .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(2).uppercase(),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }
        Text(
            text = name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun VideoCallRoundControlButton(
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    containerColor: Color = if (active) Color.White else Color.Black.copy(alpha = 0.34f),
    iconTint: Color = if (active) AppBg else Color.White
) = VideoCallRoundControlButton(
    iconPainter = rememberVectorPainter(icon),
    active = active,
    onClick = onClick,
    containerColor = containerColor,
    iconTint = iconTint
)

@Composable
private fun VideoCallRoundControlButton(
    iconPainter: Painter,
    active: Boolean,
    onClick: () -> Unit,
    containerColor: Color = if (active) Color.White else Color.Black.copy(alpha = 0.34f),
    iconTint: Color = if (active) AppBg else Color.White
) {
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(containerColor)
            .border(
                width = 1.dp,
                color = if (active) Color.White.copy(alpha = 0.36f) else Color.White.copy(alpha = 0.10f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun CallTopActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.28f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun VideoCallCameraPreview(
    modifier: Modifier = Modifier,
    onFacePresenceChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val facePresenceCallback by rememberUpdatedState(onFacePresenceChanged)
    val hasCameraPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasCameraPermission) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(CardBgMuted, AppBg))),
            contentAlignment = Alignment.Center
        ) {
            facePresenceCallback(true)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_video_call_modern),
                    contentDescription = "Camera unavailable",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Camera access off hai",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        return
    }

    val controller = remember(context) { FrontCameraPreviewController(context.applicationContext) }
    controller.onFacePresenceChanged = { facePresenceCallback(it) }
    DisposableEffect(controller) {
        onDispose {
            controller.onFacePresenceChanged = null
            controller.release()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { viewContext ->
            TextureView(viewContext).apply {
                controller.bind(this)
            }
        },
        update = { previewView ->
            controller.bind(previewView)
        }
    )
}

private class FrontCameraPreviewController(
    private val context: Context
) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraThread = HandlerThread("frndzzz-video-preview").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)
    private var textureView: TextureView? = null
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var previewSurface: Surface? = null
    private var previewSize: Size = Size(1280, 960)
    private var isOpening = false
    private var isReleased = false
    private var faceDetectionEnabled = false
    private var lastFacePresence: Boolean? = null
    var onFacePresenceChanged: ((Boolean) -> Unit)? = null

    private val surfaceListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            applyPreviewTransform()
            openCameraIfReady()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            applyPreviewTransform()
            createPreviewSession()
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            closeCamera()
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
    }

    fun bind(view: TextureView) {
        if (isReleased) return
        if (textureView !== view) {
            textureView = view
            view.surfaceTextureListener = surfaceListener
        }
        applyPreviewTransform()
        if (view.isAvailable) {
            openCameraIfReady()
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCameraIfReady() {
        val previewView = textureView ?: return
        if (isReleased || !previewView.isAvailable || cameraDevice != null || isOpening || !hasCameraPermission()) {
            if (cameraDevice != null && captureSession == null) {
                createPreviewSession()
            }
            return
        }

        val cameraId = findFrontCameraId() ?: return
        val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
        previewSize = choosePreviewSize(
            characteristics = cameraCharacteristics,
            targetWidth = previewView.width.coerceAtLeast(720),
            targetHeight = previewView.height.coerceAtLeast(960)
        )
        applyPreviewTransform()
        faceDetectionEnabled = cameraCharacteristics
            .get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES)
            ?.any { mode ->
                mode == CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE ||
                    mode == CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL
            } == true
        if (!faceDetectionEnabled) {
            notifyFacePresence(true)
        }
        isOpening = true
        try {
            cameraManager.openCamera(cameraId, cameraStateCallback, cameraHandler)
        } catch (_: Exception) {
            isOpening = false
        }
    }

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(device: CameraDevice) {
            isOpening = false
            if (isReleased) {
                device.close()
                return
            }
            cameraDevice = device
            createPreviewSession()
        }

        override fun onDisconnected(device: CameraDevice) {
            isOpening = false
            device.close()
            if (cameraDevice == device) {
                cameraDevice = null
            }
            closeSession()
        }

        override fun onError(device: CameraDevice, error: Int) {
            isOpening = false
            device.close()
            if (cameraDevice == device) {
                cameraDevice = null
            }
            closeSession()
        }
    }

    private fun createPreviewSession() {
        val previewView = textureView ?: return
        val device = cameraDevice ?: return
        val surfaceTexture = previewView.surfaceTexture ?: return

        surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
        applyPreviewTransform()
        previewSurface?.release()
        val surface = Surface(surfaceTexture)
        previewSurface = surface

        try {
            val requestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(surface)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                if (faceDetectionEnabled) {
                    set(
                        CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                        CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE
                    )
                }
            }

            device.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        if (isReleased || cameraDevice == null) {
                            session.close()
                            return
                        }
                        captureSession?.close()
                        captureSession = session
                        try {
                            session.setRepeatingRequest(
                                requestBuilder.build(),
                                captureCallback,
                                cameraHandler
                            )
                        } catch (_: Exception) {
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        session.close()
                    }
                },
                cameraHandler
            )
        } catch (_: Exception) {
            closeSession()
        }
    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            updateFacePresence(partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            updateFacePresence(result)
        }
    }

    private fun updateFacePresence(result: CaptureResult) {
        if (!faceDetectionEnabled) return
        val hasFace = result.get(CaptureResult.STATISTICS_FACES)?.isNotEmpty() == true
        notifyFacePresence(hasFace)
    }

    fun release() {
        if (isReleased) return
        isReleased = true
        closeCamera()
        cameraThread.quitSafely()
    }

    private fun closeCamera() {
        closeSession()
        cameraDevice?.close()
        cameraDevice = null
        isOpening = false
        faceDetectionEnabled = false
        lastFacePresence = null
    }

    private fun closeSession() {
        captureSession?.close()
        captureSession = null
        previewSurface?.release()
        previewSurface = null
    }

    private fun choosePreviewSize(
        characteristics: CameraCharacteristics,
        targetWidth: Int,
        targetHeight: Int
    ): Size {
        val streamMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val sizes = streamMap
            ?.getOutputSizes(SurfaceTexture::class.java)
            ?.toList()
            ?.filter { it.width > 0 && it.height > 0 }
            .orEmpty()

        if (sizes.isEmpty()) {
            return Size(1280, 960)
        }

        val targetAspectRatio = targetWidth.toFloat() / targetHeight.coerceAtLeast(1)
        val targetArea = targetWidth * targetHeight

        return sizes.minWithOrNull(
            compareBy<Size> { size ->
                abs(getPortraitAspectRatio(size) - targetAspectRatio)
            }.thenBy { size ->
                abs((size.width * size.height) - targetArea)
            }
        ) ?: Size(1280, 960)
    }

    private fun getPortraitAspectRatio(size: Size): Float {
        val shortEdge = min(size.width, size.height).toFloat()
        val longEdge = max(size.width, size.height).toFloat().coerceAtLeast(1f)
        return shortEdge / longEdge
    }

    private fun applyPreviewTransform() {
        val previewView = textureView ?: return
        if (previewView.width == 0 || previewView.height == 0) return

        val viewRect = RectF(0f, 0f, previewView.width.toFloat(), previewView.height.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        val displayRotationDegrees = when (previewView.display?.rotation ?: Surface.ROTATION_0) {
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        val correctionDegrees = (360 - displayRotationDegrees) % 360
        val showPortraitBuffer = displayRotationDegrees == 0 || displayRotationDegrees == 180
        val portraitWidth = min(previewSize.width, previewSize.height).toFloat()
        val portraitHeight = max(previewSize.width, previewSize.height).toFloat()
        val bufferRect = if (showPortraitBuffer) {
            RectF(0f, 0f, portraitWidth, portraitHeight)
        } else {
            RectF(0f, 0f, portraitHeight, portraitWidth)
        }

        bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())

        val matrix = Matrix().apply {
            setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = max(
                previewView.width / bufferRect.width(),
                previewView.height / bufferRect.height()
            )
            postScale(scale, scale, centerX, centerY)
            if (correctionDegrees != 0) {
                postRotate(correctionDegrees.toFloat(), centerX, centerY)
            }
        }

        previewView.setTransform(matrix)
    }

    private fun findFrontCameraId(): String? {
        return cameraManager.cameraIdList.firstOrNull { cameraId ->
            cameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun notifyFacePresence(hasFace: Boolean) {
        if (lastFacePresence == hasFace) return
        lastFacePresence = hasFace
        onFacePresenceChanged?.invoke(hasFace)
    }
}

@Composable
private fun rememberCallDuration(startedAtMillis: Long): String {
    var elapsedSeconds by remember(startedAtMillis) { mutableStateOf(0L) }

    LaunchedEffect(startedAtMillis) {
        while (isActive) {
            elapsedSeconds = ((System.currentTimeMillis() - startedAtMillis) / 1000L).coerceAtLeast(0L)
            delay(1000L)
        }
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
private fun rememberRemainingCallSeconds(
    startedAtMillis: Long,
    includedSecondsAtStart: Long?
): Long? {
    if (includedSecondsAtStart == null) return null
    var remainingSeconds by remember(startedAtMillis, includedSecondsAtStart) {
        mutableStateOf(
            (
                includedSecondsAtStart -
                    ((System.currentTimeMillis() - startedAtMillis) / 1000L).coerceAtLeast(0L)
                ).coerceAtLeast(0L)
        )
    }

    LaunchedEffect(startedAtMillis, includedSecondsAtStart) {
        while (isActive) {
            val elapsedSeconds = ((System.currentTimeMillis() - startedAtMillis) / 1000L).coerceAtLeast(0L)
            remainingSeconds = (includedSecondsAtStart - elapsedSeconds).coerceAtLeast(0L)
            if (remainingSeconds <= 0L) break
            delay(1000L)
        }
    }

    return remainingSeconds
}

private fun formatCallTimerLabel(remainingSeconds: Long): String {
    val totalMinutes = remainingSeconds / 60L
    val seconds = remainingSeconds % 60L
    return "%02d:%02d".format(Locale.US, totalMinutes, seconds)
}

private fun callAudioRouteIcon(route: CallAudioRoute): ImageVector {
    return when (route) {
        CallAudioRoute.SPEAKER -> Icons.AutoMirrored.Filled.VolumeUp
        CallAudioRoute.EARPIECE -> Icons.Default.Hearing
        CallAudioRoute.BLUETOOTH -> Icons.Default.BluetoothAudio
    }
}

@Composable
private fun CallTimerChip(
    text: String,
    isCritical: Boolean,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val warningTransition = rememberInfiniteTransition(label = "callTimerWarning")
    val warningAlpha by warningTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 620, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "callTimerWarningAlpha"
    )
    val borderColor = if (isCritical) {
        DangerRed.copy(alpha = warningAlpha)
    } else {
        Color.White.copy(alpha = 0.08f)
    }
    val backgroundBrush = if (isCritical) {
        Brush.horizontalGradient(
            listOf(
                Color(0x33FF5E92),
                Color(0x22B91C4C)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Accent1.copy(alpha = 0.26f),
                Accent2.copy(alpha = 0.22f)
            )
        )
    }
    val chipShape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .clip(chipShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .background(backgroundBrush)
            .border(1.5.dp, borderColor, chipShape)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isCritical) Color(0xFFFFE1EA) else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CallControlButton(
    label: String,
    icon: ImageVector? = null,
    modifier: Modifier,
    active: Boolean,
    accentBrush: Brush = Brush.horizontalGradient(
        listOf(
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.08f)
        )
    ),
    activeContentColor: Color = Color.White,
    inactiveContentColor: Color = Color.White,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (active) Modifier.background(accentBrush)
                else Modifier.background(Color.White.copy(alpha = 0.08f))
            )
            .border(
                width = 1.dp,
                brush = if (active) {
                    Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.16f), Color.Transparent))
                } else {
                    Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.04f)))
                },
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val contentColor = if (active) activeContentColor else inactiveContentColor
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SwipeCallHistoryItem(item: CallHistory, onAudioCall: () -> Unit, onVideoCall: () -> Unit, onBlock: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
        if (it == SwipeToDismissBoxValue.EndToStart) onBlock()
        false
    })
    SwipeToDismissBox(state = dismissState, backgroundContent = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF8E173A))
                .padding(end = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text("Block & Report", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            CardBgMuted.copy(alpha = 0.96f),
                            CardBg.copy(alpha = 0.98f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Accent1, Accent2)))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.name.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CallLogMetaChip(label = if (item.isVideo) "Video" else "Audio")
                    CallLogMetaChip(label = formatCompactRelativeTime(item.startedAtMillis))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "${formatClockTime(item.startedAtMillis)} • ${formatCallDuration(item.durationSeconds)}",
                    color = TextSubtle,
                    fontSize = 12.sp
                )
                item.giftHistory?.let { giftHistory ->
                    Spacer(Modifier.height(5.dp))
                    Text(
                        giftHistory,
                        color = Accent1.copy(alpha = 0.92f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CallLogActionButton(
                    iconPainter = painterResource(id = R.drawable.ic_audio_call_modern),
                    onClick = onAudioCall
                )
                CallLogActionButton(
                    iconPainter = painterResource(id = R.drawable.ic_video_call_modern),
                    onClick = onVideoCall
                )
            }
        }
    }
}

@Composable
private fun CallLogMetaChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CallLogActionButton(
    icon: ImageVector,
    onClick: () -> Unit
) = CallLogActionButton(
    iconPainter = rememberVectorPainter(icon),
    onClick = onClick
)

@Composable
private fun CallLogActionButton(
    iconPainter: Painter,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun SwipePayCoinHistoryItem(pack: CoinPack, onPay: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
        if (it == SwipeToDismissBoxValue.StartToEnd) onPay()
        false
    })
    val highlightBorder = when (pack.coins) {
        799 -> Accent2.copy(alpha = 0.92f)
        2999 -> Accent1.copy(alpha = 0.92f)
        else -> Color.White.copy(alpha = 0.07f)
    }
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.horizontalGradient(listOf(SuccessGreen, Accent2)))
                    .padding(start = 22.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🪙", fontSize = 20.sp)
                    Text("Swipe to recharge", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardBg)
                .border(1.dp, highlightBorder, RoundedCornerShape(24.dp))
        ) {
            ShineOverlay(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
                    .alpha(0.34f)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Accent1.copy(alpha = 0.28f),
                                        Accent2.copy(alpha = 0.18f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🪙", fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                     Column(
                         modifier = Modifier.weight(1f),
                         verticalArrangement = Arrangement.spacedBy(0.dp)
                     ) {
                        Text(
                            text = "${pack.coins} coins",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                     }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = "₹${pack.price}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Swipe to pay",
                            color = Accent2.copy(alpha = 0.94f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Accent2.copy(alpha = 0.94f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShineOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shine")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(animation = tween(1800, easing = LinearEasing)),
        label = "shineOffset"
    )
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.22f), Color.Transparent),
                start = Offset(offsetX, 0f),
                end = Offset(offsetX + 200f, size.height)
            )
        )
    }
}

@Composable
private fun CallIconButton(
    icon: ImageVector,
    coinsText: String,
    modifier: Modifier,
    onClick: () -> Unit
) = CallIconButton(
    iconPainter = rememberVectorPainter(icon),
    coinsText = coinsText,
    modifier = modifier,
    onClick = onClick
)

@Composable
private fun CallIconButton(
    iconPainter: Painter,
    coinsText: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(Accent1, Accent2)))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("🪙", fontSize = 14.sp)
            Text(coinsText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
