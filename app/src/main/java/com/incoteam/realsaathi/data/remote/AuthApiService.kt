package com.incoteam.realsaathi.data.remote

import com.incoteam.realsaathi.data.model.auth.SendOtpRequest
import com.incoteam.realsaathi.data.model.auth.SendOtpResponse
import com.incoteam.realsaathi.data.model.auth.DiscoveryHostsResponse
import com.incoteam.realsaathi.data.model.auth.RefreshSessionRequest
import com.incoteam.realsaathi.data.model.auth.RefreshSessionResponse
import com.incoteam.realsaathi.data.model.auth.LanguagesResponse
import com.incoteam.realsaathi.data.model.auth.RandomMatchRequest
import com.incoteam.realsaathi.data.model.auth.RandomMatchResponse
import com.incoteam.realsaathi.data.model.auth.RecordWalletTransactionRequest
import com.incoteam.realsaathi.data.model.auth.CallEventRequest
import com.incoteam.realsaathi.data.model.auth.RecordWalletTransactionResponse
import com.incoteam.realsaathi.data.model.auth.ReportUserRequest
import com.incoteam.realsaathi.data.model.auth.ReportUserResponse
import com.incoteam.realsaathi.data.model.auth.SaveHostSettingsRequest
import com.incoteam.realsaathi.data.model.auth.SaveProfileRequest
import com.incoteam.realsaathi.data.model.auth.SupportChatRequest
import com.incoteam.realsaathi.data.model.auth.SupportChatResponse
import com.incoteam.realsaathi.data.model.auth.UnblockUserRequest
import com.incoteam.realsaathi.data.model.auth.UnblockUserResponse
import com.incoteam.realsaathi.data.model.auth.HostDashboardResponse
import com.incoteam.realsaathi.data.model.auth.HostKycRequest
import com.incoteam.realsaathi.data.model.auth.HostKycResponse
import com.incoteam.realsaathi.data.model.auth.TopicTagsResponse
import com.incoteam.realsaathi.data.model.auth.UploadHostMediaResponse
import com.incoteam.realsaathi.data.model.auth.UserProfileResponse
import com.incoteam.realsaathi.data.model.auth.VerifyOtpRequest
import com.incoteam.realsaathi.data.model.auth.VerifyOtpResponse
import com.incoteam.realsaathi.data.model.auth.WalletSummaryResponse
import com.incoteam.realsaathi.data.model.auth.RechargeOrderRequest
import com.incoteam.realsaathi.data.model.auth.RechargeOrderResponse
import com.incoteam.realsaathi.data.model.auth.ZegoTokenResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url

interface AuthApiService {

    @POST
    suspend fun sendOtp(
        @Url url: String,
        @Body request: SendOtpRequest
    ): Response<SendOtpResponse>

    @POST
    suspend fun verifyOtp(
        @Url url: String,
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse>

    @POST
    suspend fun refreshSession(
        @Url url: String,
        @Body request: RefreshSessionRequest
    ): Response<RefreshSessionResponse>

    @POST
    suspend fun saveProfile(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Body request: SaveProfileRequest
    ): Response<UserProfileResponse>

    @POST
    suspend fun deleteAccount(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String
    ): Response<Map<String, Boolean>>

    @POST
    suspend fun supportChat(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Body request: SupportChatRequest
    ): Response<SupportChatResponse>

    @GET
    suspend fun getTopicTags(
        @Url url: String
    ): Response<TopicTagsResponse>

    @GET
    suspend fun getSupportedLanguages(
        @Url url: String
    ): Response<LanguagesResponse>

    @GET
    suspend fun getDiscoveryHosts(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Query("topicTags") topicTags: List<String> = emptyList(),
        @Query("languages") languages: List<String> = emptyList(),
        @Query("liveOnly") liveOnly: Boolean = true
    ): Response<DiscoveryHostsResponse>

    @POST
    suspend fun requestRandomMatch(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Body request: RandomMatchRequest
    ): Response<RandomMatchResponse>

    @POST
    suspend fun reportUser(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Body request: ReportUserRequest
    ): Response<ReportUserResponse>

    @POST
    suspend fun unblockUser(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Body request: UnblockUserRequest
    ): Response<UnblockUserResponse>

    @GET
    suspend fun getWalletSummary(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String
    ): Response<WalletSummaryResponse>

    @POST
    suspend fun rechargeOrder(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Body request: RechargeOrderRequest
    ): Response<RechargeOrderResponse>

    @POST
    suspend fun getZegoToken(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Body request: Map<String, String> = emptyMap()
    ): Response<ZegoTokenResponse>

    @POST
    suspend fun recordWalletTransaction(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Body request: RecordWalletTransactionRequest
    ): Response<RecordWalletTransactionResponse>

    @POST
    suspend fun recordCallEvent(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Body request: CallEventRequest
    ): Response<RecordWalletTransactionResponse>

    @POST
    suspend fun getHostDashboard(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Body request: Map<String, String> = emptyMap()
    ): Response<HostDashboardResponse>

    @GET
    suspend fun getHostKyc(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String
    ): Response<HostKycResponse>

    @POST
    suspend fun saveHostKyc(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Body request: HostKycRequest
    ): Response<HostKycResponse>

    @POST
    suspend fun saveHostSettings(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Body request: SaveHostSettingsRequest
    ): Response<UserProfileResponse>

    @Multipart
    @POST
    suspend fun uploadHostMedia(
        @Url url: String,
        @Header("X-Session-Token") sessionToken: String,
        @Part file: MultipartBody.Part,
        @Part("purpose") purpose: RequestBody
    ): Response<UploadHostMediaResponse>
}
