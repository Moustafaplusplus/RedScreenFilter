package com.redscreenfilter.feature.settings.model

data class BrightnessSettingsState(
    val brightness: Float,
    val brightnessPercentage: Int,
    val isExtraDimEnabled: Boolean,
    val extraDimIntensity: Float,
    val extraDimIntensityPercentage: Int
)
