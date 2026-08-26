@file:OptIn(ExperimentalFoundationApi::class)

package com.incoteam.frndzz.ui.home

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.util.lerp
import com.incoteam.frndzz.FrndzzApp
import com.incoteam.frndzz.R
import com.incoteam.frndzz.core.session.SessionManager
import com.incoteam.frndzz.data.model.auth.SaveProfileRequest
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

@Composable
fun ProfileScreen(
    sessionManager: SessionManager,
    blockedUsers: List<BlockedProfileUser>,
    forceEditProfileOnLaunch: Boolean = false,
    onUnblockUser: (String) -> Unit,
    onOpenWallet: () -> Unit,
    onOpenTransactions: () -> Unit,
    onInnerNavigate: () -> Unit,
    onInnerBack: () -> Unit,
    onLanguageAppliedToHome: () -> Unit,
    onSwitchToListenerMode: () -> Unit,
    onProfileUpdated: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as FrndzzApp }
    val authRepository = remember(app) { app.authRepository }
    val scope = rememberCoroutineScope()
    var screen by remember {
        mutableStateOf<ProfileRoute>(
            ProfileRoute.Profile
        )
    }
    var profileRefresh by rememberSaveable { mutableStateOf(0) }
    var switchingToListenerMode by remember { mutableStateOf(false) }

    fun switchToListenerMode() {
        if (switchingToListenerMode) return

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
        if (!gender.equals("Female", ignoreCase = true)) {
            toast(context, "Listener mode abhi sirf female profiles ke liye available hai.")
            return
        }
        if (UserPrefs.getAccountMode(context).equals("community", ignoreCase = true)) {
            toast(context, "Listener mode already active hai.")
            onSwitchToListenerMode()
            return
        }

        val preferredLanguage = UserPrefs.getLanguage(context)
            .takeIf { it.isNotBlank() && !it.equals("All", ignoreCase = true) }
            ?: "Hindi"
        val topicTags = UserPrefs.getInterests(context)
        val nativeLanguages = listOf(preferredLanguage)
        val communityName = UserPrefs.getCommunityName(context).trim().ifBlank { nickname }
        val communityCity = UserPrefs.getCommunityCity(context).trim().ifBlank { "India" }
        val communityAbout = UserPrefs.getCommunityAbout(context).trim()
            .ifBlank { "$nickname is available for live conversations on Frndzz." }
        val communityExperience = UserPrefs.getCommunityExperience(context).trim()
            .ifBlank { "New listener on Frndzz." }

        switchingToListenerMode = true
        scope.launch {
            authRepository.saveProfile(
                accessToken = accessToken,
                request = SaveProfileRequest(
                    nickname = nickname,
                    gender = gender,
                    preferredLanguage = preferredLanguage,
                    avatarId = UserPrefs.getAvatar(context).takeIf { it > 0 } ?: DefaultAvatarId,
                    interests = topicTags,
                    accountMode = "community",
                    topicTags = topicTags,
                    nativeLanguages = nativeLanguages,
                    communityName = communityName,
                    communityCity = communityCity,
                    communityAbout = communityAbout,
                    communityExperience = communityExperience
                )
            ).onSuccess { response ->
                UserPrefs.syncFromRemoteProfile(context, response.profile)
                UserPrefs.saveCommunityDetails(
                    ctx = context,
                    name = response.profile.communityName.orEmpty(),
                    city = response.profile.communityCity.orEmpty(),
                    about = response.profile.communityAbout.orEmpty(),
                    experience = response.profile.communityExperience.orEmpty()
                )
                sessionManager.updateUserSession(response.user)
                switchingToListenerMode = false
                toast(context, "Listener mode active ho gaya.")
                onSwitchToListenerMode()
            }.onFailure { throwable ->
                switchingToListenerMode = false
                toast(context, throwable.message ?: "Listener mode switch nahi ho paya")
            }
        }
    }

    BackHandler(enabled = switchingToListenerMode) {}

    if (switchingToListenerMode) {
        ProfileActionStateScreen(
            title = "Activating listener mode",
            subtitle = "Profile sync ho rahi hai. Thoda sa wait karo, listener side khul rahi hai."
        )
        return
    }

    BackHandler(enabled = screen != ProfileRoute.Profile) {
        screen = ProfileRoute.Profile
        onInnerBack()
    }

    when (screen) {
        ProfileRoute.Profile -> key(profileRefresh) {
            ProfileHome(
                sessionManager = sessionManager,
                onEditProfile = {
                    screen = ProfileRoute.EditProfile
                    onInnerNavigate()
                },
                onWallet = {
                    onOpenWallet()
                },
                onLanguage = {
                    screen = ProfileRoute.Language
                    onInnerNavigate()
                },
                switchingToListenerMode = switchingToListenerMode,
                onSwitchToListener = {
                    if (!switchingToListenerMode) {
                        switchToListenerMode()
                    }
                },
                onTransactions = {
                    screen = ProfileRoute.Transactions
                    onInnerNavigate()
                },
                onSettings = {
                    screen = ProfileRoute.Settings
                    onInnerNavigate()
                },
                onHelp = {
                    screen = ProfileRoute.Help
                    onInnerNavigate()
                },
                onAbout = {
                    screen = ProfileRoute.About
                    onInnerNavigate()
                },
                onLogout = onLogout
            )
        }

        ProfileRoute.EditProfile -> EditProfileScreen(
            onBack = {
                profileRefresh++
                screen = ProfileRoute.Profile
                onInnerBack()
            },
            onProfileUpdated = onProfileUpdated,
            isSetupFlow = false
        )
        ProfileRoute.Wallet -> {
            LaunchedEffect(Unit) {
                onOpenWallet()
                screen = ProfileRoute.Profile
            }
        }
        ProfileRoute.Language -> LanguageScreen(
            onBack = {
                profileRefresh++
                screen = ProfileRoute.Profile
                onInnerBack()
            },
            onLanguageApplied = {
                profileRefresh++
                screen = ProfileRoute.Profile
                onLanguageAppliedToHome()
            }
        )
        ProfileRoute.Transactions -> {
            TransactionsScreen(
                title = "Transactions",
                subtitle = "Coins, call charges, refunds, and rewards will appear here.",
                onBack = {
                    screen = ProfileRoute.Profile
                    onInnerBack()
                }
            )
        }
        ProfileRoute.Settings -> AccountSettings(
            onBack = {
                screen = ProfileRoute.Profile
                onInnerBack()
            },
            onBlockedUsers = {
                screen = ProfileRoute.Blocked
                onInnerNavigate()
            },
            onFeedback = {
                screen = ProfileRoute.Feedback
                onInnerNavigate()
            },
            onReports = {
                screen = ProfileRoute.Reports
                onInnerNavigate()
            },
            onDeleteAccount = {
                screen = ProfileRoute.DeleteAccount
                onInnerNavigate()
            }
        )
        ProfileRoute.Help -> HelpSupportScreen(
            sessionManager = sessionManager,
            onBack = {
                screen = ProfileRoute.Profile
                onInnerBack()
            },
            onFaqs = {
                screen = ProfileRoute.Faqs
                onInnerNavigate()
            }
        )
        ProfileRoute.About -> AboutUs {
            screen = ProfileRoute.Profile
            onInnerBack()
        }
        ProfileRoute.Blocked -> BlockedUsersScreen(
            blockedUsers = blockedUsers,
            onUnblockUser = onUnblockUser,
            onBack = { screen = ProfileRoute.Settings }
        )
        ProfileRoute.Faqs -> FaqScreen { screen = ProfileRoute.Help }
        ProfileRoute.Feedback -> FeedbackScreen(
            sessionManager = sessionManager,
            onBack = { screen = ProfileRoute.Settings }
        )
        ProfileRoute.Reports -> ReportsScreen(
            sessionManager = sessionManager,
            onBack = { screen = ProfileRoute.Settings }
        )
        ProfileRoute.DeleteAccount -> DeleteAccountScreen(
            onBack = { screen = ProfileRoute.Settings },
            onConfirmDelete = onLogout
        )
    }
}

