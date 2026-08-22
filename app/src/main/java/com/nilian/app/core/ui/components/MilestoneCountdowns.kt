package com.nilian.app.core.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilian.app.core.ui.theme.CardShapeLarge
import com.nilian.app.core.ui.theme.CardShapeMedium
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.core.ui.theme.PillShape
import com.nilian.app.core.ui.theme.SagePrimary
import com.nilian.app.core.ui.theme.extendedColors
import com.nilian.app.domain.model.GoalItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Visual styling configuration for Milestone Goal Countdown Cards.
 */
data class MilestoneTheme(
    val emoji: String,
    val icon: ImageVector,
    val accentColor: Color,
    val containerColor: Color
)

/**
 * Resolves a tailored emoji, icon, and accent color based on goal title and description keywords.
 * Defaults to dynamic sage/amber branding.
 */
@Composable
fun rememberMilestoneTheme(title: String, description: String? = null): MilestoneTheme {
    val text = remember(title, description) {
        "${title.lowercase(Locale.ROOT)} ${description?.lowercase(Locale.ROOT).orEmpty()}"
    }

    val extended = MaterialTheme.extendedColors

    return remember(text) {
        when {
            text.contains("sınav") || text.contains("exam") || text.contains("final") ||
            text.contains("okul") || text.contains("thesis") || text.contains("ders") || text.contains("vize") -> {
                MilestoneTheme(
                    emoji = "🎓",
                    icon = Icons.Outlined.School,
                    accentColor = Color(0xFF6366F1), // Indigo
                    containerColor = Color(0xFF6366F1).copy(alpha = 0.12f)
                )
            }
            text.contains("girişim") || text.contains("startup") || text.contains("launch") ||
            text.contains("kod") || text.contains("yazılım") || text.contains("app") || text.contains("proje") -> {
                MilestoneTheme(
                    emoji = "🚀",
                    icon = Icons.Outlined.RocketLaunch,
                    accentColor = Color(0xFFF97316), // Warm Flame Orange
                    containerColor = Color(0xFFF97316).copy(alpha = 0.12f)
                )
            }
            text.contains("spor") || text.contains("maraton") || text.contains("fitness") ||
            text.contains("koşu") || text.contains("workout") || text.contains("sağlık") -> {
                MilestoneTheme(
                    emoji = "🏃",
                    icon = Icons.Outlined.FitnessCenter,
                    accentColor = Color(0xFF10B981), // Emerald
                    containerColor = Color(0xFF10B981).copy(alpha = 0.12f)
                )
            }
            text.contains("fikir") || text.contains("idea") || text.contains("kitap") ||
            text.contains("öğren") || text.contains("read") || text.contains("yaratıcı") -> {
                MilestoneTheme(
                    emoji = "💡",
                    icon = Icons.Outlined.Lightbulb,
                    accentColor = Color(0xFFEAB308), // Muted Gold
                    containerColor = Color(0xFFEAB308).copy(alpha = 0.12f)
                )
            }
            text.contains("kariyer") || text.contains("iş") || text.contains("terfi") || text.contains("work") -> {
                MilestoneTheme(
                    emoji = "💼",
                    icon = Icons.Outlined.TrendingUp,
                    accentColor = Color(0xFF0EA5E9), // Sky Blue
                    containerColor = Color(0xFF0EA5E9).copy(alpha = 0.12f)
                )
            }
            text.contains("alışkanlık") || text.contains("meditasyon") || text.contains("huzur") || text.contains("mindful") -> {
                MilestoneTheme(
                    emoji = "🌿",
                    icon = Icons.Outlined.SelfImprovement,
                    accentColor = SagePrimary,
                    containerColor = SagePrimary.copy(alpha = 0.12f)
                )
            }
            else -> {
                MilestoneTheme(
                    emoji = "🎯",
                    icon = Icons.Filled.Flag,
                    accentColor = SagePrimary,
                    containerColor = SagePrimary.copy(alpha = 0.12f)
                )
            }
        }
    }
}

/**
 * High-impact milestone countdown card for Goals & Dashboard.
 */
