package com.redscreenfilter.feature.settings.model

data class AutomationSettingsState(
    val isSchedulingEnabled: Boolean,
    val scheduleStartLabel: String,
    val scheduleEndLabel: String,
    val isLocationSchedulingEnabled: Boolean,
    val locationOffsetMinutes: Int,
    val sunsetTime: String?,
    val sunriseTime: String?
)
