package com.incoteam.frndzz.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incoteam.frndzz.core.common.UiState
import com.incoteam.frndzz.data.model.auth.SendOtpResponse
import com.incoteam.frndzz.data.repository.AuthRepository
import com.incoteam.frndzz.utils.PhoneNumberFormatter
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _sendOtpState = MutableLiveData<UiState<SendOtpResponse>>(UiState.Idle)
    val sendOtpState: LiveData<UiState<SendOtpResponse>> = _sendOtpState

    fun sendOtp(rawPhoneNumber: String) {
        val phoneNumber = PhoneNumberFormatter.toIndianE164(rawPhoneNumber)
        if (phoneNumber.isBlank()) {
            _sendOtpState.value = UiState.Error("Enter a valid 10-digit mobile number.")
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
