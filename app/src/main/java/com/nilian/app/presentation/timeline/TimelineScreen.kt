package com.nilian.app.presentation.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilian.app.core.ui.components.CalmCard
import com.nilian.app.core.ui.components.CalmTextField
import com.nilian.app.core.ui.components.ConflictAlertBanner
import com.nilian.app.core.ui.components.NilianTopAppBar
import com.nilian.app.core.ui.components.TimeSlotItem
import com.nilian.app.core.ui.theme.BottomSheetShape
import com.nilian.app.core.ui.theme.CardShapeLarge
import com.nilian.app.core.ui.theme.CardShapeMedium
import com.nilian.app.core.ui.theme.DialogShape
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.core.ui.theme.PillShape
import com.nilian.app.core.ui.theme.SagePrimary
import com.nilian.app.core.ui.theme.extendedColors
import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.ConflictItem
import com.nilian.app.domain.model.EventCategory
import com.nilian.app.domain.model.EventItem
import com.nilian.app.domain.model.FreeSlotItem
import com.nilian.app.domain.model.TimeBlockItem
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class TimelineUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val timeBlocks: List<TimeBlockItem> = emptyList(),
    val events: List<EventItem> = emptyList(),
    val freeSlots: List<FreeSlotItem> = emptyList(),
    val conflicts: List<ConflictItem> = emptyList(),
    val isAddBlockDialogVisible: Boolean = false,
    val selectedBlockForDetail: TimeBlockItem? = null,
    val currentTime: LocalTime = LocalTime.now()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    uiState: TimelineUiState,
    onPreviousDayClick: () -> Unit,
    onNextDayClick: () -> Unit,
    onTodayClick: () -> Unit,
    onBlockClick: (TimeBlockItem) -> Unit,
    onAddBlockClick: () -> Unit,
    onDismissAddBlockDialog: () -> Unit,
    onSaveNewBlock: (title: String, blockType: BlockType, startTime: LocalTime, endTime: LocalTime) -> Unit,
    onDeleteBlock: (TimeBlockItem) -> Unit,
    onFreeSlotClick: (FreeSlotItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")
    val scrollState = rememberScrollState()

    // Auto-scroll to morning (e.g. 7:00 AM) on initial open
    LaunchedEffect(Unit) {
        scrollState.scrollTo((7 * 64).dp.value.toInt())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                NilianTopAppBar(
                    title = "24h Timeline",
                    subtitle = "Time-blocking & calendar rhythm",
                    actions = {
                        TextButton(onClick = onTodayClick) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = SagePrimary
                            )
                        }
                    }
                )

                // Date Navigation Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onPreviousDayClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Day",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = uiState.selectedDate.format(dateFormatter),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = onNextDayClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Day",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBlockClick,
                containerColor = SagePrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Time Block")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Collision Alert if any overlap
            if (uiState.conflicts.isNotEmpty()) {
                Box(modifier = Modifier.padding(16.dp)) {
                    ConflictAlertBanner(
                        title = "${uiState.conflicts.size} Overlapping Time Slots",
                        message = "Some blocks collide. Review colored blocks below."
                    )
                }
            }

            // 24h Vertical Interactive Ruler Timeline
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 16.dp)
            ) {
                // 24 Hour Slots Grid
                Column(modifier = Modifier.fillMaxWidth()) {
                    for (hour in 0..23) {
                        HourRulerRow(hour = hour)
                    }
                }

                // Render Scheduled Time Blocks Overlay
                val hourHeightDp = 64.dp
                uiState.timeBlocks.forEach { block ->
                    val startMinutes = block.startTime.hour * 60 + block.startTime.minute
                    val endMinutes = block.endTime.hour * 60 + block.endTime.minute
                    val durationMinutes = (endMinutes - startMinutes).coerceAtLeast(15)

                    val topOffset = (startMinutes / 60f) * hourHeightDp.value
                    val heightDp = (durationMinutes / 60f) * hourHeightDp.value

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 72.dp, end = 16.dp)
                            .offset(y = topOffset.dp)
                            .height(heightDp.dp)
                    ) {
                        TimelineBlockCard(
                            block = block,
                            onClick = { onBlockClick(block) }
                        )
                    }
                }

                // Current Time Line (if viewing today)
                if (uiState.selectedDate == LocalDate.now()) {
                    val currentMinutes = uiState.currentTime.hour * 60 + uiState.currentTime.minute
                    val currentTopOffset = (currentMinutes / 60f) * hourHeightDp.value

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = currentTopOffset.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .offset(x = 62.dp)
                                .clip(CircleShape)
                                .background(SagePrimary)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 68.dp, end = 8.dp)
                                .height(2.dp)
                                .background(SagePrimary)
                        )
                    }
                }
            }
        }
    }

    // Add Block Bottom Sheet / Dialog
    if (uiState.isAddBlockDialogVisible) {
        AddEditBlockModal(
            onDismiss = onDismissAddBlockDialog,
            onSave = onSaveNewBlock
        )
    }

    // Block Details Dialog
    if (uiState.selectedBlockForDetail != null) {
        val block = uiState.selectedBlockForDetail
        AlertDialog(
            onDismissRequest = { /* dismiss */ },
            shape = DialogShape,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = block.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                    if (block.hasConflict) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Conflict",
                            tint = MaterialTheme.extendedColors.conflict,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Time: ${block.startTime} - ${block.endTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Category: ${block.blockType.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (block.hasConflict) {
                        Text(
                            text = "Warning: Overlaps with another scheduled block or event.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.extendedColors.conflict
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onDeleteBlock(block) }) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissAddBlockDialog) {
                    Text(text = "Close", color = SagePrimary)
                }
            }
        )
    }
}

