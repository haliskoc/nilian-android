package com.nilian.app.presentation.habits

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilian.app.core.ui.components.CalmCard
import com.nilian.app.core.ui.components.CalmTextField
import com.nilian.app.core.ui.components.DayDotIndicator
import com.nilian.app.core.ui.components.EmptyStateWidget
import com.nilian.app.core.ui.components.NilianTopAppBar
import com.nilian.app.core.ui.components.StreakFlameBadge
import com.nilian.app.core.ui.theme.BottomSheetShape
import com.nilian.app.core.ui.theme.CardShapeLarge
import com.nilian.app.core.ui.theme.CardShapeMedium
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.core.ui.theme.PillShape
import com.nilian.app.core.ui.theme.SagePrimary
import com.nilian.app.core.ui.theme.extendedColors
import com.nilian.app.domain.model.GoalItem
import com.nilian.app.domain.model.HabitItem
import java.time.DayOfWeek
import java.time.LocalDate

data class HabitsUiState(
    val habits: List<HabitItem> = emptyList(),
    val availableGoals: List<GoalItem> = emptyList(),
    val isAddEditModalVisible: Boolean = false,
    val editingHabit: HabitItem? = null,
    val currentDayOfWeek: DayOfWeek = LocalDate.now().dayOfWeek
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    uiState: HabitsUiState,
    onToggleHabitToday: (HabitItem) -> Unit,
    onToggleHabitHistoryDay: (HabitItem, dayIndex: Int) -> Unit,
    onAddHabitClick: () -> Unit,
    onEditHabitClick: (HabitItem) -> Unit,
    onDeleteHabitClick: (HabitItem) -> Unit,
    onDismissAddEditModal: () -> Unit,
    onSaveHabit: (
        id: Long,
        title: String,
        targetDays: Set<DayOfWeek>,
        goalId: Long?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalHabits = uiState.habits.size
    val completedToday = uiState.habits.count { it.isCompletedToday }
    val bestOverallStreak = uiState.habits.maxOfOrNull { it.bestStreak } ?: 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NilianTopAppBar(
                title = "Habits & Rituals",
                subtitle = "Consistency over intensity • $completedToday of $totalHabits done today"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabitClick,
                containerColor = SagePrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Habit")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.habits.isEmpty()) {
                EmptyStateWidget(
                    title = "Build Your Daily Rituals",
                    description = "Track small daily habits that build long-term momentum.",
                    icon = Icons.Outlined.SelfImprovement,
                    actionLabel = "Create Habit",
                    onActionClick = onAddHabitClick
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary Stats Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatMiniCard(
                                title = "Completed",
                                value = "$completedToday / $totalHabits",
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                title = "Best Streak",
                                value = "🔥 $bestOverallStreak days",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Habit Items with 7-day Dot Matrix
                    items(uiState.habits, key = { it.id }) { habit ->
                        HabitCardWithMatrix(
                            habit = habit,
                            onToggleToday = { onToggleHabitToday(habit) },
                            onToggleDay = { dayIndex -> onToggleHabitHistoryDay(habit, dayIndex) },
                            onEdit = { onEditHabitClick(habit) },
                            onDelete = { onDeleteHabitClick(habit) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Modal
    if (uiState.isAddEditModalVisible) {
        AddEditHabitModal(
            habit = uiState.editingHabit,
            availableGoals = uiState.availableGoals,
            onDismiss = onDismissAddEditModal,
            onSave = onSaveHabit
        )
    }
}

@Composable
private fun StatMiniCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    CalmCard(
        shape = CardShapeMedium,
        backgroundColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun HabitCardWithMatrix(
    habit: HabitItem,
    onToggleToday: () -> Unit,
    onToggleDay: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val daysOfWeekLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val todayIndex = (LocalDate.now().dayOfWeek.value - 1) % 7

    CalmCard(
        shape = CardShapeLarge,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Title, Streak Flame, Options Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Checkbox
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (habit.isCompletedToday) SagePrimary else Color.Transparent)
                            .border(
                                width = 1.5.dp,
                                color = if (habit.isCompletedToday) SagePrimary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable { onToggleToday() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (habit.isCompletedToday) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = if (habit.isCompletedToday) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (habit.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!habit.goalTitle.isNullOrBlank()) {
                            Text(
                                text = "🎯 ${habit.goalTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SagePrimary
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StreakFlameBadge(
                        streakCount = habit.currentStreak,
                        showBestStreak = true,
                        bestStreak = habit.bestStreak
                    )

                    Box {
                        IconButton(
                            onClick = { isMenuOpen = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuOpen,
                            onDismissRequest = { isMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Habit") },
                                onClick = {
                                    isMenuOpen = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    isMenuOpen = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7-Day Completion Dot Matrix
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShapeMedium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (dayIdx in 0..6) {
                    val isDone = habit.weeklyHistory.getOrElse(dayIdx) { false }
                    val isToday = dayIdx == todayIndex
                    DayDotIndicator(
                        dayLabel = daysOfWeekLabels[dayIdx],
                        isCompleted = isDone,
                        isToday = isToday,
                        onClick = { onToggleDay(dayIdx) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditHabitModal(
    habit: HabitItem?,
    availableGoals: List<GoalItem>,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        targetDays: Set<DayOfWeek>,
        goalId: Long?
    ) -> Unit
) {
    var title by remember { mutableStateOf(habit?.title.orEmpty()) }
    var selectedGoalId by remember { mutableStateOf(habit?.goalId) }
    var selectedDays by remember {
        mutableStateOf(
            habit?.targetDaysOfWeek ?: DayOfWeek.values().toSet()
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allDays = DayOfWeek.values()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = BottomSheetShape,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (habit == null) "Create Habit" else "Edit Habit",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            CalmTextField(
                value = title,
                onValueChange = { title = it },
                label = "Habit Name",
                placeholder = "e.g. Read 20 pages, Hydrate, Morning run"
            )

            // Target Days of Week Selection
            Text(
                text = "Target Frequency",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                allDays.forEach { day ->
                    val isSelected = selectedDays.contains(day)
                    val shortName = day.name.take(1)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) SagePrimary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                selectedDays = if (isSelected) {
                                    if (selectedDays.size > 1) selectedDays - day else selectedDays
                                } else {
                                    selectedDays + day
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shortName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Link to Goal
            if (availableGoals.isNotEmpty()) {
                Text(
                    text = "Link to Goal (Optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        val isNone = selectedGoalId == null
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(if (isNone) SagePrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedGoalId = null }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "None",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isNone) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(availableGoals) { goal ->
                        val isSelected = selectedGoalId == goal.id
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(if (isSelected) SagePrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedGoalId = goal.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = "Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                habit?.id ?: 0L,
                                title,
                                selectedDays,
                                selectedGoalId
                            )
                        }
                    },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                ) {
                    Text(
                        text = if (habit == null) "Add Habit" else "Save Changes",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(name = "Habits Screen Dark", showBackground = true)
@Composable
private fun HabitsScreenDarkPreview() {
    NilianTheme(darkTheme = true) {
        HabitsScreen(
            uiState = HabitsUiState(
                habits = listOf(
                    HabitItem(
                        id = 1,
                        title = "Morning Meditation",
                        currentStreak = 14,
                        bestStreak = 24,
                        isCompletedToday = true,
                        weeklyHistory = listOf(true, true, true, true, true, true, false)
                    ),
                    HabitItem(
                        id = 2,
                        title = "Code Architecture 1hr",
                        currentStreak = 8,
                        bestStreak = 12,
                        isCompletedToday = false,
                        weeklyHistory = listOf(true, true, true, false, true, false, false)
                    )
                )
            ),
            onToggleHabitToday = {},
            onToggleHabitHistoryDay = { _, _ -> },
            onAddHabitClick = {},
            onEditHabitClick = {},
            onDeleteHabitClick = {},
            onDismissAddEditModal = {},
            onSaveHabit = { _, _, _, _ -> }
        )
    }
}
