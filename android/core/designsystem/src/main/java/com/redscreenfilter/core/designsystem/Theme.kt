package com.redscreenfilter.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary = RsfColors.ScarletCore,
    onPrimary = RsfColors.TextPrimaryOnDark,
    secondary = RsfColors.EmberOrange,
    onSecondary = RsfColors.TextPrimaryOnDark,
    tertiary = RsfColors.PulseMagenta,
    onTertiary = RsfColors.TextPrimaryOnDark,
    surface = RsfColors.CharcoalSurface,
    onSurface = RsfColors.TextPrimaryOnDark,
    surfaceVariant = RsfColors.CardOverlay,
    onSurfaceVariant = RsfColors.TextSecondaryOnDark,
    error = RsfColors.Error,
    onError = RsfColors.OnError,
    errorContainer = RsfColors.ErrorContainer,
    onErrorContainer = RsfColors.OnErrorContainer,
    background = RsfColors.CarbonBlack,
    onBackground = RsfColors.TextPrimaryOnDark
)

private val LightScheme = lightColorScheme(
    primary = RsfColors.ScarletCore,
    onPrimary = RsfColors.TextPrimaryOnDark,
    secondary = RsfColors.EmberOrange,
    onSecondary = RsfColors.TextPrimaryOnDark,
    tertiary = RsfColors.PulseMagenta,
    onTertiary = RsfColors.TextPrimaryOnDark,
    surface = RsfColors.CharcoalSurface,
    onSurface = RsfColors.TextPrimaryOnDark,
    error = RsfColors.Error,
    onError = RsfColors.OnError,
    errorContainer = RsfColors.ErrorContainer,
    onErrorContainer = RsfColors.OnErrorContainer,
    background = RsfColors.CarbonBlack,
    onBackground = RsfColors.TextPrimaryOnDark,
    surfaceVariant = RsfColors.CardOverlay,
    onSurfaceVariant = RsfColors.TextSecondaryOnDark
)

@Composable
fun RedScreenFilterTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkScheme else LightScheme

    MaterialTheme(
        colorScheme = colors,
        typography = RsfTypography,
        content = content
    )
}
