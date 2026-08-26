import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun localConfig(name: String, fallback: String): String {
    return (localProperties.getProperty(name) ?: providers.gradleProperty(name).orNull ?: fallback).trim()
}

fun normalizeBaseUrl(baseUrl: String): String {
    return baseUrl.trim().trimEnd('/') + "/"
}

fun authEndpoint(baseUrl: String, authMode: String, backendPath: String, functionName: String): String {
    val normalizedBaseUrl = normalizeBaseUrl(baseUrl)
    return if (authMode.equals("supabase_edge", ignoreCase = true)) {
        normalizedBaseUrl + "functions/v1/" + functionName
    } else {
        normalizedBaseUrl + backendPath
    }
}

val defaultRemoteBaseUrl = "https://qefskbzqbduruznfshao.supabase.co/"
val debugBaseUrl = normalizeBaseUrl(localConfig("FRNDZZ_DEBUG_BASE_URL", defaultRemoteBaseUrl))
val releaseBaseUrl = localConfig("FRNDZZ_RELEASE_BASE_URL", defaultRemoteBaseUrl)
val debugAuthMode = localConfig("FRNDZZ_AUTH_MODE", "supabase_edge")
val releaseAuthMode = localConfig("FRNDZZ_RELEASE_AUTH_MODE", "supabase_edge")
val debugSendOtpUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/auth/otp/send", "send-otp")
val debugVerifyOtpUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/auth/otp/verify", "verify-otp")
val debugRefreshSessionUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/auth/session/refresh", "refresh-session")
val debugSaveProfileUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/profile/save", "save-profile")
val debugSupportChatUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/support/chat", "support-chat")
val debugHostDashboardUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/host/dashboard", "get-host-dashboard")
val debugHostKycUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/host/kyc", "host-kyc")
val debugHostSettingsUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/host/settings/save", "save-host-settings")
val debugUploadHostMediaUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/host/media/upload", "upload-host-media")
val debugConfigTagsUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/config/tags", "config-tags")
val debugConfigLanguagesUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/config/languages", "config-languages")
val debugDiscoveryHostsUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/discovery/hosts", "discovery-hosts")
val debugRandomMatchUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/discovery/random-match", "random-match")
val debugReportUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/report", "report-user")
val debugUnblockUserUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/report/unblock", "unblock-user")
val debugWalletSummaryUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/wallet/summary", "wallet-summary")
val debugRecordWalletTransactionUrl = authEndpoint(debugBaseUrl, debugAuthMode, "v1/wallet/record", "record-wallet-transaction")
val releaseSendOtpUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/auth/otp/send", "send-otp")
val releaseVerifyOtpUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/auth/otp/verify", "verify-otp")
val releaseRefreshSessionUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/auth/session/refresh", "refresh-session")
val releaseSaveProfileUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/profile/save", "save-profile")
val releaseSupportChatUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/support/chat", "support-chat")
val releaseHostDashboardUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/host/dashboard", "get-host-dashboard")
val releaseHostKycUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/host/kyc", "host-kyc")
val releaseHostSettingsUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/host/settings/save", "save-host-settings")
val releaseUploadHostMediaUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/host/media/upload", "upload-host-media")
val releaseConfigTagsUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/config/tags", "config-tags")
val releaseConfigLanguagesUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/config/languages", "config-languages")
val releaseDiscoveryHostsUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/discovery/hosts", "discovery-hosts")
val releaseRandomMatchUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/discovery/random-match", "random-match")
val releaseReportUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/report", "report-user")
val releaseUnblockUserUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/report/unblock", "unblock-user")
val releaseWalletSummaryUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/wallet/summary", "wallet-summary")
val releaseRecordWalletTransactionUrl = authEndpoint(releaseBaseUrl, releaseAuthMode, "v1/wallet/record", "record-wallet-transaction")
val adbExecutable = sequenceOf(
    localProperties.getProperty("sdk.dir")?.let { File(it, "platform-tools/adb.exe") },
    localProperties.getProperty("sdk.dir")?.let { File(it, "platform-tools/adb") },
    System.getenv("ANDROID_HOME")?.let { File(it, "platform-tools/adb.exe") },
    System.getenv("ANDROID_HOME")?.let { File(it, "platform-tools/adb") },
    System.getenv("ANDROID_SDK_ROOT")?.let { File(it, "platform-tools/adb.exe") },
    System.getenv("ANDROID_SDK_ROOT")?.let { File(it, "platform-tools/adb") }
).filterNotNull().firstOrNull { it.exists() }

