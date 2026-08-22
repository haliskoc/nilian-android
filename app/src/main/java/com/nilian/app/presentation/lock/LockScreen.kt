package com.nilian.app.presentation.lock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilian.app.core.ui.components.CalmCard
import com.nilian.app.core.ui.theme.DialogShape
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.core.ui.theme.PillShape
import com.nilian.app.core.ui.theme.SagePrimary
import kotlin.math.roundToInt

data class LockUiState(
    val pinLength: Int = 4,
    val currentPin: String = "",
    val errorMessage: String? = null,
    val isBiometricsAvailable: Boolean = true,
    val isForgotPinDialogVisible: Boolean = false,
    val isLockedOut: Boolean = false,
    val lockoutRemainingSeconds: Int = 0
)

@Composable
fun LockScreen(
    uiState: LockUiState,
    onDigitClick: (Char) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricsClick: () -> Unit,
    onForgotPinClick: () -> Unit,
    onDismissForgotPinDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    (-20f) at 50
                    20f at 100
                    (-15f) at 150
                    15f at 200
                    (-8f) at 250
                    8f at 300
                    0f at 400
                }
            )
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SagePrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Master Lock",
                        tint = SagePrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Nilian",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Hoş geldin. Zihnini toparlamaya hazır mısın?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // PIN Indicator Dots with Shake Animation
                Row(
                    modifier = Modifier
                        .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until uiState.pinLength) {
                        val isFilled = i < uiState.currentPin.length
                        val dotColor by animateColorAsState(
                            targetValue = when {
                                uiState.errorMessage != null -> MaterialTheme.colorScheme.error
                                isFilled -> SagePrimary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            },
                            animationSpec = tween(200),
                            label = "DotColorAnim"
                        )

                        Box(
                            modifier = Modifier
                                .size(if (isFilled) 18.dp else 16.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) dotColor else Color.Transparent)
                                .border(
                                    width = 2.dp,
                                    color = dotColor,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Error / Lockout Message
                AnimatedVisibility(
                    visible = uiState.errorMessage != null || uiState.isLockedOut,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val msg = if (uiState.isLockedOut) {
                        "Çok fazla hatalı deneme. Lütfen ${uiState.lockoutRemainingSeconds}s bekleyin."
                    } else {
                        uiState.errorMessage.orEmpty()
                    }
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            // Custom Minimal Numpad
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val rows = listOf(
                    listOf('1', '2', '3'),
                    listOf('4', '5', '6'),
                    listOf('7', '8', '9')
                )

                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (digit in row) {
                            NumpadKey(
                                text = digit.toString(),
                                onClick = { onDigitClick(digit) }
                            )
                        }
                    }
                }

                // Bottom Row (Biometrics, '0', Backspace)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Biometrics Key
                    if (uiState.isBiometricsAvailable) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, radius = 36.dp),
                                    onClick = onBiometricsClick
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Biometrics Login",
                                tint = SagePrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(72.dp))
                    }

                    // Digit 0
                    NumpadKey(
                        text = "0",
                        onClick = { onDigitClick('0') }
                    )

                    // Delete Key
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = 36.dp),
                                onClick = onDeleteClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "Backspace",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Forgot PIN Footer
            TextButton(
                onClick = onForgotPinClick,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Şifremi Unuttum",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Forgot PIN Dialog
    if (uiState.isForgotPinDialogVisible) {
        AlertDialog(
            onDismissRequest = onDismissForgotPinDialog,
            shape = DialogShape,
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = SagePrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Yerel Gizlilik & PIN Sıfırlama",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Nilian, %100 çevrimdışı (offline-first) bir sistemdir. Verileriniz hiçbir harici sunucuya iletilmez.\n\nEğer ana PIN kodunuzu unuttuysanız, cihazınızın biyometrik doğrulamasını (Parmak İzi / Yüz Tanıma) kullanabilir veya uygulama verilerini Android ayarlarından sıfırlayabilirsiniz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onDismissForgotPinDialog,
                    shape = PillShape
                ) {
                    Text(
                        text = "Anladım",
                        color = SagePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
}

@Composable
private fun NumpadKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 36.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 26.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(name = "LockScreen Dark", showBackground = true)
@Composable
private fun LockScreenDarkPreview() {
    NilianTheme(darkTheme = true) {
        LockScreen(
            uiState = LockUiState(
                pinLength = 4,
                currentPin = "12",
                isBiometricsAvailable = true
            ),
            onDigitClick = {},
            onDeleteClick = {},
            onBiometricsClick = {},
            onForgotPinClick = {},
            onDismissForgotPinDialog = {}
        )
    }
}

@Preview(name = "LockScreen Light", showBackground = true)
@Composable
private fun LockScreenLightPreview() {
    NilianTheme(darkTheme = false) {
        LockScreen(
            uiState = LockUiState(
                pinLength = 4,
                currentPin = "123",
                isBiometricsAvailable = true
            ),
            onDigitClick = {},
            onDeleteClick = {},
            onBiometricsClick = {},
            onForgotPinClick = {},
            onDismissForgotPinDialog = {}
        )
    }
}
