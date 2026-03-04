package com.redscreenfilter.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class RsfSemanticColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color
)

object RsfTheme {
    val spacing: RsfSpacing = RsfSpacing
    val radius: RsfRadius = RsfRadius
    val elevation: RsfElevation = RsfElevation
    val border: RsfBorder = RsfBorder

    val typography
        @Composable
        get() = MaterialTheme.typography

    val colors: RsfSemanticColors
        @Composable
        get() {
            val scheme = MaterialTheme.colorScheme
            return RsfSemanticColors(
                primary = scheme.primary,
                onPrimary = scheme.onPrimary,
                secondary = scheme.secondary,
                onSecondary = scheme.onSecondary,
                tertiary = scheme.tertiary,
                onTertiary = scheme.onTertiary,
                surface = scheme.surface,
                onSurface = scheme.onSurface,
                surfaceVariant = scheme.surfaceVariant,
                onSurfaceVariant = scheme.onSurfaceVariant,
                error = scheme.error,
                onError = scheme.onError,
                errorContainer = scheme.errorContainer,
                onErrorContainer = scheme.onErrorContainer
            )
        }
}