val reverseDebugBackendPort by tasks.registering {
    group = "android"
    description = "Routes a physical device to the local Frndzz backend during development."

    doLast {
        val adb = adbExecutable
        if (adb == null) {
            logger.warn("ADB not found; skipping backend port reverse.")
            return@doLast
        }

        val process = ProcessBuilder(adb.absolutePath, "reverse", "tcp:4000", "tcp:4000")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            logger.warn("ADB reverse skipped: $output")
        }
    }
}

android {
    namespace = "com.incoteam.frndzz"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.incoteam.frndzz"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"$debugBaseUrl\"")
            buildConfigField("String", "SEND_OTP_URL", "\"$debugSendOtpUrl\"")
            buildConfigField("String", "VERIFY_OTP_URL", "\"$debugVerifyOtpUrl\"")
            buildConfigField("String", "REFRESH_SESSION_URL", "\"$debugRefreshSessionUrl\"")
            buildConfigField("String", "SAVE_PROFILE_URL", "\"$debugSaveProfileUrl\"")
            buildConfigField("String", "SUPPORT_CHAT_URL", "\"$debugSupportChatUrl\"")
            buildConfigField("String", "HOST_DASHBOARD_URL", "\"$debugHostDashboardUrl\"")
            buildConfigField("String", "HOST_KYC_URL", "\"$debugHostKycUrl\"")
            buildConfigField("String", "SAVE_HOST_SETTINGS_URL", "\"$debugHostSettingsUrl\"")
            buildConfigField("String", "UPLOAD_HOST_MEDIA_URL", "\"$debugUploadHostMediaUrl\"")
            buildConfigField("String", "CONFIG_TAGS_URL", "\"$debugConfigTagsUrl\"")
            buildConfigField("String", "CONFIG_LANGUAGES_URL", "\"$debugConfigLanguagesUrl\"")
            buildConfigField("String", "DISCOVERY_HOSTS_URL", "\"$debugDiscoveryHostsUrl\"")
            buildConfigField("String", "RANDOM_MATCH_URL", "\"$debugRandomMatchUrl\"")
            buildConfigField("String", "REPORT_URL", "\"$debugReportUrl\"")
            buildConfigField("String", "UNBLOCK_USER_URL", "\"$debugUnblockUserUrl\"")
            buildConfigField("String", "WALLET_SUMMARY_URL", "\"$debugWalletSummaryUrl\"")
            buildConfigField("String", "RECORD_WALLET_TRANSACTION_URL", "\"$debugRecordWalletTransactionUrl\"")
        }

        release {
            buildConfigField("String", "BASE_URL", "\"${normalizeBaseUrl(releaseBaseUrl)}\"")
            buildConfigField("String", "SEND_OTP_URL", "\"$releaseSendOtpUrl\"")
            buildConfigField("String", "VERIFY_OTP_URL", "\"$releaseVerifyOtpUrl\"")
            buildConfigField("String", "REFRESH_SESSION_URL", "\"$releaseRefreshSessionUrl\"")
            buildConfigField("String", "SAVE_PROFILE_URL", "\"$releaseSaveProfileUrl\"")
            buildConfigField("String", "SUPPORT_CHAT_URL", "\"$releaseSupportChatUrl\"")
            buildConfigField("String", "HOST_DASHBOARD_URL", "\"$releaseHostDashboardUrl\"")
            buildConfigField("String", "HOST_KYC_URL", "\"$releaseHostKycUrl\"")
            buildConfigField("String", "SAVE_HOST_SETTINGS_URL", "\"$releaseHostSettingsUrl\"")
            buildConfigField("String", "UPLOAD_HOST_MEDIA_URL", "\"$releaseUploadHostMediaUrl\"")
            buildConfigField("String", "CONFIG_TAGS_URL", "\"$releaseConfigTagsUrl\"")
            buildConfigField("String", "CONFIG_LANGUAGES_URL", "\"$releaseConfigLanguagesUrl\"")
            buildConfigField("String", "DISCOVERY_HOSTS_URL", "\"$releaseDiscoveryHostsUrl\"")
            buildConfigField("String", "RANDOM_MATCH_URL", "\"$releaseRandomMatchUrl\"")
            buildConfigField("String", "REPORT_URL", "\"$releaseReportUrl\"")
            buildConfigField("String", "UNBLOCK_USER_URL", "\"$releaseUnblockUserUrl\"")
            buildConfigField("String", "WALLET_SUMMARY_URL", "\"$releaseWalletSummaryUrl\"")
            buildConfigField("String", "RECORD_WALLET_TRANSACTION_URL", "\"$releaseRecordWalletTransactionUrl\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }
}

tasks.matching { it.name == "installDebug" }.configureEach {
    dependsOn(reverseDebugBackendPort)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
