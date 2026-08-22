package com.nilian.app.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilian.app.core.ui.components.CalmCard
import com.nilian.app.core.ui.components.CalmTextField
import com.nilian.app.core.ui.components.NilianTopAppBar
import com.nilian.app.core.ui.theme.CardShapeLarge
import com.nilian.app.core.ui.theme.CardShapeMedium
import com.nilian.app.core.ui.theme.DialogShape
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.core.ui.theme.PillShape
import com.nilian.app.core.ui.theme.SagePrimary
import com.nilian.app.core.ui.theme.extendedColors

enum class AppThemeMode(val label: String) {
    SYSTEM("System Default"),
    DARK("Charcoal Dark"),
    LIGHT("Paper Light")
}

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isBiometricsEnabled: Boolean = true,
    val isAutoLockEnabled: Boolean = true,
    val autoRolloverDefault: Boolean = true,
    val dailyMaxFocusHoursWarning: Int = 10,
    val appVersion: String = "1.0.0-calm",
    val isChangePinDialogVisible: Boolean = false,
    val isClearDataDialogVisible: Boolean = false,
    val backupSuccessMessage: String? = null
)

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onBiometricsToggle: (Boolean) -> Unit,
    onAutoLockToggle: (Boolean) -> Unit,
    onAutoRolloverToggle: (Boolean) -> Unit,
    onDailyMaxHoursChange: (Int) -> Unit,
    onChangePinClick: () -> Unit,
    onSaveNewPin: (String) -> Unit,
    onDismissChangePinDialog: () -> Unit,
    onExportBackupClick: () -> Unit,
    onImportBackupClick: () -> Unit,
    onClearDataClick: () -> Unit,
    onConfirmClearData: () -> Unit,
    onDismissClearDataDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NilianTopAppBar(
                title = "Settings & Privacy",
                subtitle = "Preferences, security & local backup"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Appearance / Theme Section
            item {
                SettingsSectionHeader(title = "Appearance", icon = Icons.Default.Palette)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemeOptionCard(
                        title = "System",
                        icon = Icons.Default.BrightnessAuto,
                        isSelected = uiState.themeMode == AppThemeMode.SYSTEM,
                        onClick = { onThemeModeChange(AppThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionCard(
                        title = "Dark",
                        icon = Icons.Default.DarkMode,
                        isSelected = uiState.themeMode == AppThemeMode.DARK,
                        onClick = { onThemeModeChange(AppThemeMode.DARK) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionCard(
                        title = "Light",
                        icon = Icons.Default.LightMode,
                        isSelected = uiState.themeMode == AppThemeMode.LIGHT,
                        onClick = { onThemeModeChange(AppThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Security & Lock Section
            item {
                SettingsSectionHeader(title = "Security & Master PIN", icon = Icons.Outlined.Lock)
                Spacer(modifier = Modifier.height(10.dp))

                CalmCard(shape = CardShapeLarge) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Change PIN Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChangePinClick() }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Change Master PIN",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Update your local unlock passcode",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = SagePrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )

                        // Biometrics Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Biometric Unlock",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Use fingerprint or face unlock",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.isBiometricsEnabled,
                                onCheckedChange = onBiometricsToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SagePrimary
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )

                        // Auto-lock on background
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Auto-Lock App",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Lock immediately when app is minimized",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.isAutoLockEnabled,
                                onCheckedChange = onAutoLockToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SagePrimary
                                )
                            )
                        }
                    }
                }
            }

            // 3. Smart Rules & Workload Balance
            item {
                SettingsSectionHeader(title = "Mindful Rules & Balance", icon = Icons.Outlined.Tune)
                Spacer(modifier = Modifier.height(10.dp))

                CalmCard(shape = CardShapeLarge) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Default Auto-Rollover",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Automatically move yesterday's uncompleted tasks to today",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = uiState.autoRolloverDefault,
                                onCheckedChange = onAutoRolloverToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SagePrimary
                                )
                            )
                        }
                    }
                }
            }

            // 4. Offline Data & JSON Backup
            item {
                SettingsSectionHeader(title = "Local Data & JSON Backup", icon = Icons.Outlined.Shield)
                Spacer(modifier = Modifier.height(10.dp))

                CalmCard(shape = CardShapeLarge) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "Your data stays 100% on your device. You can export a JSON backup at any time to preserve or migrate your schedule.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onExportBackupClick,
                                modifier = Modifier.weight(1f),
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                            ) {
                                Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export JSON")
                            }

                            OutlinedButton(
                                onClick = onImportBackupClick,
                                modifier = Modifier.weight(1f),
                                shape = PillShape
                            ) {
                                Icon(Icons.Outlined.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import JSON")
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )

                        // Clear All Data
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onClearDataClick() }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Clear All Local Data",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.error
                            )
                            Icon(
                                imageVector = Icons.Outlined.DeleteForever,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 5. About Nilian
            item {
                SettingsSectionHeader(title = "About", icon = Icons.Outlined.Info)
                Spacer(modifier = Modifier.height(10.dp))

                CalmCard(
                    shape = CardShapeLarge,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Nilian Personal Life OS",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "v${uiState.appVersion}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SagePrimary
                            )
                        }

                        Text(
                            text = "A pressure-free, offline-first personal operating system designed for students and builders to declutter minds and structure daily momentum.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    // Change PIN Dialog
    if (uiState.isChangePinDialogVisible) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = onDismissChangePinDialog,
            shape = DialogShape,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Set New Master PIN",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CalmTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 6) newPin = it },
                        label = "New PIN (4-6 digits)"
                    )
                    CalmTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 6) confirmPin = it },
                        label = "Confirm New PIN"
                    )
                    if (pinError != null) {
                        Text(
                            text = pinError.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPin.length < 4) {
                            pinError = "PIN must be at least 4 digits."
                        } else if (newPin != confirmPin) {
                            pinError = "PINs do not match."
                        } else {
                            onSaveNewPin(newPin)
                            onDismissChangePinDialog()
                        }
                    }
                ) {
                    Text("Save PIN", color = SagePrimary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissChangePinDialog) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Clear Data Confirmation Dialog
    if (uiState.isClearDataDialogVisible) {
        AlertDialog(
            onDismissRequest = onDismissClearDataDialog,
            shape = DialogShape,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Clear All Local Data?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all tasks, time blocks, habits, and goals from this device. This action cannot be undone unless you exported a JSON backup.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmClearData) {
                    Text("Delete Everything", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissClearDataDialog) {
                    Text("Cancel", color = SagePrimary)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SagePrimary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CalmCard(
        shape = CardShapeMedium,
        backgroundColor = if (isSelected) SagePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        borderColor = if (isSelected) SagePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) SagePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) SagePrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(name = "Settings Dark", showBackground = true)
@Composable
private fun SettingsScreenDarkPreview() {
    NilianTheme(darkTheme = true) {
        SettingsScreen(
            uiState = SettingsUiState(
                themeMode = AppThemeMode.DARK,
                isBiometricsEnabled = true,
                isAutoLockEnabled = true
            ),
            onThemeModeChange = {},
            onBiometricsToggle = {},
            onAutoLockToggle = {},
            onAutoRolloverToggle = {},
            onDailyMaxHoursChange = {},
            onChangePinClick = {},
            onSaveNewPin = {},
            onDismissChangePinDialog = {},
            onExportBackupClick = {},
            onImportBackupClick = {},
            onClearDataClick = {},
            onConfirmClearData = {},
            onDismissClearDataDialog = {}
        )
    }
}