@Composable
private fun HourRulerRow(hour: Int) {
    val timeLabel = String.format("%02d:00", hour)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = timeLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.width(48.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .offset(y = 8.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        )
    }
}

@Composable
private fun TimelineBlockCard(
    block: TimeBlockItem,
    onClick: () -> Unit
) {
    val extended = MaterialTheme.extendedColors
    val blockColor = when (block.blockType) {
        BlockType.SLEEP -> extended.blockSleep
        BlockType.WORKOUT -> extended.blockWorkout
        BlockType.STUDY -> extended.blockStudy
        BlockType.DEEP_WORK -> extended.blockDeepWork
        BlockType.REST -> extended.blockRest
        BlockType.BUFFER, BlockType.GENERAL, BlockType.OTHER -> extended.blockBuffer
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CardShapeMedium)
            .background(blockColor.copy(alpha = 0.22f))
            .border(
                width = if (block.hasConflict) 2.dp else 1.dp,
                color = if (block.hasConflict) extended.conflict else blockColor.copy(alpha = 0.7f),
                shape = CardShapeMedium
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = block.title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${block.startTime} - ${block.endTime} • ${block.blockType.label.split("/").first()}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            if (block.hasConflict) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Collision",
                    tint = extended.conflict,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditBlockModal(
    onDismiss: () -> Unit,
    onSave: (title: String, blockType: BlockType, startTime: LocalTime, endTime: LocalTime) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(BlockType.DEEP_WORK) }
    var startHour by remember { mutableStateOf("09") }
    var startMin by remember { mutableStateOf("00") }
    var endHour by remember { mutableStateOf("10") }
    var endMin by remember { mutableStateOf("30") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

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
                text = "Add Focus / Time Block",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            CalmTextField(
                value = title,
                onValueChange = { title = it },
                label = "Block Title",
                placeholder = "e.g. Deep Work / Kotlin Architecture"
            )

            // Block Type Selector
            Text(
                text = "Block Category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val selectableBlockTypes = listOf(
                BlockType.DEEP_WORK, BlockType.STUDY, BlockType.WORKOUT, BlockType.REST, BlockType.SLEEP, BlockType.BUFFER
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(selectableBlockTypes) { type ->
                    val isSelected = selectedType == type
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(
                                if (isSelected) SagePrimary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedType = type }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = type.label.split("/").first().trim(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Start & End Time Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalmTextField(
                    value = "$startHour:$startMin",
                    onValueChange = {
                        val parts = it.split(":")
                        if (parts.size == 2) {
                            startHour = parts[0]
                            startMin = parts[1]
                        }
                    },
                    label = "Start (HH:mm)",
                    modifier = Modifier.weight(1f)
                )

                CalmTextField(
                    value = "$endHour:$endMin",
                    onValueChange = {
                        val parts = it.split(":")
                        if (parts.size == 2) {
                            endHour = parts[0]
                            endMin = parts[1]
                        }
                    },
                    label = "End (HH:mm)",
                    modifier = Modifier.weight(1f)
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
                        val sH = startHour.toIntOrNull() ?: 9
                        val sM = startMin.toIntOrNull() ?: 0
                        val eH = endHour.toIntOrNull() ?: 10
                        val eM = endMin.toIntOrNull() ?: 30
                        onSave(
                            if (title.isBlank()) selectedType.label else title,
                            selectedType,
                            LocalTime.of(sH.coerceIn(0, 23), sM.coerceIn(0, 59)),
                            LocalTime.of(eH.coerceIn(0, 23), eM.coerceIn(0, 59))
                        )
                    },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                ) {
                    Text(text = "Schedule Block", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(name = "Timeline Dark", showBackground = true)
@Composable
private fun TimelineScreenDarkPreview() {
    NilianTheme(darkTheme = true) {
        TimelineScreen(
            uiState = TimelineUiState(
                timeBlocks = listOf(
                    TimeBlockItem(id = 1, title = "Sleep & Recharge", blockType = BlockType.SLEEP, startTime = LocalTime.of(0, 0), endTime = LocalTime.of(7, 30)),
                    TimeBlockItem(id = 2, title = "Morning Movement / Gym", blockType = BlockType.WORKOUT, startTime = LocalTime.of(8, 0), endTime = LocalTime.of(9, 15)),
                    TimeBlockItem(id = 3, title = "Nilian Jetpack Compose UI", blockType = BlockType.DEEP_WORK, startTime = LocalTime.of(10, 0), endTime = LocalTime.of(12, 30)),
                    TimeBlockItem(id = 4, title = "Project Sync Call", blockType = BlockType.STUDY, startTime = LocalTime.of(12, 0), endTime = LocalTime.of(13, 0), hasConflict = true)
                ),
                conflicts = listOf(
                    ConflictItem(titleA = "Nilian UI Sprint", titleB = "Project Sync Call", timeDescription = "12:00 - 12:30")
                )
            ),
            onPreviousDayClick = {},
            onNextDayClick = {},
            onTodayClick = {},
            onBlockClick = {},
            onAddBlockClick = {},
            onDismissAddBlockDialog = {},
            onSaveNewBlock = { _, _, _, _ -> },
            onDeleteBlock = {},
            onFreeSlotClick = {}
        )
    }
}
