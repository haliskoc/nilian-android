package com.nilian.app.presentation.goals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.outlined.DateRange
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
import com.nilian.app.core.ui.components.MilestoneCountdownBadge
import com.nilian.app.core.ui.components.MilestoneCountdownCarousel
import com.nilian.app.core.ui.components.NilianTopAppBar
import com.nilian.app.core.ui.components.rememberMilestoneTheme
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
import java.util.Locale

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
                title = "Uzun Vadeli Vizyon & Hedefler",
                subtitle = "Günlük eylemlerinizi yaşam kilometre taşlarıyla hizalayın",
                actions = {
                    TextButton(onClick = onToggleShowArchived) {
                        Text(
                            text = if (uiState.showArchived) "Aktif Hedefler" else "Arşiv",
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
                Icon(imageVector = Icons.Default.Add, contentDescription = "Yeni Hedef")
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
                    title = if (uiState.showArchived) "Arşivlenmiş Hedef Yok" else "Henüz Hedef Tanımlanmadı",
                    description = if (uiState.showArchived) {
                        "Tamamlanan veya arşivlenen hedefler burada görünür."
                    } else {
                        "Temel vizyonunuzu belirleyin, görev ve rutinleri bağlayarak istikrarlı ilerlemeyi görün."
                    },
                    icon = Icons.Outlined.Flag,
                    actionLabel = if (!uiState.showArchived) "Hedef Belirle" else null,
                    onActionClick = onAddGoalClick
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Horizon Milestone Countdowns Carousel (when active goals exist)
                    if (!uiState.showArchived && uiState.activeGoals.isNotEmpty()) {
                        item {
                            MilestoneCountdownCarousel(
                                goals = uiState.activeGoals.map { it.goal },
                                onGoalClick = { onEditGoalClick(it) },
                                title = "Kilometre Taşı Sayaçları",
                                subtitle = "Yaklaşan hedeflerinize kalan günler ve ilerleme"
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    item {
                        Text(
                            text = if (uiState.showArchived) "Arşivlenmiş Vizyonlar" else "Tüm Aktif Hedefler",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

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
    val theme = rememberMilestoneTheme(title = goal.title, description = goal.description)

    val animatedProgress by animateFloatAsState(
        targetValue = goal.progressPercent.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "GoalProgressAnim"
    )

    CalmCard(
        shape = CardShapeLarge,
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            // Header with Theme Avatar Emoji + Title + Countdown Badge + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(theme.containerColor)
                            .border(1.dp, theme.accentColor.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = theme.emoji,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!goal.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = goal.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (goal.targetDate != null) {
                        MilestoneCountdownBadge(targetDate = goal.targetDate)
                    }

                    Box {
                        IconButton(
                            onClick = { isMenuOpen = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Seçenekler",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuOpen,
                            onDismissRequest = { isMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Hedefi Düzenle") },
                                onClick = {
                                    isMenuOpen = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (goal.isArchived) "Arşivden Çıkar" else "Arşivle") },
                                onClick = {
                                    isMenuOpen = false
                                    onArchive()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sil", color = MaterialTheme.colorScheme.error) },
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

            // Progress Bar & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}% Tamamlandı",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = theme.accentColor
                )

                if (goal.targetDate != null) {
                    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
                    Text(
                        text = "Hedef: ${goal.targetDate.format(formatter)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = theme.accentColor,
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
                        text = "📋 ${goalDetails.linkedTasks.size} Görev",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "🔄 ${goalDetails.linkedHabits.size} Rutin",
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

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (goalDetails.linkedTasks.isNotEmpty()) {
                        Text(
                            text = "Bağlı Görevler:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        goalDetails.linkedTasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .clickable { onTaskToggle(task) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (task.isCompleted) SagePrimary else Color.Transparent)
                                        .border(1.dp, if (task.isCompleted) SagePrimary else MaterialTheme.colorScheme.outline, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (task.isCompleted) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
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
                            text = "Bağlı Rutinler:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        goalDetails.linkedHabits.forEach { habit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .clickable { onHabitToggle(habit) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (habit.isCompletedToday) SagePrimary else Color.Transparent)
                                        .border(1.dp, if (habit.isCompletedToday) SagePrimary else MaterialTheme.colorScheme.outline, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (habit.isCompletedToday) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                                Text(
                                    text = habit.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (habit.isCompletedToday) SagePrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "🔥 ${habit.currentStreak}g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (goalDetails.linkedTasks.isEmpty() && goalDetails.linkedHabits.isEmpty()) {
                        Text(
                            text = "Henüz bu hedefe bağlı görev veya rutin bulunmuyor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(goal?.title.orEmpty()) }
    var description by remember { mutableStateOf(goal?.description.orEmpty()) }
    var targetDate by remember { mutableStateOf(goal?.targetDate ?: LocalDate.now().plusMonths(3)) }

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
                text = if (goal == null) "Yeni Hedef Tanımla" else "Hedefi Düzenle",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            CalmTextField(
                value = title,
                onValueChange = { title = it },
                label = "Hedef Başlığı (örn: Sınavı Kazan, MVP Lansmanı)"
            )

            CalmTextField(
                value = description,
                onValueChange = { description = it },
                label = "Vizyon ve Başarı Kriterleri (İsteğe bağlı)",
                singleLine = false,
                maxLines = 3
            )

            // Quick Target Date Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Hedeflenen Tarih",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val today = LocalDate.now()
                    val dateOptions = listOf(
                        "1 Ay" to today.plusMonths(1),
                        "3 Ay" to today.plusMonths(3),
                        "6 Ay" to today.plusMonths(6),
                        "1 Yıl" to today.plusYears(1)
                    )

                    dateOptions.forEach { (label, date) ->
                        val isSelected = targetDate == date
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(if (isSelected) SagePrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { targetDate = date }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
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
                    Text("İptal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                goal?.id ?: 0L,
                                title.trim(),
                                description.trim().ifBlank { null },
                                targetDate
                            )
                            onDismiss()
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                ) {
                    Text("Kaydet")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// =========================================================================
// Previews
// =========================================================================

@Preview(name = "Goals Screen Dark", showBackground = true)
@Composable
private fun GoalsScreenDarkPreview() {
    NilianTheme(darkTheme = true) {
        GoalsScreen(
            uiState = GoalsUiState(
                activeGoals = listOf(
                    GoalWithDetails(
                        goal = GoalItem(
                            id = 1,
                            title = "Bahar Dönemi Final Sınavları",
                            description = "Tüm derslerden A ile mezuniyet",
                            targetDate = LocalDate.now().plusDays(14),
                            progressPercent = 0.65f
                        ),
                        linkedTasks = listOf(
                            TaskItem(id = 101, title = "Algoritmalar Final Soru Çözümü", isCompleted = true),
                            TaskItem(id = 102, title = "İşletim Sistemleri Özet Notları", isCompleted = false)
                        ),
                        linkedHabits = listOf(
                            HabitItem(id = 201, title = "Günde 2 saat odaklı ders", currentStreak = 8, isCompletedToday = true)
                        )
                    ),
                    GoalWithDetails(
                        goal = GoalItem(
                            id = 2,
                            title = "Nilian 1.0 Lansmanı",
                            description = "Jetpack Compose ve Room tabanlı mimari teslimi",
                            targetDate = LocalDate.now().plusDays(30),
                            progressPercent = 0.80f
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
