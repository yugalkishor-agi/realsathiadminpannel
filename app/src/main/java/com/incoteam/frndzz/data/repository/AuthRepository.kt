package com.incoteam.frndzz.data.repository

import com.google.gson.Gson
import com.incoteam.frndzz.BuildConfig
import com.incoteam.frndzz.core.session.SessionManager
import com.incoteam.frndzz.data.model.auth.DiscoveryHostsResponse
import com.incoteam.frndzz.data.model.auth.HostDashboardResponse
import com.incoteam.frndzz.data.model.auth.HostKycRequest
import com.incoteam.frndzz.data.model.auth.HostKycResponse
import com.incoteam.frndzz.data.model.auth.LanguagesResponse
import com.incoteam.frndzz.data.model.auth.RandomMatchRequest
import com.incoteam.frndzz.data.model.auth.RandomMatchResponse
import com.incoteam.frndzz.data.model.auth.RecordWalletTransactionRequest
import com.incoteam.frndzz.data.model.auth.RecordWalletTransactionResponse
import com.incoteam.frndzz.data.model.auth.RefreshSessionRequest
import com.incoteam.frndzz.data.model.auth.RefreshSessionResponse
import com.incoteam.frndzz.data.model.auth.ReportUserRequest
import com.incoteam.frndzz.data.model.auth.ReportUserResponse
import com.incoteam.frndzz.data.model.auth.SendOtpRequest
import com.incoteam.frndzz.data.model.auth.SendOtpResponse
import com.incoteam.frndzz.data.model.auth.SaveHostSettingsRequest
import com.incoteam.frndzz.data.model.auth.SaveProfileRequest
import com.incoteam.frndzz.data.model.auth.SupportChatRequest
import com.incoteam.frndzz.data.model.auth.SupportChatResponse
import com.incoteam.frndzz.data.model.auth.TopicTagsResponse
import com.incoteam.frndzz.data.model.auth.UnblockUserRequest
import com.incoteam.frndzz.data.model.auth.UnblockUserResponse
import com.incoteam.frndzz.data.model.auth.UploadHostMediaResponse
import com.incoteam.frndzz.data.model.auth.UserProfileResponse
import com.incoteam.frndzz.data.model.auth.VerifyOtpRequest
import com.incoteam.frndzz.data.model.auth.VerifyOtpResponse
import com.incoteam.frndzz.data.model.auth.WalletSummaryResponse
import com.incoteam.frndzz.utils.PhoneNumberFormatter
import com.incoteam.frndzz.data.model.common.ApiErrorResponse
import com.incoteam.frndzz.data.remote.AuthApiService
import java.io.File
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class AuthRepository(
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager
) {

    private val gson = Gson()
    private fun sessionToken(accessToken: String): String = accessToken.trim()

    suspend fun sendOtp(phoneNumber: String): Result<SendOtpResponse> = runCatching {
        val subscriberNumber = PhoneNumberFormatter.toIndianSubscriberNumber(phoneNumber)
        val request = SendOtpRequest(
            phoneNumber = phoneNumber,
            phone = subscriberNumber
        )
        val response = executeWithConfiguredEndpoint(BuildConfig.SEND_OTP_URL) { url ->
            authApiService.sendOtp(
                url = url,
                request = request
            )
        }
        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Empty response from server.")
        } else {
            throw IllegalStateException(parseError(response.errorBody()?.string(), "Unable to send OTP right now."))
        }
    }

    suspend fun verifyOtp(
        phoneNumber: String,
        otpCode: String,
        requestId: String
    ): Result<VerifyOtpResponse> = runCatching {
        val subscriberNumber = PhoneNumberFormatter.toIndianSubscriberNumber(phoneNumber)
        val request = VerifyOtpRequest(
            phoneNumber = phoneNumber,
            phone = subscriberNumber,
            otpCode = otpCode,
            otp = otpCode,
            requestId = requestId
        )
        val response = executeWithConfiguredEndpoint(BuildConfig.VERIFY_OTP_URL) { url ->
            authApiService.verifyOtp(
                url = url,
                request = request
            )
        }
        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Empty response from server.")
        } else {
            throw IllegalStateException(parseError(response.errorBody()?.string(), "OTP verification failed."))
        }
    }

    suspend fun saveProfile(
        accessToken: String,
        request: SaveProfileRequest
    ): Result<UserProfileResponse> = runCatching {
        executeProtectedRequest(
            accessToken = accessToken,
            fallbackMessage = "Unable to save profile right now."
        ) { activeToken ->
            executeWithConfiguredEndpoint(BuildConfig.SAVE_PROFILE_URL) { url ->
                authApiService.saveProfile(
                    url = url,
                    sessionToken = sessionToken(activeToken),
                    request = request
                )
            }
        }
    }

    suspend fun sendSupportChat(
        accessToken: String,
        request: SupportChatRequest
    ): Result<SupportChatResponse> = runCatching {
        executeProtectedRequest(
            accessToken = accessToken,
            fallbackMessage = "Unable to load support chat right now."
        ) { activeToken ->
            executeWithConfiguredEndpoint(BuildConfig.SUPPORT_CHAT_URL) { url ->
                authApiService.supportChat(
                    url = url,
                    sessionToken = sessionToken(activeToken),
                    request = request
                )
            }
        }
    }

    suspend fun getTopicTags(): Result<TopicTagsResponse> = runCatching {
        val response = executeWithConfiguredEndpoint(BuildConfig.CONFIG_TAGS_URL) { url ->
            authApiService.getTopicTags(url = url)
        }
        if (response.isSuccessful) {
            response.body() ?: TopicTagsResponse()
        } else {
            throw IllegalStateException(parseError(response.errorBody()?.string(), "Unable to load topics right now."))
        }
    }

    suspend fun getSupportedLanguages(): Result<LanguagesResponse> = runCatching {
        val response = executeWithConfiguredEndpoint(BuildConfig.CONFIG_LANGUAGES_URL) { url ->
            authApiService.getSupportedLanguages(url = url)
        }
        if (response.isSuccessful) {
            response.body() ?: LanguagesResponse()
        } else {
            throw IllegalStateException(parseError(response.errorBody()?.string(), "Unable to load languages right now."))
        }
    }

    suspend fun getDiscoveryHosts(
        accessToken: String,
        topicTags: List<String> = emptyList(),
        languages: List<String> = emptyList(),
        liveOnly: Boolean = true
    ): Result<DiscoveryHostsResponse> = runCatching {
        executeProtectedRequest(
            accessToken = accessToken,
            fallbackMessage = "Unable to load hosts right now."
        ) { activeToken ->
            executeWithConfiguredEndpoint(BuildConfig.DISCOVERY_HOSTS_URL) { url ->
                authApiService.getDiscoveryHosts(
                    url = url,
                    sessionToken = sessionToken(activeToken),
                    topicTags = topicTags,
                    languages = languages,
                    liveOnly = liveOnly
                )
            }
        }
    }

    suspend fun requestRandomMatch(
        accessToken: String,
        request: RandomMatchRequest
    ): Result<RandomMatchResponse> = runCatching {
        executeProtectedRequest(
            accessToken = accessToken,
            fallbackMessage = "Unable to find a random match right now."
        ) { activeToken ->
            executeWithConfiguredEndpoint(BuildConfig.RANDOM_MATCH_URL) { url ->
                authApiService.requestRandomMatch(
                    url = url,
                    sessionToken = sessionToken(activeToken),
                    request = request
                )
            }
        }
    }

    suspend fun reportUser(
        accessToken: String,
        request: ReportUserRequest
    ): Result<ReportUserResponse> = runCatching {
        executeProtectedRequest(
            accessToken = accessToken,
            fallbackMessage = "Unable to submit report right now."
        ) { activeToken ->
            executeWithConfiguredEndpoint(BuildConfig.REPORT_URL) { url ->
                authApiService.reportUser(
                    url = url,
                    sessionToken = sessionToken(activeToken),
                    request = request
                )
            }
        }
    }

    suspend fun unblockUser(
        accessToken: String,
        request: UnblockUserRequest
    ): Result<UnblockUserResponse> = runCatching {
        executeProtectedRequest(
            accessToken = accessToken,
            fallbackMessage = "Unable to unblock user right now."
        ) { activeToken ->
            executeWithConfiguredEndpoint(BuildConfig.UNBLOCK_USER_URL) { url ->
                authApiService.unblockUser(
                    url = url,
                    sessionToken = sessionToken(activeToken),
                    request = request
                )
            }
        }
    }

    suspend fun getWalletSummary(
        accessToken: String
    ): Result<WalletSummaryResponse> = runCatching {
        executeProtectedRequest(
            accessToken = accessToken,
            fallbackMessage = "Unable to load wallet right now."
        ) { activeToken ->
            executeWithConfiguredEndpoint(BuildConfig.WALLET_SUMMARY_URL) { url ->
                authApiService.getWalletSummary(
                    url = url,
                    sessionToken = sessionToken(activeToken)
                )
            }
        }
    }

    suspend fun recordWalletTransaction(
        accessToken: String,
        request: RecordWalletTransactionRequest
    ): Result<RecordWalletTransactionResponse> = runCatching {
        executeProtectedRequest(
            accessToken = accessToken,
            fallbackMessage = "Unable to record wallet transaction right now."
        ) { activeToken ->
            executeWithConfiguredEndpoint(BuildConfig.RECORD_WALLET_TRANSACTION_URL) { url ->
                authApiService.recordWalletTransaction(
                    url = url,
                    sessionToken = sessionToken(activeToken),
                    request = request
                )
            }
        }
    }

    suspend fun getHostDashboard(
        accessToken: String
    ): Result<HostDashboardResponse> = runCatching {
        executeProtectedRequest(
            accessToken = accessToken,
            fallbackMessage = "Unable to load host dashboard right now."
        ) { activeToken ->
            executeWithConfiguredEndpoint(BuildConfig.HOST_DASHBOARD_URL) { url ->
                authApiService.getHostDashboard(
                    url = url,
                    sessionToken = sessionToken(activeToken)
                )
            }
        }
    }

    suspend fun saveHostSettings(
        accessToken: String,
        request: SaveHostSettingsRequest
    ): Result<UserProfileResponse> = runCatching {
        executeProtectedRequest(
            accessToken = accessToken,
            fallbackMessage = "Unable to save host settings right now."
        ) { activeToken ->
            executeWithConfiguredEndpoint(BuildConfig.SAVE_HOST_SETTINGS_URL) { url ->
                authApiService.saveHostSettings(
                    url = url,
                    sessionToken = sessionToken(activeToken),
                    request = request
                )
            }
        }
    }

    suspend fun getHostKyc(
        accessToken: String
    ): Result<HostKycResponse> = runCatching {
        executeProtectedRequest(
            accessToken = accessToken,
            fallbackMessage = "Unable to load KYC details right now."
        ) { activeToken ->
            executeWithConfiguredEndpoint(BuildConfig.HOST_KYC_URL) { url ->
                authApiService.getHostKyc(
                    url = url,
                    sessionToken = sessionToken(activeToken)
                )
            }
        }
    }

    suspend fun saveHostKyc(
        accessToken: String,
        request: HostKycRequest
    ): Result<HostKycResponse> = runCatching {
        executeProtectedRequest(
            accessToken = accessToken,
            fallbackMessage = "Unable to submit KYC right now."
        ) { activeToken ->
            executeWithConfiguredEndpoint(BuildConfig.HOST_KYC_URL) { url ->
                authApiService.saveHostKyc(
                    url = url,
                    sessionToken = sessionToken(activeToken),
                    request = request
                )
            }
        }
    }

    suspend fun uploadHostMedia(
        accessToken: String,
        file: File,
        mimeType: String,
        purpose: String
    ): Result<UploadHostMediaResponse> = runCatching {
        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData(
            name = "file",
            filename = file.name,
            body = requestBody
        )
        val purposeBody = purpose.toRequestBody("text/plain".toMediaTypeOrNull())
        executeProtectedRequest(
            accessToken = accessToken,
            fallbackMessage = "Unable to upload host media right now."
        ) { activeToken ->
            executeWithConfiguredEndpoint(BuildConfig.UPLOAD_HOST_MEDIA_URL) { url ->
                authApiService.uploadHostMedia(
                    url = url,
                    sessionToken = sessionToken(activeToken),
                    file = filePart,
                    purpose = purposeBody
                )
            }
        }
    }

    private suspend fun refreshSessionInternal(): RefreshSessionResponse {
        val refreshToken = sessionManager.getRefreshToken().trim()
        if (refreshToken.isBlank()) {
            throw IllegalStateException("Please login again.")
        }

        val response = executeWithConfiguredEndpoint(BuildConfig.REFRESH_SESSION_URL) { url ->
            authApiService.refreshSession(
                url = url,
                request = RefreshSessionRequest(refreshToken = refreshToken)
            )
        }

        if (response.isSuccessful) {
            return response.body() ?: throw IllegalStateException("Empty response from server.")
        }

        val message = parseError(response.errorBody()?.string(), "Session expired. Please login again.")
        if (shouldLogoutOnRefreshFailure(message)) {
            sessionManager.logout()
        }
        throw IllegalStateException(message)
    }

    private suspend fun <T> executeProtectedRequest(
        accessToken: String,
        fallbackMessage: String,
        requestBlock: suspend (String) -> Response<T>
    ): T {
        val initialToken = resolveAccessToken(accessToken)
        val initialResponse = requestBlock(initialToken)
        if (initialResponse.isSuccessful) {
            return initialResponse.body() ?: throw IllegalStateException("Empty response from server.")
        }

        val initialError = parseError(initialResponse.errorBody()?.string(), fallbackMessage)
        if (!shouldRefreshSession(initialError)) {
            throw IllegalStateException(initialError)
        }

        val refreshedSession = refreshSessionInternal()
        sessionManager.saveRefreshedSession(refreshedSession)
        val retryResponse = requestBlock(refreshedSession.accessToken)
        if (retryResponse.isSuccessful) {
            return retryResponse.body() ?: throw IllegalStateException("Empty response from server.")
        }

        throw IllegalStateException(parseError(retryResponse.errorBody()?.string(), fallbackMessage))
    }

    private fun resolveAccessToken(candidateToken: String): String {
        val trimmedCandidate = candidateToken.trim()
        if (trimmedCandidate.isNotBlank()) {
            return trimmedCandidate
        }
        return sessionManager.getAccessToken().trim()
    }

    private fun shouldRefreshSession(errorMessage: String): Boolean {
        val normalized = errorMessage.trim().lowercase()
        return normalized.contains("access token expired") ||
            normalized.contains("missing session token") ||
            normalized.contains("invalid access token signature") ||
            normalized.contains("malformed access token") ||
            normalized.contains("access token subject is missing")
    }

    private fun shouldLogoutOnRefreshFailure(errorMessage: String): Boolean {
        val normalized = errorMessage.trim().lowercase()
        return normalized.contains("refresh token expired") ||
            normalized.contains("invalid refresh token signature") ||
            normalized.contains("malformed refresh token") ||
            normalized.contains("refresh token subject is missing")
    }

    private suspend fun <T> executeWithConfiguredEndpoint(
        primaryUrl: String,
        requestBlock: suspend (String) -> Response<T>
    ): Response<T> {
        return requestBlock(primaryUrl)
    }

    private fun parseError(rawBody: String?, fallbackMessage: String): String {
        if (rawBody.isNullOrBlank()) {
            return fallbackMessage
        }
        return runCatching {
            gson.fromJson(rawBody, ApiErrorResponse::class.java)?.message
        }.getOrNull().orEmpty().ifBlank { fallbackMessage }
    }
}
