package com.nilian.app.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.nilian.app.core.ui.theme.AmberSecondary
import com.nilian.app.core.ui.theme.CardShapeMedium
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.core.ui.theme.PillShape
import com.nilian.app.core.ui.theme.PriorityLowContainer
import com.nilian.app.core.ui.theme.PriorityMediumContainer
import com.nilian.app.core.ui.theme.SagePrimary
import com.nilian.app.core.ui.theme.extendedColors
import java.util.Locale

/**
 * Time Budget Status enum determining color-coding and advice.
 */
enum class TimeBudgetStatus {
    BALANCED,
    OVER_ALLOCATED,
    SPACIOUS
}

/**
 * Formats minutes into human-readable hours format (e.g., 210m -> "3.5", 120m -> "2").
 */
fun formatMinutesToHours(minutes: Int): String {
    val hours = minutes / 60f
    return if (hours % 1.0f == 0.0f) {
        hours.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", hours)
    }
}

/**
 * TimeBudgetBanner: Visual capacity meter showing task workload vs. calendar free time gap.
 *
 * Requirements:
 * - Visual meter showing "Bugünkü Görevler: 3.5 Saat | Takvimdeki Boşluk: 4 Saat"
 * - Color-coded: Calm Sage if balanced, Soft Amber if over-allocated with gentle suggestion to reschedule.
 * - Material 3 styling, subtle animations, expandable advice chip.
 */
@Composable
fun TimeBudgetBanner(
    taskEstimatedMinutes: Int,
    calendarGapMinutes: Int,
    modifier: Modifier = Modifier,
    onRescheduleClick: (() -> Unit)? = null,
    onOptimizePlanClick: (() -> Unit)? = null,
    showDetailsInitially: Boolean = false
) {
    val extended = MaterialTheme.extendedColors
    var isExpanded by remember { mutableStateOf(showDetailsInitially) }

    // Determine status
    val status = when {
        calendarGapMinutes <= 0 && taskEstimatedMinutes > 0 -> TimeBudgetStatus.OVER_ALLOCATED
        taskEstimatedMinutes > calendarGapMinutes -> TimeBudgetStatus.OVER_ALLOCATED
        taskEstimatedMinutes == 0 -> TimeBudgetStatus.SPACIOUS
        else -> TimeBudgetStatus.BALANCED
    }

    val isOverAllocated = status == TimeBudgetStatus.OVER_ALLOCATED
    val excessMinutes = (taskEstimatedMinutes - calendarGapMinutes).coerceAtLeast(0)

    // Colors
    val statusAccentColor by animateColorAsState(
        targetValue = if (isOverAllocated) AmberSecondary else SagePrimary,
        animationSpec = tween(durationMillis = 400),
        label = "TimeBudgetAccent"
    )

    val containerBgColor by animateColorAsState(
        targetValue = if (isOverAllocated) {
            PriorityMediumContainer.copy(alpha = 0.35f)
        } else {
            PriorityLowContainer.copy(alpha = 0.35f)
        },
        animationSpec = tween(durationMillis = 400),
        label = "TimeBudgetBg"
    )

    val borderStrokeColor by animateColorAsState(
        targetValue = if (isOverAllocated) {
            AmberSecondary.copy(alpha = 0.5f)
        } else {
            SagePrimary.copy(alpha = 0.35f)
        },
        animationSpec = tween(durationMillis = 400),
        label = "TimeBudgetBorder"
    )

    // Calculation for visual progress bar (Task load vs capacity)
    val capacityRatio = if (calendarGapMinutes > 0) {
        (taskEstimatedMinutes.toFloat() / calendarGapMinutes.toFloat()).coerceIn(0f, 1.5f)
    } else if (taskEstimatedMinutes > 0) {
        1.2f
    } else {
        0f
    }

    val animatedBarProgress by animateFloatAsState(
        targetValue = (capacityRatio.coerceIn(0f, 1f)),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "CapacityRatioAnim"
    )

    val taskHoursStr = formatMinutesToHours(taskEstimatedMinutes)
    val gapHoursStr = formatMinutesToHours(calendarGapMinutes)

    CalmCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        backgroundColor = containerBgColor,
        borderColor = borderStrokeColor,
        shape = CardShapeMedium,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Status icon + Text indicator + Expand toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(statusAccentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOverAllocated) Icons.Filled.Warning else Icons.Outlined.HourglassBottom,
                            contentDescription = "Time Budget Status",
                            tint = statusAccentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Zaman Bütçesi & Kapasite",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = statusAccentColor
                        )
                        Text(
                            text = "Bugünkü Görevler: $taskHoursStr Saat | Takvimdeki Boşluk: $gapHoursStr Saat",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Badge Status
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(statusAccentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isOverAllocated) "Aşırı Yük" else "Dengeli",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = statusAccentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Progress Meter
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Doluluk Oranı",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(capacityRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusAccentColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedBarProgress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(statusAccentColor)
                    )
                }
            }

            // Expandable Suggestion & Action Area
            AnimatedVisibility(visible = isExpanded || isOverAllocated) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    val suggestionText = if (isOverAllocated) {
                        "Gününüz biraz yoğun görünüyor (${excessMinutes} dk fazla planlanmış). Zihinsel dinginliğinizi korumak için 1-2 görevi yarına ertelemeyi veya sürelerini kısaltmayı düşünebilirsiniz."
                    } else if (status == TimeBudgetStatus.BALANCED) {
                        "Harika denge! Bugünkü görevleriniz gün içindeki serbest zaman pencerelerine rahatça sığıyor. Acele etmeden odaklanabilirsiniz."
                    } else {
                        "Bugün için geniş serbest zamanınız var. Dinlenebilir, derin düşüncelere dalabilir veya yeni hedefler belirleyebilirsiniz."
                    }

                    Text(
                        text = suggestionText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    if (isOverAllocated && onRescheduleClick != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = onRescheduleClick,
                                shape = PillShape,
                                border = BorderStroke(1.dp, AmberSecondary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberSecondary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = "Görevleri Yeniden Planla",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// Previews
// =========================================================================

@Preview(name = "TimeBudgetBanner Balanced - Dark", showBackground = true)
@Composable
private fun TimeBudgetBannerBalancedDarkPreview() {
    NilianTheme(darkTheme = true) {
        Surface(modifier = Modifier.padding(16.dp)) {
            TimeBudgetBanner(
                taskEstimatedMinutes = 210, // 3.5 hrs
                calendarGapMinutes = 240,   // 4.0 hrs
                showDetailsInitially = true
            )
        }
    }
}

@Preview(name = "TimeBudgetBanner OverAllocated - Light", showBackground = true)
@Composable
private fun TimeBudgetBannerOverAllocatedLightPreview() {
    NilianTheme(darkTheme = false) {
        Surface(modifier = Modifier.padding(16.dp)) {
            TimeBudgetBanner(
                taskEstimatedMinutes = 330, // 5.5 hrs
                calendarGapMinutes = 180,   // 3.0 hrs
                showDetailsInitially = true,
                onRescheduleClick = {}
            )
        }
    }
}
