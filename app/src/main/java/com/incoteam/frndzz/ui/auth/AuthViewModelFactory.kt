package com.incoteam.frndzz.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.incoteam.frndzz.data.repository.AuthRepository
import com.incoteam.frndzz.ui.auth.login.LoginViewModel
import com.incoteam.frndzz.ui.auth.otp.OtpVerificationViewModel

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(authRepository) as T
            modelClass.isAssignableFrom(OtpVerificationViewModel::class.java) -> OtpVerificationViewModel(authRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
        }
    }
}
