package com.nilian.app.presentation.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilian.app.core.ui.components.CalmCard
import com.nilian.app.core.ui.components.CalmTextField
import com.nilian.app.core.ui.components.EmptyStateWidget
import com.nilian.app.core.ui.components.NilianTopAppBar
import com.nilian.app.core.ui.theme.BottomSheetShape
import com.nilian.app.core.ui.theme.CardShapeLarge
import com.nilian.app.core.ui.theme.CardShapeMedium
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.core.ui.theme.PillShape
import com.nilian.app.core.ui.theme.SagePrimary
import com.nilian.app.domain.model.GoalItem
import com.nilian.app.domain.model.HabitItem
import com.nilian.app.domain.model.TaskItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class GoalWithDetails(
    val goal: GoalItem,
    val linkedTasks: List<TaskItem> = emptyList(),
    val linkedHabits: List<HabitItem> = emptyList()
)

data class GoalsUiState(
    val activeGoals: List<GoalWithDetails> = emptyList(),
    val archivedGoals: List<GoalWithDetails> = emptyList(),
    val showArchived: Boolean = false,
    val isAddEditModalVisible: Boolean = false,
    val editingGoal: GoalItem? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    uiState: GoalsUiState,
    onAddGoalClick: () -> Unit,
    onEditGoalClick: (GoalItem) -> Unit,
    onArchiveGoalClick: (GoalItem) -> Unit,
    onDeleteGoalClick: (GoalItem) -> Unit,
    onDismissAddEditModal: () -> Unit,
    onSaveGoal: (id: Long, title: String, description: String?, targetDate: LocalDate?) -> Unit,
    onTaskToggle: (TaskItem) -> Unit,
    onHabitToggle: (HabitItem) -> Unit,
    onToggleShowArchived: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayGoals = if (uiState.showArchived) uiState.archivedGoals else uiState.activeGoals

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NilianTopAppBar(
                title = "Long-Term Vision",
                subtitle = "Align daily actions with life milestones",
                actions = {
                    TextButton(onClick = onToggleShowArchived) {
                        Text(
                            text = if (uiState.showArchived) "Active Goals" else "Archived",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = SagePrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGoalClick,
                containerColor = SagePrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Goal")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (displayGoals.isEmpty()) {
                EmptyStateWidget(
                    title = if (uiState.showArchived) "No Archived Goals" else "No Goals Defined",
                    description = if (uiState.showArchived) {
                        "Completed or archived goals will appear here."
                    } else {
                        "Set your core vision and attach tasks and habits to see steady progress."
                    },
                    icon = Icons.Outlined.Flag,
                    actionLabel = if (!uiState.showArchived) "Define Goal" else null,
                    onActionClick = onAddGoalClick
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(displayGoals, key = { it.goal.id }) { goalDetails ->
                        GoalCardItem(
                            goalDetails = goalDetails,
                            onEdit = { onEditGoalClick(goalDetails.goal) },
                            onArchive = { onArchiveGoalClick(goalDetails.goal) },
                            onDelete = { onDeleteGoalClick(goalDetails.goal) },
                            onTaskToggle = onTaskToggle,
                            onHabitToggle = onHabitToggle
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Modal
    if (uiState.isAddEditModalVisible) {
        AddEditGoalModal(
            goal = uiState.editingGoal,
            onDismiss = onDismissAddEditModal,
            onSave = onSaveGoal
        )
    }
}

@Composable
private fun GoalCardItem(
    goalDetails: GoalWithDetails,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onTaskToggle: (TaskItem) -> Unit,
    onHabitToggle: (HabitItem) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isMenuOpen by remember { mutableStateOf(false) }
    val goal = goalDetails.goal

    val daysLeftText = remember(goal.targetDate) {
        goal.targetDate?.let { date ->
            val days = ChronoUnit.DAYS.between(LocalDate.now(), date)
            when {
                days < 0 -> "${-days} days overdue"
                days == 0L -> "Due today"
                else -> "$days days remaining"
            }
        }
    }

    CalmCard(
        shape = CardShapeLarge,
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!goal.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { isMenuOpen = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = { isMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Goal") },
                            onClick = {
                                isMenuOpen = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (goal.isArchived) "Unarchive" else "Archive") },
                            onClick = {
                                isMenuOpen = false
                                onArchive()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(18.dp))
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

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(goal.progressPercent * 100).toInt()}% Progress",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = SagePrimary
                )

                if (daysLeftText != null) {
                    Text(
                        text = daysLeftText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { goal.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SagePrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Linked Items Accordion Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShapeMedium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📋 ${goalDetails.linkedTasks.size} Tasks",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "🔄 ${goalDetails.linkedHabits.size} Habits",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expanded Section (Linked Tasks & Habits)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (goalDetails.linkedTasks.isNotEmpty()) {
                        Text(
                            text = "Linked Tasks",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        goalDetails.linkedTasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTaskToggle(task) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (task.isCompleted) SagePrimary else Color.Transparent)
                                        .border(1.dp, if (task.isCompleted) SagePrimary else MaterialTheme.colorScheme.outline, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (task.isCompleted) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (goalDetails.linkedHabits.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Linked Habits",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        goalDetails.linkedHabits.forEach { habit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onHabitToggle(habit) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔥 ${habit.title} (${habit.currentStreak}d streak)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditGoalModal(
    goal: GoalItem?,
    onDismiss: () -> Unit,
    onSave: (id: Long, title: String, description: String?, targetDate: LocalDate?) -> Unit
) {
    var title by remember { mutableStateOf(goal?.title.orEmpty()) }
    var description by remember { mutableStateOf(goal?.description.orEmpty()) }
    var targetDateString by remember {
        mutableStateOf(goal?.targetDate?.format(DateTimeFormatter.ISO_LOCAL_DATE).orEmpty())
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                text = if (goal == null) "Define Long-Term Goal" else "Edit Goal",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            CalmTextField(
                value = title,
                onValueChange = { title = it },
                label = "Goal Title",
                placeholder = "e.g. Master Android Architecture, Run Marathon"
            )

            CalmTextField(
                value = description,
                onValueChange = { description = it },
                label = "Vision / Why this matters",
                placeholder = "Describe the motivation and core outcome",
                singleLine = false,
                maxLines = 3
            )

            CalmTextField(
                value = targetDateString,
                onValueChange = { targetDateString = it },
                label = "Target Date (YYYY-MM-DD, Optional)",
                placeholder = "e.g. 2026-12-31"
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                            val parsedDate = try {
                                if (targetDateString.isNotBlank()) LocalDate.parse(targetDateString) else null
                            } catch (e: Exception) {
                                null
                            }
                            onSave(
                                goal?.id ?: 0L,
                                title,
                                description.ifBlank { null },
                                parsedDate
                            )
                        }
                    },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                ) {
                    Text(
                        text = if (goal == null) "Create Goal" else "Save Changes",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(name = "Goals Dark", showBackground = true)
@Composable
private fun GoalsScreenDarkPreview() {
    NilianTheme(darkTheme = true) {
        GoalsScreen(
            uiState = GoalsUiState(
                activeGoals = listOf(
                    GoalWithDetails(
                        goal = GoalItem(
                            id = 1,
                            title = "Launch Nilian Personal Life OS",
                            description = "Full clean architecture, Room DB, Jetpack Compose, CI/CD pipeline",
                            targetDate = LocalDate.now().plusDays(25),
                            progressPercent = 0.72f
                        ),
                        linkedTasks = listOf(
                            TaskItem(id = 1, title = "Compose UI Polish", isCompleted = true),
                            TaskItem(id = 2, title = "DataStore encryption testing", isCompleted = false)
                        ),
                        linkedHabits = listOf(
                            HabitItem(id = 1, title = "Daily 2h Deep Work", currentStreak = 14)
                        )
                    )
                )
            ),
            onAddGoalClick = {},
            onEditGoalClick = {},
            onArchiveGoalClick = {},
            onDeleteGoalClick = {},
            onDismissAddEditModal = {},
            onSaveGoal = { _, _, _, _ -> },
            onTaskToggle = {},
            onHabitToggle = {},
            onToggleShowArchived = {}
        )
    }
}
