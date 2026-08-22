package com.nilian.app.presentation.adaptive

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nilian.app.presentation.focus.FocusTimerScreen
import com.nilian.app.presentation.goals.GoalsScreen
import com.nilian.app.presentation.habits.HabitsScreen
import com.nilian.app.presentation.inbox.InboxScreen
import com.nilian.app.presentation.lock.LockScreen
import com.nilian.app.presentation.navigation.NilianAdaptiveNavigationScaffold
import com.nilian.app.presentation.navigation.NilianDestination
import com.nilian.app.presentation.settings.SettingsScreen
import com.nilian.app.presentation.tasks.TasksScreen
import com.nilian.app.presentation.timeline.TimelineScreen
import com.nilian.app.presentation.today.TodayScreen
import com.nilian.app.presentation.viewmodel.GoalsViewModel
import com.nilian.app.presentation.viewmodel.HabitsViewModel
import com.nilian.app.presentation.viewmodel.MainViewModel
import com.nilian.app.presentation.viewmodel.SettingsViewModel
import com.nilian.app.presentation.viewmodel.TasksViewModel
import com.nilian.app.presentation.viewmodel.TimelineViewModel
import com.nilian.app.presentation.viewmodel.TodayViewModel

/**
 * Adaptive Root Layout responding to screen width size classes (Compact vs Medium/Expanded).
 *
 * Phone Form Factor (Compact):
 * - Bottom NavigationBar for seamless single-handed navigation.
 * - Single-column responsive card layouts.
 *
 * Tablet Form Factor (Medium & Expanded):
 * - Fixed left NavigationRail with persistent branding.
 * - Enhanced widescreen responsive canvas with multi-column / dual-pane presentation.
 */
