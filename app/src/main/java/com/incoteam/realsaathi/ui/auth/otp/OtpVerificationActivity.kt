package com.incoteam.realsaathi.ui.auth.otp

import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.incoteam.realsaathi.RealSaathiApp
import com.incoteam.realsaathi.R
import com.incoteam.realsaathi.core.common.UiState
import com.incoteam.realsaathi.core.network.isInternetAvailable
import com.incoteam.realsaathi.core.network.registerInternetAvailabilityCallback
import com.incoteam.realsaathi.core.network.unregisterInternetAvailabilityCallback
import com.incoteam.realsaathi.core.ui.withStableFontScale
import com.incoteam.realsaathi.databinding.ActivityOtpVerificationBinding
import com.incoteam.realsaathi.ui.auth.AuthLayoutTuner
import com.incoteam.realsaathi.ui.auth.AuthViewModelFactory
import com.incoteam.realsaathi.ui.home.HomeActivity
import com.incoteam.realsaathi.ui.home.NoInternetBlockingScreen
import com.incoteam.realsaathi.ui.home.UserPrefs
import com.incoteam.realsaathi.ui.theme.RealSaathiTheme
import com.incoteam.realsaathi.utils.PhoneNumberFormatter
import android.view.inputmethod.InputMethodManager

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpVerificationBinding
    private var resendTimer: CountDownTimer? = null
    private var otpHasError = false
    private var isVerifyLoading = false
    private var isResendLoading = false
    private var canResendNow = false
    private var isInternetAvailableNow = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private val viewModel: OtpVerificationViewModel by viewModels {
        AuthViewModelFactory((application as RealSaathiApp).authRepository)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withStableFontScale())
    }

    private var currentRequestId: String = ""
    private var phoneNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isInternetAvailableNow = isInternetAvailable()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AuthLayoutTuner.apply(
            root = binding.root,
            contentContainer = binding.contentContainer,
            bottomContent = binding.bottomContent,
            regularTopPaddingDp = 96,
            compactTopPaddingDp = 64
        )
        setupOfflineOverlay()
        networkCallback = registerInternetAvailabilityCallback { available ->
            runOnUiThread {
                isInternetAvailableNow = available
                renderConnectivityState()
            }
        }

        phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER).orEmpty()
        currentRequestId = intent.getStringExtra(EXTRA_REQUEST_ID).orEmpty()

        if (phoneNumber.isBlank()) {
            Toast.makeText(this, getString(R.string.otp_request_missing), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val subscriberNumber = PhoneNumberFormatter.toIndianSubscriberNumber(phoneNumber)
        binding.textOtpPhone.text = subscriberNumber
        binding.textOtpPhone.paintFlags =
            binding.textOtpPhone.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        setupListeners()
        observeViewModel()
        updateOtpBoxes("")
        startRetryCountdown(30)
        renderConnectivityState()
    }

    private fun setupListeners() {
        binding.buttonVerifyOtp.setOnClickListener { submitOtp() }

        binding.buttonResendOtp.setOnClickListener {
            if (binding.buttonResendOtp.isEnabled) {
                viewModel.resendOtp(phoneNumber)
            }
        }

        binding.rowOtpDestination.setOnClickListener {
            finish()
        }

        binding.otpBoxesContainer.setOnClickListener {
            requestOtpFocus()
        }

        binding.rowAutoFetch.setOnClickListener {
            requestOtpFocus()
        }

        binding.editOtp.doAfterTextChanged {
            otpHasError = false
            updateOtpBoxes(it?.toString().orEmpty())
            viewModel.resetVerifyState()
        }

        binding.editOtp.setOnEditorActionListener { _, actionId, event ->
            val isDoneAction = actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO
            val isEnterKey =
                event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP
            if (isDoneAction || isEnterKey) {
                submitOtp()
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        viewModel.verifyOtpState.observe(this) { state ->
            when (state) {
                UiState.Idle -> renderVerifyLoading(false)
                UiState.Loading -> renderVerifyLoading(true)
                is UiState.Success -> {
                    renderVerifyLoading(false)
                    val sessionManager = (application as RealSaathiApp).sessionManager
                    sessionManager.saveAuthSession(state.data)
                    state.data.profile?.let { profile ->
                        UserPrefs.syncFromRemoteProfile(this, profile)
                    }
                    val shouldForceProfileSetup = state.data.isNewUser
                    startActivity(
                        Intent(this, HomeActivity::class.java).apply {
                            putExtra(HomeActivity.EXTRA_FORCE_PROFILE_SETUP, shouldForceProfileSetup)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }

                is UiState.Error -> {
                    renderVerifyLoading(false)
                    otpHasError = true
                    updateOtpBoxes(binding.editOtp.text?.toString().orEmpty())
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.resendOtpState.observe(this) { state ->
            when (state) {
                UiState.Idle -> renderResendLoading(false)
                UiState.Loading -> renderResendLoading(true)
                is UiState.Success -> {
                    renderResendLoading(false)
                    currentRequestId = state.data.requestId
                    Toast.makeText(this, getString(R.string.otp_resend_success), Toast.LENGTH_SHORT).show()
                    startRetryCountdown(if (state.data.retryAfterSeconds > 0) state.data.retryAfterSeconds else 30)
                    viewModel.resetResendState()
                }

                is UiState.Error -> {
                    renderResendLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupOfflineOverlay() {
        binding.offlineOverlay.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.offlineOverlay.setContent {
            RealSaathiTheme(darkTheme = true, dynamicColor = false) {
                NoInternetBlockingScreen(
                    title = "No internet connection",
                    subtitle = "Turn on internet to continue.",
                    hint = "Waiting for network",
                    onTap = ::showNoInternetMessage
                )
            }
        }
    }


    private fun requestOtpFocus() {
        binding.editOtp.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(binding.editOtp, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun updateOtpBoxes(otpValue: String) {
        val cards = listOf(
            binding.cardOtp1,
            binding.cardOtp2,
            binding.cardOtp3,
            binding.cardOtp4,
            binding.cardOtp5,
            binding.cardOtp6
        )
        val texts = listOf(
            binding.textOtpDigit1,
            binding.textOtpDigit2,
            binding.textOtpDigit3,
            binding.textOtpDigit4,
            binding.textOtpDigit5,
            binding.textOtpDigit6
        )

        cards.forEachIndexed { index, card ->
            val char = otpValue.getOrNull(index)?.toString().orEmpty()
            texts[index].text = char
            val isActiveSlot = !otpHasError && otpValue.length == index && otpValue.length < 6
            card.strokeColor = when {
                otpHasError -> getColor(R.color.auth_error)
                isActiveSlot -> getColor(R.color.auth_text_primary)
                else -> getColor(R.color.auth_surface_stroke)
            }
        }

        updateVerifyButtonState()
    }

    private fun startRetryCountdown(seconds: Int) {
        resendTimer?.cancel()
        val safeSeconds = seconds.coerceAtLeast(0)
        if (safeSeconds == 0) {
            canResendNow = true
            binding.buttonResendOtp.text = getString(R.string.otp_retry_now)
            syncResendButtonState()
            return
        }

        canResendNow = false

        resendTimer = object : CountDownTimer(safeSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = (millisUntilFinished / 1000L).toInt()
                val minutes = totalSeconds / 60
                val secondsLeft = totalSeconds % 60
                canResendNow = false
                binding.buttonResendOtp.text =
                    getString(R.string.otp_retry_in, String.format("%02d:%02d", minutes, secondsLeft))
                syncResendButtonState()
            }

            override fun onFinish() {
                canResendNow = true
                binding.buttonResendOtp.text = getString(R.string.otp_retry_now)
                syncResendButtonState()
            }
        }.start()
    }

    private fun updateVerifyButtonState() {
        val isEnabled = isInternetAvailableNow && !isVerifyLoading && binding.editOtp.text?.length == 6
        binding.buttonVerifyOtp.isEnabled = isEnabled
        binding.buttonVerifyOtp.alpha = if (isEnabled) 1f else 0.55f
    }

    private fun renderVerifyLoading(isLoading: Boolean) {
        isVerifyLoading = isLoading
        binding.buttonVerifyOtp.text =
            if (isLoading) getString(R.string.otp_verifying) else getString(R.string.otp_verify)
        binding.progressVerifyOtp.isVisible = isLoading
        binding.editOtp.isEnabled = isInternetAvailableNow && !isLoading
        updateVerifyButtonState()
    }

    private fun submitOtp() {
        if (!isInternetAvailableNow) {
            renderConnectivityState()
            return
        }
        viewModel.verifyOtp(
            phoneNumber = phoneNumber,
            requestId = currentRequestId,
            otpCode = binding.editOtp.text?.toString().orEmpty()
        )
    }

    private fun renderResendLoading(isLoading: Boolean) {
        isResendLoading = isLoading
        syncResendButtonState()
    }

    private fun syncResendButtonState() {
        val isEnabled = isInternetAvailableNow && canResendNow && !isResendLoading
        binding.buttonResendOtp.isEnabled = isEnabled
        binding.buttonResendOtp.alpha = if (isEnabled) 1f else 0.6f
    }

    private fun renderConnectivityState() {
        binding.offlineOverlay.isVisible = !isInternetAvailableNow
        binding.editOtp.isEnabled = isInternetAvailableNow && !isVerifyLoading
        updateVerifyButtonState()
        syncResendButtonState()
        if (!isInternetAvailableNow) {
            binding.editOtp.clearFocus()
        }
    }

    private fun showNoInternetMessage() {
        Toast.makeText(this, "No internet connection. Turn on internet.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        resendTimer?.cancel()
        unregisterInternetAvailabilityCallback(networkCallback)
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        const val EXTRA_REQUEST_ID = "extra_request_id"
    }
}
