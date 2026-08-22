package com.nilian.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.nilian.app.MainActivity
import com.nilian.app.core.database.NilianDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

// Calm Color Palette for Widget
private val WidgetBackground = Color(0xFF14171A)
private val WidgetCardBackground = Color(0xFF1E232A)
private val WidgetCardBorder = Color(0xFF2B323C)
private val SageAccent = Color(0xFF4E876A)
private val SageAccentLight = Color(0xFF68A385)
private val AmberAccent = Color(0xFFD99B43)
private val TextPrimary = Color(0xFFF1F3F5)
private val TextSecondary = Color(0xFF9EA7B3)

data class WidgetContentState(
    val dateText: String = "",
    val completedRatioText: String = "0%",
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val nextUpTitle: String = "Planlanmış etkinlik yok",
    val nextUpTime: String = "Günün geri kalanı serbest",
    val topTaskTitle: String? = null,
    val topTaskPriority: String = "MEDIUM",
    val activeHabitTitle: String? = null,
    val activeHabitStreak: Int = 0
)

class NilianAppWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = loadWidgetState(context)

        provideContent {
            GlanceTheme {
                WidgetScaffold(context = context, state = state)
            }
        }
    }

    private suspend fun loadWidgetState(context: Context): WidgetContentState = withContext(Dispatchers.IO) {
        try {
            val db = NilianDatabase.getInstance(context)
            val today = LocalDate.now()
            val now = LocalTime.now()

            // 1. Format date
            val dayName = today.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Locale.getDefault())
            val monthName = today.month.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault())
            val dateStr = "$dayName, ${today.dayOfMonth} $monthName"

            // 2. Query tasks
            val tasks = db.taskDao().getTasksForDateSync(today)
            val completedTasks = tasks.count { it.isCompleted }
            val totalTasks = tasks.size

            // 3. Query habits
            val habits = db.habitDao().getAllHabitsSync()
            val activeHabits = habits.filter { it.targetDaysOfWeek.contains(today.dayOfWeek) }
            var completedHabitsCount = 0
            for (h in activeHabits) {
                val logs = db.habitDao().getLogsForHabitSync(h.id)
                if (logs.any { it.date == today && it.isCompleted }) {
                    completedHabitsCount++
                }
            }

            val totalItems = totalTasks + activeHabits.size
            val completedItems = completedTasks + completedHabitsCount
            val percent = if (totalItems > 0) ((completedItems.toFloat() / totalItems) * 100).toInt() else 100

            // 4. Query next up block
            val timeBlocks = db.timeBlockDao().getTimeBlocksForDateSync(today)
            val nextBlock = timeBlocks.filter { it.endTime > now }
                .minByOrNull { it.startTime }

            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val nextTitle = nextBlock?.title ?: "Sıradaki Odak Aralığı"
            val nextTime = if (nextBlock != null) {
                "${nextBlock.startTime.format(timeFormatter)} - ${nextBlock.endTime.format(timeFormatter)}"
            } else {
                "Günün geri kalanı serbest ve sakin 🌿"
            }

            // 5. Top uncompleted task
            val topTask = tasks.filter { !it.isCompleted }
                .sortedBy { it.priority.ordinal }
                .firstOrNull()

            // 6. Active habit
            val topHabit = activeHabits.firstOrNull()

            WidgetContentState(
                dateText = dateStr,
                completedRatioText = "$percent%",
                completedCount = completedItems,
                totalCount = totalItems,
                nextUpTitle = nextTitle,
                nextUpTime = nextTime,
                topTaskTitle = topTask?.title,
                topTaskPriority = topTask?.priority?.name ?: "MEDIUM",
                activeHabitTitle = topHabit?.title,
                activeHabitStreak = topHabit?.currentStreak ?: 0
            )
        } catch (e: Exception) {
            val today = LocalDate.now()
            val dayName = today.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Locale.getDefault())
            WidgetContentState(
                dateText = "$dayName, ${today.dayOfMonth}",
                completedRatioText = "100%",
                nextUpTitle = "Nilian Kişisel OS",
                nextUpTime = "Bugün için odaklanmaya hazır 🌿"
            )
        }
    }
}

@Composable
private fun WidgetScaffold(
    context: Context,
    state: WidgetContentState
) {
    val launchAppIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val launchQuickCaptureIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("ACTION_TARGET", "inbox")
    }
    val launchFocusTimerIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra("ACTION_TARGET", "focus_timer")
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(WidgetBackground))
            .cornerRadius(20.dp)
            .padding(14.dp)
            .clickable(actionStartActivity(launchAppIntent))
    ) {
        // --- Header: App Brand + Date + Progress Ring ---
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌿 Nilian",
                        style = TextStyle(
                            color = ColorProvider(SageAccentLight),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = state.dateText,
                    style = TextStyle(
                        color = ColorProvider(TextPrimary),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Calm circular progress badge
            Box(
                modifier = GlanceModifier
                    .background(ColorProvider(SageAccent.copy(alpha = 0.25f)))
                    .cornerRadius(12.dp)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${state.completedRatioText} Tamam",
                    style = TextStyle(
                        color = ColorProvider(SageAccentLight),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        // --- Next Up (Sıradaki) Highlight Card ---
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(WidgetCardBackground))
                .cornerRadius(12.dp)
                .padding(10.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⏱️ SIRADAKİ",
                        style = TextStyle(
                            color = ColorProvider(AmberAccent),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(
                        text = state.nextUpTime,
                        style = TextStyle(
                            color = ColorProvider(TextSecondary),
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = state.nextUpTitle,
                    style = TextStyle(
                        color = ColorProvider(TextPrimary),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // --- Top Priority Task or Active Habit ---
        if (state.topTaskTitle != null) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ColorProvider(WidgetCardBackground.copy(alpha = 0.8f)))
                    .cornerRadius(10.dp)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯",
                    style = TextStyle(fontSize = 12.sp)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = state.topTaskTitle,
                    style = TextStyle(
                        color = ColorProvider(TextPrimary),
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        } else if (state.activeHabitTitle != null) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ColorProvider(WidgetCardBackground.copy(alpha = 0.8f)))
                    .cornerRadius(10.dp)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥",
                    style = TextStyle(fontSize = 12.sp)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = "${state.activeHabitTitle} (${state.activeHabitStreak} gün)",
                    style = TextStyle(
                        color = ColorProvider(TextPrimary),
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        // --- Action Buttons: Quick Capture + Focus Timer ---
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                text = "⚡ Brain Dump",
                onClick = actionStartActivity(launchQuickCaptureIntent),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorProvider(SageAccent),
                    contentColor = ColorProvider(Color.White)
                ),
                modifier = GlanceModifier.defaultWeight().height(36.dp)
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            Button(
                text = "⏱️ Odaklan",
                onClick = actionStartActivity(launchFocusTimerIntent),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorProvider(WidgetCardBorder),
                    contentColor = ColorProvider(TextPrimary)
                ),
                modifier = GlanceModifier.defaultWeight().height(36.dp)
            )
        }
    }
}
