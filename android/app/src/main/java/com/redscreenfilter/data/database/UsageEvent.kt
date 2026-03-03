package com.redscreenfilter.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * UsageEvent Entity
 * Represents a single usage event logged by the analytics system
 */
@Entity(tableName = "usage_events")
data class UsageEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val eventType: String, // "overlay_toggled", "settings_changed", "preset_applied"
    val overlayEnabled: Boolean,
    val opacity: Float,
    val preset: String = ""
)
