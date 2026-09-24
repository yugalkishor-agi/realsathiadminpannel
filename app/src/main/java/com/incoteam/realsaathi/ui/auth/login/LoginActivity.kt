package com.incoteam.realsaathi.ui.auth.login

import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.incoteam.realsaathi.RealSaathiApp
import com.incoteam.realsaathi.R
import com.incoteam.realsaathi.core.network.isInternetAvailable
import com.incoteam.realsaathi.core.network.registerInternetAvailabilityCallback
import com.incoteam.realsaathi.core.network.unregisterInternetAvailabilityCallback
import com.incoteam.realsaathi.core.ui.withStableFontScale
import com.incoteam.realsaathi.core.common.UiState
import com.incoteam.realsaathi.core.session.SessionManager
import com.incoteam.realsaathi.databinding.ActivityLoginBinding
import com.incoteam.realsaathi.ui.auth.AuthViewModelFactory
import com.incoteam.realsaathi.ui.auth.AuthLayoutTuner
import com.incoteam.realsaathi.ui.auth.otp.OtpVerificationActivity
import com.incoteam.realsaathi.ui.home.HomeActivity
import com.incoteam.realsaathi.ui.home.NoInternetBlockingScreen
import com.incoteam.realsaathi.ui.theme.RealSaathiTheme
import com.incoteam.realsaathi.utils.PhoneNumberFormatter
import com.incoteam.realsaathi.utils.CountryCode
import com.incoteam.realsaathi.utils.CountryCodes
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private var hasRequestedPhoneHint = false
    private var pendingPhoneNumber: String = ""
    private var isInternetAvailableNow = false
    private var isSendOtpLoading = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var selectedCountry: CountryCode = CountryCodes.all.first()

    private val viewModel: LoginViewModel by viewModels {
        AuthViewModelFactory((application as RealSaathiApp).authRepository)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withStableFontScale())
    }

    private val phoneHintLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK || result.data == null) {
            return@registerForActivityResult
        }

        runCatching {
            Identity.getSignInClient(this).getPhoneNumberFromIntent(result.data)
        }.onSuccess { phoneNumber ->
            val subscriberNumber = PhoneNumberFormatter.toIndianSubscriberNumber(phoneNumber)
            if (subscriberNumber.isNotBlank()) {
                binding.editPhone.setText(subscriberNumber)
                binding.editPhone.setSelection(subscriberNumber.length)
                showPhoneMessage(getString(R.string.login_number_note), isError = false)
            }
        }.onFailure { throwable ->
            if (throwable !is ApiException) {
                throw throwable
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isInternetAvailableNow = isInternetAvailable()
        sessionManager = (application as RealSaathiApp).sessionManager
        if (sessionManager.hasActiveSession()) {
            navigateToHome()
            return
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.textCountryCode.text = "${selectedCountry.dialCode}  ▾"
        AuthLayoutTuner.apply(
            root = binding.root,
            contentContainer = binding.contentContainer,
            bottomContent = binding.bottomContent,
            regularTopPaddingDp = 118,
            compactTopPaddingDp = 76
        )
        setupOfflineOverlay()
        networkCallback = registerInternetAvailabilityCallback { available ->
            runOnUiThread {
                isInternetAvailableNow = available
                renderConnectivityState()
            }
        }

        setupListeners()
        observeViewModel()
        renderConnectivityState()
        if (savedInstanceState == null) {
            binding.root.post { requestPhoneNumberHintIfAvailable() }
        }
    }

    private fun setupListeners() {
        binding.buttonSendOtp.setOnClickListener { submitPhoneNumber() }
        binding.textCountryCode.setOnClickListener { showCountryPicker() }
        binding.textTermsLink.setOnClickListener {
            openExternalPage("https://realsaathi.in/terms.html")
        }
        binding.textPrivacyLink.setOnClickListener {
            openExternalPage("https://realsaathi.in/privacy.html")
        }

        binding.editPhone.doAfterTextChanged {
            viewModel.resetState()
            renderConnectivityState()
        }

        binding.editPhone.setOnEditorActionListener { _, actionId, event ->
            val isDoneAction = actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO
            val isEnterKey =
                event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP
            if (isDoneAction || isEnterKey) {
                submitPhoneNumber()
                true
            } else {
                false
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

    private fun observeViewModel() {
        viewModel.sendOtpState.observe(this) { state ->
            when (state) {
                UiState.Idle -> renderLoading(false)
                UiState.Loading -> renderLoading(true)
                is UiState.Success -> {
                    renderLoading(false)
                    openOtpScreen(pendingPhoneNumber, state.data.requestId)
                }

                is UiState.Error -> {
                    renderLoading(false)
                    showPhoneMessage(state.message, isError = true)
                }
            }
        }
    }

    private fun requestPhoneNumberHintIfAvailable() {
        if (hasRequestedPhoneHint || !binding.editPhone.text.isNullOrBlank()) {
            return
        }
        hasRequestedPhoneHint = true

        val request = GetPhoneNumberHintIntentRequest.builder().build()
        Identity.getSignInClient(this)
            .getPhoneNumberHintIntent(request)
            .addOnSuccessListener { pendingIntent ->
                val intentSenderRequest = IntentSenderRequest.Builder(
                    pendingIntent.intentSender
                ).build()
                phoneHintLauncher.launch(intentSenderRequest)
            }
            .addOnFailureListener {
                // No eligible phone number hint was available, so manual entry stays in place.
            }
    }


    private fun showPhoneMessage(message: String, isError: Boolean) {
        val textColor = if (isError) {
            getColor(R.color.auth_error)
        } else {
            getColor(R.color.auth_text_hint)
        }
        val strokeColor = if (isError) {
            getColor(R.color.auth_error)
        } else {
            getColor(R.color.auth_surface_stroke)
        }

        binding.textPhoneHelper.text = message
        binding.textPhoneHelper.setTextColor(textColor)
        binding.layoutPhone.strokeColor = strokeColor
    }

    private fun renderLoading(isLoading: Boolean) {
        isSendOtpLoading = isLoading
        binding.buttonSendOtp.isEnabled = !isLoading && isInternetAvailableNow
        binding.editPhone.isEnabled = !isLoading && isInternetAvailableNow
        binding.buttonSendOtp.text =
            if (isLoading) getString(R.string.login_sending_otp) else getString(R.string.login_send_otp)
        binding.progressSendOtp.isVisible = isLoading
        binding.buttonSendOtp.alpha = if (binding.buttonSendOtp.isEnabled) 1f else 0.55f
    }

    private fun submitPhoneNumber() {
        if (!isInternetAvailableNow) {
            renderConnectivityState()
            return
        }
        val rawPhoneNumber = binding.editPhone.text?.toString().orEmpty()
        val phoneNumber = PhoneNumberFormatter.toE164(rawPhoneNumber, selectedCountry.dialCode)
        if (phoneNumber.isBlank()) {
            showPhoneMessage(getString(R.string.validation_invalid_phone), isError = true)
            return
        }

        pendingPhoneNumber = phoneNumber
        viewModel.sendOtp(rawPhoneNumber, selectedCountry.dialCode)
    }

    private fun showCountryPicker() {
        val labels = CountryCodes.all.map { "${it.name}  ${it.dialCode}" }.toTypedArray()
        val selectedIndex = CountryCodes.all.indexOf(selectedCountry).coerceAtLeast(0)
        MaterialAlertDialogBuilder(this)
            .setTitle("Select country code")
            .setSingleChoiceItems(labels, selectedIndex) { dialog, which ->
                selectedCountry = CountryCodes.all[which]
                binding.textCountryCode.text = "${selectedCountry.dialCode}  ▾"
                dialog.dismiss()
                showPhoneMessage(getString(R.string.login_number_note), isError = false)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openOtpScreen(phoneNumber: String, requestId: String) {
        startActivity(
            Intent(this, OtpVerificationActivity::class.java)
                .putExtra(OtpVerificationActivity.EXTRA_PHONE_NUMBER, phoneNumber)
                .putExtra(OtpVerificationActivity.EXTRA_REQUEST_ID, requestId)
        )
    }

    private fun openExternalPage(url: String) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun renderConnectivityState() {
        if (isInternetAvailableNow) {
            showPhoneMessage(getString(R.string.login_number_note), isError = false)
        } else {
            showPhoneMessage("No internet connection. Turn on internet to continue.", isError = true)
        }
        binding.offlineOverlay.isVisible = !isInternetAvailableNow
        binding.buttonSendOtp.isEnabled = isInternetAvailableNow && !isSendOtpLoading
        binding.editPhone.isEnabled = isInternetAvailableNow && !isSendOtpLoading
        binding.buttonSendOtp.alpha = if (binding.buttonSendOtp.isEnabled) 1f else 0.55f
    }

    private fun showNoInternetMessage() {
        Toast.makeText(this, "No internet connection. Turn on internet.", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        startActivity(
            Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    override fun onDestroy() {
        unregisterInternetAvailabilityCallback(networkCallback)
        super.onDestroy()
    }
}
