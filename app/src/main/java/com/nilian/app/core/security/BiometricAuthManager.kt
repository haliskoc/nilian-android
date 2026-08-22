package com.nilian.app.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricAvailability(val isAvailable: Boolean, val message: String) {
    AVAILABLE(true, "Biyometrik doğrulama (Parmak İzi / Yüz Tanıma) hazır."),
    NO_HARDWARE(false, "Bu cihazda parmak izi veya yüz tanıma donanımı bulunmuyor."),
    HW_UNAVAILABLE(false, "Biyometrik sensör şu an kullanılamıyor."),
    NONE_ENROLLED(false, "Cihazınızda kayıtlı bir parmak izi veya yüz tanıma bulunmuyor. Android ayarlarından ekleyebilirsiniz."),
    SECURITY_UPDATE_REQUIRED(false, "Biyometrik sensör için güvenlik güncellemesi gerekiyor."),
    UNSUPPORTED(false, "Biyometrik kilit bu cihazda desteklenmiyor.")
}

/**
 * Robust Biometric Authentication Manager supporting Fingerprint and Face Recognition.
 * Fully compliant with AndroidX Biometric standards.
 */
class BiometricAuthManager(private val context: Context) {

    private val authenticators = BIOMETRIC_STRONG or BIOMETRIC_WEAK

    /**
     * Checks if biometric hardware is present and user has enrolled biometric credentials.
     */
    fun checkBiometricAvailability(): BiometricAvailability {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HW_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.UNSUPPORTED
            else -> BiometricAvailability.UNSUPPORTED
        }
    }

    /**
     * Displays the native Android BiometricPrompt for Fingerprint or Face Recognition.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Nilian Giriş",
        subtitle: String = "Parmak izi veya yüz tanıma ile kilidi açın",
        negativeButtonText: String = "PIN ile Gir",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errorMessage: String) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        val availability = checkBiometricAvailability()
        if (!availability.isAvailable) {
            onError(-1, availability.message)
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(false) // Smooth face unlock experience without extra tap
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptInfo)
    }
}
