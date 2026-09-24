package com.incoteam.realsaathi.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incoteam.realsaathi.core.common.UiState
import com.incoteam.realsaathi.data.model.auth.SendOtpResponse
import com.incoteam.realsaathi.data.repository.AuthRepository
import com.incoteam.realsaathi.utils.PhoneNumberFormatter
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _sendOtpState = MutableLiveData<UiState<SendOtpResponse>>(UiState.Idle)
    val sendOtpState: LiveData<UiState<SendOtpResponse>> = _sendOtpState

    fun sendOtp(rawPhoneNumber: String, dialCode: String) {
        val phoneNumber = PhoneNumberFormatter.toE164(rawPhoneNumber, dialCode)
        if (phoneNumber.isBlank()) {
            _sendOtpState.value = UiState.Error("Enter a valid mobile number.")
            return
        }

        viewModelScope.launch {
            _sendOtpState.value = UiState.Loading
            authRepository.sendOtp(phoneNumber)
                .onSuccess { response ->
                    _sendOtpState.value = UiState.Success(response)
                }
                .onFailure { throwable ->
                    _sendOtpState.value =
                        UiState.Error(throwable.message ?: "Something went wrong. Please try again.")
                }
        }
    }

    fun resetState() {
        _sendOtpState.value = UiState.Idle
    }
}
