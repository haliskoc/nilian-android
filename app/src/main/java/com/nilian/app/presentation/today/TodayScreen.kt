package com.nilian.app.presentation.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilian.app.core.ui.components.CalmCard
import com.nilian.app.core.ui.components.CircularDayProgress
import com.nilian.app.core.ui.components.ConflictAlertBanner
import com.nilian.app.core.ui.components.EmptyStateWidget
import com.nilian.app.core.ui.components.MilestoneCountdownCarousel
import com.nilian.app.core.ui.components.NilianTopAppBar
import com.nilian.app.core.ui.components.PriorityBadge
import com.nilian.app.core.ui.components.StreakFlameBadge
import com.nilian.app.core.ui.components.TimeBudgetBanner
import com.nilian.app.core.ui.theme.BottomSheetShape
import com.nilian.app.core.ui.theme.CardShapeLarge
import com.nilian.app.core.ui.theme.CardShapeMedium
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.core.ui.theme.PillShape
import com.nilian.app.core.ui.theme.SagePrimary
import com.nilian.app.core.ui.theme.extendedColors
import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.ConflictItem
import com.nilian.app.domain.model.DayTemplateWithBlocks
import com.nilian.app.domain.model.EventCategory
import com.nilian.app.domain.model.EventItem
import com.nilian.app.domain.model.FreeSlotItem
import com.nilian.app.domain.model.GoalItem
import com.nilian.app.domain.model.HabitItem
import com.nilian.app.domain.model.InboxNote
import com.nilian.app.domain.model.Priority
import com.nilian.app.domain.model.TaskItem
import com.nilian.app.domain.model.TimeBlock
import com.nilian.app.domain.model.TimeBlockItem
import com.nilian.app.presentation.focus.FocusTimerScreen
import com.nilian.app.presentation.inbox.BrainDumpBottomSheet
import com.nilian.app.presentation.rituals.EveningCloseoutDialog
import com.nilian.app.presentation.rituals.MorningKickoffDialog
import com.nilian.app.presentation.templates.DayTemplatesBottomSheet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class TodayViewMode {
    DAILY,
    WEEKLY
}

data class DaySummary(
    val date: LocalDate,
    val dayName: String,
    val taskCompletedRatio: Float,
    val isToday: Boolean = false,
    val eventCount: Int = 0
)