@Composable
fun MilestoneCountdownCard(
    goal: GoalItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 260.dp,
    today: LocalDate = LocalDate.now()
) {
    val theme = rememberMilestoneTheme(title = goal.title, description = goal.description)
    val extended = MaterialTheme.extendedColors

    val daysRemaining = remember(goal.targetDate, today) {
        goal.targetDate?.let { ChronoUnit.DAYS.between(today, it) }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = goal.progressPercent.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "MilestoneProgressAnim"
    )

    val (badgeText, badgeBg, badgeColor) = remember(daysRemaining) {
        when {
            daysRemaining == null -> Triple("Süresiz Hedef", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
            daysRemaining < 0 -> Triple("${-daysRemaining} gün gecikti", extended.conflictContainer, extended.conflict)
            daysRemaining == 0L -> Triple("Son Gün: Bugün!", extended.flameBackground, extended.flameGradientStart)
            daysRemaining == 1L -> Triple("Yarın son gün", extended.warningContainer, extended.warning)
            daysRemaining <= 7L -> Triple("$daysRemaining gün kaldı ⚡", theme.containerColor, theme.accentColor)
            else -> Triple("$daysRemaining gün kaldı", theme.containerColor, theme.accentColor)
        }
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale("tr")) }

    CalmCard(
        modifier = modifier.width(width),
        shape = CardShapeLarge,
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = if (daysRemaining != null && daysRemaining <= 3) theme.accentColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        onClick = onClick,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Emoji / Icon Avatar + Days Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(theme.containerColor)
                        .border(1.dp, theme.accentColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = theme.emoji,
                        fontSize = 18.sp
                    )
                }

                // Days Remaining Pill
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(badgeBg)
                        .border(1.dp, badgeColor.copy(alpha = 0.35f), PillShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = badgeColor
                    )
                }
            }

            // Goal Title & Target Date
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (goal.targetDate != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = goal.targetDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "Sürekli Gelişim",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Progress Bar & Linked Items Summary
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "İlerleme",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = theme.accentColor
                    )
                }

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
            }

            // Bottom Counts: Tasks & Habits attached
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (goal.linkedTaskCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TaskAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${goal.linkedTaskCount} görev",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (goal.linkedHabitCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SelfImprovement,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${goal.linkedHabitCount} rutin",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Compact countdown badge for task headers, calendar blocks, or list items.
 */
@Composable
fun MilestoneCountdownBadge(
    targetDate: LocalDate?,
    title: String? = null,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now()
) {
    if (targetDate == null) return

    val days = remember(targetDate, today) { ChronoUnit.DAYS.between(today, targetDate) }
    val extended = MaterialTheme.extendedColors

    val (text, bgColor, textColor) = when {
        days < 0 -> Triple("${-days}g gecikti", extended.conflictContainer, extended.conflict)
        days == 0L -> Triple("Bugün", extended.flameBackground, extended.flameGradientStart)
        days == 1L -> Triple("Yarın", extended.warningContainer, extended.warning)
        days <= 7L -> Triple("${days}g kaldı", SagePrimary.copy(alpha = 0.15f), SagePrimary)
        else -> Triple("${days}g", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Row(
        modifier = modifier
            .clip(PillShape)
            .background(bgColor)
            .border(0.8.dp, textColor.copy(alpha = 0.35f), PillShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Timer,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            ),
            color = textColor
        )
    }
}

/**
 * Horizontal Carousel displaying active milestone goal countdowns.
 */
@Composable
fun MilestoneCountdownCarousel(
    goals: List<GoalItem>,
    onGoalClick: (GoalItem) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Yaklaşan Kilometre Taşları",
    subtitle: String? = "Vizyonunuza kalan süre ve ilerleme",
    onSeeAllClick: (() -> Unit)? = null
) {
    if (goals.isEmpty()) return

    // Sort active goals by nearest target date first
    val sortedGoals = remember(goals) {
        goals.filter { !it.isArchived }
            .sortedWith(
                compareBy(
                    { it.targetDate == null },
                    { it.targetDate }
                )
            )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🎯",
                        fontSize = 15.sp
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (onSeeAllClick != null) {
                Text(
                    text = "Tümü",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SagePrimary
                    ),
                    modifier = Modifier
                        .clip(PillShape)
                        .clickable { onSeeAllClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
        ) {
            items(sortedGoals, key = { it.id }) { goal ->
                MilestoneCountdownCard(
                    goal = goal,
                    onClick = { onGoalClick(goal) }
                )
            }
        }
    }
}

// =========================================================================
// Previews
// =========================================================================

@Preview(name = "Milestone Countdowns Light", showBackground = true)
@Composable
private fun MilestoneCountdownsLightPreview() {
    NilianTheme(darkTheme = false) {
        Surface(modifier = Modifier.padding(16.dp)) {
            val sampleGoals = listOf(
                GoalItem(
                    id = 1,
                    title = "Bahar Dönemi Final Sınavları",
                    description = "Tüm derslerden A almak",
                    targetDate = LocalDate.now().plusDays(12),
                    progressPercent = 0.72f,
                    linkedTaskCount = 8,
                    linkedHabitCount = 2
                ),
                GoalItem(
                    id = 2,
                    title = "Nilian MVP Lansmanı",
                    description = "İlk 100 beta kullanıcısına açılış",
                    targetDate = LocalDate.now().plusDays(28),
                    progressPercent = 0.50f,
                    linkedTaskCount = 14,
                    linkedHabitCount = 3
                ),
                GoalItem(
                    id = 3,
                    title = "10K Koşu Maratonu",
                    description = "Haftalık 3 antrenman ve sağlıklı beslenme",
                    targetDate = LocalDate.now().plusDays(3),
                    progressPercent = 0.85f,
                    linkedTaskCount = 4,
                    linkedHabitCount = 1
                )
            )

            MilestoneCountdownCarousel(
                goals = sampleGoals,
                onGoalClick = {}
            )
        }
    }
}

@Preview(name = "Milestone Countdowns Dark", showBackground = true)
@Composable
private fun MilestoneCountdownsDarkPreview() {
    NilianTheme(darkTheme = true) {
        Surface(modifier = Modifier.padding(16.dp)) {
            val sampleGoals = listOf(
                GoalItem(
                    id = 1,
                    title = "Bahar Dönemi Final Sınavları",
                    description = "Tüm derslerden A almak",
                    targetDate = LocalDate.now().plusDays(12),
                    progressPercent = 0.72f,
                    linkedTaskCount = 8,
                    linkedHabitCount = 2
                ),
                GoalItem(
                    id = 2,
                    title = "Nilian MVP Lansmanı",
                    description = "İlk 100 beta kullanıcısına açılış",
                    targetDate = LocalDate.now().plusDays(28),
                    progressPercent = 0.50f,
                    linkedTaskCount = 14,
                    linkedHabitCount = 3
                )
            )

            MilestoneCountdownCarousel(
                goals = sampleGoals,
                onGoalClick = {}
            )
        }
    }
}
