package com.nilian.app.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.Priority
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// =========================================================================
// 1. CalmCard: Subtle border, soft surface elevation, rounded 16/24 dp
// =========================================================================
@Composable
fun CalmCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShapeMedium,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = backgroundColor,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
    val border = BorderStroke(width = 1.dp, color = borderColor)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = cardColors,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = border
        ) {
            Box(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = cardColors,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = border
        ) {
            Box(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    }
}

// =========================================================================
// 2. StreakFlameBadge: Active streak with clean flame icon and badge
// =========================================================================
@Composable
fun StreakFlameBadge(
    streakCount: Int,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = streakCount > 0,
    showBestStreak: Boolean = false,
    bestStreak: Int = 0
) {
    val extended = MaterialTheme.extendedColors
    val badgeBg = if (isHighlighted) {
        extended.flameBackground
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isHighlighted) {
        extended.flameGradientStart
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .clip(PillShape)
            .background(badgeBg)
            .border(
                width = 1.dp,
                color = if (isHighlighted) extended.flameGradientStart.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = PillShape
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.LocalFireDepartment,
            contentDescription = "Streak Flame",
            tint = if (isHighlighted) extended.flameGradientStart else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$streakCount ${if (streakCount == 1) "day" else "days"}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
        if (showBestStreak && bestStreak > streakCount) {
            Text(
                text = "• Best: $bestStreak",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

// =========================================================================
// 3. CircularDayProgress: Circular progress with task percentage & calm text
// =========================================================================
@Composable
fun CircularDayProgress(
    progressPercent: Float, // 0.0f to 1.0f
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 8.dp,
    primaryColor: Color = SagePrimary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    label: String = "Tasks"
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "DayProgressAnim"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - strokePx, size.toPx() - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active Progress Arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val percentageInt = (animatedProgress * 100).toInt()
            Text(
                text = "$percentageInt%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$completedCount/$totalCount $label",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =========================================================================
// 4. NilianTopAppBar: Clean header with date/title, subtitle, actions
// =========================================================================
@Composable
fun NilianTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
    showBottomDivider: Boolean = false
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (navigationIcon != null) {
                        navigationIcon()
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (actions != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        actions()
                    }
                }
            }

            if (showBottomDivider) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                )
            }
        }
    }
}

// =========================================================================
// 5. ConflictAlertBanner: Calm warning banner for overlapping events
// =========================================================================
@Composable
fun ConflictAlertBanner(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Schedule Collision",
    onActionClick: (() -> Unit)? = null,
    actionLabel: String = "Review",
    onDismiss: (() -> Unit)? = null
) {
    val extended = MaterialTheme.extendedColors

    CalmCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = extended.conflictContainer,
        borderColor = extended.conflict.copy(alpha = 0.5f),
        shape = CardShapeMedium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(extended.conflict.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Conflict",
                    tint = extended.conflict,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = extended.conflict
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (onActionClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onActionClick,
                    shape = PillShape,
                    border = BorderStroke(1.dp, extended.conflict),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = extended.conflict
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

// =========================================================================
// 6. TimeSlotItem: Time blocking row component
// =========================================================================
@Composable
fun TimeSlotItem(
    title: String,
    startTime: LocalTime,
    endTime: LocalTime,
    blockType: BlockType,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    hasConflict: Boolean = false,
    isCurrent: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val extended = MaterialTheme.extendedColors
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val blockColor = when (blockType) {
        BlockType.SLEEP -> extended.blockSleep
        BlockType.WORKOUT -> extended.blockWorkout
        BlockType.STUDY -> extended.blockStudy
        BlockType.DEEP_WORK -> extended.blockDeepWork
        BlockType.REST -> extended.blockRest
        BlockType.BUFFER, BlockType.GENERAL, BlockType.OTHER -> extended.blockBuffer
    }

    val cardBorder = if (hasConflict) {
        extended.conflict
    } else if (isCurrent) {
        SagePrimary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    CalmCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = cardBorder,
        onClick = onClick,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Column
            Column(
                modifier = Modifier.width(64.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = startTime.format(timeFormatter),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = endTime.format(timeFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Category Stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(blockColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(SagePrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NOW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = SagePrimary
                            )
                        }
                    }
                }

                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Block Type Badge / Conflict Icon
            if (hasConflict) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Conflict",
                    tint = extended.conflict,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(blockColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = blockType.label.split("/").first().trim(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = blockColor
                    )
                }
            }
        }
    }
}

// =========================================================================
// 7. EmptyStateWidget: Minimal graphic/text for empty states
// =========================================================================
@Composable
fun EmptyStateWidget(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.DateRange,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        if (!actionLabel.isNullOrBlank() && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onActionClick,
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        }
    }
}

// =========================================================================
// Additional Reusable Components (Badges, Buttons, Inputs, Indicators)
// =========================================================================

@Composable
fun PriorityBadge(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    val extended = MaterialTheme.extendedColors
    val (color, label) = when (priority) {
        Priority.HIGH -> extended.priorityHigh to "High"
        Priority.MEDIUM -> extended.priorityMedium to "Medium"
        Priority.LOW -> extended.priorityLow to "Low"
    }

    Box(
        modifier = modifier
            .clip(PillShape)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = color
        )
    }
}

@Composable
fun DayDotIndicator(
    dayLabel: String,
    isCompleted: Boolean,
    isTargetDay: Boolean = true,
    isToday: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extended = MaterialTheme.extendedColors
    val dotColor = when {
        isCompleted -> extended.success
        isToday -> SagePrimary.copy(alpha = 0.5f)
        isTargetDay -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                fontSize = 10.sp
            ),
            color = if (isToday) SagePrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(dotColor)
                .then(
                    if (isToday) Modifier.border(1.5.dp, SagePrimary, CircleShape) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun CalmTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    isError: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, style = MaterialTheme.typography.bodyMedium) },
        placeholder = placeholder?.let { { Text(text = it, style = MaterialTheme.typography.bodyMedium) } },
        singleLine = singleLine,
        maxLines = maxLines,
        isError = isError,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = CardShapeMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SagePrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            focusedLabelColor = SagePrimary,
            cursorColor = SagePrimary
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier.fillMaxWidth()
    )
}