data class TodayUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val viewMode: TodayViewMode = TodayViewMode.DAILY,
    val workloadScoreHours: Float = 5.5f,
    val workloadStatus: String = "Balanced & Calm",
    val nextUpBlock: TimeBlockItem? = null,
    val nextUpEvent: EventItem? = null,
    val nextUpRemainingMinutes: Int = 35,
    val nextUpProgress: Float = 0.45f,
    val tasks: List<TaskItem> = emptyList(),
    val habits: List<HabitItem> = emptyList(),
    val freeSlots: List<FreeSlotItem> = emptyList(),
    val conflicts: List<ConflictItem> = emptyList(),
    val weekSummaries: List<DaySummary> = emptyList(),
    val isQuickAddSheetVisible: Boolean = false,
    val isBrainDumpSheetVisible: Boolean = false,
    val isDayTemplatesSheetVisible: Boolean = false,
    val recentNotes: List<InboxNote> = emptyList(),
    val activeGoals: List<GoalItem> = emptyList(),
    val totalTaskMinutes: Int = 0,
    val calendarGapMinutes: Int = 0,
    val todayEvents: List<EventItem> = emptyList(),
    val todayBlocks: List<TimeBlockItem> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    uiState: TodayUiState,
    onViewModeToggle: (TodayViewMode) -> Unit,
    onTaskToggle: (TaskItem) -> Unit,
    onHabitToggle: (HabitItem) -> Unit,
    onQuickAddClick: () -> Unit,
    onDismissQuickAddSheet: () -> Unit,
    onQuickAddOptionSelected: (String) -> Unit,
    onConflictReviewClick: (ConflictItem) -> Unit,
    onFreeSlotClick: (FreeSlotItem) -> Unit,
    onTaskClick: (TaskItem) -> Unit,
    modifier: Modifier = Modifier,
    onGoalClick: (GoalItem) -> Unit = {},
    onBrainDumpClick: () -> Unit = {},
    onDayTemplatesClick: () -> Unit = {},
    onDismissBrainDump: () -> Unit = {},
    onDismissDayTemplates: () -> Unit = {},
    onSaveBrainDumpNote: (String, String?) -> Unit = { _, _ -> },
    onApplyDayTemplate: (DayTemplateWithBlocks) -> Unit = {},
    onConvertToTask: (Long, String, Priority, Int, LocalDate?) -> Unit = { _, _, _, _, _ -> },
    onConvertToEvent: (Long, String, EventCategory, LocalDate, LocalTime, LocalTime) -> Unit = { _, _, _, _, _, _ -> },
    onConvertToGoal: (Long, String, String?, LocalDate?) -> Unit = { _, _, _, _ -> },
    onFocusSessionCompleted: (TimeBlock) -> Unit = {}
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showMorningKickoff by remember { mutableStateOf(false) }
    var showEveningCloseout by remember { mutableStateOf(false) }
    var showFocusTimer by remember { mutableStateOf(false) }
    var activeFocusTask by remember { mutableStateOf<TaskItem?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NilianTopAppBar(
                title = uiState.selectedDate.format(dateFormatter),
                subtitle = "İş Yükü: ${uiState.workloadScoreHours} sa • ${uiState.workloadStatus}",
                actions = {
                    // View Toggle Tabs (Daily / Weekly)
                    Row(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(
                                    if (uiState.viewMode == TodayViewMode.DAILY) SagePrimary else Color.Transparent
                                )
                                .clickable { onViewModeToggle(TodayViewMode.DAILY) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Günlük",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (uiState.viewMode == TodayViewMode.DAILY) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(
                                    if (uiState.viewMode == TodayViewMode.WEEKLY) SagePrimary else Color.Transparent
                                )
                                .clickable { onViewModeToggle(TodayViewMode.WEEKLY) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Haftalık",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (uiState.viewMode == TodayViewMode.WEEKLY) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onQuickAddClick,
                containerColor = SagePrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Hızlı Ekle")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.viewMode) {
                TodayViewMode.DAILY -> {
                    DailyDashboardContent(
                        uiState = uiState,
                        onTaskToggle = onTaskToggle,
                        onHabitToggle = onHabitToggle,
                        onConflictReviewClick = onConflictReviewClick,
                        onFreeSlotClick = onFreeSlotClick,
                        onTaskClick = onTaskClick,
                        onGoalClick = onGoalClick,
                        onBrainDumpClick = onBrainDumpClick,
                        onDayTemplatesClick = onDayTemplatesClick,
                        onStartMorningKickoff = { showMorningKickoff = true },
                        onStartEveningCloseout = { showEveningCloseout = true },
                        onStartFocus = { task ->
                            activeFocusTask = task
                            showFocusTimer = true
                        }
                    )
                }
                TodayViewMode.WEEKLY -> {
                    WeeklyOverviewContent(
                        uiState = uiState,
                        onDayClick = { /* Navigate or filter date */ }
                    )
                }
            }
        }
    }

    // Morning Kickoff Dialog
    if (showMorningKickoff) {
        MorningKickoffDialog(
            tasks = uiState.tasks,
            todayEvents = uiState.todayEvents,
            todayBlocks = uiState.todayBlocks,
            onDismiss = { showMorningKickoff = false },
            onStartDay = { _ ->
                showMorningKickoff = false
            }
        )
    }

    // Evening Closeout Dialog
    if (showEveningCloseout) {
        EveningCloseoutDialog(
            tasks = uiState.tasks,
            habits = uiState.habits,
            focusBlocks = uiState.todayBlocks,
            onDismiss = { showEveningCloseout = false },
            onConfirmCloseout = { _ ->
                showEveningCloseout = false
            }
        )
    }

    // Fullscreen Focus Timer Overlay
    if (showFocusTimer) {
        FocusTimerScreen(
            initialTaskTitle = activeFocusTask?.title,
            initialTaskId = activeFocusTask?.id,
            onClose = {
                showFocusTimer = false
                activeFocusTask = null
            },
            onSessionCompleted = { completedBlock ->
                onFocusSessionCompleted(completedBlock)
                showFocusTimer = false
                activeFocusTask = null
            }
        )
    }

    // Quick Add Bottom Sheet
    if (uiState.isQuickAddSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismissQuickAddSheet,
            sheetState = sheetState,
            shape = BottomSheetShape,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Nilian'a Hızlı Ekle",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Görevleri, odak bloklarını veya zihin boşaltma notlarını anında yakalayın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                QuickAddOptionItem(
                    title = "🧠 Zihin Boşaltma (Brain Dump)",
                    description = "Sıfır sürtünmeyle fikir, not ve görev yakala",
                    icon = Icons.Outlined.Psychology,
                    onClick = { onQuickAddOptionSelected("BRAIN_DUMP") }
                )
                QuickAddOptionItem(
                    title = "✨ Gün Şablonu Uygula",
                    description = "Sınav, Derin Kodlama veya Dinlenme akışını aktar",
                    icon = Icons.Outlined.AutoAwesome,
                    onClick = { onQuickAddOptionSelected("TEMPLATES") }
                )
                QuickAddOptionItem(
                    title = "Yeni Görev",
                    description = "Öncelikli eylem maddesi ve süre tahmini",
                    icon = Icons.Default.TaskAlt,
                    onClick = { onQuickAddOptionSelected("TASK") }
                )
                QuickAddOptionItem(
                    title = "Zaman Bloğu / Odak Seansı",
                    description = "24 saatlik zaman çizelgenizde odak slotu ayırın",
                    icon = Icons.Outlined.Schedule,
                    onClick = { onQuickAddOptionSelected("BLOCK") }
                )
                QuickAddOptionItem(
                    title = "Takvim Etkinliği",
                    description = "Ders, toplantı veya randevu",
                    icon = Icons.Outlined.Event,
                    onClick = { onQuickAddOptionSelected("EVENT") }
                )
                QuickAddOptionItem(
                    title = "Alışkanlık / Rutin",
                    description = "Seri takibiyle günlük ritüel başlat",
                    icon = Icons.Outlined.SelfImprovement,
                    onClick = { onQuickAddOptionSelected("HABIT") }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Brain Dump Bottom Sheet
    if (uiState.isBrainDumpSheetVisible) {
        BrainDumpBottomSheet(
            onDismiss = onDismissBrainDump,
            onSaveNote = onSaveBrainDumpNote,
            onConvertToTask = { note ->
                onConvertToTask(note.id, note.content, Priority.MEDIUM, 30, LocalDate.now())
            },
            onConvertToEvent = { note ->
                val now = LocalTime.now()
                onConvertToEvent(note.id, note.content, EventCategory.GENERAL, LocalDate.now(), now, now.plusHours(1))
            },
            onConvertToGoal = { note ->
                onConvertToGoal(note.id, note.content, null, LocalDate.now().plusMonths(3))
            },
            recentNotes = uiState.recentNotes
        )
    }

    // Day Templates Bottom Sheet
    if (uiState.isDayTemplatesSheetVisible) {
        DayTemplatesBottomSheet(
            onDismiss = onDismissDayTemplates,
            onApplyTemplate = onApplyDayTemplate
        )
    }
}

@Composable
private fun DailyDashboardContent(
    uiState: TodayUiState,
    onTaskToggle: (TaskItem) -> Unit,
    onHabitToggle: (HabitItem) -> Unit,
    onConflictReviewClick: (ConflictItem) -> Unit,
    onFreeSlotClick: (FreeSlotItem) -> Unit,
    onTaskClick: (TaskItem) -> Unit,
    onGoalClick: (GoalItem) -> Unit,
    onBrainDumpClick: () -> Unit,
    onDayTemplatesClick: () -> Unit,
    onStartMorningKickoff: () -> Unit = {},
    onStartEveningCloseout: () -> Unit = {},
    onStartFocus: (TaskItem?) -> Unit = {}
) {
    val completedTaskCount = uiState.tasks.count { it.isCompleted }
    val totalTaskCount = uiState.tasks.size
    val taskProgress = if (totalTaskCount > 0) completedTaskCount.toFloat() / totalTaskCount else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Conflict Warning Banner (if any)
        if (uiState.conflicts.isNotEmpty()) {
            item {
                val conflict = uiState.conflicts.first()
                ConflictAlertBanner(
                    title = "Çakışma Tespit Edildi (${uiState.conflicts.size})",
                    message = "${conflict.titleA} ile ${conflict.titleB} çakışıyor (${conflict.timeDescription})",
                    onActionClick = { onConflictReviewClick(conflict) }
                )
            }
        }

        // 2. Daily Hero Section (Progress + Next Up)
        item {
            CalmCard(
                shape = CardShapeLarge,
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Circular Progress
                    CircularDayProgress(
                        progressPercent = taskProgress,
                        completedCount = completedTaskCount,
                        totalCount = totalTaskCount,
                        size = 92.dp,
                        strokeWidth = 7.dp,
                        label = "Görev"
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Next Up / Active Focus
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SagePrimary)
                            )
                            Text(
                                text = "SIRADAKİ ODAK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = SagePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val nextUpTitle = uiState.nextUpBlock?.title
                            ?: uiState.nextUpEvent?.title
                            ?: "Planlı acil blok bulunmuyor"

                        Text(
                            text = nextUpTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        val timeText = if (uiState.nextUpBlock != null) {
                            "${uiState.nextUpBlock.startTime} - ${uiState.nextUpBlock.endTime} (${uiState.nextUpRemainingMinutes}dk kaldı)"
                        } else {
                            "Serbest çalışma veya dinlenme vakti"
                        }

                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { uiState.nextUpProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = SagePrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // 3. Quick Action Tools Row (Brain Dump & Day Templates)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Brain Dump Action Card
                CalmCard(
                    modifier = Modifier.weight(1f),
                    shape = CardShapeMedium,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = SagePrimary.copy(alpha = 0.4f),
                    onClick = onBrainDumpClick,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SagePrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🧠", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "Zihin Boşalt",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Hızlı Fikir Yakala",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Day Templates Action Card
                CalmCard(
                    modifier = Modifier.weight(1f),
                    shape = CardShapeMedium,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = SagePrimary.copy(alpha = 0.4f),
                    onClick = onDayTemplatesClick,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✨", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "Gün Şablonları",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tek Dokunuşla Uygula",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. Milestone Countdowns Horizon (Goals countdown badges & cards)
        if (uiState.activeGoals.isNotEmpty()) {
            item {
                MilestoneCountdownCarousel(
                    goals = uiState.activeGoals,
                    onGoalClick = onGoalClick,
                    title = "Kilometre Taşı Sayaçları",
                    subtitle = "Hedeflerinize kalan süre ve ilerleme durumu"
                )
            }
        }

        // 5. Quick Habit Checkboxes Row
        if (uiState.habits.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Günlük Ritüeller & Alışkanlıklar",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${uiState.habits.count { it.isCompletedToday }}/${uiState.habits.size} tamam",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(uiState.habits, key = { it.id }) { habit ->
                            HabitPillItem(
                                habit = habit,
                                onToggle = { onHabitToggle(habit) }
                            )
                        }
                    }
                }
            }
        }

        // 6. Free Time Gap Suggestions
        if (uiState.freeSlots.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = SagePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Farkındalık & Boş Zaman Pencereleri",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(uiState.freeSlots) { slot ->
                            CalmCard(
                                shape = CardShapeMedium,
                                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                onClick = { onFreeSlotClick(slot) },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = SagePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "${slot.durationMinutes}dk Boş Aralık",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${slot.startTime} - ${slot.endTime}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Today's Priority Task List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bugünün Görevleri",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${uiState.tasks.count { !it.isCompleted }} açık",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (uiState.tasks.isEmpty()) {
            item {
                EmptyStateWidget(
                    title = "Zihnin Sakin ve Günün Berrak",
                    description = "Bugün için bekleyen görev yok. Yeni bir eylem ekleyin veya dinlenin.",
                    icon = Icons.Outlined.CheckCircle
                )
            }
        } else {
            items(uiState.tasks, key = { it.id }) { task ->
                TodayTaskRowItem(
                    task = task,
                    onToggle = { onTaskToggle(task) },
                    onClick = { onTaskClick(task) }
                )
            }
        }
    }
}

@Composable
private fun HabitPillItem(
    habit: HabitItem,
    onToggle: () -> Unit
) {
    val isCompleted = habit.isCompletedToday

    Row(
        modifier = Modifier
            .clip(PillShape)
            .background(
                if (isCompleted) SagePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.dp,
                color = if (isCompleted) SagePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = PillShape
            )
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isCompleted) SagePrimary else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (isCompleted) SagePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Tamamlandı",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Text(
            text = habit.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (isCompleted) SagePrimary else MaterialTheme.colorScheme.onSurface
        )

        StreakFlameBadge(streakCount = habit.currentStreak)
    }
}

@Composable
private fun TodayTaskRowItem(
    task: TaskItem,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    CalmCard(
        shape = CardShapeMedium,
        onClick = onClick,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCompleted) SagePrimary else Color.Transparent
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (task.isCompleted) SagePrimary else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Tamamlandı",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (task.isRollover) {
                        Text(
                            text = "⏳ Devreden",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.extendedColors.warning
                        )
                    }
                    Text(
                        text = "${task.estimatedDurationMinutes}dk",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!task.goalTitle.isNullOrBlank()) {
                        Text(
                            text = "• ${task.goalTitle}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SagePrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            PriorityBadge(priority = task.priority)
        }
    }
}

@Composable
private fun WeeklyOverviewContent(
    uiState: TodayUiState,
    onDayClick: (DaySummary) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "7 Günlük Ritim & Denge",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Görev tamamlama oranları ve günlük enerji dağılımı.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(uiState.weekSummaries) { day ->
            CalmCard(
                shape = CardShapeMedium,
                borderColor = if (day.isToday) SagePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                onClick = { onDayClick(day) },
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = day.dayName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (day.isToday) SagePrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (day.isToday) {
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(SagePrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "BUGÜN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = SagePrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${day.eventCount} planlı oturum",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { day.taskCompletedRatio },
                            modifier = Modifier
                                .width(80.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SagePrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "${(day.taskCompletedRatio * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAddOptionItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    CalmCard(
        shape = CardShapeMedium,
        onClick = onClick,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SagePrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SagePrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(name = "Today Dashboard Dark", showBackground = true)
@Composable
private fun TodayScreenDarkPreview() {
    NilianTheme(darkTheme = true) {
        TodayScreen(
            uiState = TodayUiState(
                workloadScoreHours = 6.0f,
                tasks = listOf(
                    TaskItem(id = 1, title = "Design System Refinement", priority = Priority.HIGH, estimatedDurationMinutes = 45, isCompleted = true, goalTitle = "Nilian 1.0"),
                    TaskItem(id = 2, title = "Room Database Entities & DAOs", priority = Priority.HIGH, estimatedDurationMinutes = 60, isRollover = true),
                    TaskItem(id = 3, title = "Review weekly lecture notes", priority = Priority.MEDIUM, estimatedDurationMinutes = 30)
                ),
                habits = listOf(
                    HabitItem(id = 1, title = "Morning Meditation", currentStreak = 14, isCompletedToday = true),
                    HabitItem(id = 2, title = "30m Deep Reading", currentStreak = 7, isCompletedToday = false),
                    HabitItem(id = 3, title = "Hydration 2.5L", currentStreak = 21, isCompletedToday = true)
                ),
                freeSlots = listOf(
                    FreeSlotItem(startTime = LocalTime.of(15, 30), endTime = LocalTime.of(16, 15), durationMinutes = 45)
                ),
                nextUpBlock = TimeBlockItem(
                    id = 1,
                    title = "Deep Focus / Sprint",
                    blockType = BlockType.DEEP_WORK,
                    startTime = LocalTime.of(14, 0),
                    endTime = LocalTime.of(15, 30)
                )
            ),
            onViewModeToggle = {},
            onTaskToggle = {},
            onHabitToggle = {},
            onQuickAddClick = {},
            onDismissQuickAddSheet = {},
            onQuickAddOptionSelected = {},
            onConflictReviewClick = {},
            onFreeSlotClick = {},
            onTaskClick = {}
        )
    }
}