@Composable
fun AdaptiveRootLayout(
    windowWidthSizeClass: WindowWidthSizeClass,
    mainViewModel: MainViewModel,
    todayViewModel: TodayViewModel,
    timelineViewModel: TimelineViewModel,
    tasksViewModel: TasksViewModel,
    habitsViewModel: HabitsViewModel,
    goalsViewModel: GoalsViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val isUnlocked by mainViewModel.isUnlocked.collectAsState()
    val lockUiState by mainViewModel.lockUiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var currentDestination by rememberSaveable { mutableStateOf(NilianDestination.TODAY) }
    val isExpandedScreen = windowWidthSizeClass != WindowWidthSizeClass.Compact

    if (!isUnlocked) {
        // App Lock Screen
        LockScreen(
            uiState = lockUiState,
            onDigitClick = mainViewModel::onDigitClick,
            onDeleteClick = mainViewModel::onDeleteClick,
            onBiometricsClick = {
                val activity = context as? androidx.fragment.app.FragmentActivity
                if (activity != null) {
                    mainViewModel.onBiometricsClick(activity)
                }
            },
            onForgotPinClick = mainViewModel::onForgotPinClick,
            onDismissForgotPinDialog = mainViewModel::onDismissForgotPinDialog,
            modifier = modifier
        )
    } else {
        // Main Adaptive Scaffold
        NilianAdaptiveNavigationScaffold(
            currentDestination = currentDestination,
            onNavigateToDestination = { newDestination ->
                currentDestination = newDestination
            },
            isExpandedScreen = isExpandedScreen,
            modifier = modifier
        ) {
            Crossfade(
                targetState = currentDestination,
                animationSpec = tween(durationMillis = 200),
                label = "ScreenCrossfade"
            ) { destination ->
                when (destination) {
                    NilianDestination.TODAY -> {
                        val uiState by todayViewModel.uiState.collectAsState()
                        TodayScreen(
                            uiState = uiState,
                            onViewModeToggle = todayViewModel::onViewModeToggle,
                            onTaskToggle = todayViewModel::onTaskToggle,
                            onHabitToggle = todayViewModel::onHabitToggle,
                            onQuickAddClick = todayViewModel::onQuickAddClick,
                            onDismissQuickAddSheet = todayViewModel::onDismissQuickAddSheet,
                            onQuickAddOptionSelected = todayViewModel::onQuickAddOptionSelected,
                            onConflictReviewClick = {
                                currentDestination = NilianDestination.TIMELINE
                            },
                            onFreeSlotClick = {
                                currentDestination = NilianDestination.TIMELINE
                            },
                            onTaskClick = {
                                currentDestination = NilianDestination.TASKS
                            },
                            onGoalClick = {
                                currentDestination = NilianDestination.GOALS
                            },
                            onBrainDumpClick = todayViewModel::onOpenBrainDump,
                            onDayTemplatesClick = todayViewModel::onOpenDayTemplates,
                            onDismissBrainDump = todayViewModel::onDismissBrainDump,
                            onDismissDayTemplates = todayViewModel::onDismissDayTemplates,
                            onSaveBrainDumpNote = todayViewModel::onSaveBrainDumpNote,
                            onApplyDayTemplate = todayViewModel::onApplyDayTemplate,
                            onConvertToTask = todayViewModel::onConvertToTask,
                            onConvertToEvent = todayViewModel::onConvertToEvent,
                            onConvertToGoal = todayViewModel::onConvertToGoal,
                            onFocusSessionCompleted = todayViewModel::insertCompletedTimeBlock
                        )
                    }

                    NilianDestination.TIMELINE -> {
                        val uiState by timelineViewModel.uiState.collectAsState()
                        TimelineScreen(
                            uiState = uiState,
                            onPreviousDayClick = timelineViewModel::onPreviousDayClick,
                            onNextDayClick = timelineViewModel::onNextDayClick,
                            onTodayClick = timelineViewModel::onTodayClick,
                            onBlockClick = timelineViewModel::onBlockClick,
                            onAddBlockClick = timelineViewModel::onAddBlockClick,
                            onDismissAddBlockDialog = timelineViewModel::onDismissAddBlockDialog,
                            onSaveNewBlock = timelineViewModel::onSaveNewBlock,
                            onDeleteBlock = timelineViewModel::onDeleteBlock,
                            onFreeSlotClick = {
                                timelineViewModel.onAddBlockClick()
                            }
                        )
                    }

                    NilianDestination.TASKS -> {
                        val uiState by tasksViewModel.uiState.collectAsState()
                        TasksScreen(
                            uiState = uiState,
                            onFilterSelect = tasksViewModel::onFilterSelect,
                            onGoalFilterSelect = tasksViewModel::onGoalFilterSelect,
                            onTaskToggle = tasksViewModel::onTaskToggle,
                            onTaskClick = tasksViewModel::onEditTaskClick,
                            onAddTaskClick = tasksViewModel::onAddTaskClick,
                            onEditTaskClick = tasksViewModel::onEditTaskClick,
                            onDeleteTaskClick = tasksViewModel::onDeleteTaskClick,
                            onDismissAddEditModal = tasksViewModel::onDismissAddEditModal,
                            onSaveTask = tasksViewModel::onSaveTask
                        )
                    }

                    NilianDestination.HABITS -> {
                        val uiState by habitsViewModel.uiState.collectAsState()
                        HabitsScreen(
                            uiState = uiState,
                            onToggleHabitToday = habitsViewModel::onToggleHabitToday,
                            onToggleHabitHistoryDay = habitsViewModel::onToggleHabitHistoryDay,
                            onAddHabitClick = habitsViewModel::onAddHabitClick,
                            onEditHabitClick = habitsViewModel::onEditHabitClick,
                            onDeleteHabitClick = habitsViewModel::onDeleteHabitClick,
                            onDismissAddEditModal = habitsViewModel::onDismissAddEditModal,
                            onSaveHabit = habitsViewModel::onSaveHabit
                        )
                    }

                    NilianDestination.GOALS -> {
                        val uiState by goalsViewModel.uiState.collectAsState()
                        GoalsScreen(
                            uiState = uiState,
                            onAddGoalClick = goalsViewModel::onAddGoalClick,
                            onEditGoalClick = goalsViewModel::onEditGoalClick,
                            onArchiveGoalClick = goalsViewModel::onArchiveGoalClick,
                            onDeleteGoalClick = goalsViewModel::onDeleteGoalClick,
                            onDismissAddEditModal = goalsViewModel::onDismissAddEditModal,
                            onSaveGoal = goalsViewModel::onSaveGoal,
                            onTaskToggle = goalsViewModel::onTaskToggle,
                            onHabitToggle = goalsViewModel::onHabitToggle,
                            onToggleShowArchived = goalsViewModel::onToggleShowArchived
                        )
                    }

                    NilianDestination.SETTINGS -> {
                        val uiState by settingsViewModel.uiState.collectAsState()
                        SettingsScreen(
                            uiState = uiState,
                            onThemeModeChange = settingsViewModel::onThemeModeChange,
                            onBiometricsToggle = settingsViewModel::onBiometricsToggle,
                            onAutoLockToggle = settingsViewModel::onAutoLockToggle,
                            onAutoRolloverToggle = settingsViewModel::onAutoRolloverToggle,
                            onDailyMaxHoursChange = settingsViewModel::onDailyMaxHoursChange,
                            onChangePinClick = settingsViewModel::onChangePinClick,
                            onSaveNewPin = settingsViewModel::onSaveNewPin,
                            onDismissChangePinDialog = settingsViewModel::onDismissChangePinDialog,
                            onExportBackupClick = {
                                settingsViewModel.onExportBackupClick()
                            },
                            onImportBackupClick = {
                                settingsViewModel.onImportBackupClick()
                            },
                            onClearDataClick = settingsViewModel::onClearDataClick,
                            onConfirmClearData = settingsViewModel::onConfirmClearData,
                            onDismissClearDataDialog = settingsViewModel::onDismissClearDataDialog
                        )
                    }

                    NilianDestination.FOCUS_TIMER -> {
                        FocusTimerScreen(
                            onBackClick = {
                                currentDestination = NilianDestination.TODAY
                            }
                        )
                    }

                    NilianDestination.INBOX -> {
                        com.nilian.app.presentation.inbox.InboxScreen(
                            onBackClick = { currentDestination = NilianDestination.TODAY },
                            onSaveTasks = { tasks ->
                                tasksViewModel.onSaveBatchTasks(tasks)
                                currentDestination = NilianDestination.TASKS
                            }
                        )
                    }

                    NilianDestination.LOCK -> {
                        LockScreen(
                            uiState = lockUiState,
                            onDigitClick = mainViewModel::onDigitClick,
                            onDeleteClick = mainViewModel::onDeleteClick,
                            onBiometricsClick = {
                                val activity = context as? androidx.fragment.app.FragmentActivity
                                if (activity != null) {
                                    mainViewModel.onBiometricsClick(activity)
                                }
                            },
                            onForgotPinClick = mainViewModel::onForgotPinClick,
                            onDismissForgotPinDialog = mainViewModel::onDismissForgotPinDialog
                        )
                    }
                }
            }
        }
    }
}