// =========================================================================
// Previews
// =========================================================================

@Preview(name = "Components Dark Mode", showBackground = true)
@Composable
private fun ComponentsDarkPreview() {
    NilianTheme(darkTheme = true) {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                NilianTopAppBar(
                    title = "Saturday, 22 Aug",
                    subtitle = "All clear • 4 focus blocks planned"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularDayProgress(
                        progressPercent = 0.65f,
                        completedCount = 5,
                        totalCount = 8
                    )
                    StreakFlameBadge(streakCount = 14, showBestStreak = true, bestStreak = 21)
                }
                ConflictAlertBanner(
                    title = "2 Overlapping Blocks",
                    message = "Deep Work overlaps with Team Sync at 14:00"
                )
                TimeSlotItem(
                    title = "Deep Work / Sprint",
                    startTime = LocalTime.of(14, 0),
                    endTime = LocalTime.of(15, 30),
                    blockType = BlockType.DEEP_WORK,
                    subtitle = "Nilian Jetpack Compose UI",
                    isCurrent = true
                )
            }
        }
    }
}

@Preview(name = "Components Light Mode", showBackground = true)
@Composable
private fun ComponentsLightPreview() {
    NilianTheme(darkTheme = false) {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                NilianTopAppBar(
                    title = "Today's Focus",
                    subtitle = "3 tasks remaining"
                )
                EmptyStateWidget(
                    title = "No Tasks Remaining",
                    description = "Take a mindful break or add your next goal milestone.",
                    actionLabel = "Add Task",
                    onActionClick = {}
                )
            }
        }
    }
}
