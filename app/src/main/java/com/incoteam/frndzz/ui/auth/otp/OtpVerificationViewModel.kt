package com.incoteam.frndzz.ui.auth.otp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incoteam.frndzz.core.common.UiState
import com.incoteam.frndzz.data.model.auth.SendOtpResponse
import com.incoteam.frndzz.data.model.auth.VerifyOtpResponse
import com.incoteam.frndzz.data.repository.AuthRepository
import kotlinx.coroutines.launch

class OtpVerificationViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _verifyOtpState = MutableLiveData<UiState<VerifyOtpResponse>>(UiState.Idle)
    val verifyOtpState: LiveData<UiState<VerifyOtpResponse>> = _verifyOtpState

    private val _resendOtpState = MutableLiveData<UiState<SendOtpResponse>>(UiState.Idle)
    val resendOtpState: LiveData<UiState<SendOtpResponse>> = _resendOtpState

    fun verifyOtp(phoneNumber: String, requestId: String, otpCode: String) {
        if (otpCode.length != 6) {
            _verifyOtpState.value = UiState.Error("Enter the 6-digit OTP.")
            return
        }

        viewModelScope.launch {
            _verifyOtpState.value = UiState.Loading
            authRepository.verifyOtp(
                phoneNumber = phoneNumber,
                otpCode = otpCode,
                requestId = requestId
            ).onSuccess { response ->
                _verifyOtpState.value = UiState.Success(response)
            }.onFailure { throwable ->
                _verifyOtpState.value =
                    UiState.Error(throwable.message ?: "Something went wrong. Please try again.")
            }
        }
    }

    fun resendOtp(phoneNumber: String) {
        viewModelScope.launch {
            _resendOtpState.value = UiState.Loading
            authRepository.sendOtp(phoneNumber)
                .onSuccess { response ->
                    _resendOtpState.value = UiState.Success(response)
                }
                .onFailure { throwable ->
                    _resendOtpState.value =
                        UiState.Error(throwable.message ?: "Something went wrong. Please try again.")
                }
        }
    }

    fun resetVerifyState() {
        _verifyOtpState.value = UiState.Idle
    }

    fun resetResendState() {
        _resendOtpState.value = UiState.Idle
    }
}
