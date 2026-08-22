package com.nilian.app.presentation.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nilian.app.core.datastore.SecurityPreferences
import com.nilian.app.core.security.BiometricAuthManager
import com.nilian.app.domain.model.ThemeMode
import com.nilian.app.presentation.lock.LockUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val securityPreferences: SecurityPreferences,
    private val biometricAuthManager: BiometricAuthManager
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = securityPreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _lockUiState = MutableStateFlow(LockUiState())
    val lockUiState: StateFlow<LockUiState> = _lockUiState.asStateFlow()

    private var failedAttempts = 0

    init {
        viewModelScope.launch {
            val isPinSet = securityPreferences.checkIsPinSet()
            if (!isPinSet) {
                // If no PIN is configured, unlock automatically
                _isUnlocked.value = true
            }
        }

        viewModelScope.launch {
            securityPreferences.pinLength.collect { length ->
                _lockUiState.update { it.copy(pinLength = length) }
            }
        }

        viewModelScope.launch {
            securityPreferences.biometricsEnabled.collect { isEnabled ->
                val availability = biometricAuthManager.checkBiometricAvailability()
                _lockUiState.update {
                    it.copy(isBiometricsAvailable = isEnabled && availability.isAvailable)
                }
            }
        }
    }

    fun onDigitClick(digit: Char) {
        val current = _lockUiState.value.currentPin
        if (current.length >= _lockUiState.value.pinLength) return

        val newPin = current + digit
        _lockUiState.update { it.copy(currentPin = newPin, errorMessage = null) }

        if (newPin.length == _lockUiState.value.pinLength) {
            verifyPin(newPin)
        }
    }

    fun onDeleteClick() {
        val current = _lockUiState.value.currentPin
        if (current.isNotEmpty()) {
            _lockUiState.update { it.copy(currentPin = current.dropLast(1), errorMessage = null) }
        }
    }

    fun onBiometricsClick(activity: FragmentActivity) {
        val availability = biometricAuthManager.checkBiometricAvailability()
        if (!availability.isAvailable) {
            _lockUiState.update { it.copy(errorMessage = availability.message) }
            return
        }

        biometricAuthManager.authenticate(
            activity = activity,
            title = "Nilian Giriş",
            subtitle = "Parmak İzi veya Yüz Tanıma ile kilidi açın",
            negativeButtonText = "PIN ile Gir",
            onSuccess = {
                failedAttempts = 0
                _isUnlocked.value = true
                _lockUiState.update { it.copy(currentPin = "", errorMessage = null) }
            },
            onError = { errorCode, errorMessage ->
                // Do not unlock, display message if it was an error other than intentional user cancellation
                if (errorCode != 13 && errorCode != 10) {
                    _lockUiState.update { it.copy(errorMessage = errorMessage) }
                }
            },
            onFailed = {
                _lockUiState.update { it.copy(errorMessage = "Biyometrik doğrulama tanınamadı. Lütfen tekrar deneyin.") }
            }
        )
    }

    fun onForgotPinClick() {
        _lockUiState.update { it.copy(isForgotPinDialogVisible = true) }
    }

    fun onDismissForgotPinDialog() {
        _lockUiState.update { it.copy(isForgotPinDialogVisible = false) }
    }

    fun lockApp() {
        viewModelScope.launch {
            if (securityPreferences.checkIsPinSet()) {
                _isUnlocked.value = false
                _lockUiState.update { it.copy(currentPin = "", errorMessage = null) }
            }
        }
    }

    private fun verifyPin(pin: String) {
        viewModelScope.launch {
            val isValid = securityPreferences.verifyPin(pin)
            if (isValid) {
                failedAttempts = 0
                _isUnlocked.value = true
                _lockUiState.update { it.copy(currentPin = "", errorMessage = null) }
            } else {
                failedAttempts++
                _lockUiState.update {
                    it.copy(
                        currentPin = "",
                        errorMessage = "Hatalı PIN kodu. Lütfen tekrar deneyin."
                    )
                }
            }
        }
    }

    class Factory(
        private val securityPreferences: SecurityPreferences,
        private val biometricAuthManager: BiometricAuthManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(securityPreferences, biometricAuthManager) as T
        }
    }
}
