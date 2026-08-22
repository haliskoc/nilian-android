package com.nilian.app.presentation.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilian.app.core.ui.theme.AmberAccent
import com.nilian.app.core.ui.theme.SagePrimary
import kotlinx.coroutines.delay

enum class FocusPreset(val label: String, val minutes: Int) {
    POMODORO("25 dk Odak", 25),
    DEEP_WORK("50 dk Derin", 50),
    QUICK_FLOW("15 dk Akış", 15),
    SHORT_BREAK("5 dk Mola", 5),
    LONG_BREAK("15 dk Mola", 15)
}

@Composable
fun FocusTimerScreen(
    initialTaskTitle: String? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPreset by remember { mutableStateOf(FocusPreset.POMODORO) }
    var totalSeconds by remember { mutableIntStateOf(FocusPreset.POMODORO.minutes * 60) }
    var remainingSeconds by remember { mutableIntStateOf(FocusPreset.POMODORO.minutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var completedSessions by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                isRunning = false
                completedSessions += 1
            }
        }
    }

    val progress = if (totalSeconds > 0) {
        (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()
    } else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "TimerProgress")

    val minutesPart = remainingSeconds / 60
    val secondsPart = remainingSeconds % 60
    val formattedTime = "%02d:%02d".format(minutesPart, secondsPart)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "🌿 Sakin Odaklanma Zamanlayıcısı",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Task Focus Banner ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SagePrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = SagePrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aktif Odak Konusu",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = initialTaskTitle ?: "Zihinsel Derinlik & Odak Seansı",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "🔥 $completedSessions Seans",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AmberAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Preset Selector ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FocusPreset.values().forEach { preset ->
                val isSelected = preset == selectedPreset
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) SagePrimary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) SagePrimary else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            selectedPreset = preset
                            totalSeconds = preset.minutes * 60
                            remainingSeconds = preset.minutes * 60
                            isRunning = false
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = preset.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) SagePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- Big Circular Timer Ring ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                // Background Track
                drawCircle(
                    color = Color(0xFF262C35),
                    style = Stroke(width = strokeWidth)
                )
                // Progress Arc
                drawArc(
                    color = SagePrimary,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formattedTime,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isRunning) "Derin Odaktasın 🧘" else if (remainingSeconds == 0) "Harika İş! 🎉" else "Başlamaya Hazır",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- Play / Pause / Reset Controls ---
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Reset Button
            IconButton(
                onClick = {
                    isRunning = false
                    remainingSeconds = totalSeconds
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sıfırla",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Main Play/Pause Button
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Duraklat" else "Başlat",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Calm Wisdom Footer
        Text(
            text = "“Sakin bir zihin, en güçlü üretkenlik aracıdır.”",
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
