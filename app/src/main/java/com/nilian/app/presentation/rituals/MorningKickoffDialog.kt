package com.nilian.app.presentation.rituals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.nilian.app.core.ui.components.PriorityBadge
import com.nilian.app.core.ui.theme.AmberSecondary
import com.nilian.app.core.ui.theme.CardShapeLarge
import com.nilian.app.core.ui.theme.CardShapeMedium
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.core.ui.theme.PillShape
import com.nilian.app.core.ui.theme.SagePrimary
import com.nilian.app.core.ui.theme.extendedColors
import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.EventItem
import com.nilian.app.domain.model.Priority
import com.nilian.app.domain.model.TaskItem
import com.nilian.app.domain.model.TimeBlockItem
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * MorningKickoffDialog: 30-sec mindful morning intention & kickoff flow.
 *
 * Displays:
 * 1. Mindful morning intention and date banner.
 * 2. Rollover tasks preview from yesterday.
 * 3. Top-3 focus task checkboxes with ⭐ star badges.
 * 4. Today's schedule glance (events & time blocks).
 * 5. One-click "Güne Başla / Start Mindful Day" action.
 */
@Composable
fun MorningKickoffDialog(
    tasks: List<TaskItem>,
    todayEvents: List<EventItem>,
    todayBlocks: List<TimeBlockItem>,
    onDismiss: () -> Unit,
    onStartDay: (selectedTop3Ids: Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
    currentDate: LocalDate = LocalDate.now()
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val rolloverTasks = remember(tasks) { tasks.filter { it.isRollover && !it.isCompleted } }
    val regularTasks = remember(tasks) { tasks.filter { !it.isCompleted } }

    // Selected Top-3 Focus Task IDs (pre-populate with up to 3 highest priority tasks)
    val selectedTopTaskIds = remember {
        mutableStateListOf<Long>().apply {
            addAll(
                regularTasks
                    .sortedByDescending { it.priority == Priority.HIGH }
                    .take(3)
                    .map { it.id }
            )
        }
    }

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
                // Header: Morning Greeting + Close Button
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
                                .background(AmberSecondary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Morning Sun",
                                tint = AmberSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Sabah Başlangıç Ritüeli",
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mindful Quote / Intention Card
                    item {
                        CalmCard(
                            backgroundColor = SagePrimary.copy(alpha = 0.08f),
                            borderColor = SagePrimary.copy(alpha = 0.25f),
                            shape = CardShapeMedium,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Spa,
                                    contentDescription = null,
                                    tint = SagePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "“Bugün tek bir şeye odaklanmak, her şeyi yapmaya çalışmaktan daha değerlidir.”",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 1. Rollover Tasks Preview (if any)
                    if (rolloverTasks.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = AmberSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Dünden Devredenler (${rolloverTasks.size})",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Dün tamamlanamayan bu görevler bugünün listesine eklendi:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                rolloverTasks.forEach { task ->
                                    CalmCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        backgroundColor = AmberSecondary.copy(alpha = 0.06f),
                                        borderColor = AmberSecondary.copy(alpha = 0.3f),
                                        contentPadding = PaddingValues(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            PriorityBadge(priority = task.priority)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Top-3 Focus Tasks Selection with ⭐ Badges
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = AmberSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Günün 3 Ana Odağı",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "${selectedTopTaskIds.size}/3 seçildi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = if (selectedTopTaskIds.size <= 3) SagePrimary else MaterialTheme.extendedColors.warning
                                )
                            }

                            Text(
                                text = "Bugün enerjinizi adayacağınız en kritik 3 görevi işaretleyin:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (regularTasks.isEmpty()) {
                                Text(
                                    text = "Bugün için henüz görev eklenmemiş.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                regularTasks.forEach { task ->
                                    val isSelected = selectedTopTaskIds.contains(task.id)
                                    val isTopThreeStar = isSelected

                                    CalmCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        backgroundColor = if (isSelected) SagePrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                                        borderColor = if (isSelected) SagePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        onClick = {
                                            if (isSelected) {
                                                selectedTopTaskIds.remove(task.id)
                                            } else {
                                                if (selectedTopTaskIds.size < 3) {
                                                    selectedTopTaskIds.add(task.id)
                                                }
                                            }
                                        },
                                        contentPadding = PaddingValues(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Star Pin Icon
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isTopThreeStar) AmberSecondary.copy(alpha = 0.2f)
                                                        else Color.Transparent
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isTopThreeStar) Icons.Filled.Star else Icons.Default.StarBorder,
                                                    contentDescription = "Star",
                                                    tint = if (isTopThreeStar) AmberSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = task.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${task.estimatedDurationMinutes} dk • ${task.priority.name}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(PillShape)
                                                        .background(SagePrimary.copy(alpha = 0.18f))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "⭐ ODAK",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp
                                                        ),
                                                        color = SagePrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Today's Schedule Glance (Events & Blocks)
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = SagePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Bugünkü Takvim Özeti",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (todayEvents.isEmpty() && todayBlocks.isEmpty()) {
                                Text(
                                    text = "Bugün için takviminiz sakin ve boş.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                todayEvents.forEach { event ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "📅 ${event.title}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${event.startDateTime.format(timeFormatter)} - ${event.endDateTime.format(timeFormatter)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                todayBlocks.forEach { block ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "⏳ ${block.title}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${block.startTime.format(timeFormatter)} - ${block.endTime.format(timeFormatter)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SagePrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action: Start Mindful Day Button
                Button(
                    onClick = {
                        onStartDay(selectedTopTaskIds.toSet())
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
                        Text(
                            text = "Güne Başla (30s)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
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

@Preview(name = "MorningKickoffDialog - Dark", showBackground = true)
@Composable
private fun MorningKickoffDialogDarkPreview() {
    NilianTheme(darkTheme = true) {
        val sampleTasks = listOf(
            TaskItem(id = 1, title = "Compose UI Mimarisi Kurulumu", priority = Priority.HIGH, isRollover = true),
            TaskItem(id = 2, title = "Database Migration ve Testler", priority = Priority.MEDIUM, isRollover = false),
            TaskItem(id = 3, title = "Zaman Bütçesi Algoritması Entegrasyonu", priority = Priority.HIGH, isRollover = false)
        )
        MorningKickoffDialog(
            tasks = sampleTasks,
            todayEvents = emptyList(),
            todayBlocks = emptyList(),
            onDismiss = {},
            onStartDay = {}
        )
    }
}
