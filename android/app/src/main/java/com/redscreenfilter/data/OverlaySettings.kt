package com.redscreenfilter.data

/**
 * OverlaySettings Data Model
 * Represents the complete state of overlay configuration
 */
data class OverlaySettings(
    val isEnabled: Boolean = false,
    val opacity: Float = 0.5f,
    val scheduleEnabled: Boolean = false,
    val scheduleStartTime: String = "21:00", // HH:mm format
    val scheduleEndTime: String = "07:00",
    val useAmbientLight: Boolean = false,
    val useLocationSchedule: Boolean = false,
    val colorVariant: String = "red_standard"
)
