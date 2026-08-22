package com.nilian.app.presentation.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilian.app.core.ui.theme.SagePrimary

enum class NilianDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val showInNavigationBar: Boolean = true
) {
    LOCK("lock", "Lock", Icons.Filled.Lock, Icons.Outlined.Lock, showInNavigationBar = false),
    TODAY("today", "Today", Icons.Filled.Today, Icons.Outlined.Today, showInNavigationBar = true),
    TIMELINE("timeline", "Timeline", Icons.Filled.DateRange, Icons.Outlined.DateRange, showInNavigationBar = true),
    TASKS("tasks", "Tasks", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt, showInNavigationBar = true),
    HABITS("habits", "Habits", Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement, showInNavigationBar = true),
    GOALS("goals", "Goals", Icons.Filled.Flag, Icons.Outlined.Flag, showInNavigationBar = true),
    SETTINGS("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings, showInNavigationBar = true);

    companion object {
        val navItems = values().filter { it.showInNavigationBar }
    }
}

@Composable
fun NilianAdaptiveNavigationScaffold(
    currentDestination: NilianDestination,
    onNavigateToDestination: (NilianDestination) -> Unit,
    isExpandedScreen: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (isExpandedScreen) {
        // Tablet / Dual-Pane Layout with Side NavigationRail
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                header = {
                    Box(
                        modifier = Modifier
                            .padding(top = 20.dp, bottom = 24.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SagePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌿",
                            fontSize = 22.sp
                        )
                    }
                }
            ) {
                NilianDestination.navItems.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationRailItem(
                        selected = isSelected,
                        onClick = { onNavigateToDestination(destination) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = SagePrimary,
                            selectedTextColor = SagePrimary,
                            indicatorColor = SagePrimary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                content()
            }
        }
    } else {
        // Mobile / Compact Layout with Bottom NavigationBar
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (currentDestination.showInNavigationBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 0.dp
                    ) {
                        NilianDestination.navItems.forEach { destination ->
                            val isSelected = currentDestination == destination
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { onNavigateToDestination(destination) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SagePrimary,
                                    selectedTextColor = SagePrimary,
                                    indicatorColor = SagePrimary.copy(alpha = 0.15f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                content()
            }
        }
    }
}