@Composable
private fun FeedbackScreen(
    sessionManager: SessionManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val nickname = safeProfileValue(UserPrefs.getNickname(context), "frndzzz_user")
    val phone = safeProfileValue(sessionManager.getPhoneNumber(), "Not added")
    val publicId = UserPrefs.resolvePublicId(
        context,
        sessionManager.getPublicId()
    )
    val categories = remember {
        listOf("App experience", "Call quality", "Wallet", "Profile", "Suggestion")
    }
    var selectedCategory by rememberSaveable { mutableStateOf(categories.first()) }
    var feedbackText by rememberSaveable { mutableStateOf("") }
    val trimmedFeedback = feedbackText.trim()
    val minimumFeedbackLength = 12
    val isFeedbackValid = trimmedFeedback.length >= minimumFeedbackLength

    val sendFeedbackOnWhatsApp = {
        if (!isFeedbackValid) {
            toast(context, "Please write proper feedback")
        } else {
            val message = buildFeedbackSupportMessage(
                username = nickname,
                phone = phone,
                category = selectedCategory,
                feedback = trimmedFeedback
            )
            safeOpenUri(
                context,
                Uri.parse("https://wa.me/918446916214?text=${Uri.encode(message)}"),
                "WhatsApp not available"
            )
        }
    }

    val sendFeedbackOnEmail = {
        if (!isFeedbackValid) {
            toast(context, "Please write proper feedback")
        } else {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@frndzz.in")
                putExtra(Intent.EXTRA_SUBJECT, "Frndzzz Feedback • $selectedCategory")
                putExtra(
                    Intent.EXTRA_TEXT,
                    buildFeedbackSupportMessage(
                        username = nickname,
                        phone = phone,
                        category = selectedCategory,
                        feedback = trimmedFeedback
                    )
                )
            }
            safeStartActivity(context, intent, "No email app found")
        }
    }

    ProfilePageScaffold(title = "Feedback", onBack = onBack) {
        Spacer(Modifier.height(10.dp))
        SupportHeroCard(
            title = "Share your feedback",
            subtitle = "Tell us what feels smooth, broken, or worth improving so we can make Frndzzz better."
        )
        Spacer(Modifier.height(16.dp))
        SupportInfoCard(
            title = "Support details attached",
            rows = listOf(
                "Nickname" to nickname,
                "ID" to publicId,
                "Phone" to phone,
                "Category" to selectedCategory
            )
        )
        Spacer(Modifier.height(18.dp))
        SupportSectionLabel("Category")
        SupportChipWrap {
            categories.forEach { category ->
                SupportChoiceChip(
                    label = category,
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category }
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        SupportSectionLabel("Write feedback")
        OutlinedTextField(
            value = feedbackText,
            onValueChange = { if (it.length <= 400) feedbackText = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            maxLines = 6,
            placeholder = { Text("Tell us what you liked or what needs fixing", color = TextSubtle) },
            isError = feedbackText.isNotBlank() && !isFeedbackValid,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent2,
                unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                errorBorderColor = DangerRed,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                errorTextColor = Color.White,
                cursorColor = Accent2
            ),
            shape = RoundedCornerShape(18.dp)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (feedbackText.isBlank() || isFeedbackValid) {
                    "Support reads this message exactly as you write it."
                } else {
                    "Please add at least $minimumFeedbackLength characters."
                },
                color = if (feedbackText.isBlank() || isFeedbackValid) TextSubtle else DangerRed,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${feedbackText.length}/400",
                color = TextSubtle,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(18.dp))
        SupportSubmitRow(
            primaryText = "Send on WhatsApp",
            secondaryText = "Send via email",
            enabled = isFeedbackValid,
            onPrimaryClick = sendFeedbackOnWhatsApp,
            onSecondaryClick = sendFeedbackOnEmail
        )
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                "Your nickname and phone are added automatically so support can reply faster.",
                color = TextSubtle,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SupportSectionLabel(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun SupportInfoCard(
    title: String,
    rows: List<Pair<String, String>>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        CardBgMuted.copy(alpha = 0.96f),
                        CardBg.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.first,
                        color = TextSubtle,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = row.second,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End
                    )
                }
                if (index != rows.lastIndex) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.06f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportsScreen(
    sessionManager: SessionManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val nickname = safeProfileValue(UserPrefs.getNickname(context), "frndzzz_user")
    val phone = safeProfileValue(sessionManager.getPhoneNumber(), "Not added")
    val publicId = UserPrefs.resolvePublicId(
        context,
        sessionManager.getPublicId()
    )
    val reportTypes = remember {
        listOf("Host behaviour", "Fake profile", "Payment issue", "Chat issue", "Call issue", "Other")
    }
    var selectedType by rememberSaveable { mutableStateOf(reportTypes.first()) }
    var reportedProfile by rememberSaveable { mutableStateOf("") }
    var reportDetails by rememberSaveable { mutableStateOf("") }

    val submitReportEmail = {
        if (reportDetails.trim().length < 10) {
            toast(context, "Please add full report details")
        } else {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@frndzz.in")
                putExtra(Intent.EXTRA_SUBJECT, "Frndzzz Report • $selectedType")
                putExtra(
                    Intent.EXTRA_TEXT,
                    buildUserReportMessage(
                        username = nickname,
                        phone = phone,
                        reportType = selectedType,
                        reportedProfile = reportedProfile.trim(),
                        details = reportDetails.trim()
                    )
                )
            }
            safeStartActivity(context, intent, "No email app found")
        }
    }

    val submitReportWhatsApp = {
        if (reportDetails.trim().length < 10) {
            toast(context, "Please add full report details")
        } else {
            val message = buildUserReportMessage(
                username = nickname,
                phone = phone,
                reportType = selectedType,
                reportedProfile = reportedProfile.trim(),
                details = reportDetails.trim()
            )
            safeOpenUri(
                context,
                Uri.parse("https://wa.me/918446916214?text=${Uri.encode(message)}"),
                "WhatsApp not available"
            )
        }
    }

    ProfilePageScaffold(title = "Reports", onBack = onBack) {
        Spacer(Modifier.height(10.dp))
        Spacer(Modifier.height(20.dp))
        SupportHeroCard(
            title = "Submit a report",
            subtitle = "Share the issue clearly so the support team can review it faster."
        )
        Spacer(Modifier.height(16.dp))
        SupportInfoCard(
            title = "Support details attached",
            rows = listOf(
                "Nickname" to nickname,
                "ID" to publicId,
                "Phone" to phone
            )
        )
        Spacer(Modifier.height(18.dp))
        Text("Report type", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        SupportChipWrap {
            reportTypes.forEach { reportType ->
                SupportChoiceChip(
                    label = reportType,
                    selected = selectedType == reportType,
                    onClick = { selectedType = reportType }
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Profile or issue name", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = reportedProfile,
            onValueChange = { if (it.length <= 60) reportedProfile = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Example: Aisha or payment recharge", color = TextSubtle) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent2,
                unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Accent2
            ),
            shape = RoundedCornerShape(18.dp)
        )
        Spacer(Modifier.height(18.dp))
        Text("Report details", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = reportDetails,
            onValueChange = { if (it.length <= 500) reportDetails = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            maxLines = 7,
            placeholder = { Text("What happened, when it happened, and what support should check", color = TextSubtle) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent2,
                unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Accent2
            ),
            shape = RoundedCornerShape(18.dp)
        )
        Spacer(Modifier.height(18.dp))
        SupportSubmitRow(
            primaryText = "Report on WhatsApp",
            secondaryText = "Report via email",
            onPrimaryClick = submitReportWhatsApp,
            onSecondaryClick = submitReportEmail
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Support gets your nickname, Frndzz ID, and phone automatically with the report.",
            color = TextSubtle,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun DeleteAccountScreen(onBack: () -> Unit, onConfirmDelete: () -> Unit) {
    val reasons = listOf(
        "Call quality was poor",
        "The conversation felt uncomfortable",
        "I found a better app",
        "The experience was not useful",
        "I have privacy concerns",
        "Other"
    )
    var selectedReason by rememberSaveable { mutableStateOf<String?>(null) }
    var showConfirmSheet by rememberSaveable { mutableStateOf(false) }
    var deletePhase by rememberSaveable { mutableStateOf("idle") }

    BackHandler(enabled = deletePhase == "idle") { onBack() }

    LaunchedEffect(deletePhase) {
        if (deletePhase == "processing") {
            delay(900)
            deletePhase = "scheduled"
            delay(1450)
            onConfirmDelete()
        }
    }

    if (deletePhase != "idle") {
        DeleteAccountStatusScreen(
            title = if (deletePhase == "processing") "Scheduling deletion" else "Account deletion requested",
            subtitle = if (deletePhase == "processing") {
                "We are preparing your Frndzzz account deletion request."
            } else {
                "Your account will be deleted within 30 days, and all your data will be removed. Logging you out now."
            },
            success = deletePhase == "scheduled"
        )
        return
    }

    Box(Modifier.fillMaxSize()) {
        ProfilePageScaffold(title = "Delete Account", onBack = onBack) {
            Spacer(Modifier.height(10.dp))
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF53121E),
                                Color(0xFF25070E)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = DangerRed.copy(alpha = 0.26f),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Text(
                        text = "Delete your Frndzzz account",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Once requested, your account will be scheduled for deletion and permanently removed within 30 days.",
                        color = Color.White.copy(alpha = 0.86f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                    DeleteAccountInfoRow("You will be logged out from this device.")
                    DeleteAccountInfoRow("Your chats, calls, wallet history, and profile data will be removed.")
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Why are you leaving?", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            SupportChipWrap {
                reasons.forEach { reason ->
                    SupportChoiceChip(
                        label = reason,
                        selected = selectedReason == reason,
                        onClick = { selectedReason = reason }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { showConfirmSheet = true },
                enabled = selectedReason != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRed,
                    disabledContainerColor = DangerRed.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.72f)
                )
            ) {
                Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(20.dp))
        }

        if (showConfirmSheet && selectedReason != null) {
            DeleteAccountConfirmSheet(
                reason = selectedReason.orEmpty(),
                onCancel = { showConfirmSheet = false },
                onConfirm = {
                    showConfirmSheet = false
                    deletePhase = "processing"
                }
            )
        }
    }
}

@Composable
private fun DeleteAccountInfoRow(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(DangerRed.copy(alpha = 0.9f))
        )
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun DeleteAccountConfirmSheet(
    reason: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onCancel() }
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            CardBgMuted.copy(alpha = 0.99f),
                            AppBg.copy(alpha = 0.99f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                )
                .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.18f))
            )
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(DangerRed.copy(alpha = 0.16f))
                    .border(1.dp, DangerRed.copy(alpha = 0.34f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Text(
                text = "Confirm account deletion",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selected reason",
                color = TextSubtle,
                fontSize = 11.sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(reason, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Text(
                text = "After confirming, your Frndzzz account will be scheduled for deletion and you will be logged out immediately.",
                color = TextSubtle,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
            ) {
                Text("Delete account", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "Cancel",
                color = TextSubtle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onCancel() }
            )
        }
    }
}

@Composable
private fun DeleteAccountStatusScreen(
    title: String,
    subtitle: String,
    success: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(CardBgMuted, AppBg, AppBg)))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            CardBgMuted.copy(alpha = 0.98f),
                            CardBg.copy(alpha = 0.98f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(30.dp))
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(if (success) DangerRed.copy(alpha = 0.18f) else Accent2.copy(alpha = 0.16f))
                        .border(
                            1.dp,
                            if (success) DangerRed.copy(alpha = 0.42f) else Accent2.copy(alpha = 0.42f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (success) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(34.dp))
                    } else {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = subtitle,
                    color = TextSubtle,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ProfileHome(
    sessionManager: SessionManager,
    onEditProfile: () -> Unit,
    onWallet: () -> Unit,
    onLanguage: () -> Unit,
    switchingToListenerMode: Boolean,
    onSwitchToListener: () -> Unit,
    onTransactions: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onAbout: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val nickname = safeProfileValue(UserPrefs.getNickname(context), "frndzzz_user")
    val phone = safeProfileValue(sessionManager.getPhoneNumber(), "Not added")
    val publicId = UserPrefs.resolvePublicId(
        context,
        sessionManager.getPublicId()
    )
    val displayPublicId = publicId.takeIf(::isValidPublicIdValue)
    val avatar = avatarList.firstOrNull { it.id == UserPrefs.getAvatar(context) } ?: avatarList.first()
    Column(Modifier.fillMaxSize().background(AppBg).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Spacer(modifier = Modifier.statusBarsPadding())
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardBg)
                .padding(horizontal = 18.dp, vertical = 20.dp)
        ) {
            AvatarBubble(avatar = avatar, size = 64.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nickname, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                displayPublicId?.let {
                    Text(
                        text = "ID - $it",
                        color = TextSubtle,
                        fontSize = 11.sp
                    )
                }
            }
            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.clickable { onEditProfile() })
        }

        Spacer(Modifier.height(28.dp))
        ProfileItem("Wallet") { onWallet() }
        Spacer(Modifier.height(12.dp))
        ProfileItem("Transactions") { onTransactions() }
        Spacer(Modifier.height(12.dp))
        ProfileItem("Account Settings") { onSettings() }
        Spacer(Modifier.height(12.dp))
        ProfileItem("Help & Support") { onHelp() }
        Spacer(Modifier.height(28.dp))
        ProfileItem(text = "Logout", danger = true) { onLogout() }
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    safeOpenUri(
                        context,
                        Uri.parse("https://www.incoteam.in"),
                        "Unable to open website"
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "PRODUCT BY",
                color = TextSubtle.copy(alpha = 0.78f),
                fontSize = 10.sp,
                letterSpacing = 1.8.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "INCO TEAM TECHNOLOGY PVT LTD",
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.1.sp
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SupportHeroCard(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        CardBgMuted.copy(alpha = 0.96f),
                        CardBg.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSubtle, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun SupportChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Accent2.copy(alpha = 0.20f) else CardBg)
            .border(
                width = 1.dp,
                color = if (selected) Accent2.copy(alpha = 0.80f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(999.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.White else TextSubtle,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun SupportSubmitRow(
    primaryText: String,
    secondaryText: String,
    enabled: Boolean = true,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onPrimaryClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent1,
                disabledContainerColor = Accent1.copy(alpha = 0.34f),
                disabledContentColor = Color.White.copy(alpha = 0.72f)
            )
        ) {
            Text(primaryText, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        TextButton(
            onClick = onSecondaryClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                secondaryText,
                color = if (enabled) Color.White.copy(alpha = 0.92f) else TextSubtle.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SupportChipWrap(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 10.dp,
    verticalSpacing: Dp = 10.dp,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val horizontalSpacingPx = with(density) { horizontalSpacing.roundToPx() }
    val verticalSpacingPx = with(density) { verticalSpacing.roundToPx() }

    Layout(
        modifier = modifier.fillMaxWidth(),
        content = content
    ) { measurables, constraints ->
        val maxWidth = constraints.maxWidth
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        val positions = mutableListOf<Pair<Int, Int>>()
        var xPosition = 0
        var yPosition = 0
        var rowHeight = 0
        var layoutWidth = 0

        placeables.forEach { placeable ->
            if (xPosition > 0 && xPosition + placeable.width > maxWidth) {
                xPosition = 0
                yPosition += rowHeight + verticalSpacingPx
                rowHeight = 0
            }

            positions += xPosition to yPosition
            layoutWidth = maxOf(layoutWidth, xPosition + placeable.width)
            xPosition += placeable.width + horizontalSpacingPx
            rowHeight = maxOf(rowHeight, placeable.height)
        }

        val layoutHeight = if (placeables.isEmpty()) 0 else yPosition + rowHeight

        layout(
            width = layoutWidth.coerceIn(constraints.minWidth, constraints.maxWidth),
            height = layoutHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
        ) {
            placeables.forEachIndexed { index, placeable ->
                val (x, y) = positions[index]
                placeable.placeRelative(x, y)
            }
        }
    }
}

private fun buildFeedbackSupportMessage(
    username: String,
    phone: String,
    category: String,
    feedback: String
): String = """
    Hello Frndzzz Support,
    
    Feedback category: $category
    Nickname: $username
    Phone: $phone
    
    Feedback:
    $feedback
""".trimIndent()

private fun buildUserReportMessage(
    username: String,
    phone: String,
    reportType: String,
    reportedProfile: String,
    details: String
): String = """
    Hello Frndzzz Support,
    
    Report type: $reportType
    Nickname: $username
    Phone: $phone
    Profile or issue: ${reportedProfile.ifBlank { "Not provided" }}
    
    Report details:
    $details
""".trimIndent()

private fun safeProfileValue(value: String?, fallback: String): String {
    return value?.trim()?.takeIf { it.isNotEmpty() } ?: fallback
}

@Composable
private fun HelpSupportScreen(
    sessionManager: SessionManager,
    onBack: () -> Unit,
    onFaqs: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val nickname = safeProfileValue(UserPrefs.getNickname(context), "frndzzz_user")
    val phone = safeProfileValue(sessionManager.getPhoneNumber(), "Not added")
    val message = """
        Hello Frndzzz Support,
        Nickname: $nickname
        Phone: $phone

        I need help with...
    """.trimIndent()

    ProfilePageScaffold(title = "Help & Support", onBack = onBack) {
        Spacer(Modifier.height(10.dp))
        Spacer(Modifier.height(20.dp))
        ProfileItem("WhatsApp Support") {
            safeOpenUri(context, Uri.parse("https://wa.me/918446916214?text=${Uri.encode(message)}"), "WhatsApp not available")
        }
        Spacer(Modifier.height(12.dp))
        ProfileItem("Email Support") {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@frndzz.in")
                putExtra(Intent.EXTRA_SUBJECT, "Frndzzz App Support")
                putExtra(Intent.EXTRA_TEXT, message)
            }
            safeStartActivity(context, intent, "No email app found")
        }
        Spacer(Modifier.height(12.dp))
        ProfileItem("FAQs") { onFaqs() }
    }
}

@Composable
private fun FaqScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    val faqs = listOf(
        "What is Frndzzz?" to "Frndzzz is a calling app where you can connect with people and have audio or video conversations.",
        "How do coins work?" to "Coins are used to make calls. Different users have different per-minute rates.",
        "Is Frndzzz safe?" to "Yes, Frndzzz focuses on user safety and privacy. You can block or report users anytime.",
        "How can I contact support?" to "You can contact support via WhatsApp or Email from the Help & Support section.",
        "Can I delete my account?" to "Yes, you can request account deletion from Account Settings."
    )

    ProfilePageScaffold(title = "FAQs", onBack = onBack) {
        Spacer(Modifier.height(10.dp))
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            faqs.forEach { faq ->
                FaqItem(question = faq.first, answer = faq.second)
            }
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardBg).clickable { expanded = !expanded }.padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(question, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = Color.White)
        }
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Text(answer, color = TextSubtle, fontSize = 13.sp)
        }
    }
}

@Composable
private fun AccountSettings(
    onBack: () -> Unit,
    onBlockedUsers: () -> Unit,
    onFeedback: () -> Unit,
    onReports: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    BackHandler { onBack() }
    ProfilePageScaffold(title = "Account Settings", onBack = onBack) {
        Spacer(Modifier.height(10.dp))
        Spacer(Modifier.height(14.dp))
        SettingsItem(Icons.Default.Block, "Blocked Users") { onBlockedUsers() }
        Spacer(Modifier.height(12.dp))
        SettingsItem(Icons.Default.Feedback, "Feedback") { onFeedback() }
        Spacer(Modifier.height(12.dp))
        SettingsItem(Icons.Default.Report, "Reports") { onReports() }
        Spacer(Modifier.height(28.dp))
        SettingsItem(Icons.Default.Delete, "Delete Account", danger = true) { onDeleteAccount() }
    }
}

@Composable
private fun AboutUs(onBack: () -> Unit) {
    BackHandler { onBack() }
    ProfilePageScaffold(title = "About Us", onBack = onBack) {
        Spacer(Modifier.height(10.dp))
        Spacer(Modifier.height(24.dp))
        AboutItem("Instagram", "https://www.instagram.com/frndzz.app")
        Spacer(Modifier.height(12.dp))
        AboutItem("Privacy Policy", "https://frndzz.in/privacy.html")
        Spacer(Modifier.height(12.dp))
        AboutItem("Terms & Conditions", "https://frndzz.in/terms.html")
    }
}

@Composable
private fun ProfilePageScaffold(
    title: String,
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(CardBgMuted, AppBg, AppBg)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                FrndzzzBackHeader(
                    title = title,
                    subtitle = null,
                    onBack = onBack,
                    showBackButton = showBackButton
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                content = content
            )
        }
    }
}

@Composable
private fun ProfileSectionHeader(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        AppBackHeader(
            title = title,
            onBack = onBack
        )
    }
}

@Composable
fun AppBackHeader(title: String, onBack: () -> Unit) {
    FrndzzzBackHeader(
        title = title,
        subtitle = null,
        onBack = onBack
    )
}

@Composable
private fun KycVerificationScreen(onBack: () -> Unit) {
    BackHandler { onBack() }
    ProfilePageScaffold(title = "KYC Verification", onBack = onBack) {
        Spacer(Modifier.height(10.dp))
        SupportHeroCard(
            title = "KYC verification is being prepared",
            subtitle = "This section will verify your identity before payouts, high-value wallet activity, and trust badges."
        )
        Spacer(Modifier.height(16.dp))
        SupportInfoCard(
            title = "What will be needed",
            rows = listOf(
                "Status" to "Not submitted",
                "Document" to "Government ID",
                "Selfie" to "Required in production",
                "Review" to "Manual/vendor review pending"
            )
        )
        Spacer(Modifier.height(16.dp))
        SupportHeroCard(
            title = "Safe placeholder",
            subtitle = "No document upload is active in this build. We will enable secure KYC only after storage and vendor review are ready."
        )
    }
}

@Composable
private fun TransactionsScreen(
    title: String = "Transactions",
    subtitle: String = "Recent wallet activity",
    transactions: List<String> = emptyList(),
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    ProfilePageScaffold(title = title, onBack = onBack) {
        Spacer(Modifier.height(10.dp))
        SupportHeroCard(
            title = title,
            subtitle = subtitle
        )
        Spacer(Modifier.height(16.dp))
        if (transactions.isEmpty()) {
            SupportInfoCard(
                title = "No transactions yet",
                rows = listOf(
                    "Wallet ledger" to "Pending backend source of truth",
                    "Call charges" to "Will appear after completed paid calls",
                    "Refunds/rewards" to "Will appear after wallet integration"
                )
            )
        } else {
            transactions.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBg)
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(Accent1, Accent2))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(item, color = Color.White, fontSize = 14.sp)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ProfileItem(text: String, danger: Boolean = false, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardBg).clickable { onClick() }.padding(18.dp)
    ) {
        Text(text, color = if (danger) DangerRed else Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AboutItem(title: String, url: String) {
    val context = LocalContext.current
    ProfileItem(title) { safeOpenUri(context, Uri.parse(url), "Unable to open link") }
}

@Composable
private fun ProfileItemWithValue(
    title: String,
    value: String,
    enabled: Boolean = true,
    showLoading: Boolean = false,
    onClick: () -> Unit
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
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value,
                color = if (enabled) TextSubtle else TextSubtle.copy(alpha = 0.78f),
                fontSize = 13.sp
            )
            Spacer(Modifier.width(6.dp))
            if (showLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    tint = if (enabled) TextSubtle else TextSubtle.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, danger: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(CardBg).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (danger) DangerRed else Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, color = if (danger) DangerRed else Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextSubtle)
    }
}

@Composable
private fun BlockedUsersScreen(
    blockedUsers: List<BlockedProfileUser>,
    onUnblockUser: (String) -> Unit,
    onBack: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<BlockedProfileUser?>(null) }

    BackHandler { onBack() }
    ProfilePageScaffold(title = "Blocked Users", onBack = onBack) {
        Spacer(Modifier.height(10.dp))
        Spacer(Modifier.height(24.dp))
        if (blockedUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 72.dp), contentAlignment = Alignment.Center) {
                Text("No blocked users", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                blockedUsers.forEach { user ->
                    BlockedUserItem(user = user, onUnblockClick = {
                        selectedUser = it
                        showConfirmDialog = true
                    })
                }
            }
        }
    }

    if (showConfirmDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                selectedUser = null
            },
            title = { Text("Unblock user?") },
            text = { Text("Unblock ${selectedUser?.name} ?") },
            confirmButton = {
                TextButton(onClick = {
                    selectedUser?.id?.let(onUnblockUser)
                    showConfirmDialog = false
                    selectedUser = null
                }) { Text("Unblock", color = Accent2) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    selectedUser = null
                }) { Text("Cancel") }
            },
            containerColor = CardBg
        )
    }
}

