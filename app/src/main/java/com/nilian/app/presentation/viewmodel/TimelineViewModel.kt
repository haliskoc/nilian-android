package com.nilian.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.Event
import com.nilian.app.domain.model.FreeSlotItem
import com.nilian.app.domain.model.TimeBlock
import com.nilian.app.domain.model.TimeBlockItem
import com.nilian.app.domain.model.toItem
import com.nilian.app.domain.model.toUiConflictItem
import com.nilian.app.domain.model.toUiItem
import com.nilian.app.domain.repository.EventRepository
import com.nilian.app.domain.repository.TimeBlockRepository
import com.nilian.app.domain.usecase.DetectCollisionsUseCase
import com.nilian.app.domain.usecase.FreeSlotFinderUseCase
import com.nilian.app.presentation.timeline.TimelineUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class TimelineViewModel(
    private val timeBlockRepository: TimeBlockRepository,
    private val eventRepository: EventRepository,
    private val detectCollisionsUseCase: DetectCollisionsUseCase = DetectCollisionsUseCase(),
    private val freeSlotFinderUseCase: FreeSlotFinderUseCase = FreeSlotFinderUseCase()
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isAddBlockDialogVisible = MutableStateFlow(false)
    private val _selectedBlockForDetail = MutableStateFlow<TimeBlockItem?>(null)

    val uiState: StateFlow<TimelineUiState> = combine(
        _selectedDate,
        _isAddBlockDialogVisible,
        _selectedBlockForDetail,
        timeBlockRepository.getAllTimeBlocks(),
        eventRepository.getAllEvents()
    ) { params ->
        val date = params[0] as LocalDate
        val isAddDialogVisible = params[1] as Boolean
        val selectedBlock = params[2] as TimeBlockItem?
        val allTimeBlocks = params[3] as List<TimeBlock>
        val allEvents = params[4] as List<Event>

        val dayTimeBlocks = allTimeBlocks.filter { it.date == date }
        val dayEvents = allEvents.filter { event ->
            val sDate = event.startDateTime.toLocalDate()
            val eDate = event.endDateTime.toLocalDate()
            !date.isBefore(sDate) && !date.isAfter(eDate)
        }

        val collisions = detectCollisionsUseCase(dayEvents, dayTimeBlocks, date)
        val conflictUiItems = collisions.map { it.toUiConflictItem() }

        val conflictingBlockIds = collisions.mapNotNull {
            if (it.itemA.type == com.nilian.app.domain.model.ConflictSourceType.TIME_BLOCK) it.itemA.id
            else if (it.itemB.type == com.nilian.app.domain.model.ConflictSourceType.TIME_BLOCK) it.itemB.id
            else null
        }.toSet()

        val conflictingEventIds = collisions.mapNotNull {
            if (it.itemA.type == com.nilian.app.domain.model.ConflictSourceType.EVENT) it.itemA.id
            else if (it.itemB.type == com.nilian.app.domain.model.ConflictSourceType.EVENT) it.itemB.id
            else null
        }.toSet()

        val blockItems = dayTimeBlocks.map { it.toItem(hasConflict = conflictingBlockIds.contains(it.id)) }
        val eventItems = dayEvents.map { it.toItem(hasConflict = conflictingEventIds.contains(it.id)) }

        val freeSlots = freeSlotFinderUseCase(dayEvents, dayTimeBlocks, date).map { it.toUiItem() }

        TimelineUiState(
            selectedDate = date,
            timeBlocks = blockItems,
            events = eventItems,
            freeSlots = freeSlots,
            conflicts = conflictUiItems,
            isAddBlockDialogVisible = isAddDialogVisible,
            selectedBlockForDetail = selectedBlock,
            currentTime = LocalTime.now()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimelineUiState()
    )

    fun onPreviousDayClick() {
        _selectedDate.update { it.minusDays(1) }
    }

    fun onNextDayClick() {
        _selectedDate.update { it.plusDays(1) }
    }

    fun onTodayClick() {
        _selectedDate.value = LocalDate.now()
    }

    fun onBlockClick(blockItem: TimeBlockItem) {
        _selectedBlockForDetail.value = blockItem
    }

    fun onAddBlockClick() {
        _isAddBlockDialogVisible.value = true
    }

    fun onDismissAddBlockDialog() {
        _isAddBlockDialogVisible.value = false
    }

    fun onSaveNewBlock(title: String, blockType: BlockType, startTime: LocalTime, endTime: LocalTime) {
        _isAddBlockDialogVisible.value = false
        viewModelScope.launch {
            timeBlockRepository.insertTimeBlock(
                TimeBlock(
                    title = title,
                    blockType = blockType,
                    startTime = startTime,
                    endTime = endTime,
                    date = _selectedDate.value
                )
            )
        }
    }

    fun onDeleteBlock(blockItem: TimeBlockItem) {
        viewModelScope.launch {
            timeBlockRepository.deleteTimeBlockById(blockItem.id)
            if (_selectedBlockForDetail.value?.id == blockItem.id) {
                _selectedBlockForDetail.value = null
            }
        }
    }

    class Factory(
        private val timeBlockRepository: TimeBlockRepository,
        private val eventRepository: EventRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimelineViewModel(timeBlockRepository, eventRepository) as T
        }
    }
}
