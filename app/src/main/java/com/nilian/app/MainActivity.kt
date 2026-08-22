package com.nilian.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.domain.model.ThemeMode
import com.nilian.app.presentation.adaptive.AdaptiveRootLayout
import com.nilian.app.presentation.viewmodel.GoalsViewModel
import com.nilian.app.presentation.viewmodel.HabitsViewModel
import com.nilian.app.presentation.viewmodel.MainViewModel
import com.nilian.app.presentation.viewmodel.SettingsViewModel
import com.nilian.app.presentation.viewmodel.TasksViewModel
import com.nilian.app.presentation.viewmodel.TimelineViewModel
import com.nilian.app.presentation.viewmodel.TodayViewModel

class MainActivity : ComponentActivity() {

    private val app by lazy { application as NilianApp }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(app.securityPreferences)
    }

    private val todayViewModel: TodayViewModel by viewModels {
        TodayViewModel.Factory(
            app.taskRepository,
            app.eventRepository,
            app.habitRepository,
            app.timeBlockRepository,
            app.goalRepository
        )
    }

    private val timelineViewModel: TimelineViewModel by viewModels {
        TimelineViewModel.Factory(
            app.timeBlockRepository,
            app.eventRepository
        )
    }

    private val tasksViewModel: TasksViewModel by viewModels {
        TasksViewModel.Factory(
            app.taskRepository,
            app.goalRepository
        )
    }

    private val habitsViewModel: HabitsViewModel by viewModels {
        HabitsViewModel.Factory(
            app.habitRepository,
            app.goalRepository
        )
    }

    private val goalsViewModel: GoalsViewModel by viewModels {
        GoalsViewModel.Factory(
            app.goalRepository,
            app.taskRepository,
            app.habitRepository
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(
            app.securityPreferences,
            app.jsonBackupRestoreUseCase
        )
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val themeMode by mainViewModel.themeMode.collectAsState()

            val isDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            NilianTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AdaptiveRootLayout(
                        windowWidthSizeClass = windowSizeClass.widthSizeClass,
                        mainViewModel = mainViewModel,
                        todayViewModel = todayViewModel,
                        timelineViewModel = timelineViewModel,
                        tasksViewModel = tasksViewModel,
                        habitsViewModel = habitsViewModel,
                        goalsViewModel = goalsViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
