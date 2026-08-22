package com.nilian.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nilian.app.core.datastore.SecurityPreferences
import com.nilian.app.domain.model.ThemeMode
import com.nilian.app.domain.usecase.JsonBackupRestoreUseCase
import com.nilian.app.presentation.settings.AppThemeMode
import com.nilian.app.presentation.settings.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val securityPreferences: SecurityPreferences,
    private val jsonBackupRestoreUseCase: JsonBackupRestoreUseCase
) : ViewModel() {

    private val _isAutoLockEnabled = MutableStateFlow(true)
    private val _autoRolloverDefault = MutableStateFlow(true)
    private val _dailyMaxFocusHoursWarning = MutableStateFlow(10)
    private val _isChangePinDialogVisible = MutableStateFlow(false)
    private val _isClearDataDialogVisible = MutableStateFlow(false)
    private val _backupSuccessMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        securityPreferences.themeMode,
        securityPreferences.biometricsEnabled,
        _isAutoLockEnabled,
        _autoRolloverDefault,
        _dailyMaxFocusHoursWarning,
        _isChangePinDialogVisible,
        _isClearDataDialogVisible,
        _backupSuccessMessage
    ) { params ->
        val tm = params[0] as ThemeMode
        val biometrics = params[1] as Boolean
        val autoLock = params[2] as Boolean
        val autoRollover = params[3] as Boolean
        val maxHours = params[4] as Int
        val isPinDialog = params[5] as Boolean
        val isClearDialog = params[6] as Boolean
        val backupMsg = params[7] as String?

        val appThemeMode = when (tm) {
            ThemeMode.SYSTEM -> AppThemeMode.SYSTEM
            ThemeMode.LIGHT -> AppThemeMode.LIGHT
            ThemeMode.DARK -> AppThemeMode.DARK
        }

        SettingsUiState(
            themeMode = appThemeMode,
            isBiometricsEnabled = biometrics,
            isAutoLockEnabled = autoLock,
            autoRolloverDefault = autoRollover,
            dailyMaxFocusHoursWarning = maxHours,
            appVersion = "1.0.0-calm",
            isChangePinDialogVisible = isPinDialog,
            isClearDataDialogVisible = isClearDialog,
            backupSuccessMessage = backupMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun onThemeModeChange(mode: AppThemeMode) {
        viewModelScope.launch {
            val domainTheme = when (mode) {
                AppThemeMode.SYSTEM -> ThemeMode.SYSTEM
                AppThemeMode.LIGHT -> ThemeMode.LIGHT
                AppThemeMode.DARK -> ThemeMode.DARK
            }
            securityPreferences.setThemeMode(domainTheme)
        }
    }

    fun onBiometricsToggle(enabled: Boolean) {
        viewModelScope.launch {
            securityPreferences.setBiometricsEnabled(enabled)
        }
    }

    fun onAutoLockToggle(enabled: Boolean) {
        _isAutoLockEnabled.value = enabled
    }

    fun onAutoRolloverToggle(enabled: Boolean) {
        _autoRolloverDefault.value = enabled
    }

    fun onDailyMaxHoursChange(hours: Int) {
        _dailyMaxFocusHoursWarning.value = hours
    }

    fun onChangePinClick() {
        _isChangePinDialogVisible.value = true
    }

    fun onDismissChangePinDialog() {
        _isChangePinDialogVisible.value = false
    }

    fun onSaveNewPin(pin: String) {
        _isChangePinDialogVisible.value = false
        viewModelScope.launch {
            securityPreferences.setPin(pin)
            _backupSuccessMessage.value = "PIN başarıyla güncellendi."
        }
    }

    fun onExportBackupClick(onJsonExported: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            val json = jsonBackupRestoreUseCase.createBackupJson()
            _backupSuccessMessage.value = "Yedek JSON başarıyla oluşturuldu (${json.length} karakter)."
            onJsonExported?.invoke(json)
        }
    }

    fun onImportBackupClick(jsonString: String? = null) {
        if (jsonString != null) {
            viewModelScope.launch {
                val result = jsonBackupRestoreUseCase.restoreBackupJson(jsonString, replaceExisting = true)
                if (result.success) {
                    _backupSuccessMessage.value = "Yedek başarıyla geri yüklendi: ${result.tasksCount} görev, ${result.eventsCount} etkinlik, ${result.habitsCount} alışkanlık."
                } else {
                    _backupSuccessMessage.value = "Hata: ${result.errorMessage}"
                }
            }
        }
    }

    fun onClearDataClick() {
        _isClearDataDialogVisible.value = true
    }

    fun onDismissClearDataDialog() {
        _isClearDataDialogVisible.value = false
    }

    fun onConfirmClearData() {
        _isClearDataDialogVisible.value = false
        viewModelScope.launch {
            securityPreferences.clearPin()
            _backupSuccessMessage.value = "Tüm veriler ve güvenlik tercihleri sıfırlandı."
        }
    }

    class Factory(
        private val securityPreferences: SecurityPreferences,
        private val jsonBackupRestoreUseCase: JsonBackupRestoreUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(securityPreferences, jsonBackupRestoreUseCase) as T
        }
    }
}
