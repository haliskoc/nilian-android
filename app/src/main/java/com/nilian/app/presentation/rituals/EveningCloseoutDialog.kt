package com.nilian.app.presentation.rituals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nilian.app.core.ui.components.CalmCard
import com.nilian.app.core.ui.components.CircularDayProgress
import com.nilian.app.core.ui.theme.AmberSecondary
import com.nilian.app.core.ui.theme.CardShapeLarge
import com.nilian.app.core.ui.theme.CardShapeMedium
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.core.ui.theme.PillShape
import com.nilian.app.core.ui.theme.SagePrimary
import com.nilian.app.core.ui.theme.SlateTertiary
import com.nilian.app.core.ui.theme.extendedColors
import com.nilian.app.domain.model.HabitItem
import com.nilian.app.domain.model.TaskItem
import com.nilian.app.domain.model.TimeBlockItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * EveningCloseoutDialog: 1-min evening celebration and reflection modal.
 *
 * Displays:
 * 1. Mindful evening header with peaceful starry aesthetic.
 * 2. Completion Ring showing today's task completion percentage.
 * 3. Daily statistics (total focus hours, habits done, tasks finished).
 * 4. Remaining tasks rollover preview (peaceful reassurance for tomorrow).
 * 5. Peaceful closing message.
 * 6. "Günü Huzurla Kapat / Rest for Tonight" action button.
 */
@Composable
fun EveningCloseoutDialog(
    tasks: List<TaskItem>,
    habits: List<HabitItem>,
    focusBlocks: List<TimeBlockItem>,
    onDismiss: () -> Unit,
    onConfirmCloseout: (autoRolloverRemaining: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    currentDate: LocalDate = LocalDate.now()
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")

    val completedTasks = remember(tasks) { tasks.filter { it.isCompleted } }
    val remainingTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
    val totalTaskCount = tasks.size
    val completedCount = completedTasks.size
    val completionRatio = if (totalTaskCount > 0) completedCount.toFloat() / totalTaskCount.toFloat() else 1.0f

    val completedHabitsCount = habits.count { it.isCompletedToday }
    val totalHabitsCount = habits.size

    // Calculate total focus hours from completed deep work / study time blocks
    val totalFocusMinutes = remember(focusBlocks) {
        focusBlocks.sumOf { block ->
            java.time.Duration.between(block.startTime, block.endTime).toMinutes().coerceAtLeast(0)
        }
    }
    val focusHoursFormatted = remember(totalFocusMinutes) {
        val hours = totalFocusMinutes / 60f
        if (hours % 1.0f == 0.0f) "${hours.toInt()}" else String.format(java.util.Locale.US, "%.1f", hours)
    }

    var autoRolloverRemaining by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(CardShapeLarge),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Evening Moon + Title + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SlateTertiary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Nightlight,
                                contentDescription = "Evening Moon",
                                tint = SlateTertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Akşam Kapanış Ritüeli",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Completion Ring & Big Summary
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularDayProgress(
                                progressPercent = completionRatio,
                                completedCount = completedCount,
                                totalCount = totalTaskCount,
                                size = 110.dp,
                                strokeWidth = 9.dp,
                                primaryColor = SagePrimary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val praiseTitle = when {
                                completionRatio >= 0.8f -> "Harika Bir Gün Geçirdiniz! 🌿"
                                completionRatio >= 0.5f -> "Güzel Bir İlerleme Kaydettiniz 🌱"
                                else -> "Gününüzü Sevgiyle Tamamlayın 🌙"
                            }

                            Text(
                                text = praiseTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 2. Metrics Glance Row (Focus Hours, Habits, Completed Tasks)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Focus Hours Card
                            CalmCard(
                                modifier = Modifier.weight(1f),
                                backgroundColor = SagePrimary.copy(alpha = 0.08f),
                                borderColor = SagePrimary.copy(alpha = 0.25f),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$focusHoursFormatted Saat",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = SagePrimary
                                    )
                                    Text(
                                        text = "Odaklanma",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Habits Done Card
                            CalmCard(
                                modifier = Modifier.weight(1f),
                                backgroundColor = AmberSecondary.copy(alpha = 0.08f),
                                borderColor = AmberSecondary.copy(alpha = 0.25f),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$completedHabitsCount / $totalHabitsCount",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AmberSecondary
                                    )
                                    Text(
                                        text = "Alışkanlık",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Tasks Completed Card
                            CalmCard(
                                modifier = Modifier.weight(1f),
                                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$completedCount Görev",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Tamamlandı",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 3. Remaining Tasks Rollover Preview
                    if (remainingTasks.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassBottom,
                                        contentDescription = null,
                                        tint = SlateTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Yarına Devredecek Görevler (${remainingTasks.size})",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "Bugün bitmeyen görevler suçluluk yaratmadan yarının listesine taşınacak:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                remainingTasks.forEach { task ->
                                    CalmCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        contentPadding = PaddingValues(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "⏳ Yarın",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SlateTertiary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Peaceful Closing Message Card
                    item {
                        CalmCard(
                            backgroundColor = SlateTertiary.copy(alpha = 0.08f),
                            borderColor = SlateTertiary.copy(alpha = 0.25f),
                            shape = CardShapeMedium,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Bedtime,
                                    contentDescription = null,
                                    tint = SlateTertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "“Bugün elinizden gelenin en iyisini yaptınız. Şimdi zihninizi dinlendirme, ekranlardan uzaklaşma ve yenilenme vakti.”",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action: Rest for Tonight Button
                Button(
                    onClick = {
                        onConfirmCloseout(autoRolloverRemaining)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Günü Huzurla Kapat",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// Previews
// =========================================================================

@Preview(name = "EveningCloseoutDialog - Dark", showBackground = true)
@Composable
private fun EveningCloseoutDialogDarkPreview() {
    NilianTheme(darkTheme = true) {
        val sampleTasks = listOf(
            TaskItem(id = 1, title = "Compose UI Mimarisi", isCompleted = true),
            TaskItem(id = 2, title = "DataStore Güvenlik Entegrasyonu", isCompleted = true),
            TaskItem(id = 3, title = "Akşam Yürüyüşü", isCompleted = false)
        )
        val sampleHabits = listOf(
            HabitItem(id = 1, title = "Kitap Okuma", isCompletedToday = true),
            HabitItem(id = 2, title = "Meditasyon", isCompletedToday = true)
        )
        EveningCloseoutDialog(
            tasks = sampleTasks,
            habits = sampleHabits,
            focusBlocks = emptyList(),
            onDismiss = {},
            onConfirmCloseout = {}
        )
    }
}
