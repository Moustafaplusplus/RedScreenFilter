package com.redscreenfilter.feature.settings.model

data class WellnessSettingsState(
    val isBatteryOptimizationEnabled: Boolean,
    val isLightSensorEnabled: Boolean,
    val lightSensitivityValue: Float,
    val isLightSensorLocked: Boolean,
    val isEyeStrainReminderEnabled: Boolean,
    val notificationStyle: String
)