@Composable
private fun BlockedUserItem(
    user: BlockedProfileUser,
    onUnblockClick: (BlockedProfileUser) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardBg).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Accent1, Accent3))), contentAlignment = Alignment.Center) {
            Text(user.name.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(user.name, color = Color.White, fontSize = 15.sp)
            Text(user.subtitle, color = TextSubtle, fontSize = 12.sp)
        }
        Text("Unblock", color = Accent2, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onUnblockClick(user) })
    }
}

@Composable
fun BlockReasonDialog(onReasonSelected: (String) -> Unit, onDismiss: () -> Unit) {
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var otherReasonText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val reasons = listOf(
        "Listener not responding",
        "Using inappropriate language",
        "Asking for personal information",
        "Suspected scam or money request",
        "Fake or misleading profile",
        "Inappropriate behaviour",
        "Other"
    )

    LaunchedEffect(selectedReason) {
        if (selectedReason == "Other") {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        shape = RoundedCornerShape(22.dp),
        title = { Text("Select Reason", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedReason = reason
                            if (reason != "Other") keyboardController?.hide()
                        }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = {
                                selectedReason = reason
                                if (reason != "Other") keyboardController?.hide()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Accent2, unselectedColor = TextSubtle)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(reason, color = Color.White, fontSize = 14.sp)
                    }

                    if (reason == "Other" && selectedReason == "Other") {
                        OutlinedTextField(
                            value = otherReasonText,
                            onValueChange = { if (it.length <= 200) otherReasonText = it },
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            placeholder = { Text("Please describe the reason (min 10 characters)", color = TextSubtle) },
                            maxLines = 3,
                            isError = otherReasonText.isNotEmpty() && otherReasonText.length < 10,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Accent2,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Accent2
                            )
                        )
                        if (otherReasonText.isNotEmpty() && otherReasonText.length < 10) {
                            Text("Minimum 10 characters required", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.8.dp)
                }
            }
        },
        confirmButton = {
            val isValid = selectedReason != null && (selectedReason != "Other" || otherReasonText.length >= 10)
            Button(
                enabled = isValid && !isSubmitting,
                onClick = {
                    isSubmitting = true
                    onReasonSelected(if (selectedReason == "Other") otherReasonText.trim() else selectedReason.orEmpty())
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent1, disabledContainerColor = Color.DarkGray)
            ) {
                AnimatedContent(targetState = isSubmitting, label = "blockSubmit") { loading ->
                    if (loading) {
                        CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White, modifier = Modifier.size(22.dp))
                    } else {
                        Text("Confirm Block", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {}
    )
}

@Composable
internal fun EditProfileScreen(
    onBack: () -> Unit,
    onProfileUpdated: () -> Unit = {},
    isSetupFlow: Boolean = false,
    isHostProfile: Boolean = false,
    hostProfilePhotoUri: String = "",
    onHostProfilePhotoClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as FrndzzApp }
    val authRepository = remember(app) { app.authRepository }
    val sessionManager = remember(app) { app.sessionManager }
    var topicOptions by remember { mutableStateOf(UserPrefs.getTopicOptions(context)) }
    var languageOptions by remember { mutableStateOf(UserPrefs.getConfiguredLanguages(context, includeAll = false)) }
    val storedNickname = safeProfileValue(UserPrefs.getNickname(context), "frndzzz_user")
    val storedPublicId = UserPrefs.resolvePublicId(
        context,
        sessionManager.getPublicId()
    )
    val storedAvatar = UserPrefs.getAvatar(context)
    val rawStoredGender = UserPrefs.getGender(context)
    val storedGender = rawStoredGender.ifBlank { "Female" }
    val storedInterests = UserPrefs.getInterests(context)
    val storedAccountMode = UserPrefs.getAccountMode(context).ifBlank { "customer" }
    val storedCommunityName = UserPrefs.getCommunityName(context)
    val storedCommunityCity = UserPrefs.getCommunityCity(context)
    val storedCommunityAbout = UserPrefs.getCommunityAbout(context)
    val storedCommunityExperience = UserPrefs.getCommunityExperience(context)
    val rawStoredLanguage = UserPrefs.getLanguage(context)
    val storedLanguage = rawStoredLanguage.takeIf { it != "All" } ?: "Hindi"
    val setupLanguageOptions = languageOptions
    val communityExperienceOptions = remember {
        listOf("Fresh start", "Good at conversations", "Ready to earn part-time")
    }
    val setupStepGender = "setup_gender"
    val setupStepLanguage = "setup_language"
    val setupStepFemaleChoice = "setup_female_choice"
    val setupStepCommunityIntro = "setup_community_intro"
    val setupStepCommunityDetails = "setup_community_details"
    val setupStepForm = "setup_form"
    val accountModeCustomer = "customer"
    val accountModeCommunity = "community"

    var nickname by rememberSaveable { mutableStateOf(storedNickname) }
    var selectedAvatar by rememberSaveable { mutableStateOf(storedAvatar) }
    var selectedInterests by rememberSaveable { mutableStateOf(storedInterests) }
    var selectedGender by rememberSaveable {
        mutableStateOf(
            if (rawStoredGender.isNotBlank()) {
                rawStoredGender
            } else if (isSetupFlow) {
                ""
            } else {
                "Female"
            }
        )
    }
    var selectedLanguage by rememberSaveable { mutableStateOf(storedLanguage) }
    var selectedAccountMode by rememberSaveable {
        mutableStateOf(
            if (storedAccountMode == accountModeCommunity) {
                accountModeCommunity
            } else {
                accountModeCustomer
            }
        )
    }
    var communityName by rememberSaveable {
        mutableStateOf(
            storedCommunityName.ifBlank {
                storedNickname.takeIf { it != "frndzzz_user" }.orEmpty()
            }
        )
    }
    var communityCity by rememberSaveable { mutableStateOf(storedCommunityCity) }
    var communityAbout by rememberSaveable { mutableStateOf(storedCommunityAbout) }
    var communityExperience by rememberSaveable {
        mutableStateOf(storedCommunityExperience.ifBlank { communityExperienceOptions.first() })
    }
    var communityLanguage by rememberSaveable { mutableStateOf(storedLanguage) }
    var setupAge by rememberSaveable { mutableStateOf("") }
    var setupStep by rememberSaveable {
        mutableStateOf(
            if (!isSetupFlow) {
                setupStepForm
            } else {
                when {
                    rawStoredGender.equals("Male", ignoreCase = true) -> setupStepLanguage
                    rawStoredGender.equals("Female", ignoreCase = true) &&
                        storedAccountMode == accountModeCommunity -> setupStepCommunityDetails
                    rawStoredGender.equals("Female", ignoreCase = true) -> setupStepLanguage
                    else -> setupStepGender
                }
            }
        )
    }
    var updatePhase by rememberSaveable { mutableStateOf("idle") }

    LaunchedEffect(Unit) {
        authRepository.getTopicTags().onSuccess { response ->
            UserPrefs.saveTopicOptions(context, response.tags)
            topicOptions = UserPrefs.getTopicOptions(context)
            selectedInterests = selectedInterests.filter { it in topicOptions }
        }
        authRepository.getSupportedLanguages().onSuccess { response ->
            UserPrefs.saveLanguageOptions(context, response.languages)
            languageOptions = UserPrefs.getConfiguredLanguages(context, includeAll = false)
            if (selectedLanguage !in languageOptions.map { it.key }) {
                selectedLanguage = languageOptions.firstOrNull()?.key ?: "Hindi"
            }
            if (communityLanguage !in languageOptions.map { it.key }) {
                communityLanguage = selectedLanguage
            }
        }
    }
    val onboardingGender = if (selectedGender == "Female") "Female" else "Male"
    val totalSetupSteps = when {
        selectedGender == "Female" && selectedAccountMode == accountModeCommunity -> 6
        selectedGender == "Female" -> 4
        else -> 3
    }
    val currentSetupStep = when (setupStep) {
        setupStepGender -> 1
        setupStepLanguage -> 2
        setupStepFemaleChoice -> 3
        setupStepCommunityIntro -> 4
        setupStepCommunityDetails -> 5
        else -> totalSetupSteps
    }

    val handleBack = {
        if (!isSetupFlow) {
            onBack()
        } else {
            when (setupStep) {
                setupStepLanguage -> {
                    if (selectedGender != "Female") {
                        selectedAccountMode = accountModeCustomer
                    }
                    setupStep = setupStepGender
                }
                setupStepFemaleChoice -> setupStep = setupStepLanguage
                setupStepCommunityIntro -> setupStep = setupStepFemaleChoice
                setupStepCommunityDetails -> setupStep = setupStepCommunityIntro
                setupStepForm -> {
                    setupStep = when {
                        selectedGender == "Female" && selectedAccountMode == accountModeCommunity -> {
                            setupStepCommunityDetails
                        }
                        selectedGender == "Female" -> setupStepFemaleChoice
                        else -> setupStepLanguage
                    }
                }
            }
        }
    }

    BackHandler(
        enabled = updatePhase == "idle" && (!isSetupFlow || setupStep != setupStepGender)
    ) {
        handleBack()
    }

    val initialPage = avatarList.indexOfFirst { it.id == selectedAvatar }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { avatarList.size })

    LaunchedEffect(pagerState.currentPage) {
        selectedAvatar = avatarList[pagerState.currentPage].id
    }

    val cleanedNickname = if (isHostProfile) {
        normalizeHostDisplayName(nickname)
    } else {
        normalizeEditableUsername(nickname)
    }
    val hostNameChangeLocked = isHostProfile &&
        isLockedHostDisplayName(storedNickname) &&
        cleanedNickname.isNotBlank() &&
        cleanedNickname != storedNickname
    val nicknameError = when {
        cleanedNickname.isBlank() -> "Nickname can't be empty."
        cleanedNickname.length < 2 -> if (isHostProfile) "Use at least 2 characters for your name." else "Use at least 2 characters for your nickname."
        isHostProfile && hasDisallowedHostNameSymbols(cleanedNickname) -> "Host name me sirf name aur surname letters allowed hain."
        !isHostProfile && hasDisallowedNicknameSymbols(cleanedNickname) -> "Nickname me sirf letters, numbers, space, dot aur underscore allowed hain."
        hasBlockedNicknameContent(cleanedNickname) -> if (isHostProfile) {
            "Name me gaali, sexual words, social ID ya mobile number allowed nahi hai."
        } else {
            "Nickname me gaali, sexual words, social ID ya mobile number allowed nahi hai."
        }
        else -> null
    }
    val hasNicknameChange =
        cleanedNickname.isNotBlank() && cleanedNickname != storedNickname && nicknameError == null
    val hasAvatarChange = selectedAvatar != storedAvatar
    val hasInterestChange = selectedInterests != storedInterests
    val desiredAccountMode = when {
        isHostProfile -> "host"
        selectedGender == "Female" -> selectedAccountMode
        else -> accountModeCustomer
    }
    val hasGenderChange = !isHostProfile && selectedGender.isNotBlank() && selectedGender != rawStoredGender
    val hasLanguageChange = !isHostProfile && selectedLanguage != storedLanguage
    val hasAccountModeChange = desiredAccountMode != storedAccountMode
    val hasChanges = if (isSetupFlow) {
        true
    } else if (isHostProfile) {
        (hasNicknameChange && !hostNameChangeLocked) || hasInterestChange || hasAccountModeChange
    } else {
        hasNicknameChange || hasAvatarChange || hasInterestChange || hasGenderChange || hasLanguageChange || hasAccountModeChange
    }
    val lockedLanguageForSave = storedLanguage.ifBlank { "Hindi" }
    val lockedGenderForSave = if (isHostProfile) "Female" else selectedGender
    val languageForSave = if (isHostProfile) lockedLanguageForSave else selectedLanguage
    val nicknameForSave = if (hostNameChangeLocked) storedNickname else cleanedNickname
    val requestHostNameChange = {
        val requestedName = cleanedNickname.ifBlank { nickname.trim() }
        val message = """
            Host name change request

            Current name: $storedNickname
            Requested name: $requestedName
            Frndzz ID: ${storedPublicId.ifBlank { "Not generated" }}
            Phone: ${sessionManager.getPhoneNumber()}
        """.trimIndent()
        safeOpenUri(
            context,
            Uri.parse("https://wa.me/918446916214?text=${Uri.encode(message)}"),
            "WhatsApp not available"
        )
    }
    LaunchedEffect(updatePhase) {
        if (updatePhase == "button_loading") {
            delay(300)
            val accessToken = sessionManager.getAccessToken()
            if (accessToken.isBlank()) {
                toast(context, "Please login again")
                updatePhase = "idle"
                return@LaunchedEffect
            }

            authRepository.saveProfile(
                accessToken = accessToken,
                request = SaveProfileRequest(
                    nickname = nicknameForSave,
                    gender = lockedGenderForSave,
                    preferredLanguage = languageForSave,
                    avatarId = selectedAvatar,
                    interests = selectedInterests,
                    accountMode = desiredAccountMode,
                    topicTags = selectedInterests,
                    nativeLanguages = listOf(languageForSave).filter { it.isNotBlank() },
                    communityName = communityName,
                    communityCity = communityCity,
                    communityAbout = communityAbout,
                    communityExperience = communityExperience
                )
            ).onSuccess { response ->
                UserPrefs.syncFromRemoteProfile(context, response.profile)
                UserPrefs.savePublicId(context, response.profile.publicId.orEmpty())
                UserPrefs.saveCommunityDetails(
                    ctx = context,
                    name = response.profile.communityName.orEmpty(),
                    city = response.profile.communityCity.orEmpty(),
                    about = response.profile.communityAbout.orEmpty(),
                    experience = response.profile.communityExperience.orEmpty()
                )
                sessionManager.updateUserSession(response.user)
                updatePhase = "success"
            }.onFailure { throwable ->
                toast(context, throwable.message ?: "Profile save nahi ho paya")
                updatePhase = "idle"
            }
        } else if (updatePhase == "success") {
            onProfileUpdated()
            delay(650)
            onBack()
        }
    }

    if (updatePhase == "success") {
        EditProfileUpdateStateScreen(
            title = "Profile Updated"
        )
        return
    }

    if (isSetupFlow && setupStep == setupStepGender) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF220340),
                            Color(0xFF3C1272),
                            Color(0xFF4B1887)
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(26.dp))
                Text(
                    text = "Select your gender",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Pick your gender and avatar to make your profile unique.",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(32.dp))
                ProfileSetupGenderToggle(
                    selectedGender = onboardingGender,
                    onSelectMale = {
                        selectedGender = "Male"
                        selectedAccountMode = accountModeCustomer
                    },
                    onSelectFemale = {
                        selectedGender = "Female"
                    },
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
                Spacer(Modifier.height(30.dp))
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 18.dp,
                    contentPadding = PaddingValues(horizontal = 68.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) { page ->
                    val avatar = avatarList[page]
                    val pageOffset =
                        ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                    val isFocused = pageOffset < 0.55f
                    val scale = lerp(0.76f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                alpha = lerp(0.42f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileSetupAvatarCard(
                            imageRes = avatar.imageRes,
                            selected = isFocused
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Gender can't be changed later",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        if (onboardingGender == "Female") {
                            selectedGender = "Female"
                        } else {
                            selectedGender = "Male"
                            selectedAccountMode = accountModeCustomer
                        }
                        setupStep = setupStepLanguage
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1D1132)
                    )
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(14.dp))
            }
        }
        return
    }

    if (isSetupFlow && setupStep == setupStepLanguage) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF220340),
                            Color(0xFF3C1272),
                            Color(0xFF4B1887)
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { handleBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back_chevron),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Language you speak",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Find dostts who speak your language. Please select one.",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(28.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    setupLanguageOptions.forEach { language ->
                        ProfileSetupLanguageCard(
                            title = language.key,
                            script = language.label,
                            selected = selectedLanguage == language.key,
                            onClick = {
                                selectedLanguage = language.key
                                communityLanguage = language.key
                            }
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        communityLanguage = selectedLanguage
                        setupStep = if (selectedGender == "Female") {
                            setupStepFemaleChoice
                        } else {
                            setupStepForm
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8E69BC),
                        contentColor = Color(0xFF1D1132)
                    )
                ) {
                    Text(
                        text = "Continue",
                        color = Color(0xFF1D1132),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(14.dp))
            }
        }
        return
    }

    if (isSetupFlow && setupStep == setupStepFemaleChoice) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF220340),
                            Color(0xFF3C1272),
                            Color(0xFF4B1887)
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { handleBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back_chevron),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "How do you want to use Dostt?",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Choose how you want to begin. You can complete your setup in a few quick steps.",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(26.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(346.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProfileSetupUsageChoiceCard(
                        title = "Make new\nfriends",
                        subtitle = "Meet people, have real conversations.",
                        imageRes = R.drawable.avatar_ai_9,
                        selected = selectedAccountMode == accountModeCustomer,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedAccountMode = accountModeCustomer }
                    )
                    ProfileSetupUsageChoiceCard(
                        title = "Earn money",
                        subtitle = "Support others and earn along the way.",
                        imageRes = R.drawable.avatar_ai_10,
                        selected = selectedAccountMode == accountModeCommunity,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedAccountMode = accountModeCommunity }
                    )
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        setupStep = if (selectedAccountMode == accountModeCommunity) {
                            setupStepCommunityIntro
                        } else {
                            setupStepForm
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8E69BC),
                        contentColor = Color(0xFF1D1132)
                    )
                ) {
                    Text(
                        text = "Continue",
                        color = Color(0xFF1D1132),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(14.dp))
            }
        }
        return
    }

    if (isSetupFlow && setupStep == setupStepCommunityIntro) {
        val ageValue = setupAge.toIntOrNull()
        val validAge = ageValue != null && ageValue in 18..99
        val validInterests = selectedInterests.size in 1..5

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF220340),
                            Color(0xFF3C1272),
                            Color(0xFF4B1887)
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { handleBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back_chevron),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "Tell us about yourself",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "The information you share will be visible to all your dostts",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(30.dp))
                Text(
                    text = "Enter your Age",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = setupAge,
                    onValueChange = {
                        val digits = it.filter(Char::isDigit)
                        if (digits.length <= 2) setupAge = digits
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    singleLine = true,
                    placeholder = { Text("Age in Years", color = Color.White.copy(alpha = 0.45f)) },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(alpha = 0.88f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "This info will not be shared with Dostts",
                    color = Color.White.copy(alpha = 0.58f),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(30.dp))
                Text(
                    text = "Select your topics",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(14.dp))
                SupportChipWrap(
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 10.dp
                ) {
                    topicOptions.forEach { interest ->
                        ProfileSetupInterestChip(
                            label = interest,
                            selected = interest in selectedInterests,
                            onClick = {
                                selectedInterests = when {
                                    interest in selectedInterests -> selectedInterests - interest
                                    selectedInterests.size >= 5 -> {
                                        toast(context, "Select maximum 5 topics")
                                        selectedInterests
                                    }
                                    else -> selectedInterests + interest
                                }
                            }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Select minimum 1 topic, maximum of 5",
                    color = Color.White.copy(alpha = 0.68f),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(42.dp))
                Button(
                    onClick = {
                        if (!validAge) {
                            toast(context, "Enter a valid age between 18 and 99")
                            return@Button
                        }
                        if (!validInterests) {
                            toast(context, "Select at least 1 topic")
                            return@Button
                        }
                        setupStep = setupStepCommunityDetails
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8E69BC),
                        contentColor = Color(0xFF1D1132)
                    )
                ) {
                    Text(
                        text = "Continue",
                        color = Color(0xFF1D1132),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(18.dp))
            }
        }
        return
    }

    if (isSetupFlow && setupStep == setupStepCommunityDetails) {
        val communityDetailsValid =
            communityName.trim().length >= 2 &&
                communityCity.trim().length >= 2 &&
                communityAbout.trim().length >= 20

        ProfilePageScaffold(
            title = "Community Details",
            onBack = handleBack
        ) {
            Spacer(Modifier.height(12.dp))
            ProfileSetupHeroCard(
                badgeText = "Listener details",
                title = "Tell us a little about you",
                subtitle = "These details help us prepare your listener profile before it goes live.",
                stepIndex = 5,
                stepCount = 6,
                hint = "Just one short form"
            )
            Spacer(Modifier.height(18.dp))
            EditProfileSectionCard(
                title = "Start earning from Frndzzz community",
                subtitle = "Share a few details so we can move your onboarding forward before your profile goes live."
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.avatar_ai_9),
                        contentDescription = null,
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "Complete these details once, then we will take you to the main profile setup screen.",
                        color = TextSubtle,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = communityName,
                    onValueChange = { if (it.length <= 40) communityName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Full name", color = TextSubtle) },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent2,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Accent2
                    )
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = communityCity,
                    onValueChange = { if (it.length <= 32) communityCity = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("City", color = TextSubtle) },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent2,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Accent2
                    )
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Preferred app language",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))
                SupportChipWrap {
                    setupLanguageOptions.forEach { language ->
                        SupportChoiceChip(
                            label = "${language.label} ${languageGraphicEmoji(language.key)}",
                            selected = communityLanguage == language.key,
                            onClick = { communityLanguage = language.key }
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Experience",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))
                SupportChipWrap {
                    communityExperienceOptions.forEach { option ->
                        SupportChoiceChip(
                            label = option,
                            selected = communityExperience == option,
                            onClick = { communityExperience = option }
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = communityAbout,
                    onValueChange = { if (it.length <= 220) communityAbout = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 5,
                    placeholder = {
                        Text(
                            "Tell us about your conversation style, language comfort, and why you want to join Frndzzz.",
                            color = TextSubtle
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent2,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Accent2
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (communityDetailsValid) {
                        "Looks good. You can continue to the main profile setup."
                    } else {
                        "Add your name, city, and at least a short intro before continuing."
                    },
                    color = if (communityDetailsValid) TextSubtle else DangerRed,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = {
                    if (!communityDetailsValid) {
                        toast(context, "Please complete your community details")
                        return@Button
                    }
                    selectedLanguage = communityLanguage
                    selectedAccountMode = accountModeCommunity
                    setupStep = setupStepForm
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent2,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Continue to Profile",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(24.dp))
        }
        return
    }

    ProfilePageScaffold(
        title = if (isSetupFlow) "Create Profile" else "Edit Profile",
        onBack = if (isSetupFlow) handleBack else onBack
    ) {
        Spacer(Modifier.height(12.dp))
        if (isSetupFlow) {
            val setupHint = when {
                selectedGender == "Male" -> "Final step for customer setup"
                selectedAccountMode == accountModeCommunity -> "Final listener profile step"
                else -> "Final customer profile step"
            }
            ProfileSetupHeroCard(
                badgeText = "Almost done",
                title = "Finish your Frndzzz profile",
                subtitle = "Pick your look, nickname, topics, and language so your account opens in a polished state.",
                stepIndex = currentSetupStep,
                stepCount = totalSetupSteps,
                hint = setupHint
            )
            Spacer(Modifier.height(18.dp))
        }
        if (isHostProfile) {
            HostDpPickerCard(
                hostName = cleanedNickname.ifBlank { storedNickname },
                profilePhotoUri = hostProfilePhotoUri,
                onPickPhoto = onHostProfilePhotoClick
            )
        } else {
            EditProfileSectionCard(
                title = if (isSetupFlow) "Choose your look" else "Avatar",
                subtitle = ""
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalPager(
                        state = pagerState,
                        pageSpacing = 12.dp,
                        contentPadding = PaddingValues(horizontal = 72.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(136.dp)
                    ) { page ->
                        val avatar = avatarList[page]
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                        val scale = lerp(0.72f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    alpha = lerp(0.48f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AvatarBubble(
                                avatar = avatar,
                                size = if (pagerState.currentPage == page) 120.dp else 90.dp,
                                highlight = pagerState.currentPage == page
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        EditProfileSectionCard(
            title = if (isHostProfile) "Full name" else "Nickname",
            subtitle = if (isHostProfile) {
                "Name ek baar set hota hai. Change ke liye team ko request bhejni hogi."
            } else {
                ""
            }
        ) {
            OutlinedTextField(
                value = nickname,
                onValueChange = {
                    nickname = if (isHostProfile) {
                        normalizeHostDisplayName(it)
                    } else {
                        normalizeEditableUsername(it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        if (isHostProfile) "Name Surname" else "Nickname",
                        color = TextSubtle
                    )
                },
                singleLine = true,
                isError = nicknameError != null,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent2,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                    errorBorderColor = DangerRed,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    errorTextColor = Color.White,
                    disabledBorderColor = Color.White.copy(alpha = 0.08f),
                    disabledTextColor = TextSubtle,
                    cursorColor = Accent2
                )
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
            Spacer(Modifier.height(12.dp))
            EditProfileLockedInfoRow(
                label = "ID",
                value = storedPublicId.ifBlank { "Generating after save" },
                note = "This 8 digit numeric ID is unique and fixed for life."
            )
            if (hostNameChangeLocked) {
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = requestHostNameChange) {
                    Text(
                        text = "Request name change from team",
                        color = Accent2,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (!(isSetupFlow && selectedAccountMode == accountModeCommunity)) {
            EditProfileSectionCard(
                title = "Topics",
                subtitle = "Pick 1 to 5 topics so discovery and random match feel personal."
            ) {
                SupportChipWrap {
                    topicOptions.forEach { interest ->
                        SupportChoiceChip(
                            label = interest,
                            selected = interest in selectedInterests,
                            onClick = {
                                selectedInterests = when {
                                    interest in selectedInterests -> selectedInterests - interest
                                    selectedInterests.size >= 5 -> {
                                        toast(context, "Select maximum 5 topics")
                                        selectedInterests
                                    }
                                    else -> selectedInterests + interest
                                }
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        if (isSetupFlow) {
            EditProfileSectionCard(
                title = "Gender & language",
                subtitle = "These stay locked after you finish profile setup."
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    EditProfileLockedInfoRow(
                        label = "Gender",
                        value = selectedGender,
                        note = "Use back if you need to change it before finishing."
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                    EditProfileLockedInfoRow(
                        label = "App language",
                        value = getLanguageLabel(selectedLanguage),
                        note = "This preference is used to show same-language profiles first."
                    )
                    if (selectedGender == "Female") {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                        EditProfileLockedInfoRow(
                            label = "Profile type",
                            value = if (selectedAccountMode == accountModeCommunity) {
                                "Frndzzz community"
                            } else {
                                "Customer"
                            },
                            note = if (selectedAccountMode == accountModeCommunity) {
                                "Community details are attached to this setup."
                            } else {
                                "Normal customer profile flow selected."
                            }
                        )
                    }
                }
            }
        } else if (isHostProfile) {
            EditProfileSectionCard(
                title = "Gender & native language",
                subtitle = "These details are locked. Change ke liye support team se baat karein."
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    EditProfileLockedInfoRow(
                        label = "Gender",
                        value = "Female",
                        note = "Host profile ke liye fixed. Not editable."
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                    EditProfileLockedInfoRow(
                        label = "Native language",
                        value = getLanguageLabel(storedLanguage),
                        note = "Profile banate time selected language. Change ke liye team se request karein."
                    )
                }
            }
        } else {
            EditProfileSectionCard(
                title = "Gender & language",
                subtitle = "This box only shows your current profile details. Editing is disabled here."
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    EditProfileLockedInfoRow(
                        label = "Gender",
                        value = storedGender,
                        note = "Only one time set. Not editable here."
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                    EditProfileLockedInfoRow(
                        label = "App language",
                        value = getLanguageLabel(storedLanguage),
                        note = "Current language from your profile section."
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                if (nicknameError != null) {
                    toast(context, nicknameError)
                    return@Button
                }
                if (hostNameChangeLocked) {
                    toast(context, "Host name change ke liye request button use karo")
                    return@Button
                }
                if (selectedInterests.isEmpty()) {
                    toast(context, "Select at least one topic")
                    return@Button
                }
                if (isSetupFlow && selectedLanguage.isBlank()) {
                    toast(context, "Select your app language")
                    return@Button
                }
                if (!hasChanges) {
                    toast(context, "Nothing new to save")
                    return@Button
                }
                updatePhase = "button_loading"
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
            enabled = hasChanges && nicknameError == null && updatePhase == "idle",
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent2,
                disabledContainerColor = Accent2.copy(alpha = 0.34f),
                disabledContentColor = Color.White.copy(alpha = 0.72f)
            )
        ) {
            if (updatePhase == "button_loading") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.4.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Saving",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = if (isSetupFlow) "Complete Profile" else "Update Profile",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun EditProfileDecisionCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        listOf(
                            CardBgMuted.copy(alpha = 0.98f),
                            CardBg.copy(alpha = 0.98f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            CardBg.copy(alpha = 0.96f),
                            CardBg.copy(alpha = 0.93f)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                brush = if (selected) {
                    Brush.horizontalGradient(
                        listOf(
                            Accent2.copy(alpha = 0.52f),
                            Accent1.copy(alpha = 0.22f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                },
                shape = shape
            )
            .clickable { onClick() }
            .padding(18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditProfileMetaPill(
                    text = if (selected) "Selected" else "Tap to choose",
                    highlighted = selected
                )
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextSubtle,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }

        Box(
            modifier = Modifier
                .size(width = 92.dp, height = 106.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (selected) Accent2.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(6.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun EditProfileSectionCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        CardBgMuted.copy(alpha = 0.96f),
                        CardBg.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = {
            if (title.isNotBlank()) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = TextSubtle, fontSize = 12.sp, lineHeight = 18.sp)
            }
            content()
        })
    }
}

@Composable
private fun EditProfileLockedInfoRow(
    label: String,
    value: String,
    note: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = TextSubtle, fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text(note, color = TextSubtle, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun ProfileSetupHeroCard(
    badgeText: String,
    title: String,
    subtitle: String,
    stepIndex: Int,
    stepCount: Int,
    hint: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        CardBgMuted.copy(alpha = 0.98f),
                        CardBg.copy(alpha = 0.98f),
                        AppBg.copy(alpha = 0.94f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Accent2.copy(alpha = 0.34f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditProfileMetaPill(text = badgeText, highlighted = true)
                Text(
                    text = "Step $stepIndex of $stepCount",
                    color = TextSubtle,
                    fontSize = 12.sp
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
                Text(
                    text = subtitle,
                    color = TextSubtle,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(stepCount) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (index < stepIndex) {
                                    Brush.horizontalGradient(
                                        listOf(Accent2, Accent1)
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.08f),
                                            Color.White.copy(alpha = 0.04f)
                                        )
                                    )
                                }
                            )
                    )
                }
            }
            Text(
                text = hint,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProfileSetupGenderToggle(
    selectedGender: String,
    onSelectMale: () -> Unit,
    onSelectFemale: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Transparent)
            .border(1.dp, Color.White.copy(alpha = 0.32f), RoundedCornerShape(24.dp))
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProfileSetupGenderToggleItem(
            label = "Male",
            selected = selectedGender == "Male",
            modifier = Modifier.weight(1f),
            onClick = onSelectMale
        )
        ProfileSetupGenderToggleItem(
            label = "Female",
            selected = selectedGender == "Female",
            modifier = Modifier.weight(1f),
            onClick = onSelectFemale
        )
    }
}

@Composable
private fun ProfileSetupGenderToggleItem(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) Color.White.copy(alpha = 0.12f) else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (selected) Color.White.copy(alpha = 0.88f) else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = if (selected) 1f else 0.7f),
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileSetupAvatarCard(
    imageRes: Int,
    selected: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(274.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = Color.White.copy(alpha = if (selected) 0.92f else 0.28f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(2.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ProfileSetupLanguageCard(
    title: String,
    script: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) {
                    Color.White.copy(alpha = 0.16f)
                } else {
                    Color.White.copy(alpha = 0.08f)
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) Color.White.copy(alpha = 0.94f) else Color.White.copy(alpha = 0.20f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = script,
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProfileSetupUsageChoiceCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (selected) 0.16f else 0.10f),
                        Color(0xFF6D35AB).copy(alpha = if (selected) 0.84f else 0.74f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = if (selected) Color.White.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 23.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                ProfileSetupChatBubble(
                    modifier = Modifier.align(Alignment.TopStart)
                )
                ProfileSetupChatBubble(
                    compact = true,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(y = 20.dp)
                )
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(134.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun ProfileSetupInterestChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (selected) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.08f)
            )
            .border(
                width = 1.dp,
                color = if (selected) Color.White.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(999.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileSetupChatBubble(
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = if (compact) 0.20f else 0.14f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = if (compact) 14.dp else 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = "••••",
            color = Color.White.copy(alpha = 0.82f),
            fontSize = if (compact) 13.sp else 15.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun EditProfileMetaPill(
    text: String,
    highlighted: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (highlighted) {
                    Accent2.copy(alpha = 0.16f)
                } else {
                    Color.White.copy(alpha = 0.04f)
                }
            )
            .border(
                width = 1.dp,
                color = if (highlighted) Accent2.copy(alpha = 0.48f) else Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = if (highlighted) Color.White else TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EditProfileGenderChip(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        listOf(
                            Accent2.copy(alpha = 0.28f),
                            Accent1.copy(alpha = 0.22f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) Accent2.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else TextSubtle.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

private fun normalizeEditableUsername(input: String): String {
    val cleaned = input
        .replace("\\s+".toRegex(), " ")
        .trimStart()
        .take(24)

    val firstLetterIndex = cleaned.indexOfFirst { it.isLetter() }
    if (firstLetterIndex < 0) {
        return cleaned
    }

    return cleaned.replaceRange(
        firstLetterIndex,
        firstLetterIndex + 1,
        cleaned[firstLetterIndex].uppercase()
    )
}

private fun normalizeHostDisplayName(input: String): String {
    val cleaned = input
        .replace("\\s+".toRegex(), " ")
        .filter { it.isLetter() || it.isWhitespace() }
        .trimStart()
        .take(32)

    val firstLetterIndex = cleaned.indexOfFirst { it.isLetter() }
    if (firstLetterIndex < 0) {
        return cleaned
    }

    return cleaned.replaceRange(
        firstLetterIndex,
        firstLetterIndex + 1,
        cleaned[firstLetterIndex].uppercase()
    )
}

private fun hasDisallowedHostNameSymbols(input: String): Boolean {
    return input.any { !(it.isLetter() || it.isWhitespace()) }
}

private fun isLockedHostDisplayName(input: String): Boolean {
    val normalized = input.trim()
    val lower = normalized.lowercase()
    return normalized.isNotBlank() &&
        lower != "frndzzz_user" &&
        lower != "frndzzz user" &&
        lower != "frndzz user" &&
        !lower.startsWith("frndzzz_")
}

private fun hasDisallowedNicknameSymbols(input: String): Boolean {
    return input.any { !(it.isLetterOrDigit() || it.isWhitespace() || it == '_' || it == '.') }
}

private fun hasBlockedNicknameContent(input: String): Boolean {
    val compact = input.lowercase().replace("[^a-z0-9]".toRegex(), "")
    val totalDigits = input.count { it.isDigit() }
    val hasLongDigitRun = "\\d{10,}".toRegex().containsMatchIn(input)
    if (totalDigits >= 10 || hasLongDigitRun) {
        return true
    }

    val normalized = input.lowercase()
    if (
        normalized.contains("@") ||
        normalized.contains("http") ||
        normalized.contains("www.") ||
        normalized.contains("instagram") ||
        normalized.contains("insta") ||
        normalized.contains("whatsapp") ||
        normalized.contains("wa.me") ||
        normalized.contains("telegram") ||
        normalized.contains("t.me") ||
        normalized.contains("snapchat") ||
        normalized.contains("snap") ||
        normalized.contains("facebook") ||
        normalized.contains("youtube") ||
        normalized.contains("onlyfans") ||
        normalized.contains("linktree") ||
        "(^|[\\s_.])(?:ig|fb|yt|wa|snap)($|[\\s_.])".toRegex().containsMatchIn(normalized)
    ) {
        return true
    }

    val blockedTokens = listOf(
        "fuck", "fucker", "sex", "sexy", "porn", "porno", "xxx", "nude", "nudes",
        "boobs", "dick", "pussy", "vagina", "penis", "bitch", "asshole",
        "slut", "horny", "hotgirl", "hotboy", "camgirl", "callgirl",
        "chut", "chutiya", "chutia", "bhosdi", "bhosdike", "bhenchod", "behenchod",
        "madarchod", "randi", "rand", "lund", "lauda", "loda",
        "gaand", "gand", "harami", "kamina", "kamine", "suar"
    )
    return blockedTokens.any { compact.contains(it) }
}

private fun profileRouteOrder(route: ProfileRoute): Int = when (route) {
    ProfileRoute.Profile -> 0
    ProfileRoute.EditProfile -> 1
    ProfileRoute.Language -> 2
    ProfileRoute.Settings -> 3
    ProfileRoute.Help -> 4
    ProfileRoute.About -> 5
    ProfileRoute.Blocked -> 6
    ProfileRoute.Faqs -> 7
    ProfileRoute.Feedback -> 8
    ProfileRoute.Reports -> 9
    ProfileRoute.DeleteAccount -> 10
    ProfileRoute.Wallet -> 11
    ProfileRoute.Transactions -> 12
}

@Composable
private fun EditProfileUpdateStateScreen(
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen.copy(alpha = 0.16f))
                    .border(
                        width = 1.dp,
                        color = SuccessGreen.copy(alpha = 0.42f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileActionStateScreen(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Accent2.copy(alpha = 0.16f))
                    .border(
                        width = 1.dp,
                        color = Accent2.copy(alpha = 0.42f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(34.dp)
                )
            }
            Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                text = subtitle,
                color = TextSubtle,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LanguageScreen(
    onBack: () -> Unit,
    onLanguageApplied: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as FrndzzApp }
    val authRepository = remember(app) { app.authRepository }
    var languageOptions by remember { mutableStateOf(UserPrefs.getConfiguredLanguages(context, includeAll = true)) }
    var selectedLanguage by remember { mutableStateOf(UserPrefs.getLanguage(context)) }
    var applyingLanguage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        authRepository.getSupportedLanguages().onSuccess { response ->
            UserPrefs.saveLanguageOptions(context, response.languages)
            languageOptions = UserPrefs.getConfiguredLanguages(context, includeAll = true)
        }
    }

    LaunchedEffect(applyingLanguage) {
        if (applyingLanguage != null) {
            delay(900)
            onLanguageApplied()
        }
    }

    if (applyingLanguage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = languageGraphicEmoji(applyingLanguage.orEmpty()),
                    fontSize = 40.sp
                )
                CircularProgressIndicator(
                    color = Accent2,
                    strokeWidth = 3.dp
                )
                Text(
                    text = "Changing your preferred language...",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Please wait while we apply your language preference",
                    color = TextSubtle,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    ProfilePageScaffold(title = "Select Language", onBack = onBack) {
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            languageOptions.forEach { lang ->
                LanguageOptionCard(
                    languageKey = lang.key,
                    nativeLabel = lang.label,
                    selected = selectedLanguage == lang.key,
                    onClick = {
                        selectedLanguage = lang.key
                        UserPrefs.saveLanguage(context, lang.key)
                        applyingLanguage = lang.key
                    }
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun LanguageHeroCard(
    selectedLanguage: String,
    nativeLabel: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        CardBgMuted.copy(alpha = 0.98f),
                        CardBg.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Accent2.copy(alpha = 0.28f),
                        Accent1.copy(alpha = 0.16f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Accent2.copy(alpha = 0.18f),
                                Accent1.copy(alpha = 0.24f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "CURRENT LANGUAGE",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = selectedLanguage,
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = nativeLabel,
                    color = TextSubtle.copy(alpha = 0.95f),
                    fontSize = 15.sp
                )
            }

            Text(
                text = if (selectedLanguage == "All") {
                    "All host languages will stay visible across your experience."
                } else {
                    "Hosts who speak this language will stay more relevant across the app."
                },
                color = TextSubtle,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun LanguageOptionCard(
    languageKey: String,
    nativeLabel: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        listOf(
                            CardBgMuted.copy(alpha = 0.98f),
                            CardBg.copy(alpha = 0.98f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            CardBg.copy(alpha = 0.97f),
                            CardBg.copy(alpha = 0.94f)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                brush = if (selected) {
                    Brush.horizontalGradient(
                        listOf(
                            Accent2.copy(alpha = 0.48f),
                            Accent1.copy(alpha = 0.26f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.07f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                },
                shape = shape
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = nativeLabel,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Accent2.copy(alpha = 0.20f),
                                    Accent1.copy(alpha = 0.26f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Accent2.copy(alpha = 0.34f),
                            RoundedCornerShape(999.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Accent2,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Text(
                text = languageGraphicEmoji(languageKey),
                fontSize = 24.sp
            )
        }
    }
}

private fun languageGraphicEmoji(languageKey: String): String {
    return when (languageKey) {
        "Hindi" -> "🪔"
        "Marathi" -> "🌺"
        "Gujarati" -> "🪷"
        "Marwadi" -> "🐪"
        "Tamil" -> "🌸"
        "Telugu" -> "🎶"
        "Kannada" -> "☕"
        "Malayalam" -> "🌴"
        "Odia" -> "🐚"
        "Bengali" -> "🌼"
        "Punjabi" -> "🌾"
        "English" -> "✨"
        else -> "🌍"
    }
}

@Composable
private fun HostDpPickerCard(
    hostName: String,
    profilePhotoUri: String,
    onPickPhoto: () -> Unit
) {
    val photoBitmap = rememberProfilePhotoBitmap(profilePhotoUri)
    EditProfileSectionCard(
        title = "Profile Photo",
        subtitle = ""
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .clip(CircleShape)
                        .background(CardBgMuted)
                        .border(2.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                        .clickable { onPickPhoto() },
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
                            text = hostName.trim().take(1).uppercase().ifBlank { "H" },
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Accent2)
                        .border(2.dp, CardBg, CircleShape)
                        .clickable { onPickPhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Set DP",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditProfileAvatarHero(
    avatar: Avatar,
    nickname: String,
    selectedGender: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        CardBgMuted.copy(alpha = 0.96f),
                        CardBg.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Accent2.copy(alpha = 0.34f),
                        Accent1.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Accent2.copy(alpha = 0.22f),
                                Accent1.copy(alpha = 0.32f)
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    "AI AVATAR DP",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.3.sp
                )
            }
            Spacer(Modifier.height(18.dp))
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(156.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    avatar.colors.last().copy(alpha = 0.34f),
                                    avatar.colors.first().copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                AvatarBubble(avatar = avatar, size = 126.dp, highlight = true)
            }
            Spacer(Modifier.height(18.dp))
            Text(nickname, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Your selected AI avatar will show as your DP.",
                color = TextSubtle,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "$selectedGender profile look",
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun rememberProfilePhotoBitmap(profilePhotoUri: String): ImageBitmap? {
    val context = LocalContext.current
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = profilePhotoUri) {
        value = loadProfilePhotoBitmap(context, profilePhotoUri)
    }
    return bitmapState.value
}

private suspend fun loadProfilePhotoBitmap(
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
internal fun AvatarBubble(avatar: Avatar, size: Dp, highlight: Boolean = false) {
    val borderWidth = if (highlight) 3.dp else 1.dp
    val borderBrush = if (highlight) {
        Brush.linearGradient(listOf(Accent1, Accent2))
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.14f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    }
    val avatarScale by animateFloatAsState(
        targetValue = if (highlight) 1f else 0.95f,
        animationSpec = tween(durationMillis = 220),
        label = "avatarScale"
    )

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = avatarScale
                scaleY = avatarScale
            }
            .clip(CircleShape)
            .background(Brush.linearGradient(avatar.colors))
            .border(borderWidth, borderBrush, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = avatar.imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (highlight) 0.03f else 0.08f),
                            Color.Transparent,
                            Color.Black.copy(alpha = if (highlight) 0.08f else 0.16f)
                        )
                    )
                )
        )
    }
}
