package com.nilian.app.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark Theme ColorScheme for Nilian
val DarkColorScheme = darkColorScheme(
    primary = SagePrimary,
    onPrimary = Color.White,
    primaryContainer = SageContainerDark,
    onPrimaryContainer = SageOnContainerDark,

    secondary = AmberSecondary,
    onSecondary = Color.Black,
    secondaryContainer = AmberContainerDark,
    onSecondaryContainer = AmberOnContainerDark,

    tertiary = SlateTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = SlateContainerDark,
    onTertiaryContainer = SlateContainerLight,

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,

    error = ColorError,
    onError = Color.White,
    errorContainer = Color(0xFF4A1817),
    onErrorContainer = Color(0xFFFFB4AB)
)

// Light Theme ColorScheme for Nilian
val LightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = Color.White,
    primaryContainer = SageContainerLight,
    onPrimaryContainer = SageOnContainerLight,

    secondary = AmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = AmberContainerLight,
    onSecondaryContainer = AmberOnContainerLight,

    tertiary = SlateTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SlateContainerLight,
    onTertiaryContainer = SlateTertiaryDark,

    background = LightBackground,
    onBackground = LightTextPrimary,

    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,

    outline = LightBorder,
    outlineVariant = LightBorderSubtle,

    error = ColorError,
    onError = Color.White,
    errorContainer = ColorErrorContainer,
    onErrorContainer = Color(0xFF680003)
)

@Immutable
data class ExtendedColors(
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val conflict: Color,
    val conflictContainer: Color,
    val priorityHigh: Color,
    val priorityMedium: Color,
    val priorityLow: Color,
    val blockSleep: Color,
    val blockWorkout: Color,
    val blockStudy: Color,
    val blockDeepWork: Color,
    val blockRest: Color,
    val blockBuffer: Color,
    val flameGradientStart: Color,
    val flameGradientEnd: Color,
    val flameBackground: Color,
    val cardBorder: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        success = ColorSuccess,
        successContainer = ColorSuccessContainer,
        warning = ColorWarning,
        warningContainer = ColorWarningContainer,
        conflict = ColorConflict,
        conflictContainer = ColorConflictContainer,
        priorityHigh = PriorityHigh,
        priorityMedium = PriorityMedium,
        priorityLow = PriorityLow,
        blockSleep = BlockSleep,
        blockWorkout = BlockWorkout,
        blockStudy = BlockStudy,
        blockDeepWork = BlockDeepWork,
        blockRest = BlockRest,
        blockBuffer = BlockBuffer,
        flameGradientStart = FlameStart,
        flameGradientEnd = FlameEnd,
        flameBackground = FlameBgDark,
        cardBorder = DarkBorder
    )
}

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current

@Composable
fun NilianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default false to keep Nilian's signature sage aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) {
        ExtendedColors(
            success = ColorSuccess,
            successContainer = Color(0xFF163824),
            warning = ColorWarning,
            warningContainer = Color(0xFF3D2B11),
            conflict = ColorConflict,
            conflictContainer = Color(0xFF3E200C),
            priorityHigh = PriorityHigh,
            priorityMedium = PriorityMedium,
            priorityLow = PriorityLow,
            blockSleep = BlockSleep,
            blockWorkout = BlockWorkout,
            blockStudy = BlockStudy,
            blockDeepWork = BlockDeepWork,
            blockRest = BlockRest,
            blockBuffer = BlockBuffer,
            flameGradientStart = FlameStart,
            flameGradientEnd = FlameEnd,
            flameBackground = FlameBgDark,
            cardBorder = DarkBorder
        )
    } else {
        ExtendedColors(
            success = ColorSuccess,
            successContainer = ColorSuccessContainer,
            warning = ColorWarning,
            warningContainer = ColorWarningContainer,
            conflict = ColorConflict,
            conflictContainer = ColorConflictContainer,
            priorityHigh = PriorityHigh,
            priorityMedium = PriorityMedium,
            priorityLow = PriorityLow,
            blockSleep = BlockSleepLight,
            blockWorkout = BlockWorkoutLight,
            blockStudy = BlockStudyLight,
            blockDeepWork = BlockDeepWorkLight,
            blockRest = BlockRestLight,
            blockBuffer = BlockBufferLight,
            flameGradientStart = FlameStart,
            flameGradientEnd = FlameEnd,
            flameBackground = FlameBgLight,
            cardBorder = LightBorder
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NilianTypography,
            shapes = NilianShapes,
            content = content
        )
    }
}
