package com.incoteam.realsaathi.ui.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.incoteam.realsaathi.RealSaathiApp
import com.incoteam.realsaathi.R
import com.incoteam.realsaathi.core.session.SessionManager
import com.incoteam.realsaathi.data.model.auth.HostKycRequest
import com.incoteam.realsaathi.data.model.auth.RemoteWalletTransaction
import com.incoteam.realsaathi.data.model.auth.SaveHostSettingsRequest
import com.incoteam.realsaathi.data.model.auth.SaveProfileRequest
import com.incoteam.realsaathi.data.model.auth.DirectChatRequest
import com.incoteam.realsaathi.data.model.auth.UnblockUserRequest
import com.incoteam.realsaathi.data.repository.AuthRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HostPreviewApp(
    sessionManager: SessionManager,
    onLogout: () -> Unit,
    onExitApp: () -> Unit,
    onSwitchToCustomerMode: () -> Unit,
    onSensitiveContentVisible: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as RealSaathiApp }
    val authRepository = remember(app) { app.authRepository }
    val scope = rememberCoroutineScope()
    var hostName by rememberSaveable {
        mutableStateOf(
            sessionManager.getDisplayName().ifBlank {
                UserPrefs.getHostDisplayName(context).ifBlank { "RealSaathi" }
            }
        )
    }
    val hostId = remember(sessionManager.getUserId()) {
        sessionManager.getUserId().ifBlank {
            "host-${sessionManager.getPhoneNumber().takeLast(4).ifBlank { "0000" }}"
        }
    }
    var tab by rememberSaveable { mutableStateOf(0) }
    var showWallet by rememberSaveable { mutableStateOf(false) }
    var showHostEditProfile by rememberSaveable { mutableStateOf(false) }
    var hostProfileScreen by rememberSaveable { mutableStateOf<String?>(null) }
    var audioLive by rememberSaveable { mutableStateOf(UserPrefs.getHostAudioLiveEnabled(context)) }
    var videoLive by rememberSaveable { mutableStateOf(UserPrefs.getHostVideoLiveEnabled(context)) }
    var audioRate by rememberSaveable { mutableStateOf(UserPrefs.getHostAudioRate(context)) }
    var videoRate by rememberSaveable { mutableStateOf(UserPrefs.getHostVideoRate(context)) }
    var hostProfilePhotoUri by rememberSaveable {
        mutableStateOf(UserPrefs.getHostProfilePhotoUri(context))
    }
    val storyUploads = remember {
        mutableStateListOf<UploadedHostStory>().apply {
            addAll(UserPrefs.getHostUploadedStories(context))
        }
    }
    val logs = remember {
        mutableStateListOf<HostLog>()
    }
    val withdrawalTransactions = remember {
        mutableStateListOf<HostWithdrawalTransaction>()
    }
    val chatThreads = remember { mutableStateListOf<ChatThread>() }
    var selectedChatThreadId by rememberSaveable { mutableStateOf<String?>(null) }
    var hostDashboardLoading by remember { mutableStateOf(true) }
    var isHostHomeRefreshing by remember { mutableStateOf(false) }
    var totalEarnings by rememberSaveable { mutableStateOf(0) }
    var walletBalance by rememberSaveable { mutableStateOf(0) }
    var todayEarnings by rememberSaveable { mutableStateOf(0) }
    var pendingCapture by remember { mutableStateOf<PendingHostCapture?>(null) }
    var switchingToCustomerMode by remember { mutableStateOf(false) }
    val isLive = audioLive || videoLive
    val shouldBlockScreenshots = !showWallet && tab == 2

    DisposableEffect(shouldBlockScreenshots) {
        onSensitiveContentVisible(shouldBlockScreenshots)
        onDispose {
            if (shouldBlockScreenshots) {
                onSensitiveContentVisible(false)
            }
        }
    }

    LaunchedEffect(hostName) {
        UserPrefs.saveHostDisplayName(context, hostName)
    }

    fun applyRemoteHostProfile(profile: com.incoteam.realsaathi.data.model.auth.RemoteUserProfile) {
        UserPrefs.syncFromRemoteProfile(context, profile)
        audioLive = profile.hostAudioLive
        videoLive = profile.hostVideoLive
        audioRate = profile.hostAudioRate
        videoRate = profile.hostVideoRate
        hostProfilePhotoUri = profile.hostProfilePhotoUrl.orEmpty()
        val syncedStories = profile.hostStories.orEmpty().map { it.toUploadedHostStory() }
        if (storyUploads.toList() != syncedStories) {
            storyUploads.clear()
            storyUploads.addAll(syncedStories)
        }
    }

    fun persistHostSettings(previousAudio: Boolean, previousVideo: Boolean, previousPhoto: String) {
        val accessToken = sessionManager.getAccessToken()
        if (accessToken.isBlank()) {
            toast(context, "Please login again")
            audioLive = previousAudio
            videoLive = previousVideo
            hostProfilePhotoUri = previousPhoto
            UserPrefs.saveHostAudioLiveEnabled(context, previousAudio)
            UserPrefs.saveHostVideoLiveEnabled(context, previousVideo)
            UserPrefs.saveHostProfilePhotoUri(context, previousPhoto)
            return
        }

        val topicTags = UserPrefs.getInterests(context)
        val nativeLanguages = listOfNotNull(
            UserPrefs.getLanguage(context)
                .takeIf { it.isNotBlank() && !it.equals("All", ignoreCase = true) }
        )

        scope.launch {
            authRepository.saveHostSettings(
                accessToken = accessToken,
                request = SaveHostSettingsRequest(
                    hostAudioLive = audioLive,
                    hostVideoLive = videoLive,
                    hostAudioRate = audioRate,
                    hostVideoRate = videoRate,
                    hostProfilePhotoUrl = hostProfilePhotoUri,
                    topicTags = topicTags,
                    nativeLanguages = nativeLanguages
                )
            ).onSuccess { response ->
                sessionManager.updateUserSession(response.user)
                hostName = response.user.displayName.orEmpty().ifBlank { hostName }
                applyRemoteHostProfile(response.profile)
            }.onFailure { throwable ->
                audioLive = previousAudio
                videoLive = previousVideo
                hostProfilePhotoUri = previousPhoto
                UserPrefs.saveHostAudioLiveEnabled(context, previousAudio)
                UserPrefs.saveHostVideoLiveEnabled(context, previousVideo)
                UserPrefs.saveHostProfilePhotoUri(context, previousPhoto)
                toast(context, throwable.message ?: "Host settings save nahi ho payi")
            }
        }
    }

    suspend fun refreshHostDashboard(showErrors: Boolean) {
        val accessToken = sessionManager.getAccessToken()
        if (accessToken.isBlank()) {
            hostDashboardLoading = false
            return
        }

        authRepository.getHostDashboard(accessToken)
            .onSuccess { response ->
                sessionManager.updateUserSession(response.user)
                hostName = response.user.displayName.orEmpty().ifBlank { hostName }
                applyRemoteHostProfile(response.profile)
                totalEarnings = response.wallet.totalEarnings
                walletBalance = response.wallet.totalEarnings
                todayEarnings = response.wallet.todayEarnings
                logs.clear()
                logs.addAll(
                    response.logs.map { log ->
                        HostLog(
                            name = log.name,
                            kind = log.kind.orEmpty(),
                            isVideo = log.isVideo,
                            duration = log.duration,
                            durationSeconds = log.durationSeconds ?: 0L,
                            messageCount = log.messageCount ?: 0,
                            amount = log.amount,
                            time = log.time,
                            createdAt = log.createdAt.orEmpty(),
                            callStatus = log.callStatus.orEmpty()
                        )
                    }
                )
            }
            .onFailure { throwable ->
                if (showErrors) {
                    toast(context, throwable.message ?: "Host dashboard load nahi ho paya")
                }
            }
        hostDashboardLoading = false
    }

    suspend fun refreshHostWalletSummary(showErrors: Boolean) {
        val accessToken = sessionManager.getAccessToken()
        if (accessToken.isBlank()) return

        authRepository.getWalletSummary(accessToken)
            .onSuccess { summary ->
                withdrawalTransactions.clear()
                withdrawalTransactions.addAll(
                    summary.transactions.mapNotNull { it.toHostWithdrawalTransaction() }
                )
            }
            .onFailure { throwable ->
                if (showErrors) {
                    toast(context, throwable.message ?: "Wallet transactions load nahi ho paye")
                }
            }
    }

    fun refreshHostHome() {
        if (isHostHomeRefreshing) return
        scope.launch {
            isHostHomeRefreshing = true
            try {
                refreshHostDashboard(showErrors = true)
                refreshHostWalletSummary(showErrors = true)
                toast(context, "Host home refreshed.")
            } finally {
                isHostHomeRefreshing = false
            }
        }
    }

    fun switchToCustomerMode() {
        if (switchingToCustomerMode) return

        val accessToken = sessionManager.getAccessToken()
        if (accessToken.isBlank()) {
            toast(context, "Please login again")
            return
        }

        val nickname = UserPrefs.getNickname(context).trim()
        val gender = UserPrefs.getGender(context).trim()
        if (nickname.isBlank() || gender.isBlank()) {
            toast(context, "Profile details missing hain. Edit profile se ek baar save karke phir try karo.")
            return
        }

        val preferredLanguage = UserPrefs.getLanguage(context)
            .takeIf { it.isNotBlank() && !it.equals("All", ignoreCase = true) }
            ?: "Hindi"
        val topicTags = UserPrefs.getInterests(context)
        val nativeLanguages = listOf(preferredLanguage)

        switchingToCustomerMode = true
        scope.launch {
            authRepository.saveProfile(
                accessToken = accessToken,
                request = SaveProfileRequest(
                    nickname = nickname,
                    gender = gender,
                    preferredLanguage = preferredLanguage,
                    avatarId = UserPrefs.getAvatar(context).takeIf { it > 0 } ?: DefaultAvatarId,
                    interests = topicTags,
                    accountMode = "customer",
                    topicTags = topicTags,
                    nativeLanguages = nativeLanguages,
                    communityName = null,
                    communityCity = null,
                    communityAbout = null,
                    communityExperience = null
                )
            ).onSuccess { response ->
                sessionManager.updateUserSession(response.user)
                hostName = response.user.displayName.orEmpty().ifBlank { hostName }
                applyRemoteHostProfile(response.profile)
                switchingToCustomerMode = false
                toast(context, "Customer mode active ho gaya.")
                onSwitchToCustomerMode()
            }.onFailure { throwable ->
                switchingToCustomerMode = false
                toast(context, throwable.message ?: "Customer mode switch nahi ho paya")
            }
        }
    }


    LaunchedEffect(Unit) {
        refreshHostDashboard(showErrors = true)
    }

    LaunchedEffect(tab) {
        if (tab == 1) refreshHostDashboard(showErrors = false)
    }

    LaunchedEffect(showWallet) {
        if (showWallet) {
            refreshHostWalletSummary(showErrors = true)
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(60_000)
            refreshHostDashboard(showErrors = false)
        }
    }

    fun uploadHostProfilePhoto(sourceUri: Uri) {
        val accessToken = sessionManager.getAccessToken()
        if (accessToken.isBlank()) {
            toast(context, "Please login again")
            return
        }

        scope.launch {
            val localUri = runCatching {
                copyStoryMediaToAppStorage(context, sourceUri, HostStoryMediaType.IMAGE)
            }.getOrElse {
                toast(context, "Profile photo update nahi ho paayi.")
                return@launch
            }
            val localFile = resolveLocalMediaFile(localUri)
            if (localFile == null || !localFile.exists()) {
                toast(context, "Profile photo file ready nahi ho paayi.")
                return@launch
            }
            val mimeType = context.contentResolver.getType(sourceUri).orEmpty().ifBlank { "image/jpeg" }
            authRepository.uploadHostMedia(
                accessToken = accessToken,
                file = localFile,
                mimeType = mimeType,
                purpose = "profile_photo"
            ).onSuccess { uploadResponse ->
                val previousPhoto = hostProfilePhotoUri
                hostProfilePhotoUri = uploadResponse.publicUrl.ifBlank { localUri }
                persistHostSettings(audioLive, videoLive, previousPhoto)
            }.onFailure {
                toast(context, it.message ?: "Profile photo update nahi ho paayi.")
            }
        }
    }

    fun uploadHostStoryFile(
        localFile: File,
        mediaType: HostStoryMediaType,
        sourceLabel: String,
        mimeType: String
    ) {
        val accessToken = sessionManager.getAccessToken()
        if (accessToken.isBlank()) {
            toast(context, "Please login again")
            return
        }

        scope.launch {
            authRepository.uploadHostMedia(
                accessToken = accessToken,
                file = localFile,
                mimeType = mimeType,
                purpose = "story"
            ).onSuccess { uploadResponse ->
                val latestStories = authRepository.getHostDashboard(accessToken)
                    .getOrNull()
                    ?.profile
                    ?.hostStories
                    ?.orEmpty()
                    ?.map { it.toUploadedHostStory() }
                    ?: storyUploads.toList()
                val nextStory = createHostStoryUpload(
                    ownerId = hostId,
                    ownerName = hostName,
                    mediaUrl = uploadResponse.publicUrl,
                    mediaType = if (uploadResponse.mediaType.orEmpty().equals("VIDEO", ignoreCase = true)) {
                        HostStoryMediaType.VIDEO
                    } else {
                        mediaType
                    },
                    sourceLabel = sourceLabel
                )
                val nextStories = (listOf(nextStory) + latestStories)
                    .sortedByDescending { it.createdAtMillis }
                    .take(12)
                val topicTags = UserPrefs.getInterests(context)
                val nativeLanguages = listOfNotNull(
                    UserPrefs.getLanguage(context)
                        .takeIf { it.isNotBlank() && !it.equals("All", ignoreCase = true) }
                )
                authRepository.saveHostSettings(
                    accessToken = accessToken,
                    request = SaveHostSettingsRequest(
                        hostAudioLive = audioLive,
                        hostVideoLive = videoLive,
                        hostAudioRate = audioRate,
                        hostVideoRate = videoRate,
                        hostProfilePhotoUrl = hostProfilePhotoUri,
                        hostStories = nextStories.map { it.toRemoteHostStory() },
                        topicTags = topicTags,
                        nativeLanguages = nativeLanguages
                    )
                ).onSuccess { response ->
                    sessionManager.updateUserSession(response.user)
                    hostName = response.user.displayName.orEmpty().ifBlank { hostName }
                    applyRemoteHostProfile(response.profile)
                    toast(context, "Story sabhi devices par sync ho gayi.")
                }.onFailure {
                    toast(context, it.message ?: "Story sync nahi ho paayi.")
                }
            }.onFailure {
                toast(context, it.message ?: "Story upload nahi ho paayi.")
            }
        }
    }

    val profilePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        uploadHostProfilePhoto(uri)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val mediaType = resolveStoryMediaType(context, uri)
            val savedUri = copyStoryMediaToAppStorage(context, uri, mediaType)
            val localFile = resolveLocalMediaFile(savedUri)
            if (localFile == null || !localFile.exists()) {
                error("Story file ready nahi ho paayi.")
            }
            val mimeType = context.contentResolver.getType(uri).orEmpty().ifBlank {
                defaultMimeTypeFor(mediaType)
            }
            uploadHostStoryFile(
                localFile = localFile,
                mediaType = mediaType,
                sourceLabel = "gallery",
                mimeType = mimeType
            )
        }.onFailure {
            toast(context, "Story upload nahi ho payi.")
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val capture = pendingCapture
        pendingCapture = null
        if (capture == null) return@rememberLauncherForActivityResult
        if (result.resultCode == Activity.RESULT_OK) {
            uploadHostStoryFile(
                localFile = capture.file,
                mediaType = capture.mediaType,
                sourceLabel = "camera",
                mimeType = defaultMimeTypeFor(capture.mediaType)
            )
        } else if (capture.file.exists()) {
            capture.file.delete()
        }
    }

    fun updateAudioLive(enabled: Boolean) {
        val previousAudio = audioLive
        val previousVideo = videoLive
        val previousPhoto = hostProfilePhotoUri
        audioLive = enabled
        persistHostSettings(previousAudio, previousVideo, previousPhoto)
    }

    fun updateVideoLive(enabled: Boolean) {
        val previousAudio = audioLive
        val previousVideo = videoLive
        val previousPhoto = hostProfilePhotoUri
        videoLive = enabled
        persistHostSettings(previousAudio, previousVideo, previousPhoto)
    }

    fun launchCamera(mediaType: HostStoryMediaType) {
        runCatching {
            val file = createHostStoryStorageFile(context, mediaType, "camera")
            val outputUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = if (mediaType == HostStoryMediaType.IMAGE) {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            } else {
                Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15)
                    putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                }
            }.apply {
                putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            pendingCapture = PendingHostCapture(file = file, mediaType = mediaType)
            cameraLauncher.launch(intent)
        }.onFailure {
            toast(context, "Camera open nahi ho paya.")
        }
    }

    fun syncHostProfileNameFromPrefs() {
        hostName = UserPrefs.getNickname(context).trim()
            .ifBlank { sessionManager.getDisplayName().ifBlank { hostName } }
    }

    BackHandler {
        when {
            selectedChatThreadId != null -> selectedChatThreadId = null
            hostProfileScreen != null -> hostProfileScreen = null
            showHostEditProfile -> {
                showHostEditProfile = false
                syncHostProfileNameFromPrefs()
            }
            showWallet -> showWallet = false
            tab != 0 -> tab = 0
            else -> onExitApp()
        }
    }

    Scaffold(
        containerColor = AppBg,
        bottomBar = {
            if (!showWallet && selectedChatThreadId == null && !showHostEditProfile && hostProfileScreen == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppBg)
                        .navigationBarsPadding()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HostBottomItem(tab == 0, Icons.Default.Home, "Home") { tab = 0 }
                    HostBottomItem(tab == 1, Icons.Default.History, "Recent") { tab = 1 }
                    HostBottomItem(tab == 2, Icons.Default.ChatBubbleOutline, "Chat", chatThreads.sumOf { it.unreadCount }) { tab = 2 }
                    HostBottomItem(tab == 3, Icons.Default.Person, "Profile") { tab = 3 }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(CardBgMuted, AppBg, AppBg)))
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            if (showWallet) {
                HostWalletScreen(
                    walletBalance = walletBalance,
                    totalEarnings = totalEarnings,
                    todayEarnings = todayEarnings,
                    withdrawals = withdrawalTransactions,
                    onBack = { showWallet = false }
                )
            } else if (hostProfileScreen != null) {
                when (hostProfileScreen) {
                    "kyc" -> HostKycScreen(
                        sessionManager = sessionManager,
                        authRepository = authRepository,
                        onBack = { hostProfileScreen = null }
                    )
                    "transactions" -> HostTransactionsScreen(
                        logs = logs,
                        onBack = { hostProfileScreen = null }
                    )
                    "settings" -> HostAccountSettingsScreen(
                        sessionManager = sessionManager,
                        authRepository = authRepository,
                        onBack = { hostProfileScreen = null },
                        onFeedback = { hostProfileScreen = "feedback" },
                        onDeleted = onLogout
                    )
                    "feedback" -> FeedbackScreen(
                        sessionManager = sessionManager,
                        onBack = { hostProfileScreen = "settings" }
                    )
                    "help" -> HostInfoScreen(
                        title = "Help & Support",
                        rows = listOf(
                            "Earnings issue" to "Share screenshot and date",
                            "Call issue" to "Report from recents/call screen",
                            "KYC issue" to "Vendor flow pending",
                            "Support" to "In-app support chat coming soon"
                        ),
                        onBack = { hostProfileScreen = null }
                    )
                }
            } else if (showHostEditProfile) {
                EditProfileScreen(
                    onBack = {
                        showHostEditProfile = false
                        syncHostProfileNameFromPrefs()
                        scope.launch { refreshHostDashboard(showErrors = false) }
                    },
                    onProfileUpdated = {
                        showHostEditProfile = false
                        syncHostProfileNameFromPrefs()
                    },
                    isSetupFlow = false,
                    isHostProfile = true,
                    hostProfilePhotoUri = hostProfilePhotoUri,
                    onHostProfilePhotoClick = {
                        profilePhotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            } else if (tab == 2) {
                HostChatScreen(
                    threads = chatThreads,
                    authRepository = authRepository,
                    sessionManager = sessionManager,
                    selectedThreadId = selectedChatThreadId,
                    onOpenThread = { threadId -> selectedChatThreadId = threadId },
                    onCloseThread = { selectedChatThreadId = null }
                )
            } else if (tab == 3) {
                HostProfileHome(
                    hostName = hostName,
                    profilePhotoUri = hostProfilePhotoUri,
                    onEditProfile = { showHostEditProfile = true },
                    onKyc = { hostProfileScreen = "kyc" },
                    onWallet = { showWallet = true },
                    onTransactions = { hostProfileScreen = "transactions" },
                    onSettings = { hostProfileScreen = "settings" },
                    onHelp = { hostProfileScreen = "help" },
                    onLogout = onLogout
                )
            } else {
                HostPage(
                    title = when (tab) {
                        0 -> "RealSaathi"
                        1 -> "Recent"
                        else -> "Profile"
                    },
                    subtitle = when (tab) {
                        0 -> ""
                        1 -> ""
                        else -> ""
                    },
                    walletBalance = walletBalance,
                    totalEarnings = totalEarnings,
                    onWalletClick = { showWallet = true },
                    showWalletChip = tab != 3,
                    isRefreshing = tab == 0 && isHostHomeRefreshing,
                    onRefresh = if (tab == 0) ::refreshHostHome else null
                ) {
                    when (tab) {
                        0 -> {
                            item {
                                HostStudioCard(
                                    audioLive = audioLive,
                                    videoLive = videoLive,
                                    audioRate = audioRate,
                                    videoRate = videoRate,
                                    onAudioToggle = ::updateAudioLive,
                                    onVideoToggle = ::updateVideoLive
                                )
                            }
                            item {
                                HostTodayEarningsCard(
                                    todayEarnings = todayEarnings,
                                    onClick = { showWallet = true }
                                )
                            }
                            item {
                                HostStoryUploadCard(
                                    onClick = {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                        )
                                    }
                                )
                            }
                        }
                        1 -> {
                            item {
                                HostRecentStatsRail(
                                    logs = logs,
                                    totalEarnings = totalEarnings
                                )
                            }
                            item { HostRecentDivider() }
                            if (logs.isEmpty()) {
                                item { HostNoTransactionHistory() }
                            } else {
                                items(logs) { HostLogCard(it) }
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun HostProfileHome(
    hostName: String,
    profilePhotoUri: String,
    onEditProfile: () -> Unit,
    onKyc: () -> Unit,
    onWallet: () -> Unit,
    onTransactions: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onLogout: () -> Unit
) {
    ProfilePageScaffold(title = "Profile", onBack = {}, showBackButton = false) {
        HostProfileOverviewCard(
            hostName = hostName,
            profilePhotoUri = profilePhotoUri,
            onEditProfile = onEditProfile
        )
        Spacer(Modifier.height(28.dp))
        HostProfileItem("KYC Verification", onClick = onKyc)
        Spacer(Modifier.height(12.dp))
        HostProfileItem("Wallet", onClick = onWallet)
        Spacer(Modifier.height(12.dp))
        HostProfileItem("Transactions", onClick = onTransactions)
        Spacer(Modifier.height(12.dp))
        HostProfileItem("Account Settings", onClick = onSettings)
        Spacer(Modifier.height(12.dp))
        HostProfileItem("Help & Support", onClick = onHelp)
        Spacer(Modifier.height(28.dp))
        HostProfileItem(text = "Log out", danger = true, onClick = onLogout)
        Spacer(Modifier.height(20.dp))
        HostProductFooter()
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun HostAccountSettingsScreen(
    sessionManager: SessionManager,
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onFeedback: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var blockedUsers by remember { mutableStateOf(emptyList<com.incoteam.realsaathi.data.model.auth.BlockedUserEntry>()) }
    var reportCases by remember { mutableStateOf(emptyList<com.incoteam.realsaathi.data.model.auth.UserReportEntry>()) }
    var isProcessing by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = !isProcessing) { onBack() }

    LaunchedEffect(Unit) {
        authRepository.listBlockedUsers(sessionManager.getAccessToken())
            .onSuccess { blockedUsers = it.blockedUsers }
        authRepository.listUserReports(sessionManager.getAccessToken())
            .onSuccess { reportCases = it.reports }
    }

    ProfilePageScaffold(title = "Account Settings", onBack = onBack) {
        Spacer(Modifier.height(10.dp))
        Text("Safety and moderation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.height(8.dp))
        Text("Block ya report options call, chat, aur available profiles ke context me milenge.", color = TextSubtle, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))
        Text("Blocked users", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.height(8.dp))
        if (blockedUsers.isEmpty()) Text("No blocked users.", color = TextSubtle, fontSize = 13.sp)
        blockedUsers.forEach { entry ->
            val label = entry.profile?.username ?: entry.profile?.publicId ?: entry.blockedId.take(8)
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = Color.White, modifier = Modifier.weight(1f))
                TextButton(enabled = !isProcessing, onClick = {
                    isProcessing = true
                    scope.launch {
                        authRepository.unblockUser(sessionManager.getAccessToken(), UnblockUserRequest(entry.blockedId))
                            .onSuccess { blockedUsers = blockedUsers.filterNot { it.blockedId == entry.blockedId }; toast(context, "User unblocked") }
                            .onFailure { toast(context, it.message ?: "Unblock nahi ho paya") }
                        isProcessing = false
                    }
                }) { Text("Unblock") }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Report cases", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.height(8.dp))
        if (reportCases.isEmpty()) Text("No report cases.", color = TextSubtle, fontSize = 13.sp)
        reportCases.take(8).forEach { report ->
            HostCard {
                Text("Case ${report.id} · ${report.status.replace('_', ' ').replaceFirstChar { it.uppercase() }}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("${report.reason.replace('_', ' ')} · ${report.profile?.username ?: report.reportedId ?: "Support"}", color = TextSubtle, fontSize = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
        }
        Spacer(Modifier.height(12.dp))
        HostProfileItem(text = "Feedback", onClick = onFeedback)
        Spacer(Modifier.height(28.dp))
        HostProfileItem(text = "Delete Account", danger = true, onClick = {
            if (!isProcessing) {
                scope.launch {
                    isProcessing = true
                    authRepository.deleteAccount(sessionManager.getAccessToken()).onSuccess { onDeleted() }
                        .onFailure { toast(context, it.message ?: "Account delete nahi ho paya") }
                    isProcessing = false
                }
            }
        })
    }
}

@Composable
private fun HostInfoScreen(
    title: String,
    rows: List<Pair<String, String>>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .hostContentWidth()
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            RealSaathiBackHeader(title = title, subtitle = null, onBack = onBack)
            Spacer(Modifier.height(14.dp))
            HostCard {
                rows.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = row.first,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = row.second,
                            color = TextSubtle,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (index != rows.lastIndex) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HostKycScreen(
    sessionManager: SessionManager,
    authRepository: AuthRepository,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val density = LocalDensity.current
    val isKeyboardOpen = WindowInsets.ime.getBottom(density) > 0
    val scope = rememberCoroutineScope()
    var fullName by rememberSaveable { mutableStateOf("") }
    var emailAddress by rememberSaveable { mutableStateOf("") }
    var whatsappNumber by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var dateOfBirth by rememberSaveable { mutableStateOf("") }
    var aadhaarNumber by rememberSaveable { mutableStateOf("") }
    var aadhaarDetectedName by rememberSaveable { mutableStateOf("") }
    var aadhaarDetectedDob by rememberSaveable { mutableStateOf("") }
    var panNumber by rememberSaveable { mutableStateOf("") }
    var accountHolderName by rememberSaveable { mutableStateOf("") }
    var bankName by rememberSaveable { mutableStateOf("") }
    var bankAccountNumber by rememberSaveable { mutableStateOf("") }
    var ifscCode by rememberSaveable { mutableStateOf("") }
    var kycStatus by rememberSaveable { mutableStateOf("not_submitted") }
    var aadhaarMasked by rememberSaveable { mutableStateOf("") }
    var panMasked by rememberSaveable { mutableStateOf("") }
    var bankMasked by rememberSaveable { mutableStateOf("") }
    var savedBankName by rememberSaveable { mutableStateOf("") }
    var savedIfsc by rememberSaveable { mutableStateOf("") }
    var savedHolder by rememberSaveable { mutableStateOf("") }
    var loading by rememberSaveable { mutableStateOf(true) }
    var submitting by rememberSaveable { mutableStateOf(false) }
    var aadhaarPhotoUri by rememberSaveable { mutableStateOf("") }
    var panPhotoUri by rememberSaveable { mutableStateOf("") }
    var selfieAadhaarPhotoUri by rememberSaveable { mutableStateOf("") }
    var aadhaarAutoFilled by rememberSaveable { mutableStateOf(false) }
    var fullNameAutoFilledFromAadhaar by rememberSaveable { mutableStateOf(false) }
    var dobAutoFilledFromAadhaar by rememberSaveable { mutableStateOf(false) }
    var accountHolderAutoFilledFromAadhaar by rememberSaveable { mutableStateOf(false) }
    var aadhaarReading by rememberSaveable { mutableStateOf(false) }
    var panAutoFilled by rememberSaveable { mutableStateOf(false) }
    var panReading by rememberSaveable { mutableStateOf(false) }
    var showBankSelector by rememberSaveable { mutableStateOf(false) }
    var pendingKycCapture by remember { mutableStateOf<PendingKycCapture?>(null) }
    var previewKycPhoto by remember { mutableStateOf<KycCaptureType?>(null) }
    val isKycLocked = kycStatus.isKycLocked()
    val aadhaarValue = aadhaarNumber.onlyDigits()
    val aadhaarHasError = aadhaarValue.isNotBlank() && !isValidAadhaarNumber(aadhaarValue)
    val panValue = panNumber.trim().uppercase(Locale.ENGLISH)
    val panHasError = panValue.isNotBlank() && !Regex("^[A-Z]{5}[0-9]{4}[A-Z]$").matches(panValue)
    val normalizedFullName = fullName.replace("\\s+".toRegex(), " ").trim()
    val normalizedEmail = emailAddress.trim()
    val normalizedWhatsapp = whatsappNumber.onlyDigits()
    val normalizedPhone = phoneNumber.onlyDigits()
    val normalizedDob = dateOfBirth.trim()
    val selectedBankName = bankName.trim()
    val holderPreview = accountHolderName.replace("\\s+".toRegex(), " ").trim()
    val accountPreview = bankAccountNumber.onlyDigits()
    val ifscPreview = ifscCode.trim().uppercase(Locale.ENGLISH)
    val isKycFormReady = !isKycLocked &&
        normalizedFullName.length >= 2 &&
        isValidEmail(normalizedEmail) &&
        Regex("^\\d{10}$").matches(normalizedWhatsapp) &&
        Regex("^\\d{10}$").matches(normalizedPhone) &&
        isValidDateOfBirth(normalizedDob) &&
        namesMatch(normalizedFullName, aadhaarDetectedName) &&
        normalizedDob == aadhaarDetectedDob &&
        isValidAadhaarNumber(aadhaarValue) &&
        aadhaarAutoFilled &&
        aadhaarPhotoUri.isNotBlank() &&
        Regex("^[A-Z]{5}[0-9]{4}[A-Z]$").matches(panValue) &&
        panAutoFilled &&
        panPhotoUri.isNotBlank() &&
        selfieAadhaarPhotoUri.isNotBlank() &&
        selectedBankName.length >= 2 &&
        holderPreview.length >= 2 &&
        accountHolderAutoFilledFromAadhaar &&
        namesMatch(holderPreview, normalizedFullName) &&
        Regex("^\\d{6,18}$").matches(accountPreview) &&
        Regex("^[A-Z]{4}0[A-Z0-9]{6}$").matches(ifscPreview)

    fun acceptKycPhoto(type: KycCaptureType, file: File) {
        val savedUri = Uri.fromFile(file).toString()
        when (type) {
            KycCaptureType.AADHAAR -> {
                aadhaarReading = true
                recognizeAadhaarDetailsFromPhoto(
                    context = context,
                    file = file,
                    onSuccess = { detected ->
                        aadhaarPhotoUri = savedUri
                        aadhaarNumber = detected.number
                        aadhaarDetectedName = detected.fullName
                        aadhaarDetectedDob = detected.dateOfBirth
                        fullName = detected.fullName
                        dateOfBirth = detected.dateOfBirth
                        accountHolderName = detected.fullName
                        aadhaarAutoFilled = true
                        fullNameAutoFilledFromAadhaar = true
                        dobAutoFilledFromAadhaar = true
                        accountHolderAutoFilledFromAadhaar = true
                        aadhaarReading = false
                        toast(context, "Aadhaar details detected")
                    },
                    onFailure = { message ->
                        runCatching { file.delete() }
                        aadhaarPhotoUri = ""
                        aadhaarNumber = ""
                        aadhaarDetectedName = ""
                        aadhaarDetectedDob = ""
                        aadhaarAutoFilled = false
                        if (fullNameAutoFilledFromAadhaar) fullName = ""
                        if (dobAutoFilledFromAadhaar) dateOfBirth = ""
                        if (accountHolderAutoFilledFromAadhaar) accountHolderName = ""
                        fullNameAutoFilledFromAadhaar = false
                        dobAutoFilledFromAadhaar = false
                        accountHolderAutoFilledFromAadhaar = false
                        aadhaarReading = false
                        toast(context, "$message Please retake.")
                    }
                )
            }
            KycCaptureType.PAN -> {
                panReading = true
                recognizePanNumberFromPhoto(
                    context = context,
                    file = file,
                    onSuccess = { detectedPan ->
                        panPhotoUri = savedUri
                        panNumber = detectedPan
                        panAutoFilled = true
                        panReading = false
                        toast(context, "PAN number detected")
                    },
                    onFailure = { message ->
                        runCatching { file.delete() }
                        panPhotoUri = ""
                        panNumber = ""
                        panAutoFilled = false
                        panReading = false
                        toast(context, "$message Please retake.")
                    }
                )
            }
            KycCaptureType.SELFIE_WITH_AADHAAR -> {
                selfieAadhaarPhotoUri = savedUri
            }
        }
    }

    val kycCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val capture = pendingKycCapture
        pendingKycCapture = null
        if (capture == null) return@rememberLauncherForActivityResult
        if (result.resultCode == Activity.RESULT_OK && capture.file.exists()) {
            val qualityIssue = validateCapturedKycPhoto(capture.file)
            if (qualityIssue != null) {
                runCatching { capture.file.delete() }
                toast(context, "${capture.type.displayName} was not accepted: $qualityIssue. Please retake.")
                return@rememberLauncherForActivityResult
            }
            when (capture.type) {
                KycCaptureType.AADHAAR,
                KycCaptureType.PAN,
                KycCaptureType.SELFIE_WITH_AADHAAR -> acceptKycPhoto(capture.type, capture.file)
            }
        } else {
            runCatching { capture.file.delete() }
        }
    }

    fun launchKycCamera(type: KycCaptureType) {
        if (isKycLocked) {
            toast(context, "Verification is already submitted. Editing is locked.")
            return
        }
        runCatching {
            val file = createHostKycStorageFile(context, type)
            val outputUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            pendingKycCapture = PendingKycCapture(type = type, file = file)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            kycCameraLauncher.launch(intent)
        }.onFailure {
            pendingKycCapture = null
            toast(context, "Unable to open camera")
        }
    }

    fun retakeKycPhoto(type: KycCaptureType) {
        if (isKycLocked) {
            toast(context, "Verification is already submitted. Retake is locked.")
            return
        }
        when (type) {
            KycCaptureType.AADHAAR -> {
                deleteKycPhotoFile(aadhaarPhotoUri)
                aadhaarPhotoUri = ""
                aadhaarNumber = ""
                aadhaarDetectedName = ""
                aadhaarDetectedDob = ""
                aadhaarAutoFilled = false
                fullNameAutoFilledFromAadhaar = false
                dobAutoFilledFromAadhaar = false
                if (accountHolderAutoFilledFromAadhaar) {
                    accountHolderName = ""
                }
                accountHolderAutoFilledFromAadhaar = false
                aadhaarReading = false
            }
            KycCaptureType.PAN -> {
                deleteKycPhotoFile(panPhotoUri)
                panPhotoUri = ""
                panNumber = ""
                panAutoFilled = false
                panReading = false
            }
            KycCaptureType.SELFIE_WITH_AADHAAR -> {
                deleteKycPhotoFile(selfieAadhaarPhotoUri)
                selfieAadhaarPhotoUri = ""
            }
        }
        launchKycCamera(type)
    }

    fun openOrCaptureKycPhoto(type: KycCaptureType, imageUri: String) {
        if (imageUri.isBlank()) {
            launchKycCamera(type)
        } else {
            previewKycPhoto = type
        }
    }

    LaunchedEffect(Unit) {
        val accessToken = sessionManager.getAccessToken()
        if (accessToken.isBlank()) {
            loading = false
            return@LaunchedEffect
        }
        authRepository.getHostKyc(accessToken)
            .onSuccess { response ->
                kycStatus = response.status
                aadhaarMasked = response.aadhaarMasked.orEmpty()
                panMasked = response.panMasked.orEmpty()
                bankMasked = response.bankAccountMasked.orEmpty()
                savedBankName = response.bankName.orEmpty()
                savedIfsc = response.ifscCode.orEmpty()
                savedHolder = response.accountHolderName.orEmpty()
                bankName = response.bankName.orEmpty()
                fullName = response.fullName.orEmpty()
                emailAddress = response.emailAddress.orEmpty()
                whatsappNumber = response.whatsappNumber.orEmpty()
                phoneNumber = response.phoneNumber.orEmpty()
                dateOfBirth = response.dateOfBirth.orEmpty()
            }
            .onFailure {
                toast(context, it.message ?: "Unable to load KYC status")
            }
        loading = false
    }

    fun submitKyc() {
        if (isKycLocked) {
            toast(context, "Verification is already submitted. Review is completed within 48 hours.")
            return
        }
        val aadhaar = aadhaarNumber.onlyDigits()
        val pan = panNumber.trim().uppercase(Locale.ENGLISH)
        val holder = accountHolderName.replace("\\s+".toRegex(), " ").trim()
        val personalName = fullName.replace("\\s+".toRegex(), " ").trim()
        val email = emailAddress.trim()
        val whatsapp = whatsappNumber.onlyDigits()
        val phone = phoneNumber.onlyDigits()
        val dob = dateOfBirth.trim()
        val submitBankName = bankName.trim()
        val account = bankAccountNumber.onlyDigits()
        val ifsc = ifscCode.trim().uppercase(Locale.ENGLISH)
        val aadhaarImage = encodeKycImageForApi(context, aadhaarPhotoUri)
        val panImage = encodeKycImageForApi(context, panPhotoUri)
        val selfieImage = encodeKycImageForApi(context, selfieAadhaarPhotoUri)
        val error = validateHostKycInput(
            aadhaar = aadhaar,
            aadhaarAutoFilled = aadhaarAutoFilled,
            pan = pan,
            panAutoFilled = panAutoFilled,
            fullName = personalName,
            emailAddress = email,
            whatsappNumber = whatsapp,
            phoneNumber = phone,
            dateOfBirth = dob,
            aadhaarName = aadhaarDetectedName,
            aadhaarDob = aadhaarDetectedDob,
            holder = holder,
            accountHolderAutoFilled = accountHolderAutoFilledFromAadhaar,
            bankName = submitBankName,
            account = account,
            ifsc = ifsc,
            aadhaarImage = aadhaarImage,
            panImage = panImage,
            selfieImage = selfieImage
        )
        if (error != null) {
            toast(context, error)
            return
        }
        val accessToken = sessionManager.getAccessToken()
        if (accessToken.isBlank()) {
            toast(context, "Please login again")
            return
        }
        submitting = true
        scope.launch {
            authRepository.saveHostKyc(
                accessToken = accessToken,
                request = HostKycRequest(
                    aadhaarNumber = aadhaar,
                    panNumber = pan,
                    fullName = personalName,
                    emailAddress = email,
                    whatsappNumber = whatsapp,
                    phoneNumber = phone,
                    dateOfBirth = dob,
                    accountHolderName = holder,
                    bankName = submitBankName,
                    bankAccountNumber = account,
                    ifscCode = ifsc,
                    aadhaarImageBase64 = aadhaarImage,
                    panImageBase64 = panImage,
                    selfieWithAadhaarImageBase64 = selfieImage
                )
            ).onSuccess { response ->
                kycStatus = response.status
                aadhaarMasked = response.aadhaarMasked.orEmpty()
                panMasked = response.panMasked.orEmpty()
                bankMasked = response.bankAccountMasked.orEmpty()
                savedBankName = response.bankName.orEmpty().ifBlank { submitBankName }
                savedIfsc = response.ifscCode.orEmpty()
                savedHolder = response.accountHolderName.orEmpty()
                bankName = savedBankName
                fullName = response.fullName.orEmpty().ifBlank { personalName }
                emailAddress = response.emailAddress.orEmpty().ifBlank { email }
                whatsappNumber = response.whatsappNumber.orEmpty().ifBlank { whatsapp }
                phoneNumber = response.phoneNumber.orEmpty().ifBlank { phone }
                dateOfBirth = response.dateOfBirth.orEmpty().ifBlank { dob }
                toast(context, response.message ?: "Verification submitted. Review will be completed within 48 hours.")
                aadhaarNumber = ""
                panNumber = ""
                bankAccountNumber = ""
                aadhaarAutoFilled = false
                aadhaarReading = false
                panAutoFilled = false
                panReading = false
            }.onFailure {
                toast(context, it.message ?: "Unable to submit KYC")
            }
            submitting = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .statusBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.hostContentWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
            RealSaathiBackHeader(title = "Verification", onBack = onBack)
        }
        Column(
            modifier = Modifier
                .hostContentWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            HostCard {
                Text(
                    text = "Complete verification",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Complete KYC to enable withdrawals. Review is completed within 48 hours after submission.",
                    color = TextSubtle,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            Spacer(Modifier.height(14.dp))
            HostCard {
                Text("Verification status", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (loading) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(color = Accent2, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Text("Loading status", color = TextSubtle, fontSize = 13.sp)
                    }
                } else {
                    HostKycStatusRow("Status", formatKycStatusLabel(kycStatus))
                    if (aadhaarMasked.isNotBlank()) HostKycStatusRow("Aadhaar", aadhaarMasked)
                    if (panMasked.isNotBlank()) HostKycStatusRow("PAN", panMasked)
                    if (savedBankName.isNotBlank()) HostKycStatusRow("Bank name", savedBankName)
                    if (bankMasked.isNotBlank()) HostKycStatusRow("Bank", bankMasked)
                    if (savedIfsc.isNotBlank()) HostKycStatusRow("IFSC", savedIfsc)
                    if (savedHolder.isNotBlank()) HostKycStatusRow("Holder", savedHolder)
                    if (isKycLocked) {
                        Text(
                            text = "Your verification is submitted and locked. The team will complete review within 48 hours.",
                            color = Accent2,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            HostCard {
                Text("Personal details", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                HostKycTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                            .replace("\\s+".toRegex(), " ")
                            .filter { char -> char.isLetter() || char.isWhitespace() || char == '.' }
                            .take(50)
                    },
                    label = "Full name",
                    placeholder = "Name as per Aadhaar",
                    enabled = !isKycLocked && !fullNameAutoFilledFromAadhaar
                )
                HostKycTextField(
                    value = emailAddress,
                    onValueChange = { emailAddress = it.trim().take(80) },
                    label = "Email ID",
                    placeholder = "name@example.com",
                    keyboardType = KeyboardType.Email,
                    capitalization = KeyboardCapitalization.None,
                    isError = emailAddress.isNotBlank() && !isValidEmail(emailAddress.trim()),
                    errorText = "Enter a valid email address",
                    enabled = !isKycLocked
                )
                HostKycTextField(
                    value = whatsappNumber,
                    onValueChange = { whatsappNumber = it.onlyDigits().take(10) },
                    label = "WhatsApp number",
                    placeholder = "10 digit WhatsApp number",
                    keyboardType = KeyboardType.Phone,
                    isError = whatsappNumber.isNotBlank() && whatsappNumber.onlyDigits().length != 10,
                    errorText = "Enter a valid 10 digit WhatsApp number",
                    enabled = !isKycLocked
                )
                HostKycTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it.onlyDigits().take(10) },
                    label = "Phone number",
                    placeholder = "10 digit phone number",
                    keyboardType = KeyboardType.Phone,
                    isError = phoneNumber.isNotBlank() && phoneNumber.onlyDigits().length != 10,
                    errorText = "Enter a valid 10 digit phone number",
                    enabled = !isKycLocked
                )
                HostKycTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = normalizeDobInput(it) },
                    label = "Date of birth",
                    placeholder = "DD/MM/YYYY",
                    keyboardType = KeyboardType.Text,
                    isError = dateOfBirth.isNotBlank() && !isValidDateOfBirth(dateOfBirth),
                    errorText = "Enter date of birth as DD/MM/YYYY",
                    enabled = !isKycLocked && !dobAutoFilledFromAadhaar
                )
                if (fullNameAutoFilledFromAadhaar || dobAutoFilledFromAadhaar) {
                    Text(
                        text = "Name and date of birth are verified from Aadhaar and cannot be edited.",
                        color = Accent2,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            HostCard {
                Text("Aadhaar card", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                HostKycPhotoBox(
                    title = "Capture Aadhaar card",
                    subtitle = "Use the original physical card. Keep all text clear.",
                    imageUri = aadhaarPhotoUri,
                    onClick = { openOrCaptureKycPhoto(KycCaptureType.AADHAAR, aadhaarPhotoUri) }
                )
                HostKycTextField(
                    value = aadhaarNumber,
                    onValueChange = { aadhaarNumber = it.onlyDigits().take(12) },
                    label = "Aadhaar number",
                    placeholder = if (aadhaarReading) "Reading Aadhaar from photo..." else "Auto-filled from Aadhaar photo",
                    keyboardType = KeyboardType.Number,
                    isError = aadhaarHasError,
                    errorText = "Aadhaar number must be a valid 12 digit number",
                    enabled = !isKycLocked && !aadhaarAutoFilled && !aadhaarReading
                )
                if (aadhaarAutoFilled) {
                    Text(
                        text = "Detected from Aadhaar photo. To change it, retake the photo.",
                        color = Accent2,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            HostCard {
                Text("PAN card", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                HostKycPhotoBox(
                    title = "Capture PAN card",
                    subtitle = "Use the original physical card. Keep the PAN number visible.",
                    imageUri = panPhotoUri,
                    onClick = { openOrCaptureKycPhoto(KycCaptureType.PAN, panPhotoUri) }
                )
                HostKycTextField(
                    value = panNumber,
                    onValueChange = { panNumber = normalizePanInput(it) },
                    label = "PAN number",
                    placeholder = if (panReading) "Reading PAN from photo..." else "Auto-filled from PAN photo",
                    capitalization = KeyboardCapitalization.Characters,
                    isError = panHasError,
                    errorText = "PAN format: 5 letters, 4 digits, 1 letter. Example AAAAA8454Q",
                    enabled = !isKycLocked && !panAutoFilled && !panReading
                )
                if (panAutoFilled) {
                    Text(
                        text = "Detected from PAN photo. To change it, retake the photo.",
                        color = Accent2,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            HostCard {
                Text("Selfie", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                HostKycPhotoBox(
                    title = "Capture selfie",
                    subtitle = "Keep your face centered and clearly visible.",
                    imageUri = selfieAadhaarPhotoUri,
                    onClick = { openOrCaptureKycPhoto(KycCaptureType.SELFIE_WITH_AADHAAR, selfieAadhaarPhotoUri) }
                )
            }
            Spacer(Modifier.height(14.dp))
            HostCard {
                Text("Bank account details", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                HostBankSelectorField(
                    value = bankName,
                    enabled = !isKycLocked,
                    onClick = { showBankSelector = true }
                )
                HostKycTextField(
                    value = bankName,
                    onValueChange = {
                        bankName = it
                            .replace("\\s+".toRegex(), " ")
                            .filter { char -> char.isLetterOrDigit() || char.isWhitespace() || char in "&().-" }
                            .take(70)
                    },
                    label = "Bank name",
                    placeholder = "Select from list or type bank name",
                    enabled = !isKycLocked
                )
                HostKycTextField(
                    value = accountHolderName,
                    onValueChange = {
                        accountHolderName = it
                            .replace("\\s+".toRegex(), " ")
                            .filter { char -> char.isLetter() || char.isWhitespace() || char == '.' }
                            .take(40)
                    },
                    label = "Account holder name",
                    placeholder = "Name as per bank",
                    enabled = !isKycLocked && !accountHolderAutoFilledFromAadhaar
                )
                if (accountHolderAutoFilledFromAadhaar) {
                    Text(
                        text = "Account holder name is matched with Aadhaar and locked.",
                        color = Accent2,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
                HostKycTextField(
                    value = bankAccountNumber,
                    onValueChange = { bankAccountNumber = it.onlyDigits().take(18) },
                    label = "Bank account number",
                    placeholder = "Account number",
                    keyboardType = KeyboardType.Number,
                    enabled = !isKycLocked
                )
                HostKycTextField(
                    value = ifscCode,
                    onValueChange = { ifscCode = it.uppercase(Locale.ENGLISH).filter { char -> char.isLetterOrDigit() }.take(11) },
                    label = "IFSC code",
                    placeholder = "ABCD0123456",
                    capitalization = KeyboardCapitalization.Characters,
                    enabled = !isKycLocked
                )
            }
            Spacer(Modifier.height(18.dp))
            HostKycSubmitButton(
                loading = submitting,
                enabled = isKycFormReady,
                text = if (isKycLocked) "Submitted for review" else "Submit KYC",
                onClick = { if (!submitting) submitKyc() }
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "KYC is required before withdrawals. After submission, details cannot be edited and review is completed within 48 hours.",
                color = TextSubtle,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(if (isKeyboardOpen) 180.dp else 36.dp))
        }
    }

    if (showBankSelector) {
        HostBankSelectionDialog(
            selectedBank = bankName,
            onDismiss = { showBankSelector = false },
            onSelect = { selected ->
                val previousPrefix = kycBankOptions.firstOrNull { it.name == bankName }?.ifscPrefix
                bankName = selected.name
                if (
                    !isKycLocked &&
                    !selected.ifscPrefix.isNullOrBlank() &&
                    (ifscCode.isBlank() || ifscCode == previousPrefix || ifscCode.length <= 5)
                ) {
                    ifscCode = selected.ifscPrefix
                }
                showBankSelector = false
            }
        )
    }

    previewKycPhoto?.let { type ->
        HostKycPhotoPreviewDialog(
            title = type.displayName,
            imageUri = when (type) {
                KycCaptureType.AADHAAR -> aadhaarPhotoUri
                KycCaptureType.PAN -> panPhotoUri
                KycCaptureType.SELFIE_WITH_AADHAAR -> selfieAadhaarPhotoUri
            },
            locked = isKycLocked,
            onDismiss = { previewKycPhoto = null },
            onRetake = {
                previewKycPhoto = null
                retakeKycPhoto(type)
            }
        )
    }

}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun HostKycTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words,
    isError: Boolean = false,
    errorText: String? = null,
    enabled: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    scope.launch {
                        delay(260)
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        enabled = enabled,
        label = { Text(label, color = TextSubtle) },
        placeholder = { Text(placeholder, color = TextSubtle.copy(alpha = 0.72f)) },
        singleLine = true,
        isError = isError,
        supportingText = if (isError && !errorText.isNullOrBlank()) {
            { Text(errorText, color = DangerRed, fontSize = 11.sp) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = keyboardType
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Accent2,
            unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
            errorBorderColor = DangerRed,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            errorTextColor = Color.White,
            disabledBorderColor = Accent2.copy(alpha = 0.45f),
            disabledTextColor = Color.White,
            disabledLabelColor = TextSubtle,
            disabledPlaceholderColor = TextSubtle,
            cursorColor = Accent2
        )
    )
}

@Composable
private fun HostKycPhotoBox(
    title: String,
    subtitle: String,
    imageUri: String,
    onClick: () -> Unit
) {
    val preview = rememberHostProfilePhotoBitmap(imageUri)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(
                width = 1.dp,
                color = if (imageUri.isBlank()) Color.White.copy(alpha = 0.10f) else Accent2.copy(alpha = 0.72f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (preview != null) {
            Image(
                bitmap = preview,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.18f))
                    .padding(8.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.Black.copy(alpha = 0.54f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Photo added", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Accent2)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Text("View photo", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = Accent2,
                    modifier = Modifier.size(30.dp)
                )
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSubtle, fontSize = 12.sp, lineHeight = 16.sp)
                Text("Open camera", color = Accent2, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HostKycPhotoPreviewDialog(
    title: String,
    imageUri: String,
    locked: Boolean,
    onDismiss: () -> Unit,
    onRetake: () -> Unit
) {
    val preview = rememberHostProfilePhotoBitmap(imageUri)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (!locked) {
                TextButton(onClick = onRetake) {
                    Text("Retake", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Accent2, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (preview != null) {
                    Image(
                        bitmap = preview,
                        contentDescription = title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 380.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                    )
                } else {
                    Text("Photo preview is not available.", color = TextSubtle, fontSize = 13.sp)
                }
                if (locked) {
                    Text(
                        "Submitted details are locked while verification is pending.",
                        color = Accent2,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        },
        containerColor = CardBg,
        titleContentColor = Color.White,
        textContentColor = TextSubtle
    )
}

@Composable
private fun HostKycStatusRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextSubtle, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HostBankSelectorField(
    value: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = if (enabled) 0.04f else 0.025f))
            .border(
                width = 1.dp,
                color = if (enabled) Color.White.copy(alpha = 0.10f) else Accent2.copy(alpha = 0.45f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Bank name", color = TextSubtle, fontSize = 12.sp)
            Text(
                text = value.ifBlank { "Select bank" },
                color = if (value.isBlank()) TextSubtle.copy(alpha = 0.72f) else Color.White,
                fontSize = 15.sp,
                fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HostBankSelectionDialog(
    selectedBank: String,
    onDismiss: () -> Unit,
    onSelect: (KycBankOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Accent2, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text("Select bank", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(kycBankOptions) { bank ->
                    val selected = bank.name == selectedBank
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Accent2.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.04f))
                            .clickable { onSelect(bank) }
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            bank.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Accent2,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        containerColor = CardBg,
        titleContentColor = Color.White,
        textContentColor = TextSubtle
    )
}

@Composable
private fun HostKycSubmitButton(
    loading: Boolean,
    enabled: Boolean,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = when {
            loading -> Accent2.copy(alpha = 0.46f)
            !enabled -> Color(0xFF6C626C)
            else -> Accent2
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(enabled = enabled && !loading) { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Submitting", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            } else {
                Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun validateHostKycInput(
    aadhaar: String,
    aadhaarAutoFilled: Boolean,
    pan: String,
    panAutoFilled: Boolean,
    fullName: String,
    emailAddress: String,
    whatsappNumber: String,
    phoneNumber: String,
    dateOfBirth: String,
    aadhaarName: String,
    aadhaarDob: String,
    holder: String,
    accountHolderAutoFilled: Boolean,
    bankName: String,
    account: String,
    ifsc: String,
    aadhaarImage: String?,
    panImage: String?,
    selfieImage: String?
): String? {
    return when {
        !isValidAadhaarNumber(aadhaar) -> "Enter a valid 12 digit Aadhaar number"
        !aadhaarAutoFilled -> "Aadhaar number must be detected from the photo. Please retake Aadhaar photo"
        aadhaarImage.isNullOrBlank() -> "Capture Aadhaar card photo"
        !Regex("^[A-Z]{5}[0-9]{4}[A-Z]$").matches(pan) -> "Enter valid PAN number"
        !panAutoFilled -> "PAN number must be detected from the photo. Please retake PAN photo"
        panImage.isNullOrBlank() -> "Capture PAN card photo"
        selfieImage.isNullOrBlank() -> "Capture selfie"
        fullName.length < 2 -> "Enter full name"
        !isValidEmail(emailAddress) -> "Enter valid email ID"
        !Regex("^\\d{10}$").matches(whatsappNumber) -> "Enter valid WhatsApp number"
        !Regex("^\\d{10}$").matches(phoneNumber) -> "Enter valid phone number"
        !isValidDateOfBirth(dateOfBirth) -> "Enter valid date of birth"
        aadhaarName.isBlank() -> "Aadhaar name must be detected from the photo. Please retake Aadhaar photo"
        aadhaarDob.isBlank() -> "Aadhaar date of birth must be detected from the photo. Please retake Aadhaar photo"
        !namesMatch(fullName, aadhaarName) -> "Full name must match Aadhaar name"
        dateOfBirth != aadhaarDob -> "Date of birth must match Aadhaar"
        bankName.isBlank() -> "Select bank name"
        holder.length < 2 -> "Enter account holder name"
        !accountHolderAutoFilled -> "Account holder name must be auto-filled from Aadhaar"
        !namesMatch(holder, fullName) -> "Account holder name must match Aadhaar name"
        !Regex("^\\d{6,18}$").matches(account) -> "Enter valid bank account number"
        !Regex("^[A-Z]{4}0[A-Z0-9]{6}$").matches(ifsc) -> "Enter valid IFSC code"
        else -> null
    }
}

private data class KycBankOption(
    val name: String,
    val ifscPrefix: String?
)

private fun isValidEmail(value: String): Boolean {
    return Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)
        .matches(value.trim())
}

private fun normalizeDobInput(input: String): String {
    val digits = input
        .replace(Regex("[^0-9]"), "")
        .take(8)
    return buildString {
        digits.forEachIndexed { index, char ->
            if (index == 2 || index == 4) append('/')
            append(char)
        }
    }
}

private fun namesMatch(left: String, right: String): Boolean {
    val normalizedLeft = normalizePersonNameForMatch(left)
    val normalizedRight = normalizePersonNameForMatch(right)
    return normalizedLeft.isNotBlank() && normalizedLeft == normalizedRight
}

private fun normalizePersonNameForMatch(value: String): String {
    return value
        .uppercase(Locale.ENGLISH)
        .replace(Regex("[^A-Z ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun isValidDateOfBirth(value: String): Boolean {
    val match = Regex("^(\\d{2})/(\\d{2})/(\\d{4})$").matchEntire(value.trim()) ?: return false
    val day = match.groupValues[1].toIntOrNull() ?: return false
    val month = match.groupValues[2].toIntOrNull() ?: return false
    val year = match.groupValues[3].toIntOrNull() ?: return false
    if (year !in 1900..Calendar.getInstance().get(Calendar.YEAR)) return false
    return runCatching {
        val parser = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).apply { isLenient = false }
        val date = parser.parse("%02d/%02d/%04d".format(day, month, year)) ?: return false
        !date.after(Date())
    }.getOrDefault(false)
}

private fun isValidAadhaarNumber(value: String): Boolean {
    val digits = value.onlyDigits()
    if (!Regex("^[2-9]\\d{11}$").matches(digits)) return false
    return verifyVerhoeff(digits)
}

private fun verifyVerhoeff(number: String): Boolean {
    val multiplication = arrayOf(
        intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
        intArrayOf(1, 2, 3, 4, 0, 6, 7, 8, 9, 5),
        intArrayOf(2, 3, 4, 0, 1, 7, 8, 9, 5, 6),
        intArrayOf(3, 4, 0, 1, 2, 8, 9, 5, 6, 7),
        intArrayOf(4, 0, 1, 2, 3, 9, 5, 6, 7, 8),
        intArrayOf(5, 9, 8, 7, 6, 0, 4, 3, 2, 1),
        intArrayOf(6, 5, 9, 8, 7, 1, 0, 4, 3, 2),
        intArrayOf(7, 6, 5, 9, 8, 2, 1, 0, 4, 3),
        intArrayOf(8, 7, 6, 5, 9, 3, 2, 1, 0, 4),
        intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
    )
    val permutation = arrayOf(
        intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
        intArrayOf(1, 5, 7, 6, 2, 8, 3, 0, 9, 4),
        intArrayOf(5, 8, 0, 3, 7, 9, 6, 1, 4, 2),
        intArrayOf(8, 9, 1, 6, 0, 4, 3, 5, 2, 7),
        intArrayOf(9, 4, 5, 3, 1, 2, 6, 8, 7, 0),
        intArrayOf(4, 2, 8, 6, 5, 7, 3, 9, 0, 1),
        intArrayOf(2, 7, 9, 3, 8, 0, 6, 4, 1, 5),
        intArrayOf(7, 0, 4, 6, 9, 1, 3, 2, 5, 8)
    )
    var checksum = 0
    number.reversed().forEachIndexed { index, char ->
        val digit = char.digitToIntOrNull() ?: return false
        checksum = multiplication[checksum][permutation[index % 8][digit]]
    }
    return checksum == 0
}

private val kycBankOptions = listOf(
    KycBankOption("Abhyudaya Co-operative Bank", "ABHY0"),
    KycBankOption("Airtel Payments Bank", "AIRP0"),
    KycBankOption("Allahabad Bank", "ALLA0"),
    KycBankOption("Andhra Pragathi Grameena Bank", null),
    KycBankOption("Andhra Pradesh Grameena Vikas Bank", null),
    KycBankOption("Andhra Pradesh State Co-operative Bank", "APBL0"),
    KycBankOption("Apna Sahakari Bank", "ASBL0"),
    KycBankOption("Assam Gramin Vikash Bank", null),
    KycBankOption("AU Small Finance Bank", "AUBL0"),
    KycBankOption("Axis Bank", "UTIB0"),
    KycBankOption("Bandhan Bank", "BDBL0"),
    KycBankOption("Bangiya Gramin Vikash Bank", null),
    KycBankOption("Bank of Bahrain and Kuwait", "BBKM0"),
    KycBankOption("Bank of Baroda", "BARB0"),
    KycBankOption("Bank of Ceylon", "BCEY0"),
    KycBankOption("Bank of India", "BKID0"),
    KycBankOption("Bank of Maharashtra", "MAHB0"),
    KycBankOption("Baroda Gujarat Gramin Bank", null),
    KycBankOption("Baroda Rajasthan Kshetriya Gramin Bank", null),
    KycBankOption("Baroda UP Bank", null),
    KycBankOption("Bassein Catholic Co-operative Bank", "BACB0"),
    KycBankOption("Bihar Gramin Bank", null),
    KycBankOption("Canara Bank", "CNRB0"),
    KycBankOption("Capital Small Finance Bank", "CLBL0"),
    KycBankOption("Catholic Syrian Bank", "CSBK0"),
    KycBankOption("Central Bank of India", "CBIN0"),
    KycBankOption("Chaitanya Godavari Grameena Bank", null),
    KycBankOption("Chhattisgarh Rajya Gramin Bank", null),
    KycBankOption("Citibank India", "CITI0"),
    KycBankOption("City Union Bank", "CIUB0"),
    KycBankOption("Coastal Local Area Bank", null),
    KycBankOption("Credit Agricole Corporate and Investment Bank", "CRLY0"),
    KycBankOption("DBS Bank India", "DBSS0"),
    KycBankOption("DCB Bank", "DCBL0"),
    KycBankOption("Deutsche Bank", "DEUT0"),
    KycBankOption("Dhanlaxmi Bank", "DLXB0"),
    KycBankOption("Doha Bank", "DOHB0"),
    KycBankOption("Emirates NBD Bank", "EBIL0"),
    KycBankOption("Equitas Small Finance Bank", "ESFB0"),
    KycBankOption("ESAF Small Finance Bank", "ESMF0"),
    KycBankOption("Federal Bank", "FDRL0"),
    KycBankOption("Fincare Small Finance Bank", "FSFB0"),
    KycBankOption("FINO Payments Bank", "FINO0"),
    KycBankOption("First Abu Dhabi Bank", "NBAD0"),
    KycBankOption("G P Parsik Sahakari Bank", "PJSB0"),
    KycBankOption("Goa State Co-operative Bank", "YESB0"),
    KycBankOption("Gujarat State Co-operative Bank", "GSCB0"),
    KycBankOption("HDFC Bank", "HDFC0"),
    KycBankOption("Himachal Pradesh Gramin Bank", null),
    KycBankOption("HSBC India", "HSBC0"),
    KycBankOption("ICICI Bank", "ICIC0"),
    KycBankOption("IDBI Bank", "IBKL0"),
    KycBankOption("IDFC FIRST Bank", "IDFB0"),
    KycBankOption("India Post Payments Bank", "IPOS0"),
    KycBankOption("Indian Bank", "IDIB0"),
    KycBankOption("Indian Overseas Bank", "IOBA0"),
    KycBankOption("IndusInd Bank", "INDB0"),
    KycBankOption("Jalgaon Janata Sahakari Bank", "JSBP0"),
    KycBankOption("Jammu & Kashmir Bank", "JAKA0"),
    KycBankOption("Jana Small Finance Bank", "JSFB0"),
    KycBankOption("Jharkhand Rajya Gramin Bank", null),
    KycBankOption("Kallappanna Awade Ichalkaranji Janata Sahakari Bank", null),
    KycBankOption("Karnataka Bank", "KARB0"),
    KycBankOption("Karnataka Gramin Bank", null),
    KycBankOption("Karnataka Vikas Grameena Bank", null),
    KycBankOption("Karur Vysya Bank", "KVBL0"),
    KycBankOption("Kerala Gramin Bank", null),
    KycBankOption("Kotak Mahindra Bank", "KKBK0"),
    KycBankOption("Madhya Bihar Gramin Bank", null),
    KycBankOption("Madhya Pradesh Gramin Bank", null),
    KycBankOption("Madhyanchal Gramin Bank", null),
    KycBankOption("Mahanagar Co-operative Bank", "MCBL0"),
    KycBankOption("Maharashtra Gramin Bank", null),
    KycBankOption("Manipur Rural Bank", null),
    KycBankOption("Meghalaya Rural Bank", null),
    KycBankOption("Mizoram Rural Bank", null),
    KycBankOption("Nagaland Rural Bank", null),
    KycBankOption("Nainital Bank", "NTBL0"),
    KycBankOption("North East Small Finance Bank", "NESF0"),
    KycBankOption("Odisha Gramya Bank", null),
    KycBankOption("Paschim Banga Gramin Bank", null),
    KycBankOption("Paytm Payments Bank", "PYTM0"),
    KycBankOption("Pragathi Krishna Gramin Bank", null),
    KycBankOption("Prathama UP Gramin Bank", null),
    KycBankOption("Punjab & Sind Bank", "PSIB0"),
    KycBankOption("Punjab Gramin Bank", null),
    KycBankOption("Punjab National Bank", "PUNB0"),
    KycBankOption("Rajasthan Marudhara Gramin Bank", null),
    KycBankOption("RBL Bank", "RATN0"),
    KycBankOption("Saptagiri Grameena Bank", null),
    KycBankOption("Saraswat Co-operative Bank", "SRCB0"),
    KycBankOption("Sarva Haryana Gramin Bank", null),
    KycBankOption("Saurashtra Gramin Bank", null),
    KycBankOption("Shivalik Small Finance Bank", "SFSB0"),
    KycBankOption("South Indian Bank", "SIBL0"),
    KycBankOption("Standard Chartered Bank", "SCBL0"),
    KycBankOption("State Bank of India", "SBIN0"),
    KycBankOption("Suryoday Small Finance Bank", "SURY0"),
    KycBankOption("Tamil Nadu Grama Bank", null),
    KycBankOption("Tamilnad Mercantile Bank", "TMBL0"),
    KycBankOption("Telangana Grameena Bank", null),
    KycBankOption("Tripura Gramin Bank", null),
    KycBankOption("UCO Bank", "UCBA0"),
    KycBankOption("Ujjivan Small Finance Bank", "UJVN0"),
    KycBankOption("Union Bank of India", "UBIN0"),
    KycBankOption("Utkal Grameen Bank", null),
    KycBankOption("Uttar Bihar Gramin Bank", null),
    KycBankOption("Uttarakhand Gramin Bank", null),
    KycBankOption("Vidharbha Konkan Gramin Bank", null),
    KycBankOption("Yes Bank", "YESB0")
)

private fun normalizePanInput(input: String): String {
    val raw = input.uppercase(Locale.ENGLISH).filter { it.isLetterOrDigit() }
    val output = StringBuilder()
    raw.forEach { char ->
        when (output.length) {
            in 0..4 -> if (char.isLetter()) output.append(char)
            in 5..8 -> if (char.isDigit()) output.append(char)
            9 -> if (char.isLetter()) output.append(char)
        }
        if (output.length == 10) return@forEach
    }
    return output.toString()
}

private fun createHostKycStorageFile(context: android.content.Context, type: KycCaptureType): File {
    val directory = File(context.filesDir, "host_kyc_uploads").apply { mkdirs() }
    return File(
        directory,
        "${type.storagePrefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
    )
}

private fun deleteKycPhotoFile(imageUri: String) {
    if (imageUri.isBlank()) return
    runCatching {
        val uri = Uri.parse(imageUri)
        if (uri.scheme == "file") {
            uri.path?.takeIf { it.isNotBlank() }?.let { File(it).delete() }
        }
    }
}

private fun validateCapturedKycPhoto(file: File): String? {
    if (!file.exists() || file.length() < 18_000L) {
        return "image was not saved correctly"
    }

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth < 700 || bounds.outHeight < 420) {
        return "low resolution"
    }

    val sampleSize = calculateKycQualitySampleSize(bounds.outWidth, bounds.outHeight)
    val bitmap = BitmapFactory.decodeFile(
        file.absolutePath,
        BitmapFactory.Options().apply { inSampleSize = sampleSize }
    ) ?: return "unable to read photo"

    val qualityIssue = runCatching {
        val metrics = bitmap.kycQualityMetrics()
        when {
            metrics.averageBrightness < 34.0 -> "photo is too dark"
            metrics.averageBrightness > 238.0 -> "photo is overexposed"
            metrics.contrast < 14.0 -> "card text is not clear"
            metrics.edgeScore < 5.0 -> "photo appears blurry"
            else -> null
        }
    }.getOrDefault("photo quality check failed")
    bitmap.recycle()
    return qualityIssue
}

private fun recognizeAadhaarDetailsFromPhoto(
    context: android.content.Context,
    file: File,
    onSuccess: (AadhaarOcrResult) -> Unit,
    onFailure: (String) -> Unit
) {
    runCatching {
        val image = InputImage.fromFilePath(context, Uri.fromFile(file))
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { result ->
                val aadhaar = extractAadhaarDetailsFromText(result.text)
                recognizer.close()
                if (aadhaar == null) {
                    onFailure("Aadhaar name, DOB, or number could not be detected.")
                } else {
                    onSuccess(aadhaar)
                }
            }
            .addOnFailureListener {
                recognizer.close()
                onFailure("Aadhaar scan failed.")
            }
    }.onFailure {
        onFailure("Unable to process Aadhaar photo.")
    }
}

private fun recognizePanNumberFromPhoto(
    context: android.content.Context,
    file: File,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    runCatching {
        val image = InputImage.fromFilePath(context, Uri.fromFile(file))
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { result ->
                val pan = extractPanNumberFromText(result.text)
                recognizer.close()
                if (pan == null) {
                    onFailure("PAN number could not be detected.")
                } else {
                    onSuccess(pan)
                }
            }
            .addOnFailureListener {
                recognizer.close()
                onFailure("PAN scan failed.")
            }
    }.onFailure {
        onFailure("Unable to process PAN photo.")
    }
}

private fun extractAadhaarDetailsFromText(text: String): AadhaarOcrResult? {
    val number = extractAadhaarNumberFromText(text) ?: return null
    val dateOfBirth = extractAadhaarDobFromText(text) ?: return null
    val fullName = extractAadhaarNameFromText(text, dateOfBirth) ?: return null
    return AadhaarOcrResult(
        number = number,
        fullName = fullName,
        dateOfBirth = dateOfBirth
    )
}

private fun extractAadhaarNumberFromText(text: String): String? {
    val normalized = text
        .replace(Regex("[^0-9\\n ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    val groupedMatch = Regex("(?<!\\d)([2-9]\\d{3})\\s?(\\d{4})\\s?(\\d{4})(?!\\d)")
        .find(normalized)
    val candidate = groupedMatch?.groupValues?.drop(1)?.joinToString("").orEmpty()
    if (isValidAadhaarNumber(candidate)) {
        return candidate
    }

    val compactDigits = normalized.onlyDigits()
    return Regex("[2-9]\\d{11}")
        .findAll(compactDigits)
        .map { it.value }
        .firstOrNull { isValidAadhaarNumber(it) }
}

private fun extractAadhaarDobFromText(text: String): String? {
    val normalized = text
        .replace(Regex("(?i)D\\s*O\\s*B"), "DOB")
        .replace(Regex("(?i)Date\\s*of\\s*Birth"), "DOB")
        .replace('-', '/')

    Regex("(?i)(DOB|Birth|जन्म)\\s*[:\\-]?\\s*(\\d{1,2})\\s*/\\s*(\\d{1,2})\\s*/\\s*(\\d{4})")
        .find(normalized)
        ?.let { match ->
            val day = match.groupValues[2].padStart(2, '0')
            val month = match.groupValues[3].padStart(2, '0')
            val year = match.groupValues[4]
            return "$day/$month/$year".takeIf { isValidDateOfBirth(it) }
        }

    Regex("(?<!\\d)(\\d{1,2})\\s*/\\s*(\\d{1,2})\\s*/\\s*(\\d{4})(?!\\d)")
        .find(normalized)
        ?.let { match ->
            val day = match.groupValues[1].padStart(2, '0')
            val month = match.groupValues[2].padStart(2, '0')
            val year = match.groupValues[3]
            return "$day/$month/$year".takeIf { isValidDateOfBirth(it) }
        }

    return null
}

private fun extractAadhaarNameFromText(text: String, dateOfBirth: String): String? {
    val lines = text
        .lineSequence()
        .map { it.replace(Regex("\\s+"), " ").trim() }
        .filter { it.isNotBlank() }
        .toList()

    val dobIndex = lines.indexOfFirst { line ->
        line.contains("DOB", ignoreCase = true) ||
            line.contains("Date of Birth", ignoreCase = true) ||
            line.contains(dateOfBirth)
    }
    if (dobIndex > 0) {
        lines.take(dobIndex)
            .asReversed()
            .mapNotNull { cleanAadhaarNameCandidate(it) }
            .firstOrNull()
            ?.let { return it }
    }

    return lines
        .mapNotNull { cleanAadhaarNameCandidate(it) }
        .firstOrNull()
}

private fun cleanAadhaarNameCandidate(line: String): String? {
    val lower = line.lowercase(Locale.ENGLISH)
    val blocked = listOf(
        "government", "india", "aadhaar", "unique", "identification", "authority",
        "dob", "birth", "male", "female", "mobile", "vid", "address", "year", "govt",
        "भारत", "सरकार", "जन्म", "पुरुष", "महिला"
    )
    if (blocked.any { lower.contains(it) }) return null
    if (line.any { it.isDigit() }) return null
    val cleaned = line
        .replace(Regex("[^A-Za-z .]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
    val words = cleaned.split(" ").filter { it.length >= 2 }
    if (words.size !in 2..5) return null
    if (cleaned.length !in 5..60) return null
    return words.joinToString(" ") { word ->
        word.lowercase(Locale.ENGLISH).replaceFirstChar { it.uppercase(Locale.ENGLISH) }
    }
}

private fun extractPanNumberFromText(text: String): String? {
    val normalized = text.uppercase(Locale.ENGLISH)
    Regex("(?<![A-Z0-9])([A-Z]{5}\\s?[0-9]{4}\\s?[A-Z])(?![A-Z0-9])")
        .find(normalized)
        ?.groupValues
        ?.getOrNull(1)
        ?.filter { it.isLetterOrDigit() }
        ?.takeIf { it.matches(Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")) }
        ?.let { return it }

    val compact = normalized.filter { it.isLetterOrDigit() }
    return Regex("[A-Z]{5}[0-9]{4}[A-Z]")
        .findAll(compact)
        .map { it.value }
        .firstOrNull()
}

private fun String.isKycLocked(): Boolean {
    return when (trim().lowercase(Locale.ENGLISH)) {
        "pending", "submitted", "under_review", "approved" -> true
        else -> false
    }
}

private fun formatKycStatusLabel(status: String): String {
    return when (status.trim().lowercase(Locale.ENGLISH)) {
        "not_submitted" -> "Not submitted"
        "under_review" -> "Pending"
        "submitted", "pending" -> "Pending"
        "approved" -> "Approved"
        "rejected" -> "Rejected"
        else -> status.replace('_', ' ').replaceFirstChar { it.uppercase(Locale.ENGLISH) }
    }
}

private fun calculateKycQualitySampleSize(width: Int, height: Int): Int {
    var sample = 1
    val longest = maxOf(width, height)
    while (longest / sample > 320) {
        sample *= 2
    }
    return sample
}

private fun Bitmap.kycQualityMetrics(): KycPhotoQualityMetrics {
    val step = maxOf(1, minOf(width, height) / 90)
    var count = 0
    var sum = 0.0
    var sumSquares = 0.0
    var edgeSum = 0.0
    var edgeCount = 0
    var previousRowLuma = 0

    var y = 0
    while (y < height) {
        var previousLuma = 0
        var x = 0
        while (x < width) {
            val color = getPixel(x, y)
            val red = (color shr 16) and 0xFF
            val green = (color shr 8) and 0xFF
            val blue = color and 0xFF
            val luma = ((red * 299) + (green * 587) + (blue * 114)) / 1000
            sum += luma
            sumSquares += luma * luma
            if (x > 0) {
                edgeSum += abs(luma - previousLuma)
                edgeCount += 1
            }
            if (y > 0) {
                edgeSum += abs(luma - previousRowLuma)
                edgeCount += 1
            }
            previousLuma = luma
            previousRowLuma = luma
            count += 1
            x += step
        }
        y += step
    }

    val average = if (count > 0) sum / count else 0.0
    val variance = if (count > 0) (sumSquares / count) - (average * average) else 0.0
    val contrast = sqrt(variance.coerceAtLeast(0.0))
    val edgeScore = if (edgeCount > 0) edgeSum / edgeCount else 0.0
    return KycPhotoQualityMetrics(averageBrightness = average, contrast = contrast, edgeScore = edgeScore)
}

private fun encodeKycImageForApi(context: android.content.Context, imageUri: String): String? {
    if (imageUri.isBlank()) return null
    return runCatching {
        val uri = Uri.parse(imageUri)
        context.contentResolver.openInputStream(uri)?.use { input ->
            val original = BitmapFactory.decodeStream(input) ?: return@runCatching null
            val resized = original.scaleDownForKyc()
            val output = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 68, output)
            if (resized !== original) {
                original.recycle()
            }
            val encoded = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
            "data:image/jpeg;base64,$encoded"
        }
    }.getOrNull()
}

private fun Bitmap.scaleDownForKyc(maxSide: Int = 1280): Bitmap {
    val largestSide = maxOf(width, height)
    if (largestSide <= maxSide) return this
    val scale = maxSide.toFloat() / largestSide.toFloat()
    val targetWidth = (width * scale).toInt().coerceAtLeast(1)
    val targetHeight = (height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
}

private fun String.onlyDigits(): String = filter { it.isDigit() }

private enum class KycCaptureType(val storagePrefix: String) {
    AADHAAR("aadhaar"),
    PAN("pan"),
    SELFIE_WITH_AADHAAR("selfie_aadhaar");

    val displayName: String
        get() = when (this) {
            AADHAAR -> "Aadhaar photo"
            PAN -> "PAN photo"
            SELFIE_WITH_AADHAAR -> "Selfie"
        }
}

private data class PendingKycCapture(val type: KycCaptureType, val file: File)
private data class AadhaarOcrResult(
    val number: String,
    val fullName: String,
    val dateOfBirth: String
)
private data class KycPhotoQualityMetrics(
    val averageBrightness: Double,
    val contrast: Double,
    val edgeScore: Double
)

@Composable
private fun HostTransactionsScreen(
    logs: List<HostLog>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .hostContentWidth()
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            RealSaathiBackHeader(title = "Transactions", subtitle = "Host earnings ledger", onBack = onBack)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HostCard {
                        Text("Transactions", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Completed call earnings, payout deductions, and adjustments will appear here.",
                            color = TextSubtle,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
                if (logs.isEmpty()) {
                    item {
                        HostCard {
                            Text("No transactions yet", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Backend wallet ledger is pending. Your completed call logs will show here after host activity.",
                                color = TextSubtle,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    items(logs) { HostLogCard(it) }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HostPage(
    title: String,
    subtitle: String,
    walletBalance: Int,
    totalEarnings: Int,
    onWalletClick: () -> Unit,
    showWalletChip: Boolean = true,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .hostContentWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HostSectionHeader(
                    title = title,
                    subtitle = subtitle,
                    walletBalance = walletBalance,
                    totalEarnings = totalEarnings,
                    onWalletClick = onWalletClick,
                    showWalletChip = showWalletChip
                )
            }
        }
        if (onRefresh != null) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .hostContentWidth()
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 104.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    content = content
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .hostContentWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
        }
    }
}

@Composable
private fun HostSectionHeader(
    title: String,
    subtitle: String,
    walletBalance: Int = 0,
    totalEarnings: Int = 0,
    onWalletClick: () -> Unit = {},
    showWalletChip: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HostHeaderTitleBlock(
                title = title,
                subtitle = subtitle,
                modifier = Modifier.weight(1f)
            )
            if (showWalletChip) {
                HostEarningsChip(
                    walletBalance = walletBalance,
                    onClick = onWalletClick
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun HostHeaderTitleBlock(
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
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 26.sp
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
private fun HostEarningsChip(walletBalance: Int, onClick: () -> Unit) {
    val displayBalance = walletBalance
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "₹",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = NumberFormat.getIntegerInstance(Locale.ENGLISH).format(displayBalance),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HostProfileOverviewCard(
    hostName: String,
    profilePhotoUri: String,
    onEditProfile: () -> Unit
) {
    val context = LocalContext.current
    val displayName = UserPrefs.getNickname(context).trim().ifBlank { hostName }
    val topicTags = UserPrefs.getInterests(context)
    HostCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HostProfilePhotoBubble(
                profilePhotoUri = profilePhotoUri,
                hostName = displayName,
                modifier = Modifier.size(64.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = displayName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (topicTags.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        contentPadding = PaddingValues(top = 4.dp)
                    ) {
                        items(topicTags.take(5)) { topic ->
                            HostTopicChip(topic)
                        }
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit profile",
                tint = Color.White,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onEditProfile() }
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun HostTopicChip(topic: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = topic,
            color = TextSubtle,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HostProfilePhotoBubble(
    profilePhotoUri: String,
    hostName: String,
    modifier: Modifier = Modifier
) {
    val photoBitmap = rememberHostProfilePhotoBitmap(profilePhotoUri)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (photoBitmap != null) {
            Image(
                bitmap = photoBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = hostName.trim().take(1).uppercase(Locale.ENGLISH).ifBlank { "H" },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun rememberHostProfilePhotoBitmap(profilePhotoUri: String): ImageBitmap? {
    val context = LocalContext.current
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = profilePhotoUri) {
        value = loadBitmapFromSource(context, profilePhotoUri)
    }
    return bitmapState.value
}

@Composable
private fun HostProfileSummaryRow(
    totalEarnings: Int,
    language: String,
    onWalletClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HostProfileSummaryTile(
            title = "Wallet",
            value = formatCurrency(totalEarnings),
            modifier = Modifier.weight(1f),
            onClick = onWalletClick
        )
        HostProfileSummaryTile(
            title = "Language",
            value = language,
            modifier = Modifier.weight(1f),
            onClick = null
        )
    }
}

@Composable
private fun HostProfileSummaryTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)?
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier.then(clickableModifier),
        shape = RoundedCornerShape(18.dp),
        color = CardBg
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = TextSubtle,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HostLiveControlCard(
    audioLive: Boolean,
    videoLive: Boolean,
    onAudioToggle: () -> Unit,
    onVideoToggle: () -> Unit
) {
    HostCard {
        Text(
            text = "Live controls",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Choose how users can connect with you.",
            color = TextSubtle,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
        HostLiveControlRow(
            title = "Audio live",
            subtitle = "Accept voice calls",
            checked = audioLive,
            onClick = onAudioToggle
        )
        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
        HostLiveControlRow(
            title = "Video live",
            subtitle = "Accept video calls",
            checked = videoLive,
            onClick = onVideoToggle
        )
    }
}

@Composable
private fun HostLiveControlRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextSubtle,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { onClick() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Accent2.copy(alpha = 0.82f),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = CardBgMuted,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent
            )
        )
    }
}

private suspend fun loadBitmapFromSource(
    context: android.content.Context,
    source: String
): ImageBitmap? = withContext(Dispatchers.IO) {
    if (source.isBlank()) {
        return@withContext null
    }
    runCatching {
        if (source.startsWith("http://") || source.startsWith("https://")) {
            URL(source).openStream().use { input ->
                BitmapFactory.decodeStream(input)?.asImageBitmap()
            }
        } else {
            context.contentResolver.openInputStream(Uri.parse(source))?.use { input ->
                BitmapFactory.decodeStream(input)?.asImageBitmap()
            }
        }
    }.getOrNull()
}

@Composable
private fun HostProfileItem(
    text: String,
    danger: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .clickable { onClick() }
            .padding(18.dp),
        contentAlignment = if (danger) Alignment.Center else Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (danger) DangerRed else Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HostProductFooter() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                runCatching {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.incoteam.in")
                        )
                    )
                }
            }
            .padding(top = 6.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PRODUCT BY",
            color = TextSubtle.copy(alpha = 0.78f),
            fontSize = 10.sp,
            letterSpacing = 1.8.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "INCO TEAM TECHNOLOGY PVT LTD",
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.1.sp
        )
    }
}

@Composable
private fun HostProfileItemWithValue(
    title: String,
    value: String,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardBg)
            .clickable(enabled = enabled) { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = TextSubtle,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (enabled) {
            Text(
                text = "›",
                color = TextSubtle,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun HostStudioCard(
    audioLive: Boolean,
    videoLive: Boolean,
    audioRate: Int,
    videoRate: Int,
    onAudioToggle: (Boolean) -> Unit,
    onVideoToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = CardBg,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Accent1.copy(alpha = 0.14f),
                            Accent2.copy(alpha = 0.11f),
                            CardBg
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(76.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Accent2.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HostStudioModeToggle(
                        title = "Audio",
                        checked = audioLive,
                        onCheckedChange = onAudioToggle
                    )
                    HostStudioModeToggle(
                        title = "Video",
                        checked = videoLive,
                        onCheckedChange = onVideoToggle
                    )
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.09f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HostRateMini(
                        iconPainter = painterResource(id = R.drawable.ic_audio_call_modern),
                        amount = audioRate
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    HostRateMini(
                        iconPainter = painterResource(id = R.drawable.ic_video_call_modern),
                        amount = videoRate
                    )
                }
            }
        }
    }
}

@Composable
private fun HostStudioModeToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(if (checked) Accent2 else Color(0xFFFF4D5D))
            )
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(width = 58.dp, height = 38.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Accent2.copy(alpha = 0.82f),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = CardBgMuted,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun HostRateMini(
    icon: ImageVector,
    amount: Int,
    modifier: Modifier = Modifier
) = HostRateMini(
    iconPainter = rememberVectorPainter(icon),
    amount = amount,
    modifier = modifier
)

@Composable
private fun HostRateMini(
    iconPainter: Painter,
    amount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = "₹$amount/min",
            color = Color.White.copy(alpha = 0.86f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun HostTodayEarningsCard(
    todayEarnings: Int,
    onClick: () -> Unit
) {
    HostCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Today's earnings",
                    color = TextSubtle,
                    fontSize = 13.sp
                )
                Text(
                    text = formatCurrencyDetailed(todayEarnings.toDouble()),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "›",
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 28.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
private fun HostStoryUploadCard(
    onClick: () -> Unit
) {
    HostCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Upload story", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Accent2.copy(alpha = 0.18f))
                    .border(1.dp, Accent2.copy(alpha = 0.34f), RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Light)
            }
        }
    }
}

@Composable
private fun HostUploadAction(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.04f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HostUploadPreviewCard(story: UploadedHostStory) {
    val accent = if (story.mediaType == HostStoryMediaType.VIDEO) Accent2 else Accent1
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.04f),
        modifier = Modifier.width(188.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (story.mediaType == HostStoryMediaType.VIDEO) Icons.Default.Videocam else Icons.Default.Image,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(story.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(
                        if (story.mediaType == HostStoryMediaType.VIDEO) "Video story" else "Photo story",
                        color = TextSubtle,
                        fontSize = 11.sp
                    )
                }
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Accent3, modifier = Modifier.size(16.dp))
            }
            Text(
                story.caption,
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun HostWalletScreen(
    walletBalance: Int,
    totalEarnings: Int,
    todayEarnings: Int,
    withdrawals: List<HostWithdrawalTransaction>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .hostContentWidth()
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            RealSaathiBackHeader(title = "Wallet", subtitle = null, onBack = onBack)
            HorizontalDivider(color = Color.White.copy(alpha = 0.10f))
            Spacer(modifier = Modifier.height(14.dp))
            if (withdrawals.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    HostWalletBalanceCard(
                        walletBalance = walletBalance,
                        totalEarnings = totalEarnings,
                        todayEarnings = todayEarnings
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No transaction history",
                            color = TextSubtle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        HostWalletBalanceCard(
                            walletBalance = walletBalance,
                            totalEarnings = totalEarnings,
                            todayEarnings = todayEarnings
                        )
                    }
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(withdrawals, key = { it.id }) { withdrawal ->
                        HostWithdrawalTransactionCard(withdrawal)
                    }
                }
            }
        }
    }
}

@Composable
private fun HostWalletBalanceCard(walletBalance: Int, totalEarnings: Int, todayEarnings: Int) {
    val displayBalance = walletBalance
    HostCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text("Balance", color = TextSubtle, fontSize = 13.sp)
                Text(
                    formatCurrency(displayBalance),
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                modifier = Modifier
                    .widthIn(min = 146.dp)
                    .align(Alignment.CenterVertically),
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Payout unavailable",
                        color = TextSubtle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun HostWithdrawalTransactionCard(withdrawal: HostWithdrawalTransaction) {
    HostCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    withdrawal.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    withdrawal.detail,
                    color = TextSubtle,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(withdrawal.time, color = Color.White.copy(alpha = 0.54f), fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    withdrawal.amountText,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                HostWithdrawalStatusBadge(withdrawal.status)
            }
        }
    }
}

@Composable
private fun HostWithdrawalStatusBadge(status: HostWithdrawalStatus) {
    val color = when (status) {
        HostWithdrawalStatus.COMPLETE -> Accent2
        HostWithdrawalStatus.PENDING -> Color(0xFFFFC857)
        HostWithdrawalStatus.PROCESSING -> Accent1
        HostWithdrawalStatus.FAILED -> Color(0xFFFF6B6B)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(status.label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HostCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = CardBg, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
private fun HostNoTransactionHistory() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "NO HISTORY",
            color = TextSubtle,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun HostRecentDivider() {
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.10f),
        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
    )
}

@Composable
private fun HostStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    HostCard(modifier = modifier) {
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(label, color = TextSubtle, fontSize = 12.sp)
    }
}

@Composable
private fun HostRecentStatsRail(
    logs: List<HostLog>,
    totalEarnings: Int
) {
    val stats = remember(logs, totalEarnings) {
        buildHostRecentStats(logs, totalEarnings)
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(stats) { stat ->
            HostRecentMiniStatCard(stat)
        }
    }
}

@Composable
private fun HostRecentMiniStatCard(stat: HostRecentStat) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardBg,
        modifier = Modifier
            .width(136.dp)
            .height(92.dp)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.07f),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stat.value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stat.label,
                color = TextSubtle,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun HostRecentHistoryHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(color = Color.White.copy(alpha = 0.10f))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "Activity history",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Audio/video calls, declined calls, missed calls, messages, and earnings are listed below.",
                color = TextSubtle,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun HostLogCard(log: HostLog) {
    HostCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(log.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(hostLogTypeLabel(log), color = TextSubtle, fontSize = 12.sp)
            }
            Text(formatCurrency(log.amount), color = Accent3, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
        Text(log.time, color = TextSubtle, fontSize = 12.sp)
    }
}

@Composable
private fun HostChatScreen(
    threads: androidx.compose.runtime.snapshots.SnapshotStateList<ChatThread>,
    authRepository: AuthRepository,
    sessionManager: SessionManager,
    selectedThreadId: String?,
    onOpenThread: (String) -> Unit,
    onCloseThread: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val selectedIndex = threads.indexOfFirst { it.id == selectedThreadId }

    suspend fun refreshRemoteThreads() {
        val response = authRepository.directChat(
            sessionManager.getAccessToken(),
            DirectChatRequest(action = "threads", conversationId = "threads")
        ).getOrNull() ?: return
        val ownId = sessionManager.getUserId()
        response.threads.forEach { remote ->
            val latest = remote.latestMessage ?: return@forEach
            val message = ChatMessage(
                id = latest.id,
                text = latest.body,
                fromUser = latest.senderId == ownId,
                timestampMillis = runCatching { java.time.Instant.parse(latest.createdAt).toEpochMilli() }
                    .getOrDefault(System.currentTimeMillis())
            )
            val index = threads.indexOfFirst { it.id == remote.participantId }
            val updated = (if (index >= 0) threads[index] else ChatThread(
                id = remote.participantId,
                title = remote.participantName,
                subtitle = message.text,
                unreadCount = if (message.fromUser) 0 else 1,
                lastSeenAtMillis = message.timestampMillis,
                messages = listOf(message)
            )).copy(
                title = remote.participantName,
                subtitle = message.text,
                lastSeenAtMillis = message.timestampMillis,
                messages = (if (index >= 0) threads[index].messages else emptyList())
                    .plus(message).distinctBy { it.id }.takeLast(100)
            )
            if (index >= 0) threads[index] = updated else threads.add(updated)
        }
    }
    LaunchedEffect(Unit) {
        while (isActive) {
            refreshRemoteThreads()
            delay(15_000L)
        }
    }

    suspend fun refreshHistory(threadId: String) {
        val token = sessionManager.getAccessToken().trim()
        if (token.isBlank()) return
        val conversationId = if (sessionManager.isHost()) sessionManager.getUserId() else threadId
        val remote = authRepository.directChat(token, DirectChatRequest(action = "history", conversationId = conversationId)).getOrNull()?.messages ?: return
        val index = threads.indexOfFirst { it.id == threadId }
        if (index < 0 || remote.isEmpty()) return
        val ownId = sessionManager.getUserId()
        val current = threads[index]
        val mapped = remote.map { item ->
            ChatMessage(
                id = item.id,
                text = item.body,
                fromUser = item.senderId == ownId,
                timestampMillis = runCatching { java.time.Instant.parse(item.createdAt).toEpochMilli() }.getOrDefault(System.currentTimeMillis()),
                deliveryStatus = if (item.senderId == ownId) ChatDeliveryStatus.SENT else null
            )
        }
        threads[index] = current.copy(
            subtitle = mapped.lastOrNull()?.text ?: current.subtitle,
            unreadCount = if (selectedThreadId == threadId) 0 else current.unreadCount + mapped.count { !it.fromUser },
            lastSeenAtMillis = mapped.lastOrNull()?.timestampMillis,
            messages = (current.messages + mapped).distinctBy { it.id }.takeLast(100)
        )
    }
    LaunchedEffect(selectedThreadId) {
        val threadId = selectedThreadId ?: return@LaunchedEffect
        while (isActive) {
            refreshHistory(threadId)
            delay(15_000L)
        }
    }

    fun queueIncomingReply(threadId: String, isSupportThread: Boolean) {
        scope.launch {
            delay(if (isSupportThread) 850L else 1050L)
            val repliedAt = System.currentTimeMillis()
            val refreshedIndex = threads.indexOfFirst { it.id == threadId }
            if (refreshedIndex < 0) return@launch
            val refreshed = threads[refreshedIndex]
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
            val reply = if (isSupportThread) {
                "Support team ne note kar liya. Agar urgent ho toh yahin continue karo."
            } else {
                hostAutoReplyTextForThread(refreshed.title)
            }
            val updatedMessages = buildList {
                addAll(seenMessages)
                add(
                    ChatMessage(
                        id = "${refreshed.id}-$repliedAt-incoming",
                        text = reply,
                        fromUser = false,
                        timestampMillis = repliedAt
                    )
                )
            }
            threads[refreshedIndex] = refreshed.copy(
                subtitle = reply,
                unreadCount = if (selectedThreadId == threadId) 0 else refreshed.unreadCount + 1,
                lastSeenAtMillis = repliedAt,
                messages = updatedMessages
            )
        }
    }

    if (selectedIndex >= 0) {
        HostChatConversationScreen(
            thread = threads[selectedIndex],
            onBack = onCloseThread,
            onModerate = { reason ->
                val target = threads[selectedIndex].id
                scope.launch {
                    authRepository.reportUser(
                        sessionManager.getAccessToken(),
                        com.incoteam.realsaathi.data.model.auth.ReportUserRequest(
                            reportedUserId = target,
                            reason = reason.hostReportReasonCode(),
                            context = "chat",
                            note = reason,
                            block = true
                        )
                    ).onSuccess {
                        threads.removeAll { it.id == target }
                        onCloseThread()
                    }.onFailure { Toast.makeText(context, it.message ?: "Report nahi ho paya", Toast.LENGTH_SHORT).show() }
                }
            },
            onSendMessage = { text ->
                val current = threads[selectedIndex]
                val safeText = text.trim()
                if (safeText.isBlank()) return@HostChatConversationScreen false
                val sentAt = System.currentTimeMillis()
                threads[selectedIndex] = current.copy(
                    subtitle = safeText,
                    unreadCount = 0,
                    messages = current.messages + ChatMessage(
                        id = "${current.id}-$sentAt-host",
                        text = safeText,
                        fromUser = true,
                        timestampMillis = sentAt,
                        deliveryStatus = ChatDeliveryStatus.SENT
                    )
                )
                scope.launch {
                    val token = sessionManager.getAccessToken().trim()
                    val response = authRepository.directChat(
                        token,
                        DirectChatRequest(
                            conversationId = sessionManager.getUserId(),
                            recipientId = current.id,
                            message = safeText
                        )
                    ).getOrNull()
                    if (response == null) {
                        Toast.makeText(context, "Message send nahi ho paya. Please try again.", Toast.LENGTH_SHORT).show()
                    } else {
                        val index = threads.indexOfFirst { it.id == current.id }
                        if (index >= 0) {
                            val latest = threads[index]
                            threads[index] = latest.copy(messages = latest.messages.map { item ->
                                if (item.text == safeText && item.fromUser && item.id.endsWith("-host")) item.copy(id = response.message?.id ?: item.id) else item
                            })
                        }
                    }
                }
                true
            }
        )
    } else {
        val orderedThreads = threads.sortedWith(
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
                .background(Brush.verticalGradient(listOf(CardBgMuted, AppBg, AppBg))),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .hostContentWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                HostSectionHeader(
                    title = "Chat",
                    subtitle = "",
                    showWalletChip = false
                )
            }

            Column(
                modifier = Modifier
                    .hostContentWidth()
                    .weight(1f)
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

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredThreads, key = { it.id }) { thread ->
                        HostChatThreadRow(
                            thread = thread,
                            onClick = {
                                val index = threads.indexOfFirst { it.id == thread.id }
                                if (index >= 0) {
                                    threads[index] = threads[index].copy(unreadCount = 0)
                                }
                                onOpenThread(thread.id)
                            }
                        )
                    }
                    if (filteredThreads.isEmpty()) {
                        item { HostEmptyChatState() }
                    }
                }
            }
        }
    }
}

@Composable
private fun HostEmptyChatState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 72.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No chats yet", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Real host chat integration is pending. Demo conversations are hidden.",
                color = TextSubtle,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HostChatThreadRow(
    thread: ChatThread,
    onClick: () -> Unit
) {
    val isSupportThread = thread.id == HostSupportThreadId
    val lastMessageAt = thread.messages.lastOrNull()?.timestampMillis
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSupportThread) {
            HostSupportChatAvatar(size = 52.dp)
        } else {
            HostContactChatAvatar(name = thread.title, size = 52.dp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = if (isSupportThread) Modifier else Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isSupportThread) 4.dp else 6.dp)
            ) {
                Text(
                    text = thread.title,
                    modifier = if (isSupportThread) Modifier.widthIn(max = 170.dp) else Modifier.weight(1f),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (isSupportThread) 14.sp else 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isSupportThread) {
                    HostSupportVerifiedBadge(size = 14.dp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = thread.subtitle,
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
                    text = formatHostListTime(lastMessageAt),
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
                    Text(
                        text = thread.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun HostChatConversationScreen(
    thread: ChatThread,
    onBack: () -> Unit,
    onModerate: (String) -> Unit,
    onSendMessage: (String) -> Boolean
) {
    var showModerationDialog by rememberSaveable(thread.id) { mutableStateOf(false) }
    var message by rememberSaveable(thread.id) { mutableStateOf("") }
    val messageListState = rememberLazyListState()
    val lastOutgoingMessageId = remember(thread.messages) {
        thread.messages.lastOrNull { it.fromUser }?.id
    }
    val lastSeenLabel = if (thread.isPinned) {
        "Official support"
    } else {
        formatHostLastSeen(thread.lastSeenAtMillis ?: thread.messages.lastOrNull()?.timestampMillis)
    }
    val canSend = message.isNotBlank()

    LaunchedEffect(thread.messages.size) {
        if (thread.messages.isNotEmpty()) {
            messageListState.animateScrollToItem(thread.messages.lastIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .hostContentWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HostChatBackButton(onBack = onBack)
                Spacer(Modifier.width(12.dp))
                if (thread.id == HostSupportThreadId) {
                    HostSupportChatAvatar(size = 36.dp)
                } else {
                    HostContactChatAvatar(name = thread.title, size = 36.dp)
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
                            HostSupportVerifiedBadge(size = 15.dp)
                        }
                    }
                    Text(
                        text = lastSeenLabel,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!thread.isPinned) {
                    Text("Block / Report", color = Accent2, fontSize = 11.sp, modifier = Modifier.clickable { showModerationDialog = true })
                }
            }
            Box(
                modifier = Modifier
                    .hostContentWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.06f))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .hostContentWidth()
                    .imePadding()
            ) {
                LazyColumn(
                    state = messageListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom)
                ) {
                    items(thread.messages, key = { "${thread.id}-${it.id}" }) { item ->
                        HostChatBubble(
                            message = item,
                            showStatus = item.id == lastOutgoingMessageId
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
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
                        BasicTextField(
                            value = message,
                            onValueChange = { message = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            cursorBrush = SolidColor(Color.White),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (message.isBlank()) {
                                        Text(
                                            text = "Message...",
                                            color = Color.White.copy(alpha = 0.38f),
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
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
                                if (canSend && onSendMessage(message)) {
                                    message = ""
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
        if (showModerationDialog) {
            BlockReasonDialog(
                onReasonSelected = { reason -> showModerationDialog = false; onModerate(reason) },
                onDismiss = { showModerationDialog = false }
            )
        }
    }
}

private fun String.hostReportReasonCode(): String = when {
    contains("scam", true) || contains("fraud", true) || contains("money", true) -> "scam_fraud"
    contains("harass", true) || contains("abuse", true) -> "harassment"
    contains("fake", true) -> "fake_profile"
    contains("inappropriate", true) -> "inappropriate_content"
    contains("personal", true) -> "personal_information"
    else -> "other"
}

@Composable
private fun HostChatBubble(
    message: ChatMessage,
    showStatus: Boolean = false
) {
    if (isHostEarningNote(message)) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(0.8.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = message.text,
                    color = Accent3,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        return
    }

    val statusText = when {
        !message.fromUser -> null
        message.deliveryStatus == ChatDeliveryStatus.SEEN -> formatHostSeenRelativeTime(message.seenAtMillis)
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
            Text(
                text = message.text,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
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
                text = formatHostCompactRelativeTime(message.timestampMillis),
                color = Color.White.copy(alpha = 0.42f),
                fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun HostSupportVerifiedBadge(size: Dp) {
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
private fun HostSupportChatAvatar(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(size),
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
                    painter = painterResource(id = R.drawable.realsaathi_logo_art),
                    contentDescription = "RealSaathi logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }
    }
}

private const val HostSupportThreadId = "support"
private val HostMaxContentWidth = 360.dp

private fun Modifier.hostContentWidth(): Modifier =
    fillMaxWidth()

@Composable
private fun HostContactChatAvatar(
    name: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFFD1D1D),
                        Color(0xFFFCAF45),
                        Accent2
                    )
                )
            )
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Accent2, CardBgMuted)))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(2).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.28f).sp
            )
        }
    }
}

@Composable
private fun HostChatBackButton(onBack: () -> Unit) {
    IPhoneBackButton(onBack = onBack)
}

private fun isHostEarningNote(message: ChatMessage): Boolean {
    return !message.fromUser && message.text.startsWith("You earned ₹")
}

private fun buildInitialHostChatThreads(now: Long = System.currentTimeMillis()): List<ChatThread> {
    val supportAt = now - (28L * 60L * 1000L)
    val aishaReplyAt = now - (5L * 60L * 1000L)
    val meeraReplyAt = now - (48L * 60L * 1000L)
    val sanaReplyAt = now - (3L * 60L * 60L * 1000L)
    val rheaReplyAt = now - (25L * 60L * 60L * 1000L)

    return listOf(
        ChatThread(
            id = HostSupportThreadId,
            title = "RealSaathi Support Team",
            subtitle = "Hi! RealSaathi support team host side par bhi active hai.",
            isPinned = true,
            unreadCount = 1,
            isOnline = true,
            lastSeenAtMillis = now,
            messages = listOf(
                ChatMessage(
                    id = "support-host-1",
                    text = "Hi! RealSaathi support team host side par bhi active hai. Koi payout ya profile issue ho toh yahin message karna.",
                    fromUser = false,
                    timestampMillis = supportAt
                )
            )
        ),
        ChatThread(
            id = "host-aisha",
            title = "Aisha",
            subtitle = "Main 10 min me phir text karti hoon.",
            unreadCount = 1,
            isOnline = true,
            lastSeenAtMillis = aishaReplyAt,
            messages = listOf(
                ChatMessage(
                    id = "host-aisha-1",
                    text = "Hi Aisha, tumhari call ke baad yahin continue kar sakte hain.",
                    fromUser = true,
                    timestampMillis = aishaReplyAt - (7L * 60L * 1000L),
                    deliveryStatus = ChatDeliveryStatus.SEEN,
                    seenAtMillis = aishaReplyAt - (6L * 60L * 1000L)
                ),
                ChatMessage(
                    id = "host-aisha-2",
                    text = "Main 10 min me phir text karti hoon.",
                    fromUser = false,
                    timestampMillis = aishaReplyAt
                ),
            )
        ),
        ChatThread(
            id = "host-meera",
            title = "Meera",
            subtitle = "Aap online ho toh reply karna.",
            unreadCount = 0,
            isOnline = true,
            lastSeenAtMillis = meeraReplyAt,
            messages = listOf(
                ChatMessage(
                    id = "host-meera-1",
                    text = "Aaj ka session kaisa laga?",
                    fromUser = true,
                    timestampMillis = meeraReplyAt - (14L * 60L * 1000L),
                    deliveryStatus = ChatDeliveryStatus.SEEN,
                    seenAtMillis = meeraReplyAt - (10L * 60L * 1000L)
                ),
                ChatMessage(
                    id = "host-meera-2",
                    text = "Aap online ho toh reply karna.",
                    fromUser = false,
                    timestampMillis = meeraReplyAt
                ),
            )
        ),
        ChatThread(
            id = "host-sana",
            title = "Sana",
            subtitle = "Kal same time par free rahogi?",
            unreadCount = 2,
            isOnline = false,
            lastSeenAtMillis = sanaReplyAt,
            messages = listOf(
                ChatMessage(
                    id = "host-sana-1",
                    text = "Tumhari vibe bahut acchi lagi aaj.",
                    fromUser = true,
                    timestampMillis = sanaReplyAt - (20L * 60L * 1000L),
                    deliveryStatus = ChatDeliveryStatus.SEEN,
                    seenAtMillis = sanaReplyAt - (18L * 60L * 1000L)
                ),
                ChatMessage(
                    id = "host-sana-2",
                    text = "Kal same time par free rahogi?",
                    fromUser = false,
                    timestampMillis = sanaReplyAt
                ),
            )
        ),
        ChatThread(
            id = "host-rhea",
            title = "Rhea",
            subtitle = "Thank you, aaj ka talk genuinely accha tha.",
            unreadCount = 0,
            isOnline = false,
            lastSeenAtMillis = rheaReplyAt,
            messages = listOf(
                ChatMessage(
                    id = "host-rhea-1",
                    text = "Safe reach kar jao toh ping kar dena.",
                    fromUser = true,
                    timestampMillis = rheaReplyAt - (18L * 60L * 1000L),
                    deliveryStatus = ChatDeliveryStatus.SEEN,
                    seenAtMillis = rheaReplyAt - (14L * 60L * 1000L)
                ),
                ChatMessage(
                    id = "host-rhea-2",
                    text = "Thank you, aaj ka talk genuinely accha tha.",
                    fromUser = false,
                    timestampMillis = rheaReplyAt
                ),
            )
        )
    )
}

private fun hostAutoReplyTextForThread(title: String): String {
    return when (title.lowercase(Locale.getDefault())) {
        "aisha" -> "Main abhi yahin hoon, thoda sa time do phir long reply karti hoon."
        "meera" -> "Aapki baat achchi lagi, aur thoda share karo na."
        "sana" -> "Mujhe bhi yeh conversation acchi lag rahi hai."
        "rhea" -> "Main next break me phir se reply karti hoon."
        else -> "Main yahin hoon, continue karte hain."
    }
}

private fun formatHostClockTime(timestampMillis: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestampMillis))
}

private fun formatHostListTime(timestampMillis: Long): String {
    val pattern = if (isSameHostDay(timestampMillis, System.currentTimeMillis())) "h:mm a" else "dd MMM"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestampMillis))
}

private fun formatHostCompactRelativeTime(timestampMillis: Long?): String {
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

private fun formatHostSeenRelativeTime(timestampMillis: Long?): String {
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

private fun formatHostLastSeen(timestampMillis: Long?): String {
    if (timestampMillis == null) return "Last seen recently"
    return when {
        isSameHostDay(timestampMillis, System.currentTimeMillis()) -> "Last seen today at ${formatHostClockTime(timestampMillis)}"
        isHostYesterday(timestampMillis) -> "Last seen yesterday at ${formatHostClockTime(timestampMillis)}"
        else -> {
            val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestampMillis))
            "Last seen $date at ${formatHostClockTime(timestampMillis)}"
        }
    }
}

private fun isSameHostDay(firstMillis: Long, secondMillis: Long): Boolean {
    val first = Calendar.getInstance().apply { timeInMillis = firstMillis }
    val second = Calendar.getInstance().apply { timeInMillis = secondMillis }
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}

private fun isHostYesterday(timestampMillis: Long, nowMillis: Long = System.currentTimeMillis()): Boolean {
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val target = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    return yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
}

@Composable
private fun RowScope.HostBottomItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(24.dp)) {
            Icon(icon, contentDescription = label, tint = if (selected) Color.White else TextSubtle, modifier = Modifier.fillMaxSize())
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = if (selected) Color.White else TextSubtle)
    }
}

private fun createHostStoryUpload(
    ownerId: String,
    ownerName: String,
    mediaUrl: String,
    mediaType: HostStoryMediaType,
    sourceLabel: String
): UploadedHostStory {
    return UploadedHostStory(
        id = "host_story_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
        ownerId = ownerId,
        ownerName = ownerName,
        title = if (mediaType == HostStoryMediaType.VIDEO) "New video story" else "Fresh photo story",
        caption = if (sourceLabel == "camera") {
            "New camera story uploaded for users."
        } else {
            "New gallery story uploaded for users."
        },
        mediaUri = mediaUrl,
        mediaType = mediaType
    )
}

private fun resolveStoryMediaType(context: android.content.Context, uri: Uri): HostStoryMediaType {
    val mimeType = context.contentResolver.getType(uri).orEmpty()
    return if (mimeType.startsWith("video")) HostStoryMediaType.VIDEO else HostStoryMediaType.IMAGE
}

private fun resolveLocalMediaFile(savedUri: String): File? {
    val parsedUri = runCatching { Uri.parse(savedUri) }.getOrNull() ?: return null
    val filePath = parsedUri.path.orEmpty()
    if (parsedUri.scheme != "file" || filePath.isBlank()) return null
    return File(filePath)
}

private fun defaultMimeTypeFor(mediaType: HostStoryMediaType): String {
    return if (mediaType == HostStoryMediaType.VIDEO) "video/mp4" else "image/jpeg"
}

private fun copyStoryMediaToAppStorage(
    context: android.content.Context,
    sourceUri: Uri,
    mediaType: HostStoryMediaType
): String {
    val destination = createHostStoryStorageFile(context, mediaType, "gallery")
    context.contentResolver.openInputStream(sourceUri)?.use { input ->
        destination.outputStream().use { output ->
            input.copyTo(output)
        }
    } ?: error("Unable to read selected media")
    return Uri.fromFile(destination).toString()
}

private fun createHostStoryStorageFile(
    context: android.content.Context,
    mediaType: HostStoryMediaType,
    prefix: String
): File {
    val directory = File(context.filesDir, "host_story_uploads").apply { mkdirs() }
    val extension = if (mediaType == HostStoryMediaType.VIDEO) ".mp4" else ".jpg"
    return File(
        directory,
        "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}$extension"
    )
}

private fun formatCurrency(value: Int): String =
    "₹${NumberFormat.getIntegerInstance(Locale.forLanguageTag("en-IN")).format(value)}"

private fun formatCurrencyDetailed(value: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "₹${formatter.format(value)}"
}

private fun compactCurrency(value: Int): String =
    if (value >= 1000) "${String.format(Locale.ENGLISH, "%.1f", value / 1000f).removeSuffix(".0")}k" else value.toString()

private fun buildHostRecentStats(logs: List<HostLog>, totalEarnings: Int): List<HostRecentStat> {
    val now = System.currentTimeMillis()
    val weekStart = startOfCurrentWeekMillis()
    val monthStart = startOfCurrentMonthMillis()
    val audioCalls = logs.count { it.isAudioCallLog() }
    val videoCalls = logs.count { it.isVideoCallLog() }
    val messageCount = logs
        .filter { it.isMessageLog() }
        .sumOf { it.messageCount.takeIf { count -> count > 0 } ?: parseMessageCount(it.duration) }
    val cancelledCalls = logs.count { it.isCancelledCallLog() }
    val missedCalls = logs.count { it.isMissedCallLog() }
    val weeklyEarning = logs
        .filter { it.createdAtMillis()?.let { created -> created in weekStart..now } == true }
        .sumOf { it.amount }
    val monthlyEarning = logs
        .filter { it.createdAtMillis()?.let { created -> created in monthStart..now } == true }
        .sumOf { it.amount }
    return listOf(
        HostRecentStat("Audio Calls", audioCalls.toString()),
        HostRecentStat("Video Calls", videoCalls.toString()),
        HostRecentStat("Messages", messageCount.toString()),
        HostRecentStat("Cancelled Calls", cancelledCalls.toString()),
        HostRecentStat("Missed Calls", missedCalls.toString()),
        HostRecentStat("Weekly Earning", formatCurrency(weeklyEarning)),
        HostRecentStat("Monthly Earning", formatCurrency(monthlyEarning)),
        HostRecentStat("Lifetime Income", formatCurrency(totalEarnings))
    )
}

private fun hostLogTypeLabel(log: HostLog): String {
    return when {
        log.isCancelledCallLog() -> "Cancelled call • ${log.duration}"
        log.isMissedCallLog() -> "Missed call • ${log.duration}"
        log.isMessageLog() -> "Message • ${log.duration}"
        log.isVideoCallLog() -> "Video call • ${log.duration}"
        log.isAudioCallLog() -> "Audio call • ${log.duration}"
        else -> "Earning • ${log.duration}"
    }
}

private fun formatCallMinutes(seconds: Long): String {
    if (seconds <= 0L) return "0 min"
    val minutes = ((seconds + 59L) / 60L).coerceAtLeast(1L)
    return "$minutes min"
}

private fun parseMessageCount(duration: String): Int {
    return Regex("(\\d+)\\s*msg", RegexOption.IGNORE_CASE)
        .find(duration)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?: 0
}

private fun HostLog.isMessageLog(): Boolean {
    return kind.equals("chat", ignoreCase = true) || messageCount > 0 || duration.contains("msg", ignoreCase = true)
}

private fun HostLog.isVideoCallLog(): Boolean {
    return !isCancelledCallLog() && !isMissedCallLog() &&
        (kind.equals("video_call", ignoreCase = true) || (isVideo && durationSeconds > 0L))
}

private fun HostLog.isAudioCallLog(): Boolean {
    return !isCancelledCallLog() && !isMissedCallLog() &&
        (kind.equals("audio_call", ignoreCase = true) || (!isVideo && durationSeconds > 0L && !isMessageLog()))
}

private fun HostLog.isCancelledCallLog(): Boolean {
    return callStatus.equals("declined", true) || callStatus.equals("canceled", true) || callStatus.equals("cancelled", true) ||
        matchesCallStatus("cancel", "cancelled", "canceled")
}

private fun HostLog.isMissedCallLog(): Boolean {
    return callStatus.equals("missed", true) || matchesCallStatus("missed", "miss call", "misscall")
}

private fun HostLog.matchesCallStatus(vararg keywords: String): Boolean {
    val normalized = listOf(kind, duration, name)
        .joinToString(" ")
        .lowercase(Locale.ENGLISH)
    return keywords.any { normalized.contains(it) }
}

private fun HostLog.createdAtMillis(): Long? {
    if (createdAt.isBlank()) return null
    return runCatching {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX"
        )
        formats.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                    isLenient = false
                }.parse(createdAt)?.time
            }.getOrNull()
        }
    }.getOrNull()
}

private fun RemoteWalletTransaction.toHostWithdrawalTransaction(): HostWithdrawalTransaction? {
    if (!isHostWithdrawalTransaction()) return null
    val rupees = abs(rupeesDelta.takeIf { it != 0 } ?: rechargeAmountRupees)
    val amount = if (amountText.isNotBlank() && amountText != "0") {
        amountText
    } else {
        "-${formatCurrency(rupees)}"
    }
    return HostWithdrawalTransaction(
        id = id.orEmpty().ifBlank { "withdrawal_${createdAt.orEmpty()}_${title.hashCode()}" },
        title = title.ifBlank { "Bank withdrawal" },
        detail = detail.ifBlank { "Transfer to registered bank account" },
        amountText = amount,
        status = status.toHostWithdrawalStatus(),
        time = createdAt.toHostWalletTimeLabel()
    )
}

private fun RemoteWalletTransaction.isHostWithdrawalTransaction(): Boolean {
    val normalized = listOf(kind, title, detail, amountText, status)
        .joinToString(" ")
        .lowercase(Locale.ENGLISH)
    return kind.equals("withdrawal", ignoreCase = true) ||
        kind.equals("payout", ignoreCase = true) ||
        normalized.contains("withdraw") ||
        normalized.contains("payout") ||
        normalized.contains("bank transfer") ||
        normalized.contains("transfer to bank") ||
        (kind.equals("payment", ignoreCase = true) && rupeesDelta < 0)
}

private fun String.toHostWithdrawalStatus(): HostWithdrawalStatus {
    return when (lowercase(Locale.ENGLISH)) {
        "completed", "complete", "success", "successful" -> HostWithdrawalStatus.COMPLETE
        "processing", "in_process", "under_process" -> HostWithdrawalStatus.PROCESSING
        "failed", "rejected", "declined" -> HostWithdrawalStatus.FAILED
        else -> HostWithdrawalStatus.PENDING
    }
}

private fun String?.toHostWalletTimeLabel(): String {
    val timestamp = this.toHostWalletTimestampMillis()
    return if (timestamp > 0L) {
        SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault()).format(Date(timestamp))
    } else {
        "Recently"
    }
}

private fun String?.toHostWalletTimestampMillis(): Long {
    val raw = this?.takeIf { it.isNotBlank() } ?: return 0L
    val normalized = raw.replace(Regex("\\.(\\d{3})\\d+"), ".$1")
    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX"
    )
    return formats.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.parse(normalized)?.time
        }.getOrNull()
    } ?: 0L
}

private fun startOfCurrentWeekMillis(): Long {
    return Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun startOfCurrentMonthMillis(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun isoNow(): String {
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }.format(Date())
}

private data class HostLog(
    val name: String,
    val kind: String,
    val isVideo: Boolean,
    val duration: String,
    val durationSeconds: Long,
    val messageCount: Int,
    val amount: Int,
    val time: String,
    val createdAt: String,
    val callStatus: String
)
private data class HostWithdrawalTransaction(
    val id: String,
    val title: String,
    val detail: String,
    val amountText: String,
    val status: HostWithdrawalStatus,
    val time: String
)
private enum class HostWithdrawalStatus(val label: String) {
    COMPLETE("Complete"),
    PENDING("Pending"),
    PROCESSING("Processing"),
    FAILED("Failed")
}
private data class HostRecentStat(val label: String, val value: String)
private data class PendingHostCapture(val file: File, val mediaType: HostStoryMediaType)
