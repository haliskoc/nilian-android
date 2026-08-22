package com.nilian.app.presentation.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.nilian.app.core.ui.components.EmptyStateWidget
import com.nilian.app.core.ui.components.NilianTopAppBar
import com.nilian.app.core.ui.components.PriorityBadge
import com.nilian.app.core.ui.theme.BottomSheetShape
import com.nilian.app.core.ui.theme.CardShapeMedium
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.core.ui.theme.PillShape
import com.nilian.app.core.ui.theme.SagePrimary
import com.nilian.app.core.ui.theme.extendedColors
import com.nilian.app.domain.model.GoalItem
import com.nilian.app.domain.model.Priority
import com.nilian.app.domain.model.TaskItem
import java.time.LocalDate

enum class TaskFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    COMPLETED("Completed"),
    HIGH_PRIORITY("High Priority")
}

data class TasksUiState(
    val tasks: List<TaskItem> = emptyList(),
    val availableGoals: List<GoalItem> = emptyList(),
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val selectedGoalFilterId: Long? = null,
    val isAddEditModalVisible: Boolean = false,
    val editingTask: TaskItem? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    uiState: TasksUiState,
    onFilterSelect: (TaskFilter) -> Unit,
    onGoalFilterSelect: (Long?) -> Unit,
    onTaskToggle: (TaskItem) -> Unit,
    onTaskClick: (TaskItem) -> Unit,
    onAddTaskClick: () -> Unit,
    onEditTaskClick: (TaskItem) -> Unit,
    onDeleteTaskClick: (TaskItem) -> Unit,
    onDismissAddEditModal: () -> Unit,
    onSaveTask: (
        id: Long,
        title: String,
        description: String?,
        priority: Priority,
        durationMinutes: Int,
        dueDate: LocalDate?,
        goalId: Long?,
        autoRollover: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredTasks = uiState.tasks.filter { task ->
        val matchesStatus = when (uiState.selectedFilter) {
            TaskFilter.ALL -> true
            TaskFilter.PENDING -> !task.isCompleted
            TaskFilter.COMPLETED -> task.isCompleted
            TaskFilter.HIGH_PRIORITY -> task.priority == Priority.HIGH
        }
        val matchesGoal = uiState.selectedGoalFilterId == null || task.goalId == uiState.selectedGoalFilterId
        matchesStatus && matchesGoal
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                NilianTopAppBar(
                    title = "Tasks & Actions",
                    subtitle = "${uiState.tasks.count { !it.isCompleted }} open tasks • ${uiState.tasks.count { it.isCompleted }} completed"
                )

                // Filter Chips Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(TaskFilter.values()) { filter ->
                        val isSelected = uiState.selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(
                                    if (isSelected) SagePrimary else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onFilterSelect(filter) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Goal Filter Chips if goals exist
                    if (uiState.availableGoals.isNotEmpty()) {
                        items(uiState.availableGoals) { goal ->
                            val isSelected = uiState.selectedGoalFilterId == goal.id
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(
                                        if (isSelected) SagePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) SagePrimary else Color.Transparent,
                                        shape = PillShape
                                    )
                                    .clickable {
                                        onGoalFilterSelect(if (isSelected) null else goal.id)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🎯 ${goal.title}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) SagePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = SagePrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (filteredTasks.isEmpty()) {
                EmptyStateWidget(
                    title = "No Tasks Found",
                    description = "Nothing to show under this filter. Tap '+' to create a new task.",
                    icon = Icons.Outlined.CheckCircle,
                    actionLabel = "New Task",
                    onActionClick = onAddTaskClick
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCardItem(
                            task = task,
                            onToggle = { onTaskToggle(task) },
                            onClick = { onTaskClick(task) },
                            onEdit = { onEditTaskClick(task) },
                            onDelete = { onDeleteTaskClick(task) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Modal Bottom Sheet
    if (uiState.isAddEditModalVisible) {
        AddEditTaskModal(
            task = uiState.editingTask,
            availableGoals = uiState.availableGoals,
            onDismiss = onDismissAddEditModal,
            onSave = onSaveTask
        )
    }
}

@Composable
private fun TaskCardItem(
    task: TaskItem,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    CalmCard(
        shape = CardShapeMedium,
        onClick = onClick,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded Checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) SagePrimary else Color.Transparent)
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
                        contentDescription = "Completed",
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

                if (!task.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (task.isRollover) {
                        Text(
                            text = "⏳ Rolled over",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.extendedColors.warning
                        )
                    }

                    Text(
                        text = "⏱ ${task.estimatedDurationMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!task.goalTitle.isNullOrBlank()) {
                        Text(
                            text = "🎯 ${task.goalTitle}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SagePrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            PriorityBadge(priority = task.priority)

            // More Options Menu
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
                        text = { Text("Edit Task") },
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditTaskModal(
    task: TaskItem?,
    availableGoals: List<GoalItem>,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        description: String?,
        priority: Priority,
        durationMinutes: Int,
        dueDate: LocalDate?,
        goalId: Long?,
        autoRollover: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf(task?.title.orEmpty()) }
    var description by remember { mutableStateOf(task?.description.orEmpty()) }
    var priority by remember { mutableStateOf(task?.priority ?: Priority.MEDIUM) }
    var durationMinutes by remember { mutableIntStateOf(task?.estimatedDurationMinutes ?: 30) }
    var selectedGoalId by remember { mutableStateOf(task?.goalId) }
    var autoRollover by remember { mutableStateOf(task?.autoRollover ?: true) }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (task == null) "New Task" else "Edit Task",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            CalmTextField(
                value = title,
                onValueChange = { title = it },
                label = "Task Title",
                placeholder = "What needs to be done?"
            )

            CalmTextField(
                value = description,
                onValueChange = { description = it },
                label = "Notes / Description (Optional)",
                placeholder = "Add context or checklist",
                singleLine = false,
                maxLines = 3
            )

            // Priority Selector
            Text(
                text = "Priority Level",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.values().forEach { p ->
                    val isSelected = priority == p
                    val color = when (p) {
                        Priority.HIGH -> MaterialTheme.extendedColors.priorityHigh
                        Priority.MEDIUM -> MaterialTheme.extendedColors.priorityMedium
                        Priority.LOW -> MaterialTheme.extendedColors.priorityLow
                    }

                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(
                                if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { priority = p }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = p.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Duration Presets
            Text(
                text = "Estimated Time",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15, 30, 45, 60, 90).forEach { mins ->
                    val isSelected = durationMinutes == mins
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(
                                if (isSelected) SagePrimary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { durationMinutes = mins }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${mins}m",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Linked Goal (if any)
            if (availableGoals.isNotEmpty()) {
                Text(
                    text = "Link to Goal",
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

            // Auto-rollover Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Auto-Rollover",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Move to tomorrow if uncompleted at midnight",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = autoRollover,
                    onCheckedChange = { autoRollover = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SagePrimary
                    )
                )
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
                                task?.id ?: 0L,
                                title,
                                description.ifBlank { null },
                                priority,
                                durationMinutes,
                                LocalDate.now(),
                                selectedGoalId,
                                autoRollover
                            )
                        }
                    },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                ) {
                    Text(
                        text = if (task == null) "Create Task" else "Save Changes",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(name = "Tasks Dark", showBackground = true)
@Composable
private fun TasksScreenDarkPreview() {
    NilianTheme(darkTheme = true) {
        TasksScreen(
            uiState = TasksUiState(
                tasks = listOf(
                    TaskItem(id = 1, title = "Kotlin Flow State Handling", priority = Priority.HIGH, estimatedDurationMinutes = 45, isCompleted = false),
                    TaskItem(id = 2, title = "Design System Components", priority = Priority.MEDIUM, estimatedDurationMinutes = 60, isRollover = true),
                    TaskItem(id = 3, title = "Review Week 4 Database Schema", priority = Priority.LOW, estimatedDurationMinutes = 30, isCompleted = true)
                ),
                availableGoals = listOf(
                    GoalItem(id = 1, title = "Nilian OS Release", progressPercent = 0.7f)
                )
            ),
            onFilterSelect = {},
            onGoalFilterSelect = {},
            onTaskToggle = {},
            onTaskClick = {},
            onAddTaskClick = {},
            onEditTaskClick = {},
            onDeleteTaskClick = {},
            onDismissAddEditModal = {},
            onSaveTask = { _, _, _, _, _, _, _, _ -> }
        )
    }
}
